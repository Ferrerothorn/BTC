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
		tourney.run();
	}

	private static void showCredits() {
		GUI.postString("Welcome to FFTCG BTC, the Final Fantasy TCG Swiss Tournament Bracket Organiser!");
		GUI.postString("(Summer 2022 Edition - Made by Steve Dolman)");
	}
}
