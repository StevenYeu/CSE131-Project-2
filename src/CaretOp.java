class CaretOp extends BitwiseOp {
    public CaretOp(STO a, STO b) {
        super(a,b,"^");
    }

    public STO checkOperands(STO a, STO b) {
        typeA = a.getType();
        typeB = b.getType();


        if(!(typeA.isEquivalent(new IntType("int")))) {
            return new ErrorSTO(typeA.getName());
        }
        else if (!(typeB.isEquivalent(new IntType("int")))) {
            return new ErrorSTO(typeB.getName());
        }
        else if ( (typeA.isEquivalent(new IntType("int"))) && (typeB.isEquivalent(new IntType("int")))) {
           
            if(a instanceof ConstSTO && b instanceof ConstSTO) {
                int result = ((ConstSTO)a).getIntValue() ^ ((ConstSTO)b).getIntValue();
                return new ConstSTO(Integer.toString(result), new IntType("int"),result);

            }
            return new ExprSTO(a.getName()+"^"+b.getName(), new IntType("int")); 
 
        }
        return new ErrorSTO("Error");
        
    }
}
