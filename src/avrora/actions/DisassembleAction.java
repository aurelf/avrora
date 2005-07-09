package avrora.actions;

import avrora.Avrora;
import avrora.Main;
import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.core.Disassembler;
import avrora.core.Instr;

import java.io.File;
import java.io.FileInputStream;

/**
 * The <code>DisassembleAction</code> class represents an action that allows the user to disassemble
 * a binary file and display the instructions. This is useful for debugging the disassembler and also
 * for inspecting binaries.
 *
 * @author Ben L. Titzer
 */
public class DisassembleAction extends Action {

    public DisassembleAction() {
        super("The \"disassemble\" action disassembles a binary file into source level instructions.");
    }

    /**
     * The <code>run()</code> method executes the action. The arguments on the command line are passed.
     * The <code>Disassemble</code> action expects the first argument to be the name of the file to
     * disassemble.
     * @param args the command line arguments
     * @throws Exception if there is a problem reading the file or disassembling the instructions in the
     * file
     */
    public void run(String[] args) throws Exception {
        if ( args.length < 1 )
            Avrora.userError("no input files");

        String fname = args[0];
        Main.checkFileExists(fname);
        FileInputStream fis = new FileInputStream(new File(fname));

        byte[] buf = new byte[fis.available()];
        int len = fis.read(buf);

        Disassembler da = new Disassembler();
        for ( int index = 0; index < len; ) {
            Instr i = da.disassemble(0, buf, index);
            Terminal.println(StringUtil.addrToString(index)+": "+hb(buf, index)+" "+hb(buf, index+1)+"        "+i.toString());
            index += i.getSize();
        }
    }

    private String hb(byte[] buf, int index) {
        return StringUtil.toHex(buf[index],2);
    }
}
