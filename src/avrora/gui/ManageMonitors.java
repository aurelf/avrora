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

//This manages a file chooser box for choosing the simulator file
//also manages a dialog box for setting simulator options

package avrora.gui;

import avrora.Avrora;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public class ManageMonitors {
    public JButton monitorsButton;

    JDialog chooseMonitorsDialog;
    JButton monitorsDialogUpdate;
    LinkedList checkBoxContainer;

    AvroraGui app;


    //Returns an object of ManageSimInput which represents the file selection panel
    //Passed the filename given to avrora by command line (so it can setup default)
    //This is the "constructor" for this class
    public static ManageMonitors createManageMonitors(AvroraGui papp) {
        ManageMonitors thesetup = new ManageMonitors();

        thesetup.app = papp;

        //button that holds "Manage Monitors"
        thesetup.monitorsButton = new JButton();
        thesetup.monitorsButton.setText("Manage Monitors");
        thesetup.monitorsButton.setToolTipText("Opens a dialog so you can place monitors on the simulator");
        thesetup.monitorsButton.setHorizontalAlignment(JLabel.RIGHT);
        thesetup.monitorsButton.setMaximumSize(new Dimension(100, 40));
        thesetup.monitorsButton.addActionListener(papp);

        //Note: the dialog box is created upon the button push

        return thesetup;
    }

    //This creates the dialog box that asks for which monitors we want
    //to add
    public void createMonitorsDialog() {

        //Make sure we have nice window decorations.
        //initLookAndFeel();
        JDialog.setDefaultLookAndFeelDecorated(true);

        checkBoxContainer = new LinkedList();

        chooseMonitorsDialog = new JDialog(app.masterFrame, "Add Monitors to Selected Nodes");

        //Now we create a JPanel that will be linked to the dialog
        JPanel internalPanel = new JPanel();
        internalPanel.setOpaque(true);
        internalPanel.setLayout(new BorderLayout());

        //Let's add a title banner
        JLabel dialogBanner = new JLabel();
        dialogBanner.setText("Check the monitors you want to add");
        internalPanel.add(dialogBanner, BorderLayout.NORTH);

        JPanel belowBannerPanel = new JPanel();

        addMonitorsFromClassMap(belowBannerPanel);

        //Border of 30 on the outside
        belowBannerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        internalPanel.add(belowBannerPanel, BorderLayout.CENTER);

        //Add the "Update" button
        monitorsDialogUpdate = new JButton();
        monitorsDialogUpdate.setText("Update");
        monitorsDialogUpdate.setToolTipText("Click to update the monitors list");
        monitorsDialogUpdate.addActionListener(app);
        internalPanel.add(monitorsDialogUpdate, BorderLayout.SOUTH);
        internalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //Add the panel to the dialog
        chooseMonitorsDialog.setContentPane(internalPanel);

        //Sizes things appropiatly
        chooseMonitorsDialog.pack();

    }

    private void addMonitorsFromClassMap(JPanel belowBannerPanel) {
        //Let's get a storted list of monitor names registered with the VisualAction
        java.util.List monitorList = app.getMonitorList();
        Iterator monitorIter = monitorList.iterator();

        belowBannerPanel.setLayout(new GridLayout(monitorList.size(), 1));

        //Scroll through, adding all monitors...if the monitor is already
        //part of our VisualAction MONITORS Option, then we check it.
        while (monitorIter.hasNext()) {
            String currentMonitor = (String) monitorIter.next();
            //Add a checkbox representing this list
            JCheckBox theCheckBox = new JCheckBox(currentMonitor);

            theCheckBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            belowBannerPanel.add(theCheckBox);
            //add the check box to a container so we can examine the values at a later date
            checkBoxContainer.add(theCheckBox);
        }
    }

    //This function sees if an event was caused by
    //this panel.  If so, it reacts to it and return true, if not, it returns false
    public boolean checkAndDispatch(ActionEvent e) {
        //if the manage monitors button was pushed...we have to load our dialog
        if (e.getSource() == monitorsButton) {
            createMonitorsDialog();
            chooseMonitorsDialog.setLocationRelativeTo(null); //center on screen
            chooseMonitorsDialog.setVisible(true);

            return true;
        } else if (e.getSource() == monitorsDialogUpdate) {
            //Our goal is to find all the selected
            //nodes.  For each node, if the monitor
            //has not already been added, then we
            //create a panel for it, register that panel,
            //and add the monitor to that node's monitor list

            //We first begin by getting a vector of strings
            //that represent the names of the monitors we wish to add
            Vector toMONITORS = new Vector();
            Iterator checkBoxIter = checkBoxContainer.iterator();
            while (checkBoxIter.hasNext()) {
                JCheckBox currentBox = ((JCheckBox) checkBoxIter.next());
                if (currentBox.isSelected()) {
                    //it's selected, so add it to our list
                    toMONITORS.add(currentBox.getText());
                }
            }

            // for each selected monitor, give it a chance to attach to the nodes
            LinkedList nodes = getNodeList();
            for (int j = 0; j < toMONITORS.size(); j++) {
                String currentMonitor = ((String) toMONITORS.elementAt(j));
                VisualSimulation.MonitorFactory mf = getMonitorFactory(currentMonitor);
                mf.attach(nodes);
            }

            //We are done with the dialog...get rid of it
            chooseMonitorsDialog.setVisible(false);

            //we should also redraw the node table
            app.topologyBox.createSimpleAirTable();
            return true;
        } else {
            return false; //this module did not cause the action;
        }

    }

    private void createRadioPanel(String currentMonitor, int currentNID) {
        //if it's not already there, we add
        //the static Radio Monitor Panel to
        //the tab
        if (!VisualRadioMonitor.isDisplayed) {
            String nameOfPanel = "Global - Radio Monitor";

            //Actually display the panel...add it to the tab
            app.monitorResults.addTab(nameOfPanel, VisualRadioMonitor.masterPanel);
        }

        //Now we add this NID to our list of nodes
        //associated with this global monitor
        // TODO: reimplement adding a monitor to a node
        if (false) {
            //if the above returned true, then we have to
            //actually create a monitor panel and
            //register that panel the visual action
            //If it returned false, then that node already had
            //that monitor
            JPanel panel = new JPanel(false);  //This is the panel passed to the monitor
            JLabel filler = new JLabel("This panel will update once the simulator is run.");
            filler.setHorizontalAlignment(JLabel.CENTER);
            panel.setLayout(new GridLayout(1, 1));
            panel.add(filler);


            GridLayout theLayout = (GridLayout) VisualRadioMonitor.masterPanel.getLayout();
            if (VisualRadioMonitor.masterPanel.getComponentCount() == theLayout.getRows())
                theLayout.setRows(theLayout.getRows() + 1);

            VisualRadioMonitor.masterPanel.add(panel);

            //Now let's create the options panel
            JPanel optionsPanel = VisualRadioMonitor.optionsPanel;

            //Let's keep track of our panels...add them to hash tables
            //for this particular node
            // TODO: reimplement registering panels

        }
    }

    private void createDefaultPanel(int currentNID, String currentMonitor) {
        //if the above returned true, then we have to
        //actually create a monitor panel and
        //register that panel the visual action
        //If it returned false, then that node already had
        //that monitor
        JPanel panel = new JPanel(false);  //This is the panel passed to the monitor
        JLabel filler = new JLabel("This panel will update once the simulator is run.");
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);

        String nameOfPanel = "NID: " + Integer.toString(currentNID) + " - " + currentMonitor;

        //Actually display the panel...add it to the tab
        app.monitorResults.addTab(nameOfPanel, panel);

        //Now let's create the options panel
        JPanel optionsPanel = new JPanel(false);
        JLabel optionsFiller = new JLabel("Options for the monitor can be set here. ");
        optionsPanel.setLayout(new GridLayout(1, 1));
        optionsPanel.add(optionsFiller);

        //Let's keep track of our panels...add them to hash tables
        //for this particular node
        // TODO: reimplement registering panels
    }

    private VisualSimulation.MonitorFactory getMonitorFactory(String n) {
        return GUIDefaults.getMonitor(n);
    }

    private LinkedList getNodeList() {
        VisualSimulation sim = app.getSimulation();
        LinkedList nodes = new LinkedList();
        int[] selectedRows = app.topologyBox.table.getSelectedRows();
        for (int i = 0; i < selectedRows.length; i++) {
            //let's get the NID of that row
            Object v = app.topologyBox.theModel.getValueAt(selectedRows[i], 0);
            int nid = ((Integer) v).intValue();
            VisualSimulation.Node node = sim.getNode(nid);
            nodes.add(node);
        }
        return nodes;
    }
}