package avrora.core.isdl;

import avrora.core.isdl.ast.Expr;

import java.util.List;
import java.util.Iterator;

/**
 * @author Ben L. Titzer
 */
public class EncodingDecl {

    public final Token name;

    public final List fields;

    public int bitWidth = -1;

    public EncodingDecl(Token n, List f) {
        name = n;
        fields = f;
    }

    public static class Derived extends EncodingDecl {
        public final Token pname;
        public final List subst;
        public EncodingDecl parent;

        public Derived(Token n, Token p, List s) {
            super(n, null);
            pname = p;
            subst = s;
        }

        public void setParent(EncodingDecl p) {
            parent = p;
        }

        public int getBitWidth() {
            if ( bitWidth < 0 )
                bitWidth = parent.getBitWidth();
            return bitWidth;
        }

    }

    public int getBitWidth() {
        if ( bitWidth < 0 )
            bitWidth = computeBitWidth();
        return bitWidth;
    }

    private int computeBitWidth() {
        int accum = 0;
        Iterator i = fields.iterator();
        while ( i.hasNext() ) {
            Expr e = (Expr)i.next();
            accum += e.getBitWidth();
        }
        return accum;
    }


}
