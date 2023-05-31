package patmodel;

import java.util.ArrayDeque;
import java.util.Deque;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.PriorityType;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;

public class AppleOrchard extends DefaultContext<Object> implements ContextBuilder<Object> {
	private double MIN_DISTANCE_BETWEEN_TREES = 3;
	private static final double RAINING_NUTRIENTS_TRESHOLD = 0.5;
	private static final double SUNNY_NUTRIENTS_TRESHOLD = -0.1;

	private Context<Object> context;
	private ContinuousSpace<Object> space;

	ISchedule schedule;

	// private TupleSpace tupleSpace;

	// Daily states
	private boolean isRaining;
	private boolean isSunny;
	private boolean isWindy;

	// Arrays weather history: max 30 days
	private Deque<Boolean> sunWindow = new ArrayDeque<>();
	private Deque<Boolean> rainWindow = new ArrayDeque<>();
	private Deque<Boolean> windWindow = new ArrayDeque<>();

	// Thresholds
	private double rainThreshold;
	private double windThreshold;
	private double sunThreshold;

	public static final Random RANDOM = new Random();

	private double nutrients;

	private double totalApplesInOctober;
	
	public static final double APPLE_NUTRIENT_AMOUNT = 0.5;

	@Override
	public Context<Object> build(Context<Object> context) {

		context.setId("PATModel");

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		this.space = spaceFactory.createContinuousSpace(" space ", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.BouncyBorders(), 15, 15, 15);

		this.context = context;

		Parameters p = RunEnvironment.getInstance().getParameters();

		this.MIN_DISTANCE_BETWEEN_TREES = (double) p.getValue("initialDistanceBetweenTrees");

		SoilDesign soil = new SoilDesign();
		context.add(soil);
		space.moveTo(soil, 7.5, 0, 7.5);

		// this.tupleSpace = TupleSpace.getInstance();

		this.isRaining = false;
		this.isWindy = false;
		this.isSunny = false;

		this.totalApplesInOctober = 0;
		
		// minimal nutrients for a plant to survive at first time tick is equal to 0.1
		//this.nutrients = 1;
		initAgents();

		this.schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters params1 = ScheduleParameters.createRepeating(1, 1, 1);
		ScheduleParameters params2 = ScheduleParameters.createRepeating(1, 1, 2);
		this.schedule.schedule(params2, this, "updateSoil");
		this.schedule.schedule(params1, this, "updateWeather");		
		return context;
	}

	private void initAgents() {
		// TODO init soil and get height
		for (int row = 1; row < this.space.getDimensions().getDepth() / MIN_DISTANCE_BETWEEN_TREES; row++) {
			for (int column = 1; column < this.space.getDimensions().getWidth()
					/ MIN_DISTANCE_BETWEEN_TREES; column++) {
				Tree t = new Tree(this.context, this.space, this, 0, 0, 0, 0);
				this.context.add(t);
				this.space.moveTo(t, column * MIN_DISTANCE_BETWEEN_TREES, 0,
						row * MIN_DISTANCE_BETWEEN_TREES);
			}
		}
	}

	//temporary random weather method waiting for the fix of the stable one
	private void tempSetWeather() {
		int randomWeather = ThreadLocalRandom.current().nextInt(0, 3);
		switch(randomWeather) {
			case 0: { this.rain(true); 
					  this.sun(false);
					  this.wind(false);
					} break;
			case 1: { this.rain(false); 
					  this.sun(true);
					  this.wind(false);
					} break;
			case 2: { this.rain(false); 
					  this.sun(false);
					  this.wind(true);
					} break;
		}
	}
	
	/**
	 * Sets the state for the rain phenomenon.
	 * 
	 * @param state
	 */
	public void rain(boolean state) {
		this.isRaining = state;
		if(state == true)
			System.out.println("WEATHER: rainy");
	}

	/**
	 * Sets the state for the wind phenomenon.
	 */
	public void wind(boolean state) {
		this.isWindy = state;
		if(state == true)
			System.out.println("WEATHER: windy");
	}

	/**
	 * Sets the state for the sun phenomenon.
	 * 
	 * @param state
	 */
	public void sun(boolean state) {
		this.isSunny = state;
		if(state == true)
			System.out.println("WEATHER: sunny");
	}

	public void updateSoil() {
		deltaNutrients();
	}
	
	public void updateWeather() {
		double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if(currentTick % 365 == 305 || currentTick == 305) {
			this.printTotalNumberOfApplesInOctober();
			this.totalApplesInOctober = 0;
		}
		/*this.computeWeather(currentTick);
		this.updateWindows();*/
		this.tempSetWeather();
	}

	/*
	 * Slides and updates the rain, wind and sun time windows.
	 */
	private void updateWindows() {
		if (this.rainWindow.size() == 30)
			this.rainWindow.pop();
		this.rainWindow.add(this.isRaining);

		if (this.windWindow.size() == 30)
			this.windWindow.pop();
		this.windWindow.add(this.isWindy);

		if (this.sunWindow.size() == 30)
			this.sunWindow.pop();
		this.sunWindow.add(this.isSunny);
	}

	/*
	 * Returns the number of values where a phenomenon did not happen, then the
	 * number of false values.
	 */
	private int getFalseValues(Deque<Boolean> deque) {
		return (int) deque.stream().filter(v -> v == false).count();
	}

	/*
	 * Computes the corresponding weather based on the given current tick.
	 * 
	 * @param currentTick
	 */
	private void computeWeather(double currentTick) {
		Season season = computeSeason(currentTick);
		Pair<Double, Double> low = new Pair<>(0.0, 0.34);
		Pair<Double, Double> medium = new Pair<>(0.34, 0.67);
		Pair<Double, Double> high = new Pair<>(0.67, 1.0);

		// Computing weather for an event
		switch (season) {
		case WINTER:
			// high rain, mid wind, low sun
			computeThreshold(high, medium, low);
			break;
		case SPRING:
			// low rain, mid wind, mid sun
			computeThreshold(low, medium, medium);
			break;
		case SUMMER:
			// low rain, low wind, high sun
			computeThreshold(low, low, high);
			break;
		case AUTUMN:
			// high rain, high wind, low sun
			computeThreshold(high, high, low);
			break;
		}
		computeWeatherProbability();
	}

	/*
	 * Computes and sets the weather thresholds depending on the minimum and maximum
	 * ratio values for the rain, wind and sun. Each parameter is defined as a
	 * {@link Pair} that has as first the minimum value and as second the maximum
	 * value of the phenomenon.
	 * 
	 * @param rain
	 * 
	 * @param wind
	 * 
	 * @param sun
	 */
	private void computeThreshold(Pair<Double, Double> rain, Pair<Double, Double> wind, Pair<Double, Double> sun) {
		double rainRatio = RANDOM.nextDouble(rain.getFirst(), rain.getSecond());
		this.rainThreshold = rainRatio - rain.getFirst();

		double windRatio = RANDOM.nextDouble(wind.getFirst(), wind.getSecond());
		this.windThreshold = windRatio - wind.getFirst();

		double sunRatio = RANDOM.nextDouble(sun.getFirst(), sun.getSecond());
		this.sunThreshold = sunRatio - sun.getFirst();
	}

	/*
	 * Computes and sets the final daily states of rain, wind and sun phenomenon
	 * applying the weather probability formula.
	 */
	private void computeWeatherProbability() {
		int p = this.getFalseValues(this.rainWindow);
		rain((1 - Math.pow(Math.E, p) / (1 + Math.pow(Math.E, (1 - p)))) > this.rainThreshold);

		p = this.getFalseValues(this.windWindow);
		wind((1 - Math.pow(Math.E, p) / (1 + Math.pow(Math.E, (1 - p)))) > this.windThreshold);

		p = this.getFalseValues(this.sunWindow);
		sun((1 - Math.pow(Math.E, p) / (1 + Math.pow(Math.E, (1 - p)))) > this.sunThreshold);
	}

	/*
	 * Computes the corresponding season given the the current tick as a {@code
	 * double}.
	 * 
	 * @param currentTick
	 * 
	 * @return the computed season
	 */
	private Season computeSeason(double currentTick) {
		// Computes the month
		int month = (int) ((currentTick % 366) / 30);
		if (month == 0)
			// case day 1, aka time tick 0
			month = 1;
		if (month < 1 || month > 12)
			throw new IllegalArgumentException("time tick: " + currentTick + " generated invalid month: " + month);
		switch (month) {
		case 3:
			return Season.SPRING;
		case 4:
			return Season.SPRING;
		case 5:
			return Season.SPRING;
		case 6:
			return Season.SUMMER;
		case 7:
			return Season.SUMMER;
		case 8:
			return Season.SUMMER;
		case 9:
			return Season.AUTUMN;
		case 10:
			return Season.AUTUMN;
		case 11:
			return Season.AUTUMN;
		case 12:
			return Season.WINTER;
		case 1:
			return Season.WINTER;
		case 2:
			return Season.WINTER;
		default:
			return Season.WINTER;
		}
	}

	private void deltaNutrients() {
		if (isRaining)
			nutrients += RAINING_NUTRIENTS_TRESHOLD;
		else if (isSunny)
			if(nutrients + SUNNY_NUTRIENTS_TRESHOLD >= 0)
				nutrients += SUNNY_NUTRIENTS_TRESHOLD;
	}
	
	public void addOneToTotalApplesInOctober() {
		this.totalApplesInOctober ++;
	}

	public void printTotalNumberOfApplesInOctober() {
		System.out.println("TOTAL APPLES GROWN DURING OCTOBER: " + this.totalApplesInOctober);
	}
	
	// TODO from here remove when tuple space will be implemented

	public void addNutrients(double nutrients) {
		this.nutrients += nutrients;
	}

	public double getNutrients() {
		return nutrients;
	}

	// Until here

	class Pair<F, S> {

		private F first;
		private S second;

		public Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}

		public F getFirst() {
			return this.first;
		}

		public S getSecond() {
			return this.second;
		}
	}
}
