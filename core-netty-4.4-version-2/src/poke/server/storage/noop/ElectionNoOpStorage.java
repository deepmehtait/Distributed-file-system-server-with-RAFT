package poke.server.storage.noop;

import java.util.List;

import poke.core.Mgmt.LeaderElection;
import poke.server.storage.ElectionStorage;

public class ElectionNoOpStorage implements ElectionStorage {

	@Override
	public boolean addElection(LeaderElection le) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateElection(LeaderElection le) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeElection(long electionId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> findElections(long electionId) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
