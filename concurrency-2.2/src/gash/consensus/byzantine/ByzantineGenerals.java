package gash.consensus.byzantine;

import java.util.Random;

/**
 * implementation of (solution to) the Byzantine Generals problem as proposed by
 * Lamport et. al.
 * 
 * Description:
 * 
 * You have a general issuing commands with n lieutenants implementing the
 * command or a system with n+1 nodes. A number of lieutenants (t) are traitors
 * (send wrong message or doesn't send a message). What is the tolerance to
 * traitors that a system can support in order to reach consensus?
 * 
 * This implementation includes the addition of three conditions (last three
 * below) to ensure signed messages that allows the problem to be solved with
 * 2(t) + 1 commanders (nodes).
 * 
 * Conditions:
 * <ol>
 * <li>All loyal lieutenants obey the same order.
 * <li>If the commanding general is loyal, then every loyal lieutenant obeys the
 * order he sends.
 * <li>Every message that is sent is delivered correctly.
 * <li>The receiver of a message knows who sent it (no forgery allowed).
 * <li>The absence of a message can be detected.
 * </ol>
 * 
 * What we see is that in order to reach consensus amongst the lieutenants with
 * (t) traitors there must be 2(t) + 1 total commanders/nodes (a general + lts)
 * to reach a majority.
 * 
 * @author gash1
 * 
 */
public class ByzantineGenerals {
	public enum Command {
		Confusion, Retreat, Attack
	}

	public enum TraitorEffect {
		WrongMessage, NoMessage
	}

	private int msgCount;
	private Lieutenant[] lieutenants;
	private General general;

	public ByzantineGenerals(int numLt, int numTraitors) {
		general = new General();

		lieutenants = new Lieutenant[numLt];
		for (int n = 0; n < numLt; n++)
			lieutenants[n] = new Lieutenant(n, numLt);

		if (numTraitors > 0) {
			Random rand = new Random(System.currentTimeMillis());
			while (true) {
				int i = rand.nextInt(numLt);
				if (lieutenants[i].isLoyal()) {
					lieutenants[i].setTraitorBehavior(TraitorEffect.WrongMessage);
					numTraitors--;
				}

				if (numTraitors == 0)
					break;
			}
		}
	}

	public void issueCommand(Command cmd) {
		general.sendCommand(cmd);
	}

	void courier(Command cmd, int fromId) {
		for (Lieutenant lt : lieutenants) {
			msgCount++;
			lt.message(cmd, fromId);
		}
	}

	public void showResults() {
		int retreat = 0, attack = 0;
		for (Lieutenant lt : lieutenants) {
			if (lt.getCmd() == Command.Retreat)
				retreat++;
			else
				attack++;
		}

		System.out.println("------------------------------------------------------");
		System.out.println("Number of messages n(n+1): " + msgCount);
		System.out.println("Consensus: attack = " + attack + ", retreat = " + retreat);
		System.out.println(general.toString());
		for (Lieutenant lt : lieutenants)
			System.out.println(lt.toString());
		System.out.println("------------------------------------------------------");
	}

	public class General {
		private Command cmd;

		public General() {
		}

		public void sendCommand(Command cmd) {
			this.cmd = cmd;
			courier(cmd, -1);
		}

		public String toString() {
			if (cmd != null)
				return "General gave the command to " + cmd;
			else
				return "General issued no command";
		}
	}

	public class Lieutenant {
		private TraitorEffect traitorBehavior;

		private int id;
		private Command cmd = Command.Retreat;

		// an array is used as a simple signing (indexed using IDs). Without
		// this and forgery enabled, we could not detect fake messages sent from
		// traitors impersonating other lieutenants.
		private Command[] otherLts;

		public Lieutenant(int id, int numLt) {
			this.id = id;
			otherLts = new Command[numLt];
		}

		public void message(Command cmd, int fromId) {
			if (fromId >= 0) {
				otherLts[fromId] = cmd;
				// System.out.println("--> got " + cmd + " from Lt" +fromId);
			} else {
				// a message from the general
				if (isLoyal())
					this.cmd = cmd;
				else if (traitorBehavior == TraitorEffect.WrongMessage) {
					System.out.println("--> sending fake message");
					if (cmd == Command.Attack)
						this.cmd = Command.Retreat;
					else
						this.cmd = Command.Attack;
				} else
					return; // TraitorEffect.NoMessage

				// propagate to others in the field
				courier(this.cmd, id);
			}
		}

		public Command consensus() {
			int retreat = 0, attack = 0;
			for (Command c : otherLts) {
				if (c == null)
					;
				else if (c == Command.Retreat)
					retreat++;
				else
					attack++;
			}

			// since we are looking at a simple majority, the case where no
			// message is sent is the same as if the wrong message was sent
			if (attack > retreat)
				return Command.Attack;
			else if (retreat > attack)
				return Command.Retreat;
			else
				return Command.Confusion;
		}

		public String toString() {
			if (cmd != null && traitorBehavior == null)
				return "Lieutentant (Loyal) sends " + cmd + ", the field consensus is " + consensus();
			else if (cmd != null)
				return "Lieutentant (" + traitorBehavior + ") sends " + cmd + ", the field consensus is " + consensus();
			else
				return "Lieutentant recieved no command";
		}

		public boolean isLoyal() {
			return traitorBehavior == null;
		}

		public void setTraitorBehavior(TraitorEffect traitorBehavior) {
			this.traitorBehavior = traitorBehavior;
		}

		/**
		 * @return the cmd
		 */
		public Command getCmd() {
			return cmd;
		}
	}
}
