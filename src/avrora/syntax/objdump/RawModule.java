package avrora.syntax.objdump;

import avrora.syntax.*;
import avrora.util.StringUtil;
import avrora.core.Instr;

import java.util.HashMap;

/**
 * The <code>RawModule</code> is a subclass of <code>Module</code> that allows
 * random access when creating a program from an input source. This is needed
 * since the object dump format is special in that each item it prints has
 * its own address, and can have multiple sections.
 *
 * @author Ben L. Titzer
 */
public class RawModule extends Module {

    private class Section {
        final AbstractToken name;
        final int vma_start;
        final int lma_start;

        Section(AbstractToken n, AbstractToken vma, AbstractToken lma) {
            name = n;
            vma_start = StringUtil.evaluateIntegerLiteral(vma.image);
            lma_start = StringUtil.evaluateIntegerLiteral(lma.image);
        }
    }

    protected Section section;
    protected int currentAddress;
    protected HashMap sectionMap;

    public RawModule(boolean cs, boolean ba) {
        super(cs, ba);
        sectionMap = new HashMap();
    }

    public void newSection(AbstractToken name, AbstractToken vma, AbstractToken lma) {
        Section s = new Section(name, vma, lma);
        sectionMap.put(name.image, s);
    }

    public void enterSection(AbstractToken sect) {
        section = (Section)sectionMap.get(sect.image);
    }

    public void addBytes(AbstractToken b1, AbstractToken b2) {
        ExprList list = new ExprList();
        list.add(new Expr.Constant(b1));
        list.add(new Expr.Constant(b2));
        addDataBytes(list);
    }

    public void addBytes(AbstractToken b1, AbstractToken b2, AbstractToken b3, AbstractToken b4) {
        ExprList list = new ExprList();
        list.add(new Expr.Constant(b1));
        list.add(new Expr.Constant(b2));
        list.add(new Expr.Constant(b3));
        list.add(new Expr.Constant(b4));
        addDataBytes(list);
    }

    public void setAddress(AbstractToken addr) {
        int address = StringUtil.evaluateIntegerLiteral(addr.image);
        address = (address - section.vma_start) + section.lma_start;
        segment.setOrigin(address);
    }


    protected void simplify(Item i) {
        try {

            i.simplify();

        } catch (Throwable t) {
            // since this is a raw module, we ignore assembling errors
            // such as mismatched instruction problems--these are due to
            // objdump attempting to disassemble all data within the file,
            // even misaligned instructions and raw machine code that might
            // not be valid according to the instruction set specification
        }
    }

}
