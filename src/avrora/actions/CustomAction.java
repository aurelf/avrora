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

import avrora.util.StringUtil;
import avrora.util.Option;
import avrora.Main;
import avrora.Avrora;

/**
 * @author Ben L. Titzer
 */
public class CustomAction extends Action {
    public static final String HELP = "The \"custom\" action allows a user to specify a Java class that " +
            "contains an action to run. This is useful for external actions that " +
            "are not part of the standard Avrora distribution. The \"class\" option " +
            "specifies which Java class to load, instantiate and run. This class " +
            "must extend the avrora.Main.Action class within Avrora.";
    public final Option.Str CLASS = newOption("class", "",
            "This option is only used in the \"custom\" action to specify which Java " +
            "class contains an action to load and execute.");

    public CustomAction() {
        super("custom", HELP);
    }

    public void run(String[] args) throws Exception {
        String clname = CLASS.get();
        if (clname.equals(""))
            Avrora.userError("Custom action class must be specified in -class option");
        try {
            Class cl = Class.forName(clname);
            Action a = (Action) cl.newInstance();
            a.run(args);
        } catch (ClassNotFoundException e) {
            Avrora.userError("Could not find custom action class", StringUtil.quote(clname));
        } catch (ClassCastException e) {
            Avrora.userError("Specified class does not extend avrora.Main.Action", StringUtil.quote(clname));
        }
    }
}
