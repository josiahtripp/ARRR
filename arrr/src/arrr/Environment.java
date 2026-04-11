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

	// Get an identifier Type value from anywhere in the scope
	// Used For: Returning the Type value of an identifier in a variable access
	public Type get(String name){

		// Identifier exists in the current environment
		if(_map.containsKey(name)){
			return _map.get(name);
		}

		// Check the parent environment
		if(_parent != null){
			return _parent.get(name);
		}

		// Identifier does not exist
		return new UnitType();
	}

	// Returns the value of an identifier anywhere in the scope
	// 		Used for: Accessing a variable value
	//		Returns: the value of the identifier key-value pair
	//      Throws RuntimeException: 
	// 				Identifier does not exists anywhere in the environment chain
	public Type value(String name){

		// Exists in current scope
		if(_map.containsKey(name)){
			return _map.get(name);
		}

		// Parent exists, return its result
		if(_parent != null){
			return _parent.value(name);
		}

		// Identifier does not exist
		throw new RuntimeException("Invalid identifier: identifier \"" + name + "\" has not been declared");

	}

	// Sets an identifier anywhere in the scope (first match when back tracing environment chain)
	// 		Used for: Setting an identifier in an assignment expression
	//		Returns: true if the identifier was found and set, false if the identifier could not be found
	//      Throws RuntimeException: 
	// 				Identifier does not exists anywhere in the environment chain
	public void assign(String name, Type value){

		// Exists in current scope
		if(_map.containsKey(name)){

			// Get the present value
			Type present = _map.get(name);

			// Cast to present type
			Type updated = Type.castCheck(value, present);

			// Update map
			_map.put(name, updated);

			return;
		}

		// Parent exists, return its result
		if(_parent != null){
			_parent.assign(name, value);
			return;
		}

		// Identifier does not exist
		throw new RuntimeException("Invalid identifier: identifier \"" + name + "\" has not been declared");
	}

	// Sets an new identifier anywhere in the current scope
	// 		Used for: Declaring a new identifier in a variable or array declaration
	//      Throws RuntimeException: 
	// 				Identifier already exists in the current scope
	public void declare(String name, Type value){

		// Identifier is free in the current scope
		if(_map.get(name) == null){
			_map.put(name, value);
			return;
		}

		throw new RuntimeException("Redeclaration: identifier \"" + name + "\" already exists in the current scope");
	}
}
