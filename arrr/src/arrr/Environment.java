package arrr;

import java.util.Map;
import java.util.TreeMap;
import arrr.Type.UnitType;

public class Environment {
	private Environment _parent;
	private Map<String, Type> _map;

	public Environment(Environment parent){
		_parent = parent;
		_map = new TreeMap<>();
	}

	public Type get(String name){

		// Identifier exists in the current environment
		if(_map.containsKey(name)){
			return _map.get(name);
		}

		// Check the parent environment
		if(_parent != null){
			return _parent.get(name);
		}

		// Identifier does not exists
		return new UnitType();
	}

	public Type getCurrent(String name){

		// Identifier exists in the current environment
		if(_map.containsKey(name)){
			return _map.get(name);
		}

		return new UnitType();
	}

	// Sets a value anywhere in the scope, or creates a new one in the current scope
	public void set(String name, Type value){

		// Creating a new identifier - value pair
		if(get(name) instanceof UnitType){
			_map.put(name, value);
		}

		// Exists, just not in current scope
		if(getCurrent(name) instanceof UnitType){
			_parent.set(name, value);
		}
		else{ // Exists in current scope, add it here
			_map.put(name, value);
		}
	}

	// Sets a value only in the current scope
	public void setCurrent(String name, Type value){
		_map.put(name, value);
	}
}
