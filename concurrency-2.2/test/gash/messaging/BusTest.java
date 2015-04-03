package gash.messaging;

import gash.messaging.transports.Bus;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BusTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testBroadcast() throws Exception {
		Bus bus = new Bus(10);
		bus.broadcastMessage(3, "hello everyone");
		Thread.sleep(10000);

		bus.showReport();
	}

	@Test
	public void testChattyPrivateMessage() throws Exception {
		// this is like sending a private message with everyone allowed to read
		// the content - this would allow passive data collectors to gather
		// statistics, monitor traffic, etc
		Bus bus = new Bus(10);
		bus.privateMessageOverBroadcast(3, 7, "hello seven");
		Thread.sleep(100);
		bus.privateMessageOverBroadcast(7, 3, "hello three");
		Thread.sleep(100);
		bus.privateMessageOverBroadcast(1, 2, "what's up?");

		Thread.sleep(10000);

		bus.showReport();
	}

	@Test
	public void testPrivateMessage() throws Exception {
		Bus bus = new Bus(10);
		bus.privateMessage(3, 7, "hello seven");
		Thread.sleep(100);
		bus.privateMessage(7, 3, "hello three");
		Thread.sleep(100);
		bus.privateMessage(1, 2, "what's up?");
		Thread.sleep(10000);

		bus.showReport();
	}
}
