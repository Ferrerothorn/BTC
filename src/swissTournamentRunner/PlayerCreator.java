package swissTournamentRunner;

public class PlayerCreator {

	private Tournament t;

	public PlayerCreator(Tournament tourney) {
		t = tourney;
	}

	public void capturePlayers() {
		GUI.wipePane();
		while (!t.allParticipantsIn) {
			int playerNumbers = (Tournament.players.size() + (t.currentBattles.size() * 2));
			if (Tournament.findPlayerByName("BYE") != null) {
				playerNumbers--;
			}

			t.print("Enter the name of the next participant, or enter 'no' if done. ");
			t.print("While registering players, enter 'drop' to remove a player before we begin, or enter 'seed' to begin the tournament while choosing [some] Round 1 pairings.");
			t.print("Current Participants: " + playerNumbers + "  Rounds required: "
					+ t.logBase2(Tournament.players.size()));
			t.print("");

			t.waitForUserInput();
			String input = t.readInput();
			processPlayerName(input);
		}
		t.addBye();
	}

	public void processPlayerName(String input) throws NumberFormatException {
		try {
			switch (input.toLowerCase()) {
			case "drop":
				t.print("Enter the player number (shown below) of the player(s) you'd like to remove.");
				t.waitForUserInput();
				String dropMe = t.readInput();
				dropPlayerByIndex(dropMe);
				break;
			case "no":
				break;
			default:
				if (input.contains(",")) {
					t.addBatch(input);
				} else {
					t.addPlayer(input);
				}
				break;
			}
		} catch (NumberFormatException nfe) {
			t.print("Illegal input - that's not a usable player index.");
			t.print("Do you want to seed another pair?");
			t.waitForUserInput();
			String confirmInitialSeedingDone = t.readInput();
			if (confirmInitialSeedingDone.toLowerCase().charAt(0) == 'y') {
				processPlayerName("seed");
			} else {
				processPlayerName("no");
			}
		}
	}

	private void dropPlayerByIndex(String dropMe) {
		int index = Integer.parseInt(dropMe);

		index--;
		if (index >= 0 && index < Tournament.players.size()) {
			Player p = Tournament.players.remove(index);
			t.print("Removed " + p.getName() + ".");
		} else {
			t.print("Player at index " + dropMe + 1 + " does not exist.");
		}
		t.postListOfConfirmedSignups();
	}
}
