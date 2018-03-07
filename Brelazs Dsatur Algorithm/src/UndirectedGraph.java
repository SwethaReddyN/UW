import java.util.ArrayList;
import java.util.HashMap;

/**
 * helper class that represents an Undirected Graph
 */
public class UndirectedGraph {

	// class variables
	private int numberOfVertices;
	private int numberOfEdges;

	private HashMap<String, Vertex> vertices;

	/**
	 * parameterized constructor
	 * @param numberOfVertices number of vertices in the graph
	 */
	public UndirectedGraph(int numberOfVertices) {

		this.numberOfVertices = numberOfVertices;
		//		adjacenceMatrix = new HashMap<String, ArrayList<Integer>>();
		vertices = new HashMap<String, Vertex>();
	}

	/**
	 * method that returns the total number of edges in the graph
	 * @returns int total number of edges in the graph
	 */
	public int getTotalEdges() {

		return numberOfEdges;
	}

	/**
	 * method that returns the total number of vertices in the graph
	 * @returns int total number of vertices in the graph
	 */
	public int getTotalVertices() {

		return numberOfVertices;
	}

	/**
	 * method that returns the list of vertices of the graph
	 * @returns ArrayList List of graph vertices
	 */
	public ArrayList<Vertex> getVertices() {

		return new ArrayList<>(vertices.values());
	}

	/**
	 * method that adds a vertex to the graphs given a vertex
	 * @param vertex the vertex to be added
	 * @returns boolean true if the vertex is added false otherwise
	 */
	public boolean addVertex(int vertexId) {

		if(vertexId > numberOfVertices)
			return false;

		if(vertices.containsKey("N" + vertexId))
			return true;
		Vertex vertex = new Vertex("N" + vertexId);
		vertices.put("N" + vertexId, vertex);
		return true;
	}

	/**
	 * method that adds an edge to the graphs given a vertex
	 * @param edgePoint1 the first point of the edge
	 * @param edgePoint2 the second point of the edge
	 * @returns boolean true if the edge is added false otherwise
	 */
	public boolean addEdge(int edgePoint1, int edgePoint2) {

		if(edgePoint1 > numberOfVertices || edgePoint2 > numberOfVertices)
			return false;

		Vertex vertex1 = null, vertex2 = null;
		if(addVertex(edgePoint1)) {

			vertex1 = vertices.get("N" + edgePoint1);
		}
		if(addVertex(edgePoint2)) {

			vertex2 = vertices.get("N" + edgePoint2);
		}

		if(vertex1 != null && vertex2 != null) {
			vertex1.addAdjacentVertex(vertex2);
			vertex2.addAdjacentVertex(vertex1);
		}

		numberOfEdges++;
		return true;
	}

	/**
	 * helper function to print the graph
	 */
	public void printGraph() {

		Vertex tempVertex;
		for(int i = 0; i < numberOfVertices; i++) {

			tempVertex = vertices.get("N" + i);
			System.out.print("For vertex " + tempVertex.getVertexId() + " adjacency degree = " );
			System.out.println(tempVertex.getAdjacenceDegree());
			ArrayList<Vertex> adjacentVertices = tempVertex.getAdjacentVertices();
			for(int j = 0; j < adjacentVertices.size(); j++) {

				System.out.print(adjacentVertices.get(j).getVertexId() + "=>");
			}
			System.out.println("----------------------------");
		}
	}
}