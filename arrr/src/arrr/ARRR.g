grammar ARRR;

@header {
import arrr.Type.*;
import arrr.Type;
}
 
 program returns [Program ast]
 		locals [ArrayList<ExternalDeclaration> decls]              // ArrayList to hold external declarations
 		@init { $decls = new ArrayList<ExternalDeclaration>(); } : // Create the ArrayList

		(ext=externalDeclaration { $decls.add($ext.ast); } )*      // A program consists of 0 or more external declarations, add each to the array.

		{ $ast = new Program($decls); }                            // Finally, evaluate the program AST using the array of external declarations.
		;

externalDeclaration returns [ExternalDeclaration ast] : // An external declaration is either a global declaration or a function definition
		func=functionDefinition { $ast = $func.ast; }   // Function definition
		| var=declaration { $ast = $var.ast; }          // Global variable declaration
		;

functionDefinition returns [FunctionDefinition ast] :
		'canon' id=Identifier 'fires' cs=containerSpecifier params=parameterTypeList body=compoundStatement

		{ $ast = new FunctionDefinition($id.text, $cs.ast, $params.ast, $body.ast); }
		;

parameterTypeList returns [List<ParameterDeclaration> ast]        // ArrayList of function parameters (0 or more)
		@init { $ast = new ArrayList<ParameterDeclaration>(); } : // Create an empty ArrayList of parameters (returned when there are no parameters)
		( 'with' pl=parameterList { $ast = $pl.ast; } )?          // If there are parameters, return the ArrayList returned by the parameterList rule
		;

parameterList returns [List<ParameterDeclaration> ast]               // ArrayList of function parameters (1 or more)
		@init { $ast = new ArrayList<ParameterDeclaration>(); } :    // Create an ArrayList to hold the parameters
		first=parameterDeclaration { $ast.add($first.ast); }         // Evaluate and add the first parameter declaration to the list
		( 'an' next=parameterDeclaration { $ast.add($next.ast); } )* // Evaluate and add the remaining parameters
		;

parameterDeclaration returns [ParameterDeclaration ast] :         // A function parameter AST
		cs=containerSpecifier id=Identifier                     // Capture the parameter type and name
		{ $ast = new ParameterDeclaration($cs.ast, $id.text); } // Create the parameter and return it
		;

containerSpecifier returns [Type ast] :                                   // The container type of a function parameter
		t=typeSpecifier { $ast = $t.ast; }                          // A variable parameter
		| 'plank' t=typeSpecifier { $ast = new ArrType($t.ast); } // An array parameter
		;

typeSpecifier returns [Type ast] :              // Variable and array types
		'naught' { $ast = new VoidType(); }   // Void type, only valid for function return type
		| 'tally' { $ast = new IntegerType(); }   // Integer type
		| 'scroll' { $ast = new StringType(); } // String type
		;

compoundStatement returns [CompoundStatement ast]                                               // A function, loop, or selection statement body
		locals [List<Declaration> decls, List<Statement> stmts]                                 // ArrayList of declarations and statements (all contents)
		@init { $decls = new ArrayList<Declaration>(); $stmts = new ArrayList<Statement>(); } :

		'{'          																			 // *All declarations must come before statements
		( d=declaration { $decls.add($d.ast); } )*                                               // Declaration, add to its ArrayList
		( s=statement { $stmts.add($s.ast); } )*                                                 // Statement, add to its ArrayList
		'}'
		{ $ast = new CompoundStatement($decls, $stmts); }
		;

declaration returns [Declaration ast] :                                                               // A declaration for a variable or an array
		t=typeSpecifier id=Identifier ( 'is' exp=expression )? '!'                                 // Variable
		  { $ast = new VariableDeclaration($t.ast, $id.text, $exp.ctx != null ? $exp.ast : null); }
		| 'plank' exp=expression t=typeSpecifier id=Identifier '!'                                 // Array
		  { $ast = new ArrayDeclaration($exp.ast, $t.ast, $id.text); }
		;

expressionList returns [List<Expression> ast]              // List of expressions for a function call
		@init { $ast = new ArrayList<Expression>(); } :    // Create an ArrayList to hold the expressions
		first=expression { $ast.add($first.ast); }         // Evaluate and add the first expression to the ArrayList
		( 'an' next=expression { $ast.add($next.ast); } )* // Evaluate and add the remaining expressions to the ArrayList
		;

expression returns [Expression ast] :                                                                                      // Order defined order of operations
		'(' exp=expression ')' { $ast = $exp.ast; }                                                                            // Unary operator, parenthesis: ()
		| 'tisnot' exp=expression { $ast = new NegationExpression($exp.ast); }                                                 // Unary operator, negation: !
		| id=Identifier 'at' idx=expression { $ast = new ArrayAccessExpression($id.text, $idx.ast); }                      // Array element access: [i]
		| 'fire' id=Identifier ( 'with' args=expressionList )? 
		  { $ast = new FunctionCallExpression($id.text, $args.ctx != null ? $args.ast : null); }           // Function call
		| func=embeddedFunctionName ( 'with' args=expressionList )?
		  { $ast = new EmbeddedFunctionCallExpression($func.name, $args.ctx != null ? $args.ast : null); } // Embedded function
		| c=constantExpression { $ast = $c.ast; }                                                           // Integer literal: 12345
		| s=stringExpression { $ast = $s.ast; }                                                               // String literal: "abcdef"
		| id=Identifier { $ast = new VariableExpression($id.text); }                                                       // Variable access: n
		| left=expression 'stack\'em' right=expression                                        // Multiplication expression: *
		  { $ast = new MultiplicationExpression($left.ast, $right.ast); }
		| left=expression 'doleout' right=expression                                        // Division expression: /
		  { $ast = new DivisionExpression($left.ast, $right.ast); }
		| left=expression 'dregs' right=expression                                        // Modulo Expression: %
		  { $ast = new ModuloExpression($left.ast, $right.ast); }
		| left=expression 'bestow' right=expression                                                   // Addition expression: +
		  { $ast = new AdditionExpression($left.ast, $right.ast); }
		| left=expression 'forkover' right=expression                                                   // Subtraction expression: -
		  { $ast = new SubtractionExpression($left.ast, $right.ast); }
		| left=expression 'bemorthan' right=expression                                               // Greaterthan Expression: >
		  { $ast = new GreaterthanExpression($left.ast, $right.ast); }
		| left=expression 'belessthan' right=expression                                               // Lessthan Expression: <
		  { $ast = new LessthanExpression($left.ast, $right.ast); }
		| left=expression 'be' right=expression                                                           // Equality Expression: ==
		  { $ast = new EqualityExpression($left.ast, $right.ast); }
		| left=expression 'benot' right=expression                                                           // Inequality Expression: !=
		  { $ast = new InequalityExpression($left.ast, $right.ast); }
		| left=expression 'er' 'n' right=expression                                                               // Logical Or Expression: ||
		  { $ast = new LogicalOrExpression($left.ast, $right.ast); }
		| left=expression 'n' right=expression                                                               // Logical And Expression: &&
		  { $ast = new LogicalAndExpression($left.ast, $right.ast); }
		| <assoc=right> id=Identifier 'is' right=expression                                                                // Variable assignment expression: =
      	  { $ast = new VariableAssignmentExpression($id.text, $right.ast); }
    	| <assoc=right> id=Identifier 'at' idx=expression 'is' right=expression                                            // Array index assignment: [i] =
      	  { $ast = new ArrayAssignmentExpression($id.text, $idx.ast, $right.ast); }
		;

constantExpression returns [ConstantExpression ast] :                                   // A constant integer literal
		c=Constant { $ast = new ConstantExpression(Integer.parseInt($c.text)); }        // Positive
  		| '-' c=Constant { $ast = new ConstantExpression(-Integer.parseInt($c.text)); } // Negative
		;

stringExpression returns [StringExpression ast] :   // A string literal
		s=String { $ast = new StringExpression($s.text); }
		;

embeddedFunctionName returns [String name] :
		'yohoho'       { $name = "yohoho"; }         // Random integer (params: tally min, tally max)(returns: tally r in [min, max])
    	| 'stirthebilge' { $name = "stirthebilge"; } // Shuffle array elements: (params: plank <type> arr)(returns: plank <type> arr)
    	| 'squawk'       { $name = "squawk"; }       // Output to stdout: (params: <type> x) (returns: scroll x)
    	| 'avast\'ye'    { $name = "avast\'ye"; }     // Input from stdin: (params:) (returns: <type> x)
    	;

statement returns [Statement ast] :
		e=expression '!' { $ast = new ExpressionStatement($e.ast); }                                                                                                                     // Expression statement (assignment, function call, etc)
		| 'aye' '(' cond=expression ')' tbody=compoundStatement ( 'scurvy' fbody=compoundStatement )?                                                                // If or If/Else
		  { $ast = new SelectionStatement($cond.ast, $tbody.ast, $fbody.ctx != null ? $fbody.ast : null); }
		| 'in\'voyage' '(' cond=expression ')' body=compoundStatement                                                                                             // While loop
		  { $ast = new ConditionalLoopStatement($cond.ast, $body.ast); }
		| 'fer\'all' '(' ( init=expression )? '!' ( cond=expression )? '!' ( incr=expression )? ')' body=compoundStatement                                        // For loop
		  { $ast = new IterativeLoopStatement( $init.ctx != null ? $init.ast : null, $cond.ctx != null ? $cond.ast : null, $incr.ctx != null ? $incr.ast : null, $body.ast); }
		| 'booty' ( e=expression )? '!' {$ast = new ReturnStatement($e.ctx != null ? $e.ast : null); }                                                                                                         // Return statement (from function)
		;


 // Lexical Specification of this Programming Language
 //  - lexical specification rules start with uppercase
 Canon : 'canon' ;
 Fires : 'fires' ;
 With  : 'with' ;
 An    : 'an' ;
 Plank : 'plank' ;
 Naught : 'naught' ;
 Tally : 'tally' ;
 Scroll : 'scroll' ;
 Is : 'is' ;
 ExclamationPoint : '!' ;
 At : 'at' ;
 Er : 'er' ;
 N : 'n' ;
 Be : 'be' ;
 Benot : 'benot' ;
 Bemorthan : 'bemorthan' ;
 Belessthan : 'belessthan' ;
 Bestow : 'bestow' ;
 Forkover : 'forkover' ;
 Stackem : 'stack\'em' ;
 Doleout : 'doleout' ;
 Dregs : 'dregs' ;
 Fire : 'fire' ;
 Tisnot : 'tisnot' ;
 Yohoho : 'yohoho' ;
 Stirthebilge : 'Stirthebilge' ;
 Squawk : 'squawk' ;
 Avastye : 'avast\'ye' ;
 Aye : 'aye' ;
 Scurvy : 'scurvy' ;
 Invoyage : 'in\'voyage' ;
 Ferall : 'fer\'all' ;
 Booty : 'booty' ;

 Constant : DIGIT+ ;

 Identifier :   Letter LetterOrDigit*;

 Letter :   [a-zA-Z$_]
	|   ~[\u0000-\u00FF\uD800-\uDBFF] 
		{Character.isJavaIdentifierStart(_input.LA(-1))}?
	|   [\uD800-\uDBFF] [\uDC00-\uDFFF] 
		{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}? ;

 LetterOrDigit: [a-zA-Z0-9$_]
	|   ~[\u0000-\u00FF\uD800-\uDBFF] 
		{Character.isJavaIdentifierPart(_input.LA(-1))}?
	|    [\uD800-\uDBFF] [\uDC00-\uDFFF] 
		{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?;

 fragment DIGIT: ('0'..'9');

 WS  :  [ \t\r\n\u000C]+ -> skip;
 Comment :   '/*' .*? '*/' -> skip;
 Line_Comment :   '//' ~[\r\n]* -> skip;
 
 fragment ESCAPE : '\\' ('n' | 't' | 'r' | '\\' | '"') ;
 String : '"' ( ESCAPE | ~('\\' | '"' | '\n' | '\r') )* '"' ;
 	