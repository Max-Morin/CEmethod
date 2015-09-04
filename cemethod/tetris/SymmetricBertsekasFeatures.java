package cemethod.tetris;

/**
 * BertsekasFeatures, made symmetric.
 */
public class SymmetricBertsekasFeatures implements FeatureSet {

	/* (non-Javadoc)
	 * @see tetris.FeatureSet#dimension(tetris.Playfield)
	 */
	@Override
	public int dimension(Playfield pf) {
		return pf.width + 2;
	}

	/* (non-Javadoc)
	 * @see tetris.FeatureSet#extract(tetris.Playfield, int[])
	 */
	@Override
	public void extract(Playfield pf, int[] output) {
		int maxh = 0;
		int h;
		int h2;
		output[pf.width] = -pf.nFull;
		for(int w = 0; w < pf.width / 2; w++) {
			h = pf.heightOf[w];
			h2 = pf.heightOf[pf.width - w - 1];
			output[w] = h + h2;
			maxh = maxh > h ? maxh : h;
			maxh = maxh > h2 ? maxh : h2;
			output[pf.width] += h + h2;
		}
		for(int w = 0; w < pf.width / 2 - 1; w++) {
			output[pf.width / 2 + w] =
				abs(pf.heightOf[w + 1] - pf.heightOf[w]) +
					abs(pf.heightOf[pf.width - w - 1] - pf.heightOf[pf.width - w - 2]);
		}
		output[pf.width - 1] = abs(pf.heightOf[pf.width / 2] - pf.heightOf[pf.width / 2 - 1]);
		output[pf.width + 1] = maxh;
	}

	private static int abs(int x) {
		return x > 0 ? x : -x;
	}

}
