package avrora.core.isdl;

import avrora.util.Verbose;

import java.util.HashMap;

/**
 * @author Ben L. Titzer
 */
public class Architecture {

    Verbose.Printer printer = Verbose.getVerbosePrinter("isdl");

    HashMap subroutines;
    HashMap instructions;
    HashMap operands;
    HashMap encodings;

    public Architecture() {
        subroutines = new HashMap();
        instructions = new HashMap();
        operands = new HashMap();
        encodings = new HashMap();
    }

    public void addSubroutine(SubroutineDecl d) {
        printer.println("loading subroutine "+d.name.image+"...");
        subroutines.put(d.name.image, d);
    }

    public void addInstruction(InstrDecl i) {
        printer.println("loading instruction "+i.name.image+"...");
        instructions.put(i.name.image, i);
    }

    public void addOperand(OperandDecl d) {
        printer.println("loading operand declaration "+d.name.image+"...");
        operands.put(d.name.image, d);
    }

    public void addEncoding(EncodingDecl d) {
        printer.println("loading encoding format "+d.name.image+"...");
        encodings.put(d.name.image, d);
    }
}
