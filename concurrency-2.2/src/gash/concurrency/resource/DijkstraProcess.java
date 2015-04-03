package gash.concurrency.resource;

/**
 * Represents a process (thread) running in a mutual exclusion using the
 * Dijkstra Algorithm for ME
 * 
 * @author gash
 * 
 */
public class DijkstraProcess extends Process {
	public enum ProcessState {
		Rem, Trying, VerifyTurn, Critical, Exiting
	};

	private DijkstraME dme;
	private ProcessState pstate = ProcessState.Rem;

	public DijkstraProcess(int id, Resource resource, DijkstraME dme) {
		super(id, resource);
		this.dme = dme;
	}

	/**
	 * state engine
	 * 
	 * Note: Within a single process where the work is expected to be fairly
	 * quick, we could just perform all the work while we have the semaphore
	 * locked and only releasing it when the work is done.
	 */
	public void work() throws Exception {
		if (pstate == ProcessState.Rem && lock(ProcessState.Trying)) {
			System.out.println("\n---> P" + getProcessId() + "'s turn\n");
			pstate = ProcessState.Trying;
		} else if (pstate == ProcessState.Trying && lock(ProcessState.VerifyTurn)) {
			pstate = ProcessState.VerifyTurn;
		} else if (pstate == ProcessState.VerifyTurn && lock(ProcessState.Critical)) {
			pstate = ProcessState.Critical;
			doWork();
		} else if (pstate == ProcessState.Critical && lock(ProcessState.Exiting)) {
			pstate = ProcessState.Exiting;
		} else if (pstate == ProcessState.Exiting && lock(ProcessState.Rem))
			pstate = ProcessState.Rem;
	}

	private boolean lock(ProcessState nextState) {
		return dme.getLockAndSet(getProcessId(), nextState);
	}

	/**
	 * in critical section/state - we can do our work safely
	 */
	private void doWork() {
		resource.setValue(getProcessId());
		delay();
	}
}