import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Main Class - Program execution starts here
 * This class is executed to obtain the results of the DSaturPriority Queue Algorithm
 */
public class TCSS543 {
	
	public static void main(String arg[]) {

		AlgorithmResult tempResult = new AlgorithmResult();
		
		System.out.println("\tBT\tG1\tG2\tG3\tG4\tCG\t\t\t\tBT\tG1\tG2\tG3\tG4\tCG");
		
		// Find the result of the algorithm for vertex count starting from 10,20,30 - 200.
		for(int vCount = 10; vCount <= 200; vCount += 10) {

			AlgorithmResult result10 = new AlgorithmResult();
			AlgorithmResult result30 = new AlgorithmResult();
			AlgorithmResult result50 = new AlgorithmResult();
			AlgorithmResult result65 = new AlgorithmResult();
			AlgorithmResult result75 = new AlgorithmResult();
			AlgorithmResult result100 = new AlgorithmResult();

			/* we iterate through the algorithm 100 times by varying the
			   density and the type of graphs and take the average over 100 iterations to
			   produce the final result.
			 */
			for(int i = 0; i < 100; i++) {

				UndirectedGraph graph = GraphGenerator.binaryTree(vCount);
				tempResult = new DSaturPriorityQueue().colorTheGraph(graph);
				result10.updateResults(tempResult.getRunTime(), tempResult.getNoOfColors());

				double density = getRandomDensity(0.26, 0.34);
				graph = GraphGenerator.randomGraph(vCount, density /*0.30*/);
				tempResult = new DSaturPriorityQueue().colorTheGraph(graph);
				result30.updateResults(tempResult.getRunTime(), tempResult.getNoOfColors());

				density = getRandomDensity(0.44, 0.59);
				graph = GraphGenerator.randomGraph(vCount, density /*0.50*/);
				tempResult = new DSaturPriorityQueue().colorTheGraph(graph);
				result50.updateResults(tempResult.getRunTime(), tempResult.getNoOfColors());

				density = getRandomDensity(0.61, 0.72);
				graph = GraphGenerator.randomGraph(vCount, density /*0.65*/);
				tempResult = new DSaturPriorityQueue().colorTheGraph(graph);
				result65.updateResults(tempResult.getRunTime(), tempResult.getNoOfColors());

				density = getRandomDensity(0.73, 0.82);
				graph = GraphGenerator.randomGraph(vCount, density /*0.75*/);
				tempResult = new DSaturPriorityQueue().colorTheGraph(graph);
				result75.updateResults(tempResult.getRunTime(), tempResult.getNoOfColors());

				graph = GraphGenerator.cycle(vCount);
				tempResult = new DSaturPriorityQueue().colorTheGraph(graph);
				result100.updateResults(tempResult.getRunTime(), tempResult.getNoOfColors());
			}

			// printing the results of the algorithm
			
			System.out.print(vCount + "\t");
			System.out.print(TimeUnit.MICROSECONDS.convert((long)result10
					.getAverageRunTime(), TimeUnit.NANOSECONDS) + "\t");
			System.out.print(TimeUnit.MICROSECONDS.convert((long)result30
					.getAverageRunTime(), TimeUnit.NANOSECONDS) + "\t");
			System.out.print(TimeUnit.MICROSECONDS.convert((long)result50
					.getAverageRunTime(), TimeUnit.NANOSECONDS) + "\t");
			System.out.print(TimeUnit.MICROSECONDS.convert((long)result65
					.getAverageRunTime(), TimeUnit.NANOSECONDS) + "\t");
			System.out.print(TimeUnit.MICROSECONDS.convert((long)result75
					.getAverageRunTime(), TimeUnit.NANOSECONDS) + "\t");
			System.out.print(TimeUnit.MICROSECONDS.convert((long)result100
					.getAverageRunTime(), TimeUnit.NANOSECONDS) + "\t");
			
			
			System.out.print("\t\t");
			System.out.print(vCount + "\t");
			System.out.print(result10.getAverageColors() + "\t");
			System.out.print(result30.getAverageColors() + "\t");
			System.out.print(result50.getAverageColors() + "\t");
			System.out.print(result65.getAverageColors() + "\t");
			System.out.print(result75.getAverageColors() + "\t");
			System.out.print(result100.getAverageColors() + "\t");
			System.out.println("");
		}
	}
	
	public static double getRandomDensity(double minValue, double maxValue) {
		
		double randomNum = 0;
		
		Random random = new Random();
		randomNum = random.nextDouble() * ((maxValue - minValue) + 0.001) + minValue;
		randomNum = (double) Math.round(randomNum * 100) / 100;
		return randomNum;
	}
}


