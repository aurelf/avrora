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

package avrora.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import avrora.actions.FindGadgetsAction.Feature;
import avrora.arch.legacy.LegacyInstr;
import cck.text.Terminal;

/**
 * The <code>GadgetsSet</code> used to manage gadgest of code ... // TODO
 * 
 * @author A. Francillon
 */

public class GadgetsSet implements Cloneable {
    TreeSet<Gadget> gadgets;
    //private int gadgetsAdded =0;
    
    
    public GadgetsSet() {
        gadgets = new TreeSet<Gadget>();
    }

    public Object clone() {
        GadgetsSet o = null;
        o = new GadgetsSet();
        o.gadgets.addAll(this.gadgets);
        return (Object) o;
    }

    public Gadget smallest() {
        // assuming proper ordering of the tree set
        return (Gadget) this.gadgets.first();
    }

    public Gadget smallestStackSize() {
        // assuming proper ordering of the tree set
        // return (Gadget)this.gadgets.first();
        TreeSet<Gadget> stacksizesorted = new TreeSet<Gadget>(
                new StackSizeComparator());
        stacksizesorted.addAll(gadgets);
        return stacksizesorted.first();
    }

    public void addGadget(Gadget elem) {
 
        //gadgetsAdded ++;
        gadgets.add(elem);
    }

    public void retainAll(GadgetsSet set) {
        gadgets.retainAll(set.gadgets);
    }

    public GadgetsSet intersection(GadgetsSet other) {
        GadgetsSet Set = new GadgetsSet();
        Iterator i = this.iterator();
        while (i.hasNext()) {
            Gadget ig = (Gadget) i.next();
            Iterator o = other.iterator();
            while (o.hasNext()) {
                Gadget og = (Gadget) o.next();
                if (ig.addr == og.addr) {
                    Set.addGadget(ig);
                    break;
                }
            }

        }
        return Set;
    }

    public GadgetsSet filterGadgets(ArrayList<Feature> features ){

        GadgetsSet gadgetsSet=new GadgetsSet();
        Iterator<Gadget> gadIterator =gadgets.iterator();
        while (gadIterator.hasNext()) {
            Gadget element = (Gadget) gadIterator.next();
            gadgetsSet.addGadget(element);           
        }
//        /gadgetsSet.print();
        Iterator<Feature> i=features.iterator();        
        while(i.hasNext()){
            Feature f=i.next();            
            //Terminal.println("filtering gadgets for instr="
            //            +f.instruction.getName()+" reg="+f.register1);
            //gadgetsSet.print(); 
            //Terminal.println("===============================");
            
            gadgetsSet=gadgetsWithInstrFullLength(gadgetsSet,f.instruction,f.register1);
        }
//        Terminal.println("===========returned stripped gadget set ============");
//        gadgetsSet.print();
        // auto strinpping gadgets
        return gadgetSetStrip(gadgetsSet, features);
        //return gadgetsSet;
    }    

    public GadgetsSet excludeGadgetsWithCall(){
        GadgetsSet gadgetsSet=new GadgetsSet();
        Iterator<Gadget> gadIterator =gadgets.iterator();
        while (gadIterator.hasNext()) {
            Gadget element = (Gadget) gadIterator.next();
            if(element==null){
//                Terminal.printRed("set is null");
                return gadgetsSet;
            }
           if(element.hasInstr(LegacyInstr.CALL.class, null)==null)
                gadgetsSet.addGadget(element);
        }
        return gadgetsSet;
    }
    
    public GadgetsSet gadgetsWithInstrFullLength(GadgetsSet gadgets,Class instr,Integer register){
        GadgetsSet result= new GadgetsSet();
        Iterator<Gadget> i= gadgets.iterator();        
        while(i.hasNext()){
            Gadget g=gadgets.nextGadget(i);
            if(g.hasInstr(instr,register)!=null){
                //Terminal.println("Fitering Keeping gadget :");
                //g.print();
                result.addGadget(g);               
            }else{
                //Terminal.println("Fitering Dropping gadget :");
                //g.print();
            }
                
        }
        return result; 
    }
    public GadgetsSet gadgetSetStrip(GadgetsSet gadgets,ArrayList<Feature> features){
        
        Iterator<Gadget> ig=gadgets.iterator();
        GadgetsSet cleanGadgetSet=new GadgetsSet();
        while(ig.hasNext()){
            Gadget aGadget=gadgets.nextGadget(ig);
            cleanGadgetSet.addGadget(aGadget.gadgetStrip(features));
        }   
        return cleanGadgetSet; 
    }

    
    public Iterator<Gadget> iterator() {
        return gadgets.iterator();
    }

    public Gadget nextGadget(Iterator<Gadget> i) {
        return i.next();
    }

    /**
     * returns the number of gadgets in the set 
     * @return the number of gadgets 
     */
    public Integer size() {
        return gadgets.size();
    }

    public void print() {
        Iterator<Gadget> i = gadgets.iterator();
        //Terminal.println(gadgetsAdded+"gadgets were added while "+gadgets.size() +"are reported ");
        while (i.hasNext()) {
            i.next().print();
        }
    }

    public boolean isEmpty() {
        return gadgets.isEmpty();
    }
}
