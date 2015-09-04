package cemethod.tetris;

/**
 * A small set of features.
 */
public class SmallFeatures implements FeatureSet {
	@Override
	public int dimension(Playfield pf) {
		return 8;
	}

	@Override
	public void extract(Playfield pf, int[] output) {
		output[0] = -pf.nFull;
		output[1] = 0;
		int maxh = 0;
		for(int h : pf.heightOf) {
			maxh = 0;
			maxh = h > maxh ? h : maxh;
			output[0] += h;
			output[1] += h;
		}
		output[2] = maxh;
		output[3] = pf.heightOf[0] + pf.heightOf[pf.width - 1];
		for(int i = 0; i < pf.width - 1; i++) {
			output[3] = abs(pf.heightOf[i] - pf.heightOf[i + 1]);
		}
		output[4] = pf.holeSums();
		output[5] = pf.coltrans();
		output[6] = pf.rowtrans();
		output[7] = pf.wellsum();
	}

	private static int abs(int x) {
		return x > 0 ? x : -x;
	}
}
