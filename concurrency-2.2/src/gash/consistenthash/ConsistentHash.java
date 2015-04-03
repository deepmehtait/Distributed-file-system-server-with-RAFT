package gash.consistenthash;

import java.util.TreeMap;

public class ConsistentHash {
	private int binSize;
	private int[] binWeights;
	private TreeMap<Long, String> buckets = new TreeMap<Long, String>();

	public ConsistentHash(int binSize, int[] weights) {
		if (binSize <= 0)
			throw new RuntimeException("bin size must be greater than 0");

		this.binWeights = weights;
		this.binSize = binSize;
		init();
	}

	public void init() {
	}

	public Integer locate(String value) {
		Integer bin = null;

		return bin;
	}
}
