package poke.resources;

import io.netty.channel.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.ClientMessage;
import poke.comm.App.Header;
import poke.comm.App.Header.Routing;
import poke.comm.App.Payload;
import poke.comm.App.Request;
import poke.server.managers.ConnectionManager;
import poke.server.managers.ConnectionManager.connectionState;
import poke.server.resources.Resource;

public class RegisterResource implements Resource {
	protected static Logger logger = LoggerFactory.getLogger("Register Resource");

		
	public RegisterResource() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void process(Request request, Channel ch) {
		// TODO Auto-generated method stub
		//Add the channel and node id in connections
		//Send Reply to Client that successfully connected
		logger.info("Received Register Request from Client");
		Header head = request.getHeader(); 
		
		int clientId = request.getBody().getClientMessage().getSenderClientId();
		ConnectionManager.addConnection(clientId, ch, connectionState.CLIENTAPP);
		
		//NOw send back the reply
		Request.Builder r = Request.newBuilder();

		//Build Header
		Header.Builder h = Header.newBuilder();
		h.setRoutingId(Routing.REGISTER);
		h.setOriginator(head.getToNode());
		h.setTag("Client Successfully Registered");
		h.setReplyMsg("Client Successfully Registered");
		h.setTime(System.currentTimeMillis());
		//NOt setting Poke Status, Reply Message, ROuting Path, Name Value set
		//TO Node is the node to which this client will get connected
		h.setToNode(head.getOriginator());
		ClientMessage.Builder clBuilder  = ClientMessage.newBuilder();
		clBuilder.setMessageType(poke.comm.App.ClientMessage.MessageType.SUCCESS);
		
		//Build Payload
		Payload.Builder p = Payload.newBuilder();
		//Not adding anything as it is just a register message
		p.setClientMessage(clBuilder);

		r.setBody(p);
		r.setHeader(h);
		Request req = r.build();
		ConnectionManager.sendToClient(req, clientId);
	}


}
