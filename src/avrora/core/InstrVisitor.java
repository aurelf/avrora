package vpc.mach.avr.sir;

/**
 * @author Ben L. Titzer
 */
public interface InstrVisitor {
//--BEGIN INSTRVISITOR GENERATOR--
    public void visit(Instr.ADC i);

    public void visit(Instr.ADD i);

    public void visit(Instr.ADIW i);

    public void visit(Instr.AND i);

    public void visit(Instr.ANDI i);

    public void visit(Instr.ASR i);

    public void visit(Instr.BCLR i);

    public void visit(Instr.BLD i);

    public void visit(Instr.BRBC i);

    public void visit(Instr.BRBS i);

    public void visit(Instr.BRCC i);

    public void visit(Instr.BRCS i);

    public void visit(Instr.BREAK i);

    public void visit(Instr.BREQ i);

    public void visit(Instr.BRGE i);

    public void visit(Instr.BRHC i);

    public void visit(Instr.BRHS i);

    public void visit(Instr.BRID i);

    public void visit(Instr.BRIE i);

    public void visit(Instr.BRLO i);

    public void visit(Instr.BRLT i);

    public void visit(Instr.BRMI i);

    public void visit(Instr.BRNE i);

    public void visit(Instr.BRPL i);

    public void visit(Instr.BRSH i);

    public void visit(Instr.BRTC i);

    public void visit(Instr.BRTS i);

    public void visit(Instr.BRVC i);

    public void visit(Instr.BRVS i);

    public void visit(Instr.BSET i);

    public void visit(Instr.BST i);

    public void visit(Instr.CALL i);

    public void visit(Instr.CBI i);

    public void visit(Instr.CBR i);

    public void visit(Instr.CLC i);

    public void visit(Instr.CLH i);

    public void visit(Instr.CLI i);

    public void visit(Instr.CLN i);

    public void visit(Instr.CLR i);

    public void visit(Instr.CLS i);

    public void visit(Instr.CLT i);

    public void visit(Instr.CLV i);

    public void visit(Instr.CLZ i);

    public void visit(Instr.COM i);

    public void visit(Instr.CP i);

    public void visit(Instr.CPC i);

    public void visit(Instr.CPI i);

    public void visit(Instr.CPSE i);

    public void visit(Instr.DEC i);

    public void visit(Instr.EICALL i);

    public void visit(Instr.EIJMP i);

    public void visit(Instr.ELPM i);

    public void visit(Instr.ELPMD i);

    public void visit(Instr.ELPMPI i);

    public void visit(Instr.EOR i);

    public void visit(Instr.FMUL i);

    public void visit(Instr.FMULS i);

    public void visit(Instr.FMULSU i);

    public void visit(Instr.ICALL i);

    public void visit(Instr.IJMP i);

    public void visit(Instr.IN i);

    public void visit(Instr.INC i);

    public void visit(Instr.JMP i);

    public void visit(Instr.LD i);

    public void visit(Instr.LDD i);

    public void visit(Instr.LDI i);

    public void visit(Instr.LDPD i);

    public void visit(Instr.LDPI i);

    public void visit(Instr.LDS i);

    public void visit(Instr.LPM i);

    public void visit(Instr.LPMD i);

    public void visit(Instr.LPMPI i);

    public void visit(Instr.LSL i);

    public void visit(Instr.LSR i);

    public void visit(Instr.MOV i);

    public void visit(Instr.MOVW i);

    public void visit(Instr.MUL i);

    public void visit(Instr.MULS i);

    public void visit(Instr.MULSU i);

    public void visit(Instr.NEG i);

    public void visit(Instr.NOP i);

    public void visit(Instr.OR i);

    public void visit(Instr.ORI i);

    public void visit(Instr.OUT i);

    public void visit(Instr.POP i);

    public void visit(Instr.PUSH i);

    public void visit(Instr.RCALL i);

    public void visit(Instr.RET i);

    public void visit(Instr.RETI i);

    public void visit(Instr.RJMP i);

    public void visit(Instr.ROL i);

    public void visit(Instr.ROR i);

    public void visit(Instr.SBC i);

    public void visit(Instr.SBCI i);

    public void visit(Instr.SBI i);

    public void visit(Instr.SBIC i);

    public void visit(Instr.SBIS i);

    public void visit(Instr.SBIW i);

    public void visit(Instr.SBR i);

    public void visit(Instr.SBRC i);

    public void visit(Instr.SBRS i);

    public void visit(Instr.SEC i);

    public void visit(Instr.SEH i);

    public void visit(Instr.SEI i);

    public void visit(Instr.SEN i);

    public void visit(Instr.SER i);

    public void visit(Instr.SES i);

    public void visit(Instr.SET i);

    public void visit(Instr.SEV i);

    public void visit(Instr.SEZ i);

    public void visit(Instr.SLEEP i);

    public void visit(Instr.SPM i);

    public void visit(Instr.ST i);

    public void visit(Instr.STD i);

    public void visit(Instr.STPD i);

    public void visit(Instr.STPI i);

    public void visit(Instr.STS i);

    public void visit(Instr.SUB i);

    public void visit(Instr.SUBI i);

    public void visit(Instr.SWAP i);

    public void visit(Instr.TST i);

    public void visit(Instr.WDR i);
//--END INSTRVISITOR GENERATOR--
}
