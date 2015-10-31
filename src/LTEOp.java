//-------------------
//
//-------------------


class LTEOp extends ComparisonOp {
    public LTEOp(STO a, STO b, String s) {
        super(a,b,s);
    }

    public STO checkOperands(STO a, STO b) {
        typeA = a.getType();
        typeB = b.getType();
        boolean result;

        if(!(typeA instanceof NumericType)){
            return new ErrorSTO(a.getType().getName());
        }
        else if(!(typeB instanceof NumericType)) {
            return new ErrorSTO(b.getType().getName());
        }
        else if((typeA instanceof NumericType) && (typeB instanceof NumericType)){
            
            if(a instanceof ConstSTO && b instanceof ConstSTO) { 

                if(a.getType() instanceof IntType && b.getType() instanceof IntType)  { 
                    if (((ConstSTO)a).getIntValue() <= ((ConstSTO)b).getIntValue())
                        return new ConstSTO("true", new BoolType("bool"),1);
                    else
                        return new ConstSTO("false", new BoolType("bool"),0);
                }
                else if(a.getType() instanceof FloatType && b.getType() instanceof FloatType) {
                    if (((ConstSTO)a).getIntValue() <= ((ConstSTO)b).getIntValue())
                        return new ConstSTO("true", new BoolType("bool"),1);
                    else
                        return new ConstSTO("false", new BoolType("bool"),0);
 
                }
                else if(a.getType() instanceof FloatType && b.getType() instanceof IntType) {
                   if (((ConstSTO)a).getIntValue() <= ((ConstSTO)b).getIntValue())
                        return new ConstSTO("true", new BoolType("bool"),1);
                    else
                        return new ConstSTO("false", new BoolType("bool"),0);
                }
                else{
                    if (((ConstSTO)a).getIntValue() <= ((ConstSTO)b).getIntValue())
                        return new ConstSTO("true", new BoolType("bool"),1);
                    else
                        return new ConstSTO("false", new BoolType("bool"),0);
                }

            }

            return new ExprSTO(a.getName(), new BoolType("bool")); 
        }
            
        return new ErrorSTO(a.getName());

        }
}
