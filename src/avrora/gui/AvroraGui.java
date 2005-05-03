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

//This file defines the Avrora GUI
//It is adapted from the Swing Tutorials on java.sun.com

package avrora.gui;

import avrora.actions.VisualAction;
import avrora.Version;
import avrora.Avrora;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Enumeration;


public class AvroraGui implements ActionListener, ChangeListener {

    //Links to the visual action which started this GUI
    public VisualAction vAction;
    public String[] args;

    //High level elements of the GUI
    public JFrame masterFrame;
    public SimMenuBar topMenu;
    public ManageSimInput simInputBox;
    public ManageMonitors manageMonitorsBox;
    public ManageSimTime simTimeBox;
    public ManageTopology topologyBox;
    public JTabbedPane monitorResults; //Holds all the different monitor results, as tabbed windows
    private JLabel versioningInfo; //holds the text that displays at the bottom of the GUI
    private JPanel monitorOptions; //holds the options for the current monitor

    //This stuff should be moved to it's own class/file once
    //I get around to it
    private JPanel topologyVisual; //Holds the topology visual

    //This is the debug panel
    private JTextArea debugOutput; //This holds basic debug information
    private JPanel debugPanel; //this is init in createDebugOutput

    //For convuluted reasons it's important to store the
    //current monitor being displayed (see paint thread)
    private VisualMonitor currentMonitorDisplayed;

    //we no longer create the banner
    //private JPanel avroraBanner;  //holds the title banner

    private static final String AVRORA_VERSION = "Avrora "+Version.getVersion();
    private static final String AVRORA_GUI_VERSION = "Avrora Gui v.0.3.2";
    private static final int PAINT_THREAD_SLEEP_TIME = 200;

    //A thread that will repaint the monitor window
    PaintThread newPaintThread;

    //Called when an event occurs on the GUI
    //Really just dispatchs to the modules of the GUI and allows them
    //to handle the event
    public void actionPerformed(ActionEvent e) {
        if (manageMonitorsBox.checkAndDispatch(e)) {
            return;
        } else if (simTimeBox.checkAndDispatch(e)) {
            return;
        } else if (topMenu.checkAndDispatch(e)) {
            return;
        }
    }

    //some modules detect state changes, not actions
    public void stateChanged(ChangeEvent e) {
        if (simTimeBox.sliderAndSpinnerDispatch(e)) {
            return;
        } else if (e.getSource() == monitorResults) {
            //whenever we change the tab pane we have to change
            //the monitor options panel to correspond with the
            //right monitor

            JPanel monitorPanel = (JPanel) monitorResults.getComponentAt(monitorResults.getSelectedIndex());

            String titleOfPanel;

            //So now we have the actualy panel...we can feed that to our VisualAction
            //and it will return the corresponding options panel
            JPanel thePanel = vAction.getOptionsFromMonitor(monitorPanel);
            titleOfPanel = vAction.getMonitorNameFromChalkboard(monitorPanel);
            currentMonitorDisplayed = vAction.getMonitorClassFromChalkboard(monitorPanel);

            if (thePanel == null) {
                //then it must be the debug panel...let's set
                //the options panel to just a blank panel
                thePanel = new JPanel(false);
                titleOfPanel = "Monitor";
            }
            //Now thePanel is what we want to display
            monitorOptions.removeAll();
            monitorOptions.setLayout(new GridLayout(1, 1));
            monitorOptions.add(thePanel);
            //redo the title border, now with the title of the monitor
            monitorOptions.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(titleOfPanel + " Options"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            monitorOptions.validate();

        }
        return;
    }

    //This creates the "console window" inside the GUI
    private void createDebugOutput() {
        debugOutput = new JTextArea("This is the console. " + "Debug output will be printed here as well as text output from Avrora\n");
        debugOutput.setFont(new Font("Courier", Font.PLAIN, 14));
        debugOutput.setBackground(Color.BLACK);
        debugOutput.setForeground(Color.WHITE);
        debugOutput.setLineWrap(true);
        debugOutput.setWrapStyleWord(true);
        debugOutput.setEditable(false);
        JScrollPane debugScrollPane = new JScrollPane(debugOutput);
        debugScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //Let's not set a preferred size...we'll just accept whatever we can get
        //debugScrollPane.setPreferredSize(new Dimension(800, 600));
        debugPanel = new JPanel();
        //this sets is to that the debug panel takes up the full scenen
        debugPanel.setLayout(new OverlayLayout(debugPanel));
        debugPanel.add(debugScrollPane);
    }

    //This function creates the monitor results tab window, with the
    //debug tab as the default tab displayed
    //Should be called after createDebugOutput()
    private void createMonitorResults(AvroraGui papp) {
        monitorResults = new JTabbedPane(JTabbedPane.BOTTOM);

        //Let's not set a preferred size
        //Dimension monitorDimensions = new Dimension(830, 680);
        //monitorResults.setPreferredSize(monitorDimensions);

        //By default, only the debug tab is displayed
        //We should set it so that it takes up the full panel
        debugPanel.setPreferredSize(new Dimension(monitorResults.getSize()));
        monitorResults.addTab("Debug Information", debugPanel);

        monitorResults.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("View Monitors"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        monitorResults.addChangeListener(papp);
    }


    private void createTopologyVisual() {
        //Right now, we just display a static picture for eyecandy
        ImageIcon topologyVisualIcon = createImageIcon("images/dummypic_topology.jpg");
        JLabel topologyVisualLabel = new JLabel();
        topologyVisualLabel.setIcon(topologyVisualIcon);
        topologyVisual = new JPanel();
        topologyVisual.add(topologyVisualLabel);
        topologyVisual.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Manage Topology"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

    }

    private void createVersioningInfo() {
        versioningInfo = new JLabel(AVRORA_VERSION + "; " + AVRORA_GUI_VERSION, SwingConstants.RIGHT);
    }

    //Used by DebugStream to allow writing to the debug window
    public void debugAppend(String b) {
        debugOutput.append(b);
    }

    //This is called during createGUI...it provides the links
    //to the VisualAction
    public void createVisualLink(VisualAction pvAction, String[] pargs) {
        vAction = pvAction;
        vAction.app = this;
        args = pargs;
    }

    //This actually "creates" the GUI...and calling this, one should call showGUI
    public static JFrame createGUI(AvroraGui app, VisualAction pvAction, String[] pargs) {

        //Set the look and feel.
        initLookAndFeel();

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        app = new AvroraGui();

        //Create and set up the window.
        JFrame frame = new JFrame("Avrora GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        app.createVisualLink(pvAction, pargs);  //creates links back to VisualAction

        //First create the north quadrant, which contains
        //the banner and the sim toolbar

        //We no longer want the banner

        app.simTimeBox = ManageSimTime.createManageSimTime(app);

        //app.simInputBox = ManageSimInput.createManageSimInput(app.args, app);

        app.topMenu = SimMenuBar.createSimMenuBar(app.args, app);
        frame.setJMenuBar(app.topMenu.menuBar);

        //Let's create a subpanel for displaying simtoolbar and file selection
        JPanel toolAndFile = new JPanel();
        toolAndFile.setLayout(new BorderLayout());
        toolAndFile.add(app.simTimeBox.simTimeEverything, BorderLayout.WEST);
        //toolAndFile.add(app.simInputBox.fileSelection, BorderLayout.EAST);

        //The tool bar goes in the north quandrant
        frame.getContentPane().add(toolAndFile, BorderLayout.NORTH);

        //In the center is the tabs with all the monitor results
        app.createDebugOutput();
        app.createMonitorResults(app); //Created the tabbed panes for monitor results

        frame.getContentPane().add(app.monitorResults, BorderLayout.CENTER);

        //The west quadrant contains the topology visual
        app.createTopologyVisual();
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new GridLayout(2, 1));

        //westPanel.add(app.topologyVisual);
        app.topologyBox = ManageTopology.createManageTopology(app);
        westPanel.add(app.topologyBox.topologyVisual);


        //init monitor options to blank panel, with the typical border
        app.monitorOptions = new JPanel();
        app.monitorOptions.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Monitor Options"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        westPanel.add(app.monitorOptions);
        frame.getContentPane().add(westPanel, BorderLayout.WEST);

        //The south quadrant holds the manage monitors button and the version info
        app.manageMonitorsBox = ManageMonitors.createManageMonitors(app);
        app.createVersioningInfo();
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());
        //Add blank JPanel so our button is right size
        JPanel blankPanel = new JPanel();
        southPanel.add(blankPanel, BorderLayout.CENTER);
        //We no longer add Manage monitors -> it's been moved to the top level menu
        //southPanel.add(app.manageMonitorsBox.monitorsButton, BorderLayout.EAST);
        southPanel.add(app.versioningInfo, BorderLayout.SOUTH);
        frame.getContentPane().add(southPanel, BorderLayout.SOUTH);

        //The avrora gui needs access to the frame
        app.masterFrame = frame;

        return frame;
    }

    public static void showGui(JFrame frame) {
        //Display the window.

        frame.pack();  //set to min size the components allow if its not maxed
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); //will max window
        frame.setVisible(true);
    }

    //The swing tutorial had this code, to allow it to set up
    //the default look and feel
    //Specify the look and feel to use.  Valid values:
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    static final String LOOKANDFEEL = null;

    //Provided by swing tutorial to set up default look and feel
    private static void initLookAndFeel() {
        String lookAndFeel;

        if (LOOKANDFEEL != null) {
            if ("Metal".equals(LOOKANDFEEL)) {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            } else if ("System".equals(LOOKANDFEEL)) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            } else if ("Motif".equals(LOOKANDFEEL)) {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            } else if ("GTK+".equals(LOOKANDFEEL)) { //new in 1.4.2
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            } else {
                System.err.println("Unexpected value of LOOKANDFEEL specified: " + LOOKANDFEEL);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }

            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:" + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel (" + lookAndFeel + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (Exception e) {
                System.err.println("Couldn't get specified look and feel (" + lookAndFeel + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }

    //Provided by swing tutorial - just used to load images
    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imageURL = AvroraGui.class.getResource(path);

        if (imageURL == null) {
            throw Avrora.failure("Resource not found: " + path);
        } else {
            return new ImageIcon(imageURL);
        }
    }

    public void startPaintThread() {
        newPaintThread = new PaintThread();
        newPaintThread.start();
    }

    //This class is actually a new thread that will run
    //once every while to repaint the the monitor window
    public class PaintThread extends Thread {
        public void run() {
            try {

                //So we need to see if
                //a monitor result is currently
                //being display...if so, repaint it
                //and sleep for the specified amount of time
                while (vAction.aSimIsRunning()) {
                    if (monitorResults.getSelectedIndex() != 0) {
                        if (currentMonitorDisplayed == null) {
                            //we need to get all the nodes that are of type visualradio monitor
                            Vector allMons = VisualRadioMonitor.allCurrentMonitors;
                            for (Enumeration e = allMons.elements(); e.hasMoreElements();) {
                                VisualMonitor tempMon = (VisualMonitor) e.nextElement();
                                tempMon.updateDataAndPaint();
                            }
                        } else {
                            currentMonitorDisplayed.updateDataAndPaint();
                        }
                    }
                    Thread.currentThread().sleep(PAINT_THREAD_SLEEP_TIME);
                }
            } catch (InterruptedException except) {
                //If interrupted, do nothing
            }
        }
    }
}

