//--------------------------
//
//
//--------------------------


class IntType extends NumericType {
    public IntType(String strName){
        super(strName);
    }

    public boolean isInt() { return true; }
    public boolean isAssignable(Type t){
        if (t instanceof IntType || t instanceof FloatType){
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isEquivalent(Type t){
        if (t instanceof IntType)
            return true;
        else
            return false;
    }
}
