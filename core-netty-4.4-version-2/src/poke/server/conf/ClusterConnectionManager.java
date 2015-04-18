/*package poke.server.conf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.JoinMessage;
import poke.comm.App.Request;
import poke.server.ServerInitializer;
import poke.server.conf.ClusterConfList.ClusterConf;

public class ClusterConnectionManager extends Thread {
	protected static Logger logger = LoggerFactory.getLogger("ClusterConnection");
	private Map<Integer, Channel> connMap = new HashMap<Integer, Channel>();
		private Map<Integer, ClusterConf> clusterMap;

		public ClusterConnectionManager() {
			clusterMap = clusterConf.getClusters();
		}

		public void registerConnection(int nodeId, Channel channel) {
			// ConnectionManager.addConnection(nodeId, channel,
			// ConnectionManager.connectionState.APP);
			// TODO send join message
			connMap.put(nodeId, channel);
		}

		public ChannelFuture connect(String host, int port) {

			ChannelFuture channel = null;
			EventLoopGroup workerGroup = new NioEventLoopGroup();

			try {
				logger.info("Attempting to  connect to : "+host+" : "+port);
				Bootstrap b = new Bootstrap();
				b.group(workerGroup).channel(NioSocketChannel.class)
						.handler(new ServerInitializer(false));

				b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
				b.option(ChannelOption.TCP_NODELAY, true);
				b.option(ChannelOption.SO_KEEPALIVE, true);

				channel = b.connect(host, port).syncUninterruptibly();
				//ClusterLostListener cll = new ClusterLostListener(this);
				//channel.channel().closeFuture().addListener(cll);

			} catch (Exception e) {
				//e.printStackTrace();
				//logger.info("Cound not connect!!!!!!!!!!!!!!!!!!!!!!!!!");
				return null;
			}

			return channel;
		}

		public Request createClusterJoinMessage(int fromCluster, int fromNode,
				int toCluster, int toNode) {
			//logger.info("Creating join message");
			Request.Builder req = Request.newBuilder();

			JoinMessage.Builder jm = JoinMessage.newBuilder();
			jm.setFromClusterId(fromCluster);
			jm.setFromNodeId(fromNode);
			jm.setToClusterId(toCluster);
			jm.setToNodeId(toNode);

			req.setJoinMessage(jm.build());
			return req.build();

		}

		@Override
		public void run() {
			Iterator<Integer> it = clusterMap.keySet().iterator();
			while (true) {
				//logger.info(""+isLeader);
				if(isLeader){
					
					try {
						int key = it.next();
						if (!connMap.containsKey(key)) {
							ClusterConf cc = clusterMap.get(key);
							List<NodeDesc> nodes = cc.getClusterNodes();
							//logger.info("For cluster "+ key +" nodes "+ nodes.size());
							for (NodeDesc n : nodes) {
								String host = n.getHost();
								int port = n.getPort();

								ChannelFuture channel = connect(host, port);
								Request req = createClusterJoinMessage(35325,
										conf.getNodeId(), key, n.getNodeId());
								if (channel != null) {
									channel = channel.channel().writeAndFlush(req);
//									logger.info("Message flushed"+channel.isDone()+ " "+
//											 channel.channel().isWritable());
									if (channel.channel().isWritable()) {
										registerConnection(key,
												channel.channel());
										logger.info("Connection to cluster " + key
												+ " added");
										break;
									}
								}
							}
						}
					} catch (NoSuchElementException e) {
						//logger.info("Restarting iterations");
						it = clusterMap.keySet().iterator();
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				} else {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static class ClusterLostListener implements ChannelFutureListener {
		protected static Logger logger = LoggerFactory.getLogger("ClusterListener");
		ClusterConnectionManager ccm;

		public ClusterLostListener(ClusterConnectionManager ccm) {
			this.ccm = ccm;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			logger.info("Cluster " + future.channel()
					+ " closed. Removing connection");
			// TODO remove dead connection
		}
	}
*/