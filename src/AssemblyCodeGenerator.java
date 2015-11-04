 import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * An Assembly Code Generator example emphasizing well thought design and
 * the use of java 1.5 constructs.
 * 
 * Disclaimer : This is not code meant for you to use directly but rather
 * an example from which I hope you can learn useful constructs and
 * conventions.
 * 
 * Topics of importance (Corrosponding to the inline comment numbers below)
 * 
 * 1) We will use this variable to denote the current level of indentation.
 *    For assembly there is usually only one or two levels of nesting.
 * 
 * 2) A collection of static final error messages.  This isn't so important in
 *    your project but is useful.  The static keyword means we only make 3 
 *    strings, shared across potentially multiple AssemblyCodeGenerator(s).
 *    It is Java convention to spell constant variables with upper casing.
 *    
 * 3) FileWriter is a basic IO class which can write basic types such as
 *    Strings to a file.  For performance you may want to look into the
 *    BufferedWriter class.
 *    
 * 4) This is a template for our file header.  It is very basic consisting only
 *    of a time stamp.
 *    
 * 5) This is the string we will use as an indentation seperator.  We are 
 *    encapsulating this seperator into one variable so we only need to change
 *    the initialization if we want to change our spacing to say 4 spaces for
 *    example.  Imagine if you simply used the literal "\t" in 500 places and
 *    then you decide you want to change it to 4 spaces!  Aside from regular 
 *    expressions you have a lot of work ahead of you.
 *    
 * 6) These are constant String templates that will be used for code
 *    generation.  It is nice to isolate these in one place or even in another
 *    file so that we can quickly make universal changes if needed.  You will
 *    notice that we could generate an entirely different language by simply
 *    changing the construct definitions.  I recommend defining all operations
 *    as well as formats.  Operations are things like add, mul, set, etc.
 *    Formats are like {OPERATION} {REG_1}, {REG_2}, {REG_3} etc.
 *    
 * 7) Here we are making a call to writeAssembly to write our header with the
 *    current time.  writeAssembly explained later.
 *    
 * 8) These methods are used to increase or decrease our current indentation
 *    level.  You might ask why make a method for a simple inc/dec?  We are
 *    encapsulating the notion of adjusting indentation.  It just so happens
 *    that this current implementation is just a variable increment or 
 *    decrement, but who is to say that the operation won't be more advanced
 *    in the future.  Maybe we want to log a message everytime we increment
 *    or decrement indentation.  We wouldn't want to add the logging code
 *    everywhere we were incrementing the variable (if we didn't have the
 *    methods).
 *    
 * 9) This signature may look foreign to you.  What is says is that we have 
 *    public method named writeAssembly which takes as parameters a String
 *    followed by 1 or more strings.  This construct is called "VarArgs" and
 *    is a Java 1.5 feature.  This allows you to write one method which can
 *    be applied to any number of parameters.  This method simply takes in 
 *    a template and all the strings that will be substituted into the 
 *    template.  When you are actually in the method, the parameter 
 *    String ... params will be an array of strings.
 *    
 * 10) This is where we use our indent_level.  We will indent indent_level levels
 *     of indentation.  That is an awkward sentence isn't it!  StringBuilder is
 *     an efficient class to build strings from concatentations.  If your 
 *     concatenations span multiple lines of code, using a StringBuilder can
 *     offer signifigant performance when compared to using the + operator.
 *     This topic can get fairly detailed, send me an email or come talk to me
 *     in the lab for more details.
 * 
 * 11) Here we are writing the message to file, notice we are using the 
 *     String.format method which takes a printf like format string followed
 *     by an array of Objects which are the parameters to the format string.
 *     
 * 12) Main is just a small demo that will create a tiny assembly file in the
 *     current directory called "rc.s".  This file doesn't compile and is
 *     not meant to.
 * 
 * @author Evan Worley
 */

public class AssemblyCodeGenerator {
    // 1
    private int indent_level = 0;


    //counters for cout
    private int constStrCnt = 0;
    private int varStrCnt = 0;
    private int constFloatCnt = 0;

    // 2
    private static final String ERROR_IO_CLOSE = 
        "Unable to close fileWriter";
    private static final String ERROR_IO_CONSTRUCT = 
        "Unable to construct FileWriter for file %s";
    private static final String ERROR_IO_WRITE = 
        "Unable to write to fileWriter";

    // 3
    private FileWriter fileWriter;
    
    // 4
    private static final String FILE_HEADER = 
        "/*\n" +
        " * Generated %s\n" + 
        " */\n\n";
        
    // 5
    private static final String SEPARATOR = "\t";
    private static final String DOLLAR = ".$$.";
    private static final String AZ = ".asciz";
    
    // Operations name decl
    private static final String SET_OP = "set";
    private static final String SAVE_OP = "save";
    private static final String CALL_OP = "call";
    private static final String NOP_OP = "nop";
    private static final String RET_OP = "ret";
    private static final String RESTORE_OP = "restore";
    private static final String CMP_OP = "cmp";
    private static final String BE_OP = "be";
    private static final String ADD_OP = "add";
    private static final String PRINT_OP = "printf";
    private static final String LOAD_OP = "ld";
    




    
    // Global Var decl
    private static final String SECTION = ".section";
    private static final String ALIGN = ".align";
    private static final String GLOBAL = ".global";
    private static final String SKIP = ".skip";
    private static final String LABEL = "%S:";

    // Func Decl
    //private static final String  
    //private static





    //private static final String AssemblyFile = "%s";
    //private static final String CALL_OP = "call


    // Operation param decl
    private static final String THREE_PARAM = "%s" + SEPARATOR + "%s, %s, %s\n";
    private static final String TWO_PARAM = "%s" + SEPARATOR + "%s, %s\n";
    private static final String ONE_PARAM = "%s" + SEPARATOR + "%s\n";
    private static final String NO_PARAM = "%s" + SEPARATOR + "\n";
    private static final String NEWLINE = "\n";

    public AssemblyCodeGenerator(String fileToWrite) {
        try {
            fileWriter = new FileWriter(fileToWrite);
            
            // 7
            writeAssembly(FILE_HEADER, (new Date()).toString());
        } catch (IOException e) {
            System.err.printf(ERROR_IO_CONSTRUCT, fileToWrite);
            e.printStackTrace();
            System.exit(1);
        }
    }
    

    // 8
    public void decreaseIndent() {
        indent_level--;
    }
    
    public void dispose() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            System.err.println(ERROR_IO_CLOSE);
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void increaseIndent() {
        indent_level++;
    }
    

    
    // 9
    public void writeAssembly(String template, String ... params) {
        StringBuilder asStmt = new StringBuilder();
        
        // 10
        for (int i=0; i < indent_level; i++) {
            asStmt.append(SEPARATOR);
        }
        
        // 11
        asStmt.append(String.format(template, (Object[])params));
        
        try {
            fileWriter.write(asStmt.toString());
        } catch (IOException e) {
            System.err.println(ERROR_IO_WRITE);
            e.printStackTrace();
        }
    }
    
    // 12
    public static void main(String args[]) {
        AssemblyCodeGenerator myAsWriter = new AssemblyCodeGenerator("rc.s");

        myAsWriter.increaseIndent();
        myAsWriter.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(4095), "%l0");
        myAsWriter.increaseIndent();
        myAsWriter.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(1024), "%l1");
        myAsWriter.decreaseIndent();
        
        myAsWriter.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(512), "%l2");
        
        myAsWriter.decreaseIndent();
        myAsWriter.dispose();
    }



    public void formatHeader(){

        // .section ".rodata"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".rodata\"");
        this.decreaseIndent();

        // .$$.intFmt:
        this.writeAssembly(NO_PARAM, DOLLAR + "intFmt:");
        
        // .asciz "%d"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, AZ, "\"%d\"");
        this.decreaseIndent();

        // .$$.strFmt:
        this.writeAssembly(NO_PARAM, DOLLAR + "strFmt:");
        
        // .asciz "%s"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, AZ, "\"%s\"");
        this.decreaseIndent();

        // .$$.strTF:
        this.writeAssembly(NO_PARAM, DOLLAR + "strTF:");
        
        
        // .asciz "false\0\0\0true"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, AZ, "\"false\\0\\0\\0true\"");
        this.decreaseIndent();

        // .$$.endl:
        this.writeAssembly(NO_PARAM, DOLLAR + "strEndl:");
        
        // .asciz "\n"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, AZ, "\"\\n\"");
        this.decreaseIndent();

        // .$$.strArrBound:
        this.writeAssembly(NO_PARAM, DOLLAR + "strArrBound:");
        
        // .asciz "Index error msg"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, AZ, "\"INdex value of %d is outside legel range [0,%d].\\n\"");
        this.decreaseIndent();

        // .$$.strNullPtr:
        this.writeAssembly(NO_PARAM, DOLLAR + "strNullPtr:");
        
        // .asciz "Nullpointer error msg"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, AZ, "\"Attempt to deference NULL pointer.\\n\"");
        this.decreaseIndent();


        this.writeAssembly(NEWLINE);    
        

    }

    // This is for global/static uninit vars decl
    public void DoGlobalVarDecl(STO sto){
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".bss\"");
        this.decreaseIndent();
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(sto.getType().getSize()));
        this.decreaseIndent();
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, GLOBAL, sto.getName());
        this.decreaseIndent();
        this.writeAssembly(NO_PARAM,sto.getName()+":");
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SKIP, String.valueOf(sto.getType().getSize())); 
        this.writeAssembly(NEWLINE);
        this.decreaseIndent();
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
        this.decreaseIndent();
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(sto.getType().getSize()));
        this.decreaseIndent();
    
    
    }

    public void DoLocalVarDecl(STO sto, String reg){
    }

    // func decl with no params
    public void DoFuncStart(STO sto, String reg){


        String SAVE = "SAVE." + sto.getName() + "."+ ((FuncSTO)sto).getReturnType().getName();


        // .section   .text
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
        this.decreaseIndent();

        // .align   size
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

        // .global  func_name
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, GLOBAL, sto.getName());
        this.decreaseIndent();

        // label:
        // label.returntype:
        this.writeAssembly(NO_PARAM, sto.getName()+":");
        this.writeAssembly(NO_PARAM, sto.getName()+"."+((FuncSTO)sto).getReturnType().getName() +":");
        
        // set   SAVE.funcname.type, %g1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, SAVE , reg);
        this.decreaseIndent();

        // save   %sp, %g1, %sp
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, SAVE_OP, "%sp", reg, "%sp");
        this.decreaseIndent();

        // ! comment
        this.writeAssembly(NEWLINE);
        this.increaseIndent();
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! Store params");
        this.decreaseIndent();
        this.decreaseIndent();
        this.writeAssembly(NEWLINE);
        
    }
    
    public void DoFuncEnd(STO sto){
        String SAVE = "SAVE." + sto.getName() + "."+ ((FuncSTO)sto).getReturnType().getName();

        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! End of function " + SAVE);
        this.decreaseIndent();

        // call    funcname.type.fini
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM,  CALL_OP, sto.getName() + "." + ((FuncSTO)sto).getReturnType().getName() + ".fini"); 
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        //ret
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RET_OP);
        this.decreaseIndent();

        //restore
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RESTORE_OP);
        this.decreaseIndent();

        this.increaseIndent();
        this.writeAssembly(NO_PARAM, SAVE + "= -(" +sto.getAddress() + ") & -8");
        this.decreaseIndent();

        //funcname.type.fini
        
        this.writeAssembly(NO_PARAM, sto.getName() + "." + ((FuncSTO)sto).getReturnType().getName() + ".fini:");
        
        //save   %sp, -96, %sp
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, SAVE_OP, "%sp", "-96", "%sp");
        this.decreaseIndent();

        //ret
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RET_OP);
        this.decreaseIndent();

        //restore
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RESTORE_OP);
        this.decreaseIndent();

        
    }

    public void FuncHeader(STO sto){
 
        // .$$.printBool:
        this.writeAssembly(NO_PARAM, DOLLAR + "printBool:");
        
        // save %sp, -96, %sp
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, SAVE_OP, "%sp", "-96", "%sp");
        this.decreaseIndent();

        // set .$$.strTF, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, ".$$.strTF", "%o0");
        this.decreaseIndent();

        // cmp  %g0, %i0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP,"%g0", "%i0");
        this.decreaseIndent();

        // be  .$$.printBool2
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, DOLLAR+"printBool2");
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        // add   %o0, 8, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP,"%o0", "8", "%o0" );
        this.decreaseIndent();

        // .$$.printBool2
        this.writeAssembly(NO_PARAM, SECTION, DOLLAR + "printBool2");

        // call printf
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, PRINT_OP);
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        // ret
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RET_OP);
        this.decreaseIndent();

        // restore
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RESTORE_OP);
        this.decreaseIndent();




    }

    public void printConst(STO sto){
    
        Type typ = sto.getType();

        if(sto instanceof ExprSTO){
            if(sto.getName().equals("endl")){
                this.printNL();
                return;
            }
            //return;
        }
        
        if(!(typ instanceof BasicType)){
        
            //.section ".rodata" 
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, SECTION, "\".rodata\"");
            this.decreaseIndent();

            // .align 4
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, ALIGN, "4");
            this.decreaseIndent();

            // .$$.str.#:
            constStrCnt++;
            this.writeAssembly(NO_PARAM, DOLLAR + "str." + Integer.toString(constStrCnt) + ":");

            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, AZ, "\"" + sto.getName() + "\"" );
            this.decreaseIndent();
            // end of .rodata
        
            this.writeAssembly(NEWLINE);

            // .section ".text"
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, SECTION, "\".text\"" );
            this.decreaseIndent();

            // .align 4
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, ALIGN, "4" );
            this.decreaseIndent();

            // !comment
            this.increaseIndent();
            this.writeAssembly(NO_PARAM, "! cout << \"" +sto.getName() +"\"" );
            this.decreaseIndent();

            //set .$$.strFmt, %o0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR + "strFmt", "%o0" );
            this.decreaseIndent();

            //set .$$.str.#, %o1
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR + "str." + Integer.toString(constStrCnt), "%o1" );
            this.decreaseIndent();

            //call printf
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, CALL_OP, PRINT_OP);
            this.decreaseIndent();
            
            //nop
            this.increaseIndent();
            this.writeAssembly(NO_PARAM, NOP_OP);
            this.decreaseIndent();


        }
        else if(typ instanceof FloatType){
          
            this.writeAssembly(NEWLINE);

            //.section ".rodata" 
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, SECTION, "\".rodata\"");
            this.decreaseIndent();

            // .align 4
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, ALIGN, "4");
            this.decreaseIndent();

            // .$$.float.1
            constFloatCnt++;
            this.writeAssembly(NO_PARAM, DOLLAR + "float." + Integer.toString(constFloatCnt) + ":");

            // .single 0r#
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, ".single", "0r" + String.valueOf(((ConstSTO)sto).getFloatValue()) );
            this.decreaseIndent();

            // .section ".text"
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, SECTION, "\".text\"" );
            this.decreaseIndent();

            // .align 4
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, ALIGN, "4" );
            this.decreaseIndent();

            // !comment
            this.increaseIndent();
            this.writeAssembly(NO_PARAM, "! cout << \"" +sto.getName() +"\"" );
            this.decreaseIndent();

            //set .$$.Float.#, %o0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR + "float." + Integer.toString(constFloatCnt) , "%l7" );
            this.decreaseIndent();

            //ld [%l7], %f0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%f0");
            this.decreaseIndent();

            //call printFloat
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, CALL_OP, "printFloat");
            this.decreaseIndent();
            
            //nop
            this.increaseIndent();
            this.writeAssembly(NO_PARAM, NOP_OP);
            this.decreaseIndent();

        }
        else if(typ instanceof BoolType){
        }
        else{

        }


    
    }

    public void printNL(){

            // !comment
            this.increaseIndent();
            this.writeAssembly(NO_PARAM, "! cout << \" endl \"" );
            this.decreaseIndent();

            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR+"strEndl", "%o0");
            this.decreaseIndent();
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, CALL_OP, PRINT_OP );
            this.decreaseIndent();
            this.increaseIndent();
            this.writeAssembly(NO_PARAM, NOP_OP);
            this.decreaseIndent();
    }

    


}

