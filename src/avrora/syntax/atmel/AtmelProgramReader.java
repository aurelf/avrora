package avrora.syntax.atmel;

import avrora.core.Program;
import avrora.syntax.atmel.AtmelParser;
import avrora.Main;
import avrora.Module;

import java.io.File;
import java.io.FileInputStream;

import vpc.VPCBase;

/**
 * The <code>AtmelProgramReader</code> is an implementation of the <code>ProgramReader</code>
 * that reads source assembly files in the Atmel style syntax. It can handle only one file
 * at a time.
 * @author Ben L. Titzer
 */
public class AtmelProgramReader extends Main.ProgramReader {

    /**
     * The <code>read()</code> method takes the command line arguments passed to
     * main and interprets it as a list of filenames to load. It expects only one
     * filename to be present. It will load, parse, and simplify the program and
     * return it.
     * @param args the string arguments representing the names of the files to read
     * @return a program obtained by parsing and building the file
     * @throws avrora.syntax.atmel.ParseException if the file does not parse correctly
     * @throws java.io.IOException if there is a problem reading from the files
     */
    public Program read(String[] args) throws Exception {
        if ( args.length == 0 )
            VPCBase.userError("no input files");
        if ( args.length != 1 )
            VPCBase.userError("input type \"atmel\" accepts only one file at a time.");

        File f = new File(args[0]);
        Module module = new Module();
        FileInputStream fis = new FileInputStream(f);
        AtmelParser parser = new AtmelParser(fis, module, f.getName());
        parser.Module();
        return module.build();
    }
}
