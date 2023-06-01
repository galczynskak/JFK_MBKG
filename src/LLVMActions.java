import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

class Value {
    public String type;
    public String value;

    public Value(String type, String value) {
        this.type = type;
        this.value = value;
    }
}

public class LLVMActions extends MBKGBaseListener {
    HashMap<String, String> variables = new HashMap<String, String>();
    HashSet<String> types = new HashSet<String>() {{
        add("int");
        add("float");
    }};
    HashSet<String> definedFunctions = new HashSet<String>() {{
        add("print");
        add("scan");
    }};

    List<Value> argumentsList = new ArrayList<Value>();
    Stack<Value> stack = new Stack<Value>();

    @Override
    public void exitProgram(MBKGParser.ProgramContext ctx) {
        System.out.println(LLVMGenerator.generate());
    }

    @Override
    public void exitAssignment(MBKGParser.AssignmentContext ctx) {
        String ID;
        try {
            ID = ctx.ID().getText();
        } catch (NullPointerException e) {
            ID = ctx.declaration().getChild(1).getText();
        }
        if(!variables.containsKey(ID)){
            error(ctx.getStart().getLine(), "variable not declared");
        }

        Value v = stack.pop();
        System.out.println(v.type + ", " + v.value);
        if(!v.type.equals(variables.get(ID))) {
            error(ctx.getStart().getLine(), "assignment type mismatch");
        }
        if(v.type.equals("int")) LLVMGenerator.assignInt(ID, v.value);
        if(v.type.equals("real)")) LLVMGenerator.assignFloat(ID, v.value);
    }

    @Override
    public void exitDeclaration(MBKGParser.DeclarationContext ctx) {
        String ID = ctx.ID().getText();
        String TYPE = ctx.type().getText();
        if (!variables.containsKey(ID)) {
            if (types.contains(TYPE)){
                variables.put(ID, TYPE);
                if(TYPE.equals("int")) LLVMGenerator.declareInt(ID);
                else if(TYPE.equals("float")) LLVMGenerator.declareFloat(ID);
            } else {
                ctx.getStart().getLine();
                System.err.println("Line " + ctx.getStart().getLine() + ", unknown variable type: " + TYPE);
            }
        } else {
            ctx.getStart().getLine();
            System.err.println("Line " + ctx.getStart().getLine() + ", variabble already defined: " + ID);
        }
    }

    @Override
    public void exitFunction_call(MBKGParser.Function_callContext ctx) {
        String FUNC_NAME = ctx.function().getText();
        if(FUNC_NAME.equals("print")) {
            if(argumentsList.size() == 1) {
                Value argument = argumentsList.get(0);
                String ID = argument.value;
                String type = variables.get(ID);
                if (type != null) {
                    if(type.equals("int")){
                        LLVMGenerator.printInt(ID);
                    } else if (type.equals("float")){
                        LLVMGenerator.printFloat(ID);
                    }
                } else {
                    ctx.getStart().getLine();
                    System.err.println("Line " + ctx.getStart().getLine() + ", unknown variable: " + ID);
                }
            } else {
                ctx.getStart().getLine();
                System.err.println("Line " + ctx.getStart().getLine() + ", too many arguments in function print. Expected 1, got " + argumentsList.size());
            }
        } else if (FUNC_NAME.equals("scan")) {
            if(argumentsList.size() == 1) {
                Value argument = argumentsList.get(0);
                String ID = argument.value;
                String type = variables.get(ID);
                if(type != null){
                    if(type.equals("int")) {
                        LLVMGenerator.scanInt(ID);
                    } else if (type.equals("float")){
                        LLVMGenerator.scanFloat(ID);
                    }
                }else {
                    ctx.getStart().getLine();
                    System.err.println("Line " + ctx.getStart().getLine() + ", unknown variable: " + ID);
                }
            } else {
                ctx.getStart().getLine();
                System.err.println("line " + ctx.getStart().getLine() + ", too many argument in function scan, Expected 1, got: " + argumentsList.size());
            }
        }
        argumentsList.clear();
    }

    @Override
    public void exitInt(MBKGParser.IntContext ctx) {
        stack.push(new Value("int", ctx.INT().getText()));
    }

    @Override
    public void exitFloat(MBKGParser.FloatContext ctx) {
        stack.push(new Value("float", ctx.FLOAT().getText()));
    }

    @Override 
    public void exitId(MBKGParser.IdContext ctx) {
        String ID = ctx.ID().getText();
        if( variables.containsKey(ID) ){
            String type = variables.get(ID);
            int reg = -1;
            if(type.equals("int")){
                reg = LLVMGenerator.loadInt(ID);
            } else if (type.equals("real")){
                reg = LLVMGenerator.loadReal(ID);
            }
            stack.push( new Value(type, "%"+reg));
        } else {
            error(ctx.getStart().getLine(), "no such variable");
        }
    }

    private void error(int location, String msg) {
        System.out.println("Error at line " + location + ": " + msg);
    }

}
