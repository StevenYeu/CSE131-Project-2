//----------------
//
//----------------


class CompositeType extends Type{
    public CompositeType(String strName, int size){
        super(strName, size);
    }

    public boolean isComposite() { return true; }
    
}
