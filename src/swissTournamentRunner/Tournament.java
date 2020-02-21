package swissTournamentRunner;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;

public class Tournament {

	public static ArrayList<Player> players = new ArrayList<>();
	public ArrayList<Battle> currentBattles = new ArrayList<>();
	public ArrayList<Battle> totallyKosherPairings = new ArrayList<>();
	public ArrayList<Battle> completedBattles = new ArrayList<>();
	private String userSelection = null;
	public boolean noClicks = true;
	boolean allParticipantsIn = false;
	public static int topCutThreshold = 8;
	public int numberOfSwissRounds;
	public int numberOfDraftRounds;
	public int roundNumber = 1;
	public String roundString = "===Round " + roundNumber + "/3===";
	public GUI gui;
	public int predictionsMade = 0;
	public int correctPredictions = 0;

	public void signUpPlayers() {

		print("How many Swiss rounds did this tournament consist of?");
		waitForUserInput();
		numberOfSwissRounds = Integer.parseInt(readInput());

		while (players.size() < 32) {
			print("Enter the name of the next participant, their number of wins, and their Opponent's win rate, separated by colons.");
			print("For example:  Steve Dolman:7:55");
			print("While registering players, you can enter 'drop' to remove a player before beginning.");
			print("");
			waitForUserInput();
			String input = readInput();

			if (input.contains(",")) {
				String[] multiplePlayers = input.split(",");
				for (String p : multiplePlayers) {
					registerPlayer(p);
				}
			} else {
				registerPlayer(input);
			}
		}
	}

//	Hunter Nance:6:69,D.j. Brock:6:67,Peter Sherratt:6:59,Paul Fallon:6:57,William Lo:6:51,Brian Bylicki:5:61,Joshua Twindley:5:61,Emmanuel Onate:5:59,Colin Kauflin:5:57,James Stevenson:5:57,Sam Tuell:5:57,Gaia Filippini:5:53,Marco Greco:5:51,Mohammad Zaiem Ahmad:5:48,Bryan Lue:5:46,Victor Kristansen:4:71,Iris Gerard:4:61,James Ramsden:4:59,Chris Foulds:4:59,Pabz Avestruz III:4:57,Chantelle Emmerton:4:57,Albert Monk:4:57,David Nunez:4:55,Brandon Arruda:4:55,Digger Darckwing:4:55,Braydon Towers:4:51,Raffaele Limatola:4:46,Jimmy Nguyen:4:46,Andrea Piserchia:4:46,Joel Halvars:4:46,Bailey James:4:46,V.d. Mahp:4:46 
	private void registerPlayer(String input) {
		if (input.contains(":")) {
			String[] playerInfo = input.split(":");
			int pod = 0;
			if (getPlayers().size() < 8) {
				pod = 1;
			} else if (getPlayers().size() < 16) {
				pod = 2;
			} else if (getPlayers().size() < 24) {
				pod = 3;
			} else {
				pod = 4;
			}

			try {
				Player p = new Player(playerInfo[0], Integer.parseInt(playerInfo[1]), Integer.parseInt(playerInfo[2]),
						pod);
				p.swissRounds = numberOfSwissRounds;
				players.add(p);
				print("Player '" + playerInfo[0] + "' added.");
				GUI.addResultsString("" + getPlayers().size() + ") " + playerInfo[0]);
				print();
			} catch (Exception e) {
				print("Format incorrect. Try again!");
			}
		}
	}

	public void postListOfConfirmedSignups() {
		Collections.sort(players);
		String post = "-=-=-Registered: " + size() + " players. -=-=-" + "\n";
		for (int i = 1; i <= players.size(); i++) {
			post += "" + i + ") " + players.get(i - 1).getName() + "\n";
		}
		GUI.postResultsString(post);
	}

	public void sortRankings() {
		Collections.sort(players);
	}

	public String rankingsToOneBigString() {
		String output = "-=-=-=-Rankings-=-=-=-" + '\n';
		for (Player p : players) {
			output += p.getName() + "\n";
		}
		return output;
	}

	public String getCurrentBattles(ArrayList<Battle> battles, String roundString) {
		updateParticipantStats();
		int longestPlayerNameLength = 0;
		String battlesString = roundString + "\n";

		for (Battle b : battles) {
			if (b.getP1().getName().length() > longestPlayerNameLength) {
				longestPlayerNameLength = b.getP1().getName().length();
			}
			if (b.getP2().getName().length() > longestPlayerNameLength) {
				longestPlayerNameLength = b.getP2().getName().length();
			}
		}

		for (Battle b : battles) {
			String playerOneString = b.getP1().getName() + " (" + b.getP1().getScore()
					+ " pts)                          ";
			String playerTwoString = b.getP2().getName() + " (" + b.getP2().getScore()
					+ " pts)                          ";

			String battleString = Utils.rpad("Table " + b.getTableNumber() + ") ", 11);
			battleString += Utils.rpad(playerOneString, longestPlayerNameLength + 8) + "vs.    ";
			battleString += Utils.rpad(playerTwoString + "       ", longestPlayerNameLength + 8);
			battlesString += battleString + "\n";
		}
		return battlesString;
	}

	public boolean doesPlayerExist(String string) {
		Player p = findPlayerByName(string);
		if (p != null) {
			return true;
		}
		return false;
	}

	public void addBye() {
		if (findPlayerByName("BYE") == null) {
			PlayerCreator pc = new PlayerCreator(this);
			pc.processPlayer("BYE", 0, 0);
		}
	}

	public void shufflePlayers() {
		Collections.shuffle(players);
	}

	public void updateParticipantStats() {
		for (Player p : players) {
			p.updateParticipantStats(completedBattles);
		}
		sortRankings();
		for (Player p : players) {
			p.updatePositionInRankings(players);
		}
	}

	public void generatePairings(int attempts) {
		if (currentBattles.size() == 0 || activeGamesWereSeeded(currentBattles)) {
			while (attempts <= 100 && players.size() > 0) {
				Player p1 = players.remove(0);
				pairThisGuyUp(p1, currentBattles, attempts);
			}
		}

		currentBattles.addAll(totallyKosherPairings);
		totallyKosherPairings.clear();

		if (attempts > 100) {
			abort();
			Utils.print(generateInDepthRankings(players));
		}
		for (Battle b : currentBattles) {
			players.add(b.getP1());
			players.add(b.getP2());
		}
		int index = 0;
		for (Battle b : currentBattles) {
			b.setTableNumber(index + 1);
			index++;
		}
	}

	public String generateInDepthRankings(ArrayList<Player> ps) {

		String participantString = "";
		int longestPlayerNameLength = 0;

		for (Player p : ps) {
			if (p.getName().length() > longestPlayerNameLength) {
				longestPlayerNameLength = p.getName().length();
			}
		}

		ArrayList<Player> temp = new ArrayList<>();
		temp.addAll(ps);

		participantString += "===Rankings - Top Cut===\n";
		for (int i = 1; i <= topCutThreshold; i++) {
			if (!temp.get(i - 1).getName().equals("BYE")) {

				String pScore = Integer.toString(temp.get(i - 1).getScore());
				String pOWR = Integer.toString(temp.get(i - 1).getOppWr()) + "%";
				String dealt = Integer.toString(temp.get(i - 1).getDamageDealt());
				String received = Integer.toString(temp.get(i - 1).getDamageReceived());

				participantString += Utils.rpad("" + i + ") " + temp.get(i - 1).getName() + "                         ",
						longestPlayerNameLength + 7) + "   "
						+ Utils.rpad("Score: " + pScore + "                         ", 15) + "   "
						+ Utils.rpad(("Opp WR: " + pOWR + "  "), 14) + "  " + Utils.rpad(("Dealt: " + dealt + "  "), 14)
						+ "  " + Utils.rpad(("Received: " + received + "  "), 14) + "  \n";
			}
		}
		participantString += "==Rankings - Qualifiers==" + "\n";

		for (int j = topCutThreshold + 1; j <= temp.size(); j++) {
			if (!temp.get(j - 1).getName().equals("BYE")) {

				String pScore = Integer.toString(temp.get(j - 1).getScore());
				String pOWR = Integer.toString(temp.get(j - 1).getOppWr()) + "%";
				String dealt = Integer.toString(temp.get(j - 1).getDamageDealt());
				String received = Integer.toString(temp.get(j - 1).getDamageReceived());

				participantString += Utils.rpad("" + j + ") " + temp.get(j - 1).getName() + "                         ",
						longestPlayerNameLength + 7) + "   "
						+ Utils.rpad("Score: " + pScore + "                         ", 15) + "   "
						+ Utils.rpad("Opp WR: " + pOWR + "                         ", 12) + "    "
						+ Utils.rpad(("Dealt: " + dealt + "  "), 14) + "  "
						+ Utils.rpad(("Received: " + received + "  "), 14) + "  \n";
			}
		}
		return participantString;
	}

	private boolean activeGamesWereSeeded(ArrayList<Battle> battles) {
		for (Battle b : battles) {
			if (!b.wasSeeded) {
				return false;
			}
		}
		return true;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void pairThisGuyUp(Player p1, ArrayList<Battle> targetBattleList, int attempts) {

		try {
			boolean opponentFound = false;
			int playerIndex = 0;

			while (!opponentFound) {
				Player temp = players.get(playerIndex);
				if (!p1.getOpponentsList().contains(temp) && !temp.getOpponentsList().contains(p1)
						&& temp.getPod() == p1.getPod()) {
					temp = players.remove(playerIndex);
					Battle b = new Battle(p1, temp);
					targetBattleList.add(b);
					break;
				}
				playerIndex++;
			}
		} catch (Exception e) {
			if (attempts >= 100) {
				players.add(p1);
				abort();
			} else {
				disseminateBattles(currentBattles);
				players.add(p1);
				sortRankings();
				players.remove(p1);
				if (p1.getPositionInRankings() > players.size() / 2) {
					Collections.reverse(players);
				}
				pairThisGuyUp(p1, totallyKosherPairings, attempts + 1);
				sortRankings();

			}
		}
	}

	void abort() {
		disseminateBattles(currentBattles);
		disseminateBattles(totallyKosherPairings);
		sortRankings();
		allParticipantsIn = false;
	}

	public void disseminateBattles(ArrayList<Battle> battles) {
		for (Battle b : battles) {
			Player p1 = b.getP1();
			Player p2 = b.getP2();
			players.add(p1);
			players.add(p2);
			b = null;
		}
		battles.clear();
	}

	public void pollForResults() {
		while (currentBattles.size() > 0 && allParticipantsIn) {
			GUI.wipePane();
			print("Enter a table number to report a score for the game.");
			print();

			try {
				print(getCurrentBattles(currentBattles, "===Round " + roundNumber + "/3==="));
				GUI.pairingsBox.setCaretPosition(0);
				waitForUserInput();
				String input = readInput();
				switch (input) {
				default:
					String reportUpon = input;
					Battle b = fetchBattle(reportUpon, currentBattles);

					print("And who won in " + b.getP1().getName() + " vs. " + b.getP2().getName() + "?");
					print("1) " + b.getP1().getName());
					print("2) " + b.getP2().getName());
					print("0) Tied.");

					if (!((b.getP1().getName().equals("BYE") || (b.getP2().getName().equals("BYE"))))) {
						waitForUserInput();
						String winner = readInput();
						int p1dd;
						int p2dd;

						if (winner.equals("2")) {
							print("How much damage did " + b.getP2().getName() + " deal?");
							waitForUserInput();
							p2dd = Integer.parseInt(readInput());
							if (p2dd > 6) {
								p2dd = 6;
							}
							print("How much damage did " + b.getP1().getName() + " deal?");
							waitForUserInput();
							p1dd = Integer.parseInt(readInput());
							if (p1dd > 6) {
								p1dd = 6;
							}
						} else {
							print("How much damage did " + b.getP1().getName() + " deal?");
							waitForUserInput();
							p1dd = Integer.parseInt(readInput());
							if (p1dd > 6) {
								p1dd = 6;
							}
							print("How much damage did " + b.getP2().getName() + " deal?");
							waitForUserInput();
							p2dd = Integer.parseInt(readInput());
							if (p2dd > 6) {
								p2dd = 6;
							}
						}

						b.setP1DealtDamage(p1dd);
						b.setP2DealtDamage(p2dd);

						switch (winner) {
						case "1":
							currentBattles.remove(b);
							Utils.handleBattleWinner(b, "1");
							completedBattles.add(b);
							break;
						case "2":
							currentBattles.remove(b);
							Utils.handleBattleWinner(b, "2");
							completedBattles.add(b);
							break;
						case "0":
							currentBattles.remove(b);
							Utils.handleBattleWinner(b, "0");
							completedBattles.add(b);
							break;
						default:
							break;
						}
					} else {
						if (b.getP1().getName().equals("BYE")) {
							b.getP2().beats(b.getP1());
							completedBattles.add(b);
							currentBattles.remove(b);
							b = null;
						} else if (b.getP2().getName().equals("BYE")) {
							b.getP1().beats(b.getP2());
							completedBattles.add(b);
							currentBattles.remove(b);
							b = null;
						}
					}
					sortRankings();
					refreshScreen();
					break;

				}
			} catch (Exception e) {
				print("Specified table number does not exist.");
				pollForResults();
			}
			GUI.pairingsBox.setCaretPosition(GUI.pairingsBox.getText().length());
		}
	}

	public void refreshScreen() {
		GUI.wipePane();
		updateParticipantStats();
		Collections.shuffle(players);
		sortRankings();
		printRankings(generateInDepthRankings(players));
		getCurrentBattles(currentBattles, "===Round " + roundNumber + "/3===");
		print();
		print();
	}

	private void printRankings(String generateInDepthRankings) {
		GUI.printRankings(generateInDepthRankings);
	}

	public void print() {
		GUI.postString("");
	}

	public void print(String string) {
		GUI.postString(string);
	}

	public void waitForUserInput() {
		while (userSelection == null && noClicks == true) {
			System.out.println("");
		}
		if (userSelection.length() <= 0) {
			userSelection = null;
			waitForUserInput();
		}
	}

	private Battle fetchBattle(String reportUpon, ArrayList<Battle> cB) {
		if (isNumeric(reportUpon)) {
			for (Battle b : cB) {
				if (b.getTableNumber() == Integer.parseInt(reportUpon)) {
					return b;
				}
			}
		} else {
			for (Battle b : cB) {
				if (b.contains(reportUpon)) {
					return b;
				}
			}
		}
		return null;
	}

	public static boolean isNumeric(String str) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}

	public void assignTableNumbers(ArrayList<Battle> bIP) {
		int index = 1;
		for (Battle b : bIP) {
			b.setTableNumber(index);
			index++;
		}
	}

	int logBase2(int x) {
		int base = (int) Math.ceil(Math.log(x) / Math.log(2));
		if (base > 0) {
			return (int) Math.ceil(Math.log(x) / Math.log(2));
		}
		return 0;
	}

	public void setGUI(GUI gui) {
		this.gui = gui;
	}

	public String getUserSelection() {
		return userSelection;
	}

	public void setUserSelection(String userSelection) {
		this.userSelection = userSelection;
	}

	public String toggle(String onOrOff) {
		if (onOrOff.equals("on")) {
			return "off";
		}
		return "on";
	}

	void printHistory(Player p) {
		if (p.getOpponentsList().size() > 0) {
			for (String s : p.getListOfNamesPlayed()) {
				String output = p.getName() + " vs. " + s + " (";
				if (p.getListOfNamesBeaten().contains(s)) {
					output += p.getName();
				} else {
					output += s;
				}
				output += " won)";
				print(output);
			}
		} else {
			print("No games involving " + p.getName() + " have been reported yet.");
		}
		GUI.pairingsBox.setCaretPosition(GUI.pairingsBox.getText().length());
	}

	public void renamePlayer(String playerToRename, String newName) {
		for (Player p : players) {
			if (p.getName().equals(playerToRename)) {
				p.setName(newName);
				break;
			}
		}
		for (Battle b : currentBattles) {
			if (b.getP1().getName().equals(playerToRename)) {
				b.getP1().setName(newName);
				break;
			} else if (b.getP2().getName().equals(playerToRename)) {
				b.getP2().setName(newName);
				break;
			}
		}
		addBye();
	}

	public int size() {
		return players.size();
	}

	public void addPlayer(Player p) {
		players.add(p);
	}

	public Boolean reopenBattle(Player p1, Player p2) {
		Boolean reopen = false;
		for (Player p : p1.getOpponentsList()) {
			if (p.equals(p2)) {
				p1.getOpponentsList().remove(p);
				reopen = true;
				break;
			}
		}
		for (Player p : p2.getOpponentsList()) {
			if (p.equals(p1)) {
				p2.getOpponentsList().remove(p);
				reopen = true;
				break;
			}
		}
		for (Player p : p1.getListOfVictories()) {
			if (p.equals(p2)) {
				p1.getListOfVictories().remove(p);
				reopen = true;
				break;
			}
		}
		for (Player p : p2.getListOfVictories()) {
			if (p.equals(p1)) {
				p2.getListOfVictories().remove(p);
				reopen = true;
				break;
			}
		}
		if (reopen) {
			currentBattles.add(new Battle(p1, p2));
		}
		updateParticipantStats();
		return reopen;
	}

	public String getResultsOfAllMatchesSoFar() {
		String results = "";
		for (Battle b : completedBattles) {
			results += b.getP1().getName() + " [" + b.getP1Damage() + " - " + b.getP2Damage() + "] "
					+ b.getP2().getName() + "\n";
		}
		return results;
	}

	public void setAllParticipantsIn(boolean b) {
		allParticipantsIn = b;
	}

	public void reportBattleWinner(String text) {
		Player winner = findPlayerByName(text);
		for (Battle b : currentBattles) {
			if (b.contains(winner)) {
				if (b.getP1() == winner) {
					Utils.handleBattleWinner(b, "1");
				} else {
					Utils.handleBattleWinner(b, "2");
				}
				currentBattles.remove(b);
				break;
			}
		}
	}

	public String readInput() {
		String uS = userSelection;
		userSelection = null;
		noClicks = true;
		return uS;
	}

	public String playerList() {
		String names = "";
		for (Player p : players) {
			names += p.getName();
			names += ",";
		}
		names = names.substring(0, names.length() - 1);
		return names;
	}

	public void postTourneyProcessing() {
		GUI.postString("FINAL STANDINGS");
		updateParticipantStats();
		GUI.postString(generateInDepthRankings(players));

		GUI.postString("Thanks to everyone for taking part!");
	}

	public void run() {
		while (roundNumber <= 3 && players.size() > 1) {
			Collections.shuffle(players);
			GUI.wipePane();
			updateParticipantStats();
			sortRankings();
			if (roundNumber == 1) {
				shufflePlayers();
			}
			generatePairings(0);
			sortRankings();
			GUI.postResultsString(generateInDepthRankings(players));
			pollForResults();

			roundNumber++;
		}
		GUI.wipePane();
		postTourneyProcessing();
	}

	public Player findPlayerByName(String s) {
		for (Player p : players) {
			if (p.getName().equals(s)) {
				return p;
			}
		}
		return null;
	}

	public Battle findBattleByName(String s) {
		for (Battle b : currentBattles) {
			if (b.getP1().getName().equals(s) || b.getP2().getName().equals(s)) {
				return b;
			}
		}
		return null;
	}

	public void initialSeed(Player p1, Player p2) {
		Battle b = new Battle(p1, p2);
		b.wasSeeded = true;
		currentBattles.add(b);
	}

	public ArrayList<Battle> getCurrentBattles() {
		return currentBattles;
	}

	public int getPredictionsMade() {
		return predictionsMade;
	}

	public int getCorrectPredictions() {
		return correctPredictions;
	}

	public String listAllDamageInCompletedGames() {
		String result = "";
		for (Battle b : completedBattles) {
			result += b.toString();
		}
		if (result.length() > 0) {
			return result.substring(0, result.length() - 1);
		}
		return result;
	}
}