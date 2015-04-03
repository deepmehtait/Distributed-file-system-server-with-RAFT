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
package poke.server.management.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.Management;

import com.google.protobuf.GeneratedMessage;

/**
 * A client-side netty pipeline send/receive.
 * 
 * Note a management client is (and should only be) a trusted, private client.
 * This is not intended for public use.
 * 
 * @author gash
 * 
 */
public class MgmtHandler extends SimpleChannelInboundHandler<Management> {
	protected static Logger logger = LoggerFactory.getLogger("connect");
	protected ConcurrentMap<String, MgmtListener> listeners = new ConcurrentHashMap<String, MgmtListener>();
	private volatile Channel channel;

	public MgmtHandler() {
	}

	/**
	 * messages pass through this method. We use a blackbox design as much as
	 * possible to ensure we can replace the underlining communication without
	 * affecting behavior.
	 * 
	 * @param msg
	 * @return
	 */
	public boolean send(GeneratedMessage msg) {
		if (msg == null) {
			logger.warn("cannot send a null message!");
			return false;
		} else if (channel == null) {
			logger.error("No channel exists.");
			return false;

			// TOOD place message back on the queue
		}

		// TODO a queue is needed to prevent overloading of the socket
		// connection. For the demonstration, we don't need it
		ChannelFuture cf = channel.write(msg);
		if (cf.isDone() && !cf.isSuccess()) {
			logger.error("failed to poke!");
			return false;
		}

		return true;
	}

	/**
	 * Notification registration. Classes/Applications receiving information
	 * will register their interest in receiving content.
	 * 
	 * Note: Notification is serial, FIFO like. If multiple listeners are
	 * present, the data (message) is passed to the listener as a mutable
	 * object.
	 * 
	 * @param listener
	 */
	public void addListener(MgmtListener listener) {
		if (listener == null)
			return;

		listeners.putIfAbsent(listener.getListenerID(), listener);
	}

	/**
	 * a message was received from the server. Here we dispatch the message to
	 * the client's thread pool to minimize the time it takes to process other
	 * messages.
	 * 
	 * @param ctx
	 *            The channel the message was received from
	 * @param msg
	 *            The message
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Management msg) throws Exception {
		for (String id : listeners.keySet()) {
			MgmtListener cl = listeners.get(id);

			// TODO this may need to be delegated to a thread pool to allow
			// async processing of replies
			cl.onMessage(msg);
		}
	}
}
