package gash.concurrency.resource;

import gash.concurrency.resource.DijkstraME;

import org.junit.Test;

public class DijkstraMETest {

	@Test
	public void testRun() throws Exception {
		DijkstraME dijkstra = new DijkstraME(5);
		dijkstra.status();

		// wait for startup
		int max = 20;
		for (int n = 0; n < max; n++) {
			dijkstra.status();
			Thread.sleep(1000);
		}

		dijkstra.stop();

		System.out.println("\ntest done");
	}
}
