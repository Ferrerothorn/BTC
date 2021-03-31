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
	
	public static Battle findBattleByPlayer(Player p, ArrayList<Battle> bs) {
		for (Battle b : bs) {
			if (b.getP1().equals(p) || b.getP2().equals(p)) {
				return b;
			}
		}
		return null;
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
	
}
