package avrora.arch.avr;
import java.util.HashMap;

/**
 * The <code>AVRAddrMode</code> class represents an addressing mode for
 * this architecture. An addressing mode fixes the number and type of
 * operands, the syntax, and the encoding format of the instruction.
 */
public interface AVRAddrMode {
    public void accept(AVRAddrModeVisitor v);
    public interface XLPM extends AVRAddrMode {
        public AVROperand get_source();
        public AVROperand get_dest();
    }
    public interface LD_ST extends AVRAddrMode {
        public AVROperand get_rd();
        public AVROperand get_ar();
    }
    public static class GPRGPR implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public GPRGPR(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_GPRGPR(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class MGPRMGPR implements AVRAddrMode {
        public AVROperand.MGPR rd;
        public AVROperand.MGPR rr;
        public MGPRMGPR(AVROperand.MGPR rd, AVROperand.MGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_MGPRMGPR(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class GPR implements AVRAddrMode {
        public AVROperand.GPR r1;
        public GPR(AVROperand.GPR r1) {
            this.r1 = r1;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_GPR(r1);
        }
        public AVROperand get_r1() { return r1; }
    }
    public static class HGPRIMM8 implements AVRAddrMode {
        public AVROperand.HGPR r1;
        public AVROperand.IMM8 imm1;
        public HGPRIMM8(AVROperand.HGPR r1, AVROperand.IMM8 imm1) {
            this.r1 = r1;
            this.imm1 = imm1;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_HGPRIMM8(r1, imm1);
        }
        public AVROperand get_r1() { return r1; }
        public AVROperand get_imm1() { return imm1; }
    }
    public static class ABS implements AVRAddrMode {
        public AVROperand.PADDR target;
        public ABS(AVROperand.PADDR target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_ABS(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class BRANCH implements AVRAddrMode {
        public AVROperand.SREL target;
        public BRANCH(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_BRANCH(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class CALL implements AVRAddrMode {
        public AVROperand.LREL target;
        public CALL(AVROperand.LREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_CALL(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class WRITEBIT implements AVRAddrMode {
        public WRITEBIT() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_WRITEBIT();
        }
    }
    public static class XLPM_REG implements AVRAddrMode, XLPM {
        public AVROperand.R0_B dest;
        public AVROperand.RZ_W source;
        public XLPM_REG(AVROperand.R0_B dest, AVROperand.RZ_W source) {
            this.dest = dest;
            this.source = source;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_XLPM_REG(dest, source);
        }
        public AVROperand get_dest() { return dest; }
        public AVROperand get_source() { return source; }
    }
    public static class XLPM_D implements AVRAddrMode, XLPM {
        public AVROperand.GPR dest;
        public AVROperand.RZ_W source;
        public XLPM_D(AVROperand.GPR dest, AVROperand.RZ_W source) {
            this.dest = dest;
            this.source = source;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_XLPM_D(dest, source);
        }
        public AVROperand get_dest() { return dest; }
        public AVROperand get_source() { return source; }
    }
    public static class XLPM_INC implements AVRAddrMode, XLPM {
        public AVROperand.GPR dest;
        public AVROperand.AI_RZ_W source;
        public XLPM_INC(AVROperand.GPR dest, AVROperand.AI_RZ_W source) {
            this.dest = dest;
            this.source = source;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_XLPM_INC(dest, source);
        }
        public AVROperand get_dest() { return dest; }
        public AVROperand get_source() { return source; }
    }
    public static class LD_ST_XYZ implements AVRAddrMode, LD_ST {
        public AVROperand.GPR rd;
        public AVROperand.XYZ ar;
        public LD_ST_XYZ(AVROperand.GPR rd, AVROperand.XYZ ar) {
            this.rd = rd;
            this.ar = ar;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_LD_ST_XYZ(rd, ar);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_ar() { return ar; }
    }
    public static class LD_ST_AI_XYZ implements AVRAddrMode, LD_ST {
        public AVROperand.GPR rd;
        public AVROperand.AI_XYZ ar;
        public LD_ST_AI_XYZ(AVROperand.GPR rd, AVROperand.AI_XYZ ar) {
            this.rd = rd;
            this.ar = ar;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_LD_ST_AI_XYZ(rd, ar);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_ar() { return ar; }
    }
    public static class LD_ST_PD_XYZ implements AVRAddrMode, LD_ST {
        public AVROperand.GPR rd;
        public AVROperand.PD_XYZ ar;
        public LD_ST_PD_XYZ(AVROperand.GPR rd, AVROperand.PD_XYZ ar) {
            this.rd = rd;
            this.ar = ar;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_LD_ST_PD_XYZ(rd, ar);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_ar() { return ar; }
    }
    public static class $adc$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $adc$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$adc$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $add$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $add$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$add$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $adiw$ implements AVRAddrMode {
        public AVROperand.RDL rd;
        public AVROperand.IMM6 imm;
        public $adiw$(AVROperand.RDL rd, AVROperand.IMM6 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$adiw$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $and$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $and$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$and$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $andi$ implements AVRAddrMode {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        public $andi$(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$andi$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $asr$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $asr$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$asr$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $bclr$ implements AVRAddrMode {
        public AVROperand.IMM3 bit;
        public $bclr$(AVROperand.IMM3 bit) {
            this.bit = bit;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bclr$(bit);
        }
        public AVROperand get_bit() { return bit; }
    }
    public static class $bld$ implements AVRAddrMode {
        public AVROperand.GPR rr;
        public AVROperand.IMM3 bit;
        public $bld$(AVROperand.GPR rr, AVROperand.IMM3 bit) {
            this.rr = rr;
            this.bit = bit;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bld$(rr, bit);
        }
        public AVROperand get_rr() { return rr; }
        public AVROperand get_bit() { return bit; }
    }
    public static class $brbc$ implements AVRAddrMode {
        public AVROperand.IMM3 bit;
        public AVROperand.SREL target;
        public $brbc$(AVROperand.IMM3 bit, AVROperand.SREL target) {
            this.bit = bit;
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brbc$(bit, target);
        }
        public AVROperand get_bit() { return bit; }
        public AVROperand get_target() { return target; }
    }
    public static class $brbs$ implements AVRAddrMode {
        public AVROperand.IMM3 bit;
        public AVROperand.SREL target;
        public $brbs$(AVROperand.IMM3 bit, AVROperand.SREL target) {
            this.bit = bit;
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brbs$(bit, target);
        }
        public AVROperand get_bit() { return bit; }
        public AVROperand get_target() { return target; }
    }
    public static class $brcc$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brcc$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brcc$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brcs$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brcs$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brcs$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $break$ implements AVRAddrMode {
        public $break$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$break$();
        }
    }
    public static class $breq$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $breq$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$breq$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brge$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brge$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brge$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brhc$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brhc$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brhc$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brhs$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brhs$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brhs$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brid$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brid$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brid$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brie$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brie$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brie$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brlo$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brlo$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brlo$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brlt$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brlt$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brlt$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brmi$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brmi$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brmi$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brne$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brne$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brne$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brpl$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brpl$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brpl$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brsh$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brsh$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brsh$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brtc$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brtc$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brtc$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brts$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brts$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brts$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brvc$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brvc$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brvc$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $brvs$ implements AVRAddrMode {
        public AVROperand.SREL target;
        public $brvs$(AVROperand.SREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$brvs$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $bset$ implements AVRAddrMode {
        public AVROperand.IMM3 bit;
        public $bset$(AVROperand.IMM3 bit) {
            this.bit = bit;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bset$(bit);
        }
        public AVROperand get_bit() { return bit; }
    }
    public static class $bst$ implements AVRAddrMode {
        public AVROperand.GPR rr;
        public AVROperand.IMM3 bit;
        public $bst$(AVROperand.GPR rr, AVROperand.IMM3 bit) {
            this.rr = rr;
            this.bit = bit;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$bst$(rr, bit);
        }
        public AVROperand get_rr() { return rr; }
        public AVROperand get_bit() { return bit; }
    }
    public static class $call$ implements AVRAddrMode {
        public AVROperand.PADDR target;
        public $call$(AVROperand.PADDR target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$call$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $cbi$ implements AVRAddrMode {
        public AVROperand.IMM5 ior;
        public AVROperand.IMM3 bit;
        public $cbi$(AVROperand.IMM5 ior, AVROperand.IMM3 bit) {
            this.ior = ior;
            this.bit = bit;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cbi$(ior, bit);
        }
        public AVROperand get_ior() { return ior; }
        public AVROperand get_bit() { return bit; }
    }
    public static class $cbr$ implements AVRAddrMode {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        public $cbr$(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cbr$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $clc$ implements AVRAddrMode {
        public $clc$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clc$();
        }
    }
    public static class $clh$ implements AVRAddrMode {
        public $clh$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clh$();
        }
    }
    public static class $cli$ implements AVRAddrMode {
        public $cli$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cli$();
        }
    }
    public static class $cln$ implements AVRAddrMode {
        public $cln$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cln$();
        }
    }
    public static class $clr$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $clr$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clr$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $cls$ implements AVRAddrMode {
        public $cls$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cls$();
        }
    }
    public static class $clt$ implements AVRAddrMode {
        public $clt$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clt$();
        }
    }
    public static class $clv$ implements AVRAddrMode {
        public $clv$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clv$();
        }
    }
    public static class $clz$ implements AVRAddrMode {
        public $clz$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$clz$();
        }
    }
    public static class $com$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $com$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$com$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $cp$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $cp$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cp$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $cpc$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $cpc$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cpc$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $cpi$ implements AVRAddrMode {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        public $cpi$(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cpi$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $cpse$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $cpse$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$cpse$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $dec$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $dec$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$dec$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $eicall$ implements AVRAddrMode {
        public $eicall$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$eicall$();
        }
    }
    public static class $eijmp$ implements AVRAddrMode {
        public $eijmp$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$eijmp$();
        }
    }
    public static class $eor$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $eor$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$eor$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $fmul$ implements AVRAddrMode {
        public AVROperand.MGPR rd;
        public AVROperand.MGPR rr;
        public $fmul$(AVROperand.MGPR rd, AVROperand.MGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$fmul$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $fmuls$ implements AVRAddrMode {
        public AVROperand.MGPR rd;
        public AVROperand.MGPR rr;
        public $fmuls$(AVROperand.MGPR rd, AVROperand.MGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$fmuls$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $fmulsu$ implements AVRAddrMode {
        public AVROperand.MGPR rd;
        public AVROperand.MGPR rr;
        public $fmulsu$(AVROperand.MGPR rd, AVROperand.MGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$fmulsu$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $icall$ implements AVRAddrMode {
        public $icall$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$icall$();
        }
    }
    public static class $ijmp$ implements AVRAddrMode {
        public $ijmp$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ijmp$();
        }
    }
    public static class $in$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.IMM6 imm;
        public $in$(AVROperand.GPR rd, AVROperand.IMM6 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$in$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $inc$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $inc$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$inc$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $jmp$ implements AVRAddrMode {
        public AVROperand.PADDR target;
        public $jmp$(AVROperand.PADDR target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$jmp$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $ldd$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.YZ ar;
        public AVROperand.IMM6 imm;
        public $ldd$(AVROperand.GPR rd, AVROperand.YZ ar, AVROperand.IMM6 imm) {
            this.rd = rd;
            this.ar = ar;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ldd$(rd, ar, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_ar() { return ar; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $ldi$ implements AVRAddrMode {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        public $ldi$(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ldi$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $lds$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.DADDR addr;
        public $lds$(AVROperand.GPR rd, AVROperand.DADDR addr) {
            this.rd = rd;
            this.addr = addr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$lds$(rd, addr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_addr() { return addr; }
    }
    public static class $lsl$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $lsl$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$lsl$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $lsr$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $lsr$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$lsr$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $mov$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $mov$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$mov$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $movw$ implements AVRAddrMode {
        public AVROperand.EGPR rd;
        public AVROperand.EGPR rr;
        public $movw$(AVROperand.EGPR rd, AVROperand.EGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$movw$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $mul$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $mul$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$mul$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $muls$ implements AVRAddrMode {
        public AVROperand.HGPR rd;
        public AVROperand.HGPR rr;
        public $muls$(AVROperand.HGPR rd, AVROperand.HGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$muls$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $mulsu$ implements AVRAddrMode {
        public AVROperand.MGPR rd;
        public AVROperand.MGPR rr;
        public $mulsu$(AVROperand.MGPR rd, AVROperand.MGPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$mulsu$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $neg$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $neg$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$neg$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $nop$ implements AVRAddrMode {
        public $nop$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$nop$();
        }
    }
    public static class $or$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $or$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$or$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $ori$ implements AVRAddrMode {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        public $ori$(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ori$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $out$ implements AVRAddrMode {
        public AVROperand.IMM6 ior;
        public AVROperand.GPR rr;
        public $out$(AVROperand.IMM6 ior, AVROperand.GPR rr) {
            this.ior = ior;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$out$(ior, rr);
        }
        public AVROperand get_ior() { return ior; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $pop$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $pop$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$pop$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $push$ implements AVRAddrMode {
        public AVROperand.GPR rr;
        public $push$(AVROperand.GPR rr) {
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$push$(rr);
        }
        public AVROperand get_rr() { return rr; }
    }
    public static class $rcall$ implements AVRAddrMode {
        public AVROperand.LREL target;
        public $rcall$(AVROperand.LREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$rcall$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $ret$ implements AVRAddrMode {
        public $ret$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ret$();
        }
    }
    public static class $reti$ implements AVRAddrMode {
        public $reti$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$reti$();
        }
    }
    public static class $rjmp$ implements AVRAddrMode {
        public AVROperand.LREL target;
        public $rjmp$(AVROperand.LREL target) {
            this.target = target;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$rjmp$(target);
        }
        public AVROperand get_target() { return target; }
    }
    public static class $rol$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $rol$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$rol$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $ror$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $ror$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ror$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $sbc$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $sbc$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbc$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $sbci$ implements AVRAddrMode {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        public $sbci$(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbci$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $sbi$ implements AVRAddrMode {
        public AVROperand.IMM5 ior;
        public AVROperand.IMM3 bit;
        public $sbi$(AVROperand.IMM5 ior, AVROperand.IMM3 bit) {
            this.ior = ior;
            this.bit = bit;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbi$(ior, bit);
        }
        public AVROperand get_ior() { return ior; }
        public AVROperand get_bit() { return bit; }
    }
    public static class $sbic$ implements AVRAddrMode {
        public AVROperand.IMM5 ior;
        public AVROperand.IMM3 bit;
        public $sbic$(AVROperand.IMM5 ior, AVROperand.IMM3 bit) {
            this.ior = ior;
            this.bit = bit;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbic$(ior, bit);
        }
        public AVROperand get_ior() { return ior; }
        public AVROperand get_bit() { return bit; }
    }
    public static class $sbis$ implements AVRAddrMode {
        public AVROperand.IMM5 ior;
        public AVROperand.IMM3 bit;
        public $sbis$(AVROperand.IMM5 ior, AVROperand.IMM3 bit) {
            this.ior = ior;
            this.bit = bit;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbis$(ior, bit);
        }
        public AVROperand get_ior() { return ior; }
        public AVROperand get_bit() { return bit; }
    }
    public static class $sbiw$ implements AVRAddrMode {
        public AVROperand.RDL rd;
        public AVROperand.IMM6 imm;
        public $sbiw$(AVROperand.RDL rd, AVROperand.IMM6 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbiw$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $sbr$ implements AVRAddrMode {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        public $sbr$(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbr$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $sbrc$ implements AVRAddrMode {
        public AVROperand.GPR rr;
        public AVROperand.IMM3 bit;
        public $sbrc$(AVROperand.GPR rr, AVROperand.IMM3 bit) {
            this.rr = rr;
            this.bit = bit;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbrc$(rr, bit);
        }
        public AVROperand get_rr() { return rr; }
        public AVROperand get_bit() { return bit; }
    }
    public static class $sbrs$ implements AVRAddrMode {
        public AVROperand.GPR rr;
        public AVROperand.IMM3 bit;
        public $sbrs$(AVROperand.GPR rr, AVROperand.IMM3 bit) {
            this.rr = rr;
            this.bit = bit;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sbrs$(rr, bit);
        }
        public AVROperand get_rr() { return rr; }
        public AVROperand get_bit() { return bit; }
    }
    public static class $sec$ implements AVRAddrMode {
        public $sec$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sec$();
        }
    }
    public static class $seh$ implements AVRAddrMode {
        public $seh$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$seh$();
        }
    }
    public static class $sei$ implements AVRAddrMode {
        public $sei$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sei$();
        }
    }
    public static class $sen$ implements AVRAddrMode {
        public $sen$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sen$();
        }
    }
    public static class $ser$ implements AVRAddrMode {
        public AVROperand.HGPR rd;
        public $ser$(AVROperand.HGPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ser$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $ses$ implements AVRAddrMode {
        public $ses$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$ses$();
        }
    }
    public static class $set$ implements AVRAddrMode {
        public $set$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$set$();
        }
    }
    public static class $sev$ implements AVRAddrMode {
        public $sev$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sev$();
        }
    }
    public static class $sez$ implements AVRAddrMode {
        public $sez$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sez$();
        }
    }
    public static class $sleep$ implements AVRAddrMode {
        public $sleep$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sleep$();
        }
    }
    public static class $spm$ implements AVRAddrMode {
        public $spm$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$spm$();
        }
    }
    public static class $std$ implements AVRAddrMode {
        public AVROperand.YZ ar;
        public AVROperand.IMM6 imm;
        public AVROperand.GPR rr;
        public $std$(AVROperand.YZ ar, AVROperand.IMM6 imm, AVROperand.GPR rr) {
            this.ar = ar;
            this.imm = imm;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$std$(ar, imm, rr);
        }
        public AVROperand get_ar() { return ar; }
        public AVROperand get_imm() { return imm; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $sts$ implements AVRAddrMode {
        public AVROperand.DADDR addr;
        public AVROperand.GPR rr;
        public $sts$(AVROperand.DADDR addr, AVROperand.GPR rr) {
            this.addr = addr;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sts$(addr, rr);
        }
        public AVROperand get_addr() { return addr; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $sub$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public AVROperand.GPR rr;
        public $sub$(AVROperand.GPR rd, AVROperand.GPR rr) {
            this.rd = rd;
            this.rr = rr;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$sub$(rd, rr);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_rr() { return rr; }
    }
    public static class $subi$ implements AVRAddrMode {
        public AVROperand.HGPR rd;
        public AVROperand.IMM8 imm;
        public $subi$(AVROperand.HGPR rd, AVROperand.IMM8 imm) {
            this.rd = rd;
            this.imm = imm;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$subi$(rd, imm);
        }
        public AVROperand get_rd() { return rd; }
        public AVROperand get_imm() { return imm; }
    }
    public static class $swap$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $swap$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$swap$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $tst$ implements AVRAddrMode {
        public AVROperand.GPR rd;
        public $tst$(AVROperand.GPR rd) {
            this.rd = rd;
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$tst$(rd);
        }
        public AVROperand get_rd() { return rd; }
    }
    public static class $wdr$ implements AVRAddrMode {
        public $wdr$() {
        }
        public void accept(AVRAddrModeVisitor v) {
            v.visit_$wdr$();
        }
    }
}
