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
			t.print("You can enter 'help' mid-tournament to access the user manual.");
			t.print("While registering players, enter 'drop' to remove a player before we begin, or enter 'seed' to begin the tournament while choosing [some] Round 1 pairings.");
			t.print("Current Participants: " + (t.players.size() + (t.currentBattles.size() * 2))
					+ "  Rounds required: " + t.logBase2(t.players.size()));
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
		case "seed":
			t.print("Pick Player 1 for an initial pairing. (Numbers shown below)");
			t.waitForUserInput();
			int pairP1 = Integer.parseInt(t.readInput());
			pairP1--;
			t.print("\nSeed #" + (pairP1+1) + " chosen as " + t.players.get(pairP1).getName() + ".");

			if (pairP1 < 0 || pairP1 > t.players.size()) {
				t.print("Invalid input - initial seeding aborted.");
				break;
			}

			t.print("Pick Player 2 for an initial pairing. (Numbers shown below)");
			t.waitForUserInput();
			int pairP2 = Integer.parseInt(t.readInput());
			pairP2--;

			if (pairP2 < 0 || pairP2 > t.players.size()) {
				t.print("Invalid input - initial seeding aborted.");
				break;
			}

			if (pairP1 != pairP2) {
				t.updateParticipantStats();
				Player p1 = t.players.remove(pairP1);
				pairP2--;
				Player p2 = t.players.remove(pairP2);
				t.initialSeed(p1, p2);
				t.print("\nSeeded " + p1.getName() + " with " + p2.getName() + " for Round 1.");
				t.postListOfConfirmedSignups();
				t.print("Seed another pair? (y/n)");
				t.waitForUserInput();
				String confirmInitialSeedingDone = t.readInput();
				if (confirmInitialSeedingDone.toLowerCase().charAt(0) == 'y') {
					processPlayerName("seed");
				} else {
					processPlayerName("no");
				}
			} else {
				t.print("You can't pair someone with themself - initial seeding aborted.");
			}
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
