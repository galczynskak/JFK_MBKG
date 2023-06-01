ANTLR=../antlr-4.13.0-complete.jar

all: generate compile test

generate:
	java -jar $(ANTLR) -o output MBKG.g4

compile:
	javac -cp $(ANTLR):output:. Main.java

test:
	java -cp $(ANTLR):output:. Main test.x > test.ll
	lli-10 test.ll

clean:
	rm -f test.ll
	rm -f *.class
	rm -rf output

