package avrora.arch.avr;
import java.util.HashMap;

/**
 * The <code>AVRAddrModeVisitor</code> interface implements the visitor
 * pattern for addressing modes.
 */
public interface AVRAddrModeVisitor {
    public void visit_GPRGPR(AVROperand.GPR rd, AVROperand.GPR rr);
    public void visit_MGPRMGPR(AVROperand.MGPR rd, AVROperand.MGPR rr);
    public void visit_GPR(AVROperand.GPR rd);
    public void visit_HGPRIMM8(AVROperand.HGPR rd, AVROperand.IMM8 imm);
    public void visit_ABS(AVROperand.PADDR target);
    public void visit_BRANCH(AVROperand.SREL target);
    public void visit_CALL(AVROperand.LREL target);
    public void visit_WRITEBIT();
    public void visit_XLPM_REG(AVROperand.R0_B dest, AVROperand.RZ_W source);
    public void visit_XLPM_D(AVROperand.GPR dest, AVROperand.RZ_W source);
    public void visit_XLPM_INC(AVROperand.GPR dest, AVROperand.AI_RZ_W source);
    public void visit_LD_ST_XYZ(AVROperand.GPR rd, AVROperand.XYZ ar);
    public void visit_LD_ST_AI_XYZ(AVROperand.GPR rd, AVROperand.AI_XYZ ar);
    public void visit_LD_ST_PD_XYZ(AVROperand.GPR rd, AVROperand.PD_XYZ ar);
    public void visit_$adiw$(AVROperand.RDL rd, AVROperand.IMM6 imm);
    public void visit_$bclr$(AVROperand.IMM3 bit);
    public void visit_$bld$(AVROperand.GPR rr, AVROperand.IMM3 bit);
    public void visit_$brbc$(AVROperand.IMM3 bit, AVROperand.SREL target);
    public void visit_$brbs$(AVROperand.IMM3 bit, AVROperand.SREL target);
    public void visit_$break$();
    public void visit_$bset$(AVROperand.IMM3 bit);
    public void visit_$bst$(AVROperand.GPR rr, AVROperand.IMM3 bit);
    public void visit_$call$(AVROperand.PADDR target);
    public void visit_$cbi$(AVROperand.IMM5 ior, AVROperand.IMM3 bit);
    public void visit_$clc$();
    public void visit_$clh$();
    public void visit_$cli$();
    public void visit_$cln$();
    public void visit_$clr$(AVROperand.GPR rd);
    public void visit_$cls$();
    public void visit_$clt$();
    public void visit_$clv$();
    public void visit_$clz$();
    public void visit_$eicall$();
    public void visit_$eijmp$();
    public void visit_$fmul$(AVROperand.MGPR rd, AVROperand.MGPR rr);
    public void visit_$fmuls$(AVROperand.MGPR rd, AVROperand.MGPR rr);
    public void visit_$fmulsu$(AVROperand.MGPR rd, AVROperand.MGPR rr);
    public void visit_$icall$();
    public void visit_$ijmp$();
    public void visit_$in$(AVROperand.GPR rd, AVROperand.IMM6 imm);
    public void visit_$jmp$(AVROperand.PADDR target);
    public void visit_$ldd$(AVROperand.GPR rd, AVROperand.YZ ar, AVROperand.IMM6 imm);
    public void visit_$lds$(AVROperand.GPR rd, AVROperand.DADDR addr);
    public void visit_$lsl$(AVROperand.GPR rd);
    public void visit_$movw$(AVROperand.EGPR rd, AVROperand.EGPR rr);
    public void visit_$muls$(AVROperand.HGPR rd, AVROperand.HGPR rr);
    public void visit_$mulsu$(AVROperand.MGPR rd, AVROperand.MGPR rr);
    public void visit_$nop$();
    public void visit_$out$(AVROperand.IMM6 ior, AVROperand.GPR rr);
    public void visit_$push$(AVROperand.GPR rr);
    public void visit_$rcall$(AVROperand.LREL target);
    public void visit_$ret$();
    public void visit_$reti$();
    public void visit_$rjmp$(AVROperand.LREL target);
    public void visit_$rol$(AVROperand.GPR rd);
    public void visit_$sbi$(AVROperand.IMM5 ior, AVROperand.IMM3 bit);
    public void visit_$sbic$(AVROperand.IMM5 ior, AVROperand.IMM3 bit);
    public void visit_$sbis$(AVROperand.IMM5 ior, AVROperand.IMM3 bit);
    public void visit_$sbiw$(AVROperand.RDL rd, AVROperand.IMM6 imm);
    public void visit_$sbrc$(AVROperand.GPR rr, AVROperand.IMM3 bit);
    public void visit_$sbrs$(AVROperand.GPR rr, AVROperand.IMM3 bit);
    public void visit_$sec$();
    public void visit_$seh$();
    public void visit_$sei$();
    public void visit_$sen$();
    public void visit_$ser$(AVROperand.HGPR rd);
    public void visit_$ses$();
    public void visit_$set$();
    public void visit_$sev$();
    public void visit_$sez$();
    public void visit_$sleep$();
    public void visit_$spm$();
    public void visit_$std$(AVROperand.YZ ar, AVROperand.IMM6 imm, AVROperand.GPR rr);
    public void visit_$sts$(AVROperand.DADDR addr, AVROperand.GPR rr);
    public void visit_$tst$(AVROperand.GPR rd);
    public void visit_$wdr$();
}
