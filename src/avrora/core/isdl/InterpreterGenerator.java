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
    protected HashMap operandMap;
    protected final CodeGenerator codeGen;

    protected abstract class MapRep {
        public abstract void generateWrite(Expr ind, Expr val);
        public abstract void generateBitWrite(Expr ind, Expr b, Expr val);
        public abstract void generateRead(Expr ind);
        public abstract void generateBitRead(Expr ind, Expr b);
        public abstract void generateBitRangeWrite(Expr ind, int l, int h, Expr val);
    }

    protected class GetterSetterMap extends MapRep {

        public final String readMeth;
        public final String writeMeth;

        GetterSetterMap(String r, String w) {
            readMeth = r;
            writeMeth = w;
        }

        public void generateWrite(Expr ind, Expr val) {
            emitCall(writeMeth, ind, val);
            printer.println(";");
        }

        public void generateBitWrite(Expr ind, Expr b, Expr val) {
            // TODO: fixme
            printer.print(writeMeth+"(");
            ind.accept(codeGen);
            printer.print(", Arithmetic.setBit("+readMeth+"(");
            ind.accept(codeGen);
            printer.print("), ");
            b.accept(codeGen);
            printer.print(", ");
            val.accept(codeGen);
            printer.println("));");
        }

        public void generateRead(Expr ind) {
            emitCall(readMeth, ind);
        }

        public void generateBitRead(Expr ind, Expr b) {
            printer.print("Arithmetic.getBit("+readMeth+"(");
            ind.accept(codeGen);
            printer.print("), ");
            b.accept(codeGen);
            printer.print(")");
        }

        public void generateBitRangeWrite(Expr ind, int l, int h, Expr val) {
            if ( ind.isVariable() || ind.isLiteral() ) {
                String var = (String)operandMap.get(ind.toString());
                if ( var == null ) var = ind.toString();
                printer.print(writeMeth+"("+var+", ");
                int mask = getBitRangeMask(l, h);
                int smask = mask << l;
                int imask = ~smask;
                printer.print("("+readMeth+"("+var+")"+andString(imask) + ")");
                printer.print(" | (");
                emitAnd(val, mask);
                if ( l != 0 ) printer.print(" << " + l);
                printer.println(");");
            } else {
                throw Avrora.failure("non-constant index into map in bit-range assignment");
            }
        }
    }

    protected class IORegMap extends GetterSetterMap {
        IORegMap() {
            super("getIORegisterByte", "setIORegisterByte");
        }

        public void generateBitWrite(Expr ind, Expr b, Expr val) {
            printer.print("getIOReg(");
            ind.accept(codeGen);
            printer.print(").writeBit(");
            b.accept(codeGen);
            printer.print(", ");
            val.accept(codeGen);
            printer.println(");");
        }

        public void generateBitRead(Expr ind, Expr b) {
            printer.print("getIOReg(");
            ind.accept(codeGen);
            printer.print(").readBit(");
            b.accept(codeGen);
            printer.print(")");
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
        mapMap.put("regs", new GetterSetterMap("getRegisterByte", "setRegisterByte"));
        mapMap.put("uregs", new GetterSetterMap("getRegisterUnsigned", "setRegisterByte"));
        mapMap.put("wregs", new GetterSetterMap("getRegisterWord", "setRegisterWord"));
        mapMap.put("sram", new GetterSetterMap("getDataByte", "setDataByte"));
        mapMap.put("ioregs", new IORegMap());
        mapMap.put("program", new GetterSetterMap("getProgramByte", "setProgramByte"));
        mapMap.put("isize", new GetterSetterMap("getInstrSize", "---"));
    }

    public void generateCode() {
        printer.indent();
        // process all the instruction declarations
        Iterator i = architecture.getInstrIterator();
        while ( i.hasNext() ) {
            InstrDecl d = (InstrDecl)i.next();
            generateCode(d);
        }

        // process all the subroutine declarations
        i = architecture.getSubroutineIterator();
        while ( i.hasNext() ) {
            SubroutineDecl d = (SubroutineDecl)i.next();
            if ( !d.inline && d.hasBody() ) generateCode(d);
        }
        printer.unindent();
    }

    public void generateCode(InstrDecl d) {
        printer.startblock("public void visit("+d.getClassName()+" i) ");
        // emit the default next pc computation
        printer.println("nextPC = pc + "+(d.getEncodingSize()/8)+";");

        // initialize the map of local variables to operands
        initializeOperandMap(d);
        // emit the code of the body
        visitStmtList(d.getCode());
        // emit the cycle count update
        printer.println("cyclesConsumed += "+d.cycles+";");
        printer.endblock();
    }

    private void initializeOperandMap(InstrDecl d) {
        int regcount = 0;
        int immcount = 0;

        operandMap = new HashMap();
        Iterator i = d.getOperandIterator();
        while ( i.hasNext() ) {
            CodeRegion.Operand o = (CodeRegion.Operand)i.next();

            String name = "i.";
            if ( o.isRegister() ) {
                name += "r"+(++regcount);
            } else if ( o.isImmediate() ) {
                name += "imm"+(++immcount);
            } else {
                name += o.name.image;
            }

            operandMap.put(o.name.image, name);
        }
    }

    public void generateCode(SubroutineDecl d) {
        printer.print("public "+d.ret.image+" "+d.name.image+"(");
        Iterator i = d.getOperandIterator();
        while ( i.hasNext() ) {
            CodeRegion.Operand o = (CodeRegion.Operand)i.next();
            printer.print(o.type.image+" "+o.name.image);
            if ( i.hasNext() ) printer.print(", ");
        }
        printer.print(") ");
        printer.startblock();
        visitStmtList(d.getCode());
        printer.endblock();
    }


    public void visit(CallStmt s) {
        printer.print(s.method.image+"(");
        codeGen.visitExprList(s.args);
        printer.println(");");
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
        String var = getVariable(s.variable);
        printer.print(var+ " = ");
        s.expr.accept(codeGen);
        printer.println(";");
    }

    public void visit(VarBitAssignStmt s) {
        String var = getVariable(s.variable);
        printer.print(var+" = ");
        emitCall("Arithmetic.setBit", var, s.bit, s.expr);
        printer.println(";");
    }

    public void visit(VarBitRangeAssignStmt s) {
        String var = getVariable(s.variable);
        int mask = getBitRangeMask(s.low_bit, s.high_bit);
        int smask = mask << s.low_bit;
        int imask = ~smask;
        printer.print(var + " = (" + var + andString(imask) + ")");
        printer.print(" | (");
        emitAnd(s.expr, mask);
        if ( s.low_bit != 0 ) printer.print(" << "+s.low_bit);
        printer.println(");");
    }

    private String getVariable(Token variable) {
        String var = (String)operandMap.get(variable.image);
        if ( var == null ) var = variable.image;
        return var;
    }

    private void emitBinOp(Expr e, String op, int p, int val) {
        printer.print("(");
        codeGen.inner(e, p);
        printer.print(" "+op+" "+val+")");
    }

    private String andString(int mask) {
        return " & 0x"+StringUtil.toHex(mask,8);
    }

    private void emitAnd(Expr e, int val) {
        printer.print("(");
        codeGen.inner(e, Expr.PREC_A_AND);
        printer.print(andString(val)+")");
    }

    private void emitCall(String s, Expr e) {
        printer.print(s+"(");
        e.accept(codeGen);
        printer.print(")");
    }

    private void emitCall(String s, Expr e1, Expr e2) {
        printer.print(s+"(");
        e1.accept(codeGen);
        printer.print(", ");
        e2.accept(codeGen);
        printer.print(")");
    }

    private void emitCall(String s, Expr e1, Expr e2, Expr e3) {
        printer.print(s+"(");
        e1.accept(codeGen);
        printer.print(", ");
        e2.accept(codeGen);
        printer.print(", ");
        e3.accept(codeGen);
        printer.print(")");
    }

    private void emitCall(String s, String e1, Expr e2, Expr e3) {
        printer.print(s+"("+e1+", ");
        e2.accept(codeGen);
        printer.print(", ");
        e3.accept(codeGen);
        printer.print(")");
    }

    private void emitCall(String s, String e1, Expr e2) {
        printer.print(s+"("+e1+", ");
        e2.accept(codeGen);
        printer.print(")");
    }

    public class CodeGenerator implements CodeVisitor {

        private void inner(Expr e, int outerPrecedence) {
            if ( e.getPrecedence() < outerPrecedence ) {
                printer.print("(");
                e.accept(this);
                printer.print(")");
            } else {
                e.accept(this);
            }
        }

        private void binop(String op, Expr left, Expr right, int p) {
            inner(left, p);
            printer.print(" "+op+" ");
            inner(right, p);
        }

        public void visit(Arith.AddExpr e) {
            binop("+", e.left, e.right, e.getPrecedence());
        }

        public void visit(Arith.AndExpr e) {
            binop("&", e.left, e.right, e.getPrecedence());
        }

        public void visit(Arith.CompExpr e) {
            printer.print(e.operation);
            inner(e.operand, e.getPrecedence());
        }

        public void visit(Arith.DivExpr e) {
            binop("/", e.left, e.right, e.getPrecedence());
        }

        public void visit(Arith.MulExpr e) {
            binop("*", e.left, e.right, e.getPrecedence());
        }

        public void visit(Arith.NegExpr e) {
            printer.print(e.operation);
            inner(e.operand, e.getPrecedence());
        }

        public void visit(Arith.OrExpr e) {
            binop("|", e.left, e.right, e.getPrecedence());
        }

        public void visit(Arith.ShiftLeftExpr e) {
            binop("<<", e.left, e.right, e.getPrecedence());
        }

        public void visit(Arith.ShiftRightExpr e) {
            binop(">>", e.left, e.right, e.getPrecedence());
        }

        public void visit(Arith.SubExpr e) {
            binop("-", e.left, e.right, e.getPrecedence());
        }

        public void visit(Arith.XorExpr e) {
            binop("^", e.left, e.right, e.getPrecedence());
        }


        public void visit(BitExpr e) {
            if ( e.expr.isMap() ) {
                MapExpr me = (MapExpr)e.expr;
                MapRep mr = getMapRep(me.mapname.image);
                mr.generateBitRead(me.index, e.bit);
            } else {
                if ( e.bit.isLiteral() ) {
                    int mask = getSingleBitMask(((Literal.IntExpr)e.bit).value);
                    printer.print("((");
                    inner(e.expr,  Expr.PREC_A_ADD);
                    printer.print(" & "+mask+") != 0");
                    printer.print(")");
                } else {
                    emitCall("Arithmetic.getBit", e.expr, e.bit);
                }
            }
        }

        public void visit(BitRangeExpr e) {
            int mask = getBitRangeMask(e.low_bit, e.high_bit);
            int low = e.low_bit;
            if ( low != 0 ) {
                printer.print("(");
                emitBinOp(e.operand, ">>", Expr.PREC_A_SHIFT, low);
                printer.print(" & 0x"+StringUtil.toHex(mask, 8)+")");
            } else {
                emitAnd(e.operand, mask);
            }
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
                if ( i.hasNext() ) printer.print(", ");
            }
        }

        public void visit(Literal.BoolExpr e) {
            printer.print(e.toString());
        }

        public void visit(Literal.IntExpr e) {
            printer.print(e.toString());
        }

        public void visit(Logical.AndExpr e) {
            binop("&&", e.left, e.right, e.getPrecedence());
        }

        public void visit(Logical.EquExpr e) {
            binop("==", e.left, e.right, e.getPrecedence());
        }

        public void visit(Logical.GreaterEquExpr e) {
            binop(">=", e.left, e.right, e.getPrecedence());
        }

        public void visit(Logical.GreaterExpr e) {
            binop(">", e.left, e.right, e.getPrecedence());
        }

        public void visit(Logical.LessEquExpr e) {
            binop("<=", e.left, e.right, e.getPrecedence());
        }

        public void visit(Logical.LessExpr e) {
            binop("<", e.left, e.right, e.getPrecedence());
        }

        public void visit(Logical.NequExpr e) {
            binop("!=", e.left, e.right, e.getPrecedence());
        }

        public void visit(Logical.NotExpr e) {
            printer.print("!");
            inner(e.operand, e.getPrecedence());
        }

        public void visit(Logical.OrExpr e) {
            binop("||", e.left, e.right, e.getPrecedence());
        }

        public void visit(Logical.XorExpr e) {
            emitCall("xor", e.left, e.right);
        }


        public void visit(MapExpr e) {
            MapRep mr = getMapRep(e.mapname.image);
            mr.generateRead(e.index);
        }

        public void visit(VarExpr e) {
            printer.print(getVariable(e.variable));
        }
    }


    protected int getSingleBitMask(int bit) {
        return 1 << bit;
    }

    protected int getSingleInverseBitMask(int bit) {
        return ~(1 << bit);
    }

    protected int getBitRangeMask(int low, int high) {
        return (0xffffffff >>> (31 - (high - low)));
    }

    protected int getInverseBitRangeMask(int low, int high) {
        return ~getBitRangeMask(low, high);
    }

}