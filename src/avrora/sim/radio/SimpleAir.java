package avrora.sim.radio;

import avrora.sim.mcu.Microcontroller;
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.util.GlobalClock;
import avrora.sim.util.GlobalQueue;

import java.util.*;

/**
 * Very simple implementation of radio air. It assumes a lossless environment where all radios are able to communicate
 * with each other. This simple air is blind to the frequencies used in transmission (i.e. it assumes that all
 * frequencies are really the same).
 *
 * This class should provide the proper scheduling policy with respect to threads that
 * more complicated radio implementations
 *
 * @author Daniel Lee
 */
public class SimpleAir implements RadioAir {

    public final static SimpleAir simpleAir;

    static {
        simpleAir = new SimpleAir();
    }

    public boolean messageInAir() {
        return !messages.isEmpty();
        //return false;
    }


    protected final EnvironmentQueue globalQueue;

    protected final LinkedList messages;

    protected final HashSet radios;

    private class DebugSet extends LinkedList {

        public boolean add(Object o) {
            boolean val = super.add(o);
            System.err.println("Message size (add) " + size() + " " + val);
            return val;
        }

        public boolean remove(Object o) {
            boolean val = super.remove(o);
            System.err.println("Message size (remove) " + size() + " " + val);
            return val;
        }
    }

    private boolean scheduledGlobalMeet;

    /** The amount of cycles it takes for one byte to be sent.*/
    public final long bytePeriod = Radio.TRANSFER_TIME;
    public final long bitPeriod = 763;
    public final double transferTime = .8332651000300227;

    private /* final */ Radio.RadioPacket radioStatic;

    public SimpleAir() {
        radios = new HashSet();
        messages = new DebugSet();
        globalQueue = new EnvironmentQueue(bytePeriod);
    }

    public synchronized void addRadio(Radio r) {
        radios.add(r);
        globalQueue.add(r.getSimulatorThread());

    }

    public synchronized void removeRadio(Radio r) {
        radios.remove(r);
    }

    public synchronized void transmit(Radio r, Radio.RadioPacket f) {
        messages.addLast(f);
        System.err.println("Air.transmit() " + messages.size());

        if (!scheduledGlobalMeet) {
            scheduledGlobalMeet = true;
            //System.err.println("Scheduling... ");
            globalQueue.addTimerEvent(new ScheduleDelivery(f), 1);
        }
    }

    /** The event entered into the GLOBAL event queue, which will schedule synchronization
     * events for individual nodes.*/
    protected class ScheduleDelivery implements Simulator.Event {

        Radio.RadioPacket packet;

        ScheduleDelivery(Radio.RadioPacket f) {
            this.packet = f;
        }

        public void fire() {
            // Schedule local events.
            //double delay = 1.0; // TODO: calculate delay properly
            long globalTime = (globalQueue.getCount()) * Radio.TRANSFER_TIME;
            long delay = Radio.TRANSFER_TIME - (globalTime - packet.origination.longValue());

            System.err.println("Delay " + delay + ", Global Time: " + globalTime +
                    " , or: " + packet.origination.longValue() + ", dif: " + (globalTime - packet.origination.longValue()));
            /*
                delay = TXTime - (GT - LT); if GT is ahead of LT when this is fired

            */
            globalQueue.addDeliveryMeet(delay);
            scheduledGlobalMeet = false;
        }

    }


    protected class EnvironmentQueue extends GlobalQueue {
        protected EnvironmentQueue(long p) {
            super(p);
        }

        protected class DeliveryMeet extends LocalMeet {


            DeliveryMeet(Simulator sim, long scale, long delay) {
                super(sim, bytePeriod, delay);      // TODO cleanup
                id = "DEL";
            }

            public void action() {
                // TODO: Implement the action of this...
                /*
                    What am I doing here?
                    I am calculating the waveform.
                    Then, I am deliverying the waveform.
                    Need a sense of time...
                 */

                //System.err.println("D");
                long currentTime = simulator.getState().getCycles();
                deliverWaveForm(currentTime);

            }
        }

        protected class DeliveryMeetFactory implements LocalMeetFactory {
            public LocalMeet produce(Simulator s, long scale, long delay) { // TODO: cleanup
                return new DeliveryMeet(s, scale, delay);     // TODO: cleanup
            }
        }

        protected void addDeliveryMeet(long delay) {
            addLocalMeet(new DeliveryMeetFactory(), bytePeriod, delay);
        }
    }

    /* Conjecture: I can make this the differentiating point of an Air implementation.
     * Specifically, I can allow more complicated delivery mechanisms to simply be
     * derived from overriding this method. */
    protected synchronized void deliverWaveForm(long time) {
        // in order to calculate the radiopacket
        // grab all frames currently in the air
        // merge any that conflict, lower RSSI
        // deliver calculated form to member radios..

        System.err.println("Delivering... " + messages.size());

        if (messages.isEmpty()) {
            System.err.println("Empty queue. Blah");
            return;
        }

        Iterator packetIterator = messages.listIterator();
        Radio.RadioPacket packet = (Radio.RadioPacket) packetIterator.next();

        long del = packet.delivery.longValue();
        long diff = packet.delivery.longValue() - time;
        int tolerance = 3;

        packetIterator.remove();  // TODO: figure out right palce for this

        if (diff <= tolerance && -tolerance <= diff) {
            System.err.println("Safe time " + del + " " + time + " " + (del - time));
            // This packet is meant to be delivered now. We must determine whether any
            // other packets conflict.

            // this set is nonempty if there are any overlapping packets.

            //if(!possibleConflicts.isEmpty()) {
            if (!messages.isEmpty()) {
                //HashSet possibleConflicts = messages;//messages.headSet(new Radio.RadioPacket((byte)0x00, time, time));
                System.err.println("Overlap...");

                // conflicts exist..
                // we must perform a waveform calculation...

                TreeSet mappedSet = new TreeSet();

                long base = packet.origination.longValue() / bitPeriod;
                //Iterator packetIterator = possibleConflicts.iterator();

                while (packetIterator.hasNext()) {
                    Radio.RadioPacket current = (Radio.RadioPacket) packetIterator.next();
                    mappedSet.add(new Radio.RadioPacket(current.data,
                            current.origination.longValue() / bitPeriod - base,
                            current.delivery.longValue() / bitPeriod - base));

                    packetIterator.remove();
                    //messages.remove(current);
                }

                byte acc = packet.data;

                packetIterator = mappedSet.iterator();

                while (packetIterator.hasNext()) {
                    Radio.RadioPacket current = (Radio.RadioPacket) packetIterator.next();

                    // TODO: fix shift direction based on whether it is MSB or LSB first
                    acc |= (byte) (current.data >> current.delivery.intValue());
                }


                //messages.remove(packet);

                packet = new Radio.RadioPacket(acc, packet.origination.longValue(),
                        packet.delivery.longValue());
                packet.strength = 0;
            }

            System.err.println("Delivered data " + Integer.toHexString(0xff & packet.data));


            Iterator radioIterator = radios.iterator();

            while (radioIterator.hasNext()) {
                Radio radio = (Radio) radioIterator.next();
                radio.receive(packet);
            }


        } else if (time < del) {

            System.err.println("Unsafe time (early) del: " + del + ", cur: " + time + " " + (del - time));

            //
            // TODO: Do  I need to resched something?
            return;
        } else {
            // del < time. early.
            System.err.println("Unsafe time (late) del: " + del + ", cur: " + time + " " + (del - time));

            //globalQueue.addDeliveryMeet(diff);
            // TODO: schedule next delivery. insert mangled packet into stream.
        }


    }


}
