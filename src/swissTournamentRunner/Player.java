package swissTournamentRunner;

import java.util.ArrayList;

public class Player implements Comparable<Player> {

	String name;
	int cubeScore = 0;
	int swissScore = 0;
	int overallScore = 0;

	int cubeOWR = 0;
	int swissOWR = 0;
	double overallOWR = 0;

	int swissRounds = 0;
	int podNumber;

	private int damageDealt = 0;
	private int damageReceived = 0;
	public int lastDocumentedPosition = 0;
	public ArrayList<Player> previousRounds = new ArrayList<>();
	ArrayList<Player> victories = new ArrayList<>();

	public Player(String myName, int myScore, int myOWR, int pod, int swissRds) {
		name = myName;
		swissScore = myScore;
		swissOWR = myOWR;
		swissRounds = swissRds;
		podNumber = pod;
	}

	public Player(String myName, int myScore, int myOWR, int myDamDealt, int myDamReceived, int pod) {
		name = myName;
		swissScore = myScore;
		swissOWR = myOWR;
		damageDealt = myDamDealt;
		damageReceived = myDamReceived;
		podNumber = pod;
	}

	public void updatePositionInRankings(ArrayList<Player> players) {
		for (int i = 0; i < players.size(); i++) {
			if (this == players.get(i)) {
				lastDocumentedPosition = i + 1;
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
			cubeOWR = (100 * winsActuallyAttained) / maxWinsPossible;
		} else {
			cubeOWR = 0;
		}
		factorInSwissPerformance(cubeOWR);
	}

	private void factorInSwissPerformance(int cubeOWR) {
		if (cubeOWR == 0) {
			overallOWR = swissOWR;
		} else {
			overallOWR = swissOWR;
			overallOWR *= swissRounds;
			overallOWR /= 100;
			overallOWR += ((cubeOWR * 3) / 100);
			overallOWR *= 10;
			round(overallOWR, 2);
		}
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
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
		} else if (this.getScore() > p.getScore()) {
			return -1;
		} else if (this.getScore() < p.getScore()) {
			return 1;
		} else if (this.overallOWR > p.getOppWr()) {
			return -1;
		} else if (this.overallOWR < p.getOppWr()) {
			return 1;
		} else if (this.getDamageDealt() > p.getDamageDealt()) {
			return -1;
		} else if (this.getDamageDealt() < p.getDamageDealt()) {
			return 1;
		} else if (this.getDamageReceived() < p.getDamageReceived()) {
			return -1;
		} else if (this.getDamageReceived() > p.getDamageReceived()) {
			return 1;
		}
		return 0;
	}

	public int getScore() {
		return (cubeScore + swissScore);
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

	public double getOppWr() {
		return overallOWR;
	}

	public int getPositionInRankings() {
		return lastDocumentedPosition;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public void recalculateScore() {
		cubeScore = victories.size()*3;
		overallScore = swissScore + cubeScore;
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
		setDamageDealt(totalDamageDealt(bs));
		setDamageReceived(totalDamageReceived(bs));
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

	public int getDamageReceived() {
		return damageReceived;
	}

	public void setDamageReceived(int damageReceived) {
		this.damageReceived = damageReceived;
	}

	public int getDamageDealt() {
		return damageDealt;
	}

	public void setDamageDealt(int damageDealt) {
		this.damageDealt = damageDealt;
	}

	public Object getPod() {
		return podNumber;
	}
}