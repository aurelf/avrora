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

package avrora.sim.mcu;

import avrora.util.ClassMap;

import java.util.HashMap;

/**
 * The <code>Microcontrollers</code> class represents a static, known mapping between names and
 * implementations of microcontroller models. For example, "atmega128l" is mapped to an instance of the
 * <code>ATMega128L</code> class.
 *
 * @author Ben L. Titzer
 */
public class Microcontrollers {

    private static final ClassMap mcus = new ClassMap("Microcontroller", MicrocontrollerFactory.class);

    static {
        mcus.addInstance("atmega128l", new ATMega128L(false));
        mcus.addInstance("atmega128l-103", new ATMega128L(true));
    }

    /**
     * The <code>getMicrocontroller</code> method retrieves an instance of the <code>Microcontroller</code>
     * interface that represents the named microcontroller.
     *
     * @param name the name of the microcontroller
     * @return an instance of the <code>Microcontroller</code> interface representing the hardware device if
     *         implemented; null otherwise
     */
    public static MicrocontrollerFactory getMicrocontroller(String name) {
        return (MicrocontrollerFactory)mcus.getObjectOfClass(name);
    }
}
