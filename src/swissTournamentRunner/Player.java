package swissTournamentRunner;

import java.util.ArrayList;

public class Player implements Comparable<Player> {

	String name;
	private String winPattern;
	int score = 0;
	int oppWr = 0;
	public int damageDealt = 0;
	private int damageReceived = 0;
	private int oppDamageReceived = 0;
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

	public int getDamageReceived() {
		return damageReceived;
	}

	public int getOppDamageReceived() {
		return oppDamageReceived;
	}

	public void updatePositionInRankings(ArrayList<Player> players) {
		for (int i = 0; i < players.size(); i++) {
			if (this == players.get(i)) {
				lastDocumentedPosition = i + 1;
			}
		}
	}

	public void updateWinPattern() {
		setWinPattern("");
		for (Player p : previousRounds) {
			if (victories.contains(p)) {
				setWinPattern(getWinPattern() + "W");
			} else {
				if (!p.victories.contains(this)) {
					setWinPattern(getWinPattern() + "T");
				} else {
					setWinPattern(getWinPattern() + "L");
				}
			}
		}
	}

	public void recalculateOppWr() {
		int maxWinsPossible = 0;
		for (Player p : previousRounds) {
			maxWinsPossible += p.getListOfNamesPlayed().size();
		}
		int winsActuallyAttained = 0;
		for (Player p : previousRounds) {
			winsActuallyAttained += p.getListOfNamesBeaten().size();
		}
		if (maxWinsPossible > 0) {
			oppWr = (100 * winsActuallyAttained) / maxWinsPossible;
		} else {
			oppWr = 0;
		}
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
		} else if (this.getDamageReceived() < p.getDamageReceived()) {
			return -1;
		} else if (this.getDamageReceived() > p.getDamageReceived()) {
			return 1;
		} else if (this.getOppDamageReceived() < p.getOppDamageReceived()) {
			return -1;
		} else if (this.getOppDamageReceived() > p.getOppDamageReceived()) {
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

	private void recalculateDamageDealt(ArrayList<Battle> bs) {
		damageDealt = 0;
		for (Battle b : bs) {
			if (b.getP1() == this) {
				damageDealt += b.getP1DamageDealt();
			} else if (b.getP2() == this) {
				damageDealt += b.getP2DamageDealt();
			}
		}
	}

	private void recalculateDamageReceived(ArrayList<Battle> bs) {
		damageReceived = 0;
		for (Battle b : bs) {
			if (b.getP1() == this) {
				damageReceived += b.getP2DamageDealt();
			} else if (b.getP2() == this) {
				damageReceived += b.getP1DamageDealt();
			}
		}
	}

	public void recalculateOppDamageReceived(ArrayList<Battle> bs) {
		oppDamageReceived = 0;
		for (Player p : previousRounds) {
			for (Battle b : bs) {
				if (b.getP1() == p) {
					oppDamageReceived += b.getP2DamageDealt();
				} else if (b.getP2() == p) {
					oppDamageReceived += b.getP1DamageDealt();
				}
			}
		}
	}

	public String getWinPattern() {
		return winPattern;
	}

	public void setWinPattern(String winPattern) {
		this.winPattern = winPattern;
	}

	public void updateParticipantStats(ArrayList<Battle> bs) {
		recalculateScore();
		recalculateOppWr();
		recalculateDamageDealt(bs);
		recalculateDamageReceived(bs);
		updateWinPattern();
	}
}