/*
 * copyright 2014, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.resources;

import io.netty.channel.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.ClientMessage;
import poke.comm.App.Header;
import poke.comm.App.Header.Routing;
import poke.comm.App.Payload;
import poke.comm.App.Request;
import poke.server.conf.ServerConf;
import poke.server.managers.ConnectionManager;
import poke.server.managers.RaftManager;
import poke.server.resources.Resource;

/**
 * This is used by the leader to send the append RPC logs and after receiving the majority 
 * sending the image to all other nodes
 * @ author Ankit
 */
public class ForwardResource implements Resource {
	protected static Logger logger = LoggerFactory.getLogger("server");

	private ServerConf cfg;

	public ServerConf getCfg() {
		return cfg;
	}

	/**
	 * Set the server configuration information used to initialized the server.
	 * 
	 * @param cfg
	 */
	public void setCfg(ServerConf cfg) {
		this.cfg = cfg;
	}

	@Override
	public void process(Request request, Channel ch) {
		//create a log for image and send it to all servers in management message.
		// After receiving majority send a APP message to all servers to send the message 
		//to respective clients
		String imageName = request.getBody().getClientMessage().getMsgImageName();
		createBroadcastLogRequest(imageName);
		createBroadcastRequest(request,ch);
	}
	
	private void createBroadcastLogRequest(String imageName) {
		
		RaftManager.getInstance().createLogs(imageName);
		
	}

	/***The message is received here if THIS  node is the leader and 	it will broadcast
	 *  the message to all nodes through app channel and ask each node to send it to their client
	 */
	public void createBroadcastRequest(Request request, Channel ch){
		System.out.println("Forward receiver ID" + request.getBody().getClientMessage().getReceiverClientId());
		ReportsResource rs = new ReportsResource();
		rs.process(request, ch);
		
		ClientMessage.Builder cBuilder  = ClientMessage.newBuilder();
		ClientMessage reqClientMsg=request.getBody().getClientMessage();
				
		cBuilder.setMsgId(reqClientMsg.getMsgId());
		cBuilder.setSenderUserName(reqClientMsg.getSenderUserName());
		cBuilder.setReceiverUserName(reqClientMsg.getReceiverUserName());
		cBuilder.setSenderClientId(reqClientMsg.getSenderClientId());
		cBuilder.setReceiverClientId(reqClientMsg.getReceiverClientId());
		cBuilder.setMsgText(reqClientMsg.getMsgText());
		cBuilder.setMsgImageName(reqClientMsg.getMsgImageName());
		//cBuilder.setMsgImageBits(reqClientMsg.getMsgImageBits());
		cBuilder.setMessageType(reqClientMsg.getMessageType());
		cBuilder.setIsClient(true);
		cBuilder.setBroadcastInternal(true);
		
		Header.Builder h = Header.newBuilder();
		Header reqHeader = request.getHeader();
		//means each node receiving this message has to report this message to their respective client
		h.setRoutingId(Routing.REPORTS);
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
		ConnectionManager.broadcastServers(r);
	}

}
