package cemethod.tetris;

import java.util.Random;

import org.apache.commons.math3.random.MersenneTwister;

import cemethod.CESolver;
import cemethod.Distribution;
import cemethod.NormalDistribution;

/**
 * Main runs a training session for Tetris. 
 */
public final class Main {
	private Main() {
	}

	/**
	 * @param args as given after first running the program.
	 * @throws InterruptedException if interrupted.
	 */
	public static void main(String[] args) throws InterruptedException {
		// To benchmark: give all RNGs an explicit seed
		// and change number of threads to 1.
		// This should make the execution deterministic.

		int width = 10;
		int trainingHeight = 12;
		int evaluationHeight = 20;
		int threads = 8;
		int maxIterations = 100;
		double minVariance = 0.5;
		double initialNoise = 8.0;
		double noiseStep = -0.1;
		int sampleSize = 100;
		int elitesSize = 10;
		FeatureSet featureSet = new SymmetricMixedFeatures();
		Distribution initialDistribution =
			new NormalDistribution(new MersenneTwister(), new double[featureSet.dimension(new Playfield(5, 10))], 10);
		Tetris training = new Tetris(width, trainingHeight, new Random(), 50, featureSet);
		Tetris evaluation = new Tetris(width, evaluationHeight, new Random(), 100, featureSet);
		int evaluationTrials = 100;

		// Solver setup.
		CESolver solver = new CESolver(threads, new MersenneTwister());
		solver.setMaxIterations(maxIterations);
		solver.setMinVariance(minVariance);
		solver.setSamples(sampleSize);
		solver.setElites(elitesSize);
		solver.setInitialNoise(initialNoise);
		solver.setNoiseStep(noiseStep);
		solver.setProblem(training);
		solver.setVerbosity(1);

		if(args.length > 0) {
			if(args.length != evaluation.dimension() + 1) {
				System.out.println("Unknown arguments.");
				return;
			}
			double[] par = new double[evaluation.dimension()];
			for(int i = 0; i < par.length; i++) {
				try {
					par[i] = Double.longBitsToDouble(Long.parseLong(args[i + 1]));
				} catch(NumberFormatException e) {
					System.out.println("Unknown arguments.");
					return;
				}
			}
			if(args[0].equals("test")) {
				solver.setProblem(evaluation);
				solver.evaluateParameters(par, evaluationTrials);
				return;
			} else if(args[0].equals("show")) {
				evaluation.runTrial(par, true);
				return;
			} else {
				System.out.println("Unknown arguments.");
				return;
			}
		}

		// Run solver.
		long startTime = System.nanoTime();
		double[] opt = solver.solve(initialDistribution);
		solver.shutdown();
		System.out.println("Trained in " + (System.nanoTime() - startTime) / 1000000 / 1000.0 + " seconds.");
		System.out.println("Perf on training problem: " +
			(int)new Tetris(width, trainingHeight, new Random(), 1000, featureSet).fitness(opt));
		System.out.println("To test the fitness of these parameters, run \n" +
			"java -cp \"./commons-math3-3.5.jar:.\" tetris.Main test " + parametersToString(opt));
		System.out.println("To see a sample game, use \"show\" instead of \"test\"");
	}

	private static String parametersToString(double[] p) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < p.length; i++) {
			sb.append(Double.doubleToRawLongBits(p[i]) + "");
			if(i + 1 < p.length) {
				sb.append(' ');
			}
		}
		return sb.toString();
	}
}
