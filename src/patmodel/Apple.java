package patmodel;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.context.Context;

import java.util.List;
import java.util.Random;

public class Apple {

	// cm
	private double diameter;
	// cm
	private static final double MAX_DIAMETER = 7;
	// g/ cm^3
	private static final double APPLE_DENSITY = 0.75;
	
	// space
	private ContinuousSpace<Object> space;
	
	private boolean isFallen;
	
	private Context<Object> context;
	
	private AppleOrchard orchard;
	
	public Apple(Context<Object> context,
				 ContinuousSpace<Object> space, 
				 Grid<Object> grid,
				 AppleOrchard orchard){
		this.context = context;
		this.orchard = orchard;
		this.space = space;
		this.grow();
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 4)
	private void update() {
			this.grow();
			if(this.diameter == 7)
				this.fall();	
	}
	
	public void fall() {
		this.becomeTreeOrNutrients();
		this.context.remove(this);
	}
	
	private void grow() {
		this.diameter += 0.1;
	}	

	private void becomeTreeOrNutrients() {
		if(new Random().nextBoolean()) 
			this.becomeNutrients();
		else
			this.becomeTree();
	}
	
	private void becomeNutrients() {
		this.orchard.addNutrients();
	}
	
	private void becomeTree() {
		if(this.isFallen) {
			Tree t = new Tree(this.space, Tree.BASE_TREE_WIDHT, Tree.BASE_TREE_HEIGHT, Tree.BASE_TREE_AGE, Tree.BASE_APPLE_QUANTITY, Tree.BASE_FOLIAGE_DIAMETER);
			this.context.add(t);
			var pointToMoveTo = this.casualNearPoint();
			this.space.moveTo(t, pointToMoveTo.getX(), 
								 pointToMoveTo.getY(),
								 0);
		}
	}
	
	// this method must improved in the future
	private NdPoint casualNearPoint(){
		var gp = this.space.getLocation(this);
		return gp;
	}

	
	public double calcVolume() {
		return (4/3)*Math.PI*(this.diameter/2);
	}
	
	public double calcWeight() {
		return this.calcVolume()*APPLE_DENSITY;
	}

}
