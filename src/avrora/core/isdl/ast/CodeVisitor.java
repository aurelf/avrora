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

/**
 * The <code>CodeVisitor</code> interface represents a visitor that is
 * more specific than the <code>ExprVisitor</code> visitor, in that it
 * contains visit methods for every type of arithmetic and logical
 * operation in the IR.
 *
 * @author Ben L. Titzer
 */
public interface CodeVisitor {

    public void visit(Arith.AddExpr e);
    public void visit(Arith.AndExpr e);
    public void visit(Arith.CompExpr e);
    public void visit(Arith.DivExpr e);
    public void visit(Arith.MulExpr e);
    public void visit(Arith.NegExpr e);
    public void visit(Arith.OrExpr e);
    public void visit(Arith.ShiftLeftExpr e);
    public void visit(Arith.ShiftRightExpr e);
    public void visit(Arith.SubExpr e);
    public void visit(Arith.XorExpr e);

    public void visit(BitExpr e);
    public void visit(BitRangeExpr e);
    public void visit(CallExpr e);

    public void visit(Literal.BoolExpr e);
    public void visit(Literal.IntExpr e);

    public void visit(Logical.AndExpr e);
    public void visit(Logical.EquExpr e);
    public void visit(Logical.GreaterEquExpr e);
    public void visit(Logical.GreaterExpr e);
    public void visit(Logical.LessEquExpr e);
    public void visit(Logical.LessExpr e);
    public void visit(Logical.NequExpr e);
    public void visit(Logical.NotExpr e);
    public void visit(Logical.OrExpr e);
    public void visit(Logical.XorExpr e);
    
    public void visit(MapExpr e);
    public void visit(VarExpr e);
}
