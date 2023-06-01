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
        if(!v.type.equals(variables.get(ID))) {
            error(ctx.getStart().getLine(), "assignment type mismatch");
        }
        if(v.type.equals("int")) {
            LLVMGenerator.assignInt(ID, v.value);
        }
        if(v.type.equals("real)")) {
            LLVMGenerator.assignFloat(ID, v.value);
        }
    }

    private void error(int location, String msg) {
        System.out.println("Error at line " + location + ": " + msg);
    }

}
