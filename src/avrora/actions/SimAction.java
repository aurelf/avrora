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

import avrora.Avrora;
import avrora.core.Program;
import avrora.monitors.*;
import avrora.sim.GenInterpreter;
import avrora.sim.InterpreterFactory;
import avrora.sim.Simulator;
import avrora.sim.dbbc.DBBC;
import avrora.sim.dbbc.DBBCInterpreter;
import avrora.sim.mcu.MicrocontrollerFactory;
import avrora.sim.mcu.Microcontrollers;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.platform.Platforms;
import avrora.util.ClassMap;
import avrora.util.Option;
import avrora.util.StringUtil;
import avrora.util.Terminal;

import java.util.*;

/**
 * The <code>SimAction</code> is an abstract class that collects many of the options common to single node and
 * multiple-node simulations into one place.
 *
 * @author Ben L. Titzer
 */
public abstract class SimAction extends Action {

    public final Option.Long ICOUNT = newOption("icount", 0,
            "This option is used to terminate the " +
            "simulation after the specified number of instructions have been executed.");
    public final Option.Double SECONDS = newOption("seconds", 0.0,
            "This option is used to terminate the " +
            "simulation after the specified number of simulated seconds have passed.");
    public final Option.Long TIMEOUT = newOption("timeout", 0,
            "This option is used to terminate the " +
            "simulation after the specified number of clock cycles have passed.");
    public final Option.Str CHIP = newOption("chip", "atmega128l",
            "This option selects the microcontroller from a library of supported " +
            "microcontroller models.");
    public final Option.Str PLATFORM = newOption("platform", "",
            "This option selects the platform on which the microcontroller is built, " +
            "including the external devices such as LEDs and radio. If the platform " +
            "option is not set, the default platform is the microcontroller specified " +
            "in the \"chip\" option, with no external devices.");
    public final Option.List MONITORS = newOptionList("monitors", "",
            "This option specifies a list of monitors to be attached to the program. " +
            "Monitors collect information about the execution of the program while it " +
            "is running such as profiling data or timing information.");
    //--BEGIN EXPERIMENTAL: dbbc
    public final Option.Bool DBBC = newOption("dbbc", false,
            "This option enables the DBBC compiler. \n(Status: experimental)");
    //--END EXPERIMENTAL: dbbc
    public final Option.Bool REPORT_SECONDS = newOption("report-seconds", false,
            "This option causes all times printed out by the simulator to be reported " +
            "in seconds rather than clock cycles.");
    public final Option.Long SECONDS_PRECISION = newOption("seconds-precision", 6,
            "This option sets the precision (number of decimal places) reported for " +
            "event times in the simulation.");
    public final Option.Str VISUAL = newOption("visual", "",
            "This optiobn enables visual representation of the network. For example " +
            "topology, packet transmission, packet recption, energy " +
            "information and more. Syntax is ip address or host name and " +
            "port: 134.2.11.183:2379 \n(Status: experimental)");

    protected ClassMap monitorMap;
    protected LinkedList monitorFactoryList;
    protected HashMap monitorListMap;

    protected SimAction(String sn, String h) {
        super(sn, h);
        monitorMap = new ClassMap("Monitor", MonitorFactory.class);
        addNewMonitorType(new ProfileMonitor());
        addNewMonitorType(new MemoryMonitor());
        addNewMonitorType(new SleepMonitor());
        addNewMonitorType(new StackMonitor());
        //add energy monitor to the list 
        addNewMonitorType(new EnergyMonitor());
        addNewMonitorType(new EnergyMonitorLog());
        addNewMonitorType(new TraceMonitor());
        //add energy profile monitor to the list 
        addNewMonitorType(new EnergyProfiler());
        addNewMonitorType(new PacketMonitor());
        addNewMonitorType(new GDBServer());
        addNewMonitorType(new SimPerfMonitor());
        addNewMonitorType(new Pc());
        monitorFactoryList = new LinkedList();
        monitorListMap = new HashMap();
    }

    private void addNewMonitorType(MonitorFactory f) {
        monitorMap.addClass(f.getShortName(), f.getClass());
    }

    /**
     * The <code>getMicrocontroller()</code> method is used to get the current microcontroller from the
     * library of implemented ones, based on the command line option that was specified (-chip=xyz).
     *
     * @return an instance of <code>MicrocontrollerFactory</code> for the microcontroller specified on the
     *         command line.
     */
    protected MicrocontrollerFactory getMicrocontroller() {
        MicrocontrollerFactory mcu = Microcontrollers.getMicrocontroller(CHIP.get());
        if (mcu == null)
            Avrora.userError("Unknown microcontroller", StringUtil.quote(CHIP.get()));
        return mcu;
    }

    /**
     * The <code>getPlatform()</code> method is used to get the current platform from the library of
     * implemented ones, based on the command line option that was specified (-platform=xyz).
     *
     * @return an instance of <code>PlatformFactory</code> for the platform specified on the command line
     */
    protected PlatformFactory getPlatform() {
        String pf = PLATFORM.get();
        if ("".equals(pf)) return null;
        PlatformFactory pff = Platforms.getPlatform(pf);
        if (pff == null)
            Avrora.userError("Unknown platform", StringUtil.quote(pf));
        return pff;
    }

    /**
     * The <code>reportQuantity()</code> method is a simply utility to print out a quantity's name
     * (such as "Number of instructions executed", the value (such as 2002), and the units (such as
     * cycles) in a colorized and standardized way.
     * @param name the name of the quantity as a string
     * @param val the value of the quantity as a long integer
     * @param units the name of the units as a string
     */
    protected void reportQuantity(String name, long val, String units) {
        reportQuantity(name, Long.toString(val), units);
    }

    /**
     * The <code>reportQuantity()</code> method is a simply utility to print out a quantity's name
     * (such as "Number of instructions executed", the value (such as 2002), and the units (such as
     * cycles) in a colorized and standardized way.
     * @param name the name of the quantity as a string
     * @param val the value of the quantity as a floating point number
     * @param units the name of the units as a string
     */
    protected void reportQuantity(String name, float val, String units) {
        reportQuantity(name, Float.toString(val), units);
    }

    /**
     * The <code>reportQuantity()</code> method is a simply utility to print out a quantity's name
     * (such as "Number of instructions executed", the value (such as 2002), and the units (such as
     * cycles) in a colorized and standardized way.
     * @param name the name of the quantity as a string
     * @param val the value of the quantity as a string
     * @param units the name of the units as a string
     */
    protected void reportQuantity(String name, String val, String units) {
        Terminal.printGreen(name);
        Terminal.print(": ");
        Terminal.printBrightCyan(val);
        Terminal.println(' ' + units);
    }

    /**
     * The <code>processMonitorList()</code> method builds a list of <code>MonitorFactory</code> instances
     * from the list of strings given as an option at the command line. The list of
     * <code>MonitorFactory</code> instances is used to create monitors for each simulator as it is created.
     */
    protected void processMonitorList() {
        if (monitorFactoryList.size() > 0) return;

        List l = MONITORS.get();
        Iterator i = l.iterator();
        while (i.hasNext()) {
            String clname = (String)i.next();
            MonitorFactory mf = (MonitorFactory)monitorMap.getObjectOfClass(clname);
            mf.processOptions(options);
            monitorFactoryList.addLast(mf);
        }
    }

    int simcount;

    /**
     * The <code>newSimulator()</code> method is used by subclasses of this action to create a new instance of
     * a simulator with the correct platform. This method also creates monitors for the simulator instance as
     * specified from the command line.
     *
     * @param p the program to load onto the simulator
     * @return an instance of the <code>Simulator</code> class that has the specified programs loaded onto it
     *         and has monitors attached to as specified on the command line
     */
    protected Simulator newSimulator(Program p) {
        //--BEGIN EXPERIMENTAL: dbbc
        if (DBBC.get()) {
            return newSimulator(new DBBCInterpreter.Factory(new DBBC(p, options)), p);
        }
        //--END EXPERIMENTAL: dbbc
        return newSimulator(new GenInterpreter.Factory(), p);
    }

    /**
     * The <code>newSimulator()</code> method is a simple utility used by actions deriving from this
     * class that creates a new <code>Simulator</code> with the correct configuration, with the specified
     * program, and then applies timeouts, breakpoints, and monitors to it.
     * @param factory the factory that creates the interpreter for the simulator
     * @param p the program to load onto the simulator
     * @return a new <code>Simulator</code> instance
     */
    protected Simulator newSimulator(InterpreterFactory factory, Program p) {
        Simulator simulator;
        PlatformFactory pf = getPlatform();
        if (pf != null) {
            simulator = pf.newPlatform(simcount++, factory, p).getMicrocontroller().getSimulator();
        } else {
            simulator = getMicrocontroller().newMicrocontroller(simcount++, factory, p).getSimulator();
        }

        processTimeout(simulator);

        processMonitorList();
        LinkedList ml = new LinkedList();
        Iterator i = monitorFactoryList.iterator();
        while (i.hasNext()) {
            MonitorFactory mf = (MonitorFactory)i.next();
            Monitor m = mf.newMonitor(simulator);
            if ( m != null )
                ml.addLast(m);
        }
        monitorListMap.put(simulator, ml);
        return simulator;
    }

    /**
     * The <code>processTimeout()</code> method simply checks the command line arguments that
     * correspond to timeouts (such as clock cycles, seconds, or instructions) and inserts
     * the appropriate probes into the simulator.
     * @param s
     */
    protected void processTimeout(Simulator s) {
        if (TIMEOUT.get() > 0)
            s.insertTimeout(TIMEOUT.get());
        else if (SECONDS.get() > 0.0)
            s.insertTimeout((long)(SECONDS.get() * s.getMicrocontroller().getHz()));
        if (ICOUNT.get() > 0)
            s.insertProbe(new Simulator.InstructionCountTimeout(ICOUNT.get()));
    }

    /**
     * The <code>reportMonitors()</code> method gets a list of <code>Monitor</code> instances attached to the
     * simulator and calls each of their <code>report()</code> methods.
     *
     * @param s the simulator for which to report all the monitors
     */
    protected void reportMonitors(Simulator s) {
        LinkedList ml = (LinkedList)monitorListMap.get(s);
        if (ml == null) return;
        Iterator i = ml.iterator();
        while (i.hasNext()) {
            Monitor m = (Monitor)i.next();
            m.report();
        }
    }

    protected boolean hasMonitors(Simulator s) {
        LinkedList ml = (LinkedList)monitorListMap.get(s);
        if (ml == null) return false;
        return !ml.isEmpty();
    }

    /**
     * The <code>getLocationList()</code> method is to used to parse a list of program locations and turn them
     * into a list of <code>Main.Location</code> instances.
     *
     * @param program the program to look up labels in
     * @param v       the list of strings that are program locations
     * @return a list of program locations
     */
    public static List getLocationList(Program program, List v) {
        HashSet locset = new HashSet();

        Iterator i = v.iterator();

        while (i.hasNext()) {
            String val = (String)i.next();
            Program.Location l = program.getProgramLocation(val);
            if ( l == null )
                Avrora.userError("Label unknown", val);
            locset.add(l);
        }

        List loclist = Collections.list(Collections.enumeration(locset));
        Collections.sort(loclist, Program.LOCATION_COMPARATOR);

        return loclist;
    }

    /**
     * The <code>printSimHeader()</code> method simply prints the first line of output that names
     * the columns for the events outputted by the rest of the simulation.
     */
    protected void printSimHeader() {
        Terminal.printSeparator(Terminal.MAXLINE, "Simulation events");
        Terminal.printGreen("Node       Time   Event");
        Terminal.nextln();
        Terminal.printThinSeparator(Terminal.MAXLINE);
    }

    protected void printSeparator() {
        Terminal.printSeparator(Terminal.MAXLINE);
    }

    /**
     * The <code>initializeSimulatorStatics()</code> method simply checks a few command line
     * parameters and initializes the <code>Simulator</code> class's static variables that relate
     * to reporting time, etc.
     */
    protected void initializeSimulatorStatics() {
        Simulator.REPORT_SECONDS = REPORT_SECONDS.get();
        Simulator.SECONDS_PRECISION = (int)SECONDS_PRECISION.get();
        if ( Simulator.SECONDS_PRECISION >= Simulator.PRECISION_TABLE.length)
            Simulator.SECONDS_PRECISION = Simulator.PRECISION_TABLE.length - 1;
    }
}
