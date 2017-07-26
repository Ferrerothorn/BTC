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
			t.print("You can enter 'help' at any time for some instructions.)");
			t.print("Current Participants: " + t.players.size() + "  Rounds required: " + t.logBase2(t.players.size()));
			t.print("");
			t.waitForUserInput();
			switch (t.userSelection.toLowerCase()) {
			case "help":
				t.userSelection = null;
				Utils.showHelp();
				break;
			case "drop":
				break;
			case "no":
				t.allParticipantsIn = true;
				t.userSelection = null;
				break;
			default:
				if (t.userSelection.contains(",")) {
					t.addBatch(t.userSelection);
				} else {
					t.addPlayer(t.userSelection);
				}
				t.userSelection = null;
			}
		}
		t.addBye();
	}
}
