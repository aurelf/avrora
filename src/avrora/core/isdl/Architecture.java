package avrora.core.isdl;

import avrora.util.Verbose;
import avrora.util.StringUtil;
import avrora.util.Printer;
import avrora.core.isdl.ast.*;
import avrora.Avrora;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * @author Ben L. Titzer
 */
public class Architecture {

    Verbose.Printer printer = Verbose.getVerbosePrinter("isdl");

    HashMap subroutines;
    HashMap instructions;
    HashMap operands;
    HashMap encodings;

    public Architecture() {
        subroutines = new HashMap();
        instructions = new HashMap();
        operands = new HashMap();
        encodings = new HashMap();
    }

    public void process() {
        Iterator i = instructions.values().iterator();
        while ( i.hasNext() ) {
            InstrDecl id = (InstrDecl)i.next();
            printer.print("processing instruction "+id.name+" ");
            Inliner br = new Inliner(null);
            br.visitStmtList(id.execute);
            id.execute = new Optimizer(br.newStmts).optimize();

            if ( printer.enabled ) {
                new PrettyPrinter(printer).visitStmtList(id.execute);
            }

        }
    }

    public void dump() {

    }

    public void addSubroutine(SubroutineDecl d) {
        printer.println("loading subroutine "+d.name.image+"...");
        subroutines.put(d.name.image, d);
    }

    public void addInstruction(InstrDecl i) {
        printer.println("loading instruction "+i.name.image+"...");
        instructions.put(i.name.image, i);
    }

    public void addOperand(OperandDecl d) {
        printer.println("loading operand declaration "+d.name.image+"...");
        operands.put(d.name.image, d);
    }

    public void addEncoding(EncodingDecl d) {
        printer.println("loading encoding format "+d.name.image+"...");
        encodings.put(d.name.image, d);
    }

    public InstrDecl getInstruction(String name) {
        return (InstrDecl)instructions.get(name);
    }

    public SubroutineDecl getSubroutine(String name) {
        return (SubroutineDecl)subroutines.get(name);
    }


    /**
     * The <code>Inliner</code> class implements a visitor over the code that
     * inlines calls to known subroutines. This produces code that is free
     * of calls to the subroutines declared within the architecture description
     * and therefore is ready for constant and copy propagation optimizations.
     *
     * The <code>Inliner</code> will aggressively inline all calls, therefore
     * it cannot detect recursion. It assumes that return statements are at
     * the end of subroutines and do not occur in branches. This is not
     * enforced by any checking, which should be done in the future.
     *
     * @author Ben L. Titzer
     */
    class Inliner extends StmtRebuilder.DepthFirst {
        final Inliner parent;
        List newStmts;
        HashMap varMap;
        int tmpCount;
        String retVal;
        SubroutineDecl curSubroutine;

        Inliner(Inliner p, List ns) {
            parent = p;
            newStmts = ns;
            varMap = new HashMap();
        }

        Inliner(Inliner p) {
            parent = p;
            newStmts = new LinkedList();
            varMap = new HashMap();
        }

        public Stmt visit(CallStmt s) {
            SubroutineDecl d = getSubroutine(s.method.image);
            if ( d == null ) {
                 return new CallStmt(s.method, visitExprList(s.args));
            }
            else {
                inlineCall(s.method, d, s.args);
                return null;
            }
        }

        public Stmt visit(VarAssignStmt s) {
             String nv = varName(s.variable);
             return new VarAssignStmt(newToken(nv), s.expr.accept(this));
        }

        public Stmt visit(VarBitAssignStmt s) {
            String nv = varName(s.variable);
            return (new VarBitAssignStmt(newToken(nv), s.bit.accept(this), s.expr.accept(this)));
        }

        public Stmt visit(VarBitRangeAssignStmt s) {
            String nv = varName(s.variable);
            return (new VarBitRangeAssignStmt(newToken(nv), s.low_bit, s.high_bit, s.expr.accept(this)));
        }

        public Stmt visit(DeclStmt s) {
            String nv = newTemp(s.name.image);
            return (new DeclStmt(newToken(nv), s.type, s.init.accept(this)));
        }

        public Stmt visit(ReturnStmt s) {
            if ( curSubroutine == null )
                Avrora.failure("return not within subroutine!");

            retVal = newTemp(null);
            return (new DeclStmt(newToken(retVal), curSubroutine.ret, s.expr.accept(this)));
        }


        public Stmt visit(IfStmt s) {
            Expr nc = s.cond.accept(this);
            Inliner tr = new Inliner(this);
            Inliner fr = new Inliner(this);
            tr.visitStmtList(s.trueBranch);
            fr.visitStmtList(s.falseBranch);
            return (new IfStmt(nc, tr.newStmts, fr.newStmts));
        }

        protected String newTemp(String orig) {
            String nn;
            if ( parent != null ) nn = parent.newTemp(null);
            else nn = "tmp_"+(tmpCount++);

            if ( orig != null ) varMap.put(orig, nn);
            return nn;
        }

        protected String inlineCall(Token m, SubroutineDecl d, List args) {
            if ( d.args.size() != args.size() )
                Avrora.failure("arity mismatch in call to "+m.image+" @ "+m.beginLine+":"+m.beginColumn);

            Inliner bodyBuilder = new Inliner(this, newStmts);

            Iterator formal_iter = d.args.iterator();
            Iterator arg_iter = args.iterator();

            while ( formal_iter.hasNext() ) {
                SubroutineDecl.Formal f = (SubroutineDecl.Formal)formal_iter.next();
                Expr e = (Expr)arg_iter.next();

                // get a new temporary
                String nn = newTemp(null);

                // put the arguments in the alpha-rename map for the body
                bodyBuilder.varMap.put(f.name.image, nn);
                bodyBuilder.curSubroutine = d;

                // alpha-rename expression that is argument
                Expr ne = e.accept(this);
                newStmts.add(new DeclStmt(nn, f.type, ne));
            }

            // process body
            bodyBuilder.visitStmtList(d.execute);

            return bodyBuilder.retVal;
        }


        public Expr visit(CallExpr v) {
            SubroutineDecl d = getSubroutine(v.method.image);
            if ( d == null || d.execute == null ) {
                return super.visit(v);
            } else {
                String result = inlineCall(v.method, d, v.args);
                return new VarExpr(result);
            }
        }

        public Expr visit(VarExpr v) {
            // alpha rename all variables
            return new VarExpr(varName(v.variable));
        }

        protected String varName(String n) {
            String nn = (String)varMap.get(n);
            if ( nn == null ) return n;
            return nn;
        }

        protected String varName(Token n) {
            return varName(n.image);
        }

        protected Token newToken(String t) {
            Token tk = new Token();
            tk.image = t;
            tk.kind = ISDLParserConstants.IDENTIFIER;
            return tk;
        }
    }

    public class PrettyPrinter extends StmtVisitor.DepthFirst {

        final Printer p;

        PrettyPrinter(Printer p) {
            this.p = p;
        }

        public void visitStmtList(List s) {
            p.startblock();
            Iterator i = s.iterator();
            while ( i.hasNext() ) {
                Stmt st = (Stmt)i.next();
                st.accept(this);
            }
            p.endblock();
        }

        public void visit(IfStmt s) {
            p.print("if ( ");
            p.print(s.cond.toString());
            p.print(" ) ");
            visitStmtList(s.trueBranch);
            p.print("else ");
            visitStmtList(s.falseBranch);
        }

        public void visit(CallStmt s) {
            p.println(s.toString());
        }

        public void visit(DeclStmt s) {
            p.println(s.toString());
        }

        public void visit(MapAssignStmt s) {
            p.println(s.toString());
        }

        public void visit(MapBitAssignStmt s) {
            p.println(s.toString());
        }

        public void visit(MapBitRangeAssignStmt s) {
            p.println(s.toString());
        }

        public void visit(ReturnStmt s) {
            p.println(s.toString());
        }

        public void visit(VarAssignStmt s) {
            p.println(s.toString());
        }

        public void visit(VarBitAssignStmt s) {
            p.println(s.toString());
        }

        public void visit(VarBitRangeAssignStmt s) {
            p.println(s.toString());
        }

    }

}
