//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

import java.util.Vector;

abstract class STO
{
	private String m_strName;
	private Type m_type;
	private boolean m_isAddressable;
	private boolean m_isModifiable;
    public boolean flag = false; // check if parameter is by reference  or value
    private boolean thisTag = false;
    private boolean oTag = false;
    private boolean structTag = false;
    private boolean arrayTag = false;
    private int structOffset = 0;
    private String offset;
    private String base;
    private int value;
    private String structName;
    private String AssemblyName;
    private boolean isPointer = false;
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public STO(String strName)
	{
		this(strName, null);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public STO(String strName, Type typ)
	{
		setName(strName);
		setType(typ);
		setIsAddressable(false);
		setIsModifiable(false);
	}

    //----------------------------------------------------------------
    //
    //----------------------------------------------------------------
    public void setOffset(String o){
         offset = o;
    }

    public String getBase(){
        return base;
    }

    public void setBase(String b){
        base = b;
    }

    public String getOffset(){
        return offset;
    }

    
    public String getAddress(){
        return base.concat(offset);    
    }

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public String getName()
	{
		return m_strName;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void setName(String str)
	{
		m_strName = str;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public Type getType()
	{
		return m_type;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	private void setType(Type type)
	{
		m_type = type;
	}

	//----------------------------------------------------------------
	// Addressable refers to if the object has an address. Variables
	// and declared constants have an address, whereas results from 
	// expression like (x + y) and literal constants like 77 do not 
	// have an address.
	//----------------------------------------------------------------
	public boolean getIsAddressable()
	{
		return m_isAddressable;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void setIsAddressable(boolean addressable)
	{
		m_isAddressable = addressable;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean getIsModifiable()
	{
		return m_isModifiable;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void setIsModifiable(boolean modifiable)
	{
		m_isModifiable = modifiable;
	}

	//----------------------------------------------------------------
	// A modifiable L-value is an object that is both addressable and
	// modifiable. Objects like constants are not modifiable, so they 
	// are not modifiable L-values.
	//----------------------------------------------------------------
	public boolean isModLValue()
	{
		return getIsModifiable() && getIsAddressable();
	}

    public boolean isThis() {
       return thisTag;
    }

    public void setIsThis(boolean b) {
      thisTag = b;
    }

    public boolean getOTag() {
       return thisTag;
    }

    public void setOTag(boolean b) {
      thisTag = b;
    }

    public int getVal(){
        return value;
    }

    public void setVal(int i){
        value = i;
    }

    public void setStructTag(boolean b) {
        structTag = b;
    }

    public boolean getStructTag(){
        return structTag;
    }

    public void setArrayTag(boolean b){
        arrayTag = b;
    }

    public boolean getArrayTag(){
        return arrayTag;
    }

    public void setStructOffset(int i){
        structOffset = i;
    }

    public int getStructOffset(){
        return structOffset;
    }

    public void setStructName(String s){
        structName = s;
    }

    public String getStructName(){
        return structName;
    }

    public void setAssemblyName(String s){
        AssemblyName = s;
    }

    public String getAssemblyName(){
       return AssemblyName;
    }

    public boolean getIsPointer() {
       return isPointer;
    }

    public void setIsPointer(boolean b) {
       isPointer = b;
    }
	//----------------------------------------------------------------
	//	It will be helpful to ask a STO what specific STO it is.
	//	The Java operator instanceof will do this, but these methods 
	//	will allow more flexibility (ErrorSTO is an example of the
	//	flexibility needed).
	//----------------------------------------------------------------
	public boolean isVar() { return false; }
	public boolean isConst() { return false; }
	public boolean isExpr() { return false; }
	public boolean isFunc() { return false; }
	public boolean isStructdef() { return false; }
	public boolean isError() { return false; }

    public Vector<STO> getParams() { return null; } 
}
