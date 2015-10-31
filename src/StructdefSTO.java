//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

//---------------------------------------------------------------------
// For structdefs
//---------------------------------------------------------------------
import java.util.Vector;
class StructdefSTO extends STO
{
    Vector<STO> vars = new Vector<STO>();
    Vector<STO> funcs = new Vector<STO>();
    Vector<STO> overload = new Vector<STO>();
    Vector<STO> ctors = new Vector<STO>();
    Scope structScope;

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public StructdefSTO(String strName)
	{
		super(strName);
	}

	public StructdefSTO(String strName, Type typ)
	{
		super(strName, typ);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isStructdef()
	{
		return true;
	}

    public void setVars(Vector<STO> v) {
        vars = v;
    }

    public void setFuncs(Vector<STO> f) {
        funcs = f;
    }

    public Vector<STO> getVars() {
        return vars;
    }
    public Vector<STO> getFuncs() {
        return funcs;
    }

    public Vector<STO> getCtors() {
        for (int i =0; i < funcs.size(); i++) {   
        }
       return ctors;
    }

    public Vector<STO> OverloadCheck(String funcName) {

        Vector<STO> overload = new Vector<STO>();
        for (int i =0; i < funcs.size(); i++) {
            
            if (funcName.equals(funcs.elementAt(i).getName())) {
                overload.add(funcs.elementAt(i));
            }
        }
        return overload;
    }

    //public boolean isCtor(String c) {
       //int caps = 0;
       //for(int i =0; i < c.length(); i++) {
         // if(c.charAt(i).isUpper)   
      // }
    //}

}
