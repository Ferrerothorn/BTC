package swissTournamentRunner;

public class PlayerCreator {

	private final Tournament t;

	public PlayerCreator(Tournament tourney) {
		t = tourney;
	}

	public void capturePlayers() {
		GUI.wipePane();
		while (!t.allParticipantsIn) {
			int playerNumbers = (t.getPlayers().size() + (t.currentBattles.size() * 2));
			if (t.findPlayerByName("BYE") != null) {
				playerNumbers--;
			}

			t.print("Enter the name of the next participant, or participants separated by commas. ");
			t.print("While registering players, you can enter 'drop' to remove a player before beginning.");
			t.print("Click the 'start' button to begin - you can optionally seed Round 1 pairings there too.");
			t.print("Current Participants: " + playerNumbers + "  Rounds required: "
					+ t.logBase2(t.getPlayers().size()));
			t.print("");

			t.waitForUserInput();
			String input = t.readInput();
			processPlayerName(input);
		}
	}

	public void processPlayerName(String input) throws NumberFormatException {
		try {
			switch (input.toLowerCase()) {
			case "drop":
				t.print("Enter the player number (as shown below) of the player you'd like to remove.");
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
		}
	}

	private void dropPlayerByIndex(String dropMe) {
		int index = Integer.parseInt(dropMe);
		index--;
		if (index >= 0 && index < t.getPlayers().size()) {
			if (t.getPlayers().get(index).getName().equals("BYE")) {
				t.print("You can't drop the Bye. Either remove a real player to remove it, or add another player to overwrite it.");
			} else {
				Player p = t.getPlayers().remove(index);
				t.getPlayers().remove(t.findPlayerByName("BYE"));
				t.print("Removed " + p.getName() + ".");

			}
		} else {
			t.print("Player at index " + dropMe + 1 + " does not exist.");
		}
		t.postListOfConfirmedSignups();
	}
}
