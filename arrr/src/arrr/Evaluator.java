package arrr;
import static arrr.AST.*;
import static arrr.Value.*;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
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
import arrr.Type.UnitType;
import arrr.Type.VoidType;
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

	@override
	public T visit(AST.Program e, Env env){

	}

	public T visit(AST.FunctionDefinition e, Env env);
	public T visit(AST.ParameterDeclaration e, Env env);
	public T visit(AST.CompoundStatement e, Env env);
	public T visit(AST.VariableDeclaration e, Env env);
	public T visit(AST.ArrayDeclaration e, Env env);
	public T visit(AST.Declarator e, Env env);
	public T visit(AST.NegationExpression p, Env env); // Should always return 1 or 0
	public T visit(AST.ArrayAccessExpression e, Env env); // Should always return an integer or string
	public T visit(AST.FunctionCallExpression e, Env env){
		
		// TODO:
		// 1. Find the entry for the variable e.id() in the env chain (function name)
		// 1.5 Lookup the entry for the function in the global env
		// 2. Evaluate the parameter declaration list
		// 3. Find the return type of the function
		// 4. Create a new env for the compound statement
		// 5. Evaluate all of the argument expressions, add them as variables to the env using the names and types from the parameter list
		// 6. Evaluate the compound statement
		// 7. If the function is void type, return a new VoidType()
		// 8. If the function is not void type, return a new Type object from a return statement?

		// Get the function name
		String name = e.id();
	}

	@Override
	public T visit(AST.EmbeddedFunctionCallExpression e, Env env){

		// Persistent Random class instance
		static Random rand = null;

		// Get the name of the embedded function
		String name = e.name();

		// Create an argument list to hold evaluated argument expressions
		List<Type> args = new ArrayList<Type>();

		// Evaluate all argument expressions
		for(Type exp : e.args()){

			args.add(exp.accept(this, env));
		}

		// Random number specified range
		if(name == "yohoho"){

			// Incorrect number of arguments
			if(args.size() != 2){
				throw new RuntimeException("yohoho argument number mismatch: expected 2 but got " + args.size().toString());
			}

			// Get the range values
			int start = ((IntegerType) args.get(0)).val();
			int stop = ((IntegerType) args.get(1)).val();

			// Invalid range
			if(start >= stop){
				throw new RuntimeException("yohoho invalid range: expected at least 1 integer but got [" + start.tostring() + ", " + stop.tostring() + "]");
			}

			// Create the Random instance
			if(rand == null){
				rand = new Random();
			}

			// Return the new random integer (Does not check bounds)
			return new IntegerType(start + rand.nextInt((stop - start) + 1));
		}

		// Shuffle array
		if(name == "stirthebilge"){

			// Incorrect number of arguments
			if(args.size() != 1){
				throw new RuntimeException("stirthebilge argument number mismatch: expected 1 but got " + args.size().toString());
			}

			// Get the array (first argument)
			List<Type> arr = ((ArrayType) args.get(0)).val();

			// Shuffle the array
			Collections.shuffle(arr, rand);

			// Return the ArrayType object (argument)
			return (ArrayType) args.get(0);
		}

		// Output
		if (name == "squawk"){

			// Incorrect number of arguments
			if(args.size() != 1){
				throw new RuntimeException("squawk argument number mismatch: expected 1 but got " + args.size().toString());
			}

			// Get the message type (first argument)
			Type msg_t = args.get(0);

			// Create an empty message
			String msg;

			// Convert from int to string
			if(msg_t instanceof IntegerType){
				msg = ((IntegerType) args.get(0)).val().tostring();
			}
			else{ // Capture string
				msg = ((StringType) args.get(0)).val();
			}

			// Print message
			System.out.print(msg);

			// Return printed message as new StringType object
			return new StringType(msg);
		}

		if(name = "avast'ye"){

			// Incorrect number of arguments
			if(args.size() != 0){
				throw new RuntimeException("avast'ye argument number mismatch: expected 0 but got " + args.size().toString());
			}

			// Create an input scannner
			Scanner scanner = new Scanner(System.in)

			// Read a line of input
			String input = scanner.nextLine();
			
			// Get the first "word" (character cluster) in the input string
			input = input.split("\\s+")[0];

			// Return as integer if possible
			try{
				return new IntegerType(Integer.parseInt(input));
			}
			catch (NumberFormatException e){ // Return as string if else
				return new StringType(input);
			}
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
	
	@Override
	public T visit(AST.VariableExpression e, Env env);

	@Override
	public T visit(AST.MultiplicationExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		// Calculate the result
		int result = left * right;

		return new IntegerType(result);
	}

	@Override
	public T visit(AST.DivisionExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		// Calculate the result
		int result = left / right;

		return new IntegerType(result);
	}

	@Override
	public T visit(AST.ModuloExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		// Calculate the result
		int result = left % right;

		return new IntegerType(result);
	}

	@Override
	public T visit(AST.AdditionExpression e, Env env){

		// Evaluate the left and right expressions and retrieve types
		T left_t = e.getRight().accept(this, env);
		T right_t = e.getRight().accept(this, env);

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
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		// Calculate the result
		int result = left - right;

		return new IntegerType(result);
	}

	@Override
	public T visit(AST.GreaterthanExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		return new IntegerType(left > right ? 1 : 0);
	}

	@Override
	public T visit(AST.LessthanExpression e, Env env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		return new IntegerType(left < right ? 1 : 0);
	}

	@Override
	public T visit(AST.EqualityExpression e, Env env){

		// Evaluate the left and right expressions and retrieve types
		T left_t = e.getRight().accept(this, env);
		T right_t = e.getRight().accept(this, env);

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

	@Override
	public T visit(AST.InequalityExpression e, Env env){

		// Evaluate the left and right expressions and retrieve types
		T left_t = e.getRight().accept(this, env);
		T right_t = e.getRight().accept(this, env);

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

	@Override
	public T visit(AST.LogicalOrExpression e, Env env){
		
		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		return new IntegerType((left != 0 || right != 0) ? 1 : 0);
	}

	@Override
	public T visit(AST.LogicalAndExpression e, Env env){
		
		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		return new IntegerType((left != 0 && right != 0) ? 1 : 0);
	}

	@Override
	public T visit(AST.VariableAssignmentExpression e, Env env){

		//((IntegerType) e.exp().accept(this, env)).val()
		// 1. Find the entry for the variable e.id() in the env chain
		// 2. Evaluate the expression with e.exp().accept(this, env).???
		// 3. Update the entry in the env chain
		// 4. Return the value of the expression assigned
	}

	@Override
	public T visit(AST.ArrayAssignmentExpression e, Env env){

		// TODO:
		// 1. Find the entry for the variable e.id() in the env chain
		// 2. Evaluate the expression with e.exp().accept(this, env).???
		// 3. Update the entry in the env chain (at the specified index)
		// 4. Return the value of the expression assigned
	}

	@Override
	public T visit(AST.ExpressionStatement e, Env env){

		// Evaluate the expression
		e.exp().accept(this, env);
		
		// Return UnitType (Statement should not return anything)
		return new UnitType();
	}

	@Override
	public T visit(AST.SelectionStatement e, Env env){
		
		// TODO:
		// Evaluate the condition expression
		int cond = ((IntegerType) e.cond().accept(this, env)).val();

		// Check the condition
		if(cond != 0){

			// Create a new env for the compound statement
			// Run the compound statement tbody()
		}
		else if(e.fbody() != null){

			// Create a new env for the compound statement
			// Run the compound statement fbody()
		}

		return new UnitType();
	}

	@Override
	public T visit(AST.ConditionalLoopStatement e, Env env){

		// TODO:
		// Evaluate the condition expression
		int cond = ((IntegerType) e.cond().accept(this, env)).val();

		// Check the condition, run until it fails
		while(cond != 0){

			// Create a new env for the compound statement
			// Run the compound statement tbody()

			// Check the condition again
			cond = ((IntegerType) e.cond().accept(this, env)).val();
		}

		return new UnitType();
	}

	@Override
	public T visit(AST.IterativeLoopStatement e, Env env){

		// TODO:
		// Evaluate the initial expression
		e.init().accept(this, env)

		// Evaluate the condition expression
		int cond = ((IntegerType) e.cond().accept(this, env)).val();

		// Check the condition, run until it fails
		while(cond != 0){

			// Create a new env for the compound statement
			// Run the compound statement tbody()

			// Evaluate the increment expression
			e.incr().accept(this, env)

			// Evaluate the condition again
			cond = ((IntegerType) e.cond().accept(this, env)).val();
		}

		return new UnitType();
	}

	@Override
	public T visit(AST.ReturnStatement e, Env env){
		
		// If an expression is provided in the return statement, return it
		if(e.exp() != null){

			return e.exp().accept(this, env);
		}
		
		// Otherwise, return a void type
		return new VoidType();
	}
	
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
