package arrr;

import java.util.ArrayList;
import java.util.List;

import arrr.AST.Exp;

public interface Type {

	static class FunctionType implements Type {
		private Env _env;
		private List<String> _formals;
		private Exp _body;
		public FunVal(Env env, List<String> formals, Exp body) {
			_env = env;
			_formals = formals;
			_body = body;
		}
		public Env env() { return _env; }
		public List<String> formals() { return _formals; }
		public Exp body() { return _body; }
	    public String tostring() { 
			String result = "(lambda ( ";
			for(String formal : _formals) 
				result += formal + " ";
			result += ") ";
			result += _body.accept(new Printer.Formatter(), _env);
			return result + ")";
	    }
	}

    static class ArrayType implements Type {
        private Type _type;
        private List<Type> _val;

        public ArrayType(Type type) {
            _type = elementType;
        }

        public ArrayType(Type type, List<Type> val){
            _type = elementType;
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

        public StringType(){}

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

    static class UnitType implements Type {
        public UnitType(){}
    }
}
