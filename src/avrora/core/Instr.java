package avrora.sir;

import vpc.VPCBase;
import avrora.Operand;

import java.util.NoSuchElementException;

/**
 * The <code>Instr</code> class and its descendants represent instructions within the
 * assembly code.
 * @author Ben L. Titzer
 */
public abstract class Instr extends Elem implements InstrPrototype {

    public boolean isInstr() {
        return true;
    }

    public Data asData(int address) {
        // TODO: define correct error for this.
        throw VPCBase.failure("not data @ " + address);
    }

    public Instr asInstr(int address) {
        return this;
    }

    public abstract String getOperands();

    public String getVariant() {
        return getName();
    }

    public int getSize() {
        return 2;
    }

    public int getCycles() {
        return 1;
    }

    public abstract void accept(InstrVisitor v);

    /**
     * The <code>InvalidOperand</code> class represents a runtime error
     * thrown by the constructor of an instruction or the <code>build</code>
     * method of a prototype when an operand does not meet the restrictions
     * imposed by the AVR instruction set architecture.
     */
    public static class InvalidOperand extends RuntimeException {
        public final int number;

        InvalidOperand(int num, String msg) {
            super("invalid operand #" + num + ": " + msg);
            number = num;
        }
    }

    public static class InvalidRegister extends InvalidOperand {
        public final Register.Set set;
        public final Register register;

        public InvalidRegister(int num, Register reg, Register.Set s) {
            super(num, "must be one of " + s.contents);
            set = s;
            register = reg;
        }
    }

    public static class InvalidImmediate extends InvalidOperand {

        public final int low;
        public final int high;
        public final int value;

        public InvalidImmediate(int num, int v, int l, int h) {
            super(num, "value out of required range [" + l + ", " + h + "]");
            low = l;
            high = h;
            value = v;
        }
    }

    public static class RegisterRequired extends RuntimeException {

        public final Operand operand;

        RegisterRequired(Operand o) {
            super("register required");
            operand = o;
        }
    }

    public static class ImmediateRequired extends RuntimeException {

        public final Operand operand;

        ImmediateRequired(Operand o) {
            super("immediate required");
            operand = o;
        }
    }

    /**
     * The <code>WrongNumberOfOperands</code> class represents a runtime
     * error thrown by the <code>build</code> method of a prototype when
     * the wrong number of operands is passed to build an instruction.
     */
    public static class WrongNumberOfOperands extends RuntimeException {
        public final int expected;
        public final int found;

        WrongNumberOfOperands(int f, int e) {
            super("wrong number of operands, expected " + e + " and found " + f);
            expected = e;
            found = f;
        }
    }


    /**
     *  U T I L I T Y   F U N C T I O N S
     * ------------------------------------------------------------
     *
     * These utility functions help in the checking of operands
     * in individual instructions.
     *
     *
     *
     */
    private static void need(int num, Operand[] ops) {
        if (ops.length != num)
            throw new WrongNumberOfOperands(ops.length, num);
    }

    private static Register GPR(int num, Register reg) {
        return checkReg(num, reg, Register.GPR_set);
    }

    private static Register HGPR(int num, Register reg) {
        return checkReg(num, reg, Register.HGPR_set);
    }

    private static Register MGPR(int num, Register reg) {
        return checkReg(num, reg, Register.MGPR_set);
    }

    private static Register ADR(int num, Register reg) {
        return checkReg(num, reg, Register.ADR_set);
    }

    private static Register RDL(int num, Register reg) {
        return checkReg(num, reg, Register.RDL_set);
    }

    private static Register EGPR(int num, Register reg) {
        return checkReg(num, reg, Register.EGPR_set);
    }

    private static Register YZ(int num, Register reg) {
        return checkReg(num, reg, Register.YZ_set);
    }

    private static Register Z(int num, Register reg) {
        return checkReg(num, reg, Register.Z_set);
    }

    private static int IMM3(int num, int val) {
        return checkImm(num, val, 0, 7);
    }

    private static int IMM5(int num, int val) {
        return checkImm(num, val, 0, 31);
    }

    private static int IMM6(int num, int val) {
        return checkImm(num, val, 0, 63);
    }

    private static int IMM8(int num, int val) {
        return checkImm(num, val, 0, 255);
    }

    private static int SREL(int pc, int num, int val) {
        return checkImm(num, val - pc - 1, -64, 63);
    }

    private static int LREL(int pc, int num, int val) {
        return checkImm(num, val - pc - 1, -2048, 2047);
    }

    private static int MEM(int num, int val) {
        // TODO: fix checking of program addresses
        return checkImm(num, val, 0, 65536);
    }

    private static int checkImm(int num, int val, int low, int high) {
        if (val < low || val > high) throw new InvalidImmediate(num, val, low, high);
        return val;

    }

    private static Register checkReg(int num, Register reg, Register.Set set) {
        if (set.contains(reg)) return reg;
        throw new InvalidRegister(num, reg, set);
    }

    private static Register REG(Operand o) {
        if (o.isRegister()) return ((Operand.Register) o).getRegister();
        throw new RegisterRequired(o);
    }

    private static int IMM(Operand o) {
        if (o.isConstant()) return ((Operand.Constant) o).getValue();
        throw new ImmediateRequired(o);
    }

    /**
     *  A B S T R A C T   C L A S S E S
     * --------------------------------------------------------
     *
     *  These abstract implementations of the instruction simplify
     *  the specification of each individual instruction considerably.
     *
     *
     */
    public abstract static class REGREG_class extends Instr {
        public final Register r1;
        public final Register r2;

        REGREG_class(Register _r1, Register _r2) {
            r1 = _r1;
            r2 = _r2;
        }

        public String getOperands() {
            return r1 + ", " + r2;
        }

        public Instr build(int pc, Operand[] ops) {
            need(2, ops);
            return allocate(pc, REG(ops[0]), REG(ops[1]));
        }

        abstract Instr allocate(int pc, Register r1, Register r2);
    }

    public abstract static class REGIMM_class extends Instr {
        public final Register r1;
        public final int imm1;

        REGIMM_class(Register r, int i) {
            r1 = r;
            imm1 = i;
        }

        public String getOperands() {
            return r1 + ", " + imm1;
        }

        public Instr build(int pc, Operand[] ops) {
            need(2, ops);
            return allocate(pc, REG(ops[0]), IMM(ops[1]));
        }

        abstract Instr allocate(int pc, Register r1, int imm1);
    }

    public abstract static class IMMREG_class extends Instr {
        public final Register r1;
        public final int imm1;

        IMMREG_class(int i, Register r) {
            r1 = r;
            imm1 = i;
        }

        public String getOperands() {
            return imm1 + ", " + r1;
        }

        public Instr build(int pc, Operand[] ops) {
            need(2, ops);
            return allocate(pc, IMM(ops[0]), REG(ops[1]));
        }

        abstract Instr allocate(int pc, int imm1, Register r1);
    }

    public abstract static class REG_class extends Instr {
        public final Register r1;

        REG_class(Register r) {
            r1 = r;
        }

        public String getOperands() {
            return r1.toString();
        }

        public Instr build(int pc, Operand[] ops) {
            need(1, ops);
            return allocate(pc, REG(ops[0]));
        }

        abstract Instr allocate(int pc, Register r1);
    }

    public abstract static class IMMIMM_class extends Instr {
        public final int imm1;
        public final int imm2;

        IMMIMM_class(int i1, int i2) {
            imm1 = i1;
            imm2 = i2;
        }

        public String getOperands() {
            return imm1 + ", " + imm2;
        }

        public Instr build(int pc, Operand[] ops) {
            need(2, ops);
            return allocate(pc, IMM(ops[0]), IMM(ops[1]));
        }

        abstract Instr allocate(int pc, int imm1, int imm2);
    }

    public abstract static class IMM_class extends Instr {
        public final int imm1;

        IMM_class(int i1) {
            imm1 = i1;
        }

        public String getOperands() {
            return "" + imm1;
        }

        public Instr build(int pc, Operand[] ops) {
            need(1, ops);
            return allocate(pc, IMM(ops[0]));
        }

        abstract Instr allocate(int pc, int imm1);
    }

    public abstract static class REGREGIMM_class extends Instr {
        public final Register r1;
        public final Register r2;
        public final int imm1;

        REGREGIMM_class(Register r1, Register r2, int i1) {
            this.r1 = r1;
            this.r2 = r2;
            imm1 = i1;
        }

        public String getOperands() {
            return "" + imm1;
        }

        public Instr build(int pc, Operand[] ops) {
            need(3, ops);
            return allocate(pc, REG(ops[0]), REG(ops[1]), IMM(ops[2]));
        }

        abstract Instr allocate(int pc, Register r1, Register r2, int imm1);
    }

    public abstract static class REGIMMREG_class extends Instr {
        public final Register r1;
        public final Register r2;
        public final int imm1;

        REGIMMREG_class(Register r1, int i1, Register r2) {
            this.r1 = r1;
            this.r2 = r2;
            imm1 = i1;
        }

        public String getOperands() {
            return "" + imm1;
        }

        public Instr build(int pc, Operand[] ops) {
            need(3, ops);
            return allocate(pc, REG(ops[0]), IMM(ops[1]), REG(ops[2]));
        }

        abstract Instr allocate(int pc, Register r1, int imm1, Register r2);
    }

    public abstract static class NONE_class extends Instr {
        public String getOperands() {
            return "";
        }

        public Instr build(int pc, Operand[] ops) {
            need(0, ops);
            return allocate(pc);
        }

        abstract Instr allocate(int pc);
    }

    private static int IMM3_default = 0;
    private static int IMM5_default = 0;
    private static int IMM6_default = 0;
    private static int IMM8_default = 0;
    private static int SREL_default = 0;
    private static int LREL_default = 0;
    private static int MEM_default = 0;
    private static Register GPR_default = Register.R0;
    private static Register MGPR_default = Register.R16;
    private static Register HGPR_default = Register.R16;
    private static Register EGPR_default = Register.R0;
    private static Register ADR_default = Register.X;
    private static Register RDL_default = Register.R24;
    private static Register YZ_default = Register.Y;
    private static Register Z_default = Register.Z;


    /**
     *  I N S T R U C T I O N   D E S C R I P T I O N S
     * ----------------------------------------------------------------
     *
     * These are the actual instruction descriptions that contain the
     * constraints on operands and sizes, etc.
     * This code is GENERATED from Generator.java.
     *
     * DO NOT MODIFY THIS CODE!!!!
     */
//--BEGIN INSTR GENERATOR--
    public static class ADC extends REGREG_class { // add register to register with carry
        public String getName() { return "adc"; }
        static InstrPrototype prototype = new ADC(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new ADC(pc, a, b); }
        public ADC(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class ADD extends REGREG_class { // add register to register
        public String getName() { return "add"; }
        static InstrPrototype prototype = new ADD(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new ADD(pc, a, b); }
        public ADD(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class ADIW extends REGIMM_class { // add immediate to word register
        public String getName() { return "adiw"; }
        static InstrPrototype prototype = new ADIW(0,RDL_default,IMM6_default);
        Instr allocate(int pc,Register a,int b) { return new ADIW(pc, a, b); }
        public ADIW(int pc,Register a,int b) { super(RDL(1, a),IMM6(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class AND extends REGREG_class { // and register with register
        public String getName() { return "and"; }
        static InstrPrototype prototype = new AND(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new AND(pc, a, b); }
        public AND(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class ANDI extends REGIMM_class { // and register with immediate
        public String getName() { return "andi"; }
        static InstrPrototype prototype = new ANDI(0,HGPR_default,IMM8_default);
        Instr allocate(int pc,Register a,int b) { return new ANDI(pc, a, b); }
        public ANDI(int pc,Register a,int b) { super(HGPR(1, a),IMM8(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class ASR extends REG_class { // arithmetic shift right
        public String getName() { return "asr"; }
        static InstrPrototype prototype = new ASR(0,GPR_default);
        Instr allocate(int pc,Register a) { return new ASR(pc, a); }
        public ASR(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BCLR extends IMM_class { // clear bit in status register
        public String getName() { return "bclr"; }
        static InstrPrototype prototype = new BCLR(0,IMM3_default);
        Instr allocate(int pc,int a) { return new BCLR(pc, a); }
        public BCLR(int pc,int a) { super(IMM3(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BLD extends REGIMM_class { // load bit from T flag into register
        public String getName() { return "bld"; }
        static InstrPrototype prototype = new BLD(0,GPR_default,IMM3_default);
        Instr allocate(int pc,Register a,int b) { return new BLD(pc, a, b); }
        public BLD(int pc,Register a,int b) { super(GPR(1, a),IMM3(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRBC extends IMMIMM_class { // branch if bit in status register is clear
        public String getName() { return "brbc"; }
        static InstrPrototype prototype = new BRBC(0,IMM3_default,SREL_default);
        Instr allocate(int pc,int a,int b) { return new BRBC(pc, a, b); }
        public BRBC(int pc,int a,int b) { super(IMM3(1, a),SREL(pc, 2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRBS extends IMMIMM_class { // branch if bit in status register is set
        public String getName() { return "brbs"; }
        static InstrPrototype prototype = new BRBS(0,IMM3_default,SREL_default);
        Instr allocate(int pc,int a,int b) { return new BRBS(pc, a, b); }
        public BRBS(int pc,int a,int b) { super(IMM3(1, a),SREL(pc, 2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRCC extends IMM_class { // branch if carry flag is clear
        public String getName() { return "brcc"; }
        static InstrPrototype prototype = new BRCC(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRCC(pc, a); }
        public BRCC(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRCS extends IMM_class { // branch if carry flag is set
        public String getName() { return "brcs"; }
        static InstrPrototype prototype = new BRCS(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRCS(pc, a); }
        public BRCS(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BREAK extends NONE_class { // break
        public String getName() { return "break"; }
        static InstrPrototype prototype = new BREAK(0);
        Instr allocate(int pc) { return new BREAK(pc); }
        public BREAK(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BREQ extends IMM_class { // branch if equal
        public String getName() { return "breq"; }
        static InstrPrototype prototype = new BREQ(0,SREL_default);
        Instr allocate(int pc,int a) { return new BREQ(pc, a); }
        public BREQ(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRGE extends IMM_class { // branch if greater or equal (signed)
        public String getName() { return "brge"; }
        static InstrPrototype prototype = new BRGE(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRGE(pc, a); }
        public BRGE(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRHC extends IMM_class { // branch if H flag is clear
        public String getName() { return "brhc"; }
        static InstrPrototype prototype = new BRHC(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRHC(pc, a); }
        public BRHC(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRHS extends IMM_class { // branch if H flag is set
        public String getName() { return "brhs"; }
        static InstrPrototype prototype = new BRHS(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRHS(pc, a); }
        public BRHS(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRID extends IMM_class { // branch if interrupts are disabled
        public String getName() { return "brid"; }
        static InstrPrototype prototype = new BRID(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRID(pc, a); }
        public BRID(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRIE extends IMM_class { // branch if interrupts are enabled
        public String getName() { return "brie"; }
        static InstrPrototype prototype = new BRIE(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRIE(pc, a); }
        public BRIE(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRLO extends IMM_class { // branch if lower
        public String getName() { return "brlo"; }
        static InstrPrototype prototype = new BRLO(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRLO(pc, a); }
        public BRLO(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRLT extends IMM_class { // branch if less than zero (signed)
        public String getName() { return "brlt"; }
        static InstrPrototype prototype = new BRLT(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRLT(pc, a); }
        public BRLT(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRMI extends IMM_class { // branch if minus
        public String getName() { return "brmi"; }
        static InstrPrototype prototype = new BRMI(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRMI(pc, a); }
        public BRMI(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRNE extends IMM_class { // branch if not equal
        public String getName() { return "brne"; }
        static InstrPrototype prototype = new BRNE(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRNE(pc, a); }
        public BRNE(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRPL extends IMM_class { // branch if positive
        public String getName() { return "brpl"; }
        static InstrPrototype prototype = new BRPL(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRPL(pc, a); }
        public BRPL(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRSH extends IMM_class { // branch if same or higher
        public String getName() { return "brsh"; }
        static InstrPrototype prototype = new BRSH(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRSH(pc, a); }
        public BRSH(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRTC extends IMM_class { // branch if T flag is clear
        public String getName() { return "brtc"; }
        static InstrPrototype prototype = new BRTC(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRTC(pc, a); }
        public BRTC(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRTS extends IMM_class { // branch if T flag is set
        public String getName() { return "brts"; }
        static InstrPrototype prototype = new BRTS(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRTS(pc, a); }
        public BRTS(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRVC extends IMM_class { // branch if V flag is clear
        public String getName() { return "brvc"; }
        static InstrPrototype prototype = new BRVC(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRVC(pc, a); }
        public BRVC(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BRVS extends IMM_class { // branch if V flag is set
        public String getName() { return "brvs"; }
        static InstrPrototype prototype = new BRVS(0,SREL_default);
        Instr allocate(int pc,int a) { return new BRVS(pc, a); }
        public BRVS(int pc,int a) { super(SREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BSET extends IMM_class { // set flag in status register
        public String getName() { return "bset"; }
        static InstrPrototype prototype = new BSET(0,IMM3_default);
        Instr allocate(int pc,int a) { return new BSET(pc, a); }
        public BSET(int pc,int a) { super(IMM3(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class BST extends REGIMM_class { // store bit in register into T flag
        public String getName() { return "bst"; }
        static InstrPrototype prototype = new BST(0,GPR_default,IMM3_default);
        Instr allocate(int pc,Register a,int b) { return new BST(pc, a, b); }
        public BST(int pc,Register a,int b) { super(GPR(1, a),IMM3(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CALL extends IMM_class { // call absolute address
        public String getName() { return "call"; }
        static InstrPrototype prototype = new CALL(0,MEM_default);
        Instr allocate(int pc,int a) { return new CALL(pc, a); }
        public CALL(int pc,int a) { super(MEM(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getSize() { return 4; }
        public int getCycles() { return 4; }
    }
    public static class CBI extends IMMIMM_class { // clear bit in IO register
        public String getName() { return "cbi"; }
        static InstrPrototype prototype = new CBI(0,IMM5_default,IMM3_default);
        Instr allocate(int pc,int a,int b) { return new CBI(pc, a, b); }
        public CBI(int pc,int a,int b) { super(IMM5(1, a),IMM3(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class CBR extends REGIMM_class { // clear bits in register
        public String getName() { return "cbr"; }
        static InstrPrototype prototype = new CBR(0,HGPR_default,IMM8_default);
        Instr allocate(int pc,Register a,int b) { return new CBR(pc, a, b); }
        public CBR(int pc,Register a,int b) { super(HGPR(1, a),IMM8(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CLC extends NONE_class { // clear C flag
        public String getName() { return "clc"; }
        static InstrPrototype prototype = new CLC(0);
        Instr allocate(int pc) { return new CLC(pc); }
        public CLC(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CLH extends NONE_class { // clear H flag
        public String getName() { return "clh"; }
        static InstrPrototype prototype = new CLH(0);
        Instr allocate(int pc) { return new CLH(pc); }
        public CLH(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CLI extends NONE_class { // clear I flag
        public String getName() { return "cli"; }
        static InstrPrototype prototype = new CLI(0);
        Instr allocate(int pc) { return new CLI(pc); }
        public CLI(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CLN extends NONE_class { // clear N flag
        public String getName() { return "cln"; }
        static InstrPrototype prototype = new CLN(0);
        Instr allocate(int pc) { return new CLN(pc); }
        public CLN(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CLR extends REG_class { // clear register (set to zero)
        public String getName() { return "clr"; }
        static InstrPrototype prototype = new CLR(0,GPR_default);
        Instr allocate(int pc,Register a) { return new CLR(pc, a); }
        public CLR(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CLS extends NONE_class { // clear S flag
        public String getName() { return "cls"; }
        static InstrPrototype prototype = new CLS(0);
        Instr allocate(int pc) { return new CLS(pc); }
        public CLS(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CLT extends NONE_class { // clear T flag
        public String getName() { return "clt"; }
        static InstrPrototype prototype = new CLT(0);
        Instr allocate(int pc) { return new CLT(pc); }
        public CLT(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CLV extends NONE_class { // clear V flag
        public String getName() { return "clv"; }
        static InstrPrototype prototype = new CLV(0);
        Instr allocate(int pc) { return new CLV(pc); }
        public CLV(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CLZ extends NONE_class { // clear Z flag
        public String getName() { return "clz"; }
        static InstrPrototype prototype = new CLZ(0);
        Instr allocate(int pc) { return new CLZ(pc); }
        public CLZ(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class COM extends REG_class { // one's compliment register
        public String getName() { return "com"; }
        static InstrPrototype prototype = new COM(0,GPR_default);
        Instr allocate(int pc,Register a) { return new COM(pc, a); }
        public COM(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CP extends REGREG_class { // compare registers
        public String getName() { return "cp"; }
        static InstrPrototype prototype = new CP(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new CP(pc, a, b); }
        public CP(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CPC extends REGREG_class { // compare registers with carry
        public String getName() { return "cpc"; }
        static InstrPrototype prototype = new CPC(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new CPC(pc, a, b); }
        public CPC(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CPI extends REGIMM_class { // compare register with immediate
        public String getName() { return "cpi"; }
        static InstrPrototype prototype = new CPI(0,HGPR_default,IMM8_default);
        Instr allocate(int pc,Register a,int b) { return new CPI(pc, a, b); }
        public CPI(int pc,Register a,int b) { super(HGPR(1, a),IMM8(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class CPSE extends REGREG_class { // compare registers and skip if equal
        public String getName() { return "cpse"; }
        static InstrPrototype prototype = new CPSE(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new CPSE(pc, a, b); }
        public CPSE(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class DEC extends REG_class { // decrement register by one
        public String getName() { return "dec"; }
        static InstrPrototype prototype = new DEC(0,GPR_default);
        Instr allocate(int pc,Register a) { return new DEC(pc, a); }
        public DEC(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class EICALL extends NONE_class { // extended indirect call
        public String getName() { return "eicall"; }
        static InstrPrototype prototype = new EICALL(0);
        Instr allocate(int pc) { return new EICALL(pc); }
        public EICALL(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 4; }
    }
    public static class EIJMP extends NONE_class { // extended indirect jump
        public String getName() { return "eijmp"; }
        static InstrPrototype prototype = new EIJMP(0);
        Instr allocate(int pc) { return new EIJMP(pc); }
        public EIJMP(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class ELPM extends NONE_class { // extended load program memory to r0
        public String getName() { return "elpm"; }
        static InstrPrototype prototype = new ELPM(0);
        Instr allocate(int pc) { return new ELPM(pc); }
        public ELPM(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 3; }
    }
    public static class ELPMD extends REGREG_class { // extended load program memory to register
        public String getName() { return "elpm"; }
        public String getVariant() { return "elpmd"; }
        static InstrPrototype prototype = new ELPMD(0,GPR_default,Z_default);
        Instr allocate(int pc,Register a,Register b) { return new ELPMD(pc, a, b); }
        public ELPMD(int pc,Register a,Register b) { super(GPR(1, a),Z(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 3; }
    }
    public static class ELPMPI extends REGREG_class { // extended load program memory to register and post-increment
        public String getName() { return "elpm"; }
        public String getVariant() { return "elpmpi"; }
        static InstrPrototype prototype = new ELPMPI(0,GPR_default,Z_default);
        Instr allocate(int pc,Register a,Register b) { return new ELPMPI(pc, a, b); }
        public ELPMPI(int pc,Register a,Register b) { super(GPR(1, a),Z(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 3; }
    }
    public static class EOR extends REGREG_class { // exclusive or register with register
        public String getName() { return "eor"; }
        static InstrPrototype prototype = new EOR(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new EOR(pc, a, b); }
        public EOR(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class FMUL extends REGREG_class { // fractional multiply register with register to r0
        public String getName() { return "fmul"; }
        static InstrPrototype prototype = new FMUL(0,MGPR_default,MGPR_default);
        Instr allocate(int pc,Register a,Register b) { return new FMUL(pc, a, b); }
        public FMUL(int pc,Register a,Register b) { super(MGPR(1, a),MGPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class FMULS extends REGREG_class { // signed fractional multiply register with register to r0
        public String getName() { return "fmuls"; }
        static InstrPrototype prototype = new FMULS(0,MGPR_default,MGPR_default);
        Instr allocate(int pc,Register a,Register b) { return new FMULS(pc, a, b); }
        public FMULS(int pc,Register a,Register b) { super(MGPR(1, a),MGPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class FMULSU extends REGREG_class { // signed/unsigned fractional multiply register with register to r0
        public String getName() { return "fmulsu"; }
        static InstrPrototype prototype = new FMULSU(0,MGPR_default,MGPR_default);
        Instr allocate(int pc,Register a,Register b) { return new FMULSU(pc, a, b); }
        public FMULSU(int pc,Register a,Register b) { super(MGPR(1, a),MGPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class ICALL extends NONE_class { // indirect call through Z register
        public String getName() { return "icall"; }
        static InstrPrototype prototype = new ICALL(0);
        Instr allocate(int pc) { return new ICALL(pc); }
        public ICALL(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 3; }
    }
    public static class IJMP extends NONE_class { // indirect jump through Z register
        public String getName() { return "ijmp"; }
        static InstrPrototype prototype = new IJMP(0);
        Instr allocate(int pc) { return new IJMP(pc); }
        public IJMP(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class IN extends REGIMM_class { // read from IO register into register
        public String getName() { return "in"; }
        static InstrPrototype prototype = new IN(0,GPR_default,IMM6_default);
        Instr allocate(int pc,Register a,int b) { return new IN(pc, a, b); }
        public IN(int pc,Register a,int b) { super(GPR(1, a),IMM6(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class INC extends REG_class { // increment register by one
        public String getName() { return "inc"; }
        static InstrPrototype prototype = new INC(0,GPR_default);
        Instr allocate(int pc,Register a) { return new INC(pc, a); }
        public INC(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class JMP extends IMM_class { // absolute jump
        public String getName() { return "jmp"; }
        static InstrPrototype prototype = new JMP(0,MEM_default);
        Instr allocate(int pc,int a) { return new JMP(pc, a); }
        public JMP(int pc,int a) { super(MEM(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getSize() { return 4; }
        public int getCycles() { return 3; }
    }
    public static class LD extends REGREG_class { // load from SRAM
        public String getName() { return "ld"; }
        static InstrPrototype prototype = new LD(0,GPR_default,ADR_default);
        Instr allocate(int pc,Register a,Register b) { return new LD(pc, a, b); }
        public LD(int pc,Register a,Register b) { super(GPR(1, a),ADR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class LDD extends REGREGIMM_class { // load from SRAM with displacement
        public String getName() { return "ldd"; }
        static InstrPrototype prototype = new LDD(0,GPR_default,YZ_default,IMM6_default);
        Instr allocate(int pc,Register a,Register b,int c) { return new LDD(pc, a, b, c); }
        public LDD(int pc,Register a,Register b,int c) { super(GPR(1, a),YZ(2, b),IMM6(3, c)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class LDI extends REGIMM_class { // load immediate into register
        public String getName() { return "ldi"; }
        static InstrPrototype prototype = new LDI(0,HGPR_default,IMM8_default);
        Instr allocate(int pc,Register a,int b) { return new LDI(pc, a, b); }
        public LDI(int pc,Register a,int b) { super(HGPR(1, a),IMM8(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class LDPD extends REGREG_class { // load from SRAM with pre-decrement
        public String getName() { return "ld"; }
        public String getVariant() { return "ldpd"; }
        static InstrPrototype prototype = new LDPD(0,GPR_default,ADR_default);
        Instr allocate(int pc,Register a,Register b) { return new LDPD(pc, a, b); }
        public LDPD(int pc,Register a,Register b) { super(GPR(1, a),ADR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class LDPI extends REGREG_class { // load from SRAM with post-increment
        public String getName() { return "ld"; }
        public String getVariant() { return "ldpi"; }
        static InstrPrototype prototype = new LDPI(0,GPR_default,ADR_default);
        Instr allocate(int pc,Register a,Register b) { return new LDPI(pc, a, b); }
        public LDPI(int pc,Register a,Register b) { super(GPR(1, a),ADR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class LDS extends REGIMM_class { // load direct from SRAM
        public String getName() { return "lds"; }
        static InstrPrototype prototype = new LDS(0,GPR_default,MEM_default);
        Instr allocate(int pc,Register a,int b) { return new LDS(pc, a, b); }
        public LDS(int pc,Register a,int b) { super(GPR(1, a),MEM(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getSize() { return 4; }
        public int getCycles() { return 2; }
    }
    public static class LPM extends NONE_class { // load program memory into r0
        public String getName() { return "lpm"; }
        static InstrPrototype prototype = new LPM(0);
        Instr allocate(int pc) { return new LPM(pc); }
        public LPM(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 3; }
    }
    public static class LPMD extends REGREG_class { // load program memory into register
        public String getName() { return "lpm"; }
        public String getVariant() { return "lpmd"; }
        static InstrPrototype prototype = new LPMD(0,GPR_default,Z_default);
        Instr allocate(int pc,Register a,Register b) { return new LPMD(pc, a, b); }
        public LPMD(int pc,Register a,Register b) { super(GPR(1, a),Z(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 3; }
    }
    public static class LPMPI extends REGREG_class { // load program memory into register and post-increment
        public String getName() { return "lpm"; }
        public String getVariant() { return "lpmpi"; }
        static InstrPrototype prototype = new LPMPI(0,GPR_default,Z_default);
        Instr allocate(int pc,Register a,Register b) { return new LPMPI(pc, a, b); }
        public LPMPI(int pc,Register a,Register b) { super(GPR(1, a),Z(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 3; }
    }
    public static class LSL extends REG_class { // logical shift left
        public String getName() { return "lsl"; }
        static InstrPrototype prototype = new LSL(0,GPR_default);
        Instr allocate(int pc,Register a) { return new LSL(pc, a); }
        public LSL(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class LSR extends REG_class { // logical shift right
        public String getName() { return "lsr"; }
        static InstrPrototype prototype = new LSR(0,GPR_default);
        Instr allocate(int pc,Register a) { return new LSR(pc, a); }
        public LSR(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class MOV extends REGREG_class { // copy register to register
        public String getName() { return "mov"; }
        static InstrPrototype prototype = new MOV(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new MOV(pc, a, b); }
        public MOV(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class MOVW extends REGREG_class { // copy two registers to two registers
        public String getName() { return "movw"; }
        static InstrPrototype prototype = new MOVW(0,EGPR_default,EGPR_default);
        Instr allocate(int pc,Register a,Register b) { return new MOVW(pc, a, b); }
        public MOVW(int pc,Register a,Register b) { super(EGPR(1, a),EGPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class MUL extends REGREG_class { // multiply register with register to r0
        public String getName() { return "mul"; }
        static InstrPrototype prototype = new MUL(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new MUL(pc, a, b); }
        public MUL(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class MULS extends REGREG_class { // signed multiply register with register to r0
        public String getName() { return "muls"; }
        static InstrPrototype prototype = new MULS(0,HGPR_default,HGPR_default);
        Instr allocate(int pc,Register a,Register b) { return new MULS(pc, a, b); }
        public MULS(int pc,Register a,Register b) { super(HGPR(1, a),HGPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class MULSU extends REGREG_class { // signed/unsigned multiply register with register to r0
        public String getName() { return "mulsu"; }
        static InstrPrototype prototype = new MULSU(0,MGPR_default,MGPR_default);
        Instr allocate(int pc,Register a,Register b) { return new MULSU(pc, a, b); }
        public MULSU(int pc,Register a,Register b) { super(MGPR(1, a),MGPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class NEG extends REG_class { // two's complement register
        public String getName() { return "neg"; }
        static InstrPrototype prototype = new NEG(0,GPR_default);
        Instr allocate(int pc,Register a) { return new NEG(pc, a); }
        public NEG(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class NOP extends NONE_class { // do nothing operation
        public String getName() { return "nop"; }
        static InstrPrototype prototype = new NOP(0);
        Instr allocate(int pc) { return new NOP(pc); }
        public NOP(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class OR extends REGREG_class { // or register with register
        public String getName() { return "or"; }
        static InstrPrototype prototype = new OR(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new OR(pc, a, b); }
        public OR(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class ORI extends REGIMM_class { // or register with immediate
        public String getName() { return "ori"; }
        static InstrPrototype prototype = new ORI(0,HGPR_default,IMM8_default);
        Instr allocate(int pc,Register a,int b) { return new ORI(pc, a, b); }
        public ORI(int pc,Register a,int b) { super(HGPR(1, a),IMM8(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class OUT extends IMMREG_class { // write from register to IO register
        public String getName() { return "out"; }
        static InstrPrototype prototype = new OUT(0,IMM6_default,GPR_default);
        Instr allocate(int pc,int a,Register b) { return new OUT(pc, a, b); }
        public OUT(int pc,int a,Register b) { super(IMM6(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class POP extends REG_class { // pop from the stack to register
        public String getName() { return "pop"; }
        static InstrPrototype prototype = new POP(0,GPR_default);
        Instr allocate(int pc,Register a) { return new POP(pc, a); }
        public POP(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class PUSH extends REG_class { // push register to the stack
        public String getName() { return "push"; }
        static InstrPrototype prototype = new PUSH(0,GPR_default);
        Instr allocate(int pc,Register a) { return new PUSH(pc, a); }
        public PUSH(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class RCALL extends IMM_class { // relative call
        public String getName() { return "rcall"; }
        static InstrPrototype prototype = new RCALL(0,LREL_default);
        Instr allocate(int pc,int a) { return new RCALL(pc, a); }
        public RCALL(int pc,int a) { super(LREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 3; }
    }
    public static class RET extends NONE_class { // return to caller
        public String getName() { return "ret"; }
        static InstrPrototype prototype = new RET(0);
        Instr allocate(int pc) { return new RET(pc); }
        public RET(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 4; }
    }
    public static class RETI extends NONE_class { // return from interrupt
        public String getName() { return "reti"; }
        static InstrPrototype prototype = new RETI(0);
        Instr allocate(int pc) { return new RETI(pc); }
        public RETI(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 4; }
    }
    public static class RJMP extends IMM_class { // relative jump
        public String getName() { return "rjmp"; }
        static InstrPrototype prototype = new RJMP(0,LREL_default);
        Instr allocate(int pc,int a) { return new RJMP(pc, a); }
        public RJMP(int pc,int a) { super(LREL(pc, 1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class ROL extends REG_class { // rotate left through carry flag
        public String getName() { return "rol"; }
        static InstrPrototype prototype = new ROL(0,GPR_default);
        Instr allocate(int pc,Register a) { return new ROL(pc, a); }
        public ROL(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class ROR extends REG_class { // rotate right through carry flag
        public String getName() { return "ror"; }
        static InstrPrototype prototype = new ROR(0,GPR_default);
        Instr allocate(int pc,Register a) { return new ROR(pc, a); }
        public ROR(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SBC extends REGREG_class { // subtract register from register with carry
        public String getName() { return "sbc"; }
        static InstrPrototype prototype = new SBC(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new SBC(pc, a, b); }
        public SBC(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SBCI extends REGIMM_class { // subtract immediate from register with carry
        public String getName() { return "sbci"; }
        static InstrPrototype prototype = new SBCI(0,HGPR_default,IMM8_default);
        Instr allocate(int pc,Register a,int b) { return new SBCI(pc, a, b); }
        public SBCI(int pc,Register a,int b) { super(HGPR(1, a),IMM8(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SBI extends IMMIMM_class { // set bit in IO register
        public String getName() { return "sbi"; }
        static InstrPrototype prototype = new SBI(0,IMM5_default,IMM3_default);
        Instr allocate(int pc,int a,int b) { return new SBI(pc, a, b); }
        public SBI(int pc,int a,int b) { super(IMM5(1, a),IMM3(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class SBIC extends IMMIMM_class { // skip if bit in IO register is clear
        public String getName() { return "sbic"; }
        static InstrPrototype prototype = new SBIC(0,IMM5_default,IMM3_default);
        Instr allocate(int pc,int a,int b) { return new SBIC(pc, a, b); }
        public SBIC(int pc,int a,int b) { super(IMM5(1, a),IMM3(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SBIS extends IMMIMM_class { // skip if bit in IO register is set
        public String getName() { return "sbis"; }
        static InstrPrototype prototype = new SBIS(0,IMM5_default,IMM3_default);
        Instr allocate(int pc,int a,int b) { return new SBIS(pc, a, b); }
        public SBIS(int pc,int a,int b) { super(IMM5(1, a),IMM3(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SBIW extends REGIMM_class { // subtract immediate from word 
        public String getName() { return "sbiw"; }
        static InstrPrototype prototype = new SBIW(0,RDL_default,IMM6_default);
        Instr allocate(int pc,Register a,int b) { return new SBIW(pc, a, b); }
        public SBIW(int pc,Register a,int b) { super(RDL(1, a),IMM6(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class SBR extends REGIMM_class { // set bits in register
        public String getName() { return "sbr"; }
        static InstrPrototype prototype = new SBR(0,HGPR_default,IMM8_default);
        Instr allocate(int pc,Register a,int b) { return new SBR(pc, a, b); }
        public SBR(int pc,Register a,int b) { super(HGPR(1, a),IMM8(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SBRC extends REGIMM_class { // skip if bit in register cleared
        public String getName() { return "sbrc"; }
        static InstrPrototype prototype = new SBRC(0,GPR_default,IMM3_default);
        Instr allocate(int pc,Register a,int b) { return new SBRC(pc, a, b); }
        public SBRC(int pc,Register a,int b) { super(GPR(1, a),IMM3(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SBRS extends REGIMM_class { // skip if bit in register set
        public String getName() { return "sbrs"; }
        static InstrPrototype prototype = new SBRS(0,GPR_default,IMM3_default);
        Instr allocate(int pc,Register a,int b) { return new SBRS(pc, a, b); }
        public SBRS(int pc,Register a,int b) { super(GPR(1, a),IMM3(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SEC extends NONE_class { // set C (carry) flag
        public String getName() { return "sec"; }
        static InstrPrototype prototype = new SEC(0);
        Instr allocate(int pc) { return new SEC(pc); }
        public SEC(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SEH extends NONE_class { // set H (half carry) flag
        public String getName() { return "seh"; }
        static InstrPrototype prototype = new SEH(0);
        Instr allocate(int pc) { return new SEH(pc); }
        public SEH(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SEI extends NONE_class { // set I (interrupt enable) flag
        public String getName() { return "sei"; }
        static InstrPrototype prototype = new SEI(0);
        Instr allocate(int pc) { return new SEI(pc); }
        public SEI(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SEN extends NONE_class { // set N (negative) flag
        public String getName() { return "sen"; }
        static InstrPrototype prototype = new SEN(0);
        Instr allocate(int pc) { return new SEN(pc); }
        public SEN(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SER extends REG_class { // set bits in register
        public String getName() { return "ser"; }
        static InstrPrototype prototype = new SER(0,GPR_default);
        Instr allocate(int pc,Register a) { return new SER(pc, a); }
        public SER(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SES extends NONE_class { // set S (signed) flag
        public String getName() { return "ses"; }
        static InstrPrototype prototype = new SES(0);
        Instr allocate(int pc) { return new SES(pc); }
        public SES(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SET extends NONE_class { // set T flag
        public String getName() { return "set"; }
        static InstrPrototype prototype = new SET(0);
        Instr allocate(int pc) { return new SET(pc); }
        public SET(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SEV extends NONE_class { // set V (overflow) flag
        public String getName() { return "sev"; }
        static InstrPrototype prototype = new SEV(0);
        Instr allocate(int pc) { return new SEV(pc); }
        public SEV(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SEZ extends NONE_class { // set Z (zero) flag
        public String getName() { return "sez"; }
        static InstrPrototype prototype = new SEZ(0);
        Instr allocate(int pc) { return new SEZ(pc); }
        public SEZ(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SLEEP extends NONE_class { // enter sleep mode
        public String getName() { return "sleep"; }
        static InstrPrototype prototype = new SLEEP(0);
        Instr allocate(int pc) { return new SLEEP(pc); }
        public SLEEP(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SPM extends NONE_class { // store to program memory from r0
        public String getName() { return "spm"; }
        static InstrPrototype prototype = new SPM(0);
        Instr allocate(int pc) { return new SPM(pc); }
        public SPM(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class ST extends REGREG_class { // store from register to SRAM
        public String getName() { return "st"; }
        static InstrPrototype prototype = new ST(0,ADR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new ST(pc, a, b); }
        public ST(int pc,Register a,Register b) { super(ADR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class STD extends REGIMMREG_class { // store from register to SRAM with displacement
        public String getName() { return "std"; }
        static InstrPrototype prototype = new STD(0,YZ_default,IMM6_default,GPR_default);
        Instr allocate(int pc,Register a,int b,Register c) { return new STD(pc, a, b, c); }
        public STD(int pc,Register a,int b,Register c) { super(YZ(1, a),IMM6(2, b),GPR(3, c)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class STPD extends REGREG_class { // store from register to SRAM with pre-decrement
        public String getName() { return "st"; }
        public String getVariant() { return "stpd"; }
        static InstrPrototype prototype = new STPD(0,ADR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new STPD(pc, a, b); }
        public STPD(int pc,Register a,Register b) { super(ADR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class STPI extends REGREG_class { // store from register to SRAM with post-increment
        public String getName() { return "st"; }
        public String getVariant() { return "stpi"; }
        static InstrPrototype prototype = new STPI(0,ADR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new STPI(pc, a, b); }
        public STPI(int pc,Register a,Register b) { super(ADR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getCycles() { return 2; }
    }
    public static class STS extends IMMREG_class { // store direct to SRAM
        public String getName() { return "sts"; }
        static InstrPrototype prototype = new STS(0,MEM_default,GPR_default);
        Instr allocate(int pc,int a,Register b) { return new STS(pc, a, b); }
        public STS(int pc,int a,Register b) { super(MEM(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
        public int getSize() { return 4; }
        public int getCycles() { return 2; }
    }
    public static class SUB extends REGREG_class { // subtract register from register
        public String getName() { return "sub"; }
        static InstrPrototype prototype = new SUB(0,GPR_default,GPR_default);
        Instr allocate(int pc,Register a,Register b) { return new SUB(pc, a, b); }
        public SUB(int pc,Register a,Register b) { super(GPR(1, a),GPR(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SUBI extends REGIMM_class { // subtract immediate from register
        public String getName() { return "subi"; }
        static InstrPrototype prototype = new SUBI(0,HGPR_default,IMM8_default);
        Instr allocate(int pc,Register a,int b) { return new SUBI(pc, a, b); }
        public SUBI(int pc,Register a,int b) { super(HGPR(1, a),IMM8(2, b)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class SWAP extends REG_class { // swap nibbles in register
        public String getName() { return "swap"; }
        static InstrPrototype prototype = new SWAP(0,GPR_default);
        Instr allocate(int pc,Register a) { return new SWAP(pc, a); }
        public SWAP(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class TST extends REG_class { // compare registers
        public String getName() { return "tst"; }
        static InstrPrototype prototype = new TST(0,GPR_default);
        Instr allocate(int pc,Register a) { return new TST(pc, a); }
        public TST(int pc,Register a) { super(GPR(1, a)); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
    public static class WDR extends NONE_class { // watchdog timer reset
        public String getName() { return "wdr"; }
        static InstrPrototype prototype = new WDR(0);
        Instr allocate(int pc) { return new WDR(pc); }
        public WDR(int pc) { super(); }
        public void accept(InstrVisitor v) { v.visit(this); }
    }
//--END INSTR GENERATOR--

}
