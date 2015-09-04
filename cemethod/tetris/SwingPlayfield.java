package cemethod.tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A Playfield that graphically shows the progress of the current game.
 */
public class SwingPlayfield extends Playfield {
	/**
	 * The number of lines clear since construction.
	 */
	private int lines;

	/**
	 * The JPanel we draw to.
	 */
	private TetrisDrawable td;

	/**
	 * milliseconds to spend doing each move.
	 */
	private final int millispermove;

	/**
	 * A tetris playfield Swing component.
	 */
	class TetrisDrawable extends JPanel {
		/**
		 * Displayed in top left corner.
		 */
		public int counter;
		/**
		 * The colors that should be used for drawing.
		 */
		public final Color[][] squareColors;
		/**
		 * The side length of a square in pixels.
		 */
		private static final int SQUARESIZE = 30;

		/**
		 * @param w The width.
		 * @param h The height.
		 */
		public TetrisDrawable(int w, int h) {
			super();
			squareColors = new Color[w][h];
			Dimension d = new Dimension(w * SQUARESIZE, h * SQUARESIZE);
			setMinimumSize(d);
			setMaximumSize(d);
			setPreferredSize(d);
			setSize(d);
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		public void paintComponent(Graphics g) {
			for(int x = 0; x < squareColors.length; x++) {
				for(int y = 0; y < squareColors[x].length; y++) {
					g.setColor(squareColors[x][y]);
					g.fillRect(SQUARESIZE * x, SQUARESIZE * y, SQUARESIZE, SQUARESIZE);
				}
			}
			g.setColor(Color.BLACK);
			g.drawString(counter + " ", 15, 15);
		}
	}

	/**
	 * @param width width of this playfield.
	 * @param height height of this playfield.
	 * @param millispermove milliseconds to spend on every move.
	 */
	public SwingPlayfield(int width, int height, int millispermove) {
		super(width, height);
		this.millispermove = millispermove;
		lines = 0;
		initializeSwing();
	}

	private void initializeSwing() {
		JFrame f = new JFrame("Tetris");
		td = new TetrisDrawable(width, height);
		f.add(td);
		f.pack();
		f.setVisible(true);
		update();
	}

	/* (non-Javadoc)
	 * @see tetris.Playfield#place(tetris.OrientedPiece, int)
	 */
	@Override
	public int place(OrientedPiece op, int col) {
		placeWithoutClearing(op, col);
		update();
		try {
			Thread.sleep(millispermove / 2);
		} catch(InterruptedException e) {
			// Do nothing.
		}
		int ret = removeFull();
		lines += ret;
		update();
		update();
		try {
			Thread.sleep(millispermove / 2);
		} catch(InterruptedException e) {
			// Do nothing.
		}
		return ret;
	}

	private void update() {
		for(int r = 0; r < height; r++) {
			for(int c = 0; c < width; c++) {
				if(isSquareFull(height - r - 1, c)) {
					if(td.squareColors[c][r] == Color.BLUE || td.squareColors[c][r] == Color.PINK) {
						td.squareColors[c][r] = Color.BLUE;
					} else {
						td.squareColors[c][r] = Color.PINK;
					}
				} else {
					td.squareColors[c][r] = Color.WHITE;
				}
			}
		}
		td.counter = lines;
		td.repaint();
	}
}
