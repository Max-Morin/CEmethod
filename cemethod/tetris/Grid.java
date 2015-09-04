package cemethod.tetris;

import java.util.Arrays;

/**
 * A Grid is a rectangle of squares, each which may be either full or empty.
 */
public class Grid {
	/**
	 * Representation of the columns of the playfield.
	 * Each entry is an integer, where bit i is set
	 * whenever the i:th square of the column, counted
	 * from the bottom, is full.  
	 */
	protected final int[] columns;
	/**
	 * The height of the columns, cached for performance
	 */
	protected final int[] heightOf;
	/**
	 * The height of the Playfield.
	 */
	public final int height;
	/**
	 * The width of the Playfield.
	 */
	public final int width;

	/**
	 * The number of full squares.
	 */
	protected int nFull;

	/**
	 * Whether to use run-time invariant detection. 
	 */
	protected static final boolean DEBUG = true;

	/**
	 * @param width width of the grid.
	 * @param height height of the grid.
	 */
	public Grid(int width, int height) {
		if(height > 30) { throw new IllegalArgumentException("Height too large for Grid."); }
		this.width = width;
		this.height = height;
		columns = new int[width];
		heightOf = new int[width];
		nFull = 0;
	}

	/**
	 * Mutates this to make it a copy of g. g must have the same width
	 * and height.
	 * @param g the grid to copy.
	 */
	public void setTo(Grid g) {
		for(int c = 0; c < width; c++) {
			columns[c] = g.columns[c];
			heightOf[c] = g.heightOf[c];
		}
		nFull = g.nFull;
		if(DEBUG) {
			checkInvariants();
		}
	}

	/**
	 * @param column
	 * @return The height of column number column.
	 */
	protected int calculateHeight(int column) {
		int c = columns[column];
		int ans = 0;
		while(c > 0) {
			ans += 1;
			c >>= 1;
		}
		return ans;
	}

	/**
	 * @param row the row to check.
	 * @param col the column to check.
	 * @return True if (row, col) is full.
	 */
	protected boolean isSquareFull(int row, int col) {
		return (columns[col] & 1 << row) != 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(height + "x" + width + " grid:\n");
		for(int i = height - 1; i >= 0; i--) {
			for(int column : columns) {
				s.append((column >> i & 1) == 1 ? 'X' : ' ');
			}
			s.append('\n');
		}
		return s.toString();
	}

	protected static int max(int a, int b) {
		return a > b ? a : b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(columns);
		result = prime * result + height;
		result = prime * result + width;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) { return true; }
		if(obj == null) { return false; }
		if(getClass() != obj.getClass()) { return false; }
		Grid other = (Grid)obj;
		if(!Arrays.equals(columns, other.columns)) { return false; }
		if(height != other.height) { return false; }
		if(width != other.width) { return false; }
		return true;
	}

	/**
	 * Only for debugging.
	 * @return Calculates the number of full squares of this grid.
	 * This should always be equal to nFull.
	 */
	protected int calcFull() {
		int full = 0;
		for(int w = 0; w < height + 4; w++) {
			for(int c = 0; c < width; c++) {
				if(isSquareFull(w, c)) {
					full++;
				}
			}
		}
		return full;
	}

	/**
	 * @return The number of empty squares with a full square somewhere above it.
	 */
	private int numberOfHoles() {
		int ans = 0;
		for(int c : columns) {
			while(c > 0) {
				if((c & 1) == 0) {
					ans++;
				}
				c >>= 1;
			}
		}
		return ans;
	}

	/**
	 * Check if this Grid is in a legal state.
	 * Grid has many calculations cached for performance,
	 * so there is a risk a cached value being wrong.
	 * This function will throw a RuntimeException in case of bugs.
	 */
	public void checkInvariants() {
		if(calcFull() != nFull) { throw new RuntimeException(); }
		int heightSum = 0;
		for(int w = 0; w < width; w++) {
			if(heightOf[w] != calculateHeight(w)) { throw new RuntimeException(); }
			heightSum += heightOf[w];
		}
		if(heightSum - nFull != numberOfHoles()) { throw new RuntimeException(); }
	}
}
