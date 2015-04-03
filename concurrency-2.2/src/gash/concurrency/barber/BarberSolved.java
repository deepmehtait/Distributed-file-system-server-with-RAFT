package gash.concurrency.barber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Managing access to a limited resource
 * 
 * @author gash1
 * 
 */
public class BarberSolved implements Runnable {
	private Semaphore lockBarber = new Semaphore(1, true);
	private Semaphore lockWaiting = new Semaphore(10, true);
	private List<Customer> customers = Collections.synchronizedList(new ArrayList<Customer>());

	public void addCustomer(Customer c) {
		customers.add(c);
	}

	public void acquireHaircut(Customer c) {
		try {
			lockWaiting.acquire(1);
			customers.add(c);
			c.setStatus(Customer.Status.W);
			System.out.println("   " + c.getCustomerName() + " sits in waiting room");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void releaseHaircut(String name) {
		lockWaiting.release(1);
		System.out.println(name + " is done");
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
					Thread.sleep(200);
				} else {
					System.out.println("   " + c.getCustomerName() + " gets a haircut");
					Thread.sleep(500);
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
		try {
			lockBarber.acquire();
			for (Customer c : customers) {
				if (c.getStatus() == Customer.Status.W) {
					c.setStatus(Customer.Status.C);
					return c;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lockBarber.release();
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
		synchronized (customers) {
			StringBuffer sb = new StringBuffer();
			sb.append("Customers: ");
			if (customers.size() > 0) {
				for (Customer c : customers)
					sb.append(c.getStatus() + " ");
			} else
				sb.append("no customers");

			return sb.toString();
		}
	}

	public static class Customer extends Thread {
		// simplified for printing: O=none, W=waiting, C=cutting, X-no service,
		// G=done
		public enum Status {
			O, W, C, X, G
		}

		private static Random rand = new Random(System.currentTimeMillis());

		private int id;
		private Status status = Status.O;
		private BarberSolved barber;

		public Customer(int id, BarberSolved barber) {
			this.id = id;
			this.barber = barber;
		}

		@Override
		public void run() {
			try {
				// wait before trying to get a hair cut
				Thread.sleep(1000);

				// this ensures that no customer leaves (status of X) as they
				// will be blocked on arrival if there is no room in the waiting
				// room.
				barber.acquireHaircut(this);
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
			if (status == Status.G)
				barber.releaseHaircut(getCustomerName());
		}

		/**
		 * @return the name
		 */
		public String getCustomerName() {
			return "C" + id;
		}
	}

	public static void main(String[] args) {
		BarberSolved barber = new BarberSolved();
		for (int n = 0; n < 20; n++) {
			Customer c = new Customer(n, barber);
			c.start();
		}

		barber.run();
	}
}
