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

package avrora.util.profiling;

import avrora.util.Terminal;

import java.util.Vector;

/**
 * The Databank class is a static class that collects Database objects and generates a report on all of them
 * when the report() method is called.
 *
 * @author Ben L. Titzer
 */
public class Databank {

    private static Vector databases = new Vector(10);
    private static Vector visitors = new Vector(4);

    /**
     * Register a database with the databank
     *
     * @param d - the database object to register
     */
    public static void registerDatabase(Database d) {
        databases.add(d);
    }

    /**
     * Register a database with the databank
     *
     * @param v - the visitor object to register
     */
    public static void registerVisitor(DatabaseVisitor v) {
        visitors.add(v);
    }

    /**
     * Generate a textual report of the databases.
     */
    public static void textReport() {
        int numfull = 0, numDatabases = databases.size();
        if (databases.size() == 0) {
            Terminal.print("\nDatabank collected no databases.\n");
            return;
        }

        // freeze all databases and count the number that have data.
        for (int cntr = 0; cntr < numDatabases; cntr++) {
            Database d = (Database)databases.get(cntr);
            d.freeze();

            if (!d.isEmpty()) {
                numfull++;
            }
        }

        // report number of full and empty databases
        Terminal.print("\nDatabank collected " + numfull + " non-empty databases of " + numDatabases + " total.\n");

        // generate reports for each database
        for (int cntr = 0; cntr < numDatabases; cntr++) {
            Database d = (Database)databases.get(cntr);
            if (!d.isEmpty()) {
                Terminal.print("\nDatabase: " + d.toString());
                Terminal.print("\n=======================================================================\n");
                d.textReport();
            }
        }
    }

    /**
     * Dispatch all visitors onto databases
     */
    public static void dispatchVisitors() {
        int numDatabases = databases.size();
        int numVisitors = visitors.size();

        if (numDatabases == 0) return;

        // freeze the databases
        for (int cntr = 0; cntr < numDatabases; cntr++) {
            Database d = (Database)databases.get(cntr);
            d.processData();
        }

        // dispatch the visitors one at a time
        for (int v = 0; v < numVisitors; v++) {
            DatabaseVisitor visitor = (DatabaseVisitor)visitors.get(v);

            for (int cntr = 0; cntr < numDatabases; cntr++) {
                Database d = (Database)databases.get(cntr);
                visitor.visit(d);
            }
        }
    }

    /**
     * Freeze all the databases.
     */
    public static void freeze() {
        int numDatabases = databases.size();
        // freeze the databases
        for (int cntr = 0; cntr < numDatabases; cntr++) {
            Database d = (Database)databases.get(cntr);
            d.freeze();
        }
    }
}

