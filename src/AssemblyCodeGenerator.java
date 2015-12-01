import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import java.util.Stack;
import java.util.LinkedList;

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
 */

public class AssemblyCodeGenerator {
    // 1
    private int indent_level = 0;
    


    //counters for cout
    private int constStrCnt = 0;
    private int varStrCnt = 0;
    private int constFloatCnt = 0;

    //counters for if stmt
    private int cmpCnt = 0;
    private int endIfCnt = 0;

    private int andorCnt = 0;

    private int loopCnt = 0;

    // counter for CtorDtor
    private int ctordtor = 0;


    // while loop branch label
    private Stack<Integer> wlabel = new Stack<Integer>();

    //branch label
    private Stack<Integer> blabel = new Stack<Integer>();

    // DtorFuncs
    private Stack<STO> Dtors = new Stack<STO>();

    private Stack<STO> globalsDtors = new Stack<STO>();
    private Stack<Integer> numDtors = new Stack<Integer>();
    private Stack<Integer> numGlobalsDtors = new Stack<Integer>();

    private boolean holdOff = false;
    private boolean dtorFlag = false;

    private Stack<Integer> andor = new Stack<Integer>();


    // This is the hold off buffer that handles premature printing
    StringBuilder bufferStmt = new StringBuilder();
    

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
    // cmp for int
    private static final String CMP_OP = "cmp";
    // cmp for float
    private static final String FCMP_OP = "fcmps";

    private static final String FITOS_OP = "fitos";
    private static final String FSTOI_OP = "fstoi";

    
    // Operations arithmetric
    // +/- UNarySign
    private static final String ADD_OP = "add";
    private static final String SUB_OP = "sub";
    private static final String NEG_OP = "neg";
    // for float 
    private static final String FADD_OP = "fadds";
    private static final String FSUB_OP = "fsubs";
    private static final String FNEG_OP = "fnegs";
   
    // *, /, %
    private static final String MUL_OP = ".mul";
    private static final String DIV_OP = ".div";
    private static final String MOD_OP = ".rem";
    // for float
    private static final String FMUL_OP = "fmuls";
    private static final String FDIV_OP = "fdivs";

    


    //bitwise
    private static final String AND_OP = "and";
    private static final String OR_OP = "or";
    private static final String XOR_OP = "xor";
    

    private static final String MOV_OP = "mov";
    private static final String PRINT_OP = "printf";
    private static final String LOAD_OP = "ld";
    private static final String EXIT_OP = "exit";
    private static final String STORE_OP = "st";
    // for float
    private static final String FMOV_OP = "fmovs";

    
    // cmp for if statement
    private static final String BA_OP = "ba";
    private static final String BLE_OP = "ble";
    private static final String BE_OP = "be";
    private static final String BNE_OP = "bne";
    private static final String BL_OP = "bl";
    private static final String BG_OP = "bg";
    private static final String BGE_OP = "bge";



    private static final String INC_OP = "inc";
    private static final String DEC_OP = "dec";
    // for float
    private static final String FBLE_OP = "fble";
    private static final String FBE_OP = "fbe";
    private static final String FBNE_OP = "fbne";
    private static final String FBL_OP = "fbl";
    private static final String FBG_OP = "fbg";
    private static final String FBGE_OP = "fbge";

    

    
    // Var decl
    private static final String SECTION = ".section";
    private static final String ALIGN = ".align";
    private static final String GLOBAL = ".global";
    private static final String SKIP = ".skip";
    private static final String LABEL = "%s:"; // lol we never use this
    private static final String WORD = ".word";
    private static final String SINGLE = ".single";
    private static final String INIT = ".$.init.";
   

    // regs
      //local reg
    private static final String l0 = "%l0";
    private static final String l1 = "%l1";
    private static final String l2 = "%l2";
    private static final String l3 = "%l3";
    private static final String l4 = "%l4";
    private static final String l5 = "%l5";
    private static final String l6 = "%l6";
    private static final String l7 = "%l7";
      // output reg
    private static final String o0 = "%o0";
    private static final String o1 = "%o1";
    private static final String o2 = "%o2";
    private static final String o3 = "%o3";
    private static final String o4 = "%o4";
    private static final String o5 = "%o5";
    
     // input reg
    private static final String i0 = "%i0";
    private static final String i1 = "%i1";
    private static final String i2 = "%i2";
    private static final String i3 = "%i3";
    private static final String i4 = "%i4";
    private static final String i5 = "%i5";
    
     // global reg
    private static final String g0 = "%g0";
    
     // float reg
    private static final String f0 = "%f0";
    private static final String f1 = "%f1";




    //private static final String AssemblyFile = "%s";


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
    public void setholdOff(boolean b){
        holdOff = b;
    }

    public boolean getholdOff(){
        return holdOff;
    }


    public void writeAssembly(String template, String ... params) {
        
        if( ! holdOff ){
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
        // hold off
        else{
            for (int i=0; i < indent_level; i++) {
                bufferStmt.append(SEPARATOR);
            }
            bufferStmt.append(String.format(template, (Object[])params));
        }
        
    }


    // This allows use to print the holdoff
    public void TimeToWrite(){

        try {
            fileWriter.write(bufferStmt.toString());
        } catch (IOException e) {
            System.err.println(ERROR_IO_WRITE);
            e.printStackTrace();
        }
        bufferStmt = new StringBuilder();  
    }
    
    // 12 don't actually used for anything
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


    // ----------------------------------------------------------------------------------
    // File header, contains helpers for cout 
    // ----------------------------------------------------------------------------------


    public void formatHeader(){

        // .section ".rodata"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".rodata\"");
        this.decreaseIndent();

        // .align 4
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
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
        this.writeAssembly(ONE_PARAM, AZ, "\"Index value of %d is outside legal range [0,%d).\\n\"");
        this.decreaseIndent();

        // .$$.strNullPtr:
        this.writeAssembly(NO_PARAM, DOLLAR + "strNullPtr:");
        
        // .asciz "Nullpointer error msg"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, AZ, "\"Attempt to dereference NULL pointer.\\n\"");
        this.decreaseIndent();


        this.writeAssembly(NEWLINE);    
        // .section   .text
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
        this.decreaseIndent();

        // .align   size
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

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
        this.writeAssembly(ONE_PARAM, BE_OP, DOLLAR+"printBool2");
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
        this.writeAssembly(NO_PARAM, DOLLAR + "printBool2:");

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


        this.writeAssembly(NEWLINE);

        // .$$.arrCheck
        this.writeAssembly(NO_PARAM, DOLLAR + "arrCheck:");

        // save %sp, -96, %sp
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, SAVE_OP, "%sp", "-96", "%sp");
        this.decreaseIndent();
        
        // cmp %io,%go
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP, "%i0","%g0");
        this.decreaseIndent();

        // bl .$$.arrCheck2
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BL_OP, DOLLAR + "arrCheck2");
        this.decreaseIndent();


        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();


        // cmp %io,%i1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP, "%i0","%i1");
        this.decreaseIndent();

        // bge .$$.arrCheck2
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BGE_OP, DOLLAR + "arrCheck2");
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



        // .$$.arrCheck2
        this.writeAssembly(NO_PARAM, DOLLAR + "arrCheck2:");


        // set .$$.strArrBound, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, ".$$.strArrBound", "%o0");
        this.decreaseIndent();


        // mov %i0 %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP,"%i0","%o1");
        this.decreaseIndent();

        // call printf
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, PRINT_OP);
        this.decreaseIndent();

  
        // mov %i1 %o2
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP,"%i1","%o2");
        this.decreaseIndent();


        // call exit
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, EXIT_OP);
        this.decreaseIndent();


        // mov 1 %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP,"1","%o0");
        this.decreaseIndent();


        // ret
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RET_OP);
        this.decreaseIndent();

        // restore
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RESTORE_OP);
        this.decreaseIndent();




        this.writeAssembly(NEWLINE);

        // .$$.ptrCheck
        this.writeAssembly(NO_PARAM, DOLLAR + "ptrCheck:");

        // save %sp, -96, %sp
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, SAVE_OP, "%sp", "-96", "%sp");
        this.decreaseIndent();
        
        // cmp %io,%g0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP, "%i0","%g0");
        this.decreaseIndent();

        // bne .$$.ptrCheck2
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BNE_OP, DOLLAR + "ptrCheck2");
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        //set .$$.strNullPtr %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, ".$$.strNullPtr", "%o0");
        this.decreaseIndent();


        // call printf
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, PRINT_OP);
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();


        // call exit
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, EXIT_OP);
        this.decreaseIndent();

        // mov 1 %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP,"1","%o0");
        this.decreaseIndent();


        // .$$.ptrCheck2
        this.writeAssembly(NO_PARAM, DOLLAR + "ptrCheck2:");

        // ret
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RET_OP);
        this.decreaseIndent();

        // restore
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RESTORE_OP);
        this.decreaseIndent();


        

    }


    // ----------------------------------------------------------------------------------
    // This is for global/static uninit vars decl, now includes 1D array, and static 
    // ----------------------------------------------------------------------------------
    public void DoGlobalVarDecl(STO sto, String name, String optstatic){

        this.writeAssembly(NEWLINE);

        // .section .bss
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".bss\"");
        this.decreaseIndent();

        // .align  4 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

        if(optstatic == null){
            // .global varname
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, GLOBAL, sto.getName());
            this.decreaseIndent();
        }

        // varname:
        this.writeAssembly(NO_PARAM,name+":");

        // .skip # (should auto init var to 0/false)
        this.increaseIndent();
        if(sto.getType() instanceof ArrayType){
            this.writeAssembly(ONE_PARAM, SKIP, String.valueOf(((ArrayType)sto.getType()).getTotalSize()));
        }
        else{
            this.writeAssembly(ONE_PARAM, SKIP, String.valueOf(sto.getType().getSize()));
        }
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        // .section .text
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
        this.decreaseIndent();

        // .align   4
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();
    
    
    }

    // ----------------------------------------------------------------------------------
    // This is for static init var decl starter
    // ----------------------------------------------------------------------------------
    public void DoStaticGuardStart(STO sto, String name){

        this.writeAssembly(NEWLINE);

        // .section .bss
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".bss\"");
        this.decreaseIndent();

        // .align  4 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

        // varname:
        this.writeAssembly(NO_PARAM, name+":");

        // .skip # (should anto init var to 0/false
        this.increaseIndent();
        if(sto.getType() instanceof ArrayType){
            this.writeAssembly(ONE_PARAM, SKIP, String.valueOf(((ArrayType)sto.getType()).getTotalSize()));
        }
        else{
            this.writeAssembly(ONE_PARAM, SKIP, String.valueOf(sto.getType().getSize()));
        }
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        // .section .text
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
        this.decreaseIndent();

        // .align   4
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        // ! Start init guard
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! Start init guard");
        this.decreaseIndent();

        // set name, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, name, "%o0");
        this.decreaseIndent();

        // ld [%o0], %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%o0");
        this.decreaseIndent();

        // cmp %o0, %g0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP, "%o0", "%g0");
        this.decreaseIndent();

        //bne name.done
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BNE_OP, name+".done");
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        // pure formatting indent
        this.increaseIndent();

    }

    // -------------------------------------------------------------------------
    // This is for static init var decl starter
    // -------------------------------------------------------------------------
    public void DoStaticGuardEnd(String name){

        // pure formatting indent
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);
    
        // ! End init guard
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! End init guard");
        this.decreaseIndent();

        // set name, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, name, "%o0");
        this.decreaseIndent();
        
        // mov 1, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP, "1", "%o1");
        this.decreaseIndent();

        // st %o1, [%o0]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o1", "[%o0]");
        this.decreaseIndent();

        // name.done:
        this.writeAssembly(NO_PARAM, name+".done:");
        
    }


    // ----------------------------------------------------------------------------------
    // This is for static init vars decl -- to be implemented
    // ----------------------------------------------------------------------------------
    public void DoStaticLocalVarInit(STO sto, String section, String name){

        this.writeAssembly(NEWLINE);

        // .section .bss
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".bss\"");
        this.decreaseIndent();

        // .align  4 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

        // varname:
        this.writeAssembly(NO_PARAM,name+":");

        // .word # (should anto init var to 0/false
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, WORD, String.valueOf(sto.getType().getSize())); 
        this.writeAssembly(NEWLINE);
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        // .section .text
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
        this.decreaseIndent();

        // .align   4
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();
     
    }




    // ----------------------------------------------------------------------------------
    // This is for local array assign
    // ----------------------------------------------------------------------------------

    public void DoArrayCheck(STO sto, STO expr, STO result){
        
        this.writeAssembly(NEWLINE);

        // lit case
        if(expr instanceof ConstSTO && !(((ConstSTO)expr).getLitTag())){

            int value = ((ConstSTO)expr).getIntValue();

            // ! comment
            this.increaseIndent();
            this.writeAssembly(NO_PARAM, "! "+sto.getName()+"["+String.valueOf(value)+"]");
            this.decreaseIndent();

            // set #, %o0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(value), "%o0");
            this.decreaseIndent();


        }
        // non lit case
        else{

            // ! comment
            this.increaseIndent();
            this.writeAssembly(NO_PARAM, "! "+sto.getName()+"["+expr.getName()+"]");
            this.decreaseIndent();

            //set expr.offset, %l7
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, expr.getOffset(), "%l7");
            this.decreaseIndent();

            //add %fp, %l7, %l7
            this.increaseIndent();
            this.writeAssembly(THREE_PARAM, ADD_OP, expr.getBase(),"%l7", "%l7");
            this.decreaseIndent();

            //added 11/29
            if(expr.getIsPointer()) {
               this.load(l7,l7);
            }

            //ld  [%l7], [%o0]
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%o0");
            this.decreaseIndent();

        }



        // call  .$$.arrCheck 
        if(!(sto.getType() instanceof PointerType)){
            // set totalsize, %o1
            //int total = ((ArrayType)sto.getType()).getLength();
            int total = ((ArrayType)sto.getType()).getSize();
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(total), "%o1");
            this.decreaseIndent();

            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, CALL_OP, DOLLAR+"arrCheck");
            this.decreaseIndent();

            // nop
            this.increaseIndent();
            this.writeAssembly(NO_PARAM, NOP_OP);
            this.decreaseIndent();
        }
        // set  base type size, %o0

        int baseSize;
        if(sto.getType() instanceof PointerType){
            baseSize = ((PointerType)sto.getType()).getBaseType().getSize();
        }
        else{
            baseSize = ((ArrayType)sto.getType()).getBaseType().getSize();
        }
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(baseSize),"%o1");
        this.decreaseIndent();

        // call .mul
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, MUL_OP);
        this.decreaseIndent();

        // nop 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();


        //mov %o0, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP, "%o0", "%o1");
        this.decreaseIndent();

        String reg = "%o0";
        if(sto.getType() instanceof PointerType){
            reg = "%l7";
        }


        //set offset, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), reg);
        this.decreaseIndent();

        //add base, "%o0", "%o0"
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), reg, reg);
        this.decreaseIndent();
    
        if(sto.getStructTag() || sto.getIsPointer() || sto.flag){
            // ld [%o0], %o0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%o0");
            this.decreaseIndent();
        }
        if(sto.getType() instanceof PointerType){
            // ld "[%l7]", %o0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%o0");
            this.decreaseIndent();
        }
        //call .$$.ptrCheck
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, DOLLAR+"ptrCheck");
        this.decreaseIndent();

        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        //add %o0, %o1, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, "%o0", "%o1", "%o0");
        this.decreaseIndent();

        //set result.offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, result.getOffset(), "%o1");
        this.decreaseIndent();

        //add %fp, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, result.getBase(),"%o1", "%o1");
        this.decreaseIndent();

        //st  %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();

    }


    public void DoStructArray(STO array, STO offset, int  length) {
        this.writeAssembly(NEWLINE);

         // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! "+array.getName()+"["+String.valueOf(length)+"]");
        this.decreaseIndent();

        // set #, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(length), "%o0");
        this.decreaseIndent();

         int total = ((ArrayType)array.getType()).getSize();
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(total), "%o1");
        this.decreaseIndent();

        // call  .$$.arrCheck
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, DOLLAR+"arrCheck");
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        // set  base type size, %o0 
        int baseSize = ((ArrayType)array.getType()).getBaseType().getSize();
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(baseSize),"%o1");
        this.decreaseIndent();

        // call .mul
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, MUL_OP);
        this.decreaseIndent();

        // nop 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        //mov %o0, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP, "%o0", "%o1");
        this.decreaseIndent();

        //set offset, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, array.getOffset(), "%o0");
        this.decreaseIndent();

        //add base, "%o0", "%o0"
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, array.getBase(), "%o0", "%o0");
        this.decreaseIndent();
    
        if(array.getStructTag()){
            // ld [%o0], %o0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%o0");
            this.decreaseIndent();
        }
        //call .$$.ptrCheck
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, DOLLAR+"ptrCheck");
        this.decreaseIndent();

        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        //add %o0, %o1, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, "%o0", "%o1", "%o0");
        this.decreaseIndent();

        //set result.offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, offset.getOffset(), "%o1");
        this.decreaseIndent();

        //add %fp, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, offset.getBase(),"%o1", "%o1");
        this.decreaseIndent();

        //st  %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();

    }

    // ----------------------------------------------------------------------------------
    // This is for global/static init var decl
    // ----------------------------------------------------------------------------------
    public void DoGlobalVarInitLit(STO sto, String num, String name, String optstatic){
        
        this.writeAssembly(NEWLINE);
        
        // .section .data
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".data\"");
        this.decreaseIndent();

        // .align  4 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

        if(optstatic == null){
            // .global varname
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, GLOBAL, sto.getName());
            this.decreaseIndent();
        }

        // varname:
        this.writeAssembly(NO_PARAM, name+":");

        this.increaseIndent();
        if(sto.getType() instanceof FloatType){
            // .single #
            this.writeAssembly(ONE_PARAM, SINGLE, "0r"+num);
        }
        else{
            // .word  #
            this.writeAssembly(ONE_PARAM, WORD, num);
        }
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        // .section .text
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
        this.decreaseIndent();

        // .align   4
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

    }


    public void DoGlobalVarInitVar(STO sto, String optstatic){
        
        this.writeAssembly(NEWLINE);

        // .section .bss
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".bss\"");
        this.decreaseIndent();

        // .align  4 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

        // .global varname
        if(optstatic == null){
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, GLOBAL, sto.getName());
            this.decreaseIndent();
        }

        // varname:
        this.writeAssembly(NO_PARAM,sto.getName()+":");

        // .skip  # (not always 4, since we have struct) 
        this.increaseIndent();
        if(sto.getType() instanceof ArrayType){
            this.writeAssembly(ONE_PARAM, SKIP, String.valueOf(((ArrayType)sto.getType()).getTotalSize()));
        }
        else{
            this.writeAssembly(ONE_PARAM, SKIP, String.valueOf(sto.getType().getSize()));
        }
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        // .section  .text
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
        this.decreaseIndent();

        // .align   4
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

        // call the helper start
        this.initGlobalVarStart(sto);



    }
    //---------------------------------------------------------------
    // A helper function that init var to another var, first half
    //---------------------------------------------------------------
    public void initGlobalVarStart(STO sto){
    
        // .$.init.varname:
        this.writeAssembly(NO_PARAM, INIT + sto.getName() + ":");
        
        // set   SAVE..$.init.y, %g1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, "SAVE."+ INIT +sto.getOffset(), "%g1" );
        this.decreaseIndent();

        //save   %sp, %g1, %sp
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, SAVE_OP, "%sp", "%g1", "%sp");
        this.decreaseIndent();

        this.increaseIndent();
        
    }

    //---------------------------------------------------------------
    // handles var assignment
    //---------------------------------------------------------------
    public void DoVarAssign(STO sto, STO expr, STO promote){

        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "!"+ sto.getName() + " = " + expr.getName());
        this.decreaseIndent();

        // set    sto offset, %o1  
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%o1");
        this.decreaseIndent();

        // add    sto base, %o1, %o1  
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        
        // do load for reference added 11/14
        if(sto.flag || sto.getArrayTag() || sto.getStructTag() || sto.getIsPointer()) {
                // ld    [%o1], o1
           this.increaseIndent();
           this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o1]", "%o1");
           this.decreaseIndent();
        }


        if(expr.getType() instanceof NullPointerType){
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, "0", o0);
            this.decreaseIndent();

            this.store(o0,o1);
        }

        else{
            this.getVar(expr, l7);
        
             // check for pointer, array
             if(expr.getArrayTag() || expr.getIsPointer() || expr.flag){
                 this.increaseIndent();
                 this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%l7");
                 this.decreaseIndent();
             }


             //float to float
            if(expr.getType() instanceof FloatType){

                // ld   [%l7], %f0
                this.increaseIndent();
                this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%f0");
                this.decreaseIndent();


                // st   %f0, [%o1]
                this.increaseIndent();
                this.writeAssembly(TWO_PARAM, STORE_OP, "%f0", "[%o1]");
                this.decreaseIndent();

           }
           else{
               // ld   [%l7], %o0
               this.increaseIndent();
               this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%o0");
               this.decreaseIndent();

                // int to float, type promote
                if(promote != null){
                    this.DoTypePromotion(promote, "%f0", "%o0");

                    //st   %f0, [%o1]
                    this.increaseIndent();
                    this.writeAssembly(TWO_PARAM, STORE_OP, "%f0", "[%o1]");
                    this.decreaseIndent();

               }
               else{
                   // st   %o0, [%o1]
                   this.increaseIndent();
                   this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
                   this.decreaseIndent();
               }

           }


        }





    }

    // --------------------------------------------------------------
    // This is a helper that handles float specifically
    // called in DoFloatAssign and DoPrintConstFloat
    // --------------------------------------------------------------
    public void DoFloatRoData(STO sto, String reg){
          
        this.writeAssembly(NEWLINE);


        //.section ".rodata" 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".rodata\"");
        this.decreaseIndent();

        // .align 4
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, "4");
        this.decreaseIndent();

        // .$$.float.#
        constFloatCnt++;
        this.writeAssembly(NO_PARAM, DOLLAR + "float." + Integer.toString(constFloatCnt) + ":");

        // .single 0r#
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SINGLE, "0r" + String.valueOf(((ConstSTO)sto).getFloatValue()) );
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        // .section ".text"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"" );
        this.decreaseIndent();

        // .align 4
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, "4" );
        this.decreaseIndent();

        //set .$$.float.#, %l7
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR + "float." + Integer.toString(constFloatCnt) , "%l7" );
        this.decreaseIndent();

        //ld [%l7], %f0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", reg);
        this.decreaseIndent();

    }
    // ---------------------------------------------------------------
    // This is assignment for const Float 
    // ---------------------------------------------------------------
    public void DoFloatAssign(STO a, STO b, STO promote){

        this.writeAssembly(NEWLINE);

        //! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! " + a.getName() + " = " + b.getName());
        this.decreaseIndent();

        
        // set   var name, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, a.getOffset(), "%o1" );
        this.decreaseIndent();

        // add   %g0, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, a.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        // add load op for ref
        // ld [%o1] %o1
        if(a.flag || a.getArrayTag() || a.getStructTag() || a.getIsPointer()) {
           this.increaseIndent();
           this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o1]", "%o1" );
           this.decreaseIndent();
        
        }

            
        if(promote != null){
            int val = ((ConstSTO)b).getIntValue();
            // set #, %o0 
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(val) , "%o0");
            this.decreaseIndent();

            // do type promotion
            this.DoTypePromotion(promote, "%f0", "%o0");

        }
        // rodata only for float to float
        else{
            this.DoFloatRoData(b, "%f0");
        }
        // st    %f0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%f0", "[%o1]");
        this.decreaseIndent();

    }

    // ---------------------------------------------------------------
    // This is assignment for const Int and Bool 
    // ---------------------------------------------------------------
    public void DoConstAssign(STO a, String b, String forcommentonly){

        this.writeAssembly(NEWLINE);

        //! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! " + a.getName() + " = " + forcommentonly);
        this.decreaseIndent();

        // set   offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, a.getOffset(), "%o1" );
        this.decreaseIndent();

        // add   base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, a.getBase(), "%o1", "%o1");
        this.decreaseIndent();


        // add load op for ref 
        // ld [%o1] %o1
        if(a.flag || a.getArrayTag() || a.getStructTag() || a.getIsPointer()) {
           this.increaseIndent();
           this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o1]", "%o1");
           this.decreaseIndent();

        }


        // set   #, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, b, "%o0");
        this.decreaseIndent();

        // st    %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();
        
    }



    //---------------------------------------------------------------
    // A helper function that init var to another var, second half
    //---------------------------------------------------------------
    public void initGlobalVarEnd(STO sto, STO func){
        
        this.writeAssembly(NEWLINE);

        this.decreaseIndent();

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! End of function " + INIT + sto.getName() + ".fini" );
        this.decreaseIndent();

        // call    .$.init.b.fini
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, INIT+sto.getName()+".fini");
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

        // SAVE..$.init.b = -(base + offset) & -8
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "SAVE." + INIT + sto.getName() + " = -("+ func.getAddress() +") & -8");//work for global case but might need to change it for local
        
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        //.$.init.var name.fini:
        
        this.writeAssembly(NO_PARAM, INIT + sto.getName() + ".fini:" );
        

        // save   %sp, -96, %sp
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, SAVE_OP, "%sp", "-96", "%sp");
        this.decreaseIndent();

        // ret
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RET_OP);
        this.decreaseIndent();

        // restore
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RESTORE_OP);
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        //.section ".init"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".init\"");
        this.decreaseIndent();

        //.align   4
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

        //call     .$.init.var name
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, INIT+sto.getName());
        this.decreaseIndent();

        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        //.section  ".text"
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
        this.decreaseIndent();

        //.align    4
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();

        
    }


    public void FuncGroup(STO func) {
        this.writeAssembly(NEWLINE);

        // .global  func_name
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, GLOBAL, func.getName());
        this.decreaseIndent();

        // label:
        this.writeAssembly(NO_PARAM, func.getName()+":");      
    
    }


    // ---------------------------------------------------------------------------------
    // Struct constructor call with no params
    // ---------------------------------------------------------------------------------
    public void DoCtor(STO sto, STO func){
        

        this.writeAssembly(NEWLINE);

        // ! s.name(...)
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! "+sto.getName()+"."+sto.getType().getName()+ "( ... )");
        this.decreaseIndent();

        // set  offset, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%o0");
        this.decreaseIndent();

        // add  %fp, %o0, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), "%o0", "%o0");
        this.decreaseIndent();

        // add arry check -- 11/22 might breaks shit
        if(sto.getIsPointer() || sto.getArrayTag()){
            // ld [%o0] %o0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%o0");
            this.decreaseIndent();
        }

        // call  funccall
        this.increaseIndent();
        if(sto.getType() instanceof ArrayType) {
            this.writeAssembly(ONE_PARAM, CALL_OP, ((ArrayType)sto.getType()).getBaseType().getName()+"."+func.getName()+"."+func.getAssemblyName());

        }
        else {
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getType().getName()+"."+sto.getType().getName()+"."+func.getAssemblyName());

        }
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();


    }

    // functionf for passing "this" parameter
    public void DoCtorThis(STO sto) {

        this.writeAssembly(NEWLINE);

         // set  offset, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%o0");
        this.decreaseIndent();

        // add  %fp, %o0, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), "%o0", "%o0");
        this.decreaseIndent();

        // add array check -- 11/22 might break shit
        if(sto.getIsPointer() || sto.getArrayTag()){
            // ld [%o0], %o0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%o0");
            this.decreaseIndent();
        }

    }

    //----------------------
    // Struct Assign
    //----------------------
    public void DoStructAssign(STO a, STO b){
        
        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! "+ a.getName() +" = " + b.getName());
        this.decreaseIndent();

         // set  a.offset, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, a.getOffset(), "%o0");
        this.decreaseIndent();

        // add  %fp, %o0, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, a.getBase(), "%o0", "%o0");
        this.decreaseIndent();

        if(a.getIsPointer()){
            this.load(o0, o0);
        }
        // set  b.offset, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, b.getOffset(), "%o1");
        this.decreaseIndent();

        // add  %fp, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, b.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        if(b.getIsPointer()){
            this.load(o1, o1);
        }

        //set struct size %o2
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(a.getType().getSize()), "%o2"); 
        this.decreaseIndent();


        //call memmove
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, "memmove");
        this.decreaseIndent();

        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

    }


    // ----------------------------------------------------------------------------------
    // Struct Var Usage
    // ----------------------------------------------------------------------------------
    public void DoStructCall(STO sto, STO result){
        
        this.writeAssembly(NEWLINE);

        //! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! "+sto.getName()+"."+result.getName());
        this.decreaseIndent();

        //set sto.offset, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%o0");
        this.decreaseIndent();

        //add sto.base, %o0, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(),"%o0", "%o0");
        this.decreaseIndent();

        // check for array, pointer and ref
        if(sto.getArrayTag() || sto.getIsPointer() || sto.flag) {

            
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP,"[%o0]", "%o0");
            this.decreaseIndent();
        }

        //set offset in struct, %o1
        this.increaseIndent();  
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(result.getStructOffset()), "%o1");
        this.decreaseIndent();

        //add %g0, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, "%g0", "%o1", "%o1");
        this.decreaseIndent();

        //add %o0, %o1, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, "%o0", "%o1", "%o0");
        this.decreaseIndent();

        //set result.offset %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, result.getOffset(), "%o1");
        this.decreaseIndent();

        //add result.base %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, result.getBase(),"%o1", "%o1");
        this.decreaseIndent();

        //st %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();

    }


    // ----------------------------------------------------------------------------------
    // Struct Var self Usage
    // ----------------------------------------------------------------------------------
    public void DoThisCall(STO result){
        
        this.writeAssembly(NEWLINE);

        //! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! this."+result.getName());
        this.decreaseIndent();

        //set sto.offset, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, "68", "%o0");
        this.decreaseIndent();

        //add sto.base, %o0, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, "%fp","%o0", "%o0");
        this.decreaseIndent();

        // ld 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%o0");
        this.decreaseIndent();


        //set offset in struct, %o1
        this.increaseIndent();  
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(result.getStructOffset()), "%o1");
        this.decreaseIndent();

        //add %g0, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, "%g0", "%o1", "%o1");
        this.decreaseIndent();

        //add %o0, %o1, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, "%o0", "%o1", "%o0");
        this.decreaseIndent();

        //set result.offset %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, result.getOffset(), "%o1");
        this.decreaseIndent();

        //add result.base %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, result.getBase(),"%o1", "%o1");
        this.decreaseIndent();

        //st %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();

    }

    // ----------------------------------------------------------------------------------
    // func decl with no params (first half before the body) 
    // ----------------------------------------------------------------------------------

    public void DoFuncStart(STO sto, String reg, String optstructname){

        String SAVE;
        if(optstructname != null){
            String s = sto.getName();
            if(sto.getName().charAt(0) == '~'){
                s= s.replace('~', '$');

            }
            
            SAVE = "SAVE." +optstructname+"."+ s + "."+ ((FuncSTO)sto).getAssemblyName();

            // label.params:
            this.writeAssembly(NO_PARAM, optstructname+"."+ s +"."+((FuncSTO)sto).getAssemblyName() +":");
            

        }
        else{

            SAVE = "SAVE." + sto.getName() + "."+ ((FuncSTO)sto).getAssemblyName();

            // label.params:
            this.writeAssembly(NO_PARAM, sto.getName()+"."+((FuncSTO)sto).getAssemblyName() +":");
        }
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

        // for pure formatting
        this.increaseIndent();
        
    }
    

    // ----------------------------------------------------------------------------------
    // func decl with no params (second half after the body)
    // ----------------------------------------------------------------------------------

    public void DoFuncEnd(STO sto, String optstructname){
 
        String SAVE;
        String NAME;
        if(optstructname != null){
            
            String s = sto.getName();
            if(sto.getName().charAt(0) == '~'){
                s = s.replace('~', '$');

            }

            SAVE = "SAVE." +optstructname+"."+ s + "."+ ((FuncSTO)sto).getAssemblyName();
            NAME = optstructname +"." + s + "."+ ((FuncSTO)sto).getAssemblyName();
            
        }
        else{
            SAVE = "SAVE." + sto.getName() + "."+ ((FuncSTO)sto).getAssemblyName();
            NAME = sto.getName() + "."+ ((FuncSTO)sto).getAssemblyName();

        }
        // for pure formating, reflect to indent in DoFuncStart
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! End of function " + SAVE);
        this.decreaseIndent();

        // call    funcname.type.fini
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM,  CALL_OP, NAME + ".fini"); 
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

        // SAVE,funcname.type = -(base + offset) & -8
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, SAVE + " = -("+ sto.getAddress() + ") & -8"); // need to fix sth about local vars
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        //funcname.type.fini
        this.writeAssembly(NO_PARAM, NAME+ ".fini:");
        
        //save   %sp, -96, %sp
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, SAVE_OP, "%sp", "-96", "%sp");
        this.decreaseIndent();

        
    }

    /*
     * The next Eight methods handle cout
     */

    // ----------------------------------------------------------------------------------
    // This is for const String cout
    // ----------------------------------------------------------------------------------
    public void printConstStr(STO sto, String reg){

        this.writeAssembly(NEWLINE);
    
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

        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! cout << \"" +sto.getName() +"\"" );
        this.decreaseIndent();

        //set .$$.strFmt, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR + "strFmt", "%o0" );
        this.decreaseIndent();

        //set .$$.str.#, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR + "str." + Integer.toString(constStrCnt), reg );
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



    // ----------------------------------------------------------------------------------
    // This is for const Float cout
    // ----------------------------------------------------------------------------------
    public void printConstFloat(STO sto, String reg){


        this.writeAssembly(NEWLINE);

        // !comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! cout << " +sto.getName() );
        this.decreaseIndent();
        
        this.DoFloatRoData(sto, reg);

        //call printFloat
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, "printFloat");
        this.decreaseIndent();
            
        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

    }
    // ----------------------------------------------------------------------------------
    // This is for const Int cout
    // ----------------------------------------------------------------------------------
    public void printConstInt(STO sto, String reg){

        int num = ((ConstSTO)sto).getIntValue();
          
        this.writeAssembly(NEWLINE);


        // !comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! cout << " +sto.getName()  );
        this.decreaseIndent();

        //set, #, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(num) , reg );
        this.decreaseIndent();

        //set .$$.intFmt, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR + "intFmt", "%o0");
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
    // ----------------------------------------------------------------------------------
    // This is for const Bool cout
    // ----------------------------------------------------------------------------------
    public void printConstBool(STO sto, String reg){

        Boolean b = ((ConstSTO)sto).getBoolValue();
        int num = b ? 1 : 0;

        this.writeAssembly(NEWLINE);


        // !comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! cout << " +sto.getName()  );
        this.decreaseIndent();

        //set, #, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(num) , reg );
        this.decreaseIndent();

        //call .$$.printBool
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, DOLLAR + "printBool");
        this.decreaseIndent();
            
        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();


    }       


    // --------------------------------------------------------------------
    // handles endl for print
    // --------------------------------------------------------------------
    public void printNL(String reg){

        this.writeAssembly(NEWLINE);

        // !comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! cout << endl" );
        this.decreaseIndent();

        // set .$$.strEndl, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR+"strEndl", reg);
        this.decreaseIndent();

        // call printf
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, PRINT_OP );
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();
    }

    // --------------------------------------------------------------------
    // handles all var for cout
    // --------------------------------------------------------------------

    public void printVar(STO sto, String reg){

        Type t = sto.getType();

        this.writeAssembly(NEWLINE);

        // !comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! cout << " + sto.getName() );
        this.decreaseIndent(); 

        // set varname, %l7
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), reg);
        this.decreaseIndent();

        // add base, %l7, %l7
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), reg, reg);
        this.decreaseIndent(); 


        this.increaseIndent();
        if(t instanceof BoolType){

            // if param is a reference
            if(sto.flag == true || sto.getArrayTag() || sto.getStructTag() || sto.getIsPointer()) {
               this.writeAssembly(TWO_PARAM, LOAD_OP, "["+reg+"]", reg);
               //this.decreaseIndent();

            }

            // ld [%l7], %o0/
            this.writeAssembly(TWO_PARAM, LOAD_OP, "["+reg+"]", "%o0");
            this.decreaseIndent();

            // call .$$.printBool
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, CALL_OP, DOLLAR+"printBool");
        }
        else if( t instanceof IntType){

            if(sto.flag == true || sto.getArrayTag() || sto.getStructTag() || sto.getIsPointer()) {
               this.writeAssembly(TWO_PARAM, LOAD_OP, "["+reg+"]", reg);
               //this.decreaseIndent();

            }



            // ld [%l7], %o1
            this.writeAssembly(TWO_PARAM, LOAD_OP, "["+reg+"]", "%o1");
            this.decreaseIndent();

            // set .$$.intFmt, %o0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR+"intFmt", "%o0");
            this.decreaseIndent();
            this.increaseIndent();
            // call printf
            this.writeAssembly(ONE_PARAM, CALL_OP, PRINT_OP);
        }
        else if( t instanceof FloatType){
 
            if(sto.flag == true || sto.getArrayTag() || sto.getStructTag() || sto.getIsPointer()) {
               this.writeAssembly(TWO_PARAM, LOAD_OP, "["+reg+"]", reg);
               //this.decreaseIndent();

            }
            // ld [%l7], %f0
            this.writeAssembly(TWO_PARAM, LOAD_OP, "["+reg+"]", "%f0");
            this.decreaseIndent();

            // call printFloat
            this.increaseIndent();
            this.writeAssembly(ONE_PARAM, CALL_OP, "printFloat" );

        }
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

    
    
    }
    

    //-----------------------------------------------------
    // This handles the Lit case of exit
    //-----------------------------------------------------

    public void DoExitLit(STO sto){
        
        int val = ((ConstSTO)sto).getIntValue();

        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! exit("+sto.getName()+")");
        this.decreaseIndent();

        //set   #, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(val), "%o0");
        this.decreaseIndent();

        //call    exit
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, EXIT_OP);
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

    }

    // ----------------------------------------------------------
    // This handles the Const var and regular var case for exit
    // ----------------------------------------------------------
    public void DoExitBase(STO sto){

        this.writeAssembly(NEWLINE);

        // ! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! exit(" + sto.getName() + ")");
        this.decreaseIndent();

        // set  offset, %l7
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%l7");
        this.decreaseIndent();

        // add  base, %l7, %l7
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(),"%l7", "%l7");
        this.decreaseIndent();

        // ld    [%l7], %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%o0");
        this.decreaseIndent();
 
        //call    exit
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, EXIT_OP);
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();


    }

    // -------------------------------------------------------------------
    // A helper that takes care that if the operand in DoBinary is var
    // -------------------------------------------------------------------
    public void DoOperandLit(STO sto, String reg){
        ConstSTO con = (ConstSTO)sto;

        if(sto.getType() instanceof IntType){
            int value = con.getIntValue();

            // set  #, reg
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(value), reg);
            this.decreaseIndent();
        }
        else if(sto.getType() instanceof FloatType){
            float value = con.getFloatValue();
            
            this.DoFloatRoData(sto, reg);

        }
        else {
            int value = con.getBoolValue() ? 1 : 0;

            // set  #, reg
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(value), reg);
            this.decreaseIndent();

        }

    }




    // -------------------------------------------------------------------
    // A helper that takes care that if the operand in DoBinary is var
    // -------------------------------------------------------------------
    public void DoOperand(STO sto, String reg){
        
        // set  sto.offset, %l7
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%l7");
        this.decreaseIndent();

        // add  sto.base, %l7, %l7
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(),"%l7", "%l7");
        this.decreaseIndent();
        
        // changed 11/21 sto.getIstPointer 
        if(sto.flag == true || sto.getArrayTag() || sto.getStructTag() || sto.getIsPointer()) {
            // ld    [%l7], %l7
           this.increaseIndent();
           this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%l7");
           this.decreaseIndent();

        
        }

        // ld    [%l7], reg
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", reg);
        this.decreaseIndent();


    }


    // -------------------------------------------------------------------
    // This handles all arithmetic expression for int (don't know how different it is from float, etc)
    // -------------------------------------------------------------------
    public void DoBinaryInt(STO a, STO b, String op, STO result){


        this.writeAssembly(NEWLINE);

        // ! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! " + result.getName());
        this.decreaseIndent();


        // check the first operand is Lit 
        if(a instanceof ConstSTO && (!((ConstSTO)a).getLitTag())){
           this.DoOperandLit(a, "%o0"); 
            
        }
        // null pointer check
        else if(a.getType() instanceof NullPointerType){
           // set 0, %o0
           this.increaseIndent();
           this.writeAssembly(TWO_PARAM, SET_OP, "0", "%o0");
           this.decreaseIndent();
        }
        else{
            this.DoOperand(a, "%o0");
        }


        // check the second operand is Lit
        if(b instanceof ConstSTO && (!((ConstSTO)b).getLitTag())){
           this.DoOperandLit(b, "%o1"); 
            
        }
        // null pointer check
        else if(b.getType() instanceof NullPointerType){
           // set 0, %o1
           this.increaseIndent();
           this.writeAssembly(TWO_PARAM, SET_OP, "0", "%o1");
           this.decreaseIndent();
        }

        else{
            this.DoOperand(b, "%o1");
        }



        if(op.equals("+")){
            this.DoPrimary(ADD_OP, "%o0", "%o1", "%o0");
        }
        else if(op.equals("-")){
            this.DoPrimary(SUB_OP, "%o0", "%o1", "%o0");
        }
        else if(op.equals("/")){
            this.DoSecondary(DIV_OP);
        }
        else if(op.equals("*")){
            this.DoSecondary(MUL_OP);
        }
        else if(op.equals("%")){
            this.DoSecondary(MOD_OP);
        }
        else if(op.equals("|")){
            this.DoPrimary(OR_OP, "%o0", "%o1", "%o0");
        }
        else if(op.equals("&")){
            this.DoPrimary(AND_OP, "%o0", "%o1", "%o0");
        }
        else if(op.equals("^")){
            this.DoPrimary(XOR_OP, "%o0", "%o1", "%o0");
        }
        else if(op.equals(">")){
            cmpCnt++;
            this.DoCmp(BLE_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));

        }
        else if(op.equals("<")){
            cmpCnt++;
            this.DoCmp(BGE_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));

        }
        else if(op.equals("<=")){
            cmpCnt++;
            this.DoCmp(BG_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));

        }
        else if(op.equals(">=")){
            cmpCnt++;
            this.DoCmp(BL_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));

        }
        else if(op.equals("==")){
            cmpCnt++;
            this.DoCmp(BNE_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));

        }
        else if(op.equals("!=")){
            cmpCnt++;
            this.DoCmp(BE_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));

        }
        else if(op.equals("")){
            cmpCnt++;
            this.DoCmp(BE_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));

        }

 
        // set  result.offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, result.getOffset(), "%o1");
        this.decreaseIndent();

        // add  result.base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, result.getBase(),"%o1", "%o1");
        this.decreaseIndent();

        // st    %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();


    }
    // -------------------------------------------------------------------
    // Int to Float promotion helper method
    // -------------------------------------------------------------------
    public void DoTypePromotion(STO sto, String reg, String reg2){
        //set     	offset, %l7
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%l7");
		this.decreaseIndent();

        //add     	base, %l7, %l7
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), "%l7", "%l7");
		this.decreaseIndent();
       
		//st      	reg2, [%l7]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, reg2, "[%l7]");
		this.decreaseIndent();

		//ld      	[%l7], %f0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", reg);
		this.decreaseIndent();

		//fitos   	%f0, %f0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, FITOS_OP, reg, reg);
		this.decreaseIndent();

    }
    // -------------------------------------------------------------------
    // This handles all arithmetic expression for float
    // -------------------------------------------------------------------
    public void DoBinaryFloat(STO a, STO promoteA, STO b, STO promoteB, String op, STO result){


        // for cmp
        String CmpReg = "%f0";

        // handles first operand reg
        String regA = "%f0";
        if(promoteA != null){
            regA = "%o0";
        }
        // handles second operand reg
        String regB = "%f1";
        if(promoteB != null){
            regB = "%o1";
        }

        this.writeAssembly(NEWLINE);

        // ! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! " + result.getName());
        this.decreaseIndent();

        // check the first operand is Lit 
        if(a instanceof ConstSTO && (!((ConstSTO)a).getLitTag())){
           this.DoOperandLit(a, regA); 
            
        }
        else{
            this.DoOperand(a, regA);
        }

        // check the first operand is int
        if(promoteA != null){
            this.DoTypePromotion(promoteA, "%f0", "%o0");
        }


        // check the second operand is Lit
        if(b instanceof ConstSTO && (!((ConstSTO)b).getLitTag())){
           this.DoOperandLit(b, regB); 
            
        }
        else{
            this.DoOperand(b, regB);
        }

        // check the second operand is int
        if(promoteB != null){
            this.DoTypePromotion(promoteB, "%f1", "%o1");
        }



        if(op.equals("+")){
            this.DoPrimary(FADD_OP, "%f0", "%f1", "%f0");
        }
        else if(op.equals("-")){
            this.DoPrimary(FSUB_OP, "%f0", "%f1", "%f0");
        }
        else if(op.equals("/")){
            this.DoPrimary(FDIV_OP, "%f0", "%f1", "%f0");
        }
        else if(op.equals("*")){
            this.DoPrimary(FMUL_OP, "%f0", "%f1", "%f0");
        }
        else if(op.equals(">")){
            cmpCnt++;
            this.DoCmpFloat(FBLE_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));
            CmpReg = "%o0";

        }
        else if(op.equals("<")){
            cmpCnt++;
            this.DoCmpFloat(FBGE_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));
            CmpReg = "%o0";
        }
        else if(op.equals("<=")){
            cmpCnt++;
            this.DoCmpFloat(FBG_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));

            CmpReg = "%o0";
        }
        else if(op.equals(">=")){
            cmpCnt++;
            this.DoCmpFloat(FBL_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));
            CmpReg = "%o0";


        }
        else if(op.equals("==")){
            cmpCnt++;
            this.DoCmpFloat(FBNE_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));
            CmpReg = "%o0";


        }
        else if(op.equals("!=")){
            cmpCnt++;
            this.DoCmpFloat(FBE_OP, DOLLAR+"cmp."+String.valueOf(cmpCnt));
            CmpReg = "%o0";


        }


 
        // set  result.offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, result.getOffset(), "%o1");
        this.decreaseIndent();

        // add  result.base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, result.getBase(),"%o1", "%o1");
        this.decreaseIndent();

        // st    %f0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, CmpReg, "[%o1]");
        this.decreaseIndent();


    }

    // -------------------------------------------------------------------
    // This handles all arithmetic expression for bool LHS
    // -------------------------------------------------------------------
    public void DoBinaryBoolLHS(STO a, String op){


        this.writeAssembly(NEWLINE);

        // ! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! Short Circuit LHS");
        this.decreaseIndent();


        // check the first operand is Lit 
        if(a instanceof ConstSTO && (!((ConstSTO)a).getLitTag())){
           this.DoOperandLit(a, "%o0"); 
            
        }
        else{
            this.DoOperand(a, "%o0");
        }


        if(op.equals("&&")){
            andorCnt++;
            andor.push(andorCnt);
            this.DoCmpBool(BE_OP, DOLLAR+"andorSkip."+String.valueOf(andorCnt));
            
        }
        else if(op.equals("||")){
            andorCnt++;
            andor.push(andorCnt);
            this.DoCmpBool(BNE_OP, DOLLAR+"andorSkip."+String.valueOf(andorCnt));
        }

    }






    // -------------------------------------------------------------------
    // This handles all arithmetic expression for bool RHS
    // -------------------------------------------------------------------
    public void DoBinaryBoolRHS(STO a, STO b, String op, STO result){

        String s = "";
        String nots = "";
        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! " +result.getName());
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! Short Circuit RHS");
        this.decreaseIndent();



        // check the second operand is Lit
        if(b instanceof ConstSTO && (!((ConstSTO)b).getLitTag())){
           this.DoOperandLit(b, "%o0"); 
            
        }
        else{
            this.DoOperand(b, "%o0");
        }


        int cnt = andor.pop();
        if(op.equals("&&")){
            this.DoCmpBool(BE_OP, DOLLAR+"andorSkip."+String.valueOf(cnt));
            s = "0";
            nots = "1";

        }
        else if(op.equals("||")){
            this.DoCmpBool(BNE_OP, DOLLAR+"andorSkip."+String.valueOf(cnt));
            s = "1";
            nots = "0";
        }

        //ba     .$$.andorEnd.#
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BA_OP, DOLLAR+"andorEnd."+String.valueOf(cnt));
        this.decreaseIndent();

        // move #, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP, nots, "%o0");
        this.decreaseIndent();

        //.$$.andorSkip.#:
        
        this.writeAssembly(NO_PARAM, DOLLAR+"andorSkip."+String.valueOf(cnt)+":");
        

        // mov  s, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP, s, "%o0");
        this.decreaseIndent();

        //.$$.andorEnd.#:
        
        this.writeAssembly(NO_PARAM, DOLLAR+"andorEnd."+String.valueOf(cnt)+":");
        

        if((!(a instanceof ConstSTO)) || (!(b instanceof ConstSTO))){
        
            // set  result.offset, %o1
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, SET_OP, result.getOffset(), "%o1");
            this.decreaseIndent();

            // add  result.base, %o1, %o1
            this.increaseIndent();
            this.writeAssembly(THREE_PARAM, ADD_OP, result.getBase(),"%o1", "%o1");
            this.decreaseIndent();

            // st    %o0, [%o1]
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
            this.decreaseIndent();
        }
    }


    //------------------------------------------------------------------
    // This should handle unary for both int and float
    //------------------------------------------------------------------
    public void DoUnary(STO sto, STO unary, String reg, String s){

        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! "+ unary.getName());
        this.decreaseIndent();

        // set sto.offset, %l7
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%l7");
        this.decreaseIndent();

        // add sto.base, %l7, %l7 
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), "%l7", "%l7");
        this.decreaseIndent();

        // added 11/29
        if(sto.flag || sto.getIsPointer() || sto.getArrayTag() || sto.getStructTag()) {
           this.load(l7,l7);
        }

        // ld [%l7], reg
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", reg);
        this.decreaseIndent();

        this.increaseIndent();

        // neg/fneg reg, reg
        if(s == "+"){
            if(sto.getType() instanceof IntType){
                this.writeAssembly(TWO_PARAM, MOV_OP, reg, reg);
            }
            else{
                this.writeAssembly(TWO_PARAM, FMOV_OP, reg, reg);
            }
        }
        else if(s == "-"){
            if(sto.getType() instanceof IntType){
                this.writeAssembly(TWO_PARAM, NEG_OP, reg, reg);
            }
            else {
                this.writeAssembly(TWO_PARAM, FNEG_OP, reg, reg);
            }
        }
        // xor %o0, 1, %o0
        else{
            
            this.writeAssembly(THREE_PARAM, XOR_OP, "%o0", String.valueOf(1), "%o0");
        }
        this.decreaseIndent();

        // set unary.getoffset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, unary.getOffset(), "%o1");
        this.decreaseIndent();


        // add unary.getBase, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, unary.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        // st reg, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, reg, "[%o1]");
        this.decreaseIndent();


    }




    // -------------------------------------------------------------------
    // This handles pre/post inc/dev only for int 
    // -------------------------------------------------------------------
    public void DoPrePostInt(STO a, STO b, String op, STO result, String reg){

        this.writeAssembly(NEWLINE);

        // ! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! " + result.getName());
        this.decreaseIndent();


        this.DoOperand(a, "%o0");
       

        this.DoOperandLit(b, "%o1"); 
            


        if(op.equals("++")){
            this.DoPrimary(ADD_OP, "%o0", "%o1", "%o2");
        }
        else if(op.equals("--")){
            this.DoPrimary(SUB_OP, "%o0", "%o1", "%o2");
        }
 
        // set  result.offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, result.getOffset(), "%o1");
        this.decreaseIndent();

        // add  result.base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, result.getBase(),"%o1", "%o1");
        this.decreaseIndent();

        // st    %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, reg, "[%o1]");
        this.decreaseIndent();

        // set a.getoffset, %o1
        this.increaseIndent();        
        this.writeAssembly(TWO_PARAM, SET_OP, a.getOffset(), "%o1");
        this.decreaseIndent();

        // add %fp, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, a.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        // for array
        if(a.getArrayTag() || a.getStructTag() || a.flag){
            //ld    [%o1], %o1
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o1]", "%o1");
            this.decreaseIndent();
        }
        // st   %o2, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o2", "[%o1]");
        this.decreaseIndent();
       

    }

    // -------------------------------------------------------------------
    // This handles pre/post inc/dev only for float
    // -------------------------------------------------------------------
    public void DoPrePostFloat(STO a, STO b, String op, STO result, String reg){

        this.writeAssembly(NEWLINE);

        // ! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! " + result.getName());
        this.decreaseIndent();


        this.DoOperand(a, "%f0");
        this.DoFloatRoData(b, "%f1");

        //this.DoOperandLit(b, "%o1"); 
            
        

        if(op.equals("++")){
            this.DoPrimary(FADD_OP, "%f0", "%f1", "%f2");
        }
        else if(op.equals("--")){
            this.DoPrimary(FSUB_OP, "%f0", "%f1", "%f2");
        }
 
        // set  result.offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, result.getOffset(), "%o1");
        this.decreaseIndent();

        // add  result.base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, result.getBase(),"%o1", "%o1");
        this.decreaseIndent();

        // st    reg, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, reg, "[%o1]");
        this.decreaseIndent();

        // set a.getoffset, %o1
        this.increaseIndent();        
        this.writeAssembly(TWO_PARAM, SET_OP, a.getOffset(), "%o1");
        this.decreaseIndent();

        // add a.base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, a.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        // for array struct ref
        if(a.getArrayTag() || a.getStructTag() || a.flag){
            //ld    [%o1], %o1
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o1]", "%o1");
            this.decreaseIndent();
        }


        // st   %f2, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%f2", "[%o1]");
        this.decreaseIndent();
       

    }


    // -------------------------------------------------------------------
    // This handles +/-, and bitwise, called in DoBinaryInt
    // -------------------------------------------------------------------
    public void DoPrimary(String op, String src1, String src2, String dist){
        
        // +/- reg, reg2, reg
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, op, src1, src2, dist);
        this.decreaseIndent();
    }

    // -------------------------------------------------------------------
    // This handles cmp
    // -------------------------------------------------------------------
    public void DoCmp(String op, String label){
         // cmp  %o0, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM,  CMP_OP, "%o0", "%o1");
        this.decreaseIndent();

        // op   label
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, op, label);
        this.decreaseIndent();

        // mov   %g0, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP, "%g0", "%o0");
        this.decreaseIndent();

        // inc   %o0
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, INC_OP, "%o0");
        this.decreaseIndent();

        // label:
        this.writeAssembly(NO_PARAM, label+":");

    
    }

    //--------------------------------------------------------------------
    // This handles cmp for bool
    //--------------------------------------------------------------------
    public void DoCmpBool(String op, String label) {
        //cmp  %o0, %g0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP, "%o0", "%g0");
        this.decreaseIndent();

        // op  label
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, op, label);
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();


    }
    // -------------------------------------------------------------------
    //
    // -------------------------------------------------------------------
    public void DoCmpFloat(String op, String label){
         // cmp  %f0, %f1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM,  FCMP_OP, "%f0", "%f1");
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        // op   label
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, op, label);
        this.decreaseIndent();

        // mov   %g0, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP, "%g0", "%o0");
        this.decreaseIndent();

        // inc   %o0
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, INC_OP, "%o0");
        this.decreaseIndent();

        // label:
        this.writeAssembly(NO_PARAM, label+":");

    
    }

    // -------------------------------------------------------------------
    // This handles *, /, %, called in DoBinaryInt
    // -------------------------------------------------------------------
    public void DoSecondary(String op){
        
        // call  *,/,%
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, op);
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        // mov %o0, %o0  (but why?)
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP, "%o0", "%o0");
        this.decreaseIndent();   

    }


    // ----------------------------------------------------------------
    // This handles the if case with only literal as conditon
    // ----------------------------------------------------------------
    public void DoIfLitCond(STO sto){


        int val = ((ConstSTO)sto).getBoolValue() ? 1: 0;

        this.writeAssembly(NEWLINE);
        //! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! if("+sto.getName()+")");
        this.decreaseIndent();

        // set    val  %o0 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(val), "%o0");
        this.decreaseIndent();

        // cmp     %o0, %g0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP, "%o0", "%g0");
        this.decreaseIndent();   


        // be      .$$.else.# 
        this.increaseIndent();
        endIfCnt++;
        this.writeAssembly(ONE_PARAM, BE_OP, DOLLAR+"else."+ String.valueOf(endIfCnt));
        blabel.push(endIfCnt);
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();   

        //pure formatting indentation
        this.increaseIndent();
			 
    }

    // ----------------------------------------------------------------
    // This handles the while case with only literal as conditon
    // ----------------------------------------------------------------
    public void DoWhileLitCond(STO sto){


        int val = ((ConstSTO)sto).getBoolValue() ? 1: 0;

        this.writeAssembly(NEWLINE);

        //! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! Check loop condition");
        this.decreaseIndent();

        // set    val  %o0 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(val), "%o0");
        this.decreaseIndent();

        // cmp     %o0, %g0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP, "%o0", "%g0");
        this.decreaseIndent();   


        // be      .$$.loopCheck.# 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BE_OP, DOLLAR+"loopCheck."+ String.valueOf(loopCnt));
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();   

        //pure formatting indentation
        this.increaseIndent();
			 
    }

    
    // ----------------------------------------------------------------
    // This handles the if case with only literal as conditon
    // ----------------------------------------------------------------

    public void DoIfExprCond(STO sto){

        this.writeAssembly(NEWLINE);

        //! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! if("+sto.getName()+")");
        this.decreaseIndent();

        // set    offset  %l7 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%l7");
        this.decreaseIndent();

        //add     base, %l7, %l7
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), "%l7", "%l7");
        this.decreaseIndent();

        // ld      [%l7], %o0    (This was %g0 before, should be wrong, don't know it's not caught -- change to %o0 in Nov.13 12:21)
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%o0");
        this.decreaseIndent(); 

        // cmp     %o0, %g0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP, "%o0", "%g0");
        this.decreaseIndent();   


        // be      .$$.else.# 
        this.increaseIndent();
        endIfCnt++;
        this.writeAssembly(ONE_PARAM, BE_OP, DOLLAR+"else."+ String.valueOf(endIfCnt));
        blabel.push(endIfCnt);
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent(); 

        //pure formatting indentation
        this.increaseIndent();
    }

    // ----------------------------------------------------------------
    // This handles the foreach
    // ----------------------------------------------------------------
    public void DoForEach(STO expr, STO sto, String s, STO theFuture){

        this.writeAssembly(NEWLINE);

        //! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! foreach ( ... )");
        this.decreaseIndent();
        
        //! traversal ptr = --array 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! traversal ptr = --array");
        this.decreaseIndent();

        //set  offset, %o0 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, expr.getOffset(), "%o0");
        this.decreaseIndent();

        //add base, %o0, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, expr.getBase(), "%o0", "%o0");
        this.decreaseIndent();

        // changed 11/29 from flag to isStructTag -- changed 11/30 from sto to expr
        if(expr.getStructTag()){
            this.load(o0, o0);

        }
        //set 4, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(sto.getType().getSize()), "%o1");
        this.decreaseIndent();
        
        //sub %o0, %o1, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, SUB_OP, "%o0", "%o1", "%o0");
        this.decreaseIndent();

        //set future offset, %o1 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, theFuture.getOffset(), "%o1");
        this.decreaseIndent();

        //add future base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, theFuture.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        //st  %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();

        // .$$.loopCheck.1:
        this.writeAssembly(NO_PARAM, DOLLAR+"loopCheck."+String.valueOf(++loopCnt)+":");
        wlabel.push(loopCnt);

        //! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! ++traversal ptr");
        this.decreaseIndent();

        //set future offset, "%o1"
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, theFuture.getOffset(), "%o1");
        this.decreaseIndent();

        //add future base, "%o1", %o1"
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, theFuture.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        //ld [%o1], %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o1]", "%o0");
        this.decreaseIndent();

        // set 4, %o2 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP,String.valueOf(sto.getType().getSize()), "%o2");
        this.decreaseIndent();

        //add %o0, %o2, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, "%o0", "%o2", "%o0");
        this.decreaseIndent();

        //st %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();


        //! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! traversal ptr < array end addr");
        this.decreaseIndent();

        //set array offset, %o1 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, expr.getOffset(), "%o0");
        this.decreaseIndent();

        //add array base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, expr.getBase(), "%o0", "%o0");
        this.decreaseIndent();


        // changed 11/29 from flag to isStructTag -- 11/30 from sto to expr
        if(expr.getStructTag()){
            this.load(o0, o0);
        }

        //set #, %o1
        int i = ((ArrayType)expr.getType()).getTotalSize();

        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(i), "%o1");
        this.decreaseIndent();

        //add %o0, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, "%o0", "%o1", "%o1");
        this.decreaseIndent();

        //set future offset, %o0 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, theFuture.getOffset(), "%o0");
        this.decreaseIndent();

        //add future base, %o0, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, theFuture.getBase(), "%o0", "%o0");
        this.decreaseIndent();

        //ld [%o0], %o0 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%o0");
        this.decreaseIndent();

        //cmp %o0, %o1 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP, "%o0", "%o1");
        this.decreaseIndent();


        //bge .$$.loopEnd.1
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BGE_OP, DOLLAR+"loopEnd."+String.valueOf(loopCnt));
        this.decreaseIndent();

        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        //! iterVar = currentElem
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! iterVar = currentElem");
        this.decreaseIndent();
        
        
        //set sto offset, %o1 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%o1");
        this.decreaseIndent();

        //add future base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        if(sto.flag == true){
           if(sto.getType() instanceof FloatType){

              //st [%o0], %o1
              this.increaseIndent();
              this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
              this.decreaseIndent();

           }
           else{
              //st %o0, [%o1]
              this.increaseIndent();
              this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
              this.decreaseIndent();
           }

        }
        else{

           if(sto.getType() instanceof FloatType){
              //ld [%o0], %f0
              this.increaseIndent();
              this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%f0");
              this.decreaseIndent();
              //st [%f0], %o1
              this.increaseIndent();
              this.writeAssembly(TWO_PARAM, STORE_OP, "%f0", "[%o1]");
              this.decreaseIndent();

           }
           else{
              //ld [%o0], %o0
              this.increaseIndent();
              this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%o0");
              this.decreaseIndent();

              //st %o0, [%o1]
              this.increaseIndent();
              this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
              this.decreaseIndent();
           }
        }

        // Start of loop body
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! Start of loop body");
        this.decreaseIndent();

        // pure indent
        this.increaseIndent();
 
    }


    // ----------------------------------------------------------------
    // This handles the while case with only literal as conditon
    // ----------------------------------------------------------------

    public void DoWhileOpenLoop(){

        this.writeAssembly(NEWLINE);

        //! comment 
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! while( ... )");
        this.decreaseIndent();

        // .$$. loopCheck.#.:
        loopCnt++;
        wlabel.push(loopCnt);
        this.writeAssembly(NO_PARAM, DOLLAR+"loopCheck."+String.valueOf(loopCnt)+":");

    }

    public void DoWhileExprCond(STO sto){

        this.writeAssembly(NEWLINE);

        //! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! Check loop condition");
        this.decreaseIndent();


        // set    offset  %l7 
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%l7");
        this.decreaseIndent();

        //add     base, %l7, %l7
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), "%l7", "%l7");
        this.decreaseIndent();

        // ld      [%l7], %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%o0");
        this.decreaseIndent(); 

        // cmp     %o0, %g0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, CMP_OP, "%o0", "%g0");
        this.decreaseIndent();   


        // be      .$$.loopEnd.# 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BE_OP, DOLLAR+"loopEnd."+ String.valueOf(loopCnt));
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent(); 

        //pure formatting indentation
        this.increaseIndent();

        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! Start of loop body");
        this.decreaseIndent();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------
    public void DoWhileCloseLoop(){

        int val = wlabel.pop();

        this.writeAssembly(NEWLINE);

        // for pure formatting
        this.decreaseIndent();

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! End of loop body" );
        this.decreaseIndent();

        // ba     .$$.loopCheck.#
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BA_OP, DOLLAR+ "loopCheck."+String.valueOf(val));
        this.decreaseIndent();

        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        //.$$.loopEnd.#:
        this.writeAssembly(NO_PARAM, DOLLAR+ "loopEnd."+String.valueOf(val)+":");



    }

    // --------------------------------------------------------------
    // handles break statement
    // --------------------------------------------------------------
    public void DoBreak(){

        int val = wlabel.peek();

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! break" );
        this.decreaseIndent();

        // ba     .$$.loopEnd.#
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BA_OP, DOLLAR+ "loopEnd."+String.valueOf(val));
        this.decreaseIndent();

        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

    }

    // --------------------------------------------------------------
    // handles continue statement
    // --------------------------------------------------------------
    public void DoContinue(){

        int val = wlabel.peek();

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! continue" );
        this.decreaseIndent();

        // ba     .$$.loopCheck.#
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BA_OP, DOLLAR+ "loopCheck."+String.valueOf(val));
        this.decreaseIndent();

        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

    }

    // ------------------------------------
    // handles the else
    // ------------------------------------
    public void DoElse(){

        // get the counter
        int val = blabel.peek();

        this.writeAssembly(NEWLINE);

        this.decreaseIndent();

        // ba        .$$.endif.#
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, BA_OP, DOLLAR+"endif."+String.valueOf(val));
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent(); 

        //pure formatting indentation
        this.decreaseIndent();

        this.writeAssembly(NEWLINE);


        // ! else
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! else");
        this.decreaseIndent(); 

        // .$$.else.#:
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, DOLLAR+"else."+String.valueOf(val) + ":");
        this.decreaseIndent(); 


    }

    // ------------------------------------
    // handles the end if
    // ------------------------------------
    public void DoEndIf(){

        //pop the counter
        int val = blabel.pop();

        this.writeAssembly(NEWLINE);


        // ! endif
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! endif");
        this.decreaseIndent(); 

        // .$$.endif.#:
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, DOLLAR+"endif."+String.valueOf(val) + ":");
        this.decreaseIndent();

        this.increaseIndent();
    
    }

    //------------------------------------------
    // handles func call with no params
    //------------------------------------------
    public void DoFuncCallNoParamVoid(STO sto){
    
        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! " + sto.getName()+"(...)"); 
        this.decreaseIndent();

        //call foo.void
        this.increaseIndent();
        // check if func in struct
        if(sto.getStructTag()){
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getStructName()+"."+sto.getName()+"."+((FuncSTO)sto).getAssemblyName()); 
        }
        else if (((FuncSTO)sto).getIsExtern()) {
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getName());
        }
        else{
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getName()+"."+((FuncSTO)sto).getAssemblyName()); 
        }
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP); 
        this.decreaseIndent();

    }




    //------------------------------------------
    // handles func call with no params
    //------------------------------------------
    public void DoFuncCallNoParam(STO sto, STO func){
    
        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! " + sto.getName()+"(...)"); 
        this.decreaseIndent();

        //call foo.void
        this.increaseIndent();
        // check if func is in struct
        if(func.getStructTag()){
            this.writeAssembly(ONE_PARAM, CALL_OP, func.getStructName()+"."+sto.getName()+"."+((FuncSTO)func).getAssemblyName()); 
        }
        else if (((FuncSTO)func).getIsExtern()) {
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getName());
        }
        else{
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getName()+"."+((FuncSTO)func).getAssemblyName());
        }

        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP); 
        this.decreaseIndent();

        // set offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%o1"); 
        this.decreaseIndent();


        //add base %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), "%o1", "%o1"); 
        this.decreaseIndent();


        if(sto.getType() instanceof FloatType && !(func.flag)) {
           // st %o0, [%o1]
           this.increaseIndent();
           this.writeAssembly(TWO_PARAM, STORE_OP, "%f0", "[%o1]"); 
           this.decreaseIndent();
        }
        else {
           // st %o0, [%o1]
           this.increaseIndent();
           this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]"); 
           this.decreaseIndent();
        
        }
    }

    //------------------------------------------
    // handles func call with params
    //------------------------------------------
    public int DoFuncCallParam(STO sto, STO func, Vector<STO> valuelist, int offset){
    
        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! " + func.getName()+"(...)"); 
        this.decreaseIndent();

        Vector<STO> paramlist = ((FuncSTO)func).getParams();

        int reg = 0;

        if((func.getType() instanceof StructType)) {

            reg = 1;
        }

        for(int i = 0; i < paramlist.size(); i++){
            STO param = paramlist.get(i);
            Type t = paramlist.get(i).getType();
            STO value = valuelist.get(i);
            if(value instanceof ConstSTO && !((ConstSTO)value).getLitTag()){
                         
                ConstSTO con = (ConstSTO)value;
                int val =0; 
                float valf = 0;
                
                if(t instanceof IntType){
                    val = con.getIntValue();
                     // ! comment
                    this.increaseIndent();
                    this.writeAssembly(NO_PARAM, "! "+param.getName()+" <- "+String.valueOf(val)); 
                    this.decreaseIndent();

                    // ! set  # %o1
                    this.increaseIndent();
                    this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(val), "%o"+String.valueOf(reg));
                    this.decreaseIndent();


                }
                else if (t instanceof FloatType) {
                    valf = con.getFloatValue();
                     // ! comment
                    this.increaseIndent();
                    this.writeAssembly(NO_PARAM, "! "+param.getName()+"<-"+String.valueOf(valf)); 
                    this.decreaseIndent();


                    // Type promotion for int
                    if(value.getType() instanceof IntType){
                        
                        val = con.getIntValue();

                        //set val, %o#
                        this.increaseIndent();
                        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(val), "%o"+String.valueOf(reg));
                        this.decreaseIndent();

                        STO promote = new ExprSTO("promote");
                        offset ++;
                        promote.setBase("%fp");
                        promote.setOffset(String.valueOf(-offset*4));

                        this.DoTypePromotion(promote, "%f"+String.valueOf(reg), "%o"+String.valueOf(reg));

                    }
                    else{
                        // rodata for float 
                        this.DoFloatRoData(value,"%f"+String.valueOf(reg));
                    } 
                    // ! set  # %o1
                   // this.increaseIndent();
                   // this.writeAssembly(TWO_PARAM, SET_OP, DOLLAR + "float." + Integer.toString(constFloatCnt), "%f"+String.valueOf(i));
                    //this.decreaseIndent();

                }
                else {
                   val = con.getBoolValue() ? 1 : 0;
                    // ! comment
                    this.increaseIndent();
                    this.writeAssembly(NO_PARAM, "! "+param.getName()+"<-"+String.valueOf(val)); 
                    this.decreaseIndent();

                    // ! set  # %o1
                    this.increaseIndent();
                    this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(val), "%o"+String.valueOf(reg));
                    this.decreaseIndent();

                }
            
            }
            // var case
            else{
                    // ! comment
                    this.increaseIndent();
                    this.writeAssembly(NO_PARAM, "! "+param.getName()+"<-"+value.getName()); 
                    this.decreaseIndent();

                    // pass by value for non lit
                    
                    
                    
                    if(param.flag == false){
                       // set  offset %l7
                       this.increaseIndent();
                       this.writeAssembly(TWO_PARAM, SET_OP, value.getOffset(), "%l7");
                       this.decreaseIndent();

                       // add base %l7, %l7
                       this.increaseIndent();
                       this.writeAssembly(THREE_PARAM, ADD_OP, value.getBase(), "%l7", "%l7");
                       this.decreaseIndent();

                       // ld [%l7] %l7 ---- there is one case that's been needed 11/17 for array and ref
                       if(value.getArrayTag() || value.flag || value.getIsPointer()){
                           this.increaseIndent();
                           this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%l7");
                           this.decreaseIndent();
                       }

                       // ld [%l7], %o1
                       this.increaseIndent();
                       if(param.getType() instanceof FloatType) {
                          if(value.getType() instanceof IntType){
                             // Type promotion
                             this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%o"+String.valueOf(reg));
                             
                             STO promote = new ExprSTO("promote");
                             offset ++;
                             promote.setBase("%fp");
                             promote.setOffset(String.valueOf(-offset*4));

                             this.decreaseIndent(); 
                             this.DoTypePromotion(promote, "%f"+String.valueOf(reg), "%o"+String.valueOf(reg));
                             this.increaseIndent();
                          }
                          else{
                             this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%f"+String.valueOf(reg));
                          }

                       }
                       else {
                          
                          this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%o"+String.valueOf(reg));
                          
                       }
                       this.decreaseIndent();
                    }
                    // pass by reference
                    else{
                      
                       // set  offset %o1
                       this.increaseIndent();
                       this.writeAssembly(TWO_PARAM, SET_OP, value.getOffset(), "%o"+String.valueOf(reg));
                       this.decreaseIndent();

                       // add base %o1, %o1
                       this.increaseIndent();
                       this.writeAssembly(THREE_PARAM, ADD_OP, value.getBase(), "%o"+String.valueOf(reg), "%o"+String.valueOf(reg));
                       this.decreaseIndent();

                     
                    

                       // ADDED if arg passed in to function param is a ref
                       // 11/29 added
                       if(value.flag == true || value.getStructTag() || value.getArrayTag() || value.getIsPointer()) {
                          // ld [%l7], %o1
                          this.increaseIndent();
                          this.writeAssembly(TWO_PARAM, LOAD_OP,  "["+"%o"+String.valueOf(reg)+"]", "%o"+String.valueOf(reg));
                          this.decreaseIndent();

                       }


                    }

            }
            reg++;
            
            
        }

        //call foo.param
        this.increaseIndent();
        if (((FuncSTO)func).getIsExtern()) {
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getName());
        }
        else if(!(func.getStructTag())) {
           this.writeAssembly(ONE_PARAM, CALL_OP, sto.getName()+"."+((FuncSTO)func).getAssemblyName()); 
        }
        else { // note change --11/17
           this.writeAssembly(ONE_PARAM, CALL_OP, func.getStructName()+"."+func.getName()+"."+func.getAssemblyName()); 

        }
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP); 
        this.decreaseIndent();

        if( !(func.getType() instanceof VoidType) && !(func.getType() instanceof StructType) ) {
           // set offset, %o1
           this.increaseIndent();
           this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%o1"); 
           this.decreaseIndent();


           //add base %o1, %o1
           this.increaseIndent();
           this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(), "%o1", "%o1"); 
           this.decreaseIndent();



            // st %o0, [%o1]
           this.increaseIndent();
           if(sto.getType() instanceof FloatType && sto.flag==false) {   
              this.writeAssembly(TWO_PARAM, STORE_OP, "%f0", "[%o1]"); 
           }
           else {
              this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]"); 
           }
           this.decreaseIndent();

        
        }
        return offset;
        
      }

    // --------------------------------------------
    // handles func with params
    // --------------------------------------------
    public void DoParams(STO sto){
      
        Vector<STO> paramlist = ((FuncSTO)sto).getParams();
        
        // for struct ctor
        int reg = 0;
        if(sto.getType() instanceof StructType) {
           reg = 1;
        }

        for(int i = 0; i < paramlist.size(); i++){
            // st     reg, [%fp+68]
            String base = paramlist.get(i).getBase();
            String offset = paramlist.get(i).getOffset();
            this.increaseIndent();

            // float case uses %f
            if(paramlist.get(i).getType() instanceof FloatType && paramlist.get(i).flag == false) {
                this.writeAssembly(TWO_PARAM, STORE_OP, "%f"+String.valueOf(reg), "["+base+"+"+offset+"]");
   
            }
            else { // other
               this.writeAssembly(TWO_PARAM, STORE_OP, "%i"+String.valueOf(reg), "["+base+"+"+offset+"]");
            }
            this.decreaseIndent();
            reg++;
        }

    }

    public void DoThisParam() {
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%i"+String.valueOf(0), "[" + "%fp" + "+" + "68" + "]");
        this.decreaseIndent();

    }

    // --------------------------------------------
    // handles func void return
    // --------------------------------------------
    public void DoReturnVoid(STO sto){
        
        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! return;"); 
        this.decreaseIndent();

        //call name.type.fini
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, sto.getName()+"."+((FuncSTO)sto).getAssemblyName()+".fini"); 
        this.decreaseIndent();


        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP); 
        this.decreaseIndent();


        // ret
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RET_OP); 
        this.decreaseIndent();
;
        // restore
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, RESTORE_OP);
        this.decreaseIndent();

    }

    // --------------------------------------------
    // handles func Lit return
    // --------------------------------------------
    public void DoReturnLit(STO sto, String expr, STO lit, STO promote){

        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! return " + expr); 
        this.decreaseIndent();

        
        
        
        if(lit.getType() instanceof FloatType) {
            
            this.DoFloatRoData(lit,"%f0");
        
        }
        else {
          if(promote != null) {
              //set  #, "%i0"
              this.increaseIndent();
              this.writeAssembly(TWO_PARAM, SET_OP, expr, "%i0");
              this.decreaseIndent();
 
              this.DoTypePromotion(promote, "%f0", "%i0");

           }
           else{     
              //set  #, "%i0"
              this.increaseIndent();
              this.writeAssembly(TWO_PARAM, SET_OP, expr, "%i0");
              this.decreaseIndent();
           }
        }
        

        //call name.param.fini
        this.increaseIndent();
        //check if func in struct
        if(sto.getStructTag()){
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getStructName()+"."+sto.getName()+"."+((FuncSTO)sto).getAssemblyName()+".fini");
        }
        else{
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getName()+"."+((FuncSTO)sto).getAssemblyName()+".fini");
        }
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

    // --------------------------------------------
    // handles func int/float/bool return
    // --------------------------------------------
    public void DoReturnNonVoid(STO sto, STO expr, STO promote){
        
        this.writeAssembly(NEWLINE);

        String reg = "%l7";
        if(sto.flag == true){
            reg = "%i0";
        }

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! return "+ expr.getName()); 
        this.decreaseIndent();

        // set  offset, %l7/%i0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, expr.getOffset(), reg); 
        this.decreaseIndent();

        // add  base, %l7/%i0,%i0/%l7 
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, expr.getBase(), reg, reg);
        this.decreaseIndent();

       
        if(expr.getIsPointer() || expr.flag || expr.getArrayTag() || expr.getStructTag()) {
              this.increaseIndent();
              this.writeAssembly(TWO_PARAM, LOAD_OP, "["+reg+"]", reg);
              this.decreaseIndent();
            
        }


        if(!sto.flag) {
                // ld   [%l7], %i0
           this.increaseIndent();
           if(expr.getType() instanceof FloatType) {
               // float case
              /*if(expr.flag || expr.getArrayTag()) {
                     this.load(reg,reg);
              }*/

              this.writeAssembly(TWO_PARAM, LOAD_OP, "["+reg+"]", "%f0"); 
           }
           else {
              // Type Promotion int to float
              if(promote != null){
                  this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%i0"); 
                
                  this.decreaseIndent();
                  this.DoTypePromotion(promote, "%f0", "%i0");
                  this.increaseIndent();
              }
              else{
                  // non float case
                  /*if(expr.flag || expr.getArrayTag()) {
                     this.load(reg,reg);
                  }*/
                  this.writeAssembly(TWO_PARAM, LOAD_OP, "["+reg+"]", "%i0");
              }
           }
           this.decreaseIndent();
        
        }
        /*else {
            if(expr.flag) {

               this.increaseIndent();
               this.writeAssembly(TWO_PARAM, LOAD_OP, "[%i0]", "%i0"); 
               this.decreaseIndent();
            }
        
        }*/

        //call name.type.fini
        this.increaseIndent();
        //check if fun in struct
        if(sto.getStructTag()){
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getStructName()+"."+sto.getName()+"."+((FuncSTO)sto).getAssemblyName()+".fini");
        }
        else{
            this.writeAssembly(ONE_PARAM, CALL_OP, sto.getName()+"."+((FuncSTO)sto).getAssemblyName()+".fini");
        }
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

    // -----------------------------------------------------------------------------------
    //  This handles cin
    // -----------------------------------------------------------------------------------
    public void DoCin(STO expr){
        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! cin >>"+ expr.getName()); 
        this.decreaseIndent();

        //call inputInt or inputFloat
        this.increaseIndent();
        if(expr.getType() instanceof IntType){
            this.writeAssembly(ONE_PARAM, CALL_OP, "inputInt"); 
        }
        else{
            this.writeAssembly(ONE_PARAM, CALL_OP, "inputFloat");
        }
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        // set  offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, expr.getOffset(), "%o1"); 
        this.decreaseIndent();

        // add  base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, expr.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        if(expr.getStructTag() || expr.getArrayTag() || expr.flag || expr.getIsPointer()) {
           this.load(o1,o1);
        }

        // st   %o0/%f0, [%o1]
        this.increaseIndent();
        if(expr.getType() instanceof IntType){
            this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]"); 
        }
        else{
            this.writeAssembly(TWO_PARAM, STORE_OP, "%f0", "[%o1]");
        }
        this.decreaseIndent();

    }

    // --------------------------------------------------------------------------------
    // this handles pointer dereference
    // --------------------------------------------------------------------------------
    public void DoDereference(STO sto, STO result){
        
        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! *"+result.getName());
        this.decreaseIndent();

        //set sto.offset, %l7
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%l7");
        this.decreaseIndent();

        //add sto.base, %l7, %l7
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(),"%l7", "%l7");
        this.decreaseIndent();

        if(sto.getArrayTag() || sto.flag || sto.getStructTag() || sto.getIsPointer()){
            // ld [%l7], %l7
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%l7");
            this.decreaseIndent();

        }

        //ld  [%l7], [%o0]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%o0");
        this.decreaseIndent();

 
        // call  .$$.ptrCheck
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, DOLLAR+"ptrCheck");
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();


        //set result.offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, result.getOffset(), "%o1");
        this.decreaseIndent();

        //add result.base, "%o1", "%o1"
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, result.getBase(), "%o1", "%o1");
        this.decreaseIndent();

        //st %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();

    
    }

    // -------------------------------------------------------------
    // This handles addressOf
    // -------------------------------------------------------------
    public void DoAddress(STO sto, STO result){
        
        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! "+result.getName());
        this.decreaseIndent();

        //set sto.offset, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%o0");
        this.decreaseIndent();

        //add sto.base, %o0, %o0
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(),"%o0", "%o0");
        this.decreaseIndent();

        if(sto.getArrayTag() || sto.flag || sto.getIsPointer() ){
            // ld [%o0] %o0
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%o0");
            this.decreaseIndent();
        }

        //set result.offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, result.getOffset(), "%o1");
        this.decreaseIndent();

        //add result.base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, result.getBase(),"%o1", "%o1");
        this.decreaseIndent();


        //st  %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();

    }

    // ------------------------------------------------------------
    // This handles new
    // ------------------------------------------------------------
    public void DoNew(STO sto){

        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! new( "+sto.getName() + " )");
        this.decreaseIndent();

        // mov 1, %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, MOV_OP, "1", "%o0");
        this.decreaseIndent();

        //set sto.base.size, %o1
        this.increaseIndent();
        int i = ((PointerType)sto.getType()).getBaseType().getSize();
        this.writeAssembly(TWO_PARAM, SET_OP, String.valueOf(i), "%o1");
        this.decreaseIndent();

        //call  calloc
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, "calloc");
        this.decreaseIndent();

        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();

        //set sto.offset, %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, sto.getOffset(), "%o1");
        this.decreaseIndent();

        //add sto.base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, sto.getBase(),"%o1", "%o1");
        this.decreaseIndent();

        // added 11/21 sto.getIsPointer() and sto.flag
        if(sto.getArrayTag() || sto.getIsPointer() || sto.flag){ 
            //ld [%o1], %o1
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o1]", "%o1");
            this.decreaseIndent();

        }


        //st  %o0, [%o1]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o0", "[%o1]");
        this.decreaseIndent();

    }

    public void DoDelete(STO deleted) {

        this.writeAssembly(NEWLINE);

        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! delete( "+deleted.getName() + " )");
        this.decreaseIndent();


        //set deleted.offset, %l7
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, deleted.getOffset(), "%l7");
        this.decreaseIndent();

        //add sto.base, %l7, %l7
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, deleted.getBase(),"%l7", "%l7");
        this.decreaseIndent();

        // added 11/21 sto.getIsPointer() and sto.flag
        if(deleted.getArrayTag() || deleted.getIsPointer() || deleted.flag){ 
            //ld [%o1], %o1
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%l7");
            this.decreaseIndent();

        }

        //ld [%l7] %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]","%o0");
        this.decreaseIndent();

        //call  .$$.ptrCheck
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP, DOLLAR + "ptrCheck");
        this.decreaseIndent();


        // nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();



        //set deleted.offset, %l7
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, deleted.getOffset(), "%l7");
        this.decreaseIndent();

        //add sto.base, %l7, %l7
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, deleted.getBase(),"%l7", "%l7");
        this.decreaseIndent();

        // added 11/21 sto.getIsPointer() and sto.flag
        if(deleted.getArrayTag() || deleted.getIsPointer() || deleted.flag){ 
            //ld [%o1], %o1
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]", "%l7");
            this.decreaseIndent();

        }

        //ld [%l7] %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, LOAD_OP, "[%l7]","%o0");
        this.decreaseIndent();


        //call  free
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, CALL_OP,"free");
        this.decreaseIndent();


        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();


        //set deleted.offset, %l7
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, deleted.getOffset(), "%o1");
        this.decreaseIndent();

        //add sto.base, %l7, %l7
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, deleted.getBase(),"%o1", "%o1");
        this.decreaseIndent();


        if(deleted.getArrayTag() || deleted.getIsPointer() || deleted.flag){ 
            //ld [%o1], %o1
            this.increaseIndent();
            this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o1]", "%o1");
            this.decreaseIndent();

        }



        //st [%l7] %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%g0","[%o1]");
        this.decreaseIndent();

    }


    public void DoDtorHeader(STO dtor, STO offset) {

        this.writeAssembly(NEWLINE);
        ctordtor++;

        if(!dtor.getIsGlobal()) {
          Dtors.push(dtor);
          this.dtorFlag = true;
          numDtors.push(ctordtor);

        }
        else {
          globalsDtors.push(dtor);
          numGlobalsDtors.push(ctordtor);
        }

        String label = DOLLAR + "ctorDtor"+ "." + String.valueOf(ctordtor);

        // .section .bss
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".bss\"");
        this.decreaseIndent();

        // .align  4 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();


        // DTOR name:
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, label+":");
        this.decreaseIndent();

        // .skip  4 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SKIP, String.valueOf(4));
        this.decreaseIndent();


        // .section .text
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
        this.decreaseIndent();


        // .align  4 
        this.increaseIndent();
        this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
        this.decreaseIndent();


        this.writeAssembly(NEWLINE);

        //set  dtor label %o0
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, label, "%o0");
        this.decreaseIndent();

        //set offset,  %o1
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, SET_OP, offset.getOffset(), "%o1");
        this.decreaseIndent();

        //add , base, %o1, %o1
        this.increaseIndent();
        this.writeAssembly(THREE_PARAM, ADD_OP, offset.getBase(),"%o1", "%o1");
        this.decreaseIndent();

        if(offset.getArrayTag()) {
           this.load(o1,o1);
        }

        //st %o1 [%o0]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%o1","[%o0]");
        this.decreaseIndent();
       
    }

    public void DoDtorLocalCall() {

       if(Dtors.isEmpty()) {
          //ret
          this.increaseIndent();
          this.writeAssembly(NO_PARAM, RET_OP);
          this.decreaseIndent();

          //restore
          this.increaseIndent();
          this.writeAssembly(NO_PARAM, RESTORE_OP);
          this.decreaseIndent();
       }

       while(!Dtors.isEmpty()) {
          STO dtor = Dtors.pop();
          int num = numDtors.pop();
          this.DoDtorAseembly(dtor,num);
       
       }
    
    }

    public void DoDtorCallGlobal() {
 		
 	   this.RetRestore();

       while(!globalsDtors.isEmpty()) {
          STO dtor = globalsDtors.pop();
          int num = numGlobalsDtors.pop();
          this.DoDtorAseembly(dtor,num);
       
       }
   }

    public void DoDtorAseembly(STO dtor, int num) {

	  String label = DOLLAR + "ctorDtor"+ "." + String.valueOf(num);
	  String first = dtor.getName().replace("~","");
	  String second = dtor.getName().replace("~","$");

	  if(dtor.getIsGlobal()) {

         this.writeAssembly(NO_PARAM, label+".fini:");

         this.increaseIndent();
         this.writeAssembly(THREE_PARAM,SAVE_OP ,"%sp","-96","%sp");
         this.decreaseIndent();

      }

	  //set  dtor label %o0
      this.increaseIndent();
      this.writeAssembly(TWO_PARAM, SET_OP, label, "%o0");
      this.decreaseIndent();

      //ld  [%o0] %o0
      this.increaseIndent();
      this.writeAssembly(TWO_PARAM, LOAD_OP, "[%o0]", "%o0");
      this.decreaseIndent();

      //cmp  %o0 %g0
      this.increaseIndent();
      this.writeAssembly(TWO_PARAM, CMP_OP, "%o0", "%g0");
      this.decreaseIndent();

      //be  label.fini.skip
      this.increaseIndent();
      this.writeAssembly(ONE_PARAM, BE_OP, label+".fini"+".skip");
      this.decreaseIndent();

      //nop
      this.increaseIndent();
      this.writeAssembly(NO_PARAM, NOP_OP);
      this.decreaseIndent();

      //call  dtor
      this.increaseIndent();
      this.writeAssembly(ONE_PARAM, CALL_OP, first+"."+second+"."+((FuncSTO)dtor).getAssemblyName());
      this.decreaseIndent();

      //nop
      this.increaseIndent();
      this.writeAssembly(NO_PARAM, NOP_OP);
      this.decreaseIndent();

      //set  dtor label %o0
      this.increaseIndent();
      this.writeAssembly(TWO_PARAM, SET_OP, label, "%o0");
      this.decreaseIndent();

      //st %o1 [%o0]
      this.increaseIndent();
      this.writeAssembly(TWO_PARAM, STORE_OP, "%g0","[%o0]");
      this.decreaseIndent();

      // label.fini.skip
      this.writeAssembly(NO_PARAM, label+".fini"+".skip:");


          if(dtor.getIsGlobal()) {

             //ret
             this.increaseIndent();
             this.writeAssembly(NO_PARAM, RET_OP);
             this.decreaseIndent();

             //restore
             this.increaseIndent();
             this.writeAssembly(NO_PARAM, RESTORE_OP);
             this.decreaseIndent();

             this.writeAssembly(NEWLINE);
             // .section .fini
             this.increaseIndent();
             this.writeAssembly(ONE_PARAM, SECTION, "\".fini\"");
             this.decreaseIndent();

             // .align  4 
             this.increaseIndent();
             this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
             this.decreaseIndent();

             //call  label.fini.skip
             this.increaseIndent();
             this.writeAssembly(ONE_PARAM, CALL_OP, label+".fini");
             this.decreaseIndent();
             
             //nop
             this.increaseIndent();
             this.writeAssembly(NO_PARAM, NOP_OP);
             this.decreaseIndent();

             this.writeAssembly(NEWLINE);

             // .section .text
             this.increaseIndent();
             this.writeAssembly(ONE_PARAM, SECTION, "\".text\"");
             this.decreaseIndent();

             // .align  4 
             this.increaseIndent();
             this.writeAssembly(ONE_PARAM, ALIGN, String.valueOf(4));
             this.decreaseIndent();
             
          }


    }

    public void DoDtorParam() {
        //st %i0 [%fp+68]
        this.increaseIndent();
        this.writeAssembly(TWO_PARAM, STORE_OP, "%i0","[%fp+68]");
        this.decreaseIndent();
    
    }

    public void RetRestore() {

         if(this.dtorFlag == true) {
    	   this.increaseIndent();
           this.writeAssembly(NO_PARAM, RET_OP);
           this.decreaseIndent();

           //restore
           this.increaseIndent();
           this.writeAssembly(NO_PARAM, RESTORE_OP);
           this.decreaseIndent();
         }
    }

    public void RetRestoreStruct() {

    	   this.increaseIndent();
           this.writeAssembly(NO_PARAM, RET_OP);
           this.decreaseIndent();

           //restore
           this.increaseIndent();
           this.writeAssembly(NO_PARAM, RESTORE_OP);
           this.decreaseIndent();
    }

    public void DoTypeCast(STO casted, Type cast, STO offset, STO promote) {

        if(casted.getType() instanceof FloatType && cast instanceof IntType) {
           this.DoIntToFloat(casted,cast,offset);
        }
        else if(casted.getType() instanceof BoolType && cast instanceof IntType) {
            this.DoIntAndBool(casted,cast,offset);
        }
        else if(casted.getType() instanceof IntType && cast instanceof BoolType){
            this.DoIntAndBool(casted,cast,offset);
        }
        else if (casted.getType() instanceof FloatType && cast instanceof BoolType) {
            this.DoBoolToFloat(casted,cast,offset,promote);
        }
        else if(casted.getType() instanceof BoolType && cast instanceof FloatType) {
           this.DoFloatToBool(casted,cast,offset,promote);
        }
        else if(casted.getType() instanceof IntType && cast instanceof FloatType) {
           this.DoFloatToInt(casted,cast,offset,promote);
        }
        else {
            this.DoSameAndPointer(casted,cast,offset);
        
        }
    
    }


    public void DoIntToFloat(STO casted,Type cast, STO offset) {
       
       this.writeAssembly(NEWLINE);
       // ! comment
       this.increaseIndent();
       this.writeAssembly(NO_PARAM, "! ( "+cast.getName() + " )" +casted.getName());
       this.decreaseIndent();

       if(casted instanceof ConstSTO && !(((ConstSTO)casted).getLitTag()) ) {
          // this.getVar(casted,l7);
          // int val = (int)(((ConstSTO)casted).getFloatValue());
          // this.setVal(String.valueOf(val),o0);
           //this.store(o0,o1);
       }
       else {

          // does set add
          this.getVar(casted,l7);
          this.load(l7,f0);
          this.fstoi(f0);
          // does set add
          this.getVar(offset,o1);
          this.store(f0,o1);
       
       }
        
        



    }

    public void DoFloatToInt(STO casted,Type cast,STO Offset,STO promote) {

        this.writeAssembly(NEWLINE);
        
        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! ( "+cast.getName() + " )" +casted.getName());
        this.decreaseIndent();
       
        if(casted instanceof ConstSTO && !(((ConstSTO)casted).getLitTag()) ) {
          // this.getVar(casted,l7);
          // this.DoFloatRoData(casted,f0);
           //this.store(f0,o1);
        }
        else {
           this.getVar(casted,l7);
           this.load(l7,o0);
           this.getVar(promote,l7);
           this.store(o0,l7);
           this.load(l7,f0);
           this.fitos(f0);
           this.getVar(Offset,o1);
           this.store(f0,o1);
        }




    }

    public void DoIntAndBool(STO casted,Type cast,STO offset) {
        
        String label = DOLLAR+"cmp."+String.valueOf(++cmpCnt);
        this.writeAssembly(NEWLINE);
        
        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! ( "+cast.getName() + " )" +casted.getName());
        this.decreaseIndent();

       if(casted instanceof ConstSTO && !(((ConstSTO)casted).getLitTag()) ) {
           /*if(casted.getType() instanceof BoolType && cast instanceof IntType) {
              this.getVar(casted,l7);
              boolean val = (((ConstSTO)casted).getBoolValue());
              int valint = val ? 1 : 0;
              this.setVal(String.valueOf(valint),o0);
              this.store(o0,o1);
           }
           else if(casted.getType() instanceof IntType && cast instanceof BoolType) {
              this.getVar(casted,l7);
              int val = (((ConstSTO)casted).getIntValue());
              if(val > 0) {val = 1;}
              else {val = 0;}
              this.setVal(String.valueOf(val),o0);
              this.store(o0,o1);
           }*/
       }
       else {
       

           this.getVar(casted,l7);
           this.load(l7,o0);
           this.compare(CMP_OP,o0,g0);
           this.branch(BE_OP,label);
           this.move(g0,o0);
           this.move(String.valueOf(1),o0);
           this.label(label+":");
           this.getVar(offset,o1);
           this.store(o0,o1); 
       }


    
    }

    public void DoBoolToFloat(STO casted,Type cast,STO offset,STO promote) {

        String label = DOLLAR+"cmp."+String.valueOf(++cmpCnt);
        this.writeAssembly(NEWLINE);
        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! ( "+cast.getName() + " )" +casted.getName());
        this.decreaseIndent();


       if(casted instanceof ConstSTO && !(((ConstSTO)casted).getLitTag()) ) {
           /*if(casted.getType() instanceof FloatType && cast instanceof BoolType) {
              this.getVar(casted,l7);
              float val = (((ConstSTO)casted).getFloatValue());
              if(val > 0) {val = 1;}
              else {val = 0;}
              this.setVal(String.valueOf(val),o0);
              this.store(o0,o1);
           }*/
       }
       else {
              this.getVar(casted,l7);
              this.load(l7,f0);
              this.getVar(promote,l7);
              this.store(g0,l7);
              this.load(l7,f1);
              this.fitos(f1);
              this.compare(FCMP_OP,f0,f1);
              this.nop();
              this.branch(FBE_OP,label);
              this.move(g0,o0);
              this.move(String.valueOf(1),o0);
              this.label(label+":");
              this.getVar(offset,o1);
              this.store(o0,o1);
       
       }



    
    }


    public void DoFloatToBool(STO casted,Type cast,STO offset, STO promote) {
        String label = DOLLAR+"cmp."+String.valueOf(++cmpCnt);
        this.writeAssembly(NEWLINE);
        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! ( "+cast.getName() + " )" +casted.getName());
        this.decreaseIndent();

        if(casted instanceof ConstSTO && !(((ConstSTO)casted).getLitTag()) ) {
           /*if(casted.getType() instanceof BoolType && cast instanceof FloatType) {
              this.getVar(casted,l7);
              this.DoFloatRoData(casted,f0);
              this.store(o0,o1);
           }*/
        }
        else {

           this.getVar(casted,l7);
           this.load(l7,o0);
           this.compare(CMP_OP,o0,g0);
           this.branch(BE_OP,label);
           this.move(g0,o0);
           this.move(String.valueOf(1),o0);
           this.label(label+":");
           this.getVar(promote,l7);
           this.store(o0,l7);
           this.load(l7,f0);
           this.fitos(f0);
           this.getVar(offset,o1);
           this.store(f0,o1);
        }
    }

    public void DoSameAndPointer(STO casted,Type cast,STO offset) {
        
        
        this.writeAssembly(NEWLINE);
        // ! comment
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, "! ( "+cast.getName() + " )" +casted.getName());
        this.decreaseIndent();

        if(casted instanceof ConstSTO && !(((ConstSTO)casted).getLitTag()) ) {
        }
        else {
           this.getVar(casted,l7);
           if(casted.getStructTag() || casted.getArrayTag() || casted.getIsPointer()) {
              this.load(l7,l7);
           }
           this.load(l7,o0);
           this.getVar(offset,o1);
           this.store(o0,o1);
        }


         
    }


    public void getVar( STO sto, String reg ) {
       this.increaseIndent();
       this.writeAssembly(TWO_PARAM,SET_OP,sto.getOffset(),reg);
       this.decreaseIndent();

       this.increaseIndent();
       this.writeAssembly(THREE_PARAM,ADD_OP,sto.getBase(),reg,reg);
       this.decreaseIndent();
    
    }

    public void setVal(String val, String reg) {
       this.increaseIndent();
       this.writeAssembly(TWO_PARAM,SET_OP,val,reg);
       this.decreaseIndent();

    }

    public void store(String reg1, String reg2) {
       this.increaseIndent();
       this.writeAssembly(TWO_PARAM,STORE_OP,reg1,"["+reg2+"]");
       this.decreaseIndent();
    
    }

    public void load(String reg1, String reg2) {
       this.increaseIndent();
       this.writeAssembly(TWO_PARAM,LOAD_OP ,"["+reg1+"]",reg2);
       this.decreaseIndent();

    }

    public void fstoi(String reg) {
       this.increaseIndent();
       this.writeAssembly(TWO_PARAM,FSTOI_OP, reg, reg);
       this.decreaseIndent();

    }

    public void fitos(String reg) {
       this.increaseIndent();
       this.writeAssembly(TWO_PARAM,FITOS_OP, reg, reg);
       this.decreaseIndent();

    }

    public void compare(String cmp, String reg1, String reg2) {

       //cmp  reg1 reg2
       this.increaseIndent();
       this.writeAssembly(TWO_PARAM, cmp, reg1, reg2);
       this.decreaseIndent(); 
    }


    public void branch(String bran, String label) {
       //branch  label
       this.increaseIndent();
       this.writeAssembly(ONE_PARAM, bran, label);
       this.decreaseIndent();
    }

    public void move(String val, String reg) {
       this.increaseIndent();
       this.writeAssembly(TWO_PARAM, MOV_OP, val,reg);
       this.decreaseIndent();
    }

    public void label(String label) {
        this.writeAssembly(NO_PARAM, label);
    }

    public void nop() {
        //nop
        this.increaseIndent();
        this.writeAssembly(NO_PARAM, NOP_OP);
        this.decreaseIndent();
    }


}

