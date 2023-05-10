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

	Context<Object> context;
	Grid<Object> grid;
	ContinuousSpace<Object> space;
	
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
 	
		
		return context;
	}
	
	public void rain(boolean state) {

	}

	public void wind(boolean state) {

	}

	public void sun(boolean state) {

	}

	@ScheduledMethod(start = 1, interval = 1)
	private void update() {

	}
	
		
}
