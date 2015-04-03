package gash.messaging;

import gash.messaging.transports.Hub;
import gash.messaging.transports.Hub.SpokeNode;

import org.junit.Test;

public class HubTest {

	@Test
	public void testBasicHub() throws Exception {
		Hub hub = new Hub();
		SpokeNode a = new SpokeNode(1, hub);
		a.setName("A");

		SpokeNode b = new SpokeNode(2, hub);
		b.setName("B");

		SpokeNode c = new SpokeNode(3, hub);
		c.setName("C");

		SpokeNode d = new SpokeNode(4, hub);
		d.setName("D");

		SpokeNode e = new SpokeNode(5, hub);
		e.setName("E");

		SpokeNode f = new SpokeNode(6, hub);
		f.setName("F");

		// add nodes to our network
		hub.addNode(a);
		hub.addNode(b);
		hub.addNode(c);
		hub.addNode(d);
		hub.addNode(e);
		hub.addNode(f);
		
		// messages from outside of the spokes
		hub.privateMessage(3, 1, "There can only be one");
		Thread.sleep(100);
		hub.privateMessage(4, 3, "hello three");
		Thread.sleep(200);
		hub.privateMessage(6, 2, "I've got you cubed");
		
		// demo delay
		Thread.sleep(1000);
		
		// broadcast
		hub.broadcastMessage(5, "Hey you guys!");

		// demo delay
		Thread.sleep(2000);
	}
}
