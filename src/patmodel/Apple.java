package patmodel;

public class Apple {

	// cm
	private double diameter;
	// cm
	private static final double MAX_DIAMETER = 7;
	// g/ cm^3
	private static final double APPLE_DENSITY = 0.75;
	
	private boolean isFallen;
	
	public Apple(){
		this.grow();
		this.isFallen = false;
	}
	
	public void becomeNutrients() {
		if(!this.isFallen)
			return;
		// we first need to have access to 
		// nutrients variable and increase it
	}
	
	public void becomeTree() {
		if(this.isFallen) {
			// we first need to have access to the 
			// trees variable and add a new one
		}
	}
	
	public void fall(){
		// we need the wind behavior to make this method better
		if(!this.isFallen) {
			if(this.diameter == 7)
				this.isFallen = true;
		}
	}

	public double calcVolume() {
		return (4/3)*Math.PI*(this.diameter/2);
	}
	
	public double calcWeight() {
		return this.calcVolume()*APPLE_DENSITY;
	}
	
	public void grow() {
		if(this.diameter < MAX_DIAMETER)
			this.diameter += 0.1;
	}
	
}
