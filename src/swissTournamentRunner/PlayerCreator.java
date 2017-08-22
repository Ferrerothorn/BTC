package swissTournamentRunner;

public class PlayerCreator {

	private Tournament t;

	public PlayerCreator(Tournament tourney) {
		t = tourney;
	}

	public void capturePlayers() {
		GUI.wipePane();
		while (!t.allParticipantsIn) {
			t.print("Enter the name of the next participant, or enter 'no' if done. ");
			t.print("You can enter 'help' at any time for some instructions, or 'drop' to remove a player you registered before we begin.");
			t.print("Current Participants: " + (t.players.size() + (t.currentBattles.size() * 2))
					+ "  Rounds required: " + t.logBase2(t.players.size()));
			t.print("");
			t.waitForUserInput();
			String input = t.readInput();
			processPlayerName(input);
		}
		t.addBye();
	}

	public void processPlayerName(String input) throws NumberFormatException{
		try {
		switch (input.toLowerCase()) {
		case "help":
			Utils.showHelp();
			break;
		case "drop":
			t.print("Enter the player number (shown below) of the player you'd like to remove.");
			t.waitForUserInput();
			int dropMe = Integer.parseInt(t.readInput());
			dropMe--;
			if (dropMe >= 0 && dropMe < t.players.size()) {
				Player p = t.players.remove(dropMe);
				t.print("Removed " + p.getName() + ".");
			} else {
				t.print("Player at index " + dropMe + 1 + " does not exist.");
			}
			t.postListOfConfirmedSignups();
			break;
		case "no":
			t.allParticipantsIn = true;
			break;
		default:
			if (input.contains(",")) {
				t.addBatch(input);
			} else {
				t.addPlayer(input);
			}
		}
		}
		catch (NumberFormatException nfe){
			t.print("Illegal input - too long to constitute an integer.");
			processPlayerName(input);
		}
	}
}
