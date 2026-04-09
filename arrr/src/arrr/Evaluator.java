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
	public T visit(AST.FunctionCallExpression e, Env env); // Should always return the return type of the function
	public T visit(AST.EmbeddedFunctionCallExpression e, Env env); // Should always return the return type of the embedded function

	@Override
	public Type visit(AST.ConstantExpression e, Env env){
		return new IntegerType(e.val());
	}

	@Override
	public T visit(AST.StringExpression e, Env env);{
		return new StringType(e.str());
	}
	
	public T visit(AST.VariableExpression e, Env env); // Should always return an integer, string, or plank (reference)

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

		// Evaluate the left and right expression and retrive types
		T left_t = e.getRight().accept(this);
		T right_t = e.getRight().accept(this);

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

	public T visit(AST.GreaterthanExpression e, Env env); // Should always return 1 or 0
	public T visit(AST.LessthanExpression e, Env env); // Should always return 1 or 0
	public T visit(AST.EqualityExpression e, Env env); // Should always return 1 or 0
	public T visit(AST.InequalityExpression e, Env env); // Should always return 1 or 0
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
