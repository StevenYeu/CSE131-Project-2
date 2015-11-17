//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

import java.util.Vector;

class FuncSTO extends STO
{
	private Type m_returnType;
    private Vector<STO> params = new Vector<STO>();
    boolean isStruct = false;
    boolean tag = false;
    boolean isOver = false; // if func is Overloaded

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public FuncSTO(String strName)
	{
		super (strName);
		setReturnType(null);
		// You may want to change the isModifiable and isAddressable                      
		// fields as necessary
	}

    // need a constructor that sets type
    public FuncSTO(String strName, Type t)
    {
        super (strName, t);
        setReturnType(null);
    }

    public boolean isTag() {
       return tag;
    }

    public void setTag(boolean b) {
       tag = b;
    }

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isFunc() 
	{ 
		return true;
		// You may want to change the isModifiable and isAddressable                      
		// fields as necessary
	}

	//----------------------------------------------------------------
	// This is the return type of the function. This is different from 
	// the function's type (for function pointers - which we are not 
	// testing in this project).
	//----------------------------------------------------------------
	public void setReturnType(Type typ)
	{
		m_returnType = typ;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public Type getReturnType ()
	{
		return m_returnType;
	}

    public void addParam(STO a) {
        params.add(a);
    }

    public Vector<STO> getParams() {
        return params;
    }

    public void setParams(Vector<STO> p) {
       params = p;
    }

    public boolean getIsStruct() {
       return isStruct;
    }

    public void setIsStruct(boolean b) {
       isStruct = b;
    }

    public String getAssemblyName() {
        if(params.isEmpty()) {return "void";}
        String name = "";
        for(int i =0; i < params.size();i++) {
           name = name.concat(params.get(i).getType().getName());
           if(i == params.size()-1) {
              break;
           }
           name = name.concat(".");

        }
        return name;
    }
}
