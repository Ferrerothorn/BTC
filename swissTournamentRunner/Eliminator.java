package swissTournamentRunner;

import java.util.ArrayList;

public class Eliminator {

	ArrayList<Player> players = new ArrayList<>();
	Tournament t = new Tournament();
	
	public Eliminator(ArrayList<Player> players, Tournament t) {
		this.players = players;
		this.t= t;
	}

	public void eliminate() {
		t.sortRankings();
		ArrayList<Player> cull = new ArrayList<>();
		for (Player p : players) {
			if (p.getOpponentsList().size() - p.getListOfVictories().size() >= t.x_elimination) {
				cull.add(p);
			}
		}
		for (Player p : cull) {
			players.remove(p);
		}
		if (players.size() == 1) {
			t.abort();
		} else {
			t.dropPlayer("BYE");
			t.addBye();
		}
	}

}
