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
    
    /**
     * The <code>name</code> field stores a reference to the name of the
     * instruction as a string.
     */
    public final String name;
    
    /**
     * The <code>size</code> field stores the size of the instruction in
     * bytes.
     */
    public final int size;
    protected AVRInstr(String name, int size) {
        this.name = name;
        this.size = size;
    }
    public abstract static class GPRGPR_Instr extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.GPR rr;
        protected GPRGPR_Instr(String name, int size, AVRAddrMode.GPRGPR am) {
            super(name, size);
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_GPRGPR(rd, rr);
        }
    }
    public abstract static class MGPRMGPR_Instr extends AVRInstr {
        public final AVROperand.MGPR rd;
        public final AVROperand.MGPR rr;
        protected MGPRMGPR_Instr(String name, int size, AVRAddrMode.MGPRMGPR am) {
            super(name, size);
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_MGPRMGPR(rd, rr);
        }
    }
    public abstract static class GPR_Instr extends AVRInstr {
        public final AVROperand.GPR rd;
        protected GPR_Instr(String name, int size, AVRAddrMode.GPR am) {
            super(name, size);
            this.rd = am.rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_GPR(rd);
        }
    }
    public abstract static class HGPRIMM8_Instr extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.IMM8 imm;
        protected HGPRIMM8_Instr(String name, int size, AVRAddrMode.HGPRIMM8 am) {
            super(name, size);
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_HGPRIMM8(rd, imm);
        }
    }
    public abstract static class ABS_Instr extends AVRInstr {
        public final AVROperand.PADDR target;
        protected ABS_Instr(String name, int size, AVRAddrMode.ABS am) {
            super(name, size);
            this.target = am.target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_ABS(target);
        }
    }
    public abstract static class BRANCH_Instr extends AVRInstr {
        public final AVROperand.SREL target;
        protected BRANCH_Instr(String name, int size, AVRAddrMode.BRANCH am) {
            super(name, size);
            this.target = am.target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_BRANCH(target);
        }
    }
    public abstract static class CALL_Instr extends AVRInstr {
        public final AVROperand.LREL target;
        protected CALL_Instr(String name, int size, AVRAddrMode.CALL am) {
            super(name, size);
            this.target = am.target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_CALL(target);
        }
    }
    public abstract static class WRITEBIT_Instr extends AVRInstr {
        protected WRITEBIT_Instr(String name, int size, AVRAddrMode.WRITEBIT am) {
            super(name, size);
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_WRITEBIT();
        }
    }
    public abstract static class XLPM_REG_Instr extends AVRInstr {
        public final AVROperand.R0_B dest;
        public final AVROperand.RZ_W source;
        protected XLPM_REG_Instr(String name, int size, AVRAddrMode.XLPM_REG am) {
            super(name, size);
            this.dest = am.dest;
            this.source = am.source;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_XLPM_REG(dest, source);
        }
    }
    public abstract static class XLPM_D_Instr extends AVRInstr {
        public final AVROperand.GPR dest;
        public final AVROperand.RZ_W source;
        protected XLPM_D_Instr(String name, int size, AVRAddrMode.XLPM_D am) {
            super(name, size);
            this.dest = am.dest;
            this.source = am.source;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_XLPM_D(dest, source);
        }
    }
    public abstract static class XLPM_INC_Instr extends AVRInstr {
        public final AVROperand.GPR dest;
        public final AVROperand.AI_RZ_W source;
        protected XLPM_INC_Instr(String name, int size, AVRAddrMode.XLPM_INC am) {
            super(name, size);
            this.dest = am.dest;
            this.source = am.source;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_XLPM_INC(dest, source);
        }
    }
    public abstract static class LD_ST_XYZ_Instr extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.XYZ ar;
        protected LD_ST_XYZ_Instr(String name, int size, AVRAddrMode.LD_ST_XYZ am) {
            super(name, size);
            this.rd = am.rd;
            this.ar = am.ar;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_LD_ST_XYZ(rd, ar);
        }
    }
    public abstract static class LD_ST_AI_XYZ_Instr extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.AI_XYZ ar;
        protected LD_ST_AI_XYZ_Instr(String name, int size, AVRAddrMode.LD_ST_AI_XYZ am) {
            super(name, size);
            this.rd = am.rd;
            this.ar = am.ar;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_LD_ST_AI_XYZ(rd, ar);
        }
    }
    public abstract static class LD_ST_PD_XYZ_Instr extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.PD_XYZ ar;
        protected LD_ST_PD_XYZ_Instr(String name, int size, AVRAddrMode.LD_ST_PD_XYZ am) {
            super(name, size);
            this.rd = am.rd;
            this.ar = am.ar;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_LD_ST_PD_XYZ(rd, ar);
        }
    }
    public abstract static class XLPM_Instr extends AVRInstr {
        public final AVRAddrMode.XLPM am;
        public final AVROperand source;
        public final AVROperand dest;
        protected XLPM_Instr(String name, int size, AVRAddrMode.XLPM am) {
            super(name, size);
            this.am = am;
            this.source = am.get_source();
            this.dest = am.get_dest();
        }
        public void accept(AVRAddrModeVisitor v) {
            am.accept(v);
        }
    }
    public abstract static class LD_ST_Instr extends AVRInstr {
        public final AVRAddrMode.LD_ST am;
        public final AVROperand rd;
        public final AVROperand ar;
        protected LD_ST_Instr(String name, int size, AVRAddrMode.LD_ST am) {
            super(name, size);
            this.am = am;
            this.rd = am.get_rd();
            this.ar = am.get_ar();
        }
        public void accept(AVRAddrModeVisitor v) {
            am.accept(v);
        }
    }
    public static class ADC extends GPRGPR_Instr {
        ADC(int size, AVRAddrMode.GPRGPR am) {
            super("adc", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ADD extends GPRGPR_Instr {
        ADD(int size, AVRAddrMode.GPRGPR am) {
            super("add", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ADIW extends AVRInstr {
        public final AVROperand.RDL rd;
        public final AVROperand.IMM6 imm;
        ADIW(int size, AVRAddrMode.$adiw$ am) {
            super("adiw", size);
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$adiw$(rd, imm);
        }
    }
    
    public static class AND extends GPRGPR_Instr {
        AND(int size, AVRAddrMode.GPRGPR am) {
            super("and", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ANDI extends HGPRIMM8_Instr {
        ANDI(int size, AVRAddrMode.HGPRIMM8 am) {
            super("andi", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ASR extends GPR_Instr {
        ASR(int size, AVRAddrMode.GPR am) {
            super("asr", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BCLR extends AVRInstr {
        public final AVROperand.IMM3 bit;
        BCLR(int size, AVRAddrMode.$bclr$ am) {
            super("bclr", size);
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bclr$(bit);
        }
    }
    
    public static class BLD extends AVRInstr {
        public final AVROperand.GPR rr;
        public final AVROperand.IMM3 bit;
        BLD(int size, AVRAddrMode.$bld$ am) {
            super("bld", size);
            this.rr = am.rr;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bld$(rr, bit);
        }
    }
    
    public static class BRBC extends AVRInstr {
        public final AVROperand.IMM3 bit;
        public final AVROperand.SREL target;
        BRBC(int size, AVRAddrMode.$brbc$ am) {
            super("brbc", size);
            this.bit = am.bit;
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brbc$(bit, target);
        }
    }
    
    public static class BRBS extends AVRInstr {
        public final AVROperand.IMM3 bit;
        public final AVROperand.SREL target;
        BRBS(int size, AVRAddrMode.$brbs$ am) {
            super("brbs", size);
            this.bit = am.bit;
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brbs$(bit, target);
        }
    }
    
    public static class BRCC extends BRANCH_Instr {
        BRCC(int size, AVRAddrMode.BRANCH am) {
            super("brcc", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRCS extends BRANCH_Instr {
        BRCS(int size, AVRAddrMode.BRANCH am) {
            super("brcs", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BREAK extends AVRInstr {
        BREAK(int size, AVRAddrMode.$break$ am) {
            super("break", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$break$();
        }
    }
    
    public static class BREQ extends BRANCH_Instr {
        BREQ(int size, AVRAddrMode.BRANCH am) {
            super("breq", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRGE extends BRANCH_Instr {
        BRGE(int size, AVRAddrMode.BRANCH am) {
            super("brge", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRHC extends BRANCH_Instr {
        BRHC(int size, AVRAddrMode.BRANCH am) {
            super("brhc", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRHS extends BRANCH_Instr {
        BRHS(int size, AVRAddrMode.BRANCH am) {
            super("brhs", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRID extends BRANCH_Instr {
        BRID(int size, AVRAddrMode.BRANCH am) {
            super("brid", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRIE extends BRANCH_Instr {
        BRIE(int size, AVRAddrMode.BRANCH am) {
            super("brie", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRLO extends BRANCH_Instr {
        BRLO(int size, AVRAddrMode.BRANCH am) {
            super("brlo", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRLT extends BRANCH_Instr {
        BRLT(int size, AVRAddrMode.BRANCH am) {
            super("brlt", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRMI extends BRANCH_Instr {
        BRMI(int size, AVRAddrMode.BRANCH am) {
            super("brmi", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRNE extends BRANCH_Instr {
        BRNE(int size, AVRAddrMode.BRANCH am) {
            super("brne", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRPL extends BRANCH_Instr {
        BRPL(int size, AVRAddrMode.BRANCH am) {
            super("brpl", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRSH extends BRANCH_Instr {
        BRSH(int size, AVRAddrMode.BRANCH am) {
            super("brsh", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRTC extends BRANCH_Instr {
        BRTC(int size, AVRAddrMode.BRANCH am) {
            super("brtc", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRTS extends BRANCH_Instr {
        BRTS(int size, AVRAddrMode.BRANCH am) {
            super("brts", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRVC extends BRANCH_Instr {
        BRVC(int size, AVRAddrMode.BRANCH am) {
            super("brvc", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BRVS extends BRANCH_Instr {
        BRVS(int size, AVRAddrMode.BRANCH am) {
            super("brvs", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class BSET extends AVRInstr {
        public final AVROperand.IMM3 bit;
        BSET(int size, AVRAddrMode.$bset$ am) {
            super("bset", size);
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bset$(bit);
        }
    }
    
    public static class BST extends AVRInstr {
        public final AVROperand.GPR rr;
        public final AVROperand.IMM3 bit;
        BST(int size, AVRAddrMode.$bst$ am) {
            super("bst", size);
            this.rr = am.rr;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bst$(rr, bit);
        }
    }
    
    public static class CALL extends AVRInstr {
        public final AVROperand.PADDR target;
        CALL(int size, AVRAddrMode.$call$ am) {
            super("call", size);
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$call$(target);
        }
    }
    
    public static class CBI extends AVRInstr {
        public final AVROperand.IMM5 ior;
        public final AVROperand.IMM3 bit;
        CBI(int size, AVRAddrMode.$cbi$ am) {
            super("cbi", size);
            this.ior = am.ior;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cbi$(ior, bit);
        }
    }
    
    public static class CBR extends HGPRIMM8_Instr {
        CBR(int size, AVRAddrMode.HGPRIMM8 am) {
            super("cbr", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CLC extends AVRInstr {
        CLC(int size, AVRAddrMode.$clc$ am) {
            super("clc", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clc$();
        }
    }
    
    public static class CLH extends AVRInstr {
        CLH(int size, AVRAddrMode.$clh$ am) {
            super("clh", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clh$();
        }
    }
    
    public static class CLI extends AVRInstr {
        CLI(int size, AVRAddrMode.$cli$ am) {
            super("cli", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cli$();
        }
    }
    
    public static class CLN extends AVRInstr {
        CLN(int size, AVRAddrMode.$cln$ am) {
            super("cln", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cln$();
        }
    }
    
    public static class CLR extends AVRInstr {
        public final AVROperand.GPR rd;
        CLR(int size, AVRAddrMode.$clr$ am) {
            super("clr", size);
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clr$(rd);
        }
    }
    
    public static class CLS extends AVRInstr {
        CLS(int size, AVRAddrMode.$cls$ am) {
            super("cls", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cls$();
        }
    }
    
    public static class CLT extends AVRInstr {
        CLT(int size, AVRAddrMode.$clt$ am) {
            super("clt", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clt$();
        }
    }
    
    public static class CLV extends AVRInstr {
        CLV(int size, AVRAddrMode.$clv$ am) {
            super("clv", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clv$();
        }
    }
    
    public static class CLZ extends AVRInstr {
        CLZ(int size, AVRAddrMode.$clz$ am) {
            super("clz", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clz$();
        }
    }
    
    public static class COM extends GPR_Instr {
        COM(int size, AVRAddrMode.GPR am) {
            super("com", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CP extends GPRGPR_Instr {
        CP(int size, AVRAddrMode.GPRGPR am) {
            super("cp", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CPC extends GPRGPR_Instr {
        CPC(int size, AVRAddrMode.GPRGPR am) {
            super("cpc", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CPI extends HGPRIMM8_Instr {
        CPI(int size, AVRAddrMode.HGPRIMM8 am) {
            super("cpi", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class CPSE extends GPRGPR_Instr {
        CPSE(int size, AVRAddrMode.GPRGPR am) {
            super("cpse", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class DEC extends GPR_Instr {
        DEC(int size, AVRAddrMode.GPR am) {
            super("dec", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class EICALL extends AVRInstr {
        EICALL(int size, AVRAddrMode.$eicall$ am) {
            super("eicall", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$eicall$();
        }
    }
    
    public static class EIJMP extends AVRInstr {
        EIJMP(int size, AVRAddrMode.$eijmp$ am) {
            super("eijmp", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$eijmp$();
        }
    }
    
    public static class EOR extends GPRGPR_Instr {
        EOR(int size, AVRAddrMode.GPRGPR am) {
            super("eor", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class FMUL extends AVRInstr {
        public final AVROperand.MGPR rd;
        public final AVROperand.MGPR rr;
        FMUL(int size, AVRAddrMode.$fmul$ am) {
            super("fmul", size);
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$fmul$(rd, rr);
        }
    }
    
    public static class FMULS extends AVRInstr {
        public final AVROperand.MGPR rd;
        public final AVROperand.MGPR rr;
        FMULS(int size, AVRAddrMode.$fmuls$ am) {
            super("fmuls", size);
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$fmuls$(rd, rr);
        }
    }
    
    public static class FMULSU extends AVRInstr {
        public final AVROperand.MGPR rd;
        public final AVROperand.MGPR rr;
        FMULSU(int size, AVRAddrMode.$fmulsu$ am) {
            super("fmulsu", size);
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$fmulsu$(rd, rr);
        }
    }
    
    public static class ICALL extends AVRInstr {
        ICALL(int size, AVRAddrMode.$icall$ am) {
            super("icall", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$icall$();
        }
    }
    
    public static class IJMP extends AVRInstr {
        IJMP(int size, AVRAddrMode.$ijmp$ am) {
            super("ijmp", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ijmp$();
        }
    }
    
    public static class IN extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.IMM6 imm;
        IN(int size, AVRAddrMode.$in$ am) {
            super("in", size);
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$in$(rd, imm);
        }
    }
    
    public static class INC extends GPR_Instr {
        INC(int size, AVRAddrMode.GPR am) {
            super("inc", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class JMP extends AVRInstr {
        public final AVROperand.PADDR target;
        JMP(int size, AVRAddrMode.$jmp$ am) {
            super("jmp", size);
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$jmp$(target);
        }
    }
    
    public static class LDD extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.YZ ar;
        public final AVROperand.IMM6 imm;
        LDD(int size, AVRAddrMode.$ldd$ am) {
            super("ldd", size);
            this.rd = am.rd;
            this.ar = am.ar;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ldd$(rd, ar, imm);
        }
    }
    
    public static class LDI extends HGPRIMM8_Instr {
        LDI(int size, AVRAddrMode.HGPRIMM8 am) {
            super("ldi", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class LDS extends AVRInstr {
        public final AVROperand.GPR rd;
        public final AVROperand.DADDR addr;
        LDS(int size, AVRAddrMode.$lds$ am) {
            super("lds", size);
            this.rd = am.rd;
            this.addr = am.addr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$lds$(rd, addr);
        }
    }
    
    public static class LSL extends AVRInstr {
        public final AVROperand.GPR rd;
        LSL(int size, AVRAddrMode.$lsl$ am) {
            super("lsl", size);
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$lsl$(rd);
        }
    }
    
    public static class LSR extends GPR_Instr {
        LSR(int size, AVRAddrMode.GPR am) {
            super("lsr", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class MOV extends GPRGPR_Instr {
        MOV(int size, AVRAddrMode.GPRGPR am) {
            super("mov", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class MOVW extends AVRInstr {
        public final AVROperand.EGPR rd;
        public final AVROperand.EGPR rr;
        MOVW(int size, AVRAddrMode.$movw$ am) {
            super("movw", size);
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$movw$(rd, rr);
        }
    }
    
    public static class MUL extends GPRGPR_Instr {
        MUL(int size, AVRAddrMode.GPRGPR am) {
            super("mul", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class MULS extends AVRInstr {
        public final AVROperand.HGPR rd;
        public final AVROperand.HGPR rr;
        MULS(int size, AVRAddrMode.$muls$ am) {
            super("muls", size);
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$muls$(rd, rr);
        }
    }
    
    public static class MULSU extends AVRInstr {
        public final AVROperand.MGPR rd;
        public final AVROperand.MGPR rr;
        MULSU(int size, AVRAddrMode.$mulsu$ am) {
            super("mulsu", size);
            this.rd = am.rd;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$mulsu$(rd, rr);
        }
    }
    
    public static class NEG extends GPR_Instr {
        NEG(int size, AVRAddrMode.GPR am) {
            super("neg", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class NOP extends AVRInstr {
        NOP(int size, AVRAddrMode.$nop$ am) {
            super("nop", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$nop$();
        }
    }
    
    public static class OR extends GPRGPR_Instr {
        OR(int size, AVRAddrMode.GPRGPR am) {
            super("or", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ORI extends HGPRIMM8_Instr {
        ORI(int size, AVRAddrMode.HGPRIMM8 am) {
            super("ori", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class OUT extends AVRInstr {
        public final AVROperand.IMM6 ior;
        public final AVROperand.GPR rr;
        OUT(int size, AVRAddrMode.$out$ am) {
            super("out", size);
            this.ior = am.ior;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$out$(ior, rr);
        }
    }
    
    public static class POP extends GPR_Instr {
        POP(int size, AVRAddrMode.GPR am) {
            super("pop", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class PUSH extends AVRInstr {
        public final AVROperand.GPR rr;
        PUSH(int size, AVRAddrMode.$push$ am) {
            super("push", size);
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$push$(rr);
        }
    }
    
    public static class RCALL extends AVRInstr {
        public final AVROperand.LREL target;
        RCALL(int size, AVRAddrMode.$rcall$ am) {
            super("rcall", size);
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$rcall$(target);
        }
    }
    
    public static class RET extends AVRInstr {
        RET(int size, AVRAddrMode.$ret$ am) {
            super("ret", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ret$();
        }
    }
    
    public static class RETI extends AVRInstr {
        RETI(int size, AVRAddrMode.$reti$ am) {
            super("reti", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$reti$();
        }
    }
    
    public static class RJMP extends AVRInstr {
        public final AVROperand.LREL target;
        RJMP(int size, AVRAddrMode.$rjmp$ am) {
            super("rjmp", size);
            this.target = am.target;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$rjmp$(target);
        }
    }
    
    public static class ROL extends AVRInstr {
        public final AVROperand.GPR rd;
        ROL(int size, AVRAddrMode.$rol$ am) {
            super("rol", size);
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$rol$(rd);
        }
    }
    
    public static class ROR extends GPR_Instr {
        ROR(int size, AVRAddrMode.GPR am) {
            super("ror", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBC extends GPRGPR_Instr {
        SBC(int size, AVRAddrMode.GPRGPR am) {
            super("sbc", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBCI extends HGPRIMM8_Instr {
        SBCI(int size, AVRAddrMode.HGPRIMM8 am) {
            super("sbci", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBI extends AVRInstr {
        public final AVROperand.IMM5 ior;
        public final AVROperand.IMM3 bit;
        SBI(int size, AVRAddrMode.$sbi$ am) {
            super("sbi", size);
            this.ior = am.ior;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbi$(ior, bit);
        }
    }
    
    public static class SBIC extends AVRInstr {
        public final AVROperand.IMM5 ior;
        public final AVROperand.IMM3 bit;
        SBIC(int size, AVRAddrMode.$sbic$ am) {
            super("sbic", size);
            this.ior = am.ior;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbic$(ior, bit);
        }
    }
    
    public static class SBIS extends AVRInstr {
        public final AVROperand.IMM5 ior;
        public final AVROperand.IMM3 bit;
        SBIS(int size, AVRAddrMode.$sbis$ am) {
            super("sbis", size);
            this.ior = am.ior;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbis$(ior, bit);
        }
    }
    
    public static class SBIW extends AVRInstr {
        public final AVROperand.RDL rd;
        public final AVROperand.IMM6 imm;
        SBIW(int size, AVRAddrMode.$sbiw$ am) {
            super("sbiw", size);
            this.rd = am.rd;
            this.imm = am.imm;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbiw$(rd, imm);
        }
    }
    
    public static class SBR extends HGPRIMM8_Instr {
        SBR(int size, AVRAddrMode.HGPRIMM8 am) {
            super("sbr", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SBRC extends AVRInstr {
        public final AVROperand.GPR rr;
        public final AVROperand.IMM3 bit;
        SBRC(int size, AVRAddrMode.$sbrc$ am) {
            super("sbrc", size);
            this.rr = am.rr;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbrc$(rr, bit);
        }
    }
    
    public static class SBRS extends AVRInstr {
        public final AVROperand.GPR rr;
        public final AVROperand.IMM3 bit;
        SBRS(int size, AVRAddrMode.$sbrs$ am) {
            super("sbrs", size);
            this.rr = am.rr;
            this.bit = am.bit;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbrs$(rr, bit);
        }
    }
    
    public static class SEC extends AVRInstr {
        SEC(int size, AVRAddrMode.$sec$ am) {
            super("sec", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sec$();
        }
    }
    
    public static class SEH extends AVRInstr {
        SEH(int size, AVRAddrMode.$seh$ am) {
            super("seh", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$seh$();
        }
    }
    
    public static class SEI extends AVRInstr {
        SEI(int size, AVRAddrMode.$sei$ am) {
            super("sei", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sei$();
        }
    }
    
    public static class SEN extends AVRInstr {
        SEN(int size, AVRAddrMode.$sen$ am) {
            super("sen", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sen$();
        }
    }
    
    public static class SER extends AVRInstr {
        public final AVROperand.HGPR rd;
        SER(int size, AVRAddrMode.$ser$ am) {
            super("ser", size);
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ser$(rd);
        }
    }
    
    public static class SES extends AVRInstr {
        SES(int size, AVRAddrMode.$ses$ am) {
            super("ses", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ses$();
        }
    }
    
    public static class SET extends AVRInstr {
        SET(int size, AVRAddrMode.$set$ am) {
            super("set", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$set$();
        }
    }
    
    public static class SEV extends AVRInstr {
        SEV(int size, AVRAddrMode.$sev$ am) {
            super("sev", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sev$();
        }
    }
    
    public static class SEZ extends AVRInstr {
        SEZ(int size, AVRAddrMode.$sez$ am) {
            super("sez", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sez$();
        }
    }
    
    public static class SLEEP extends AVRInstr {
        SLEEP(int size, AVRAddrMode.$sleep$ am) {
            super("sleep", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sleep$();
        }
    }
    
    public static class SPM extends AVRInstr {
        SPM(int size, AVRAddrMode.$spm$ am) {
            super("spm", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$spm$();
        }
    }
    
    public static class STD extends AVRInstr {
        public final AVROperand.YZ ar;
        public final AVROperand.IMM6 imm;
        public final AVROperand.GPR rr;
        STD(int size, AVRAddrMode.$std$ am) {
            super("std", size);
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
        public final AVROperand.DADDR addr;
        public final AVROperand.GPR rr;
        STS(int size, AVRAddrMode.$sts$ am) {
            super("sts", size);
            this.addr = am.addr;
            this.rr = am.rr;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sts$(addr, rr);
        }
    }
    
    public static class SUB extends GPRGPR_Instr {
        SUB(int size, AVRAddrMode.GPRGPR am) {
            super("sub", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SUBI extends HGPRIMM8_Instr {
        SUBI(int size, AVRAddrMode.HGPRIMM8 am) {
            super("subi", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class SWAP extends GPR_Instr {
        SWAP(int size, AVRAddrMode.GPR am) {
            super("swap", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class TST extends AVRInstr {
        public final AVROperand.GPR rd;
        TST(int size, AVRAddrMode.$tst$ am) {
            super("tst", size);
            this.rd = am.rd;
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$tst$(rd);
        }
    }
    
    public static class WDR extends AVRInstr {
        WDR(int size, AVRAddrMode.$wdr$ am) {
            super("wdr", size);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$wdr$();
        }
    }
    
    public static class ELPM extends XLPM_Instr {
        ELPM(int size, AVRAddrMode.XLPM am) {
            super("elpm", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class LPM extends XLPM_Instr {
        LPM(int size, AVRAddrMode.XLPM am) {
            super("lpm", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class LD extends LD_ST_Instr {
        LD(int size, AVRAddrMode.LD_ST am) {
            super("ld", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
    public static class ST extends LD_ST_Instr {
        ST(int size, AVRAddrMode.LD_ST am) {
            super("st", size, am);
        }
        public void accept(AVRInstrVisitor v) { v.visit(this); }
    }
    
}
