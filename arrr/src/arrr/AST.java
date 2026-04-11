package arrr;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.List;

import arrr.AST.ASTNode;
import arrr.AST.BinaryExpression;
import arrr.AST.CompoundStatement;
import arrr.AST.Declaration;
import arrr.AST.ExternalDeclaration;
import arrr.AST.ParameterDeclaration;
import arrr.Type.*;
import arrr.AST.UnaryExpression;


public interface AST {

	public static abstract class ASTNode implements AST {
		public abstract <T> T accept(Visitor<T> visitor, Environment env);
	}

	public static class Program extends ASTNode {
		List<ExternalDeclaration> _decls;

		public Program(List<ExternalDeclaration> decls) {
			_decls = decls;
		}
		
		public List<ExternalDeclaration> decls() {
			return _decls;
		}
		
		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public Type type() {
			return _type;
		}

		public List<ParameterDeclaration> params() {
			return _params;
		}

		public CompoundStatement body(){
			return _body;
		}
		
		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class ParameterDeclaration extends ASTNode {
		Type _type;
		String _id;

		public ParameterDeclaration(Type type, String id) {
			_type = type;
			_id = id;
		}

		public String id() {
			return _id;
		}

		public Type type(){
			return _type;
		}
		
		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}
	
	public static class CompoundStatement extends ASTNode {
		List<Declaration> _decls;
		List<Statement> _stmts;

		public CompoundStatement(List<Declaration> decls, List<Statement> stmts){
			_decls = decls;
			_stmts = stmts;
		}

		public List<Declaration> decls(){
			return _decls;
		}

		public List<Statement> stmts(){
			return _stmts;
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static abstract class Declaration extends ExternalDeclaration {

	}

	public static class VariableDeclaration extends Declaration {
		Type _type;
		String _id;
		Expression _exp;

		public VariableDeclaration(Type type, String id, Expression exp) {
			_type = type;
			_id = id;
			_exp = exp;
		}

		public Type type() {
			return _type;
		}

		public String id() {
			return _id;
		}

		public Expression exp(){
			return _exp;
		}
		
		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public Type type() {
			return _type;
		}

		public String id() {
			return _id;
		}
		
		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static abstract class Expression extends ASTNode {

	}

	public static abstract class UnaryExpression extends Expression {
		Expression _exp;

		public UnaryExpression(Expression exp) {
			_exp = exp;
		}

		public Expression getExp() {
			return _exp;
		}
	}

	public static class NegationExpression extends UnaryExpression {

		public NegationExpression(Expression exp){
			super(exp);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class FunctionCallExpression extends Expression {
		String _id;
		List<Expression> _args;

		public FunctionCallExpression(String id, List<Expression> args){
			_id = id;
			_args = args;

			if(_args == null){
				_args = new ArrayList<Expression>();
			}
		}

		public String id(){
			return _id;
		}

		public List<Expression> args(){
			return _args;
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class EmbeddedFunctionCallExpression extends Expression {
		String _name;
		List<Expression> _args;

		public EmbeddedFunctionCallExpression(String id, List<Expression> args){
			_name = id;
			_args = args;

			if(_args == null){
				_args = new ArrayList<Expression>();
			}
		}

		public String name(){
			return _name;
		}

		public List<Expression> args(){
			return _args;
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class StringExpression extends Expression {
		private String _str;

		public StringExpression(String val){
			_str = val.substring(1, val.length() - 1).replace("\\n", "\n");
			_str = _str.replace("\\t", "\t");
			_str = _str.replace("\\r", "\r");
			_str = _str.replace("\\\\", "\\");
			_str = _str.replace("\\\"", "\"");
		}

		public String str(){
			return _str;
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class DivisionExpression extends BinaryExpression {

		public DivisionExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class ModuloExpression extends BinaryExpression {

		public ModuloExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class AdditionExpression extends BinaryExpression {

		public AdditionExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class SubtractionExpression extends BinaryExpression {

		public SubtractionExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}
	
	public static class GreaterthanExpression extends BinaryExpression {

		public GreaterthanExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class LessthanExpression extends BinaryExpression {

		public LessthanExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class EqualityExpression extends BinaryExpression {

		public EqualityExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class InequalityExpression extends BinaryExpression {

		public InequalityExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class LogicalOrExpression extends BinaryExpression {

		public LogicalOrExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class LogicalAndExpression extends BinaryExpression {

		public LogicalAndExpression(Expression left, Expression right){
			super(left, right);
		}

		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}

	public static class SelectionStatement extends Statement {
		Expression _cond;
		CompoundStatement _tbody;
		CompoundStatement _fbody;

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

		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public <T> T accept(Visitor<T> visitor, Environment env) {
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

		public <T> T accept(Visitor<T> visitor, Environment env) {
			return visitor.visit(this, env);
		}
	}
	
	public interface Visitor <T> {
		public T visit(AST.Program e, Environment env);
		public T visit(AST.FunctionDefinition e, Environment env);
		public T visit(AST.ParameterDeclaration e, Environment env);
		public T visit(AST.CompoundStatement e, Environment env);
		public T visit(AST.VariableDeclaration e, Environment env);
		public T visit(AST.ArrayDeclaration e, Environment env);
		public T visit(AST.NegationExpression p, Environment env);
		public T visit(AST.ArrayAccessExpression e, Environment env);
		public T visit(AST.FunctionCallExpression e, Environment env);
		public T visit(AST.EmbeddedFunctionCallExpression e, Environment env);
		public T visit(AST.ConstantExpression d, Environment env);
		public T visit(AST.StringExpression e, Environment env);
		public T visit(AST.VariableExpression e, Environment env);
		public T visit(AST.MultiplicationExpression e, Environment env);
		public T visit(AST.DivisionExpression e, Environment env);
		public T visit(AST.ModuloExpression e, Environment env);
		public T visit(AST.AdditionExpression e, Environment env);
		public T visit(AST.SubtractionExpression e, Environment env);
		public T visit(AST.GreaterthanExpression e, Environment env);
		public T visit(AST.LessthanExpression e, Environment env);
		public T visit(AST.EqualityExpression e, Environment env);
		public T visit(AST.InequalityExpression e, Environment env);
		public T visit(AST.LogicalOrExpression e, Environment env);
		public T visit(AST.LogicalAndExpression e, Environment env);
		public T visit(AST.VariableAssignmentExpression e, Environment env);
		public T visit(AST.ArrayAssignmentExpression e, Environment env);
		public T visit(AST.ExpressionStatement e, Environment env);
		public T visit(AST.SelectionStatement e, Environment env);
		public T visit(AST.ConditionalLoopStatement e, Environment env);
		public T visit(AST.IterativeLoopStatement e, Environment env);
		public T visit(AST.ReturnStatement e, Environment env);
	}	
}