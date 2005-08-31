package avrora.arch.msp430;
import avrora.arch.*;

/**
 * The <code>MSP430Instr</code> class is a container (almost a namespace)
 * for all of the instructions in this architecture. Each inner class
 * represents an instruction in the architecture and also extends the
 * outer class.
 */
public abstract class MSP430Instr implements AbstractInstr {
    
    /**
     * The <code>accept()</code> method accepts an instruction visitor and
     * calls the appropriate <code>visit()</code> method for this
     * instruction.
     * @param v the instruction visitor to accept
     */
    public abstract void accept(MSP430InstrVisitor v);
    
    /**
     * The <code>accept()</code> method accepts an addressing mode visitor
     * and calls the appropriate <code>visit_*()</code> method for this
     * instruction's addressing mode.
     * @param v the addressing mode visitor to accept
     */
    public void accept(MSP430AddrModeVisitor v) {
        // the default implementation of accept() is empty
    }
    
    /**
     * The <code>toString()</code> method converts this instruction to a
     * string representation. For instructions with operands, this method
     * will render the operands in the appropriate syntax as declared in the
     * architecture description.
     * @return a string representation of this instruction
     */
    public String toString() {
        // the default implementation of toString() simply returns the name
        return name;
    }
    
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
    
    /**
     * The <code>getSize()</code> method returns the size of this instruction
     * in bytes.
     */
    public int getSize() {
        return size;
    }
    
    
    /**
     * The <code>getName()</code> method returns the name of this
     * instruction.
     */
    public String getName() {
        return name;
    }
    
    
    /**
     * The <code>getArchitecture()</code> method returns the architecture of
     * this instruction.
     */
    public AbstractArchitecture getArchitecture() {
        return null;
    }
    
    
    /**
     * The default constructor for the <code>MSP430Instr</code> class accepts
     * a string name and a size for each instruction.
     * @param name the string name of the instruction
     * @param size the size of the instruction in bytes
     */
    protected MSP430Instr(String name, int size) {
        this.name = name;
        this.size = size;
    }
    
    public abstract static class JMP_Instr extends MSP430Instr {
        public final MSP430Operand.JUMP target;
        protected JMP_Instr(String name, int size, MSP430AddrMode.JMP am) {
            super(name, size);
            this.target = am.target;
        }
        public void accept(MSP430AddrModeVisitor v) {
            v.visit_JMP(this, target);
        }
        public String toString() {
            return name + ' ' + target;
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
            am.accept(this, v);
        }
        public String toString() {
            return name+am.toString();
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
            am.accept(this, v);
        }
        public String toString() {
            return name+am.toString();
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
            am.accept(this, v);
        }
        public String toString() {
            return name+am.toString();
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
            am.accept(this, v);
        }
        public String toString() {
            return name+am.toString();
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
        CLRC(int size) {
            super("clrc", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
        }
    }
    
    public static class CLRN extends MSP430Instr {
        CLRN(int size) {
            super("clrn", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
        }
    }
    
    public static class CLRZ extends MSP430Instr {
        CLRZ(int size) {
            super("clrz", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
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
        DINT(int size) {
            super("dint", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
        }
    }
    
    public static class EINT extends MSP430Instr {
        EINT(int size) {
            super("eint", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
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
        NOP(int size) {
            super("nop", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
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
        RET(int size) {
            super("ret", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
        }
    }
    
    public static class RETI extends MSP430Instr {
        RETI(int size) {
            super("reti", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
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
        SETC(int size) {
            super("setc", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
        }
    }
    
    public static class SETN extends MSP430Instr {
        SETN(int size) {
            super("setn", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
        }
    }
    
    public static class SETZ extends MSP430Instr {
        SETZ(int size) {
            super("setz", size);
        }
        public void accept(MSP430InstrVisitor v) { v.visit(this); }
        public String toString() {
            return name;
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
