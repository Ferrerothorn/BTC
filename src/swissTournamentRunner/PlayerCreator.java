package swissTournamentRunner;

public class PlayerCreator {

	private Tournament t;

	public PlayerCreator(Tournament tourney) {
		t = tourney;
	}

	public void processPlayer(String input, int score, int OWR) throws NumberFormatException {
		try {
			switch (input.toLowerCase()) {
			case "drop":
				t.print("Enter the player number (as shown below) of the player you'd like to remove.");
				t.waitForUserInput();
				String dropMe = t.readInput();
				dropPlayerByIndex(dropMe);
				break;
			default:
				
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
