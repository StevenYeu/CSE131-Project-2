//--------------------
//
//--------------------


class VoidType extends Type{
    public VoidType(String strName, int size){
        super(strName, size);
    }

    public boolean isVoid(){ return true; }

    public boolean isAssignable(Type t){ return false; }
    public boolean isEquivalent(Type t){ return false; }
}
