package avrora.stack.isea;

import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.Avrora;

/**
 * @author Ben L. Titzer
 */
public class ISEAbstractState {

    public void merge(ISEAbstractState s) {
        if ( depth != s.depth ) throw Avrora.failure("stack height mismatch");
        // merge elements (registers)
        for ( int cntr = 0; cntr < elements.length; cntr++ ) {
            elements[cntr].read = elements[cntr].read || s.elements[cntr].read;
            elements[cntr].value = ISEValue.merge(elements[cntr].value, s.elements[cntr].value);
        }
        // merge stack contents
        for ( int cntr = 0; cntr < depth; cntr++ ) {
            stack[cntr] = ISEValue.merge(stack[cntr], s.stack[cntr]);
        }
    }

    public boolean equals(Object o) {
        if ( !(o instanceof ISEAbstractState) ) return false;
        ISEAbstractState s = (ISEAbstractState)o;
        if ( depth != s.depth ) return false;
        // check that the elements are the same
        for ( int cntr = 0; cntr < elements.length; cntr++ ) {
            if( elements[cntr].read != s.elements[cntr].read ) return false;
            if( elements[cntr].value != s.elements[cntr].value ) return false;
        }
        // check that the stack contents are the same
        for ( int cntr = 0; cntr < depth; cntr++ ) {
            if( stack[cntr] != s.stack[cntr] ) return false;
        }
        return true;
    }

    public ISEAbstractState copy() {
        return new ISEAbstractState(elements, stack, depth);
    }

    public void push(byte val) {
        stack[depth++] = val;
    }

    public byte pop() {
        return stack[--depth];
    }

    public void print(int pc) {
        Terminal.print(StringUtil.addrToString(pc)+": ");
        for ( int cntr = 0; cntr < elements.length; cntr++ ) {
            Element e = elements[cntr];
            String star = e.read ? "*" : "";
            String str = star+ISEValue.toString(e.value);
            Terminal.print(StringUtil.rightJustify(str, 4));
            if ( cntr % 16 == 15 )
                nextln();
        }
        nextln();
        Terminal.print("(");
        for ( int cntr = depth; cntr > 0; cntr-- ) {
            Terminal.print(ISEValue.toString(stack[cntr-1]));
            if ( cntr > 1 ) Terminal.print(", ");
        }
        Terminal.print(")");

        Terminal.nextln();
    }

    protected void nextln() {
        Terminal.print("\n        ");
    }

    public static class Element {
        public final String name;
        boolean read;
        byte value;

        public Element(String n, byte val, boolean r) {
            name = n;
            value = val;
            read = r;
        }

        public Element copy() {
            return new Element(name, value, read);
        }
    }

    public byte readElement(int element) {
        elements[element].read = true;
        return elements[element].value;
    }

    public byte getElement(int element) {
        return elements[element].value;
    }

    public void writeElement(int element, byte val) {
        elements[element].value = val;
    }

    public boolean isRead(int element) {
        return elements[element].read;
    }

    final Element[] elements;
    final byte[] stack;
    int depth;

    public ISEAbstractState(Element[] e, byte[] nstack, int ndepth) {
        elements = new Element[e.length];
        for ( int cntr = 0; cntr < e.length; cntr++ ) {
            Element et = e[cntr];
            elements[cntr] = new Element(et.name, et.value, et.read);
        }
        stack = new byte[nstack.length];
        System.arraycopy(nstack, 0, stack, 0, ndepth);
        depth = ndepth;
    }
}
