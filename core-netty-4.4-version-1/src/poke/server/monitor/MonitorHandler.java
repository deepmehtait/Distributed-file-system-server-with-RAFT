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
package poke.server.monitor;

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
 * Receive a heartbeat (HB). This class is used internally by servers to receive
 * HB notices for nodes in its neighborhood list (DAG).
 * 
 * @author gash
 * 
 */
public class MonitorHandler extends SimpleChannelInboundHandler<Management> {
	protected static Logger logger = LoggerFactory.getLogger("mgmt");

	protected ConcurrentMap<Integer, MonitorListener> listeners = new ConcurrentHashMap<Integer, MonitorListener>();
	private volatile Channel channel;

	public MonitorHandler() {
	}

	public String getNodeName() {
		if (channel != null)
			return channel.localAddress().toString();
		else
			return String.valueOf(this.hashCode());
	}

	public Integer getNodeId() {
		if (listeners.size() > 0)
			return listeners.values().iterator().next().getListenerID();
		else
			return -9999;
	}

	public void addListener(MonitorListener listener) {
		if (listener == null)
			return;

		listeners.putIfAbsent(listener.getListenerID(), listener);
	}

	public boolean send(GeneratedMessage msg) {
		// TODO a queue is needed to prevent overloading of the socket
		// connection. For the demonstration, we don't need it
		ChannelFuture cf = channel.write(msg);
		if (cf.isDone() && !cf.isSuccess()) {
			logger.error("failed to send message!");
			return false;
		}

		return true;
	}

	/**
	 * a message was received from the server. Here we dispatch the message to
	 * the listeners to process - note if the listeners are not threaded, this
	 * read will block until all listeners have processed the message.
	 * 
	 * @param msg
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Management msg) throws Exception {
		for (Integer id : listeners.keySet()) {
			MonitorListener ml = listeners.get(id);
			// TODO this may need to be delegated to a thread pool to allow
			// async processing of replies
			ml.onMessage(msg, ctx);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.error("monitor channel inactive");

		// TODO try to reconnect
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
	}

}
