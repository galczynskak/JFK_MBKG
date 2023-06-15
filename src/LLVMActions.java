import java.util.*;

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
    HashMap<String, String> globalVariables = new HashMap<String, String>();
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

    Boolean isGlobal;

    @Override
    public void enterProgram(MBKGParser.ProgramContext ctx) {
        isGlobal = true;
    }

    @Override
    public void exitProgram(MBKGParser.ProgramContext ctx) {
        LLVMGenerator.close_main();
        System.out.println(LLVMGenerator.generate());
    }

    @Override
    public void exitDeclAssign(MBKGParser.DeclAssignContext ctx) {
        String ID = ctx.declaration().getChild(1).getText();
        String ArrayOperation = ctx.operation().getChild(0).getText();
        if (ArrayOperation.charAt(0) != '[') {
            if (!variables.containsKey(ID) && !globalVariables.containsKey(ID)) {
                error(ctx.getStart().getLine(), "variable not declared 1");
            }
            Value v = stack.pop();
            if (variables.containsKey(ID)) {
                if (!v.type.equals(variables.get(ID))) {
                    error(ctx.getStart().getLine(), "assignment type mismatch 1 ");
                }
            } else {
                if (!v.type.equals(globalVariables.get(ID))) {
                    System.out.println(v.type);
                    error(ctx.getStart().getLine(), "assignment type mismatch 2");
                }
            }
            if (v.type.equals("int")) {
                LLVMGenerator.assignInt(getScope(ID), v.value);
            }
            if (v.type.equals("float")) {
                LLVMGenerator.assignFloat(getScope(ID), v.value);
            }
        } else {
            try {
                ID = ctx.declaration().getChild(2).getText();
                String arrType = variables.get(ID);
                if (arrType == null) {
                    arrType = globalVariables.get(ID);
                }
                String[] split_array_type = arrType.split("\\[");
                String type = split_array_type[0];
                String len = split_array_type[1].split("\\]")[0];
                List<String> values = new ArrayList<>();

                if (argumentsList.size() > Integer.parseInt(len)) {
                    error(ctx.getStart().getLine(), "array is bigger than declared");
                }
                for (Value v : argumentsList) {

                    if ((v.type.equals("ID") && variables.containsKey(v.value))
                            || (globalVariables.containsKey(v.value) && globalVariables.get(v.value).contains(type))) {
                        if (type.equals("int")) {
                            values.add("%" + LLVMGenerator.loadInt(getScope(v.value)));
                        } else if (type.equals("float")) {
                            values.add("%" + LLVMGenerator.loadFloat(getScope(v.value)));
                        }
                    } else if (v.type.equals("ARRAY_ID") && variables.containsKey(v.value)) {
                        String[] split_array_id = v.value.split("\\[");
                        String id = split_array_id[0];
                        String arrId = split_array_id[1].split("\\]")[0];
                        if (type.equals("int")) {
                            values.add("%" + LLVMGenerator.loadIntArrayValue(getScope(id), arrId, len));
                        } else if (type.equals("float")) {
                            values.add("%" + LLVMGenerator.loadFloatArrayValue(getScope(id), arrId, len));
                        }
                    } else if (v.type.equals("int") || v.type.equals("float")) {
                        values.add(v.value);
                    }
                }
                for (int i = 0; i < values.size(); i++) {
                    if (type.equals("int")) {
                        LLVMGenerator.assignArrayIntElement(values.get(i), getScope(ID), Integer.toString(i), len);
                    } else if (type.equals("float")) {
                        LLVMGenerator.assignArrayFloatElement(values.get(i), getScope(ID), Integer.toString(i), len);
                    }
                }
                argumentsList.clear();
            } catch (ArrayIndexOutOfBoundsException e) {
                error(ctx.getStart().getLine(), "variable is not an array");
            }
        }

    }

    @Override
    public void exitIdAssign(MBKGParser.IdAssignContext ctx) {
        String ID = ctx.ID().getText();

        if (!variables.containsKey(ID) && !globalVariables.containsKey(ID)) {
            error(ctx.getStart().getLine(), "variable not declared 2");
        }
        String ArrayOperation = ctx.operation().getChild(0).getText();
        if (ArrayOperation.charAt(0) != '[') {
            Value v = stack.pop();
            if (variables.containsKey(ID)) {
                if (!v.type.equals(variables.get(ID))) {
                    error(ctx.getStart().getLine(), "assignment type mismatch 3");
                }
            } else {
                if (!v.type.equals(globalVariables.get(ID))) {
                    error(ctx.getStart().getLine(), "assignment type mismatch 4");
                }
            }
            if (v.type.equals("int")) {
                LLVMGenerator.assignInt(getScope(ID), v.value);
            }
            if (v.type.equals("float")) {
                LLVMGenerator.assignFloat(getScope(ID), v.value);
            }
        } else {
            try {
                String arrType = variables.get(ID);
                if (arrType == null) {
                    arrType = globalVariables.get(ID);
                }
                String[] split_array_type = arrType.split("\\[");
                String type = split_array_type[0];
                String len = split_array_type[1].split("\\]")[0];
                List<String> values = new ArrayList<>();
                if (argumentsList.size() != Integer.parseInt(len)) {
                    error(ctx.getStart().getLine(), "array size mismatch");
                }
                for (Value v : argumentsList) {

                    if (v.type.equals("ID") && (
                            (variables.containsKey(v.value) && variables.get(v.value).contains(type))
                                    || (globalVariables.containsKey(v.value) && globalVariables.get(v.value).contains(type)))
                    ) {
                        if (type.equals("int")) {
                            values.add("%" + LLVMGenerator.loadInt(getScope(v.value)));
                        } else if (type.equals("float")) {
                            values.add("%" + LLVMGenerator.loadFloat(getScope(v.value)));
                        }
                    } else if (v.type.equals("ARRAY_ID") && (
                            (variables.containsKey(v.value) && variables.get(v.value).contains(type))
                                    || (globalVariables.containsKey(v.value) && globalVariables.get(v.value).contains(type))
                    )) {
                        String[] split_array_id = v.value.split("\\[");
                        String id = split_array_id[0];
                        String arrId = split_array_id[1].split("\\]")[0];
                        if (type.equals("int")) {
                            values.add("%" + LLVMGenerator.loadIntArrayValue(getScope(id), arrId, len));
                        } else if (type.equals("float")) {
                            values.add("%" + LLVMGenerator.loadFloatArrayValue(getScope(id), arrId, len));
                        }
                    } else if (v.type.equals("int") || v.type.equals("float")) {
                        values.add(v.value);
                    }
                }
                for (int i = 0; i < values.size(); i++) {
                    if (type.equals("int")) {
                        LLVMGenerator.assignArrayIntElement(values.get(i), getScope(ID), Integer.toString(i), len);
                    } else if (type.equals("float")) {
                        LLVMGenerator.assignArrayFloatElement(values.get(i), getScope(ID), Integer.toString(i), len);
                    }
                }
                argumentsList.clear();
            } catch (ArrayIndexOutOfBoundsException e) {
                error(ctx.getStart().getLine(), "variable is not an array");
            }
        }

    }

    @Override
    public void exitArrayIdAssign(MBKGParser.ArrayIdAssignContext ctx) {
        String ARRAY_ID = ctx.ARRAY_ID().getText();
        String[] split_array_id = ARRAY_ID.split("\\[");
        String id = split_array_id[0];
        String arrId = split_array_id[1].split("\\]")[0];
        if (!variables.containsKey(id) && !globalVariables.containsKey(id)) {
            error(ctx.getStart().getLine(), "variable not declared 3");
        }
        String arrType = variables.get(id);
        if (arrType == null) {
            arrType = globalVariables.get(id);
        }
        String[] split_array_type = arrType.split("\\[");
        String type = split_array_type[0];
        String len = split_array_type[1].split("\\]")[0];

        if (Integer.parseInt(arrId) >= Integer.parseInt(len) || Integer.parseInt(arrId) < 0) {
            error(ctx.getStart().getLine(), "allocation fail :) (:");
        }

        Value v = stack.pop();
        if (!v.type.equals(type)) {
            error(ctx.getStart().getLine(), "arrayId assignment type mismatch");
        }
        if (v.type.equals("int")) {
            LLVMGenerator.assignArrayIntElement(v.value, getScope(id), arrId, len);
        }
        if (v.type.equals("float")) {
            LLVMGenerator.assignArrayFloatElement(v.value, getScope(id), arrId, len);
        }
    }

    @Override
    public void exitDeclaration(MBKGParser.DeclarationContext ctx) {
        String ID = ctx.ID().getText();
        String TYPE = ctx.type().getText();

        if ((!variables.containsKey(ID) && !isGlobal) || (!globalVariables.containsKey(ID) && isGlobal)) {
            if (types.contains(TYPE)) {
                try {
                    String ARRAY_LEN = ctx.array_declaration().getChild(1).getText();
                    if (isGlobal) {
                        globalVariables.put(ID, TYPE + '[' + ARRAY_LEN + ']');
                    } else {
                        variables.put(ID, TYPE + '[' + ARRAY_LEN + ']');
                    }
                    if (TYPE.equals("int")) {
                        LLVMGenerator.declareIntArray(ID, ARRAY_LEN, isGlobal);
                    } else if (TYPE.equals("float")) {
                        LLVMGenerator.declareFloatArray(ID, ARRAY_LEN, isGlobal);
                    }
                } catch (NullPointerException ex) {
                    if (isGlobal) {
                        globalVariables.put(ID, TYPE);
                    } else {
                        variables.put(ID, TYPE);
                    }
                    if (TYPE.equals("int")) LLVMGenerator.declareInt(ID, isGlobal);
                    else if (TYPE.equals("float")) LLVMGenerator.declareFloat(ID, isGlobal);
                }
            } else {
                ctx.getStart().getLine();
                System.err.println("Line " + ctx.getStart().getLine() + ", unknown variable type: " + TYPE);
            }
        } else {
            ctx.getStart().getLine();
            System.err.println("Line " + ctx.getStart().getLine() + ", variable already defined: " + ID);
        }
    }

    @Override
    public void exitFunction_call(MBKGParser.Function_callContext ctx) {
        String FUNC_NAME = ctx.function().getText();
        if (FUNC_NAME.equals("print")) {
            if (argumentsList.size() == 1) {
                Value argument = argumentsList.get(0);
                String ID = argument.value;
                String type = variables.get(ID);
                if (type == null) {
                    type = globalVariables.get(ID);
                }
                if (type != null) {
                    if (type.equals("int")) {
                        LLVMGenerator.printInt(getScope(ID));
                    } else if (type.equals("float")) {
                        LLVMGenerator.printFloat(getScope(ID));
                    }
                } else {
                    if (argument.type.equals("int")) {
                        LLVMGenerator.printConstantInt(ID);
                    } else if (argument.type.equals("float")) {
                        LLVMGenerator.printConstantFloat(ID);
                    } else {
                        ctx.getStart().getLine();
                        System.err.println("Line " + ctx.getStart().getLine() + ", unknown variable: " + ID);
                    }
                }
            } else {
                if (argumentsList.size() == 0) {
                    ctx.getStart().getLine();
                    System.err.println("line " + ctx.getStart().getLine() + ", no arguments in function print, Expected 1, got 0");
                } else {
                    ctx.getStart().getLine();
                    System.err.println("line " + ctx.getStart().getLine() + ", too many argument in function print, Expected 1, got: " + argumentsList.size());
                }
            }
        } else if (FUNC_NAME.equals("scan")) {
            if (argumentsList.size() == 1) {
                Value argument = argumentsList.get(0);
                String ID = argument.value;
                String type = variables.get(ID);
                if (type == null) {
                    type = globalVariables.get(ID);
                }
                if (type != null) {
                    if (type.equals("int")) {
                        LLVMGenerator.scanInt(getScope(ID));
                    } else if (type.equals("float")) {
                        LLVMGenerator.scanFloat(getScope(ID));
                    }
                } else {
                    ctx.getStart().getLine();
                    System.err.println("Line " + ctx.getStart().getLine() + ", unknown variable: " + ID);
                }
            } else {
                if (argumentsList.size() == 0) {
                    ctx.getStart().getLine();
                    System.err.println("line " + ctx.getStart().getLine() + ", no arguments in function scan, Expected 1, got 0");
                } else {
                    ctx.getStart().getLine();
                    System.err.println("line " + ctx.getStart().getLine() + ", too many argument in function scan, Expected 1, got: " + argumentsList.size());
                }

            }
        }
        argumentsList.clear();
    }

    @Override
    public void exitValue(MBKGParser.ValueContext ctx) {
        try {
            argumentsList.add(new Value("ID", ctx.ID().getText()));
        } catch (NullPointerException e) {
        }

        try {
            argumentsList.add(new Value("int", ctx.INT().getText()));
        } catch (NullPointerException e) {
        }

        try {
            argumentsList.add(new Value("float", ctx.FLOAT().getText()));
        } catch (NullPointerException e) {
        }

        try {
            argumentsList.add(new Value("ARRAY_ID", ctx.ARRAY_ID().getText()));
        } catch (NullPointerException e) {
        }
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
        if (variables.containsKey(ID) || globalVariables.containsKey(ID)) {
            String type = variables.get(ID);
            if (type == null) {
                type = globalVariables.get(ID);
            }
            int reg = -1;
            if (type.equals("int")) {
                reg = LLVMGenerator.loadInt(getScope(ID));
            } else if (type.equals("float")) {
                reg = LLVMGenerator.loadFloat(getScope(ID));
            }
            stack.push(new Value(type, "%" + reg));
        } else {
            error(ctx.getStart().getLine(), "no such variable");
        }
    }

    @Override
    public void exitArray_id(MBKGParser.Array_idContext ctx) {
        String ARRAY_ID = ctx.ARRAY_ID().getText();
        String[] split_array_id = ARRAY_ID.split("\\[");
        String id = split_array_id[0];
        String arrId = split_array_id[1].split("\\]")[0];
        if (variables.containsKey(id) || globalVariables.containsKey(id)) {
            String arrType = variables.get(id);
            if (arrType == null) {
                arrType = globalVariables.get(id);
            }
            String[] split_array_type = arrType.split("\\[");
            String type = split_array_type[0];
            String len = split_array_type[1].split("\\]")[0];
            int reg = -1;
            if (type.equals("int")) {
                reg = LLVMGenerator.loadIntArrayValue(getScope(id), arrId, len);
            } else if (type.equals("float")) {
                reg = LLVMGenerator.loadFloatArrayValue(getScope(id), arrId, len);
            }
            stack.push(new Value(type, "%" + reg));
        } else {
            error(ctx.getStart().getLine(), "no such array");
        }
    }

    @Override
    public void exitAdd(MBKGParser.AddContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type.equals(v2.type)) {
            if (v1.type.equals("int")) {
                LLVMGenerator.addInt(v1.value, v2.value);
                stack.push(new Value("int", "%" + (LLVMGenerator.reg - 1)));
            }
            if (v1.type.equals("float")) {
                LLVMGenerator.addFloat(v1.value, v2.value);
                stack.push(new Value("float", "%" + (LLVMGenerator.reg - 1)));
            }
        } else {
            error(ctx.getStart().getLine(), "addition type mismatch");
        }
    }

    @Override
    public void exitSub(MBKGParser.SubContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type.equals(v2.type)) {
            if (v1.type.equals("int")) {
                LLVMGenerator.subInt(v2.value, v1.value);
                stack.push(new Value("int", "%" + (LLVMGenerator.reg - 1)));
            }
            if (v1.type.equals("float")) {
                LLVMGenerator.subFloat(v2.value, v1.value);
                stack.push(new Value("float", "%" + (LLVMGenerator.reg - 1)));
            }
        } else {
            error(ctx.getStart().getLine(), "subtraction type mismatch");
        }
    }

    @Override
    public void exitMult(MBKGParser.MultContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type.equals(v2.type)) {
            if (v1.type.equals("int")) {
                LLVMGenerator.multInt(v1.value, v2.value);
                stack.push(new Value("int", "%" + (LLVMGenerator.reg - 1)));
            }
            if (v1.type.equals("float")) {
                LLVMGenerator.multFloat(v1.value, v2.value);
                stack.push(new Value("float", "%" + (LLVMGenerator.reg - 1)));
            }
        } else {
            error(ctx.getStart().getLine(), "multiplication type mismatch");
        }
    }

    @Override
    public void exitDiv(MBKGParser.DivContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type.equals(v2.type)) {
            if (v1.type.equals("int")) {
                LLVMGenerator.divInt(v2.value, v1.value);
                stack.push(new Value("int", "%" + (LLVMGenerator.reg - 1)));
            }
            if (v1.type.equals("float")) {
                LLVMGenerator.divFloat(v2.value, v1.value);
                stack.push(new Value("float", "%" + (LLVMGenerator.reg - 1)));
            }
        } else {
            error(ctx.getStart().getLine(), "division type mismatch");
        }
    }

    @Override
    public void enterBlockif(MBKGParser.BlockifContext ctx) {
        isGlobal = false;
        LLVMGenerator.ifstart();
    }

    @Override
    public void exitBlockif(MBKGParser.BlockifContext ctx) {
        LLVMGenerator.ifend();
    }

    @Override
    public void exitBlockelse(MBKGParser.BlockelseContext ctx) {
        LLVMGenerator.elseend();
        isGlobal = true;
    }

    @Override
    public void exitCondition(MBKGParser.ConditionContext ctx) {
        String ID = ctx.ID().getText();
        String operation = ctx.if_operation().getText();
        String value = ctx.comparable_value().getText();

        if (globalVariables.containsKey(ID) || variables.containsKey(ID)) {
            String type = "";
            if (globalVariables.containsKey(ID)) {
                type = globalVariables.get(ID);
            } else if (variables.containsKey(ID)) {
                type = variables.get(ID);
            }

            if ((type.equals("int") && value.contains("\\.")) || (type.equals("float") && !value.contains("\\."))) {
                error(ctx.getStart().getLine(), "mismatching comparison types");
            }
            String operation_text = "";
            switch (operation) {
                case "==":
                    operation_text = "eq";
                    break;
                case "!=":
                    operation_text = "ne";
                    break;
                case ">":
                    operation_text = "ult";
                    break;
                case "<":
                    operation_text = "ugt";
                    break;
            }
            if (type.equals("int")) {
                LLVMGenerator.icmp(getScope(ID), value, "i32", operation_text);
            } else if (type.equals("float")) {
                LLVMGenerator.icmp(getScope(ID), value, "double", operation_text);
            }
        } else {
            error(ctx.getStart().getLine(), "variable not defined");
        }


    }

    public String getScope(String ID) {
        String scopedID;
        if (isGlobal) {
            scopedID = "@" + ID;
        } else {
            if (!variables.containsKey(ID)) {
                scopedID = "@" + ID;
            } else {
                scopedID = "%" + ID;
            }
        }
        return scopedID;
    }


    private void error(int location, String msg) {
        System.out.println("Error at line " + location + ": " + msg);
    }

}
