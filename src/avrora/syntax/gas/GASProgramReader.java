package avrora.syntax.gas;

import avrora.core.Program;
import avrora.syntax.gas.GASParser;
import avrora.Main;
import avrora.Module;
import avrora.Avrora;

import java.io.File;
import java.io.FileInputStream;

import vpc.VPCBase;

/**
 * The <code>GASProgramReader</code> is an implementation of the <code>ProgramReader</code>
 * that reads a source program in the GAS-style syntax and builds a program from it.
 * @author Ben L. Titzer
 */
public class GASProgramReader extends Main.ProgramReader {

    /**
     * The <code>read()</code> method accepts a list of filenames as strings, loads
     * them, resolves symbols, and produces a simplified program.
     * @param args the string names of the files to load
     * @return a program built from the specified source files
     * @throws avrora.syntax.gas.ParseException if a parse error is encountered
     * @throws java.io.IOException if there is a problem reading from one of
     * the files
     */
    public Program read(String[] args) throws Exception {
        if ( args.length == 0 )
            Avrora.userError("no input files");
        // TODO: handle multiple GAS files and link them
        if ( args.length != 1 )
            Avrora.userError("input type \"gas\" accepts only one file at a time.");

        File f = new File(args[0]);
        Module module = new Module();
        FileInputStream fis = new FileInputStream(f);
        GASParser parser = new GASParser(fis, module, f.getName());
        parser.Module();
        return module.build();
    }
}
