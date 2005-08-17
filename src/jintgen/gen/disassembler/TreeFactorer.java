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

package jintgen.gen.disassembler;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

/**
 * @author Ben L. Titzer
 */
public class TreeFactorer {

    final DecodingTree oldRoot;
    final HashMap<String, HashMap<DecodingTree, DecodingTree>> pathMap;

    public TreeFactorer(DecodingTree dt) {
        oldRoot = dt;
        pathMap = new HashMap<String, HashMap<DecodingTree, DecodingTree>>();
    }

    public DecodingTree getNewTree() {
        return rebuild("root:", oldRoot);
    }

    private DecodingTree rebuild(String prefix, DecodingTree dt) {
        String nprefix = prefix+"@"+"["+dt.left_bit+":"+dt.right_bit+"]";
        String vstr = prefix+dt.getLabel()+"["+dt.left_bit+":"+dt.right_bit+"]";

        // make a shallow copy of this node
        DecodingTree ndt = dt.shallowCopy();

        // rebuild each of the children
        for ( Map.Entry<Integer, DecodingTree> e : dt.children.entrySet() ) {
            int value = e.getKey();
            DecodingTree child = e.getValue();
            DecodingTree nchild = rebuild(nprefix, child);
            ndt.children.put(new Integer(value), nchild);
        }

        DecodingTree prev = getPrevTree(vstr, ndt);
        if ( prev != null ) return prev;

        // cache this node and (and its subgraph)
        addNewTree(vstr, ndt);
        return ndt;
    }

    private void addNewTree(String vstr, DecodingTree ndt) {
        HashMap<DecodingTree, DecodingTree> set = getTreeMap(vstr);
        set.put(ndt, ndt);
    }

    private HashMap<DecodingTree, DecodingTree> getTreeMap(String vstr) {
        HashMap<DecodingTree, DecodingTree> set = pathMap.get(vstr);
        if ( set == null ) {
            set = new HashMap<DecodingTree, DecodingTree>();
            pathMap.put(vstr, set);
        }
        return set;
    }

    private DecodingTree getPrevTree(String vstr, DecodingTree dt) {
        HashMap<DecodingTree, DecodingTree> set = getTreeMap(vstr);
        return set.get(dt);
    }
}
