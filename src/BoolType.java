//---------------------
//
//
//---------------------


class BoolType extends BasicType{
    
    public BoolType (String strName){
        super(strName);
    }

    public boolean isBool() { return true; }
    
    public boolean isAssignable(Type t) {
        if( t instanceof BoolType)
            return true;
        else 
            return false;
    }

    public boolean isEquivalent(Type t) {
        if( t instanceof BoolType)
            return true;
        else
            return false;
    }
}
