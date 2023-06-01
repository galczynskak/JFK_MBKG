public class LLVMGenerator {

    static String header_text = "";
    static String main_text = "";
    static int reg = 1;

    static void assignInt(String id, String value) {
        main_text += "store i32 " + value + ", i32* %" + id + "\n";
    }

    static void assignFloat(String id, String value) {
        main_text += "store double " + value + ", double* %" + id + "\n";
    }

    static void declareInt(String id) {
        main_text += "%" + id + " = alloca i32\n";
    }

    static void declareFloat(String id) {
        main_text += "%" + id + " = alloca double\n";
    }

    static void printInt(String id) {
        main_text += "%" + reg + " = load i32, i32* %" + id + "\n";
        reg++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strp, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printFloat(String id) {
        main_text += "%" + reg + " = load double, double* %" + id + "\n";
        reg++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (reg - 1) + ")\n";
        reg++;
    }

    static void scanInt(String id) {
        main_text += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i32* %" + id + ")\n";
        reg++;
    }

    static void scanFloat(String id) {
        main_text += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strsd, i32 0, i32 0), double* %" + id + ")\n";
        reg++;
    }


    static void addInt(String v1, String v2) {
        main_text += "%" + reg + " = add i32 " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void addFloat(String v1, String v2) {
        main_text += "%" + reg + " = fadd double " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void subInt(String v1, String v2) {
        main_text += "%" + reg + " = sub i32 " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void subFloat(String v1, String v2) {
        main_text += "%" + reg + " = fsub double " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void multInt(String v1, String v2) {
        main_text += "%" + reg + " = mul i32 " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void multFloat(String v1, String v2) {
        main_text += "%" + reg + " = fmul double " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void divInt(String v1, String v2) {
        main_text += "%" + reg + " = sdiv i32 " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static void divFloat(String v1, String v2) {
        main_text += "%" + reg + " = fdiv double " + v1 + ", " + v2 + "\n";
        reg++;
    }

    static int loadInt(String id) {
        main_text += "%" + reg + " = load i32, i32* %" + id + "\n";
        reg++;
        return reg - 1;
    }

    static int loadFloat(String id) {
        main_text += "%" + reg + " = load double, double* %" + id + "\n";
        reg++;
        return reg - 1;
    }

    static String generate() {
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
