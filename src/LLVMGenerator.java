public class LLVMGenerator {

    static String header_text = "";
    static String main_text = "";
    static int reg = 1;

    static void assignInt(String id, String value){
        main_text += "store i32 "+value+", i32* %"+id+"\n";
    }

    static void assignFloat(String id, String value){
        main_text += "store double "+value+", double* %"+id+"\n";
    }

    static String generate(){
        String text = "";
        text += "declare i32 @printf(i8*, ...)\n";
        text += "declare i32 @__isoc99_scanf(i8*, ...)\n";
        text += "@strp = constant [4 x i8] c\"%d\\0A\\00\"\n";
        text += "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n";
        text += "@strs = constant [3 x i8] c\"%d\\00\"\n";
        text += "@strsd = constant [4 x i8] c\"%lf\\00\"\n";
        text += header_text;
        text += "define i32 @main() nounwind{\n";
        text += main_text;
        text += "ret i32 0 }\n";
        return text;
    }
}
