package cemethod.tetris;

/**
 * A set of features for tetris.
 */
public interface FeatureSet {
	/**
	 * @param pf a playfield of the size this featureset is intended to be used on.
	 * @return The number of features this set has.
	 */
	int dimension(Playfield pf);

	/**
	 * extracts the features from pf and puts into output.
	 * @param pf The playfield to consider.
	 * @param output output vector of length dimension().
	 */
	void extract(Playfield pf, int[] output);
}
