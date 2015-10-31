//---------------------------
//
//
//---------------------------


class BinaryOp extends Operator {
	STO op1;
	STO op2;	

	public BinaryOp(STO a, STO b, String s){
		super(s);
        op1 = a;
		op2 = b;
	}

}	
