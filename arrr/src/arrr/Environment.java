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

	public void set(String name, Type value){
		_map.put(name, value);
	}
}
