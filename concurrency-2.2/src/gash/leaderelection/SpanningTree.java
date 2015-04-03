package gash.leaderelection;

import gash.messaging.Message;
import gash.messaging.Message.Delivery;
import gash.messaging.Node;
import gash.messaging.transports.Bus;
import gash.messaging.transports.MessageTransport;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SpanningTree {
	static AtomicInteger msgID = new AtomicInteger(0);

	private MessageTransport<Message> transport;
	private ArrayList<TreeNode> tree = new ArrayList<TreeNode>();

	public SpanningTree() {
		transport = new Bus<Message>(0);
		// transport = new Line(10, true);
	}

	public TreeNode lookupNode(int nodeID) {
		for (TreeNode tn : tree) {
			if (tn.getNodeId() == nodeID)
				return tn;
		}

		return null;
	}

	synchronized void sendMsg(TreeNode from, int toID, TreeNode.MessageReply reply) {
		Message m = new Message(SpanningTree.msgID.incrementAndGet());
		m.setDeliverAs(Delivery.Direct);
		m.setDestination(toID);
		m.setMessage(reply.toString());

		if (from != null)
			m.setOriginator(from.getNodeId());

		transport.sendMessage(m);
	}

	public TreeNode getRoot() {
		for (TreeNode tn : tree) {
			if (tn.getParent() == null)
				return tn;
		}

		return null;
	}

	/**
	 * not a new message
	 * 
	 * @param from
	 * @param toID
	 * @param reply
	 */
	synchronized void forwardMsg(int toID, Message msg) {
		Message m = new Message(msg.getId());
		m.setDeliverAs(Delivery.Direct);
		m.setDestination(toID);
		m.setMessage(msg.getMessage());
		m.setOriginator(msg.getOriginator());

		transport.sendMessage(m);
	}

	protected ArrayList<TreeNode> getTree() {
		return tree;
	}

	protected void setTree(ArrayList<TreeNode> tree) {
		this.tree = tree;
	}

	public void addNode(TreeNode node) {
		tree.add(node);
		transport.addNode(node);

		if (!node.isAlive())
			node.start();
	}

	/**
	 * represents a node (process) in the network. Class only represents leader
	 * monitoring and election process. This would exist as a separate
	 * communication path or embedded within the main communication traffic. If
	 * embedded, a priority for reading leader messages should be used.
	 * 
	 * @author gash1
	 * 
	 */
	public static class TreeNode extends Node<Message> {
		public enum MessageReply {
			Invite, Accept, Reject, Message
		}

		SpanningTree network;
		TreeNode parent;
		// ArrayList<TreeNode> parent = new ArrayList<TreeNode>();
		ArrayList<TreeNode> children = new ArrayList<TreeNode>();
		int numReports = 0;
		boolean done = false;

		// don't propagate messages already received
		int lastMessageId = -1;

		public TreeNode(SpanningTree network, boolean isRoot) {
			super(0);
			this.network = network;

			// if (isRoot) {
			// parent = this;
			// }
		}

		public void printTree() {
			if (children.size() > 0) {
				for (TreeNode tn : children) {
					System.out.println(this.getName() + " (" + this.getNodeId() + ") -> " + tn.getName() + " ("
							+ tn.getNodeId() + ")");

					tn.printTree();
				}
			}
		}

		public void process(Message msg) {
			MessageReply tag = MessageReply.valueOf(msg.getMessage());

			TreeNode sourceNode = network.lookupNode(msg.getOriginator());

			System.out.println("--> " + this.getName() + " (" + this.getNodeId() + ") got a " + msg.getMessage()
					+ " from " + sourceNode.getName() + " (" + sourceNode.getNodeId() + ")");

			if (tag == MessageReply.Message) {
				if (lastMessageId < msg.getId()) {
					lastMessageId = msg.getId();
					for (TreeNode tn : children)
						network.forwardMsg(tn.getNodeId(), msg);
				}
			} else if (tag == MessageReply.Invite) {
				// a node (parent) invites a node (child) to join its network
				if (parent == null) {
					numReports++;
					parent = sourceNode;
					network.sendMsg(this, sourceNode.getNodeId(), MessageReply.Accept);
					for (TreeNode tn : children)
						if (tn.getId() != nodeId && tn.getId() != msg.getOriginator())
							network.sendMsg(this, tn.getNodeId(), MessageReply.Invite);
				} else
					network.sendMsg(this, sourceNode.getNodeId(), MessageReply.Reject);
			} else if (tag == MessageReply.Accept || tag == MessageReply.Reject) {
				if (tag == MessageReply.Accept)
					children.add(sourceNode);
				numReports++;
				if (numReports == children.size()) {
					done = true;
					System.out.println("Tree complete");
				}
			}

		}

		public void addChild(TreeNode node) {
			children.add(node);

			// TODO allow for a node to have multiple parents
			// node.setPrevious(this);
		}

		public void buildTree() {
			TreeNode root = network.getRoot();
			if (root != null)
				network.sendMsg(root, root.getNodeId(), MessageReply.Invite);
			else
				throw new RuntimeException("No root to propagate message");
		}

		public void postMessage() {
			network.sendMsg(this, this.getNodeId(), MessageReply.Message);
		}

		public TreeNode getParent() {
			return parent;
		}
	}

}
