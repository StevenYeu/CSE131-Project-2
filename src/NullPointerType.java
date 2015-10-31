//---------
//
//---------

class NullPointerType extends PointerType {
    public NullPointerType( String strNum){
        super(strNum, 0);
    }

    public boolean isNullPointer() { return true; }
    public boolean isEquivalent(Type t) {
        if(t instanceof PointerType)
            return true;
        return false;
    }
    public boolean isAssignable(Type t) {
        return this.isEquivalent(t);
    }
}

