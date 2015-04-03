package gash.concurrency.resource;

import gash.concurrency.resource.DijkstraProcess.ProcessState;

public class BakeryProcess extends Process {
	private BakeryME bme;
	private ProcessState pstate = ProcessState.Rem;

	public BakeryProcess(int id, Resource resource, BakeryME bme) {
		super(id, resource);
		this.bme = bme;
	}

	@Override
	public void work() throws Exception {
		// TODO Auto-generated method stub

	}

}
