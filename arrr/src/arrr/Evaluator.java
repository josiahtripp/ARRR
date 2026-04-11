package arrr;
import static arrr.AST.*;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.lang.model.type.UnknownTypeException;
import javax.management.openmbean.ArrayType;

import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.IOException;

import arrr.AST.CompoundStatement;
import arrr.AST.Declaration;
import arrr.AST.ExternalDeclaration;
import arrr.AST.ParameterDeclaration;
import arrr.AST.Program;
import arrr.AST.Visitor;
import arrr.Environment.*;
import arrr.Type.*;

public class Evaluator implements Visitor<Type> {

	// Create the global base environment
	Environment global_environment = new Environment(null);
	
	// Build the global environment by evaluating the program
	Type buildProgramEnvironment(Program p) {
		return (Type) p.accept(this, global_environment);
	}

	// Invoke the program and execute it!
	void executeProgram(){

		// Get the function type object
		Type func_t =  global_environment.get("vessel");

		// Function does not exist
		if(!(func_t instanceof FunctionType)){
			throw new RuntimeException("There be no seaworthy ship in the port! (No program entry point)");
		}

		FunctionType func = ((FunctionType) func_t);

		// Wrong number of arguments or wrong return type
		if(func.params().size() != 0 || !(func.type() instanceof VoidType)){
			throw new RuntimeException("Entry point invalid");
		}

		// Evaluate the body compound statement
		func.body().accept(this, global_environment);
	}

	@Override
	public Type visit(AST.Program e, Environment env){

		// Evaluate all external declarations
		for(ExternalDeclaration decl : e.decls()){
			decl.accept(this, env);
		}

		return new UnitType();
	}

	@Override
	public Type visit(AST.FunctionDefinition e, Environment env){

		// Retrieve the name of the function
		String name = e.id();

		// Function name already in use
		if(!(env.get(name) instanceof UnitType)){
			throw new RuntimeException("Function definition for " + name + " invalid: Redeclaration of Identifier");
		}

		// Create the new function type object
		FunctionType func = new FunctionType(e.type(), e.params(), e.body());

		// Add the function type object to the current (global) environment
		env.set(name, func);

		return new UnitType();
	}

	@Override
	public Type visit(AST.ParameterDeclaration e, Environment env){
		return new UnitType();
	}

	@Override
	public Type visit(AST.CompoundStatement e, Environment env){

		// Evaluate all declarations
		for(Declaration decl : e.decls()){
			decl.accept(this, env);
		}

		// Evaluate all statements
		for(Statement stmt : e.stmts()){

			// Evaluate the statement
			Type result = (Type) stmt.accept(this, env);

			// Return statement evaluated, return expression
			if(!(result instanceof UnitType)){
				return result;
			}
		}

		// All evaluated, return UnitType to indicate end reached without return statement
		return new UnitType();
	}

	@Override
	public Type visit(AST.VariableDeclaration e, Environment env){

		// Retrive the type of the variable
		Type type = e.type();

		// Retrieve the name of the variable
		String name = e.id();

		Type init = new UnitType();

		// Retrieve the initializer value
		if(e.exp() != null){

			// Evaluate the expression
			init = e.exp().accept(this, env);
		}

		// Variable name already in use in current scope
		if(!(env.getCurrent(name) instanceof UnitType)){
			throw new RuntimeException("Variable declaration for " + name + " invalid: Identifier already in use in current scope");
		}

		// String variable
		if(type instanceof StringType){

			if(init instanceof UnitType){ // No initializer
				init = new StringType("");
			}
			else{

				if(init instanceof IntegerType){ // Integer initializer (convert to string)
					init = new StringType(String.valueOf(((IntegerType) init).val()));
				}
				else if(init instanceof StringType){ // String initializer 
				}
				else{ // Error
					throw new RuntimeException("");
				}
			}
		}

		// Integer variable
		if(type instanceof IntegerType){

			if(init instanceof UnitType){ // No initializer
				init = new IntegerType(0);
			}
			else{

				if(!(init instanceof IntegerType)){
					throw new RuntimeException("");
				}
			}
		}

		// Invalid variable type
		if(init instanceof UnitType){
			throw new RuntimeException("Invalid varible type: " + init.getClass().toString());
		}

		// Set the variable in the current env
		env.set(name, init);

		return new UnitType();
	}

	@Override
	public Type visit(AST.ArrayDeclaration e, Environment env){

		// Evaluate the array size expression
		int size = ((IntegerType) e.exp().accept(this, env)).val();

		// Retrieve the element type
		Type type = e.type();

		// Retrieve the name of the array
		String name = e.id();

		// Array name already in use in current scope
		if(!(env.getCurrent(name) instanceof UnitType)){
			throw new RuntimeException("Array declaration for " + name + " invalid: Identifier already in use in current scope");
		}

		// String element type
		if(type instanceof StringType){

			List<Type> list = new ArrayList<Type>(size);
			for(int i = 0; i < size; i++){
				list.set(i, new StringType(""));
			}

			Type arr = new ArrType(new StringType(), list);
			env.set(name, arr);
			return new UnitType();
		}

		// Integer element type
		if(type instanceof IntegerType){

			List<Type> list = new ArrayList<Type>(size);
			for(int i = 0; i < size; i++){
				list.set(i, new IntegerType(0));
			}

			Type arr = new ArrType(new IntegerType(), list);
			env.set(name, arr);
			return new UnitType();
		}

		// Invalid element type
		throw new RuntimeException("Invalid Array element type: " + type.getClass().toString());
	}

	@Override
	public Type visit(AST.NegationExpression e, Environment env){

		// Evaluate expression to integer
		int result = ((IntegerType) e.getExp().accept(this, env)).val();

		return new IntegerType(result == 0 ? 1 : 0);
	}

	@Override
	public Type visit(AST.ArrayAccessExpression e, Environment env){
		
		Type result = env.get(e.id());

		if(result instanceof UnitType){
			throw new RuntimeException("Undeclared identifier \"" + e.id() + "\"");
		}

		int idx = ((IntegerType) e.idx().accept(this, env)).val();

		return ((ArrType)result).val().get(idx);
	}

	@Override
	public Type visit(AST.FunctionCallExpression e, Environment env){

		// Get the function type object
		FunctionType func = (FunctionType) env.get(e.id());

		// Wrong number of arguments
		if(e.args().size() != func.params().size()){
			throw new RuntimeException("Function call argument mismatch: Expected " + func.params().size() + " but got " + e.args().size());
		}

		// Create a new environment for the body of the function call
		Environment body_env = new Environment(env);

		// Evaluate all argument expressions
		for(int i = 0; i < e.args().size(); i++){

			// Evaluate the argument expression
			Type result = e.args().get(i).accept(this, env);

			// Convert integer to string type
			if(func.params().get(i).type() instanceof StringType && result instanceof IntegerType){
				result = new StringType(String.valueOf(((IntegerType) result).val()));
			}

			// Argument type mismatch
			if(result.getClass() != func.params().get(i).type().getClass()){
				throw new RuntimeException("Function call: mismatch parameter type");
			}

			// Array element type mismatch
			if(result instanceof ArrType){

				if(((ArrType) result).type().getClass() 
					!= ((ArrType) func.params().get(i).type()).type().getClass()){
						throw new RuntimeException("Function call: mismatch parameter type");
				}
			}

			// Add the parameter to the function body environment
			body_env.set(func.params().get(i).id(), result);
		}

		// Evaluate the body compound statement
		Type result = func.body().accept(this, body_env);

		// Void function returned without return statement
		if(func.type() instanceof VoidType){
			if(result instanceof UnitType){
				result = new VoidType();
			}
		}

		// Mismatch return type
		if(result.getClass() != func.type().getClass()){
			throw new RuntimeException("Function call: mismatch return type");
		}

		// Mismatch return array element type
		if(result instanceof ArrType){
			if(((ArrType) result).type().getClass() != ((ArrType) func.type()).type().getClass()){
				throw new RuntimeException("Function call: mismatch return type");
			}
		}

		return result;
	}

	@Override
	public Type visit(AST.EmbeddedFunctionCallExpression e, Environment env){

		// Get the name of the embedded function
		String name = e.name();

		// Create an argument list to hold evaluated argument expressions
		List<Type> args = new ArrayList<Type>();

		// Evaluate all argument expressions
		for(Expression exp : e.args()){

			args.add(exp.accept(this, env));
		}

		// Random number specified range
		if(name == "yohoho"){

			// Incorrect number of arguments
			if(args.size() != 2){
				throw new RuntimeException("yohoho argument number mismatch: expected 2 but got " + args.size());
			}

			// Get the range values
			int start = ((IntegerType) args.get(0)).val();
			int stop = ((IntegerType) args.get(1)).val();

			// Invalid range
			if(start >= stop){
				throw new RuntimeException("yohoho invalid range: expected at least 1 integer but got [" + start + ", " + stop + "]");
			}

			// Create the Random instance
			Random rand = new Random();

			// Return the new random integer (Does not check bounds)
			return new IntegerType(start + rand.nextInt((stop - start) + 1));
		}

		// Shuffle array
		if(name == "stirthebilge"){

			// Incorrect number of arguments
			if(args.size() != 1){
				throw new RuntimeException("stirthebilge argument number mismatch: expected 1 but got " + args.size());
			}

			// Get the array (first argument)
			List<Type> arr = ((ArrType) args.get(0)).val();

			// Shuffle the array
			Collections.shuffle(arr);

			// Return the ArrType object (argument)
			return (ArrType) args.get(0);
		}

		// Output
		if (name == "squawk"){

			// Incorrect number of arguments
			if(args.size() != 1){
				throw new RuntimeException("squawk argument number mismatch: expected 1 but got " + args.size());
			}

			// Get the message type (first argument)
			Type msg_t = args.get(0);

			// Create an empty message
			String msg;

			// Convert from int to string
			if(msg_t instanceof IntegerType){
				msg = String.valueOf(((IntegerType) args.get(0)).val());
			}
			else{ // Capture string
				msg = ((StringType) args.get(0)).val();
			}

			// Print message
			System.out.print(msg);

			// Return printed message as new StringType object
			return new StringType(msg);
		}

		if(name == "avast'ye"){

			// Incorrect number of arguments
			if(args.size() != 0){
				throw new RuntimeException("avast'ye argument number mismatch: expected 0 but got " + args.size());
			}

			// Create an input scannner
			Scanner scanner = new Scanner(System.in);

			// Read a line of input
			String input = scanner.nextLine();
			
			// Get the first "word" (character cluster) in the input string
			input = input.split("\\s+")[0];

			// Return as integer if possible
			try{
				return new IntegerType(Integer.parseInt(input));
			}
			catch (NumberFormatException ecpt){ // Return as string if else
				return new StringType(input);
			}
		}

		return new UnitType();
	}

	@Override
	public Type visit(AST.ConstantExpression e, Environment env){
		return new IntegerType(e.val());
	}

	@Override
	public Type visit(AST.StringExpression e, Environment env){
		return new StringType(e.str());
	}
	
	@Override
	public Type visit(AST.VariableExpression e, Environment env){

		Type result = env.get(e.id());

		if(result instanceof UnitType){
			throw new RuntimeException("Undeclared identifier \"" + e.id() + "\"");
		}

		return result;
	}

	@Override
	public Type visit(AST.MultiplicationExpression e, Environment env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		// Calculate the result
		int result = left * right;

		return new IntegerType(result);
	}

	@Override
	public Type visit(AST.DivisionExpression e, Environment env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		// Calculate the result
		int result = left / right;

		return new IntegerType(result);
	}

	@Override
	public Type visit(AST.ModuloExpression e, Environment env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		// Calculate the result
		int result = left % right;

		return new IntegerType(result);
	}

	@Override
	public Type visit(AST.AdditionExpression e, Environment env){

		// Evaluate the left and right expressions and retrieve types
		Type left_t = e.getRight().accept(this, env);
		Type right_t = e.getRight().accept(this, env);

		// Check if either expression is StringType
		boolean left_is_str = (left_t instanceof StringType);
		boolean right_is_str = (right_t instanceof StringType);

		// Perform concatenation
		if(left_is_str || right_is_str){

			String result = "";

			if(left_is_str){ // Left is string
				result += ((StringType) left_t).val();
			}
			else{
				result += String.valueOf(((IntegerType) left_t).val());
			}

			if(right_is_str){ // Right is string
				result += ((StringType) right_t).val();
			}
			else{
				result += String.valueOf(((IntegerType) right_t).val());
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
	public Type visit(AST.SubtractionExpression e, Environment env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		// Calculate the result
		int result = left - right;

		return new IntegerType(result);
	}

	@Override
	public Type visit(AST.GreaterthanExpression e, Environment env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		return new IntegerType(left > right ? 1 : 0);
	}

	@Override
	public Type visit(AST.LessthanExpression e, Environment env){

		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		return new IntegerType(left < right ? 1 : 0);
	}

	@Override
	public Type visit(AST.EqualityExpression e, Environment env){

		// Evaluate the left and right expressions and retrieve types
		Type left_t = e.getRight().accept(this, env);
		Type right_t = e.getRight().accept(this, env);

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
	public Type visit(AST.InequalityExpression e, Environment env){

		// Evaluate the left and right expressions and retrieve types
		Type left_t = e.getRight().accept(this, env);
		Type right_t = e.getRight().accept(this, env);

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
	public Type visit(AST.LogicalOrExpression e, Environment env){
		
		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		return new IntegerType((left != 0 || right != 0) ? 1 : 0);
	}

	@Override
	public Type visit(AST.LogicalAndExpression e, Environment env){
		
		// Evaluate the left and right expressions (Must be IntegerType)
		int left = ((IntegerType) e.getLeft().accept(this, env)).val();
		int right = ((IntegerType) e.getRight().accept(this, env)).val();

		return new IntegerType((left != 0 && right != 0) ? 1 : 0);
	}

	@Override
	public Type visit(AST.VariableAssignmentExpression e, Environment env){

		// Get the variable object
		Type type = env.get(e.id());

		// Variable does not exist
		if(type instanceof UnitType){
			throw new RuntimeException("");
		}

		// Evaluate the expression to assign
		Type value = e.exp().accept(this, env);

		// Integer variable 
		if(type instanceof IntegerType){

			if(value instanceof IntegerType){ // Expresison is integer type
				env.set(e.id(), value);
				return value;
			}
		}

		// String variable
		else if(type instanceof StringType){

			if(value instanceof IntegerType){ // Expression is integer type
				String str = String.valueOf(((IntegerType) value).val());
				value = new StringType(str);
			}

			if(value instanceof StringType){ // Expression is string type
				env.set(e.id(), value);
				return value;
			}
		}

		// Array
		else if(type instanceof ArrType){
			
			// Elements are the same type
			if(((ArrType) type).type().getClass() == ((ArrType) value).type().getClass()){

				env.set(e.id(), value);
				return value;
			}
		}

		// Invalid expression type to be assigned
		throw new RuntimeException("Invalid variable assignment: Expected type " + type.getClass().getSimpleName() + " but got " + value.getClass().getSimpleName());
	}

	@Override
	public Type visit(AST.ArrayAssignmentExpression e, Environment env){

		// Get the Array object
		ArrType arr = (ArrType) env.get(e.id());

		// Evaluate the index expression
		int idx = ((IntegerType) e.idx().accept(this, env)).val();
	
		// Evaluate the expression to assign
		Type value = e.exp().accept(this, env);

		// Integer element array
		if(arr.type() instanceof IntegerType){

			if(value instanceof IntegerType){ // Expression is integer type
				arr.setIdx(idx, value);
				return value;
			}
		}

		// String element array
		else if(arr.type() instanceof StringType){

			if(value instanceof IntegerType){ // Expression is integer type
				String str = String.valueOf(((IntegerType) value).val());
				value = new StringType(str);
			}

			if(value instanceof StringType){ // Expression is string type
				arr.setIdx(idx, value);
				return value;
			}
		}

		return new UnitType();
	}

	@Override
	public Type visit(AST.ExpressionStatement e, Environment env){

		// Evaluate the expression
		e.exp().accept(this, env);
		
		// Return UnitType (Statement should not return anything)
		return new UnitType();
	}

	@Override
	public Type visit(AST.SelectionStatement e, Environment env){
		
		// Evaluate the condition expression
		int cond = ((IntegerType) e.cond().accept(this, env)).val();

		// Check the condition
		if(cond != 0){

			// Create a new environment with the current as its parent
			Environment tbody_env = new Environment(env);

			// Evaluate the body compound statement
			Type result = (Type) e.tbody().accept(this, tbody_env);

			// Return statement evaluated
			if(!(result instanceof UnitType)){
				throw new RuntimeException("selection statement: Invalid return statement");
			}
			
		}
		else if(e.fbody() != null){

			// Create a new environment with the current as its parent
			Environment fbody_env = new Environment(env);

			// Evaluate the body compound statement
			Type result = (Type) e.fbody().accept(this, fbody_env);

			// Return statement evaluated
			if(!(result instanceof UnitType)){
				throw new RuntimeException("selection statement: Invalid return statement");
			}
		}

		return new UnitType();
	}

	@Override
	public Type visit(AST.ConditionalLoopStatement e, Environment env){

		// Evaluate the condition expression
		int cond = ((IntegerType) e.cond().accept(this, env)).val();

		// Check the condition, run until it fails
		while(cond != 0){

			// Create a new environment with the current as its parent
			Environment body_env = new Environment(env);

			// Evaluate the body compound statement
			Type result = (Type) e.body().accept(this, body_env);

			// Return statement evaluated
			if(!(result instanceof UnitType)){
				throw new RuntimeException("conditional loop: Invalid return statement");
			}

			// Check the condition again
			cond = ((IntegerType) e.cond().accept(this, env)).val();
		}

		return new UnitType();
	}

	@Override
	public Type visit(AST.IterativeLoopStatement e, Environment env){

		int inf = 0; // Infinite loop, condition expression is null
		int cond = 0;

		// Evaluate the initial expression
		if(e.init() != null){
			e.init().accept(this, env);
		}

		// Evaluate the condition expression
		if(e.cond() != null){
			cond = ((IntegerType) e.cond().accept(this, env)).val();
		}
		else{
			inf = 1;
		}

		// Check the condition, run until it fails
		while(cond != 0 || inf != 0){

			// Create a new environment with the current as its parent
			Environment body_env = new Environment(env);

			// Evaluate the body compound statement
			Type result = (Type) e.body().accept(this, body_env);

			// Return statement evaluated
			if(!(result instanceof UnitType)){
				throw new RuntimeException("iterative conditional loop: Invalid return statement");
			}

			// Evaluate the increment expression
			if(e.incr() != null){
				e.incr().accept(this, env);
			}

			// Evaluate the condition again
			if(inf == 0){
				cond = ((IntegerType) e.cond().accept(this, env)).val();
			}
		}

		return new UnitType();
	}

	@Override
	public Type visit(AST.ReturnStatement e, Environment env){
		
		// If an expression is provided in the return statement, return it
		if(e.exp() != null){

			return e.exp().accept(this, env);
		}
		
		// Otherwise, return a void type
		return new VoidType();
	}
}
