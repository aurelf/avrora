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
        boolean large;
        ParamTypes params;
        int cycles;

        String className;

        Description(String n, ParamTypes p, int c) {
            variant = name = n;
            VARIANT = variant.toUpperCase();
            params = p;
            className = "Instr." + VARIANT;
            cycles = c;
        }

        Description(String v, String n, ParamTypes p, int c) {
            variant = v;
            name = n;
            VARIANT = variant.toUpperCase();
            params = p;
            className = "Instr." + VARIANT;
            cycles = c;
        }

        void writeVisitMethod(SectionFile f) throws java.io.IOException {
            f.writeLine("    public void visit(" + className + " i);");
        }

        void writeSetInsert(SectionFile f) throws java.io.IOException {
            f.writeLine("        instructions.put(" + quote(variant) + ", " + className + ".prototype);");
        }

        void writeClassDecl(SectionFile f) throws java.io.IOException {
            f.writeLine("    public static class " + VARIANT + " extends " + params.baseClass + " {");
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
        int _1cyc = 1;
        int _2cyc = 2;
        int _3cyc = 3;
        int _4cyc = 4;

        instr("adc", GPR_GPR, _1cyc);
        instr("add", GPR_GPR, _1cyc);
        instr("adiw", RDL_IMM6, _2cyc);
        instr("and", GPR_GPR, _1cyc);
        instr("andi", HGPR_IMM8, _1cyc);
        instr("asr", GPR_, _1cyc);
        instr("bclr", IMM3_, _1cyc);
        instr("bld", GPR_IMM3, _1cyc);
        instr("brbc", IMM3_SREL, _1cyc);
        instr("brbs", IMM3_SREL, _1cyc);
        instr("brcc", SREL_, _1cyc);
        instr("brcs", SREL_, _1cyc);
        instr("break", NONE, _1cyc);
        instr("breq", SREL_, _1cyc);
        instr("brge", SREL_, _1cyc);
        instr("brhc", SREL_, _1cyc);
        instr("brhs", SREL_, _1cyc);
        instr("brid", SREL_, _1cyc);
        instr("brie", SREL_, _1cyc);
        instr("brlo", SREL_, _1cyc);
        instr("brlt", SREL_, _1cyc);
        instr("brmi", SREL_, _1cyc);
        instr("brne", SREL_, _1cyc);
        instr("brpl", SREL_, _1cyc);
        instr("brsh", SREL_, _1cyc);
        instr("brtc", SREL_, _1cyc);
        instr("brts", SREL_, _1cyc);
        instr("brvc", SREL_, _1cyc);
        instr("brvs", SREL_, _1cyc);
        instr("bset", IMM3_, _1cyc);
        instr("bst", GPR_IMM3, _1cyc);
        large("call", MEM_, _4cyc);
        instr("cbi", IMM5_IMM3, _2cyc);
        instr("cbr", HGPR_IMM8, _1cyc);
        instr("clc", NONE, _1cyc);
        instr("clh", NONE, _1cyc);
        instr("cli", NONE, _1cyc);
        instr("cln", NONE, _1cyc);
        instr("clr", GPR_, _1cyc);
        instr("cls", NONE, _1cyc);
        instr("clt", NONE, _1cyc);
        instr("clv", NONE, _1cyc);
        instr("clz", NONE, _1cyc);
        instr("com", GPR_, _1cyc);
        instr("cp", GPR_GPR, _1cyc);
        instr("cpc", GPR_GPR, _1cyc);
        instr("cpi", HGPR_IMM8, _1cyc);
        instr("cpse", GPR_GPR, _1cyc);
        instr("dec", GPR_, _1cyc);
        instr("eicall", NONE, _4cyc);
        instr("eijmp", NONE, _2cyc);
        instr("elpm", NONE, _3cyc);
        instr("elpmd", "elpm", GPR_Z, _3cyc); // variant
        instr("elpmpi", "elpm", GPR_Z, _3cyc); // variant
        instr("eor", GPR_GPR, _1cyc);
        instr("fmul", MGPR_MGPR, _2cyc);
        instr("fmuls", MGPR_MGPR, _2cyc);
        instr("fmulsu", MGPR_MGPR, _2cyc);
        instr("icall", NONE, _3cyc);
        instr("ijmp", NONE, _2cyc);
        instr("in", GPR_IMM6, _1cyc);
        instr("inc", GPR_, _1cyc);
        large("jmp", MEM_, _3cyc);
        instr("ld", "ld", GPR_ADR, _2cyc);
        instr("ldd", GPR_YZ_IMM6, _2cyc);
        instr("ldi", HGPR_IMM8, _1cyc);
        instr("ldpd", "ld", GPR_ADR, _2cyc); // variant
        instr("ldpi", "ld", GPR_ADR, _2cyc); // variant
        large("lds", GPR_MEM, _2cyc);
        instr("lpm", NONE, _3cyc);
        instr("lpmd", "lpm", GPR_Z, _3cyc); // variant
        instr("lpmpi", "lpm", GPR_Z, _3cyc); // variant
        instr("lsl", GPR_, _1cyc);
        instr("lsr", GPR_, _1cyc);
        instr("mov", GPR_GPR, _1cyc);
        instr("movw", EGPR_EGPR, _1cyc);
        instr("mul", GPR_GPR, _2cyc);
        instr("muls", HGPR_HGPR, _2cyc);
        instr("mulsu", MGPR_MGPR, _2cyc);
        instr("neg", GPR_, _1cyc);
        instr("nop", NONE, _1cyc);
        instr("or", GPR_GPR, _1cyc);
        instr("ori", HGPR_IMM8, _1cyc);
        instr("out", IMM6_GPR, _1cyc);
        instr("pop", GPR_, _2cyc);
        instr("push", GPR_, _2cyc);
        instr("rcall", LREL_, _3cyc);
        instr("ret", NONE, _4cyc);
        instr("reti", NONE, _4cyc);
        instr("rjmp", LREL_, _2cyc);
        instr("rol", GPR_, _1cyc);
        instr("ror", GPR_, _1cyc);
        instr("sbc", GPR_GPR, _1cyc);
        instr("sbci", HGPR_IMM8, _1cyc);
        instr("sbi", IMM5_IMM3, _2cyc);
        instr("sbic", IMM5_IMM3, _1cyc);
        instr("sbis", IMM5_IMM3, _1cyc);
        instr("sbiw", RDL_IMM6, _2cyc);
        instr("sbr", HGPR_IMM8, _1cyc);
        instr("sbrc", GPR_IMM3, _1cyc);
        instr("sbrs", GPR_IMM3, _1cyc);
        instr("sec", NONE, _1cyc);
        instr("seh", NONE, _1cyc);
        instr("sei", NONE, _1cyc);
        instr("sen", NONE, _1cyc);
        instr("ser", GPR_, _1cyc);
        instr("ses", NONE, _1cyc);
        instr("set", NONE, _1cyc);
        instr("sev", NONE, _1cyc);
        instr("sez", NONE, _1cyc);
        instr("sleep", NONE, _1cyc);
        instr("spm", NONE, _1cyc);
        instr("st", "st", ADR_GPR, _2cyc);
        instr("std", YZ_IMM6_GPR, _2cyc);
        instr("stpd", "st", ADR_GPR, _2cyc); // variant
        instr("stpi", "st", ADR_GPR, _2cyc); // variant
        large("sts", MEM_GPR, _2cyc);
        instr("sub", GPR_GPR, _1cyc);
        instr("subi", HGPR_IMM8, _1cyc);
        instr("swap", GPR_, _1cyc);
        instr("tst", GPR_, _1cyc);
        instr("wdr", NONE, _1cyc);

    }


    static void instr(String name, ParamTypes types, int cycles) {
        add(new Description(name, types, cycles));
    }

    static void instr(String variant, String name, ParamTypes types, int cycles) {
        add(new Description(variant, name, types, cycles));
    }

    static void large(String name, ParamTypes types, int cycles) {
        Description d = new Description(name, types, cycles);
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
