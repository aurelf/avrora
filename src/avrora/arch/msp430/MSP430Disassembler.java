package avrora.arch.msp430;
import java.util.Arrays;

/**
 * The <code>MSP430Disassembler</code> class decodes bit patterns into
 * instructions. It has been generated automatically by jIntGen from a
 * file containing a description of the instruction set and their
 * encodings.
 * 
 * The following options have been specified to tune this implementation:
 * 
 * </p>-word-size=16
 * </p>-parallel-trees=true
 * </p>-multiple-trees=false
 */
public class MSP430Disassembler {
    static class InvalidInstruction extends Exception {
        InvalidInstruction(int pc)  {
            super("Invalid instruction at "+pc);
        }
    }
    static final MSP430Symbol.GPR[] GPR_table = {
        MSP430Symbol.GPR.R0,  // 0 (0b0000) -> r0
        MSP430Symbol.GPR.R1,  // 1 (0b0001) -> r1
        MSP430Symbol.GPR.R2,  // 2 (0b0010) -> r2
        MSP430Symbol.GPR.R3,  // 3 (0b0011) -> r3
        MSP430Symbol.GPR.R4,  // 4 (0b0100) -> r4
        MSP430Symbol.GPR.R5,  // 5 (0b0101) -> r5
        MSP430Symbol.GPR.R6,  // 6 (0b0110) -> r6
        MSP430Symbol.GPR.R7,  // 7 (0b0111) -> r7
        MSP430Symbol.GPR.R8,  // 8 (0b1000) -> r8
        MSP430Symbol.GPR.R9,  // 9 (0b1001) -> r9
        MSP430Symbol.GPR.R10,  // 10 (0b1010) -> r10
        MSP430Symbol.GPR.R11,  // 11 (0b1011) -> r11
        MSP430Symbol.GPR.R12,  // 12 (0b1100) -> r12
        MSP430Symbol.GPR.R13,  // 13 (0b1101) -> r13
        MSP430Symbol.GPR.R14,  // 14 (0b1110) -> r14
        MSP430Symbol.GPR.R15 // 15 (0b1111) -> r15
    };
    static final MSP430Symbol.SREG[] SREG_table = {
        null,  // 0 (0b0000) -> null
        null,  // 1 (0b0001) -> null
        MSP430Symbol.SREG.R2,  // 2 (0b0010) -> r2
        MSP430Symbol.SREG.R3,  // 3 (0b0011) -> r3
        MSP430Symbol.SREG.R4,  // 4 (0b0100) -> r4
        MSP430Symbol.SREG.R5,  // 5 (0b0101) -> r5
        MSP430Symbol.SREG.R6,  // 6 (0b0110) -> r6
        MSP430Symbol.SREG.R7,  // 7 (0b0111) -> r7
        MSP430Symbol.SREG.R8,  // 8 (0b1000) -> r8
        MSP430Symbol.SREG.R9,  // 9 (0b1001) -> r9
        MSP430Symbol.SREG.R10,  // 10 (0b1010) -> r10
        MSP430Symbol.SREG.R11,  // 11 (0b1011) -> r11
        MSP430Symbol.SREG.R12,  // 12 (0b1100) -> r12
        MSP430Symbol.SREG.R13,  // 13 (0b1101) -> r13
        MSP430Symbol.SREG.R14,  // 14 (0b1110) -> r14
        MSP430Symbol.SREG.R15 // 15 (0b1111) -> r15
    };
    static int readop_2(MSP430Disassembler d) {
        int result = (d.word1 & 0xFFFF);
        return result;
    }
    static int readop_1(MSP430Disassembler d) {
        int result = ((d.word0 >>> 12) & 0x000F);
        return result;
    }
    static int readop_4(MSP430Disassembler d) {
        int result = ((d.word0 >>> 6) & 0x03FF);
        return result;
    }
    static int readop_0(MSP430Disassembler d) {
        int result = ((d.word0 >>> 4) & 0x000F);
        return result;
    }
    static int readop_3(MSP430Disassembler d) {
        int result = (d.word2 & 0xFFFF);
        return result;
    }
    static class AUTOREG_B_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.AIREG_B source = new MSP430Operand.AIREG_B(SREG_table[readop_0(d)]);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class REGSYM_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SREG source = new MSP430Operand.SREG(SREG_table[readop_0(d)]);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class ABSABS_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.ABSO source = new MSP430Operand.ABSO(readop_2(d));
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_3(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMREG_1_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(-1);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class IMMIND_4_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(2);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class AUTO_B_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.AIREG_B source = new MSP430Operand.AIREG_B(SREG_table[readop_1(d)]);
            return d.fill_1(source);
        }
    }
    static class IMMIND_1_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(-1);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class INDREG_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SREG source_reg = new MSP430Operand.SREG(SREG_table[readop_0(d)]);
            MSP430Operand.IMM source_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX source = new MSP430Operand.INDX(source_reg, source_index);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class AUTOREG_W_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.AIREG_W source = new MSP430Operand.AIREG_W(SREG_table[readop_0(d)]);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class IREGABS_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IREG source = new MSP430Operand.IREG(SREG_table[readop_0(d)]);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMREG_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class IMMSYM_4_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(2);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class SYM_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SYM source = new MSP430Operand.SYM(readop_2(d));
            return d.fill_1(source);
        }
    }
    static class IMMABS_4_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(2);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMIND_2_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(0);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class INDABS_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SREG source_reg = new MSP430Operand.SREG(SREG_table[readop_0(d)]);
            MSP430Operand.IMM source_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX source = new MSP430Operand.INDX(source_reg, source_index);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_3(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMREG_3_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(1);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class IMMABS_2_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(0);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class ABSREG_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.ABSO source = new MSP430Operand.ABSO(readop_2(d));
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class IMMIND_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_3(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class IMMSYM_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_3(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMM_6_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(8);
            return d.fill_1(source);
        }
    }
    static class IMMABS_5_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(4);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class REGABS_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SREG source = new MSP430Operand.SREG(SREG_table[readop_0(d)]);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class SYMSYM_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SYM source = new MSP430Operand.SYM(readop_2(d));
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_3(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMSYM_2_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(0);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMSYM_6_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(8);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class SYMABS_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SYM source = new MSP430Operand.SYM(readop_2(d));
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_3(d));
            return d.fill_2(source, dest);
        }
    }
    static class AUTOIND_W_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.AIREG_W source = new MSP430Operand.AIREG_W(SREG_table[readop_0(d)]);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class IMMREG_5_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(4);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class AUTOABS_B_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.AIREG_B source = new MSP430Operand.AIREG_B(SREG_table[readop_0(d)]);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class REG_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SREG source = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_1(source);
        }
    }
    static class IMMREG_2_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(0);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class IMMIND_6_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(8);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class IMM_4_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(2);
            return d.fill_1(source);
        }
    }
    static class INDIND_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SREG source_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM source_index = new MSP430Operand.IMM(readop_3(d));
            MSP430Operand.INDX source = new MSP430Operand.INDX(source_reg, source_index);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_3(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class IMM_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(readop_2(d));
            return d.fill_1(source);
        }
    }
    static class AUTOIND_B_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.AIREG_B source = new MSP430Operand.AIREG_B(SREG_table[readop_0(d)]);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class REGREG_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SREG source = new MSP430Operand.SREG(SREG_table[readop_0(d)]);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class IMMIND_3_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(1);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class JMP_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.JUMP source = new MSP430Operand.JUMP(readop_4(d));
            return d.fill_1(source);
        }
    }
    static class IMMREG_6_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(8);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class IMMABS_1_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(-1);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMM_1_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(-1);
            return d.fill_1(source);
        }
    }
    static class IMMABS_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_3(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMIND_5_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(4);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class IMM_3_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(1);
            return d.fill_1(source);
        }
    }
    static class SYMREG_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SYM source = new MSP430Operand.SYM(readop_2(d));
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class IREGIND_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IREG source = new MSP430Operand.IREG(SREG_table[readop_0(d)]);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class IND_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SREG source_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM source_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX source = new MSP430Operand.INDX(source_reg, source_index);
            return d.fill_1(source);
        }
    }
    static class IMM_2_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(0);
            return d.fill_1(source);
        }
    }
    static class IMMABS_3_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(1);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMSYM_5_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(4);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMSYM_1_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(-1);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class ABSIND_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.ABSO source = new MSP430Operand.ABSO(readop_2(d));
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_3(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class AUTOSYM_W_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.AIREG_W source = new MSP430Operand.AIREG_W(SREG_table[readop_0(d)]);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMM_5_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(4);
            return d.fill_1(source);
        }
    }
    static class ABS_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.ABSO source = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_1(source);
        }
    }
    static class AUTO_W_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.AIREG_W source = new MSP430Operand.AIREG_W(SREG_table[readop_1(d)]);
            return d.fill_1(source);
        }
    }
    static class AUTOABS_W_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.AIREG_W source = new MSP430Operand.AIREG_W(SREG_table[readop_0(d)]);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class INDSYM_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SREG source_reg = new MSP430Operand.SREG(SREG_table[readop_0(d)]);
            MSP430Operand.IMM source_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX source = new MSP430Operand.INDX(source_reg, source_index);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_3(d));
            return d.fill_2(source, dest);
        }
    }
    static class REGIND_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SREG source = new MSP430Operand.SREG(SREG_table[readop_0(d)]);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_2(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class IREG_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IREG source = new MSP430Operand.IREG(SREG_table[readop_1(d)]);
            return d.fill_1(source);
        }
    }
    static class SYMIND_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.SYM source = new MSP430Operand.SYM(readop_2(d));
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_3(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return d.fill_2(source, dest);
        }
    }
    static class IREGREG_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IREG source = new MSP430Operand.IREG(SREG_table[readop_0(d)]);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class IMMREG_4_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(2);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            return d.fill_2(source, dest);
        }
    }
    static class AUTOSYM_B_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.AIREG_B source = new MSP430Operand.AIREG_B(SREG_table[readop_0(d)]);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class ABSSYM_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.ABSO source = new MSP430Operand.ABSO(readop_2(d));
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_3(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMABS_6_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(8);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class IMMSYM_3_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(1);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    static class IREGSYM_0_reader extends OperandReader {
        MSP430Operand[] read(MSP430Disassembler d) {
            MSP430Operand.IREG source = new MSP430Operand.IREG(SREG_table[readop_0(d)]);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_2(d));
            return d.fill_2(source, dest);
        }
    }
    final MSP430Operand[] OPERANDS_0 = new MSP430Operand[0];
    final MSP430Operand[] OPERANDS_1 = new MSP430Operand[1];
    final MSP430Operand[] OPERANDS_2 = new MSP430Operand[2];
    MSP430Operand[] fill_1(MSP430Operand o0){
        OPERANDS_1[0] = o0;
        return OPERANDS_1;
    }
    MSP430Operand[] fill_2(MSP430Operand o0, MSP430Operand o1){
        OPERANDS_2[0] = o0;
        OPERANDS_2[1] = o1;
        return OPERANDS_2;
    }
    
    /**
     * The <code>DTNode</code> class represents a node in a decoding graph.
     * Each node compares a range of bits and branches to other nodes based
     * on the value. Each node may also have an action (such as fixing the
     * addressing mode or instruction) that is executed when the node is
     * reached. Actions on the root node are not executed.
     */
    static abstract class DTNode {
        final int left_bit;
        final int mask;
        final Action action;
        DTNode(Action a, int lb, int msk) { action = a; left_bit = lb; mask = msk; }
        abstract DTNode move(int val);
    }
    
    /**
     * The <code>DTArrayNode</code> implementation is used for small (less
     * than 32) and dense (more than 50% full) edge lists. It uses an array
     * of indices that is directly indexed by the bits extracted from the
     * stream.
     */
    static class DTArrayNode extends DTNode {
        final DTNode[] nodes;
        DTArrayNode(Action a, int lb, int msk, DTNode[] n) {
            super(a, lb, msk);
            nodes = n;
        }
        DTNode move(int val) {
            return nodes[val];
        }
    }
    
    /**
     * The DTSortedNode implementation is used for sparse edge lists. It uses
     * a sorted array of indices and uses binary search on the value of the
     * bits.
     */
    static class DTSortedNode extends DTNode {
        final DTNode def;
        final DTNode[] nodes;
        final int[] values;
        DTSortedNode(Action a, int lb, int msk, int[] v, DTNode[] n, DTNode d) {
            super(a, lb, msk);
            values = v;
            nodes = n;
            def = d;
        }
        DTNode move(int val) {
            int ind = Arrays.binarySearch(values, val);
            if ( ind >= 0 && ind < values.length && values[ind] == val )
                return nodes[ind];
            else
                return def;
        }
    }
    
    /**
     * The <code>DTTerminal</code> class represents a terminal node in the
     * decoding tree. Terminal nodes are reached when decoding is finished,
     * and represent either successful decoding (meaning instruction and
     * addressing mode were discovered) or unsucessful decoding (meaning the
     * bit pattern does not encode a valid instruction.
     */
    static class DTTerminal extends DTNode {
        DTTerminal(Action a) {
            super(a, 0, 0);
        }
        DTNode move(int val) {
            return null;
        }
    }
    
    /**
     * The <code>ERROR</code> node is reached for incorrectly encoded
     * instructions and indicates that the bit pattern was an incorrectly
     * encoded instruction.
     */
    public static final DTTerminal ERROR = new DTTerminal(new ErrorAction());
    
    /**
     * The <code>Action</code> class represents an action that can happen
     * when the decoder reaches a particular node in the tree. The action may
     * be to fix the instruction or addressing mode, or to signal an error.
     */
    static abstract class Action {
        abstract void execute(MSP430Disassembler d) throws InvalidInstruction;
    }
    
    /**
     * The <code>ErrorAction</code> class is an action that is fired when the
     * decoding tree reaches a state which indicates the bit pattern is not a
     * valid instruction.
     */
    static class ErrorAction extends Action {
        void execute(MSP430Disassembler d) throws InvalidInstruction { throw new InvalidInstruction(0); }
    }
    
    /**
     * The <code>SetBuilder</code> class is an action that is fired when the
     * decoding tree reaches a node where the instruction is known. This
     * action fires and sets the <code>builder</code> field to point the
     * appropriate builder for the instruction.
     */
    static class SetBuilder extends Action {
        MSP430InstrBuilder.Single builder;
        SetBuilder(MSP430InstrBuilder.Single b) { builder = b; }
        void execute(MSP430Disassembler d) throws InvalidInstruction { d.builder = builder; }
    }
    
    /**
     * The <code>SetReader</code> class is an action that is fired when the
     * decoding tree reaches a node where the addressing mode is known. This
     * action fires and sets the <code>operands</code> field to point the
     * operands read from the instruction stream.
     */
    static class SetReader extends Action {
        OperandReader reader;
        SetReader(OperandReader r) { reader = r; }
        void execute(MSP430Disassembler d) throws InvalidInstruction { d.operands = reader.read(d); }
    }
    
    /**
     * The <code>OperandReader</code> class is an object that is capable of
     * reading the operands from the bit pattern of an instruction, once the
     * addressing mode is known. One of these classes is generated for each
     * addressing mode. When the addressing mode is finally known, an action
     * will fire that sets the operand reader which is used to read the
     * operands from the bit pattern.
     */
    static abstract class OperandReader {
        abstract MSP430Operand[] read(MSP430Disassembler d);
    }
    
    /**
     * The <code>builder</code> field stores a reference to the builder that
     * was discovered as a result of traversing the decoder tree. The builder
     * corresponds to one and only one instruction and has a method that can
     * build a new instance of the instruction from the operands.
     */
    private MSP430InstrBuilder.Single builder;
    
    /**
     * The <code>operands</code> field stores a reference to the operands
     * that were extracted from the bit pattern as a result of traversing the
     * decoding tree. When a node is reached where the addressing mode is
     * known, then the action on that node executes and reads the operands
     * from the bit pattern, storing them in this field.
     */
    private MSP430Operand[] operands;
    
    /**
     * The <code>word0</code> field stores a word-sized chunk of the
     * instruction stream. It is used by the decoders instead of repeatedly
     * accessing the array. This implementation has been configured with
     * 16-bit words.
     */
    private int word0;
    
    /**
     * The <code>word1</code> field stores a word-sized chunk of the
     * instruction stream. It is used by the decoders instead of repeatedly
     * accessing the array. This implementation has been configured with
     * 16-bit words.
     */
    private int word1;
    
    /**
     * The <code>word2</code> field stores a word-sized chunk of the
     * instruction stream. It is used by the decoders instead of repeatedly
     * accessing the array. This implementation has been configured with
     * 16-bit words.
     */
    private int word2;
    
    /**
     * The <code>make_instr0()</code> method creates a new instance of a
     * decoding tree by allocating the DTNode instances and connecting the
     * references together correctly. It is called only once in the static
     * initialization of the disassembler to build a single shared instance
     * of the decoder tree implementation and the reference to the root node
     * is stored in a single private static field of the same name.
     */
    static DTNode make_instr0() {
        DTNode T1 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.AND));
        DTNode T2 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.AND_B));
        DTNode N3 = new DTArrayNode(null, 8, 15, new DTNode[] {T2, T2, T2, T2, T1, T1, T1, T1, T2, T2, T2, T2, T1, T1, T1, T1});
        DTNode T4 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.MOV));
        DTNode T5 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.MOV_B));
        DTNode N6 = new DTArrayNode(null, 8, 15, new DTNode[] {T5, T5, T5, T5, T4, T4, T4, T4, T5, T5, T5, T5, T4, T4, T4, T4});
        DTNode T7 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SUB));
        DTNode T8 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SUB_B));
        DTNode N9 = new DTArrayNode(null, 8, 15, new DTNode[] {T8, T8, T8, T8, T7, T7, T7, T7, T8, T8, T8, T8, T7, T7, T7, T7});
        DTNode T10 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIT));
        DTNode T11 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIT_B));
        DTNode N12 = new DTArrayNode(null, 8, 15, new DTNode[] {T11, T11, T11, T11, T10, T10, T10, T10, T11, T11, T11, T11, T10, T10, T10, T10});
        DTNode T13 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JL));
        DTNode T14 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JGE));
        DTNode T15 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JMP));
        DTNode T16 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JN));
        DTNode N17 = new DTArrayNode(null, 4, 3, new DTNode[] {T16, T14, T13, T15});
        DTNode T18 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SUBC));
        DTNode T19 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SUBC_B));
        DTNode N20 = new DTArrayNode(null, 8, 15, new DTNode[] {T19, T19, T19, T19, T18, T18, T18, T18, T19, T19, T19, T19, T18, T18, T18, T18});
        DTNode T21 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIC));
        DTNode T22 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIC_B));
        DTNode N23 = new DTArrayNode(null, 8, 15, new DTNode[] {T22, T22, T22, T22, T21, T21, T21, T21, T22, T22, T22, T22, T21, T21, T21, T21});
        DTNode T24 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JNC));
        DTNode T25 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JEQ));
        DTNode T26 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JC));
        DTNode T27 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JNE));
        DTNode N28 = new DTArrayNode(null, 4, 3, new DTNode[] {T27, T25, T24, T26});
        DTNode T29 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIS));
        DTNode T30 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIS_B));
        DTNode N31 = new DTArrayNode(null, 8, 15, new DTNode[] {T30, T30, T30, T30, T29, T29, T29, T29, T30, T30, T30, T30, T29, T29, T29, T29});
        DTNode T32 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.CMP));
        DTNode T33 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.CMP_B));
        DTNode N34 = new DTArrayNode(null, 8, 15, new DTNode[] {T33, T33, T33, T33, T32, T32, T32, T32, T33, T33, T33, T33, T32, T32, T32, T32});
        DTNode T35 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADDC));
        DTNode T36 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADDC_B));
        DTNode N37 = new DTArrayNode(null, 8, 15, new DTNode[] {T36, T36, T36, T36, T35, T35, T35, T35, T36, T36, T36, T36, T35, T35, T35, T35});
        DTNode T38 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SWPB));
        DTNode T39 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.CALL));
        DTNode T40 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SXT));
        DTNode T41 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.RRA_B));
        DTNode T42 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.RRC_B));
        DTNode N43 = new DTSortedNode(null, 4, 255, new int[] {0, 1, 2, 3, 12, 13, 14, 15, 16, 17, 18, 19, 28, 29, 30, 31, 44, 45, 46, 47}, new DTNode[] {T42, T42, T42, T42, T38, T38, T38, T38, T41, T41, T41, T41, T40, T40, T40, T40, T39, T39, T39, T39}, ERROR);
        DTNode T44 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.XOR));
        DTNode T45 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.XOR_B));
        DTNode N46 = new DTArrayNode(null, 8, 15, new DTNode[] {T45, T45, T45, T45, T44, T44, T44, T44, T45, T45, T45, T45, T44, T44, T44, T44});
        DTNode T47 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.DADD));
        DTNode T48 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.DADD_B));
        DTNode N49 = new DTArrayNode(null, 8, 15, new DTNode[] {T48, T48, T48, T48, T47, T47, T47, T47, T48, T48, T48, T48, T47, T47, T47, T47});
        DTNode T50 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADD));
        DTNode T51 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADD_B));
        DTNode N52 = new DTArrayNode(null, 8, 15, new DTNode[] {T51, T51, T51, T51, T50, T50, T50, T50, T51, T51, T51, T51, T50, T50, T50, T50});
        DTNode T53 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.TST));
        DTNode T54 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.PUSH));
        DTNode T55 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADC_B));
        DTNode T56 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.TST_B));
        DTNode T57 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.RRA));
        DTNode T58 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.RRC));
        DTNode T59 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.PUSH_B));
        DTNode T60 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADC));
        DTNode N61 = new DTSortedNode(null, 4, 255, new int[] {48, 49, 50, 51, 52, 53, 54, 55, 72, 73, 74, 75, 76, 77, 78, 79, 132, 133, 134, 135, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151}, new DTNode[] {T55, T55, T55, T55, T60, T60, T60, T60, T56, T56, T56, T56, T53, T53, T53, T53, T58, T58, T58, T58, T57, T57, T57, T57, T59, T59, T59, T59, T54, T54, T54, T54}, ERROR);
        DTNode N0 = new DTArrayNode(null, 0, 15, new DTNode[] {N61, N43, N28, N17, N6, N52, N37, N20, N9, N34, N49, N12, N23, N31, N46, N3});
        return N0;
    }
    
    /**
     * The <code>make_addr0()</code> method creates a new instance of a
     * decoding tree by allocating the DTNode instances and connecting the
     * references together correctly. It is called only once in the static
     * initialization of the disassembler to build a single shared instance
     * of the decoder tree implementation and the reference to the root node
     * is stored in a single private static field of the same name.
     */
    static DTNode make_addr0() {
        DTNode T1 = new DTTerminal(new SetReader(new IMMABS_6_reader()));
        DTNode T2 = new DTTerminal(new SetReader(new IMMIND_6_reader()));
        DTNode T3 = new DTTerminal(new SetReader(new IMMSYM_6_reader()));
        DTNode N4 = new DTArrayNode(null, 12, 15, new DTNode[] {T3, T2, T1, T2, T2, T2, T2, T2, T2, T2, T2, T2, T2, T2, T2, T2});
        DTNode T5 = new DTTerminal(new SetReader(new AUTOABS_W_0_reader()));
        DTNode T6 = new DTTerminal(new SetReader(new AUTOIND_W_0_reader()));
        DTNode T7 = new DTTerminal(new SetReader(new AUTOSYM_W_0_reader()));
        DTNode N8 = new DTArrayNode(null, 12, 15, new DTNode[] {T7, T6, T5, T6, T6, T6, T6, T6, T6, T6, T6, T6, T6, T6, T6, T6});
        DTNode T9 = new DTTerminal(new SetReader(new IMMABS_1_reader()));
        DTNode T10 = new DTTerminal(new SetReader(new IMMIND_1_reader()));
        DTNode T11 = new DTTerminal(new SetReader(new IMMSYM_1_reader()));
        DTNode N12 = new DTArrayNode(null, 12, 15, new DTNode[] {T11, T10, T9, T10, T10, T10, T10, T10, T10, T10, T10, T10, T10, T10, T10, T10});
        DTNode T13 = new DTTerminal(new SetReader(new IMMABS_0_reader()));
        DTNode T14 = new DTTerminal(new SetReader(new IMMIND_0_reader()));
        DTNode T15 = new DTTerminal(new SetReader(new IMMSYM_0_reader()));
        DTNode N16 = new DTArrayNode(null, 12, 15, new DTNode[] {T15, T14, T13, T14, T14, T14, T14, T14, T14, T14, T14, T14, T14, T14, T14, T14});
        DTNode N17 = new DTArrayNode(null, 4, 15, new DTNode[] {N16, N8, N4, N12, N8, N8, N8, N8, N8, N8, N8, N8, N8, N8, N8, N8});
        DTNode T18 = new DTTerminal(new SetReader(new REGREG_0_reader()));
        DTNode T19 = new DTTerminal(new SetReader(new IMMREG_2_reader()));
        DTNode N20 = new DTArrayNode(null, 4, 15, new DTNode[] {T18, T18, T18, T19, T18, T18, T18, T18, T18, T18, T18, T18, T18, T18, T18, T18});
        DTNode T21 = new DTTerminal(new SetReader(new REGABS_0_reader()));
        DTNode T22 = new DTTerminal(new SetReader(new IMMABS_2_reader()));
        DTNode N23 = new DTArrayNode(null, 4, 15, new DTNode[] {T21, T21, T21, T22, T21, T21, T21, T21, T21, T21, T21, T21, T21, T21, T21, T21});
        DTNode T24 = new DTTerminal(new SetReader(new REGIND_0_reader()));
        DTNode T25 = new DTTerminal(new SetReader(new IMMIND_2_reader()));
        DTNode N26 = new DTArrayNode(null, 4, 15, new DTNode[] {T24, T24, T24, T25, T24, T24, T24, T24, T24, T24, T24, T24, T24, T24, T24, T24});
        DTNode T27 = new DTTerminal(new SetReader(new REGSYM_0_reader()));
        DTNode T28 = new DTTerminal(new SetReader(new IMMSYM_2_reader()));
        DTNode N29 = new DTArrayNode(null, 4, 15, new DTNode[] {T27, T27, T27, T28, T27, T27, T27, T27, T27, T27, T27, T27, T27, T27, T27, T27});
        DTNode N30 = new DTArrayNode(null, 12, 15, new DTNode[] {N29, N26, N23, N26, N26, N26, N26, N26, N26, N26, N26, N26, N26, N26, N26, N26});
        DTNode T31 = new DTTerminal(new SetReader(new AUTOABS_B_0_reader()));
        DTNode T32 = new DTTerminal(new SetReader(new AUTOIND_B_0_reader()));
        DTNode T33 = new DTTerminal(new SetReader(new AUTOSYM_B_0_reader()));
        DTNode N34 = new DTArrayNode(null, 12, 15, new DTNode[] {T33, T32, T31, T32, T32, T32, T32, T32, T32, T32, T32, T32, T32, T32, T32, T32});
        DTNode N35 = new DTArrayNode(null, 4, 15, new DTNode[] {N16, N34, N4, N12, N34, N34, N34, N34, N34, N34, N34, N34, N34, N34, N34, N34});
        DTNode T36 = new DTTerminal(new SetReader(new IMMREG_6_reader()));
        DTNode T37 = new DTTerminal(new SetReader(new AUTOREG_B_0_reader()));
        DTNode T38 = new DTTerminal(new SetReader(new IMMREG_1_reader()));
        DTNode T39 = new DTTerminal(new SetReader(new IMMREG_0_reader()));
        DTNode N40 = new DTArrayNode(null, 4, 15, new DTNode[] {T39, T37, T36, T38, T37, T37, T37, T37, T37, T37, T37, T37, T37, T37, T37, T37});
        DTNode T41 = new DTTerminal(new SetReader(new AUTOREG_W_0_reader()));
        DTNode N42 = new DTArrayNode(null, 4, 15, new DTNode[] {T39, T41, T36, T38, T41, T41, T41, T41, T41, T41, T41, T41, T41, T41, T41, T41});
        DTNode T43 = new DTTerminal(new SetReader(new IMMREG_5_reader()));
        DTNode T44 = new DTTerminal(new SetReader(new IREGREG_0_reader()));
        DTNode T45 = new DTTerminal(new SetReader(new IMMREG_4_reader()));
        DTNode N46 = new DTArrayNode(null, 4, 15, new DTNode[] {T44, T44, T43, T45, T44, T44, T44, T44, T44, T44, T44, T44, T44, T44, T44, T44});
        DTNode T47 = new DTTerminal(new SetReader(new ABSABS_0_reader()));
        DTNode T48 = new DTTerminal(new SetReader(new ABSIND_0_reader()));
        DTNode T49 = new DTTerminal(new SetReader(new ABSSYM_0_reader()));
        DTNode N50 = new DTArrayNode(null, 12, 15, new DTNode[] {T49, T48, T47, T48, T48, T48, T48, T48, T48, T48, T48, T48, T48, T48, T48, T48});
        DTNode T51 = new DTTerminal(new SetReader(new INDABS_0_reader()));
        DTNode T52 = new DTTerminal(new SetReader(new INDIND_0_reader()));
        DTNode T53 = new DTTerminal(new SetReader(new INDSYM_0_reader()));
        DTNode N54 = new DTArrayNode(null, 12, 15, new DTNode[] {T53, T52, T51, T52, T52, T52, T52, T52, T52, T52, T52, T52, T52, T52, T52, T52});
        DTNode T55 = new DTTerminal(new SetReader(new IMMABS_3_reader()));
        DTNode T56 = new DTTerminal(new SetReader(new IMMIND_3_reader()));
        DTNode T57 = new DTTerminal(new SetReader(new IMMSYM_3_reader()));
        DTNode N58 = new DTArrayNode(null, 12, 15, new DTNode[] {T57, T56, T55, T56, T56, T56, T56, T56, T56, T56, T56, T56, T56, T56, T56, T56});
        DTNode T59 = new DTTerminal(new SetReader(new SYMABS_0_reader()));
        DTNode T60 = new DTTerminal(new SetReader(new SYMIND_0_reader()));
        DTNode T61 = new DTTerminal(new SetReader(new SYMSYM_0_reader()));
        DTNode N62 = new DTArrayNode(null, 12, 15, new DTNode[] {T61, T60, T59, T60, T60, T60, T60, T60, T60, T60, T60, T60, T60, T60, T60, T60});
        DTNode N63 = new DTArrayNode(null, 4, 15, new DTNode[] {N62, N54, N50, N58, N54, N54, N54, N54, N54, N54, N54, N54, N54, N54, N54, N54});
        DTNode T64 = new DTTerminal(new SetReader(new ABSREG_0_reader()));
        DTNode T65 = new DTTerminal(new SetReader(new INDREG_0_reader()));
        DTNode T66 = new DTTerminal(new SetReader(new IMMREG_3_reader()));
        DTNode T67 = new DTTerminal(new SetReader(new SYMREG_0_reader()));
        DTNode N68 = new DTArrayNode(null, 4, 15, new DTNode[] {T67, T65, T64, T66, T65, T65, T65, T65, T65, T65, T65, T65, T65, T65, T65, T65});
        DTNode T69 = new DTTerminal(new SetReader(new IMMABS_5_reader()));
        DTNode T70 = new DTTerminal(new SetReader(new IMMIND_5_reader()));
        DTNode T71 = new DTTerminal(new SetReader(new IMMSYM_5_reader()));
        DTNode N72 = new DTArrayNode(null, 12, 15, new DTNode[] {T71, T70, T69, T70, T70, T70, T70, T70, T70, T70, T70, T70, T70, T70, T70, T70});
        DTNode T73 = new DTTerminal(new SetReader(new IREGABS_0_reader()));
        DTNode T74 = new DTTerminal(new SetReader(new IREGIND_0_reader()));
        DTNode T75 = new DTTerminal(new SetReader(new IREGSYM_0_reader()));
        DTNode N76 = new DTArrayNode(null, 12, 15, new DTNode[] {T75, T74, T73, T74, T74, T74, T74, T74, T74, T74, T74, T74, T74, T74, T74, T74});
        DTNode T77 = new DTTerminal(new SetReader(new IMMABS_4_reader()));
        DTNode T78 = new DTTerminal(new SetReader(new IMMIND_4_reader()));
        DTNode T79 = new DTTerminal(new SetReader(new IMMSYM_4_reader()));
        DTNode N80 = new DTArrayNode(null, 12, 15, new DTNode[] {T79, T78, T77, T78, T78, T78, T78, T78, T78, T78, T78, T78, T78, T78, T78, T78});
        DTNode N81 = new DTArrayNode(null, 4, 15, new DTNode[] {N76, N76, N72, N80, N76, N76, N76, N76, N76, N76, N76, N76, N76, N76, N76, N76});
        DTNode N82 = new DTArrayNode(null, 8, 15, new DTNode[] {N20, N68, N46, N40, N20, N68, N46, N42, N30, N63, N81, N35, N30, N63, N81, N17});
        DTNode T83 = new DTTerminal(null);
        DTNode N84 = new DTArrayNode(new SetReader(new JMP_0_reader()), 4, 3, new DTNode[] {T83, T83, T83, T83});
        DTNode T85 = new DTTerminal(new SetReader(new IMM_6_reader()));
        DTNode T86 = new DTTerminal(new SetReader(new AUTO_W_0_reader()));
        DTNode T87 = new DTTerminal(new SetReader(new IMM_1_reader()));
        DTNode T88 = new DTTerminal(new SetReader(new IMM_0_reader()));
        DTNode N89 = new DTArrayNode(null, 12, 15, new DTNode[] {T88, T86, T85, T87, T86, T86, T86, T86, T86, T86, T86, T86, T86, T86, T86, T86});
        DTNode T90 = new DTTerminal(new SetReader(new IMM_5_reader()));
        DTNode T91 = new DTTerminal(new SetReader(new IREG_0_reader()));
        DTNode T92 = new DTTerminal(new SetReader(new IMM_4_reader()));
        DTNode N93 = new DTArrayNode(null, 12, 15, new DTNode[] {T91, T91, T90, T92, T91, T91, T91, T91, T91, T91, T91, T91, T91, T91, T91, T91});
        DTNode T94 = new DTTerminal(new SetReader(new AUTO_B_0_reader()));
        DTNode N95 = new DTArrayNode(null, 12, 15, new DTNode[] {T88, T94, T85, T87, T94, T94, T94, T94, T94, T94, T94, T94, T94, T94, T94, T94});
        DTNode T96 = new DTTerminal(new SetReader(new REG_0_reader()));
        DTNode T97 = new DTTerminal(new SetReader(new IMM_2_reader()));
        DTNode N98 = new DTArrayNode(null, 12, 15, new DTNode[] {T96, T96, T96, T97, T96, T96, T96, T96, T96, T96, T96, T96, T96, T96, T96, T96});
        DTNode T99 = new DTTerminal(new SetReader(new ABS_0_reader()));
        DTNode T100 = new DTTerminal(new SetReader(new IND_0_reader()));
        DTNode T101 = new DTTerminal(new SetReader(new IMM_3_reader()));
        DTNode T102 = new DTTerminal(new SetReader(new SYM_0_reader()));
        DTNode N103 = new DTArrayNode(null, 12, 15, new DTNode[] {T102, T100, T99, T101, T100, T100, T100, T100, T100, T100, T100, T100, T100, T100, T100, T100});
        DTNode N104 = new DTSortedNode(null, 4, 255, new int[] {0, 1, 2, 3, 12, 13, 14, 15, 16, 17, 18, 19, 28, 29, 30, 31, 44, 45, 46, 47}, new DTNode[] {N98, N103, N93, N95, N98, N103, N93, N89, N98, N103, N93, N95, N98, N103, N93, N89, N98, N103, N93, N89}, ERROR);
        DTNode N105 = new DTSortedNode(null, 4, 255, new int[] {48, 49, 50, 51, 52, 53, 54, 55, 72, 73, 74, 75, 76, 77, 78, 79, 132, 133, 134, 135, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151}, new DTNode[] {N98, N103, N93, N95, N98, N103, N93, N89, N98, N103, N93, N95, N98, N103, N93, N89, N98, N103, N93, N89, N98, N103, N93, N89, N98, N103, N93, N95, N98, N103, N93, N89}, ERROR);
        DTNode N0 = new DTArrayNode(null, 0, 15, new DTNode[] {N105, N104, N84, N84, N82, N82, N82, N82, N82, N82, N82, N82, N82, N82, N82, N82});
        return N0;
    }
    
    /**
     * The <code>instr0</code> field stores a reference to the root of a
     * decoding tree. It is the starting point for decoding a bit pattern.
     */
    private static final DTNode instr0 = make_instr0();
    
    /**
     * The <code>addr0</code> field stores a reference to the root of a
     * decoding tree. It is the starting point for decoding a bit pattern.
     */
    private static final DTNode addr0 = make_addr0();
    
    /**
     * The <code>decode()</code> method is the main entrypoint to the
     * disassembler. Given an array of bytes or shorts and an index, the
     * disassembler will attempt to decode one instruction at that location.
     * If successful, the method will return a referece to a new
     * <code>MSP430Instr</code> object. 
     * @param base the base address of the array
     * @param index the index into the array where to begin decoding
     * @param code the actual code
     * @return an instance of the <code>MSP430Instr</code> class
     * corresponding to the instruction at this address if a valid
     * instruction exists here
     * @throws InvalidInstruction if the bit pattern at this address does not
     * correspond to a valid instruction
     */
    public MSP430Instr decode(int base, int index, byte[] code) throws InvalidInstruction {
        word0 = code[index + 1] << 8 | (code[index + 0] & 0xF);
        word1 = code[index + 3] << 8 | (code[index + 2] & 0xF);
        word2 = code[index + 5] << 8 | (code[index + 4] & 0xF);
        builder = null;
        operands = null;
        return decode_root();
    }
    
    /**
     * The <code>decode_root()</code> method begins decoding the bit pattern
     * into an instruction. This implementation is <i>parallel</i>, meaning
     * there are two trees: one for the instruction resolution and one of the
     * addressing mode resolution. By beginning at the root node of the
     * addressing mode and instruction resolution trees, the loop compares
     * bits in the bit patterns and moves down the two trees in parallel.
     * When both trees reach an endpoint, the comparison stops and an
     * instruction will be built. This method accepts the value of the first
     * word of the bits and begins decoding from there.
     */
    MSP430Instr decode_root() throws InvalidInstruction {
        DTNode addr = addr0;
        DTNode instr = instr0;
        while (true) {
            int bits = (word0 >> addr.left_bit) & addr.mask;
            addr = addr.move(bits);
            if ( instr != null ) instr = instr.move(bits);
            if ( addr == null ) break;
            if ( addr.action != null ) addr.action.execute(this);
            if ( instr != null && instr.action != null ) instr.action.execute(this);
        }
        if ( builder != null && operands != null ) return builder.build(operands);
        return null;
    }
}
