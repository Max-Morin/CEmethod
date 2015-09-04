package cemethod.tetris;

/**
 *
 */
public class SymmetricMixedFeatures implements FeatureSet {
	/**
	 * This feature set is build upon. 
	 */
	private final SymmetricBertsekasFeatures sbf;

	/**
	 * 
	 */
	public SymmetricMixedFeatures() {
		sbf = new SymmetricBertsekasFeatures();
	}

	/* (non-Javadoc)
	 * @see tetris.FeatureSet#dimension(tetris.Playfield)
	 */
	@Override
	public int dimension(Playfield pf) {
		return sbf.dimension(pf) + 3;
	}

	/* (non-Javadoc)
	 * @see tetris.FeatureSet#extract(tetris.Playfield, int[])
	 */
	@Override
	public void extract(Playfield pf, int[] output) {
		sbf.extract(pf, output);
		output[pf.width + 2] = pf.coltrans();
		output[pf.width + 3] = pf.rowtrans();
		output[pf.width + 4] = pf.wellsum();
	}
}
