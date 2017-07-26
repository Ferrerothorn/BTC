package swissTournamentRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class TntFileManager {

	static String line;

	public static void saveTournament(Tournament t) {

		if (!t.activeMetadataFile.equals("TournamentInProgress.tnt")) {
			String output = "";
			File file = new File(t.activeMetadataFile);

			output += "PLAYERS:\n";
			for (Player p : t.players) {
				output += p.getName() + ",";
			}
			output = output.substring(0, output.length() - 1);
			output += "\nVICTORIES:\n";
			for (Player p : t.players) {
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
			if (t.isElimination) {
				output += "elimination:" + t.x_elimination + "\n";
			}
			output += "topCut:" + t.topCutThreshold + "\n";

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
		t.players.clear();
		t.currentBattles.clear();

		t.activeMetadataFile = fileName;
		BufferedReader br = new BufferedReader(new FileReader(t.activeMetadataFile));
		try {
			line = br.readLine();

			if (line.contains("PLAYERS")) {
				line = br.readLine();
				while (!line.contains("VICTORIES")) {
					t.addBatch(line);
					line = br.readLine();
				}
				line = br.readLine();
				while (!line.contains("GAMES")) {
					t.addGamesToPlayerHistory(line);
					line = br.readLine();
				}
				line = br.readLine();
				while (!line.contains("PROPERTIES")) {
					t.currentBattles.add(t.parseLineToBattle(line));
					line = br.readLine();
				}
				t.assignTableNumbers(t.currentBattles);
				line = br.readLine();
				while (line != null) {
					t.parseProperties(line);
					line = br.readLine();
				}

			}
		} catch (IOException e) {
			GUI.postString("Error reading supplied file, starting at line: \"" + line + "\"");
		}

		finally {
			br.close();
		}
		t.updateParticipantStats();
	}
}
