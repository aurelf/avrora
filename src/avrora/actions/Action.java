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

package avrora.actions;

import avrora.util.Option;
import avrora.util.Options;

/**
 * The <code>Action</code> class defines a new action that the main driver is
 * capable of executing. Each instance of <code>Action</code> is inserted into
 * a hash map in the main class, with the key being its name. For example,
 * the action to simulate a program is inserted into this hash map with the key
 * "simulate", and an instance of <code>avrora.actions.SimulateAction</code>.
 */
public abstract class Action {

    public final String help;
    public final String shortName;

    public final Options options;

    protected Action(String sn, String h) {
        shortName = sn;
        help = h;
        options = new Options();
    }

    /**
     * The <code>run()</code> method is called by the main class and is passed
     * the remaining command line arguments after options have been stripped out.
     *
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public abstract void run(String[] args) throws Exception;

    /**
     * The <code>getHelp()</code> method returns a string that is used in reporting
     * the command line help to the user.
     * @return an unformatted paragraph that contains the text explanation of what this
     * action does.
     */
    public String getHelp() {
        return help;
    }

    /**
     * The <code>getShortName()</code> method returns the name of this action
     * as a short string. This short string is the name that it can be referred
     * to as from the command line. For example, to execute the BenchmarkAction,
     * the -action=benchmark option must be supplied.
     * @return the name of this action as a short string
     */
    public String getShortName() {
        return shortName;
    }

    protected Option.Bool newOption(String name, boolean val, String desc) {
        return options.newOption(name, val, desc);
    }

    protected Option.Long newOption(String name, long val, String desc) {
        return options.newOption(name, val, desc);
    }

    protected Option.Interval newOption(String name, long l, long h, String desc) {
        return options.newOption(name, l, h, desc);
    }

    protected Option.Str newOption(String name, String val, String desc) {
        return options.newOption(name, val, desc);
    }

    protected Option.List newOptionList(String name, String val, String desc) {
        return options.newOptionList(name, val, desc);
    }
}
