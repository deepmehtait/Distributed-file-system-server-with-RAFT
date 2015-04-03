package poke.server.queue;

import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DiscreteQueueTest {
	NavigableMap<Float, String> queue;

	@Before
	public void setUp() throws Exception {
		queue = new TreeMap<Float, String>();
		queue.put(1.1f, "one point one");
		queue.put(1.0f, "one point zero");
		queue.put(3.0f, "three point zero");
		queue.put(2.5f, "two point five");
		queue.put(2.1f, "two point one");
	}

	@After
	public void tearDown() throws Exception {
		queue = null;
	}

	/**
	 * test the use of java 1.6's navigable map for the discrete simulation
	 * queue
	 */
	@Test
	public void testQueueTimeConcept() {

		SortedMap<Float, String> list = queue.headMap(2.1f, true);
		for (String v : list.values())
			System.out.println("found: " + v);

		list.clear();

		for (String v : queue.values())
			System.out.println("remainder: " + v);

	}

}
