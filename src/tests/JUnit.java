package tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

import swissTournamentRunner.Battle;
import swissTournamentRunner.GUI;
import swissTournamentRunner.Player;
import swissTournamentRunner.PlayerCreator;
import swissTournamentRunner.TntFileManager;
import swissTournamentRunner.Tournament;
import swissTournamentRunner.Utils;

public class JUnit {

	public Tournament t = new Tournament();
	public TntFileManager tntfm = new TntFileManager(t);

	@Before
	public void setup() {
		Tournament.players.clear();
		t.currentBattles.clear();
		Tournament.dropped.clear();
		t.setAllParticipantsIn(true);
		new GUI(t);
	}

	@Test
	public void testAddPlayersToTournament() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		assertEquals(2, Tournament.players.size());
	}

	@Test
	public void testAddingOddNumberedListThenAddingSingleDoesntGiveTwoByes() {
		t.addBatch("P1,P2,P3");
		t.addPlayer("P4");
		assertEquals(4, Tournament.players.size());
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
		assertEquals(0, Tournament.findPlayerByName("P1").getScore());
		assertEquals(3, Tournament.findPlayerByName("P2").getScore());
		assertEquals(1, Tournament.findPlayerByName("P3").getScore());
		assertEquals(1, Tournament.findPlayerByName("P4").getScore());
	}

	@Test
	public void testCompareIdenticalPlayersIsTie() {
		Player p1 = new Player("P1", 0, 0, 0, 0);
		Player p2 = new Player("P2", 0, 0, 0, 0);
		p1.lastDocumentedPosition = 1;
		p2.lastDocumentedPosition = 1;
		t.addPlayer(p1);
		t.addPlayer(p2);
		assertEquals(0, p1.compareTo(p2));
	}

	@Test
	public void testComparePlayerToByeReturnsPlayer() {
		Player p1 = new Player("P1");
		t.addPlayer(p1);
		t.addBye();
		p1.lastDocumentedPosition = 1;
		assertEquals(-1, p1.compareTo(Tournament.players.get(1)));
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
		p1.lastDocumentedPosition = 1;
		p2.lastDocumentedPosition = 1;
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
		new GUI(t);
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
		assertEquals(5, Tournament.players.size());
		assertEquals(3, t.getNumberOfRounds());
		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		t.dropPlayer("P2");
		assertEquals(5, Tournament.players.size());
		assertEquals(3, t.getNumberOfRounds());
		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		t.dropPlayer("P4");
		assertEquals(5, Tournament.players.size());
		assertEquals(2, Tournament.livePlayerCount());
		t.recalculateRounds();
		assertEquals(1, t.getNumberOfRounds());
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
		p1.lastDocumentedPosition = 1;
		p2.lastDocumentedPosition = 1;
		p3.lastDocumentedPosition = 1;
		p4.lastDocumentedPosition = 1;
		t.sortRankings();
		assertEquals("P1", Tournament.players.get(0).getName());
		assertEquals("P3", Tournament.players.get(1).getName());
		assertEquals("P2", Tournament.players.get(2).getName());
		assertEquals("P4", Tournament.players.get(3).getName());
	}

	@Test
	public void testTiedSoGoToTB_p2() {
		Player p1 = new Player("P1", 3, 0, 0, 0);
		Player p2 = new Player("P2", 3, 1, 0, 0);
		p1.lastDocumentedPosition = 1;
		p2.lastDocumentedPosition = 1;
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
		p1.lastDocumentedPosition = 1;
		p2.lastDocumentedPosition = 1;
		t.addPlayer("P1");
		t.addPlayer("P2");
		assertEquals(1, p1.compareTo(p2));
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
		p1.lastDocumentedPosition = 1;
		p2.lastDocumentedPosition = 1;
		assertEquals(1, p1.compareTo(p2));
	}

	@Test
	public void testTiedEveryTieBreaker() {
		Player p1 = new Player("P1", 3, 1, 3, 0);
		Player p2 = new Player("P2", 3, 1, 3, 0);
		p1.lastDocumentedPosition = 1;
		p2.lastDocumentedPosition = 1;
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
		p1.lastDocumentedPosition = 1;
		p2.lastDocumentedPosition = 1;
		p3.lastDocumentedPosition = 1;
		p4.lastDocumentedPosition = 1;
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		t.addPlayer(p4);
		t.sortRankings();
		assertEquals("-=-=-=-Rankings-=-=-=-\nP2\nP3\nP1\nP4\n", t.rankingsToOneBigString());
	}

	@Test
	public void testAddBye_AddsNoBye() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addBye();
		assertEquals(false, t.doesPlayerExist("BYE"));
	}

	@Test
	public void testAddBye_AddsBye() {
		t.addPlayer("P1");
		t.addBye();
		assertEquals(true, t.doesPlayerExist("BYE"));
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
		assertEquals("-=-=-=-Rankings-=-=-=-\nP1\nP2\nP3\n", t.rankingsToOneBigString());

	}

	@Test
	public void testFiveManPeckingOrder() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		Player p4 = new Player("P4");
		Player p5 = new Player("P5");

		p1.lastDocumentedPosition = 1;
		p2.lastDocumentedPosition = 1;
		p3.lastDocumentedPosition = 1;
		p4.lastDocumentedPosition = 1;
		p5.lastDocumentedPosition = 1;
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
		assertEquals("-=-=-=-Rankings-=-=-=-\nP5\nP4\nP3\nP2\nP1\n", t.rankingsToOneBigString());

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
		assertEquals(4, t.size());
	}

	@Test
	public void testDroppingNonByeUserIn4PTourneyAddsBye() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addPlayer("P4");
		t.dropPlayer("P2");
		assertEquals(5, t.size());
	}

	@Test
	public void testBatchAdd() {
		String batchAdd = "P1,P2,P3,P4,P5,P6,P7,P8";
		t.addBatch(batchAdd);
		assertEquals(8, Tournament.players.size());
		assertEquals("P1", Tournament.players.get(0).getName());
	}

	@Test
	public void testAddLateParticipantsCorrectlyManipulatesByes() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addBye();
		t.generatePairings(0);
		assertEquals(true, t.doesPlayerExist("BYE"));
		t.addBatch("P4,P5,P6");
		assertEquals(6, t.size());
		assertEquals(false, t.doesPlayerExist("BYE"));
	}

	@Test
	public void testAddBatchTrimsWhitespace() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addBatch(" P4,P5 ,  P6   ,                      P7 ,	P8 	");
		assertEquals(8, Tournament.players.size());
		for (Player p : Tournament.players) {
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
		assertEquals(true, t.doesPlayerExist("BYE"));
		assertEquals(8, t.size());
	}

	@Test
	public void testAddingUsersToTourneyWithBye_AssertByeNoLongerRequired() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addBye();
		t.addBatch("P4,P5,P6,P7,P8");
		assertEquals(false, t.doesPlayerExist("BYE"));
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
		assertEquals(false, t.doesPlayerExist("BYE"));
		assertEquals(4, t.size());
	}

	@Test
	public void testAddingUsersToTourneyWithoutBye_AssertByeStillNotRequired() {
		t.addPlayer("P1");
		t.addPlayer("P2");
		t.addPlayer("P3");
		t.addPlayer("P4");
		t.addBatch("P5,P6,P7,P8");
		assertEquals(false, t.doesPlayerExist("BYE"));
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
		assertEquals(true, t.doesPlayerExist("BYE"));
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
	public void testDroppingPlayer_AddsBye_DroppingAnother_RemovesBye_SameRound() {
		t.addBatch("p1,p2,p3,p4,p5,p6,p7,p8,p9,p0");
		assertEquals(10, Tournament.players.size());
		t.dropPlayer("p1");
		assertEquals(11, Tournament.players.size());
		assertEquals(10, Tournament.livePlayerCount());
		t.dropPlayer("p0");
		assertEquals(11, Tournament.players.size());
		assertEquals(8, Tournament.livePlayerCount());
	}

	@Test
	public void testDroppingPlayer_AddsBye_DroppingAnother_RemovesBye_NextRound() {
		t.addBatch("p0,p1,p2,p3,p4,p5,p6,p7,p8,p9");
		assertEquals(10, Tournament.players.size());
		Player p0 = Tournament.findPlayerByName("p0");
		Tournament.players.remove(p0);
		Player p1 = Tournament.findPlayerByName("p1");
		Tournament.players.remove(p1);
		t.initialSeed(p0, p1);
		t.generatePairings(0);
		while (t.currentBattles.size() > 1) {
			Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		}
		assertEquals(10, Tournament.livePlayerCount());
		t.dropPlayer("p1");
		assertEquals(11, Tournament.players.size());
		assertEquals(10, Tournament.livePlayerCount());
		Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		t.updateParticipantStats();
		t.sortRankings();
		t.generatePairings(0);
		while (t.currentBattles.size() > 1) {
			Utils.handleBattleWinner(t.currentBattles.remove(0), "1");
		}
		t.dropPlayer("p0");
		assertEquals(11, Tournament.players.size());
		assertEquals(8, Tournament.livePlayerCount());
	}

	@Test
	public void testSaveLoadTournament() {
		t = new Tournament();
		t.setGUI(new GUI(t));
		try {
			TntFileManager.loadTournament(t, "testdata.tnt");
			assertEquals(4, t.getNumberOfRounds());
			assertEquals(8, Tournament.players.size());
			tntfm.saveTournament();
			TntFileManager.loadTournament(t, "testdata.tnt");
			assertEquals(4, t.getNumberOfRounds());
			assertEquals(8, Tournament.players.size());
			Battle b = t.currentBattles.get(3);
			assertEquals(7, b.getElo(b.getP2()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Test
	public void testAddsGameToPlayerHistory() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		t.addPlayer(p1);
		t.addPlayer(p2);

		TntFileManager.addGamesToPlayerHistory("P1_[P2]_[P2]");
		TntFileManager.addGamesToPlayerHistory("P2_[]_[P1]");

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

	@Test
	public void testRecalculateScoreInvolvingTiebreaker() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		p1.beats(p2);
		p1.tied(p3);
		p1.recalculateScore();
		p2.recalculateScore();
		p3.recalculateScore();
		assertEquals(4, p1.getScore());
		assertEquals(0, p2.getScore());
		assertEquals(1, p3.getScore());
	}

	@Test
	public void testProcessPlayerNameInPlayerCapture() {
		PlayerCreator pc = new PlayerCreator(t);
		assertEquals(0, Tournament.players.size());
		pc.processPlayerName("A");
		assertEquals(2, Tournament.players.size());
	}

	@Test
	public void testProcessPlayerName_no_DoesntAddPlayer() {
		PlayerCreator pc = new PlayerCreator(t);
		pc.processPlayerName("no");
		assertEquals(0, Tournament.players.size());
	}

	@Test
	public void testProcessPlayerName_addBatch() {
		PlayerCreator pc = new PlayerCreator(t);
		pc.processPlayerName("A,B,C,D");
		assertEquals(4, Tournament.players.size());
	}

	@Test
	public void testDropPlayerBeforeTourneyBegins() {
		PlayerCreator pc = new PlayerCreator(t);
		pc.processPlayerName("A,B,C,D");
		t.setUserSelection("1");
		pc.processPlayerName("drop");
		assertEquals(3, Tournament.players.size());
		t.setUserSelection("111111");
		pc.processPlayerName("drop");
		assertEquals(3, Tournament.players.size());
		t.setUserSelection("-111111");
		pc.processPlayerName("drop");
		assertEquals(3, Tournament.players.size());
	}

	@Test
	public void testTopCutOffersNewTourney() {
		t.addBatch("1,2,3,4,5,6,7,8");
		t.numberOfRounds = 3;
		Tournament.topCutThreshold = 4;
		t.generatePairings(0);
		Utils.autocompleteRound(t.currentBattles);
		t.generatePairings(0);
		Utils.autocompleteRound(t.currentBattles);
		t.generatePairings(0);
		Utils.autocompleteRound(t.currentBattles);
		t.setUserSelection("n");
		t.postTourneyProcessing();
		assertEquals(8, Tournament.players.size());
	}

	@Test
	public void testPlayerListReturnsPlayerNames() {
		Player p1 = new Player("P1");
		Player p2 = new Player("P2");
		Player p3 = new Player("P3");
		t.addPlayer(p1);
		t.addPlayer(p2);
		t.addPlayer(p3);
		assertEquals("P1,P2,P3", t.playerList());
	}

	@Test
	public void testActivePlayerSize() {
		t.addBatch("P1,P2,P3,P4,P5,P6,P7,P8");
		assertEquals(0, Tournament.dropped.size());
		assertEquals(8, Tournament.players.size());
		assertEquals(8, Tournament.livePlayerCount());

		t.dropPlayer("P8");
		assertEquals(1, Tournament.dropped.size());
		assertEquals(9, Tournament.players.size());
		assertEquals(8, Tournament.livePlayerCount());

		t.dropPlayer("P7");
		assertEquals(3, Tournament.dropped.size());
		assertEquals(9, Tournament.players.size());
		assertEquals(6, Tournament.livePlayerCount());

		t.dropPlayer("P6");
		assertEquals(3, Tournament.dropped.size());
		assertEquals(9, Tournament.players.size());
		assertEquals(6, Tournament.livePlayerCount());

		t.dropPlayer("P8");
		assertEquals(3, Tournament.dropped.size());
		assertEquals(9, Tournament.players.size());
		assertEquals(6, Tournament.livePlayerCount());

	}
}
