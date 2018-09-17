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
			t.print("Current Participants: " + playerNumbers + "  Rounds required: " + t.logBase2(t.players.size())); 
		    t.print("");
			t.print("START BUTTON XD");
			
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
				t.print("Enter the player number (shown below)  of the player(s) you'd like to remove.");
				t.waitForUserInput();
				String dropMe = t.readInput();
				if (!dropMe.contains(",")) {
					dropPlayerByIndex(dropMe);
					break;
				} else {
					String[] indexes = dropMe.split(",");
					for (String s : indexes) {
						s = Utils.trimWhitespace(s);
					}
					for (String s : indexes) {
						dropPlayerByIndex(s);
					}
					break;
				}
			case "no":
			//	t.allParticipantsIn = true;
				break;
			case "seed":
				t.print("Pick Player 1 for an initial pairing. (Numbers shown below)");
				t.waitForUserInput();
				int pairP1 = Integer.parseInt(t.readInput());
				pairP1--;
				Player p1 = Tournament.players.get(pairP1);
				t.print("\nSeed chosen as " + p1.getName() + ".");

				if (pairP1 < 0 || pairP1 > Tournament.players.size()) {
					t.print("Invalid input - initial seeding aborted.");
					break;
				}

				t.print("Pick Player 2 for an initial pairing. (Numbers shown below)");
				t.waitForUserInput();
				int pairP2 = Integer.parseInt(t.readInput());
				pairP2--;
				Player p2 = Tournament.players.get(pairP2);

				if (pairP2 < 0 || pairP2 > Tournament.players.size()) {
					t.print("Invalid input - initial seeding aborted.");
					break;
				}

				if (pairP1 != pairP2) {
					t.updateParticipantStats();
					Tournament.players.remove(p1);
					Tournament.players.remove(p2);
					t.initialSeed(p1, p2);
					t.print("\nSeeded " + p1.getName() + " with " + p2.getName() + " for Round 1.");
					t.postListOfConfirmedSignups();
					t.print("Seed another pair, or enter 'no' to begin the tournament.");
					processPlayerName("seed");
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
