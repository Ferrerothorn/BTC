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
		processedName = processedName.replaceAll("À", "A");
		processedName = processedName.replaceAll("à", "a");
		processedName = processedName.replaceAll("Á", "A");	
		processedName = processedName.replaceAll("á", "a");
		processedName = processedName.replaceAll("Â", "A");	
		processedName = processedName.replaceAll("â", "a");
		processedName = processedName.replaceAll("Ã", "A");
		processedName = processedName.replaceAll("ã", "a");
		processedName = processedName.replaceAll("Ä", "A");
		processedName = processedName.replaceAll("ä", "a");
		processedName = processedName.replaceAll("Ç", "C");
		processedName = processedName.replaceAll("ç", "c");
		processedName = processedName.replaceAll("È", "E");
		processedName = processedName.replaceAll("è", "e");
		processedName = processedName.replaceAll("É", "E");
		processedName = processedName.replaceAll("é", "e");
		processedName = processedName.replaceAll("Ê", "E");
		processedName = processedName.replaceAll("ê", "e");
		processedName = processedName.replaceAll("Ë", "E");
		processedName = processedName.replaceAll("ë", "e");
		processedName = processedName.replaceAll("Ì", "I");
		processedName = processedName.replaceAll("ì", "i");
		processedName = processedName.replaceAll("Í", "I");
		processedName = processedName.replaceAll("í", "i");
		processedName = processedName.replaceAll("Î", "I");
		processedName = processedName.replaceAll("î", "i");
		processedName = processedName.replaceAll("Ï", "I");
		processedName = processedName.replaceAll("ï", "i");
		processedName = processedName.replaceAll("Ñ", "N");
		processedName = processedName.replaceAll("ñ", "n");
		processedName = processedName.replaceAll("Ò", "O");
		processedName = processedName.replaceAll("ò", "o");
		processedName = processedName.replaceAll("Ó", "O");
		processedName = processedName.replaceAll("ó", "o");
		processedName = processedName.replaceAll("Ô", "O");
		processedName = processedName.replaceAll("ô", "o");
		processedName = processedName.replaceAll("Õ", "O");
		processedName = processedName.replaceAll("õ", "o");
		processedName = processedName.replaceAll("Ö", "O");
		processedName = processedName.replaceAll("ö", "o");
		processedName = processedName.replaceAll("Š", "S");
		processedName = processedName.replaceAll("š", "s");
		processedName = processedName.replaceAll("Ú", "U");
		processedName = processedName.replaceAll("ù", "u");
		processedName = processedName.replaceAll("Û", "U");
		processedName = processedName.replaceAll("ú", "u");
		processedName = processedName.replaceAll("Ü", "U");
		processedName = processedName.replaceAll("û", "u");
		processedName = processedName.replaceAll("Ù", "U");
		processedName = processedName.replaceAll("ü", "u");
		processedName = processedName.replaceAll("Ý", "Y");
		processedName = processedName.replaceAll("ý", "y");
		processedName = processedName.replaceAll("Ÿ", "Y");
		processedName = processedName.replaceAll("ÿ", "y");
		processedName = processedName.replaceAll("Ž", "Z");
		processedName = processedName.replaceAll("ž", "z");
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
