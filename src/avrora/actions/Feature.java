package avrora.actions;

import avrora.arch.legacy.LegacyInstr;
import cck.text.Terminal;

public class Feature {
    // LegacyInstr instr;
    Class instruction = null;

    // Class not_instruction=null;
    Integer register1 = null;

    Integer register2 = null;

    Byte val1 = null;

    Byte val2 = null;
    
    int status=0;
    int status1=0;
    int status2=0;
    static int ADDR_HIGH=1;
    static int ADDR_LOW=2;
    static int DATA=3;
    
    
    // Feature(Class instruction,boolean match){
    // if (match)
    // this.instruction=instruction;
    // else
    // this.not_instruction=instruction;
    // }
    Feature(Class instruction, Integer register) {
        this.instruction = instruction;
        this.register1 = register;
    }

    Feature(Class instruction, Integer register, byte val1) {
        this.instruction = instruction;
        this.register1 = register;
        this.val1 = val1;
    }
    
    Feature(Class instruction, Integer register, byte val1, int kindof) {
        this.instruction = instruction;
        this.register1 = register;
        this.val1 = val1;
        this.status=kindof;
    }

    Feature(Class instruction, Integer register1, byte val1, Integer register2,
            byte val2) {
        this.instruction = instruction;
        this.register1 = register1;
        this.val1 = val1;
        this.register2 = register2;
        this.val2 = val2;

    }
    Feature(Class instruction, Integer register1, byte val1, int kindof1, Integer register2,
            byte val2, int kindof2) {
        this.instruction = instruction;
        this.register1 = register1;
        this.val1 = val1;
        this.register2 = register2;
        this.val2 = val2;
        this.status1=kindof1;
        this.status2=kindof2;
         
    }

    /**
     * check if <code>instructionToMatch</code> and feature are matching
     * 
     * @return Object of the match i.e. type of instruction, <code>null</code>
     *         if no match
     */

    boolean matches(LegacyInstr instructionToMatch) {
        // // matching a not rule
        // if(instruction==null && not_instruction!=null)
        // return !not_instruction.isInstance(instructionToMatch);
        //            
        if (!instruction.isInstance(instructionToMatch))
            return false;
        // same instruction let's see if register match
        if (instructionToMatch instanceof LegacyInstr.POP) {
            LegacyInstr.POP instr = (LegacyInstr.POP) instructionToMatch;
            if (instr.r1.getNumber() == this.register1)
                // ok we found instruction
                return true;
            else
                // this is not a needed instruction
                return false;
        }
        Terminal.println("handling of class "
                + instructionToMatch.getClass().getCanonicalName()
                + "not implemented ");
        // well default ...
        return false;

    }
}
