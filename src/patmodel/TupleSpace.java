package patmodel;

import java.util.HashMap;

public class TupleSpace {
	
	private static TupleSpace instance = null;
	
	private HashMap<String, Object> space;
	
	public TupleSpace() {
		this.space = new HashMap<>();	
	}
	
	public static TupleSpace getInstance() {
		if(instance == null)
			instance = new TupleSpace();
		return instance;
	}
	
	
	public Object in(String name){
		Object chosenEntry = null;
		for(String currentEntry : this.space.keySet())
			if(currentEntry.equals(name))
				chosenEntry = this.space.get(currentEntry);
		this.space.remove(chosenEntry);
		return chosenEntry;
	}
	
	public Object rd(String name){
		Object chosenEntry = null;
		for(String currentEntry : this.space.keySet())
			if(currentEntry.equals(name))
				chosenEntry = this.space.get(currentEntry);
		return chosenEntry;
	}
	
	public void out(String name, Object object){
		this.space.put(name, object);
	}
	
}
