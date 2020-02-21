package swissTournamentRunner;

public class Battle {

	Player p1;
	Player p2;
	int tableNumber;
	int podNumber;
	public boolean wasSeeded = false;
	private int p1DealtDamage = 0;
	private int p2DealtDamage = 0;

	public Battle(Player myP1, Player myP2) {
		p1 = myP1;
		p2 = myP2;
	}

	public Battle(Player myP1, Player myP2, int i, int j) {
		p1 = myP1;
		p2 = myP2;
		setP1DealtDamage(i);
		setP2DealtDamage(j);
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
	
	public int getPodNumber() {
		return podNumber;
	}

	public void setPodNumber(int podNumber) {
		this.podNumber = podNumber;
	}

	
	public int getP1Damage() {
		return getP1DealtDamage();
	}

	public int getP2Damage() {
		return getP2DealtDamage();
	}

	public String toString() {
		return p1.getName() + "," + p2.getName() + ";" + getP1DealtDamage() + "," + getP2DealtDamage() + "/";
	}

	public Player otherPlayer(Player p) {
		if (p.equals(p1)) {
			return p2;
		}
		return p1;
	}

	public boolean contains(String name) {
		return p1.getName().toLowerCase().contains(name) || p2.getName().toLowerCase().contains(name);
	}

	public void setP1Damage(int parseInt) {
		setP1DealtDamage(parseInt);
	}

	public void setP2Damage(int parseInt) {
		setP2DealtDamage(parseInt);
	}

	public int getP1DealtDamage() {
		return p1DealtDamage;
	}

	public void setP1DealtDamage(int p1DealtDamage) {
		this.p1DealtDamage = p1DealtDamage;
	}

	public int getP2DealtDamage() {
		return p2DealtDamage;
	}

	public void setP2DealtDamage(int p2DealtDamage) {
		this.p2DealtDamage = p2DealtDamage;
	}
}
