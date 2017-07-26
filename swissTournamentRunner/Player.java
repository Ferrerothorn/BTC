package swissTournamentRunner;

import java.util.ArrayList;

public class Player implements Comparable<Player> {

	String name;
	int score = 0;
	int tb = 0;
	int oppWr = 0;
	int oppOppWr = 0;
	int oppositionTBSum = 0;
	int lastDocumentedPosition = 0;
	public ArrayList<Player> previousRounds = new ArrayList<>();
	ArrayList<Player> victories = new ArrayList<>();

	public Player(String string) {
		name = string;
	}

	public Player(String myName, int myScore, int myTb, int myOppWr, int myOppOppWr) {
		name = myName;
		score = myScore;
		tb = myTb;
		oppWr = myOppWr;
		oppOppWr = myOppOppWr;
	}

	public void updatePositionInRankings(ArrayList<Player> players) {

		for (int i = 0; i < players.size(); i++) {
			if (this == players.get(i)) {
				lastDocumentedPosition = i + 1;
			}
		}
	}

	public void recalculateOppWr() {
		Double opponentWinRate = 0.0;
		int people = 0;
		for (Player p : previousRounds) {
			opponentWinRate += (double) p.victories.size() / p.previousRounds.size();
			people++;
		}
		if (people != 0) {
			opponentWinRate /= previousRounds.size();
			opponentWinRate *= 100;
		}
		oppWr = opponentWinRate.intValue();

	}

	public void recalculateOppOppWr() {
		Double opponentOpponentWinRate = 0.0;
		int people = 0;
		for (Player p : previousRounds) {
			for (Player q : p.previousRounds) {
				if (this != q) {
					opponentOpponentWinRate += (double) q.victories.size() / q.previousRounds.size();
					people++;
				}
			}
		}
		if (people != 0) {
			opponentOpponentWinRate = (double) (opponentOpponentWinRate / people);
			opponentOpponentWinRate *= 100;
		}

		oppOppWr = opponentOpponentWinRate.intValue();
	}

	@Override
	public int compareTo(Player p) {
		if (this.name.equals("BYE")) {
			return 1;
		} else if (p.getName().equals("BYE")) {
			return -1;
		} else if (this.score > p.getScore()) {
			return -1;
		} else if (this.score < p.getScore()) {
			return 1;
		} else if (this.tb > p.getTB()) {
			return -1;
		} else if (this.tb < p.getTB()) {
			return 1;
		} else if (this.oppWr > p.getOppWr()) {
			return -1;
		} else if (this.oppWr < p.getOppWr()) {
			return 1;
		} else if (this.oppOppWr > p.getOppOppWr()) {
			return -1;
		} else if (this.oppOppWr < p.getOppOppWr()) {
			return 1;
		} else if (this.oppositionTBSum > p.oppositionTBSum) {
			return -1;
		} else if (this.oppositionTBSum < p.oppositionTBSum) {
			return 1;
		}
		return 0;
	}

	public void recalculateOppositionTBSum() {
		oppositionTBSum = 0;
		for (Player p : previousRounds) {
			oppositionTBSum += p.getTB();
		}
	}
	
	public int getTB() {
		return tb;
	}

	public int getScore() {
		return score;
	}

	public String getName() {
		return name;
	}

	public void beats(Player p2) {
		this.logOpponent(p2);
		p2.logOpponent(this);
		this.victories.add(p2);
		this.recalculateScore();
		p2.recalculateScore();
	}

	public void tied(Player p2) {
		this.logOpponent(p2);
		p2.logOpponent(this);
		this.recalculateScore();
		p2.recalculateScore();
	}

	private void logOpponent(Player foe) {
		this.previousRounds.add(foe);
	}

	public ArrayList<Player> getOpponentsList() {
		return previousRounds;
	}

	public ArrayList<Player> getListOfVictories() {
		return victories;
	}

	public void recalculateTB() {
		tb = 0;
		for (Player p : victories) {
			if (p.getScore() == this.score) {
				tb++;
			}
		}
	}

	public int getOppWr() {
		return oppWr;
	}

	public int getOppOppWr() {
		return oppOppWr;
	}

	public int getPositionInRankings() {
		return lastDocumentedPosition;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public void recalculateScore() {
		score = (3 * victories.size());
		for (Player p : previousRounds) {
			if (!victories.contains(p) && !p.victories.contains(this) && p.previousRounds.contains(this)) {
				score++;
			}
		}
	}

	public ArrayList<String> getListOfNamesPlayed() {
		ArrayList<String> namesPlayed = new ArrayList<>();
		for (Player p : getOpponentsList()) {
			namesPlayed.add(p.getName());
		}
		return namesPlayed;
	}

	public ArrayList<String> getListOfNamesBeaten() {
		ArrayList<String> namesBeaten = new ArrayList<>();
		for (Player p : getListOfVictories()) {
			namesBeaten.add(p.getName());
		}
		return namesBeaten;
	}

	public void addToListOfVictories(Player beaten) {
		victories.add(beaten);
	}

	public void addToListOfPlayed(Player played) {
		previousRounds.add(played);
	}

}