package avrora.arch.msp430;
import java.util.HashMap;
public abstract class MSP430InstrBuilder {
    public abstract MSP430Instr build(int size, MSP430AddrMode am);
    static final HashMap builders = new HashMap();
    static MSP430InstrBuilder add(String name, MSP430InstrBuilder b) {
        builders.put(name, b);
        return b;
    }
    public static class ADC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.ADC(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class ADC_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.ADC_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class ADD_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.ADD(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class ADD_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.ADD_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class ADDC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.ADDC(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class ADDC_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.ADDC_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class AND_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.AND(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class AND_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.AND_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class BIC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.BIC(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class BIC_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.BIC_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class BIS_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.BIS(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class BIS_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.BIS_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class BIT_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.BIT(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class BIT_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.BIT_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class BR_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.BR(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class CALL_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.CALL(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class CLR_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.CLR(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class CLR_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.CLR_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class CLRC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.CLRC(size);
        }
    }
    public static class CLRN_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.CLRN(size);
        }
    }
    public static class CLRZ_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.CLRZ(size);
        }
    }
    public static class CMP_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.CMP(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class CMP_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.CMP_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class DADC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.DADC(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class DADC_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.DADC_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class DADD_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.DADD(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class DADD_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.DADD_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class DEC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.DEC(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class DEC_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.DEC_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class DECD_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.DECD(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class DECD_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.DECD_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class DINT_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.DINT(size);
        }
    }
    public static class EINT_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.EINT(size);
        }
    }
    public static class INC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.INC(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class INC_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.INC_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class INCD_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.INCD(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class INCD_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.INCD_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class INV_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.INV(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class INV_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.INV_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class JC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JC(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JHS_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JHS(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JEQ_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JEQ(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JZ_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JZ(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JGE_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JGE(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JL_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JL(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JMP_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JMP(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JN_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JN(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JNC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JNC(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JLO_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JLO(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JNE_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JNE(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class JNZ_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.JNZ(size, (MSP430AddrMode.JMP)am);
        }
    }
    public static class MOV_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.MOV(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class MOV_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.MOV_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class NOP_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.NOP(size);
        }
    }
    public static class POP_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.POP(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class POP_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.POP_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class PUSH_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.PUSH(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class PUSH_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.PUSH_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class RET_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.RET(size);
        }
    }
    public static class RETI_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.RETI(size);
        }
    }
    public static class RLA_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.RLA(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class RLA_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.RLA_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class RLC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.RLC(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class RLC_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.RLC_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class RRA_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.RRA(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class RRA_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.RRA_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class RRC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.RRC(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class RRC_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.RRC_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class SBC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SBC(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class SBC_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SBC_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class SETC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SETC(size);
        }
    }
    public static class SETN_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SETN(size);
        }
    }
    public static class SETZ_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SETZ(size);
        }
    }
    public static class SUB_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SUB(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class SUB_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SUB_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class SUBC_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SUBC(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class SUBC_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SUBC_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class SBB_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SBB(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class SBB_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SBB_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static class SWPB_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SWPB(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class SXT_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.SXT(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class TST_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.TST(size, (MSP430AddrMode.SINGLE_W)am);
        }
    }
    public static class TST_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.TST_B(size, (MSP430AddrMode.SINGLE_B)am);
        }
    }
    public static class XOR_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.XOR(size, (MSP430AddrMode.DOUBLE_W)am);
        }
    }
    public static class XOR_B_builder extends MSP430InstrBuilder {
        public MSP430Instr build(int size, MSP430AddrMode am) {
            return new MSP430Instr.XOR_B(size, (MSP430AddrMode.DOUBLE_B)am);
        }
    }
    public static final MSP430InstrBuilder ADC = add("adc", new ADC_builder());
    public static final MSP430InstrBuilder ADC_B = add("adc.b", new ADC_B_builder());
    public static final MSP430InstrBuilder ADD = add("add", new ADD_builder());
    public static final MSP430InstrBuilder ADD_B = add("add.b", new ADD_B_builder());
    public static final MSP430InstrBuilder ADDC = add("addc", new ADDC_builder());
    public static final MSP430InstrBuilder ADDC_B = add("addc.b", new ADDC_B_builder());
    public static final MSP430InstrBuilder AND = add("and", new AND_builder());
    public static final MSP430InstrBuilder AND_B = add("and.b", new AND_B_builder());
    public static final MSP430InstrBuilder BIC = add("bic", new BIC_builder());
    public static final MSP430InstrBuilder BIC_B = add("bic.b", new BIC_B_builder());
    public static final MSP430InstrBuilder BIS = add("bis", new BIS_builder());
    public static final MSP430InstrBuilder BIS_B = add("bis.b", new BIS_B_builder());
    public static final MSP430InstrBuilder BIT = add("bit", new BIT_builder());
    public static final MSP430InstrBuilder BIT_B = add("bit.b", new BIT_B_builder());
    public static final MSP430InstrBuilder BR = add("br", new BR_builder());
    public static final MSP430InstrBuilder CALL = add("call", new CALL_builder());
    public static final MSP430InstrBuilder CLR = add("clr", new CLR_builder());
    public static final MSP430InstrBuilder CLR_B = add("clr.b", new CLR_B_builder());
    public static final MSP430InstrBuilder CLRC = add("clrc", new CLRC_builder());
    public static final MSP430InstrBuilder CLRN = add("clrn", new CLRN_builder());
    public static final MSP430InstrBuilder CLRZ = add("clrz", new CLRZ_builder());
    public static final MSP430InstrBuilder CMP = add("cmp", new CMP_builder());
    public static final MSP430InstrBuilder CMP_B = add("cmp.b", new CMP_B_builder());
    public static final MSP430InstrBuilder DADC = add("dadc", new DADC_builder());
    public static final MSP430InstrBuilder DADC_B = add("dadc.b", new DADC_B_builder());
    public static final MSP430InstrBuilder DADD = add("dadd", new DADD_builder());
    public static final MSP430InstrBuilder DADD_B = add("dadd.b", new DADD_B_builder());
    public static final MSP430InstrBuilder DEC = add("dec", new DEC_builder());
    public static final MSP430InstrBuilder DEC_B = add("dec.b", new DEC_B_builder());
    public static final MSP430InstrBuilder DECD = add("decd", new DECD_builder());
    public static final MSP430InstrBuilder DECD_B = add("decd.b", new DECD_B_builder());
    public static final MSP430InstrBuilder DINT = add("dint", new DINT_builder());
    public static final MSP430InstrBuilder EINT = add("eint", new EINT_builder());
    public static final MSP430InstrBuilder INC = add("inc", new INC_builder());
    public static final MSP430InstrBuilder INC_B = add("inc.b", new INC_B_builder());
    public static final MSP430InstrBuilder INCD = add("incd", new INCD_builder());
    public static final MSP430InstrBuilder INCD_B = add("incd.b", new INCD_B_builder());
    public static final MSP430InstrBuilder INV = add("inv", new INV_builder());
    public static final MSP430InstrBuilder INV_B = add("inv.b", new INV_B_builder());
    public static final MSP430InstrBuilder JC = add("jc", new JC_builder());
    public static final MSP430InstrBuilder JHS = add("jhs", new JHS_builder());
    public static final MSP430InstrBuilder JEQ = add("jeq", new JEQ_builder());
    public static final MSP430InstrBuilder JZ = add("jz", new JZ_builder());
    public static final MSP430InstrBuilder JGE = add("jge", new JGE_builder());
    public static final MSP430InstrBuilder JL = add("jl", new JL_builder());
    public static final MSP430InstrBuilder JMP = add("jmp", new JMP_builder());
    public static final MSP430InstrBuilder JN = add("jn", new JN_builder());
    public static final MSP430InstrBuilder JNC = add("jnc", new JNC_builder());
    public static final MSP430InstrBuilder JLO = add("jlo", new JLO_builder());
    public static final MSP430InstrBuilder JNE = add("jne", new JNE_builder());
    public static final MSP430InstrBuilder JNZ = add("jnz", new JNZ_builder());
    public static final MSP430InstrBuilder MOV = add("mov", new MOV_builder());
    public static final MSP430InstrBuilder MOV_B = add("mov.b", new MOV_B_builder());
    public static final MSP430InstrBuilder NOP = add("nop", new NOP_builder());
    public static final MSP430InstrBuilder POP = add("pop", new POP_builder());
    public static final MSP430InstrBuilder POP_B = add("pop.b", new POP_B_builder());
    public static final MSP430InstrBuilder PUSH = add("push", new PUSH_builder());
    public static final MSP430InstrBuilder PUSH_B = add("push.b", new PUSH_B_builder());
    public static final MSP430InstrBuilder RET = add("ret", new RET_builder());
    public static final MSP430InstrBuilder RETI = add("reti", new RETI_builder());
    public static final MSP430InstrBuilder RLA = add("rla", new RLA_builder());
    public static final MSP430InstrBuilder RLA_B = add("rla.b", new RLA_B_builder());
    public static final MSP430InstrBuilder RLC = add("rlc", new RLC_builder());
    public static final MSP430InstrBuilder RLC_B = add("rlc.b", new RLC_B_builder());
    public static final MSP430InstrBuilder RRA = add("rra", new RRA_builder());
    public static final MSP430InstrBuilder RRA_B = add("rra.b", new RRA_B_builder());
    public static final MSP430InstrBuilder RRC = add("rrc", new RRC_builder());
    public static final MSP430InstrBuilder RRC_B = add("rrc.b", new RRC_B_builder());
    public static final MSP430InstrBuilder SBC = add("sbc", new SBC_builder());
    public static final MSP430InstrBuilder SBC_B = add("sbc.b", new SBC_B_builder());
    public static final MSP430InstrBuilder SETC = add("setc", new SETC_builder());
    public static final MSP430InstrBuilder SETN = add("setn", new SETN_builder());
    public static final MSP430InstrBuilder SETZ = add("setz", new SETZ_builder());
    public static final MSP430InstrBuilder SUB = add("sub", new SUB_builder());
    public static final MSP430InstrBuilder SUB_B = add("sub.b", new SUB_B_builder());
    public static final MSP430InstrBuilder SUBC = add("subc", new SUBC_builder());
    public static final MSP430InstrBuilder SUBC_B = add("subc.b", new SUBC_B_builder());
    public static final MSP430InstrBuilder SBB = add("sbb", new SBB_builder());
    public static final MSP430InstrBuilder SBB_B = add("sbb.b", new SBB_B_builder());
    public static final MSP430InstrBuilder SWPB = add("swpb", new SWPB_builder());
    public static final MSP430InstrBuilder SXT = add("sxt", new SXT_builder());
    public static final MSP430InstrBuilder TST = add("tst", new TST_builder());
    public static final MSP430InstrBuilder TST_B = add("tst.b", new TST_B_builder());
    public static final MSP430InstrBuilder XOR = add("xor", new XOR_builder());
    public static final MSP430InstrBuilder XOR_B = add("xor.b", new XOR_B_builder());
    public static int checkValue(int val, int low, int high) {
        if ( val < low || val > high ) {
            throw new Error();
        }
        return val;
    }
}
