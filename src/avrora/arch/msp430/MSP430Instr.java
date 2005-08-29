package avrora.arch.msp430;

/**
 * The <code>MSP430Instr</code> class is a container (almost a namespace)
 * for all of the instructions in this architecture. Each inner class
 * represents an instruction in the architecture and also extends the
 * outer class.
 */
public abstract class MSP430Instr {
    public abstract void accept(MSP430InstrVisitor v);
    public abstract void accept(MSP430AddrModeVisitor v);
    public static class ADC extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        ADC(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class ADC_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        ADC_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class ADD extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        ADD(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class ADD_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        ADD_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class ADDC extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        ADDC(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class ADDC_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        ADDC_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class AND extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        AND(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class AND_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        AND_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class BIC extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        BIC(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class BIC_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        BIC_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class BIS extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        BIS(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class BIS_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        BIS_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class BIT extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        BIT(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class BIT_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        BIT_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class BR extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        BR(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class CALL extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        CALL(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class CLR extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        CLR(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class CLR_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        CLR_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class CLRC extends MSP430Instr {
        CLRC(MSP430AddrMode.$clrc$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$clrc$();
        }
    }
    
    public static class CLRN extends MSP430Instr {
        CLRN(MSP430AddrMode.$clrn$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$clrn$();
        }
    }
    
    public static class CLRZ extends MSP430Instr {
        CLRZ(MSP430AddrMode.$clrz$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$clrz$();
        }
    }
    
    public static class CMP extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        CMP(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class CMP_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        CMP_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class DADC extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        DADC(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class DADC_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        DADC_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class DADD extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        DADD(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class DADD_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        DADD_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class DEC extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        DEC(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class DEC_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        DEC_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class DECD extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        DECD(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class DECD_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        DECD_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class DINT extends MSP430Instr {
        DINT(MSP430AddrMode.$dint$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$dint$();
        }
    }
    
    public static class EINT extends MSP430Instr {
        EINT(MSP430AddrMode.$eint$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$eint$();
        }
    }
    
    public static class INC extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        INC(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class INC_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        INC_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class INCD extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        INCD(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class INCD_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        INCD_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class INV extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        INV(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class INV_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        INV_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class JC extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JC(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JHS extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JHS(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JEQ extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JEQ(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JZ extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JZ(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JGE extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JGE(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JL extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JL(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JMP extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JMP(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JN extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JN(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JNC extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JNC(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JLO extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JLO(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JNE extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JNE(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class JNZ extends MSP430Instr {
        public MSP430Operand.JUMP source;
        JNZ(MSP430AddrMode.JMP am) {
            this.source = am.source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    
    public static class MOV extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        MOV(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class MOV_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        MOV_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class NOP extends MSP430Instr {
        NOP(MSP430AddrMode.$nop$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$nop$();
        }
    }
    
    public static class POP extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        POP(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class POP_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        POP_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class PUSH extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        PUSH(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class PUSH_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        PUSH_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class RET extends MSP430Instr {
        RET(MSP430AddrMode.$ret$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$ret$();
        }
    }
    
    public static class RETI extends MSP430Instr {
        RETI(MSP430AddrMode.$reti$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$reti$();
        }
    }
    
    public static class RLA extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        RLA(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class RLA_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        RLA_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class RLC extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        RLC(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class RLC_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        RLC_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class RRA extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        RRA(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class RRA_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        RRA_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class RRC extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        RRC(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class RRC_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        RRC_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class SBC extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        SBC(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class SBC_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        SBC_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class SETC extends MSP430Instr {
        SETC(MSP430AddrMode.$setc$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$setc$();
        }
    }
    
    public static class SETN extends MSP430Instr {
        SETN(MSP430AddrMode.$setn$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$setn$();
        }
    }
    
    public static class SETZ extends MSP430Instr {
        SETZ(MSP430AddrMode.$setz$ am) {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$setz$();
        }
    }
    
    public static class SUB extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        SUB(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class SUB_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        SUB_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class SUBC extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        SUBC(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class SUBC_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        SUBC_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class SBB extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        SBB(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class SBB_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        SBB_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class SWPB extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        SWPB(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class SXT extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        SXT(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class TST extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_W addrMode;
        TST(MSP430AddrMode.SINGLE_W am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class TST_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430AddrMode.SINGLE_B addrMode;
        TST_B(MSP430AddrMode.SINGLE_B am) {
            this.source = am.get_source();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class XOR extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_W addrMode;
        XOR(MSP430AddrMode.DOUBLE_W am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class XOR_B extends MSP430Instr {
        public final MSP430Operand source;
        public final MSP430Operand dest;
        public final MSP430AddrMode.DOUBLE_B addrMode;
        XOR_B(MSP430AddrMode.DOUBLE_B am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
}
