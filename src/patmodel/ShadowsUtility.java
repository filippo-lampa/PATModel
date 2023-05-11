package patmodel;

import java.util.stream.Stream;

import org.antlr.stringtemplate.test.TestStringTemplate.Tree;

import java.util.List;
import java.util.ArrayList;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;

public final class ShadowsUtility {

	private ShadowsUtility() {
        throw new java.lang.UnsupportedOperationException("Utility class and cannot be instantiated");
    }
	
	//TODO possibility to change the ContinuousSpace to a variable;
	
	
	//TODO modify
	public static double percentageTreeCovered(Tree tree, ContinuousSpace<Object> space){
		
		List<Tree> neighbours = getNeighboursTreeInRange(tree, space);
		neighbours = filterNeighboursThatIntersect(space, neighbours, tree);
		neighbours = filterTallerNeighbours(neighbours, tree.getHeight());
		//at this point we should have the trees that intersect with the one passed and that are taller
		
		//at the end there are the function needed for the areas, but
		// i have to find a way to understand which circles are intersected with each other
		
		percentageCalculation(space, neighbours);
		
        return 1.0;
    }
	
	//TODO control and decide what to do with the maxRange variable
	public static List<Tree> getNeighboursTreeInRange(Tree tree, ContinuousSpace<Object> space) {
		double maxRange = 4.0; //this depend on the max radius possible * 2
		
		return Stream.of(space.getObjects())
				.filter(o -> o.getClass().equals(tree.getClass()) 
						&& space.getDistance(space.getLocation(o), space.getLocation(tree)) < maxRange)
				.toList();
	}
	
	//TODO control 
	private static List<Tree> filterNeighboursThatIntersect(ContinuousSpace<Object> space, List<Tree> neighbours, Tree tree) {
		
		List<Tree> intersect = new ArrayList();
		
		for(Tree t : neighbours) {
			if(space.getDistance(space.getLocation(t), space.getLocation(tree)) < tree.getWidth()/2 + t.getWidth()/2) {
				intersect.add(t);
			}
		}
		return intersect;
		
		/*return Stream.of(neighbours)
				.filter(t -> space.getDistance(space.getLocation(t), space.getLocation(tree)) < tree.getWidth()/2 + t.getWidth()/2)
				.toList();*/
	}
	
	//TODO control
	private static List<Tree> filterTallerNeighbours(List<Tree> neighbours, double treeHeight) {
		
		List<Tree> taller = new ArrayList();
		
		for(Tree t : neighbours) {
			if(t.getHeight() > treeHeight) {
				intersect.add(t);
			}
		}
		return taller;
		
		/*return Stream.of(neighbours)
				.filter(t -> t.getHeight() > treeHeight)
				.toList();*/
	}
	
	private static double percentageCalculation(ContinuousSpace<Object> space, List<Tree> neighbours, Tree tree) {
		
		List<List<Tree>> couples = new ArrayList();
		List<Tree> alones = new ArrayList();
		
		List<Tree> app = new ArrayList<>();
		app.addAll(neighbours);
		
		int i;
		for(Tree t : neighbours) {
			app.remove(t);
			i = 0;
			for(Tree t2 : app) {
				if(space.getDistance(space.getLocation(t), space.getLocation(t2)) < t.getWidth()/2 + t2.getWidth()/2) {
					List<Tree> couple = new ArrayList<>();
					couple.add(t);
					couple.add(t2);
					couples.add(couple);
					i++;
				}
			}
			if(i==0) alones.add(t);
		}
		
		double areaCouples = calculateAreaIntersectionCouplesTree(space, couples, tree);
		double areaAlones = calculateAreaIntersectionAlonesTree(space, alones, tree);
		double areaTreeTotal = (tree.getWidth()*tree.getWidth())*Math.PI;
		
		double percentageAreaCovered = (areaAlones+areaCouples)/areaTreeTotal;
		
		return percentageAreaCovered;
		
	}
	
	private static double calculateAreaIntersectionCouplesTree(ContinuousSpace<Object> space, List<List<Tree>> couples, Tree tree) {
		
		double totalArea = 0;
		
		for(List<Tree> couple : couples) {
			
			double a1 = intersectionAreaTwoCircles(space, couple.get(0), tree);
			double a2 = intersectionAreaTwoCircles(space, couple.get(1), tree);
			double a3 = circleOverlapTriangleArea(space, couple.get(0), couple.get(1), tree);
			
			totalArea += a1 + a2 - a3;
		}
		
		return totalArea;
	}
	
	private static double calculateAreaIntersectionAlonesTree(ContinuousSpace<Object> space, List<Tree> alones,Tree tree) {
		double totalArea = 0;
		
		for(Tree t : alones) {
			totalArea += intersectionAreaTwoCircles(space, t, tree);
		}
		
		return totalArea;
	}
	
	
	public static double intersectionAreaTwoCircles(ContinuousSpace<Object> space, Tree tree1, Tree tree2) {
	    double d = space.getDistance(space.getLocation(t), space.getLocation(tree));
	    double r1 = tree1.getWidth()/2;
	    double r2 = tree2.getWidth()/2;
	    
	    double angle1 = Math.acos((Math.pow(r1, 2) + Math.pow(d, 2) - Math.pow(r2, 2)) / (2 * r1 * d));
	    double segmentArea = r1 * r1 * angle1 - r1 * r1 * Math.sin(2 * angle1) / 2;
	    double intersectionArea = 2 * segmentArea;
	    
	    return intersectionArea;
	}
	
	
	public static double circleOverlapTriangleArea(ContinuousSpace<Object> space, Tree tree1, Tree tree2, Tree tree3) {
		double d12 = space.getDistance(space.getLocation(tree1), space.getLocation(tree2));
		double d23 = space.getDistance(space.getLocation(tree2), space.getLocation(tree3));
		double d31 = space.getDistance(space.getLocation(tree3), space.getLocation(tree1));

	    double s = (d12 + d23 + d31) / 2;
	    double areaTri = Math.sqrt(s * (s - d12) * (s - d23) * (s - d31));

	    return areaTri;
	}
	
	
	
}
