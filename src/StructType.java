//----------
//
//----------

import java.util.Vector;

class StructType extends CompositeType {
    Scope local;

    Vector<STO> functions = new Vector<STO>();

    public StructType(String strName){
        super(strName,0);
    }

    public boolean isStruct(){ return true; }
    public boolean isEquivalent(Type t){
        if(t instanceof StructType){
            if( t.getName().equals(this.getName())){
                return true;
            }

        }
        return false;
    }

    public boolean isAssignable(Type t){
        return this.isEquivalent(t);
    }

    public void setScope(Scope s) {
       local = s;
       functions = local.getLocals();
    }

    public Scope getScope() {
       return local;
    }

    public Vector<STO> OverloadCheckStruct(String funcName) { // for do formal params

        Vector<STO> overloaded = new Vector<STO>();
        for (int i =0; i < functions.size(); i++) {

            if(functions.get(i) instanceof FuncSTO){
            
                if (funcName.equals(functions.elementAt(i).getName())) {
                    if(functions.get(i).getOTag() == false) {
                        overloaded.add(functions.elementAt(i));
                    }
                }
            }
        }
        return overloaded;
    }

    public Vector<STO> OverloadCheckStructCall(String funcName) { // calls

        Vector<STO> overloaded = new Vector<STO>();
        for (int i =0; i < functions.size(); i++) {
            if(functions.get(i) instanceof FuncSTO){
                if (funcName.equals(functions.elementAt(i).getName())) {
                    overloaded.add(functions.elementAt(i));
                }
            }
        }
        return overloaded;
    }

    public void OffStructTag(){
        if(functions.isEmpty()){
            return;
        }

        for(int i = 0; i < functions.size(); i++){
            if(functions.get(i) instanceof FuncSTO){
                if(functions.get(i).getOTag())
                    functions.get(i).setOTag(false);
            }
        }
        
    }
 
}
