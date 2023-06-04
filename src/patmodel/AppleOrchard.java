package patmodel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;

import patmodel.logger.*;

public class AppleOrchard extends DefaultContext<Object> implements ContextBuilder<Object> {
	private double MIN_DISTANCE_BETWEEN_TREES = 3;
	private static final double RAINING_NUTRIENTS_TRESHOLD = 0.5;
	private static final double SUNNY_NUTRIENTS_TRESHOLD = -0.1;
	public static final int DAYS_IN_A_YEAR = 365;
	public static final int DAYS_IN_A_MONTH = 30;
	private static final double BONUS_AMOUNT = 0.1;
	private static final double MALUS_AMOUNT = 0.2;
	public static final double APPLE_NUTRIENT_AMOUNT = 0.5;
	
	public static final double OCTOBER_ENDS_AT_DAY = 300;
	
	public static final Random RANDOM = new Random();
	
	private Context<Object> context;
	private ContinuousSpace<Object> space;

	ISchedule schedule;

	private TupleSpace tupleSpace;

	private boolean isRaining;
	private boolean isSunny;
	private boolean isWindy;
	
	private int consecutiveNonWindyDays = 0;
	private int consecutiveNonSunnyDays = 0;
	private int consecutiveNonRainyDays = 0;
	
	private int consecutiveWindyDays = 0;
	private int consecutiveSunnyDays = 0;
	private int consecutiveRainyDays = 0;
	
	private int windyDaysInAMonth = 0;
	private int sunnyDaysInAMonth = 0;
	private int rainyDaysInAMonth = 0;
	
	//History arrays
	private ArrayList<boolean[]> historyWinter = new ArrayList<boolean[]>();
	private ArrayList<boolean[]> historySpring = new ArrayList<boolean[]>();
	private ArrayList<boolean[]> historySummer = new ArrayList<boolean[]>();
	private ArrayList<boolean[]> historyAutumn = new ArrayList<boolean[]>();

	private double nutrients;
	private double totalApplesInOctober;
	
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

		this.tupleSpace = TupleSpace.getInstance();

		this.isRaining = false;
		this.isWindy = false;
		this.isSunny = false;
		
		this.totalApplesInOctober = 0;
		
		this.tupleSpace.out("nutrients", 0.0);
		
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

	
	/**
	 * Sets the state for the rain phenomenon.
	 * 
	 * @param state
	 */
	public void rain(boolean state) {
		this.isRaining = state;
		if(state == true) {
			Logger.getLogger().Debug("WEATHER: rainy", true, this.getClass().getName());
		}
	}

	/**
	 * Sets the state for the wind phenomenon.
	 */
	public void wind(boolean state) {
		this.isWindy = state;
		if(state == true) {
			this.tupleSpace.out("wind", true);
			Logger.getLogger().Debug("WEATHER: windy", true, this.getClass().getName());
		}
	}

	/**
	 * Sets the state for the sun phenomenon.
	 * 
	 * @param state
	 */
	public void sun(boolean state) {
		this.isSunny = state;
		if(state == true) {
			Logger.getLogger().Debug("WEATHER: sunny", true, this.getClass().getName());
		}
	}

	public void updateSoil() {
		deltaNutrients();
	}
	
	/*
	 * Manages the update of the weather at each timetick 
	 */
	public void updateWeather() {
		double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if(currentTick % DAYS_IN_A_YEAR == OCTOBER_ENDS_AT_DAY|| currentTick == OCTOBER_ENDS_AT_DAY) {
			this.printTotalNumberOfApplesInOctober();
			this.totalApplesInOctober = 0;
		}
		//reset wind
		this.tupleSpace.out("wind", false);
		this.computeWeather(currentTick);
	}

	/*
	 * Computes the corresponding weather based on the given current tick.
	 * The weather is computed by referring to a real dataset of what was the weather in
	 * Macerata Marche in 2021-2022
	 * 
	 * @param currentTick
	 */
	private void computeWeather(double currentTick) {
		if(currentTick == 1) {
			String line;
			//if this is the first time load the dataset.
		  try (BufferedReader br = new BufferedReader(new FileReader("./src/data/dataset_simplified.csv"))) {
	            while ((line = br.readLine()) != null) {
	                String[] data = line.split(",");
	                boolean[] weatherInfo = new boolean[3];
	                for(int i = 1; i < data.length; i++) {
	                	weatherInfo[i-1] = data[i].equals("1");
	                }
	                switch(Integer.parseInt(data[0])) {
                		case 0: 
                			historyWinter.add(weatherInfo);
                			break;
                		case 1:
                			historySpring.add(weatherInfo);
                			break;
                		case 2:
                			historySummer.add(weatherInfo);
                			break;
                		case 3:
                			historyAutumn.add(weatherInfo);
                			break;
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }		  
		}

		// Get the current season and compute weather.
		Season season = computeSeason(currentTick);
		switch (season) {
		case WINTER:
			generateRandomDays(historyWinter);
			break;
		case SPRING:
			generateRandomDays(historySpring);
			break;
		case SUMMER:
			generateRandomDays(historySummer);
			break;
		case AUTUMN:
			generateRandomDays(historyAutumn);
			break;
		}
		
		// Compute monthly rates
		double windyMonthlyRate = 0.0;
		double sunnyMonthlyRate = 0.0;
		double rainyMonthlyRate = 0.0;
		
		if(this.windyDaysInAMonth != 0) {
			windyMonthlyRate = (double) windyDaysInAMonth / (double) DAYS_IN_A_MONTH;
		}
		if(this.sunnyDaysInAMonth != 0) {
			sunnyMonthlyRate = (double) sunnyDaysInAMonth / (double) DAYS_IN_A_MONTH;

		}
		if(this.rainyDaysInAMonth != 0) {
			rainyMonthlyRate = (double) rainyDaysInAMonth / (double) DAYS_IN_A_MONTH;
		}
		
		//Compute decision
		boolean shouldWind = computeProbability(windyMonthlyRate, consecutiveNonWindyDays, consecutiveWindyDays);
		boolean shouldSun = computeProbability(sunnyMonthlyRate, consecutiveNonSunnyDays, consecutiveSunnyDays);
		boolean shouldRain = computeProbability(rainyMonthlyRate, consecutiveNonRainyDays, consecutiveRainyDays);
		
		//If no rain or no sun, choose one randomly.
		if(shouldSun == false && shouldRain == false) {
			boolean defaultDecision = RANDOM.nextBoolean();
			sun(defaultDecision);
			rain(!defaultDecision);
		}
		else {
			sun(shouldSun);
			rain(shouldRain);	
		}
		wind(shouldWind);

		//update consecutive days
		this.consecutiveNonWindyDays = shouldWind ? 0 : this.consecutiveNonWindyDays + 1;
		this.consecutiveNonSunnyDays = shouldSun ? 0 : this.consecutiveNonSunnyDays + 1;
		this.consecutiveNonRainyDays = shouldRain ? 0 : this.consecutiveNonRainyDays + 1;
		
		this.consecutiveWindyDays = shouldWind ? this.consecutiveWindyDays + 1 : 0;
		this.consecutiveSunnyDays = shouldSun ? this.consecutiveSunnyDays + 1 : 0;
		this.consecutiveRainyDays = shouldRain ? this.consecutiveRainyDays + 1 : 0;
	}
	
	/*
	 * Computes the probability of an event given the rate of it, the consecutive and non consecutive amount of days
	 */
	private boolean computeProbability(double rateOfEvent, double consecutiveNonDays, double consecutiveDays) {
        Random random = new Random();
        double randomDouble = random.nextDouble();
        double bonus = consecutiveNonDays * BONUS_AMOUNT;
        double malus = consecutiveDays * MALUS_AMOUNT;
        double cumulativeProbabilityOfEvent = rateOfEvent + bonus - malus;
        return randomDouble <= cumulativeProbabilityOfEvent;
    }
	
	// Pick 30 days of a specific season and save the windy,sunny and rainy ones
	private void generateRandomDays(ArrayList<boolean[]> historySeason) {
		this.windyDaysInAMonth = 0;
		this.sunnyDaysInAMonth = 0;
		this.rainyDaysInAMonth = 0;
		for(int i = 0; i < DAYS_IN_A_MONTH; i++) {
			int maxSize = historySeason.size();
			int randomIndex = RANDOM.nextInt(maxSize);
			boolean[] pickedDay = historySeason.get(randomIndex);
			if(pickedDay[0]) {
				this.windyDaysInAMonth = this.windyDaysInAMonth + 1;
			}
			if(pickedDay[1]) {
				this.sunnyDaysInAMonth = this.sunnyDaysInAMonth + 1;
			}
			if(pickedDay[2]) {
				this.rainyDaysInAMonth = this.rainyDaysInAMonth + 1;
			}
		}
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
		int month = (int) ((currentTick % DAYS_IN_A_YEAR) / DAYS_IN_A_MONTH);
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
			this.tupleSpace.out("nutrients", (double)this.tupleSpace.rd("nutrients") + RAINING_NUTRIENTS_TRESHOLD);
		else if (isSunny)
			if(nutrients + SUNNY_NUTRIENTS_TRESHOLD >= 0)
				this.tupleSpace.out("nutrients", (double)this.tupleSpace.rd("nutrients") + SUNNY_NUTRIENTS_TRESHOLD);
		// Perhaps if the weather is windy we should push something to the tuple space, so that trees and apples can check for the wind conditions and in case die or fall
	}
	
	public void addOneToTotalApplesInOctober() {
		this.totalApplesInOctober ++;
	}
	
	public double getTotalNumberOfApplesInOctober() {
		return this.totalApplesInOctober;
	}
	
	public void printTotalNumberOfApplesInOctober() {
		Logger.getLogger().Info("Total Apples Grown in October: " + this.totalApplesInOctober, true, this.getClass().getName());
	}
}
