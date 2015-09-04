package cemethod.tetris;

/**
 * A piece for which an orientation has been chosen.
 */
public class OrientedPiece extends Grid {
	/**
	 * The height of the empty space under the grid in
	 * column index.
	 * Example:
	 * ##_
	 * #_#
	 * #__
	 *  has heights 0, 2, and 1 respectively.
	 */
	public final int[] heightBelow;

	/**
	 * @param a The piece.
	 * @param r The desired orientation of the piece.
	 */
	public OrientedPiece(boolean[][] a, int r) {
		super((r & 1) == 0 ? a.length : a[0].length, (r & 1) == 0 ? a[0].length : a.length);
		int rot = r % 4;
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < a[0].length; j++) {
				int x = transformx(i, j, width, rot);
				int y = transformy(i, j, height, rot);
				columns[x] |= a[i][j] ? 1 << y : 0;
			}
		}
		heightBelow = new int[width];
		for(int c = 0; c < width; c++) {
			heightBelow[c] = spaceBelow(c);
			heightOf[c] = calculateHeight(c);
		}
		nFull = calcFull();
		if(DEBUG) {
			checkInvariants();
		}
	}

	/**
	 * @param column
	 * @return The value that should be in heightBelow[column].
	 */
	private int spaceBelow(int column) {
		int c = columns[column];
		int ans = 1;
		while(c == c >> ans << ans) {
			ans++;
		}
		ans--;
		return ans;
	}

	/**
	 * @param x
	 * @param y
	 * @param w
	 * @param r
	 * @return The resulting x coordinate when rotating the coordinate
	 * (x, y) r times counter-clockwise, assuming the width of the grid is w.
	 */
	private static int transformx(int x, int y, int w, int r) {
		if(r == 0) { return x; }
		if(r == 1) { return w - y - 1; }
		if(r == 2) { return w - x - 1; }
		return y;
	}

	/**
	 * @param x
	 * @param y
	 * @param h
	 * @param r
	 * @return The resulting y coordinate when rotation the coordinate
	 * (x, y) r times counter-clockwise, assuming the height of the grid is h.
	 */
	private static int transformy(int x, int y, int h, int r) {
		if(r == 0) { return y; }
		if(r == 1) { return x; }
		if(r == 2) { return h - y - 1; }
		return h - x - 1;
	}
}
