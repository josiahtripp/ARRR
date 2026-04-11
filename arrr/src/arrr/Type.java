package arrr;

import java.util.ArrayList;
import java.util.List;

import arrr.AST.CompoundStatement;
import arrr.AST.ParameterDeclaration;
import arrr.Type.ArrType;
import arrr.Type.FunctionType;
import arrr.Type.IntegerType;
import arrr.Type.ReturnType;
import arrr.Type.StringType;
import arrr.Type.VoidType;


public interface Type {

    public static Type castCheck(Type from, Type to){

        // Objects are the same type
        if(Type.match(from, to)){

            // Return the source object, no cast needed
            return from;
        }

        // From int to string
        if(from instanceof IntegerType && to instanceof StringType){

            // Create a new string type object from the int value of from
            return new StringType(String.valueOf(((IntegerType) from).val()));
        }

        // Default int
        if(from == null && to instanceof IntegerType){
            return new IntegerType(0);
        }

        // Default string
        if(from == null && to instanceof StringType){
            return new StringType("");
        }

        // Invalid cast
        throw new RuntimeException("Invalid cast: cannot convert type \"" + from.getClass().toString() + "\" to \"" + to.getClass().toString() + "\"");
    }

    // t1, t2 CANNOT be subclass FunctionType
    public static boolean match(Type t1, Type t2) {
        
        // Reference the same object
        if (t1 == t2){
            return true;
        }

        // Null reference
        if (t1 == null || t2 == null){
            return false;
        }

        // Mismatch subclass
        if (t1.getClass() != t2.getClass()){
            return false;
        }

        // Array subclass
        if (t1 instanceof ArrType) {
            ArrType a1 = (ArrType) t1;
            ArrType a2 = (ArrType) t2;
            return Type.match(a1.type(), a2.type());
        }

        // Return subclass
        if (t1 instanceof ReturnType) {
            ReturnType r1 = (ReturnType) t1;
            ReturnType r2 = (ReturnType) t2;
            return Type.match(r1.val(), r2.val());
        }

        return true;
    }

    public static int intValue(Type t){

        // Already integer type
        if(t instanceof IntegerType){
            return ((IntegerType) t).val();
        }

        // Cast to integer type and return
        return Type.intValue(castCheck(t, new IntegerType()));
    }

	static class FunctionType implements Type {
		private Type _type; // Return type
        private List<ParameterDeclaration> _params; // Parameters
		private CompoundStatement _body; // Function body

        public FunctionType(Type type, List<ParameterDeclaration> params, CompoundStatement body){
            _type = type;
            _params = params;
            _body = body;
        }

        public Type type(){
            return _type;
        }

        public List<ParameterDeclaration> params(){
            return _params;
        }

        public CompoundStatement body(){
            return _body;
        }
	}

    static class ArrType implements Type {
        private Type _type;
        private List<Type> _val;

        public ArrType(Type type) {
            _type = type;
        }

        public ArrType(Type type, List<Type> val){
            _type = type;
            _val = val;
        }

        public Type type() {
            return _type;
        }

        public List<Type> val() {
            return _val;
        }

        public void setIdx(int idx, Type value) {
            _val.set(idx, value);
        }
    }

	static class IntegerType implements Type {
	    private int _val;

        public IntegerType(){}

	    public IntegerType(int val){
            _val = val;
        }

	    public int val(){ 
            return _val; 
        }
	}

	static class StringType implements Type {
		private String _val;

        public StringType() {}

	    public StringType(String val){
            _val = val;
        }

	    public String val(){
            return _val; 
        }
	}

    static class VoidType implements Type {
        public VoidType(){}
    }

    static class ReturnType implements Type {
        private Type _val;

        public ReturnType() {
            _val = new VoidType();
        }

	    public ReturnType(Type val){
            _val = val;
        }

	    public Type val(){
            return _val; 
        }
    }

    static class UnitType implements Type {
        public UnitType(){}
    }
}