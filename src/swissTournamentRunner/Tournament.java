package swissTournamentRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Tournament {

	public static ArrayList<Player> players = new ArrayList<>();
	public static ArrayList<Player> dropZone = new ArrayList<>();
	public ArrayList<Battle> currentBattles = new ArrayList<>();
	public static ArrayList<String> dropped = new ArrayList<>();
	public ArrayList<Battle> totallyKosherPairings = new ArrayList<>();
	TntFileManager tntfm = new TntFileManager(this);
	public static String roundString;
	private String userSelection = null;
	public boolean noClicks = true;
	public String elo = "off";
	boolean allParticipantsIn = false;
	public static int topCutThreshold = 0;
	public int numberOfRounds;
	public int roundNumber = 1;
	public GUI gui;
	public String activeMetadataFile = "TournamentInProgress.tnt";
	public static Logger logger = Logger.getLogger(Tournament.class.getName());
	public static FileHandler fh;
	public int predictionsMade;
	public int correctPredictions;
	public int elimination;

	public void signUpPlayers() {
		if (activeMetadataFile.equals("TournamentInProgress.tnt")) {
			print("Enter the name of this tournament.");
			waitForUserInput();
			activeMetadataFile = readInput();
			GUI.frame.setTitle(activeMetadataFile);
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
		logger.info("addPlayer: " + p1);
		if (doesPlayerExist("BYE") && !doesPlayerExist(p1)) {
			renamePlayer("BYE", p1);
		} else if (!doesPlayerExist(p1)) {
			if (p1.length() > 0) {
				players.add(new Player(p1));
			}
		} else {
			dropped.remove(p1);
		}
		while (numberOfRounds < (logBase2(players.size()))) {
			numberOfRounds++;
		}
		if (!allParticipantsIn) {
			postListOfConfirmedSignups();
		}
	}

	public void postListOfConfirmedSignups() {
		Collections.sort(players);
		int totalNumberOfPlayers = activePlayerSize() + currentBattles.size() * 2;

		String post = "-=-=-Registered: " + totalNumberOfPlayers + " players. -=-=-" + "\n";

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

		if (getElo().equals("on")) {
			Collections.sort(currentBattles);
		}

		for (Battle b : battles) {
			String playerOneString = b.getP1().getName() + " (" + b.getP1().getPositionInRankings()
					+ ")                          ";
			String playerTwoString = b.getP2().getName() + " (" + b.getP2().getPositionInRankings()
					+ ")                          ";

			String battleString = Utils.rpad("Table " + b.getTableNumber() + ") ", 11);
			battleString += Utils.rpad(playerOneString, longestPlayerNameLength + 8) + "vs.    ";
			battleString += Utils.rpad(playerTwoString + "       ", longestPlayerNameLength + 8);
			if (getElo().equals("on")) {
				battleString += "[" + b.getElo(b.getP1()) + "% - " + b.getElo(b.getP2()) + "%]";
			}
			battlesString += battleString + "\n";
		}
		return battlesString;
	}

	public boolean doesPlayerExist(String string) {
		logger.info("doesPlayerExist: " + string);
		Player p = findPlayerByName(string);
		if (p != null) {
			return true;
		}
		return false;
	}

	public void addBye() {
		logger.info("addBye()");
		if (dropped.contains("BYE")) {
			dropped.remove("BYE");
		} else {
			if (findPlayerByName("BYE") == null && activePlayerSize() % 2 == 1) {
				addPlayer("BYE");
			}
		}
	}

	public void shufflePlayers() {
		Collections.shuffle(players);
	}

	public void updateParticipantStats() {
		for (Player p : players) {
			p.recalculateScore();
		}
		for (Player p : players) {
			p.recalculateTB();
		}
		for (Player p : players) {
			p.recalculateOppWr();
		}
		for (Player p : players) {
			p.recalculateOppOppWr();
		}
		for (Player p : players) {
			p.recalculateOppositionTBSum();
		}
		sortRankings();
		for (Player p : players) {
			p.updatePositionInRankings(players);
		}
	}

	public void generatePairings(int attempts) {
		if (currentBattles.size() == 0 || activeGamesWereSeeded(currentBattles)) {

			while (players.size() > 0 && attempts <= 100) {
				Player p1 = players.remove(0);
				pairThisGuyUp(p1, currentBattles, attempts);
			}
			currentBattles.addAll(totallyKosherPairings);
			totallyKosherPairings.clear();

			players.addAll(dropZone);
			dropZone.clear();
			if (attempts > 100) {
				abort();
				Utils.print(generateInDepthRankings(players));
			}
			for (Battle b : currentBattles) {
				players.add(b.getP1());
				players.add(b.getP2());
			}
		}
	}

	public static String generateInDepthRankings(ArrayList<Player> ps) {
		String participantString = "";
		int longestPlayerNameLength = 0;

		for (Player p : ps) {
			if (p.getName().length() > longestPlayerNameLength) {
				longestPlayerNameLength = p.getName().length();
			}
		}

		if (topCutThreshold != 0) {
			participantString += "===Rankings - Top Cut===" + "\n";
			for (int i = 1; i <= topCutThreshold; i++) {
				if (!ps.get(i - 1).getName().equals("BYE")) {

					String pScore = Integer.toString(ps.get(i - 1).getScore());
					String pTB = Integer.toString(ps.get(i - 1).getTB());
					String pOWR = Integer.toString(ps.get(i - 1).getOppWr()) + "%";
					String pOOWR = Integer.toString(ps.get(i - 1).getOppOppWr()) + "%";

					participantString += Utils.rpad(
							"" + i + ") " + ps.get(i - 1).getName() + "                         ",
							longestPlayerNameLength + 7)
							+ Utils.rpad("Score: " + pScore + "                         ", 15) + "   "
							+ Utils.rpad("TB: " + pTB + "                         ", 8) + "   "
							+ Utils.rpad(("Opp WR: " + pOWR + "  "), 14) + "  "
							+ Utils.rpad("Opp Opp WR: " + pOOWR + "  ", 18) + "  "
							+ Utils.rpad("STB: " + ps.get(i - 1).oppositionTBSum, 9) + '\n';
				}
			}
			participantString += "==Rankings - Qualifiers==" + "\n";
		} else {
			participantString += "-=-=-=-Rankings-=-=-=-" + '\n';
		}

		for (int j = topCutThreshold + 1; j <= ps.size(); j++) {
			if (!ps.get(j - 1).getName().equals("BYE")) {

				String pScore = Integer.toString(ps.get(j - 1).getScore());
				String pTB = Integer.toString(ps.get(j - 1).getTB());
				String pOWR = Integer.toString(ps.get(j - 1).getOppWr()) + "%";
				String pOOWR = Integer.toString(ps.get(j - 1).getOppOppWr()) + "%";

				participantString += Utils.rpad("" + j + ") " + ps.get(j - 1).getName() + "                         ",
						longestPlayerNameLength + 7) + "   "
						+ Utils.rpad("Score: " + pScore + "                         ", 15) + "   "
						+ Utils.rpad("TB: " + pTB + "                         ", 8) + "   "
						+ Utils.rpad("Opp WR: " + pOWR + "                         ", 12) + "    "
						+ Utils.rpad("Opp Opp WR: " + pOOWR + "                         ", 16) + "  "
						+ Utils.rpad("STB: " + ps.get(j - 1).oppositionTBSum, 9) + '\n';
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

	public void pairThisGuyUp(Player p1, ArrayList<Battle> targetBattleList, int attempts) {
		logger.info("pairThisGuyUp: " + p1.getName());

		if (dropped.contains(p1.getName())) {
			dropZone.add(p1);
			players.remove(p1);
		} else {
			try {
				boolean opponentFound = false;
				int playerIndex = 0;

				while (!opponentFound) {
					Player temp = players.get(playerIndex);
					System.out.println("Active player count: " + activePlayerSize());
					if ((!p1.getOpponentsList().contains(temp) && !temp.getOpponentsList().contains(p1)
							&& !dropped.contains(temp.getName())) || elimination > 0) {
						temp = players.remove(playerIndex);
						Battle b = new Battle(p1, temp);
						logger.info("Pairing decided between: " + p1.getName() + " (" + p1.lastDocumentedPosition + ") "
								+ " and " + temp.getName() + " (" + temp.lastDocumentedPosition + ").");
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
		assignTableNumbers(currentBattles);

		while (currentBattles.size() > 0 && allParticipantsIn) {
			updateRoundString();
			print("Enter a table number to report a score for the game.");

			try {
				print(getCurrentBattles(currentBattles, roundString));
				GUI.pairingsBox.setCaretPosition(GUI.pairingsBox.getText().length());

				waitForUserInput();
				String input = readInput();

				switch (input) {
				case "admintools":
					print("Admin functions enabled.");
					waitForUserInput();
					String adminCommand = readInput();
					adminTools(adminCommand);
					break;
				default:
					int reportUpon = Integer.parseInt(input);
					Battle b = fetchBattle(reportUpon, currentBattles);

					logger.info("Reporting result of: " + b.getP1().getName() + " vs " + b.getP2().getName() + ".");
					currentBattles.remove(b);

					print("And who won in " + b.getP1().getName() + " vs. " + b.getP2().getName() + "?");
					print("1) " + b.getP1().getName() + " (" + b.getElo(b.getP1()) + "% predicted win rate)");
					print("2) " + b.getP2().getName() + " (" + b.getElo(b.getP2()) + "% predicted win rate)");
					print("0) Tied.");

					if (!((b.getP1().getName().equals("BYE") || (b.getP2().getName().equals("BYE"))))) {
						waitForUserInput();
						String winner = readInput();
						if (winner.equals("1") || winner.equals("2") || winner.equals("0")) {
							if (b.getElo(b.getP1()) != 50) {
								predictionsMade++;
							}
							if ((b.getElo(b.getP1()) > 50 && winner.equals("1"))
									|| (b.getElo(b.getP2()) > 50 && winner.equals("2"))) {
								correctPredictions++;
							}

							Utils.handleBattleWinner(b, winner);
						} else {
							currentBattles.add(b);
						}
					} else {
						if (b.getP1().getName().equals("BYE")) {
							b.getP2().beats(b.getP1());
							b = null;
						} else if (b.getP2().getName().equals("BYE")) {
							b.getP1().beats(b.getP2());
							b = null;
						}
					}
					refreshScreen();
					break;
				}
			} catch (Exception e) {
				GUI.wipePane();
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
		logger.info("Waiting for input.");
		while (userSelection == null && noClicks == true) {
			System.out.println("");
		}
		if (userSelection.length() <= 0) {
			userSelection = null;
			waitForUserInput();
		}
	}

	private Battle fetchBattle(int reportUpon, ArrayList<Battle> cB) {
		for (Battle b : cB) {
			if (b.getTableNumber() == reportUpon) {
				return b;
			}
		}
		return null;
	}

	public void assignTableNumbers(ArrayList<Battle> bIP) {
		int index = 1;
		for (Battle b : bIP) {
			b.setTableNumber(index);
			index++;
		}
	}

	int logBase2(int x) {
		logger.info("logBase2: " + x);
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

	public void adminTools(String string) {
		switch (string.toLowerCase()) {
		case "roundrobin":
			generateRRpairings();
			break;
		case "killall -9":
			currentBattles.clear();
			players.clear();
			break;
		default:
			print("Invalid admin command. Returning to tournament...\n");
			break;
		}
		GUI.pairingsBox.setCaretPosition(GUI.pairingsBox.getText().length());
	}

	public String toggle(String onOrOff) {
		if (onOrOff.equals("on")) {
			return "off";
		}
		return "on";
	}

	void printHistory(Player p) {
		logger.info("printHistory: " + p.getName());
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

	public void generateRRpairings() {

		logger.info("generateRRpairings");
		currentBattles.clear();
		for (Player p : players) {
			p.getOpponentsList().clear();
			p.getListOfVictories().clear();
		}
		players.remove(findPlayerByName("BYE"));
		this.setNumberOfRounds(1);
		this.roundNumber = 1;

		for (Player p : players) {
			for (Player q : players) {
				if (p != q && !activeBattleExists(currentBattles, p, q)) {
					currentBattles.add(new Battle(p, q));
				}
			}
		}
		assignTableNumbers(currentBattles);
	}

	private boolean activeBattleExists(ArrayList<Battle> battles, Player p, Player q) {
		logger.info("noSuchPairing");
		boolean exists = false;
		for (Battle b : battles) {
			if ((b.getP1().equals(p) && b.getP2().equals(q)) || (b.getP1().equals(q) && b.getP2().equals(p))) {
				exists = true;
			}
		}
		return exists;
	}

	void setTopCut(int parseInt) {
		topCutThreshold = parseInt;
	}

	public void addBatch(String playerList) {
		logger.info("addBatch: " + playerList);
		String[] names = playerList.split(",");
		ArrayList<String> newPlayerNames = new ArrayList<>();
		for (String s : names) {
			newPlayerNames.add(s);
		}
		for (String s : newPlayerNames) {
			addPlayer(Utils.trimWhitespace(s));
			postListOfConfirmedSignups();
		}
		addBye();
	}

	public void renamePlayer(String playerToRename, String newName) {
		logger.info("renamePlayer: " + playerToRename + " --> " + newName);
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

	public void dropPlayer(String nameToDrop) {
		logger.info("dropPlayer: " + nameToDrop);
		dropped.add(nameToDrop);

		for (Battle b : currentBattles) {
			if (b.contains(nameToDrop)) {
				if (b.getP1().getName().equals(nameToDrop)) {
					b.getP2().beats(b.getP1());
				} else {
					b.getP1().beats(b.getP2());
				}
				currentBattles.remove(b);
				GUI.pairingsBox.setText(getCurrentBattles(currentBattles, roundString));
				updateParticipantStats();
				GUI.resultsBox.setText(generateInDepthRankings(players) + "\n");
				break;
			}
		}
		if (!doesPlayerExist("BYE")) {
			players.add(new Player("BYE"));
		}
		if (activePlayerSize() % 2 == 1 && !dropped.contains("BYE")) {
			dropped.add("BYE");
		}
		if (activePlayerSize() % 2 == 1 && dropped.contains("BYE")) {
			dropped.remove("BYE");
		}
	}

	private int activePlayerSize() {
		return players.size() + -dropped.size();
	}

	public void alterTopCut(String newSize) throws NumberFormatException {
		logger.info("alterTopCut: " + newSize);
		try {
			int tC = Integer.parseInt(newSize);
			if (tC < activePlayerSize()) {
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
		return activePlayerSize();
	}

	public void addPlayer(Player p) {
		players.add(p);
	}

	public void setNumberOfRounds(int newNumberOfRounds) {
		numberOfRounds = newNumberOfRounds;
	}

	public int getNumberOfRounds() {
		return numberOfRounds;
	}

	public static int getTopCutThreshold() {
		return topCutThreshold;
	}

	public static void setTopCutThreshold(int topCutThreshold) {
		logger.info("setTopCutThreshold: " + topCutThreshold);
		Tournament.topCutThreshold = topCutThreshold;
	}

	public void alterRoundNumbers(String newMax) throws NumberFormatException {
		logger.info("alterRoundNubers: " + newMax);
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

	public Boolean reopenBattle(Player p1, Player p2) {
		logger.info("reopenBattle: " + p1.getName() + "-" + p2.getName());
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

		logger.info("getResultsOfAllMatchesSoFar");
		String results = "";
		for (Player p : players) {
			for (Player iBeat : p.getListOfVictories()) {
				results += p.getName() + " vs. " + iBeat.getName() + " (" + p.getName() + " won)\n";
			}
		}
		for (Player p : players) {
			for (Player didWeTie : p.getOpponentsList()) {
				if (!(didWeTie.getListOfVictories().contains(p) || p.getListOfVictories().contains(didWeTie))
						&& p.getOpponentsList().contains(didWeTie) && didWeTie.getOpponentsList().contains(p)) {
					results += p.getName() + " vs. " + didWeTie.getName() + " (Tied)\n";
				}
			}
		}
		return results;
	}

	public void setAllParticipantsIn(boolean b) {
		allParticipantsIn = b;
	}

	public void reportBattleWinner(String text) {
		logger.info("reportBattleWinner: " + text);
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

	public String postTournamentAwards() throws IndexOutOfBoundsException {
		logger.info("postTournamentAwards");
		String output = "";
		try {
			Player p1 = fetchHardestFoughtPlayer();
			Player p2 = fetchHighestSTBPlayer();
			output += "Congratulations to " + players.get(0).getName() + " on winning this tournament!\n";
			output += "Props to " + p1.getName() + " for enduring the toughest range of opponents.\n";
			output += "Shoutout to " + p2.getName()
					+ " for generally playing against opponents on top of their peer group.";
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Exception thrown: Tried to access unavailable player.");
		}
		return output;
	}

	private Player fetchHighestSTBPlayer() {
		logger.info("fetchHighestSTBPlayer");
		int highestSTB = 0;
		Player topSTB = null;
		for (Player p : players) {
			if (p.getSTB() >= highestSTB && !p.getName().equals("BYE") && p.isDropped == false) {
				topSTB = p;
				highestSTB = p.getSTB();
			}
		}
		return topSTB;
	}

	private Player fetchHardestFoughtPlayer() {
		logger.info("fetchHardestFoughtPlayer");
		int highestOWR = 0;
		Player hardest = null;
		for (Player p : players) {
			if (p.getOppWr() > highestOWR && !p.getName().equals("BYE") && p.isDropped == false) {
				hardest = p;
				highestOWR = p.getOppWr();
			}
		}
		return hardest;
	}

	public String readInput() {
		logger.info("readInput");
		String uS = userSelection;
		userSelection = null;
		noClicks = true;
		return uS;
	}

	public String playerList() {
		logger.info("playerList");
		String names = "";
		for (Player p : players) {
			names += p.getName();
			names += ",";
		}
		names = names.substring(0, names.length() - 1);
		return names;

	}

	public void postTourneyProcessing() {
		logger.info("postTourneyProcessing");
		GUI.postString("FINAL STANDINGS");
		updateParticipantStats();
		GUI.postString(generateInDepthRankings(players));
		GUI.postString(postTournamentAwards());
		GUI.postString(predictionAnalysis());

		if (topCutThreshold > 1) {
			GUI.postString("Should we progress to the top cut of " + topCutThreshold + "? (y/n)");
			waitForUserInput();
			String input = readInput();
			if (input.toLowerCase().charAt(0) == 'y') {
				activeMetadataFile = activeMetadataFile.replace(".", "-topCut.");
				ArrayList<String> topCut = new ArrayList<String>();
				for (int i = 0; i < topCutThreshold; i++) {
					topCut.add(players.get(i).getName());
				}
				players.clear();
				for (String player : topCut) {
					addPlayer(player);
				}
				roundNumber = 1;
				numberOfRounds = logBase2(players.size());
				topCutThreshold = 0;
				dropped.clear();
				run();
			} else {
				GUI.postString("Thanks to everyone for taking part!");
			}
		}
	}

	private String predictionAnalysis() {
		String s = "";
		s += "Over this tournament, " + predictionsMade + " match result predictions were made.\n";
		s += "Of these, " + correctPredictions + " were correct.";
		return s;
	}

	public void run() {
		logger.info("run");
		while (roundNumber <= getNumberOfRounds() && activePlayerSize() > 1) {
			Collections.shuffle(players);
			GUI.wipePane();
			updateParticipantStats();

			sortRankings();
			if (roundNumber == 1) {
				shufflePlayers();
			}
			generatePairings(0);
			assignTableNumbers(currentBattles);
			save();
			sortRankings();
			gui.refreshReportResults(gui.seedPanel);
			GUI.postResultsString(generateInDepthRankings(players));
			GUI.pairingsBox.setCaretPosition(0);
			pollForResults();
			if (elimination > 0) {
				for (Player p : players) {
					if (p.getListOfNamesPlayed().size() - p.getListOfNamesBeaten().size() >= elimination) {
						dropPlayer(p.getName());
					}
				}
			} else {
				roundNumber++;
			}
		}

		save();
		GUI.wipePane();
		postTourneyProcessing();
	}

	void save() {
		logger.info("save");
		tntfm.saveTournament();
	}

	public void initialSeed(Player p1, Player p2) {
		logger.info("initialSeed: " + p1.getName() + "-" + p2.getName());
		Battle b = new Battle(p1, p2);
		b.wasSeeded = true;
		currentBattles.add(b);
	}

	public String getElo() {
		return elo;
	}

	public void setElo(String elo) {
		this.elo = elo;
	}

	public void setUpLogger() {
		try {
			fh = new FileHandler("BTCLogFile-" + activeMetadataFile.replace(".tnt", "") + ".log");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.addHandler(fh);
		SimpleFormatter formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
	}

	public static Player findPlayerByName(String s) {
		for (Player p : players) {
			if (p.getName().equals(s)) {
				return p;
			}
		}
		return null;
	}

	public void setUpLoggers() {
		setUpLogger();
		gui.setUpLogger();
	}

	public Battle findBattleByPlayerName(String text) {
		for (Battle b : currentBattles) {
			if (b.getP1().getName().equals(text) || b.getP2().getName().equals(text)) {
				return b;
			}
		}
		return null;
	}
}