package swissTournamentRunner;

import java.util.ArrayList;

public class Utils {

	public static void autocompleteRound(ArrayList<Battle> battles) {
		while (battles.size() > 0) {
			handleBattleWinner(battles.remove(0), "1");
		}
	}

	public static void handleBattleWinner(Battle b, String winner) {
		if (b.getP1().getName().equals("BYE")) {
			b.getP2().beats(b.getP1());
			b = null;
			return;
		} else if (b.getP2().getName().equals("BYE")) {
			b.getP1().beats(b.getP2());
			b = null;
			return;
		} else {
			switch (winner) {
			case "1":
				b.getP1().beats(b.getP2());
				b = null;
				break;
			case "2":
				b.getP2().beats(b.getP1());
				b = null;
				break;
			case "0":
				b.getP1().tied(b.getP2());
				b = null;
				break;
			default:
				break;
			}
		}
	}

	public static String trimWhitespace(String s) {
		if (s.length() == 0) {
			return s;
		}
		if (s.charAt(0) == ' ' || s.charAt(0) == '\t') {
			return trimWhitespace(s.substring(1));
		}
		if (s.charAt(s.length() - 1) == ' ' || s.charAt(s.length() - 1) == '\t') {
			return trimWhitespace(s.substring(0, s.length() - 1));
		}
		return s;
	}

	public static void print() {
		GUI.postString("");
	}

	public static void print(String string) {
		GUI.postString(string);
	}

	public static String rpad(String inStr, int finalLength) {
		return (inStr
				+ "                                                                                                                          ")
						.substring(0, finalLength);
	}

	public static void reportWinnerByName(String winner, String loser, ArrayList<Battle> currentBattles) {
		for (Battle b : currentBattles) {
			if (b.getP1().getName().equals(winner) && b.getP2().getName().equals(loser) && winner != "BYE") {
				handleBattleWinner(b, "1");
				currentBattles.remove(b);
				break;
			}
			if (b.getP2().getName().equals(winner) && b.getP1().getName().equals(loser) && winner != "BYE") {
				handleBattleWinner(b, "2");
				currentBattles.remove(b);
				break;
			}
		}
		if (winner == "BYE") {
			reportWinnerByName(loser, winner, currentBattles);
		}
	}

	public static String eloBuilder(Battle b) {
		return "[" + b.getElo(b.getP1()) + "% - " + b.getElo(b.getP2()) + "%]";
	}

	public static boolean dropPlayerMidBattle(String nameToDrop, ArrayList<Battle> currentBattles,
			ArrayList<String> dropped) {
		for (Battle b : currentBattles) {
			if (b.getP1().getName().equals(nameToDrop) && b.getP2().getName().equals("BYE")) {
				currentBattles.remove(b);
				Player toDropZone = b.getP1();
				dropped.add(toDropZone.getName());
				Player bye = b.getP2();
				toDropZone.beats(bye);
				dropped.add("BYE");
				return true;
			} else if (b.getP2().getName().equals(nameToDrop) && b.getP1().getName().equals("BYE")) {
				currentBattles.remove(b);
				Player toDropZone = b.getP2();
				dropped.add(toDropZone.getName());
				Player bye = b.getP1();
				toDropZone.beats(bye);
				dropped.add("BYE");
				return true;
			} else if (b.getP1().getName().equals(nameToDrop)) {
				currentBattles.remove(b);
				Player toDropZone = b.getP1();
				b.getP2().beats(toDropZone);
				dropped.add(b.getP1().getName());
				return true;
			} else if (b.getP2().getName().equals(nameToDrop)) {
				currentBattles.remove(b);
				Player toDropZone = b.getP2();
				b.getP1().beats(toDropZone);
				dropped.add(b.getP2().getName());
				return true;
			}
		}
		return false;
	}

	public static boolean dropPlayerOutsideBattle(ArrayList<Player> players, String nameToDrop,
			ArrayList<Battle> currentBattles, ArrayList<String> dropped) {
		for (Player p : players) {
			if (p.getName().equals(nameToDrop)) {
				dropped.add(p.getName());
				return true;
			}
		}
		return false;
	}

}
