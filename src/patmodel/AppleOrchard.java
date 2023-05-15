package patmodel;

import java.util.Random;

import kotlin.Pair;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;

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

	// TODO: add array weather history: max 30 days

	private int sunStartTimetick;
	private int rainEndTimetick;
	private int windEndTimetick;
	private int sunEndTimetick;
	public static final Random RANDOM = new Random();

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

		// minimal nutrients for a plant to survive at first time tick is equal to 0.1
		this.nutrients = 1;
		initAgents();
		return context;
	}

	private void initAgents() {
		// TODO init soil and get height
		double soilHeight = 1;
		for (int row = 1; row < this.space.getDimensions().getDepth() / MIN_DISTANCE_BETWEEN_TREES; ++row) {
			for (int column = 1; column < this.space.getDimensions().getWidth()
					/ MIN_DISTANCE_BETWEEN_TREES; ++column) {
				Tree t = new Tree(space, grid, Tree.BASE_TREE_WIDHT, Tree.BASE_TREE_HEIGHT, Tree.BASE_TREE_AGE,
						Tree.BASE_APPLE_QUANTITY, Tree.BASE_FOLIAGE_DIAMETER);
				this.context.add(t);
				this.space.moveTo(t, column * MIN_DISTANCE_BETWEEN_TREES, soilHeight, row * MIN_DISTANCE_BETWEEN_TREES);
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
		computeWeather(currentTick);

		// Randomness of the three probability combined

		// Actual condition based on probability combined ( what happens )

	}

	private void computeWeather(double currentTick) {
		var season = computeSeason(currentTick);
		var low = new Pair<>(0.0, 0.34);
		var medium = new Pair<>(0.34, 0.67);
		var high = new Pair<>(0.67, 1.0);
		// Computing weather for an event
		switch (season) {
		case WINTER:
			// high rain / mid wind / low sun
			weather(high, medium, low);
			break;
		case SPRING:
			// low rain / mid wind / mid sun
			weather(low, medium, medium);
			break;
		case SUMMER:
			// low rain / low wind / high sun
			weather(low, low, high);
			break;
		case AUTUMN:
			// high rain / high wind / low sun
			weather(high, high, low);
			break;
		}
	}

	private void weather(Pair<Double, Double> rain, Pair<Double, Double> wind, Pair<Double, Double> sun) {
		var rainRatio = RANDOM.nextDouble(rain.getFirst(), rain.getSecond());
		rainRatio = rainRatio - rain.getFirst();
		var cond = rainRatio > (rain.getSecond() - rain.getFirst()) / 2;
		rain(cond);
		var windRatio = RANDOM.nextDouble(wind.getFirst(), wind.getSecond());
		windRatio = windRatio - wind.getFirst();
		cond = windRatio > (wind.getSecond() - wind.getFirst()) / 2;
		wind(cond);
		var sunRatio = RANDOM.nextDouble(sun.getFirst(), sun.getSecond());
		sunRatio = sunRatio - sun.getFirst();
		cond = sunRatio > (sun.getSecond() - sun.getFirst()) / 2;
		sun(cond);
	}

	private Season computeSeason(double currentTick) {
		// Compute the month
		int month = (int) ((currentTick % 366) / 30);
		if (month == 0) {
			// case day 1, aka time tick 0
			month = 1;
		}
		if (month < 1 || month > 12) {
			throw new IllegalArgumentException("time tick: " + currentTick + " generated invalid month: " + month);
		}
		return switch (month) {
		case 3, 4, 5 -> Season.SPRING;
		case 6, 7, 8 -> Season.SUMMER;
		case 9, 10, 11 -> Season.AUTUMN;
		case 12, 1, 2 -> Season.WINTER;
		default -> Season.WINTER;
		};
	}

	private void deltaNutrients() {
		if (isRaining) {

		}
	}

}
