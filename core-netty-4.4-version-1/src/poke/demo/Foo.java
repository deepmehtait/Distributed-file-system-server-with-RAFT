package poke.demo;

import java.util.ArrayList;

public class Foo extends Thread {
	public Foo() {
		super();
	}

	public void run() {
		System.out.println("hello " + this.getId());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<Foo> list = new ArrayList<Foo>(4);
		for (int i = 0, I = 4; i < I; i++) {
			list.add(new Foo());
		}

		// run them
		for (Foo h : list)
			h.start();
	}
}
