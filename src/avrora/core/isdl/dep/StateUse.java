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

package avrora.core.isdl.dep;

import avrora.core.isdl.CodeRegion;

/**
 * The <code>StateUse</code> class represents the result of a dependency analysis of a particular instruction
 * declaration (e.g. the ADD instruction's declaration). It represents a value that might be read or written by the
 * instruction. For example, the instruction might read registers, memory, or write registers, memory, or other internal
 * state.
 *
 * @author Ben L. Titzer
 */
public abstract class StateUse {

    /**
     * The <code>Index</code> class represents a possible index into a map (such as the register file or memory). An
     * index might be a constant, an instruction operand, a linear combination of an operand and a constant, or it could
     * be an unknown (dynamically computed) value.
     */
    public static class Index {

        /**
         * The <code>isConstant()</code> method returns whether this index is a constant that is not dependent on either
         * operands to the instruction or runtime values
         *
         * @return true if this index is a constant that is not dependent on either instruction operands or runtime
         *         values; false otherwise
         */
        public boolean isConstant() {
            return false;
        }

        /**
         * The <code>isKnown()</code> method returns whether this index is known statically given the instruction
         * operands. Thus, for a given instruction instance, this index can be computed without runtime values.
         *
         * @return true if this index can be computed solely from instruction operands.
         */
        public boolean isKnown() {
            return true;
        }

        public static class Constant extends Index {
            public final int index;

            Constant(int i) {
                index = i;
            }

            public boolean isConstant() {
                return true;
            }
        }

        public static class Operand extends Index {
            public final CodeRegion.Operand operand;

            Operand(CodeRegion.Operand op) {
                operand = op;
            }
        }

        public static class OperandPlusConstant extends Index {
            public final CodeRegion.Operand operand;
            public final int index;

            OperandPlusConstant(CodeRegion.Operand op, int i) {
                operand = op;
                index = i;
            }
        }

        public static class UNKNOWN extends Index {
            public boolean isKnown() {
                return false;
            }
        }

    }

    /**
     * The <code>MapUse</code> class represents the usage of a map (i.e. a register file or memory). The map has a
     * string that describes its name as well as an instance of the <code>StateUse.Index</code> class that indicates the
     * index in the map that is read or written.
     */
    public static class MapUse extends StateUse {
        public final String mapname;
        public final Index index;
        public final boolean definite;

        MapUse(String mn, Index i, boolean d) {
            mapname = mn;
            index = i;
            definite = d;
        }
    }


    /**
     * The <code>GlobalUse</code> class represents the usage of a globally declared state variable that is visible
     * within the instruction specification. Its name as a string is given.
     */
    public static class GlobalUse extends StateUse {
        public final String globalName;
        public final boolean definite;

        GlobalUse(String gn, boolean d) {
            globalName = gn;
            definite = d;
        }
    }

    /**
     * The <code>BitUse</code> class represents the usage of a bit of some state that is used. It contains a reference
     * to the <code>StateUse</code> representing the whole state variable, as well as an index that indicates the actual
     * bit that is read or written.
     */
    public static class BitUse extends StateUse {
        public final StateUse value;
        public final Index bit;
        public final boolean definite;

        BitUse(StateUse val, Index b, boolean d) {
            value = val;
            bit = b;
            definite = d;
        }
    }
}
