package poke.server.conf;

import java.util.ArrayList;
import java.util.TreeMap;

public class ClusterConfList {

	TreeMap<Integer, ClusterConf> clusters;

	public TreeMap<Integer, ClusterConf> getClusters() {
		return clusters;
	}

	public void setClusters(TreeMap<Integer, ClusterConf> clusters) {
		this.clusters = clusters;
	}

	public static class ClusterConf {
		private String clusterName;
		private ArrayList<NodeDesc> clusterNodes;

		public String getClusterName() {
			return clusterName;
		}

		public void setClusterName(String clusterName) {
			this.clusterName = clusterName;
		}

		public ArrayList<NodeDesc> getClusterNodes() {
			return clusterNodes;
		}

		public void setClusterNodes(ArrayList<NodeDesc> clusterNodes) {
			this.clusterNodes = clusterNodes;
		}

	}
}
