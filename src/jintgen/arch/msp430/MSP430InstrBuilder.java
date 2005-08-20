package jintgen.arch.msp430;
import java.util.HashMap;
public class MSP430InstrBuilder {
    public static abstract class Single {
        public abstract MSP430Instr build(MSP430Operand[] operands);
    }
    static final HashMap builders = new HashMap();
    static Single addSingle(String name, Single s) {
        builders.put(name, s);
        return s;
    }
    public static final Single ADC = addSingle("adc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.ADC((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single ADC_B = addSingle("adc.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.ADC_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single ADD = addSingle("add", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.ADD((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single ADD_B = addSingle("add.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.ADD_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single ADDC = addSingle("addc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.ADDC((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single ADDC_B = addSingle("addc.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.ADDC_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single AND = addSingle("and", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.AND((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single AND_B = addSingle("and.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.AND_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single BIC = addSingle("bic", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.BIC((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single BIC_B = addSingle("bic.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.BIC_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single BIS = addSingle("bis", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.BIS((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single BIS_B = addSingle("bis.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.BIS_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single BIT = addSingle("bit", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.BIT((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single BIT_B = addSingle("bit.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.BIT_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single BR = addSingle("br", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.BR((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single CALL = addSingle("call", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.CALL((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single CLR = addSingle("clr", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.CLR((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single CLR_B = addSingle("clr.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.CLR_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single CLRC = addSingle("clrc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.CLRC();
        }
    });
    public static final Single CLRN = addSingle("clrn", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.CLRN();
        }
    });
    public static final Single CLRZ = addSingle("clrz", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.CLRZ();
        }
    });
    public static final Single CMP = addSingle("cmp", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.CMP((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single CMP_B = addSingle("cmp.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.CMP_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single DADC = addSingle("dadc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.DADC((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single DADC_B = addSingle("dadc.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.DADC_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single DADD = addSingle("dadd", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.DADD((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single DADD_B = addSingle("dadd.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.DADD_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single DEC = addSingle("dec", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.DEC((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single DEC_B = addSingle("dec.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.DEC_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single DECD = addSingle("decd", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.DECD((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single DECD_B = addSingle("decd.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.DECD_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single DINT = addSingle("dint", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.DINT();
        }
    });
    public static final Single EINT = addSingle("eint", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.EINT();
        }
    });
    public static final Single INC = addSingle("inc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.INC((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single INC_B = addSingle("inc.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.INC_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single INCD = addSingle("incd", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.INCD((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single INCD_B = addSingle("incd.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.INCD_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single INV = addSingle("inv", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.INV((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single INV_B = addSingle("inv.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.INV_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single JC = addSingle("jc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JC((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JHS = addSingle("jhs", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JHS((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JEQ = addSingle("jeq", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JEQ((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JZ = addSingle("jz", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JZ((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JGE = addSingle("jge", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JGE((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JL = addSingle("jl", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JL((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JMP = addSingle("jmp", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JMP((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JN = addSingle("jn", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JN((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JNC = addSingle("jnc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JNC((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JLO = addSingle("jlo", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JLO((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JNE = addSingle("jne", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JNE((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single JNZ = addSingle("jnz", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.JNZ((MSP430Operand.JUMP)operands[0]);
        }
    });
    public static final Single MOV = addSingle("mov", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.MOV((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single MOV_B = addSingle("mov.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.MOV_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single NOP = addSingle("nop", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.NOP();
        }
    });
    public static final Single POP = addSingle("pop", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.POP((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single POP_B = addSingle("pop.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.POP_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single PUSH = addSingle("push", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.PUSH((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single PUSH_B = addSingle("push.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.PUSH_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single RET = addSingle("ret", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.RET();
        }
    });
    public static final Single RETI = addSingle("reti", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.RETI();
        }
    });
    public static final Single RLA = addSingle("rla", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.RLA((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single RLA_B = addSingle("rla.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.RLA_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single RLC = addSingle("rlc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.RLC((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single RLC_B = addSingle("rlc.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.RLC_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single RRA = addSingle("rra", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.RRA((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single RRA_B = addSingle("rra.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.RRA_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single RRC = addSingle("rrc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.RRC((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single RRC_B = addSingle("rrc.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.RRC_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single SBC = addSingle("sbc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.SBC((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single SBC_B = addSingle("sbc.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.SBC_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single SETC = addSingle("setc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.SETC();
        }
    });
    public static final Single SETN = addSingle("setn", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.SETN();
        }
    });
    public static final Single SETZ = addSingle("setz", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 0;
            return new MSP430Instr.SETZ();
        }
    });
    public static final Single SUB = addSingle("sub", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.SUB((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single SUB_B = addSingle("sub.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.SUB_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single SUBC = addSingle("subc", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.SUBC((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single SUBC_B = addSingle("subc.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.SUBC_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single SBB = addSingle("sbb", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.SBB((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single SBB_B = addSingle("sbb.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.SBB_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static final Single SWPB = addSingle("swpb", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.SWPB((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single SXT = addSingle("sxt", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.SXT((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single TST = addSingle("tst", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.TST((MSP430Operand.SINGLE_W_source_union)operands[0]);
        }
    });
    public static final Single TST_B = addSingle("tst.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 1;
            return new MSP430Instr.TST_B((MSP430Operand.SINGLE_B_source_union)operands[0]);
        }
    });
    public static final Single XOR = addSingle("xor", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.XOR((MSP430Operand.DOUBLE_W_source_union)operands[0], (MSP430Operand.DOUBLE_W_dest_union)operands[1]);
        }
    });
    public static final Single XOR_B = addSingle("xor.b", new Single() {
        public MSP430Instr build(MSP430Operand[] operands) {
            assert operands.length == 2;
            return new MSP430Instr.XOR_B((MSP430Operand.DOUBLE_B_source_union)operands[0], (MSP430Operand.DOUBLE_B_dest_union)operands[1]);
        }
    });
    public static int checkValue(int val, int low, int high) {
        if ( val < low || val > high ) {
            throw new Error();
        }
        return val;
    }
}
