package jintgen.arch.avr;

/**
 * The <code>AVRInstr</code> class is a container (almost a namespace)
 * for all of the instructions in this architecture. Each inner class
 * represents an instruction in the architecture and also extends the
 * outer class.
 */
public class AVRInstr {
    public static class ADC extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        ADC(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ADD extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        ADD(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ADIW extends AVRInstr {
        public final AVROperand.RDL rd;
        public final AVROperand.IMM6 imm;
        ADIW(AVROperand.RDL rd, AVROperand.IMM6 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class AND extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        AND(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ANDI extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.IMM8 imm;
        ANDI(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ASR extends AVRInstr {
        public final AVROperand.GPR rd;
        ASR(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BCLR extends AVRInstr {
        public final AVROperand.IMM3 bit;
        BCLR(AVROperand.IMM3 bit) {
            this.bit = bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BLD extends AVRInstr {
        public final AVROperand.GPR rr;
        public final AVROperand.IMM3 bit;
        BLD(AVROperand.GPR rr, AVROperand.IMM3 bit) {
            this.rr = rr;
            this.bit = bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRBC extends AVRInstr {
        public final AVROperand.IMM3 bit;
        public final AVROperand.SREL target;
        BRBC(AVROperand.IMM3 bit, AVROperand.SREL target) {
            this.bit = bit;
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRBS extends AVRInstr {
        public final AVROperand.IMM3 bit;
        public final AVROperand.SREL target;
        BRBS(AVROperand.IMM3 bit, AVROperand.SREL target) {
            this.bit = bit;
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRCC extends AVRInstr {
        public final AVROperand.SREL target;
        BRCC(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRCS extends AVRInstr {
        public final AVROperand.SREL target;
        BRCS(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BREAK extends AVRInstr {
        BREAK() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BREQ extends AVRInstr {
        public final AVROperand.SREL target;
        BREQ(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRGE extends AVRInstr {
        public final AVROperand.SREL target;
        BRGE(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRHC extends AVRInstr {
        public final AVROperand.SREL target;
        BRHC(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRHS extends AVRInstr {
        public final AVROperand.SREL target;
        BRHS(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRID extends AVRInstr {
        public final AVROperand.SREL target;
        BRID(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRIE extends AVRInstr {
        public final AVROperand.SREL target;
        BRIE(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRLO extends AVRInstr {
        public final AVROperand.SREL target;
        BRLO(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRLT extends AVRInstr {
        public final AVROperand.SREL target;
        BRLT(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRMI extends AVRInstr {
        public final AVROperand.SREL target;
        BRMI(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRNE extends AVRInstr {
        public final AVROperand.SREL target;
        BRNE(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRPL extends AVRInstr {
        public final AVROperand.SREL target;
        BRPL(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRSH extends AVRInstr {
        public final AVROperand.SREL target;
        BRSH(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRTC extends AVRInstr {
        public final AVROperand.SREL target;
        BRTC(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRTS extends AVRInstr {
        public final AVROperand.SREL target;
        BRTS(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRVC extends AVRInstr {
        public final AVROperand.SREL target;
        BRVC(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRVS extends AVRInstr {
        public final AVROperand.SREL target;
        BRVS(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BSET extends AVRInstr {
        public final AVROperand.IMM3 bit;
        BSET(AVROperand.IMM3 bit) {
            this.bit = bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BST extends AVRInstr {
        public final AVROperand.GPR rr;
        public final AVROperand.IMM3 bit;
        BST(AVROperand.GPR rr, AVROperand.IMM3 bit) {
            this.rr = rr;
            this.bit = bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CALL extends AVRInstr {
        public final AVROperand.PADDR target;
        CALL(AVROperand.PADDR target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CBI extends AVRInstr {
        public final AVROperand.IMM5 ior;
        public final AVROperand.IMM3 bit;
        CBI(AVROperand.IMM5 ior, AVROperand.IMM3 bit) {
            this.ior = ior;
            this.bit = bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CBR extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.IMM8 imm;
        CBR(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CLC extends AVRInstr {
        CLC() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CLH extends AVRInstr {
        CLH() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CLI extends AVRInstr {
        CLI() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CLN extends AVRInstr {
        CLN() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CLR extends AVRInstr {
        public final AVROperand.GPR rd;
        CLR(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CLS extends AVRInstr {
        CLS() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CLT extends AVRInstr {
        CLT() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CLV extends AVRInstr {
        CLV() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CLZ extends AVRInstr {
        CLZ() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class COM extends AVRInstr {
        public final AVROperand.GPR rd;
        COM(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CP extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        CP(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CPC extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        CPC(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CPI extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.IMM8 imm;
        CPI(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CPSE extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        CPSE(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class DEC extends AVRInstr {
        public final AVROperand.GPR rd;
        DEC(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class EICALL extends AVRInstr {
        EICALL() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class EIJMP extends AVRInstr {
        EIJMP() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class EOR extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        EOR(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class FMUL extends AVRInstr {
        public final AVROperand.MGPR rd;
        public final AVROperand.MGPR rr;
        FMUL(AVROperand.MGPR rd, AVROperand.MGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class FMULS extends AVRInstr {
        public final AVROperand.MGPR rd;
        public final AVROperand.MGPR rr;
        FMULS(AVROperand.MGPR rd, AVROperand.MGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class FMULSU extends AVRInstr {
        public final AVROperand.MGPR rd;
        public final AVROperand.MGPR rr;
        FMULSU(AVROperand.MGPR rd, AVROperand.MGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ICALL extends AVRInstr {
        ICALL() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class IJMP extends AVRInstr {
        IJMP() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class IN extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.IMM6 imm;
        IN(AVROperand.GPR rd, AVROperand.IMM6 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class INC extends AVRInstr {
        public final AVROperand.GPR rd;
        INC(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class JMP extends AVRInstr {
        public final AVROperand.PADDR target;
        JMP(AVROperand.PADDR target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class LDD extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.YZ ar;
        public final AVROperand.IMM6 imm;
        LDD(AVROperand.GPR rd, AVROperand.YZ ar, AVROperand.IMM6 imm) {
            this.rd = rd;
            this.ar = ar;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class LDI extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.IMM8 imm;
        LDI(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class LDS extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.DADDR addr;
        LDS(AVROperand.GPR rd, AVROperand.DADDR addr) {
            this.rd = rd;
            this.addr = addr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class LSL extends AVRInstr {
        public final AVROperand.GPR rd;
        LSL(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class LSR extends AVRInstr {
        public final AVROperand.GPR rd;
        LSR(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class MOV extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        MOV(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class MOVW extends AVRInstr {
        public final AVROperand.EGPR rd;
        public final AVROperand.EGPR rr;
        MOVW(AVROperand.EGPR rd, AVROperand.EGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class MUL extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        MUL(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class MULS extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.HGPR rr;
        MULS(AVROperand.HGPR rd, AVROperand.HGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class MULSU extends AVRInstr {
        public final AVROperand.MGPR rd;
        public final AVROperand.MGPR rr;
        MULSU(AVROperand.MGPR rd, AVROperand.MGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class NEG extends AVRInstr {
        public final AVROperand.GPR rd;
        NEG(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class NOP extends AVRInstr {
        NOP() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class OR extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        OR(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ORI extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.IMM8 imm;
        ORI(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class OUT extends AVRInstr {
        public final AVROperand.IMM6 ior;
        public final AVROperand.GPR rr;
        OUT(AVROperand.IMM6 ior, AVROperand.GPR rr) {
            this.ior = ior;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class POP extends AVRInstr {
        public final AVROperand.GPR rd;
        POP(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class PUSH extends AVRInstr {
        public final AVROperand.GPR rr;
        PUSH(AVROperand.GPR rr) {
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class RCALL extends AVRInstr {
        public final AVROperand.LREL target;
        RCALL(AVROperand.LREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class RET extends AVRInstr {
        RET() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class RETI extends AVRInstr {
        RETI() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class RJMP extends AVRInstr {
        public final AVROperand.LREL target;
        RJMP(AVROperand.LREL target) {
            this.target = target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ROL extends AVRInstr {
        public final AVROperand.GPR rd;
        ROL(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ROR extends AVRInstr {
        public final AVROperand.GPR rd;
        ROR(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBC extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        SBC(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBCI extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.IMM8 imm;
        SBCI(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBI extends AVRInstr {
        public final AVROperand.IMM5 ior;
        public final AVROperand.IMM3 bit;
        SBI(AVROperand.IMM5 ior, AVROperand.IMM3 bit) {
            this.ior = ior;
            this.bit = bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBIC extends AVRInstr {
        public final AVROperand.IMM5 ior;
        public final AVROperand.IMM3 bit;
        SBIC(AVROperand.IMM5 ior, AVROperand.IMM3 bit) {
            this.ior = ior;
            this.bit = bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBIS extends AVRInstr {
        public final AVROperand.IMM5 ior;
        public final AVROperand.IMM3 bit;
        SBIS(AVROperand.IMM5 ior, AVROperand.IMM3 bit) {
            this.ior = ior;
            this.bit = bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBIW extends AVRInstr {
        public final AVROperand.RDL rd;
        public final AVROperand.IMM6 imm;
        SBIW(AVROperand.RDL rd, AVROperand.IMM6 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBR extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.IMM8 imm;
        SBR(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBRC extends AVRInstr {
        public final AVROperand.GPR rr;
        public final AVROperand.IMM3 bit;
        SBRC(AVROperand.GPR rr, AVROperand.IMM3 bit) {
            this.rr = rr;
            this.bit = bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBRS extends AVRInstr {
        public final AVROperand.GPR rr;
        public final AVROperand.IMM3 bit;
        SBRS(AVROperand.GPR rr, AVROperand.IMM3 bit) {
            this.rr = rr;
            this.bit = bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SEC extends AVRInstr {
        SEC() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SEH extends AVRInstr {
        SEH() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SEI extends AVRInstr {
        SEI() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SEN extends AVRInstr {
        SEN() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SER extends AVRInstr {
        public final AVROperand.HGPR rd;
        SER(AVROperand.HGPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SES extends AVRInstr {
        SES() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SET extends AVRInstr {
        SET() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SEV extends AVRInstr {
        SEV() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SEZ extends AVRInstr {
        SEZ() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SLEEP extends AVRInstr {
        SLEEP() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SPM extends AVRInstr {
        SPM() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class STD extends AVRInstr {
        public final AVROperand.YZ ar;
        public final AVROperand.IMM6 imm;
        public final AVROperand.GPR rr;
        STD(AVROperand.YZ ar, AVROperand.IMM6 imm, AVROperand.GPR rr) {
            this.ar = ar;
            this.imm = imm;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class STS extends AVRInstr {
        public final AVROperand.DADDR addr;
        public final AVROperand.GPR rr;
        STS(AVROperand.DADDR addr, AVROperand.GPR rr) {
            this.addr = addr;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SUB extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        SUB(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SUBI extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.IMM8 imm;
        SUBI(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SWAP extends AVRInstr {
        public final AVROperand.GPR rd;
        SWAP(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class TST extends AVRInstr {
        public final AVROperand.GPR rd;
        TST(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class WDR extends AVRInstr {
        WDR() {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ELPM extends AVRInstr {
        public final AVROperand.XLPM_source_union source;
        public final AVROperand.XLPM_dest_union dest;
        ELPM(AVROperand.XLPM_source_union source, AVROperand.XLPM_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class LPM extends AVRInstr {
        public final AVROperand.XLPM_source_union source;
        public final AVROperand.XLPM_dest_union dest;
        LPM(AVROperand.XLPM_source_union source, AVROperand.XLPM_dest_union dest) {
            this.source = source;
            this.dest = dest;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class LD extends AVRInstr {
        public final AVROperand.LD_ST_rd_union rd;
        public final AVROperand.LD_ST_ar_union ar;
        LD(AVROperand.LD_ST_rd_union rd, AVROperand.LD_ST_ar_union ar) {
            this.rd = rd;
            this.ar = ar;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ST extends AVRInstr {
        public final AVROperand.LD_ST_rd_union rd;
        public final AVROperand.LD_ST_ar_union ar;
        ST(AVROperand.LD_ST_rd_union rd, AVROperand.LD_ST_ar_union ar) {
            this.rd = rd;
            this.ar = ar;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
}
