package avrora.actions;

import avrora.Avrora;
import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.core.Disassembler;
import avrora.core.Instr;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author Ben L. Titzer
 */
public class DisassembleAction extends Action {

    public DisassembleAction() {
        super("The \"disassemble\" action disassembles a binary file into source level instructions.");
    }

    public void run(String[] args) throws Exception {
        if ( args.length < 1 )
            Avrora.userError("no input files");

        File f = new File(args[0]);
        FileInputStream fis = new FileInputStream(f);

        byte[] buf = new byte[fis.available()];
        int len = fis.read(buf);

        Disassembler da = new Disassembler();
        for ( int index = 0; index < buf.length-1; ) {
            Instr i = da.disassemble(buf, index);
            Terminal.println(StringUtil.addrToString(index)+" "+hb(buf, index)+hb(buf, index+1)+": "+i.toString());
            index += i.getSize();
        }
    }

    private String hb(byte[] buf, int index) {
        return StringUtil.toHex(buf[index],2);
    }
}
