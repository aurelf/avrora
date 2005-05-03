/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package avrora.actions;

import avrora.Main;
import avrora.Avrora;
import avrora.Defaults;
import avrora.core.Program;
import avrora.gui.*;
import avrora.monitors.*;
import avrora.sim.GenInterpreter;
import avrora.sim.InterpreterFactory;
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.radio.SimpleAir;
import avrora.util.*;

import javax.swing.*;
import java.util.*;

//The VisualAction provides a link between the GUI and the simulator.
//It physically starts the GUI and also dispatchs simulator threads

public class VisualAction extends SimAction {


    public static Hashtable nodeTable; //everyone can access the hashtable of nodes
    //corresponding to the data about all the nodes
    //in the sim

    static {
        nodeTable = new Hashtable();
    }

    Program program;  //just reuse program each time we start a sim

    public LinkedList visualOptions; //list of all options that can be done visually


    ClassMap monitorMap;
    ClassMap globalMonitorMap;  //this holds a list of monitors that are "global"
    //a global monitor is a monitor that is added to
    //every node, but only has one display panel
    //note that SimAction defines a "local" monitorMap

    //Here is the collection that holds all the sim nodes
    private Vector theNodes;
    public int currentNID;

    boolean simisrunning; //true if the sim is currently running

    public AvroraGui app; //allows us to access GUI

    SimTimeEvents theEvents; //all the events for controlling sim time (e.g. pausing)

    long startms, endms;

    //Here are our events for controlling the sim time
    /*
private PauseEvent thePauseEvent;
private SpeedChangeEventAndProbe theSpeedChangeEventAndProbe;
private RealtimeEvent theRealtimeEvent;
private SingleStepProbe theSingleStepProbe;
     */

    public static final String HELP = "The \"visual\" action launches a GUI from which you can start simulations.";

    public final Option.Long RANDOMSEED = newOption("random-seed", 0, "This option is used to seed a pseudo-random number generator used in the " + "simulation. If this option is set to non-zero, then its value is used as " + "the seed for reproducible simulation results. If this option is not set, " + "those parts of simulation that rely on random numbers will have seeds " + "chosen based on system parameters that vary from run to run.");
    public final Option.Interval RANDOM_START = newOption("random-start", 0, 0, "This option causes the simulator to insert a random delay before starting " + "each node in order to prevent artificial cycle-level synchronization. The " + "starting delay is pseudo-randomly chosen with uniform distribution over the " + "specified interval, which is measured in clock cycles. If the \"random-seed\" " + "option is set to a non-zero value, then its value is used as the seed to the " + "pseudo-random number generator.");
    public final Option.Long STAGGER_START = newOption("stagger-start", 0, "This option causes the simulator to insert a progressively longer delay " + "before starting each node in order to avoid artificial cycle-level " + "synchronization between nodes. The starting times are staggered by the specified number " + "of clock cycles. For example, if this option is given the " + "value X, then node 0 will start at time 0, node 1 at time 1*X, node 2 at " + "time 2*X, etc.");

    public VisualAction() {
        super("visual", HELP);

        monitorMap = new ClassMap("Monitor", MonitorFactory.class);  //for regular monitors
        globalMonitorMap = new ClassMap("Monitor", MonitorFactory.class);  //for global monitors

        simisrunning = false;

        //If adding a new local visual monitor, just add an instance
        //of it here
        addNewMonitorType("PC Monitor", new VisualPCMonitor());
        addNewMonitorType("Register Monitor", new VisualRegMonitor());
        addNewMonitorType("Radio Monitor", new VisualRadioMonitor());
        /*INSERT NEW MONITORS HERE*/

        //these can be used to test the GUI...there are no visuals for them
        addNewMonitorType("Profile", new ProfileMonitor());
        addNewMonitorType("Memory", new MemoryMonitor());
        addNewMonitorType("Stack", new StackMonitor());

        //Here are a list of options that
        //the visual can process
        visualOptions = new LinkedList();
        visualOptions.add(SECONDS);
        visualOptions.add(PLATFORM);
        //visualOptions.add(REALTIME);  //deprecated - added into ManageSimTime toolbar now
        /*INSERT NEW OPTIONS HERE*/

        //Init the list of sim nodes
        theNodes = new Vector();
        currentNID = 0;

        //init the sim timing events/probes
        /*
                thePauseEvent = new PauseEvent();
                theSpeedChangeEventAndProbe = new SpeedChangeEventAndProbe();
                theRealtimeEvent = new RealtimeEvent();
                theSingleStepProbe = new SingleStepProbe();
        */

    }

    /*SimAction has the same function, but its private */
    private void addNewMonitorType(String n, MonitorFactory f) {
        monitorMap.addClass(n, f.getClass());
    }

    private void addNewGlobalMonitorType(String n, MonitorFactory f) {
        globalMonitorMap.addClass(n, f.getClass());
    }

    //The GUI needs to be able to access the classmap of monitors
    public ClassMap getMonitorMap() {
        return monitorMap;
    }

    //The following functions allow the GUI to interact with
    //the vector of SimNodes

    public void addNode(SimNode theNode) {
        theNode.NID = currentNID;
        currentNID++;
        theNodes.add(theNode);  //add to our internal vector, maybe if I have time
        //I'd replace everything with the hashtable and remove this
        nodeTable.put(new Integer(theNode.NID), theNode);
    }

    //remove node with NID
    public void removeNode(int NID) {
        //Find the node with that NID
        for (int i = 0; i < theNodes.size(); i++) {
            if (((SimNode) theNodes.elementAt(i)).NID == NID) {
                theNodes.removeElementAt(i);
                return;
            }

        }

        nodeTable.remove(new Integer(NID)); //remove it from our hash table
    }

    public Vector getNodes() {
        return theNodes;
    }

    //This function returns true if was able to add the monitor
    //it returns false if it was unable to add the node because it was already there
    public boolean addMonitorToNode(String monitorName, int NID) {
        //Find the node with that NID
        for (int i = 0; i < theNodes.size(); i++) {
            if (((SimNode) theNodes.elementAt(i)).NID == NID) {
                SimNode currentNode = ((SimNode) theNodes.elementAt(i));
                //Found the node, now let's see if the monitor was already
                //there
                for (int j = 0; j < currentNode.monitors.size(); j++) {
                    if (currentNode.monitors.elementAt(j).equals(monitorName)) {
                        return false; //monitor already existed
                    }
                }
                //it did not find the monitor, therefore we add it
                currentNode.monitors.add(monitorName);
                return true;
            }
        }
        return false;
    }

    public void registerMonitorPanelsWithNode(JPanel monitorPanel, JPanel optionPanel, String monitorName, int NID) {
        //Find the node with that NID
        for (int i = 0; i < theNodes.size(); i++) {
            if (((SimNode) theNodes.elementAt(i)).NID == NID) {
                SimNode currentNode = ((SimNode) theNodes.elementAt(i));
                //Found the node, now let's register the panel
                currentNode.monitorPanels.put(monitorName, monitorPanel);
                currentNode.monitorOptionsPanels.put(monitorName, optionPanel);
            }
        }
    }

    //This function will take a monitor chalkboard and return it's corresponding
    //panel (or null if it can't find it)
    public JPanel getOptionsFromMonitor(JPanel monitorPanel) {
        //we have to look through all nodes
        for (Enumeration e = theNodes.elements(); e.hasMoreElements();) {
            SimNode currentNode = (SimNode) e.nextElement();
            if (currentNode.monitorPanels.contains(monitorPanel)) {
                //then we found it in this node,
                //but we have to "back up" and find the key (name of monitor)
                //that corresponds to the value found (the chalkboard)
                for (Enumeration e2 = currentNode.monitorPanels.keys(); e2.hasMoreElements();) {
                    String currentName = (String) e2.nextElement();
                    //If this value equals the chalkboard we are looking for . . .
                    if (currentNode.monitorPanels.get(currentName) == monitorPanel) {
                        //now we have the name, we just need to grab the options
                        //panel using the name
                        return (JPanel) currentNode.monitorOptionsPanels.get(currentName);
                    }
                }
            }
        }
        //searched everywhere and couldn't find it
        return null;
    }

    //This function will return the name of a monitor
    //if you give it the chalkboard that it draws to
    public String getMonitorNameFromChalkboard(JPanel monitorPanel) {
        //we have to look through all nodes
        for (Enumeration e = theNodes.elements(); e.hasMoreElements();) {
            SimNode currentNode = (SimNode) e.nextElement();
            if (currentNode.monitorPanels.contains(monitorPanel)) {
                //then we found it in this node,
                //but we have to "back up" and find the key (name of monitor)
                //that corresponds to the value found (the chalkboard)
                for (Enumeration e2 = currentNode.monitorPanels.keys(); e2.hasMoreElements();) {
                    String currentName = (String) e2.nextElement();
                    //If this value equals the chalkboard we are looking for . . .
                    if (currentNode.monitorPanels.get(currentName) == monitorPanel) {
                        //now we have the name, we just need to grab the options
                        //panel using the name
                        return currentName;
                    }
                }
            }
        }
        //searched everywhere and couldn't find it
        return null;
    }

    //This function will return the instance of a monitor
    //if you give it the chalkboard that it draws to
    public VisualMonitor getMonitorClassFromChalkboard(JPanel monitorPanel) {
        //we have to look through all nodes
        for (Enumeration e = theNodes.elements(); e.hasMoreElements();) {
            SimNode currentNode = (SimNode) e.nextElement();
            if (currentNode.monitorPanels.contains(monitorPanel)) {
                //then we found it in this node,
                //but we have to "back up" and find the key (name of monitor)
                //that corresponds to the value found (the chalkboard)
                for (Enumeration e2 = currentNode.monitorPanels.keys(); e2.hasMoreElements();) {
                    String currentName = (String) e2.nextElement();
                    //If this value equals the chalkboard we are looking for . . .
                    if (currentNode.monitorPanels.get(currentName) == monitorPanel) {
                        //now we have the name, we just need to grab the options
                        //panel using the name
                        return (VisualMonitor) currentNode.monitorInstances.get(currentName);
                    }
                }
            }
        }
        //searched everywhere and couldn't find it
        return null;
    }


    //End section of GUI->vector of Sim Node management



    /**
     * The <code>run()</code> method is called by the main class.
     * This just starts the GUI
     * If a file was specified by the command line, it'll be passed
     * to args
     */
    public void run(String[] args) throws Exception {

        //Let's turn off colors
        Terminal.useColors = false;

        //Provide nothing to the array if it's empty
        if (args.length < 1) {
            args = new String[1];
            args[0] = "";
        }

        //Need a pointer to the visual action to give to the GUI
        //but once we are in the new thread the this pointer dissappears
        //so we save it here
        VisualAction tempthis = this;

        //For now, we don't run it in a seperate thread.
        //We'll wait until we have problems until we try the whole seperate thread thing
        AvroraGui app = new AvroraGui();
        JFrame frame = AvroraGui.createGUI(app, tempthis, args);
        AvroraGui.showGui(frame);

        //If we needed to, code to run the GUI in a seperate thread is here
        /*
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
                public void run()
                {
                        AvroraGui app = new AvroraGui();
                        JFrame frame = AvroraGui.createGUI(app,tempthis,args);
                        //PrintStream tempstream = new PrintStream(app.getDebugOutputStream());
                        //Terminal.setOutput(tempstream);
                        AvroraGui.showGui(frame);
                        //app.debugAppend("HELLO");
                }
        });
        */

    }


    //returns true if a sim is running
    public boolean aSimIsRunning() {
        return simisrunning;
    }

    //This will pause the sim if a sim is running and it's not already paused
    //If the sim is already paused, it will unpause it
    public void changePauseStatus() {
        if (theEvents == null) {
            return;
        }

        if (theEvents.pause.ispaused) {
            //if paused, then unpause
            theEvents.pause.unpause();
        } else {
            theEvents.pause.pause();
        }
        //have to rewrite this for multi-node sims
    }

    //Will return true if the simulator is paused
    //will return false if not paused or there is no running sim
    public boolean getPauseStatus() {
        if (theEvents == null) {
            return false;
        }
        return theEvents.pause.ispaused;
    }

    public void stopSim() {
        if (!simisrunning) {
            //if a simulator isn't running, then return immediatly
            return;
        }

        //If it's paused, first we unpause it
        if (getPauseStatus()) {
            changePauseStatus();
            //Note, this might mean our sim will run for a couple more cycles
        }

        //Let's scroll through all our define nodes and stop the simualtors
        //if they've been declared.
        for (Enumeration e = theNodes.elements(); e.hasMoreElements();) {
            SimNode currentNode = ((SimNode) e.nextElement());
            if (currentNode.theSimulator != null) {
                //I think it's okay to just call stop on all nodes
                //If this doesn't work, we can always insert a local meet or
                //an event that will stop everything.
                currentNode.theSimulator.stop();
            }
        }


        /*This doesn't work!!!
        //We should remove all radios from SimpleAir as well
        for (Enumeration e = theNodes.elements() ; e.hasMoreElements() ;)
        {
        SimNode currentNode = ((SimNode)e.nextElement());
        if(currentNode.theRadio!=null)
        {
        SimpleAir.simpleAir.removeRadio(currentNode.theRadio);
        }
        }
        */

        simisrunning = false;
        //update the AvroraGUI
        app.topMenu.updateMenuBar();
        app.masterFrame.setJMenuBar(app.topMenu.menuBar);
    }

    public void singleStepProceedToNext() {
        //have to rewrite this for multi-node sims
    }

    //This function will handle the insertion of three events/probes: theRealTimeEvent, theSpeedChangeEventAndProbe,
    //and the SingleSteppingProbe
    //called by class ManageSimTime
    //arguments: delay: the delay in milliseconds if running the theSpeedChangeEvent
    //			inbetweenperiod: the amount of time inbetween sleeps for theSpeedChangeEvent
    //			instrOrCylces: true implies that we want the inbetween period variable to represent instructions,
    //							false implies we want it to represent cycles
    //			eventChoice: 0=> run in realtime, 1=> run at fullspeed, 2=> run with the specified delay/cycles
    //						3=> singlestep through
    public void setSimChangeSpeed(int delay, long inbetweenperiod, boolean instrOrCycles, int eventChoice) {

        //can use for debug
        //System.out.println("setSimChangeSpeed " + Integer.toString(delay) + " " + Long.toString(inbetweenperiod) + " " + Boolean.toString(instrOrCycles) + " " + Integer.toString(eventChoice));

        /*
        switch (eventChoice)
                {
                    //realtime
                    case 0:
                        theRealtimeEvent.insert();
                        theSpeedChangeEventAndProbe.remove();
                        theSingleStepProbe.remove();
                        break;
                    //fullspeed
                    case 1:
                        theRealtimeEvent.remove();
                        theSpeedChangeEventAndProbe.remove();
                        theSingleStepProbe.remove();
                        break;
                    //run with specified delay
                    case 2:
                        theSpeedChangeEventAndProbe.setOptions(delay, inbetweenperiod, instrOrCycles);
                        theSpeedChangeEventAndProbe.insert();
                        theSingleStepProbe.remove();
                        theRealtimeEvent.remove();
                        break;
                    //single step through
                    case 3:
                        theRealtimeEvent.remove();
                        theSpeedChangeEventAndProbe.remove();
                        theSingleStepProbe.insert();
                        break;
                }
        **/
    }

    //This starts a simulation
    //We assume the terminal has already been redirected by the
    //time this function is called
    public void runVisualCall() throws Exception {
        initializeSimulatorStatics(); //SimAction does this function
        runSimulation();
    }

    private void runSimulation() throws Exception {
        //we should clear the panels (in case they were changes from a previous run of the sim)
        clearMonitorPanels();

        //set up new sim time events
        theEvents = new SimTimeEvents(SimpleAir.simpleAir);

        //reset our global clock
        //SimpleAir.simpleAir = new SimpleAir();

        //Let's tell our global clock about the sim nodes
        // TODO: fix the simple air connection
        //SimpleAir.simpleAir.setNodes(theNodes);

        for (Enumeration e = theNodes.elements(); e.hasMoreElements();) {
            //Get the sim node we are working with this time around
            SimNode currentNode = (SimNode) e.nextElement();

            //Get a program
            String[] args = new String[1];
            args[0] = currentNode.path;

            Program program = Main.getProgramReader().read(args);

            //get a simulator object
            currentNode.theSimulator = newSimulator(new GenInterpreter.Factory(), program, currentNode);

            //get the microcontroller
            currentNode.theMicrocontroller = currentNode.theSimulator.getMicrocontroller();

            //add sim to it's own thread
            currentNode.theThread = new SimulatorThread(currentNode.theSimulator);

            //set the lowest priority, so user can still use GUI
            //even when thread is running
            //currentNode.theThread.setPriority(Thread.MIN_PRIORITY);

            currentNode.theRadio = currentNode.theMicrocontroller.getRadio();

            if (currentNode.theRadio != null) {
                currentNode.theRadio.setSimulatorThread(currentNode.theThread);

                //simpleAir = new SimpleAir();
                //simpleAir.addRadio(currentNode.theRadio);
                SimpleAir.simpleAir.addRadio(currentNode.theRadio);
            }

            processRandom(currentNode.theSimulator);
            processStagger(currentNode.theSimulator);
        }

        // enable channel utilization accounting
        //simpleAir.recordUtilization(CHANNEL_UTIL.get());

        long startms = System.currentTimeMillis();
        try {
            printSimHeader();
            startSimulationThreads();

        } finally {
            joinSimulationThreads();
            printSeparator();

            // compute simulation time
            long endms = System.currentTimeMillis();
            reportTime(startms, endms);
            //reportUtilization();
            reportAllMonitors();
        }
    }

    private void startSimulationThreads() {
        simisrunning = true;

        //AvroraGUI needs a couple things to do done
        app.startPaintThread();  //will start a thread to paint the monitor window
        app.topMenu.updateMenuBar();
        app.masterFrame.setJMenuBar(app.topMenu.menuBar);

        for (Enumeration e = theNodes.elements(); e.hasMoreElements();) {
            ((SimNode) e.nextElement()).theThread.start();
        }
    }

    private void joinSimulationThreads() throws InterruptedException {
        for (Enumeration e = theNodes.elements(); e.hasMoreElements();) {
            ((SimNode) e.nextElement()).theThread.join();
        }
        simisrunning = false;
    }

    //This needed to be overwritten because for visual monitors we need to attach
    //the correct panel
    protected Simulator newSimulator(InterpreterFactory factory, Program p, SimNode currentNode) {
        //taken from SimAction
        Simulator simulator;
        PlatformFactory pf = getPlatform();
        if (pf != null) {
            simulator = pf.newPlatform(currentNode.NID, factory, p).getMicrocontroller().getSimulator();
        } else {
            long hz = CLOCKSPEED.get();
            long exthz = EXTCLOCKSPEED.get();
            if ( exthz == 0 ) exthz = hz;
            if ( exthz > hz )
                Avrora.userError("External clock is greater than main clock speed", exthz+"hz");
            simulator = Defaults.newSimulator(simcount++, MCU.get(), hz, exthz, factory, p);
        }

        processTimeout(simulator);

        //So now we need to create monitors and attach
        //them to our new simulator.

        //Scroll through all requested monitors for this node
        for (Enumeration e = currentNode.monitors.elements(); e.hasMoreElements();) {

            String monitorName = (String) e.nextElement();
            //so we have the name of a monitor....let's create
            //a monitor factory and do all those good things that we do
            //with a monitor
            MonitorFactory mf = (MonitorFactory) monitorMap.getObjectOfClass(monitorName);
            mf.processOptions(options); //it's doubtful that this does anything....but we should
            //call it anyway
            Monitor currentMonitor = mf.newMonitor(simulator);

            //If it's a visual monitor, we have to let it know about the
            //existence of it's chalkboard and options panel (and give it
            //access to write to those two panels)
            if (currentMonitor instanceof VisualMonitor) {
                //we have to add the two panels
                //assocatied with the monitor/node pair
                JPanel monitorPanel = (JPanel) currentNode.monitorPanels.get(monitorName);
                JPanel monitorOptionsPanel = (JPanel) currentNode.monitorOptionsPanels.get(monitorName);

                ((VisualMonitor) currentMonitor).setVisualPanel(monitorPanel, monitorOptionsPanel, this);

            }

            //Add the monitor we have created to the running instances
            //of monitors for this node.
            if (currentMonitor != null) {
                currentNode.monitorInstances.put(monitorName, currentMonitor);
            }
        }

        return simulator;
    }

    private void reportTime(long startms, long endms) {
        TermUtil.reportQuantity("Time for simulation", StringUtil.milliToSecs(endms - startms), "seconds");
    }

    ////***********STUFF TO DO FOR MULTI SIM************//////////
    private void reportAllMonitors() {
        //add code so that all the monitors in all the
        //different nodes get called
        for (Enumeration e = theNodes.elements(); e.hasMoreElements();) {
            //Get the sim node we are working with this time around
            SimNode currentNode = (SimNode) e.nextElement();

            //for each node, go through and call report for each monitor instance
            for (Enumeration e2 = currentNode.monitorInstances.elements(); e2.hasMoreElements();) {
                Monitor currentMon = (Monitor) e2.nextElement();
                currentMon.report();
            }
        }
    }

    //This should be called before starting a simulation
    //so all the monitor panels are resized and cleared
    //While this should probably go in ManageMonitors, then
    //we couldn't access it from VisualAction, so here it is!
    private void clearMonitorPanels() {
        ////***********STUFF TO DO FOR MULTI SIM************//////////
        //Change this so it scrolls through all the monitor threads
        //in all the different nodes
        /*
                for (Enumeration e = monitorPanels.elements(); e.hasMoreElements(); )
                {
                    //Clear the panel & reset to original size
                    JPanel currentpanel = (JPanel)e.nextElement();
                    currentpanel.removeAll();
                    currentpanel.setPreferredSize(new Dimension(800, 600));
                }
        **/
    }

    Random random;

    void processRandom(Simulator simulator) {
        long size = RANDOM_START.getHigh() - RANDOM_START.getLow();
        long delay = 0;
        if (size > 0) {
            if (random == null) {
                long seed;
                if ((seed = RANDOMSEED.get()) != 0)
                    random = new Random(seed);
                else
                    random = new Random();
            }

            delay = random.nextLong();
            if (delay < 0) delay = -delay;
            delay = delay % size;
        }

        simulator.delay(RANDOM_START.getLow() + delay);
    }

    long stagger;

    void processStagger(Simulator simulator) {
        simulator.delay(stagger);
        stagger += STAGGER_START.get();
    }
}
