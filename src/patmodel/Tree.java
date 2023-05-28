package patmodel;

import java.util.ArrayList;

import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight;

import groovyjarjarantlr4.v4.parse.ANTLRParser.finallyClause_return;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.relogo.ide.dynamics.NetLogoSystemDynamicsParser.intg_return;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.util.ContextUtils;
import repast.simphony.visualization.visualization3D.style.DefaultStyle3D;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import repast.simphony.ws.DisplayProperties;

public class Tree extends DefaultStyle3D<Tree>{
	public static final double BASE_TREE_WIDHT = 0.01;
	public static final double BASE_TREE_HEIGHT = 0.02;
	public static final double BASE_TREE_AGE = 1;
	public static final int BASE_APPLE_QUANTITY = 10;
	public static final double BASE_FOLIAGE_DIAMETER = 0.02;
	private int MAX_AGE = 70*365;
	private final int MIN_AGE_PRODUCE_APPLE = 2*365;
	
	private double MAX_FOLIAGE_DIAMETER = 4;
	private double MAX_HEIGHT = 3;
	private final int MAX_APPLE_QUANTITY = 20;
	private double MAX_WIDTH = 0.3;
	
	private double width = 0.0;
	private double height = 0.0;
	private double age = 0;
	private double diameter = 0.0;
	
	private Context<Object> context;
	private ContinuousSpace<Object> space;
	private ArrayList<Apple> appleList;

	private int numberOfDaysWithoutNutrients  = 0;
	private int MAX_DAYS_SURVIVED_WITHOUT_NUTRIENTS = 14;
	
	private AppleOrchard soil;
	
	public Tree(Context<Object> context, ContinuousSpace<Object> space, AppleOrchard soil, double width, double height, double age, double diameter) {
		this.context = context;
		this.space = space;
		this.width = width;
		this.height = height;
		this.age = age;
		this.appleList = new ArrayList<>();
		this.soil = soil;
		//TODO in case implement initial apple creation, note that age matters!!!
		this.diameter = diameter;
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		this.MAX_HEIGHT = (double)p.getValue("treeMaxHeight");
		this.MAX_WIDTH = (double)p.getValue("treeMaxWidth");
		this.MAX_FOLIAGE_DIAMETER= (double)p.getValue("treeMaxFoliageDiameter");
		this.MAX_AGE= (int)p.getValue("treeMaxAge");
		
	}
	
	
	private boolean absorbNutrients(double amount) {
		if (soil.getNutrients()>=amount) {
			soil.addNutrients(-amount);
			return true;
		}
		return false;
	}
	
	private double calcNutrientsToGrow() {
		double ageContributions = 0;
		double ageInYears = age/365;
		if(ageInYears > 10.0) {//Once the tree is bigger thant 10 years, it will always need the maximum amount which is 0.01
			ageContributions = 0.001;
		} else {
			ageContributions = (ageInYears)/100;
		}
		return 0.1 + (width * 0.002) + (height * 0.002) + (diameter * 0.001) + ageContributions + (appleList.size()*0.002);
	}
	
	private boolean checkAgeTooOld() {
		return age >= MAX_AGE;
	}
	
	private boolean checkAgeTooYoung() {
		return age <= MIN_AGE_PRODUCE_APPLE;
	}
	
	private double calcNutrientsToSurvive( ) {
		return (width * 0.002) + (height * 0.002) + (diameter * 0.001);
	}
	
	private void createApples() {
		if(!checkAgeTooYoung()) {
			Apple newApple = new Apple(this.context, this.space, this.soil);
			if(appleList.size() > MAX_APPLE_QUANTITY) // remove additional apples
				releaseApples(appleList.size() - MAX_APPLE_QUANTITY);
			appleList.add(newApple);
		}		
	}
	
	private void die() {
		Context context = ContextUtils.getContext(this);
		context.remove(this);
	}
	
	private void notEnoughNutrients() {
		if(appleList.size() > 0) {
			releaseApples(1);
		} else if(this.numberOfDaysWithoutNutrients <= this.MAX_DAYS_SURVIVED_WITHOUT_NUTRIENTS){
			numberOfDaysWithoutNutrients++;
		} else {
			die();
		}
		return;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void update() {
		
		//Update method is called once per time tick
		double toGrow = calcNutrientsToGrow();
		double toSurvive = calcNutrientsToSurvive();
		if(checkAgeTooOld()) {
			die();
			return;
		}
		if(getSoilNutrientsQuantity() >= toGrow) {
			boolean result = absorbNutrients(toGrow);
			if(!result) {
				notEnoughNutrients();
			}
			createApples();
			grow();
		}
		else if(getSoilNutrientsQuantity() >= toSurvive){
			boolean result = absorbNutrients(toSurvive);
			if (!result) {
				notEnoughNutrients();
			}
		}else {
			notEnoughNutrients();
		}
		age += 1;
	}
	
	private double getSoilNutrientsQuantity() {
		return soil.getNutrients();
	}
	
	private void grow() {
		if(width < MAX_WIDTH) width += 0.0001;
		if(height < MAX_HEIGHT) height += 0.01;
		if(diameter < MAX_FOLIAGE_DIAMETER) diameter += 0.02;
	}
    
	public double getIconSize() {
		return width * 1000;
	}
	
	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}
	
	public void releaseApples(int nApples) {
		for(int i = 0; i < nApples; i++) {//release a random apple
			int index = AppleOrchard.RANDOM.nextInt(appleList.size());
			Apple toRemove = appleList.get(index);
			toRemove.fall();
			appleList.remove(toRemove);
		}
	}
	
	
}