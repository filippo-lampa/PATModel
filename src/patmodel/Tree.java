package patmodel;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Tree {
	public static final double BASE_TREE_WIDHT = 1;
	public static final double BASE_TREE_HEIGHT = 2;
	public static final double BASE_TREE_AGE = 1;
	public static final int BASE_APPLE_QUANTITY = 10;
	public static final double BASE_FOLIAGE_DIAMETER = 2;
	private final int MAX_AGE = 70*365;
	private final int MIN_AGE_PRODUCE_APPLE = 2*365;
	
	private final int MAX_FOLIAGE_DIAMETER = 4;
	private final int MAX_HEIGHT = 3;
	private final int MAX_APPLE_QUANTITY = 20;
	
	private double width = 0.0;
	private double height = 0.0;
	private double age = 0;
	private int appleQuantity = 0;
	private double diameter = 0.0;
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public Tree(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	public Tree(ContinuousSpace<Object> space, Grid<Object> grid, double width, double height, double age, int appleQuantity, double diameter) {
		this.space = space;
		this.grid = grid;
		this.width = width;
		this.height = height;
		this.age = age;
		this.appleQuantity = appleQuantity;
		this.diameter = diameter;
	}
	
	
	private void absorbNutrients(double amount) {
		//soil.decreaseNutrients(amount);
		//TODO wait for communication implementation
	}
	
	private double calcNutrientsToGrow() {
		return width + height + diameter + (age/10) + (appleQuantity*0.2);
		//TODO implement formula to calculate the correct value
	}
	
	private boolean checkAgeTooOld() {
		if(age >= MAX_AGE ) {
			return true;
		}else {
			return false;
		}	
	}
	
	private boolean checkAgeTooYoung() {
		return age > MIN_AGE_PRODUCE_APPLE;
		}
	
	private double calcNutrientsToSurvive( ) {
		//TODO implement formula to calculate the correct value
		return (width + height + diameter + (age/10))/2;
	}
	
	private void createApples() {
		if(checkAgeTooYoung() && appleQuantity < MAX_APPLE_QUANTITY)
			appleQuantity =+1;
	}
	
	private void die() {
		var context = ContextUtils.getContext(this);
		context.remove(this);
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	private void update() {
		//Update method is called once per time tick
		double toGrow = calcNutrientsToGrow();
		double toSurvive = calcNutrientsToSurvive();
		if(checkAgeTooOld()) {
			die();
			return;
		}
		if(getSoilNutrientsQuantity() >= toGrow) {
			absorbNutrients(toGrow);
			createApples();
			grow();
		}
		else if(getSoilNutrientsQuantity() >= toSurvive){
			absorbNutrients(toSurvive);
		}else {
			die();
			return;
		}
		age += 1;
	}
	
	private double getSoilNutrientsQuantity() {
		//return soil.getNutrientsAmount();
		//TODO wait for communication implementation
		return 0;
	}
	
	private void grow() {
		width =+ 0.0001;
		height=+ 0.01;
		diameter =+ 0.02;
	}
	
	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}
}