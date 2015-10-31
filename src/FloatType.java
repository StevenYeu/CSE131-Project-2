//---------------------
//
//
//---------------------


class FloatType extends NumericType {
    public FloatType(String strName){
        super(strName);
    }
    public boolean isFloat() { return true; }
    
    public boolean isAssignable(Type t) {
        if (t instanceof FloatType)
            return true;
        else
            return false;
    }

    public boolean isEquivalent(Type t) {
        if (t instanceof FloatType)
            return true;
        else 
            return false;
    }
}
