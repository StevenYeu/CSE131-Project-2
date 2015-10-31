//-----------------------------------
//
//
//-----------------------------------'

import java_cup.runtime.*;

class Operator {

    String op;
	public Operator(String s) {
        op = s;
	}

    public STO checkOperands(STO a, STO b){ return null; }
    public STO checkOperands(STO a){ return null; }

    public String getOp() {return op;}


} 
