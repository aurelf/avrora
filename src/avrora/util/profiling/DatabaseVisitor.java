/**
 * @author Ben L. Titzer
 **/
package avrora.util.profiling;


abstract public class DatabaseVisitor {
    abstract public void visit(Database d);

    abstract public void visit(DataItem i);
}
