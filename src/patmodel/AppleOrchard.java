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

	private static final double RAINING_NUTRIENTS_TRESHOLD = 0.5;
	private static final double SUNNY_NUTRIENTS_TRESHOLD = -0.1;

	private Context<Object> context;
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	private ContinuousSpace<Object> space2;
	
	private boolean isRaining;
	private boolean isSunny;
	private boolean isWindy;
	
	private double nutrients;
	
	public static final int TREE_VISUALISATION_Y_OFFSET = 16;
	
	@Override
	public Context<Object> build(Context<Object> context) {
		
		context.setId("PATModel");

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		this.space = spaceFactory.createContinuousSpace (" space ", context, new RandomCartesianAdder < Object >(),
				new repast.simphony.space.continuous.BouncyBorders(), 100, 100, 100);
	
		/*ContinuousSpaceFactory spaceFactory2 = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		this.space2 = spaceFactory2.createContinuousSpace ("soil", context, new RandomCartesianAdder < Object >(),
				new repast.simphony.space.continuous.BouncyBorders(), 100, 20, 100);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
 		this.grid = gridFactory.createGrid("grid", context, GridBuilderParameters.multiOccupancyND( 
 				new SimpleGridAdder<Object>(), new InfiniteBorders<>(), 100, 100, 100));*/

 		this.context = context;
 		
 		Parameters p;
 	
 		SoilDesign soil = new SoilDesign();
		context.add(soil);
		space.moveTo(soil, 50, 0, 50);
		
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
		this.space.moveTo(t, 0, this.TREE_VISUALISATION_Y_OFFSET, 0);
	}
	
	public void rain(boolean state) {
		this.isRaining = state;
	}

	public void wind(boolean state) {
		this.isWindy = state;
	}

	public void sun(boolean state) {
		this.isSunny = state;
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
			nutrients += RAINING_NUTRIENTS_TRESHOLD;
		} else if(isSunny) {
			nutrients += SUNNY_NUTRIENTS_TRESHOLD;
		}
	}
	
	//TODO rimuovo quando sarà implementato il tuple space
	
	public void addNutrients(double nutrients) {
		this.nutrients += nutrients;
	}
	
		
}
