import java.util.ArrayList;

/**
 * Vertex Class that represents a vertex in the Graph
 */
public class Vertex {


	// class variables
	String vertexId;
	int vertexColor = -1;
	int saturationDegree = 1;
	int adjacencyDegree = 0;

	ArrayList<Vertex> adjacentVertices = new ArrayList<Vertex>();

	/**
	 * Parameterized constructor
	 * @param vertexId Id of the vertex.
	 */
	public Vertex(String vertexId) {

		this.vertexId = vertexId;
		saturationDegree = 0;
	}

	/**
	 * method to obtain the vertexId
	 * @return string Id of the vertex.
	 */
	public String getVertexId() {

		return vertexId;
	}

	/**
	 * method to obtain the vertex color
	 * @return int color of the vertex
	 */
	public int getVertexColor() {

		return vertexColor;
	}

	/**
	 * Setter method to set the vertex color
	 */
	public void setVertexColor(int color) {

		vertexColor = color;
	}

	/**
	 * method to check if a given vertex is adjacent to the current vertex
	 * @return boolean returns true if the the given vertex is adjacent to the current 
	 * vertex else returns false
	 */
	public boolean isAdjacentVertex(Vertex vertex) {

		for(int i = 0; i < adjacentVertices.size(); i++) {

			if(adjacentVertices.get(i).getVertexId()
					.compareTo(vertex.vertexId) == 0)
				return true;
		}
		return false;
	}

	/**
	 * method to add an adjacent vertex to a given vertex
	 * @return boolean returns true after adding an adjacent vertex.
	 */
	public boolean addAdjacentVertex(Vertex vertex) {

		if(isAdjacentVertex(vertex)) {
			return true;
		}

		adjacentVertices.add(vertex);
		incrementAdjacencyDegree();
		return true;
	}

	/**
	 * method that returns the adjacent vertices
	 * @return ArrayList returns an array list of vertices.
	 */
	public ArrayList<Vertex> getAdjacentVertices() {

		return adjacentVertices;
	}

	/**
	 * method that compares a given vertex's saturation degree and adjacency degree with
	 * the current vertex
	 * This method first compares the saturation degree and compares the adjacency degree
	 * if the saturation degree of two vertices are equal
	 * @return boolean returns true if the the combination of saturation and
	 * adjacency degree is greater than the current vertex by the above explained logic.
	 */
	public boolean compareVertex(Vertex comparisonVertex) {

		if(comparisonVertex.getSaturationDegree() > saturationDegree)
			return true;

		if(comparisonVertex.getSaturationDegree() == saturationDegree)
			if(comparisonVertex.getAdjacenceDegree() > adjacencyDegree)
				return true;
		return false;
	}

	/**
	 * method that increments the adjacency degree of the current vertex
	 */
	public void incrementAdjacencyDegree() {

		this.adjacencyDegree++;
	}

	/**
	 * method that increments the saturation degree of the current vertex
	 */
	public void incrementSaturationDegree() {

		this.saturationDegree++;
	}

	/**
	 * method that returns the adjacency degree
	 * @return int adjacency degree of the current vertex
	 */
	public int getAdjacenceDegree() {

		return adjacencyDegree;
	}

	/**
	 * method that returns the saturation degree
	 * @return int saturation degree of the current vertex
	 */
	public int getSaturationDegree() {

		return saturationDegree;
	}
}