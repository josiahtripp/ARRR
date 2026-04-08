grammar ARRR;
 
 program returns [Program ast]
 		locals [ArrayList<ExternalDeclaration> decls] // Local variables within the parser (keeps track of global variables and function definitions)
 		@init { $decls = new ArrayList<ExternalDeclaration>(); } : // Initilization code run for the parser (Creates the array to hold external declarations)

		(ext=externalDeclaration { $decls.add($ext.ast); } )* // A program consists of 0 or more external declarations, add each to the array.

		{ $ast = new Program($decls); } // Finally, evaluate the program using the array of external declarations.
		;

externalDeclaration returns [ExternalDeclaration ast] : // An external declaration is either a global declaration or a function definition
		func=functionDefinition { $ast = $func.ast; } // Function definition
		| var=declaration { $ast = $var.ast; } // Global variable declaration
		;

functionDefinition returns [FunctionDefinition ast] : // This can only be one thing, and must be written only one way
		'canon' id=Identifier 'fires' type=containerSpecifier params=parameterTypeList body=compoundStatement

		{ $ast = new FunctionDefinition($id.text, $type.ast, $params.ast, $body.ast); }
		;

parameterTypeList returns [List<ParameterDeclaration> ast] // List of parameters for a function (0 or more)
		@init { $ast = new ArrayList<ParameterDeclaration>(); } : // Create an array to hold an empty parameter list (returned when there are no parameters)
		( 'with' pl=parameterList { $ast = $pl.ast; } )? // Otherwise, use the list provided by the parameterList rule
		;

parameterList returns [List<ParameterDeclaration> ast] // List of parameters for a function (1 or more)
		@init { $ast = new ArrayList<ParameterDeclaration>(); } : // Create an array to hold the parameter declarations
		first=parameterDeclaration { $ast.add($first.ast); } // Evaluate and add the first parameter declaration to the list
		( 'an' next=parameterDeclaration { $ast.add($next.ast); } )* // Evaluate and add the rest of the parameters
		;

parameterDeclaration returns [ParameterDeclaration ast] : // A function parameter declaration
		type=containerSpecifier id=Identifier { $ast = new ParameterDeclaration($type.ast, $id.text); } // Create the object to be added to the array list
		;

containerSpecifier returns [Type ast] : // The container type of a function parameter declaration
		t=typeSpecifier { $ast = $t.ast; } // Just a basic variable type
		| 'plank' t=typeSpecifier { $ast = new PlankType($t.ast); } // An array type
		;

typeSpecifier returns [Type ast] : // Types in ARRR
		'naught' { $ast = new NaughtType(); } // Void type, only valid for function return type
		| 'tally' { $ast = new TallyType(); } // Integer type
		| 'scroll' { $ast = new ScrollType(); } // String type
		;

compoundStatement returns [CompoundStatement ast] // A function body, loop body, or selection statement body
		locals [List<Declaration> decls, List<Statement> stmts]
		@init { $decls = new ArrayList<Declaration>(); $stmts = new ArrayList<Statement>(); } :

		'{' 
		( d=declaration { $decls.add($d.ast); } )* // All declarations must come before statements
		( s=statement { $stmts.add($s.ast) } )*
		'}'
		;

declaration returns [Declaration ast] : // A declaration for a variable or an array
		t=typeSpecifier d=declarator '!' { $ast = new VariableDeclaration($t.ast, $d.ast); }
		| 'plank' e=expression t=typeSpecifier id=Identifier '!' { $ast = new PlankDeclaration($e.ast, $t.ast, $id.text); }
		;

declarator returns [Declarator ast] : // Variable declaration or initialization
		id=Identifier  (  'is' e=expression )? { $ast = new Declarator($id.text, $e != null ? $e.ast : null); }
		;

expressionList returns [List<Expression> ast]
		@init { $ast = new ArrayList<Expression>(); } : // Create an array to hold the expressions
		first=expression { $ast.add($first.ast); } // Evaluate and add the first expression to the list
		( 'an' next=expression { $ast.add($next.ast); } )* // Evaluate and add the rest of the expressions
		;

expression returns [Expression ast] :
		'(' e=expression ')' { $ast = $e.ast; } // Unary operator, parenthesis
		| 'tisnot' e=expression { $ast = new NegationExpression($e.ast); } // Unary operator, negation
		| id=Identifier 'at' idx=expression { $ast = new ArrayAccessExpression($id.text, $idx.ast); } // Array access expression
		| 'fire' id=Identifier ( 'with' args=expressionList )? 
		  { $ast = FunctionCallExpression($id.text, $args != null ? $args.ast : new ArrayList<Expression>() ); } // Function call expression
		| func=embeddedFunctionName ( 'with' args=expressionList )?
		  { $ast = EmbeddedFunctionCallExpression($func.name, $args != null ? $args.ast : new ArrayList<Expression>() ); } // Embedded function call expression
		| c=Constant { $ast = new ConstantExpression($c.text); } // Integer literal
		| s=String { $ast = new StringExpression($s.text); } // String literal
		| id=Identifier { $ast = new VariableExpression($id.text); } // Variable
		| left=expression op=( 'stack\'em' | 'doleout' | 'dregs' ) right=expression // Multiplicative expression
		  { $ast = new MultiplicativeExpression($left.ast, $op.text, $right.ast); }
		| left=expression op=( 'bestow' | 'forkover' )  right=expression // Additive expression
		  { $ast = new AdditiveExpression($left.ast, $op.text, $right.ast); }
		| left=expression op=( 'bemorthan' | 'belessthan' ) right=expression // Relational Expression
		  { $ast = new RelationalExpression($left.ast, $op.text, $right.ast); }
		| left=expression op=( 'be' | 'benot' ) right=expression // Equality Expression
		  { $ast = new EqualityExpression($left.ast, $op.text, $right.ast); }
		| left=expression op=( 'er' | 'n' ) right=expression // Logical OR / AND
		  { $ast = new LogicalExpression($left.ast, $op.text, $right.ast); }
		| <assoc=right> id=Identifier 'is' right=expression // Variable assignment expression
      	  { $ast = new AssignmentExp($id.text, $right.ast); }
    	| <assoc=right> id=Identifier 'at' idx=expression 'is' right=expression // Array index assignment
      	  { $ast = new ArrayAssignmentExpression($id.text, $idx.ast, $right.ast); }
		;

embeddedFunctionName returns [String name] : // Names of embedded functions
		'yohoho'       { $name = "yohoho"; }
    	| 'stirthebilge' { $name = "stirthebilge"; }
    	| 'squawk'       { $name = "squawk"; }
    	| 'avast\'ye'    { $name = "avast'ye"; }
    	;

statement returns [Statement ast] :
		e=expression '!' { $ast = $e.ast; } // Expression statement (assignment, function call, etc)
		| 'aye' '(' cond=expression ')' cs=compoundStatement ( 'scurvy' scs=compoundStatement )? // If or If/Else
		  { $ast = new SelectionStatement($cond.ast, $cs.ast, $scs != null ? $scs.ast : null); }
		| 'in\'voyage' '(' cond=expression ')' cs=compoundStatement // While loop
		  { $ast = new ConditionalLoopStatement($cond.ast, cs.ast); }
		| 'fer\'all' '(' ( init=expression )? '!' ( cond=expression )? '!' ( incr=expression )? ')' cs=compoundStatement // For loop
		  { $ast = new IterativeLoopStatement($init != null ? $init.ast : null, $cond != null ? $cond.ast : null, $incr != null ? $incr.ast : null, $cs.ast); }
		| 'booty' ( e=expression )? '!' {$ast = $e.ast}
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


 Define : 'define' ;
 Let : 'let' ;
 Dot : '.' ;
 Lambda : 'lambda' ;
 If : 'if' ; 
 Car : 'car' ; 
 Cdr : 'cdr' ; 
 Cons : 'cons' ; 
 List : 'list' ; 
 Null : 'null?' ; 
 Less : '<' ;
 Equal : '=' ;
 Greater : '>' ;
 TrueLiteral : '#t' ;
 FalseLiteral : '#f' ;

 Number : DIGIT+ ;

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

 AT : '@';
 ELLIPSIS : '...';
 WS  :  [ \t\r\n\u000C]+ -> skip;
 Comment :   '/*' .*? '*/' -> skip;
 Line_Comment :   '//' ~[\r\n]* -> skip;
 
 fragment ESCQUOTE : '\\"';
 StrLiteral :   '"' ( ESCQUOTE | ~('\n'|'\r') )*? '"';
 	