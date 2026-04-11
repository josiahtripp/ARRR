package arrr;

import java.util.ArrayList;
import java.util.List;

import arrr.AST.CompoundStatement;
import arrr.AST.ParameterDeclaration;


public interface Type {

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
