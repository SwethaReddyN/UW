import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * Class that implements the DSatur Priority Queue algorithm
 */

public class DSaturPriorityQueue {

	/**
	 * method that implements graph coloring using DSatur algorithms
	 * @param graph Undirected graph
	 * @returns AlgorithmResult Returns the result of the class type AlgorithmResult
	 */
	public AlgorithmResult colorTheGraph(UndirectedGraph graph) {

		// initialize variables
		ArrayList<Vertex> adjacentVertices = null;
		HashSet<Integer> notAvailableColorList = new HashSet<Integer>();
		long runTime = 0;
		int tempColor, maxColor = 1;
		Vertex tempVertex = null, currentVertex = null;
		boolean colorSelected = false;
		
		// create a new priority queue using a helper method
		PriorityQueue<Vertex> verticesQueue = createPriorityQueue(graph);
		
		long startTime = System.nanoTime();

		// check if the priority queue is empty
		while(!verticesQueue.isEmpty()) {
			// obtain the top of the priority queue
			currentVertex = verticesQueue.poll();


			// obtain the adjacent vertices for the current vertex using a helper function
			adjacentVertices = currentVertex.getAdjacentVertices(); 

			/* increment the saturation degree of the adjacent vertices
				for a given selected vertex
			*/
			for(int i = 0; i < adjacentVertices.size(); i++) {
				
				tempVertex = adjacentVertices.get(i);
				tempColor = tempVertex.getVertexColor();
				if(tempColor != -1)
					notAvailableColorList.add(tempColor);
				else {
					
					verticesQueue.remove(tempVertex);
					tempVertex.incrementSaturationDegree();
					verticesQueue.add(tempVertex);
				}
			}

			// color the selected vertex using the algorithms
			for(int i = 1; i <= maxColor; i++) {
				
				if(!notAvailableColorList.contains(i)) {
					
					currentVertex.setVertexColor(i);
					colorSelected = true;
					break;
				}
			}
			
			if(!colorSelected) {
				
				currentVertex.setVertexColor(++maxColor);
			}
			colorSelected = false;
			notAvailableColorList.clear();
		} 

		// obtain the total time taken by the algorithm
		long endTime = System.nanoTime();
		runTime = endTime - startTime;

		// return the runtime and max color obtained by the algorithms
		return new AlgorithmResult(runTime, maxColor);
	}

	/**
	 * helper method that creates a new priority queue given an undirected graph
	 * @param graph the undirected graph
	 * @returns PriorityQueue priority queue created from the undirected graph
	 */
	public PriorityQueue<Vertex> createPriorityQueue(UndirectedGraph graph) {

		int numberOfVertices = graph.getTotalVertices();
		
		PriorityQueue<Vertex> verticesQueue = new PriorityQueue<Vertex>(numberOfVertices, 
				new Comparator<Vertex>() {
		    /**
             * override method that compares the saturation degree and adjacency degree of two vertices
             * This method first compares the saturation degree and compares the adjacency degree
             * if the saturation degree of two vertices are equal.
             * @return int returns the saturation degree or the adjacency degree by the above mentioned logic
             */
			@Override
			public int compare(Vertex vertex1, Vertex vertex2) {

				int satDegreeCmp = Integer.compare(vertex2.getSaturationDegree(), 
						vertex1.getSaturationDegree());

				return (satDegreeCmp != 0)?  satDegreeCmp : (
						Integer.compare(vertex2.getAdjacenceDegree(), vertex1.getAdjacenceDegree())); 
			}
		}); 

		// add all the graph vertices to the priority queue by the order of priority
		verticesQueue.addAll(graph.getVertices());
		return verticesQueue;
	}
}