package swissTournamentRunner;

import java.util.ArrayList;

public class Utils {

	public static void autocompleteRound(ArrayList<Battle> battles) {
		while (battles.size() > 0) {
			handleBattleWinner(battles.remove(0), "1");
		}
	}

	public static void handleBattleWinner(Battle b, String winner) {
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
}
