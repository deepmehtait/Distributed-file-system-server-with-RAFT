package gash.consistenthash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Test;

public class RingTest {

	@Test
	public void testBasicRing() {
		BasicRing ring = new BasicRing();
		ring.createNodes(10);

		ring.printNodeRanges(new File("/tmp/basic.out"));
	}

	@Test
	public void testEqualAreaRing() {
		EqualAreaRing ring = new EqualAreaRing();
		ring.createNodes(1000);

		ring.printNodeRanges(new File("/tmp/fixed.out"));
	}

	@Test
	public void testMovableSegmentsRing() throws Exception {
		MovableSegmentsRing ring = new MovableSegmentsRing();
		ring.createNodes(10);

		File dataFn = new File("testdata-5m-fml.txt");
		if (!dataFn.exists()) {
			throw new RuntimeException("Missing test data use ant (build.xml) to create it");
		}

		BufferedReader br = new BufferedReader(new FileReader(dataFn));
		String name = null;
		int N = 0;
		long st = System.currentTimeMillis();
		while ((name = br.readLine()) != null) {
			ring.addData(name);
			N++;
		}

		long et = System.currentTimeMillis();
		System.out.println("data added in in " + (et - st) + " msec, " + ((et - st) / N) + " msec/rec");

		File outF = new File("/tmp/movable-1m-fml.out");
		ring.printNodeRanges(outF);
		System.out.println("hash distribution written to" + outF.getAbsolutePath());
		br.close();
	}
}
