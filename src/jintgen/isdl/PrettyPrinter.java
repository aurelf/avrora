/**
 * Copyright (c) 2005, Regents of the University of California
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

package jintgen.isdl;

import cck.text.Printer;
import jintgen.jigir.*;
import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class PrettyPrinter extends StmtVisitor.DepthFirst {

    final Printer p;
    private Architecture arch;

    PrettyPrinter(Architecture arch, Printer p) {
        this.arch = arch;
        this.p = p;
    }

    public void visitStmtList(List<Stmt> s) {
        p.startblock();
        if (s == null) {
            p.println(" // empty body");
        } else {
            for ( Stmt st : s ) st.accept(this);

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

    public void visit(AssignStmt s) {
        p.println(s.toString());
    }

    public void visit(ReturnStmt s) {
        p.println(s.toString());
    }
}
