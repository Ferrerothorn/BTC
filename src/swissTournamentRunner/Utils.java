package swissTournamentRunner;

import java.util.Random;

public class Utils {

	public static void handleBattleWinner(Battle b, String whichPlayerWon) {
		switch (whichPlayerWon) {
			case "1" -> b.getP1().beats(b.getP2());
			case "2" -> b.getP2().beats(b.getP1());
			case "0" -> b.getP1().tied(b.getP2());
			default -> {
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

	public static void print(String string) {
		GUI.postString(string);
	}

	public static String rpad(String inStr, int finalLength) {
		return (inStr
				+ "                                                                                                                          ")
						.substring(0, finalLength);
	}

	public static String sanitise(String sanitiseThis) {
		String processedName = sanitiseThis;
		processedName = processedName.replaceAll("�", "A");
		processedName = processedName.replaceAll("�", "a");
		processedName = processedName.replaceAll("�", "A");	
		processedName = processedName.replaceAll("�", "a");
		processedName = processedName.replaceAll("�", "A");	
		processedName = processedName.replaceAll("�", "a");
		processedName = processedName.replaceAll("�", "A");
		processedName = processedName.replaceAll("�", "a");
		processedName = processedName.replaceAll("�", "A");
		processedName = processedName.replaceAll("�", "a");
		processedName = processedName.replaceAll("�", "C");
		processedName = processedName.replaceAll("�", "c");
		processedName = processedName.replaceAll("�", "E");
		processedName = processedName.replaceAll("�", "e");
		processedName = processedName.replaceAll("�", "E");
		processedName = processedName.replaceAll("�", "e");
		processedName = processedName.replaceAll("�", "E");
		processedName = processedName.replaceAll("�", "e");
		processedName = processedName.replaceAll("�", "E");
		processedName = processedName.replaceAll("�", "e");
		processedName = processedName.replaceAll("�", "I");
		processedName = processedName.replaceAll("�", "i");
		processedName = processedName.replaceAll("�", "I");
		processedName = processedName.replaceAll("�", "i");
		processedName = processedName.replaceAll("�", "I");
		processedName = processedName.replaceAll("�", "i");
		processedName = processedName.replaceAll("�", "I");
		processedName = processedName.replaceAll("�", "i");
		processedName = processedName.replaceAll("�", "N");
		processedName = processedName.replaceAll("�", "n");
		processedName = processedName.replaceAll("�", "O");
		processedName = processedName.replaceAll("�", "o");
		processedName = processedName.replaceAll("�", "O");
		processedName = processedName.replaceAll("�", "o");
		processedName = processedName.replaceAll("�", "O");
		processedName = processedName.replaceAll("�", "o");
		processedName = processedName.replaceAll("�", "O");
		processedName = processedName.replaceAll("�", "o");
		processedName = processedName.replaceAll("�", "O");
		processedName = processedName.replaceAll("�", "o");
		processedName = processedName.replaceAll("�", "S");
		processedName = processedName.replaceAll("�", "s");
		processedName = processedName.replaceAll("�", "U");
		processedName = processedName.replaceAll("�", "u");
		processedName = processedName.replaceAll("�", "U");
		processedName = processedName.replaceAll("�", "u");
		processedName = processedName.replaceAll("�", "U");
		processedName = processedName.replaceAll("�", "u");
		processedName = processedName.replaceAll("�", "U");
		processedName = processedName.replaceAll("�", "u");
		processedName = processedName.replaceAll("�", "Y");
		processedName = processedName.replaceAll("�", "y");
		processedName = processedName.replaceAll("�", "Y");
		processedName = processedName.replaceAll("�", "y");
		processedName = processedName.replaceAll("�", "Z");
		processedName = processedName.replaceAll("�", "z");
		return trimWhitespace(processedName);
	}

	public static Player getRandomPlayer(Tournament t) {
		Random r = new Random();
		int rand = r.nextInt(t.getPlayers().size());
		Player p = t.getPlayers().get(rand);
		if (p.isDropped() || p.getName().equals("BYE")) {
			return getRandomPlayer(t);
		}
		return p;
	}
	
}
