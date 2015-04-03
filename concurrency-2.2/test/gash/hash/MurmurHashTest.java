package gash.hash;

import org.junit.Test;

public class MurmurHashTest {
	static int NUM = 1000;
	private static final String[] values = { "one", "One", "ONe", "ONE", "one", "red", "blue", "green", "pink", "dragon" };

	@Test
	public void testHash() {
		Ketama ketama = new Ketama();
		for (String v : values) {

			System.out.println(v + " -> " + ketama.hash(v));
		}
	}
	
	//@Test
	public void testHash2() {
		MurmurHash murmur = new MurmurHash();

		byte[] bytes = new byte[4];
		for (int i = 0; i < NUM; i++) {
			bytes[0] = (byte) (i & 0xff);
			bytes[1] = (byte) ((i & 0xff00) >> 8);
			bytes[2] = (byte) ((i & 0xff0000) >> 16);
			bytes[3] = (byte) ((i & 0xff000000) >> 24);
			String v = new String(bytes);
			System.out.println(Integer.toHexString(i) + " -> " + murmur.hash(v));
		}
	}

}
