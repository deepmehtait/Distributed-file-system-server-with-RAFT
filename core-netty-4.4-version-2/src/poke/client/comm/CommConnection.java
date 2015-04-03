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
package poke.client.comm;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.Request;

import com.google.protobuf.GeneratedMessage;

/**
 * provides an abstraction of the communication to the remote server. This could
 * be a public (request) or a internal (management) request.
 * 
 * Note you cannot use the same instance for both management and request based
 * messaging.
 * 
 * @author gash
 * 
 */
public class CommConnection {
	protected static Logger logger = LoggerFactory.getLogger("connect");

	private String host;
	private int port;
	private ChannelFuture channel; // do not use directly call
									// connect()!

	private EventLoopGroup group;
	private CommHandler handler;

	// our surge protection using a in-memory cache for messages
	private LinkedBlockingDeque<com.google.protobuf.GeneratedMessage> outbound;

	// message processing is delegated to a threading model
	private OutboundWorker worker;

	/**
	 * Create a connection instance to this host/port. On construction the
	 * connection is attempted.
	 * 
	 * @param host
	 * @param port
	 */
	public CommConnection(String host, int port) {
		this.host = host;
		this.port = port;

		init();
	}

	/**
	 * release all resources
	 */
	public void release() {
		group.shutdownGracefully();
	}

	/**
	 * send a message - note this is asynchronous
	 * 
	 * @param req
	 *            The request
	 * @exception An
	 *                exception is raised if the message cannot be enqueued.
	 */
	public void sendMessage(Request req) throws Exception {
		// enqueue message
		outbound.put(req);
	}

	/**
	 * abstraction of notification in the communication
	 * 
	 * @param listener
	 */
	public void addListener(CommListener listener) {
		// note: the handler should not be null as we create it on construction

		try {
			handler.addListener(listener);
		} catch (Exception e) {
			logger.error("failed to add listener", e);
		}
	}

	private void init() {
		// the queue to support client-side surging
		outbound = new LinkedBlockingDeque<com.google.protobuf.GeneratedMessage>();

		group = new NioEventLoopGroup();
		try {
			handler = new CommHandler();
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(handler);
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);

			// Make the connection attempt.
			channel = b.connect(host, port).syncUninterruptibly();

			// want to monitor the connection to the server s.t. if we loose the
			// connection, we can try to re-establish it.
			ClientClosedListener ccl = new ClientClosedListener(this);
			channel.channel().closeFuture().addListener(ccl);

		} catch (Exception ex) {
			logger.error("failed to initialize the client connection", ex);

		}

		// start outbound message processor
		worker = new OutboundWorker(this);
		worker.start();
	}

	/**
	 * create connection to remote server
	 * 
	 * @return
	 */
	protected Channel connect() {
		// Start the connection attempt.
		if (channel == null) {
			init();
		}

		if (channel.isDone() && channel.isSuccess())
			return channel.channel();
		else
			throw new RuntimeException("Not able to establish connection to server");
	}

	/**
	 * queues outgoing messages - this provides surge protection if the client
	 * creates large numbers of messages.
	 * 
	 * @author gash
	 * 
	 */
	protected class OutboundWorker extends Thread {
		CommConnection conn;
		boolean forever = true;

		public OutboundWorker(CommConnection conn) {
			this.conn = conn;

			if (conn.outbound == null)
				throw new RuntimeException("connection worker detected null queue");
		}

		@Override
		public void run() {
			Channel ch = conn.connect();
			if (ch == null || !ch.isOpen()) {
				CommConnection.logger.error("connection missing, no outbound communication");
				return;
			}

			while (true) {
				if (!forever && conn.outbound.size() == 0)
					break;

				try {
					// block until a message is enqueued
					GeneratedMessage msg = conn.outbound.take();
					if (ch.isWritable()) {
						CommHandler handler = conn.connect().pipeline().get(CommHandler.class);

						if (!handler.send(msg))
							conn.outbound.putFirst(msg);

					} else
						conn.outbound.putFirst(msg);
				} catch (InterruptedException ie) {
					break;
				} catch (Exception e) {
					CommConnection.logger.error("Unexpected communcation failure", e);
					break;
				}
			}

			if (!forever) {
				CommConnection.logger.info("connection queue closing");
			}
		}
	}

	/**
	 * usage:
	 * 
	 * <pre>
	 * channel.getCloseFuture().addListener(new ClientClosedListener(queue));
	 * </pre>
	 * 
	 * @author gash
	 * 
	 */
	public static class ClientClosedListener implements ChannelFutureListener {
		CommConnection cc;

		public ClientClosedListener(CommConnection cc) {
			this.cc = cc;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			// we lost the connection or have shutdown.

			// @TODO if lost, try to re-establish the connection
		}
	}
}
