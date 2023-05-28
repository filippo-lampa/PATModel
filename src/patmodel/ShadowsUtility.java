package patmodel;

import java.util.stream.Stream;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;

public final class ShadowsUtility {

	private ShadowsUtility() {
        throw new java.lang.UnsupportedOperationException("Utility class and cannot be instantiated");
    }
	
	
	public static double percentageTreeCovered(Tree tree, ContinuousSpace<Object> space){
		
		List<Tree> neighbours = getNeighboursTreeInRange(tree, space);
		neighbours = filterNeighboursThatIntersect(space, neighbours, tree);
		neighbours = filterTallerNeighbours(neighbours, tree.getHeight());
		
        return percentageCalculation(space, neighbours, tree);
    }
	
	//-------------------- Starting filter of neighbors -------------------------
	
	
	private static List<Tree> getNeighboursTreeInRange(Tree tree, ContinuousSpace<Object> space) {
		double maxRange = 4.0; //this depend on the max radius possible * 2
		
		return Stream.of(space.getObjects())
				.filter(o -> o.getClass().equals(tree.getClass()) 
						&& space.getDistance(space.getLocation(o), space.getLocation(tree)) < maxRange)
				.map(o -> (Tree) o)
				.toList();
	}
	
	
	private static List<Tree> filterNeighboursThatIntersect(ContinuousSpace<Object> space, List<Tree> neighbours, Tree tree) {
		
		List<Tree> intersect = new ArrayList<Tree>();
		
		for(Tree t : neighbours) {
			if(space.getDistance(space.getLocation(t), space.getLocation(tree)) < tree.getWidth()/2 + t.getWidth()/2) {
				intersect.add(t);
			}
		}
		return intersect;
	}
	
	private static List<Tree> filterTallerNeighbours(List<Tree> neighbours, double treeHeight) {
		
		List<Tree> taller = new ArrayList<Tree>();
		
		for(Tree t : neighbours) {
			if(t.getHeight() > treeHeight) {
				taller.add(t);
			}
		}
		return taller;
	}
	
	//----------------------------------------------------------------------
	
	//-------------------- Shadow calculation -------------------------
	
	private static double percentageCalculation(ContinuousSpace<Object> space, List<Tree> neighbours, Tree tree) {
		
		List<LinkedList<Tree>> neighbourIntersections = new ArrayList<LinkedList<Tree>>();
		List<Tree> app = new ArrayList<>();
		app.addAll(neighbours);
		
		int i;
		for(Tree t : neighbours) {
			app.remove(t);
			i = 0;
			for(Tree t2 : app) {
				if(space.getDistance(space.getLocation(t), space.getLocation(t2)) < t.getWidth()/2 + t2.getWidth()/2
						&& hasCommonArea(space.getLocation(tree), tree.getWidth(), space.getLocation(t), t.getWidth(), space.getLocation(t2), t2.getWidth())) {
					LinkedList<Tree> couple = new LinkedList<Tree>();
					couple.add(t);
					couple.add(t2);
					neighbourIntersections.add(couple);
					i++;
				}
			}
			if(i==0) {
				LinkedList<Tree> alone = new LinkedList<Tree>();
				alone.add(t);
				neighbourIntersections.add(alone);
			}
		}
		
		
		neighbourIntersections = mergeLists(neighbourIntersections);
		
		double areaShadow = 0.0;
		for (LinkedList<Tree> list : neighbourIntersections) {
			
			if(list.size() == 1) {
				areaShadow += intersectionAreaTwoCircles(space, tree, list.getFirst());
			}
			else {
				for(int j = 0; j<list.size(); j++) {
					if(j == 0) {
						areaShadow += intersectionAreaTwoCircles(space, tree, list.get(0));
					}
					else {
						areaShadow += intersectionAreaTwoCircles(space, tree, list.get(j)) 
								- circleOverlapTriangleArea(space, tree, list.get(j-1), list.get(j));
					}
				}
			}	
		}
		
		double areaTreeTotal = (tree.getWidth()*tree.getWidth())*Math.PI;
		
		return areaShadow/areaTreeTotal;
		
	}
	
	private static List<LinkedList<Tree>> mergeLists(List<LinkedList<Tree>> lists) {
        List<LinkedList<Tree>> mergedLists = new ArrayList<LinkedList<Tree>>();

        for (LinkedList<Tree> list : lists) {
            boolean merged = false;

            // Iterate over the merged lists to find a merge candidate
            ListIterator<LinkedList<Tree>> iterator = mergedLists.listIterator();
            while (iterator.hasNext()) {
                LinkedList<Tree> mergedList = iterator.next();

                // Check if the first element of the current list matches the last element of the merged list
                if (mergedList.getLast().equals(list.getFirst())) {
                    mergedList.removeLast();
                    mergedList.addAll(list);
                    merged = true;
                    break;
                }

                // Check if the last element of the current list matches the first element of the merged list
                if (list.getLast().equals(mergedList.getFirst())) {
                    mergedList.removeFirst();
                    list.addAll(mergedList);
                    iterator.set(list);
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                // If no merge occurred, add the list as a separate merged list as linked list
                mergedLists.add(list);
            }
        }

        return mergedLists;
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
	
	//-------------------------------------------------------------------------
	
	//---------------- Common area calculation ------------------------------
	
	
	private static boolean hasCommonArea(NdPoint center1, double r1, NdPoint center2, double r2, NdPoint center3, double r3) {
        // Find the intersection points of the circles
        NdPoint[] intersections = findIntersectionPoints(center2, r2, center3, r3);
        
        return findWitchPointIsInsideCircle(center1, r1, intersections) != null;
    }

    private static boolean isPointInsideCircle(NdPoint center1, double radius1, NdPoint point) {
        double distance1 = Math.sqrt(Math.pow(point.getX() - center1.getX(), 2) + Math.pow(point.getY() - center1.getY(), 2));
        return distance1 <= radius1;
    }

    private static NdPoint[] findIntersectionPoints(NdPoint center1, double r1, NdPoint center2, double r2) {

        // Calculate the distance between the centers of the two circles
    	double distance = Math.sqrt(Math.pow((center2.getX() - center1.getX()),2) + Math.pow((center2.getY() - center1.getY()),2));

        // Calculate the distance from the first center to the intersection point
        double a = (r1 * r1 - r2 * r2 + distance * distance) / (2 * distance);

        // Calculate the height of the triangle formed by the centers and the intersection point
        double h = Math.sqrt(r1 * r1 - a * a);

        // Calculate the coordinates of the intersection points
        double x3 = center1.getX() + a * (center2.getX() - center1.getX()) / distance;
        double y3 = center1.getY() + a * (center2.getY() - center1.getY()) / distance;

        // Calculate the coordinates of Intersection Point 1
        double x = x3 + h * (center2.getY() - center1.getY()) / distance;
        double y = y3 - h * (center2.getX() - center1.getX()) / distance;
        NdPoint intersection1 = new NdPoint(x,y);

        // Calculate the coordinates of Intersection Point 2
        x = x3 - h * (center2.getY() - center1.getY()) / distance;
        y = y3 + h * (center2.getX() - center1.getX()) / distance;
        NdPoint intersection2 = new NdPoint(x,y);

        // Return the intersection points as an array
        return new NdPoint[]{intersection1, intersection2};
    }
    
    private static NdPoint findWhichPointIsInsideCircle(NdPoint center1, double r1, NdPoint[] points) {
    	if(isPointInsideCircle(center1, r1, points[0])) return points[0];
    	else if(isPointInsideCircle(center1, r1, points[1])) return points[1];
    	else return null;
	}
	
	//------------------------------------------------
    
    
    //----------- Math Area Circles ----------------
	
	private static double intersectionAreaTwoCircles(ContinuousSpace<Object> space, Tree tree1, Tree tree2) {
	    double d = space.getDistance(space.getLocation(tree1), space.getLocation(tree2));
	    
	    NdPoint center1 = space.getLocation(tree1);
		NdPoint center2 = space.getLocation(tree2);
		double r1 = tree1.getWidth()/2;
	    double r2 = tree2.getWidth()/2;
	    NdPoint[] intersectionPoints = findIntersectionPoints(center1, r1, center2, r2);
	    
	    double circularSegmentArea1 = circularSegmentArea(center1, intersectionPoints[0], intersectionPoints[1], r1);
	    double circularSegmentArea2 = circularSegmentArea(center2, intersectionPoints[0], intersectionPoints[1], r2);
	    
	    return circularSegmentArea1 + circularSegmentArea2;
	}
	
	
	private static double circleOverlapTriangleArea(ContinuousSpace<Object> space, Tree tree1, Tree tree2, Tree tree3) {
		
		NdPoint center1 = space.getLocation(tree1);
		NdPoint center2 = space.getLocation(tree2);
		NdPoint center3 = space.getLocation(tree3);
		double r1 = tree1.getWidth()/2;
	    double r2 = tree2.getWidth()/2;
	    double r3 = tree3.getWidth()/2;
		
	    NdPoint[] intersectionPoints12 = findIntersectionPoints(center1, r1, center2, r2);
	    NdPoint[] intersectionPoints23 = findIntersectionPoints(center2, r2, center3, r3);
	    NdPoint[] intersectionPoints31 = findIntersectionPoints(center3, r3, center1, r1);
		
	    NdPoint point1 = findWitchPointIsInsideCircle(center3, r3, intersectionPoints12);
	    NdPoint point2 = findWitchPointIsInsideCircle(center1, r1, intersectionPoints23);
	    NdPoint point3 = findWitchPointIsInsideCircle(center2, r2, intersectionPoints31);
		
	    if(point1 == null || point2 == null || point3 == null) {
	    	throw new NullPointerException("This is not supposed to happen");
	    }
	    
	    //calculate the area using the Shoelace formula for the inside triangle
		double area = 0.5 * Math.abs((point1.getX() * (point2.getY() - point3.getY()) 
				+ point2.getX() * (point3.getY() - point1.getY()) 
				+ point3.getX() * (point1.getY() - point2.getY())));
		
		//calculate the remaining parts that are not in the triangle
		area += circularSegmentArea(center1, point1, point3);
		area += circularSegmentArea(center2, point1, point2);
		area += circularSegmentArea(center3, point2, point3);
		
	    return area;
	}
	
	
	
	private static double circularSegmentArea(NdPoint center, NdPoint point1, NdPoint point2) {
		// Calculate radius
        double radius = Math.sqrt(Math.pow(center.getX() - point1.getX(), 2) + Math.pow(center.getY() - point1.getY(), 2));
        
        return circularSegmentArea(center, point1, point2, radius);
	}
	
	private static double circularSegmentArea(NdPoint center, NdPoint point1, NdPoint point2, double radius) {
		
		double centerX = center.getX();
		double centerY = center.getY();
		double point1X = point1.getX();
		double point1Y = point1.getY();
		double point2X = point2.getX();
		double point2Y = point2.getY();
		
		// Calculate the arc length
        double centralAngle = Math.acos(((point1X - centerX) * (point2X - centerX) + (point1Y - centerY) * (point2Y - centerY))
                / (Math.sqrt(Math.pow(point1X - centerX, 2) + Math.pow(point1Y - centerY, 2))
                * Math.sqrt(Math.pow(point2X - centerX, 2) + Math.pow(point2Y - centerY, 2))));
        double arcLength = radius * centralAngle;
        
        // Calculate the area of the circular sector
        double sectorArea = 0.5 * Math.pow(radius, 2) * centralAngle;
        
        // Calculate the area of the triangle
        double triangleArea = 0.5 * Math.abs((point1X - centerX) * (point2Y - centerY) - (point2X - centerX) * (point1Y - centerY));
        
        // Calculate the area of the circular segment
        double segmentArea = sectorArea - triangleArea;
        
        return segmentArea;
	}
	
	
	
	
	
}
