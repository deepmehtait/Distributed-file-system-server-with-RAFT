package gash.hash;

import org.junit.Test;

public class KetamaTest {
	private static final String[] values = { "one", "One", "ONe", "ONE", "one", "red", "blue", "green", "pink", "dragon" };

	@Test
	public void testHash() {
		Ketama ketama = new Ketama();
		for (String v : values) {

			System.out.println(v + " -> " + ketama.hash(v));
		}
	}

}
