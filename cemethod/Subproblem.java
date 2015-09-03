package cemethod;

/**
 *
 * A single parameter problem and parameter vector to evaluate.
 */
class Subproblem {
	/**
	 * problem to use.
	 */
	Function problem;
	/**
	 * parameters to use.
	 */
	double[] parameters;
	/**
	 * index of parameters (in CESolver).
	 */
	int index;

	/**
	 * @param p problem to use.
	 * @param params parameter vector to evaluate.
	 * @param i index of this parameter vector (see CESolver).
	 */
	public Subproblem(Function p, double[] params, int i) {
		problem = p;
		parameters = params;
		index = i;
	}
}
