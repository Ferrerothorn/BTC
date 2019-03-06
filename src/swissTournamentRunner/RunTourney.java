package swissTournamentRunner;

public class RunTourney {

	static Tournament tourney = new Tournament();

	public static void main(String[] args) {
		GUI gui = new GUI(tourney);
		tourney.setGUI(gui);
		GUI.createAndShowGUI(true);

		showCredits();
		tourney.signUpPlayers();
		tourney.allParticipantsIn = true;
		tourney.save();
		tourney.run();
	}

	private static void showCredits() {
		GUI.postString("Welcome to B-T-C, the Swiss Tournament Bracket Organiser!");
		GUI.postString("(Spring 2019 Edition - Made by Steve Dolman)");
		GUI.postString("Shoutout to Sera-Jane Cooley, Rachel Dolman, and Darren Macey for help in testing and debug!");
	}
}
