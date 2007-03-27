package enginuity.NewGUI.tools;

import Jama.Matrix;

public class FitData {

	static double[] x_values;
	static double[] y_values;
	
	static double a; // Min x value

	static double b; // Max x value

	static int n = 100; // Iterations

	static int m = 0; // Number of x values

	static double[][] A_array;

	static double[][] B_array;

	static double[] results;

	
	/**
	 * Do a full smooth on passed double data
	 * @param intialData
	 * @return
	 */
	public static Double[][] getFullSmooth(Double[][] intialData){
		int width = intialData.length;
		int height = intialData[0].length;
		
		Double[][] returnData = new Double[width][height];
		
		double[] tempXValues = new double[width];
		for(int i = 0; i < width; i++){
			tempXValues[i] = i;
		}
		
		double[] tempYValues = new double[width];
		
		// Row pass
		for(int j = 0; j < height; j++){
			for(int i = 0; i < width; i++){
				tempYValues[i] = intialData[i][j];
			}
			init(tempXValues, tempYValues);
			for(int i = 0; i < width; i++){
				returnData[i][j] = getSmoothYValue(i);
			}
		}
		
		// Column Pass
		tempXValues = new double[height];
		for(int i = 0; i < height; i++){
			tempXValues[i] = i;
		}
		
		tempYValues = new double[height];
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				tempYValues[j] = returnData[i][j];
			}
			init(tempXValues, tempYValues);
			for(int j = 0; j < height; j++){
				returnData[i][j] = getSmoothYValue(j);
			}
		}
		
		return returnData;
	}
	
	
	/**
	 * Initialize smoother with passed data
	 * 
	 * @param xVals
	 * @param yVals
	 */
	public static void init(double[] xVals, double[] yVals) {
		x_values = xVals;
		y_values = yVals;

		a = getMinValue(x_values);
		b = getMaxValue(x_values);

		m = x_values.length - 1;

		// Guess at a valid value of n
		n = (int) (m / 4);
		if (n == 0) {
			n = 1;
		}

		A_array = new double[n + 1][n + 1];
		B_array = new double[n + 1][1];

		// Build (n+1)x(n+1) array of coefficients
		for (int i = 0; i <= n; i++) {

			for (int j = 0; j <= n; j++) {

				double kValue = (evalChebyshev(getZSubK(0), i))
						* (evalChebyshev(getZSubK(0), j));
				for (int k = 1; k <= m; k++) {
					kValue += (evalChebyshev(getZSubK(k), i))
							* (evalChebyshev(getZSubK(k), j));
				}
				A_array[j][i] = kValue;
			}
		}
		// Build nx1 array of values
		for (int i = 0; i <= n; i++) {
			double kValue = y_values[0] * (evalChebyshev(getZSubK(0), i));

			for (int k = 1; k <= m; k++) {
				kValue += y_values[k] * (evalChebyshev(getZSubK(k), i));
			}
			B_array[i][0] = kValue;
		}
		// Solve for coeffs
		Matrix alpha_matrix = new Matrix(A_array);
		Matrix beta_matrix = new Matrix(B_array);
		Matrix resultMatrix = alpha_matrix.solve(beta_matrix);

		// Get coeff results
		results = resultMatrix.getColumnPackedCopy();
		
	}

	/**
	 * Get smoothed y value for passed x value
	 * 
	 * @param xValue
	 * @return
	 */
	public static double getSmoothYValue(double xValue) {
		double interimValue = getScaledXValue(xValue);

		double yValue = evalChebyshev(interimValue, 0) * results[0];
		for (int j = 1; j <= n; j++) {
			yValue += evalChebyshev(interimValue, j) * results[j];
		}
		
		return yValue;
	}

	/**
	 * Gets scaled x value in range [-1, 1]
	 * 
	 * @param value
	 * @return
	 */
	private static double getScaledXValue(double value) {
		double returnValue = (2 * value - a - b) / (b - a);
		return returnValue;
	}

	/**
	 * Evaluates the passed value for the indexed value
	 * 
	 * Recursive method shown for fun.
	 * 
	 * @param value
	 * @param index
	 * @return
	 */
	private static double evalChebyshev(double value, int index) {
		double returnValue = Math.cos(index * Math.acos(value));
		return returnValue;
	}

	/**
	 * Return the Z sub K value for the given max and min x values
	 * 
	 * @param index
	 * @return
	 */
	private static double getZSubK(int index) {
		double returnValue = (2 * x_values[index] - a - b) / (b - a);
		return returnValue;
	}

	/**
	 * Get max value in passed array
	 * 
	 * @param valueArray
	 * @return
	 */
	private static double getMaxValue(double[] valueArray) {
		double returnValue = valueArray[0];

		for (int i = 1; i < valueArray.length; i++) {
			double temp = valueArray[i];
			if (temp > returnValue) {
				returnValue = temp;
			}
		}

		return returnValue;
	}

	/**
	 * Get min value in passed array
	 * 
	 * @param valueArray
	 * @return
	 */
	private static double getMinValue(double[] valueArray) {
		double returnValue = valueArray[0];

		for (int i = 1; i < valueArray.length; i++) {
			double temp = valueArray[i];
			if (temp < returnValue) {
				returnValue = temp;
			}
		}

		return returnValue;
	}
}
