package gash.concurrency.resource;

import gash.concurrency.resource.DijkstraProcess.ProcessState;

import java.util.concurrent.Semaphore;

/**
 * simulates Dijstrata's protocol
 * 
 * Notes:
 * <ol>
 * <li>Dijstrata algo requires a shared, semaphore register to coordinate turns.
 * This is really difficult to implement across processes
 * </ol>
 * 
 * @author gash
 * 
 */
public class DijkstraME {
	private Resource resource;
	private Process[] processes;

	private final Semaphore turn = new Semaphore(1, false); // not fair
	private ProcessState flag[];

	public DijkstraME(int numProcessors) {
		resource = new Resource();
		init(numProcessors);
	}

	public synchronized boolean getLockAndSet(int id, ProcessState desiredState) {
		boolean r = false;
		try {
			r = turn.tryAcquire(1);
			if (r) {
				int myturn = -1;
				for (int n = 0; n < flag.length; n++) {
					if (flag[n] != ProcessState.Rem) {
						myturn = n;
						break;
					}
				}

				if (myturn == -1 || myturn == id) {
					flag[id] = desiredState;
				} else {
					r = false;
				}
			}
		} finally {
			turn.release();
		}

		return r;
	}

	private void init(int numProcessors) {	
		flag = new ProcessState[numProcessors];
		for (int n = 0; n < numProcessors; n++)
			flag[n] = ProcessState.Rem;

		processes = new Process[numProcessors];
		for (int n = 0; n < numProcessors; n++) {
			processes[n] = new DijkstraProcess(n, resource, this);
			processes[n].start();
		}
		
		// debugging
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < flag.length; n++)
			sb.append(String.format("%-23s",""+n));
		System.out.println(sb);
		System.out.println("----------------------------------------------------------------------------------------------------------------------------------");
		
	}

	public void status() {
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < flag.length; n++)
			sb.append(String.format("%-23s",
					flag[n] + " (" + processes[n].getProcessState() + ")"));

		System.out.println(sb + "  resource = " + resource.getValue());
	}

	public void stop() {
		if (processes == null)
			return;

		System.out.println("\n** STOPPING PROCESSES **\n");
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
