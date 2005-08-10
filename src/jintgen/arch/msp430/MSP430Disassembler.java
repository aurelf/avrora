package jintgen.arch.msp430;
public class MSP430Disassembler {
    static class InvalidInstruction extends Exception {
        InvalidInstruction(int word1, int pc)  {
            super("Invalid instruction at "+pc);
        }
    }
    static final MSP430Symbol.GPR[] GPR_table = {
        MSP430Symbol.GPR.PC,  // 0 (0b0000) -> pc
        MSP430Symbol.GPR.SREG,  // 1 (0b0001) -> sreg
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
}