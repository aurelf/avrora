package avrora.sir;

import avrora.sir.Register;
import avrora.Operand;
import avrora.AVRErrorReporter;

import java.util.HashSet;
import java.util.HashMap;

/**
 * @author Ben L. Titzer
 */
public class InstructionSet {

    private static final HashMap instructions = new HashMap();

    static {
//--BEGIN INSTRUCTIONSET GENERATOR--
        instructions.put("adc", Instr.ADC.prototype);
        instructions.put("add", Instr.ADD.prototype);
        instructions.put("adiw", Instr.ADIW.prototype);
        instructions.put("and", Instr.AND.prototype);
        instructions.put("andi", Instr.ANDI.prototype);
        instructions.put("asr", Instr.ASR.prototype);
        instructions.put("bclr", Instr.BCLR.prototype);
        instructions.put("bld", Instr.BLD.prototype);
        instructions.put("brbc", Instr.BRBC.prototype);
        instructions.put("brbs", Instr.BRBS.prototype);
        instructions.put("brcc", Instr.BRCC.prototype);
        instructions.put("brcs", Instr.BRCS.prototype);
        instructions.put("break", Instr.BREAK.prototype);
        instructions.put("breq", Instr.BREQ.prototype);
        instructions.put("brge", Instr.BRGE.prototype);
        instructions.put("brhc", Instr.BRHC.prototype);
        instructions.put("brhs", Instr.BRHS.prototype);
        instructions.put("brid", Instr.BRID.prototype);
        instructions.put("brie", Instr.BRIE.prototype);
        instructions.put("brlo", Instr.BRLO.prototype);
        instructions.put("brlt", Instr.BRLT.prototype);
        instructions.put("brmi", Instr.BRMI.prototype);
        instructions.put("brne", Instr.BRNE.prototype);
        instructions.put("brpl", Instr.BRPL.prototype);
        instructions.put("brsh", Instr.BRSH.prototype);
        instructions.put("brtc", Instr.BRTC.prototype);
        instructions.put("brts", Instr.BRTS.prototype);
        instructions.put("brvc", Instr.BRVC.prototype);
        instructions.put("brvs", Instr.BRVS.prototype);
        instructions.put("bset", Instr.BSET.prototype);
        instructions.put("bst", Instr.BST.prototype);
        instructions.put("call", Instr.CALL.prototype);
        instructions.put("cbi", Instr.CBI.prototype);
        instructions.put("cbr", Instr.CBR.prototype);
        instructions.put("clc", Instr.CLC.prototype);
        instructions.put("clh", Instr.CLH.prototype);
        instructions.put("cli", Instr.CLI.prototype);
        instructions.put("cln", Instr.CLN.prototype);
        instructions.put("clr", Instr.CLR.prototype);
        instructions.put("cls", Instr.CLS.prototype);
        instructions.put("clt", Instr.CLT.prototype);
        instructions.put("clv", Instr.CLV.prototype);
        instructions.put("clz", Instr.CLZ.prototype);
        instructions.put("com", Instr.COM.prototype);
        instructions.put("cp", Instr.CP.prototype);
        instructions.put("cpc", Instr.CPC.prototype);
        instructions.put("cpi", Instr.CPI.prototype);
        instructions.put("cpse", Instr.CPSE.prototype);
        instructions.put("dec", Instr.DEC.prototype);
        instructions.put("eicall", Instr.EICALL.prototype);
        instructions.put("eijmp", Instr.EIJMP.prototype);
        instructions.put("elpm", Instr.ELPM.prototype);
        instructions.put("elpmd", Instr.ELPMD.prototype);
        instructions.put("elpmpi", Instr.ELPMPI.prototype);
        instructions.put("eor", Instr.EOR.prototype);
        instructions.put("fmul", Instr.FMUL.prototype);
        instructions.put("fmuls", Instr.FMULS.prototype);
        instructions.put("fmulsu", Instr.FMULSU.prototype);
        instructions.put("icall", Instr.ICALL.prototype);
        instructions.put("ijmp", Instr.IJMP.prototype);
        instructions.put("in", Instr.IN.prototype);
        instructions.put("inc", Instr.INC.prototype);
        instructions.put("jmp", Instr.JMP.prototype);
        instructions.put("ld", Instr.LD.prototype);
        instructions.put("ldd", Instr.LDD.prototype);
        instructions.put("ldi", Instr.LDI.prototype);
        instructions.put("ldpd", Instr.LDPD.prototype);
        instructions.put("ldpi", Instr.LDPI.prototype);
        instructions.put("lds", Instr.LDS.prototype);
        instructions.put("lpm", Instr.LPM.prototype);
        instructions.put("lpmd", Instr.LPMD.prototype);
        instructions.put("lpmpi", Instr.LPMPI.prototype);
        instructions.put("lsl", Instr.LSL.prototype);
        instructions.put("lsr", Instr.LSR.prototype);
        instructions.put("mov", Instr.MOV.prototype);
        instructions.put("movw", Instr.MOVW.prototype);
        instructions.put("mul", Instr.MUL.prototype);
        instructions.put("muls", Instr.MULS.prototype);
        instructions.put("mulsu", Instr.MULSU.prototype);
        instructions.put("neg", Instr.NEG.prototype);
        instructions.put("nop", Instr.NOP.prototype);
        instructions.put("or", Instr.OR.prototype);
        instructions.put("ori", Instr.ORI.prototype);
        instructions.put("out", Instr.OUT.prototype);
        instructions.put("pop", Instr.POP.prototype);
        instructions.put("push", Instr.PUSH.prototype);
        instructions.put("rcall", Instr.RCALL.prototype);
        instructions.put("ret", Instr.RET.prototype);
        instructions.put("reti", Instr.RETI.prototype);
        instructions.put("rjmp", Instr.RJMP.prototype);
        instructions.put("rol", Instr.ROL.prototype);
        instructions.put("ror", Instr.ROR.prototype);
        instructions.put("sbc", Instr.SBC.prototype);
        instructions.put("sbci", Instr.SBCI.prototype);
        instructions.put("sbi", Instr.SBI.prototype);
        instructions.put("sbic", Instr.SBIC.prototype);
        instructions.put("sbis", Instr.SBIS.prototype);
        instructions.put("sbiw", Instr.SBIW.prototype);
        instructions.put("sbr", Instr.SBR.prototype);
        instructions.put("sbrc", Instr.SBRC.prototype);
        instructions.put("sbrs", Instr.SBRS.prototype);
        instructions.put("sec", Instr.SEC.prototype);
        instructions.put("seh", Instr.SEH.prototype);
        instructions.put("sei", Instr.SEI.prototype);
        instructions.put("sen", Instr.SEN.prototype);
        instructions.put("ser", Instr.SER.prototype);
        instructions.put("ses", Instr.SES.prototype);
        instructions.put("set", Instr.SET.prototype);
        instructions.put("sev", Instr.SEV.prototype);
        instructions.put("sez", Instr.SEZ.prototype);
        instructions.put("sleep", Instr.SLEEP.prototype);
        instructions.put("spm", Instr.SPM.prototype);
        instructions.put("st", Instr.ST.prototype);
        instructions.put("std", Instr.STD.prototype);
        instructions.put("stpd", Instr.STPD.prototype);
        instructions.put("stpi", Instr.STPI.prototype);
        instructions.put("sts", Instr.STS.prototype);
        instructions.put("sub", Instr.SUB.prototype);
        instructions.put("subi", Instr.SUBI.prototype);
        instructions.put("swap", Instr.SWAP.prototype);
        instructions.put("tst", Instr.TST.prototype);
        instructions.put("wdr", Instr.WDR.prototype);
//--END INSTRUCTIONSET GENERATOR--
    }

    public static InstrPrototype getPrototype(String name) {
        return (InstrPrototype) instructions.get(name.toLowerCase());
    }

}
