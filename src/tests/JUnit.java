package tests;

import org.junit.Before;
import swissTournamentRunner.GUI;
import swissTournamentRunner.Tournament;

public class JUnit {

	public Tournament t = new Tournament();

	@Before
	public void setup() {
		t.getPlayers().clear();
		t.currentBattles.clear();
		t.setAllParticipantsIn(true);
		new GUI(t);
	}
}
