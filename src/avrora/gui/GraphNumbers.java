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
import avrora.util.profiling.Measurements;
import avrora.actions.VisualAction;
import avrora.Avrora;

/**
 * The class assists visual monitors with graphing time-series data
 * values.  It visually displays them using a line graph
 */
public class GraphNumbers extends JPanel implements ChangeListener, AdjustmentListener {

    private Measurements publicNumbers; //access by monitors to add stuff
    private Measurements privateNumbers; //only accessed by paint
    private final JPanel parentPanel;
    
    private Object vSync; //just a private sync variable

    /**
    * This is the bar that determines what part of
    * the graph is displayed
    */
    public JScrollBar horzBar;

    TimeScale timeScale;

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
    private Color cursorColor;
    private int minvalue;

    //Other features to add:
    //ability to user this class "on top of" another GraphNumbers class => multiple lines on one graph
    //ability to see/get the value of the line based upon a mouse over/mouse click event
    //double check to see if scroll bar is sizing correctly

    /**
     * Called by a visual action that wants this class to help with displaying time series data
     * @param pminvalue The min value for the y-axis
     * @param pmaxvalue The max value for the y-axis
     */
    public GraphNumbers(JPanel parent, int pminvalue, int pmaxvalue) {

        parentPanel = parent;

        vSync = new Object();

        publicNumbers = new Measurements();
        privateNumbers = new Measurements();

        //Set option defaults
        lineColor = Color.GREEN; //default line color is green
        backColor = Color.BLACK; //default background color is black
        cursorColor = Color.CYAN;
        minvalue = pminvalue; //min and max values for the y-axis
        maxvalue = pmaxvalue;
        timeScale = new TimeScale();
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

        int width = this.getSize().width;
        long maxtime = privateNumbers.size();
        int newExtent = timeScale.getExtent(width, maxtime);
        int size = timeScale.getScrollBarSize(maxtime);
        //to handle the case where we really don't need the bar
        if (size < newExtent) {
            //then we just have a scroll bar that does nothing
            horzBar.setValues(0, 0, 0, 0);
            return;
        }

        int newValue = horzBar.getValue();
        //we check to see if the bar is current at it's maximum...if so
        //then keep it that way despite adding new values
        if (newValue + horzBar.getModel().getExtent() == horzBar.getMaximum()) {
            newValue = size - newExtent;
        }

        horzBar.setValues(newValue, newExtent, 0, size);
    }

    /**
     * This is called to get the visual widget that the user can set step 
     * size with.
     * @return A panel containing a spinner that controls stepsize value
     */
    public JPanel getZoomLevelOption() {
        if (stepsizeVisual == null) {
            makeZoomLevelOption();
        }
        JPanel returnthis = new JPanel();
        returnthis.setLayout(new BorderLayout());
        JLabel stepSizeLabel = new JLabel("Zoom Level: ");
        returnthis.add(stepSizeLabel, BorderLayout.WEST);
        JSpinner spinit = new JSpinner(stepsizeVisual);
        spinit.setPreferredSize(new Dimension(80, 20));
        returnthis.add(spinit, BorderLayout.EAST);
        returnthis.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return returnthis;
    }

    private void makeZoomLevelOption() {
        stepsizeVisual = new SpinnerNumberModel();
        stepsizeVisual.setValue(new Integer(timeScale.getZoom()+1));
        stepsizeVisual.setMinimum(new Integer(1));
        stepsizeVisual.setMaximum(new Integer(timeScale.getMaxZoom()+1));
        stepsizeVisual.addChangeListener(this);
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
        allOptions.add(getZoomLevelOption());
        allOptions.add(visualSetMaxValue());
        allOptions.add(new JPanel()); //filler so there is blank space
        return allOptions;
    }

    /**
     * This function is called by fire methods inside a monitor.  It
     * physically adds data values that will be displayed upon
     * next update/repaint
     * @param number the value for the time series data in question
     */
    public void recordNumber(int number) {
        synchronized (vSync) {
            publicNumbers.add(number);
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

            privateNumbers.addAll(publicNumbers);
            int max = privateNumbers.max();
            if ( max > maxvalue ) maxvalue = max;
            publicNumbers = new Measurements();
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

        //Note: does this need to be synched?
        //Right now, no.
        timeScale.setPosition(horzBar.getValue());
        timeScale.drawScale(panelDimen, g);
        long startTime = timeScale.getStartTime();

        //Let's draw all the lines

        //the y coordinate will be multiplied by the following scaling factor
        int maxheight = panelDimen.height - timeScale.height;
        double scalingfactor = maxheight / ((double) (maxvalue - minvalue));
        int eofpx = 0; //holds coorinates of last line we drew
        int eofpy = 0;
        boolean firstone = true;  //the first line is a special case

        //we have to look up the starting value for the x-axis
        Measurements.Iterator mi = privateNumbers.iterator((int)startTime);
        int max = (int)(startTime + panelDimen.width * timeScale.getScale());
        g.setColor(lineColor);
        for (long i = startTime; mi.hasNext() && i < max ; i++)
        {
            double currentYPointdb = mi.next();
            if (firstone) {
                //the we don't draw the vertical line, we just draw the horzintal
                eofpy = (int)(currentYPointdb * scalingfactor);
                eofpx = timeScale.getX(i);
                g.drawLine(0, eofpy, eofpx, eofpy);
                firstone = false;
            } else {
                //two lines (one horzintal and one vertical), using the previous point as a starting location
                int temploc = (int)(currentYPointdb * scalingfactor);
                //vertical
                int npx = timeScale.getX(i);
                g.drawLine(eofpx, eofpy, eofpx, temploc);
                //horz
                g.drawLine(eofpx, temploc, npx, temploc);
                eofpy = temploc;
                eofpx = npx;
            }
            if (!mi.hasNext() ) {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(eofpx, 0, panelDimen.width, panelDimen.height);
                g.setColor(cursorColor);
                g.drawLine(eofpx, 0, eofpx, maxheight);
            }
        }
    }

    /**
     * this function processes the monitor options and re-sets the internal variables appropiatly
     ** @param e Info about the event that happened
     */
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == stepsizeVisual) {
            adjustZoom();
            repaint();
        } else if (e.getSource() == maxvalueVisual) {
            maxvalue = ((Integer) maxvalueVisual.getValue()).intValue();
            repaint();
        }
    }

    private void adjustZoom() {
        int zoomlevel = ((Integer) stepsizeVisual.getValue()).intValue() - 1;
        timeScale.setZoom(zoomlevel);
        this.horzBar.setValue(timeScale.getPosition());
        this.updateHorzBar();
    }

    /**
     * This function handles a user change to the scroll bar
     * @param e Info about the event that happened
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        repaint();
    }

}
