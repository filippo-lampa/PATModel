package patmodel;

public class Soil {

	public double nutrientsAmount = 100.0;
	
	public Soil(double initialNutrients) {
		nutrientsAmount = initialNutrients;
	}
	
	public void increaseNutriens(double amount) {
		nutrientsAmount += amount;
	}
	
	public void decreaseNutrients(double amount) {
		nutrientsAmount -= amount;
		if(nutrientsAmount < 0)
			nutrientsAmount = 0;
	}

	public double getNutrientsAmount() {
		return nutrientsAmount;
	}

	public void setNutrientsAmount(double nutrientsAmount) {
		this.nutrientsAmount = nutrientsAmount;
	}
}
