//-------------------
//
//-------------------


class EqualOp extends ComparisonOp {
    public EqualOp(STO a, STO b, String s) {
        super(a,b,s);
    }

    public STO checkOperands(STO a, STO b) {
        typeA = a.getType();
        typeB = b.getType();


        if((typeA instanceof NumericType) && (typeB instanceof NumericType)){

             // constant folding
             if(a instanceof ConstSTO && b instanceof ConstSTO) { 

                if(a.getType() instanceof IntType && b.getType() instanceof IntType)  { 
                    if (((ConstSTO)a).getIntValue() == ((ConstSTO)b).getIntValue())
                        return new ConstSTO("true", new BoolType("bool"),1);
                    else
                        return new ConstSTO("false", new BoolType("bool"),0);
                }
                else if(a.getType() instanceof FloatType && b.getType() instanceof FloatType) {
                    if (((ConstSTO)a).getIntValue() == ((ConstSTO)b).getIntValue())
                        return new ConstSTO("true", new BoolType("bool"),1);
                    else
                        return new ConstSTO("false", new BoolType("bool"),0);
 
                }
                else if(a.getType() instanceof FloatType && b.getType() instanceof IntType) {
                   if (((ConstSTO)a).getIntValue() == ((ConstSTO)b).getIntValue())
                        return new ConstSTO("true", new BoolType("bool"),1);
                    else
                        return new ConstSTO("false", new BoolType("bool"),0);
                }
                else{
                    if (((ConstSTO)a).getIntValue() == ((ConstSTO)b).getIntValue())
                        return new ConstSTO("true", new BoolType("bool"),1);
                    else
                        return new ConstSTO("false", new BoolType("bool"),0);
                }

            }

            return new ExprSTO(a.getName(), new BoolType("bool")); 
        }
        else if ( (typeA.isEquivalent(new BoolType("bool"))) && (typeB.isEquivalent(new BoolType("bool")))) {

             if(a instanceof ConstSTO && b instanceof ConstSTO) { 

                if(((ConstSTO)a).getBoolValue() == ((ConstSTO)b).getBoolValue() ) {
                    return new ConstSTO("true", new BoolType("bool"),1);
                }
                else {
                    return new ConstSTO("false", new BoolType("bool"),0);
                }

            }

            return new ExprSTO(a.getName(), new BoolType("bool"));
        }

        else if ( (typeA.isEquivalent(typeB))  &&    typeB.isEquivalent(typeA))  {
             return new ExprSTO(typeA.getName(), new BoolType("bool"));
        }
        else if ( (typeA instanceof PointerType)  &&   (typeB instanceof NullPointerType) ) {
             return new ExprSTO(typeA.getName(), new BoolType("bool"));
        }
        else if ( (typeB instanceof PointerType)  &&   (typeA instanceof NullPointerType) ) {
             return new ExprSTO(typeA.getName(), new BoolType("bool"));
        }
        else {
            return new ErrorSTO("Error");
        } 
        
    }
}
