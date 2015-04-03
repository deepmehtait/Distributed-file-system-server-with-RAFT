package gash.leaderelection;

import gash.leaderelection.Bully.BullyNode;
import gash.leaderelection.Bully.BullyWrapperNode;
import gash.messaging.StatNode;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BullyTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testStartup() throws Exception {
		Bully b = new Bully();

		StatNode stat = new StatNode(Integer.MAX_VALUE - 1);
		BullyWrapperNode bw = new BullyWrapperNode(stat);
		b.addNode(bw);
		if (!bw.isAlive())
			bw.start();

		for (int n = 0; n < 10; n++) {
			BullyNode node = new BullyNode(n);
			b.addNode(node);
			if (!node.isAlive())
				node.start();
		}

		// allow a couple of leaders to die so that we can see the leader
		// election process in action
		Thread.sleep(40000);

		stat.report();
	}
}
