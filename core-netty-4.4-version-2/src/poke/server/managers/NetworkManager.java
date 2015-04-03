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
package poke.server.managers;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.Management;
import poke.core.Mgmt.Network;
import poke.core.Mgmt.Network.NetworkAction;
import poke.server.conf.ServerConf;

/**
 * The network manager contains the node's view of the network. This view is
 * created through messages sent by other nodes to this node. For every
 * connection created, this manager creates a map.
 * 
 * @author gash
 * 
 */
public class NetworkManager {
	protected static Logger logger = LoggerFactory.getLogger("network");
	protected static AtomicReference<NetworkManager> instance = new AtomicReference<NetworkManager>();

	private static ServerConf conf;

	/** @brief the number of votes this server can cast */
	private int votes = 1;

	public static NetworkManager initManager(ServerConf conf) {
		NetworkManager.conf = conf;
		instance.compareAndSet(null, new NetworkManager());
		return instance.get();
	}

	public static NetworkManager getInstance() {
		// TODO throw exception if not initialized!
		return instance.get();
	}

	/**
	 * initialize the manager for this server
	 * 
	 */
	protected NetworkManager() {

	}

	/**
	 * @param args
	 */
	public void processRequest(Management mgmt, Channel channel) {
		Network req = mgmt.getGraph();
		if (req == null || channel == null)
			return;

		logger.info("Network: node '" + req.getFromNodeId() + "' sent a " + req.getAction());

		/**
		 * Outgoing: when a node joins to another node, the connection is
		 * monitored to relay to the requester that the node (this) is active -
		 * send a heartbeatMgr
		 */
		if (req.getAction().getNumber() == NetworkAction.NODEJOIN_VALUE) {
			if (channel.isOpen()) {
				// can i cast socka?
				SocketAddress socka = channel.localAddress();
				if (socka != null) {
					// this node will send messages to the requesting client
					InetSocketAddress isa = (InetSocketAddress) socka;
					logger.info("NODEJOIN: " + isa.getHostName() + ", " + isa.getPort());
					HeartbeatManager.getInstance().addOutgoingChannel(req.getFromNodeId(), isa.getHostName(),
							isa.getPort(), channel, socka);
				}
			} else
				logger.warn(req.getFromNodeId() + " not writable");
		} else if (req.getAction().getNumber() == NetworkAction.NODEDEAD_VALUE) {
			// possible failure - node is considered dead
		} else if (req.getAction().getNumber() == NetworkAction.NODELEAVE_VALUE) {
			// node removing itself from the network (gracefully)
		} else if (req.getAction().getNumber() == NetworkAction.ANNOUNCE_VALUE) {
			// nodes sending their info in response to a create map
		} else if (req.getAction().getNumber() == NetworkAction.CREATEMAP_VALUE) {
			// request to create a network topology map
		}

		// may want to reply to exchange information
	}
}
