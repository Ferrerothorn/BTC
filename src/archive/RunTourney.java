package archive;

public class RunTourney {

	static Tournament tourney = new Tournament();

	public static void main(String[] args) {

		GUI gui = new GUI(tourney);
		tourney.setGUI(gui);
		GUI.createAndShowGUI(true);

		showCredits();
		tourney.signUpPlayers();
		tourney.allParticipantsIn = true;

		while (tourney.roundNumber <= tourney.getNumberOfRounds() && tourney.players.size() > 1) {
			GUI.wipePane();
			tourney.shufflePlayers();
			tourney.updateParticipantStats();
			tourney.sortRankings();
			GUI.postResultsString(GUI.generateInDepthRankings(tourney.players));
			tourney.generatePairings(0);
			tourney.pollForResults();
			// while (!(tourney.confirmPhase())) {
			// }
			if (tourney.isElimination) {
				tourney.elimination();
			} else {
				tourney.roundNumber++;
			}
		}
		TntFileManager.saveTournament(tourney);
		GUI.wipePane();
		GUI.postString("FINAL STANDINGS");
		tourney.updateParticipantStats();
		GUI.postString(GUI.generateInDepthRankings(tourney.players));
	}

	private static void showCredits() {
		GUI.postString("Welcome to B-T-C, the Swiss Tournament Bracket Organiser!");
		GUI.postString("(Version 8.3 - Made by Steve Dolman)");
		GUI.postString(
				"Shoutout to Rachel Dolman, Darren Macey, and Steve Dolman (another one) for help in testing and debug.");
	}
}
