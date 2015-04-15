package poke.resources;

import io.netty.channel.Channel;

import java.io.InputStream;
import java.util.Set;

import poke.comm.App.ClientMessage;
import poke.comm.App.Header;
import poke.comm.App.Header.Routing;
import poke.comm.App.Payload;
import poke.comm.App.Request;
import poke.ftp.ftpConnection;
import poke.server.managers.ConnectionManager;
import poke.server.managers.ConnectionManager.connectionState;
import poke.server.resources.Resource;

import com.google.protobuf.ByteString;

public class ReportsResource implements Resource {

	@Override
	public void process(Request request, Channel ch) {
		
		ftpConnection ftp = new ftpConnection();
		//Download File
		ftp.connect();
		System.out.println("Receiver =sahjvdhvsaddsc" + request.getBody().getClientMessage().getReceiverClientId());

		ClientMessage cm = request.getBody().getClientMessage();
		int receiverClientId = 2000;
		int senderClientId = cm.getSenderClientId();
		
		Request r = createClientRequest(request, ftp);
		System.out.println("Receiver =ghchgc" + receiverClientId);
		//IF receiver is -1 or is not present then all nodes will broad cast
		//except for the node which had sent the request
		if(receiverClientId ==-1 || receiverClientId == 0)
		{
			if(ConnectionManager.checkClient(connectionState.CLIENTAPP,senderClientId))
			{
				@SuppressWarnings("unchecked")
				Set<Integer> keys = ConnectionManager.getKeySetConnections(connectionState.CLIENTAPP);
				for(int i : keys)
				{
					if(i!=senderClientId)
					{
						ConnectionManager.sendToClient(r, i);
					}	
				}
			}
			else
				ConnectionManager.broadcast(r);
		}
		//If receiver is present then check if that client is connected to this node 
		//and send it to him
		else
		{
			System.out.println("In Else of Repports Resource");
			if(ConnectionManager.checkClient(connectionState.CLIENTAPP,receiverClientId))
			{
				System.out.println("send to client");
				ConnectionManager.sendToClient(r, receiverClientId);
			}
		}	
	}
	
	public Request createClientRequest(Request request, ftpConnection ftp)
	{
		try{
			ClientMessage.Builder cBuilder  = ClientMessage.newBuilder();
			ClientMessage reqClientMsg=request.getBody().getClientMessage();
			
			
			cBuilder.setMsgId(reqClientMsg.getMsgId());
			cBuilder.setSenderUserName(reqClientMsg.getSenderUserName());
			cBuilder.setReceiverUserName(reqClientMsg.getReceiverUserName());
			cBuilder.setSenderClientId(reqClientMsg.getSenderClientId());
			cBuilder.setReceiverClientId(reqClientMsg.getReceiverClientId());
			cBuilder.setMsgText(reqClientMsg.getMsgText());
			cBuilder.setMsgImageName(reqClientMsg.getMsgImageName());
			InputStream is1 = ftp.retrieveImage();
			cBuilder.setMsgImageBits(ByteString.readFrom(is1));
			cBuilder.setMessageType(reqClientMsg.getMessageType());
			cBuilder.setIsClient(true);
			cBuilder.setBroadcastInternal(true);
			
			
			Header.Builder h = Header.newBuilder();
			Header reqHeader = request.getHeader();
			//means each node receiving this message has to report this message to their respective client
			h.setRoutingId(Routing.JOBS);
			h.setOriginator(reqHeader.getOriginator());
			h.setTag(reqHeader.getTag());
			h.setTime(System.currentTimeMillis());
			h.setToNode(reqHeader.getToNode());
			
			//add client msg to body
			Payload.Builder payload = Payload.newBuilder();
			payload.setClientMessage(cBuilder);
				
			
			//add body to request
			Request.Builder req = Request.newBuilder();
			req.setBody(payload);
			req.setHeader(h);
			Request r = req.build();
			
			return r;

		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
				
	}
}
