package gash.messaging.transports;

import gash.messaging.Message;
import gash.messaging.Node;

public interface MessageTransport<M extends Message> {

	/**
	 * this is only for testing - normally we would not expose this
	 * 
	 * @return
	 */
	Node<M>[] getNodes();
	
	void addNode(Node<M> node);

	void sendMessage(M msg);

	void sendMessage(int fromNodeId, int toNodeId, String text);

	void broadcastMessage(int fromNodeId, String text);
}