package gash.leaderelection;

import gash.leaderelection.raft.Raft;
import gash.leaderelection.raft.Raft.RaftNode;
import gash.leaderelection.raft.RaftMessage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RaftTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testStartup() throws Exception {
		Raft raft = new Raft();
		for(int i=0;i<5;i++)
		{
			RaftNode<RaftMessage> rn = new RaftNode<RaftMessage>(i);
			raft.addNode(rn);
		}

		// allow a couple of leaders to die so that we can see the leader
		// election process in action
		Thread.sleep(40000);
	}


}
