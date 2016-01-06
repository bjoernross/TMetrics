/*******************************************************************************
 * This file is part of Tmetrics.
 *
 * Tmetrics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tmetrics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tmetrics. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.tmetrics.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Perform a linear regression using gradient descent.
 * 
 * Expects training data in the form of a featureMatrix and labels.
 * 
 * @author Erwin, Björn
 * 
 */
public class LinearRegression implements Serializable {

	private static final long serialVersionUID = 1L;

	// *** Input ***:

	// feature matrix := X
	private SparseMatrix featureMatrix;

	// labels := Y
	private List<Float> labels;

	// Others:
	// step length in regression := alpha (default = 0.01)
	private float alpha;

	// epsilon: when the difference in MSE's between iterations is smaller than
	// epsilon, gradient descent is considered to have converged
	private float epsilon = 0.00001f;

	// whether or not to regularize. results were dismal
	private boolean regularize = false;

	// regularization parameter
	private float lambda = 0.5f;

	// *** Output ***:

	// estimated parameters
	private List<Float> parameters;

	// errors, i. e. deviation of parameters from actual labels
	private List<Float> errors;

	// estimated sentiment for training tweets
	private List<Float> estimates;

	/**
	 * The constructor: receives the input to do a regression: the feature
	 * matrix(:= X) and the labels (:= y)
	 * 
	 * @param featureMatrix
	 * @param labels
	 */
	public LinearRegression(SparseMatrix featureMatrix, List<Float> labels) {
		this.featureMatrix = featureMatrix;
		this.labels = labels;
		this.alpha = 0.01f;
		this.epsilon = 0.00001f;

		this.parameters = new ArrayList<Float>();

		this.trainModelGradientDescent();

		// Useful statistics :)
		// System.out.println("Number of training tweets: " +
		// this.labels.size());
		// System.out.println("Training data estimates: " + this.estimates);
		// System.out.println("Training data labels: " + this.labels);
		// System.out.println("Training data errors: " + this.errors);
		// System.out.println("Training data MSE: " + getMSE());

	}

	/**
	 * The constructor: receives the input to do a regression: the feature
	 * matrix(:= X) and the labels (:= y). And it receives the step length of
	 * the gradient descent which is used in regression.
	 * 
	 * @param featureMatrix
	 * @param labels
	 * @param alpha
	 */
	public LinearRegression(SparseMatrix featureMatrix, List<Float> labels,
			float alpha) {
		this.alpha = alpha;

		this.featureMatrix = featureMatrix;
		this.labels = labels;

		this.parameters = new ArrayList<Float>();

		this.trainModelGradientDescent();
	}

	/**
	 * Get estimated parameters
	 * 
	 * @return estimated parameters
	 */
	public List<Float> getParameters() {
		return parameters;
	}

	/**
	 * Get errors for input data
	 * 
	 * @return errors
	 */
	public List<Float> getErrors() {
		return errors;
	}

	/**
	 * Get estimated values for input data
	 * 
	 * @return estimated values
	 */
	public List<Float> getEstimates() {
		return estimates;
	}

	/**
	 * Get mean squared error
	 * 
	 * @return Mean Squared Error
	 */
	private float getMSE() {
		return ListUtil.meanSquared(errors);
	}

	/**
	 * Perform the gradient descent algorithm to train the regression model
	 * using the feature featureMatrix and the human labels
	 * 
	 * After this method has been run, the parameters should be at their optimum
	 */
	private void trainModelGradientDescent() {

		// a) init parameters vector
		for (int i = 0; i < featureMatrix.ncol(); i++) {
			parameters.add(0.0f);
		}

		// b) gradient descent iterations
		float alpha = this.alpha;
		List<Float> gradient = null;
		float current_mse = 999;
		float last_mse;
		int i = 0;
		// for(int i = 0 ; i < 500; i++){
		do {
			gradient = calculateGradient();
			// math: parameters = parameters - alpha*Gradient
			gradient = ListUtil.multiply(gradient, alpha);
			if (this.regularize == true) {
				this.regularizeParameters();
			}
			parameters = ListUtil.subtract(parameters, gradient);
			// System.out.println("MSE: " + getMSE() + " in iteration " + i);
			last_mse = current_mse;
			current_mse = getMSE();
			i++;

		} while (last_mse - current_mse > this.epsilon);
		estimateSentiment();
		System.out.println("Regression model finbal MSE: " + getMSE()
				+ ", in iteration " + i);

	}

	/**
	 * Regularization
	 * 
	 * If this.regularize is set to true, this method is called during
	 * trainModelGradientDescent and alters the parameter values by lowering
	 * them with a penalty specified by the regularization parameter.
	 * 
	 * Results were dismal, so there is currently no way of accessing
	 * regularization from outside this class.
	 */
	private void regularizeParameters() {
		for (int i = 1; i < parameters.size(); i++) {
			float newParameter = parameters.get(i)
					* (1 - alpha * lambda / (float) featureMatrix.nrow());
			parameters.set(i, newParameter);
		}
	}

	/**
	 * Calculates the sentiment vector for the training data, based on the
	 * current parameters
	 */
	private void estimateSentiment() {
		estimates = featureMatrix.multiply(parameters);

		// compare estimates with actual human labels
		errors = ListUtil.subtract(estimates, this.labels);
	}

	/**
	 * Calculate the gradient according to the current estimates, which is used
	 * during gradient descent
	 * 
	 * @return Gradient
	 */
	private List<Float> calculateGradient() {
		// estimate sentiment using current parameters
		estimateSentiment();

		// compute gradient
		List<Float> gradient = ListUtil.multiply(
				featureMatrix.quiringProduct(errors),
				1 / ((float) featureMatrix.nrow()));

		// return it
		return gradient;
	}

}
