package cemethod;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * This class shows some sample uses of this package.
 */
public class Main {
	/**
	 * Ackley's function. A common test function for optimization methods.
	 */
	public static class Ackley implements Function {
		private final int dim;

		/**
		 * @param dim The dimension of this problem.
		 */
		public Ackley(int dim) {
			this.dim = dim;
		}

		@Override
		public int dimension() {
			return dim;
		}

		@Override
		public double fitness(double[] v) {
			double s1 = 0;
			double s2 = 0;
			for(int i = 0; i < v.length; i++) {
				s1 += v[i]*v[i];
				s2 += Math.cos(2*Math.PI*v[i]);
			}
			return 20*Math.exp(-0.2*Math.sqrt(s1/dim))+
				Math.exp(s2/dim)-Math.E-20;
		}
	}

	private Main() {
	}

	/**
	 * @param args None.
	 */
	public static void main(String[] args) {
		RandomGenerator r = new MersenneTwister();
		ackleyDemo(r);
	}

	private static void ackleyDemo(RandomGenerator r) {
		int dimension = 100;
		Function ack = new Ackley(dimension);
		int threads = 8;
		int maxIterations = 1000;
		double minVariance = 0;
		double initialNoise = 9;
		double noiseStep = -0.01;
		int sampleSize = 1000;
		int elitesSize = 50;
		double[] initialGuess = new double[dimension];
		for(int i = 0; i < dimension; i++) {
			// We do not simply make out initial guess 0,
			// since that is the optimum (no cheating). 
			initialGuess[i] = 10 - 20*r.nextDouble();
		}
		double initialVariance = 30;

		// Solver setup.
		CESolver solver = new CESolver(threads, r);
		solver.setMaxIterations(maxIterations);
		solver.setMinVariance(minVariance);
		solver.setSamples(sampleSize);
		solver.setProblem(ack);
		solver.setElites(elitesSize);
		solver.setInitialNoise(initialNoise);
		solver.setNoiseStep(noiseStep);
		try {
			double[] v = solver.solve(new GeneralNormalDistribution(r, initialGuess, initialVariance));
			System.out.println("perf: " + ack.fitness(v));
		} catch(InterruptedException e) {
			System.exit(1);
		}
		solver.shutdown();
	}
}
