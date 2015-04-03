package gash.leaderelection;

import gash.leaderelection.SpanningTree.TreeNode;

import org.junit.Test;

public class SpanningTreeTest {

	@Test
	public void testBuildSpanningTree() throws Exception {

		SpanningTree st = new SpanningTree();

		// simple a->b a->f b->c b->d d->e
		TreeNode a = new TreeNode(st, true);
		a.setName("A");
		a.setNodeId(1);

		TreeNode b = new TreeNode(st, false);
		b.setName("B");
		b.setNodeId(2);

		TreeNode c = new TreeNode(st, false);
		c.setName("C");
		c.setNodeId(3);

		TreeNode d = new TreeNode(st, false);
		d.setName("D");
		d.setNodeId(4);

		TreeNode e = new TreeNode(st, false);
		e.setName("E");
		e.setNodeId(5);

		TreeNode f = new TreeNode(st, false);
		f.setName("F");
		f.setNodeId(6);

		// edges
		a.addChild(b);
		b.addChild(c);
		b.addChild(d);
		d.addChild(e);
		a.addChild(f);

		// network
		st.addNode(a);
		st.addNode(b);
		st.addNode(c);
		st.addNode(d);
		st.addNode(e);
		st.addNode(f);

		System.out.println("Tree:");
		a.printTree();

		System.out.println("\n---------------------------------\n");

		// initiate network discovery
		c.buildTree();

		// TODO add loop to test if the tree is complete
		Thread.sleep(1000);

		// now send a message to flood
		System.out.println("\n---------------------------------\n");
		b.postMessage();

		// allow a couple of leaders to die so that we can see the leader
		// election process in action
		Thread.sleep(5000);
	}

}
