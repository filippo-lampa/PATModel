package patmodel;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.context.Context;

public class Apple {

	private double diameter;

	private double MAX_DIAMETER = 7;

	private ContinuousSpace<Object> space;

	private Context<Object> context;

	private AppleOrchard orchard;

	public Apple(Context<Object> context, ContinuousSpace<Object> space, AppleOrchard orchard) {
		this.context = context;
		this.orchard = orchard;
		this.space = space;
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.MAX_DIAMETER = (double) p.getValue("appleMaxDiameter");
	}

	@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void update() {
		this.grow();
		if (this.diameter == MAX_DIAMETER)
			this.fall();
	}

	public void fall() {
		this.becomeTreeOrNutrients();
		this.context.remove(this);
	}

	private void grow() {
		this.diameter += .1;
	}

	public double getIconSize() {
		return diameter;
	}

	private void becomeTreeOrNutrients() {
		if (AppleOrchard.RANDOM.nextDouble() < .5)
			this.becomeNutrients();
		else
			this.becomeTree();
	}

	private void becomeNutrients() {
		this.orchard.addNutrients(AppleOrchard.APPLE_NUTRIENT_AMOUNT);
	}

	private void becomeTree() {
		var t = new Tree(this.context, this.space, this.orchard, Tree.BASE_TREE_WIDHT, Tree.BASE_TREE_HEIGHT,
				Tree.BASE_TREE_AGE, Tree.BASE_FOLIAGE_DIAMETER);
		this.context.add(t);
		var pointToMoveTo = this.casualNearPoint(2);
		this.space.moveTo(t, pointToMoveTo.getX(), 0, pointToMoveTo.getZ());

	}

	/**
	 * Given an offset, returns a random point near this agent.
	 */
	private NdPoint casualNearPoint(double offset) {
		var tmp = this.space.getLocation(this);
		var x = this.randomDoubleInTheSpace(tmp.getX(), offset);
		var y = this.randomDoubleInTheSpace(tmp.getY(), offset);
		var z = this.randomDoubleInTheSpace(tmp.getZ(), offset);
		return new NdPoint(x, y, z);
	}

	/**
	 * Given a value and an offset, returns a random value within
	 * [value-offset(inclusive), value+offset (exclusive)]. The value is truncated
	 * if it exceeds the limits of the continuous space i.e. 0 and 15.
	 */
	private double randomDoubleInTheSpace(double value, double offset) {
		value = AppleOrchard.RANDOM.nextDouble(value - offset, value + offset);
		value = value >= 15 ? 14.9 : value;
		value = value < 0 ? 0 : value;
		return value;
	}

}
