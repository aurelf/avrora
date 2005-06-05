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

package avrora.sim.dbbc;

import avrora.Avrora;
import avrora.core.*;
import avrora.sim.BaseInterpreter;
import avrora.sim.GenInterpreter;
import avrora.sim.InterpreterFactory;
import avrora.sim.Simulator;
import avrora.sim.mcu.MicrocontrollerProperties;

import java.util.Iterator;

/**
 * @author Ben L. Titzer
 */
public class DBBCInterpreter extends GenInterpreter {

    public static final class Factory extends InterpreterFactory {

        protected final DBBC compiler;

        public Factory(DBBC comp) {
            compiler = comp;
        }

        public BaseInterpreter newInterpreter(Simulator s, Program p, MicrocontrollerProperties pr) {
            return new DBBCInterpreter(compiler, s, p, pr);
        }
    }

    DBBC compiler;
    Program program;

    class CompiledBlockBeginInstr extends Instr {
        protected final int address;
        protected final Instr instr;
        protected final InstrVisitor interpreter;
        protected final DBBC.CompiledBlock block;


        protected CompiledBlockBeginInstr(Instr i, int a, DBBC.CompiledBlock b, InstrVisitor interp) {
            super(new InstrProperties(i.properties.name, i.properties.variant, i.properties.size, 0));
            instr = i;
            address = a;
            block = b;
            interpreter = interp;
        }

        public void accept(InstrVisitor v) {

            if (v == interpreter) {
                long headDelta = clock.getFirstEventDelta();

                if (headDelta < 0 || block.wcet < headDelta) {
                    // there is no event that could happen in the middle of this block
                    block.execute(DBBCInterpreter.this);
                } else {
                    // an event will happen during this block's execution--simply invoke one instruction
                    instr.accept(interpreter);
                }
            } else {
                instr.accept(v);
            }
        }

        public Instr build(int address, Operand[] ops) {
            return instr.build(address, ops);
        }

        public String getOperands() {
            return instr.getOperands();
        }

    }


    public DBBCInterpreter(DBBC comp, Simulator s, Program p, MicrocontrollerProperties pr) {
        super(s, p, pr);

        compiler = comp;
        program = p;

        try {
            compileProgram();
        } catch (Exception e) {
            e.printStackTrace();
            throw Avrora.failure("cannot compile program: " + e.toString());
        }
    }

    protected void compileProgram() throws Exception {
        ControlFlowGraph cfg = program.getCFG();
        Iterator i = cfg.getSortedBlockIterator();
        while (i.hasNext()) {
            ControlFlowGraph.Block b = (ControlFlowGraph.Block)i.next();
            int addr = b.getAddress();
            DBBC.CompiledBlock cb = compiler.getCompiledBlock(addr);
            if (cb != null) {
                throw Avrora.unimplemented();
                //flash_instr[addr] = new CompiledBlockBeginInstr(flash_instr[addr], addr, cb, this);
            }
        }
    }

}
