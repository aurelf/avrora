package avrora.sir;

import vpc.VPCBase;
import vpc.util.Printer;
import vpc.util.SectionFile;
import vpc.util.ColorTerminal;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

/**
 * The <code>Generator</code> class generates Java source code that implements
 * the description of each instruction. Since there is more than 1,000 lines of
 * redundant code (and painfully boring) source code, a generator was designed
 * to spit out this boringly straightforward source code from this internal
 * description.
 *
 * This makes adding new instructions easy, but necessitates changes to any
 * visitor implementations to account for the new instruction.
 *
 * @author Ben L. Titzer
 */
public class Generator extends VPCBase {

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
            return constraint + "(" + num + ", " + alpha(num) + ")";
        }
    }

    static class RelOperand extends Operand {
        RelOperand(String t, String c) {
            super(t, c);
        }

        public String getConstraint(int num) {
            return constraint + "(pc, " + num + ", " + alpha(num) + ")";
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
            defaults = commalist("0", d(o1));
            formals = commalist("int pc", o1.getType() + " a");
            params = "pc, a";
            constraints = c(1, o1);
        }

        ParamTypes(String bc, Operand o1, Operand o2) {
            baseClass = bc;
            defaults = commalist("0", d(o1), d(o2));
            formals = commalist("int pc", p(1, o1), p(2, o2));
            params = "pc, a, b";
            constraints = commalist(c(1, o1), c(2, o2));
        }

        ParamTypes(String bc, Operand o1, Operand o2, Operand o3) {
            baseClass = bc;
            defaults = commalist("0", d(o1), d(o2), d(o3));
            formals = commalist("int pc", p(1, o1), p(2, o2), p(3, o3));
            params = "pc, a, b, c";
            constraints = commalist(c(1, o1), c(2, o2), c(3, o3));
        }

        private String d(Operand o) {
            return o.constraint + "_default";
        }

        String p(int num, Operand o) {
            return o.getType() + " " + alpha(num);
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
            f.writeLine("    public void visit(" + className + " i); // "+comment);
        }

        void writeSetInsert(SectionFile f) throws java.io.IOException {
            f.writeLine("        instructions.put(" + quote(variant) + ", " + className + ".prototype);");
        }

        void writeClassDecl(SectionFile f) throws java.io.IOException {
            f.writeLine("    public static class " + VARIANT + " extends " + params.baseClass + " { // "+comment);
            f.writeLine("        public String getName() { return " + quote(name) + "; }");

            if (variant != name)
                f.writeLine("        public String getVariant() { return " + quote(variant) + "; }");
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
    static Operand MEM = new Operand("int", "MEM");

    static ParamTypes GPR_GPR = new ParamTypes("REGREG_class", GPR, GPR);
    static ParamTypes MGPR_MGPR = new ParamTypes("REGREG_class", MGPR, MGPR);
    static ParamTypes EGPR_EGPR = new ParamTypes("REGREG_class", EGPR, EGPR);
    static ParamTypes HGPR_HGPR = new ParamTypes("REGREG_class", HGPR, HGPR);
    static ParamTypes HGPR_IMM8 = new ParamTypes("REGIMM_class", HGPR, IMM8);
    static ParamTypes RDL_IMM6 = new ParamTypes("REGIMM_class", RDL, IMM6);
    static ParamTypes GPR_MEM = new ParamTypes("REGIMM_class", GPR, MEM);
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
    static ParamTypes IMM3_SREL = new ParamTypes("IMMIMM_class", IMM3, SREL); // fixme ??
    static ParamTypes SREL_ = new ParamTypes("IMM_class", SREL); // fixme ??
    static ParamTypes MEM_ = new ParamTypes("IMM_class", MEM); // fixme ??
    static ParamTypes LREL_ = new ParamTypes("IMM_class", LREL); // fixme ??

    static ParamTypes MEM_GPR = new ParamTypes("IMMREG_class", MEM, GPR);
    static ParamTypes IMM6_GPR = new ParamTypes("IMMREG_class", IMM6, GPR);

    private static List list;

    static {

        list = new LinkedList();
        int _1cyc = 1; // just for clarity in each declaration
        int _2cyc = 2;
        int _3cyc = 3;
        int _4cyc = 4;

        instr("adc", GPR_GPR, _1cyc, "add register to register with carry");
        instr("add", GPR_GPR, _1cyc, "add register to register");
        instr("adiw", RDL_IMM6, _2cyc, "add immediate to word register");
        instr("and", GPR_GPR, _1cyc, "and register with register");
        instr("andi", HGPR_IMM8, _1cyc, "and register with immediate");
        instr("asr", GPR_, _1cyc, "arithmetic shift right");
        instr("bclr", IMM3_, _1cyc, "clear bit in status register");
        instr("bld", GPR_IMM3, _1cyc, "load bit from T flag into register");
        instr("brbc", IMM3_SREL, _1cyc, "branch if bit in status register is clear");
        instr("brbs", IMM3_SREL, _1cyc, "branch if bit in status register is set");
        instr("brcc", SREL_, _1cyc, "branch if carry flag is clear");
        instr("brcs", SREL_, _1cyc, "branch if carry flag is set");
        instr("break", NONE, _1cyc, "break");
        instr("breq", SREL_, _1cyc, "branch if equal");
        instr("brge", SREL_, _1cyc, "branch if greater or equal (signed)");
        instr("brhc", SREL_, _1cyc, "branch if H flag is clear");
        instr("brhs", SREL_, _1cyc, "branch if H flag is set");
        instr("brid", SREL_, _1cyc, "branch if interrupts are disabled");
        instr("brie", SREL_, _1cyc, "branch if interrupts are enabled");
        instr("brlo", SREL_, _1cyc, "branch if lower");
        instr("brlt", SREL_, _1cyc, "branch if less than zero (signed)");
        instr("brmi", SREL_, _1cyc, "branch if minus");
        instr("brne", SREL_, _1cyc, "branch if not equal");
        instr("brpl", SREL_, _1cyc, "branch if positive");
        instr("brsh", SREL_, _1cyc, "branch if same or higher");
        instr("brtc", SREL_, _1cyc, "branch if T flag is clear");
        instr("brts", SREL_, _1cyc, "branch if T flag is set");
        instr("brvc", SREL_, _1cyc, "branch if V flag is clear");
        instr("brvs", SREL_, _1cyc, "branch if V flag is set");
        instr("bset", IMM3_, _1cyc, "set flag in status register");
        instr("bst", GPR_IMM3, _1cyc, "store bit in register into T flag");
        large("call", MEM_, _4cyc, "call absolute address");
        instr("cbi", IMM5_IMM3, _2cyc, "clear bit in IO register");
        instr("cbr", HGPR_IMM8, _1cyc, "clear bits in register");
        instr("clc", NONE, _1cyc, "clear C flag");
        instr("clh", NONE, _1cyc, "clear H flag");
        instr("cli", NONE, _1cyc, "clear I flag");
        instr("cln", NONE, _1cyc, "clear N flag");
        instr("clr", GPR_, _1cyc, "clear register (set to zero)");
        instr("cls", NONE, _1cyc, "clear S flag");
        instr("clt", NONE, _1cyc, "clear T flag");
        instr("clv", NONE, _1cyc, "clear V flag");
        instr("clz", NONE, _1cyc, "clear Z flag");
        instr("com", GPR_, _1cyc, "one's compliment register");
        instr("cp", GPR_GPR, _1cyc, "compare registers");
        instr("cpc", GPR_GPR, _1cyc, "compare registers with carry");
        instr("cpi", HGPR_IMM8, _1cyc, "compare register with immediate");
        instr("cpse", GPR_GPR, _1cyc, "compare registers and skip if equal");
        instr("dec", GPR_, _1cyc, "decrement register by one");
        instr("eicall", NONE, _4cyc, "extended indirect call");
        instr("eijmp", NONE, _2cyc, "extended indirect jump");
        instr("elpm", NONE, _3cyc, "extended load program memory to r0");
        instr("elpmd", "elpm", GPR_Z, _3cyc, "extended load program memory to register"); // variant
        instr("elpmpi", "elpm", GPR_Z, _3cyc, "extended load program memory to register and post-increment"); // variant
        instr("eor", GPR_GPR, _1cyc, "exclusive or register with register");
        instr("fmul", MGPR_MGPR, _2cyc, "fractional multiply register with register to r0");
        instr("fmuls", MGPR_MGPR, _2cyc, "signed fractional multiply register with register to r0");
        instr("fmulsu", MGPR_MGPR, _2cyc, "signed/unsigned fractional multiply register with register to r0");
        instr("icall", NONE, _3cyc, "indirect call through Z register");
        instr("ijmp", NONE, _2cyc, "indirect jump through Z register");
        instr("in", GPR_IMM6, _1cyc, "read from IO register into register");
        instr("inc", GPR_, _1cyc, "increment register by one");
        large("jmp", MEM_, _3cyc, "absolute jump");
        instr("ld", "ld", GPR_ADR, _2cyc, "load from SRAM");
        instr("ldd", GPR_YZ_IMM6, _2cyc, "load from SRAM with displacement");
        instr("ldi", HGPR_IMM8, _1cyc, "load immediate into register");
        instr("ldpd", "ld", GPR_ADR, _2cyc, "load from SRAM with pre-decrement"); // variant
        instr("ldpi", "ld", GPR_ADR, _2cyc, "load from SRAM with post-increment"); // variant
        large("lds", GPR_MEM, _2cyc, "load direct from SRAM");
        instr("lpm", NONE, _3cyc, "load program memory into r0");
        instr("lpmd", "lpm", GPR_Z, _3cyc, "load program memory into register"); // variant
        instr("lpmpi", "lpm", GPR_Z, _3cyc, "load program memory into register and post-increment"); // variant
        instr("lsl", GPR_, _1cyc, "logical shift left");
        instr("lsr", GPR_, _1cyc, "logical shift right");
        instr("mov", GPR_GPR, _1cyc, "copy register to register");
        instr("movw", EGPR_EGPR, _1cyc, "copy two registers to two registers");
        instr("mul", GPR_GPR, _2cyc, "multiply register with register to r0");
        instr("muls", HGPR_HGPR, _2cyc, "signed multiply register with register to r0");
        instr("mulsu", MGPR_MGPR, _2cyc, "signed/unsigned multiply register with register to r0");
        instr("neg", GPR_, _1cyc, "two's complement register");
        instr("nop", NONE, _1cyc, "do nothing operation");
        instr("or", GPR_GPR, _1cyc, "or register with register");
        instr("ori", HGPR_IMM8, _1cyc, "or register with immediate");
        instr("out", IMM6_GPR, _1cyc, "write from register to IO register");
        instr("pop", GPR_, _2cyc, "pop from the stack to register");
        instr("push", GPR_, _2cyc, "push register to the stack");
        instr("rcall", LREL_, _3cyc, "relative call");
        instr("ret", NONE, _4cyc, "return to caller");
        instr("reti", NONE, _4cyc, "return from interrupt");
        instr("rjmp", LREL_, _2cyc, "relative jump");
        instr("rol", GPR_, _1cyc, "rotate left through carry flag");
        instr("ror", GPR_, _1cyc, "rotate right through carry flag");
        instr("sbc", GPR_GPR, _1cyc, "subtract register from register with carry");
        instr("sbci", HGPR_IMM8, _1cyc, "subtract immediate from register with carry");
        instr("sbi", IMM5_IMM3, _2cyc, "set bit in IO register");
        instr("sbic", IMM5_IMM3, _1cyc, "skip if bit in IO register is clear");
        instr("sbis", IMM5_IMM3, _1cyc, "skip if bit in IO register is set");
        instr("sbiw", RDL_IMM6, _2cyc, "subtract immediate from word ");
        instr("sbr", HGPR_IMM8, _1cyc, "set bits in register");
        instr("sbrc", GPR_IMM3, _1cyc, "skip if bit in register cleared");
        instr("sbrs", GPR_IMM3, _1cyc, "skip if bit in register set");
        instr("sec", NONE, _1cyc, "set C (carry) flag");
        instr("seh", NONE, _1cyc, "set H (half carry) flag");
        instr("sei", NONE, _1cyc, "set I (interrupt enable) flag");
        instr("sen", NONE, _1cyc, "set N (negative) flag");
        instr("ser", GPR_, _1cyc, "set bits in register");
        instr("ses", NONE, _1cyc, "set S (signed) flag");
        instr("set", NONE, _1cyc, "set T flag");
        instr("sev", NONE, _1cyc, "set V (overflow) flag");
        instr("sez", NONE, _1cyc, "set Z (zero) flag");
        instr("sleep", NONE, _1cyc, "enter sleep mode");
        instr("spm", NONE, _1cyc, "store to program memory from r0");
        instr("st", "st", ADR_GPR, _2cyc, "store from register to SRAM");
        instr("std", YZ_IMM6_GPR, _2cyc, "store from register to SRAM with displacement");
        instr("stpd", "st", ADR_GPR, _2cyc, "store from register to SRAM with pre-decrement"); // variant
        instr("stpi", "st", ADR_GPR, _2cyc, "store from register to SRAM with post-increment"); // variant
        large("sts", MEM_GPR, _2cyc, "store direct to SRAM");
        instr("sub", GPR_GPR, _1cyc, "subtract register from register");
        instr("subi", HGPR_IMM8, _1cyc, "subtract immediate from register");
        instr("swap", GPR_, _1cyc, "swap nibbles in register");
        instr("tst", GPR_, _1cyc, "compare registers");
        instr("wdr", NONE, _1cyc, "watchdog timer reset");

    }


    static void instr(String name, ParamTypes types, int cycles, String comment) {
        add(new Description(name, types, cycles, comment));
    }

    static void instr(String variant, String name, ParamTypes types, int cycles, String comment) {
        add(new Description(variant, name, types, cycles, comment));
    }

    static void large(String name, ParamTypes types, int cycles, String comment) {
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
        vpc.Main.parseOptions(args);
        Generator g = new Generator();

        ColorTerminal.printBrightBlue("AVR Instruction Set Class Generator");
        ColorTerminal.print(" - (c) 2004 Ben L. Titzer\n\n");
        ColorTerminal.println("This is an instruction set generator. It generates Java");
        ColorTerminal.println("source code that implement the classes for the AVR instruction");
        ColorTerminal.println("set analysis tools. The files modified are: \n");
        ColorTerminal.println("   Instr.java");
        ColorTerminal.println("   InstructionSet.java");
        ColorTerminal.println("   InstrVisitor.java\n");
        ColorTerminal.printRed("You must rebuild the analysis tools after running the generator.");
        ColorTerminal.nextln();

        g.generate();
    }

}
