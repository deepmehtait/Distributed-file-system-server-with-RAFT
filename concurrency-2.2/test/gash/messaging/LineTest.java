package gash.messaging;

import gash.messaging.transports.Line;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LineTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testLine() throws Exception {
		Line l = new Line(20, false);
		l.sendMessage(3, 9, "hello world");
		Thread.sleep(10000);
	}
	
	@Test
	public void testLine2() throws Exception {
		Line l = new Line(20, false);
		l.sendMessage(9, 3, "hello world");
		Thread.sleep(10000);
	}
	
	@Test
	public void testUnknownNode() throws Exception {
		Line l = new Line(20, false);
		l.sendMessage(3, 30, "hello world");
		Thread.sleep(10000);
	}
}
