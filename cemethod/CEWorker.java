package cemethod;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * Thread used by CESolver for parallelizing evaluation of parameter vectors.
 */
class CEWorker extends Thread {
	/**
	 * Queue for receiving problems.
	 */
	private final LinkedBlockingQueue<Subproblem> problemQueue;
	/**
	 * Queue for returning results.
	 */
	private final LinkedBlockingQueue<Perf> resultQueue;

	/**
	 * @param input input queue.
	 * @param output output queue.
	 */
	CEWorker(LinkedBlockingQueue<Subproblem> input, LinkedBlockingQueue<Perf> output) {
		super();
		problemQueue = input;
		resultQueue = output;
	}

	/**
	 * Starts working on jobs from the input queue.
	 */
	@Override
	public void run() {
		while(!isInterrupted()) {
			doone();
		}
	}

	/**
	 * Processes a single problem instance.
	 */
	private void doone() {
		Subproblem prob;
		try {
			prob = problemQueue.take();
		} catch(InterruptedException e) {
			interrupt();
			return;
		}
		Perf perf = new Perf();
		perf.index = prob.index;
		perf.performance = prob.problem.fitness(prob.parameters);
		try {
			resultQueue.put(perf);
		} catch(InterruptedException e) {
			interrupt();
			throw new RuntimeException("Thread interrupted while working.");
		}
	}
}
