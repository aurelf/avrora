/**
 * Copyright (c) 2004, Regents of the University of California
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

package avrora.core.isdl.ast;

import java.util.Iterator;
import java.util.List;

/**
 * The <code>StmtVisitor</code> interface implements the visitor pattern so that clients can visit the
 * abstract syntax tree nodes representing statements in the program.
 *
 * @author Ben L. Titzer
 */
public interface StmtVisitor {

    public void visit(CallStmt s);

    public void visit(CommentStmt s);

    public void visit(DeclStmt s);

    public void visit(IfStmt s);

    public void visit(MapAssignStmt s);

    public void visit(MapBitAssignStmt s);

    public void visit(MapBitRangeAssignStmt s);

    public void visit(ReturnStmt s);

    public void visit(VarAssignStmt s);

    public void visit(VarBitAssignStmt s);

    public void visit(VarBitRangeAssignStmt s);

    /**
     * The <code>DepthFirst</code> class is a base implementation of the <code>StmtVisitor</code> interface
     * that visits the tree in depth-first order.
     *
     * @author Ben L. Titzer
     */
    public class DepthFirst implements StmtVisitor {
        public void visit(CallStmt s) {
            // terminal node
        }

        public void visit(CommentStmt s) {
            // terminal node
        }
        public void visit(DeclStmt s) {
            // terminal node
        }

        public void visit(IfStmt s) {
            visitStmtList(s.trueBranch);
            visitStmtList(s.falseBranch);
        }

        protected void visitStmtList(List l) {
            Iterator i = l.iterator();
            // visit all the statements in the block
            while (i.hasNext()) {
                Stmt t = (Stmt)i.next();
                t.accept(this);
            }
        }

        public void visit(MapAssignStmt s) {
            // terminal node
        }

        public void visit(MapBitAssignStmt s) {
            // terminal node
        }

        public void visit(MapBitRangeAssignStmt s) {
            // terminal node
        }

        public void visit(ReturnStmt s) {
            // terminal node
        }

        public void visit(VarAssignStmt s) {
            // terminal node
        }

        public void visit(VarBitAssignStmt s) {
            // terminal node
        }

        public void visit(VarBitRangeAssignStmt s) {
            // terminal node
        }

    }
}
