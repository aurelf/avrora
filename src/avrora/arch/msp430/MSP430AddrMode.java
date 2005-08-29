package avrora.arch.msp430;
import java.util.HashMap;

/**
 * The <code>MSP430AddrMode</code> class represents an addressing mode
 * for this architecture. An addressing mode fixes the number and type of
 * operands, the syntax, and the encoding format of the instruction.
 */
public interface MSP430AddrMode {
    public void accept(MSP430AddrModeVisitor v);
    public interface DOUBLE_W extends MSP430AddrMode {
        public MSP430Operand get_source();
        public MSP430Operand get_dest();
    }
    public interface SINGLE_W extends MSP430AddrMode {
        public MSP430Operand get_source();
    }
    public interface DOUBLE_B extends MSP430AddrMode {
        public MSP430Operand get_source();
        public MSP430Operand get_dest();
    }
    public interface SINGLE_B extends MSP430AddrMode {
        public MSP430Operand get_source();
    }
    public static class REG implements MSP430AddrMode, SINGLE_W, SINGLE_B {
        public MSP430Operand.SREG source;
        public REG(MSP430Operand.SREG source) {
            this.source = source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_REG(source);
        }
        public MSP430Operand get_source() { return source; }
    }
    public static class REGREG implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.SREG source;
        public MSP430Operand.SREG dest;
        public REGREG(MSP430Operand.SREG source, MSP430Operand.SREG dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_REGREG(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class REGIND implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.SREG source;
        public MSP430Operand.INDX dest;
        public REGIND(MSP430Operand.SREG source, MSP430Operand.INDX dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_REGIND(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class REGSYM implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.SREG source;
        public MSP430Operand.SYM dest;
        public REGSYM(MSP430Operand.SREG source, MSP430Operand.SYM dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_REGSYM(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class REGABS implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.SREG source;
        public MSP430Operand.ABSO dest;
        public REGABS(MSP430Operand.SREG source, MSP430Operand.ABSO dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_REGABS(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class IND implements MSP430AddrMode, SINGLE_W, SINGLE_B {
        public MSP430Operand.INDX source;
        public IND(MSP430Operand.INDX source) {
            this.source = source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IND(source);
        }
        public MSP430Operand get_source() { return source; }
    }
    public static class INDREG implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.INDX source;
        public MSP430Operand.SREG dest;
        public INDREG(MSP430Operand.INDX source, MSP430Operand.SREG dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_INDREG(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class INDIND implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.INDX source;
        public MSP430Operand.INDX dest;
        public INDIND(MSP430Operand.INDX source, MSP430Operand.INDX dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_INDIND(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class SYM implements MSP430AddrMode, SINGLE_W, SINGLE_B {
        public MSP430Operand.SYM source;
        public SYM(MSP430Operand.SYM source) {
            this.source = source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_SYM(source);
        }
        public MSP430Operand get_source() { return source; }
    }
    public static class SYMREG implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.SYM source;
        public MSP430Operand.SREG dest;
        public SYMREG(MSP430Operand.SYM source, MSP430Operand.SREG dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_SYMREG(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class INDSYM implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.INDX source;
        public MSP430Operand.SYM dest;
        public INDSYM(MSP430Operand.INDX source, MSP430Operand.SYM dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_INDSYM(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class INDABS implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.INDX source;
        public MSP430Operand.ABSO dest;
        public INDABS(MSP430Operand.INDX source, MSP430Operand.ABSO dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_INDABS(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class SYMABS implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.SYM source;
        public MSP430Operand.ABSO dest;
        public SYMABS(MSP430Operand.SYM source, MSP430Operand.ABSO dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_SYMABS(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class SYMIND implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.SYM source;
        public MSP430Operand.INDX dest;
        public SYMIND(MSP430Operand.SYM source, MSP430Operand.INDX dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_SYMIND(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class SYMSYM implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.SYM source;
        public MSP430Operand.SYM dest;
        public SYMSYM(MSP430Operand.SYM source, MSP430Operand.SYM dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_SYMSYM(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class ABSSYM implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.ABSO source;
        public MSP430Operand.SYM dest;
        public ABSSYM(MSP430Operand.ABSO source, MSP430Operand.SYM dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_ABSSYM(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class ABS implements MSP430AddrMode, SINGLE_W, SINGLE_B {
        public MSP430Operand.ABSO source;
        public ABS(MSP430Operand.ABSO source) {
            this.source = source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_ABS(source);
        }
        public MSP430Operand get_source() { return source; }
    }
    public static class ABSREG implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.ABSO source;
        public MSP430Operand.SREG dest;
        public ABSREG(MSP430Operand.ABSO source, MSP430Operand.SREG dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_ABSREG(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class ABSIND implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.ABSO source;
        public MSP430Operand.INDX dest;
        public ABSIND(MSP430Operand.ABSO source, MSP430Operand.INDX dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_ABSIND(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class ABSABS implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.ABSO source;
        public MSP430Operand.ABSO dest;
        public ABSABS(MSP430Operand.ABSO source, MSP430Operand.ABSO dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_ABSABS(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class IREGSYM implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.IREG source;
        public MSP430Operand.SYM dest;
        public IREGSYM(MSP430Operand.IREG source, MSP430Operand.SYM dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IREGSYM(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class IREG implements MSP430AddrMode, SINGLE_W, SINGLE_B {
        public MSP430Operand.IREG source;
        public IREG(MSP430Operand.IREG source) {
            this.source = source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IREG(source);
        }
        public MSP430Operand get_source() { return source; }
    }
    public static class IREGREG implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.IREG source;
        public MSP430Operand.SREG dest;
        public IREGREG(MSP430Operand.IREG source, MSP430Operand.SREG dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IREGREG(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class IREGIND implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.IREG source;
        public MSP430Operand.INDX dest;
        public IREGIND(MSP430Operand.IREG source, MSP430Operand.INDX dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IREGIND(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class IREGABS implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.IREG source;
        public MSP430Operand.ABSO dest;
        public IREGABS(MSP430Operand.IREG source, MSP430Operand.ABSO dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IREGABS(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class IMM implements MSP430AddrMode, SINGLE_W, SINGLE_B {
        public MSP430Operand.IMM source;
        public IMM(MSP430Operand.IMM source) {
            this.source = source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IMM(source);
        }
        public MSP430Operand get_source() { return source; }
    }
    public static class IMMREG implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.IMM source;
        public MSP430Operand.SREG dest;
        public IMMREG(MSP430Operand.IMM source, MSP430Operand.SREG dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IMMREG(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class IMMIND implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.IMM source;
        public MSP430Operand.INDX dest;
        public IMMIND(MSP430Operand.IMM source, MSP430Operand.INDX dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IMMIND(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class IMMSYM implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.IMM source;
        public MSP430Operand.SYM dest;
        public IMMSYM(MSP430Operand.IMM source, MSP430Operand.SYM dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IMMSYM(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class IMMABS implements MSP430AddrMode, DOUBLE_W, DOUBLE_B {
        public MSP430Operand.IMM source;
        public MSP430Operand.ABSO dest;
        public IMMABS(MSP430Operand.IMM source, MSP430Operand.ABSO dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IMMABS(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class AUTO_B implements MSP430AddrMode, SINGLE_B {
        public MSP430Operand.AIREG_B source;
        public AUTO_B(MSP430Operand.AIREG_B source) {
            this.source = source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTO_B(source);
        }
        public MSP430Operand get_source() { return source; }
    }
    public static class AUTOREG_B implements MSP430AddrMode, DOUBLE_B {
        public MSP430Operand.AIREG_B source;
        public MSP430Operand.SREG dest;
        public AUTOREG_B(MSP430Operand.AIREG_B source, MSP430Operand.SREG dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOREG_B(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class AUTOIND_B implements MSP430AddrMode, DOUBLE_B {
        public MSP430Operand.AIREG_B source;
        public MSP430Operand.INDX dest;
        public AUTOIND_B(MSP430Operand.AIREG_B source, MSP430Operand.INDX dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOIND_B(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class AUTOSYM_B implements MSP430AddrMode, DOUBLE_B {
        public MSP430Operand.AIREG_B source;
        public MSP430Operand.SYM dest;
        public AUTOSYM_B(MSP430Operand.AIREG_B source, MSP430Operand.SYM dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOSYM_B(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class AUTOABS_B implements MSP430AddrMode, DOUBLE_B {
        public MSP430Operand.AIREG_B source;
        public MSP430Operand.ABSO dest;
        public AUTOABS_B(MSP430Operand.AIREG_B source, MSP430Operand.ABSO dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOABS_B(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class AUTO_W implements MSP430AddrMode, SINGLE_W {
        public MSP430Operand.AIREG_W source;
        public AUTO_W(MSP430Operand.AIREG_W source) {
            this.source = source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTO_W(source);
        }
        public MSP430Operand get_source() { return source; }
    }
    public static class AUTOREG_W implements MSP430AddrMode, DOUBLE_W {
        public MSP430Operand.AIREG_W source;
        public MSP430Operand.SREG dest;
        public AUTOREG_W(MSP430Operand.AIREG_W source, MSP430Operand.SREG dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOREG_W(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class AUTOIND_W implements MSP430AddrMode, DOUBLE_W {
        public MSP430Operand.AIREG_W source;
        public MSP430Operand.INDX dest;
        public AUTOIND_W(MSP430Operand.AIREG_W source, MSP430Operand.INDX dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOIND_W(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class AUTOSYM_W implements MSP430AddrMode, DOUBLE_W {
        public MSP430Operand.AIREG_W source;
        public MSP430Operand.SYM dest;
        public AUTOSYM_W(MSP430Operand.AIREG_W source, MSP430Operand.SYM dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOSYM_W(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class AUTOABS_W implements MSP430AddrMode, DOUBLE_W {
        public MSP430Operand.AIREG_W source;
        public MSP430Operand.ABSO dest;
        public AUTOABS_W(MSP430Operand.AIREG_W source, MSP430Operand.ABSO dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOABS_W(source, dest);
        }
        public MSP430Operand get_source() { return source; }
        public MSP430Operand get_dest() { return dest; }
    }
    public static class JMP implements MSP430AddrMode {
        public MSP430Operand.JUMP source;
        public JMP(MSP430Operand.JUMP source) {
            this.source = source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
        public MSP430Operand get_source() { return source; }
    }
    public static class $clrc$ implements MSP430AddrMode {
        public $clrc$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$clrc$();
        }
    }
    public static class $clrn$ implements MSP430AddrMode {
        public $clrn$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$clrn$();
        }
    }
    public static class $clrz$ implements MSP430AddrMode {
        public $clrz$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$clrz$();
        }
    }
    public static class $dint$ implements MSP430AddrMode {
        public $dint$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$dint$();
        }
    }
    public static class $eint$ implements MSP430AddrMode {
        public $eint$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$eint$();
        }
    }
    public static class $nop$ implements MSP430AddrMode {
        public $nop$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$nop$();
        }
    }
    public static class $ret$ implements MSP430AddrMode {
        public $ret$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$ret$();
        }
    }
    public static class $reti$ implements MSP430AddrMode {
        public $reti$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$reti$();
        }
    }
    public static class $setc$ implements MSP430AddrMode {
        public $setc$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$setc$();
        }
    }
    public static class $setn$ implements MSP430AddrMode {
        public $setn$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$setn$();
        }
    }
    public static class $setz$ implements MSP430AddrMode {
        public $setz$() {
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$setz$();
        }
    }
}
