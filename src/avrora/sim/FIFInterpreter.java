package avrora.sim;

import avrora.core.Instr;
import avrora.core.Program;
import avrora.core.InstrVisitor;
import avrora.Avrora;
import avrora.util.Arithmetic;

/**
 * @author Ben L. Titzer
 */
public class FIFInterpreter extends BaseInterpreter {

    protected static class FIFInstr {

        public final int pc;
        public final int opcode;
        public final Instr instr;
        public int r1;
        public int r2;
        public int imm1;
        public int imm2;

        private boolean breakPoint;
        private boolean breakFired;

        public Simulator.Probe probe;

        public FIFInstr next;
        public FIFInstr other;

        public FIFInstr(Instr i, int pc_, int inum_) {
            instr = i;
            pc = pc_;
            opcode = inum_;
        }
    }

    protected FIFInstr fifMap[];

    protected FIFInstr curInstr;
    protected FIFInstr nextInstr;

    public FIFInterpreter(Simulator s, Program p, int fs, int is, int ss) {
        super(s, p, fs, is, ss);
        fifMap = new FIFInstr[p.program_end];
        buildFIFMap();
        curInstr = fifMap[0];
    }

    public void insertProbe(Simulator.Probe p) {
        throw Avrora.unimplemented();
    }

    public void insertProbe(Simulator.Probe p, int prog_addr) {
        throw Avrora.unimplemented();
    }

    public void removeProbe(Simulator.Probe p) {
        throw Avrora.unimplemented();
    }

    public void removeProbe(Simulator.Probe p, int prog_addr) {
        throw Avrora.unimplemented();
    }

    public void insertBreakPoint(int addr) {
        throw Avrora.unimplemented();
    }

    public void removeBreakPoint(int addr) {
        throw Avrora.unimplemented();
    }

    protected void runLoop() {

        nextInstr = curInstr;
        cyclesConsumed = 0;

        while (shouldRun) {

            if (justReturnedFromInterrupt) {
                // don't process the interrupt if we just returned from
                // an interrupt handler, because the hardware manual says
                // that at least one instruction is executed after
                // returning from an interrupt.
                justReturnedFromInterrupt = false;
            } else if (I) {

                // check if there are any pending (posted) interrupts
                if (postedInterrupts != 0) {
                    // the lowest set bit is the highest priority posted interrupt
                    int lowestbit = Arithmetic.lowestBit(postedInterrupts);

                    // fire the interrupt (update flag register(s) state)
                    simulator.triggerInterrupt(lowestbit);

                    // store the return address
                    pushPC(nextInstr.pc);

                    // set PC to interrupt handler
                    nextInstr = fifMap[simulator.getInterruptVectorAddress(lowestbit)];
                    curInstr = nextInstr;

                    // disable interrupts
                    I = false;

                    // process any timed events
                    advanceCycles(4);
                    sleeping = false;

                }
            }

            if (sleeping)
                sleepLoop();
            else {
                if (activeProbe.isEmpty())
                    fastLoop();
                else
                    instrumentedLoop();
            }
        }
    }

    private void sleepLoop() {
        innerLoop = true;
        while (innerLoop) {
            long delta = eventQueue.getHeadDelta();
            if (delta <= 0) delta = 1;
            advanceCycles(delta);
        }
    }

    private void fastLoop() {
        innerLoop = true;
        while (innerLoop) {
            // visit the actual instruction (or probe)
            execute(nextInstr);
            curInstr = nextInstr;
            advanceCycles(cyclesConsumed);
        }
    }

    private void instrumentedLoop() {
        innerLoop = true;
        while (innerLoop) {
            // get the current instruction
            int curPC = nextInstr.pc; // at this point pc == nextPC
            Instr i = nextInstr.instr;

            // visit the actual instruction (or probe)
            activeProbe.fireBefore(i, curPC, this);
            execute(nextInstr);
            curInstr = nextInstr;
            advanceCycles(cyclesConsumed);
            activeProbe.fireAfter(i, curPC, this);
        }
    }

    public int getPC() {
        return curInstr.pc;
    }

    private void buildFIFMap() {
        FIFInstr last = null;
        FIFBuilder builder = new FIFBuilder();

        for ( int cntr = 0; cntr < simulator.program.program_end; cntr += 2) {
            Instr i = impression.readInstr(cntr);
            FIFInstr cur = builder.build(cntr, i);
            if ( last != null ) {
                if ( last.pc + last.instr.getSize() == cntr )
                    last.next = cur;
            }
            last = cur;
            fifMap[cntr] = cur;
        }
    }

//--BEGIN FIF GENERATOR--
    public static final int ADC_code = 0;
    public static final int ADD_code = 1;
    public static final int ADIW_code = 2;
    public static final int AND_code = 3;
    public static final int ANDI_code = 4;
    public static final int ASR_code = 5;
    public static final int BCLR_code = 6;
    public static final int BLD_code = 7;
    public static final int BRBC_code = 8;
    public static final int BRBS_code = 9;
    public static final int BRCC_code = 10;
    public static final int BRCS_code = 11;
    public static final int BREAK_code = 12;
    public static final int BREQ_code = 13;
    public static final int BRGE_code = 14;
    public static final int BRHC_code = 15;
    public static final int BRHS_code = 16;
    public static final int BRID_code = 17;
    public static final int BRIE_code = 18;
    public static final int BRLO_code = 19;
    public static final int BRLT_code = 20;
    public static final int BRMI_code = 21;
    public static final int BRNE_code = 22;
    public static final int BRPL_code = 23;
    public static final int BRSH_code = 24;
    public static final int BRTC_code = 25;
    public static final int BRTS_code = 26;
    public static final int BRVC_code = 27;
    public static final int BRVS_code = 28;
    public static final int BSET_code = 29;
    public static final int BST_code = 30;
    public static final int CALL_code = 31;
    public static final int CBI_code = 32;
    public static final int CBR_code = 33;
    public static final int CLC_code = 34;
    public static final int CLH_code = 35;
    public static final int CLI_code = 36;
    public static final int CLN_code = 37;
    public static final int CLR_code = 38;
    public static final int CLS_code = 39;
    public static final int CLT_code = 40;
    public static final int CLV_code = 41;
    public static final int CLZ_code = 42;
    public static final int COM_code = 43;
    public static final int CP_code = 44;
    public static final int CPC_code = 45;
    public static final int CPI_code = 46;
    public static final int CPSE_code = 47;
    public static final int DEC_code = 48;
    public static final int EICALL_code = 49;
    public static final int EIJMP_code = 50;
    public static final int ELPM_code = 51;
    public static final int ELPMD_code = 52;
    public static final int ELPMPI_code = 53;
    public static final int EOR_code = 54;
    public static final int FMUL_code = 55;
    public static final int FMULS_code = 56;
    public static final int FMULSU_code = 57;
    public static final int ICALL_code = 58;
    public static final int IJMP_code = 59;
    public static final int IN_code = 60;
    public static final int INC_code = 61;
    public static final int JMP_code = 62;
    public static final int LD_code = 63;
    public static final int LDD_code = 64;
    public static final int LDI_code = 65;
    public static final int LDPD_code = 66;
    public static final int LDPI_code = 67;
    public static final int LDS_code = 68;
    public static final int LPM_code = 69;
    public static final int LPMD_code = 70;
    public static final int LPMPI_code = 71;
    public static final int LSL_code = 72;
    public static final int LSR_code = 73;
    public static final int MOV_code = 74;
    public static final int MOVW_code = 75;
    public static final int MUL_code = 76;
    public static final int MULS_code = 77;
    public static final int MULSU_code = 78;
    public static final int NEG_code = 79;
    public static final int NOP_code = 80;
    public static final int OR_code = 81;
    public static final int ORI_code = 82;
    public static final int OUT_code = 83;
    public static final int POP_code = 84;
    public static final int PUSH_code = 85;
    public static final int RCALL_code = 86;
    public static final int RET_code = 87;
    public static final int RETI_code = 88;
    public static final int RJMP_code = 89;
    public static final int ROL_code = 90;
    public static final int ROR_code = 91;
    public static final int SBC_code = 92;
    public static final int SBCI_code = 93;
    public static final int SBI_code = 94;
    public static final int SBIC_code = 95;
    public static final int SBIS_code = 96;
    public static final int SBIW_code = 97;
    public static final int SBR_code = 98;
    public static final int SBRC_code = 99;
    public static final int SBRS_code = 100;
    public static final int SEC_code = 101;
    public static final int SEH_code = 102;
    public static final int SEI_code = 103;
    public static final int SEN_code = 104;
    public static final int SER_code = 105;
    public static final int SES_code = 106;
    public static final int SET_code = 107;
    public static final int SEV_code = 108;
    public static final int SEZ_code = 109;
    public static final int SLEEP_code = 110;
    public static final int SPM_code = 111;
    public static final int ST_code = 112;
    public static final int STD_code = 113;
    public static final int STPD_code = 114;
    public static final int STPI_code = 115;
    public static final int STS_code = 116;
    public static final int SUB_code = 117;
    public static final int SUBI_code = 118;
    public static final int SWAP_code = 119;
    public static final int TST_code = 120;
    public static final int WDR_code = 121;
    protected class FIFBuilder implements InstrVisitor {
        private FIFInstr instr;
        private int pc;
        protected FIFInstr build(int pc, Instr i) {
            this.pc = pc;
            i.accept(this);
            return instr;
        }
        public void visit(Instr.ADC i) {
            instr = new FIFInstr(i, pc, ADC_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.ADD i) {
            instr = new FIFInstr(i, pc, ADD_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.ADIW i) {
            instr = new FIFInstr(i, pc, ADIW_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.AND i) {
            instr = new FIFInstr(i, pc, AND_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.ANDI i) {
            instr = new FIFInstr(i, pc, ANDI_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.ASR i) {
            instr = new FIFInstr(i, pc, ASR_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.BCLR i) {
            instr = new FIFInstr(i, pc, BCLR_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BLD i) {
            instr = new FIFInstr(i, pc, BLD_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRBC i) {
            instr = new FIFInstr(i, pc, BRBC_code);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }
        public void visit(Instr.BRBS i) {
            instr = new FIFInstr(i, pc, BRBS_code);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }
        public void visit(Instr.BRCC i) {
            instr = new FIFInstr(i, pc, BRCC_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRCS i) {
            instr = new FIFInstr(i, pc, BRCS_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BREAK i) {
            instr = new FIFInstr(i, pc, BREAK_code);
        }
        public void visit(Instr.BREQ i) {
            instr = new FIFInstr(i, pc, BREQ_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRGE i) {
            instr = new FIFInstr(i, pc, BRGE_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRHC i) {
            instr = new FIFInstr(i, pc, BRHC_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRHS i) {
            instr = new FIFInstr(i, pc, BRHS_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRID i) {
            instr = new FIFInstr(i, pc, BRID_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRIE i) {
            instr = new FIFInstr(i, pc, BRIE_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRLO i) {
            instr = new FIFInstr(i, pc, BRLO_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRLT i) {
            instr = new FIFInstr(i, pc, BRLT_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRMI i) {
            instr = new FIFInstr(i, pc, BRMI_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRNE i) {
            instr = new FIFInstr(i, pc, BRNE_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRPL i) {
            instr = new FIFInstr(i, pc, BRPL_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRSH i) {
            instr = new FIFInstr(i, pc, BRSH_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRTC i) {
            instr = new FIFInstr(i, pc, BRTC_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRTS i) {
            instr = new FIFInstr(i, pc, BRTS_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRVC i) {
            instr = new FIFInstr(i, pc, BRVC_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BRVS i) {
            instr = new FIFInstr(i, pc, BRVS_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BSET i) {
            instr = new FIFInstr(i, pc, BSET_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.BST i) {
            instr = new FIFInstr(i, pc, BST_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.CALL i) {
            instr = new FIFInstr(i, pc, CALL_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.CBI i) {
            instr = new FIFInstr(i, pc, CBI_code);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }
        public void visit(Instr.CBR i) {
            instr = new FIFInstr(i, pc, CBR_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.CLC i) {
            instr = new FIFInstr(i, pc, CLC_code);
        }
        public void visit(Instr.CLH i) {
            instr = new FIFInstr(i, pc, CLH_code);
        }
        public void visit(Instr.CLI i) {
            instr = new FIFInstr(i, pc, CLI_code);
        }
        public void visit(Instr.CLN i) {
            instr = new FIFInstr(i, pc, CLN_code);
        }
        public void visit(Instr.CLR i) {
            instr = new FIFInstr(i, pc, CLR_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.CLS i) {
            instr = new FIFInstr(i, pc, CLS_code);
        }
        public void visit(Instr.CLT i) {
            instr = new FIFInstr(i, pc, CLT_code);
        }
        public void visit(Instr.CLV i) {
            instr = new FIFInstr(i, pc, CLV_code);
        }
        public void visit(Instr.CLZ i) {
            instr = new FIFInstr(i, pc, CLZ_code);
        }
        public void visit(Instr.COM i) {
            instr = new FIFInstr(i, pc, COM_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.CP i) {
            instr = new FIFInstr(i, pc, CP_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.CPC i) {
            instr = new FIFInstr(i, pc, CPC_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.CPI i) {
            instr = new FIFInstr(i, pc, CPI_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.CPSE i) {
            instr = new FIFInstr(i, pc, CPSE_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.DEC i) {
            instr = new FIFInstr(i, pc, DEC_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.EICALL i) {
            instr = new FIFInstr(i, pc, EICALL_code);
        }
        public void visit(Instr.EIJMP i) {
            instr = new FIFInstr(i, pc, EIJMP_code);
        }
        public void visit(Instr.ELPM i) {
            instr = new FIFInstr(i, pc, ELPM_code);
        }
        public void visit(Instr.ELPMD i) {
            instr = new FIFInstr(i, pc, ELPMD_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.ELPMPI i) {
            instr = new FIFInstr(i, pc, ELPMPI_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.EOR i) {
            instr = new FIFInstr(i, pc, EOR_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.FMUL i) {
            instr = new FIFInstr(i, pc, FMUL_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.FMULS i) {
            instr = new FIFInstr(i, pc, FMULS_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.FMULSU i) {
            instr = new FIFInstr(i, pc, FMULSU_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.ICALL i) {
            instr = new FIFInstr(i, pc, ICALL_code);
        }
        public void visit(Instr.IJMP i) {
            instr = new FIFInstr(i, pc, IJMP_code);
        }
        public void visit(Instr.IN i) {
            instr = new FIFInstr(i, pc, IN_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.INC i) {
            instr = new FIFInstr(i, pc, INC_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.JMP i) {
            instr = new FIFInstr(i, pc, JMP_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.LD i) {
            instr = new FIFInstr(i, pc, LD_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.LDD i) {
            instr = new FIFInstr(i, pc, LDD_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.LDI i) {
            instr = new FIFInstr(i, pc, LDI_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.LDPD i) {
            instr = new FIFInstr(i, pc, LDPD_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.LDPI i) {
            instr = new FIFInstr(i, pc, LDPI_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.LDS i) {
            instr = new FIFInstr(i, pc, LDS_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.LPM i) {
            instr = new FIFInstr(i, pc, LPM_code);
        }
        public void visit(Instr.LPMD i) {
            instr = new FIFInstr(i, pc, LPMD_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.LPMPI i) {
            instr = new FIFInstr(i, pc, LPMPI_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.LSL i) {
            instr = new FIFInstr(i, pc, LSL_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.LSR i) {
            instr = new FIFInstr(i, pc, LSR_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.MOV i) {
            instr = new FIFInstr(i, pc, MOV_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.MOVW i) {
            instr = new FIFInstr(i, pc, MOVW_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.MUL i) {
            instr = new FIFInstr(i, pc, MUL_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.MULS i) {
            instr = new FIFInstr(i, pc, MULS_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.MULSU i) {
            instr = new FIFInstr(i, pc, MULSU_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.NEG i) {
            instr = new FIFInstr(i, pc, NEG_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.NOP i) {
            instr = new FIFInstr(i, pc, NOP_code);
        }
        public void visit(Instr.OR i) {
            instr = new FIFInstr(i, pc, OR_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.ORI i) {
            instr = new FIFInstr(i, pc, ORI_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.OUT i) {
            instr = new FIFInstr(i, pc, OUT_code);
            instr.imm1 = i.imm1;
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.POP i) {
            instr = new FIFInstr(i, pc, POP_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.PUSH i) {
            instr = new FIFInstr(i, pc, PUSH_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.RCALL i) {
            instr = new FIFInstr(i, pc, RCALL_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.RET i) {
            instr = new FIFInstr(i, pc, RET_code);
        }
        public void visit(Instr.RETI i) {
            instr = new FIFInstr(i, pc, RETI_code);
        }
        public void visit(Instr.RJMP i) {
            instr = new FIFInstr(i, pc, RJMP_code);
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.ROL i) {
            instr = new FIFInstr(i, pc, ROL_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.ROR i) {
            instr = new FIFInstr(i, pc, ROR_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.SBC i) {
            instr = new FIFInstr(i, pc, SBC_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.SBCI i) {
            instr = new FIFInstr(i, pc, SBCI_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.SBI i) {
            instr = new FIFInstr(i, pc, SBI_code);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }
        public void visit(Instr.SBIC i) {
            instr = new FIFInstr(i, pc, SBIC_code);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }
        public void visit(Instr.SBIS i) {
            instr = new FIFInstr(i, pc, SBIS_code);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }
        public void visit(Instr.SBIW i) {
            instr = new FIFInstr(i, pc, SBIW_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.SBR i) {
            instr = new FIFInstr(i, pc, SBR_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.SBRC i) {
            instr = new FIFInstr(i, pc, SBRC_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.SBRS i) {
            instr = new FIFInstr(i, pc, SBRS_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.SEC i) {
            instr = new FIFInstr(i, pc, SEC_code);
        }
        public void visit(Instr.SEH i) {
            instr = new FIFInstr(i, pc, SEH_code);
        }
        public void visit(Instr.SEI i) {
            instr = new FIFInstr(i, pc, SEI_code);
        }
        public void visit(Instr.SEN i) {
            instr = new FIFInstr(i, pc, SEN_code);
        }
        public void visit(Instr.SER i) {
            instr = new FIFInstr(i, pc, SER_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.SES i) {
            instr = new FIFInstr(i, pc, SES_code);
        }
        public void visit(Instr.SET i) {
            instr = new FIFInstr(i, pc, SET_code);
        }
        public void visit(Instr.SEV i) {
            instr = new FIFInstr(i, pc, SEV_code);
        }
        public void visit(Instr.SEZ i) {
            instr = new FIFInstr(i, pc, SEZ_code);
        }
        public void visit(Instr.SLEEP i) {
            instr = new FIFInstr(i, pc, SLEEP_code);
        }
        public void visit(Instr.SPM i) {
            instr = new FIFInstr(i, pc, SPM_code);
        }
        public void visit(Instr.ST i) {
            instr = new FIFInstr(i, pc, ST_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.STD i) {
            instr = new FIFInstr(i, pc, STD_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.STPD i) {
            instr = new FIFInstr(i, pc, STPD_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.STPI i) {
            instr = new FIFInstr(i, pc, STPI_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.STS i) {
            instr = new FIFInstr(i, pc, STS_code);
            instr.imm1 = i.imm1;
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.SUB i) {
            instr = new FIFInstr(i, pc, SUB_code);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }
        public void visit(Instr.SUBI i) {
            instr = new FIFInstr(i, pc, SUBI_code);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }
        public void visit(Instr.SWAP i) {
            instr = new FIFInstr(i, pc, SWAP_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.TST i) {
            instr = new FIFInstr(i, pc, TST_code);
            instr.r1 = i.r1.getNumber();
        }
        public void visit(Instr.WDR i) {
            instr = new FIFInstr(i, pc, WDR_code);
        }
    }
    protected void execute(FIFInstr i) {
        nextInstr = nextInstr.next;
        switch ( i.opcode )  {
            case ADC_code:  {
                int tmp_0 = getRegisterUnsigned(i.r1);
                int tmp_1 = getRegisterUnsigned(i.r2);
                int tmp_2 = bit(C);
                int tmp_3 = tmp_0 + tmp_1 + tmp_2;
                int tmp_4 = (tmp_0 & 0x0000000F);
                int tmp_5 = (tmp_1 & 0x0000000F);
                boolean tmp_6 = ((tmp_0 & 128) != 0);
                boolean tmp_7 = ((tmp_1 & 128) != 0);
                boolean tmp_8 = ((tmp_3 & 128) != 0);
                H = ((tmp_4 + tmp_5 + tmp_2 & 16) != 0);
                C = ((tmp_3 & 256) != 0);
                N = ((tmp_3 & 128) != 0);
                Z = low(tmp_3) == 0;
                V = tmp_6 && tmp_7 && !tmp_8 || !tmp_6 && !tmp_7 && tmp_8;
                S = xor(N, V);
                byte tmp_9 = low(tmp_3);
                setRegisterByte(i.r1, tmp_9);
                cyclesConsumed += 1;
                break;
            }
            case ADD_code:  {
                int tmp_0 = getRegisterUnsigned(i.r1);
                int tmp_1 = getRegisterUnsigned(i.r2);
                int tmp_2 = 0;
                int tmp_3 = tmp_0 + tmp_1 + tmp_2;
                int tmp_4 = (tmp_0 & 0x0000000F);
                int tmp_5 = (tmp_1 & 0x0000000F);
                boolean tmp_6 = ((tmp_0 & 128) != 0);
                boolean tmp_7 = ((tmp_1 & 128) != 0);
                boolean tmp_8 = ((tmp_3 & 128) != 0);
                H = ((tmp_4 + tmp_5 + tmp_2 & 16) != 0);
                C = ((tmp_3 & 256) != 0);
                N = ((tmp_3 & 128) != 0);
                Z = low(tmp_3) == 0;
                V = tmp_6 && tmp_7 && !tmp_8 || !tmp_6 && !tmp_7 && tmp_8;
                S = xor(N, V);
                byte tmp_9 = low(tmp_3);
                setRegisterByte(i.r1, tmp_9);
                cyclesConsumed += 1;
                break;
            }
            case ADIW_code:  {
                int tmp_0 = getRegisterWord(i.r1);
                int tmp_1 = tmp_0 + i.imm1;
                boolean tmp_2 = ((tmp_1 & 32768) != 0);
                boolean tmp_3 = ((tmp_0 & 32768) != 0);
                C = !tmp_2 && tmp_3;
                N = tmp_2;
                V = !tmp_3 && tmp_2;
                Z = (tmp_1 & 0x0000FFFF) == 0;
                S = xor(N, V);
                setRegisterWord(i.r1, tmp_1);
                cyclesConsumed += 2;
                break;
            }
            case AND_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = getRegisterByte(i.r2);
                int tmp_2 = tmp_0 & tmp_1;
                N = ((tmp_2 & 128) != 0);
                Z = low(tmp_2) == 0;
                V = false;
                S = xor(N, V);
                byte tmp_3 = low(tmp_2);
                setRegisterByte(i.r1, tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case ANDI_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = i.imm1;
                int tmp_2 = tmp_0 & tmp_1;
                N = ((tmp_2 & 128) != 0);
                Z = low(tmp_2) == 0;
                V = false;
                S = xor(N, V);
                byte tmp_3 = low(tmp_2);
                setRegisterByte(i.r1, tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case ASR_code:  {
                byte tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = tmp_0;
                boolean tmp_2 = ((tmp_0 & 128) != 0);
                int tmp_3 = (tmp_1 & 255) >> 1;
                tmp_3 = Arithmetic.setBit(tmp_3, 7, tmp_2);
                C = ((tmp_1 & 1) != 0);
                N = tmp_2;
                Z = low(tmp_3) == 0;
                V = xor(N, C);
                S = xor(N, V);
                byte tmp_4 = low(tmp_3);
                setRegisterByte(i.r1, tmp_4);
                cyclesConsumed += 1;
                break;
            }
            case BCLR_code:  {
                getIOReg(SREG).writeBit(i.imm1, false);
                cyclesConsumed += 1;
                break;
            }
            case BLD_code:  {
                setRegisterByte(i.r1, Arithmetic.setBit(getRegisterByte(i.r1), i.imm1, T));
                cyclesConsumed += 1;
                break;
            }
            case BRBC_code:  {
                if ( !getIOReg(SREG).readBit(i.imm1) ) {
                    int tmp_0 = i.imm2;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRBS_code:  {
                if ( getIOReg(SREG).readBit(i.imm1) ) {
                    int tmp_0 = i.imm2;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRCC_code:  {
                if ( !C ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRCS_code:  {
                if ( C ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BREAK_code:  {
                stop();
                cyclesConsumed += 1;
                break;
            }
            case BREQ_code:  {
                if ( Z ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRGE_code:  {
                if ( !S ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRHC_code:  {
                if ( !H ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRHS_code:  {
                if ( H ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRID_code:  {
                if ( !I ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRIE_code:  {
                if ( I ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRLO_code:  {
                if ( C ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRLT_code:  {
                if ( S ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRMI_code:  {
                if ( N ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRNE_code:  {
                if ( !Z ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRPL_code:  {
                if ( !N ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRSH_code:  {
                if ( !C ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRTC_code:  {
                if ( !T ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRTS_code:  {
                if ( T ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRVC_code:  {
                if ( !V ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BRVS_code:  {
                if ( V ) {
                    int tmp_0 = i.imm1;
                    int tmp_1 = tmp_0;
                    int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                    nextInstr = fifMap[tmp_2];
                    cyclesConsumed = cyclesConsumed + 1;
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case BSET_code:  {
                getIOReg(SREG).writeBit(i.imm1, true);
                cyclesConsumed += 1;
                break;
            }
            case BST_code:  {
                T = Arithmetic.getBit(getRegisterByte(i.r1), i.imm1);
                cyclesConsumed += 1;
                break;
            }
            case CALL_code:  {
                int tmp_0 = nextInstr.pc;
                tmp_0 = tmp_0 / 2;
                pushByte(low(tmp_0));
                pushByte(high(tmp_0));
                int tmp_1 = i.imm1;
                int tmp_2 = tmp_1 * 2;
                nextInstr = fifMap[tmp_2];
                cyclesConsumed += 4;
                break;
            }
            case CBI_code:  {
                getIOReg(i.imm1).writeBit(i.imm2, false);
                cyclesConsumed += 2;
                break;
            }
            case CBR_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = ~i.imm1;
                int tmp_2 = tmp_0 & tmp_1;
                N = ((tmp_2 & 128) != 0);
                Z = low(tmp_2) == 0;
                V = false;
                S = xor(N, V);
                byte tmp_3 = low(tmp_2);
                setRegisterByte(i.r1, tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case CLC_code:  {
                C = false;
                cyclesConsumed += 1;
                break;
            }
            case CLH_code:  {
                H = false;
                cyclesConsumed += 1;
                break;
            }
            case CLI_code:  {
                disableInterrupts();
                cyclesConsumed += 1;
                break;
            }
            case CLN_code:  {
                N = false;
                cyclesConsumed += 1;
                break;
            }
            case CLR_code:  {
                S = false;
                V = false;
                N = false;
                Z = true;
                setRegisterByte(i.r1, low(0));
                cyclesConsumed += 1;
                break;
            }
            case CLS_code:  {
                S = false;
                cyclesConsumed += 1;
                break;
            }
            case CLT_code:  {
                T = false;
                cyclesConsumed += 1;
                break;
            }
            case CLV_code:  {
                V = false;
                cyclesConsumed += 1;
                break;
            }
            case CLZ_code:  {
                Z = false;
                cyclesConsumed += 1;
                break;
            }
            case COM_code:  {
                int tmp_0 = 255 - getRegisterByte(i.r1);
                C = true;
                N = ((tmp_0 & 128) != 0);
                Z = low(tmp_0) == 0;
                V = false;
                S = xor(N, V);
                setRegisterByte(i.r1, low(tmp_0));
                cyclesConsumed += 1;
                break;
            }
            case CP_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = getRegisterByte(i.r2);
                int tmp_2 = 0;
                int tmp_3 = tmp_0 - tmp_1 - tmp_2;
                boolean tmp_4 = ((tmp_0 & 128) != 0);
                boolean tmp_5 = ((tmp_1 & 128) != 0);
                boolean tmp_6 = ((tmp_3 & 128) != 0);
                boolean tmp_7 = ((tmp_0 & 8) != 0);
                boolean tmp_8 = ((tmp_1 & 8) != 0);
                boolean tmp_9 = ((tmp_3 & 8) != 0);
                H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
                C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
                N = tmp_6;
                Z = low(tmp_3) == 0;
                V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
                S = xor(N, V);
                byte tmp_10 = low(tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case CPC_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = getRegisterByte(i.r2);
                int tmp_2 = bit(C);
                int tmp_3 = tmp_0 - tmp_1 - tmp_2;
                boolean tmp_4 = ((tmp_0 & 128) != 0);
                boolean tmp_5 = ((tmp_1 & 128) != 0);
                boolean tmp_6 = ((tmp_3 & 128) != 0);
                boolean tmp_7 = ((tmp_0 & 8) != 0);
                boolean tmp_8 = ((tmp_1 & 8) != 0);
                boolean tmp_9 = ((tmp_3 & 8) != 0);
                H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
                C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
                N = tmp_6;
                Z = low(tmp_3) == 0 && Z;
                V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
                S = xor(N, V);
                byte tmp_10 = low(tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case CPI_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = i.imm1;
                int tmp_2 = 0;
                int tmp_3 = tmp_0 - tmp_1 - tmp_2;
                boolean tmp_4 = ((tmp_0 & 128) != 0);
                boolean tmp_5 = ((tmp_1 & 128) != 0);
                boolean tmp_6 = ((tmp_3 & 128) != 0);
                boolean tmp_7 = ((tmp_0 & 8) != 0);
                boolean tmp_8 = ((tmp_1 & 8) != 0);
                boolean tmp_9 = ((tmp_3 & 8) != 0);
                H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
                C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
                N = tmp_6;
                Z = low(tmp_3) == 0;
                V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
                S = xor(N, V);
                byte tmp_10 = low(tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case CPSE_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = getRegisterByte(i.r2);
                int tmp_2 = tmp_0;
                int tmp_3 = tmp_1;
                int tmp_4 = 0;
                int tmp_5 = tmp_2 - tmp_3 - tmp_4;
                boolean tmp_6 = ((tmp_2 & 128) != 0);
                boolean tmp_7 = ((tmp_3 & 128) != 0);
                boolean tmp_8 = ((tmp_5 & 128) != 0);
                boolean tmp_9 = ((tmp_2 & 8) != 0);
                boolean tmp_10 = ((tmp_3 & 8) != 0);
                boolean tmp_11 = ((tmp_5 & 8) != 0);
                H = !tmp_9 && tmp_10 || tmp_10 && tmp_11 || tmp_11 && !tmp_9;
                C = !tmp_6 && tmp_7 || tmp_7 && tmp_8 || tmp_8 && !tmp_6;
                N = tmp_8;
                Z = low(tmp_5) == 0;
                V = tmp_6 && !tmp_7 && !tmp_8 || !tmp_6 && tmp_7 && tmp_8;
                S = xor(N, V);
                byte tmp_12 = low(tmp_5);
                if ( tmp_0 == tmp_1 ) {
                    int tmp_13 = getInstrSize(nextInstr.pc);
                    nextInstr = fifMap[nextInstr.pc + tmp_13];
                    if ( tmp_13 == 4 ) {
                        cyclesConsumed = cyclesConsumed + 2;
                    }
                    else {
                        cyclesConsumed = cyclesConsumed + 1;
                    }
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case DEC_code:  {
                int tmp_0 = getRegisterUnsigned(i.r1);
                byte tmp_1 = low(tmp_0 - 1);
                N = ((tmp_1 & 128) != 0);
                Z = tmp_1 == 0;
                V = tmp_0 == 128;
                S = xor(N, V);
                setRegisterByte(i.r1, tmp_1);
                cyclesConsumed += 1;
                break;
            }
            case EICALL_code:  {
                cyclesConsumed += 4;
                break;
            }
            case EIJMP_code:  {
                cyclesConsumed += 2;
                break;
            }
            case ELPM_code:  {
                int tmp_0 = getRegisterWord(RZ);
                tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(RAMPZ) & 0x000000FF) << 16);
                setRegisterByte(R0, getProgramByte(tmp_0));
                cyclesConsumed += 3;
                break;
            }
            case ELPMD_code:  {
                int tmp_0 = getRegisterWord(RZ);
                tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(RAMPZ) & 0x000000FF) << 16);
                setRegisterByte(i.r1, getProgramByte(tmp_0));
                cyclesConsumed += 3;
                break;
            }
            case ELPMPI_code:  {
                int tmp_0 = getRegisterWord(RZ);
                tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(RAMPZ) & 0x000000FF) << 16);
                setRegisterByte(i.r1, getProgramByte(tmp_0));
                setRegisterWord(RZ, tmp_0 + 1);
                cyclesConsumed += 3;
                break;
            }
            case EOR_code:  {
                byte tmp_0 = low(getRegisterByte(i.r1) ^ getRegisterByte(i.r2));
                N = ((tmp_0 & 128) != 0);
                Z = tmp_0 == 0;
                V = false;
                S = xor(N, V);
                setRegisterByte(i.r1, tmp_0);
                cyclesConsumed += 1;
                break;
            }
            case FMUL_code:  {
                int tmp_0 = getRegisterUnsigned(i.r1) * getRegisterUnsigned(i.r2) << 1;
                Z = (tmp_0 & 0x0000FFFF) == 0;
                C = ((tmp_0 & 65536) != 0);
                setRegisterByte(R0, low(tmp_0));
                setRegisterByte(R1, high(tmp_0));
                cyclesConsumed += 2;
                break;
            }
            case FMULS_code:  {
                int tmp_0 = getRegisterByte(i.r1) * getRegisterByte(i.r2) << 1;
                Z = (tmp_0 & 0x0000FFFF) == 0;
                C = ((tmp_0 & 65536) != 0);
                setRegisterByte(R0, low(tmp_0));
                setRegisterByte(R1, high(tmp_0));
                cyclesConsumed += 2;
                break;
            }
            case FMULSU_code:  {
                int tmp_0 = getRegisterByte(i.r1) * getRegisterUnsigned(i.r2) << 1;
                Z = (tmp_0 & 0x0000FFFF) == 0;
                C = ((tmp_0 & 65536) != 0);
                setRegisterByte(R0, low(tmp_0));
                setRegisterByte(R1, high(tmp_0));
                cyclesConsumed += 2;
                break;
            }
            case ICALL_code:  {
                int tmp_0 = nextInstr.pc;
                tmp_0 = tmp_0 / 2;
                pushByte(low(tmp_0));
                pushByte(high(tmp_0));
                int tmp_1 = getRegisterWord(RZ);
                int tmp_2 = tmp_1 * 2;
                nextInstr = fifMap[tmp_2];
                cyclesConsumed += 3;
                break;
            }
            case IJMP_code:  {
                int tmp_0 = getRegisterWord(RZ);
                int tmp_1 = tmp_0 * 2;
                nextInstr = fifMap[tmp_1];
                cyclesConsumed += 2;
                break;
            }
            case IN_code:  {
                setRegisterByte(i.r1, getIORegisterByte(i.imm1));
                cyclesConsumed += 1;
                break;
            }
            case INC_code:  {
                int tmp_0 = getRegisterUnsigned(i.r1);
                byte tmp_1 = low(tmp_0 + 1);
                N = ((tmp_1 & 128) != 0);
                Z = tmp_1 == 0;
                V = tmp_0 == 127;
                S = xor(N, V);
                setRegisterByte(i.r1, tmp_1);
                cyclesConsumed += 1;
                break;
            }
            case JMP_code:  {
                int tmp_0 = i.imm1;
                int tmp_1 = tmp_0 * 2;
                nextInstr = fifMap[tmp_1];
                cyclesConsumed += 3;
                break;
            }
            case LD_code:  {
                setRegisterByte(i.r1, getDataByte(getRegisterWord(i.r2)));
                cyclesConsumed += 2;
                break;
            }
            case LDD_code:  {
                setRegisterByte(i.r1, getDataByte(getRegisterWord(i.r2) + i.imm1));
                cyclesConsumed += 2;
                break;
            }
            case LDI_code:  {
                setRegisterByte(i.r1, low(i.imm1));
                cyclesConsumed += 1;
                break;
            }
            case LDPD_code:  {
                int tmp_0 = getRegisterWord(i.r2) - 1;
                setRegisterByte(i.r1, getDataByte(tmp_0));
                setRegisterWord(i.r2, tmp_0);
                cyclesConsumed += 2;
                break;
            }
            case LDPI_code:  {
                int tmp_0 = getRegisterWord(i.r2);
                setRegisterByte(i.r1, getDataByte(tmp_0));
                setRegisterWord(i.r2, tmp_0 + 1);
                cyclesConsumed += 2;
                break;
            }
            case LDS_code:  {
                setRegisterByte(i.r1, getDataByte(i.imm1));
                cyclesConsumed += 2;
                break;
            }
            case LPM_code:  {
                setRegisterByte(R0, getProgramByte(getRegisterWord(RZ)));
                cyclesConsumed += 3;
                break;
            }
            case LPMD_code:  {
                setRegisterByte(i.r1, getProgramByte(getRegisterWord(RZ)));
                cyclesConsumed += 3;
                break;
            }
            case LPMPI_code:  {
                int tmp_0 = getRegisterWord(RZ);
                setRegisterByte(i.r1, getProgramByte(tmp_0));
                setRegisterWord(RZ, tmp_0 + 1);
                cyclesConsumed += 3;
                break;
            }
            case LSL_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                boolean tmp_1 = false;
                int tmp_2 = tmp_0 << 1;
                tmp_2 = Arithmetic.setBit(tmp_2, 0, tmp_1);
                H = ((tmp_2 & 16) != 0);
                C = ((tmp_2 & 256) != 0);
                N = ((tmp_2 & 128) != 0);
                Z = low(tmp_2) == 0;
                V = xor(N, C);
                S = xor(N, V);
                byte tmp_3 = low(tmp_2);
                setRegisterByte(i.r1, tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case LSR_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                boolean tmp_1 = false;
                int tmp_2 = (tmp_0 & 255) >> 1;
                tmp_2 = Arithmetic.setBit(tmp_2, 7, tmp_1);
                C = ((tmp_0 & 1) != 0);
                N = tmp_1;
                Z = low(tmp_2) == 0;
                V = xor(N, C);
                S = xor(N, V);
                byte tmp_3 = low(tmp_2);
                setRegisterByte(i.r1, tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case MOV_code:  {
                setRegisterByte(i.r1, getRegisterByte(i.r2));
                cyclesConsumed += 1;
                break;
            }
            case MOVW_code:  {
                setRegisterWord(i.r1, getRegisterWord(i.r2));
                cyclesConsumed += 1;
                break;
            }
            case MUL_code:  {
                int tmp_0 = getRegisterUnsigned(i.r1) * getRegisterUnsigned(i.r2);
                C = ((tmp_0 & 32768) != 0);
                Z = (tmp_0 & 0x0000FFFF) == 0;
                setRegisterWord(R0, tmp_0);
                cyclesConsumed += 2;
                break;
            }
            case MULS_code:  {
                int tmp_0 = getRegisterByte(i.r1) * getRegisterByte(i.r2);
                C = ((tmp_0 & 32768) != 0);
                Z = (tmp_0 & 0x0000FFFF) == 0;
                setRegisterWord(R0, tmp_0);
                cyclesConsumed += 2;
                break;
            }
            case MULSU_code:  {
                int tmp_0 = getRegisterByte(i.r1) * getRegisterUnsigned(i.r2);
                C = ((tmp_0 & 32768) != 0);
                Z = (tmp_0 & 0x0000FFFF) == 0;
                setRegisterWord(R0, tmp_0);
                cyclesConsumed += 2;
                break;
            }
            case NEG_code:  {
                int tmp_0 = 0;
                int tmp_1 = getRegisterByte(i.r1);
                int tmp_2 = 0;
                int tmp_3 = tmp_0 - tmp_1 - tmp_2;
                boolean tmp_4 = ((tmp_0 & 128) != 0);
                boolean tmp_5 = ((tmp_1 & 128) != 0);
                boolean tmp_6 = ((tmp_3 & 128) != 0);
                boolean tmp_7 = ((tmp_0 & 8) != 0);
                boolean tmp_8 = ((tmp_1 & 8) != 0);
                boolean tmp_9 = ((tmp_3 & 8) != 0);
                H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
                C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
                N = tmp_6;
                Z = low(tmp_3) == 0;
                V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
                S = xor(N, V);
                byte tmp_10 = low(tmp_3);
                setRegisterByte(i.r1, tmp_10);
                cyclesConsumed += 1;
                break;
            }
            case NOP_code:  {
                cyclesConsumed += 1;
                break;
            }
            case OR_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = getRegisterByte(i.r2);
                int tmp_2 = tmp_0 | tmp_1;
                N = ((tmp_2 & 128) != 0);
                Z = low(tmp_2) == 0;
                V = false;
                S = xor(N, V);
                byte tmp_3 = low(tmp_2);
                setRegisterByte(i.r1, tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case ORI_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = i.imm1;
                int tmp_2 = tmp_0 | tmp_1;
                N = ((tmp_2 & 128) != 0);
                Z = low(tmp_2) == 0;
                V = false;
                S = xor(N, V);
                byte tmp_3 = low(tmp_2);
                setRegisterByte(i.r1, tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case OUT_code:  {
                setIORegisterByte(i.imm1, getRegisterByte(i.r1));
                cyclesConsumed += 1;
                break;
            }
            case POP_code:  {
                setRegisterByte(i.r1, popByte());
                cyclesConsumed += 2;
                break;
            }
            case PUSH_code:  {
                pushByte(getRegisterByte(i.r1));
                cyclesConsumed += 2;
                break;
            }
            case RCALL_code:  {
                int tmp_0 = nextInstr.pc;
                tmp_0 = tmp_0 / 2;
                pushByte(low(tmp_0));
                pushByte(high(tmp_0));
                int tmp_1 = i.imm1;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                cyclesConsumed += 3;
                break;
            }
            case RET_code:  {
                byte tmp_0 = popByte();
                byte tmp_1 = popByte();
                int tmp_2 = uword(tmp_1, tmp_0) * 2;
                nextInstr = fifMap[tmp_2];
                cyclesConsumed += 4;
                break;
            }
            case RETI_code:  {
                byte tmp_0 = popByte();
                byte tmp_1 = popByte();
                int tmp_2 = uword(tmp_1, tmp_0) * 2;
                nextInstr = fifMap[tmp_2];
                enableInterrupts();
                justReturnedFromInterrupt = true;
                cyclesConsumed += 4;
                break;
            }
            case RJMP_code:  {
                int tmp_0 = i.imm1;
                int tmp_1 = tmp_0 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_1];
                cyclesConsumed += 2;
                break;
            }
            case ROL_code:  {
                int tmp_0 = getRegisterUnsigned(i.r1);
                boolean tmp_1 = C;
                int tmp_2 = tmp_0 << 1;
                tmp_2 = Arithmetic.setBit(tmp_2, 0, tmp_1);
                H = ((tmp_2 & 16) != 0);
                C = ((tmp_2 & 256) != 0);
                N = ((tmp_2 & 128) != 0);
                Z = low(tmp_2) == 0;
                V = xor(N, C);
                S = xor(N, V);
                byte tmp_3 = low(tmp_2);
                setRegisterByte(i.r1, tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case ROR_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                boolean tmp_1 = C;
                int tmp_2 = (tmp_0 & 255) >> 1;
                tmp_2 = Arithmetic.setBit(tmp_2, 7, tmp_1);
                C = ((tmp_0 & 1) != 0);
                N = tmp_1;
                Z = low(tmp_2) == 0;
                V = xor(N, C);
                S = xor(N, V);
                byte tmp_3 = low(tmp_2);
                setRegisterByte(i.r1, tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case SBC_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = getRegisterByte(i.r2);
                int tmp_2 = bit(C);
                int tmp_3 = tmp_0 - tmp_1 - tmp_2;
                boolean tmp_4 = ((tmp_0 & 128) != 0);
                boolean tmp_5 = ((tmp_1 & 128) != 0);
                boolean tmp_6 = ((tmp_3 & 128) != 0);
                boolean tmp_7 = ((tmp_0 & 8) != 0);
                boolean tmp_8 = ((tmp_1 & 8) != 0);
                boolean tmp_9 = ((tmp_3 & 8) != 0);
                H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
                C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
                N = tmp_6;
                Z = low(tmp_3) == 0 && Z;
                V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
                S = xor(N, V);
                byte tmp_10 = low(tmp_3);
                setRegisterByte(i.r1, tmp_10);
                cyclesConsumed += 1;
                break;
            }
            case SBCI_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = i.imm1;
                int tmp_2 = bit(C);
                int tmp_3 = tmp_0 - tmp_1 - tmp_2;
                boolean tmp_4 = ((tmp_0 & 128) != 0);
                boolean tmp_5 = ((tmp_1 & 128) != 0);
                boolean tmp_6 = ((tmp_3 & 128) != 0);
                boolean tmp_7 = ((tmp_0 & 8) != 0);
                boolean tmp_8 = ((tmp_1 & 8) != 0);
                boolean tmp_9 = ((tmp_3 & 8) != 0);
                H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
                C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
                N = tmp_6;
                Z = low(tmp_3) == 0 && Z;
                V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
                S = xor(N, V);
                byte tmp_10 = low(tmp_3);
                setRegisterByte(i.r1, tmp_10);
                cyclesConsumed += 1;
                break;
            }
            case SBI_code:  {
                getIOReg(i.imm1).writeBit(i.imm2, true);
                cyclesConsumed += 2;
                break;
            }
            case SBIC_code:  {
                if ( !getIOReg(i.imm1).readBit(i.imm2) ) {
                    int tmp_0 = getInstrSize(nextInstr.pc);
                    nextInstr = fifMap[nextInstr.pc + tmp_0];
                    if ( tmp_0 == 4 ) {
                        cyclesConsumed = cyclesConsumed + 2;
                    }
                    else {
                        cyclesConsumed = cyclesConsumed + 1;
                    }
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case SBIS_code:  {
                if ( getIOReg(i.imm1).readBit(i.imm2) ) {
                    int tmp_0 = getInstrSize(nextInstr.pc);
                    nextInstr = fifMap[nextInstr.pc + tmp_0];
                    if ( tmp_0 == 4 ) {
                        cyclesConsumed = cyclesConsumed + 2;
                    }
                    else {
                        cyclesConsumed = cyclesConsumed + 1;
                    }
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case SBIW_code:  {
                int tmp_0 = getRegisterWord(i.r1);
                int tmp_1 = tmp_0 - i.imm1;
                boolean tmp_2 = ((tmp_0 & 32768) != 0);
                boolean tmp_3 = ((tmp_1 & 32768) != 0);
                V = tmp_2 && !tmp_3;
                N = tmp_3;
                Z = (tmp_1 & 0x0000FFFF) == 0;
                C = tmp_3 && !tmp_2;
                S = xor(N, V);
                setRegisterWord(i.r1, tmp_1);
                cyclesConsumed += 2;
                break;
            }
            case SBR_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = i.imm1;
                int tmp_2 = tmp_0 | tmp_1;
                N = ((tmp_2 & 128) != 0);
                Z = low(tmp_2) == 0;
                V = false;
                S = xor(N, V);
                byte tmp_3 = low(tmp_2);
                setRegisterByte(i.r1, tmp_3);
                cyclesConsumed += 1;
                break;
            }
            case SBRC_code:  {
                if ( !Arithmetic.getBit(getRegisterByte(i.r1), i.imm1) ) {
                    int tmp_0 = getInstrSize(nextInstr.pc);
                    nextInstr = fifMap[nextInstr.pc + tmp_0];
                    if ( tmp_0 == 4 ) {
                        cyclesConsumed = cyclesConsumed + 2;
                    }
                    else {
                        cyclesConsumed = cyclesConsumed + 1;
                    }
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case SBRS_code:  {
                if ( Arithmetic.getBit(getRegisterByte(i.r1), i.imm1) ) {
                    int tmp_0 = getInstrSize(nextInstr.pc);
                    nextInstr = fifMap[nextInstr.pc + tmp_0];
                    if ( tmp_0 == 4 ) {
                        cyclesConsumed = cyclesConsumed + 2;
                    }
                    else {
                        cyclesConsumed = cyclesConsumed + 1;
                    }
                }
                else {
                }
                cyclesConsumed += 1;
                break;
            }
            case SEC_code:  {
                C = true;
                cyclesConsumed += 1;
                break;
            }
            case SEH_code:  {
                H = true;
                cyclesConsumed += 1;
                break;
            }
            case SEI_code:  {
                enableInterrupts();
                cyclesConsumed += 1;
                break;
            }
            case SEN_code:  {
                N = true;
                cyclesConsumed += 1;
                break;
            }
            case SER_code:  {
                setRegisterByte(i.r1, low(255));
                cyclesConsumed += 1;
                break;
            }
            case SES_code:  {
                S = true;
                cyclesConsumed += 1;
                break;
            }
            case SET_code:  {
                T = true;
                cyclesConsumed += 1;
                break;
            }
            case SEV_code:  {
                V = true;
                cyclesConsumed += 1;
                break;
            }
            case SEZ_code:  {
                Z = true;
                cyclesConsumed += 1;
                break;
            }
            case SLEEP_code:  {
                enterSleepMode();
                cyclesConsumed += 1;
                break;
            }
            case SPM_code:  {
                cyclesConsumed += 1;
                break;
            }
            case ST_code:  {
                setDataByte(getRegisterWord(i.r1), getRegisterByte(i.r2));
                cyclesConsumed += 2;
                break;
            }
            case STD_code:  {
                setDataByte(getRegisterWord(i.r1) + i.imm1, getRegisterByte(i.r2));
                cyclesConsumed += 2;
                break;
            }
            case STPD_code:  {
                int tmp_0 = getRegisterWord(i.r1) - 1;
                setDataByte(tmp_0, getRegisterByte(i.r2));
                setRegisterWord(i.r1, tmp_0);
                cyclesConsumed += 2;
                break;
            }
            case STPI_code:  {
                int tmp_0 = getRegisterWord(i.r1);
                setDataByte(tmp_0, getRegisterByte(i.r2));
                setRegisterWord(i.r1, tmp_0 + 1);
                cyclesConsumed += 2;
                break;
            }
            case STS_code:  {
                setDataByte(i.imm1, getRegisterByte(i.r1));
                cyclesConsumed += 2;
                break;
            }
            case SUB_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = getRegisterByte(i.r2);
                int tmp_2 = 0;
                int tmp_3 = tmp_0 - tmp_1 - tmp_2;
                boolean tmp_4 = ((tmp_0 & 128) != 0);
                boolean tmp_5 = ((tmp_1 & 128) != 0);
                boolean tmp_6 = ((tmp_3 & 128) != 0);
                boolean tmp_7 = ((tmp_0 & 8) != 0);
                boolean tmp_8 = ((tmp_1 & 8) != 0);
                boolean tmp_9 = ((tmp_3 & 8) != 0);
                H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
                C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
                N = tmp_6;
                Z = low(tmp_3) == 0;
                V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
                S = xor(N, V);
                byte tmp_10 = low(tmp_3);
                setRegisterByte(i.r1, tmp_10);
                cyclesConsumed += 1;
                break;
            }
            case SUBI_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                int tmp_1 = i.imm1;
                int tmp_2 = 0;
                int tmp_3 = tmp_0 - tmp_1 - tmp_2;
                boolean tmp_4 = ((tmp_0 & 128) != 0);
                boolean tmp_5 = ((tmp_1 & 128) != 0);
                boolean tmp_6 = ((tmp_3 & 128) != 0);
                boolean tmp_7 = ((tmp_0 & 8) != 0);
                boolean tmp_8 = ((tmp_1 & 8) != 0);
                boolean tmp_9 = ((tmp_3 & 8) != 0);
                H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
                C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
                N = tmp_6;
                Z = low(tmp_3) == 0;
                V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
                S = xor(N, V);
                byte tmp_10 = low(tmp_3);
                setRegisterByte(i.r1, tmp_10);
                cyclesConsumed += 1;
                break;
            }
            case SWAP_code:  {
                int tmp_0 = getRegisterUnsigned(i.r1);
                int tmp_1 = 0;
                tmp_1 = (tmp_1 & 0xFFFFFFF0) | ((((tmp_0 >> 4) & 0x0000000F) & 0x0000000F));
                tmp_1 = (tmp_1 & 0xFFFFFF0F) | (((tmp_0 & 0x0000000F) & 0x0000000F) << 4);
                setRegisterByte(i.r1, low(tmp_1));
                cyclesConsumed += 1;
                break;
            }
            case TST_code:  {
                int tmp_0 = getRegisterByte(i.r1);
                V = false;
                Z = low(tmp_0) == 0;
                N = ((tmp_0 & 128) != 0);
                S = xor(N, V);
                cyclesConsumed += 1;
                break;
            }
            case WDR_code:  {
                cyclesConsumed += 1;
                break;
            }
        }
    }
//--END FIF GENERATOR--

    //
    //  U T I L I T I E S
    // ------------------------------------------------------------
    //
    //  These are utility functions for expressing instructions
    //  more concisely. They are private and can be inlined by
    //  the JIT compiler or javac -O.
    //
    //

    public static final int R0 = 0;
    public static final int RZ = 30;
    public static final int R1 = 1;

    private void pushPC(int npc) {
        npc = npc / 2;
        pushByte(Arithmetic.low(npc));
        pushByte(Arithmetic.high(npc));
    }

    private int popPC() {
        byte high = popByte();
        byte low = popByte();
        return Arithmetic.uword(low, high) * 2;
    }

    private boolean xor(boolean a, boolean b) {
        return (a && !b) || (b && !a);
    }

    private byte low(int val) {
        return (byte) val;
    }

    private byte high(int val) {
        return (byte) (val >> 8);
    }

    private byte bit(boolean val) {
        if (val) return 1;
        return 0;
    }

    private int uword(byte low, byte high) {
        return Arithmetic.uword(low, high);
    }

    private void enterSleepMode() {
        sleeping = true;
        innerLoop = false;
    }

    protected int getRegisterUnsigned(int reg) {
        return regs[reg];
    }

    protected byte getRegisterByte(int reg) {
        return regs[reg];
    }

    protected void setRegisterByte(int reg, byte val) {
        regs[reg] = val;
    }

    protected int getRegisterWord(int reg) {
        return Arithmetic.uword(regs[reg], regs[reg+1]);
    }

    protected void setRegisterWord(int reg, int val) {
        regs[reg] = low(val);
        regs[reg+1] = high(val);
    }

}
