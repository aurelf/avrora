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

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

import avrora.util.Terminal;
import avrora.actions.VisualAction;

/**
 * The class assists visual monitors with graphing time-series data
 * values.  It visually displays them using a line graph
 */
public class GraphNumbers extends JPanel implements ChangeListener, AdjustmentListener {

    private MyVector publicNumbers; //access by monitors to add stuff
    private MyVector privateNumbers; //only accessed by paint
    private JPanel parentPanel;
    
    private Object vSync; //just a private sync variable

    /**
    * This is the bar that determines what part of
    * the graph is displayed
    */
    public JScrollBar horzBar;

    //All these fields can be set by the options panel
    
    /**
     * The number of pixels per x-axis value
     */
    public int stepsize;
    
    /**
     * The visual widget that sets the step size
     */
    public SpinnerNumberModel stepsizeVisual;
    
    /**
     * The max value of the y-axis
     */
    public int maxvalue;
    
    /**
     * The visual wdiget that sets the max value for the y-axis
     */
    public SpinnerNumberModel maxvalueVisual;

    //options not done yet
    private Color lineColor; //color of line that is drawn
    private Color backColor; //color of background
    private Color tickColor; //color of tick marks/graph lines
    private int xAxisMajorTickMark; //number of plot points before drawing major tick mark
    private int minvalue;

    //Other features to add:
    //ability to user this class "on top of" another GraphNumbers class => multiple lines on one graph
    //ability to see/get the value of the line based upon a mouse over/mouse click event
    //double check to see if scroll bar is sizing correctly

    /**
     * Called by a visual action that wants this class to help with displaying time series data
     * @param pminvalue The min value for the y-axis
     * @param pmaxvalue The max value for the y-axis
     * @param pstepsize The step size for the x-axis
     */
    public GraphNumbers(int pminvalue, int pmaxvalue, int pstepsize) {
        
        vSync = new Object();

        publicNumbers = new MyVector();
        privateNumbers = new MyVector();

        //Set option defaults
        lineColor = Color.GREEN; //default line color is green
        backColor = Color.BLACK; //default background color is black
        tickColor = Color.LIGHT_GRAY; //default tick mark color is gray
        xAxisMajorTickMark = 20; //go 20 plot points before drawing x-axis tick mark
        minvalue = pminvalue; //min and max values for the y-axis
        maxvalue = pmaxvalue;
        stepsize = pstepsize; //x-axis step size
    }

    /**
     * Returns a panel which can be displayed that contains the graph numbers
     * panel and a horz scrollbar at the bottom that makes changes viewing area easy
     * @return Basically, what you want to display to the screen
     */
    public JPanel chalkboardAndBar() {
        JPanel temppanel = new JPanel();
        temppanel.setLayout(new BorderLayout());
        horzBar = new JScrollBar(JScrollBar.HORIZONTAL);
        horzBar.addAdjustmentListener(this);

        //init the scroll bar
        updateHorzBar();

        JPanel innertemppanel = new JPanel();
        innertemppanel.setLayout(new OverlayLayout(innertemppanel));
        innertemppanel.add(this);
        temppanel.add(innertemppanel, BorderLayout.NORTH);

        //we need to adjust the this panel's preferred size
        //by subtracting the horz scrollbar's size from it
        Dimension newDimen = parentPanel.getSize();
        newDimen.height = newDimen.height - horzBar.getPreferredSize().height;
        this.setPreferredSize(newDimen);

        temppanel.add(this, BorderLayout.NORTH);
        temppanel.add(horzBar, BorderLayout.SOUTH);

        return temppanel;
    }

    /**
     * This function updates the scroll bar as new
     * numbers are added to the vector or if we decided to
     * jump to a certian value
     * Synchronized because GUI thread and paintthread will access the horz bar
     */
    public synchronized void updateHorzBar() {
        int newExtent = this.getSize().width / stepsize;

        //to handle the case where we really don't need the bar
        if (privateNumbers.size() < newExtent) {
            //then we just have a scroll bar that does nothing
            horzBar.setValues(0, 0, 0, 0);
            return;
        }

        int newValue = horzBar.getValue();
        //we check to see if the bar is current at it's maximum...if so
        //then keep it that way despite adding new values
        if (horzBar.getValue() + horzBar.getModel().getExtent() == horzBar.getMaximum()) {
            newValue = privateNumbers.size() - newExtent;
        }
        horzBar.setValues(newValue, newExtent, 0, privateNumbers.size());
    }

    /**
     * used by paint so it knows what value to start painting with
     */
    private int getHorzBarValue() {
        //Note: does this need to be synched?
        //Right now, no.
        return horzBar.getValue();
    }

    //Every option you can set has:
    //a) a getmethod which returns it's value
    //b) a setmethod which sets it's value
    //c) a visualSet method which returns a component that can be used to adjust/view its value
    //   (listeners are already set up and handeled by this class)

     /**
     * @return stepsize value
     */
    public int getStepSize() {
        return stepsize;
    }

    /**
     * @param pstepsize The value that stepsize should be set to
     */
    public void setStepSize(int pstepsize) {
        stepsize = pstepsize;
    }

    /**
     * This is called to get the visual widget that the user can set step 
     * size with.
     * @return A panel containing a spinner that controls stepsize value
     */
    public JPanel visualSetStepSize() {
        if (stepsizeVisual == null) {
            createstepsizeVisual();
        }
        JPanel returnthis = new JPanel();
        returnthis.setLayout(new BorderLayout());
        JLabel stepSizeLabel = new JLabel("X-Axis Step Size: ");
        returnthis.add(stepSizeLabel, BorderLayout.WEST);
        JSpinner spinit = new JSpinner(stepsizeVisual);
        spinit.setPreferredSize(new Dimension(80, 20));
        returnthis.add(spinit, BorderLayout.EAST);
        returnthis.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return returnthis;
    }

    private void createstepsizeVisual() {
        stepsizeVisual = new SpinnerNumberModel();
        stepsizeVisual.setValue(new Integer(stepsize));
        stepsizeVisual.setMinimum(new Integer(1));
        stepsizeVisual.addChangeListener(this);
    }

     /**
     * @return y-axis max value
     */
    public int getMaxValue() {
        return maxvalue;
    }

    /**
     * @param pmaxvalue The value that maxvalue should be set to
     */
    public void setMaxValue(int pmaxvalue) {
        maxvalue = pmaxvalue;
    }

   /**
    * This is called to get the visual widget that the user can set y-axis
    * max value with.
    * @return A panel containing a spinner that controls maxvalue value
   */
    public JPanel visualSetMaxValue() {
        if (maxvalueVisual == null) {
            createmaxvalueVisual();
        }
        JPanel returnthis = new JPanel();
        returnthis.setLayout(new BorderLayout());
        JLabel maxvalueLabel = new JLabel("Y-Axis max value");
        returnthis.add(maxvalueLabel, BorderLayout.WEST);
        JSpinner spinit = new JSpinner(maxvalueVisual);
        spinit.setPreferredSize(new Dimension(80, 20));
        returnthis.add(spinit, BorderLayout.EAST);
        returnthis.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return returnthis;
    }

    private void createmaxvalueVisual() {
        maxvalueVisual = new SpinnerNumberModel();
        maxvalueVisual.setValue(new Integer(maxvalue));
        stepsizeVisual.setMinimum(new Integer(1));
        maxvalueVisual.addChangeListener(this);
    }

    /**
     * This function returns a panel that has all
     * the visual options aligned in a column
     * @return a panel that can be directly displayed to the screen
     */
    public JPanel getOptionsPanel() {
        JPanel allOptions = new JPanel();
        allOptions.setLayout(new GridLayout(10, 1));
        //allOptions.setLayout(new BorderLayout());
        allOptions.add(visualSetStepSize());
        allOptions.add(visualSetMaxValue());
        allOptions.add(new JPanel()); //filler so there is blank space
        return allOptions;
    }

    /**
     * Used in order to size thing correctly.  Should be called
     * right after the constructor is called
     */
    public void setParentPanel(JPanel pparentPanel) {
        parentPanel = pparentPanel;
    }

    /**
     * This function is called by fire methods inside a monitor.  It
     * physically adds data values that will be displayed upon
     * next update/repaint
     * @param anAddress the value for the time series data in question
     */
    public void addToVector(int anAddress) {
        synchronized (vSync) {
            publicNumbers.add(anAddress);
        }
    }

    /**
     * This function is called by paint and it does what is necessary
     * to update the privateNumbers vector
     * returns true if it actually got some numbers, otherwise returns false
     * It might also be called by paint thread
     */
    public boolean internalUpdate() {
        synchronized (vSync) {
            if (publicNumbers.size() == 0) {
                return false;
            }
            //so we need to take anything in private and move it to public

            //do the move
            try {
                privateNumbers.addAll(publicNumbers);
            } catch (OutOfMemoryError e) {
                //Note that it's possible to get an out of memory exception
                //elsewhere, but "most probably" it will be here
                Terminal.println("RAN OUT OF HEAP SPACE FOR MONITOR");
                Terminal.println("SIZE OF MONITORS VECTOR AT THE TIME: " + Integer.toString(privateNumbers.size()));
                //TODO: Find a stop sim function and use it here
                //vAction.stopSim();
            }

            publicNumbers.removeAllElements();
        }

        //and update the horz scroll bar to reflect the new values
        updateHorzBar();
        return true;
    }

    /**
     * This actually paints the graph...note that it repaints the whole graph
     * everytime its called (to improve performance, we could make use of an update function)
     * The code here is actually faily ugly
     * but eh..
     * @param g The graphic that represents the panel to be painted
     */
    public void paint(Graphics g) {

        Dimension panelDimen = this.getSize();

        //Set up background color, will erase all previous lines
        g.setColor(backColor);
        g.fillRect(0, 0, panelDimen.width, panelDimen.height);


        //Let's draw all the lines

        //the y coordinate will be multiplied by the following scaling factor
        double scalingfactor = ((double) panelDimen.height) / ((double) (maxvalue - minvalue));
        int eofpx = 0; //holds coorinates of last line we drew
        int eofpy = 0;
        boolean firstone = true;  //the first line is a special case

        //we have to look up the starting value for the x-axis
        int startingvalue = getHorzBarValue();
        for (int i = startingvalue; i < startingvalue + panelDimen.width / stepsize && i < privateNumbers.size(); i++)
                //for (Enumeration e = privateNumbers.elements(); e.hasMoreElements(); )
        {
            //Integer currentYPoint = (Integer)e.nextElement();
            Integer currentYPoint = new Integer(privateNumbers.get(i));
            double currentYPointdb = currentYPoint.doubleValue();
            if (firstone) {
                //the we don't draw the vertical line, we just draw the horzintal
                Double temploc = new Double(currentYPointdb * scalingfactor);
                eofpy = temploc.intValue();
                eofpx = 0;
                g.setColor(lineColor);
                g.drawLine(eofpx, eofpy, stepsize, eofpy);
                eofpx = stepsize;
                firstone = false;
            } else {
                //two lines (one horzintal and one vertical), using the previous point as a starting location
                Double temploc = new Double(currentYPointdb * scalingfactor);
                //vertical
                g.setColor(lineColor);
                g.drawLine(eofpx, eofpy, eofpx, temploc.intValue());
                //horz
                g.setColor(lineColor);
                g.drawLine(eofpx, temploc.intValue(), eofpx + stepsize, temploc.intValue());
                eofpy = temploc.intValue();
                eofpx = eofpx + stepsize;
            }
            //Let's draw some horzintal markers to make things snazzy
            if (eofpx / stepsize % xAxisMajorTickMark == 0) {
                g.setColor(tickColor);
                g.drawLine(eofpx, 0, eofpx, panelDimen.height);
            }
        }
    }

    /**
     * this function processes the monitor options and re-sets the internal variables appropiatly
     ** @param e Info about the event that happened
     */
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == stepsizeVisual) {
            stepsize = ((Integer) stepsizeVisual.getValue()).intValue();
            repaint();
        } else if (e.getSource() == maxvalueVisual) {
            maxvalue = ((Integer) maxvalueVisual.getValue()).intValue();
            repaint();
        }
    }

    /**
     * This function handles a user change to the scroll bar
     * @param e Info about the event that happened
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        repaint();
    }

    /**
     * We don't want to store millions of Integer, but we still want
     * an array that grows...so we define a MyVector class just for that
     */
    public class MyVector {
        int[] vec;
        int current;

        public MyVector() {
            vec = new int[100];
            current = 0;
        }

        public void add(int a) {
            if (current == vec.length) {
                //create a new array of double the size
                int[] vec2 = new int[vec.length * 2];
                System.arraycopy(vec, 0, vec2, 0, vec.length);
                vec = vec2;
            }
            vec[current] = a;
            current++;
        }

        public int get(int i) {
            if (i < current)
                return vec[i];
            else
                return 0; //this is sorta stupid, but we'll be careful
        }

        public void addAll(MyVector a) {
            for (int i = 0; i < a.size(); i++) {
                add(a.get(i));
            }
        }

        public int size() {
            return current;
        }

        public void removeAllElements() {
            vec = new int[100];
            current = 0;
        }
    }
}