package avrora.core.isdl;

import avrora.core.isdl.ast.*;
import avrora.util.Printer;
import avrora.util.StringUtil;
import avrora.Avrora;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

/**
 * The <code>InterpreterGenerator</code> class is a visitor over the code of an
 * instruction declaration or subroutine that generates the appropriate Java
 * code that implements an interpreter for the architecture.
 *
 * @author Ben L. Titzer
 */
public class InterpreterGenerator extends StmtVisitor.DepthFirst {

    protected final Printer printer;
    protected final Architecture architecture;

    protected final HashMap mapMap;
    protected final CodeGenerator codeGen;

    protected abstract class MapRep {
        public abstract void generateWrite(Expr ind, Expr val);
        public abstract void generateBitWrite(Expr ind, Expr b, Expr val);
        public abstract void generateBitRangeWrite(Expr ind, int l, int h, Expr val);
        public abstract void generateRead(Expr ind);
        public abstract void generateBitRead(Expr ind, Expr b);
        public abstract void generateBitRangeRead(Expr ind, int l, int h);
    }

    protected class MethodMap extends MapRep {

        public final String readMeth;
        public final String writeMeth;

        MethodMap(String r, String w) {
            readMeth = r;
            writeMeth = w;
        }

        public void generateWrite(Expr ind, Expr val) {
            printer.print(writeMeth+"(");
            // TODO: visit index and value
            printer.println(");");
        }

        public void generateBitWrite(Expr ind, Expr b, Expr val) {
            printer.print(writeMeth+"(");
            // TODO: visit index and value
            printer.println(");");
        }

        public void generateBitRangeWrite(Expr ind, int l, int h, Expr val) {

        }

        public void generateRead(Expr ind) {

        }

        public void generateBitRead(Expr ind, Expr b) {

        }

        public void generateBitRangeRead(Expr ind, int l, int h) {

        }
    }


    public InterpreterGenerator(Architecture a, Printer p) {
        printer = p;
        architecture = a;
        mapMap = new HashMap();
        codeGen = new CodeGenerator();

        initializeMaps();
    }

    private void initializeMaps() {

    }

    public void generateCode() {
        Iterator i = architecture.getInstrIterator();
        while ( i.hasNext() ) {
            InstrDecl d = (InstrDecl)i.next();
            generateCode(d);
        }
    }

    public void generateCode(InstrDecl d) {
        printer.startblock("public void visit("+d.getClassName()+" i) ");
        visitStmtList(d.getCode());
        printer.endblock();
    }


    public void visit(CallStmt s) {
        printer.println(s.method.image);
        codeGen.visitExprList(s.args);
        printer.println(";");
    }

    public void visit(DeclStmt s) {
        printer.print(s.type.image+" "+s.name.image+" = ");
        s.init.accept(codeGen);
        printer.println(";");
    }

    public void visit(IfStmt s) {
        printer.print("if ( ");
        s.cond.accept(codeGen);
        printer.print(" ) ");
        printer.startblock();
        visitStmtList(s.trueBranch);
        printer.endblock();
        printer.startblock("else");
        visitStmtList(s.falseBranch);
        printer.endblock();
    }

    public void visit(MapAssignStmt s) {
        MapRep mr = getMapRep(s.mapname.image);
        mr.generateWrite(s.index, s.expr);
    }

    public void visit(MapBitAssignStmt s) {
        MapRep mr = getMapRep(s.mapname.image);
        mr.generateBitWrite(s.index, s.bit, s.expr);
    }

    public void visit(MapBitRangeAssignStmt s) {
        MapRep mr = getMapRep(s.mapname.image);
        mr.generateBitRangeWrite(s.index, s.low_bit, s.high_bit, s.expr);
    }

    private MapRep getMapRep(String n) {
        MapRep mr = (MapRep)mapMap.get(n);
        if ( mr == null )
            throw Avrora.failure("unknown map "+ StringUtil.quote(n));
        return mr;
    }

    public void visit(ReturnStmt s) {
        printer.print("return ");
        s.expr.accept(codeGen);
        printer.println(";");
    }

    public void visit(VarAssignStmt s) {
        printer.print(s.variable.image+ " = ");
        s.expr.accept(codeGen);
        printer.println(";");
    }

    public void visit(VarBitAssignStmt s) {
        printer.print(s.variable.image+" = Arithmetic.setBit("+s.variable.image+", ");
        s.expr.accept(codeGen);
        printer.println(");");
    }

    public void visit(VarBitRangeAssignStmt s) {
        printer.print(s.variable.image+ " = ");
        s.expr.accept(codeGen);
        printer.println(";");
    }

    public class CodeGenerator implements CodeVisitor {
        private void embed(Expr e, int outerPrecedence) {
            if ( e.getPrecedence() < outerPrecedence ) {
                printer.print("(");
                e.accept(this);
                printer.print(")");
            } else {
                e.accept(this);
            }
        }

        public void visit(Arith.AddExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" + ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Arith.AndExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" & ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Arith.CompExpr e) {
            e.operand.accept(this);
        }

        public void visit(Arith.DivExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" / ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Arith.MulExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" * ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Arith.NegExpr e) {
            e.operand.accept(this);
        }

        public void visit(Arith.OrExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" | ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Arith.ShiftLeftExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" << ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Arith.ShiftRightExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" >> ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Arith.SubExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" - ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Arith.XorExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" ^ ");
            embed(e.right, e.getPrecedence());
        }


        public void visit(BitExpr e) {
            e.expr.accept(this);
            e.bit.accept(this);
        }

        public void visit(BitRangeExpr e) {
            e.operand.accept(this);
        }

        public void visit(CallExpr e) {
            printer.print(e.method.image+"(");
            visitExprList(e.args);
            printer.print(")");
        }

        private void visitExprList(List l) {
            Iterator i = l.iterator();
            while ( i.hasNext() ) {
                Expr a = (Expr)i.next();
                a.accept(this);
            }
        }

        public void visit(Literal.BoolExpr e) {
            printer.print(e.toString());
        }

        public void visit(Literal.IntExpr e) {
            printer.print(e.toString());
        }

        public void visit(Logical.AndExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" && ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Logical.EquExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" == ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Logical.GreaterEquExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" >= ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Logical.GreaterExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" > ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Logical.LessEquExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" <= ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Logical.LessExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" < ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Logical.NequExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" != ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Logical.NotExpr e) {
            e.operand.accept(this);
        }

        public void visit(Logical.OrExpr e) {
            embed(e.left, e.getPrecedence());
            printer.print(" || ");
            embed(e.right, e.getPrecedence());
        }

        public void visit(Logical.XorExpr e) {
            printer.print("xor(");
            e.left.accept(this);
            printer.print(", ");
            e.right.accept(this);
            printer.print(")");
        }


        public void visit(MapExpr e) {
            e.index.accept(this);
        }

        public void visit(VarExpr e) {
            printer.print(e.variable.image);
        }
    }


    protected int getSingleBitMask(int bit) {
        return 1 << bit;
    }

    protected int getSingleInverseBitMask(int bit) {
        return ~(1 << bit);
    }

    protected int getBitRangeMask(int low, int high) {
        if ( low > high ) {
            // swap roles of low and high
            return (0xffffffff >>> (31 - low)) & (0xffffffff << high);
        } else {
            return (0xffffffff >>> (31 - high)) & (0xffffffff << low);
        }
    }

    protected int getInverseBitRangeMask(int low, int high) {
        return ~getBitRangeMask(low, high);
    }

}
