/**
 * A helper class that contains methods to
 * calculate the results of the algorithm
 */

public class AlgorithmResult {

	private long totalRunTime = 0;
	private int totalNoOfColors = 0;

	private double averageRunTime = 0.0;
	private float averageNoOfColors = 0;

	private int executionCount = 0;

    /**
     * Default constructor
     */
	public AlgorithmResult() {

	}

    /**
     * Parameterized constructor
     * @param  runTime the runtime of the algorithm
     * @param noOfColors number of colors used by the algorithm to color the graph
     */
	public AlgorithmResult(long runTime, int noOfColors) {

		totalRunTime = runTime;
		totalNoOfColors = noOfColors;
		executionCount = 1;

		averageNoOfColors = totalNoOfColors;
		averageRunTime = totalRunTime;
	}

    /**
     * returns the average runtime computed
     * @return long average runtime of the algorithm
     */
	public double getAverageRunTime() {

		return averageRunTime;
	}

    /**
     * returns the average number of colors computed
     * @return int average number of colors computed by the algorithm
     */
	public float getAverageColors() {

		return averageNoOfColors;
	}

    /**
     * returns the total runtime computed
     * @return long total runtime of the algorithm
     */
	public long getRunTime() {

		return totalRunTime;
	}

    /**
     * returns the total number of colors computed by the algorithm
     * @return int total number of colors
     */
	public int getNoOfColors() {

		return totalNoOfColors;
	}

    /**
     * a method that updates the results of the algorithm
     * @param runtTime runtime of the algorithm
     * @param numberOfColors number of colors computed by the algorithm
     */
	public void updateResults(long runtTime, int numberOfColors) {

		totalNoOfColors += numberOfColors;
		totalRunTime += runtTime;
		executionCount++;

		averageNoOfColors = totalNoOfColors / executionCount;
		averageRunTime = totalRunTime / executionCount;
	}
}
