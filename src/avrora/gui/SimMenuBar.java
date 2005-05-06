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

//This manages the high level menu bar for the GUI

package avrora.gui;

import javax.swing.*;
import java.awt.event.*;

public class SimMenuBar {
    public JMenuBar menuBar; //this is the high level menu that should be displayed

    ManageSimInput theSimInput; //this is all the code for getting sim files
    //and changing options....it's done this way because
    //there was a time when there was no menu bar

    AvroraGui app;


    private static final String FILE = "File";
    private static final String SIMOPTIONS = "Options...";
    private static final String LOADPROGRAM = "Load Program...";
    private static final String ADDFILE = "Add Nodes...";
    private static final String REMOVENODES = "Remove Nodes...";

    private static final String MONITORS = "Monitors";
    private static final String ADDMONITORS = "Add Monitors...";

    //Returns an object of ManageSimInput which represents the file selection panel
    //Passed the filename given to avrora by command line (so it can setup default)
    public static SimMenuBar createSimMenuBar(String[] args, AvroraGui papp) {
        SimMenuBar thesetup = new SimMenuBar();
        thesetup.theSimInput = ManageSimInput.createManageSimInput(args, papp);

        thesetup.app = papp;

        thesetup.menuBar = new JMenuBar();
        thesetup.updateMenuBar();

        return thesetup;
    }

    public void updateMenuBar() {

        if (!app.getSimulation().isRunning()) {
            menuBar.removeAll();
            JMenu newMenu;
            newMenu = new JMenu(FILE);
            menuBar.add(newMenu);

            JMenuItem newItem;

            newItem = new JMenuItem(SIMOPTIONS);
            newItem.addActionListener(app);
            newMenu.add(newItem);

            newItem = new JMenuItem(LOADPROGRAM);
            newItem.addActionListener(app);
            newMenu.add(newItem);

            newItem = new JMenuItem(ADDFILE);
            newItem.addActionListener(app);
            newMenu.add(newItem);

            newItem = new JMenuItem(REMOVENODES);
            newItem.addActionListener(app);
            newMenu.add(newItem);

            newMenu = new JMenu(MONITORS);
            menuBar.add(newMenu);

            newItem = new JMenuItem(ADDMONITORS);
            newItem.addActionListener(app);
            newMenu.add(newItem);
        } else {
            menuBar.removeAll();
            menuBar.add(new JMenu("Sim is running"));
            //Currently no options can be changed during runtime
        }
    }

    public boolean checkAndDispatch(ActionEvent e) {
        Object source = (e.getSource());
        if (source instanceof JMenuItem) {
            if (((JMenuItem) source).getText().equals(SIMOPTIONS)) {
                theSimInput.createSetOptionsDialog();
                theSimInput.setOptionsDialog.setLocationRelativeTo(null); //center on screen
                theSimInput.setOptionsDialog.setVisible(true);
                return true;
            }
            if (((JMenuItem) source).getText().equals(ADDFILE)) {
                theSimInput.createFileSelectionDialog();
                theSimInput.fileSelectionDialog.setLocationRelativeTo(null); //center on screen
                theSimInput.fileSelectionDialog.setVisible(true);
                return true;
            }
            if (((JMenuItem) source).getText().equals(REMOVENODES)) {
                app.topologyBox.removeSelectedNodes();
            }
            if (((JMenuItem) source).getText().equals(ADDMONITORS)) {
                app.manageMonitorsBox.createMonitorsDialog();
                app.manageMonitorsBox.chooseMonitorsDialog.setLocationRelativeTo(null); //center on screen
                app.manageMonitorsBox.chooseMonitorsDialog.setVisible(true);
            }
        }

        //We should check to see if something in theSimInput caused the action
        return theSimInput.checkAndDispatch(e);
    }


}
