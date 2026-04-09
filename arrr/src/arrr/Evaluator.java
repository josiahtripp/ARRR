package arrr;
import static arrr.AST.*;
import static arrr.Value.*;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import arrr.AST.AddExp;
import arrr.AST.BoolExp;
import arrr.AST.CallExp;
import arrr.AST.CarExp;
import arrr.AST.CdrExp;
import arrr.AST.ConsExp;
import arrr.AST.DefineDecl;
import arrr.AST.DivExp;
import arrr.AST.EqualExp;
import arrr.AST.EvalExp;
import arrr.AST.GreaterExp;
import arrr.AST.IfExp;
import arrr.AST.LambdaExp;
import arrr.AST.LessExp;
import arrr.AST.LetExp;
import arrr.AST.ListExp;
import arrr.AST.MultExp;
import arrr.AST.NullExp;
import arrr.AST.NumExp;
import arrr.AST.Program;
import arrr.AST.ReadExp;
import arrr.AST.StrExp;
import arrr.AST.SubExp;
import arrr.AST.UnitExp;
import arrr.AST.Visitor;
import arrr.Env.*;
import arrr.Type.IntegerType;
import arrr.Type.StringType;
import arrr.Value.BoolVal;
import arrr.Value.DynamicError;
import arrr.Value.NumVal;
import arrr.Value.PairVal;
import arrr.Value.StringVal;
import arrr.Value.UnitVal;

public class Evaluator implements Visitor<Value> {
	
	Printer.Formatter ts = new Printer.Formatter();

	Env initEnv = initialEnv(); //New for definelang
	
	Value valueOf(Program p) {
			return (Value) p.accept(this, initEnv);
	}

	Value callVessel(){

		/* main (vessel) call implementation */
		try{
			Value vesselVal = initEnv.get("vessel");

			if(vesselVal instanceof Value.FunVal){

				CallExp callVessel = new CallExp(new VarExp("vessel"), new ArrayList<Exp>());
				return (Value) callVessel.accept(this, initEnv);
			}
			return new NumVal(-1);

		} catch (Exception e){

			System.out.println("ARRR! There be no seaworthy vessel in this port! (Program is missing entry point \"vessel\")");
			return new DynamicError(e.getMessage());
		}
	}

	@Override
	public T visit(AST.Program e, Env env){

	}
	@Override
	public Value visit(AST.FunctionDefinition e, Env env){
		List<String> formals = new ArrayList<String>();
		for(AST.ParameterDeclaration p: e.parameters()){
			formals.add(p.name());
		}	
		Value.FunVal fun = new Value.FunVal(env, formals, e.body());
		if (env instanceof GlobalEnv){
			((GlobalEnv) env).extend(e.name(), fun);
		}
		return new UnitVal();
	}

	@Override
	public Value visit(AST.ParameterDeclaration e, Env env){
		return new UnitVal();
	}

	@Override
	public Value visit(AST.CompoundStatement e, Env env){
		Env localEnv = env;

    	for (AST.Declaration d : e.declarations()) {
        	d.accept(this, localEnv);
    	}

    	for (AST.Statement s : e.statements()) {
    	    Value v = (Value) s.accept(this, localEnv);

        	if (v instanceof ReturnVal) {
        	    return v;
    	    }
		}

    	return new UnitVal();
	}

	@Override
	public Value visit(AST.VariableDeclaration e, Env env) {
	    AST.Declarator d = e.declarator();
	    String name = d.name();

	    Value value = new UnitVal();
	    if (d.initializer() != null) {
	        value = (Value) d.initializer().accept(this, env);
	    }

	    if (env instanceof GlobalEnv) {
	        ((GlobalEnv) env).extend(name, value);
	    }

	    return new UnitVal();
	}

	@Override
	public Value visit(AST.ArrayDeclaration e, Env env){
		int size = (int) ((NumVal) e.sizeExpression().accept(this, env)).v();
		List<Value> arr = new ArrayList<>();
		for(int i=0; i<size; i++){
			arr.add(new UnitVal());
		}
		Value arrayVal = new Value.ArrayVal(arr);
		if (env instanceof GlobalEnv) {
			((GlobalEnv) env).extend(e.name(), arrayVal);
		}
		return new UnitVal();
	}

	@Override
	public Value visit(AST.Declarator e, Env env){
		if(e.initializer() != null){
			return (Value) e.initializer().accept(this, env);
		}
		return new UnitVal();
	}
	@Override
	public Value visit(AST.NegationExpression p, Env env){// Should always return 1 or 0
		Value v = (Value) e.getExpression().accept(this, env);
		if(v instanceof NumVal){
			return new NumVal(((NumVal) v).v() == 0 ? 1 : 0);
		}
		System.out.println("tisnot expects an integer expression");
		return new DynamicError(e.getMessage());
	} 
	@override
	public Value visit(AST.ArrayAccessExpression e, Env env);{// Should always return an integer or string
    	Value arrVal = env.get(e.name());
    	if (!(arrVal instanceof ArrayVal)) {
    	    return new DynamicError(e.name() + " is not an array");
		}

    	Value idxVal = (Value) e.index().accept(this, env);
    	if (!(idxVal instanceof NumVal)) {
    	    return new DynamicError("array index must be an integer");
    	}

    	int index = (int) ((NumVal) idxVal).v();
    	List<Value> elements = ((ArrayVal) arrVal).elements();

    	if (index < 0 || index >= elements.size()) {
    	    return new DynamicError("array index out of bounds");
    	}

    	return elements.get(index);
	}
	@override
	public Value visit(AST.FunctionCallExpression e, Env env){// Should always return the return type of the function
    	Value fn = env.get(e.name());

    	if (!(fn instanceof Value.FunVal)) {
    	    return new DynamicError(e.name() + " is not a function");
    	}

    	Value.FunVal fun = (Value.FunVal) fn;

    	List<Value> actuals = new ArrayList<>();
    	for (AST.Expression arg : e.arguments()) {
    	    actuals.add((Value) arg.accept(this, env));
    	}

    	List<String> formals = fun.formals();
    	if (formals.size() != actuals.size()) {
    	    return new DynamicError("argument count mismatch in call to " + e.name());
    	}

    	Env funEnv = fun.env();
    	for (int i = 0; i < formals.size(); i++) {
    	    funEnv = new ExtendEnv(funEnv, formals.get(i), actuals.get(i));
    	}

    	Value result = (Value) fun.body().accept(this, funEnv);

    	if (result instanceof ReturnVal) {
    	    return ((ReturnVal) result).value();// Should always return the return type of the embedded function
	
    	}

    	return result;
	} 
	@Override
	public Value visit(AST.EmbeddedFunctionCallExpression e, Env env) {
	    String name = e.name();
	    List<AST.Expression> args = e.arguments();

	    switch (name) {
	        case "yohoho": {
	            if (args.size() != 2) {
	                return new DynamicError("yohoho expects 2 arguments");
	            }

	            int min = (int) ((NumVal) args.get(0).accept(this, env)).v();
	            int max = (int) ((NumVal) args.get(1).accept(this, env)).v();

	            int r = min + (int)(Math.random() * (max - min + 1));
	            return new NumVal(r);
	        }
	
	        case "squawk": {
	            if (args.size() != 1) {
	                return new DynamicError("squawk expects 1 argument");
	            }
	
	            Value v = (Value) args.get(0).accept(this, env);
	            System.out.println(v);
	            return new StringVal(v.toString());
	        }
	
	        case "avast'ye": {
	            // placeholder until input is implemented
	            return new StringVal("");
	        }
	
	        case "stirthebilge": {
	            return new DynamicError("stirthebilge not implemented yet");
	        }
	
	        default:
	            return new DynamicError("unknown embedded function: " + name);
	    }
	}
	@Override
	public Type visit(AST.ConstantExpression e, Env env){
		return new IntegerType(e.val());
	}

	@Override
	public T visit(AST.StringExpression e, Env env);{
		return new StringType(e.str());
	}
	
	public T visit(AST.VariableExpression e, Env env);

	@Override
	public T visit(AST.MultiplicationExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this)).val();
		int right = ((IntegerType) e.getRight().accept(this)).val();

		// Calculate the result
		int result = left * right;

		return new IntegerType(result);
	}

	@Override
	public T visit(AST.DivisionExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this)).val();
		int right = ((IntegerType) e.getRight().accept(this)).val();

		// Calculate the result
		int result = left / right;

		return new IntegerType(result);
	}

	@Override
	public T visit(AST.ModuloExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this)).val();
		int right = ((IntegerType) e.getRight().accept(this)).val();

		// Calculate the result
		int result = left % right;

		return new IntegerType(result);
	}

	@Override
	public T visit(AST.AdditionExpression e, Env env){

		// Evaluate the left and right expressions and retrieve types
		T left_t = e.getRight().accept(this);
		T right_t = e.getRight().accept(this);

		// Check if either expression is StringType
		boolean left_is_str = (left_t instanceof StringType);
		boolean right_is_str = (right_t instanceof StringType);

		// Perform concatenation
		if(left_is_str || right_is_str){

			String result;

			if(left_is_str){ // Left is string
				result += ((StringType) left_t).val();
			}
			else{
				result += ((IntegerType) left_t).val().tostring();
			}

			if(right_is_str){ // Right is string
				result += ((StringType) right_t).val();
			}
			else{
				result += ((IntegerType) right_t).val().tostring();
			}

			return new StringType(result);
		}

		// Both expressions are integers, cast to IntegerType and retrieve values
		int left = ((IntegerType) left_t).val();
		int right = ((IntegerType) right_t).val();

		// Calculate the result
		int result = left + right;

		return new IntegerType(result);
	}

	@Override
	public T visit(AST.SubtractionExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this)).val();
		int right = ((IntegerType) e.getRight().accept(this)).val();

		// Calculate the result
		int result = left - right;

		return new IntegerType(result);
	}

	@Override
	public T visit(AST.GreaterthanExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this)).val();
		int right = ((IntegerType) e.getRight().accept(this)).val();

		return new IntegerType(left > right ? 1 : 0);
	}

	@Override
	public T visit(AST.LessthanExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this)).val();
		int right = ((IntegerType) e.getRight().accept(this)).val();

		return new IntegerType(left < right ? 1 : 0);
	}

	public T visit(AST.EqualityExpression e, Env env){

		// Evaluate the left and right expressions and retrieve types
		T left_t = e.getRight().accept(this);
		T right_t = e.getRight().accept(this);

		// Check if either expression is StringType
		boolean left_is_str = (left_t instanceof StringType);
		boolean right_is_str = (right_t instanceof StringType);

		// Perform string comparison
		if(left_is_str && right_is_str){

			// Both expression are string, cast to StringType and retrieve values
			String left = ((StringType) left_t).val();
			String right = ((StringType) right_t).val();

			return new IntegerType(left == right ? 1 : 0);
		}

		// Both expressions are integers, cast to IntegerType and retrieve values
		int left = ((IntegerType) left_t).val();
		int right = ((IntegerType) right_t).val();

		return new IntegerType(left == right ? 1 : 0);
	}

	public T visit(AST.InequalityExpression e, Env env){

		// Evaluate the left and right expressions and retrieve types
		T left_t = e.getRight().accept(this);
		T right_t = e.getRight().accept(this);

		// Check if either expression is StringType
		boolean left_is_str = (left_t instanceof StringType);
		boolean right_is_str = (right_t instanceof StringType);

		// Perform string comparison
		if(left_is_str && right_is_str){

			// Both expression are string, cast to StringType and retrieve values
			String left = ((StringType) left_t).val();
			String right = ((StringType) right_t).val();

			return new IntegerType(left == right ? 0 : 1);
		}

		// Both expressions are integers, cast to IntegerType and retrieve values
		int left = ((IntegerType) left_t).val();
		int right = ((IntegerType) right_t).val();

		return new IntegerType(left == right ? 0 : 1);
	}

	public T visit(AST.LogicalOrExpression e, Env env); // Should always return 1 or 0
	public T visit(AST.LogicalAndExpression e, Env env); // Should always return 1 or 0 
	public T visit(AST.VariableAssignmentExpression e, Env env); // Should always return the value assigned
	public T visit(AST.ArrayAssignmentExpression e, Env env); // Should always return the value assigned
	public T visit(AST.ExpressionStatement e, Env env); // Should not return anything (evaluate expression)
	public T visit(AST.SelectionStatement e, Env env); // Should not return anything
	public T visit(AST.ConditionalLoopStatement e, Env env); // Should not return anything
	public T visit(AST.IterativeLoopStatement e, Env env); // Should not return anything
	public T visit(AST.ReturnStatement e, Env env); // Should not return anything (evaluate expression)
	
	@Override
	public Value visit(AddExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 0;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result += intermediate.v(); //Semantics of AddExp in terms of the target language.
		}
		return new NumVal(result);
	}
	
	@Override
	public Value visit(UnitExp e, Env env) {
		return new UnitVal();
	}

	@Override
	public Value visit(NumExp e, Env env) {
		return new NumVal(e.v());
	}

	@Override
	public Value visit(StrExp e, Env env) {
		return new StringVal(e.v());
	}

	@Override
	public Value visit(BoolExp e, Env env) {
		return new BoolVal(e.v());
	}

	@Override
	public Value visit(DivExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v(); 
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result / rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(MultExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 1;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result *= intermediate.v(); //Semantics of MultExp.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(Program p, Env env) {

		try {
			for(DefineDecl d: p.decls())
				d.accept(this, initEnv);
			
			return (Value) p.e().accept(this, initEnv);

		} catch (ClassCastException e) {
			return new DynamicError(e.getMessage());
		}
	}

	@Override
	public Value visit(SubExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v();
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result - rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(VarExp e, Env env) {
		// Previously, all variables had value 42. New semantics.
		return env.get(e.name());
	}	

	@Override
	public Value visit(LetExp e, Env env) { // New for varlang.
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Value> values = new ArrayList<Value>(value_exps.size());
		
		for(Exp exp : value_exps) 
			values.add((Value)exp.accept(this, env));
		
		Env new_env = env;
		for (int index = 0; index < names.size(); index++)
			new_env = new ExtendEnv(new_env, names.get(index), values.get(index));

		return (Value) e.body().accept(this, new_env);		
	}	
	
	@Override
	public Value visit(DefineDecl e, Env env) { // New for definelang.
		String name = e.name();
		Exp value_exp = e.value_exp();
		Value value = (Value) value_exp.accept(this, env);
		((GlobalEnv) initEnv).extend(name, value);
		return new Value.UnitVal();		
	}	

	@Override
	public Value visit(LambdaExp e, Env env) {
        // Create a function value with three components:
		//  1. formal parameters of the function - e.formals()
		//  2. actual body of the function - e.body()
		//  3. mapping from the free variables in the function body to their values.
		return new Value.FunVal(env, e.formals(), e.body());
	}
	
	@Override
	public Value visit(CallExp e, Env env) {
		Object result = e.operator().accept(this, env);
		if(!(result instanceof Value.FunVal))
			return new Value.DynamicError("Operator not a function in call " +  ts.visit(e, env));
		Value.FunVal operator =  (Value.FunVal) result; //Dynamic checking
		List<Exp> operands = e.operands();

		// Call-by-value semantics
		List<Value> actuals = new ArrayList<Value>(operands.size());
		for(Exp exp : operands) 
			actuals.add((Value)exp.accept(this, env));
		
		List<String> formals = operator.formals();
 		if (formals.size()!=actuals.size())
			return new Value.DynamicError("Argument mismatch in call " + ts.visit(e, env));

		Env fun_env = operator.env();
		for (int index = 0; index < formals.size(); index++)
			fun_env = new ExtendEnv(fun_env, formals.get(index), actuals.get(index));
		
		return (Value) operator.body().accept(this, fun_env);
	}
		
	@Override
	public Value visit(IfExp e, Env env) { // New for .
		Object result = e.conditional().accept(this, env);
		if(!(result instanceof Value.BoolVal))
			return new Value.DynamicError("Condition not a boolean in expression " +  ts.visit(e, env));
		Value.BoolVal condition =  (Value.BoolVal) result; //Dynamic checking
		
		if(condition.v())
			return (Value) e.then_exp().accept(this, env);
		else return (Value) e.else_exp().accept(this, env);
	}

	@Override
	public Value visit(LessExp e, Env env) { // New for .
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env);
		return new Value.BoolVal(first.v() < second.v());
	}
	
	@Override
	public Value visit(EqualExp e, Env env) { // New for .
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env);
		return new Value.BoolVal(first.v() == second.v());
	}

	@Override
	public Value visit(GreaterExp e, Env env) { // New for .
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env);
		return new Value.BoolVal(first.v() > second.v());
	}
	
	@Override
	public Value visit(CarExp e, Env env) { 
		Value.PairVal pair = (Value.PairVal) e.arg().accept(this, env);
		return pair.fst();
	}
	
	@Override
	public Value visit(CdrExp e, Env env) { 
		Value.PairVal pair = (Value.PairVal) e.arg().accept(this, env);
		return pair.snd();
	}
	
	@Override
	public Value visit(ConsExp e, Env env) { 
		Value first = (Value) e.fst().accept(this, env);
		Value second = (Value) e.snd().accept(this, env);
		return new Value.PairVal(first, second);
	}

	@Override
	public Value visit(ListExp e, Env env) { // New for .
		List<Exp> elemExps = e.elems();
		int length = elemExps.size();
		if(length == 0)
			return new Value.Null();
		
		//Order of evaluation: left to right e.g. (list (+ 3 4) (+ 5 4)) 
		Value[] elems = new Value[length];
		for(int i=0; i<length; i++)
			elems[i] = (Value) elemExps.get(i).accept(this, env);
		
		Value result = new Value.Null();
		for(int i=length-1; i>=0; i--) 
			result = new PairVal(elems[i], result);
		return result;
	}	
	
	@Override
	public Value visit(NullExp e, Env env) {
		Value val = (Value) e.arg().accept(this, env);
		return new BoolVal(val instanceof Value.Null);
	}

	public Value visit(EvalExp e, Env env) {
		StringVal programText = (StringVal) e.code().accept(this, env);
		Program p = _reader.parse();
		return (Value) p.accept(this, env);
	}

	public Value visit(ReadExp e, Env env) {
		StringVal fileName = (StringVal) e.file().accept(this, env);
		try {
			String text = Reader.readFile("" + System.getProperty("user.dir") + File.separator + fileName.v());
			return new StringVal(text);
		} catch (IOException ex) {
			return new DynamicError(ex.getMessage());
		}
	}

	private Env initialEnv() {
		GlobalEnv initEnv = new GlobalEnv();
		
		/* Procedure: (read <filename>). Following is same as (define read (lambda (file) (read file))) */
		List<String> formals = new ArrayList<>();
		formals.add("file");
		Exp body = new AST.ReadExp(new VarExp("file"));
		Value.FunVal readFun = new Value.FunVal(initEnv, formals, body);
		initEnv.extend("read", readFun);

		/* Procedure: (require <filename>). Following is same as (define require (lambda (file) (eval (read file)))) */
		formals = new ArrayList<>();
		formals.add("file");
		body = new EvalExp(new AST.ReadExp(new VarExp("file")));
		Value.FunVal requireFun = new Value.FunVal(initEnv, formals, body);
		initEnv.extend("require", requireFun);
		
		/* Add new built-in procedures here */ 
		
		return initEnv;
	}
	
	Reader _reader; 
	public Evaluator(Reader reader) {
		_reader = reader;
	}
}
