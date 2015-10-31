//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

import java.util.*;

class SymbolTable
{
	private Stack<Scope> m_stkScopes;
	private int m_nLevel;
	private Scope m_scopeGlobal;
	private FuncSTO m_func = null;
    private StructdefSTO m_struct = null;
    private Vector<STO> functions = new Vector<STO>();
    
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public SymbolTable()
	{
		m_nLevel = 0;
		m_stkScopes = new Stack<Scope>();
		m_scopeGlobal = null;
    
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void insert(STO sto)
	{
		Scope scope = m_stkScopes.peek();
		scope.InsertLocal(sto);
        	
    }

    //----------------------------------------------------------------
    // added Check for overloaded Functions, create a list for all functions with
    // the same name
    //---------------------------------------------------------------
    public Vector<STO> OverloadCheck(String funcName) {

        Vector<STO> overloaded = new Vector<STO>();
        for (int i =0; i < functions.size(); i++) {
            
            if (funcName.equals(functions.elementAt(i).getName())) {
                 if(((FuncSTO)functions.get(i)).isTag() == false) {
                  overloaded.add(functions.elementAt(i));
                }
            }
        }
        return overloaded;
    }

    public Vector<STO> OverloadCheckFun(String funcName) { // for dofuncall

        Vector<STO> overloaded = new Vector<STO>();
        for (int i =0; i < functions.size(); i++) {
            
            if (funcName.equals(functions.elementAt(i).getName())) {
                  overloaded.add(functions.elementAt(i));
            }
        }
        return overloaded;
    }

    public Vector<STO> OverloadCheckParam(String funcName) { // for do formal params

        Vector<STO> overloaded = new Vector<STO>();
        for (int i =0; i < functions.size(); i++) {
            
            if (funcName.equals(functions.elementAt(i).getName())) {
                if(functions.get(i).getOTag() == false) {
                  overloaded.add(functions.elementAt(i));
                }
            }
        }
        return overloaded;
    }

    public void TagOff() {
       for (int i =0; i < functions.size(); i++) {
          if(functions.get(i).getOTag() == true) {
             functions.get(i).setOTag(false);
          }
        }
    }



	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public STO accessGlobal(String strName)
	{
		return m_scopeGlobal.access(strName);
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public STO accessLocal(String strName)
	{
		Scope scope = m_stkScopes.peek();
		return scope.accessLocal(strName);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public STO access(String strName)
	{
		Stack<STO> stk = new Stack();
		Scope scope;
		STO stoReturn = null;	

		for (Enumeration<Scope> e = m_stkScopes.elements(); e.hasMoreElements();)
		{
			scope = e.nextElement();

			if ((stoReturn = scope.access(strName)) != null) {
				stk.push(stoReturn);
            }
		}

        if(stk.isEmpty()) {
          return null;
        }
        else {

		  return stk.pop();
        
        }

	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void openScope()
	{
		Scope scope = new Scope();

		// The first scope created will be the global scope.
		if (m_scopeGlobal == null)
			m_scopeGlobal = scope;

		m_stkScopes.push(scope);
		m_nLevel++;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void closeScope()
	{
		m_stkScopes.pop();
		m_nLevel--;
	}

    //----------------------------------------------------------------
    // added for struct
    //----------------------------------------------------------------
    public Scope getCurrScope()
    {
        return m_stkScopes.peek();
    }
    //----------------------------------------------------------------
    // added for struct
    //----------------------------------------------------------------
    public Scope getAboveScope(){
        //Scope tempScope = m_stkScopes.pop();
        Vector<Scope> temp  = new Vector<Scope>();
        for(int i = 0;i < m_stkScopes.size()-1; i++) {
            temp.addElement(m_stkScopes.pop());
        
        }
        Scope result = m_stkScopes.peek(); 
        for(int i = temp.size()-1; i >= 0; i--) {
            m_stkScopes.push(temp.get(i));
        }
        return result;
    }
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public int getLevel()
	{
		return m_nLevel;
	}


	//----------------------------------------------------------------
	//	This is the function currently being parsed.
	//---------------------------------------------------------------

    public void addFunc(STO f) {
        if( f instanceof FuncSTO){
            functions.add(f);
        }
    }

    public void removeFunc(STO f) {
       if(f instanceof FuncSTO) {
          functions.remove(f);
       }
    }

	public FuncSTO getFunc() { return m_func; }
	public void setFunc(FuncSTO sto) { m_func = sto; }

    // added for struct
    public StructdefSTO getStruct() { return m_struct;}
    public void setStruct(StructdefSTO sto) { m_struct = sto;}

}
