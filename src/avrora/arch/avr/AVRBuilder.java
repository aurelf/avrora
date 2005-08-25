package avrora.arch.avr;
import java.util.HashMap;
public class AVRBuilder {
    public static abstract class Single {
        public abstract AVRInstr build(AVROperand[] operands);
    }
    static final HashMap builders = new HashMap();
    static Single addSingle(String name, Single s) {
        builders.put(name, s);
        return s;
    }
    public static final Single ADC  = addSingle("adc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.ADC((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single ADD  = addSingle("add", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.ADD((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single ADIW  = addSingle("adiw", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.ADIW((AVROperand.RDL)operands[0], (AVROperand.IMM6)operands[1]);
        }
    });
    public static final Single AND  = addSingle("and", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.AND((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single ANDI  = addSingle("andi", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.ANDI((AVROperand.HGPR)operands[0], (AVROperand.IMM8)operands[1]);
        }
    });
    public static final Single ASR  = addSingle("asr", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.ASR((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single BCLR  = addSingle("bclr", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BCLR((AVROperand.IMM3)operands[0]);
        }
    });
    public static final Single BLD  = addSingle("bld", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.BLD((AVROperand.GPR)operands[0], (AVROperand.IMM3)operands[1]);
        }
    });
    public static final Single BRBC  = addSingle("brbc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.BRBC((AVROperand.IMM3)operands[0], (AVROperand.SREL)operands[1]);
        }
    });
    public static final Single BRBS  = addSingle("brbs", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.BRBS((AVROperand.IMM3)operands[0], (AVROperand.SREL)operands[1]);
        }
    });
    public static final Single BRCC  = addSingle("brcc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRCC((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRCS  = addSingle("brcs", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRCS((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BREAK  = addSingle("break", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.BREAK();
        }
    });
    public static final Single BREQ  = addSingle("breq", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BREQ((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRGE  = addSingle("brge", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRGE((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRHC  = addSingle("brhc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRHC((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRHS  = addSingle("brhs", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRHS((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRID  = addSingle("brid", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRID((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRIE  = addSingle("brie", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRIE((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRLO  = addSingle("brlo", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRLO((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRLT  = addSingle("brlt", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRLT((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRMI  = addSingle("brmi", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRMI((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRNE  = addSingle("brne", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRNE((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRPL  = addSingle("brpl", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRPL((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRSH  = addSingle("brsh", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRSH((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRTC  = addSingle("brtc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRTC((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRTS  = addSingle("brts", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRTS((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRVC  = addSingle("brvc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRVC((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BRVS  = addSingle("brvs", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BRVS((AVROperand.SREL)operands[0]);
        }
    });
    public static final Single BSET  = addSingle("bset", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.BSET((AVROperand.IMM3)operands[0]);
        }
    });
    public static final Single BST  = addSingle("bst", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.BST((AVROperand.GPR)operands[0], (AVROperand.IMM3)operands[1]);
        }
    });
    public static final Single CALL  = addSingle("call", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.CALL((AVROperand.PADDR)operands[0]);
        }
    });
    public static final Single CBI  = addSingle("cbi", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.CBI((AVROperand.IMM5)operands[0], (AVROperand.IMM3)operands[1]);
        }
    });
    public static final Single CBR  = addSingle("cbr", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.CBR((AVROperand.HGPR)operands[0], (AVROperand.IMM8)operands[1]);
        }
    });
    public static final Single CLC  = addSingle("clc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.CLC();
        }
    });
    public static final Single CLH  = addSingle("clh", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.CLH();
        }
    });
    public static final Single CLI  = addSingle("cli", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.CLI();
        }
    });
    public static final Single CLN  = addSingle("cln", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.CLN();
        }
    });
    public static final Single CLR  = addSingle("clr", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.CLR((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single CLS  = addSingle("cls", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.CLS();
        }
    });
    public static final Single CLT  = addSingle("clt", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.CLT();
        }
    });
    public static final Single CLV  = addSingle("clv", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.CLV();
        }
    });
    public static final Single CLZ  = addSingle("clz", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.CLZ();
        }
    });
    public static final Single COM  = addSingle("com", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.COM((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single CP  = addSingle("cp", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.CP((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single CPC  = addSingle("cpc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.CPC((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single CPI  = addSingle("cpi", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.CPI((AVROperand.HGPR)operands[0], (AVROperand.IMM8)operands[1]);
        }
    });
    public static final Single CPSE  = addSingle("cpse", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.CPSE((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single DEC  = addSingle("dec", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.DEC((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single EICALL  = addSingle("eicall", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.EICALL();
        }
    });
    public static final Single EIJMP  = addSingle("eijmp", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.EIJMP();
        }
    });
    public static final Single EOR  = addSingle("eor", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.EOR((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single FMUL  = addSingle("fmul", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.FMUL((AVROperand.MGPR)operands[0], (AVROperand.MGPR)operands[1]);
        }
    });
    public static final Single FMULS  = addSingle("fmuls", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.FMULS((AVROperand.MGPR)operands[0], (AVROperand.MGPR)operands[1]);
        }
    });
    public static final Single FMULSU  = addSingle("fmulsu", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.FMULSU((AVROperand.MGPR)operands[0], (AVROperand.MGPR)operands[1]);
        }
    });
    public static final Single ICALL  = addSingle("icall", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.ICALL();
        }
    });
    public static final Single IJMP  = addSingle("ijmp", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.IJMP();
        }
    });
    public static final Single IN  = addSingle("in", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.IN((AVROperand.GPR)operands[0], (AVROperand.IMM6)operands[1]);
        }
    });
    public static final Single INC  = addSingle("inc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.INC((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single JMP  = addSingle("jmp", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.JMP((AVROperand.PADDR)operands[0]);
        }
    });
    public static final Single LDD  = addSingle("ldd", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 3;
            return new AVRInstr.LDD((AVROperand.GPR)operands[0], (AVROperand.YZ)operands[1], (AVROperand.IMM6)operands[2]);
        }
    });
    public static final Single LDI  = addSingle("ldi", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.LDI((AVROperand.HGPR)operands[0], (AVROperand.IMM8)operands[1]);
        }
    });
    public static final Single LDS  = addSingle("lds", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.LDS((AVROperand.GPR)operands[0], (AVROperand.DADDR)operands[1]);
        }
    });
    public static final Single LSL  = addSingle("lsl", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.LSL((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single LSR  = addSingle("lsr", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.LSR((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single MOV  = addSingle("mov", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.MOV((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single MOVW  = addSingle("movw", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.MOVW((AVROperand.EGPR)operands[0], (AVROperand.EGPR)operands[1]);
        }
    });
    public static final Single MUL  = addSingle("mul", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.MUL((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single MULS  = addSingle("muls", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.MULS((AVROperand.HGPR)operands[0], (AVROperand.HGPR)operands[1]);
        }
    });
    public static final Single MULSU  = addSingle("mulsu", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.MULSU((AVROperand.MGPR)operands[0], (AVROperand.MGPR)operands[1]);
        }
    });
    public static final Single NEG  = addSingle("neg", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.NEG((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single NOP  = addSingle("nop", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.NOP();
        }
    });
    public static final Single OR  = addSingle("or", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.OR((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single ORI  = addSingle("ori", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.ORI((AVROperand.HGPR)operands[0], (AVROperand.IMM8)operands[1]);
        }
    });
    public static final Single OUT  = addSingle("out", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.OUT((AVROperand.IMM6)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single POP  = addSingle("pop", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.POP((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single PUSH  = addSingle("push", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.PUSH((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single RCALL  = addSingle("rcall", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.RCALL((AVROperand.LREL)operands[0]);
        }
    });
    public static final Single RET  = addSingle("ret", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.RET();
        }
    });
    public static final Single RETI  = addSingle("reti", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.RETI();
        }
    });
    public static final Single RJMP  = addSingle("rjmp", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.RJMP((AVROperand.LREL)operands[0]);
        }
    });
    public static final Single ROL  = addSingle("rol", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.ROL((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single ROR  = addSingle("ror", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.ROR((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single SBC  = addSingle("sbc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SBC((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single SBCI  = addSingle("sbci", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SBCI((AVROperand.HGPR)operands[0], (AVROperand.IMM8)operands[1]);
        }
    });
    public static final Single SBI  = addSingle("sbi", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SBI((AVROperand.IMM5)operands[0], (AVROperand.IMM3)operands[1]);
        }
    });
    public static final Single SBIC  = addSingle("sbic", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SBIC((AVROperand.IMM5)operands[0], (AVROperand.IMM3)operands[1]);
        }
    });
    public static final Single SBIS  = addSingle("sbis", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SBIS((AVROperand.IMM5)operands[0], (AVROperand.IMM3)operands[1]);
        }
    });
    public static final Single SBIW  = addSingle("sbiw", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SBIW((AVROperand.RDL)operands[0], (AVROperand.IMM6)operands[1]);
        }
    });
    public static final Single SBR  = addSingle("sbr", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SBR((AVROperand.HGPR)operands[0], (AVROperand.IMM8)operands[1]);
        }
    });
    public static final Single SBRC  = addSingle("sbrc", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SBRC((AVROperand.GPR)operands[0], (AVROperand.IMM3)operands[1]);
        }
    });
    public static final Single SBRS  = addSingle("sbrs", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SBRS((AVROperand.GPR)operands[0], (AVROperand.IMM3)operands[1]);
        }
    });
    public static final Single SEC  = addSingle("sec", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.SEC();
        }
    });
    public static final Single SEH  = addSingle("seh", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.SEH();
        }
    });
    public static final Single SEI  = addSingle("sei", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.SEI();
        }
    });
    public static final Single SEN  = addSingle("sen", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.SEN();
        }
    });
    public static final Single SER  = addSingle("ser", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.SER((AVROperand.HGPR)operands[0]);
        }
    });
    public static final Single SES  = addSingle("ses", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.SES();
        }
    });
    public static final Single SET  = addSingle("set", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.SET();
        }
    });
    public static final Single SEV  = addSingle("sev", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.SEV();
        }
    });
    public static final Single SEZ  = addSingle("sez", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.SEZ();
        }
    });
    public static final Single SLEEP  = addSingle("sleep", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.SLEEP();
        }
    });
    public static final Single SPM  = addSingle("spm", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.SPM();
        }
    });
    public static final Single STD  = addSingle("std", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 3;
            return new AVRInstr.STD((AVROperand.YZ)operands[0], (AVROperand.IMM6)operands[1], (AVROperand.GPR)operands[2]);
        }
    });
    public static final Single STS  = addSingle("sts", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.STS((AVROperand.DADDR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single SUB  = addSingle("sub", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SUB((AVROperand.GPR)operands[0], (AVROperand.GPR)operands[1]);
        }
    });
    public static final Single SUBI  = addSingle("subi", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.SUBI((AVROperand.HGPR)operands[0], (AVROperand.IMM8)operands[1]);
        }
    });
    public static final Single SWAP  = addSingle("swap", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.SWAP((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single TST  = addSingle("tst", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 1;
            return new AVRInstr.TST((AVROperand.GPR)operands[0]);
        }
    });
    public static final Single WDR  = addSingle("wdr", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 0;
            return new AVRInstr.WDR();
        }
    });
    public static final Single ELPM  = addSingle("elpm", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.ELPM((AVROperand.XLPM_source_union)operands[0], (AVROperand.XLPM_dest_union)operands[1]);
        }
    });
    public static final Single LPM  = addSingle("lpm", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.LPM((AVROperand.XLPM_source_union)operands[0], (AVROperand.XLPM_dest_union)operands[1]);
        }
    });
    public static final Single LD  = addSingle("ld", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.LD((AVROperand.LD_ST_rd_union)operands[0], (AVROperand.LD_ST_ar_union)operands[1]);
        }
    });
    public static final Single ST  = addSingle("st", new Single() {
        public AVRInstr build(AVROperand[] operands) {
            // assert operands.length == 2;
            return new AVRInstr.ST((AVROperand.LD_ST_rd_union)operands[0], (AVROperand.LD_ST_ar_union)operands[1]);
        }
    });
    public static int checkValue(int val, int low, int high) {
        if ( val < low || val > high ) {
            throw new Error();
        }
        return val;
    }
}
