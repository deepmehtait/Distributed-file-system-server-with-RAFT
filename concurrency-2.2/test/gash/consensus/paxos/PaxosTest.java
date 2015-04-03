package gash.consensus.paxos;

import gash.consensus.paxos.PaxosNode.Role;
import gash.consensus.paxos.core.Heartbeat;
import gash.consensus.paxos.core.Request.RequestState;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PaxosTest {

	private ArrayList<PaxosNode> nodes;

	@Before
	public void setup() throws Exception {
		int numNodes = 6;

		nodes = new ArrayList<PaxosNode>(numNodes);
		for (int n = 0; n < numNodes; n++) {
			PaxosNode node = new PaxosNode(n);
			nodes.add(node);
			node.start();
		}

		// setup network (linked list)
		for (int n = 0; n < numNodes; n++) {
			if (n + 1 < numNodes)
				nodes.get(n).setNext(nodes.get(n + 1));

			if (n - 1 >= 0)
				nodes.get(n).setPrevious(nodes.get(n - 1));
		}

		// close the ring
		nodes.get(0).setPrevious(nodes.get(numNodes - 1));
		nodes.get(numNodes - 1).setNext(nodes.get(0));

		// assign roles
		nodes.get(5).setRole(Role.Proposer);
		nodes.get(4).setRole(Role.Acceptor);
		nodes.get(3).setRole(Role.Learner);
		nodes.get(2).setRole(Role.Acceptor);
		nodes.get(1).setRole(Role.Learner);
		nodes.get(0).setRole(Role.Acceptor);

		// start the heart beat
		for (PaxosNode node : nodes) {
			Heartbeat hb = new Heartbeat(node);
			hb.setDaemon(true);
			hb.start();
		}
	}

	@After
	public void teardown() throws Exception {
		for (PaxosNode node : nodes) {
			node.shutdown();
		}

		Thread.sleep(1000);
	}

	@Test
	public void testSixNodes() throws Exception {

		// DEMO: declare the leader
		nodes.get(5).demoDeclareAsLeader();
		Thread.sleep(2000);

		// we can send it to any node and it should arrive at the proposer!
		// inject a request
		TextRequest req = new TextRequest();
		req.setData("Hello I'm a request!");
		req.setRequestID(0);
		req.setRequestor("John");
		req.setState(RequestState.New);
		nodes.get(1).submitRequest(req);

		// wait for the thread to complete
		nodes.get(0).join();

		System.out.println("** done **");
	}

	@Test
	public void testNoQuorum() throws Exception {

		// DEMO: set as unreliable/faulty
		nodes.get(2).demoUnreliable();
		nodes.get(4).demoUnreliable();

		// DEMO: declare the leader
		nodes.get(5).demoDeclareAsLeader();
		Thread.sleep(2000);

		// we can send it to any node and it should arrive at the proposer!
		// inject a request
		TextRequest req = new TextRequest();
		req.setData("Hello I'm a request!");
		req.setRequestID(0);
		req.setRequestor("John");
		req.setState(RequestState.New);
		nodes.get(1).submitRequest(req);

		// wait for the thread to complete
		nodes.get(0).join();

		System.out.println("** done **");
	}
}
