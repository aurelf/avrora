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
    public static class InvalidInstruction extends Exception {
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
    static int readop_0(MSP430Disassembler d) {
        int result = (d.word1 & 0xFFFF);
        return result;
    }
    static int readop_2(MSP430Disassembler d) {
        int result = (d.word0 & 0x000F);
        return result;
    }
    static int readop_4(MSP430Disassembler d) {
        int result = (d.word0 & 0x03FF);
        return result;
    }
    static int readop_1(MSP430Disassembler d) {
        int result = ((d.word0 >>> 8) & 0x000F);
        return result;
    }
    static int readop_3(MSP430Disassembler d) {
        int result = (d.word2 & 0xFFFF);
        return result;
    }
    static class IMM_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(readop_0(d));
            return new MSP430AddrMode.IMM(source);
        }
    }
    static class AUTOREG_B_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.AIREG_B source = new MSP430Operand.AIREG_B(SREG_table[readop_1(d)]);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.AUTOREG_B(source, dest);
        }
    }
    static class IREGSYM_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IREG source = new MSP430Operand.IREG(SREG_table[readop_1(d)]);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.IREGSYM(source, dest);
        }
    }
    static class ABSIND_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.ABSO source = new MSP430Operand.ABSO(readop_0(d));
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_3(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.ABSIND(source, dest);
        }
    }
    static class REGABS_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SREG source = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.REGABS(source, dest);
        }
    }
    static class IMMIND_5_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(4);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.IMMIND(source, dest);
        }
    }
    static class IMMREG_1_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(-1);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.IMMREG(source, dest);
        }
    }
    static class IMMIND_4_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(2);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.IMMIND(source, dest);
        }
    }
    static class IMM_6_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(8);
            return new MSP430AddrMode.IMM(source);
        }
    }
    static class REGSYM_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SREG source = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.REGSYM(source, dest);
        }
    }
    static class REGIND_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SREG source = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.REGIND(source, dest);
        }
    }
    static class IMMSYM_1_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(-1);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.IMMSYM(source, dest);
        }
    }
    static class INDREG_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SREG source_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM source_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX source = new MSP430Operand.INDX(source_reg, source_index);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.INDREG(source, dest);
        }
    }
    static class ABSABS_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.ABSO source = new MSP430Operand.ABSO(readop_0(d));
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_3(d));
            return new MSP430AddrMode.ABSABS(source, dest);
        }
    }
    static class IREGIND_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IREG source = new MSP430Operand.IREG(SREG_table[readop_1(d)]);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.IREGIND(source, dest);
        }
    }
    static class IMMREG_6_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(8);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.IMMREG(source, dest);
        }
    }
    static class IMM_3_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(1);
            return new MSP430AddrMode.IMM(source);
        }
    }
    static class IMMREG_4_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(2);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.IMMREG(source, dest);
        }
    }
    static class INDABS_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SREG source_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM source_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX source = new MSP430Operand.INDX(source_reg, source_index);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_3(d));
            return new MSP430AddrMode.INDABS(source, dest);
        }
    }
    static class IMMIND_2_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(0);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.IMMIND(source, dest);
        }
    }
    static class SYMREG_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SYM source = new MSP430Operand.SYM(readop_0(d));
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.SYMREG(source, dest);
        }
    }
    static class ABS_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.ABSO source = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.ABS(source);
        }
    }
    static class IMMREG_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.IMMREG(source, dest);
        }
    }
    static class IMMSYM_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_3(d));
            return new MSP430AddrMode.IMMSYM(source, dest);
        }
    }
    static class IREGREG_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IREG source = new MSP430Operand.IREG(SREG_table[readop_1(d)]);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.IREGREG(source, dest);
        }
    }
    static class INDIND_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SREG source_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM source_index = new MSP430Operand.IMM(readop_3(d));
            MSP430Operand.INDX source = new MSP430Operand.INDX(source_reg, source_index);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_3(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.INDIND(source, dest);
        }
    }
    static class REGREG_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SREG source = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.REGREG(source, dest);
        }
    }
    static class IMM_1_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(-1);
            return new MSP430AddrMode.IMM(source);
        }
    }
    static class AUTO_B_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.AIREG_B source = new MSP430Operand.AIREG_B(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.AUTO_B(source);
        }
    }
    static class ABSREG_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.ABSO source = new MSP430Operand.ABSO(readop_0(d));
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.ABSREG(source, dest);
        }
    }
    static class AUTOIND_W_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.AIREG_W source = new MSP430Operand.AIREG_W(SREG_table[readop_1(d)]);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.AUTOIND_W(source, dest);
        }
    }
    static class AUTOSYM_B_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.AIREG_B source = new MSP430Operand.AIREG_B(SREG_table[readop_1(d)]);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.AUTOSYM_B(source, dest);
        }
    }
    static class IND_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SREG source_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM source_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX source = new MSP430Operand.INDX(source_reg, source_index);
            return new MSP430AddrMode.IND(source);
        }
    }
    static class INDSYM_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SREG source_reg = new MSP430Operand.SREG(SREG_table[readop_1(d)]);
            MSP430Operand.IMM source_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX source = new MSP430Operand.INDX(source_reg, source_index);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_3(d));
            return new MSP430AddrMode.INDSYM(source, dest);
        }
    }
    static class IMMREG_3_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(1);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.IMMREG(source, dest);
        }
    }
    static class SYM_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SYM source = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.SYM(source);
        }
    }
    static class IREGABS_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IREG source = new MSP430Operand.IREG(SREG_table[readop_1(d)]);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.IREGABS(source, dest);
        }
    }
    static class IMMSYM_6_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(8);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.IMMSYM(source, dest);
        }
    }
    static class IMMABS_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_3(d));
            return new MSP430AddrMode.IMMABS(source, dest);
        }
    }
    static class SYMABS_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SYM source = new MSP430Operand.SYM(readop_0(d));
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_3(d));
            return new MSP430AddrMode.SYMABS(source, dest);
        }
    }
    static class SYMIND_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SYM source = new MSP430Operand.SYM(readop_0(d));
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_3(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.SYMIND(source, dest);
        }
    }
    static class IREG_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IREG source = new MSP430Operand.IREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.IREG(source);
        }
    }
    static class JMP_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.JUMP source = new MSP430Operand.JUMP(readop_4(d));
            return new MSP430AddrMode.JMP(source);
        }
    }
    static class IMMABS_3_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(1);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.IMMABS(source, dest);
        }
    }
    static class AUTO_W_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.AIREG_W source = new MSP430Operand.AIREG_W(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.AUTO_W(source);
        }
    }
    static class IMMABS_6_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(8);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.IMMABS(source, dest);
        }
    }
    static class SYMSYM_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SYM source = new MSP430Operand.SYM(readop_0(d));
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_3(d));
            return new MSP430AddrMode.SYMSYM(source, dest);
        }
    }
    static class IMMIND_1_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(-1);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.IMMIND(source, dest);
        }
    }
    static class AUTOIND_B_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.AIREG_B source = new MSP430Operand.AIREG_B(SREG_table[readop_1(d)]);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.AUTOIND_B(source, dest);
        }
    }
    static class IMMREG_2_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(0);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.IMMREG(source, dest);
        }
    }
    static class IMMREG_5_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(4);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.IMMREG(source, dest);
        }
    }
    static class IMMABS_5_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(4);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.IMMABS(source, dest);
        }
    }
    static class IMMIND_3_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(1);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.IMMIND(source, dest);
        }
    }
    static class IMM_4_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(2);
            return new MSP430AddrMode.IMM(source);
        }
    }
    static class IMMSYM_3_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(1);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.IMMSYM(source, dest);
        }
    }
    static class IMMABS_1_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(-1);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.IMMABS(source, dest);
        }
    }
    static class REG_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.SREG source = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.REG(source);
        }
    }
    static class IMMABS_2_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(0);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.IMMABS(source, dest);
        }
    }
    static class IMM_2_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(0);
            return new MSP430AddrMode.IMM(source);
        }
    }
    static class AUTOREG_W_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.AIREG_W source = new MSP430Operand.AIREG_W(SREG_table[readop_1(d)]);
            MSP430Operand.SREG dest = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            return new MSP430AddrMode.AUTOREG_W(source, dest);
        }
    }
    static class IMMSYM_2_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(0);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.IMMSYM(source, dest);
        }
    }
    static class IMM_5_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(4);
            return new MSP430AddrMode.IMM(source);
        }
    }
    static class AUTOSYM_W_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.AIREG_W source = new MSP430Operand.AIREG_W(SREG_table[readop_1(d)]);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.AUTOSYM_W(source, dest);
        }
    }
    static class IMMSYM_5_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(4);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.IMMSYM(source, dest);
        }
    }
    static class IMMABS_4_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(2);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.IMMABS(source, dest);
        }
    }
    static class AUTOABS_W_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.AIREG_W source = new MSP430Operand.AIREG_W(SREG_table[readop_1(d)]);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.AUTOABS_W(source, dest);
        }
    }
    static class IMMIND_6_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(8);
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.IMMIND(source, dest);
        }
    }
    static class IMMSYM_4_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(2);
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_0(d));
            return new MSP430AddrMode.IMMSYM(source, dest);
        }
    }
    static class IMMIND_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.IMM source = new MSP430Operand.IMM(readop_0(d));
            MSP430Operand.SREG dest_reg = new MSP430Operand.SREG(SREG_table[readop_2(d)]);
            MSP430Operand.IMM dest_index = new MSP430Operand.IMM(readop_3(d));
            MSP430Operand.INDX dest = new MSP430Operand.INDX(dest_reg, dest_index);
            return new MSP430AddrMode.IMMIND(source, dest);
        }
    }
    static class AUTOABS_B_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.AIREG_B source = new MSP430Operand.AIREG_B(SREG_table[readop_1(d)]);
            MSP430Operand.ABSO dest = new MSP430Operand.ABSO(readop_0(d));
            return new MSP430AddrMode.AUTOABS_B(source, dest);
        }
    }
    static class ABSSYM_0_reader extends OperandReader {
        MSP430AddrMode read(MSP430Disassembler d) {
            MSP430Operand.ABSO source = new MSP430Operand.ABSO(readop_0(d));
            MSP430Operand.SYM dest = new MSP430Operand.SYM(readop_3(d));
            return new MSP430AddrMode.ABSSYM(source, dest);
        }
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
        abstract DTNode move(MSP430Disassembler d, int val);
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
        DTNode move(MSP430Disassembler d, int val) {
            if ( action != null ) action.execute(d);
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
        DTNode move(MSP430Disassembler d, int val) {
            if ( action != null ) action.execute(d);
            int ind = Arrays.binarySearch(values, val);
            if ( ind >= 0 && ind < values.length && values[ind] == val )
                return nodes[ind];
            else
                return def;
        }
    }
    
    /**
     * The <code>DTErrorTerm</code> class is a node that terminates the
     * exploration of the instruction decoder with failure.
     */
    static class DTErrorTerm extends DTNode {
        DTErrorTerm() { super(null, 0, 0); }
        DTNode move(MSP430Disassembler d, int bits) {
            d.state = ERR;
            return this;
        }
    }
    
    /**
     * The <code>ERROR</code> node is reached for incorrectly encoded
     * instructions and indicates that the bit pattern was an incorrectly
     * encoded instruction.
     */
    public static final DTErrorTerm ERROR = new DTErrorTerm();
    
    /**
     * The <code>Action</code> class represents an action that can happen
     * when the decoder reaches a particular node in the tree. The action may
     * be to fix the instruction or addressing mode, or to signal an error.
     */
    static abstract class Action {
        abstract void execute(MSP430Disassembler d);
    }
    
    /**
     * The <code>ErrorAction</code> class is an action that is fired when the
     * decoding tree reaches a state which indicates the bit pattern is not a
     * valid instruction.
     */
    static class ErrorAction extends Action {
        void execute(MSP430Disassembler d) { d.state = ERR; }
    }
    
    /**
     * The <code>SetBuilder</code> class is an action that is fired when the
     * decoding tree reaches a node where the instruction is known. This
     * action fires and sets the <code>builder</code> field to point the
     * appropriate builder for the instruction.
     */
    static class SetBuilder extends Action {
        MSP430InstrBuilder builder;
        SetBuilder(MSP430InstrBuilder b) { builder = b; }
        void execute(MSP430Disassembler d) { d.builder = builder; }
    }
    
    /**
     * The <code>SetReader</code> class is an action that is fired when the
     * decoding tree reaches a node where the addressing mode is known. This
     * action fires and sets the <code>addrMode</code> field to point the
     * operands read from the instruction stream.
     */
    static class SetReader extends Action {
        OperandReader reader;
        SetReader(OperandReader r) { reader = r; }
        void execute(MSP430Disassembler d) { d.addrMode = reader.read(d); }
    }
    
    /**
     * The <code>DTLoop</code> class is a node that terminates the
     * exploration of the decoder when both the instruction and addressing
     * mode decoders have reached the this state.
     */
    static class DTLoop extends DTNode {
        DTLoop() { super(null, 0, 0); }
        DTNode move(MSP430Disassembler d, int bits) {
            if ( d.terminated >= 2 ) d.state = OK;
            return this;
        }
    }
    
    /**
     * The <code>DTTerminal</code> class is a node that terminates the
     * exploration of the instruction decoder.
     */
    static class DTTerminal extends DTNode {
        DTTerminal(Action a) { super(a, 0, 0); }
        DTNode move(MSP430Disassembler d, int bits) {
            d.terminated++;
            if ( action != null ) action.execute(d);
            return LOOP;
        }
    }
    
    /**
     * The <code>LOOP</code> node is reached when either of the decoder trees
     * reaches a terminal node. This node essentially waits for both trees to
     * reach either an OK state  or an ERR state.
     */
    public static final DTLoop LOOP = new DTLoop();
    
    /**
     * The <code>OperandReader</code> class is an object that is capable of
     * reading the operands from the bit pattern of an instruction, once the
     * addressing mode is known. One of these classes is generated for each
     * addressing mode. When the addressing mode is finally known, an action
     * will fire that sets the operand reader which is used to read the
     * operands from the bit pattern.
     */
    static abstract class OperandReader {
        abstract MSP430AddrMode read(MSP430Disassembler d);
    }
    
    /**
     * The <code>builder</code> field stores a reference to the builder that
     * was discovered as a result of traversing the decoder tree. The builder
     * corresponds to one and only one instruction and has a method that can
     * build a new instance of the instruction from the operands.
     */
    private MSP430InstrBuilder builder;
    
    /**
     * The <code>addrMode</code> field stores a reference to the operands
     * that were extracted from the bit pattern as a result of traversing the
     * decoding tree. When a node is reached where the addressing mode is
     * known, then the action on that node executes and reads the operands
     * from the bit pattern, storing them in this field.
     */
    private MSP430AddrMode addrMode;
    
    /**
     * The <code>state</code> field controls the execution of the main
     * decoder loop. When the decoder begins execution, the state field is
     * set to <code>MOVE</code>. The decoder continues until an action fires
     * or a terminal node is reached that sets this field to either
     * <code>OK</code> or <code>ERROR</code>.
     */
    private int state;
    private static final int MOVE = 0;
    private static final int OK = 1;
    private static final int ERR = -1;
    
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
    int terminated;
    
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
        DTNode N4 = new DTArrayNode(null, 0, 15, new DTNode[] {T3, T2, T1, T2, T2, T2, T2, T2, T2, T2, T2, T2, T2, T2, T2, T2});
        DTNode T5 = new DTTerminal(new SetReader(new AUTOABS_B_0_reader()));
        DTNode T6 = new DTTerminal(new SetReader(new AUTOIND_B_0_reader()));
        DTNode T7 = new DTTerminal(new SetReader(new AUTOSYM_B_0_reader()));
        DTNode N8 = new DTArrayNode(null, 0, 15, new DTNode[] {T7, T6, T5, T6, T6, T6, T6, T6, T6, T6, T6, T6, T6, T6, T6, T6});
        DTNode T9 = new DTTerminal(new SetReader(new IMMABS_1_reader()));
        DTNode T10 = new DTTerminal(new SetReader(new IMMIND_1_reader()));
        DTNode T11 = new DTTerminal(new SetReader(new IMMSYM_1_reader()));
        DTNode N12 = new DTArrayNode(null, 0, 15, new DTNode[] {T11, T10, T9, T10, T10, T10, T10, T10, T10, T10, T10, T10, T10, T10, T10, T10});
        DTNode T13 = new DTTerminal(new SetReader(new IMMABS_0_reader()));
        DTNode T14 = new DTTerminal(new SetReader(new IMMIND_0_reader()));
        DTNode T15 = new DTTerminal(new SetReader(new IMMSYM_0_reader()));
        DTNode N16 = new DTArrayNode(null, 0, 15, new DTNode[] {T15, T14, T13, T14, T14, T14, T14, T14, T14, T14, T14, T14, T14, T14, T14, T14});
        DTNode N17 = new DTArrayNode(null, 8, 15, new DTNode[] {N16, N8, N4, N12, N8, N8, N8, N8, N8, N8, N8, N8, N8, N8, N8, N8});
        DTNode T18 = new DTTerminal(new SetReader(new REGREG_0_reader()));
        DTNode T19 = new DTTerminal(new SetReader(new IMMREG_2_reader()));
        DTNode N20 = new DTArrayNode(null, 8, 15, new DTNode[] {T18, T18, T18, T19, T18, T18, T18, T18, T18, T18, T18, T18, T18, T18, T18, T18});
        DTNode T21 = new DTTerminal(new SetReader(new REGABS_0_reader()));
        DTNode T22 = new DTTerminal(new SetReader(new IMMABS_2_reader()));
        DTNode N23 = new DTArrayNode(null, 8, 15, new DTNode[] {T21, T21, T21, T22, T21, T21, T21, T21, T21, T21, T21, T21, T21, T21, T21, T21});
        DTNode T24 = new DTTerminal(new SetReader(new REGIND_0_reader()));
        DTNode T25 = new DTTerminal(new SetReader(new IMMIND_2_reader()));
        DTNode N26 = new DTArrayNode(null, 8, 15, new DTNode[] {T24, T24, T24, T25, T24, T24, T24, T24, T24, T24, T24, T24, T24, T24, T24, T24});
        DTNode T27 = new DTTerminal(new SetReader(new REGSYM_0_reader()));
        DTNode T28 = new DTTerminal(new SetReader(new IMMSYM_2_reader()));
        DTNode N29 = new DTArrayNode(null, 8, 15, new DTNode[] {T27, T27, T27, T28, T27, T27, T27, T27, T27, T27, T27, T27, T27, T27, T27, T27});
        DTNode N30 = new DTArrayNode(null, 0, 15, new DTNode[] {N29, N26, N23, N26, N26, N26, N26, N26, N26, N26, N26, N26, N26, N26, N26, N26});
        DTNode T31 = new DTTerminal(new SetReader(new AUTOABS_W_0_reader()));
        DTNode T32 = new DTTerminal(new SetReader(new AUTOIND_W_0_reader()));
        DTNode T33 = new DTTerminal(new SetReader(new AUTOSYM_W_0_reader()));
        DTNode N34 = new DTArrayNode(null, 0, 15, new DTNode[] {T33, T32, T31, T32, T32, T32, T32, T32, T32, T32, T32, T32, T32, T32, T32, T32});
        DTNode N35 = new DTArrayNode(null, 8, 15, new DTNode[] {N16, N34, N4, N12, N34, N34, N34, N34, N34, N34, N34, N34, N34, N34, N34, N34});
        DTNode T36 = new DTTerminal(new SetReader(new IMMREG_6_reader()));
        DTNode T37 = new DTTerminal(new SetReader(new AUTOREG_W_0_reader()));
        DTNode T38 = new DTTerminal(new SetReader(new IMMREG_1_reader()));
        DTNode T39 = new DTTerminal(new SetReader(new IMMREG_0_reader()));
        DTNode N40 = new DTArrayNode(null, 8, 15, new DTNode[] {T39, T37, T36, T38, T37, T37, T37, T37, T37, T37, T37, T37, T37, T37, T37, T37});
        DTNode T41 = new DTTerminal(new SetReader(new AUTOREG_B_0_reader()));
        DTNode N42 = new DTArrayNode(null, 8, 15, new DTNode[] {T39, T41, T36, T38, T41, T41, T41, T41, T41, T41, T41, T41, T41, T41, T41, T41});
        DTNode T43 = new DTTerminal(new SetReader(new IMMREG_5_reader()));
        DTNode T44 = new DTTerminal(new SetReader(new IREGREG_0_reader()));
        DTNode T45 = new DTTerminal(new SetReader(new IMMREG_4_reader()));
        DTNode N46 = new DTArrayNode(null, 8, 15, new DTNode[] {T44, T44, T43, T45, T44, T44, T44, T44, T44, T44, T44, T44, T44, T44, T44, T44});
        DTNode T47 = new DTTerminal(new SetReader(new ABSABS_0_reader()));
        DTNode T48 = new DTTerminal(new SetReader(new ABSIND_0_reader()));
        DTNode T49 = new DTTerminal(new SetReader(new ABSSYM_0_reader()));
        DTNode N50 = new DTArrayNode(null, 0, 15, new DTNode[] {T49, T48, T47, T48, T48, T48, T48, T48, T48, T48, T48, T48, T48, T48, T48, T48});
        DTNode T51 = new DTTerminal(new SetReader(new INDABS_0_reader()));
        DTNode T52 = new DTTerminal(new SetReader(new INDIND_0_reader()));
        DTNode T53 = new DTTerminal(new SetReader(new INDSYM_0_reader()));
        DTNode N54 = new DTArrayNode(null, 0, 15, new DTNode[] {T53, T52, T51, T52, T52, T52, T52, T52, T52, T52, T52, T52, T52, T52, T52, T52});
        DTNode T55 = new DTTerminal(new SetReader(new IMMABS_3_reader()));
        DTNode T56 = new DTTerminal(new SetReader(new IMMIND_3_reader()));
        DTNode T57 = new DTTerminal(new SetReader(new IMMSYM_3_reader()));
        DTNode N58 = new DTArrayNode(null, 0, 15, new DTNode[] {T57, T56, T55, T56, T56, T56, T56, T56, T56, T56, T56, T56, T56, T56, T56, T56});
        DTNode T59 = new DTTerminal(new SetReader(new SYMABS_0_reader()));
        DTNode T60 = new DTTerminal(new SetReader(new SYMIND_0_reader()));
        DTNode T61 = new DTTerminal(new SetReader(new SYMSYM_0_reader()));
        DTNode N62 = new DTArrayNode(null, 0, 15, new DTNode[] {T61, T60, T59, T60, T60, T60, T60, T60, T60, T60, T60, T60, T60, T60, T60, T60});
        DTNode N63 = new DTArrayNode(null, 8, 15, new DTNode[] {N62, N54, N50, N58, N54, N54, N54, N54, N54, N54, N54, N54, N54, N54, N54, N54});
        DTNode T64 = new DTTerminal(new SetReader(new ABSREG_0_reader()));
        DTNode T65 = new DTTerminal(new SetReader(new INDREG_0_reader()));
        DTNode T66 = new DTTerminal(new SetReader(new IMMREG_3_reader()));
        DTNode T67 = new DTTerminal(new SetReader(new SYMREG_0_reader()));
        DTNode N68 = new DTArrayNode(null, 8, 15, new DTNode[] {T67, T65, T64, T66, T65, T65, T65, T65, T65, T65, T65, T65, T65, T65, T65, T65});
        DTNode T69 = new DTTerminal(new SetReader(new IMMABS_5_reader()));
        DTNode T70 = new DTTerminal(new SetReader(new IMMIND_5_reader()));
        DTNode T71 = new DTTerminal(new SetReader(new IMMSYM_5_reader()));
        DTNode N72 = new DTArrayNode(null, 0, 15, new DTNode[] {T71, T70, T69, T70, T70, T70, T70, T70, T70, T70, T70, T70, T70, T70, T70, T70});
        DTNode T73 = new DTTerminal(new SetReader(new IREGABS_0_reader()));
        DTNode T74 = new DTTerminal(new SetReader(new IREGIND_0_reader()));
        DTNode T75 = new DTTerminal(new SetReader(new IREGSYM_0_reader()));
        DTNode N76 = new DTArrayNode(null, 0, 15, new DTNode[] {T75, T74, T73, T74, T74, T74, T74, T74, T74, T74, T74, T74, T74, T74, T74, T74});
        DTNode T77 = new DTTerminal(new SetReader(new IMMABS_4_reader()));
        DTNode T78 = new DTTerminal(new SetReader(new IMMIND_4_reader()));
        DTNode T79 = new DTTerminal(new SetReader(new IMMSYM_4_reader()));
        DTNode N80 = new DTArrayNode(null, 0, 15, new DTNode[] {T79, T78, T77, T78, T78, T78, T78, T78, T78, T78, T78, T78, T78, T78, T78, T78});
        DTNode N81 = new DTArrayNode(null, 8, 15, new DTNode[] {N76, N76, N72, N80, N76, N76, N76, N76, N76, N76, N76, N76, N76, N76, N76, N76});
        DTNode N82 = new DTArrayNode(null, 4, 15, new DTNode[] {N20, N68, N46, N40, N20, N68, N46, N42, N30, N63, N81, N35, N30, N63, N81, N17});
        DTNode T83 = new DTTerminal(null);
        DTNode N84 = new DTArrayNode(new SetReader(new JMP_0_reader()), 10, 3, new DTNode[] {T83, T83, T83, T83});
        DTNode T85 = new DTTerminal(new SetReader(new REG_0_reader()));
        DTNode T86 = new DTTerminal(new SetReader(new IMM_2_reader()));
        DTNode N87 = new DTArrayNode(null, 0, 15, new DTNode[] {T85, T85, T85, T86, T85, T85, T85, T85, T85, T85, T85, T85, T85, T85, T85, T85});
        DTNode T88 = new DTTerminal(new SetReader(new IMM_5_reader()));
        DTNode T89 = new DTTerminal(new SetReader(new IREG_0_reader()));
        DTNode T90 = new DTTerminal(new SetReader(new IMM_4_reader()));
        DTNode N91 = new DTArrayNode(null, 0, 15, new DTNode[] {T89, T89, T88, T90, T89, T89, T89, T89, T89, T89, T89, T89, T89, T89, T89, T89});
        DTNode T92 = new DTTerminal(new SetReader(new IMM_6_reader()));
        DTNode T93 = new DTTerminal(new SetReader(new AUTO_B_0_reader()));
        DTNode T94 = new DTTerminal(new SetReader(new IMM_1_reader()));
        DTNode T95 = new DTTerminal(new SetReader(new IMM_0_reader()));
        DTNode N96 = new DTArrayNode(null, 0, 15, new DTNode[] {T95, T93, T92, T94, T93, T93, T93, T93, T93, T93, T93, T93, T93, T93, T93, T93});
        DTNode T97 = new DTTerminal(new SetReader(new AUTO_W_0_reader()));
        DTNode N98 = new DTArrayNode(null, 0, 15, new DTNode[] {T95, T97, T92, T94, T97, T97, T97, T97, T97, T97, T97, T97, T97, T97, T97, T97});
        DTNode T99 = new DTTerminal(new SetReader(new ABS_0_reader()));
        DTNode T100 = new DTTerminal(new SetReader(new IND_0_reader()));
        DTNode T101 = new DTTerminal(new SetReader(new IMM_3_reader()));
        DTNode T102 = new DTTerminal(new SetReader(new SYM_0_reader()));
        DTNode N103 = new DTArrayNode(null, 0, 15, new DTNode[] {T102, T100, T99, T101, T100, T100, T100, T100, T100, T100, T100, T100, T100, T100, T100, T100});
        DTNode N104 = new DTSortedNode(null, 4, 255, new int[] {4, 5, 6, 7, 8, 9, 10, 11, 20, 21, 22, 23, 24, 25, 26, 27, 40, 41, 42, 43}, new DTNode[] {N87, N103, N91, N96, N87, N103, N91, N98, N87, N103, N91, N96, N87, N103, N91, N98, N87, N103, N91, N98}, ERROR);
        DTNode N105 = new DTSortedNode(null, 4, 255, new int[] {48, 49, 50, 51, 52, 53, 54, 55, 72, 73, 74, 75, 76, 77, 78, 79, 128, 129, 130, 131, 136, 137, 138, 139, 144, 145, 146, 147, 148, 149, 150, 151}, new DTNode[] {N87, N103, N91, N98, N87, N103, N91, N96, N87, N103, N91, N98, N87, N103, N91, N96, N87, N103, N91, N98, N87, N103, N91, N98, N87, N103, N91, N98, N87, N103, N91, N96}, ERROR);
        DTNode N0 = new DTArrayNode(null, 12, 15, new DTNode[] {N105, N104, N84, N84, N82, N82, N82, N82, N82, N82, N82, N82, N82, N82, N82, N82});
        return N0;
    }
    
    /**
     * The <code>addr0</code> field stores a reference to the root of a
     * decoding tree. It is the starting point for decoding a bit pattern.
     */
    private static final DTNode addr0 = make_addr0();
    
    /**
     * The <code>make_instr0()</code> method creates a new instance of a
     * decoding tree by allocating the DTNode instances and connecting the
     * references together correctly. It is called only once in the static
     * initialization of the disassembler to build a single shared instance
     * of the decoder tree implementation and the reference to the root node
     * is stored in a single private static field of the same name.
     */
    static DTNode make_instr0() {
        DTNode T1 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.AND_B));
        DTNode T2 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.AND));
        DTNode N3 = new DTArrayNode(null, 4, 15, new DTNode[] {T2, T2, T2, T2, T1, T1, T1, T1, T2, T2, T2, T2, T1, T1, T1, T1});
        DTNode T4 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.MOV_B));
        DTNode T5 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.MOV));
        DTNode N6 = new DTArrayNode(null, 4, 15, new DTNode[] {T5, T5, T5, T5, T4, T4, T4, T4, T5, T5, T5, T5, T4, T4, T4, T4});
        DTNode T7 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SUB_B));
        DTNode T8 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SUB));
        DTNode N9 = new DTArrayNode(null, 4, 15, new DTNode[] {T8, T8, T8, T8, T7, T7, T7, T7, T8, T8, T8, T8, T7, T7, T7, T7});
        DTNode T10 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIT_B));
        DTNode T11 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIT));
        DTNode N12 = new DTArrayNode(null, 4, 15, new DTNode[] {T11, T11, T11, T11, T10, T10, T10, T10, T11, T11, T11, T11, T10, T10, T10, T10});
        DTNode T13 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JL));
        DTNode T14 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JGE));
        DTNode T15 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JMP));
        DTNode T16 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JN));
        DTNode N17 = new DTArrayNode(null, 10, 3, new DTNode[] {T16, T14, T13, T15});
        DTNode T18 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SUBC_B));
        DTNode T19 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SUBC));
        DTNode N20 = new DTArrayNode(null, 4, 15, new DTNode[] {T19, T19, T19, T19, T18, T18, T18, T18, T19, T19, T19, T19, T18, T18, T18, T18});
        DTNode T21 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIC_B));
        DTNode T22 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIC));
        DTNode N23 = new DTArrayNode(null, 4, 15, new DTNode[] {T22, T22, T22, T22, T21, T21, T21, T21, T22, T22, T22, T22, T21, T21, T21, T21});
        DTNode T24 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JNC));
        DTNode T25 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JEQ));
        DTNode T26 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JC));
        DTNode T27 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.JNE));
        DTNode N28 = new DTArrayNode(null, 10, 3, new DTNode[] {T27, T25, T24, T26});
        DTNode T29 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIS_B));
        DTNode T30 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.BIS));
        DTNode N31 = new DTArrayNode(null, 4, 15, new DTNode[] {T30, T30, T30, T30, T29, T29, T29, T29, T30, T30, T30, T30, T29, T29, T29, T29});
        DTNode T32 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.CMP_B));
        DTNode T33 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.CMP));
        DTNode N34 = new DTArrayNode(null, 4, 15, new DTNode[] {T33, T33, T33, T33, T32, T32, T32, T32, T33, T33, T33, T33, T32, T32, T32, T32});
        DTNode T35 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADDC_B));
        DTNode T36 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADDC));
        DTNode N37 = new DTArrayNode(null, 4, 15, new DTNode[] {T36, T36, T36, T36, T35, T35, T35, T35, T36, T36, T36, T36, T35, T35, T35, T35});
        DTNode T38 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.RRC_B));
        DTNode T39 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SWPB));
        DTNode T40 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.SXT));
        DTNode T41 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.RRA_B));
        DTNode T42 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.CALL));
        DTNode N43 = new DTSortedNode(null, 4, 255, new int[] {4, 5, 6, 7, 8, 9, 10, 11, 20, 21, 22, 23, 24, 25, 26, 27, 40, 41, 42, 43}, new DTNode[] {T38, T38, T38, T38, T39, T39, T39, T39, T41, T41, T41, T41, T40, T40, T40, T40, T42, T42, T42, T42}, ERROR);
        DTNode T44 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.XOR_B));
        DTNode T45 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.XOR));
        DTNode N46 = new DTArrayNode(null, 4, 15, new DTNode[] {T45, T45, T45, T45, T44, T44, T44, T44, T45, T45, T45, T45, T44, T44, T44, T44});
        DTNode T47 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.DADD_B));
        DTNode T48 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.DADD));
        DTNode N49 = new DTArrayNode(null, 4, 15, new DTNode[] {T48, T48, T48, T48, T47, T47, T47, T47, T48, T48, T48, T48, T47, T47, T47, T47});
        DTNode T50 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADD_B));
        DTNode T51 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADD));
        DTNode N52 = new DTArrayNode(null, 4, 15, new DTNode[] {T51, T51, T51, T51, T50, T50, T50, T50, T51, T51, T51, T51, T50, T50, T50, T50});
        DTNode T53 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.RRA));
        DTNode T54 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.TST_B));
        DTNode T55 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.PUSH_B));
        DTNode T56 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADC));
        DTNode T57 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.TST));
        DTNode T58 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.ADC_B));
        DTNode T59 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.PUSH));
        DTNode T60 = new DTTerminal(new SetBuilder(MSP430InstrBuilder.RRC));
        DTNode N61 = new DTSortedNode(null, 4, 255, new int[] {48, 49, 50, 51, 52, 53, 54, 55, 72, 73, 74, 75, 76, 77, 78, 79, 128, 129, 130, 131, 136, 137, 138, 139, 144, 145, 146, 147, 148, 149, 150, 151}, new DTNode[] {T56, T56, T56, T56, T58, T58, T58, T58, T57, T57, T57, T57, T54, T54, T54, T54, T60, T60, T60, T60, T53, T53, T53, T53, T59, T59, T59, T59, T55, T55, T55, T55}, ERROR);
        DTNode N0 = new DTArrayNode(null, 12, 15, new DTNode[] {N61, N43, N28, N17, N6, N52, N37, N20, N9, N34, N49, N12, N23, N31, N46, N3});
        return N0;
    }
    
    /**
     * The <code>instr0</code> field stores a reference to the root of a
     * decoding tree. It is the starting point for decoding a bit pattern.
     */
    private static final DTNode instr0 = make_instr0();
    
    /**
     * The <code>decode()</code> method is the main entrypoint to the
     * disassembler. Given an array of type <code>byte[]</code>, a base
     * address, and an index, the disassembler will attempt to decode one
     * instruction at that location. If successful, the method will return a
     * reference to a new <code>MSP430Instr</code> object. 
     * @param base the base address of the array
     * @param index the index into the array where to begin decoding
     * @param code the actual code
     * @return an instance of the <code>MSP430Instr</code> class
     * corresponding to the instruction at this address if a valid
     * instruction exists here; null otherwise
     */
    public MSP430Instr decode(int base, int index, byte[] code) {
        word0 = ((code[index + 0] & 0xFF) << 0) | ((code[index + 1] & 0xFF) << 8);
        word1 = ((code[index + 2] & 0xFF) << 0) | ((code[index + 3] & 0xFF) << 8);
        word2 = ((code[index + 4] & 0xFF) << 0) | ((code[index + 5] & 0xFF) << 8);
        return decode_root();
    }
    
    /**
     * The <code>decode()</code> method is the main entrypoint to the
     * disassembler. Given an array of type <code>char[]</code>, a base
     * address, and an index, the disassembler will attempt to decode one
     * instruction at that location. If successful, the method will return a
     * reference to a new <code>MSP430Instr</code> object. 
     * @param base the base address of the array
     * @param index the index into the array where to begin decoding
     * @param code the actual code
     * @return an instance of the <code>MSP430Instr</code> class
     * corresponding to the instruction at this address if a valid
     * instruction exists here; null otherwise
     */
    public MSP430Instr decode(int base, int index, char[] code) {
        word0 = (code[index + 0] << 0);
        word1 = (code[index + 1] << 0);
        word2 = (code[index + 2] << 0);
        return decode_root();
    }
    
    /**
     * The <code>decode()</code> method is the main entrypoint to the
     * disassembler. Given an array of type <code>short[]</code>, a base
     * address, and an index, the disassembler will attempt to decode one
     * instruction at that location. If successful, the method will return a
     * reference to a new <code>MSP430Instr</code> object. 
     * @param base the base address of the array
     * @param index the index into the array where to begin decoding
     * @param code the actual code
     * @return an instance of the <code>MSP430Instr</code> class
     * corresponding to the instruction at this address if a valid
     * instruction exists here; null otherwise
     */
    public MSP430Instr decode(int base, int index, short[] code) {
        word0 = ((code[index + 0] & 0xFFFF) << 0);
        word1 = ((code[index + 1] & 0xFFFF) << 0);
        word2 = ((code[index + 2] & 0xFFFF) << 0);
        return decode_root();
    }
    
    /**
     * The <code>decoder_root()</code> method begins decoding the bit pattern
     * into an instruction.
     */
    MSP430Instr decode_root() {
        builder = null;
        addrMode = null;
        return run_decoder(addr0, instr0);
    }
    
    /**
     * The <code>run_decoder()</code> method begins decoding the bit pattern
     * into an instruction. This implementation is <i>parallel</i>, meaning
     * there are two trees: one for the instruction resolution and one of the
     * addressing mode resolution. By beginning at the root node of the
     * addressing mode and instruction resolution trees, the loop compares
     * bits in the bit patterns and moves down the two trees in parallel.
     * When both trees reach an endpoint, the comparison stops and an
     * instruction will be built. This method accepts the value of the first
     * word of the bits and begins decoding from there.
     */
    MSP430Instr run_decoder(DTNode addr, DTNode instr) {
        state = MOVE;
        terminated = 0;
        while ( state == MOVE ) {
            int bits = (word0 >> addr.left_bit) & addr.mask;
            addr = addr.move(this, bits);
            instr = instr.move(this, bits);
        }
        if ( state == ERR ) return null;
        else return builder.build(addrMode);
    }
}
