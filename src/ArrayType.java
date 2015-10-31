//-----------------------
//
//-----------------------
import java.util.Vector;

class ArrayType extends CompositeType{

    Type next;
    int dimension;
    public ArrayType(String strName, int size,int dim){
        super(strName, size);
        dimension  = dim;
    }

    public boolean isArray() { return true; }

    public boolean isEquivalent(Type t) {     
       if (t instanceof ArrayType) {
           if(this.getSize() == t.getSize()) {
            return (this.getNext()).isEquivalent(((ArrayType)t).getNext());
           }
           else {
            return false;
           }
       } 
       else {
           return false;
           
       }
    }
    public boolean isAssignable(Type t){
        if (t instanceof ArrayType) {
            return this.getNext().isAssignable(((ArrayType)t).getNext());
        }
        else {
            return false;
        }
    }

    public Type getNext() {
        return next;
    }

    public Type getBaseType() {
        if(dimension == 1) {
            return next;
        }
        else {
            return ((ArrayType)next).getBaseType();
        }
    }


    public int getTotalSize() {
        if (dimension ==1) {
            return (4 * this.getSize());
        }
        else {
            return this.getSize()*((ArrayType)next).getTotalSize();
        }
    }

    public void addNext(Type t) {
        if (next == null) {
            next = t;
        }
        else {
            ((ArrayType)next).addNext(t);
        }
    }
}
