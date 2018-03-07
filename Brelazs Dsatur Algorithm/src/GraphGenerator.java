import java.util.Random;

/**
 * A helper class that is used to generate undirected graphs
 * This class generates three types of graphs
 * 1. random graph
 * 2. Cycle graph
 * 3. BinaryTree graph
 */
public class GraphGenerator {

    /**
     * method that generates a random graph given the number of vertices and graph density.
     * @param verticesCount desired number of vertices in the graph
     * @param graphDensity desired density of the graph
     */
	public static UndirectedGraph randomGraph(int verticesCount, double graphDensity) {

		if (graphDensity < 0.0 || graphDensity > 1.0)
			throw new IllegalArgumentException("Graph Density must be between 0 and 1");

		UndirectedGraph graph = new UndirectedGraph(verticesCount);

		Random random = new Random();
		for (int i = 0; i < verticesCount; i++) {

			for (int j = i + 1; j < verticesCount; j++) {

				if (random.nextDouble() < graphDensity) {

					graph.addEdge(i, j);
				}
			}
		}
		return graph;
	}

    /**
     * method that generates a cycle graph given the number of vertices.
     * @param verticesCount desired number of vertices in the graph
     */
	public static UndirectedGraph cycle(int verticesCount) {

		int[] vertices = new int[verticesCount];
		UndirectedGraph graph = new UndirectedGraph(verticesCount);

		for (int i = 0; i < verticesCount; i++)
			vertices[i] = i;

		for (int i = 0; i < verticesCount - 1; i++) {

			for(int j = i + 1; j < verticesCount; j++)
				graph.addEdge(vertices[i], vertices[j]);
		}

		return graph;
	}

    /**
     * method that generates a binary tree graph given the number of vertices.
     * @param verticesCount desired number of vertices in the graph
     */
	public static UndirectedGraph binaryTree(int verticesCount) {

		UndirectedGraph graph = new UndirectedGraph(verticesCount);

		int[] vertices = new int[verticesCount];
		for (int i = 0; i < verticesCount; i++)
			vertices[i] = i;

		for (int i = 1; i < verticesCount; i++) {

			graph.addEdge(vertices[i], vertices[(i - 1) / 2]);
		}
		return graph;
	}
}
