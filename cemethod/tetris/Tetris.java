package cemethod.tetris;

import java.util.Random;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * A specification of the Tetris problem. Capable of estimating the fitness
 * of AI parameters and simulating games.
 */
public class Tetris implements cemethod.Function {
	/**
	 * height of tetris playfield.
	 */
	private final int h;
	/**
	 * width of tetris playfield.
	 */
	private final int w;
	/**
	 * Source of randomness for seed to RNG when running a trial.
	 */
	private final Random r;
	/**
	 * Number of games per evaluation.
	 */
	private final int trials;
	/**
	 * The FeatureSet to use when evaluating states.
	 */
	private final FeatureSet fs;

	/**
	 * @param w width of tetris playfield.
	 * @param h height of tetris playfield.
	 * @param r source of randomness for piece generation.
	 * @param trials number of trials to average over for each evaluation. 
	 * @param fs the feature set to use when evaluating.
	 */
	public Tetris(int w, int h, Random r, int trials, FeatureSet fs) {
		this.r = r;
		this.w = w;
		this.h = h;
		this.trials = trials;
		this.fs = fs;
	}

	/* (non-Javadoc)
	 * @see cemethod.CEProblem#dimension()
	 */
	@Override
	public int dimension() {
		return fs.dimension(new Playfield(w, h));
	}

	/* (non-Javadoc)
	 * @see cemethod.CEProblem#fitness(double[])
	 */
	@Override
	public double fitness(double[] v) {
		double perf = 0;
		for(int i = 0; i < trials; i++) {
			perf += runTrial(v, false);
		}
		perf /= trials;
		return perf;
	}

	private static Piece getRandomPiece(RandomGenerator rng) {
		int n = rng.nextInt(Piece.PIECES.length);
		return Piece.PIECES[n];
	}

	/**
	 * Simulates a single game of tetris.
	 * @param param The AI weights to use.
	 * @param display If true, the game is displayed using Swing.
	 * @return The number of lines cleared.
	 */
	public int runTrial(double[] param, boolean display) {
		Playfield b;
		if(display) {
			b = new SwingPlayfield(w, h, 800);
		} else {
			b = new Playfield(w, h);
		}

		// Scratch memory:
		int[] mem = new int[dimension()];
		Playfield tmp = new Playfield(w, h);
		RandomGenerator rng = new MersenneTwister(r.nextLong());

		int lines = 0;
		// This loop does not allocate anything except for the iterator.
		while(!b.isTerminal()) {
			Piece current = getRandomPiece(rng);
			OrientedPiece bestPiece = null;
			int bestCol = 0;
			double bestVal = 0;
			// Try all possible orientations and columns for the piece:
			for(OrientedPiece op : current) {
				for(int c = 0; c + op.width <= b.width; c++) {
					tmp.setTo(b);
					tmp.place(op, c);
					double val = eval(tmp, param, mem);
					if(val > bestVal || bestPiece == null) {
						bestVal = val;
						bestPiece = op;
						bestCol = c;
					}
				}
			}
			lines += b.place(bestPiece, bestCol);
		}
		return lines;
	}

	private double eval(Playfield b, double[] par, int[] mem) {
		if(b.isTerminal()) { return -1.0 / 0.0; }
		double ans = 0;
		fs.extract(b, mem);
		for(int c = 0; c < par.length; c++) {
			ans += par[c] * mem[c];
		}
		return ans;
	}
}
