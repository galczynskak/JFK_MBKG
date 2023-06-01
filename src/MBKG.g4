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

expression1: expression2
		   | expression1 '+' expression1
		   ;

expression2: expression3
		   | expression2 '-' expression2
		   ;

expression3: expression4
		   | expression3 '*' expression3
		   ;

expression4: expression5
		   | expression4 '/' expression4
		   ;

expression5: value
		   | '(' expression1 ')'
		   ;

value: ID | INT | FLOAT ;

SCAN: 'scan' ;

PRINT: 'print' ;

ID : ('a'..'z'|'A'..'Z')+;

INT : '0'..'9'+;

FLOAT : '0'..'9'+'.''0'..'9'+;

NEWLINE: '\r'? '\n';
