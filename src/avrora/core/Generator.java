package avrora.core;

import avrora.util.SectionFile;
import avrora.util.StringUtil;
import avrora.util.Terminal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>Generator</code> class generates Java source code that implements
 * the description of each instruction. Since there is more than 1,000 lines of
 * redundant code (and painfully boring) source code, a generator was designed
 * to spit out this boringly straightforward source code from this internal
 * description.
 * <p/>
 * This makes adding new instructions easy, but necessitates changes to any
 * visitor implementations to account for the new instruction.
 *
 * @author Ben L. Titzer
 */
public class Generator {

    public static boolean CLEAR;

    static class Operand {
        String constraint;
        private String type;

        Operand(String t, String c) {
            type = t;
            constraint = c;
        }

        public String getType() {
            return type;
        }

        public String getConstraint(int num) {
            return constraint + "(" + num + ", " + StringUtil.alpha(num) + ")";
        }
    }

    static class RelOperand extends Operand {
        RelOperand(String t, String c) {
            super(t, c);
        }

        public String getConstraint(int num) {
            return constraint + "(pc, " + num + ", " + StringUtil.alpha(num) + ")";
        }
    }


    static class ParamTypes {

        String baseClass;
        String defaults;
        String formals;
        String params;
        String constraints;

        ParamTypes(String bc) {
            baseClass = bc;
            defaults = "0";
            formals = "int pc";
            params = "pc";
            constraints = "";
        }

        ParamTypes(String bc, Operand o1) {
            baseClass = bc;
            defaults = StringUtil.commalist("0", d(o1));
            formals = StringUtil.commalist("int pc", o1.getType() + " a");
            params = "pc, a";
            constraints = c(1, o1);
        }

        ParamTypes(String bc, Operand o1, Operand o2) {
            baseClass = bc;
            defaults = StringUtil.commalist("0", d(o1), d(o2));
            formals = StringUtil.commalist("int pc", p(1, o1), p(2, o2));
            params = "pc, a, b";
            constraints = StringUtil.commalist(c(1, o1), c(2, o2));
        }

        ParamTypes(String bc, Operand o1, Operand o2, Operand o3) {
            baseClass = bc;
            defaults = StringUtil.commalist("0", d(o1), d(o2), d(o3));
            formals = StringUtil.commalist("int pc", p(1, o1), p(2, o2), p(3, o3));
            params = "pc, a, b, c";
            constraints = StringUtil.commalist(c(1, o1), c(2, o2), c(3, o3));
        }

        private String d(Operand o) {
            return o.constraint + "_default";
        }

        String p(int num, Operand o) {
            return o.getType() + " " + StringUtil.alpha(num);
        }

        String c(int num, Operand o) {
            return o.getConstraint(num);
        }
    }

    static class Description {
        String variant;
        String VARIANT;
        String name;
        String comment;
        boolean large;
        ParamTypes params;
        int cycles;

        String className;

        Description(String n, ParamTypes p, int c, String com) {
            variant = name = n;
            VARIANT = variant.toUpperCase();
            params = p;
            className = "Instr." + VARIANT;
            cycles = c;
            comment = com;
        }

        Description(String v, String n, ParamTypes p, int c, String com) {
            variant = v;
            name = n;
            VARIANT = variant.toUpperCase();
            params = p;
            className = "Instr." + VARIANT;
            cycles = c;
            comment = com;
        }

        void writeVisitMethod(SectionFile f) throws java.io.IOException {
            f.writeLine("    public void visit(" + className + " i); // " + comment);
        }

        void writeSetInsert(SectionFile f) throws java.io.IOException {
            f.writeLine("        instructions.put(" + StringUtil.quote(variant) + ", " + className + ".prototype);");
        }

        void writeClassDecl(SectionFile f) throws java.io.IOException {
            f.writeLine("    public static class " + VARIANT + " extends " + params.baseClass + " { // " + comment);
            f.writeLine("        public String getName() { return " + StringUtil.quote(name) + "; }");

            if (variant != name)
                f.writeLine("        public String getVariant() { return " + StringUtil.quote(variant) + "; }");
            f.writeLine("        static InstrPrototype prototype = new " + VARIANT + "(" + params.defaults + ");");
            f.writeLine("        Instr allocate(" + params.formals + ") { return new " + VARIANT + "(" + params.params + "); }");
            f.writeLine("        public " + VARIANT + "(" + params.formals + ") { super(" + params.constraints + "); }");
            f.writeLine("        public void accept(InstrVisitor v) { v.visit(this); }");
            if (large)
                f.writeLine("        public int getSize() { return 4; }");
            if (cycles != 1)
                f.writeLine("        public int getCycles() { return " + cycles + "; }");
            f.writeLine("    }");
        }

    }

    static Operand GPR = new Operand("Register", "GPR");
    static Operand HGPR = new Operand("Register", "HGPR");
    static Operand MGPR = new Operand("Register", "MGPR");
    static Operand EGPR = new Operand("Register", "EGPR");
    static Operand ADR = new Operand("Register", "ADR");
    static Operand RDL = new Operand("Register", "RDL");
    static Operand YZ = new Operand("Register", "YZ");
    static Operand Z = new Operand("Register", "Z");

    static Operand IMM3 = new Operand("int", "IMM3");
    static Operand IMM5 = new Operand("int", "IMM5");
    static Operand IMM6 = new Operand("int", "IMM6");
    static Operand IMM8 = new Operand("int", "IMM8");
    static Operand SREL = new RelOperand("int", "SREL");
    static Operand LREL = new RelOperand("int", "LREL");
    static Operand DADDR = new Operand("int", "DADDR");
    static Operand PADDR = new Operand("int", "PADDR");

    static ParamTypes GPR_GPR = new ParamTypes("REGREG_class", GPR, GPR);
    static ParamTypes MGPR_MGPR = new ParamTypes("REGREG_class", MGPR, MGPR);
    static ParamTypes EGPR_EGPR = new ParamTypes("REGREG_class", EGPR, EGPR);
    static ParamTypes HGPR_HGPR = new ParamTypes("REGREG_class", HGPR, HGPR);
    static ParamTypes HGPR_IMM8 = new ParamTypes("REGIMM_class", HGPR, IMM8);
    static ParamTypes RDL_IMM6 = new ParamTypes("REGIMM_class", RDL, IMM6);
    static ParamTypes GPR_DADDR = new ParamTypes("REGIMM_class", GPR, DADDR);
    static ParamTypes NONE = new ParamTypes("NONE_class");
    static ParamTypes GPR_ = new ParamTypes("REG_class", GPR);
    static ParamTypes GPR_IMM3 = new ParamTypes("REGIMM_class", GPR, IMM3);
    static ParamTypes GPR_IMM6 = new ParamTypes("REGIMM_class", GPR, IMM6);
    static ParamTypes IMM5_IMM3 = new ParamTypes("IMMIMM_class", IMM5, IMM3);
    static ParamTypes GPR_Z = new ParamTypes("REGREG_class", GPR, Z);
    static ParamTypes GPR_ADR = new ParamTypes("REGREG_class", GPR, ADR);
    static ParamTypes ADR_GPR = new ParamTypes("REGREG_class", ADR, GPR);
    static ParamTypes GPR_YZ_IMM6 = new ParamTypes("REGREGIMM_class", GPR, YZ, IMM6);
    static ParamTypes YZ_IMM6_GPR = new ParamTypes("REGIMMREG_class", YZ, IMM6, GPR);
    static ParamTypes IMM3_ = new ParamTypes("IMM_class", IMM3);
    static ParamTypes IMM3_SREL = new ParamTypes("IMMWORD_class", IMM3, SREL); // fixme ??
    static ParamTypes SREL_ = new ParamTypes("WORD_class", SREL); // fixme ??
    static ParamTypes PADDR_ = new ParamTypes("WORD_class", PADDR); // fixme ??
    static ParamTypes LREL_ = new ParamTypes("WORD_class", LREL); // fixme ??

    static ParamTypes DADDR_GPR = new ParamTypes("IMMREG_class", DADDR, GPR);
    static ParamTypes IMM6_GPR = new ParamTypes("IMMREG_class", IMM6, GPR);

    private static List list;

    static {

        list = new LinkedList();
        int _1cyc = 1; // just for clarity in each declaration
        int _2cyc = 2;
        int _3cyc = 3;
        int _4cyc = 4;

        instr2("adc", GPR_GPR, _1cyc, "add register to register with carry");
        instr2("add", GPR_GPR, _1cyc, "add register to register");
        instr2("adiw", RDL_IMM6, _2cyc, "add immediate to word register");
        instr2("and", GPR_GPR, _1cyc, "and register with register");
        instr2("andi", HGPR_IMM8, _1cyc, "and register with immediate");
        instr2("asr", GPR_, _1cyc, "arithmetic shift right");
        instr2("bclr", IMM3_, _1cyc, "clear bit in status register");
        instr2("bld", GPR_IMM3, _1cyc, "load bit from T flag into register");
        instr2("brbc", IMM3_SREL, _1cyc, "branch if bit in status register is clear");
        instr2("brbs", IMM3_SREL, _1cyc, "branch if bit in status register is set");
        instr2("brcc", SREL_, _1cyc, "branch if carry flag is clear");
        instr2("brcs", SREL_, _1cyc, "branch if carry flag is set");
        instr2("break", NONE, _1cyc, "break");
        instr2("breq", SREL_, _1cyc, "branch if equal");
        instr2("brge", SREL_, _1cyc, "branch if greater or equal (signed)");
        instr2("brhc", SREL_, _1cyc, "branch if H flag is clear");
        instr2("brhs", SREL_, _1cyc, "branch if H flag is set");
        instr2("brid", SREL_, _1cyc, "branch if interrupts are disabled");
        instr2("brie", SREL_, _1cyc, "branch if interrupts are enabled");
        instr2("brlo", SREL_, _1cyc, "branch if lower");
        instr2("brlt", SREL_, _1cyc, "branch if less than zero (signed)");
        instr2("brmi", SREL_, _1cyc, "branch if minus");
        instr2("brne", SREL_, _1cyc, "branch if not equal");
        instr2("brpl", SREL_, _1cyc, "branch if positive");
        instr2("brsh", SREL_, _1cyc, "branch if same or higher");
        instr2("brtc", SREL_, _1cyc, "branch if T flag is clear");
        instr2("brts", SREL_, _1cyc, "branch if T flag is set");
        instr2("brvc", SREL_, _1cyc, "branch if V flag is clear");
        instr2("brvs", SREL_, _1cyc, "branch if V flag is set");
        instr2("bset", IMM3_, _1cyc, "set flag in status register");
        instr2("bst", GPR_IMM3, _1cyc, "store bit in register into T flag");
        instr4("call", PADDR_, _4cyc, "call absolute address");
        instr2("cbi", IMM5_IMM3, _2cyc, "clear bit in IO register");
        instr2("cbr", HGPR_IMM8, _1cyc, "clear bits in register");
        instr2("clc", NONE, _1cyc, "clear C flag");
        instr2("clh", NONE, _1cyc, "clear H flag");
        instr2("cli", NONE, _1cyc, "clear I flag");
        instr2("cln", NONE, _1cyc, "clear N flag");
        instr2("clr", GPR_, _1cyc, "clear register (set to zero)");
        instr2("cls", NONE, _1cyc, "clear S flag");
        instr2("clt", NONE, _1cyc, "clear T flag");
        instr2("clv", NONE, _1cyc, "clear V flag");
        instr2("clz", NONE, _1cyc, "clear Z flag");
        instr2("com", GPR_, _1cyc, "one's compliment register");
        instr2("cp", GPR_GPR, _1cyc, "compare registers");
        instr2("cpc", GPR_GPR, _1cyc, "compare registers with carry");
        instr2("cpi", HGPR_IMM8, _1cyc, "compare register with immediate");
        instr2("cpse", GPR_GPR, _1cyc, "compare registers and skip if equal");
        instr2("dec", GPR_, _1cyc, "decrement register by one");
        instr2("eicall", NONE, _4cyc, "extended indirect call");
        instr2("eijmp", NONE, _2cyc, "extended indirect jump");
        instr2("elpm", NONE, _3cyc, "extended load program memory to r0");
        instr2("elpmd", "elpm", GPR_Z, _3cyc, "extended load program memory to register"); // variant
        instr2("elpmpi", "elpm", GPR_Z, _3cyc, "extended load program memory to register and post-increment"); // variant
        instr2("eor", GPR_GPR, _1cyc, "exclusive or register with register");
        instr2("fmul", MGPR_MGPR, _2cyc, "fractional multiply register with register to r0");
        instr2("fmuls", MGPR_MGPR, _2cyc, "signed fractional multiply register with register to r0");
        instr2("fmulsu", MGPR_MGPR, _2cyc, "signed/unsigned fractional multiply register with register to r0");
        instr2("icall", NONE, _3cyc, "indirect call through Z register");
        instr2("ijmp", NONE, _2cyc, "indirect jump through Z register");
        instr2("in", GPR_IMM6, _1cyc, "read from IO register into register");
        instr2("inc", GPR_, _1cyc, "increment register by one");
        instr4("jmp", PADDR_, _3cyc, "absolute jump");
        instr2("ld", "ld", GPR_ADR, _2cyc, "load from SRAM");
        instr2("ldd", GPR_YZ_IMM6, _2cyc, "load from SRAM with displacement");
        instr2("ldi", HGPR_IMM8, _1cyc, "load immediate into register");
        instr2("ldpd", "ld", GPR_ADR, _2cyc, "load from SRAM with pre-decrement"); // variant
        instr2("ldpi", "ld", GPR_ADR, _2cyc, "load from SRAM with post-increment"); // variant
        instr4("lds", GPR_DADDR, _2cyc, "load direct from SRAM");
        instr2("lpm", NONE, _3cyc, "load program memory into r0");
        instr2("lpmd", "lpm", GPR_Z, _3cyc, "load program memory into register"); // variant
        instr2("lpmpi", "lpm", GPR_Z, _3cyc, "load program memory into register and post-increment"); // variant
        instr2("lsl", GPR_, _1cyc, "logical shift left");
        instr2("lsr", GPR_, _1cyc, "logical shift right");
        instr2("mov", GPR_GPR, _1cyc, "copy register to register");
        instr2("movw", EGPR_EGPR, _1cyc, "copy two registers to two registers");
        instr2("mul", GPR_GPR, _2cyc, "multiply register with register to r0");
        instr2("muls", HGPR_HGPR, _2cyc, "signed multiply register with register to r0");
        instr2("mulsu", MGPR_MGPR, _2cyc, "signed/unsigned multiply register with register to r0");
        instr2("neg", GPR_, _1cyc, "two's complement register");
        instr2("nop", NONE, _1cyc, "do nothing operation");
        instr2("or", GPR_GPR, _1cyc, "or register with register");
        instr2("ori", HGPR_IMM8, _1cyc, "or register with immediate");
        instr2("out", IMM6_GPR, _1cyc, "write from register to IO register");
        instr2("pop", GPR_, _2cyc, "pop from the stack to register");
        instr2("push", GPR_, _2cyc, "push register to the stack");
        instr2("rcall", LREL_, _3cyc, "relative call");
        instr2("ret", NONE, _4cyc, "return to caller");
        instr2("reti", NONE, _4cyc, "return from interrupt");
        instr2("rjmp", LREL_, _2cyc, "relative jump");
        instr2("rol", GPR_, _1cyc, "rotate left through carry flag");
        instr2("ror", GPR_, _1cyc, "rotate right through carry flag");
        instr2("sbc", GPR_GPR, _1cyc, "subtract register from register with carry");
        instr2("sbci", HGPR_IMM8, _1cyc, "subtract immediate from register with carry");
        instr2("sbi", IMM5_IMM3, _2cyc, "set bit in IO register");
        instr2("sbic", IMM5_IMM3, _1cyc, "skip if bit in IO register is clear");
        instr2("sbis", IMM5_IMM3, _1cyc, "skip if bit in IO register is set");
        instr2("sbiw", RDL_IMM6, _2cyc, "subtract immediate from word ");
        instr2("sbr", HGPR_IMM8, _1cyc, "set bits in register");
        instr2("sbrc", GPR_IMM3, _1cyc, "skip if bit in register cleared");
        instr2("sbrs", GPR_IMM3, _1cyc, "skip if bit in register set");
        instr2("sec", NONE, _1cyc, "set C (carry) flag");
        instr2("seh", NONE, _1cyc, "set H (half carry) flag");
        instr2("sei", NONE, _1cyc, "set I (interrupt enable) flag");
        instr2("sen", NONE, _1cyc, "set N (negative) flag");
        instr2("ser", GPR_, _1cyc, "set bits in register");
        instr2("ses", NONE, _1cyc, "set S (signed) flag");
        instr2("set", NONE, _1cyc, "set T flag");
        instr2("sev", NONE, _1cyc, "set V (overflow) flag");
        instr2("sez", NONE, _1cyc, "set Z (zero) flag");
        instr2("sleep", NONE, _1cyc, "enter sleep mode");
        instr2("spm", NONE, _1cyc, "store to program memory from r0");
        instr2("st", "st", ADR_GPR, _2cyc, "store from register to SRAM");
        instr2("std", YZ_IMM6_GPR, _2cyc, "store from register to SRAM with displacement");
        instr2("stpd", "st", ADR_GPR, _2cyc, "store from register to SRAM with pre-decrement"); // variant
        instr2("stpi", "st", ADR_GPR, _2cyc, "store from register to SRAM with post-increment"); // variant
        instr4("sts", DADDR_GPR, _2cyc, "store direct to SRAM");
        instr2("sub", GPR_GPR, _1cyc, "subtract register from register");
        instr2("subi", HGPR_IMM8, _1cyc, "subtract immediate from register");
        instr2("swap", GPR_, _1cyc, "swap nibbles in register");
        instr2("tst", GPR_, _1cyc, "test for zero or minus");
        instr2("wdr", NONE, _1cyc, "watchdog timer reset");

    }


    static void instr2(String name, ParamTypes types, int cycles, String comment) {
        add(new Description(name, types, cycles, comment));
    }

    static void instr2(String variant, String name, ParamTypes types, int cycles, String comment) {
        add(new Description(variant, name, types, cycles, comment));
    }

    static void instr4(String name, ParamTypes types, int cycles, String comment) {
        Description d = new Description(name, types, cycles, comment);
        d.large = true;
        add(d);
    }

    static void add(Description d) {
        list.add(d);
    }

    public void generate() throws java.io.IOException {
        SectionFile visitor = new SectionFile("InstrVisitor.java", "INSTRVISITOR GENERATOR");
        SectionFile instr = new SectionFile("Instr.java", "INSTR GENERATOR");
        SectionFile instrset = new SectionFile("InstructionSet.java", "INSTRUCTIONSET GENERATOR");

        if (!CLEAR) {
            Iterator i = list.iterator();
            while (i.hasNext()) {
                Description d = (Description) i.next();
                d.writeClassDecl(instr);
                d.writeSetInsert(instrset);
                d.writeVisitMethod(visitor);
            }
        } else {
            instr.writeLine("    // empty");
            visitor.writeLine("    // empty");
            instrset.writeLine("    // empty");
        }

        instr.close();
        visitor.close();
        instrset.close();
    }

    public static void main(String[] args) throws java.io.IOException {
        avrora.Main.parseOptions(args);
        Generator g = new Generator();

        Terminal.printBrightBlue("AVR Instruction Set Class Generator");
        Terminal.print(" - (c) 2004 Ben L. Titzer\n\n");
        Terminal.println("This is an instruction set generator. It generates Java");
        Terminal.println("source code that implement the classes for the AVR instruction");
        Terminal.println("set analysis tools. The files modified are: \n");
        Terminal.println("   Instr.java");
        Terminal.println("   InstructionSet.java");
        Terminal.println("   InstrVisitor.java\n");
        Terminal.printRed("You must rebuild the analysis tools after running the generator.");
        Terminal.nextln();

        g.generate();
    }

}
