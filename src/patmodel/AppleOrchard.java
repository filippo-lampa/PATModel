package patmodel;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.InfiniteBorders;
import repast.simphony.space.grid.SimpleGridAdder;


public class AppleOrchard extends DefaultContext<Object> implements ContextBuilder<Object> {
	private static final double MIN_DISTANCE_BETWEEN_TREES = 3;
	private Context<Object> context;
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;

	private double nutrients;

	private boolean isRaining;
	private boolean isSunny;
	private boolean isWindy;

	private double randomWeight; // a threshold-like for weather probability

	// timeticks
	private int rainStartTimetick;
	private int windStartTimetick;
	private int sunStartTimetick;
	private int rainEndTimetick;
	private int windEndTimetick;
	private int sunEndTimetick;

	@Override
	public Context<Object> build(Context<Object> context) {

		context.setId("PinkAppleModel");

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		this.space = spaceFactory.createContinuousSpace(" space ", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.InfiniteBorders<>(), 100, 100, 100);

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
		//TODO init soil and get height
		double soilHeight = 1;
		for(int row = 0; row<this.space.getDimensions().getDepth()/MIN_DISTANCE_BETWEEN_TREES; ++row) {
			for(int column = 0; column<this.space.getDimensions().getWidth()/MIN_DISTANCE_BETWEEN_TREES; ++column) {
				Tree t = new Tree(space, grid, Tree.BASE_TREE_WIDHT, Tree.BASE_TREE_HEIGHT, Tree.BASE_TREE_AGE, Tree.BASE_APPLE_QUANTITY, Tree.BASE_FOLIAGE_DIAMETER);
				this.context.add(t);
				this.space.moveTo(t, column * MIN_DISTANCE_BETWEEN_TREES, row * MIN_DISTANCE_BETWEEN_TREES, soilHeight);
			}
			
		}
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

		double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();

		double rainDuration = currentTick - this.rainStartTimetick;
		double windDuration = currentTick - this.windStartTimetick;
		double sunDuration = currentTick - this.sunStartTimetick;
		double weight_rain = computeRandomWeight(currentTick);
		double weight_wind = computeRandomWeight(currentTick);
		double weight_sun = computeRandomWeight(currentTick);
		
		// Randomness of the three probability combined
		
		// Actual condition based on probability combined ( what happens )
		
	}
	
	private double computeRandomWeight(double currentTick) {
		double weight = 0;
		Season season = computeSeason(currentTick);
		
		//Compute weight for an event
		switch(season) {
			case SPRING:
				//high sun / mid wind / low rain
				break;
			case SUMMER:
				//high sun / low wind / low rain
				break;
			case AUTUMN:
				//high rain / high wind / low sun
				break;
			case WINTER:
				//mid rain / mid wind / low sun 
				break;
		}
		
		return weight;
	}

	private Season computeSeason(double currentTick) {
		//Compute the month 
		int month = (int) ((currentTick % 365) / 30);
		
		//Compute seasons
		Season season;
		
		//Spring
		if(month > 3 && month <= 5) {
			season = Season.SPRING;
		}
		//Summer
		else if (month > 5 && month <= 9 ) {
			season = Season.SUMMER;
		}
		//Fall
		else if (month > 9 && month <= 12) {
			season = Season.AUTUMN;
		}
		//Winter
		else{
			season = Season.WINTER;
		}
		
		return season;
	}

	private void deltaNutrients() {
		if (isRaining) {

		}

	}

}
