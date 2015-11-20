//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

//---------------------------------------------------------------------
// This is the top of the Type hierarchy. You most likely will need to
// create sub-classes (since this one is abstract) that handle specific
// types, such as IntType, FloatType, ArrayType, etc.
//---------------------------------------------------------------------

//---------------------------------------------------------------------
// 
//
//---------------------------------------------------------------------


abstract class Type
{
	// Name of the Type (e.g., int, bool, some structdef, etc.)
	private String m_typeName;
	private int m_size;
    private boolean isPointer = false;

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public Type(String strName, int size)
	{
		setName(strName);
		setSize(size);
	}

    public Type(String strName){
        setName(strName);
        setSize(4);
    }
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public String getName()
	{
		return m_typeName;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	private void setName(String str)
	{
		m_typeName = str;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public int getSize()
	{
		return m_size;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void setSize(int size)
	{
		m_size = size;
	}

    public boolean getIsPointer(){
       return isPointer;
    }
    public void setIsPointer(boolean b){
       isPointer = b;
    }

	//----------------------------------------------------------------
	//	It will be helpful to ask a Type what specific Type it is.
	//	The Java operator instanceof will do this, but you may
	//	also want to implement methods like isNumeric(), isInt(),
	//	etc. Below is an example of isInt(). Feel free to
	//	change this around.
	//----------------------------------------------------------------
	public boolean isError() {return false;}
	
    public boolean isBasic() {return false;}
    public boolean isComposite() {return false;}
    public boolean isVoid() {return false;}

    public boolean isAssignable(Type t) {return false;}
    public boolean isEquivalent(Type t) {return false;}
}
