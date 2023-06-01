package patmodel;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.context.Context;

public class Apple {
	// cm
	private double diameter;
	// cm
	private double MAX_DIAMETER = 7;
	// g/ cm^3
	private static final double APPLE_DENSITY = 0.75;
	
	// space
	private ContinuousSpace<Object> space;
	
	private boolean isFallen;
	
	private Context<Object> context;
	
	private AppleOrchard orchard;
	
	private TupleSpace tupleSpace;
	
	public Apple(Context<Object> context,
				 ContinuousSpace<Object> space, 
				 AppleOrchard orchard){
		this.context = context;
		this.orchard = orchard;
		this.space = space;
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		this.tupleSpace = TupleSpace.getInstance();
		
		this.MAX_DIAMETER = (double)p.getValue("appleMaxDiameter");
		
		this.grow();
		
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void update() {
			this.grow();
			if(this.diameter == MAX_DIAMETER)
				this.fall();	
	}
	
	public void fall() {
		this.becomeTreeOrNutrients();
		this.context.remove(this);
	}
	
	private void grow() {
		this.diameter += 0.1;
	}	

	public double getIconSize() {
		return diameter;
	}
	
	private void becomeTreeOrNutrients() {
		if(AppleOrchard.RANDOM.nextBoolean()) 
			this.becomeNutrients();
		else
			this.becomeTree();
	}
	
	private void becomeNutrients() {
		this.tupleSpace.out("nutrients",(double)this.tupleSpace.in("nutrients") + AppleOrchard.APPLE_NUTRIENT_AMOUNT);
	}
	
	private void becomeTree() {
		if(this.isFallen) {
			Tree t = new Tree(this.context, this.space, this.orchard, Tree.BASE_TREE_WIDHT, Tree.BASE_TREE_HEIGHT, Tree.BASE_TREE_AGE, Tree.BASE_FOLIAGE_DIAMETER);
			this.context.add(t);
			NdPoint pointToMoveTo = this.casualNearPoint();
			this.space.moveTo(t, pointToMoveTo.getX(), 
								 pointToMoveTo.getY(),
								 0);
		}
	}
	
	// this method must improved in the future
	private NdPoint casualNearPoint(){
		NdPoint gp = this.space.getLocation(this);
		return gp;
	}

	
	public double calcVolume() {
		return (4/3)*Math.PI*(this.diameter/2);
	}
	
	public double calcWeight() {
		return this.calcVolume()*APPLE_DENSITY;
	}

}
