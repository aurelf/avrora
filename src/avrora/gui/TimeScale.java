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

import avrora.Avrora;
import avrora.util.StringUtil;

import java.awt.*;

/**
 * The <code>TimeScale</code> class handles the conversion of time scales in displaying timing windows
 * within the GUI simulation. It has an internal notion of the scale and the start time. It has methods
 * to render a scale bar and to convert a time scale value (in cycles) to an X coordinate in the drawing
 * rectangle.
 *
 * @author Ben L. Titzer
 */
public class TimeScale {
    final int height;
    long startTime;
    long hz;
    final Color backgroundColor;
    final Color borderColor;
    final Color tickColor;

    ZoomLevel[] zooms;
    int zoom;
    protected static final double ONE_BILLION = 1000000000;

    TimeScale() {
        height = 30;
        backgroundColor = Color.GRAY;
        borderColor = Color.WHITE;
        tickColor = Color.RED;
        hz = 7372800;
        zoom = 0;
        buildZooms();
    }

    private void buildZooms() {
        zooms = new ZoomLevel[3];
        zooms[0] = new ZoomLevel();
        zooms[0].scale = 2;
        zooms[0].pos = 7;
        zooms[0].dec = 2;
        zooms[0].nsecs = 10000;
        zooms[0].units = "msec";
        zooms[1] = new ZoomLevel();
        zooms[1].scale = 1;
        zooms[1].pos = 7;
        zooms[1].dec = 2;
        zooms[1].nsecs = 10000;
        zooms[1].units = "msec";
        zooms[2] = new ZoomLevel();
        zooms[2].scale = 0.5;
        zooms[2].pos = 7;
        zooms[2].dec = 2;
        zooms[2].nsecs = 10000;
        zooms[2].units = "msec";
    }

    class ZoomLevel {
        double scale; // scales in cycles per pixel
        int dec;      // decimal positions within the unit
        int pos;      // decimal position in seconds (e.g. milliseconds)
        long nsecs;    // 10^pos
        String units; // string unit name (e.g. "ms")
    }

    public void drawScale(Dimension dim, Graphics g) {
        g.setColor(backgroundColor);
        int y = dim.height - height;
        int my = (int)(y + height * 0.6);
        int medy = (int)(y + height * 0.4);
        g.fillRect(0, y, dim.width, dim.height);
        g.setColor(borderColor);
        g.drawLine(0, y, dim.width, y);

        g.setColor(tickColor);

        double majorTickWidth = getMajorTickWidth();
        double minorTickWidth = majorTickWidth / 10;
        int tick = 1;
        double max = dim.width + majorTickWidth;
        for ( double pos = getFirstTick(majorTickWidth); pos < max; pos += majorTickWidth ) {
            // draw the sub-ticks for this label
            for ( int mt = 1; mt < 10; mt++ ) {
                int mx = (int)(pos + minorTickWidth*mt);
                if ( mt == 5 )
                    g.drawLine(mx, medy, mx, dim.height);
                else
                    g.drawLine(mx, my, mx, dim.height);
            }
            // draw the string label for this tick
            int xpos = (int)pos;
            drawTickLabel(tick, g, xpos, y);
            // draw the line from top to bottom
            g.drawLine(xpos, 0, xpos, dim.height);
            tick++;
        }
    }

    private int getFirstTick(double majWidth) {
        ZoomLevel zl = getZoomLevel();
        double startPix = startTime / zl.scale;
        return (int)(-(startPix % majWidth));
    }

    private double getMajorTickWidth() {
        ZoomLevel zl = getZoomLevel();
        return (hz * zl.nsecs / ONE_BILLION) / zl.scale;
    }

    private void drawTickLabel(int tick, Graphics g, int cntr, int y) {
        FontMetrics m = g.getFontMetrics();
        String str = tick+" "+getZoomLevel().units;
        int width = m.stringWidth(str);
        g.drawString(str, cntr - width - 3, y + 12);
    }

    private ZoomLevel getZoomLevel() {
        return zooms[zoom];
    }

    int divs[] = { 10, 10, 10, 10, 10, 10, 10, 10, 10 };

    public int getX(long time) {
        if ( time < startTime ) return -1;
        return (int)((time - startTime) / getZoomLevel().scale);
    }

    public void zoomin() {
        if ( zoom < zooms.length-1 ) zoom++;
    }

    public void zoomout() {
        if ( zoom > 0 ) zoom--;
    }

    public int getExtent(int width, long maxtime) {
        return width;
    }

    public int getScrollBarSize(long maxtime) {
        return (int)(maxtime / getZoomLevel().scale);
    }

    public void setPosition(int np) {
        startTime = (long)(np * getZoomLevel().scale);
    }
}
