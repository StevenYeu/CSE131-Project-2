//----------------
//
//----------------


class MulOp extends ArithmetricOp {

    public MulOp(STO a,STO b, String s) {
        super(a,b,s);
    }

    public STO checkOperands(STO a, STO b) {
        typeA = a.getType();
        typeB = b.getType();

        if (!(typeA instanceof NumericType)){
            return new ErrorSTO(a.getType().getName());
        }
        else if(!(typeB instanceof NumericType)) {
            return new ErrorSTO(b.getType().getName());
        }
        else if((typeA instanceof IntType) && (typeB instanceof IntType)){
            if(a instanceof ConstSTO && b instanceof ConstSTO) {
                int result = ((ConstSTO)a).getIntValue() * ((ConstSTO)b).getIntValue();
                return new ConstSTO(Integer.toString(result), new IntType("int"),result);

            }
            return new ExprSTO(a.getName(), new IntType("int")); 
        }
        else {
            if(a instanceof ConstSTO && b instanceof ConstSTO) {
                float result = ((ConstSTO)a).getFloatValue() * ((ConstSTO)b).getFloatValue();

                return new ConstSTO(Float.toString(result), new FloatType("float"),result);

            }
            return new ExprSTO(a.getName(), new FloatType("float"));
        }
    }
 
}
