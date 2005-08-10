package jintgen.arch.msp430;
public class MSP430Instr {
    public static class ADC extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        ADC(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class ADC_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        ADC_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class ADD extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        ADD(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class ADD_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        ADD_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class ADDC extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        ADDC(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class ADDC_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        ADDC_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class AND extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        AND(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class AND_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        AND_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIC extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        BIC(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIC_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        BIC_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIS extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        BIS(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIS_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        BIS_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIT extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        BIT(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIT_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        BIT_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BR extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        BR(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CALL extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        CALL(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CLR extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        CLR(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CLR_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        CLR_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CLRC extends MSP430Instr {
        CLRC() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CLRN extends MSP430Instr {
        CLRN() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CLRZ extends MSP430Instr {
        CLRZ() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CMP extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        CMP(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CMP_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        CMP_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DADC extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        DADC(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DADC_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        DADC_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DADD extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        DADD(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DADD_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        DADD_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DEC extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        DEC(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DEC_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        DEC_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DECD extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        DECD(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DECD_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        DECD_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DINT extends MSP430Instr {
        DINT() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class EINT extends MSP430Instr {
        EINT() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INC extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        INC(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INC_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        INC_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INCD extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        INCD(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INCD_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        INCD_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INV extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        INV(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INV_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        INV_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JC extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JC(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JHS extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JHS(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JEQ extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JEQ(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JZ extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JZ(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JGE extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JGE(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JL extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JL(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JMP extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JMP(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JN extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JN(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JNC extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JNC(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JLO extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JLO(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JNE extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JNE(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JNZ extends MSP430Instr {
        public final MSP430Operand.JUMP_W source;
        JNZ(MSP430Operand.JUMP_W source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class MOV extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        MOV(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class MOV_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        MOV_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class NOP extends MSP430Instr {
        NOP() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class POP extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        POP(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class POP_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        POP_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class PUSH extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        PUSH(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class PUSH_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        PUSH_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RET extends MSP430Instr {
        RET() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RETI extends MSP430Instr {
        RETI() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RLA extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        RLA(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RLA_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        RLA_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RLC extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        RLC(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RLC_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        RLC_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RRA extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        RRA(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RRA_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        RRA_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RRC extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        RRC(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RRC_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        RRC_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SBC extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        SBC(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SBC_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        SBC_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SETC extends MSP430Instr {
        SETC() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SETN extends MSP430Instr {
        SETN() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SETZ extends MSP430Instr {
        SETZ() {
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SUB extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        SUB(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SUB_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        SUB_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SUBC extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        SUBC(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SUBC_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        SUBC_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SBB extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        SBB(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SBB_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        SBB_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SWPB extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        SWPB(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SXT extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        SXT(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class TST extends MSP430Instr {
        public final MSP430Operand.SINGLE_W_source_union source;
        TST(MSP430Operand.SINGLE_W_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class TST_B extends MSP430Instr {
        public final MSP430Operand.SINGLE_B_source_union source;
        TST_B(MSP430Operand.SINGLE_B_source_union source) {
            this.source = source;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class XOR extends MSP430Instr {
        public final MSP430Operand.DOUBLE_W_source_union source;
        public final MSP430Operand.DOUBLE_W_dest_union dest;
        XOR(MSP430Operand.DOUBLE_W_source_union source, MSP430Operand.DOUBLE_W_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class XOR_B extends MSP430Instr {
        public final MSP430Operand.DOUBLE_B_source_union source;
        public final MSP430Operand.DOUBLE_B_dest_union dest;
        XOR_B(MSP430Operand.DOUBLE_B_source_union source, MSP430Operand.DOUBLE_B_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
}
