grammar MBKG;

program: block;

block: (statement? NEWLINE)*;

statement: declaration
		 | function_call
		 | assignment
		 | ifblock
		 | loopblock
		 ;

loopblock: LOOP ID condition BEGIN blockfor ENDLOOP;

blockfor: block;

array_declaration : '{' INT '}';

array_values : value ',' array_values
		   	 | value
		   	 ;

array: '[' array_values ']';

declaration: type ID
		   | type array_declaration ID
		   ;

type: 'int' | 'float' ;

function_call: function value ;

function: SCAN | PRINT ;

assignment: declaration '=' operation	#declAssign
		  | ID '=' operation			#idAssign
		  | ARRAY_ID '=' expression1	#arrayIdAssign
		  ;

ifblock: IF condition BEGIN blockif ENDIF ELSE blockelse ENDELSE;

blockif: block;

blockelse: block;

condition: ID if_operation comparable_value;

if_operation: 	  EQUALS #equal
				| NOTEQUALS #notequal
				| LESS #less
				| GREATER #greater
				;

operation: expression1 | array ;

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

expression5: ID 	                        #id
		   | INT	                        #int 
		   | FLOAT 							#float
		   | ARRAY_ID						#array_id
		   | '(' expression1 ')'			#par
		   ;

value: ID
	 | INT
	 | FLOAT
	 | ARRAY_ID
	 ;

comparable_value: INT | FLOAT;

LOOP: 'loop';

ENDLOOP: 'endloop';

SCAN: 'scan' ;

PRINT: 'print' ;

IF: 'if';

ENDIF: 'endif';

ELSE: 'else';

ENDELSE: 'endelse';

BEGIN: 'begin';

EQUALS: '==';

NOTEQUALS: '!=';

GREATER: '>';

LESS: '<';

ID : ('a'..'z'|'A'..'Z')+;

ARRAY_ID : ('a'..'z'|'A'..'Z')+'[''0'..'9'+']';

INT : '0'..'9'+;

FLOAT : '0'..'9'+'.''0'..'9'+;

NEWLINE: '\r'? '\n';

WS:   (' '|'\t')+ { skip(); }
    ;