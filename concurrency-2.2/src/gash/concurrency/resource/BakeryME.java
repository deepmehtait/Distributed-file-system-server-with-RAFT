package gash.concurrency.resource;

import java.util.concurrent.Semaphore;

public class BakeryME {
	private Resource resource;
	private Process[] processes;

	private final Semaphore turn = new Semaphore(1, false); // not fair
	private int myTurn = -1;

	public BakeryME(int numProcessors) {
		resource = new Resource();
		init(numProcessors);
	}

	public synchronized boolean getLock(int id) {
		boolean r = turn.tryAcquire(1);
		if (r) {
			if (myTurn == -1 || myTurn == id) {
				System.out.println("--> " + id + " got lock (myTurn=" + myTurn
						+ ")");
				myTurn = id;
			} else {
				r = false;
				turn.release();
			}
		}
		return r;
	}

	public void releaseLock(int id, boolean cycleDone) {
		if (myTurn == id) {
			turn.release(1);
			if (cycleDone)
				myTurn = -1;
		}
	}

	public int getWhosTurnIsIt() {
		return myTurn;
	}

	private void init(int numProcessors) {
		System.out.println("** STARTING PROCESSES **");

		processes = new Process[numProcessors];
		for (int n = 0; n < numProcessors; n++) {
			processes[n] = new BakeryProcess((n + 1), resource, this);
			processes[n].start();
		}
	}

	public void status() {
		System.out.println("---------------------------------");
		for (Process p : processes)
			p.status();
	}

	public void stop() {
		if (processes == null)
			return;

		System.out.println("** STOPPING PROCESSES **");
		for (Process p : processes) {
			p.end();
		}

		// wait for threads to complete
		while (true) {
			boolean done = false;
			for (Process p : processes) {
				done |= p.isAlive();
			}
			status();

			if (!done)
				break;

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
