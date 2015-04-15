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

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.Request;
import poke.core.Mgmt.Management;

/**
 * the connection map for server-to-server communication.
 * 
 * Note the connections/channels are initialized through the heartbeat manager
 * as it starts (and maintains) the connections through monitoring of processes.
 * 
 * 
 * TODO refactor to make this the consistent form of communication for the rest
 * of the code
 * 
 * @author gash
 * 
 */
public class ConnectionManager {
	protected static Logger logger = LoggerFactory.getLogger("management");

	/** node ID to channel */
	private static HashMap<Integer, Channel> appconnections = new HashMap<Integer, Channel>();
	private static HashMap<Integer, Channel> connections = new HashMap<Integer, Channel>();
	private static HashMap<Integer, Channel> mgmtConnections = new HashMap<Integer, Channel>();
	public static enum connectionState {SERVERAPP, SERVERMGMT, CLIENTAPP };

	public static void addConnection(Integer nodeId, Channel channel, connectionState state) {
		logger.info("ConnectionManager adding connection to " + nodeId);

		if (state == connectionState.SERVERMGMT)
			mgmtConnections.put(nodeId, channel);
		else if(state == connectionState.CLIENTAPP ){
			connections.put(nodeId, channel);
			logger.info("Client Added in Connection Manager " + nodeId);
		}
		else if(state == connectionState.SERVERAPP)
			appconnections.put(nodeId, channel);
			logger.info("Node Added in Connection Manager " + nodeId);
	}

	public static Channel getConnection(Integer nodeId, connectionState state) {
				if (state == connectionState.SERVERMGMT)
					return mgmtConnections.get(nodeId);
				else if(state == connectionState.CLIENTAPP ){
					logger.info("Client got in Connection Manager " + nodeId);
					return connections.get(nodeId);
					
				}
				else if(state == connectionState.SERVERAPP)
				{
					logger.info("Node got in Connection Manager " + nodeId);
					return appconnections.get(nodeId);
				}
				return null;
					
	}

	public synchronized static void removeConnection(Integer nodeId, connectionState state) {
		if (state == connectionState.SERVERMGMT)
			mgmtConnections.remove(nodeId);
		else if(state == connectionState.CLIENTAPP ){
			connections.remove(nodeId);
		}
		else if(state == connectionState.SERVERAPP)
			appconnections.remove(nodeId);
	}

	public synchronized static void removeConnection(Channel channel, connectionState state) {
		if (state == connectionState.SERVERMGMT)
		{
			if (!mgmtConnections.containsValue(channel)) {
				return;
			}
			logger.info("Management Connections Size : " + ConnectionManager.getNumMgmtConnections());
			for (Integer nid : mgmtConnections.keySet()) {
				if (channel == mgmtConnections.get(nid)) {
					mgmtConnections.remove(nid);
					break;
				}
			}
			logger.info("Management Connections Size After Removal: " + ConnectionManager.getNumMgmtConnections());
		}
		else if(state == connectionState.CLIENTAPP ){
			if (!connections.containsValue(channel)) {
				return;
			}

			for (Integer nid : connections.keySet()) {
				if (channel == connections.get(nid)) {
					connections.remove(nid);
					break;
				}
			}
		}
		else if(state == connectionState.SERVERAPP)
		{
			if (!appconnections.containsValue(channel)) {
				return;
			}

			for (Integer nid : appconnections.keySet()) {
				if (channel == appconnections.get(nid)) {
					appconnections.remove(nid);
					break;
				}
			}

		}
	}

	public synchronized static void broadcast(Request req) {
		if (req == null)
			return;

		for (Channel ch : connections.values())
			ch.writeAndFlush(req);
	}
	
	public synchronized static void broadcastServers(Request req) {
		if (req == null)
			return;

		for (Channel ch : appconnections.values())
			ch.writeAndFlush(req);
	}

	public synchronized static void broadcast(Management mgmt) {
		if (mgmt == null)
			return;

		for (Channel ch : mgmtConnections.values())
			ch.write(mgmt);
	}
	
	public synchronized static void broadcastAndFlush(Management mgmt) {
		if (mgmt == null)
			return;

		for (Channel ch : mgmtConnections.values())
			ch.writeAndFlush(mgmt);
	}

	public static int getNumMgmtConnections() {
		return mgmtConnections.size();
	}
	public synchronized static void sendToNode(Request req,
			Integer destination) {
		if (req == null)
			return;
		if (appconnections.get(destination) != null){
			appconnections.get(destination).writeAndFlush(req);
		} else
			System.out.println("No destination found");
	}
	
	public synchronized static void sendToClient(Request req,
			Integer destination) {
		if (req == null)
			return;
		if (connections.get(destination) != null){
			connections.get(destination).writeAndFlush(req);
		} else
			System.out.println("No clients found");
	}
	public static Set getKeySetConnections(connectionState state){
		if(state == connectionState.CLIENTAPP)
		return connections.keySet();
		else if(state == connectionState.SERVERAPP)
			return appconnections.keySet();
		else if(state == connectionState.SERVERMGMT)
			return mgmtConnections.keySet();
		else 
			return null;
	}
	public static boolean checkClient(connectionState state, int key)
	{
		if(state == connectionState.CLIENTAPP)
			return connections.containsKey(key);
		else if(state == connectionState.SERVERAPP)
			return appconnections.containsKey(key);
		else if(state == connectionState.SERVERMGMT)
			return mgmtConnections.containsKey(key);
		else 
				return false;
	}
}
