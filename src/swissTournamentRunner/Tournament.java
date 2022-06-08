package swissTournamentRunner;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Tournament {

	public static ArrayList<Player> players = new ArrayList<>();
	public static ArrayList<Player> dropped = new ArrayList<>();
	public ArrayList<Battle> currentBattles = new ArrayList<>();
	public ArrayList<Battle> totallyKosherPairings = new ArrayList<>();
	public ArrayList<Battle> completedBattles = new ArrayList<>();
	TntFileManager tntFileManager = new TntFileManager(this);
	String roundString;
	private String userSelection = null;
	public boolean noClicks = true;
	public boolean doubleElimination = false;
	public String elo = "off";
	boolean allParticipantsIn = false;
	public static int topCutThreshold = 0;
	public int numberOfRounds = 0;
	public int roundNumber = 1;
	public GUI gui;
	public String activeMetadataFile = "TournamentInProgress.tnt";
	public int predictionsMade = 0;
	public int correctPredictions = 0;

	public void signUpPlayers() {
		if (activeMetadataFile.equals("TournamentInProgress.tnt")) {
			print("Enter the name of this tournament.");
			waitForUserInput();
			activeMetadataFile = readInput();
			if (!activeMetadataFile.contains(".tnt")) {
				activeMetadataFile += ".tnt";
			}
		}
		File file = new File(activeMetadataFile);
		if (file.exists()) {
			try {
				TntFileManager.loadTournament(this, activeMetadataFile);
				refreshScreen();
			} catch (IOException e) {
				GUI.postString("Error reading supplied file.");
			}
		} else {
			PlayerCreator playerCreator = new PlayerCreator(this);
			playerCreator.capturePlayers();
		}
	}

	public void addPlayer(String p1) {
		if (!doesPlayerExist(p1)) {
			if (p1.length() > 0) {
				String temp = Utils.sanitise(p1);
				temp = Utils.sanitise(temp);
				players.add(new Player(temp));
			}
		}
		while (numberOfRounds < (logBase2(players.size()))) {
			numberOfRounds++;
		}
		if (!allParticipantsIn) {
			postListOfConfirmedSignups();
		}
	}

	public void addLatePlayer(String p1) {
		players.add(new Player(p1));
		while (numberOfRounds < (logBase2(players.size()))) {
			numberOfRounds++;
		}
		if (!allParticipantsIn) {
			postListOfConfirmedSignups();
		}
	}

	public void postListOfConfirmedSignups() {
		Collections.sort(players);
		StringBuilder post = new StringBuilder("-=-=-Registered: " + size() + " player(s) -=-=-" + "\n");
		for (int i = 1; i <= players.size(); i++) {
			post.append(i).append(") ").append(players.get(i - 1).getName()).append("\n");
		}
		GUI.postResultsString(post.toString());
	}

	public void sortRankings() {
		Collections.sort(players);
	}

	public String getCurrentBattles(ArrayList<Battle> battles, String roundString) {
		updateParticipantStats();
		int longestPlayerNameLength = 0;
		StringBuilder battlesString = new StringBuilder(roundString + "\n");

		for (Battle b : battles) {
			if (b.getP1().getName().length() > longestPlayerNameLength) {
				longestPlayerNameLength = b.getP1().getName().length();
			}
			if (b.getP2().getName().length() > longestPlayerNameLength) {
				longestPlayerNameLength = b.getP2().getName().length();
			}
		}

		if (getElo().equals("on")) {
			Collections.sort(currentBattles);
		}

		for (Battle b : battles) {
			String playerOneString = b.getP1().getName() + " (" + b.getP1().getScore()
					+ " pts)";
			String playerTwoString = b.getP2().getName() + " (" + b.getP2().getScore()
					+ " pts)";

			String battleString = Utils.rpad("Table " + b.getTableNumber() + ") ", 11);
			battleString += Utils.rpad(playerOneString, longestPlayerNameLength + 8) + " vs.    ";
			battleString += Utils.rpad(playerTwoString + "       ", longestPlayerNameLength + 8);
			if (getElo().equals("on")) {
				battleString += "[" + b.getElo(b.getP1()) + "% - " + b.getElo(b.getP2()) + "%]";
			}
			battlesString.append(battleString).append("\n");
		}
		return battlesString.toString();
	}

	public boolean doesPlayerExist(String string) {
		Player p = findPlayerByName(string);
		return p != null;
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
		if (numberOfRounds < logBase2(getLivePlayerCount())) {
			numberOfRounds++;
		}
		if (currentBattles.size() == 0 || activeGamesWereSeeded(currentBattles)) {
			if (getLivePlayerCount() % 2 == 1) {
				boolean byeExists = checkByeExists();
				boolean byeNeeded = checkByeNeeded();
				facilitateByeAddition(byeExists, byeNeeded);
			}
			while (getLivePlayerCount() > 0 && attempts <= 100) {
				Player p1 = players.remove(0);
				if (!p1.isDropped()) {
					pairThisGuyUp(p1, currentBattles, attempts);
				} else {
					players.add(p1);
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
	}

	private void facilitateByeAddition(boolean byeExists, boolean byeNeeded) {
		if (byeExists) {
			Player bye = findPlayerByName("BYE");
			if (byeNeeded) {
				if (bye.isDropped()) {
					bye.setDropped(false);
					dropped.remove(bye);
				} else {
					bye.setDropped(true);
					dropped.add(bye);
				}
			} else {
				bye = findPlayerByName("BYE");
				bye.setDropped(true);
				dropped.add(bye);
			}
		} else {
			if (byeNeeded) {
				players.add(new Player("BYE"));
			}
		}
	}

	private boolean checkByeExists() {
		return findPlayerByName("BYE") != null;
	}

	private boolean checkByeNeeded() {
		return ((getLivePlayerCount() % 2) == 1);
	}

	public String generateInDepthRankings(ArrayList<Player> ps) {

		StringBuilder participantString = new StringBuilder();
		int longestPlayerNameLength = 0;

		for (Player p : ps) {
			if (p.getName().length() > longestPlayerNameLength) {
				longestPlayerNameLength = p.getName().length();
			}
		}

		ArrayList<Player> temp = new ArrayList<>(ps);

		for (Player p : dropped) {
			temp.remove(p);
		}

		if (topCutThreshold != 0) {
			participantString.append("===Rankings - Top Cut===\n");
			for (int i = 1; i <= topCutThreshold; i++) {
				if (!temp.get(i - 1).getName().equals("BYE")) {

					String pScore = Integer.toString(temp.get(i - 1).getScore());
					String pOWR = temp.get(i - 1).getOppWr() + "%";
					String aveReceived = Double.toString(temp.get(i - 1).getAverageDamageReceived());
					String aveDealt = Double.toString(temp.get(i - 1).getAverageDamageDealt());

					participantString.append(Utils.rpad(
							"" + i + ") " + temp.get(i - 1).getName() + "                         ",
							longestPlayerNameLength + 7)).append("   ").append(Utils.rpad("Score: " + pScore + "                         ", 15)).append("   ").append(Utils.rpad(("Opp WR: " + pOWR + "  "), 14)).append("  ").append(Utils.rpad(("Ave. Dam. Taken: " + aveReceived + "  "), 21)).append("     ").append(Utils.rpad(("Ave. Dam. Dealt: " + aveDealt + "  "), 21)).append("     ").append(Utils.rpad("Win Pattern: " + temp.get(i - 1).getWinPattern(), 24)).append("  ").append('\n');
				}
			}
			participantString.append("==Rankings - Qualifiers==" + "\n");
		} else {
			participantString.append("===Rankings===\n");
		}

		for (int j = topCutThreshold + 1; j <= temp.size(); j++) {
			if (!temp.get(j - 1).getName().equals("BYE")) {

				String pScore = Integer.toString(temp.get(j - 1).getScore());
				String pOWR = temp.get(j - 1).getOppWr() + "%";
				String aveReceived = Double.toString(temp.get(j - 1).getAverageDamageReceived());
				String aveDealt = Double.toString(temp.get(j - 1).getAverageDamageDealt());

				participantString.append(Utils.rpad(
						"" + j + ") " + temp.get(j - 1).getName() + "                         ",
						longestPlayerNameLength + 7)).append("   ").append(Utils.rpad("Score: " + pScore + "                         ", 15)).append("   ").append(Utils.rpad(("Opp WR: " + pOWR + "  "), 14)).append("  ").append(Utils.rpad(("Ave. Dam. Taken: " + aveReceived + "  "), 21)).append("     ").append(Utils.rpad(("Ave. Dam. Dealt: " + aveDealt + "  "), 21)).append("     ").append(Utils.rpad("Win Pattern: " + temp.get(j - 1).getWinPattern(), 24)).append("  ").append('\n');
			}
		}
		return participantString.toString();
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

	public ArrayList<Player> getDroppedPlayers() {
		return dropped;
	}

	public void pairThisGuyUp(Player p1, ArrayList<Battle> targetBattleList, int attempts) {

		try {
			int playerIndex = 0;

			while (true) {
				Player temp = players.get(playerIndex);
				if (!p1.getOpponentsList().contains(temp) && !temp.getOpponentsList().contains(p1)
						&& !temp.isDropped()) {
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
		}
		battles.clear();
	}

	public void pollForResults() {
		while (currentBattles.size() > 0 && allParticipantsIn) {
			updateRoundString();
			GUI.wipePane();
			print("Enter a table number to report a score for the game.");
			print();

			try {
				print(getCurrentBattles(currentBattles, roundString));
				GUI.pairingsBox.setCaretPosition(0);
				waitForUserInput();
				String input = readInput();
				if ("admintools".equals(input)) {
					print("Admin functions enabled.");
					waitForUserInput();
					String adminCommand = readInput();
					adminTools(adminCommand);
				} else {
					Battle b = fetchBattle(input, currentBattles);

					assert b != null;
					print("And who won in " + b.getP1().getName() + " vs. " + b.getP2().getName() + "?");
					print("1) " + b.getP1().getName() + " (" + b.getElo(b.getP1()) + "% predicted win rate)");
					print("2) " + b.getP2().getName() + " (" + b.getElo(b.getP2()) + "% predicted win rate)");
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
							print("How much damage did " + b.getP1().getName() + " deal?");
							waitForUserInput();
							p1dd = Integer.parseInt(readInput());
						} else {
							print("How much damage did " + b.getP1().getName() + " deal?");
							waitForUserInput();
							p1dd = Integer.parseInt(readInput());
							print("How much damage did " + b.getP2().getName() + " deal?");
							waitForUserInput();
							p2dd = Integer.parseInt(readInput());
						}

						b.setP1DamageDealt(p1dd);
						b.setP2DamageDealt(p2dd);

						if (!b.getP1().getName().equals("BYE") && !b.getP2().getName().equals("BYE")
								&& ((winner.equals("1") && b.getElo(b.getP1()) > 50)
								|| (winner.equals("2") && b.getElo(b.getP2()) > 50))) {
							correctPredictions++;
						}
						if (b.getElo(b.getP1()) != 50) {
							predictionsMade++;
						}
						switch (winner) {
							case "1" -> {
								currentBattles.remove(b);
								if (activeMetadataFile.contains("topCut")) {
									eliminatePlayer(b.getP2());
								}
								Utils.handleBattleWinner(b, "1");
								completedBattles.add(b);
							}
							case "2" -> {
								currentBattles.remove(b);
								if (activeMetadataFile.contains("topCut")) {
									eliminatePlayer(b.getP1());
								}
								Utils.handleBattleWinner(b, "2");
								completedBattles.add(b);
							}
							case "0" -> {
								currentBattles.remove(b);
								Utils.handleBattleWinner(b, "0");
								completedBattles.add(b);
							}
							default -> {
							}
						}
					} else {
						if (b.getP1().getName().equals("BYE")) {
							b.getP2().beats(b.getP1());
							completedBattles.add(b);
							currentBattles.remove(b);
						} else if (b.getP2().getName().equals("BYE")) {
							b.getP1().beats(b.getP2());
							completedBattles.add(b);
							currentBattles.remove(b);
						}
					}
					sortRankings();
					refreshScreen();
				}
			} catch (Exception e) {
				print("Specified table number does not exist.");
				pollForResults();
			}
			GUI.pairingsBox.setCaretPosition(GUI.pairingsBox.getText().length());
			save();
		}
	}

	private void updateRoundString() {
		roundString = ("-=-=-=-ROUND " + roundNumber + "/" + numberOfRounds + "-=-=-=-");
	}

	public void refreshScreen() {
		GUI.wipePane();
		updateParticipantStats();
		Collections.shuffle(players);
		sortRankings();
		printRankings(generateInDepthRankings(players));
		getCurrentBattles(currentBattles, roundString);
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
		while (userSelection == null && noClicks) {
			System.out.println();
		}
		assert userSelection != null;
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
		double logBase2ofX = Math.log(x) / Math.log(2);

		int base = (int) Math.ceil(logBase2ofX);
		if (base > 0) {
			return (int) Math.ceil(logBase2ofX);
		}
		return 0;
	}

	public void setGUI(GUI gui) {
		this.gui = gui;
	}

	public void setUserSelection(String userSelection) {
		this.userSelection = userSelection;
	}

	public void adminTools(String string) {
		switch (string.toLowerCase()) {
			case "acr" -> {
				while (currentBattles.size() > 0) {
					Battle b = currentBattles.remove(0);
					if (b.getP1().getName().equals("BYE")) {
						Utils.handleBattleWinner(b, Integer.toString(2));
					} else if (b.getP2().getName().equals("BYE")) {
						Utils.handleBattleWinner(b, Integer.toString(1));
					} else {
						Random r = new Random();
						int win = r.nextInt(2);
						win++;
						Utils.handleBattleWinner(b, Integer.toString(win));
					}
				}
				updateParticipantStats();
				sortRankings();
				GUI.postResultsString(generateInDepthRankings(players));
			}
			case "de" -> doubleElimination = true;
			case "roundrobin" -> generateRoundRobinPairings();
			case "killall -9" -> {
				currentBattles.clear();
				players.clear();
			}
			default -> print("Invalid admin command. Returning to tournament...\n");
		}
		GUI.pairingsBox.setCaretPosition(GUI.pairingsBox.getText().length());
	}

	public String toggle(String onOrOff) {
		if (onOrOff.equals("on")) {
			return "off";
		}
		return "on";
	}

	private void generateRoundRobinPairings() {
		currentBattles.clear();
		for (Player p : players) {
			p.getOpponentsList().clear();
			p.getListOfVictories().clear();
		}
		players.remove(findPlayerByName("BYE"));
		this.setNumberOfRounds(1);
		this.roundNumber = 1;

		int index = 1;
		for (Player p : players) {
			for (Player q : players) {
				if (p != q && !activeBattleExists(currentBattles, p, q)) {
					Battle b = new Battle(p, q);
					b.setTableNumber(index);
					index++;
					currentBattles.add(b);
				}
			}
		}
	}

	private boolean activeBattleExists(ArrayList<Battle> battles, Player p, Player q) {
		boolean exists = false;
		for (Battle b : battles) {
			if ((b.getP1().equals(p) && b.getP2().equals(q)) || (b.getP1().equals(q) && b.getP2().equals(p))) {
				exists = true;
				break;
			}
		}
		return exists;
	}

	void setTopCut(int parseInt) {
		topCutThreshold = parseInt;
	}

	public void addBatch(String playerList) {
		String[] names = playerList.split(",");
		for (String s : names) {
			addPlayer(Utils.sanitise(Utils.sanitise(s)));
			postListOfConfirmedSignups();
		}
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
	}

	public void alterTopCut(String newSize) throws NumberFormatException {
		try {
			int tC = Integer.parseInt(newSize);
			if (tC < size()) {
				setTopCut(tC);
				Utils.print("Top Cut size set to " + tC + ".\n");
			} else {
				print("Invalid - suggested top cut size is too large.");
				print("Size must be a less than the number of players.");
			}
		} catch (NumberFormatException e) {
			print("Invalid input - top cut size must be a number.");
			print("Size must be a less than the number of players.");
			print("Alternatively, enter '0' to remove the Top Cut.\n");
		}
	}

	public int size() {
		return players.size();
	}

	public void setNumberOfRounds(int newNumberOfRounds) {
		numberOfRounds = newNumberOfRounds;
	}

	public int getNumberOfRounds() {
		return numberOfRounds;
	}

	public int getTopCutThreshold() {
		return topCutThreshold;
	}

	public void alterRoundNumbers(String newMax) throws NumberFormatException {
		try {
			int newNumOfRounds = Integer.parseInt(newMax);
			if (newNumOfRounds < players.size() && newNumOfRounds >= logBase2(players.size())) {
				setNumberOfRounds(newNumOfRounds);
				print("Number of rounds updated to " + getNumberOfRounds() + ".");
				updateRoundString();
			} else {
				print("Invalid number of rounds for a Swiss tournament.");
				print("We need to have less rounds than the number of players, and at least logBase2(number of players).");
			}
		} catch (NumberFormatException e) {
			print("Illegal input - try submitting a number of rounds as a number.");
		}
	}

	public void reopenBattle(Player p1, Player p2) {
		boolean reopen = false;
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
	}

	public String getResultsOfAllMatchesSoFar() {
		StringBuilder results = new StringBuilder();
		for (Battle b : completedBattles) {
			results.append(b.getP1().getName()).append(" ").append(b.getP1().hasBeaten(b.getP2())).append(" [").append(b.getP1DamageDealt()).append(" - ").append(b.getP2DamageDealt()).append("] ").append(b.getP2().hasBeaten(b.getP1())).append(" ").append(b.getP2().getName()).append("\n");
		}
		return results.toString();
	}

	public String getResultsOfAllMatchesByPlayerSoFar(Player p) {
		StringBuilder results = new StringBuilder();
		for (Battle b : completedBattles) {
			if (b.contains(p)) {
				results.append(b.getP1().getName()).append(" ").append(b.getP1().hasBeaten(b.getP2())).append(" [").append(b.getP1DamageDealt()).append(" - ").append(b.getP2DamageDealt()).append("] ").append(b.getP2().hasBeaten(b.getP1())).append(" ").append(b.getP2().getName()).append("\n");
			}
		}
		return results.toString();
	}

	public String postTournamentAwards() throws IndexOutOfBoundsException {
		String output = "";
		try {
			Player p1 = fetchHardestFoughtPlayer();
			Player p3 = fetchBiggestMilker();
			Player p4 = fetchHardestDoneBy();

			Collections.sort(players);
			output += "Congratulations to " + players.get(0).getName() + " on winning this tournament!\n";
			if (p1 != null) {
				output += "Props to " + p1.getName() + " for enduring the toughest range of opponents.\n";
			}
			if (p3 != null) {
				output += p3.getName()
						+ " can thank their lucky stars for being generally paired down the most considering their win rate.\n";
			}
			if (p4 != null) {
				output += "Commiserations to " + p4.getName() + " for being paired up unusually often.\n";
			}
			if (predictionsMade > 0) {
				output += "Of the " + predictionsMade + " match result predictions made, " + correctPredictions
						+ " were correct.\n";
			}
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Exception thrown: Tried to access unavailable player.");
		}
		return output;
	}

	private Player fetchHardestDoneBy() {
		Collections.sort(players);
		Collections.reverse(players);
		for (Player p : players) {
			if (p.getOppWr() > 50 && !dropped.contains(p) && !p.isDropped()) {
				return p;
			}
		}
		return null;
	}

	private Player fetchBiggestMilker() {
		Collections.sort(players);
		for (Player p : players) {
			if (p.getOppWr() < 50 && !dropped.contains(p) && !p.isDropped() && !p.getName().equals("BYE")) {
				return p;
			}
		}
		return null;
	}

	private Player fetchHardestFoughtPlayer() {
		int highestOWR = 0;
		Player hardest = null;
		for (Player p : players) {
			if (p.getOppWr() > highestOWR && !p.getName().equals("BYE") && !p.isDropped()) {
				hardest = p;
				highestOWR = p.getOppWr();
			}
		}
		return hardest;
	}

	public String readInput() {
		String uS = userSelection;
		userSelection = null;
		noClicks = true;
		return uS;
	}

	public void postTourneyProcessing() {
		GUI.postString("FINAL STANDINGS");
		updateParticipantStats();
		GUI.postString(generateInDepthRankings(players));
		GUI.postString(postTournamentAwards());

		if (topCutThreshold > 1) {
			GUI.postString("Should we progress to the top cut of " + topCutThreshold + "? (y/n)");
			waitForUserInput();
			String input = readInput();
			if (input.toLowerCase().charAt(0) == 'y') {
				activeMetadataFile = activeMetadataFile.replace(".", "-topCut.");
				ArrayList<String> topCut = new ArrayList<>();
				for (int i = 0; i < topCutThreshold; i++) {
					topCut.add(players.get(i).getName());
				}
				players.clear();
				dropped.clear();
				for (String player : topCut) {
					addPlayer(player);
				}
				roundNumber = 1;
				numberOfRounds = logBase2(players.size());
				topCutThreshold = 0;
				for (int j = 0; j < players.size() / 2; j++) {
					currentBattles.add(new Battle(players.get(j), players.get(players.size() - (j + 1))));
				}
				assignTableNumbers(currentBattles);
				completedBattles.clear();
				run();
			} else {
				GUI.postString("Thanks to everyone for taking part!");
			}
		}
	}

	public void run() {
		while (roundNumber <= getNumberOfRounds() && players.size() > 1) {
			Collections.shuffle(players);
			GUI.wipePane();
			updateParticipantStats();
			sortRankings();
			if (roundNumber == 1) {
				shufflePlayers();
			}
			generatePairings(0);
			save();
			sortRankings();
			GUI.postResultsString(generateInDepthRankings(players));
			pollForResults();

			roundNumber++;
		}
		save();
		GUI.wipePane();
		postTourneyProcessing();
	}

	void save() {
		tntFileManager.saveTournament();
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

	public String getElo() {
		return elo;
	}

	public void setElo(String elo) {
		this.elo = elo;
	}

	public void eliminatePlayer(Player p) {
		dropped.add(p);
	}

	public void dropPlayer(String string) {
		if (getLivePlayerCount() > 2) {
			Player toDrop = findPlayerByName(string);
			toDrop.setDropped(true);
			dropped.add(toDrop);
			numberOfRounds = (logBase2(getLivePlayerCount()));
		} else {
			print("You can't drop a player when there are only 2, or less, remaining players.");
		}
	}

	public int getLivePlayerCount() {
		return players.size() - dropped.size();
	}

	public int getPredictionsMade() {
		return predictionsMade;
	}

	public int getCorrectPredictions() {
		return correctPredictions;
	}

	public String listAllDamageInCompletedGames() {
		StringBuilder result = new StringBuilder();
		for (Battle b : completedBattles) {
			result.append(b.toString());
		}
		if (result.length() > 0) {
			return result.substring(0, result.length() - 1);
		}
		return result.toString();
	}

	public void addBatchFromFile(String line) {
		String[] names = line.split(",");
		for (String s : names) {
			Player p = new Player(Utils.sanitise(s));
			players.add(p);
			postListOfConfirmedSignups();
		}
	}
}