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

    private int relative(int address) {
        return address*2 + pc + 2;
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
        Register.Z, 
        Register.Y
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
    static final Register[] XYZ_table = {
        Register.X, 
        Register.Y, 
        Register.Z, 
        null
    };
    private Instr decode_OUT_0(int word1) {
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
    private Instr decode_IN_0(int word1) {
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
    private Instr decode_0(int word1) {
        // get value of bits logical[4:4]
        int value = (word1 >> 11) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_OUT_0(word1);
            case 0x00000: return decode_IN_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SBI_0(int word1) {
        int ior = 0;
        int bit = 0;
        // logical[0:7] -> 
        // logical[8:12] -> ior[4:0]
        ior |= ((word1 >> 3) & 0x0001F);
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.SBI(pc, ior, bit);
    }
    private Instr decode_SBIC_0(int word1) {
        int ior = 0;
        int bit = 0;
        // logical[0:7] -> 
        // logical[8:12] -> ior[4:0]
        ior |= ((word1 >> 3) & 0x0001F);
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.SBIC(pc, ior, bit);
    }
    private Instr decode_SBIS_0(int word1) {
        int ior = 0;
        int bit = 0;
        // logical[0:7] -> 
        // logical[8:12] -> ior[4:0]
        ior |= ((word1 >> 3) & 0x0001F);
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.SBIS(pc, ior, bit);
    }
    private Instr decode_CBI_0(int word1) {
        int ior = 0;
        int bit = 0;
        // logical[0:7] -> 
        // logical[8:12] -> ior[4:0]
        ior |= ((word1 >> 3) & 0x0001F);
        // logical[13:15] -> bit[2:0]
        bit |= (word1 & 0x00007);
        return new Instr.CBI(pc, ior, bit);
    }
    private Instr decode_1(int word1) {
        // get value of bits logical[6:7]
        int value = (word1 >> 8) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_SBI_0(word1);
            case 0x00001: return decode_SBIC_0(word1);
            case 0x00003: return decode_SBIS_0(word1);
            case 0x00000: return decode_CBI_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SBIW_0(int word1) {
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
    private Instr decode_ADIW_0(int word1) {
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
    private Instr decode_2(int word1) {
        // get value of bits logical[7:7]
        int value = (word1 >> 8) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_SBIW_0(word1);
            case 0x00000: return decode_ADIW_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_ASR_0(int word1) {
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
    private Instr decode_CLI_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLI(pc);
    }
    private Instr decode_SES_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SES(pc);
    }
    private Instr decode_SPM_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.SPM(pc);
    }
    private Instr decode_CLC_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLC(pc);
    }
    private Instr decode_WDR_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.WDR(pc);
    }
    private Instr decode_CLV_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLV(pc);
    }
    private Instr decode_ICALL_0(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.ICALL(pc);
    }
    private Instr decode_RET_0(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.RET(pc);
    }
    private Instr decode_3(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_ICALL_0(word1);
            case 0x00000: return decode_RET_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SEV_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEV(pc);
    }
    private Instr decode_SEI_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEI(pc);
    }
    private Instr decode_CLS_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLS(pc);
    }
    private Instr decode_EICALL_0(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.EICALL(pc);
    }
    private Instr decode_RETI_0(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.RETI(pc);
    }
    private Instr decode_4(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_EICALL_0(word1);
            case 0x00000: return decode_RETI_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SEN_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEN(pc);
    }
    private Instr decode_CLH_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLH(pc);
    }
    private Instr decode_CLZ_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLZ(pc);
    }
    private Instr decode_LPM_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.LPM(pc);
    }
    private Instr decode_SET_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SET(pc);
    }
    private Instr decode_EIJMP_0(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.EIJMP(pc);
    }
    private Instr decode_SEZ_0(int word1) {
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEZ(pc);
    }
    private Instr decode_5(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_EIJMP_0(word1);
            case 0x00000: return decode_SEZ_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_ELPM_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.ELPM(pc);
    }
    private Instr decode_CLT_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLT(pc);
    }
    private Instr decode_BREAK_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.BREAK(pc);
    }
    private Instr decode_SLEEP_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.SLEEP(pc);
    }
    private Instr decode_CLN_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.CLN(pc);
    }
    private Instr decode_SEH_0(int word1) {
        if ( (word1 & 0x00001) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEH(pc);
    }
    private Instr decode_IJMP_0(int word1) {
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.IJMP(pc);
    }
    private Instr decode_SEC_0(int word1) {
        // logical[0:7] -> 
        // logical[8:8] -> 
        // logical[9:11] -> 
        // logical[12:15] -> 
        return new Instr.SEC(pc);
    }
    private Instr decode_6(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_IJMP_0(word1);
            case 0x00000: return decode_SEC_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_7(int word1) {
        // get value of bits logical[7:11]
        int value = (word1 >> 4) & 0x0001F;
        switch ( value ) {
            case 0x0000F: return decode_CLI_0(word1);
            case 0x00004: return decode_SES_0(word1);
            case 0x0001E: return decode_SPM_0(word1);
            case 0x00008: return decode_CLC_0(word1);
            case 0x0001A: return decode_WDR_0(word1);
            case 0x0000B: return decode_CLV_0(word1);
            case 0x00010: return decode_3(word1);
            case 0x00003: return decode_SEV_0(word1);
            case 0x00007: return decode_SEI_0(word1);
            case 0x0000C: return decode_CLS_0(word1);
            case 0x00011: return decode_4(word1);
            case 0x00002: return decode_SEN_0(word1);
            case 0x0000D: return decode_CLH_0(word1);
            case 0x00009: return decode_CLZ_0(word1);
            case 0x0001C: return decode_LPM_0(word1);
            case 0x00006: return decode_SET_0(word1);
            case 0x00001: return decode_5(word1);
            case 0x0001D: return decode_ELPM_0(word1);
            case 0x0000E: return decode_CLT_0(word1);
            case 0x00019: return decode_BREAK_0(word1);
            case 0x00018: return decode_SLEEP_0(word1);
            case 0x0000A: return decode_CLN_0(word1);
            case 0x00005: return decode_SEH_0(word1);
            case 0x00000: return decode_6(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_JMP_0(int word1) {
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
    private Instr decode_INC_0(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.INC(pc, getReg(GPR_table, rd));
    }
    private Instr decode_SWAP_0(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.SWAP(pc, getReg(GPR_table, rd));
    }
    private Instr decode_8(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_INC_0(word1);
            case 0x00000: return decode_SWAP_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_ROR_0(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.ROR(pc, getReg(GPR_table, rd));
    }
    private Instr decode_LSR_0(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LSR(pc, getReg(GPR_table, rd));
    }
    private Instr decode_9(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_ROR_0(word1);
            case 0x00000: return decode_LSR_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_CALL_0(int word1) {
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
    private Instr decode_DEC_0(int word1) {
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
    private Instr decode_NEG_0(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.NEG(pc, getReg(GPR_table, rd));
    }
    private Instr decode_COM_0(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.COM(pc, getReg(GPR_table, rd));
    }
    private Instr decode_10(int word1) {
        // get value of bits logical[15:15]
        int value = (word1 >> 0) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_NEG_0(word1);
            case 0x00000: return decode_COM_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_11(int word1) {
        // get value of bits logical[12:14]
        int value = (word1 >> 1) & 0x00007;
        switch ( value ) {
            case 0x00002: return decode_ASR_0(word1);
            case 0x00004: return decode_7(word1);
            case 0x00006: return decode_JMP_0(word1);
            case 0x00001: return decode_8(word1);
            case 0x00003: return decode_9(word1);
            case 0x00007: return decode_CALL_0(word1);
            case 0x00005: return decode_DEC_0(word1);
            case 0x00000: return decode_10(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_12(int word1) {
        // get value of bits logical[6:6]
        int value = (word1 >> 9) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_2(word1);
            case 0x00000: return decode_11(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_MUL_0(int word1) {
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
    private Instr decode_STPI_2(int word1) {
        // this method decodes STPI when ar == Z
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.STPI(pc, Register.Z, getReg(GPR_table, rr));
    }
    private Instr decode_PUSH_0(int word1) {
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.PUSH(pc, getReg(GPR_table, rr));
    }
    private Instr decode_STPD_0(int word1) {
        // this method decodes STPD when ar == X
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.STPD(pc, Register.X, getReg(GPR_table, rr));
    }
    private Instr decode_STPD_1(int word1) {
        // this method decodes STPD when ar == Y
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.STPD(pc, Register.Y, getReg(GPR_table, rr));
    }
    private Instr decode_STPD_2(int word1) {
        // this method decodes STPD when ar == Z
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.STPD(pc, Register.Z, getReg(GPR_table, rr));
    }
    private Instr decode_STPI_0(int word1) {
        // this method decodes STPI when ar == X
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.STPI(pc, Register.X, getReg(GPR_table, rr));
    }
    private Instr decode_STPI_1(int word1) {
        // this method decodes STPI when ar == Y
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.STPI(pc, Register.Y, getReg(GPR_table, rr));
    }
    private Instr decode_STS_0(int word1) {
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
    private Instr decode_13(int word1) {
        // get value of bits logical[12:15]
        int value = (word1 >> 0) & 0x0000F;
        switch ( value ) {
            case 0x00002: return decode_STPI_2(word1);
            case 0x0000F: return decode_PUSH_0(word1);
            case 0x0000D: return decode_STPD_0(word1);
            case 0x00009: return decode_STPD_1(word1);
            case 0x00001: return decode_STPD_2(word1);
            case 0x0000E: return decode_STPI_0(word1);
            case 0x0000A: return decode_STPI_1(word1);
            case 0x00000: return decode_STS_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_POP_0(int word1) {
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.POP(pc, getReg(GPR_table, rd));
    }
    private Instr decode_LDPD_2(int word1) {
        // this method decodes LDPD when ar == Z
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LDPD(pc, getReg(GPR_table, rd), Register.Z);
    }
    private Instr decode_LPMD_0(int word1) {
        int rd = 0;
        int z = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LPMD(pc, getReg(GPR_table, rd), getReg(Z_table, z));
    }
    private Instr decode_LDPI_0(int word1) {
        // this method decodes LDPI when ar == X
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LDPI(pc, getReg(GPR_table, rd), Register.X);
    }
    private Instr decode_LDPI_1(int word1) {
        // this method decodes LDPI when ar == Y
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LDPI(pc, getReg(GPR_table, rd), Register.Y);
    }
    private Instr decode_ELPMD_0(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.ELPMD(pc, getReg(GPR_table, rd), getReg(Z_table, rr));
    }
    private Instr decode_LDPI_2(int word1) {
        // this method decodes LDPI when ar == Z
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LDPI(pc, getReg(GPR_table, rd), Register.Z);
    }
    private Instr decode_LDPD_0(int word1) {
        // this method decodes LDPD when ar == X
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LDPD(pc, getReg(GPR_table, rd), Register.X);
    }
    private Instr decode_LDPD_1(int word1) {
        // this method decodes LDPD when ar == Y
        int rd = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LDPD(pc, getReg(GPR_table, rd), Register.Y);
    }
    private Instr decode_ELPMPI_0(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.ELPMPI(pc, getReg(GPR_table, rd), getReg(Z_table, rr));
    }
    private Instr decode_LPMPI_0(int word1) {
        int rd = 0;
        int z = 0;
        // logical[0:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:15] -> 
        return new Instr.LPMPI(pc, getReg(GPR_table, rd), getReg(Z_table, z));
    }
    private Instr decode_LDS_0(int word1) {
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
    private Instr decode_14(int word1) {
        // get value of bits logical[12:15]
        int value = (word1 >> 0) & 0x0000F;
        switch ( value ) {
            case 0x0000F: return decode_POP_0(word1);
            case 0x00002: return decode_LDPD_2(word1);
            case 0x00004: return decode_LPMD_0(word1);
            case 0x0000D: return decode_LDPI_0(word1);
            case 0x00009: return decode_LDPI_1(word1);
            case 0x00006: return decode_ELPMD_0(word1);
            case 0x00001: return decode_LDPI_2(word1);
            case 0x0000E: return decode_LDPD_0(word1);
            case 0x0000A: return decode_LDPD_1(word1);
            case 0x00007: return decode_ELPMPI_0(word1);
            case 0x00005: return decode_LPMPI_0(word1);
            case 0x00000: return decode_LDS_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_15(int word1) {
        // get value of bits logical[6:6]
        int value = (word1 >> 9) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_13(word1);
            case 0x00000: return decode_14(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_16(int word1) {
        // get value of bits logical[4:5]
        int value = (word1 >> 10) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_1(word1);
            case 0x00001: return decode_12(word1);
            case 0x00003: return decode_MUL_0(word1);
            case 0x00000: return decode_15(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_17(int word1) {
        // get value of bits logical[2:2]
        int value = (word1 >> 13) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_0(word1);
            case 0x00000: return decode_16(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_STD_0(int word1) {
        int ar = 0;
        int imm = 0;
        int rr = 0;
        // logical[0:1] -> 
        // logical[2:2] -> imm[5]
        imm = Arithmetic.setBit(imm, 5, Arithmetic.getBit(word1, 13));
        // logical[3:3] -> 
        // logical[4:5] -> imm[4:3]
        imm |= ((word1 >> 10) & 0x00003) << 3;
        // logical[6:6] -> 
        // logical[7:11] -> rr[4:0]
        rr |= ((word1 >> 4) & 0x0001F);
        // logical[12:12] -> ar[0]
        ar = Arithmetic.setBit(ar, 0, Arithmetic.getBit(word1, 3));
        // logical[13:15] -> imm[2:0]
        imm |= (word1 & 0x00007);
        return new Instr.STD(pc, getReg(YZ_table, ar), imm, getReg(GPR_table, rr));
    }
    private Instr decode_LDD_0(int word1) {
        int rd = 0;
        int ar = 0;
        int imm = 0;
        // logical[0:1] -> 
        // logical[2:2] -> imm[5]
        imm = Arithmetic.setBit(imm, 5, Arithmetic.getBit(word1, 13));
        // logical[3:3] -> 
        // logical[4:5] -> imm[4:3]
        imm |= ((word1 >> 10) & 0x00003) << 3;
        // logical[6:6] -> 
        // logical[7:11] -> rd[4:0]
        rd |= ((word1 >> 4) & 0x0001F);
        // logical[12:12] -> ar[0]
        ar = Arithmetic.setBit(ar, 0, Arithmetic.getBit(word1, 3));
        // logical[13:15] -> imm[2:0]
        imm |= (word1 & 0x00007);
        return new Instr.LDD(pc, getReg(GPR_table, rd), getReg(YZ_table, ar), imm);
    }
    private Instr decode_18(int word1) {
        // get value of bits logical[6:6]
        int value = (word1 >> 9) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_STD_0(word1);
            case 0x00000: return decode_LDD_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_19(int word1) {
        // get value of bits logical[3:3]
        int value = (word1 >> 12) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_17(word1);
            case 0x00000: return decode_18(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_ORI_0(int word1) {
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
    private Instr decode_SUBI_0(int word1) {
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
    private Instr decode_ANDI_0(int word1) {
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
    private Instr decode_SBCI_0(int word1) {
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
    private Instr decode_20(int word1) {
        // get value of bits logical[2:3]
        int value = (word1 >> 12) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_ORI_0(word1);
            case 0x00001: return decode_SUBI_0(word1);
            case 0x00003: return decode_ANDI_0(word1);
            case 0x00000: return decode_SBCI_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_LDI_0(int word1) {
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
    private Instr decode_RCALL_0(int word1) {
        int target = 0;
        // logical[0:3] -> 
        // logical[4:15] -> target[11:0]
        target |= (word1 & 0x00FFF);
        return new Instr.RCALL(pc, relative(target));
    }
    private Instr decode_BST_0(int word1) {
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
    private Instr decode_BLD_0(int word1) {
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
    private Instr decode_21(int word1) {
        // get value of bits logical[6:6]
        int value = (word1 >> 9) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_BST_0(word1);
            case 0x00000: return decode_BLD_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_BRPL_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRPL(pc, relative(target));
    }
    private Instr decode_BRGE_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRGE(pc, relative(target));
    }
    private Instr decode_BRTC_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRTC(pc, relative(target));
    }
    private Instr decode_BRNE_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRNE(pc, relative(target));
    }
    private Instr decode_BRVC_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRVC(pc, relative(target));
    }
    private Instr decode_BRID_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRID(pc, relative(target));
    }
    private Instr decode_BRHC_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRHC(pc, relative(target));
    }
    private Instr decode_BRCC_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRCC(pc, relative(target));
    }
    private Instr decode_22(int word1) {
        // get value of bits logical[13:15]
        int value = (word1 >> 0) & 0x00007;
        switch ( value ) {
            case 0x00002: return decode_BRPL_0(word1);
            case 0x00004: return decode_BRGE_0(word1);
            case 0x00006: return decode_BRTC_0(word1);
            case 0x00001: return decode_BRNE_0(word1);
            case 0x00003: return decode_BRVC_0(word1);
            case 0x00007: return decode_BRID_0(word1);
            case 0x00005: return decode_BRHC_0(word1);
            case 0x00000: return decode_BRCC_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SBRS_0(int word1) {
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
    private Instr decode_SBRC_0(int word1) {
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
    private Instr decode_23(int word1) {
        // get value of bits logical[6:6]
        int value = (word1 >> 9) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_SBRS_0(word1);
            case 0x00000: return decode_SBRC_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_BRMI_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRMI(pc, relative(target));
    }
    private Instr decode_BRLT_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRLT(pc, relative(target));
    }
    private Instr decode_BRTS_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRTS(pc, relative(target));
    }
    private Instr decode_BREQ_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BREQ(pc, relative(target));
    }
    private Instr decode_BRVS_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRVS(pc, relative(target));
    }
    private Instr decode_BRIE_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRIE(pc, relative(target));
    }
    private Instr decode_BRHS_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRHS(pc, relative(target));
    }
    private Instr decode_BRLO_0(int word1) {
        int target = 0;
        // logical[0:5] -> 
        // logical[6:12] -> target[6:0]
        target |= ((word1 >> 3) & 0x0007F);
        // logical[13:15] -> 
        return new Instr.BRLO(pc, relative(target));
    }
    private Instr decode_24(int word1) {
        // get value of bits logical[13:15]
        int value = (word1 >> 0) & 0x00007;
        switch ( value ) {
            case 0x00002: return decode_BRMI_0(word1);
            case 0x00004: return decode_BRLT_0(word1);
            case 0x00006: return decode_BRTS_0(word1);
            case 0x00001: return decode_BREQ_0(word1);
            case 0x00003: return decode_BRVS_0(word1);
            case 0x00007: return decode_BRIE_0(word1);
            case 0x00005: return decode_BRHS_0(word1);
            case 0x00000: return decode_BRLO_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_25(int word1) {
        // get value of bits logical[4:5]
        int value = (word1 >> 10) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_21(word1);
            case 0x00001: return decode_22(word1);
            case 0x00003: return decode_23(word1);
            case 0x00000: return decode_24(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_RJMP_0(int word1) {
        int target = 0;
        // logical[0:3] -> 
        // logical[4:15] -> target[11:0]
        target |= (word1 & 0x00FFF);
        return new Instr.RJMP(pc, relative(target));
    }
    private Instr decode_26(int word1) {
        // get value of bits logical[2:3]
        int value = (word1 >> 12) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_LDI_0(word1);
            case 0x00001: return decode_RCALL_0(word1);
            case 0x00003: return decode_25(word1);
            case 0x00000: return decode_RJMP_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_OR_0(int word1) {
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
    private Instr decode_EOR_0(int word1) {
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
    private Instr decode_MOV_0(int word1) {
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
    private Instr decode_AND_0(int word1) {
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
    private Instr decode_27(int word1) {
        // get value of bits logical[4:5]
        int value = (word1 >> 10) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_OR_0(word1);
            case 0x00001: return decode_EOR_0(word1);
            case 0x00003: return decode_MOV_0(word1);
            case 0x00000: return decode_AND_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_SUB_0(int word1) {
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
    private Instr decode_CP_0(int word1) {
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
    private Instr decode_ADC_0(int word1) {
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
    private Instr decode_CPSE_0(int word1) {
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
    private Instr decode_28(int word1) {
        // get value of bits logical[4:5]
        int value = (word1 >> 10) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_SUB_0(word1);
            case 0x00001: return decode_CP_0(word1);
            case 0x00003: return decode_ADC_0(word1);
            case 0x00000: return decode_CPSE_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_CPI_0(int word1) {
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
    private Instr decode_SBC_0(int word1) {
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
    private Instr decode_CPC_0(int word1) {
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
    private Instr decode_ADD_0(int word1) {
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
    private Instr decode_MULS_0(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:7] -> 
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.MULS(pc, getReg(HGPR_table, rd), getReg(HGPR_table, rr));
    }
    private Instr decode_MOVW_0(int word1) {
        int rd = 0;
        int rr = 0;
        // logical[0:7] -> 
        // logical[8:11] -> rd[3:0]
        rd |= ((word1 >> 4) & 0x0000F);
        // logical[12:15] -> rr[3:0]
        rr |= (word1 & 0x0000F);
        return new Instr.MOVW(pc, getReg(EGPR_table, rd), getReg(EGPR_table, rr));
    }
    private Instr decode_FMULSU_0(int word1) {
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
    private Instr decode_FMULS_0(int word1) {
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
    private Instr decode_29(int word1) {
        // get value of bits logical[12:12]
        int value = (word1 >> 3) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_FMULSU_0(word1);
            case 0x00000: return decode_FMULS_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_FMUL_0(int word1) {
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
    private Instr decode_MULSU_0(int word1) {
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
    private Instr decode_30(int word1) {
        // get value of bits logical[12:12]
        int value = (word1 >> 3) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_FMUL_0(word1);
            case 0x00000: return decode_MULSU_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_31(int word1) {
        // get value of bits logical[8:8]
        int value = (word1 >> 7) & 0x00001;
        switch ( value ) {
            case 0x00001: return decode_29(word1);
            case 0x00000: return decode_30(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_NOP_0(int word1) {
        if ( (word1 & 0x000FF) != 0x00000 ) {
            throw Avrora.failure("INVALID INSTRUCTION");
        }
        // logical[0:7] -> 
        // logical[8:15] -> 
        return new Instr.NOP(pc);
    }
    private Instr decode_32(int word1) {
        // get value of bits logical[6:7]
        int value = (word1 >> 8) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_MULS_0(word1);
            case 0x00001: return decode_MOVW_0(word1);
            case 0x00003: return decode_31(word1);
            case 0x00000: return decode_NOP_0(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_33(int word1) {
        // get value of bits logical[4:5]
        int value = (word1 >> 10) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_SBC_0(word1);
            case 0x00001: return decode_CPC_0(word1);
            case 0x00003: return decode_ADD_0(word1);
            case 0x00000: return decode_32(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_34(int word1) {
        // get value of bits logical[2:3]
        int value = (word1 >> 12) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_27(word1);
            case 0x00001: return decode_28(word1);
            case 0x00003: return decode_CPI_0(word1);
            case 0x00000: return decode_33(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
    private Instr decode_root(int word1) {
        // get value of bits logical[0:1]
        int value = (word1 >> 14) & 0x00003;
        switch ( value ) {
            case 0x00002: return decode_19(word1);
            case 0x00001: return decode_20(word1);
            case 0x00003: return decode_26(word1);
            case 0x00000: return decode_34(word1);
            default:
            throw Avrora.failure("INVALID INSTRUCTION");
        }
    }
//--END DISASSEM GENERATOR--

}