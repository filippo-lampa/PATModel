package patmodel;

public class Tuple {

	String name;
	Object object;

	public Tuple(String name, Object object) {
		this.name = name;
		this.object= object;
	}

	public String getName() {
		return name;
	}

	public Object getObject() {
		return object;
	}

}