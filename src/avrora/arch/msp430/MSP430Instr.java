package avrora.arch.msp430;

/**
 * The <code>MSP430Instr</code> class is a container (almost a namespace)
 * for all of the instructions in this architecture. Each inner class
 * represents an instruction in the architecture and also extends the
 * outer class.
 */
public abstract class MSP430Instr {
    public abstract void accept(MSP430InstrVisitor v);
    public abstract void accept(MSP430AddrModeVisitor v);
    
    /**
     * The <code>name</code> field stores a reference to the name of the
     * instruction as a string.
     */
    public final String name;
    
    /**
     * The <code>size</code> field stores the size of the instruction in
     * bytes.
     */
    public final int size;
    protected MSP430Instr(String name, int size) {
        this.name = name;
        this.size = size;
    }
    public abstract static class REG_Instr extends MSP430Instr {
        public final MSP430Operand.SREG source;
        protected REG_Instr(String name, int size, MSP430AddrMode.REG am) {
            super(name, size);
            this.source = am.source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_REG(source);
        }
    }
    public abstract static class REGREG_Instr extends MSP430Instr {
        public final MSP430Operand.SREG source;
        public final MSP430Operand.SREG dest;
        protected REGREG_Instr(String name, int size, MSP430AddrMode.REGREG am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_REGREG(source, dest);
        }
    }
    public abstract static class REGIND_Instr extends MSP430Instr {
        public final MSP430Operand.SREG source;
        public final MSP430Operand.INDX dest;
        protected REGIND_Instr(String name, int size, MSP430AddrMode.REGIND am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_REGIND(source, dest);
        }
    }
    public abstract static class REGSYM_Instr extends MSP430Instr {
        public final MSP430Operand.SREG source;
        public final MSP430Operand.SYM dest;
        protected REGSYM_Instr(String name, int size, MSP430AddrMode.REGSYM am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_REGSYM(source, dest);
        }
    }
    public abstract static class REGABS_Instr extends MSP430Instr {
        public final MSP430Operand.SREG source;
        public final MSP430Operand.ABSO dest;
        protected REGABS_Instr(String name, int size, MSP430AddrMode.REGABS am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_REGABS(source, dest);
        }
    }
    public abstract static class IND_Instr extends MSP430Instr {
        public final MSP430Operand.INDX source;
        protected IND_Instr(String name, int size, MSP430AddrMode.IND am) {
            super(name, size);
            this.source = am.source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IND(source);
        }
    }
    public abstract static class INDREG_Instr extends MSP430Instr {
        public final MSP430Operand.INDX source;
        public final MSP430Operand.SREG dest;
        protected INDREG_Instr(String name, int size, MSP430AddrMode.INDREG am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_INDREG(source, dest);
        }
    }
    public abstract static class INDIND_Instr extends MSP430Instr {
        public final MSP430Operand.INDX source;
        public final MSP430Operand.INDX dest;
        protected INDIND_Instr(String name, int size, MSP430AddrMode.INDIND am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_INDIND(source, dest);
        }
    }
    public abstract static class SYM_Instr extends MSP430Instr {
        public final MSP430Operand.SYM source;
        protected SYM_Instr(String name, int size, MSP430AddrMode.SYM am) {
            super(name, size);
            this.source = am.source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_SYM(source);
        }
    }
    public abstract static class SYMREG_Instr extends MSP430Instr {
        public final MSP430Operand.SYM source;
        public final MSP430Operand.SREG dest;
        protected SYMREG_Instr(String name, int size, MSP430AddrMode.SYMREG am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_SYMREG(source, dest);
        }
    }
    public abstract static class INDSYM_Instr extends MSP430Instr {
        public final MSP430Operand.INDX source;
        public final MSP430Operand.SYM dest;
        protected INDSYM_Instr(String name, int size, MSP430AddrMode.INDSYM am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_INDSYM(source, dest);
        }
    }
    public abstract static class INDABS_Instr extends MSP430Instr {
        public final MSP430Operand.INDX source;
        public final MSP430Operand.ABSO dest;
        protected INDABS_Instr(String name, int size, MSP430AddrMode.INDABS am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_INDABS(source, dest);
        }
    }
    public abstract static class SYMABS_Instr extends MSP430Instr {
        public final MSP430Operand.SYM source;
        public final MSP430Operand.ABSO dest;
        protected SYMABS_Instr(String name, int size, MSP430AddrMode.SYMABS am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_SYMABS(source, dest);
        }
    }
    public abstract static class SYMIND_Instr extends MSP430Instr {
        public final MSP430Operand.SYM source;
        public final MSP430Operand.INDX dest;
        protected SYMIND_Instr(String name, int size, MSP430AddrMode.SYMIND am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_SYMIND(source, dest);
        }
    }
    public abstract static class SYMSYM_Instr extends MSP430Instr {
        public final MSP430Operand.SYM source;
        public final MSP430Operand.SYM dest;
        protected SYMSYM_Instr(String name, int size, MSP430AddrMode.SYMSYM am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_SYMSYM(source, dest);
        }
    }
    public abstract static class ABSSYM_Instr extends MSP430Instr {
        public final MSP430Operand.ABSO source;
        public final MSP430Operand.SYM dest;
        protected ABSSYM_Instr(String name, int size, MSP430AddrMode.ABSSYM am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_ABSSYM(source, dest);
        }
    }
    public abstract static class ABS_Instr extends MSP430Instr {
        public final MSP430Operand.ABSO source;
        protected ABS_Instr(String name, int size, MSP430AddrMode.ABS am) {
            super(name, size);
            this.source = am.source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_ABS(source);
        }
    }
    public abstract static class ABSREG_Instr extends MSP430Instr {
        public final MSP430Operand.ABSO source;
        public final MSP430Operand.SREG dest;
        protected ABSREG_Instr(String name, int size, MSP430AddrMode.ABSREG am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_ABSREG(source, dest);
        }
    }
    public abstract static class ABSIND_Instr extends MSP430Instr {
        public final MSP430Operand.ABSO source;
        public final MSP430Operand.INDX dest;
        protected ABSIND_Instr(String name, int size, MSP430AddrMode.ABSIND am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_ABSIND(source, dest);
        }
    }
    public abstract static class ABSABS_Instr extends MSP430Instr {
        public final MSP430Operand.ABSO source;
        public final MSP430Operand.ABSO dest;
        protected ABSABS_Instr(String name, int size, MSP430AddrMode.ABSABS am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_ABSABS(source, dest);
        }
    }
    public abstract static class IREGSYM_Instr extends MSP430Instr {
        public final MSP430Operand.IREG source;
        public final MSP430Operand.SYM dest;
        protected IREGSYM_Instr(String name, int size, MSP430AddrMode.IREGSYM am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IREGSYM(source, dest);
        }
    }
    public abstract static class IREG_Instr extends MSP430Instr {
        public final MSP430Operand.IREG source;
        protected IREG_Instr(String name, int size, MSP430AddrMode.IREG am) {
            super(name, size);
            this.source = am.source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IREG(source);
        }
    }
    public abstract static class IREGREG_Instr extends MSP430Instr {
        public final MSP430Operand.IREG source;
        public final MSP430Operand.SREG dest;
        protected IREGREG_Instr(String name, int size, MSP430AddrMode.IREGREG am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IREGREG(source, dest);
        }
    }
    public abstract static class IREGIND_Instr extends MSP430Instr {
        public final MSP430Operand.IREG source;
        public final MSP430Operand.INDX dest;
        protected IREGIND_Instr(String name, int size, MSP430AddrMode.IREGIND am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IREGIND(source, dest);
        }
    }
    public abstract static class IREGABS_Instr extends MSP430Instr {
        public final MSP430Operand.IREG source;
        public final MSP430Operand.ABSO dest;
        protected IREGABS_Instr(String name, int size, MSP430AddrMode.IREGABS am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IREGABS(source, dest);
        }
    }
    public abstract static class IMM_Instr extends MSP430Instr {
        public final MSP430Operand.IMM source;
        protected IMM_Instr(String name, int size, MSP430AddrMode.IMM am) {
            super(name, size);
            this.source = am.source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IMM(source);
        }
    }
    public abstract static class IMMREG_Instr extends MSP430Instr {
        public final MSP430Operand.IMM source;
        public final MSP430Operand.SREG dest;
        protected IMMREG_Instr(String name, int size, MSP430AddrMode.IMMREG am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IMMREG(source, dest);
        }
    }
    public abstract static class IMMIND_Instr extends MSP430Instr {
        public final MSP430Operand.IMM source;
        public final MSP430Operand.INDX dest;
        protected IMMIND_Instr(String name, int size, MSP430AddrMode.IMMIND am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IMMIND(source, dest);
        }
    }
    public abstract static class IMMSYM_Instr extends MSP430Instr {
        public final MSP430Operand.IMM source;
        public final MSP430Operand.SYM dest;
        protected IMMSYM_Instr(String name, int size, MSP430AddrMode.IMMSYM am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IMMSYM(source, dest);
        }
    }
    public abstract static class IMMABS_Instr extends MSP430Instr {
        public final MSP430Operand.IMM source;
        public final MSP430Operand.ABSO dest;
        protected IMMABS_Instr(String name, int size, MSP430AddrMode.IMMABS am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_IMMABS(source, dest);
        }
    }
    public abstract static class AUTO_B_Instr extends MSP430Instr {
        public final MSP430Operand.AIREG_B source;
        protected AUTO_B_Instr(String name, int size, MSP430AddrMode.AUTO_B am) {
            super(name, size);
            this.source = am.source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTO_B(source);
        }
    }
    public abstract static class AUTOREG_B_Instr extends MSP430Instr {
        public final MSP430Operand.AIREG_B source;
        public final MSP430Operand.SREG dest;
        protected AUTOREG_B_Instr(String name, int size, MSP430AddrMode.AUTOREG_B am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOREG_B(source, dest);
        }
    }
    public abstract static class AUTOIND_B_Instr extends MSP430Instr {
        public final MSP430Operand.AIREG_B source;
        public final MSP430Operand.INDX dest;
        protected AUTOIND_B_Instr(String name, int size, MSP430AddrMode.AUTOIND_B am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOIND_B(source, dest);
        }
    }
    public abstract static class AUTOSYM_B_Instr extends MSP430Instr {
        public final MSP430Operand.AIREG_B source;
        public final MSP430Operand.SYM dest;
        protected AUTOSYM_B_Instr(String name, int size, MSP430AddrMode.AUTOSYM_B am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOSYM_B(source, dest);
        }
    }
    public abstract static class AUTOABS_B_Instr extends MSP430Instr {
        public final MSP430Operand.AIREG_B source;
        public final MSP430Operand.ABSO dest;
        protected AUTOABS_B_Instr(String name, int size, MSP430AddrMode.AUTOABS_B am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOABS_B(source, dest);
        }
    }
    public abstract static class AUTO_W_Instr extends MSP430Instr {
        public final MSP430Operand.AIREG_W source;
        protected AUTO_W_Instr(String name, int size, MSP430AddrMode.AUTO_W am) {
            super(name, size);
            this.source = am.source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTO_W(source);
        }
    }
    public abstract static class AUTOREG_W_Instr extends MSP430Instr {
        public final MSP430Operand.AIREG_W source;
        public final MSP430Operand.SREG dest;
        protected AUTOREG_W_Instr(String name, int size, MSP430AddrMode.AUTOREG_W am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOREG_W(source, dest);
        }
    }
    public abstract static class AUTOIND_W_Instr extends MSP430Instr {
        public final MSP430Operand.AIREG_W source;
        public final MSP430Operand.INDX dest;
        protected AUTOIND_W_Instr(String name, int size, MSP430AddrMode.AUTOIND_W am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOIND_W(source, dest);
        }
    }
    public abstract static class AUTOSYM_W_Instr extends MSP430Instr {
        public final MSP430Operand.AIREG_W source;
        public final MSP430Operand.SYM dest;
        protected AUTOSYM_W_Instr(String name, int size, MSP430AddrMode.AUTOSYM_W am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOSYM_W(source, dest);
        }
    }
    public abstract static class AUTOABS_W_Instr extends MSP430Instr {
        public final MSP430Operand.AIREG_W source;
        public final MSP430Operand.ABSO dest;
        protected AUTOABS_W_Instr(String name, int size, MSP430AddrMode.AUTOABS_W am) {
            super(name, size);
            this.source = am.source;
            this.dest = am.dest;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_AUTOABS_W(source, dest);
        }
    }
    public abstract static class JMP_Instr extends MSP430Instr {
        public final MSP430Operand.JUMP source;
        protected JMP_Instr(String name, int size, MSP430AddrMode.JMP am) {
            super(name, size);
            this.source = am.source;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(source);
        }
    }
    public abstract static class DOUBLE_W_Instr extends MSP430Instr {
        public final MSP430AddrMode.DOUBLE_W am;
        public final MSP430Operand source;
        public final MSP430Operand dest;
        protected DOUBLE_W_Instr(String name, int size, MSP430AddrMode.DOUBLE_W am) {
            super(name, size);
            this.am = am;
            this.source = am.get_source();
            this.dest = am.get_dest();
        }
        public void accept(MSP430AddrModeVisitor v) {
            am.accept(v);
        }
    }
    public abstract static class SINGLE_W_Instr extends MSP430Instr {
        public final MSP430AddrMode.SINGLE_W am;
        public final MSP430Operand source;
        protected SINGLE_W_Instr(String name, int size, MSP430AddrMode.SINGLE_W am) {
            super(name, size);
            this.am = am;
            this.source = am.get_source();
        }
        public void accept(MSP430AddrModeVisitor v) {
            am.accept(v);
        }
    }
    public abstract static class DOUBLE_B_Instr extends MSP430Instr {
        public final MSP430AddrMode.DOUBLE_B am;
        public final MSP430Operand source;
        public final MSP430Operand dest;
        protected DOUBLE_B_Instr(String name, int size, MSP430AddrMode.DOUBLE_B am) {
            super(name, size);
            this.am = am;
            this.source = am.get_source();
            this.dest = am.get_dest();
        }
        public void accept(MSP430AddrModeVisitor v) {
            am.accept(v);
        }
    }
    public abstract static class SINGLE_B_Instr extends MSP430Instr {
        public final MSP430AddrMode.SINGLE_B am;
        public final MSP430Operand source;
        protected SINGLE_B_Instr(String name, int size, MSP430AddrMode.SINGLE_B am) {
            super(name, size);
            this.am = am;
            this.source = am.get_source();
        }
        public void accept(MSP430AddrModeVisitor v) {
            am.accept(v);
        }
    }
    public static class ADC extends SINGLE_W_Instr {
        ADC(int size, MSP430AddrMode.SINGLE_W am) {
            super("adc", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class ADC_B extends SINGLE_B_Instr {
        ADC_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("adc.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class ADD extends DOUBLE_W_Instr {
        ADD(int size, MSP430AddrMode.DOUBLE_W am) {
            super("add", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class ADD_B extends DOUBLE_B_Instr {
        ADD_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("add.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class ADDC extends DOUBLE_W_Instr {
        ADDC(int size, MSP430AddrMode.DOUBLE_W am) {
            super("addc", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class ADDC_B extends DOUBLE_B_Instr {
        ADDC_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("addc.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class AND extends DOUBLE_W_Instr {
        AND(int size, MSP430AddrMode.DOUBLE_W am) {
            super("and", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class AND_B extends DOUBLE_B_Instr {
        AND_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("and.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIC extends DOUBLE_W_Instr {
        BIC(int size, MSP430AddrMode.DOUBLE_W am) {
            super("bic", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIC_B extends DOUBLE_B_Instr {
        BIC_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("bic.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIS extends DOUBLE_W_Instr {
        BIS(int size, MSP430AddrMode.DOUBLE_W am) {
            super("bis", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIS_B extends DOUBLE_B_Instr {
        BIS_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("bis.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIT extends DOUBLE_W_Instr {
        BIT(int size, MSP430AddrMode.DOUBLE_W am) {
            super("bit", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BIT_B extends DOUBLE_B_Instr {
        BIT_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("bit.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class BR extends SINGLE_W_Instr {
        BR(int size, MSP430AddrMode.SINGLE_W am) {
            super("br", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CALL extends SINGLE_W_Instr {
        CALL(int size, MSP430AddrMode.SINGLE_W am) {
            super("call", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CLR extends SINGLE_W_Instr {
        CLR(int size, MSP430AddrMode.SINGLE_W am) {
            super("clr", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CLR_B extends SINGLE_B_Instr {
        CLR_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("clr.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CLRC extends MSP430Instr {
        CLRC(int size, MSP430AddrMode.$clrc$ am) {
            super("clrc", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$clrc$();
        }
    }
    
    public static class CLRN extends MSP430Instr {
        CLRN(int size, MSP430AddrMode.$clrn$ am) {
            super("clrn", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$clrn$();
        }
    }
    
    public static class CLRZ extends MSP430Instr {
        CLRZ(int size, MSP430AddrMode.$clrz$ am) {
            super("clrz", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$clrz$();
        }
    }
    
    public static class CMP extends DOUBLE_W_Instr {
        CMP(int size, MSP430AddrMode.DOUBLE_W am) {
            super("cmp", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class CMP_B extends DOUBLE_B_Instr {
        CMP_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("cmp.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DADC extends SINGLE_W_Instr {
        DADC(int size, MSP430AddrMode.SINGLE_W am) {
            super("dadc", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DADC_B extends SINGLE_B_Instr {
        DADC_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("dadc.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DADD extends DOUBLE_W_Instr {
        DADD(int size, MSP430AddrMode.DOUBLE_W am) {
            super("dadd", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DADD_B extends DOUBLE_B_Instr {
        DADD_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("dadd.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DEC extends SINGLE_W_Instr {
        DEC(int size, MSP430AddrMode.SINGLE_W am) {
            super("dec", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DEC_B extends SINGLE_B_Instr {
        DEC_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("dec.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DECD extends SINGLE_W_Instr {
        DECD(int size, MSP430AddrMode.SINGLE_W am) {
            super("decd", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DECD_B extends SINGLE_B_Instr {
        DECD_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("decd.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class DINT extends MSP430Instr {
        DINT(int size, MSP430AddrMode.$dint$ am) {
            super("dint", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$dint$();
        }
    }
    
    public static class EINT extends MSP430Instr {
        EINT(int size, MSP430AddrMode.$eint$ am) {
            super("eint", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$eint$();
        }
    }
    
    public static class INC extends SINGLE_W_Instr {
        INC(int size, MSP430AddrMode.SINGLE_W am) {
            super("inc", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INC_B extends SINGLE_B_Instr {
        INC_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("inc.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INCD extends SINGLE_W_Instr {
        INCD(int size, MSP430AddrMode.SINGLE_W am) {
            super("incd", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INCD_B extends SINGLE_B_Instr {
        INCD_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("incd.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INV extends SINGLE_W_Instr {
        INV(int size, MSP430AddrMode.SINGLE_W am) {
            super("inv", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class INV_B extends SINGLE_B_Instr {
        INV_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("inv.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JC extends JMP_Instr {
        JC(int size, MSP430AddrMode.JMP am) {
            super("jc", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JHS extends JMP_Instr {
        JHS(int size, MSP430AddrMode.JMP am) {
            super("jhs", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JEQ extends JMP_Instr {
        JEQ(int size, MSP430AddrMode.JMP am) {
            super("jeq", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JZ extends JMP_Instr {
        JZ(int size, MSP430AddrMode.JMP am) {
            super("jz", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JGE extends JMP_Instr {
        JGE(int size, MSP430AddrMode.JMP am) {
            super("jge", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JL extends JMP_Instr {
        JL(int size, MSP430AddrMode.JMP am) {
            super("jl", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JMP extends JMP_Instr {
        JMP(int size, MSP430AddrMode.JMP am) {
            super("jmp", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JN extends JMP_Instr {
        JN(int size, MSP430AddrMode.JMP am) {
            super("jn", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JNC extends JMP_Instr {
        JNC(int size, MSP430AddrMode.JMP am) {
            super("jnc", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JLO extends JMP_Instr {
        JLO(int size, MSP430AddrMode.JMP am) {
            super("jlo", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JNE extends JMP_Instr {
        JNE(int size, MSP430AddrMode.JMP am) {
            super("jne", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class JNZ extends JMP_Instr {
        JNZ(int size, MSP430AddrMode.JMP am) {
            super("jnz", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class MOV extends DOUBLE_W_Instr {
        MOV(int size, MSP430AddrMode.DOUBLE_W am) {
            super("mov", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class MOV_B extends DOUBLE_B_Instr {
        MOV_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("mov.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class NOP extends MSP430Instr {
        NOP(int size, MSP430AddrMode.$nop$ am) {
            super("nop", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$nop$();
        }
    }
    
    public static class POP extends SINGLE_W_Instr {
        POP(int size, MSP430AddrMode.SINGLE_W am) {
            super("pop", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class POP_B extends SINGLE_B_Instr {
        POP_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("pop.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class PUSH extends SINGLE_W_Instr {
        PUSH(int size, MSP430AddrMode.SINGLE_W am) {
            super("push", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class PUSH_B extends SINGLE_B_Instr {
        PUSH_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("push.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RET extends MSP430Instr {
        RET(int size, MSP430AddrMode.$ret$ am) {
            super("ret", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$ret$();
        }
    }
    
    public static class RETI extends MSP430Instr {
        RETI(int size, MSP430AddrMode.$reti$ am) {
            super("reti", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$reti$();
        }
    }
    
    public static class RLA extends SINGLE_W_Instr {
        RLA(int size, MSP430AddrMode.SINGLE_W am) {
            super("rla", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RLA_B extends SINGLE_B_Instr {
        RLA_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("rla.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RLC extends SINGLE_W_Instr {
        RLC(int size, MSP430AddrMode.SINGLE_W am) {
            super("rlc", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RLC_B extends SINGLE_B_Instr {
        RLC_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("rlc.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RRA extends SINGLE_W_Instr {
        RRA(int size, MSP430AddrMode.SINGLE_W am) {
            super("rra", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RRA_B extends SINGLE_B_Instr {
        RRA_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("rra.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RRC extends SINGLE_W_Instr {
        RRC(int size, MSP430AddrMode.SINGLE_W am) {
            super("rrc", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class RRC_B extends SINGLE_B_Instr {
        RRC_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("rrc.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SBC extends SINGLE_W_Instr {
        SBC(int size, MSP430AddrMode.SINGLE_W am) {
            super("sbc", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SBC_B extends SINGLE_B_Instr {
        SBC_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("sbc.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SETC extends MSP430Instr {
        SETC(int size, MSP430AddrMode.$setc$ am) {
            super("setc", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$setc$();
        }
    }
    
    public static class SETN extends MSP430Instr {
        SETN(int size, MSP430AddrMode.$setn$ am) {
            super("setn", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$setn$();
        }
    }
    
    public static class SETZ extends MSP430Instr {
        SETZ(int size, MSP430AddrMode.$setz$ am) {
            super("setz", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_$setz$();
        }
    }
    
    public static class SUB extends DOUBLE_W_Instr {
        SUB(int size, MSP430AddrMode.DOUBLE_W am) {
            super("sub", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SUB_B extends DOUBLE_B_Instr {
        SUB_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("sub.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SUBC extends DOUBLE_W_Instr {
        SUBC(int size, MSP430AddrMode.DOUBLE_W am) {
            super("subc", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SUBC_B extends DOUBLE_B_Instr {
        SUBC_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("subc.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SBB extends DOUBLE_W_Instr {
        SBB(int size, MSP430AddrMode.DOUBLE_W am) {
            super("sbb", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SBB_B extends DOUBLE_B_Instr {
        SBB_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("sbb.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SWPB extends SINGLE_W_Instr {
        SWPB(int size, MSP430AddrMode.SINGLE_W am) {
            super("swpb", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class SXT extends SINGLE_W_Instr {
        SXT(int size, MSP430AddrMode.SINGLE_W am) {
            super("sxt", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class TST extends SINGLE_W_Instr {
        TST(int size, MSP430AddrMode.SINGLE_W am) {
            super("tst", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class TST_B extends SINGLE_B_Instr {
        TST_B(int size, MSP430AddrMode.SINGLE_B am) {
            super("tst.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class XOR extends DOUBLE_W_Instr {
        XOR(int size, MSP430AddrMode.DOUBLE_W am) {
            super("xor", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
    public static class XOR_B extends DOUBLE_B_Instr {
        XOR_B(int size, MSP430AddrMode.DOUBLE_B am) {
            super("xor.b", size, am);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
    }
    
}
