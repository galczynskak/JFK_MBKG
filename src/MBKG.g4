grammar MBKG;

program: (statement? NEWLINE)* statement? ;

statement: declaration
		 | function_call
		 | assignment
		 ;
declaration: type ID ;

type: 'int' | 'float' ;

function_call: function value ;

function: SCAN | PRINT ;

assignment: declaration '=' operation
		  | ID '=' operation
		  ;

operation: expression1 ;

expression1: expression2					#single1
		   | expression1 '+' expression1	#add
		   ;

expression2: expression3					#single2
		   | expression2 '-' expression2	#sub
		   ;

expression3: expression4					#single3
		   | expression3 '*' expression3	#mult
		   ;

expression4: expression5					#single4
		   | expression4 '/' expression4	#div
		   ;

expression5: value							#single5
		   | '(' expression1 ')'			#par
		   ;

value: ID 	 #id
	 | INT	 #int 
	 | FLOAT #float
	 ;

SCAN: 'scan' ;

PRINT: 'print' ;

ID : ('a'..'z'|'A'..'Z')+;

INT : '0'..'9'+;

FLOAT : '0'..'9'+'.''0'..'9'+;

NEWLINE: '\r'? '\n';

WS:   (' '|'\t')+ { skip(); }
    ;