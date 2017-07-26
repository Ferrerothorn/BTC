package tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

import swissTournamentRunner.Battle;
import swissTournamentRunner.GUI;
import swissTournamentRunner.Player;
import swissTournamentRunner.TntFileManager;
import swissTournamentRunner.Tournament;
import swissTournamentRunner.Utils;

public class JUnit {

	public Tournament t = new Tournament();

	@Before
	public void setup() {
		t.players.clear();
		t.currentBattles.clear();
		t.setAllParticipantsIn(true);
	}

	@Test
	public void testAddPlayersToTournament() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		assertEquals(2, t.players.size());
	}

	@Test
	public void testAddingListThenAddingSinglesDoesntGiveTwoByes() {
		t.addBatch("P1,P2,P3");
		t.addPlayer("P4");
		assertEquals(4, t.players.size());
	}

	@Test
	public void testGeneratingPairingsTwiceDoesntWork() {
		t.addBatch("P1,P2,P3,P4");
		t.generatePairings(0);
		assertEquals(2, t.currentBattles.size());
		t.generatePairings(0);
		assertEquals(2, t.currentBattles.size());
	}

	@Test
	public void testOverwritingRoundNumberPersists() {
		t.addBatch("p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16");
		t.setNumberOfRounds(7);
		t.generatePairings(0);
		Utils.autocompleteRound(t.currentBattles);

		t.generatePairings(0);
		Utils.autocompleteRound(t.currentBattles);

		t.generatePairings(0);
		Utils.autocompleteRound(t.currentBattles);

		t.generatePairings(0);
		Utils.autocompleteRound(t.currentBattles);

		t.generatePairings(0);
		Utils.autocompleteRound(t.currentBattles);

		t.generatePairings(0);
		Utils.autocompleteRound(t.currentBattles);
		assertEquals(7, t.numberOfRounds);
	}

	@Test
	public void testCheckBattleTableNumber() {
		Player p1 = new Player("P1", 0, 0, 0, 0);
		Player p2 = new Player("P2", 0, 0, 0, 0);
		Player p3 = new Player("P3", 0, 0, 0, 0);
		Player p4 = new Player("P4", 0, 0, 0, 0);
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.generatePairings(0);
		t.assignTableNumbers(t.currentBattles);
		assertEquals(1, t.currentBattles.get(0).getTableNumber());
		assertEquals(2, t.currentBattles.get(1).getTableNumber());
	}

	@Test
	public void testRecalculateTBs() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		p1.beats(p2);
		p2.beats(p3);
		p3.beats(p1);
		t.updateParticipantStats();
		assertEquals(1, p1.getTB());
		assertEquals(1, p2.getTB());
		assertEquals(1, p3.getTB());
	}

	@Test
	public void testHandleBattleWinner_On3_AddsTie() {
		t.addBatch("P1,P2,P3,P4");
		t.generatePairings(0);
		Battle b = t.currentBattles.remove(0);
		Utils.handleBattleWinner(b, "2");
		b = t.currentBattles.remove(0);
		Utils.handleBattleWinner(b, "0");
		t.updateParticipantStats();
		assertEquals(0, t.findPlayerByName("P1").getScore());
		assertEquals(3, t.findPlayerByName("P2").getScore());
		assertEquals(1, t.findPlayerByName("P3").getScore());
		assertEquals(1, t.findPlayerByName("P4").getScore());
	}

	@Test
	public void testCompareIdenticalPlayersIsTie() {
		Player p1 = new Player("P1", 0, 0, 0, 0);
		Player p2 = new Player("P2", 0, 0, 0, 0);
		t.addPlayer(p1);
		t.addPlayer(p2);
		assertEquals(0, p1.compareTo(p2));
	}

	@Test
	public void testComparePlayerToByeReturnsPlayer() {
		Player p1 = new Player("P1");
		t.addPlayer(p1);
		t.addBye();
		assertEquals(-1, p1.compareTo(t.players.get(1)));
	}

	@Test
	public void testP1Better() {
		Player p1 = new Player("P1", 1, 0, 0, 0);
		Player p2 = new Player("P2", 0, 0, 0, 0);
		t.addPlayer("P1");
		t.addPlayer("P2");
		assertEquals(-1, p1.compareTo(p2));
	}

	@Test
	public void testCompareP2Better() {
		Player p1 = new Player("P1", 0, 0, 0, 0);
		Player p2 = new Player("P2", 1, 0, 0, 0);
		t.addPlayer("P1");
		t.addPlayer("P2");
		assertEquals(1, p1.compareTo(p2));
	}

	@Test
	public void testTiedSoGoToTB() {
		Player p1 = new Player("P1", 3, 1, 0, 0);
		Player p2 = new Player("P2", 3, 0, 0, 0);
		t.addPlayer("P1");
		t.addPlayer("P2");
		assertEquals(-1, p1.compareTo(p2));
	}

	@Test
	public void testAddingNoNamePlayerFails() {
		t.addPlayer("");
		assertEquals(0, t.players.size());
		assertEquals(99, t.getX_elimination());
	}

	@Test
	public void reopenBattle_P2sBattle() {
		Player p1 = new Player("P1", 0, 0, 0, 0);
		Player p2 = new Player("P2", 3, 0, 0, 0);
		t.addPlayer(p1);
		t.addPlayer(p2);
		p1.beats(p2);
		t.reopenBattle(p2, p1);
		assertEquals(0, p1.previousRounds.size());
		assertEquals(0, p2.previousRounds.size());
	}

	@Test
	public void testDropPlayers() {
		GUI gui = new GUI(t);
		Player p1 = new Player("P1", 0, 0, 0, 0);
		Player p2 = new Player("P2", 0, 0, 0, 0);
		Player p3 = new Player("P3", 0, 0, 0, 0);
		Player p4 = new Player("P4", 0, 0, 0, 0);
		t.setNumberOfRounds(3);
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.generatePairings(0);
		t.dropPlayer("P4");
		assertEquals(4, t.players.size());
		assertEquals(3, t.getNumberOfRounds());
		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		t.dropPlayer("P2");
		assertEquals(4, t.players.size());
		assertEquals(3, t.getNumberOfRounds());
		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		t.dropPlayer("P4");
		assertEquals(2, t.players.size());
		assertEquals(2, t.getNumberOfRounds());
	}

	@Test
	public void testGetRankings_GivenTournamentStructure() {
		Player p4 = new Player("P4", 0, 0, 0, 0);
		Player p3 = new Player("P3", 6, 0, 0, 0);
		Player p2 = new Player("P2", 3, 0, 0, 0);
		Player p1 = new Player("P1", 9, 0, 0, 0);
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.sortRankings();
		assertEquals(
				"-=-=-=-Rankings-=-=-=-\n"
						+ "1) P1       Score: 9          TB: 0      Opp WR: 0%      Opp Opp WR: 0%    STB: 0   \n"
						+ "2) P3       Score: 6          TB: 0      Opp WR: 0%      Opp Opp WR: 0%    STB: 0   \n"
						+ "3) P2       Score: 3          TB: 0      Opp WR: 0%      Opp Opp WR: 0%    STB: 0   \n"
						+ "4) P4       Score: 0          TB: 0      Opp WR: 0%      Opp Opp WR: 0%    STB: 0   \n",
				GUI.generateInDepthRankings(t.players));
	}

	@Test
	public void testTiedSoGoToTB_p2() {
		Player p1 = new Player("P1", 3, 0, 0, 0);
		Player p2 = new Player("P2", 3, 1, 0, 0);
		t.addPlayer("P1");
		t.addPlayer("P2");
		assertEquals(1, p1.compareTo(p2));
	}

	@Test
	public void testTiedSoGoToOppWr() {
		Player p1 = new Player("P1", 3, 1, 3, 0);
		Player p2 = new Player("P2", 3, 1, 0, 0);
		t.addPlayer("P1");
		t.addPlayer("P2");
		assertEquals(-1, p1.compareTo(p2));
	}

	@Test
	public void testTiedSoGoToOppWrp2() {
		Player p1 = new Player("P1", 3, 1, 0, 0);
		Player p2 = new Player("P2", 3, 1, 3, 0);
		t.addPlayer("P1");
		t.addPlayer("P2");
		assertEquals(1, p1.compareTo(p2));
	}

	@Test
	public void testSavingThenReloadingPersistsByeRecords() {
		t.addBatch("1,2,3,4,5,6,7");
		assertEquals(8, t.players.size());
		t.activeMetadataFile = "test.tnt";
		t.generatePairings(0);

		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		TntFileManager.saveTournament(t);

		Utils.handleBattleWinner(t.currentBattles.remove(2), "1");
		TntFileManager.saveTournament(t);

		t.currentBattles.clear();
		t.players.clear();
		try {
			TntFileManager.loadTournament(t, t.activeMetadataFile);
		} catch (IOException e) {
		}
		assertEquals(3, t.getNumberOfRounds());
		assertEquals(8, t.players.size());
		assertEquals(2, t.currentBattles.size());
		assertEquals(1, t.findPlayerByName("1").getListOfNamesPlayed().size());
		assertEquals(1, t.findPlayerByName("BYE").getListOfNamesPlayed().size());
		File file = new File("test.tnt");
		if (file.exists()) {
			file.delete();
		}
	}

	@Test
	public void testTiedSoGoToOppOppWr_P1Wins() {
		Player p1 = new Player("P1", 3, 1, 0, 1);
		Player p2 = new Player("P2", 3, 1, 0, 0);
		assertEquals(-1, p1.compareTo(p2));
	}

	@Test
	public void testTiedSoGoToOppOppWr_P2Wins() {
		Player p1 = new Player("P1", 3, 1, 0, 0);
		Player p2 = new Player("P2", 3, 1, 0, 1);
		assertEquals(1, p1.compareTo(p2));
	}

	@Test
	public void testTiedEveryTieBreaker() {
		Player p1 = new Player("P1", 3, 1, 3, 0);
		Player p2 = new Player("P2", 3, 1, 3, 0);
		t.addPlayer("P1");
		t.addPlayer("P2");
		assertEquals(0, p1.compareTo(p2));
	}

	@Test
	public void testSortFourParticipants() {
		Player p1 = new Player("P1", 3, 1, 2, 0);
		Player p2 = new Player("P2", 6, 1, 3, 0);
		Player p3 = new Player("P3", 3, 1, 3, 0);
		Player p4 = new Player("P4", 0, 0, 6, 0);
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.sortRankings();
		assertEquals("-=-=-=-Rankings-=-=-=-\nP2P3P1P4", t.rankingsToOneBigString());
	}

	@Test
	public void testAddBye_AddsNoBye() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addBye();
		assertEquals(false, t.containsPlayer("BYE"));
	}

	@Test
	public void testAddBye_AddsBye() {
		t.addPlayer("P1");
		t.addBye();
		assertEquals(true, t.containsPlayer("BYE"));
	}

	@Test
	public void testFightWinnerP1GetsPoints() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		assertEquals(0, p1.getScore());
		assertEquals(0, p2.getScore());
		p1.beats(p2);
		assertEquals(3, p1.getScore());
		assertEquals(0, p2.getScore());
	}

	@Test
	public void testFightLogsEachOtherInFightHistory() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		assertEquals(0, p1.getOpponentsList().size());
		assertEquals(0, p2.getOpponentsList().size());
		p2.beats(p1);
		assertEquals(1, p1.getOpponentsList().size());
		assertEquals(1, p2.getOpponentsList().size());
	}

	@Test
	public void testFightWinnerGetsVictoryLogged() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		assertEquals(0, p1.getListOfVictories().size());
		assertEquals(0, p2.getListOfVictories().size());
		p1.beats(p2);
		assertEquals(1, p1.getListOfVictories().size());
		assertEquals("P2", p1.getListOfVictories().get(0).getName());
		assertEquals(0, p2.getListOfVictories().size());
	}

	@Test
	public void test1beats2_2beats3_1gt2gt3() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);

		p1.beats(p2);
		p2.beats(p3);

		assertEquals(1, p1.getListOfVictories().size());
		assertEquals(1, p2.getListOfVictories().size());
		assertEquals(0, p3.getListOfVictories().size());
		assertEquals("-=-=-=-Rankings-=-=-=-\nP1P2P3", t.rankingsToOneBigString());

	}

	@Test
	public void testFiveManPeckingOrder() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		Player p4 = new Player("P4");
		Player p5 = new Player("P5");

		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.addPlayer(p5);
		t.shufflePlayers();

		p2.beats(p1);
		p3.beats(p2);
		p3.beats(p1);
		p4.beats(p3);
		p4.beats(p2);
		p4.beats(p1);
		p5.beats(p4);
		p5.beats(p3);
		p5.beats(p3);
		p5.beats(p1);

		t.sortRankings();
		assertEquals("-=-=-=-Rankings-=-=-=-\nP5P4P3P2P1", t.rankingsToOneBigString());

	}

	@Test
	public void testPairEveryoneCorrectlyThreeRoundsIn() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addPlayer("P4");
		t.addPlayer("P5");
		t.addBye();

		t.shufflePlayers();
		t.sortRankings();
		t.generatePairings(0);
		assertEquals(3, t.currentBattles.size());
	}

	@Test
	public void testUpdatePositionInRankings() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		t.addPlayer(p1);
		t.addPlayer(p2);
		p1.beats(p2);
		t.sortRankings();
		t.updateParticipantStats();
		assertEquals(1, p1.getPositionInRankings());
	}

	@Test
	public void testDroppingNonByeUserRemovesBye() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addBye();
		p1.beats(p2);
		t.dropPlayer("P2");
		assertEquals(2, t.size());
	}

	@Test
	public void testDroppingNonByeUserIn4PTourneyAddsBye() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addPlayer("P4");
		t.dropPlayer("P2");
		assertEquals(4, t.size());
	}

	@Test
	public void testBatchAdd() {
		String batchAdd = "P1,P2,P3,P4,P5,P6,P7,P8";
		t.addBatch(batchAdd);
		assertEquals(8, t.players.size());
		assertEquals("P1", t.players.get(0).getName());
	}

	@Test
	public void testAddLateParticipantsCorrectlyManipulatesByes() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addBye();
		t.generatePairings(0);
		assertEquals(true, t.containsPlayer("BYE"));
		t.addBatch("P4,P5,P6");
		assertEquals(6, t.size());
		assertEquals(false, t.containsPlayer("BYE"));
	}

	@Test
	public void testAddBatchTrimsWhitespace() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addBatch(" P4,P5 ,  P6   ,                      P7 ,	P8 	");
		assertEquals(8, t.players.size());
		for (Player p : t.players) {
			assertEquals(2, p.getName().length());
		}
	}

	@Test
	public void testAddingUsersToTourneyWithBye_AssertByeStillRequired() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addBye();
		t.addBatch("P4,P5,P6,P7");
		assertEquals(true, t.containsPlayer("BYE"));
		assertEquals(8, t.size());
	}

	@Test
	public void testAddingUsersToTourneyWithBye_AssertByeNoLongerRequired() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addBye();
		t.addBatch("P4,P5,P6,P7,P8");
		assertEquals(false, t.containsPlayer("BYE"));
		assertEquals(8, t.size());
	}

	@Test
	public void testAddingUserWithDuplicateNameDoesntAdd() {
		t.addPlayer("Tom");
		t.addPlayer("Dick");
		t.addPlayer("Harry");
		t.addPlayer("Sally");
		assertEquals(4, t.size());
		t.addPlayer("Sally");
		assertEquals(false, t.containsPlayer("BYE"));
		assertEquals(4, t.size());
	}

	@Test
	public void testAddingUsersToTourneyWithoutBye_AssertByeStillNotRequired() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addPlayer("P4");
		t.addBatch("P5,P6,P7,P8");
		assertEquals(false, t.containsPlayer("BYE"));
		assertEquals(8, t.size());
	}

	@Test
	public void testAddingUsersToTourneyWithoutBye_AssertByeNowRequired() {
		t.setAllParticipantsIn(true);
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addPlayer("P4");
		t.addBatch("P5,P6,P7");
		assertEquals(8, t.size());
		assertEquals(true, t.containsPlayer("BYE"));
	}

	@Test
	public void testAddingUserIncrementsRequiredRounds() {
		t.addBatch("P1,P2,P3,P4,P5,P6,P7,P8");
		t.setNumberOfRounds(3);
		t.addBatch("P9,P10");
		assertEquals(4, t.getNumberOfRounds());
	}

	@Test
	public void testRemovingUserDecrementsRequiredRounds() {
		t.addBatch("P1,P2,P3,P4,P5,P6");
		t.setNumberOfRounds(3);
		t.dropPlayer("P5");
		t.dropPlayer("P6");
		assertEquals(3, t.getNumberOfRounds());
	}

	@Test
	public void testReopenGame() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		Player p4 = new Player("P4");

		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);

		t.generatePairings(0);
		assertEquals(2, t.currentBattles.size());
		Battle b = t.currentBattles.remove(0);
		Utils.handleBattleWinner(b, "1");
		assertEquals(1, b.getP1().getListOfVictories().size());
		assertEquals(0, b.getP2().getListOfVictories().size());
		assertEquals(1, b.getP1().getOpponentsList().size());
		assertEquals(1, b.getP2().getOpponentsList().size());
		assertEquals(1, t.currentBattles.size());

		t.reopenBattle(b.getP1(), b.getP2());

		assertEquals(0, b.getP1().getListOfVictories().size());
		assertEquals(0, b.getP2().getListOfVictories().size());
		assertEquals(0, b.getP1().getOpponentsList().size());
		assertEquals(0, b.getP2().getOpponentsList().size());
		assertEquals(2, t.currentBattles.size());
	}

	@Test
	public void testCannotReopenNonexistantGame() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		Player p4 = new Player("P4");

		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);

		t.generatePairings(0);
		Battle b = t.currentBattles.remove(0);
		Utils.handleBattleWinner(b, "1");
		t.reopenBattle(p1, p4);
		assertEquals(1, t.currentBattles.size());
	}

	@Test
	public void testReopeningGameDropsScore() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");

		t.addPlayer(p1);
		t.addPlayer(p2);

		t.generatePairings(0);
		assertEquals(1, t.currentBattles.size());
		Battle b = t.currentBattles.remove(0);
		Utils.handleBattleWinner(b, "1");

		assertEquals(3, b.getP1().getScore());
		assertEquals(0, b.getP2().getScore());

		t.reopenBattle(b.getP1(), b.getP2());

		assertEquals(0, b.getP1().getScore());
		assertEquals(0, b.getP2().getScore());
	}

	@Test
	public void testPreviousRoundsAreLoggedCorrectly() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		t.addPlayer(p1);
		t.addPlayer(p2);

		p1.beats(p2);

		assertEquals(1, p1.getListOfVictories().size());
		assertEquals(1, p1.getOpponentsList().size());
		assertEquals(1, p2.getOpponentsList().size());
		assertEquals(0, p2.getListOfVictories().size());
	}

	@Test
	public void testGetResultsOfAllMatchesSoFar_NoTies() {
		Player p1 = new Player("P1");
		Player p3 = new Player("P3");
		Player p2 = new Player("P2");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);

		p2.beats(p3);
		p1.beats(p2);

		assertEquals("P1 vs. P2 (P1 won)\nP2 vs. P3 (P2 won)\n", t.getResultsOfAllMatchesSoFar());
	}

	@Test
	public void testGetResultsOfAllMatchesSoFar_WinsAndTies() {

		Player p1 = new Player("P1");
		Player p3 = new Player("P3");
		Player p2 = new Player("P2");
		Player p4 = new Player("P4");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);

		p1.beats(p2);
		p2.beats(p3);
		p3.tied(p4);
		t.sortRankings();

		assertEquals("P1 vs. P2 (P1 won)\nP2 vs. P3 (P2 won)\nP3 vs. P4 (Tied)\nP4 vs. P3 (Tied)\n",
				t.getResultsOfAllMatchesSoFar());
	}

	@Test
	public void testSingleElimination_EliminatesCorrectly() {
		Player p1 = new Player("P1");
		Player p3 = new Player("P3");
		Player p2 = new Player("P2");
		Player p4 = new Player("P4");
		Player p5 = new Player("P5");
		Player p6 = new Player("P6");
		Player p7 = new Player("P7");
		Player p8 = new Player("P8");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.addPlayer(p5);
		t.addPlayer(p6);
		t.addPlayer(p7);
		t.addPlayer(p8);

		p1.beats(p2);
		p3.beats(p4);
		p5.beats(p6);
		p7.beats(p8);

		t.setX_elimination(1);
		t.elimination();

		assertEquals(4, t.players.size());
	}

	@Test
	public void testDroppingPlayer_AddsBye_DroppingAnother_RemovesBye_SameRound() {
		t.addBatch("p1,p2,p3,p4,p5,p6,p7,p8,p9,p0");
		assertEquals(10, t.players.size());
		t.generatePairings(0);
		Utils.handleBattleWinner(t.currentBattles.remove(4), "2");
		t.dropPlayer("p9");
		assertEquals(10, t.players.size());
		t.dropPlayer("p0");
		t.updateParticipantStats();
		assertEquals(8, t.players.size());
	}

	@Test
	public void testDroppingPlayer_AddsBye_DroppingAnother_RemovesBye_NextRound() {
		t.addBatch("p1,p2,p3,p4,p5,p6,p7,p8,p9,p0");
		assertEquals(10, t.players.size());
		t.generatePairings(0);
		while (t.currentBattles.size() > 1) {
			Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		}
		t.dropPlayer("p8");
		assertEquals(10, t.players.size());
		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		t.updateParticipantStats();
		t.sortRankings();

		t.generatePairings(0);
		while (t.currentBattles.size() > 1) {
			Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		}
		t.dropPlayer("p6");
		assertEquals(8, t.players.size());
	}

	@Test
	public void testDoubleElimination_EliminatesCorrectly() {
		Player p1 = new Player("P1");
		Player p3 = new Player("P3");
		Player p2 = new Player("P2");
		Player p4 = new Player("P4");
		Player p5 = new Player("P5");
		Player p6 = new Player("P6");
		Player p7 = new Player("P7");
		Player p8 = new Player("P8");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.addPlayer(p5);
		t.addPlayer(p6);
		t.addPlayer(p7);
		t.addPlayer(p8);

		p1.beats(p2);
		p3.beats(p4);
		p5.beats(p6);
		p7.beats(p8);

		p1.beats(p3);
		p5.beats(p7);
		p2.beats(p4);
		p6.beats(p8);

		t.setX_elimination(2);
		t.elimination();

		assertEquals(6, t.players.size());
	}

	@Test
	public void testTripleEliminationReachesSingleWinner() {
		t.addBatch("p1,p2,p3,p4,p5,p6,p7,p8");
		t.setX_elimination(3);
		while (t.players.size() > 1) {
			t.generatePairings(0);
			Utils.autocompleteRound(t.currentBattles);
			t.elimination();
		}
		assertEquals(1, t.players.size());
	}

	@Test
	public void testTwoManEliminationTourneyEnds() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.setX_elimination(1);
		p1.beats(p2);
		t.elimination();
		assertEquals(1, t.players.size());
	}

	@Test
	public void testFourManEliminationTourneyEnds() {

		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		Player p4 = new Player("P4");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.setX_elimination(1);
		t.generatePairings(0);
		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		t.elimination();
		assertEquals(2, t.players.size());
		t.generatePairings(0);
		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		t.elimination();
		assertEquals(1, t.players.size());
		assertEquals("P1", t.players.get(0).getName());
	}

	@Test
	public void testTwelveManEliminationTourneyEnds() {

		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		Player p4 = new Player("P4");
		Player p5 = new Player("P5");
		Player p6 = new Player("P6");
		Player p7 = new Player("P7");
		Player p8 = new Player("P8");
		Player p9 = new Player("P9");
		Player p10 = new Player("P10");
		Player p11 = new Player("P11");
		Player p12 = new Player("P12");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.addPlayer(p5);
		t.addPlayer(p6);
		t.addPlayer(p7);
		t.addPlayer(p8);
		t.addPlayer(p9);
		t.addPlayer(p10);
		t.addPlayer(p11);
		t.addPlayer(p12);
		t.setX_elimination(2);

		t.generatePairings(0);
		while (t.currentBattles.size() > 0) {
			Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		}
		t.elimination();
		assertEquals(12, t.players.size());

		t.generatePairings(0);
		while (t.currentBattles.size() > 0) {
			Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		}
		t.elimination();
		assertEquals(10, t.players.size());

		t.generatePairings(0);
		while (t.currentBattles.size() > 0) {
			Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		}
		t.elimination();
		assertEquals(6, t.players.size());

		t.generatePairings(0);
		while (t.currentBattles.size() > 0) {
			Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		}
		t.elimination();
		assertEquals(4, t.players.size());

		t.generatePairings(0);
		while (t.currentBattles.size() > 0) {
			Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		}
		t.elimination();
		assertEquals(2, t.players.size());
	}

	@Test
	public void testSaveLoadTournament() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		Player p4 = new Player("P4");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.setNumberOfRounds(3);
		t.generatePairings(0);
		t.activeMetadataFile = "test.tnt";
		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		TntFileManager.saveTournament(t);
		t.currentBattles.clear();
		t.players.clear();
		try {
			TntFileManager.loadTournament(t, t.activeMetadataFile);
		} catch (IOException e) {
		}
		assertEquals(3, t.getNumberOfRounds());
		assertEquals(4, t.players.size());
		assertEquals(1, t.currentBattles.size());
		assertEquals(3, t.players.get(0).getScore());
		assertEquals(1, t.players.get(0).getOpponentsList().size());
		assertEquals(1, t.players.get(0).getListOfVictories().size());
		File file = new File("test.tnt");
		if (file.exists()) {
			file.delete();
		}
	}

	@Test
	public void testAddsGameToPlayerHistory() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		t.addPlayer(p1);
		t.addPlayer(p2);

		t.addGamesToPlayerHistory("P1_[P2]_[P2]");
		t.addGamesToPlayerHistory("P2_[]_[P1]");

		assertEquals(1, p1.getOpponentsList().size());
		assertEquals(1, p1.getListOfVictories().size());
		assertEquals(1, p2.getOpponentsList().size());
		assertEquals(0, p2.getListOfVictories().size());
	}

	@Test
	public void testTyingGameAddsOnePoint() {

		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);

		p1.beats(p2);
		p2.tied(p3);

		assertEquals(1, p1.getListOfVictories().size());
		assertEquals(1, p1.getOpponentsList().size());
		assertEquals(3, p1.getScore());

		assertEquals(0, p2.getListOfVictories().size());
		assertEquals(2, p2.getOpponentsList().size());
		assertEquals(1, p2.getScore());

		assertEquals(0, p3.getListOfVictories().size());
		assertEquals(1, p3.getOpponentsList().size());
		assertEquals(1, p3.getScore());
	}

	@Test
	public void testAddingExtraRoundsThenReportingResultsDoesntResetRoundNumber() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		Player p4 = new Player("P4");
		Player p5 = new Player("P5");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.addPlayer(p5);
		t.addBye();
		t.setNumberOfRounds(5);

		t.generatePairings(0);
		while (t.currentBattles.size() > 0) {
			Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		}

		assertEquals(5, t.numberOfRounds);
	}
}
