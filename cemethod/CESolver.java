package cemethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * This class implements the noisy cross-entropy method for optimization, see e.g.
 * http://ie.technion.ac.il/CE/files/papers/Learning%20Tetris%20Using%20the%20Noisy%20Cross-Entropy%20Method.pdf .
 */
public final class CESolver {
	/**
	 * Queue to send problems to workers.
	 */
	private final LinkedBlockingQueue<Subproblem> problemQueue;
	/**
	 * Queue to get results from workers.
	 */
	private final LinkedBlockingQueue<Perf> resultQueue;
	/**
	 * Worker threads.
	 */
	private final List<CEWorker> workers;
	/**
	 * Sample size per iteration.
	 */
	private int samples;
	/**
	 * Samples used for next iteration.
	 */
	private int elites;
	/**
	 * Maximal number of iterations before stopping.
	 */
	private int maxIterations;
	/**
	 * Minimal variance before stopping.
	 */
	private double minVariance;
	/**
	 * Problem considered.
	 */
	private Function problem;
	/**
	 * RNG used.
	 */
	private final RandomGenerator r;
	/**
	 * Noise change per iteration.
	 */
	private double noiseStep;
	/**
	 * Noise added first iteration.
	 */
	private double initialNoise;

	/**
	 * @return the maxIterations
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * @param maxIterations the maxIterations to set
	 */
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * Note that all the setters should be called before using the solver.
	 * @param threads The number of threads to use when solving problems.
	 * @param r The RNG to use for generating samples.
	 */
	public CESolver(int threads, RandomGenerator r) {
		problemQueue = new LinkedBlockingQueue<Subproblem>();
		resultQueue = new LinkedBlockingQueue<Perf>();
		workers = new ArrayList<CEWorker>();
		for(int i = 0; i < threads; i++) {
			workers.add(new CEWorker(problemQueue, resultQueue));
			workers.get(i).start();
		}
		this.r = r;
	}

	/**
	 * @return the number of samples to generate each iteration.
	 */
	public int getSamples() {
		return samples;
	}

	/**
	 * @param samples the number of samples to generate each iteration.
	 */
	public void setSamples(int samples) {
		this.samples = samples;
	}

	/**
	 * @return the step by which the noice is reduced every iteration.
	 */
	public double getNoiseStep() {
		return noiseStep;
	}

	/**
	 * @param noiseStep the step by which the noice should be reduced every iteration.
	 */
	public void setNoiseStep(double noiseStep) {
		this.noiseStep = noiseStep;
	}

	/**
	 * @return the initial noise level.
	 */
	public double getInitialNoise() {
		return initialNoise;
	}

	/**
	 * @param initialNoise the initial noise level to use.
	 */
	public void setInitialNoise(double initialNoise) {
		this.initialNoise = initialNoise;
	}

	/**
	 * @return the number of elite samples per iteration.
	 */
	public int getElites() {
		return elites;
	}

	/**
	 * @param elites the number of elite samples per iteration.
	 */
	public void setElites(int elites) {
		this.elites = elites;
	}

	/**
	 * @return the variance used as stopping criterion
	 */
	public double getMinVariance() {
		return minVariance;
	}

	/**
	 * @param minVariance the variance to be used as stopping criterion.
	 */
	public void setMinVariance(double minVariance) {
		this.minVariance = minVariance;
	}

	/**
	 * @return the problem to solve.
	 */
	public Function getProblem() {
		return problem;
	}

	/**
	 * @param problem the problem to solve.
	 */
	public void setProblem(Function problem) {
		this.problem = problem;
	}

	/**
	 * Useful with threads = 1 for deterministic execution.
	 * @param n the seed for the RNG.
	 */
	public void seed(long n) {
		r.setSeed(n);
	}

	/**
	 * Shuts down all threads used by this solver.
	 */
	public void shutdown() {
		for(CEWorker w : workers) {
			w.interrupt();
		}
	}

	/**
	 * @param initial The distribution to start with.
	 * @return The vector giving the maximal found value.
	 * @throws InterruptedException In case it is interrupted while working.
	 */
	public double[] solve(Distribution initial) throws InterruptedException {
		double[] best = null;
		Distribution d = initial;
		final int save = 1; // Save the best vector found.
		List<Point> sampleList = new ArrayList<Point>();
		for(int i = 0; i < samples; i++) {
			sampleList.add(new Point(d.sample()));
		}

		for(int iter = 1; iter <= maxIterations && d.getVar() > minVariance; iter++) {
			for(int i = save; i < samples; i++) {
				sampleList.set(i, new Point(d.sample()));
			}
			for(int i = 0; i < samples; i++) {
				problemQueue.add(new Subproblem(problem, sampleList.get(i).vec, i));
			}
			for(int i = 0; i < samples; i++) {
				Perf perf = resultQueue.take();
				sampleList.get(perf.index).performance = perf.performance;
			}

			Collections.sort(sampleList);
			double noise = initialNoise + noiseStep * (iter-1);
			List<Point> eliteSamples = sampleList.subList(0, elites);
			d.fitTo(toDoubleArrayArray(eliteSamples), noise > 0 ? noise : 0);
			if(Double.isNaN(d.getVar())) {
				System.out.println("NaN Value found.");
				return best;
			}

			best = sampleList.get(0).vec;
		}
		return best;
	}

	private static double[][] toDoubleArrayArray(List<Point> list) {
		double[][] ret = new double[list.size()][];
		for(int i = 0; i < ret.length; i++) {
			ret[i] = list.get(i).vec;
		}
		return ret;
	}

	/**
	 * Evaluates the vector v several times, averaging the results.
	 * This is useful to test the fitness of a vector
	 * for a stochastic function.
	 * @param v the vector to evaluate.
	 * @param trials the number of trials to do. At least 2.
	 * @return the mean and standard deviation of the trials.
	 * @throws InterruptedException if interrupted.
	 */
	public EvaluationResult evaluateParameters(double[] v, int trials) throws InterruptedException {
		if(trials < 2) { throw new IllegalArgumentException(
			"Must run at least two trials to compute sample standard deviation."); }
		Perf perf;
		double m = 0;
		double s = 0;
		for(int i = 0; i < trials; i++) {
			problemQueue.add(new Subproblem(problem, v, i));
		}
		// see https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
		for(int i = 1; i <= trials; i++) {
			perf = resultQueue.take();
			double x = perf.performance;
			double delta = x - m;
			m = m + delta / i;
			s = s + delta * (x - m);
		}
		return new EvaluationResult(m, s / (trials - 1));
	}
}
