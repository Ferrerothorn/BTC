package swissTournamentRunner;

public class Battle implements Comparable<Battle> {

	Player p1;
	Player p2;
	int tableNumber;
	public boolean wasSeeded = false;

	public Battle(Player myP1, Player myP2) {
		p1 = myP1;
		p2 = myP2;
	}

	public Player getP1() {
		return p1;
	}

	public Player getP2() {
		return p2;
	}

	public void setTableNumber(int tN) {
		tableNumber = tN;
	}

	public int getTableNumber() {
		return tableNumber;
	}

	public boolean contains(Player winner) {
		if (p1 == winner || p2 == winner) {
			return true;
		}
		return false;
	}

	public int getElo(Player p) {
		Player opponent = otherPlayer(p);
		float ourEloScore = p.getOppWr() * p.getScore();
		ourEloScore++;
		float theirEloScore = opponent.getOppWr() * opponent.getScore();
		theirEloScore++;
		float ourElo = 1;
		double power = (theirEloScore - ourEloScore) / 400;
		power = Math.pow(10, power);
		ourElo += power;
		ourElo = 1 / ourElo;
		ourElo *= 100;
		ourElo = Math.round(ourElo);
		return (int) ourElo;
	}

	public Player otherPlayer(Player p) {
		if (p.equals(p1)) {
			return p2;
		}
		return p1;
	}

	@Override
	public int compareTo(Battle compareTo) {
		if (this.shoeInFactor() > compareTo.shoeInFactor()) {
			return -1;
		}
		return 1;
	}

	private int shoeInFactor() {
		return Math.abs(getElo(p1) - getElo(p2));
	}
}
