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

package avrora;

/**
 * The <code>Version</code> class represents a version number, including the major version, the commit number,
 * as well as the date and time of the last commit.
 *
 * @author Ben L. Titzer
 */
public class Version {

    /**
     * The <code>prefix</code> field stores the string that the prefix of the version (if any) for this
     * version.
     */
    public final String prefix = "Beta ";

    /**
     * The <code>major</code> field stores the string that represents the major version number (the release
     * number).
     */
    public final String major = "1.6";

    /**
     * The <code>commit</code> field stores the commit number (i.e. the number of code revisions committed to
     * CVS since the last release).
     */
    public final int commit = 0;

    /**
     * The <code>getVersion()</code> method returns a reference to a <code>Version</code> object
     * that represents the version of the code base.
     * @return a <code>Version</code> object representing the current version
     */
    public static Version getVersion() {
        return new Version();
    }

    /**
     * The <code>toString()</code> method converts this version to a string.
     * @return a string representation of this version
     */
    public String toString() {
        return prefix + major + '.' + commit;
    }
}
