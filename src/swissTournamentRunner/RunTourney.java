package swissTournamentRunner;

public class RunTourney {

	static Tournament tourney = new Tournament();

	public static void main(String[] args) {
		GUI gui = new GUI(tourney);
		tourney.setGUI(gui);
		GUI.createAndShowGUI(true);

		showCredits();
		tourney.signUpPlayers();
		tourney.setUpLoggers();
		tourney.save();
		tourney.run();
	}

	private static void showCredits() {
		GUI.postString("Welcome to B-T-C, the Swiss Tournament Bracket Organiser!");
		GUI.postString("(2018 Edition, version 1.3 - Made by Steve Dolman.)");
		GUI.postString("Shoutout to Rachel Dolman and Darren Macey for help in testing and debug - #NAK4LYF!");
		GUI.postString("Couldn't have done it without my muse, Sera-Jane Cooley. :)");
	}
}
