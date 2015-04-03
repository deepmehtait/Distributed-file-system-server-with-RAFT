package gash.consensus.byzantine;

import gash.consensus.byzantine.ByzantineGenerals.Command;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ByzantineGeneralsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test3and1() throws Exception {
		// minimum number of nodes to succeed
		ByzantineGenerals bg = new ByzantineGenerals(3, 1);

		bg.issueCommand(Command.Attack);
		bg.showResults();
	}

	@Test
	public void test2and1() throws Exception {

		// will fail
		ByzantineGenerals bg = new ByzantineGenerals(2, 1);
		// ByzantineGenerals bg = new ByzantineGenerals(4, 2);

		bg.issueCommand(Command.Attack);
		bg.showResults();
	}

	@Test
	public void test10and2() throws Exception {
		// will succeed
		ByzantineGenerals bg = new ByzantineGenerals(10, 2);

		bg.issueCommand(Command.Attack);
		bg.showResults();
	}
}
