/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package avrora.core;

import avrora.util.Arithmetic;
import avrora.Avrora;

/**
 * @author Ben L. Titzer
 */
public class Disassembler {

    int pc;
    byte[] code;

    public Instr disassemble(byte[] buffer, int index) {
        int word1 = Arithmetic.word(buffer[index+0], buffer[index+1]);
        this.pc = index;
        this.code = buffer;
        return decode_root(word1);
    }

    private Register getReg(Register[] table, int index) {
        if ( index < 0 || index >= table.length )  {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        Register reg = table[index];
        if ( reg == null )  {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        return reg;
    }

    private int getWord(int word) {
        return Arithmetic.uword(code[word*2], code[word*2 + 1]);
    }

    private int getByte(int word) {
        return code[word*2] & 0xff;
    }

//--BEGIN DISASSEM GENERATOR--
    static final Register[] GPR_table = {
        Register.R0, 
        Register.R1, 
        Register.R2, 
        Register.R3, 
        Register.R4, 
        Register.R5, 
        Register.R6, 
        Register.R7, 
        Register.R8, 
        Register.R9, 
        Register.R10, 
        Register.R11, 
        Register.R12, 
        Register.R13, 
        Register.R14, 
        Register.R15, 
        Register.R16, 
        Register.R17, 
        Register.R18, 
        Register.R19, 
        Register.R20, 
        Register.R21, 
        Register.R22, 
        Register.R23, 
        Register.R24, 
        Register.R25, 
        Register.R26, 
        Register.R27, 
        Register.R28, 
        Register.R29, 
        Register.R30, 
        Register.R31
    };
    static final Register[] HGPR_table = {
        Register.R16, 
        Register.R17, 
        Register.R18, 
        Register.R19, 
        Register.R20, 
        Register.R21, 
        Register.R22, 
        Register.R23, 
        Register.R24, 
        Register.R25, 
        Register.R26, 
        Register.R27, 
        Register.R28, 
        Register.R29, 
        Register.R30, 
        Register.R31
    };
    static final Register[] MGPR_table = {
        Register.R16, 
        Register.R17, 
        Register.R18, 
        Register.R19, 
        Register.R20, 
        Register.R21, 
        Register.R22, 
        Register.R23
    };
    static final Register[] YZ_table = {
        Register.Y, 
        Register.Z
    };
    static final Register[] Z_table = {
        Register.Z
    };
    static final Register[] EGPR_table = {
        Register.R0, 
        Register.R2, 
        Register.R4, 
        Register.R6, 
        Register.R8, 
        Register.R10, 
        Register.R12, 
        Register.R14, 
        Register.R16, 
        Register.R18, 
        Register.R20, 
        Register.R22, 
        Register.R24, 
        Register.R26, 
        Register.R28, 
        Register.R30
    };
    static final Register[] RDL_table = {
        Register.R24, 
        Register.R26, 
        Register.R28, 
        Register.R30
    };
    static final Register[] ADR_table = {
        Register.X, 
        Register.Y, 
        Register.Z, 
        null
    };
    private Instr decode_BST(int word1) {
        if ( (word1 & 0x00008) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        int rr = 0;
        int bit = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:12] -> 
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.BST(pc, getReg(GPR_table, rr), bit);
    }
    private Instr decode_BLD(int word1) {
        if ( (word1 & 0x00008) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        int rr = 0;
        int bit = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:12] -> 
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.BLD(pc, getReg(GPR_table, rr), bit);
    }
    private Instr decode_0(int word1) {
        // get value of bits logical[6:6]
        int value = (word1 >> 9) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_BST(word1);
            case 0x00000: return decode_BLD(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_BRPL(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRPL(pc, target);
    }
    private Instr decode_BRGE(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRGE(pc, target);
    }
    private Instr decode_BRTC(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRTC(pc, target);
    }
    private Instr decode_BRNE(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRNE(pc, target);
    }
    private Instr decode_BRVC(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRVC(pc, target);
    }
    private Instr decode_BRID(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRID(pc, target);
    }
    private Instr decode_BRHC(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRHC(pc, target);
    }
    private Instr decode_BRCC(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRCC(pc, target);
    }
    private Instr decode_1(int word1) {
        // get value of bits logical[13:15]
        int value = (word1 >> 0) & 0x00007;
        switch ( value ) {
            case 0x00002: return decode_BRPL(word1);
            case 0x00004: return decode_BRGE(word1);
            case 0x00006: return decode_BRTC(word1);
            case 0x00001: return decode_BRNE(word1);
            case 0x00003: return decode_BRVC(word1);
            case 0x00007: return decode_BRID(word1);
            case 0x00005: return decode_BRHC(word1);
            case 0x00000: return decode_BRCC(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SBRS(int word1) {
        if ( (word1 & 0x00008) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        int rr = 0;
        int bit = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:12] -> 
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.SBRS(pc, getReg(GPR_table, rr), bit);
    }
    private Instr decode_SBRC(int word1) {
        if ( (word1 & 0x00008) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        int rr = 0;
        int bit = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:12] -> 
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.SBRC(pc, getReg(GPR_table, rr), bit);
    }
    private Instr decode_2(int word1) {
        // get value of bits logical[6:6]
        int value = (word1 >> 9) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_SBRS(word1);
            case 0x00000: return decode_SBRC(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_BRMI(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRMI(pc, target);
    }
    private Instr decode_BRLT(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRLT(pc, target);
    }
    private Instr decode_BRTS(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRTS(pc, target);
    }
    private Instr decode_BREQ(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BREQ(pc, target);
    }
    private Instr decode_BRVS(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRVS(pc, target);
    }
    private Instr decode_BRIE(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRIE(pc, target);
    }
    private Instr decode_BRHS(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRHS(pc, target);
    }
    private Instr decode_BRLO(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRLO(pc, target);
    }
    private Instr decode_3(int word1) {
        // get value of bits logical[13:15]
        int value = (word1 >> 0) & 0x00007;
        switch ( value ) {
            case 0x00002: return decode_BRMI(word1);
            case 0x00004: return decode_BRLT(word1);
            case 0x00006: return decode_BRTS(word1);
            case 0x00001: return decode_BREQ(word1);
            case 0x00003: return decode_BRVS(word1);
            case 0x00007: return decode_BRIE(word1);
            case 0x00005: return decode_BRHS(word1);
            case 0x00000: return decode_BRLO(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_4(int word1) {
        // get value of bits logical[4:5]
        int value = (word1 >> 10) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_0(word1);
            case 0x00001: return decode_1(word1);
            case 0x00003: return decode_2(word1);
            case 0x00000: return decode_3(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SBCI(int word1) {
        int rd = 0;
        int imm = 0;
        // logical[0:3] -> 
        // logical[4:7] -> imm[7:4]
        imm |= ((word1 >> 8) & 0x0000F) << 4;
        // logical[8:11] -> rd[4:1]
        rd |= ((word1 >> 4) & 0x0000F) << 1;
        // logical[12:15] -> imm[3:0]
        imm |= (word1 & 0x0000F);
        return new Instr.SBCI(pc, getReg(HGPR_table, rd), imm);
    }
    private Instr decode_OUT(int word1) {
        int ior = 0;
        int rr = 0;
        // logical[0:4] -> 
        // logical[5:6] -> ior[5:4]
        ior |= ((word1 >> 9) & 0x00003) << 4;
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> ior[3:0]
        ior |= (word1 & 0x0000F);
        return new Instr.OUT(pc, ior, getReg(GPR_table, rr));
    }
    private Instr decode_IN(int word1) {
        int rd = 0;
        int imm = 0;
        // logical[0:4] -> 
        // logical[5:6] -> imm[5:4]
        imm |= ((word1 >> 9) & 0x00003) << 4;
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> imm[3:0]
        imm |= (word1 & 0x0000F);
        return new Instr.IN(pc, getReg(GPR_table, rd), imm);
    }
    private Instr decode_5(int word1) {
        // get value of bits logical[4:4]
        int value = (word1 >> 11) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_OUT(word1);
            case 0x00000: return decode_IN(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_CPI(int word1) {
        int rd = 0;
        int imm = 0;
        // logical[0:3] -> 
        // logical[4:7] -> imm[7:4]
        imm |= ((word1 >> 8) & 0x0000F) << 4;
        // logical[8:11] -> rd[4:1]
        rd |= ((word1 >> 4) & 0x0000F) << 1;
        // logical[12:15] -> imm[3:0]
        imm |= (word1 & 0x0000F);
        return new Instr.CPI(pc, getReg(HGPR_table, rd), imm);
    }
    private Instr decode_ANDI(int word1) {
        int rd = 0;
        int imm = 0;
        // logical[0:3] -> 
        // logical[4:7] -> imm[7:4]
        imm |= ((word1 >> 8) & 0x0000F) << 4;
        // logical[8:11] -> rd[4:1]
        rd |= ((word1 >> 4) & 0x0000F) << 1;
        // logical[12:15] -> imm[3:0]
        imm |= (word1 & 0x0000F);
        return new Instr.ANDI(pc, getReg(HGPR_table, rd), imm);
    }
    private Instr decode_RJMP(int word1) {
        int target = 0;
        // logical[0:3] -> 
        // logical[4:15] -> target[11:0]
        target |= (word1 & 0x00FFF);
        return new Instr.RJMP(pc, target);
    }
    private Instr decode_OR(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.OR(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_EOR(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.EOR(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_MOV(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.MOV(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_AND(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.AND(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_6(int word1) {
        // get value of bits logical[4:5]
        int value = (word1 >> 10) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_OR(word1);
            case 0x00001: return decode_EOR(word1);
            case 0x00003: return decode_MOV(word1);
            case 0x00000: return decode_AND(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_RCALL(int word1) {
        int target = 0;
        // logical[0:3] -> 
        // logical[4:15] -> target[11:0]
        target |= (word1 & 0x00FFF);
        return new Instr.RCALL(pc, target);
    }
    private Instr decode_SBI(int word1) {
        int ior = 0;
        int bit = 0;
        // logical[0:7] -> 
        // logical[8:12] -> ior[4:0]
        ior |= ((word1 >> 3) & 0x0001F);
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.SBI(pc, ior, bit);
    }
    private Instr decode_SBIC(int word1) {
        int ior = 0;
        int bit = 0;
        // logical[0:7] -> 
        // logical[8:12] -> ior[4:0]
        ior |= ((word1 >> 3) & 0x0001F);
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.SBIC(pc, ior, bit);
    }
    private Instr decode_SBIS(int word1) {
        int ior = 0;
        int bit = 0;
        // logical[0:7] -> 
        // logical[8:12] -> ior[4:0]
        ior |= ((word1 >> 3) & 0x0001F);
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.SBIS(pc, ior, bit);
    }
    private Instr decode_CBI(int word1) {
        int ior = 0;
        int bit = 0;
        // logical[0:7] -> 
        // logical[8:12] -> ior[4:0]
        ior |= ((word1 >> 3) & 0x0001F);
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.CBI(pc, ior, bit);
    }
    private Instr decode_7(int word1) {
        // get value of bits logical[6:7]
        int value = (word1 >> 8) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_SBI(word1);
            case 0x00001: return decode_SBIC(word1);
            case 0x00003: return decode_SBIS(word1);
            case 0x00000: return decode_CBI(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SBIW(int word1) {
        int rd = 0;
        int imm = 0;
        // logical[0:7] -> 
        // logical[8:9] -> imm[5:4]
        imm |= ((word1 >> 6) & 0x00003) << 4;
        // logical[10:11] -> rd[2:1]
        rd |= ((word1 >> 4) & 0x00003) << 1;
        // logical[12:15] -> imm[3:0]
        imm |= (word1 & 0x0000F);
        return new Instr.SBIW(pc, getReg(RDL_table, rd), imm);
    }
    private Instr decode_ADIW(int word1) {
        int rd = 0;
        int imm = 0;
        // logical[0:7] -> 
        // logical[8:9] -> imm[5:4]
        imm |= ((word1 >> 6) & 0x00003) << 4;
        // logical[10:11] -> rd[2:1]
        rd |= ((word1 >> 4) & 0x00003) << 1;
        // logical[12:15] -> imm[3:0]
        imm |= (word1 & 0x0000F);
        return new Instr.ADIW(pc, getReg(RDL_table, rd), imm);
    }
    private Instr decode_8(int word1) {
        // get value of bits logical[7:7]
        int value = (word1 >> 8) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_SBIW(word1);
            case 0x00000: return decode_ADIW(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_ASR(int word1) {
        if ( (word1 & 0x00001) != 0x00001 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> 
        return new Instr.ASR(pc, getReg(GPR_table, rd));
    }
    private Instr decode_CLI(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLI(pc);
    }
    private Instr decode_SES(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SES(pc);
    }
    private Instr decode_SPM(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.SPM(pc);
    }
    private Instr decode_CLC(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLC(pc);
    }
    private Instr decode_WDR(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.WDR(pc);
    }
    private Instr decode_CLV(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLV(pc);
    }
    private Instr decode_ICALL(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.ICALL(pc);
    }
    private Instr decode_RET(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.RET(pc);
    }
    private Instr decode_9(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_ICALL(word1);
            case 0x00000: return decode_RET(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SEV(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEV(pc);
    }
    private Instr decode_SEI(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEI(pc);
    }
    private Instr decode_CLS(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLS(pc);
    }
    private Instr decode_EICALL(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.EICALL(pc);
    }
    private Instr decode_RETI(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.RETI(pc);
    }
    private Instr decode_10(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_EICALL(word1);
            case 0x00000: return decode_RETI(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SEN(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEN(pc);
    }
    private Instr decode_CLH(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLH(pc);
    }
    private Instr decode_CLZ(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLZ(pc);
    }
    private Instr decode_LPM(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.LPM(pc);
    }
    private Instr decode_SET(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SET(pc);
    }
    private Instr decode_EIJMP(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.EIJMP(pc);
    }
    private Instr decode_SEZ(int word1) {
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEZ(pc);
    }
    private Instr decode_11(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_EIJMP(word1);
            case 0x00000: return decode_SEZ(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_ELPM(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.ELPM(pc);
    }
    private Instr decode_CLT(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLT(pc);
    }
    private Instr decode_BREAK(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.BREAK(pc);
    }
    private Instr decode_SLEEP(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.SLEEP(pc);
    }
    private Instr decode_CLN(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLN(pc);
    }
    private Instr decode_SEH(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEH(pc);
    }
    private Instr decode_IJMP(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.IJMP(pc);
    }
    private Instr decode_SEC(int word1) {
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEC(pc);
    }
    private Instr decode_12(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_IJMP(word1);
            case 0x00000: return decode_SEC(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_13(int word1) {
        // get value of bits logical[7:11]
        int value = (word1 >> 4) & 0x0001F;
        switch ( value ) {
            case 0x0000F: return decode_CLI(word1);
            case 0x00004: return decode_SES(word1);
            case 0x0001E: return decode_SPM(word1);
            case 0x00008: return decode_CLC(word1);
            case 0x0001A: return decode_WDR(word1);
            case 0x0000B: return decode_CLV(word1);
            case 0x00010: return decode_9(word1);
            case 0x00003: return decode_SEV(word1);
            case 0x00007: return decode_SEI(word1);
            case 0x0000C: return decode_CLS(word1);
            case 0x00011: return decode_10(word1);
            case 0x00002: return decode_SEN(word1);
            case 0x0000D: return decode_CLH(word1);
            case 0x00009: return decode_CLZ(word1);
            case 0x0001C: return decode_LPM(word1);
            case 0x00006: return decode_SET(word1);
            case 0x00001: return decode_11(word1);
            case 0x0001D: return decode_ELPM(word1);
            case 0x0000E: return decode_CLT(word1);
            case 0x00019: return decode_BREAK(word1);
            case 0x00018: return decode_SLEEP(word1);
            case 0x0000A: return decode_CLN(word1);
            case 0x00005: return decode_SEH(word1);
            case 0x00000: return decode_12(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_JMP(int word1) {
        int word2 = getWord(1);
        int target = 0;
        // logical[0:6] -> 
        // logical[7:11] -> target[21:17]
        target |= ((word1 >> 4) & 0x0001F) << 17;
        // logical[12:14] -> 
        // logical[15:15] -> target[16:16]
        target |= (word1 & 0x00001) << 16;
        // logical[16:31] -> target[15:0]
        target |= (word2 & 0x0FFFF);
        return new Instr.JMP(pc, target);
    }
    private Instr decode_INC(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.INC(pc, getReg(GPR_table, rd));
    }
    private Instr decode_SWAP(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.SWAP(pc, getReg(GPR_table, rd));
    }
    private Instr decode_14(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_INC(word1);
            case 0x00000: return decode_SWAP(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_ROR(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.ROR(pc, getReg(GPR_table, rd));
    }
    private Instr decode_LSR(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LSR(pc, getReg(GPR_table, rd));
    }
    private Instr decode_15(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_ROR(word1);
            case 0x00000: return decode_LSR(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_CALL(int word1) {
        int word2 = getWord(1);
        int target = 0;
        // logical[0:6] -> 
        // logical[7:11] -> target[21:17]
        target |= ((word1 >> 4) & 0x0001F) << 17;
        // logical[12:14] -> 
        // logical[15:15] -> target[16:16]
        target |= (word1 & 0x00001) << 16;
        // logical[16:31] -> target[15:0]
        target |= (word2 & 0x0FFFF);
        return new Instr.CALL(pc, target);
    }
    private Instr decode_DEC(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.DEC(pc, getReg(GPR_table, rd));
    }
    private Instr decode_NEG(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.NEG(pc, getReg(GPR_table, rd));
    }
    private Instr decode_COM(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.COM(pc, getReg(GPR_table, rd));
    }
    private Instr decode_16(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_NEG(word1);
            case 0x00000: return decode_COM(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_17(int word1) {
        // get value of bits logical[12:14]
        int value = (word1 >> 1) & 0x00007;
        switch ( value ) {
            case 0x00002: return decode_ASR(word1);
            case 0x00004: return decode_13(word1);
            case 0x00006: return decode_JMP(word1);
            case 0x00001: return decode_14(word1);
            case 0x00003: return decode_15(word1);
            case 0x00007: return decode_CALL(word1);
            case 0x00005: return decode_DEC(word1);
            case 0x00000: return decode_16(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_18(int word1) {
        // get value of bits logical[6:6]
        int value = (word1 >> 9) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_8(word1);
            case 0x00000: return decode_17(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_MUL(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rd[4:4]
        rd |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rr[4:4]
        rr |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rr[3:0]
        rr |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rd[3:0]
        rd |= (word1 & 0x0000F);
        return new Instr.MUL(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_PUSH(int word1) {
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.PUSH(pc, getReg(GPR_table, rr));
    }
    private Instr decode_STS(int word1) {
        int word2 = getWord(1);
        int addr = 0;
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        // logical[16:31] -> addr[15:0]
        addr |= (word2 & 0x0FFFF);
        return new Instr.STS(pc, addr, getReg(GPR_table, rr));
    }
    private Instr decode_19(int word1) {
        // get value of bits logical[12:15]
        int value = (word1 >> 0) & 0x0000F;
        switch ( value ) {
            case 0x0000F: return decode_PUSH(word1);
            case 0x00000: return decode_STS(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_POP(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.POP(pc, getReg(GPR_table, rd));
    }
    private Instr decode_LPMD(int word1) {
        int rd = 0;
        int z = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LPMD(pc, getReg(GPR_table, rd), getReg(Z_table, z));
    }
    private Instr decode_ELPMD(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.ELPMD(pc, getReg(GPR_table, rd), getReg(Z_table, rr));
    }
    private Instr decode_ELPMPI(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.ELPMPI(pc, getReg(GPR_table, rd), getReg(Z_table, rr));
    }
    private Instr decode_LPMPI(int word1) {
        int rd = 0;
        int z = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LPMPI(pc, getReg(GPR_table, rd), getReg(Z_table, z));
    }
    private Instr decode_LDS(int word1) {
        int word2 = getWord(1);
        int rd = 0;
        int addr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        // logical[16:31] -> addr[15:0]
        addr |= (word2 & 0x0FFFF);
        return new Instr.LDS(pc, getReg(GPR_table, rd), addr);
    }
    private Instr decode_20(int word1) {
        // get value of bits logical[12:15]
        int value = (word1 >> 0) & 0x0000F;
        switch ( value ) {
            case 0x0000F: return decode_POP(word1);
            case 0x00004: return decode_LPMD(word1);
            case 0x00006: return decode_ELPMD(word1);
            case 0x00007: return decode_ELPMPI(word1);
            case 0x00005: return decode_LPMPI(word1);
            case 0x00000: return decode_LDS(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_21(int word1) {
        // get value of bits logical[6:6]
        int value = (word1 >> 9) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_19(word1);
            case 0x00000: return decode_20(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_22(int word1) {
        // get value of bits logical[4:5]
        int value = (word1 >> 10) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_7(word1);
            case 0x00001: return decode_18(word1);
            case 0x00003: return decode_MUL(word1);
            case 0x00000: return decode_21(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_ORI(int word1) {
        int rd = 0;
        int imm = 0;
        // logical[0:3] -> 
        // logical[4:7] -> imm[7:4]
        imm |= ((word1 >> 8) & 0x0000F) << 4;
        // logical[8:11] -> rd[4:1]
        rd |= ((word1 >> 4) & 0x0000F) << 1;
        // logical[12:15] -> imm[3:0]
        imm |= (word1 & 0x0000F);
        return new Instr.ORI(pc, getReg(HGPR_table, rd), imm);
    }
    private Instr decode_SUB(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.SUB(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_CP(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.CP(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_ADC(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.ADC(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_CPSE(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.CPSE(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_23(int word1) {
        // get value of bits logical[4:5]
        int value = (word1 >> 10) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_SUB(word1);
            case 0x00001: return decode_CP(word1);
            case 0x00003: return decode_ADC(word1);
            case 0x00000: return decode_CPSE(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_LDI(int word1) {
        int rd = 0;
        int imm = 0;
        // logical[0:3] -> 
        // logical[4:7] -> imm[7:4]
        imm |= ((word1 >> 8) & 0x0000F) << 4;
        // logical[8:11] -> rd[4:1]
        rd |= ((word1 >> 4) & 0x0000F) << 1;
        // logical[12:15] -> imm[3:0]
        imm |= (word1 & 0x0000F);
        return new Instr.LDI(pc, getReg(HGPR_table, rd), imm);
    }
    private Instr decode_SUBI(int word1) {
        int rd = 0;
        int imm = 0;
        // logical[0:3] -> 
        // logical[4:7] -> imm[7:4]
        imm |= ((word1 >> 8) & 0x0000F) << 4;
        // logical[8:11] -> rd[4:1]
        rd |= ((word1 >> 4) & 0x0000F) << 1;
        // logical[12:15] -> imm[3:0]
        imm |= (word1 & 0x0000F);
        return new Instr.SUBI(pc, getReg(HGPR_table, rd), imm);
    }
    private Instr decode_SBC(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.SBC(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_CPC(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.CPC(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_ADD(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:5] -> 
        // logical[6:6] -> rr[4:4]
        rr |= ((word1 >> 9) & 0x00001) << 4;
        // logical[7:7] -> rd[4:4]
        rd |= ((word1 >> 8) & 0x00001) << 4;
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.ADD(pc, getReg(GPR_table, rd), getReg(GPR_table, rr));
    }
    private Instr decode_MULS(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:7] -> 
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.MULS(pc, getReg(HGPR_table, rd), getReg(HGPR_table, rr));
    }
    private Instr decode_MOVW(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:7] -> 
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.MOVW(pc, getReg(EGPR_table, rd), getReg(EGPR_table, rr));
    }
    private Instr decode_FMULSU(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> rd[2:0]
        rd |= ((word1 >> 4) & 0x00007);
        // logical[12:12] -> 
        // logical[13:15] -> rr[2:0]
        rr |= (word1 & 0x00007);
        return new Instr.FMULSU(pc, getReg(MGPR_table, rd), getReg(MGPR_table, rr));
    }
    private Instr decode_FMULS(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> rd[2:0]
        rd |= ((word1 >> 4) & 0x00007);
        // logical[12:12] -> 
        // logical[13:15] -> rr[2:0]
        rr |= (word1 & 0x00007);
        return new Instr.FMULS(pc, getReg(MGPR_table, rd), getReg(MGPR_table, rr));
    }
    private Instr decode_24(int word1) {
        // get value of bits logical[12:12]
        int value = (word1 >> 3) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_FMULSU(word1);
            case 0x00000: return decode_FMULS(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_FMUL(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> rd[2:0]
        rd |= ((word1 >> 4) & 0x00007);
        // logical[12:12] -> 
        // logical[13:15] -> rr[2:0]
        rr |= (word1 & 0x00007);
        return new Instr.FMUL(pc, getReg(MGPR_table, rd), getReg(MGPR_table, rr));
    }
    private Instr decode_MULSU(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> rd[2:0]
        rd |= ((word1 >> 4) & 0x00007);
        // logical[12:12] -> 
        // logical[13:15] -> rr[2:0]
        rr |= (word1 & 0x00007);
        return new Instr.MULSU(pc, getReg(MGPR_table, rd), getReg(MGPR_table, rr));
    }
    private Instr decode_25(int word1) {
        // get value of bits logical[12:12]
        int value = (word1 >> 3) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_FMUL(word1);
            case 0x00000: return decode_MULSU(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_26(int word1) {
        // get value of bits logical[8:8]
        int value = (word1 >> 7) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_24(word1);
            case 0x00000: return decode_25(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_NOP(int word1) {
        if ( (word1 & 0x000FF) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.NOP(pc);
    }
    private Instr decode_27(int word1) {
        // get value of bits logical[6:7]
        int value = (word1 >> 8) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_MULS(word1);
            case 0x00001: return decode_MOVW(word1);
            case 0x00003: return decode_26(word1);
            case 0x00000: return decode_NOP(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_28(int word1) {
        // get value of bits logical[4:5]
        int value = (word1 >> 10) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_SBC(word1);
            case 0x00001: return decode_CPC(word1);
            case 0x00003: return decode_ADD(word1);
            case 0x00000: return decode_27(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_root(int word1) {
        // get value of bits logical[0:3]
        int value = (word1 >> 12) & 0x0000F;
        switch ( value ) {
            case 0x0000F: return decode_4(word1);
            case 0x00004: return decode_SBCI(word1);
            case 0x0000B: return decode_5(word1);
            case 0x00003: return decode_CPI(word1);
            case 0x00007: return decode_ANDI(word1);
            case 0x0000C: return decode_RJMP(word1);
            case 0x00002: return decode_6(word1);
            case 0x0000D: return decode_RCALL(word1);
            case 0x00009: return decode_22(word1);
            case 0x00006: return decode_ORI(word1);
            case 0x00001: return decode_23(word1);
            case 0x0000E: return decode_LDI(word1);
            case 0x00005: return decode_SUBI(word1);
            case 0x00000: return decode_28(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
//--END DISASSEM GENERATOR--

}
