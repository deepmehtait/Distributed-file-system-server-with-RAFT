package gash.leaderelection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FloodMaxTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testStartup() throws Exception {
		FloodMax fm = new FloodMax(4);

		// allow a couple of leaders to die so that we can see the leader
		// election process in action
		Thread.sleep(40000);
	}
}
