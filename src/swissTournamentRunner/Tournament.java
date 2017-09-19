package swissTournamentRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Tournament {

	public ArrayList<Player> players = new ArrayList<>();
	public ArrayList<Battle> currentBattles = new ArrayList<>();
	public ArrayList<Battle> totallyKosherPairings = new ArrayList<>();
	TntFileManager tntfm = new TntFileManager(this);
	static String roundString;
	private String userSelection = null;
	private boolean noClicks = true;
	private String elo = "off";
	private String sortElo = "off";
	boolean allParticipantsIn = false;
	public static int topCutThreshold = 0;
	public int numberOfRounds;
	public int roundNumber = 1;
	public GUI gui;
	public int x_elimination = 99999;
	public Boolean isElimination = false;
	public String activeMetadataFile = "TournamentInProgress.tnt";

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
		if (containsPlayer("BYE")) {
			renamePlayer("BYE", p1);
		}
		if (!containsPlayer(p1)) {
			if (p1.length() > 0) {
				players.add(new Player(p1));
			}
		}
		while (numberOfRounds < (logBase2(players.size() + currentBattles.size() * 2))) {
			numberOfRounds++;
		}
		if (!allParticipantsIn) {
			postListOfConfirmedSignups();
		}
	}

	public void postListOfConfirmedSignups() {
		Collections.sort(players);
		String post = "-=-=-Registered: " + (players.size() + 2 * currentBattles.size()) + " players. -=-=-" + "\n";
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
			output += p.getName();
		}
		return output;
	}

	public boolean containsPlayer(String string) {
		for (Player p : players) {
			if (p.getName().equals(string)) {
				return true;
			}
		}
		for (Battle b : currentBattles) {
			if (b.getP1().getName().equals(string)) {
				return true;
			}
			if (b.getP2().getName().equals(string)) {
				return true;
			}
		}
		return false;
	}

	public void addBye() {
		if (players.size() % 2 != 0) {
			players.add(new Player("BYE"));
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

		// If there are no active battles, that means we're between rounds, and
		// need pairings.
		// Otherwise, if there are active battles, check that nobody has a
		// score. If not, it's because they were just initially seeded.
		if (currentBattles.size() == 0 || activeGamesWereSeeded(currentBattles)) {

			while (players.size() > 0 && attempts <= 100) {
				Player p1 = players.remove(0);
				pairThisGuyUp(p1, currentBattles, attempts);
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
		try {
			boolean opponentFound = false;
			int playerIndex = 0;

			while (!opponentFound) {
				Player temp = players.get(playerIndex);
				if (isElimination || (!p1.getOpponentsList().contains(temp) && !temp.getOpponentsList().contains(p1))) {
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
		assignTableNumbers(currentBattles);

		while (currentBattles.size() > 0 && allParticipantsIn) {
			roundString = ("-=-=-=-ROUND " + roundNumber + "/" + numberOfRounds + "-=-=-=-");
			print("Enter a table number to report a score for the game.");
			tntfm.saveTournament();

			try {
				GUI.printCurrentBattles(currentBattles, roundString);
				GUI.pairingsBox.setCaretPosition(GUI.pairingsBox.getText().length());

				waitForUserInput();
				String input = readInput();

				switch (input) {

				case "help":
					Utils.showHelp();
					break;
				case "admintools":
					adminTools();
					break;
				default:
					int reportUpon = Integer.parseInt(input);
					Battle b = fetchBattle(reportUpon, currentBattles);
					currentBattles.remove(b);

					print("And who won in " + b.getP1().getName() + " vs. " + b.getP2().getName() + "?");
					print("1) " + b.getP1().getName() + " (" + b.getElo(b.getP1()) + "% predicted win rate)");
					print("2) " + b.getP2().getName() + " (" + b.getElo(b.getP2()) + "% predicted win rate)");
					print("0) Tied.");

					if (!((b.getP1().getName().equals("BYE") || (b.getP2().getName().equals("BYE"))))) {
						waitForUserInput();
						String winner = readInput();
						if (winner.equals("1") || winner.equals("2") || winner.equals("0")) {
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
				print("Illegal input.");
				pollForResults();
			}
			GUI.pairingsBox.setCaretPosition(GUI.pairingsBox.getText().length());
			tntfm.saveTournament();
		}
	}

	public void refreshScreen() {
		GUI.wipePane();
		updateParticipantStats();
		printRankings(generateInDepthRankings(players));
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
			String s = new String("");
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

	public void adminTools() {
		print("Admin functions enabled.");
		waitForUserInput();
		String adminCommand = readInput();

		switch (adminCommand.toLowerCase()) {
		case "elo":
			setElo(toggle(elo));
			if (getElo().equals("on")) {
				print("ELO switched on");
			} else {
				print("ELO switched off");
			}
			break;
		case "topcut":
			print("Enter the number of players that constitutes a Top Cut for this tournament.\n");
			print("(Must be less than the number of players.)\n");
			print("Alternatively, enter '0' to remove the Top Cut.\n");
			waitForUserInput();
			int tC = Integer.parseInt(readInput());
			if (tC < players.size()) {
				setTopCut(tC);
			} else {
				print("Invalid - suggested top cut size is too large.");
			}
			break;
		case "matchesof":
			print("Enter player whose game history you'd like to see.\n");
			waitForUserInput();
			String showHistory = readInput();
			Player p = Utils.findPlayerByName(showHistory, players);
			printHistory(p);
			GUI.pairingsBox.setCaretPosition(GUI.pairingsBox.getText().length());
			break;
		case "roundrobin":
			generateRRpairings();
			break;
		case "sortelo":
			setSortElo(toggle(getSortElo()));
			GUI.wipePane();
			if (getSortElo().equals("on")) {
				print("Active battles ordered by ELO difference.");
				GUI.printCurrentBattles(currentBattles, roundString);
			} else {
				print("Stopping sorting ongoing battles by ELO difference.");
			}
			break;
		case "load":
			print("Enter the file name to load.\n");
			waitForUserInput();
			String fileName = readInput();
			if (!fileName.contains(".tnt")) {
				fileName += ".tnt";
			}
			File loadFrom = new File(fileName);
			if (loadFrom.exists()) {
				try {
					TntFileManager.loadTournament(this, fileName);
					refreshScreen();
				} catch (IOException e) {
					print("Error loading file.");
				}
			} else {
				print("That file doesn't exist - check again.");
			}
			break;
		case "matches":
			print(getResultsOfAllMatchesSoFar());
			break;
		case "addround":
			print("Enter the new number of desired rounds for the tournament.\n");
			waitForUserInput();
			int newNumOfRounds = Integer.parseInt(readInput());
			if (newNumOfRounds < players.size() && newNumOfRounds >= logBase2(players.size())) {
				setNumberOfRounds(newNumOfRounds);
				print("Number of rounds updated to " + getNumberOfRounds() + ".");
			} else {
				print("Invalid number of rounds for a Swiss tournament.");
				print("We need to have less rounds than the number of players, and at least logBase2(number of players).");
			}
			break;
		case "drop":
			print("Enter player name to drop.\n");
			waitForUserInput();
			dropPlayer(readInput());
			break;
		case "dropplayer":
			print("Enter player name to drop.\n");
			waitForUserInput();
			dropPlayer(readInput());
			break;
		case "dropuser":
			print("Enter player name to drop.\n");
			waitForUserInput();
			dropPlayer(readInput());
			break;
		case "editname":
			print("Enter player whose name should be changed.\n");
			waitForUserInput();
			String renameMe = readInput();
			print("Enter player's new name.\n");
			waitForUserInput();
			String newName = readInput();
			renamePlayer(renameMe, newName);
			break;
		case "addbatch":
			print("Enter a list of players, separated by commas.\n");
			waitForUserInput();
			String playersList = readInput();
			addBatch(playersList);
			break;
		case "addplayer":
			print("Enter a list of players, separated by commas.\n");
			waitForUserInput();
			String playerList = readInput();
			addBatch(playerList);
			break;
		case "reopengame":
			print("To reopen a game, first enter the name of one of the players in the game.\n");
			print("(Case sensitive)\n");
			waitForUserInput();
			Player p1 = Utils.findPlayerByName(readInput(), players);
			if (p1.getOpponentsList().size() == 0) {
				print("That player hasn't played any games yet.\n");
				break;
			} else {
				print("Reopen game with which opponent? \n");
				for (int i = 1; i <= p1.getOpponentsList().size(); i++) {
					print("" + i + ") " + p1.getOpponentsList().get(i - 1).getName());
				}
				waitForUserInput();
				int reopenIndex = Integer.parseInt(readInput());
				Player p2 = p1.getOpponentsList().get(reopenIndex - 1);
				reopenBattle(p1, p2);
			}
			break;
		case "killall -9":
			currentBattles.clear();
			players.clear();
			break;
		case "elimination":
			print("To convert to X-Elimination, please first enter the number of losses after which a player is eliminated.\n");
			waitForUserInput();
			x_elimination = Integer.parseInt(readInput());
			print("Players will be eliminated after " + x_elimination + " losses.");
			currentBattles.clear();
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

	private void printHistory(Player p) {
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
	}

	private void generateRRpairings() {
		currentBattles.clear();
		for (Player p : players) {
			p.getOpponentsList().clear();
			p.getListOfVictories().clear();
		}
		this.setNumberOfRounds(1);
		this.roundNumber = 1;

		for (Player p : players) {
			for (Player q : players) {
				if (p != q && noSuchPairing(currentBattles, p, q)) {
					currentBattles.add(new Battle(p, q));
				}
			}
		}
		assignTableNumbers(currentBattles);
	}

	private boolean noSuchPairing(ArrayList<Battle> battles, Player p, Player q) {
		boolean doesntExist = true;
		for (Battle b : battles) {
			if ((b.getP1().equals(p) || b.getP2().equals(p)) && (b.getP1().equals(q) || b.getP2().equals(q))) {
				doesntExist = false;
			}
		}
		return doesntExist;
	}

	void setTopCut(int parseInt) {
		topCutThreshold = parseInt;
	}

	public void addBatch(String playerList) {
		String[] names = playerList.split(",");
		ArrayList<String> newPlayerNames = new ArrayList<>();
		for (String s : names) {
			newPlayerNames.add(s);
		}

		if (containsPlayer("BYE")) {
			renamePlayer("BYE", newPlayerNames.remove(0));
		}

		for (String s : newPlayerNames) {
			addPlayer(Utils.trimWhitespace(s));
			postListOfConfirmedSignups();
		}
		if (allParticipantsIn) {
			addBye();
		}
	}

	public void renamePlayer(String renameMe, String newName) {
		for (Player p : players) {
			if (p.getName().equals(renameMe)) {
				p.setName(newName);
				break;
			}
		}
		for (Battle b : currentBattles) {
			if (b.getP1().equals(renameMe)) {
				b.getP1().setName(newName);
				break;
			} else if (b.getP2().equals(renameMe)) {
				b.getP2().setName(newName);
				break;
			}
		}
	}

	public void dropPlayer(String nameToDrop) {

		Boolean foundPlayerToDrop = false;
		for (Battle b : currentBattles) {
			if (b.getP1().getName().equals(nameToDrop) && b.getP2().getName().equals("BYE")) {
				currentBattles.remove(b);
				players.remove(b.getP1());
				players.remove(b.getP2());
				foundPlayerToDrop = true;
				break;
			} else if (b.getP2().getName().equals(nameToDrop) && b.getP1().getName().equals("BYE")) {
				currentBattles.remove(b);
				players.remove(b.getP1());
				players.remove(b.getP2());
				foundPlayerToDrop = true;
				break;
			} else if (b.getP2().getName().equals(nameToDrop) || b.getP1().getName().equals(nameToDrop)) {
				foundPlayerToDrop = true;
				print("You can't drop a player while that player's in a non-Bye battle.");
				break;
			}
		}
		if (!foundPlayerToDrop) {
			Player toDrop = Utils.findPlayerByName(nameToDrop, players);
			if (toDrop != null) {
				players.remove(toDrop);
			}
		}

		if (topCutThreshold >= players.size()) {
			topCutThreshold = 0;
		}

		if (!nameToDrop.equals("BYE") && (players.size() % 2 == 1) && !containsPlayer("BYE")) {
			addPlayer("BYE");
		} else if (!nameToDrop.equals("BYE")) {
			dropPlayer("BYE");
		}

		if ((players.size() % 2 == 1) && containsPlayer("BYE")) {
			Battle byeMatch = null;
			for (Battle b : currentBattles) {
				if (b.getP1().getName().equals("BYE")) {
					byeMatch = b;
					b.getP2().beats(b.getP1());
					b = null;
				} else if (b.getP2().getName().equals("BYE")) {
					byeMatch = b;
					b.getP1().beats(b.getP2());
					b = null;
				}
			}
			if (byeMatch != null) {
				currentBattles.remove(byeMatch);
			}
			dropPlayer("BYE");
		}

		if (!isElimination) {
			while (numberOfRounds > players.size()) {
				numberOfRounds--;
			}
		}
	}

	public int size() {
		return players.size();
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

	public void reopenBattle(Player p1, Player p2) {
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
	}

	public String getResultsOfAllMatchesSoFar() {
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

	public void elimination() {
		Eliminator elim = new Eliminator(players, this);
		elim.eliminate();
	}

	public void setAllParticipantsIn(boolean b) {
		allParticipantsIn = b;
	}

	public void reportBattleWinner(String text) {
		Player winner = Utils.findPlayerByName(text, players);
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
		int highestSTB = 0;
		Player topSTB = null;
		for (Player p : players) {
			if (p.getSTB() >= highestSTB) {
				topSTB = p;
				highestSTB = p.getSTB();
			}
		}
		return topSTB;
	}

	private Player fetchHardestFoughtPlayer() {
		int highestOWR = 0;
		Player hardest = null;
		for (Player p : players) {
			if (p.getOppWr() > highestOWR) {
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
		GUI.postString(postTournamentAwards());

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
			sortRankings();
			GUI.postResultsString(generateInDepthRankings(players));
			pollForResults();
			if (isElimination) {
				elimination();
			} else {
				roundNumber++;
			}
		}

		tntfm.saveTournament();
		GUI.wipePane();
		postTourneyProcessing();
	}

	public void initialSeed(Player p1, Player p2) {
		Battle b = new Battle(p1, p2);
		b.wasSeeded = true;
		currentBattles.add(b);
	}

	public String getElo() {
		return elo;
	}

	public String getSortElo() {
		return sortElo;
	}

	public void setElo(String elo) {
		this.elo = elo;
	}

	public void setSortElo(String sortElo) {
		this.sortElo = sortElo;
	}
}