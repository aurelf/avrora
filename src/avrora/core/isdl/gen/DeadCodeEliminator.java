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

import java.util.*;

/**
 * @author Ben L. Titzer
 */
public class DeadCodeEliminator extends StmtRebuilder.DepthFirst {

    Set globals;

    protected class DefUseEnvironment {
        DefUseEnvironment parent;
        HashSet dead;
        HashSet alive;

        DefUseEnvironment(DefUseEnvironment parent) {
            this.parent = parent;
            alive = new HashSet();
            dead = new HashSet();
        }

        void use(String var) {
            alive.add(var);
            dead.remove(var);
        }

        void def(String var) {
            dead.add(var);
            alive.remove(var);
        }

        boolean isDead(String name) {
            if (alive.contains(name))
                return false;
            else if (dead.contains(name))
                return true;
            else if (parent != null) return parent.isDead(name);
            return true;
        }

        void mergeIntoParent(DefUseEnvironment sibling) {
            addLiveIns(sibling);
            addLiveIns(this);
            addDead(sibling);
        }

        private void addLiveIns(DefUseEnvironment sibling) {
            Iterator i = sibling.alive.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                parent.alive.add(o);
            }
        }

        private void addDead(DefUseEnvironment sibling) {
            Iterator i = dead.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                // dead on both branches
                if (sibling.dead.contains(o)) {
                    parent.alive.remove(o);
                    parent.dead.add(o);
                }
            }
        }
    }

    public DeadCodeEliminator(Set globals) {
        this.globals = globals;
    }

    public LinkedList process(LinkedList stmts) {
        DefUseEnvironment du = new DefUseEnvironment(null);
        du.alive.addAll(globals);
        return (LinkedList)visitStmtList(stmts, du);
    }

    public List visitStmtList(List l, Object env) {
        Collections.reverse(l);
        List nl = new LinkedList();
        boolean changed = false;

        Iterator i = l.iterator();
        while (i.hasNext()) {
            Stmt sa = (Stmt)i.next();
            Stmt na = sa.accept(this, env);
            if (na == null) {
                changed = true;
                continue;
            }
            if (na != sa) changed = true;
            nl.add(na);
        }

        if (changed) {
            Collections.reverse(nl);
            return nl;
        }
        Collections.reverse(l);
        return l;
    }

    public Stmt visit(IfStmt s, Object env) {
        DefUseEnvironment denv = (DefUseEnvironment)env;

        DefUseEnvironment tenv = new DefUseEnvironment(denv);
        List nt = visitStmtList(s.trueBranch, tenv);
        DefUseEnvironment fenv = new DefUseEnvironment(denv);
        List nf = visitStmtList(s.falseBranch, fenv);

        tenv.mergeIntoParent(fenv);

        Expr nc = s.cond.accept(this, denv);

        if (nc != s.cond || nt != s.trueBranch || nf != s.falseBranch)
            return new IfStmt(nc, nt, nf);
        else
            return s;
    }

    public Stmt visit(DeclStmt s, Object env) {
        DefUseEnvironment denv = (DefUseEnvironment)env;

        if (denv.isDead(s.name.toString())) return null;

        denv.def(s.name.toString());

        s.init.accept(this, env);
        return s;
    }

    public Stmt visit(VarAssignStmt s, Object env) {
        DefUseEnvironment denv = (DefUseEnvironment)env;

        if (denv.isDead(s.variable.toString())) return null;

        denv.def(s.variable.toString());

        s.expr.accept(this, env);
        return s;
    }

    public Expr visit(VarExpr e, Object env) {
        DefUseEnvironment denv = (DefUseEnvironment)env;
        denv.use(e.variable.toString());
        return e;
    }

}
