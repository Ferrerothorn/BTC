package swissTournamentRunner;

import java.util.ArrayList;

public class Player implements Comparable<Player> {

	String name;
	String winPattern;
	int score = 0;
	int oppWr = 0;
	int damageDealt = 0;
	int damageReceived = 0;
	public int lastDocumentedPosition = 0;
	public ArrayList<Player> previousRounds = new ArrayList<>();
	ArrayList<Player> victories = new ArrayList<>();

	public Player(String string) {
		name = string;
	}

	public Player(String myName, int myScore, int myTb, int myOppWr, int myOppOppWr) {
		name = myName;
		score = myScore;
		oppWr = myOppWr;
	}

	public void updatePositionInRankings(ArrayList<Player> players) {
		for (int i = 0; i < players.size(); i++) {
			if (this == players.get(i)) {
				lastDocumentedPosition = i + 1;
			}
		}
	}

	public void updateWinPattern() {
		winPattern = "";
		for (Player p : previousRounds) {
			if (victories.contains(p)) {
				winPattern += "W";
			} else {
				if (!p.victories.contains(this)) {
					winPattern += "T";
				} else {
					winPattern += "L";
				}
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

	@Override
	public int compareTo(Player p) {

		if (this.lastDocumentedPosition == 0 && p.lastDocumentedPosition == 0) {
			if (this.name.compareTo(p.name) < 0) {
				return -1;
			}
			return 1;
		} else if (this.name.equals("BYE")) {
			return 1;
		} else if (p.getName().equals("BYE")) {
			return -1;
		} else if (this.score > p.getScore()) {
			return -1;
		} else if (this.score < p.getScore()) {
			return 1;
		} else if (this.oppWr > p.getOppWr()) {
			return -1;
		} else if (this.oppWr < p.getOppWr()) {
			return 1;
		} else if (this.damageDealt > p.damageDealt) {
			return -1;
		} else if (this.damageDealt < p.damageDealt) {
			return 1;
		} else if (this.damageReceived < p.damageReceived) {
			return -1;
		} else if (this.damageReceived > p.damageReceived) {
			return 1;
		}
		return 0;
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

	public int getOppWr() {
		return oppWr;
	}

	public int getPositionInRankings() {
		return lastDocumentedPosition;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public void recalculateScore() {
		score = (3 * victories.size());
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

	public void updateParticipantStats(ArrayList<Battle> bs) {
		recalculateScore();
		recalculateOppWr();
		damageDealt = totalDamageDealt(bs);
		damageReceived = totalDamageReceived(bs);
		updateWinPattern();
	}

	public int totalDamageDealt(ArrayList<Battle> bs) {
		int total = 0;
		for (Battle b : bs) {
			if (b.getP1().equals(this)) {
				total += b.getP1Damage();
			}
			if (b.getP2().equals(this)) {
				total += b.getP2Damage();
			}
		}
		return total;
	}

	public int totalDamageReceived(ArrayList<Battle> bs) {
		int total = 0;
		for (Battle b : bs) {
			if (b.getP1().equals(this)) {
				total += b.getP2Damage();
			}
			if (b.getP2().equals(this)) {
				total += b.getP1Damage();
			}
		}
		return total;
	}

}