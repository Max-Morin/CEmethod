package cemethod.tetris;

/**
 * A playfield is a representation of the state of the playfield of a Tetris game.
 * It is a grid which pieces may be dropped into.
 */
public class Playfield extends Grid {
	/**
	 * True if the game is lost.
	 */
	private boolean terminated;

	/**
	 * @param width width of new playfield.
	 * @param height height of new playfeld.
	 */
	public Playfield(int width, int height) {
		super(width, height);
		if(height > 30) { throw new IllegalArgumentException("Playfield does not support such big playfields."); }
		terminated = false;
		if(DEBUG) {
			checkInvariants();
		}
	}

	/**
	 * @param pf playfield to copy into this playfield.
	 */
	public void setTo(Playfield pf) {
		super.setTo(pf);
		terminated = pf.terminated;
		if(DEBUG) {
			checkInvariants();
		}
	}

	/**
	 * @return True if game has ended.
	 */
	public boolean isTerminal() {
		return terminated;
	}

	/**
	 * Places p into the playfield, removing rows as needed.
	 * @param p The piece to be dropped.
	 * @param col The leftmost column of where the piece is to be dropped.
	 * @return The number of rows cleared.
	 */
	public int place(OrientedPiece p, int col) {
		int maxHeight = placeWithoutClearing(p, col);
		int r = removeFull();
		if(maxHeight - r > height) {
			terminated = true;
		}
		if(DEBUG) {
			checkInvariants();
		}
		return r;
	}

	/**
	 * Places p with its leftmost column at col.
	 */
	protected int placeWithoutClearing(OrientedPiece p, int col) {
		if(col + p.width > width || col < 0) { throw new IllegalArgumentException("Piece placed outside of playfield!"); }
		int placementHeight = 0;
		for(int w = 0; w < p.width; w++) {
			placementHeight = max(placementHeight, heightOf[col + w] - p.heightBelow[w]);
		}
		for(int w = 0; w < p.width; w++) {
			heightOf[col + w] = placementHeight + p.heightOf[w];
			columns[col + w] |= p.columns[w] << placementHeight;
		}
		nFull += p.nFull;
		return placementHeight + p.height;
	}

	/**
	 * Removes all full rows.
	 * @return The number of rows removed.
	 */
	protected int removeFull() {
		int ans = 0;
		int fullRows = ~0;
		for(int c : columns) {
			fullRows &= c;
		}
		int index = 0;
		while(fullRows > 0) {
			if((fullRows & 1) == 1) {
				removeRow(index);
				ans++;
			} else {
				index++;
			}
			fullRows >>= 1;
		}
		if(DEBUG) {
			checkInvariants();
		}
		return ans;
	}

	private void removeRow(int i) {
		if(DEBUG) {
			for(int c = 0; c < width; c++) {
				if(!isSquareFull(i, c)) { throw new RuntimeException("Faulty call to removeRow()."); }
			}
		}
		int lowermask = (1 << i) - 1;
		int highermask = ~((1 << i + 1) - 1);
		for(int c = 0; c < width; c++) {
			columns[c] = (columns[c] & highermask) >> 1 | columns[c] & lowermask;
			heightOf[c] = calculateHeight(c);
		}
		nFull -= width;
		if(DEBUG) {
			checkInvariants();
		}
	}

	// None of the methods below mutate the playfield.

	private static int min(int a, int b) {
		return a < b ? a : b;
	}

	int wellsum() {
		int ans = 0;
		int m = heightOf[1] - heightOf[0];
		ans += m > 1 ? m : 0;
		for(int c = 2; c < width; c++) {
			m = min(heightOf[c - 2], heightOf[c]) - heightOf[c - 1];
			ans += m > 1 ? m : 0;
		}
		m = heightOf[width - 1] - heightOf[width - 2];
		ans += m > 1 ? m : 0;
		return ans;
	}

	int coltrans() {
		int ans = 0;
		for(int x : columns) {
			// x & ~x << 1 is 1 exactly where a 1 changes to a 0.
			// ~x & x << 1 is 1 exactly where a 0 changes to a 1.
			ans += Integer.bitCount(x & ~x << 1 | ~x & x << 1);
		}
		return ans;
	}

	int rowtrans() {
		int ans = 0;
		for(int c = 1; c < width; c++) {
			ans += Integer.bitCount(columns[c] ^ columns[c - 1]);
		}
		return ans;
	}

	int holeSums() {
		int ans = 0;
		for(int c : columns) {
			int nHoles = 0;
			while(c > 0) {
				if((c & 1) == 1) {
					ans += nHoles;
				} else {
					nHoles++;
				}
				c >>= 1;
			}
		}
		return ans;
	}

	@Override
	public void checkInvariants() {
		if(terminated) { return; }
		super.checkInvariants();
	}
}
