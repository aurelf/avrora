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

package avrora.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;

public class ManageTopology {
    JPanel topologyVisual; //high level visual
    JTable table;
    DefaultTableModel theModel;

    AvroraGui app;

    public static ManageTopology createManageTopology(AvroraGui papp) {
        ManageTopology theSetup = new ManageTopology();
        theSetup.app = papp;

        theSetup.topologyVisual = new JPanel();
        
        //So we can display one of two screens here....the topology
        //or a SimpleAir list of all the nodes in the visual
        
        //For now, we only allow user to see a SimpleAir list
        theSetup.createSimpleAirTable();

        theSetup.topologyVisual.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Manage Topology"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        return theSetup;
    }

    //This function will create a table of all the nodes
    //currently registered with the VisualAction class
    public void createSimpleAirTable() {
        Vector columnNames = new Vector();
        
        //Here are the column ID's
        columnNames.add("Node ID");
        columnNames.add("Program");
        columnNames.add("Monitors");
                
        //Create emtpy table
        theModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(theModel);
        
        //fill the table with all the data from vAction
        Iterator ni = app.getSimulation().getNodeIterator();
        while ( ni.hasNext() ) {
            VisualSimulation.Node currentNode = (VisualSimulation.Node)ni.next();
            Vector tempVector = new Vector();
            tempVector.add(new Integer(currentNode.id));
            tempVector.add(currentNode.getProgram().getName());
            Iterator i = currentNode.getMonitors().iterator();
            StringBuffer mstrBuffer = new StringBuffer(100);
            while ( i.hasNext() ) {
                mstrBuffer.append(i.next());
                if ( i.hasNext() ) {
                    mstrBuffer.append(",");
                }
            }
            tempVector.add(mstrBuffer.toString());
            theModel.addRow(tempVector);
        }

        JScrollPane scrollpane = new JScrollPane(table);
        //remove anything currently in topologyVisual
        topologyVisual.removeAll();
        topologyVisual.add(scrollpane);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setPreferredScrollableViewportSize(new Dimension(300, 200));
        topologyVisual.revalidate();
    }

    //Ostensibly the user has selected nodes in the table
    //for the Simple Air Module.  We want to remove any of those selected
    //nodes from our visualAction Vector
    public void removeSelectedNodes() {
        //Let's find out which nodes are selected
        int[] selectedRows = table.getSelectedRows();
        for (int i = 0; i < selectedRows.length; i++) {
            //let's get the NID of that row, and tell the 
            //visual action to remove it
            Integer integer = ((Integer) (theModel.getValueAt(selectedRows[i], 0)));
            app.getSimulation().removeNode(integer.intValue());
        }
        
        //We should redraw the table
        createSimpleAirTable();
    }

}

