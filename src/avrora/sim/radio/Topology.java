/**
 * Created on 02.11.2004
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

package avrora.sim.radio;

import cck.util.Util;
import java.io.*;
import java.util.*;

/**
 * handles node positions.
 *
 * @author Olaf Landsiedel
 */
public class Topology {

    //structure of the node positions
    private final ArrayList nodes;

    /**
     * new topology
     *
     * @param fileName file to parse for topology
     */
    public Topology(String fileName) throws IOException {
        nodes = new ArrayList();
        parse(new BufferedReader(new FileReader(fileName)));
    }

    private void parse(BufferedReader f) throws IOException {
        String line;
        while ((line = f.readLine()) != null) {
            parseLine(line);
        }
        f.close();
    }

    /**
     * parse one line of the file
     *
     * @param line
     */
    private void parseLine(String line) {
        String nodeName = "";
        int[] positions = new int[3];

        //check for comment
        if (!line.startsWith("#")) {
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            int count = 0;
            while (tokenizer.hasMoreTokens() && count < 4) {
                try {
                    if (count == 0)
                        nodeName = tokenizer.nextToken();
                    else {
                        positions[count - 1] = Integer.parseInt(tokenizer.nextToken());
                    }
                    count++;
                } catch (NoSuchElementException e) {
                    throw Util.failure("Error reading topology tokens");
                }
            }
            if (count == 4) {
                //parsing of this line went well -> found 4 tokens
                nodes.add(new RadiusModel.Position(positions[0], positions[1], positions[2]));
            }
        }
    }

    public RadiusModel.Position getPosition(int id) {
        return ((RadiusModel.Position)nodes.get(id));
    }
}
