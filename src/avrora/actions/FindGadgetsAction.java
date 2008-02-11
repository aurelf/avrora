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

import avrora.Defaults;
import avrora.Main;
import avrora.arch.*;
import avrora.arch.legacy.*;
import avrora.actions.Gadget;
import avrora.actions.GadgetsSet;
import avrora.core.Program;
import avrora.core.ProgramReader;
import cck.text.StringUtil;
import cck.text.Terminal;
import cck.util.Option;
import cck.util.Util;
import java.io.FileInputStream;
import java.io.IOException;


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
        gadgets=new GadgetsSet();
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
        byte[] buf = new byte[128];

        //AbstractArchitecture arch = ArchitectureRegistry.getArchitecture(ARCH.get());
        //da = arch.getDisassembler();
        if ( !FILE.isBlank() ) {
            // load and lookup a file for gadgets
            System.out.println("searching for gadgets in file "+ FILE.get());
            findGadgets();
            gadgets.print();
            System.out.println("using file "+ FILE.get());
            // Gadgets with pop
            GadgetsSet popgadgets = gadgetsWithPop(gadgets);
            Terminal.println("there are "+ popgadgets.size() + " gadgets with pop instr" );
            popgadgets.print();
            System.out.println("using file "+ FILE.get());
            
            // Well memcpy needs registers r22, r23, r24,r25 to be set wiuth addresses 
            // or r26, r27, r30,r31 to be set wiuth addresses 
            // and r21,r20 to be set with length
//            GadgetsSet popgadgetsr = gadgetsWithPop(gadgets,22);
//            Terminal.println("there are "+ popgadgetsr.size() + " gadgets with pop r22 instr" );
//            popgadgets.print();
           //popgadgets=gadgetsWithPop(gadgets);
//           popgadgets=findGadgetsPopR(22,popgadgets);   
//           popgadgets=findGadgetsPopR(23,popgadgets);  
//           popgadgets=findGadgetsPopR(24,popgadgets); 
//           popgadgets=findGadgetsPopR(25,popgadgets);
//           popgadgets=findGadgetsPopR(30,popgadgets); 
//           popgadgets=findGadgetsPopR(31,popgadgets);
            
            
//            Terminal.println("there are "+ popgadgets.size() + " gadgets with pop r22,r23,r24,r25 instr" );

            //popgadgets.print();

            //Terminal.println("gadgets with ld");            
            //GadgetsSet ldGadgets;
            //ldGadgets=gadgetsWithInstr(gadgets,LegacyInstr.LD.class, null);
            //ldGadgets.print();
            
/*            Terminal.println("gadgets with st");
            GadgetsSet stGadgets;
            stGadgets=gadgetsWithInstr(gadgets,LegacyInstr.ST.class, null);
            stGadgets.print();
  */
            /*
            Terminal.println("gadgets with pop r18");
            GadgetsSet pop18Gadgets;
            pop18Gadgets=gadgetsWithInstr(gadgets,LegacyInstr.POP.class, 18);
            pop18Gadgets.print();

            
            Terminal.println("gadgets with pop r30");
            GadgetsSet pop30Gadgets;
            pop30Gadgets=gadgetsWithInstr(gadgets,LegacyInstr.POP.class, 29);
            pop30Gadgets.print();
*/          
            /*Terminal.println("gadgets with movw");
            GadgetsSet movwGadgets;
            movwGadgets=gadgetsWithInstr(gadgets,LegacyInstr.MOVW.class,null);
            movwGadgets.print();
*/
//            CreateShellCodeInjectByteToMemory();
            

        } else {
            // disassemble the bytes specified on the command line
            disassembleArguments(args, buf);
        }
    }
    

    public Gadget smallestGadget(GadgetsSet g){
        return (Gadget)g.gadgets.first();
    }

    public byte[] CreateShellCodeInjectByteToMemory(){
        byte[] shell=new byte[100];
        
        // strategy 1:
        // - copy address from stack to reg with a pop
        // - copy value from stack to reg with a pop
        // - copy from reg to memory with ld 
        
        // step 1 find shortests ld gadgets 
        GadgetsSet stGadgets=gadgetsWithInstr(gadgets,LegacyInstr.ST.class, null);
        System.out.println("found gadgets ");
        stGadgets.print();
        Gadget smallSt=smallestGadget(stGadgets);
        System.out.println("smallest ST gadget");
        smallSt.print();
        LegacyInstr.ST st=(LegacyInstr.ST)smallSt.contents.get((smallSt.contents.firstKey()));
        int dataReg= st.r2.getNumber();
        int addrReg= st.r1.getNumber();
        System.out.println("data reg = "+dataReg + " Addrreg "+addrReg);
        
        //      find possible Addrreg pop ?
        Terminal.println("smallest gadget with pop r"+addrReg);
        GadgetsSet popAddrGadgets;
        popAddrGadgets=gadgetsWithInstr(gadgets,LegacyInstr.POP.class, addrReg);
        popAddrGadgets.print();
        Gadget smalladdrGadget=smallestGadget(popAddrGadgets);
        smalladdrGadget.print();

        //      find possible pop ?
        Terminal.println("smallest gadget with pop r"+dataReg);
        GadgetsSet popDataGadgets;
        popDataGadgets=gadgetsWithInstr(gadgets,LegacyInstr.POP.class, dataReg);
        popDataGadgets.print();
        Gadget smallpopGadget=smallestGadget(popDataGadgets);
        smallpopGadget.print();
       
        
        
        
        return shell;
      }
    
    
    private GadgetsSet gadgetsWithInstr(GadgetsSet gadgets,Class instr,Integer register){
        GadgetsSet result= new GadgetsSet();

        java.util.Iterator i= gadgets.iterator();
        
        while(i.hasNext()){
            Gadget g=gadgets.nextGadget(i);
            Integer pos=g.hasInstr(instr,register);
            if(pos!=null){
                //System.out.println("gadget match at pos "+pos);
                //g.print();
                Gadget newG= new Gadget(g,pos);
                //newG.print();
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
        popgadgetsr.print();
    }
    
    private GadgetsSet gadgetsWithPop(GadgetsSet gadgets){
        return gadgetsWithPop(gadgets, null);
    }
    
    private GadgetsSet gadgetsWithPop(GadgetsSet gadgets,int reg){
        return gadgetsWithPop(gadgets, new Integer(reg));
    }
    
    private GadgetsSet gadgetsWithPop(GadgetsSet gadgets,Integer register){
        GadgetsSet result= new GadgetsSet();

        java.util.Iterator  i= gadgets.iterator();
        
        while(i.hasNext()){
            Gadget g=gadgets.nextGadget(i);
            if(null!=g.hasPop(register)){
                result.addGadget(g);
            }
        }
        return result; 
    }    
    private void disassembleArguments(String[] args, byte[] buf) {
        Terminal.println("running Lookup with arguments :"+args+"\n");
        if ( args.length < 1 )
            Util.userError("no input data");

        for ( int cntr = 0; cntr < args.length; cntr++ ) {
            buf[cntr] = (byte)StringUtil.evaluateIntegerLiteral(args[cntr]);
        }

        disassembleAndPrint(buf, 0);
    }

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

    private void findGadgets() {
        String fname = FILE.get();
        String args[]= new String[1];
        args[0]=fname;      
        ProgramReader reader=new Defaults.AutoProgramReader();
        Program program;
        try {
            program = reader.read(args);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("failed to read program " + FILE.get());
            e.printStackTrace();
            return;
        }
        int lastret=0;
        // starting direct from flash_data as we don't care it a section is marked as 
        // executable or not ... we just can execute them is we find interesting 
        // gadgets in there ...    
        for ( int cntr = program.program_start; cntr < program.program_end; ) {
            try{
                AbstractInstr instr = program.disassembleInstr(cntr);                
            
                cntr=cntr+2;
                if(instr instanceof LegacyInstr.RET || instr instanceof LegacyInstr.RETI){
                    int from=cntr-60;
                    if(lastret>from)
                        from=lastret;
                     System.out.println("asking for gadget from "+from+ " to " + cntr);
                    Gadget newGadget=createGadget(program, from, cntr);
                    gadgets.addGadget(newGadget);
                    lastret=cntr;
                }
            }
            catch(Exception i){
               i.printStackTrace();
            }
        }
    }


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
