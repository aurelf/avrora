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

package avrora.core.isdl.gen;

import avrora.core.isdl.ast.*;
import avrora.util.Arithmetic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Ben L. Titzer
 */
public class ConstantPropagator extends StmtRebuilder.DepthFirst {

    protected static Literal.IntExpr ZERO = new Literal.IntExpr(0);
    protected static Literal.IntExpr ONE = new Literal.IntExpr(1);
    protected static Literal.BoolExpr TRUE = new Literal.BoolExpr(true);
    protected static Literal.BoolExpr FALSE = new Literal.BoolExpr(false);

    protected static HashSet trackedMaps;

    protected class ConstantEnvironment {
        ConstantEnvironment parent;
        HashMap constantMap;
        HashMap mapMap; // HashMap<String, HashMap>

        ConstantEnvironment(ConstantEnvironment p) {
            parent = p;
            constantMap = new HashMap();
            mapMap = new HashMap();
        }

        Expr lookup(String name) {
            Expr e = (Expr)constantMap.get(name);
            if (e != null) return e;
            if (parent != null)
                return parent.lookup(name);
            else
                return null;
        }

        void put(String name, Expr e) {
            if (e instanceof Literal.IntExpr) {
                int ival = intValueOf(e);
                if (ival == 0)
                    e = ZERO;
                else if (ival == 1) e = ONE;
            } else if (e instanceof Literal.BoolExpr) {
                boolean bval = boolValueOf(e);
                e = bval ? TRUE : FALSE;
            }
            constantMap.put(name, e);
        }

        void remove(String name) {
            constantMap.remove(name);
            if (parent != null) parent.remove(name);
        }

        Expr lookupMap(String name, int index) {
            if (!trackedMaps.contains(name)) return null;

            return lookupMap_fast(name, index);
        }

        private Expr lookupMap_fast(String name, int index) {
            HashMap map = (HashMap)mapMap.get(name);
            if (map != null) {
                Expr e = (Expr)map.get(new Integer(index));
                if (e != null) return e;
            }

            if (parent != null) {
                return parent.lookupMap_fast(name, index);
            }

            return null;
        }

        void putMap(String mapname, int index, Expr e) {
            if (!trackedMaps.contains(mapname)) return;

            HashMap map = (HashMap)mapMap.get(mapname);
            if (map == null) {
                map = new HashMap();
                mapMap.put(mapname, map);
            }

            map.put(new Integer(index), e);

        }

        void removeMap(String mapname, int index) {
            if (!trackedMaps.contains(mapname)) return;

            HashMap map = (HashMap)mapMap.get(mapname);
            if (map != null) {
                mapMap.remove(new Integer(index));
            }

            if (parent != null) parent.removeMap(mapname, index);
        }

        void removeAll(String mapname) {
            if (!trackedMaps.contains(mapname)) return;

            mapMap.remove(mapname);

            if (parent != null) parent.removeAll(mapname);
        }

        void mergeToParent(ConstantEnvironment sibling) {
            mergeIntoParent(this, sibling);
            mergeIntoParent(sibling, this);
        }

        void mergeIntoParent(ConstantEnvironment a, ConstantEnvironment b) {
            Iterator i = a.constantMap.keySet().iterator();
            while (i.hasNext()) {
                String key = (String)i.next();
                Expr e = a.lookup(key);
                Expr o = b.lookup(key);
                // TODO: reference equality is just a first-order approximation of expression equality
                if (e == o)
                    a.parent.put(key, e);
                else
                    a.parent.remove(key);
            }
        }
    }

    public ConstantPropagator() {
        trackedMaps = new HashSet();
        trackedMaps.add("$regs"); // this is all for now
    }

    public LinkedList process(LinkedList stmts) {
        LinkedList s = (LinkedList)visitStmtList(stmts, new ConstantEnvironment(null));
        return s;
    }

    public Stmt visit(DeclStmt s, Object env) {
        ConstantEnvironment cenv = (ConstantEnvironment)env;

        Expr ne = update(s.name.toString(), s.init, cenv);
        if (s.init != ne)
            return new DeclStmt(s.name, s.type, ne);
        else
            return s;
    }

    public Stmt visit(VarAssignStmt s, Object env) {
        ConstantEnvironment cenv = (ConstantEnvironment)env;

        Expr ne = update(s.variable.toString(), s.expr, cenv);
        if (s.expr != ne)
            return new VarAssignStmt(s.variable, ne);
        else
            return s;
    }

    public Stmt visit(VarBitRangeAssignStmt s, Object env) {
        ConstantEnvironment cenv = (ConstantEnvironment)env;

        Expr ne = s.expr.accept(this, env);

        if (ne.isLiteral()) {
            Expr ve = cenv.lookup(s.variable.toString());

            if (ve != null && ve.isLiteral()) {
                int eval = intValueOf(ne);
                int vval = intValueOf(ve);
                int mask = Arithmetic.getBitRangeMask(s.low_bit, s.high_bit);
                int smask = mask << s.low_bit;
                int imask = ~smask;

                // TODO: make sure this is correct!
                int nval = vval & imask | ((eval & mask) << s.low_bit);
                cenv.put(s.variable.toString(), new Literal.IntExpr(nval));
            } else {
                cenv.remove(s.variable.toString());
            }
        } else {
            cenv.remove(s.variable.toString());
        }

        if (ne != s.expr)
            return new VarBitRangeAssignStmt(s.variable, s.low_bit, s.high_bit, ne);
        else
            return s;
    }

    public Stmt visit(VarBitAssignStmt s, Object env) {
        ConstantEnvironment cenv = (ConstantEnvironment)env;

        Expr ne = s.expr.accept(this, env);
        Expr nb = s.bit.accept(this, env);

        if (ne.isLiteral() && nb.isLiteral()) {
            Expr ve = cenv.lookup(s.variable.toString());

            if (ve != null && ve.isLiteral()) {
                boolean eval = boolValueOf(ne);
                int bval = intValueOf(nb);
                int vval = intValueOf(ve);

                cenv.put(s.variable.toString(), new Literal.IntExpr(Arithmetic.setBit(vval, bval, eval)));
            } else {
                cenv.remove(s.variable.toString());
            }
        } else {
            cenv.remove(s.variable.toString());
        }

        if (ne != s.expr || nb != s.bit)
            return new VarBitAssignStmt(s.variable, nb, ne);
        else
            return s;
    }

    public Stmt visit(MapAssignStmt s, Object env) {
        ConstantEnvironment cenv = (ConstantEnvironment)env;

        Expr ni = s.index.accept(this, env);
        Expr ne = s.expr.accept(this, env);

        String mapname = s.mapname.toString();
        if (ni.isLiteral()) {
            int index = intValueOf(ni);

            if (ne.isVariable()) {
                VarExpr ve = (VarExpr)ne;
                Expr e = cenv.lookup(ve.variable.toString());
                if (e != null) {
                    // propagate the constant
                    cenv.putMap(mapname, index, e);
                } else {
                    // propagate the copy
                    cenv.putMap(mapname, index, ve);
                }
            } else if (ne.isLiteral()) {
                // propagate this constant forward
                cenv.putMap(mapname, index, ne);
            } else {
                cenv.removeMap(mapname, index);
            }

        } else {
            cenv.removeAll(mapname);
        }

        if (ni != s.index || ne != s.expr)
            return new MapAssignStmt(s.mapname, ni, ne);
        else
            return s;
    }


    private Expr update(String name, Expr val, ConstantEnvironment cenv) {
        Expr ne = val.accept(this, cenv);
        if (ne.isLiteral()) {
            // propagate this constant forward
            cenv.put(name, ne);
        } else if (ne.isVariable()) {
            VarExpr ve = (VarExpr)ne;
            Expr e = cenv.lookup(ve.variable.toString());
            if (e != null) {
                // propagate the constant
                cenv.put(name, e);
            } else {
                // propagate the copy
                cenv.put(name, ve);
            }
        } else {
            // complex expression: remove from constant map
            cenv.remove(name);
        }
        return ne;
    }

    public Expr visit(VarExpr e, Object env) {
        ConstantEnvironment cenv = (ConstantEnvironment)env;

        Expr ce = cenv.lookup(e.variable.toString());
        if (ce != null)
            return ce;
        else
            return e;
    }

    public Expr visit(BitExpr e, Object env) {
        Expr nexpr = e.expr.accept(this, env);
        Expr nbit = e.bit.accept(this, env);

        if (nexpr.isLiteral() && nbit.isLiteral()) {
            int eval = intValueOf(nexpr);
            int bval = intValueOf(nbit);
            return new Literal.BoolExpr(Arithmetic.getBit(eval, bval));
        }

        if (nexpr != e.expr || nbit != e.bit)
            return new BitExpr(nexpr, nbit);
        else
            return e;
    }

    public Expr visit(BitRangeExpr e, Object env) {
        Expr nexpr = e.operand.accept(this, env);

        if (nexpr.isLiteral()) {
            int eval = intValueOf(nexpr);
            int mask = Arithmetic.getBitRangeMask(e.low_bit, e.high_bit);
            return new Literal.IntExpr((eval >> e.low_bit) & mask);
        }

        if (nexpr != e.operand)
            return new BitRangeExpr(nexpr, e.low_bit, e.high_bit);
        else
            return e;
    }

    // --- binary operations ---

    public Expr visit(Arith.AddExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return new Literal.IntExpr(lval + rval);
        }

        if (l != e.left || r != e.right)
            return new Arith.AddExpr(l, r);
        else
            return e;
    }

    public Expr visit(Arith.AndExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return new Literal.IntExpr(lval & rval);
        }

        if (l != e.left || r != e.right)
            return new Arith.AndExpr(l, r);
        else
            return e;
    }

    public Expr visit(Arith.CompExpr e, Object env) {
        Expr o = e.operand.accept(this, env);

        if (o.isLiteral()) {
            int lval = intValueOf(o);
            return new Literal.IntExpr(~lval);
        }

        if (o != e.operand) return new Arith.CompExpr(o);
        return e;
    }

    public Expr visit(Arith.DivExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return new Literal.IntExpr(lval / rval);
        }
        if (l != e.left || r != e.right)
            return new Arith.DivExpr(l, r);
        else
            return e;
    }

    public Expr visit(Arith.MulExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return new Literal.IntExpr(lval * rval);
        }

        if (l != e.left || r != e.right)
            return new Arith.MulExpr(l, r);
        else
            return e;
    }

    public Expr visit(Arith.NegExpr e, Object env) {
        Expr o = e.operand.accept(this, env);

        if (o.isLiteral()) {
            int lval = intValueOf(o);
            return new Literal.IntExpr(-lval);
        }

        if (o != e.operand) return new Arith.NegExpr(o);
        return e;
    }

    public Expr visit(Arith.OrExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return new Literal.IntExpr(lval | rval);
        }

        if (l != e.left || r != e.right)
            return new Arith.OrExpr(l, r);
        else
            return e;
    }

    public Expr visit(Arith.ShiftLeftExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return new Literal.IntExpr(lval << rval);
        }

        if (l != e.left || r != e.right)
            return new Arith.ShiftLeftExpr(l, r);
        else
            return e;
    }

    public Expr visit(Arith.ShiftRightExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return new Literal.IntExpr(lval >> rval);
        }

        if (l != e.left || r != e.right)
            return new Arith.ShiftRightExpr(l, r);
        else
            return e;
    }

    public Expr visit(Arith.SubExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return new Literal.IntExpr(lval - rval);
        }

        if (l != e.left || r != e.right)
            return new Arith.SubExpr(l, r);
        else
            return e;
    }

    public Expr visit(Arith.XorExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return new Literal.IntExpr(lval ^ rval);
        }

        if (l != e.left || r != e.right)
            return new Arith.XorExpr(l, r);
        else
            return e;
    }

    public Expr visit(Literal.BoolExpr e, Object env) {
        if (e.value)
            return TRUE;
        else
            return FALSE;
    }

    public Expr visit(Literal.IntExpr e, Object env) {
        if (e.value == 0)
            return ZERO;
        else if (e.value == 1)
            return ONE;
        else
            return e;
    }

    public Expr visit(Logical.AndExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l == FALSE) {
            return FALSE;
        } else if (l == TRUE) {
            return r;
        }

        if (l != e.left || r != e.right)
            return new Logical.AndExpr(l, r);
        else
            return e;
    }

    public Expr visit(Logical.EquExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return lval == rval ? TRUE : FALSE;
        }

        if (l != e.left || r != e.right)
            return new Logical.EquExpr(l, r);
        else
            return e;
    }

    public Expr visit(Logical.GreaterEquExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return lval >= rval ? TRUE : FALSE;
        }

        if (l != e.left || r != e.right)
            return new Logical.GreaterEquExpr(l, r);
        else
            return e;
    }

    public Expr visit(Logical.GreaterExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return lval > rval ? TRUE : FALSE;
        }

        if (l != e.left || r != e.right)
            return new Logical.GreaterExpr(l, r);
        else
            return e;
    }

    public Expr visit(Logical.LessEquExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return lval <= rval ? TRUE : FALSE;
        }

        if (l != e.left || r != e.right)
            return new Logical.LessEquExpr(l, r);
        else
            return e;
    }

    public Expr visit(Logical.LessExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return lval < rval ? TRUE : FALSE;
        }

        if (l != e.left || r != e.right)
            return new Logical.LessExpr(l, r);
        else
            return e;
    }

    public Expr visit(Logical.NequExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            int lval = intValueOf(l);
            int rval = intValueOf(r);
            return lval != rval ? TRUE : FALSE;
        }

        if (l != e.left || r != e.right)
            return new Logical.NequExpr(l, r);
        else
            return e;
    }

    public Expr visit(Logical.NotExpr e, Object env) {
        Expr ne = e.operand.accept(this, env);

        if (ne == TRUE) {
            return FALSE;
        } else if (ne == FALSE) {
            return TRUE;
        }

        if (ne != e.operand) return new Logical.NotExpr(ne);
        return e;
    }

    public Expr visit(Logical.OrExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l == TRUE) {
            return TRUE;
        } else if (l == FALSE) {
            return r;
        }

        if (l != e.left || r != e.right)
            return new Logical.OrExpr(l, r);
        else
            return e;
    }

    public Expr visit(Logical.XorExpr e, Object env) {
        Expr l = e.left.accept(this, env);
        Expr r = e.right.accept(this, env);

        if (l.isLiteral() && r.isLiteral()) {
            boolean lval = boolValueOf(l);
            boolean rval = boolValueOf(r);
            return lval != rval ? TRUE : FALSE;
        } else if (l == FALSE) {
            return r;
        } else if (r == FALSE) {
            return l;
        } else if (l == TRUE) {
            return new Logical.NotExpr(r);
        } else if (r == TRUE) {
            return new Logical.NotExpr(l);
        }

        if (l != e.left || r != e.right)
            return new Logical.XorExpr(l, r);
        else
            return e;
    }

    public Expr visit(MapExpr e, Object env) {
        ConstantEnvironment cenv = (ConstantEnvironment)env;

        Expr ne = e.index.accept(this, env);

        if (ne.isLiteral()) {
            int index = intValueOf(ne);
            Expr v = cenv.lookupMap(e.mapname.toString(), index);
            if (v != null) return v;
        }

        if (ne != e.index) return new MapExpr(e.mapname, ne);
        return e;
    }

    // --- utilities ---

    private int intValueOf(Expr nexpr) {
        return ((Literal.IntExpr)nexpr).value;
    }

    private boolean boolValueOf(Expr nexpr) {
        return ((Literal.BoolExpr)nexpr).value;
    }
}
