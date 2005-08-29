package avrora.arch.avr;

/**
 * The <code>AVRInstr</code> class is a container (almost a namespace)
 * for all of the instructions in this architecture. Each inner class
 * represents an instruction in the architecture and also extends the
 * outer class.
 */
public abstract class AVRInstr {
    public abstract void accept(AVRInstrVisitor v);
    public abstract void accept(AVRAddrModeVisitor v);
    public static class ADC extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        ADC(AVRAddrMode.$adc$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$adc$(rd, rr);
        }
    }
    
    public static class ADD extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        ADD(AVRAddrMode.$add$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$add$(rd, rr);
        }
    }
    
    public static class ADIW extends AVRInstr {
        public AVROperand.RDL rd;
        public AVROperand.IMM6 imm;
        ADIW(AVRAddrMode.$adiw$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$adiw$(rd, imm);
        }
    }
    
    public static class AND extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        AND(AVRAddrMode.$and$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$and$(rd, rr);
        }
    }
    
    public static class ANDI extends AVRInstr {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        ANDI(AVRAddrMode.$andi$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$andi$(rd, imm);
        }
    }
    
    public static class ASR extends AVRInstr {
        public AVROperand.GPR rd;
        ASR(AVRAddrMode.$asr$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$asr$(rd);
        }
    }
    
    public static class BCLR extends AVRInstr {
        public AVROperand.IMM3 bit;
        BCLR(AVRAddrMode.$bclr$ am) {
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bclr$(bit);
        }
    }
    
    public static class BLD extends AVRInstr {
        public AVROperand.GPR rr;
        public AVROperand.IMM3 bit;
        BLD(AVRAddrMode.$bld$ am) {
            this.rr = am.rr;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bld$(rr, bit);
        }
    }
    
    public static class BRBC extends AVRInstr {
        public AVROperand.IMM3 bit;
        public AVROperand.SREL target;
        BRBC(AVRAddrMode.$brbc$ am) {
            this.bit = am.bit;
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brbc$(bit, target);
        }
    }
    
    public static class BRBS extends AVRInstr {
        public AVROperand.IMM3 bit;
        public AVROperand.SREL target;
        BRBS(AVRAddrMode.$brbs$ am) {
            this.bit = am.bit;
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brbs$(bit, target);
        }
    }
    
    public static class BRCC extends AVRInstr {
        public AVROperand.SREL target;
        BRCC(AVRAddrMode.$brcc$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brcc$(target);
        }
    }
    
    public static class BRCS extends AVRInstr {
        public AVROperand.SREL target;
        BRCS(AVRAddrMode.$brcs$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brcs$(target);
        }
    }
    
    public static class BREAK extends AVRInstr {
        BREAK(AVRAddrMode.$break$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$break$();
        }
    }
    
    public static class BREQ extends AVRInstr {
        public AVROperand.SREL target;
        BREQ(AVRAddrMode.$breq$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$breq$(target);
        }
    }
    
    public static class BRGE extends AVRInstr {
        public AVROperand.SREL target;
        BRGE(AVRAddrMode.$brge$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brge$(target);
        }
    }
    
    public static class BRHC extends AVRInstr {
        public AVROperand.SREL target;
        BRHC(AVRAddrMode.$brhc$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brhc$(target);
        }
    }
    
    public static class BRHS extends AVRInstr {
        public AVROperand.SREL target;
        BRHS(AVRAddrMode.$brhs$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brhs$(target);
        }
    }
    
    public static class BRID extends AVRInstr {
        public AVROperand.SREL target;
        BRID(AVRAddrMode.$brid$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brid$(target);
        }
    }
    
    public static class BRIE extends AVRInstr {
        public AVROperand.SREL target;
        BRIE(AVRAddrMode.$brie$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brie$(target);
        }
    }
    
    public static class BRLO extends AVRInstr {
        public AVROperand.SREL target;
        BRLO(AVRAddrMode.$brlo$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brlo$(target);
        }
    }
    
    public static class BRLT extends AVRInstr {
        public AVROperand.SREL target;
        BRLT(AVRAddrMode.$brlt$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brlt$(target);
        }
    }
    
    public static class BRMI extends AVRInstr {
        public AVROperand.SREL target;
        BRMI(AVRAddrMode.$brmi$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brmi$(target);
        }
    }
    
    public static class BRNE extends AVRInstr {
        public AVROperand.SREL target;
        BRNE(AVRAddrMode.$brne$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brne$(target);
        }
    }
    
    public static class BRPL extends AVRInstr {
        public AVROperand.SREL target;
        BRPL(AVRAddrMode.$brpl$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brpl$(target);
        }
    }
    
    public static class BRSH extends AVRInstr {
        public AVROperand.SREL target;
        BRSH(AVRAddrMode.$brsh$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brsh$(target);
        }
    }
    
    public static class BRTC extends AVRInstr {
        public AVROperand.SREL target;
        BRTC(AVRAddrMode.$brtc$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brtc$(target);
        }
    }
    
    public static class BRTS extends AVRInstr {
        public AVROperand.SREL target;
        BRTS(AVRAddrMode.$brts$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brts$(target);
        }
    }
    
    public static class BRVC extends AVRInstr {
        public AVROperand.SREL target;
        BRVC(AVRAddrMode.$brvc$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brvc$(target);
        }
    }
    
    public static class BRVS extends AVRInstr {
        public AVROperand.SREL target;
        BRVS(AVRAddrMode.$brvs$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brvs$(target);
        }
    }
    
    public static class BSET extends AVRInstr {
        public AVROperand.IMM3 bit;
        BSET(AVRAddrMode.$bset$ am) {
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bset$(bit);
        }
    }
    
    public static class BST extends AVRInstr {
        public AVROperand.GPR rr;
        public AVROperand.IMM3 bit;
        BST(AVRAddrMode.$bst$ am) {
            this.rr = am.rr;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bst$(rr, bit);
        }
    }
    
    public static class CALL extends AVRInstr {
        public AVROperand.PADDR target;
        CALL(AVRAddrMode.$call$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$call$(target);
        }
    }
    
    public static class CBI extends AVRInstr {
        public AVROperand.IMM5 ior;
        public AVROperand.IMM3 bit;
        CBI(AVRAddrMode.$cbi$ am) {
            this.ior = am.ior;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cbi$(ior, bit);
        }
    }
    
    public static class CBR extends AVRInstr {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        CBR(AVRAddrMode.$cbr$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cbr$(rd, imm);
        }
    }
    
    public static class CLC extends AVRInstr {
        CLC(AVRAddrMode.$clc$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clc$();
        }
    }
    
    public static class CLH extends AVRInstr {
        CLH(AVRAddrMode.$clh$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clh$();
        }
    }
    
    public static class CLI extends AVRInstr {
        CLI(AVRAddrMode.$cli$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cli$();
        }
    }
    
    public static class CLN extends AVRInstr {
        CLN(AVRAddrMode.$cln$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cln$();
        }
    }
    
    public static class CLR extends AVRInstr {
        public AVROperand.GPR rd;
        CLR(AVRAddrMode.$clr$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clr$(rd);
        }
    }
    
    public static class CLS extends AVRInstr {
        CLS(AVRAddrMode.$cls$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cls$();
        }
    }
    
    public static class CLT extends AVRInstr {
        CLT(AVRAddrMode.$clt$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clt$();
        }
    }
    
    public static class CLV extends AVRInstr {
        CLV(AVRAddrMode.$clv$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clv$();
        }
    }
    
    public static class CLZ extends AVRInstr {
        CLZ(AVRAddrMode.$clz$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clz$();
        }
    }
    
    public static class COM extends AVRInstr {
        public AVROperand.GPR rd;
        COM(AVRAddrMode.$com$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$com$(rd);
        }
    }
    
    public static class CP extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        CP(AVRAddrMode.$cp$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cp$(rd, rr);
        }
    }
    
    public static class CPC extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        CPC(AVRAddrMode.$cpc$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cpc$(rd, rr);
        }
    }
    
    public static class CPI extends AVRInstr {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        CPI(AVRAddrMode.$cpi$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cpi$(rd, imm);
        }
    }
    
    public static class CPSE extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        CPSE(AVRAddrMode.$cpse$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cpse$(rd, rr);
        }
    }
    
    public static class DEC extends AVRInstr {
        public AVROperand.GPR rd;
        DEC(AVRAddrMode.$dec$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$dec$(rd);
        }
    }
    
    public static class EICALL extends AVRInstr {
        EICALL(AVRAddrMode.$eicall$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$eicall$();
        }
    }
    
    public static class EIJMP extends AVRInstr {
        EIJMP(AVRAddrMode.$eijmp$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$eijmp$();
        }
    }
    
    public static class EOR extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        EOR(AVRAddrMode.$eor$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$eor$(rd, rr);
        }
    }
    
    public static class FMUL extends AVRInstr {
        public AVROperand.MGPR rd;
        public AVROperand.MGPR rr;
        FMUL(AVRAddrMode.$fmul$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$fmul$(rd, rr);
        }
    }
    
    public static class FMULS extends AVRInstr {
        public AVROperand.MGPR rd;
        public AVROperand.MGPR rr;
        FMULS(AVRAddrMode.$fmuls$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$fmuls$(rd, rr);
        }
    }
    
    public static class FMULSU extends AVRInstr {
        public AVROperand.MGPR rd;
        public AVROperand.MGPR rr;
        FMULSU(AVRAddrMode.$fmulsu$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$fmulsu$(rd, rr);
        }
    }
    
    public static class ICALL extends AVRInstr {
        ICALL(AVRAddrMode.$icall$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$icall$();
        }
    }
    
    public static class IJMP extends AVRInstr {
        IJMP(AVRAddrMode.$ijmp$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ijmp$();
        }
    }
    
    public static class IN extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.IMM6 imm;
        IN(AVRAddrMode.$in$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$in$(rd, imm);
        }
    }
    
    public static class INC extends AVRInstr {
        public AVROperand.GPR rd;
        INC(AVRAddrMode.$inc$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$inc$(rd);
        }
    }
    
    public static class JMP extends AVRInstr {
        public AVROperand.PADDR target;
        JMP(AVRAddrMode.$jmp$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$jmp$(target);
        }
    }
    
    public static class LDD extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.YZ ar;
        public AVROperand.IMM6 imm;
        LDD(AVRAddrMode.$ldd$ am) {
            this.rd = am.rd;
            this.ar = am.ar;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ldd$(rd, ar, imm);
        }
    }
    
    public static class LDI extends AVRInstr {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        LDI(AVRAddrMode.$ldi$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ldi$(rd, imm);
        }
    }
    
    public static class LDS extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.DADDR addr;
        LDS(AVRAddrMode.$lds$ am) {
            this.rd = am.rd;
            this.addr = am.addr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$lds$(rd, addr);
        }
    }
    
    public static class LSL extends AVRInstr {
        public AVROperand.GPR rd;
        LSL(AVRAddrMode.$lsl$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$lsl$(rd);
        }
    }
    
    public static class LSR extends AVRInstr {
        public AVROperand.GPR rd;
        LSR(AVRAddrMode.$lsr$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$lsr$(rd);
        }
    }
    
    public static class MOV extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        MOV(AVRAddrMode.$mov$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$mov$(rd, rr);
        }
    }
    
    public static class MOVW extends AVRInstr {
        public AVROperand.EGPR rd;
        public AVROperand.EGPR rr;
        MOVW(AVRAddrMode.$movw$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$movw$(rd, rr);
        }
    }
    
    public static class MUL extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        MUL(AVRAddrMode.$mul$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$mul$(rd, rr);
        }
    }
    
    public static class MULS extends AVRInstr {
        public AVROperand.HGPR rd;
        public AVROperand.HGPR rr;
        MULS(AVRAddrMode.$muls$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$muls$(rd, rr);
        }
    }
    
    public static class MULSU extends AVRInstr {
        public AVROperand.MGPR rd;
        public AVROperand.MGPR rr;
        MULSU(AVRAddrMode.$mulsu$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$mulsu$(rd, rr);
        }
    }
    
    public static class NEG extends AVRInstr {
        public AVROperand.GPR rd;
        NEG(AVRAddrMode.$neg$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$neg$(rd);
        }
    }
    
    public static class NOP extends AVRInstr {
        NOP(AVRAddrMode.$nop$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$nop$();
        }
    }
    
    public static class OR extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        OR(AVRAddrMode.$or$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$or$(rd, rr);
        }
    }
    
    public static class ORI extends AVRInstr {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        ORI(AVRAddrMode.$ori$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ori$(rd, imm);
        }
    }
    
    public static class OUT extends AVRInstr {
        public AVROperand.IMM6 ior;
        public AVROperand.GPR rr;
        OUT(AVRAddrMode.$out$ am) {
            this.ior = am.ior;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$out$(ior, rr);
        }
    }
    
    public static class POP extends AVRInstr {
        public AVROperand.GPR rd;
        POP(AVRAddrMode.$pop$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$pop$(rd);
        }
    }
    
    public static class PUSH extends AVRInstr {
        public AVROperand.GPR rr;
        PUSH(AVRAddrMode.$push$ am) {
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$push$(rr);
        }
    }
    
    public static class RCALL extends AVRInstr {
        public AVROperand.LREL target;
        RCALL(AVRAddrMode.$rcall$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$rcall$(target);
        }
    }
    
    public static class RET extends AVRInstr {
        RET(AVRAddrMode.$ret$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ret$();
        }
    }
    
    public static class RETI extends AVRInstr {
        RETI(AVRAddrMode.$reti$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$reti$();
        }
    }
    
    public static class RJMP extends AVRInstr {
        public AVROperand.LREL target;
        RJMP(AVRAddrMode.$rjmp$ am) {
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$rjmp$(target);
        }
    }
    
    public static class ROL extends AVRInstr {
        public AVROperand.GPR rd;
        ROL(AVRAddrMode.$rol$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$rol$(rd);
        }
    }
    
    public static class ROR extends AVRInstr {
        public AVROperand.GPR rd;
        ROR(AVRAddrMode.$ror$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ror$(rd);
        }
    }
    
    public static class SBC extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        SBC(AVRAddrMode.$sbc$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbc$(rd, rr);
        }
    }
    
    public static class SBCI extends AVRInstr {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        SBCI(AVRAddrMode.$sbci$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbci$(rd, imm);
        }
    }
    
    public static class SBI extends AVRInstr {
        public AVROperand.IMM5 ior;
        public AVROperand.IMM3 bit;
        SBI(AVRAddrMode.$sbi$ am) {
            this.ior = am.ior;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbi$(ior, bit);
        }
    }
    
    public static class SBIC extends AVRInstr {
        public AVROperand.IMM5 ior;
        public AVROperand.IMM3 bit;
        SBIC(AVRAddrMode.$sbic$ am) {
            this.ior = am.ior;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbic$(ior, bit);
        }
    }
    
    public static class SBIS extends AVRInstr {
        public AVROperand.IMM5 ior;
        public AVROperand.IMM3 bit;
        SBIS(AVRAddrMode.$sbis$ am) {
            this.ior = am.ior;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbis$(ior, bit);
        }
    }
    
    public static class SBIW extends AVRInstr {
        public AVROperand.RDL rd;
        public AVROperand.IMM6 imm;
        SBIW(AVRAddrMode.$sbiw$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbiw$(rd, imm);
        }
    }
    
    public static class SBR extends AVRInstr {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        SBR(AVRAddrMode.$sbr$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbr$(rd, imm);
        }
    }
    
    public static class SBRC extends AVRInstr {
        public AVROperand.GPR rr;
        public AVROperand.IMM3 bit;
        SBRC(AVRAddrMode.$sbrc$ am) {
            this.rr = am.rr;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbrc$(rr, bit);
        }
    }
    
    public static class SBRS extends AVRInstr {
        public AVROperand.GPR rr;
        public AVROperand.IMM3 bit;
        SBRS(AVRAddrMode.$sbrs$ am) {
            this.rr = am.rr;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbrs$(rr, bit);
        }
    }
    
    public static class SEC extends AVRInstr {
        SEC(AVRAddrMode.$sec$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sec$();
        }
    }
    
    public static class SEH extends AVRInstr {
        SEH(AVRAddrMode.$seh$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$seh$();
        }
    }
    
    public static class SEI extends AVRInstr {
        SEI(AVRAddrMode.$sei$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sei$();
        }
    }
    
    public static class SEN extends AVRInstr {
        SEN(AVRAddrMode.$sen$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sen$();
        }
    }
    
    public static class SER extends AVRInstr {
        public AVROperand.HGPR rd;
        SER(AVRAddrMode.$ser$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ser$(rd);
        }
    }
    
    public static class SES extends AVRInstr {
        SES(AVRAddrMode.$ses$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ses$();
        }
    }
    
    public static class SET extends AVRInstr {
        SET(AVRAddrMode.$set$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$set$();
        }
    }
    
    public static class SEV extends AVRInstr {
        SEV(AVRAddrMode.$sev$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sev$();
        }
    }
    
    public static class SEZ extends AVRInstr {
        SEZ(AVRAddrMode.$sez$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sez$();
        }
    }
    
    public static class SLEEP extends AVRInstr {
        SLEEP(AVRAddrMode.$sleep$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sleep$();
        }
    }
    
    public static class SPM extends AVRInstr {
        SPM(AVRAddrMode.$spm$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$spm$();
        }
    }
    
    public static class STD extends AVRInstr {
        public AVROperand.YZ ar;
        public AVROperand.IMM6 imm;
        public AVROperand.GPR rr;
        STD(AVRAddrMode.$std$ am) {
            this.ar = am.ar;
            this.imm = am.imm;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$std$(ar, imm, rr);
        }
    }
    
    public static class STS extends AVRInstr {
        public AVROperand.DADDR addr;
        public AVROperand.GPR rr;
        STS(AVRAddrMode.$sts$ am) {
            this.addr = am.addr;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sts$(addr, rr);
        }
    }
    
    public static class SUB extends AVRInstr {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        SUB(AVRAddrMode.$sub$ am) {
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sub$(rd, rr);
        }
    }
    
    public static class SUBI extends AVRInstr {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        SUBI(AVRAddrMode.$subi$ am) {
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$subi$(rd, imm);
        }
    }
    
    public static class SWAP extends AVRInstr {
        public AVROperand.GPR rd;
        SWAP(AVRAddrMode.$swap$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$swap$(rd);
        }
    }
    
    public static class TST extends AVRInstr {
        public AVROperand.GPR rd;
        TST(AVRAddrMode.$tst$ am) {
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$tst$(rd);
        }
    }
    
    public static class WDR extends AVRInstr {
        WDR(AVRAddrMode.$wdr$ am) {
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$wdr$();
        }
    }
    
    public static class ELPM extends AVRInstr {
        public final AVROperand source;
        public final AVROperand dest;
        public final AVRAddrMode.XLPM addrMode;
        ELPM(AVRAddrMode.XLPM am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class LPM extends AVRInstr {
        public final AVROperand source;
        public final AVROperand dest;
        public final AVRAddrMode.XLPM addrMode;
        LPM(AVRAddrMode.XLPM am) {
            this.source = am.get_source();
            this.dest = am.get_dest();
            this.addrMode = am;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class LD extends AVRInstr {
        public final AVROperand rd;
        public final AVROperand ar;
        public final AVRAddrMode.LD_ST addrMode;
        LD(AVRAddrMode.LD_ST am) {
            this.rd = am.get_rd();
            this.ar = am.get_ar();
            this.addrMode = am;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
    public static class ST extends AVRInstr {
        public final AVROperand rd;
        public final AVROperand ar;
        public final AVRAddrMode.LD_ST addrMode;
        ST(AVRAddrMode.LD_ST am) {
            this.rd = am.get_rd();
            this.ar = am.get_ar();
            this.addrMode = am;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            addrMode.accept(v);
        }
    }
    
}
