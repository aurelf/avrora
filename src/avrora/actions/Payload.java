package avrora.actions;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import avrora.arch.legacy.LegacyInstr;
import cck.text.StringUtil;
import cck.text.Terminal;
import cck.util.Arithmetic;

class Payload {
    static final int MAX_PAYLOAD_LENGTH = 100;

    // the actual paload
    
    protected Stack<Byte> payload;
    // a description of the stack in a buyte array 
    Byte [] buf; 
    // describes the gadgets chaining that will occur chen "executing" the
    // payload
    protected GadgetsSet flow;

    public Payload() {
        payload = new Stack<Byte>();
        flow = new GadgetsSet();
    }

    void addGadget( Gadget candidategadget,
            ArrayList<Feature> origFeatures)
            throws LegacyInstr.InvalidImmediate {
        ArrayList<Feature> features = origFeatures;
        Iterator i = candidategadget.reverseIterator();
        // We are going to look at all the instrucitons of the gadget
        // in reverse order as the last instr is the one who matters
        // (i.e. in code with pop r1, pop r1 we want the last pop to load r1
        // )
        // also the paload is goning to be executed in reverse order
        // by now all of them should be keept
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            LegacyInstr gadgetInstr = (LegacyInstr) entry.getValue();
            applyFeature(features, gadgetInstr);
        }
        // finally the address to call the gadget
        Terminal.println("adding address to candidate "
                + StringUtil.to0xHex(candidategadget.entryPointAddr(), 4));
        candidategadget.print();
        addAddress(candidategadget.entryPointAddr());
    }

    public void applyFeature( ArrayList<Feature> features,  LegacyInstr instr) {
        // if this instruction matches one in features
        Iterator<Feature> iterFeatures = features.iterator();
        while (iterFeatures.hasNext()) {
            Feature f = iterFeatures.next();
            if (f.matches(instr)) {
                // so bad that it's almost not working ...
                // Terminal.println("adding parameter in stack
                // "+StringUtil.to0xHex(f.val1, 2));
                payload.add(f.val1);
                if(this instanceof PayloadLD &&  f.status==Feature.ADDR_HIGH)
                    ((PayloadLD)this).addrPosInPayload=payload.size();
                if(this instanceof PayloadLD && f.status==Feature.DATA)
                    ((PayloadLD)this).dataPosInPayload=payload.size();
                // we are done with this one ...
                iterFeatures.remove();
                // TODO make this clean ...
                return;
            }
        }
        // well we didn't match any feature, then question is do we need to add
        // crap to the stack ?

        if (instr instanceof LegacyInstr.POP) {
            // we don't care of this value
            // Terminal.println("adding random parameter in stack 00");
            payload.add((byte) 0);
        } else if (instr instanceof LegacyInstr.OUT) {
            // nothing to do, instruction don't touch stack
        } else if (instr instanceof LegacyInstr.CALL
                || instr instanceof LegacyInstr.ICALL
                || instr instanceof LegacyInstr.BRBC
                || instr instanceof LegacyInstr.BRBC) {
            Terminal.printCyan("instr " + instr.properties.name
                    + " is your gadget valid ?\n");
        }
        // else
        // Terminal.println("instruction is neither in features nor a pop...
        // adopt ignore strategy...");
    }



    public void addAddressToPayload(Byte[] addr)
            throws LegacyInstr.InvalidImmediate {
        if (addr.length > 2)
            throw new LegacyInstr.InvalidImmediate(1, addr[0] + 255 * addr[1],
                    0, 65536);
        // TODO check endianess ....
        payload.push(addr[0]);
        payload.push(addr[1]);

    }

    private Byte[] toBytes(Integer integer) throws LegacyInstr.InvalidImmediate {
        Byte[] byteaddr = new Byte[2];
        // Terminal.println("to Byes "+integer);
        // Terminal.print("converting Integer "+StringUtil.to0xHex(integer, 4));
        if (integer > 65536)
            throw new LegacyInstr.InvalidImmediate(1, integer, 0, 65536);

        byteaddr[0] = Arithmetic.low(integer);
        byteaddr[1] = Arithmetic.high(integer);
        // Terminal.println(" to bytes"+StringUtil.to0xHex(byteaddr[0],2)+
        // " and "+StringUtil.to0xHex(byteaddr[1],2));

        return byteaddr;
    }


    public void addAddress( Integer addr)
            throws LegacyInstr.InvalidImmediate {

        // divide by 2 
        // value of program counter stored on stack are always
        // /2 of the address to jump to 
        addAddressToPayload(toBytes(addr/2));

    }
    
    /**
     * last step is to reboot the node those two bytes will be eaten by last ret
     * 
     * @param payload
     * @throws Exception
     */
    public void addFakeReboot() {
        try {
            addAddress(new Integer(0));
        } catch (Exception e) {
            Terminal.print("that should never hapopen");
            e.printStackTrace();
        }
    }

    private static <T> Stack<T> reverseStack(Stack<T> in) {
        Stack<T> out = new Stack<T>();
        while (!in.empty()) {
            out.push(in.pop());
        }
        return out;
    }


    public Byte[] toByteArray() {
        Byte array[] = new Byte[payload.size()];
        array = reverseStack(payload).toArray(array);
        return array;
    }

    public Byte pop() {
        return payload.pop();
    }

    public Byte push(Byte item) {
        return payload.push(item);
    }

    
    static class PayloadMemcpy extends Payload{
        private Integer addr;
        private Integer data;

        private Integer addrReg;
        private Integer dataReg;
        
        public Integer getAddr() {
            return addr;
        }
        public void setAddr(Integer addr) {
            this.addr = addr;
        }
        public Integer getAddrReg() {
            return addrReg;
        }
        public void setAddrReg(Integer addrReg) {
            this.addrReg = addrReg;
        }
        public Integer getData() {
            return data;
        }
        public void setData(Integer data) {
            this.data = data;
        }
        public Integer getDataReg() {
            return dataReg;
        }
        public void setDataReg(Integer dataReg) {
            this.dataReg = dataReg;
        }
        
    }
    static class PayloadLD extends Payload{
        private Integer addr;
        private Integer data;

        private Integer addrPosInPayload;
        private Integer dataPosInPayload;

        private Integer addrReg;
        private Integer dataReg;
          
        public String getParamsString() {
            String Params=new String();            
            // mind the reversed stack order ....
            if (buf==null)  
                buf=this.toByteArray();
            
            //int length=buf.length;
            Terminal.println("Payload Size"+buf.length);
            Params+="#define  payload_data_pos " + (buf.length-dataPosInPayload)+ "\n";
            Params+="#define payload_addr_pos " + (buf.length-addrPosInPayload)+ "\n";
            Params+="#define payload_length " + buf.length+ "\n";
            return Params;
        }

        public Integer getAddr() {
            return addr;
        }
        public void setAddr(Integer addr) {
            this.addr = addr;
        }
        public Integer getAddrReg() {
            return addrReg;
        }
        public void setAddrReg(Integer addrReg) {
            this.addrReg = addrReg;
        }
        public Integer getData() {
            return data;
        }
        public void setData(Integer data) {
            this.data = data;
        }
        public Integer getDataReg() {
            return dataReg;
        }
        public void setDataReg(Integer dataReg) {
            this.dataReg = dataReg;
        }
    }

    public int size() {
        return payload.size();
    }
    
    

    /**
     * Print a C shapped array containing payload
     * 
     * @param buf
     */

    public void PrintPayload() {
        if (buf==null)
            buf=toByteArray();
        if (toByteArray()== null) {
            Terminal.println("//none");
            return;
        }

        
        Terminal.println("uint8_t payload_length=" + buf.length + ";");
        Terminal.print("uint8_t payload[]={");
        for (int i = 0; i < buf.length; i++) {
            Terminal.print(StringUtil.to0xHex(buf[i], 2));
            if (i < buf.length - 1)
                Terminal.print(",");
        }
        Terminal.println("};");
    }
    
    public void PrintPayload(String fname) {
        if (buf==null)
            buf=this.toByteArray();
        PrintWriter fis;
        try {
            fis = new PrintWriter(fname);
        } catch (FileNotFoundException e) {
            Terminal.println("file "+fname+" not found " );
            e.printStackTrace();
            return;
        }
        
        if (buf == null) {
            fis.print("//none ");
            return;
        }
        Terminal.print("To file " + fname +"\n");
        fis.print(getParamsString());
        fis.print("uint8_t payload[]={");
        for (int i = 0; i < buf.length; i++) {
            //Terminal.print(StringUtil.to0xHex(buf[i], 2)+" ");
            fis.print(StringUtil.to0xHex(buf[i], 2));
             if (i < buf.length - 1)
                fis.print(",");
        }
        //Terminal.print("\n");       
        fis.println("};");
        fis.close();
    }

    private String getParamsString() {
            String Params=new String();          
            if (buf==null)  
                buf=this.toByteArray();
              
            if (this.getClass() ==PayloadLD.class){
                return ((PayloadLD)this).getParamsString();
            }
            Params+="uint8_t payload_length=" + buf.length+ ";\n";
            return Params;
    }

}
