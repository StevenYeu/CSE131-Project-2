class NotOp extends UnaryOp {
    Type typeA;
    public NotOp(STO a) {
        super(a,"!");
    }

    public STO checkOperands(STO a) {
        typeA = a.getType();

        if(!(typeA.isEquivalent(new BoolType("bool")))) {
            return new ErrorSTO(typeA.getName());
        }
        else {
            if(a instanceof ConstSTO) {
                if (!((ConstSTO)a).getBoolValue()) {
                    return new ConstSTO("true",new BoolType("bool"), 1);
                }
                else {
                    return new ConstSTO("false",new BoolType("bool"), 0);
                }
            }


            return new ExprSTO("! "+ a.getName(), new BoolType("bool"));
        
        }
        
    }
}
