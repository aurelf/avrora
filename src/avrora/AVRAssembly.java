package avrora;

import vpc.Compilation;
import vpc.CompilationError;
import vpc.VPCBase;
import avrora.sim.Simulator;
import avrora.sim.mcu.ATMega128L;
import avrora.syntax.atmel.ParseException;
import avrora.syntax.atmel.Token;
import avrora.syntax.atmel.AtmelParser;
import vpc.core.Program;
import vpc.core.ProgramPoint;
import vpc.core.AbstractToken;
import vpc.core.AbstractParseException;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

/**
 * @author Ben L. Titzer
 */
public class AVRAssembly extends VPCBase {

    private static AtmelParser parser;

    /**
     * The <code>ParseStage</code> class implements the phase of compilation
     * relating to parsing the source file and building the core program
     * representation for later phases of the compilation.
     * @author Ben L. Titzer
     */
    public static class ParseStage extends Compilation.Stage {
        public ParseStage() {
            super("avr-parse");
        }

        public void visitProgram(Program p) {
            try {
                p.acceptFileVisitor(new ParseStageVisitor(p));
            } catch (IOException e) {
                userError("IOException: " + e.getMessage());
            }
        }

    }

    public static class ParseStageVisitor implements Program.FileVisitor {
        private final Program program;

        ParseStageVisitor(Program p) {
            program = p;
        }

        public void visit(File f) throws java.io.IOException {

            FileInputStream fis = new FileInputStream(f);
            Module module = new Module();
            AtmelParser parser = new AtmelParser(fis, module, f.getName());

            try {
                parser.Module();
                avrora.core.Program p = module.build();
                Simulator s = new ATMega128L().loadProgram(p);
                s.start();
            } catch (AbstractParseException e) {
                AbstractToken tok = e.currentToken.getNextToken();
                String report = "parse error at token " + quote(tok) + "\n" + e.getMessage();
                throw new CompilationError(e.programPoint, report, "ParseError", EMPTY_STRING_ARRAY);
            }
        }
    }

}
