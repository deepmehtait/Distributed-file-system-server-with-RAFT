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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.ClientMessage;
import poke.comm.App.Header;
import poke.comm.App.Header.Routing;
import poke.comm.App.Payload;
import poke.comm.App.Request;
import poke.comm.App.RoutingPath;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.managers.ConnectionManager;
import poke.server.resources.Resource;

/**
 * The forward resource is used by the ResourceFactory to send requests to a
 * destination that is not this server.
 * 
 * Strategies used by the Forward can include TTL (max hops), durable tracking,
 * endpoint hiding.
 * 
 * @author gash
 * 
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
		/*Integer nextNode = determineForwardNode(request);
		if (nextNode != null) {
			Request fwd = ResourceUtil.buildForwardMessage(request, cfg);
			return fwd;
		} else {
			Request reply = null;
			// cannot forward the message - no one to forward request to as
			// the request has traveled all known/available edges of this node
			String statusMsg = "Unable to forward message, no paths or have already traversed";
			Request rtn = ResourceUtil.buildError(request.getHeader(), PokeStatus.NOREACHABLE, statusMsg);
			return rtn;
			}
*/
	/***The message is received here if THIS  node is the leader and 	it will broadcast
	 *  the message to all nodes through app channel and ask each node to send it to their client
	 */
		System.out.println("Forward ma receiver " + request.getBody().getClientMessage().getReceiverClientId());
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

	/**
	 * Find the nearest node that has not received the request.
	 * 
	 * TODO this should use the heartbeat to determine which node is active in
	 * its list.
	 * 
	 * @param request
	 * @return
	 */
/*	private Integer determineForwardNode(Request request) {
		List<RoutingPath> paths = request.getHeader().getPathList();
		if (paths == null || paths.size() == 0) {
			// pick first nearest
			NodeDesc nd = cfg.getAdjacent().getAdjacentNodes().values().iterator().next();
			return nd.getNodeId();
		} else {
			// if this server has already seen this message return null
			for (RoutingPath rp : paths) {
				for (NodeDesc nd : cfg.getAdjacent().getAdjacentNodes().values()) {
					if (nd.getNodeId() != rp.getNodeId())
						return nd.getNodeId();
				}
			}
		}

		return null;
	}*/
}
