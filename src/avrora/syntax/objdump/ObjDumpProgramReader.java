package avrora.syntax.objdump;

import avrora.Avrora;
import avrora.Main;
import avrora.syntax.Module;
import avrora.syntax.Module;
import avrora.core.Program;

import java.io.*;

/**
 * The <code>AtmelProgramReader</code> is an implementation of the <code>ProgramReader</code>
 * that reads source assembly files in the Atmel style syntax. It can handle only one file
 * at a time.
 *
 * @author Ben L. Titzer
 */
public class ObjDumpProgramReader extends Main.ProgramReader {

    /**
     * The <code>read()</code> method takes the command line arguments passed to
     * main and interprets it as a list of filenames to load. It expects only one
     * filename to be present. It will load, parse, and simplify the program and
     * return it.
     *
     * @param args the string arguments representing the names of the files to read
     * @return a program obtained by parsing and building the file
     * @throws avrora.syntax.objdump.ParseException
     *                             if the file does not parse correctly
     * @throws java.io.IOException if there is a problem reading from the files
     */
    public Program read(String[] args) throws Exception {
        if (args.length == 0)
            Avrora.userError("no input files");
        if (args.length != 1)
            Avrora.userError("input type \"objdump\" accepts only one file at a time.");

        File f = new File(args[0]);
        Module module = new Module(true, true);
        StringBuffer buf = new ObjDumpPreprocessor(args[0]).getCleanObjDumpCode();
        Reader r = new StringReader(buf.toString());

        ObjDumpParser parser = new ObjDumpParser(r, module, f.getName());
        parser.Module();
        return module.build();
    }
}
