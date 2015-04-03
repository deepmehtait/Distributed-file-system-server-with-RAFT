package gash.concurrency.barber;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Barber implements Runnable {
	public enum ChairStatus {
		Empty, Inuse, Sleeping
	}

	private static Random rand = new Random(System.currentTimeMillis());

	private ChairStatus chair = ChairStatus.Empty;
	private int maxWaiting = 10;
	private ArrayList<Customer> customers = new ArrayList<Customer>();

	public void addCustomer(Customer c) {
		customers.add(c);
	}

	@Override
	public void run() {
		try {
			System.out.println("Initial state - " + this);

			while (hasMoreCustomers()) {
				// check for a customer
				Customer c = nextCustomer();

				if (c == null) {
					System.out.println("   barber sleeping");
					chair = ChairStatus.Sleeping;
					Thread.sleep(500);
					chair = ChairStatus.Empty;
				} else {
					System.out.println("   cutting: " + c.getCustomerName());
					chair = ChairStatus.Inuse;
					c.setStatus(Customer.Status.C);
					Thread.sleep((1 + rand.nextInt(10)) * 100);
					chair = ChairStatus.Empty;
					c.setStatus(Customer.Status.G);
				}

				// how is the barber doing
				System.out.println(new Date() + " - " + this);
			}

			System.out.println("\n** done **");
			System.out.println("\nFinal state - " + this);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Customer nextCustomer() {
		synchronized (customers) {
			for (Customer c : customers) {
				if (c.getStatus() == Customer.Status.W)
					return c;
			}
		}

		return null;
	}

	public boolean hasMoreCustomers() {
		// waiting for customers to arrive
		if (customers.size() == 0)
			return true;

		int active = 0;
		for (Customer c : customers) {
			if (c.getStatus() != Customer.Status.X && c.getStatus() != Customer.Status.G)
				active++;
		}

		return active > 0;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Customers: ");
		if (customers.size() > 0) {
			for (Customer c : customers)
				sb.append(c.getStatus() + " ");
		} else
			sb.append("no customers");

		return sb.toString();
	}

	public void arrive(Customer c) {
		synchronized (customers) {
			int waiting = 0;
			for (Customer t : customers)
				if (t.getStatus() == Customer.Status.W)
					waiting++;

			if (waiting >= maxWaiting) {
				c.setStatus(Customer.Status.X);
				System.out.println("   " + c.getCustomerName() + " leaves - waiting room full");
			} else {
				c.setStatus(Customer.Status.W);
				System.out.println("   " + c.getCustomerName() + " sits in waiting room");
			}

			customers.add(c);
		}
	}

	public static class Customer extends Thread {
		// simplified for printing: O=none, W=waiting, C=cutting, X-no service, G=done
		public enum Status {
			O, W, C, X, G
		}

		private static Random rand = new Random(System.currentTimeMillis());

		private int id;
		private Status status = Status.O;
		private Barber barber;

		public Customer(int id, Barber barber) {
			this.id = id;
			this.barber = barber;
		}

		@Override
		public void run() {
			try {
				// randomly wait before trying to get a hair cut
				Thread.sleep((id + rand.nextInt(10)) * 100);

				barber.arrive(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public String toString() {
			return getCustomerName() + ": " + status;
		}

		/**
		 * @return the status
		 */
		public Status getStatus() {
			return status;
		}

		/**
		 * @param status
		 *            the status to set
		 */
		public void setStatus(Status status) {
			this.status = status;
		}

		/**
		 * @return the name
		 */
		public String getCustomerName() {
			return "C" + id;
		}
	}

	public static void main(String[] args) {
		Barber barber = new Barber();
		for (int n = 0; n < 40; n++) {
			Customer c = new Customer(n, barber);
			c.start();
		}

		barber.run();
	}
}
