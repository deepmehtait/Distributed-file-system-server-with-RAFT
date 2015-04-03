package gash.concurrency.resource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public abstract class Process extends Thread {
	public enum State {
		Idle, Starting, Running, Ending, Done
	};

	private int id;
	private boolean runFlag = true;
	protected State state = State.Idle;
	protected Resource resource;
	protected DateFormat df = new SimpleDateFormat("HH:mm:ss");
	Random rand = new Random(System.currentTimeMillis());

	public Process(int id, Resource resource) {
		this.id = id;
		this.resource = resource;
	}

	public int getProcessId() {
		return id;
	}

	public abstract void work() throws Exception;

	public State getProcessState() {
		return state;
	}

	public void end() {
		runFlag = false;
	}

	public void status() {
		System.out.println("P" + id + " (" + df.format(new Date()) + ") "
				+ state + ", value = " + resource.getValue());
	}

	public void run() {
		init();
		while (runFlag) {
			try {
				state = State.Running;
				work();
				delay();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cleanup();
	}

	private void init() {
		state = State.Starting;

		// simulate startup latency
		delay();
	}

	private void cleanup() {
		state = State.Ending;

		// simulate cleanup latency
		delay();

		state = State.Done;
	}

	protected void delay() {
		try {
			long t = 500 + rand.nextInt(2000);
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}