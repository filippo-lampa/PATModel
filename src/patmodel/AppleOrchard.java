package patmodel;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.InfiniteBorders;
import repast.simphony.space.grid.SimpleGridAdder;

public class AppleOrchard extends DefaultContext<Object> implements ContextBuilder<Object>{

	private Context<Object> context;
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	
	private boolean isRaining;
	private boolean isSunny;
	private boolean isWindy;
	
	private double nutrients;
	
	@Override
	public Context<Object> build(Context<Object> context) {
		
		context.setId("PinkAppleModel");

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		this.space = spaceFactory.createContinuousSpace (" space ", context, new RandomCartesianAdder < Object >(),
				new repast.simphony.space.continuous.InfiniteBorders<>(), 100, 100, 100);
	
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
 		this.grid = gridFactory.createGrid("grid", context, GridBuilderParameters.multiOccupancyND( 
 				new SimpleGridAdder<Object>(), new InfiniteBorders<>(), 100, 100, 100));

 		this.context = context;
 		
 		Parameters p;
 	
 		this.isRaining = false;
 		this.isWindy = false;
 		this.isSunny = false;
		
 		//minimal nutrients for a plant to survive at first time tick is equal to 0.1
 		this.nutrients = 1;
 		initAgents();
		return context;
	}
	
	private void initAgents() {
		Tree t = new Tree(space, grid, Tree.BASE_TREE_WIDHT, Tree.BASE_TREE_HEIGHT, Tree.BASE_TREE_AGE, Tree.BASE_APPLE_QUANTITY, Tree.BASE_FOLIAGE_DIAMETER);
		this.context.add(t);
		this.space.moveTo(t, 0, 0, 0);
	}
	
	public void rain(boolean state) {
		
	}

	public void wind(boolean state) {

	}

	public void sun(boolean state) {

	}

	@ScheduledMethod(start = 1, interval = 1, priority = 2)
	private void updateSoil() {
		deltaNutrients();
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 1)
	private void updateWeather() {
	}

	private void deltaNutrients() {
		if(isRaining) {
			
		}
			
	}
	
	
		
}
