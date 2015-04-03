package poke.client.util;

import poke.comm.App.Header;
import poke.comm.App.JobDesc;
import poke.comm.App.NameValueSet;
import poke.comm.App.Ping;
import poke.comm.App.PokeStatus;
import poke.util.PrintNode;

public class ClientUtil {

	public static void printJob(JobDesc job) {
		if (job == null) {
			System.out.println("job is null");
			return;
		}

		if (job.hasNameSpace())
			System.out.println("NameSpace: " + job.getNameSpace());

		if (job.hasJobId()) {
		}

		if (job.hasStatus()) {
			System.out.println("Status:    " + job.getStatus());
		}

		if (job.hasOptions()) {
			NameValueSet nvs = job.getOptions();
			PrintNode.print(nvs);
		}
	}

	public static void printPing(Ping f) {
		if (f == null) {
			System.out.println("ping is null");
			return;
		}

		System.out.println("Poke: " + f.getTag() + " - " + f.getNumber());
	}

	public static void printHeader(Header h) {
		System.out.println("-------------------------------------------------------");
		System.out.println("Header");
		System.out.println(" - Orig   : " + h.getOriginator());
		System.out.println(" - Req ID : " + h.getRoutingId());
		System.out.println(" - Tag    : " + h.getTag());
		System.out.println(" - Time   : " + h.getTime());
		System.out.println(" - Status : " + h.getReplyCode());
		if (h.getReplyCode().getNumber() != PokeStatus.SUCCESS_VALUE)
			System.out.println(" - Re Msg : " + h.getReplyMsg());

		System.out.println("");
	}

}
