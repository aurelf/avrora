/**
 * Created on 13.11.2004
 *
 * Copyright (c) 2004, Olaf Landsiedel, Protocol Engineering and
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
package avrora.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * @author Olaf Landsiedel
 */
public class Visual {

    private static Socket socket;
    private static OutputStream out;
    private static boolean connected;

    public static void connect(String host) {
        //syntax: host:port
        StringTokenizer st = new StringTokenizer(host, ":");
        String addr = st.nextToken();
        String port = st.nextToken();
        try {
            socket = new Socket(addr, Integer.parseInt(port));
            out = socket.getOutputStream();
            connected = true;
        } catch (IOException e) {
            System.out.println("Cannot connect to visual host " + addr + ", port " + port);
            System.out.println("will continue anyway...");
            e.printStackTrace();
        }
    }

    public static void close() {
        if (connected) {
            try {
                out.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("Cannot close connection for visualization");
                System.out.println("will continue anyway...");
                e.printStackTrace();
            }
        }
    }

    public static synchronized void send(int node, String messageType, String m, int[] mA) {
        if (connected) {
            String send = node + " " + messageType + " " + m;
            for (int i = 0; i < mA.length; i++) {
                send += " " + mA[i];
            }
            send += "\n";
            out(send);
        }
    }

    public static synchronized void send(int node, String messageType, String message) {
        if (connected) {
            out(node + " " + messageType + " " + message + "\n");
        }
    }

    public static synchronized void send(int node, String messageType, int message) {
        if (connected) {
            out(node + " " + messageType + " " + message + "\n");
        }
    }

    public static synchronized void send(int node, String messageType, String m1, String m2) {
        if (connected) {
            out(node + " " + messageType + " " + m1 + " " + m2 + "\n");
        }
    }

    public static synchronized void send(int node, String messageType, int m1, int m2) {
        if (connected) {
            out(node + " " + messageType + " " + m1 + " " + m2 + "\n");
        }
    }

    private static synchronized void out(String data) {
        try {
            out.write(data.getBytes());
        } catch (IOException e) {
            System.out.println("cannot write data");
            e.printStackTrace();
            System.exit(1);
        }

    }
}
