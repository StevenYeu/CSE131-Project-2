class OrOp extends BooleanOp {
    public OrOp(STO a, STO b) {
        super(a,b,"||");
    }

    public STO checkOperands(STO a, STO b) {
        typeA = a.getType();
        typeB = b.getType();


        if(!(typeA.isEquivalent(new BoolType("bool")))) {
            return new ErrorSTO(typeA.getName());
        }
        else if (!(typeB.isEquivalent(new BoolType("bool")))) {
            return new ErrorSTO(typeB.getName());
        }
        else if ( (typeA.isEquivalent(new BoolType("bool"))) && (typeB.isEquivalent(new BoolType("bool")))) {

           if(a instanceof ConstSTO && b instanceof ConstSTO) { 

                if(((ConstSTO)a).getBoolValue() || ((ConstSTO)b).getBoolValue() ) {
                    return new ConstSTO("true", new BoolType("bool"),1);
                }
                else {
                    return new ConstSTO("false", new BoolType("bool"),0);
                }

            }
 
            return new ExprSTO("result", new BoolType("bool"));
        }
        return new ErrorSTO("Error");
        
    }
}
