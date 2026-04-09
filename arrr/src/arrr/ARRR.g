grammar ARRR;
 
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
		'canon' id=Identifier 'fires' type=containerSpecifier params=parameterTypeList body=compoundStatement

		{ $ast = new FunctionDefinition($id.text, $type.ast, $params.ast, $body.ast); }
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
		type=containerSpecifier id=Identifier                     // Capture the parameter type and name
		{ $ast = new ParameterDeclaration($type.ast, $id.text); } // Create the parameter and return it
		;

containerSpecifier returns [Type ast] :                             // The container type of a function parameter
		t=typeSpecifier { $ast = $t.ast; }                          // A variable parameter
		| 'plank' t=typeSpecifier { $ast = new PlankType($t.ast); } // An array parameter
		;

typeSpecifier returns [Type ast] :              // Variable and array types
		'naught' { $ast = new NaughtType(); }   // Void type, only valid for function return type
		| 'tally' { $ast = new TallyType(); }   // Integer type
		| 'scroll' { $ast = new ScrollType(); } // String type
		;

compoundStatement returns [CompoundStatement ast]                                               // A function, loop, or selection statement body
		locals [List<Declaration> decls, List<Statement> stmts]                                 // ArrayList of declarations and statements (all contents)
		@init { $decls = new ArrayList<Declaration>(); $stmts = new ArrayList<Statement>(); } :

		'{' 																					// *All declarations must come before statements
		( d=declaration { $decls.add($d.ast); } )*                                              // Declaration, add to its ArrayList
		( s=statement { $stmts.add($s.ast) } )*                                                 // Statement, add to its ArrayList
		'}'
		;

declaration returns [Declaration ast] :                                                      // A declaration for a variable or an array
		t=typeSpecifier d=declarator '!'                                                     // Variable
		  { $ast = new VariableDeclaration($t.ast, $d.ast); }
		| 'plank' e=expression t=typeSpecifier id=Identifier '!'                             // Array
		  { $ast = new PlankDeclaration($e.ast, $t.ast, $id.text); }
		;

declarator returns [Declarator ast] :                                    // Variable declaration or initialization
		id=Identifier  (  'is' e=expression )?                           // Check if variable has initializer expression
		{ $ast = new Declarator($id.text, $e != null ? $e.ast : null); } // Return with initializer AST if present
		;

expressionList returns [List<Expression> ast]              // List of expressions for a function call
		@init { $ast = new ArrayList<Expression>(); } :    // Create an ArrayList to hold the expressions
		first=expression { $ast.add($first.ast); }         // Evaluate and add the first expression to the ArrayList
		( 'an' next=expression { $ast.add($next.ast); } )* // Evaluate and add the remaining expressions to the ArrayList
		;

expression returns [Expression ast] :                                                                                      // Order defined order of operations
		'(' e=expression ')' { $ast = $e.ast; }                                                                            // Unary operator, parenthesis: ()
		| 'tisnot' e=expression { $ast = new NegationExpression($e.ast); }                                                 // Unary operator, negation: !
		| id=Identifier 'at' idx=expression { $ast = new ArrayAccessExpression($id.text, $idx.ast); }                      // Array element access: [i]
		| 'fire' id=Identifier ( 'with' args=expressionList )? 
		  { $ast = FunctionCallExpression($id.text, $args != null ? $args.ast : new ArrayList<Expression>() ); }           // Function call
		| func=embeddedFunctionName ( 'with' args=expressionList )?
		  { $ast = EmbeddedFunctionCallExpression($func.name, $args != null ? $args.ast : new ArrayList<Expression>() ); } // Embedded function
		| c=Constant { $ast = new ConstantExpression($c.text); }                                                           // Integer literal: 12345
		| s=String { $ast = new StringExpression($s.text); }                                                               // String literal: "abcdef"
		| id=Identifier { $ast = new VariableExpression($id.text); }                                                       // Variable access: n
		| left=expression op=( 'stack\'em' | 'doleout' | 'dregs' ) right=expression                                        // Multiplicative expression: * / %
		  { $ast = new MultiplicativeExpression($left.ast, $op.text, $right.ast); }
		| left=expression op=( 'bestow' | 'forkover' )  right=expression                                                   // Additive expression: + -
		  { $ast = new AdditiveExpression($left.ast, $op.text, $right.ast); }
		| left=expression op=( 'bemorthan' | 'belessthan' ) right=expression                                               // Relational Expression: < >
		  { $ast = new RelationalExpression($left.ast, $op.text, $right.ast); }
		| left=expression op=( 'be' | 'benot' ) right=expression                                                           // Equality Expression: == !=
		  { $ast = new EqualityExpression($left.ast, $op.text, $right.ast); }
		| left=expression op=( 'er' | 'n' ) right=expression                                                               // Logical Expression: || &&
		  { $ast = new LogicalExpression($left.ast, $op.text, $right.ast); }
		| <assoc=right> id=Identifier 'is' right=expression                                                                // Variable assignment expression: =
      	  { $ast = new AssignmentExp($id.text, $right.ast); }
    	| <assoc=right> id=Identifier 'at' idx=expression 'is' right=expression                                            // Array index assignment: [i] =
      	  { $ast = new ArrayAssignmentExpression($id.text, $idx.ast, $right.ast); }
		;

embeddedFunctionName returns [String name] :
		'yohoho'       { $name = "yohoho"; }         // Random integer (params: tally min, tally max)(returns: tally r in [min, max])
    	| 'stirthebilge' { $name = "stirthebilge"; } // Shuffle array elements: (params: plank <type> arr)(returns: plank <type> arr)
    	| 'squawk'       { $name = "squawk"; }       // Output to stdout: (params: <type> x) (returns: scroll x)
    	| 'avast\'ye'    { $name = "avast'ye"; }     // Input from stdin: (params:) (returns: <type> x)
    	;

statement returns [Statement ast] :
		e=expression '!' { $ast = $e.ast; }                                                                                                                     // Expression statement (assignment, function call, etc)
		| 'aye' '(' cond=expression ')' cs=compoundStatement ( 'scurvy' scs=compoundStatement )?                                                                // If or If/Else
		  { $ast = new SelectionStatement($cond.ast, $cs.ast, $scs != null ? $scs.ast : null); }
		| 'in\'voyage' '(' cond=expression ')' cs=compoundStatement                                                                                             // While loop
		  { $ast = new ConditionalLoopStatement($cond.ast, cs.ast); }
		| 'fer\'all' '(' ( init=expression )? '!' ( cond=expression )? '!' ( incr=expression )? ')' cs=compoundStatement                                        // For loop
		  { $ast = new IterativeLoopStatement($init != null ? $init.ast : null, $cond != null ? $cond.ast : null, $incr != null ? $incr.ast : null, $cs.ast); }
		| 'booty' ( e=expression )? '!' {$ast = $e.ast}                                                                                                         // Return statement (from function)
		;

// BELOW ARE RULES CREATED FOR FuncLang

 definedecl returns [DefineDecl ast] :
 		'(' Define 
 			id=Identifier
 			e=exp
 		')' { $ast = new DefineDecl($id.text, $e.ast); }
 		;

 exp returns [Exp ast]: 
		va=varexp { $ast = $va.ast; }
		| num=numexp { $ast = $num.ast; }
		| str=strexp { $ast = $str.ast; }
		| bl=boolexp { $ast = $bl.ast; }
        | add=addexp { $ast = $add.ast; }
        | sub=subexp { $ast = $sub.ast; }
        | mul=multexp { $ast = $mul.ast; }
        | div=divexp { $ast = $div.ast; }
        | let=letexp { $ast = $let.ast; }
        | lam=lambdaexp { $ast = $lam.ast; }
        | call=callexp { $ast = $call.ast; }
        | i=ifexp { $ast = $i.ast; }
        | less=lessexp { $ast = $less.ast; }
        | eq=equalexp { $ast = $eq.ast; }
        | gt=greaterexp { $ast = $gt.ast; }
        | car=carexp { $ast = $car.ast; }
        | cdr=cdrexp { $ast = $cdr.ast; }
        | cons=consexp { $ast = $cons.ast; }
        | list=listexp { $ast = $list.ast; }
        | nl=nullexp { $ast = $nl.ast; }
        ;

 // New Expressions for 

 lambdaexp returns [LambdaExp ast] 
        locals [ArrayList<String> formals ]
 		@init { $formals = new ArrayList<String>(); } :
 		'(' Lambda 
 			'(' (id=Identifier { $formals.add($id.text); } )* ')'
 			body=exp 
 		')' { $ast = new LambdaExp($formals, $body.ast); }
 		;

 callexp returns [CallExp ast] 
        locals [ArrayList<Exp> arguments = new ArrayList<Exp>();  ] :
 		'(' f=exp 
 			( e=exp { $arguments.add($e.ast); } )* 
 		')' { $ast = new CallExp($f.ast,$arguments); }
 		;

 ifexp returns [IfExp ast] :
 		'(' If 
 		    e1=exp 
 			e2=exp 
 			e3=exp 
 		')' { $ast = new IfExp($e1.ast,$e2.ast,$e3.ast); }
 		;

 lessexp returns [LessExp ast] :
 		'(' Less 
 		    e1=exp 
 			e2=exp 
 		')' { $ast = new LessExp($e1.ast,$e2.ast); }
 		;

 equalexp returns [EqualExp ast] :
 		'(' Equal 
 		    e1=exp 
 			e2=exp 
 		')' { $ast = new EqualExp($e1.ast,$e2.ast); }
 		;

 greaterexp returns [GreaterExp ast] :
 		'(' Greater 
 		    e1=exp 
 			e2=exp 
 		')' { $ast = new GreaterExp($e1.ast,$e2.ast); }
 		;

// Expressions related to list

 carexp returns [CarExp ast] :
 		'(' Car 
 		    e=exp 
 		')' { $ast = new CarExp($e.ast); }
 		;

 cdrexp returns [CdrExp ast] :
 		'(' Cdr 
 		    e=exp 
 		')' { $ast = new CdrExp($e.ast); }
 		;

 consexp returns [ConsExp ast] :
 		'(' Cons 
 		    e1=exp 
 			e2=exp 
 		')' { $ast = new ConsExp($e1.ast,$e2.ast); }
 		;

 listexp returns [ListExp ast] 
        locals [ArrayList<Exp> list]
 		@init { $list = new ArrayList<Exp>(); } :
 		'(' List 
 		    ( e=exp { $list.add($e.ast); } )* 
 		')' { $ast = new ListExp($list); }
 		;

 nullexp returns [NullExp ast] :
 		'(' Null 
 		    e=exp 
 		')' { $ast = new NullExp($e.ast); }
 		;
 
 strexp returns [StrExp ast] :
 		s=StrLiteral { $ast = new StrExp($s.text); } 
 		;

 boolexp returns [BoolExp ast] :
 		TrueLiteral { $ast = new BoolExp(true); } 
 		| FalseLiteral { $ast = new BoolExp(false); } 
 		;
 
 // Other Standard Expressions
 
  numexp returns [NumExp ast]:
 		n0=Number { $ast = new NumExp(Integer.parseInt($n0.text)); } 
  		| '-' n0=Number { $ast = new NumExp(-Integer.parseInt($n0.text)); }
  		| n0=Number Dot n1=Number { $ast = new NumExp(Double.parseDouble($n0.text+"."+$n1.text)); }
  		| '-' n0=Number Dot n1=Number { $ast = new NumExp(Double.parseDouble("-" + $n0.text+"."+$n1.text)); }
  		;		

 addexp returns [AddExp ast]
        locals [ArrayList<Exp> list]
 		@init { $list = new ArrayList<Exp>(); } :
 		'(' '+'
 		    e=exp { $list.add($e.ast); } 
 		    ( e=exp { $list.add($e.ast); } )+
 		')' { $ast = new AddExp($list); }
 		;

 subexp returns [SubExp ast]  
        locals [ArrayList<Exp> list]
 		@init { $list = new ArrayList<Exp>(); } :
 		'(' '-'
 		    e=exp { $list.add($e.ast); } 
 		    ( e=exp { $list.add($e.ast); } )+ 
 		')' { $ast = new SubExp($list); }
 		;

 multexp returns [MultExp ast] 
        locals [ArrayList<Exp> list]
 		@init { $list = new ArrayList<Exp>(); } :
 		'(' '*'
 		    e=exp { $list.add($e.ast); } 
 		    ( e=exp { $list.add($e.ast); } )+ 
 		')' { $ast = new MultExp($list); }
 		;
 
 divexp returns [DivExp ast] 
        locals [ArrayList<Exp> list]
 		@init { $list = new ArrayList<Exp>(); } :
 		'(' '/'
 		    e=exp { $list.add($e.ast); } 
 		    ( e=exp { $list.add($e.ast); } )+ 
 		')' { $ast = new DivExp($list); }
 		;

 varexp returns [VarExp ast]: 
 		id=Identifier { $ast = new VarExp($id.text); }
 		;

 letexp  returns [LetExp ast] 
        locals [ArrayList<String> names, ArrayList<Exp> value_exps]
 		@init { $names = new ArrayList<String>(); $value_exps = new ArrayList<Exp>(); } :
 		'(' Let 
 			'(' ( '(' id=Identifier e=exp ')' { $names.add($id.text); $value_exps.add($e.ast); } )+  ')'
 			body=exp 
 			')' { $ast = new LetExp($names, $value_exps, $body.ast); }
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
 Invoyage 'in\'voyage' ;
 Ferall 'fer\'all' ;
 Booty 'booty' ;

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
 
 fragment ESCQUOTE : '\\"';
 String :   '"' ( ESCQUOTE | ~('\n'|'\r') )*? '"';
 	