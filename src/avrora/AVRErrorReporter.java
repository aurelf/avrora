package avrora;

import avrora.core.Register;
import avrora.core.Operand;
import avrora.util.StringUtil;
import avrora.ErrorReporter;
import avrora.syntax.*;

/**
 * The <code>AVRErrorReporter</code> contains one method per compilation
 * error. The method constructs a <code>CompilationError</code> object
 * that represents the error and throws it. One method per type of error
 * provides a convenient interface and allows pinpointing the generation
 * of each type of error within the verifier.
 * 
 * @author Ben L. Titzer
 */
public class AVRErrorReporter extends ErrorReporter {

    private ProgramPoint point(AbstractToken t) {
        return new ProgramPoint(t.file, t.beginLine, t.beginColumn, t.endColumn);
    }

    private ProgramPoint point(ASTNode n) {
        AbstractToken l = n.getLeftMostToken();
        AbstractToken r = n.getRightMostToken();
        return new ProgramPoint(l.file, l.beginLine, l.beginColumn, r.endColumn);
    }

    public void UnknownRegister(AbstractToken reg) {
        String report = "unknown register " + StringUtil.quote(reg);
        error(report, "UnknownRegister", point(reg));
    }

    public void InstructionCannotBeInSegment(String seg, AbstractToken instr) {
        String report = "instructions cannot be declared in " + seg + " cseg";
        error(report, "InstructionCannotBeInSegment", point(instr));
    }

    public void UnknownInstruction(AbstractToken instr) {
        String report = "unknown instruction " + StringUtil.quote(instr);
        error(report, "UnknownInstruction", point(instr));
    }

    public void RegisterExpected(SyntacticOperand o) {
        String report = "register expected";
        error(report, "RegisterExpected", point(o));
    }

    public void IncorrectRegister(SyntacticOperand o, Register reg, String expect) {
        String report = "incorrected register " + StringUtil.quote(reg) + ", expected one of " + expect;
        error(report, "IncorrectRegister", point(o));
    }

    public void ConstantExpected(SyntacticOperand o) {
        String report = "constant expected";
        error(report, "ConstantExpected", point(o));
    }

    public void ConstantOutOfRange(SyntacticOperand o, int value, String range) {
        String report = "constant " + StringUtil.quote("" + value) + " out of expected range " + range;
        error(report, "ConstantOutOfRange", "" + value, point(o));
    }

    public void WrongNumberOfOperands(AbstractToken instr, int seen, int expected) {
        String report = "wrong number of operands to instruction " + StringUtil.quote(instr) + ", expected "
                + expected + " and found " + seen;
        error(report, "WrongNumberOfOperands", point(instr));
    }

    public void UnknownVariable(AbstractToken name) {
        String report = "unknown variable or label " + StringUtil.quote(name.image);
        error(report, "UnknownVariable", name.image, point(name));
    }

    public void DataCannotBeInSegment(ExprList l) {
        String report = "initialized data cannot be in data cseg";
        error(report, "DataCannotBeInSegment", point(l));
    }

    public void IncludeFileNotFound(AbstractToken tok) {
        String report = "include file not found " + tok;
        error(report, "IncludeFileNotFound", point(tok));
    }
}
