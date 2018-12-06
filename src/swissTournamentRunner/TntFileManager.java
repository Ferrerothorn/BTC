package swissTournamentRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class TntFileManager {

	static Tournament t;
	static String line;

	public TntFileManager(Tournament tourney) {
		t = tourney;
	}

	public void saveTournament() {

		if (!t.activeMetadataFile.equals("TournamentInProgress.tnt")) {
			String output = "";
			File file = new File(t.activeMetadataFile);

			output += "PLAYERS:\n";
			for (Player p : Tournament.players) {
				output += p.getName() + ",";
			}
			output = output.substring(0, output.length() - 1) + "\n";

			output += "VICTORIES:\n";
			for (Player p : Tournament.players) {
				output += p.getName() + "_" + p.getListOfNamesBeaten().toString() + "_"
						+ p.getListOfNamesPlayed().toString() + "\n";
			}
			output += "GAMES:\n";
			for (Battle b : t.currentBattles) {
				output += b.getP1().getName() + "," + b.getP2().getName() + "\n";
			}
			output += "PROPERTIES:\n";
			output += "On Round:" + t.roundNumber + "\n";
			output += "numberOfRounds:" + t.numberOfRounds + "\n";
			output += "topCut:" + Tournament.getTopCutThreshold() + "\n";
			output += "ELO:" + t.getElo() + "\n";
			String s = "";
			for (Player p : Tournament.dropped) {
				s += p.getName();
				s += ",";
			}
			if (s.length() > 0) {
				s = s.substring(0, s.length() - 1);
			}
			output += "Dropped:" + s + "\n";
			try {
				PrintWriter writer = new PrintWriter(file, "UTF-8");
				writer.print(output);
				writer.close();
			} catch (FileNotFoundException e) {
				Utils.print("Couldn't write file.");
			} catch (UnsupportedEncodingException e) {
				Utils.print("Unsupported encoding.");
			}
		}
	}

	public static void loadTournament(Tournament t, String fileName) throws IOException {
		Tournament.players.clear();
		t.currentBattles.clear();

		t.gui.toolbar.remove(t.gui.startButton);
		GUI.frame.repaint();

		t.activeMetadataFile = fileName;
		BufferedReader br = new BufferedReader(new FileReader(t.activeMetadataFile));
		try {
			line = br.readLine();

			if (line.contains("PLAYERS")) {
				line = br.readLine();

				while (!line.contains("VICTORIES")) {
					t.addBatch(line);
					line = br.readLine();
					t.addBye();
				}
				line = br.readLine();
				while (!line.contains("GAMES")) {
					addGamesToPlayerHistory(line);
					line = br.readLine();
				}
				line = br.readLine();
				while (!line.contains("PROPERTIES")) {
					t.currentBattles.add(parseLineToBattle(line));
					line = br.readLine();
				}
				t.assignTableNumbers(t.currentBattles);
				line = br.readLine();
				while (line != null) {
					parseProperties(line);
					line = br.readLine();
				}
				t.assignTableNumbers(t.currentBattles);
			}
		} catch (IOException e) {
			GUI.postString("Error reading supplied file, starting at line: \"" + line + "\"");
		}

		finally {
			br.close();
		}
		t.updateParticipantStats();
	}

	public static void parseProperties(String line2) {
		try {
			String[] propertyPair = line.split(":");
			switch (propertyPair[0].toLowerCase()) {

			case "on round":
				t.roundNumber = Integer.parseInt(propertyPair[1]);
				break;
			case "numberofrounds":
				t.numberOfRounds = Integer.parseInt(propertyPair[1]);
				break;
			case "topcut":
				int tC = Integer.parseInt(propertyPair[1]);
				if (tC < Tournament.players.size()) {
					t.setTopCut(tC);
				}
				break;
			case "elo":
				if (propertyPair[1].equals("on")) {
					t.setElo("on");
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			GUI.postString("Error reading supplied file, starting at line: \"" + line + "\".");
		}
	}

	static Battle parseLineToBattle(String line) {
		String[] currentCombatants = line.split(",");
		Player p1 = Tournament.findPlayerByName(currentCombatants[0]);
		Player p2 = Tournament.findPlayerByName(currentCombatants[1]);
		Battle b = new Battle(p1, p2);
		return b;
	}

	public static void addGamesToPlayerHistory(String line) {
		try {
			String[] information = line.split("_");
			Player p = Tournament.findPlayerByName(information[0]);

			String hasBeaten = information[1];
			hasBeaten = hasBeaten.replaceAll("\\[", "");
			hasBeaten = hasBeaten.replaceAll("\\]", "");
			String[] playersBeaten = hasBeaten.split(",");
			for (String s : playersBeaten) {
				if (s.length() > 0) {
					p.addToListOfVictories(Tournament.findPlayerByName(Utils.trimWhitespace(s)));
				}
			}

			String hasPlayed = information[2];
			hasPlayed = hasPlayed.replaceAll("\\[", "");
			hasPlayed = hasPlayed.replaceAll("\\]", "");
			String[] playersPlayed = hasPlayed.split(",");
			for (String s : playersPlayed) {
				if (s.length() > 0) {
					p.addToListOfPlayed(Tournament.findPlayerByName(Utils.trimWhitespace(s)));
				}
			}
		} catch (Exception e) {
			GUI.postString("Error reading supplied file, starting at line: \"" + line + "\".");
		}
	}

}
