package swissTournamentRunner;

import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.Sides;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class GUI implements ActionListener {

	private final static String newline = "\n";
	public static Tournament tourney;
	public static JTextField textField;
	public static JTextArea pairingsBox;
	public static JTextArea resultsBox;
	public static JFrame frame = new JFrame("BTC");
	public JToolBar toolbar;
	public JButton repairButton;
	public JButton startButton;

	public GUI(Tournament t) {
		tourney = t;
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setLayout(new MigLayout("wrap ", "[grow,fill]"));
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(screenSize.width, screenSize.height);
		
		toolbar = new JToolBar("Admin Tools");

		startButton = new JButton("START");
		startButton.addActionListener(e -> {
			if (t.currentBattles.size() == 0) {
				JFrame seedPanel = new JFrame("Start Tournament?");
				seedPanel.setSize(300, 130);
				seedPanel.setLayout(new MigLayout("", "[grow,fill]"));
				seedPanel.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						t.postListOfConfirmedSignups();
					}
				});
				JButton start = new JButton("Start Tournament");
				seedPanel.add(start);
				seedPanel.setVisible(true);

				start.addActionListener(arg0 -> {
					t.allParticipantsIn = true;
					t.setUserSelection("no");
					seedPanel.dispose();
					startButton.setVisible(false);
					toolbar.repaint();
				});
			}
		});
		toolbar.add(startButton);
		
		JButton addRoundButton = new JButton("Edit # of Rounds");
		addRoundButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				JFrame addRoundBox = new JFrame("New Round Number");
				addRoundBox.setSize(500, 100);
				addRoundBox.setLayout(new GridLayout());
				JTextField input = new JTextField("Enter new round number here.");
				JButton addRoundButton1 = new JButton("Submit");
				addRoundBox.add(input);
				addRoundBox.add(addRoundButton1);
				addRoundBox.setVisible(true);
				addRoundButton1.addActionListener(e1 -> {
					String newMax = input.getText();
					t.alterRoundNumbers(newMax);
					t.save();
					addRoundBox.dispose();
				});
			}
		});
		toolbar.add(addRoundButton);

		JButton eloButton = new JButton("ELO");
		eloButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				t.elo = t.toggle(t.elo);
				pairingsBox.setCaretPosition(0);
				pairingsBox.setText("ELO switched " + t.elo + ".\n");

				postString(t.getCurrentBattles(t.currentBattles, t.roundString));
				t.save();
			}
		});
		toolbar.add(eloButton);

		JButton randomPlayerButton = new JButton("Random Player");
		randomPlayerButton.addActionListener(e -> {
			if (Tournament.players.size() > 0) {
				postString(Utils.getRandomPlayer(tourney).getName() + " chosen for prize draw.");
			}
		});
		toolbar.add(randomPlayerButton);

		JButton topCutButton = new JButton("Top Cut");
		topCutButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				JFrame topCutBox = new JFrame("New Top Cut Size");
				topCutBox.setSize(500, 100);
				topCutBox.setLayout(new GridLayout());
				JTextField input = new JTextField("Enter desired Top Cut size here.");
				JButton submitButton = new JButton("Submit");
				topCutBox.add(input);
				topCutBox.add(submitButton);
				topCutBox.setVisible(true);
				submitButton.addActionListener(e12 -> {
					String newTopCutSize = input.getText();
					t.alterTopCut(newTopCutSize);
					t.save();
					topCutBox.dispose();
				});
			}
		});
		toolbar.add(topCutButton);

		JButton matches = new JButton("All Matches");
		matches.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				pairingsBox.setCaretPosition(0);
				pairingsBox.setText(t.getResultsOfAllMatchesSoFar() + "\n");
				postString(t.getCurrentBattles(t.currentBattles, t.roundString));
			}
		});
		toolbar.add(matches);

		JButton matchesOfButton = new JButton("One Player's Matches");
		matchesOfButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				JFrame matchesOfButtonFrame = new JFrame("Show results for specified player's reported matches");
				matchesOfButtonFrame.setSize(550, 150);
				matchesOfButtonFrame.setLayout(new MigLayout("", "[grow,fill]"));
				ArrayList<String> playerNames = new ArrayList<>();
				for (Player p : t.getPlayers()) {
					playerNames.add(p.getName());
				}
				Collections.sort(playerNames);
				String[] ps = playerNames.toArray(new String[0]);
				JComboBox<String> players = new JComboBox<>(ps);
				JButton submitGetMatches = new JButton("Submit");
				matchesOfButtonFrame.add(players, "span 3,wrap");
				matchesOfButtonFrame.add(submitGetMatches);
				matchesOfButtonFrame.setVisible(true);
				submitGetMatches.addActionListener(e18 -> {
					String selected = Objects.requireNonNull(players.getSelectedItem()).toString();
					pairingsBox.setCaretPosition(0);
					pairingsBox.setText(t.getResultsOfAllMatchesByPlayerSoFar(t.findPlayerByName(selected)) + "\n");
					postString(t.getCurrentBattles(t.currentBattles, t.roundString));
				});
			}
		});
		toolbar.add(matchesOfButton);

		JButton addPlayersButton = new JButton("Add Players");
		addPlayersButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				JFrame addPlayersBox = new JFrame("Add Player(s)");
				addPlayersBox.setSize(700, 150);
				addPlayersBox.setLayout(new GridLayout());
				JTextField input = new JTextField("Enter new player's name here.");
				JButton submitButton = new JButton("Submit");
				addPlayersBox.add(input);
				addPlayersBox.add(submitButton);
				addPlayersBox.setVisible(true);
				submitButton.addActionListener(e13 -> {
					String newPlayer = input.getText();
					t.addLatePlayer(newPlayer);
					t.save();
					addPlayersBox.dispose();
				});
			}
		});
		toolbar.add(addPlayersButton);

		JButton dropPlayersButton = new JButton("Drop Player");
		dropPlayersButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				JFrame dropPlayersBox = new JFrame("Drop Player");
				dropPlayersBox.setSize(450, 130);
				dropPlayersBox.setLayout(new GridLayout());
				ArrayList<String> playerNames = new ArrayList<>();
				for (Player p : t.getPlayers()) {
					if (!t.getDroppedPlayers().contains(p)) {
						playerNames.add(p.getName());
					}
				}
				Collections.sort(playerNames);
				playerNames.remove("BYE");
				String[] ps = playerNames.toArray(new String[0]);
				JComboBox<String> input = new JComboBox<>(ps);
				JButton dropSubmitButton = new JButton("Submit");
				dropPlayersBox.add(input);
				dropPlayersBox.add(dropSubmitButton);
				dropPlayersBox.setVisible(true);
				dropSubmitButton.addActionListener(e14 -> {
					String dropPlayer = Objects.requireNonNull(input.getSelectedItem()).toString();
					tourney.dropPlayer(dropPlayer);
					t.save();
					dropPlayersBox.dispose();
					pairingsBox.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
				});
			}
		});
		toolbar.add(dropPlayersButton);

		JButton editNameButton = new JButton("Edit Name");
		editNameButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				JFrame nameEditor = new JFrame("Edit Name");
				nameEditor.setSize(400, 100);
				nameEditor.setLayout(new MigLayout("", "[grow,fill]"));
				ArrayList<String> playerNames = new ArrayList<>();
				for (Player p : t.getPlayers()) {
					playerNames.add(p.getName());
				}
				playerNames.remove("BYE");
				Collections.sort(playerNames);
				String[] ps = playerNames.toArray(new String[0]);
				JComboBox<String> players = new JComboBox<>(ps);
				JTextField editedName = new JTextField("Enter new name here.");
				JButton submitEditName = new JButton("Submit");
				players.addActionListener(e15 -> editedName.setText(Objects.requireNonNull(players.getSelectedItem()).toString()));
				nameEditor.add(players, "span 3,wrap");
				nameEditor.add(editedName, "span 2");
				nameEditor.add(submitEditName);
				nameEditor.setVisible(true);
				submitEditName.addActionListener(e16 -> {
					String oldName = Objects.requireNonNull(players.getSelectedItem()).toString();
					String newName = editedName.getText();
					t.renamePlayer(oldName, newName);
					pairingsBox.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
					resultsBox.setText(t.generateInDepthRankings(t.getPlayers()) + "\n");
					t.save();
					nameEditor.dispose();
				});
			}
		});
		toolbar.add(editNameButton);

		JButton reopenGameButton = new JButton("Reopen Game");
		reopenGameButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				JFrame reopenGameEditor = new JFrame("Reopen Game");
				reopenGameEditor.setSize(400, 100);
				reopenGameEditor.setLayout(new MigLayout("", "[grow,fill]"));
				ArrayList<String> playerNames = new ArrayList<>();
				for (Battle b : t.completedBattles) {
					playerNames.add(b.getP1().getName() + " vs. " + b.getP2().getName());
				}
				Collections.sort(playerNames);
				String[] games = playerNames.toArray(new String[0]);
				JComboBox<String> listOfGames = new JComboBox<>(games);
				JButton submitEditName = new JButton("Submit");
				reopenGameEditor.add(listOfGames, "span 4");
				reopenGameEditor.add(submitEditName);
				reopenGameEditor.setVisible(true);
				submitEditName.addActionListener(e19 -> {
					String s = Objects.requireNonNull(listOfGames.getSelectedItem()).toString();
					String[] combatants = s.split(" vs. ");
					Player p1 = t.findPlayerByName(combatants[0]);
					Player p2 = t.findPlayerByName(combatants[1]);

					for (Battle b : t.completedBattles) {
						if (b.getP1().getName().equals(combatants[0])
								&& b.getP2().getName().equals(combatants[1])) {
							b.setP1DamageDealt(0);
							b.setP2DamageDealt(0);
							t.reopenBattle(p1, p2);
							t.completedBattles.remove(b);
							t.updateParticipantStats();
							pairingsBox.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
							resultsBox.setText(t.generateInDepthRankings(t.getPlayers()) + "\n");
							postString("Game between " + p1.getName() + " and " + p2.getName() + " reopened.");
							t.save();
							reopenGameEditor.dispose();
							break;
						}
					}
				});
			}
		});
		toolbar.add(reopenGameButton);

		JButton seedPairingButton = new JButton("Edit Pairings");
		seedPairingButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				JFrame seedEditor = new JFrame("Choose two players to 'swap seats'");
				seedEditor.setSize(800, 100);
				seedEditor.setLayout(new MigLayout("", "[grow,fill]"));
				ArrayList<String> ongoingMatchNames = new ArrayList<>();
				for (Battle b : t.currentBattles) {
					ongoingMatchNames.add(b.getP1().getName() + " vs. " + b.getP2().getName());
				}
				Collections.sort(ongoingMatchNames);
				JButton submitSeed = new JButton("Submit");

				ArrayList<String> playerNames = new ArrayList<>();
				for (Battle b : t.currentBattles) {
					playerNames.add(b.getP1().getName());
					playerNames.add(b.getP2().getName());
				}
				Collections.sort(playerNames);

				String[] ps = playerNames.toArray(new String[0]);
				JComboBox<String> player1 = new JComboBox<>(ps);
				JComboBox<String> player2 = new JComboBox<>(ps);
				seedEditor.add(player1, "span 2");
				seedEditor.add(player2, "span 2");
				seedEditor.add(submitSeed, "span 1");
				seedEditor.setVisible(true);

				submitSeed.addActionListener(e110 -> {
					String p1name = Objects.requireNonNull(player1.getSelectedItem()).toString();
					String p2name = Objects.requireNonNull(player2.getSelectedItem()).toString();

					Player p1 = t.findPlayerByName(p1name);
					Player p2 = t.findPlayerByName(p2name);

					Battle p1Battle = t.findBattleByName(p1name);
					Battle p2Battle = t.findBattleByName(p2name);

					if (p1Battle.getP1().equals(p1)) {
						if (p2Battle.getP1().equals(p2)) {
							p1Battle.setPlayer("1", p2);
							p2Battle.setPlayer("1", p1);
						} else {
							p1Battle.setPlayer("1", p2);
							p2Battle.setPlayer("2", p1);
						}
					}
					if (p1Battle.getP2().equals(p1)) {
						if (p2Battle.getP1().equals(p2)) {
							p1Battle.setPlayer("2", p2);
							p2Battle.setPlayer("1", p1);
						} else {
							p1Battle.setPlayer("2", p2);
							p2Battle.setPlayer("2", p1);
						}
					}
					pairingsBox.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
					resultsBox.setText(t.generateInDepthRankings(t.getPlayers()) + "\n");
					t.save();
				});
			}
		});
		toolbar.add(seedPairingButton);

		JButton findInRankings = new JButton("Find Ranking");
		findInRankings.addActionListener(e -> {
			JFrame nameEditor = new JFrame("Find Position In Rankings");
			nameEditor.setSize(400, 115);
			nameEditor.setLayout(new MigLayout("", "[grow,fill]"));
			ArrayList<String> playerNames = new ArrayList<>();
			for (Player p : t.getPlayers()) {
				playerNames.add(p.getName());
			}
			Collections.sort(playerNames);
			String[] ps = playerNames.toArray(new String[0]);
			JComboBox<String> players = new JComboBox<>(ps);
			JButton submitEditName = new JButton("Submit");
			nameEditor.add(players, "span 2, wrap");
			nameEditor.add(submitEditName);
			nameEditor.setVisible(true);
			submitEditName.addActionListener(e17 -> {
				Player p1 = t.findPlayerByName(Objects.requireNonNull(players.getSelectedItem()).toString());
				t.print(p1.getName() + " is currently #" + p1.getPositionInRankings() + " in the rankings.\n");
				t.save();
			});
		});
		toolbar.add(findInRankings);

		JButton reportGameButton = new JButton("Report Win");
		reportGameButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {

				JFrame reportGameWindow = new JFrame("Report Win (tick box next to winner)");
				reportGameWindow.setSize(560, 130);
				reportGameWindow.setLayout(new MigLayout("", "[grow,fill]"));

				ArrayList<String> currentBattles = new ArrayList<>();
				for (Battle b : t.currentBattles) {
					currentBattles.add(b.getP1().getName() + " vs. " + b.getP2().getName());
				}
				String[] games = currentBattles.toArray(new String[0]);

				JLabel p1Label = new JLabel("Winner?");
				JLabel p1Dam = new JLabel("Dam. dealt");
				JLabel blank = new JLabel("Game to report");
				JLabel p2Dam = new JLabel("Dam. dealt");
				JLabel p2Label = new JLabel("Winner?");

				JCheckBox lhsPlayer = new JCheckBox("");
				lhsPlayer.setSelected(false);
				JTextField lhsPlayerDealtDamageInput = new JTextField(40);
				JComboBox<String> listOfGames = new JComboBox<>(games);

				//omg
				JTextField rhsPlayerDealtDamageInput = new JTextField(40);
				JCheckBox rhsPlayer = new JCheckBox("");
				rhsPlayer.setSelected(false);

				JButton submitResults = new JButton("Submit");

				reportGameWindow.add(p1Label);
				reportGameWindow.add(p1Dam);
				reportGameWindow.add(blank, "span 3");
				reportGameWindow.add(p2Dam);
				reportGameWindow.add(p2Label, "wrap");

				reportGameWindow.add(lhsPlayer);
				reportGameWindow.add(lhsPlayerDealtDamageInput);
				reportGameWindow.add(listOfGames, "span 3");
				reportGameWindow.add(rhsPlayerDealtDamageInput);
				reportGameWindow.add(rhsPlayer, "wrap");
				reportGameWindow.add(submitResults, "span");

				reportGameWindow.setVisible(true);

				submitResults.addActionListener(e111 -> {
					if (lhsPlayer.isSelected() || rhsPlayer.isSelected()) {

						String[] combatants = Objects.requireNonNull(listOfGames.getSelectedItem()).toString().split(" vs. ");
						Battle b = t.findBattleByName(combatants[0]);

						int lhsPlayerDealtDamage = 0;
						int rhsPlayerDealtDamage = 0;

						try {
							lhsPlayerDealtDamage = Integer.parseInt(lhsPlayerDealtDamageInput.getText());
							rhsPlayerDealtDamage = Integer.parseInt(rhsPlayerDealtDamageInput.getText());
						}
						catch(Exception exception){
							reportGameWindow.dispose();
						}


						if (b.getP1().getName().equals("BYE")) {
							b.getP2().beats(b.getP1());
							t.completedBattles.add(b);
							pairingsBox.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
							resultsBox.setText(t.generateInDepthRankings(t.getPlayers()) + "\n");
							pairingsBox.setCaretPosition(0);
							resultsBox.setCaretPosition(0);
							reportGameWindow.dispose();
						} else if (b.getP2().getName().equals("BYE")) {
							b.getP1().beats(b.getP2());
							t.completedBattles.add(b);
							pairingsBox.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
							resultsBox.setText(t.generateInDepthRankings(t.getPlayers()) + "\n");
							pairingsBox.setCaretPosition(0);
							resultsBox.setCaretPosition(0);
							reportGameWindow.dispose();
						} else {
							boolean isDraw = (lhsPlayer.isSelected() == rhsPlayer.isSelected());
							if (isDraw) {
								t.currentBattles.remove(b);
								Utils.handleBattleWinner(b, "0");
								b.setP1DamageDealt(lhsPlayerDealtDamage);
								b.setP2DamageDealt(rhsPlayerDealtDamage);
								b.setP1DamageDealt(lhsPlayerDealtDamage);
								b.setP2DamageDealt(rhsPlayerDealtDamage);
								t.completedBattles.add(b);
								pairingsBox
										.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
								resultsBox.setText(t.generateInDepthRankings(t.getPlayers()) + "\n");
								pairingsBox.setCaretPosition(0);
								resultsBox.setCaretPosition(0);
								reportGameWindow.dispose();
							} else if (lhsPlayer.isSelected()) {
								t.currentBattles.remove(b);
								Utils.handleBattleWinner(b, "1");
								b.setP1DamageDealt(lhsPlayerDealtDamage);
								b.setP2DamageDealt(rhsPlayerDealtDamage);
								b.setP1DamageDealt(lhsPlayerDealtDamage);
								b.setP2DamageDealt(rhsPlayerDealtDamage);
								t.completedBattles.add(b);
								pairingsBox
										.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
								resultsBox.setText(t.generateInDepthRankings(t.getPlayers()) + "\n");
								pairingsBox.setCaretPosition(0);
								resultsBox.setCaretPosition(0);
								reportGameWindow.dispose();
							} else if (rhsPlayer.isSelected()) {
								t.currentBattles.remove(b);
								Utils.handleBattleWinner(b, "2");
								b.setP1DamageDealt(lhsPlayerDealtDamage);
								b.setP2DamageDealt(rhsPlayerDealtDamage);
								b.setP1DamageDealt(lhsPlayerDealtDamage);
								b.setP2DamageDealt(rhsPlayerDealtDamage);
								t.completedBattles.add(b);
								pairingsBox
										.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
								resultsBox.setText(t.generateInDepthRankings(t.getPlayers()) + "\n");
								pairingsBox.setCaretPosition(0);
								resultsBox.setCaretPosition(0);
								reportGameWindow.dispose();
							}
						}}
						if (t.currentBattles.size() == 0) {
							t.setUserSelection("k");
						}
						t.save();
					});

			}
		});
		toolbar.add(reportGameButton);

		JButton printPairingsButton = new JButton("Print Pairings");
		printPairingsButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				printFile(t.getCurrentBattles(t.currentBattles, t.roundString));
			}
		});
		toolbar.add(printPairingsButton);

		repairButton = new JButton("Re-pair Round");
		repairButton.addActionListener(e -> {
			if (t.currentBattles.size() > 0) {
				JFrame repairPanel = new JFrame("Re-Pair Round?");
				repairPanel.setSize(400, 130);
				repairPanel.setLayout(new MigLayout("", "[grow,fill]"));
				repairPanel.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
					}
				});
				ArrayList<String> playerNames = new ArrayList<>();
				for (Player p : t.getPlayers()) {
					playerNames.add(p.getName());
				}
				Collections.sort(playerNames);
				JButton repair = new JButton("This is a destructive operation. Are you sure?");
				repairPanel.add(repair);
				repairPanel.setVisible(true);

				repair.addActionListener(arg0 -> {
					t.currentBattles.clear();
					t.sortRankings();
					repairPanel.dispose();
					t.generatePairings(0);
					t.refreshScreen();
					t.print(t.getCurrentBattles(t.currentBattles, t.roundString));
					t.save();
				});
			}
		});
		toolbar.add(repairButton);

		

		pairingsBox = new JTextArea(20, 60);
		pairingsBox.setEditable(false);
		pairingsBox.setLineWrap(true);
		pairingsBox.setFont(new Font("monospaced", Font.PLAIN, 20));

		resultsBox = new JTextArea(20, 30);
		resultsBox.setEditable(false);
		resultsBox.setLineWrap(false);
		resultsBox.setFont(new Font("monospaced", Font.PLAIN, 16));

		JLabel inputLabel = new JLabel(" Enter options here: ");
		textField = new JTextField(500);
		textField.addActionListener(this);

		JPanel inputArea = new JPanel();
		inputArea.setLayout(new MigLayout());
		inputArea.add(inputLabel);
		inputArea.add(textField, "span 5");

		frame.add(toolbar, "grow, wrap");
		frame.add(new JScrollPane(pairingsBox), "grow, wrap");
		frame.add(new JScrollPane(resultsBox), "grow");
		frame.add(inputArea, "grow");
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String text = textField.getText();
		tourney.setUserSelection(text);
		if (text.length() > 0) {
			if (text.length() <= 30) {
				pairingsBox.append(" " + text + newline);
			}
			textField.setText(null);
			pairingsBox.setCaretPosition(pairingsBox.getDocument().getLength());
		}
	}

	public static void createAndShowGUI(Boolean show) {
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(show);
	}

	public static void postString(String s) {
		pairingsBox.append(s + newline);
		pairingsBox.setCaretPosition(pairingsBox.getDocument().getLength());
	}

	public static void postResultsString(String s) {
		resultsBox.setText("");
		resultsBox.append(s + newline);
		resultsBox.setCaretPosition(0);
	}

	public static void wipePane() {
		pairingsBox.setText("");
		pairingsBox.setCaretPosition(pairingsBox.getDocument().getLength());
	}

	public static void printRankings(String generateInDepthRankings) {
		postResultsString(generateInDepthRankings);
	}

	public static void printFile(String pairingsString) {
		InputStream textStream = new ByteArrayInputStream(pairingsString.getBytes(StandardCharsets.UTF_8));
		DocFlavor myFormat = DocFlavor.INPUT_STREAM.TEXT_PLAIN_US_ASCII;
		Doc myDoc = new SimpleDoc(textStream, myFormat, null);
		PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
		attributeSet.add(new Copies(1));
		attributeSet.add(MediaSize.ISO.A4);
		attributeSet.add(Sides.DUPLEX);
		PrintService[] services = PrintServiceLookup.lookupPrintServices(myFormat, attributeSet);
		if (services.length > 0) {
			DocPrintJob job = services[0].createPrintJob();
			try {
				job.print(myDoc, attributeSet);
			} catch (PrintException pe) {
				pe.printStackTrace();
			}
		}
	}
}