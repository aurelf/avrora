package avrora.arch.msp430;
import java.util.HashMap;

/**
 * The <code>MSP430AddrModeVisitor</code> interface implements the
 * visitor pattern for addressing modes.
 */
public interface MSP430AddrModeVisitor {
    public void visit_REG(MSP430Operand.SREG source);
    public void visit_REGREG(MSP430Operand.SREG source, MSP430Operand.SREG dest);
    public void visit_REGIND(MSP430Operand.SREG source, MSP430Operand.INDX dest);
    public void visit_REGSYM(MSP430Operand.SREG source, MSP430Operand.SYM dest);
    public void visit_REGABS(MSP430Operand.SREG source, MSP430Operand.ABSO dest);
    public void visit_IND(MSP430Operand.INDX source);
    public void visit_INDREG(MSP430Operand.INDX source, MSP430Operand.SREG dest);
    public void visit_INDIND(MSP430Operand.INDX source, MSP430Operand.INDX dest);
    public void visit_SYM(MSP430Operand.SYM source);
    public void visit_SYMREG(MSP430Operand.SYM source, MSP430Operand.SREG dest);
    public void visit_INDSYM(MSP430Operand.INDX source, MSP430Operand.SYM dest);
    public void visit_INDABS(MSP430Operand.INDX source, MSP430Operand.ABSO dest);
    public void visit_SYMABS(MSP430Operand.SYM source, MSP430Operand.ABSO dest);
    public void visit_SYMIND(MSP430Operand.SYM source, MSP430Operand.INDX dest);
    public void visit_SYMSYM(MSP430Operand.SYM source, MSP430Operand.SYM dest);
    public void visit_ABSSYM(MSP430Operand.ABSO source, MSP430Operand.SYM dest);
    public void visit_ABS(MSP430Operand.ABSO source);
    public void visit_ABSREG(MSP430Operand.ABSO source, MSP430Operand.SREG dest);
    public void visit_ABSIND(MSP430Operand.ABSO source, MSP430Operand.INDX dest);
    public void visit_ABSABS(MSP430Operand.ABSO source, MSP430Operand.ABSO dest);
    public void visit_IREGSYM(MSP430Operand.IREG source, MSP430Operand.SYM dest);
    public void visit_IREG(MSP430Operand.IREG source);
    public void visit_IREGREG(MSP430Operand.IREG source, MSP430Operand.SREG dest);
    public void visit_IREGIND(MSP430Operand.IREG source, MSP430Operand.INDX dest);
    public void visit_IREGABS(MSP430Operand.IREG source, MSP430Operand.ABSO dest);
    public void visit_IMM(MSP430Operand.IMM source);
    public void visit_IMMREG(MSP430Operand.IMM source, MSP430Operand.SREG dest);
    public void visit_IMMIND(MSP430Operand.IMM source, MSP430Operand.INDX dest);
    public void visit_IMMSYM(MSP430Operand.IMM source, MSP430Operand.SYM dest);
    public void visit_IMMABS(MSP430Operand.IMM source, MSP430Operand.ABSO dest);
    public void visit_AUTO_B(MSP430Operand.AIREG_B source);
    public void visit_AUTOREG_B(MSP430Operand.AIREG_B source, MSP430Operand.SREG dest);
    public void visit_AUTOIND_B(MSP430Operand.AIREG_B source, MSP430Operand.INDX dest);
    public void visit_AUTOSYM_B(MSP430Operand.AIREG_B source, MSP430Operand.SYM dest);
    public void visit_AUTOABS_B(MSP430Operand.AIREG_B source, MSP430Operand.ABSO dest);
    public void visit_AUTO_W(MSP430Operand.AIREG_W source);
    public void visit_AUTOREG_W(MSP430Operand.AIREG_W source, MSP430Operand.SREG dest);
    public void visit_AUTOIND_W(MSP430Operand.AIREG_W source, MSP430Operand.INDX dest);
    public void visit_AUTOSYM_W(MSP430Operand.AIREG_W source, MSP430Operand.SYM dest);
    public void visit_AUTOABS_W(MSP430Operand.AIREG_W source, MSP430Operand.ABSO dest);
    public void visit_JMP(MSP430Operand.JUMP source);
    public void visit_$clrc$();
    public void visit_$clrn$();
    public void visit_$clrz$();
    public void visit_$dint$();
    public void visit_$eint$();
    public void visit_$nop$();
    public void visit_$ret$();
    public void visit_$reti$();
    public void visit_$setc$();
    public void visit_$setn$();
    public void visit_$setz$();
}
