//----------------
//
//----------------


class PointerType extends CompositeType{
    Type next;
    int numPointers;
    public PointerType(String strNum){
        super(strNum, 4);
    }

    public PointerType(String strName,int num){
        super(strName,4);
        numPointers = num;
    }
    
    public int getSize(){
        return 4;
    }

    public void setNumPointers(int p) {
        numPointers = p;
    }
    
    public boolean isPointer() { return true; }

    public boolean isEquivalent(Type t) {
        if(t instanceof NullPointerType) {
           return true;
        }

        if( t instanceof PointerType ) {
            if(this.getNumPtr() == ((PointerType)t).getNumPtr()){
                return (this.getNext()).isEquivalent(((PointerType)t).getNext());
            }             
        }
        return false;
    }

    public boolean isAssignable(Type t){
       return this.isEquivalent(t);            
    }

    public Type getNext() {
        return next;
    }
    public Type getBaseType() {
        if( numPointers == 1 ) {
            return next;
        }
        else {
            return ((PointerType)next).getBaseType();
        }
    }

    public void setBaseType(Type t) {
         if( numPointers == 1 ) {
             next = t;
        }
        else {
            this.setBaseType(((PointerType)next).getBaseType());
        }

    }

    public void addNext(Type t) {
        if( next == null){
            next = t;
        }
        else {
            ((PointerType)next).addNext(t);
        }
    }

    public int getNumPtr() {
       return numPointers;
    }



}
