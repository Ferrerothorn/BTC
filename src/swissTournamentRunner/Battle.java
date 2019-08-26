package swissTournamentRunner;

public class Battle implements Comparable<Battle> {

	Player p1;
	Player p2;
	int tableNumber;
	public boolean wasSeeded = false;
	int p1DealtDamage = 0;
	int p2DealtDamage = 0;

	public int getP1Damage() {
		return p1DealtDamage;
	}

	public int getP2Damage() {
		return p2DealtDamage;
	}

	public String toString() {
		return p1.getName() + "," + p2.getName() + ";" + p1DealtDamage + "," + p2DealtDamage + "/";
	}

	public Battle(Player myP1, Player myP2) {
		p1 = myP1;
		p2 = myP2;
	}

	public Battle(Player myP1, Player myP2, int i, int j) {
		p1 = myP1;
		p2 = myP2;
		p1DealtDamage = i;
		p2DealtDamage = j;
	}

	public Player getP1() {
		return p1;
	}

	public Player getP2() {
		return p2;
	}

	public void setPlayer(String s, Player p) {
		if (s.contains("1")) {
			this.p1 = p;
		} else {
			this.p2 = p;
		}
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
		if (p.getName().equals("BYE")) {
			return 0;
		}
		if (opponent.getName().equals("BYE")) {
			return 100;
		}
		float ourEloScore = (p.getOppWr() * p.getScore()) + 1;
		float theirEloScore = (opponent.getOppWr() * opponent.getScore()) + 1;
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
		if (this.shoeInFactor() >= compareTo.shoeInFactor()) {
			return 1;
		}
		return -1;
	}

	private int shoeInFactor() {
		return Math.abs(getElo(p1) - getElo(p2));
	}

	public boolean contains(String name) {
		return p1.getName().toLowerCase().contains(name) || p2.getName().toLowerCase().contains(name);
	}

	public void setP1Damage(int parseInt) {
		p1DealtDamage = parseInt;
	}

	public void setP2Damage(int parseInt) {
		p2DealtDamage = parseInt;
	}
}
