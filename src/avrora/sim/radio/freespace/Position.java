/**
 * Created on 29.10.2004
 *
 * Copyright (c) 2004-2005, Olaf Landsiedel, Protocol Engineering and
 * Distributed Systems, University of Tuebingen
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
 * Neither the name of the Protocol Engineering and Distributed Systems
 * Group, the name of the University of Tuebingen nor the names of its 
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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

package avrora.sim.radio.freespace;

/**
 * Represents a three dim. position
 *
 * @author Olaf Landsiedel
 */
public class Position {

    private int x;
    private int y;
    private int z;
    private String nodeName;

    /**
     * new position
     *
     * @param x
     * @param y
     * @param z
     */
    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.nodeName = "";
    }

    /**
     * new position, including node name
     *
     * @param name node name
     * @param x
     * @param y
     * @param z
     */
    public Position(String name, int x, int y, int z) {
        this.nodeName = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * get x coordinate
     *
     * @return x
     */
    public int getX() {
        return x;
    }

    /**
     * get y coordinate
     *
     * @return y
     */
    public int getY() {
        return y;
    }

    /**
     * get z coordinate
     *
     * @return z
     */
    public int getZ() {
        return z;
    }

    /**
     * get node name
     *
     * @return node name
     */
    public String getNodeName() {
        return nodeName;
    }
}
