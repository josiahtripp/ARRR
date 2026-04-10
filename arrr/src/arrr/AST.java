package arrr;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.List;

import arrr.AST.ASTNode;
import arrr.AST.BinaryComparator;
import arrr.AST.BinaryExpression;
import arrr.AST.CompoundArithExp;
import arrr.AST.CompoundStatement;
import arrr.AST.Declaration;
import arrr.AST.Declarator;
import arrr.AST.ExternalDeclaration;
import arrr.AST.ParameterDeclaration;
import arrr.AST.StringType;
import arrr.AST.TallyType;
import arrr.AST.UnaryExp;
import arrr.AST.UnaryExpression;
import arrr.AST.VoidType;


/**
 * This class hierarchy represents a program and its derivations manipulated by this interpreter.
 * 
 * @author Josiah Tripp
 * 
 */
public interface AST {

	public static abstract class ASTNode implements AST {
		public abstract <T> T accept(Visitor<T> visitor, Env env);
	}

	public static class Program extends ASTNode {
		List<ExternalDeclaration> _decls;

		public Program(List<ExternalDeclaration> decls) {
			_decls = decls;
		}
		
		public List<ExternalDeclaration> decls() {
			return _decls;
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static abstract class ExternalDeclaration extends ASTNode {

	}

	public static class FunctionDefinition extends ExternalDeclaration {
		String _id;
		Type _type;
		List<ParameterDeclaration> _params;
		CompoundStatement _body;

		public FunctionDefinition(String id, Type type, List<ParameterDeclaration> params, CompoundStatement body) {
			_id = id;
			_type = type;
			_params = params;
			_body = body;
		}

		public String id() {
			return _id;
		}

		public ContainerSpecifier type() {
			return _type;
		}

		public List<ExternalDeclaration> params() {
			return _params;
		}

		public CompoundStatement body(){
			return _body;
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class ParameterDeclaration extends ASTNode {
		Type _type;
		String _id;

		public ParameterDeclaration(Type _type, String id) {
			_type = type;
			_id = id;
		}

		public String id() {
			return _id;
		}

		public ContainerSpecifier type(){
			return _type;
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}
	
	public static class CompoundStatement extends ASTNode {
		List<Declaration> _decls;
		List<Statements> _stmts;

		public CompoundStatement(List<Declaration> decls, List<Statements> stmts){
			_decls = decls;
			_stmts = stmts;
		}

		public List<Declaration> decls(){
			return _decls;
		}

		public List<Statement> stmts(){
			return _stmts;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static abstract class Declaration extends ExternalDeclaration {

	}

	public static class VariableDeclaration extends Declaration {
		Type _type;
		Declarator _decl;

		public VariableDeclaration(Type type, Declarator decl) {
			_type = type;
			decl = _decl;
		}

		public TypeSpecifier type() {
			return _type;
		}

		public Declarator decl() {
			return _decl;
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class ArrayDeclaration extends Declaration {
		Expression _exp;
		Type _type;
		String _id;

		public ArrayDeclaration(Expression exp, Type type, String id) {
			_exp = exp;
			_type = type;
			_id = id;
		}

		public Expression exp(){
			return _exp;
		}

		public TypeSpecifier type() {
			return _type;
		}

		public String id() {
			return _id;
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class Declarator extends ASTNode {
		String _id;
		Expression _exp;

		public Declarator(String id, Expresion exp){
			_id = id;
			_exp = exp;
		}

		public String id(){
			return _id;
		}

		public Expression exp(){
			return _exp;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static abstract class Expression extends ASTNode {

	}

	public static abstract class UnaryExpression extends Expression {
		Expression _exp;

		public UnaryExp(Expression exp) {
			_exp = exp;
		}

		public Exp getExp() {
			return _exp;
		}
	}

	public static class NegationExpression extends UnaryExpression {

		public NegationExpression(Expression exp){
			super(exp);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class ArrayAccessExpression extends Expression {
		String _id;
		Expression _idx;

		public ArrayAccessExpression(String id, Expression idx){
			_id = id;
			_idx = idx;
		}

		public String id(){
			return _id;
		}

		public Expression idx(){
			return _idx;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class FunctionCallExpression extends Expression {
		String _id;
		List<Expression> _args;

		public FunctionCallExpression(String id, List<Expression> args){
			_id = id;
			_args = args;
		}

		public String id(){
			return _id;
		}

		public List<Expression> args(){
			return _args;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class EmbeddedFunctionCallExpression extends Expression {
		String _name;
		List<Expression> _args;

		public EmbeddedFunctionCallExpression(String id, List<Expression> args){
			_name = name;
			_args = args;
		}

		public String name(){
			return _name;
		}

		public List<Expression> args(){
			return _args;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class ConstantExpression extends Expression {
		private int _val;

		public ConstantExpression(int val){
			_val = val;
		}

		public int val(){
			return _val;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class StringExpression extends Expression {
		private String _str;

		public StringExpression(String val){
			_str = str.substring(1, str.length() - 1);
		}

		public String str(){
			return _str;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class VariableExpression extends Expression {
		String _id;

		public VariableExpression(String id){
			_id = id;
		}

		public String id(){
			return _id;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static abstract class BinaryExpression extends Expression {
		Expression _left;
		Expression _right;

		public BinaryExpression(Expression left, Expression right) {
			_left  = left;
			_right = right;
		}

		public Expression getLeft() {
			return _left;
		}

		public Expression getRight() {
			return _right;
		}
	}

	public static class MultiplicationExpression extends BinaryExpression {

		public MultiplicationExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class DivisionExpression extends BinaryExpression {

		public DivisionExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class ModuloExpression extends BinaryExpression {

		public ModuloExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class AdditionExpression extends BinaryExpression {

		public AdditionExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class SubtractionExpression extends BinaryExpression {

		public SubtractionExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}
	
	public static class GreaterthanExpression extends BinaryExpression {

		public GreaterthanExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class LessthanExpression extends BinaryExpression {

		public LessthanExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class EqualityExpression extends BinaryExpression {

		public EqualityExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class InequalityExpression extends BinaryExpression {

		public InequalityExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class LogicalOrExpression extends BinaryExpression {

		public LogicalOrExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class LogicalAndExpression extends BinaryExpression {

		public LogicalAndExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class VariableAssignmentExpression extends Expression {
		String _id;
		Expression _exp;

		public VariableAssignmentExpression(String id, Expression exp){
			_id = id;
			_exp = exp;
		}

		public String id(){
			return _id;
		}

		public Expression exp(){
			return _exp;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class ArrayAssignmentExpression extends Expression {
		String _id;
		Expression _idx;
		Expression _exp;

		public ArrayAssignmentExpression(String id, Expression idx, Expression exp){
			_id = id;
			_idx = idx;
			_exp = exp;
		}

		public String id(){
			return _id;
		}

		public Expression idx(){
			return _idx;
		}

		public Expression exp(){
			return _exp;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static abstract class Statement extends ASTNode {

	}

	public static class ExpressionStatement extends Statement {
		Expression _exp;

		public ExpressionStatement(Expression exp){
			_exp = exp;
		}

		public Expression exp(){
			return _exp;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class SelectionStatement extends Statement {
		Expression _cond;
		CompoundStatement _tbody;
		ComoundStatement _fbody;

		public SelectionStatement(Expression cond, CompoundStatement tbody, CompoundStatement fbody){
			_cond = cond;
			_tbody = tbody;
			_fbody = fbody;
		}

		public Expression cond(){
			return _cond;
		}

		public CompoundStatement tbody(){
			return _tbody;
		}

		public CompoundStatement fbody(){
			return _fbody;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class ConditionalLoopStatement extends Statement {
		Expression _cond;
		CompoundStatement _body;

		public ConditionalLoopStatement(Expression cond, CompoundStatement body){
			_cond = cond;
			_body = body;
		}

		public Expression cond(){
			return _cond;
		}

		public CompoundStatement body(){
			return _body;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class IterativeLoopStatement extends Statement {
		Expression _init;
		Expression _cond;
		Expression _incr;
		CompoundStatement _body;

		public IterativeLoopStatement(Expression init, Expression cond, Expression incr, CompoundStatement body){
			_init = init;
			_cond = cond;
			_incr = incr;
			_body = body;
		}

		public Expression init(){
			return _init;
		}

		public Expression cond(){
			return _cond;
		}

		public Expression incr(){
			return _incr;
		}

		public CompoundStatement body(){
			return _body;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class ReturnStatement extends Statement {
		Expression _exp;

		public ReturnStatement(Expression exp){
			_exp = exp;
		}

		public Expression exp(){
			return _exp;
		}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class UnitExp extends Exp {
		
		public UnitExp() {}

		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class NumExp extends Exp {
		double _val;

		public NumExp(double v) {
			_val = v;
		}

		public double v() {
			return _val;
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class StrExp extends Exp {
		String _val;

		public StrExp(String v) {
			_val = v;
		}

		public String v() {
			return _val;
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class BoolExp extends Exp {
		boolean _val;

		public BoolExp(boolean v) {
			_val = v;
		}

		public boolean v() {
			return _val;
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static abstract class CompoundArithExp extends Exp {
		List<Exp> _rest;

		public CompoundArithExp() {
			_rest = new ArrayList<Exp>();
		}

		public CompoundArithExp(Exp fst) {
			_rest = new ArrayList<Exp>();
			_rest.add(fst);
		}

		public CompoundArithExp(List<Exp> args) {
			_rest = new ArrayList<Exp>();
			for (Exp e : args)
				_rest.add((Exp) e);
		}

		public CompoundArithExp(Exp fst, List<Exp> rest) {
			_rest = new ArrayList<Exp>();
			_rest.add(fst);
			_rest.addAll(rest);
		}

		public CompoundArithExp(Exp fst, Exp second) {
			_rest = new ArrayList<Exp>();
			_rest.add(fst);
			_rest.add(second);
		}

		public Exp fst() {
			return _rest.get(0);
		}

		public Exp snd() {
			return _rest.get(1);
		}

		public List<Exp> all() {
			return _rest;
		}

		public void add(Exp e) {
			_rest.add(e);
		}
		
	}

	public static class AddExp extends CompoundArithExp {
		public AddExp(Exp fst) {
			super(fst);
		}

		public AddExp(List<Exp> args) {
			super(args);
		}

		public AddExp(Exp fst, List<Exp> rest) {
			super(fst, rest);
		}

		public AddExp(Exp left, Exp right) {
			super(left, right);
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class SubExp extends CompoundArithExp {

		public SubExp(Exp fst) {
			super(fst);
		}

		public SubExp(List<Exp> args) {
			super(args);
		}

		public SubExp(Exp fst, List<Exp> rest) {
			super(fst, rest);
		}

		public SubExp(Exp left, Exp right) {
			super(left, right);
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class DivExp extends CompoundArithExp {
		public DivExp(Exp fst) {
			super(fst);
		}

		public DivExp(List<Exp> args) {
			super(args);
		}

		public DivExp(Exp fst, List<Exp> rest) {
			super(fst, rest);
		}

		public DivExp(Exp left, Exp right) {
			super(left, right);
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	public static class MultExp extends CompoundArithExp {
		public MultExp(Exp fst) {
			super(fst);
		}

		public MultExp(List<Exp> args) {
			super(args);
		}

		public MultExp(Exp fst, List<Exp> rest) {
			super(fst, rest);
		}

		public MultExp(Exp left, Exp right) {
			super(left, right);
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}
	
	/**
	 * A let expression has the syntax 
	 * 
	 *  (let ((name expression)* ) expression)
	 *  
	 * @author hridesh
	 *
	 */
	public static class LetExp extends Exp {
		List<String> _names;
		List<Exp> _value_exps; 
		Exp _body;
		
		public LetExp(List<String> names, List<Exp> value_exps, Exp body) {
			_names = names;
			_value_exps = value_exps;
			_body = body;
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
		
		public List<String> names() { return _names; }
		
		public List<Exp> value_exps() { return _value_exps; }

		public Exp body() { return _body; }

	}
	
	/**
	 * A define declaration has the syntax 
	 * 
	 *  (define name expression)
	 *  
	 * @author hridesh
	 *
	 */
	public static class DefineDecl extends Exp {
		String _name;
		Exp _value_exp; 
		
		public DefineDecl(String name, Exp value_exp) {
			_name = name;
			_value_exp = value_exp;
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
		
		public String name() { return _name; }
		
		public Exp value_exp() { return _value_exp; }

	}
	
	/**
	 * An anonymous procedure declaration has the syntax
	 * 
	 * @author hridesh
	 *
	 */
	public static class LambdaExp extends Exp {		
		List<String> _formals;
		Exp _body; 
		
		public LambdaExp(List<String> formals, Exp body) {
			_formals = formals;
			_body = body;
		}
		
		public List<String> formals() { return _formals; }
		
		public Exp body() { return _body; }
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}
	
	/**
	 * A call expression has the syntax
	 * 
	 * @author hridesh
	 *
	 */
	public static class CallExp extends Exp {
		Exp _operator; 
		List<Exp> _operands;
		
		public CallExp(Exp operator, List<Exp> operands) {
			_operator = operator; 
			_operands = operands;
		}
		
		public Exp operator() { return _operator; }

		public List<Exp> operands() { return _operands; }
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	/**
	 * An if expression has the syntax
	 * 
	 * (if conditional_expression true_expression false_expression)
	 * 
	 * @author hridesh
	 *
	 */
	public static class IfExp extends Exp {
		Exp _conditional; 
		Exp _then_exp; 
		Exp _else_exp; 
		
		public IfExp(Exp conditional, Exp then_exp, Exp else_exp) {
			_conditional = conditional;
			_then_exp = then_exp; 
			_else_exp = else_exp; 
		}
		
		public Exp conditional() { return _conditional; }
		public Exp then_exp() { return _then_exp; }
		public Exp else_exp() { return _else_exp; }
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}
	
	/**
	 * A less expression has the syntax
	 * 
	 * ( < first_expression second_expression )
	 * 
	 * @author hridesh
	 *
	 */
	public static class LessExp extends BinaryComparator {
		public LessExp(Exp first_exp, Exp second_exp) {
			super(first_exp, second_exp);
		}
				
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}
	
	public static abstract class BinaryComparator extends Exp {
		private Exp _first_exp; 
		private Exp _second_exp; 
		BinaryComparator(Exp first_exp, Exp second_exp) {
			_first_exp = first_exp;
			_second_exp = second_exp; 
		}
		public Exp first_exp() { return _first_exp; }
		public Exp second_exp() { return _second_exp; }
	}

	/**
	 * An equal expression has the syntax
	 * 
	 * ( == first_expression second_expression )
	 * 
	 * @author hridesh
	 *
	 */
	public static class EqualExp extends BinaryComparator {
		public EqualExp(Exp first_exp, Exp second_exp) {
			super(first_exp, second_exp);
		}
		
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	/**
	 * A greater expression has the syntax
	 * 
	 * ( > first_expression second_expression )
	 * 
	 * @author hridesh
	 *
	 */
	public static class GreaterExp extends BinaryComparator {
		public GreaterExp(Exp first_exp, Exp second_exp) {
			super(first_exp, second_exp);
		}
				
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	/**
	 * A car expression has the syntax
	 * 
	 * ( car expression )
	 * 
	 * @author hridesh
	 *
	 */
	public static class CarExp extends Exp {
		private Exp _arg; 
		public CarExp(Exp arg){
			_arg = arg;
		}
		public Exp arg() { return _arg; }
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}
	
	/**
	 * A cdr expression has the syntax
	 * 
	 * ( car expression )
	 * 
	 * @author hridesh
	 *
	 */
	public static class CdrExp extends Exp {
		private Exp _arg; 
		public CdrExp(Exp arg){
			_arg = arg;
		}
		public Exp arg() { return _arg; }
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}
	
	/**
	 * A cons expression has the syntax
	 * 
	 * ( cons expression expression )
	 * 
	 * @author hridesh
	 *
	 */
	public static class ConsExp extends Exp {
		private Exp _fst; 
		private Exp _snd; 
		public ConsExp(Exp fst, Exp snd){
			_fst = fst;
			_snd = snd;
		}
		public Exp fst() { return _fst; }
		public Exp snd() { return _snd; }
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	/**
	 * A list expression has the syntax
	 * 
	 * ( list expression* )
	 * 
	 * @author hridesh
	 *
	 */
	public static class ListExp extends Exp {
		private List<Exp> _elems; 
		public ListExp(List<Exp> elems){
			_elems = elems;
		}
		public List<Exp> elems() { return _elems; }
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}
	
	/**
	 * A null expression has the syntax
	 * 
	 * ( null? expression )
	 * 
	 * @author hridesh
	 *
	 */
	public static class NullExp extends Exp {
		private Exp _arg; 
		public NullExp(Exp arg){
			_arg = arg;
		}
		public Exp arg() { return _arg; }
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	/**
	 * Eval expression: evaluate the program that is _val
	 * @author hridesh
	 *
	 */
	public static class EvalExp extends Exp {
		private Exp _code; 
		public EvalExp(Exp code){
			_code = code;
		}
		public Exp code() { return _code; }
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}

	/**
	 * Read expression: reads the file that is _file
	 * @author hridesh
	 *
	 */
	public static class ReadExp extends Exp {
		private Exp _file; 
		public ReadExp(Exp file){
			_file = file;
		}
		public Exp file() { return _file; }
		public <T> T accept(Visitor<T> visitor, Env env) {
			return visitor.visit(this, env);
		}
	}
	
	public interface Visitor <T> {
		public T visit(AST.Program e, Env env);
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
		public T visit(AST.ConstantExpression d, Env env); // Should always return an integer
		public T visit(AST.StringExpression e, Env env); // Should always return a string
		public T visit(AST.VariableExpression e, Env env); // Should always return an integer, string, or plank (reference)
		public T visit(AST.MultiplicationExpression e, Env env); // Should always return an integer
		public T visit(AST.DivisionExpression e, Env env); // Should always return an integer
		public T visit(AST.ModuloExpression e, Env env); // Should always an integer
		public T visit(AST.AdditionExpression e, Env env); // Should always return an integer or a string
		public T visit(AST.SubtractionExpression e, Env env); // Should always return an integer
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
	}	
}