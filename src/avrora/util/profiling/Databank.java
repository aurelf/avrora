/**
 * @author Ben L. Titzer
 **/
package avrora.util.profiling;

import avrora.util.Terminal;

import java.util.Vector;

/**
 * The Databank class is a static class that collects Database objects
 * and generates a report on all of them when the report() method is called.
 **/
public class Databank {

    private static Vector databases = new Vector(10);
    private static Vector visitors = new Vector(4);

    /**
     * Register a database with the databank
     * @param d - the database object to register
     **/
    public static void registerDatabase(Database d) {
        databases.add(d);
    }

    /**
     * Register a database with the databank
     * @param v - the visitor object to register
     **/
    public static void registerVisitor(DatabaseVisitor v) {
        visitors.add(v);
    }

    /**
     * Generate a textual report of the databases.
     **/
    public static void textReport() {
        int numfull = 0, numDatabases = databases.size();
        if (databases.size() == 0) {
            Terminal.print("\nDatabank collected no databases.\n");
            return;
        }

        // freeze all databases and count the number that have data.
        for (int cntr = 0; cntr < numDatabases; cntr++) {
            Database d = (Database) databases.get(cntr);
            d.freeze();

            if (!d.isEmpty()) {
                numfull++;
            }
        }

        // report number of full and empty databases
        Terminal.print("\nDatabank collected " + numfull + " non-empty databases of " + numDatabases + " total.\n");

        // generate reports for each database
        for (int cntr = 0; cntr < numDatabases; cntr++) {
            Database d = (Database) databases.get(cntr);
            if (!d.isEmpty()) {
                Terminal.print("\nDatabase: " + d.toString());
                Terminal.print("\n=======================================================================\n");
                d.textReport();
            }
        }
    }

    /**
     * Dispatch all visitors onto databases
     **/
    public static void dispatchVisitors() {
        int numDatabases = databases.size();
        int numVisitors = visitors.size();

        if (numDatabases == 0) return;

        // freeze the databases
        for (int cntr = 0; cntr < numDatabases; cntr++) {
            Database d = (Database) databases.get(cntr);
            d.processData();
        }

        // dispatch the visitors one at a time
        for (int v = 0; v < numVisitors; v++) {
            DatabaseVisitor visitor = (DatabaseVisitor) visitors.get(v);

            for (int cntr = 0; cntr < numDatabases; cntr++) {
                Database d = (Database) databases.get(cntr);
                visitor.visit(d);
            }
        }
    }

    /**
     * Freeze all the databases.
     **/
    public static void freeze() {
        int numDatabases = databases.size();
        // freeze the databases
        for (int cntr = 0; cntr < numDatabases; cntr++) {
            Database d = (Database) databases.get(cntr);
            d.freeze();
        }
    }
}

