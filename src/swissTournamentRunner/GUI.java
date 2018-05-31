package swissTournamentRunner;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
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
	public JButton startButton;

	public GUI(Tournament t) {
		tourney = t;
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setLayout(new MigLayout("wrap ", "[grow,fill]"));

		toolbar = new JToolBar("Admin Tools");

		JButton addRoundButton = new JButton("Edit # of Rounds");
		addRoundButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (t.currentBattles.size() > 0) {
					JFrame addRoundBox = new JFrame("New Round Number");
					addRoundBox.setSize(500, 100);
					addRoundBox.setLayout(new GridLayout());
					JTextField input = new JTextField("Enter new round number here.");
					JButton addRoundButton = new JButton("Submit");
					addRoundBox.add(input);
					addRoundBox.add(addRoundButton);
					addRoundBox.setVisible(true);
					addRoundButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							String newMax = input.getText();
							t.alterRoundNumbers(newMax);
							t.save();
							addRoundBox.dispose();
						}
					});
				}
			}
		});
		toolbar.add(addRoundButton);

		JButton eloButton = new JButton("ELO");
		eloButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (t.currentBattles.size() > 0) {
					t.elo = t.toggle(t.elo);
					pairingsBox.setCaretPosition(0);
					pairingsBox.setText("ELO switched " + t.elo + ".\n");
					postString(t.getCurrentBattles(t.currentBattles, Tournament.roundString));
					t.save();
				}
			}
		});
		toolbar.add(eloButton);

		JButton topCutButton = new JButton("Top Cut");
		topCutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (t.currentBattles.size() > 0) {
					JFrame topCutBox = new JFrame("New Top Cut Size");
					topCutBox.setSize(500, 100);
					topCutBox.setLayout(new GridLayout());
					JTextField input = new JTextField("Enter desired Top Cut size here.");
					JButton submitButton = new JButton("Submit");
					topCutBox.add(input);
					topCutBox.add(submitButton);
					topCutBox.setVisible(true);
					submitButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							String newTCsize = input.getText();
							t.alterTopCut(newTCsize);
							t.save();
							topCutBox.dispose();
						}
					});
				}
			}
		});
		toolbar.add(topCutButton);

		JButton matches = new JButton("All Matches");
		matches.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (t.currentBattles.size() > 0) {
					pairingsBox.setCaretPosition(0);
					pairingsBox.setText(t.getResultsOfAllMatchesSoFar() + "\n");
					postString(t.getCurrentBattles(t.currentBattles, Tournament.roundString));
				}
			}
		});
		toolbar.add(matches);

		JButton matchesOfButton = new JButton("One Player's Matches");
		matchesOfButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (t.currentBattles.size() > 0) {
					JFrame matchesOfButtonFrame = new JFrame("Return Player's Matches");
					matchesOfButtonFrame.setSize(450, 150);
					matchesOfButtonFrame.setLayout(new MigLayout("", "[grow,fill]"));
					ArrayList<String> playerNames = new ArrayList<String>();
					for (Player p : t.players) {
						playerNames.add(p.getName());
					}
					Collections.sort(playerNames);
					String[] ps = playerNames.toArray(new String[playerNames.size()]);
					@SuppressWarnings({ "rawtypes", "unchecked" })
					JComboBox players = new JComboBox(ps);
					JButton submitGetMatches = new JButton("Submit");
					matchesOfButtonFrame.add(players, "span 3,wrap");
					matchesOfButtonFrame.add(submitGetMatches);
					matchesOfButtonFrame.setVisible(true);
					submitGetMatches.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							String selected = players.getSelectedItem().toString();
							t.printHistory(Utils.findPlayerByName(selected, t.players));
							t.save();
						}
					});
				}
			}
		});
		toolbar.add(matchesOfButton);

		JButton addPlayersButton = new JButton("Add Players");
		addPlayersButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (t.currentBattles.size() > 0) {
					JFrame addPlayersBox = new JFrame("Add Player(s)");
					addPlayersBox.setSize(700, 400);
					addPlayersBox.setLayout(new GridLayout());
					JTextField input = new JTextField("Enter comma-separated player list here.");
					JButton submitButton = new JButton("Submit");
					addPlayersBox.add(input);
					addPlayersBox.add(submitButton);
					addPlayersBox.setVisible(true);
					submitButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							String newPlayers = input.getText();
							t.addBatch(newPlayers);
							t.save();
							addPlayersBox.dispose();
						}
					});
				}
			}
		});
		toolbar.add(addPlayersButton);

		JButton dropPlayersButton = new JButton("Drop Player(s)");
		dropPlayersButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (t.currentBattles.size() > 0) {
					JFrame dropPlayersBox = new JFrame("Drop Player(s)");
					dropPlayersBox.setSize(450, 150);
					dropPlayersBox.setLayout(new GridLayout());
					JTextField input = new JTextField("Enter comma-separated player list of players to drop.");
					JButton dropSubmitButton = new JButton("Submit");
					dropPlayersBox.add(input);
					dropPlayersBox.add(dropSubmitButton);
					dropPlayersBox.setVisible(true);
					dropSubmitButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							String newPlayers = input.getText();
							t.dropPlayer(newPlayers);
							t.save();
							dropPlayersBox.dispose();
						}
					});
				}
			}
		});
		toolbar.add(dropPlayersButton);

		JButton editNameButton = new JButton("Edit Name");
		editNameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (t.currentBattles.size() > 0) {
					JFrame nameEditor = new JFrame("Edit Name");
					nameEditor.setSize(400, 100);
					nameEditor.setLayout(new MigLayout("", "[grow,fill]"));
					ArrayList<String> playerNames = new ArrayList<String>();
					for (Player p : t.players) {
						playerNames.add(p.getName());
					}
					Collections.sort(playerNames);
					String[] ps = playerNames.toArray(new String[playerNames.size()]);
					@SuppressWarnings({ "rawtypes", "unchecked" })
					JComboBox players = new JComboBox(ps);
					JTextField editedName = new JTextField("Enter new name here.");
					JButton submitEditName = new JButton("Submit");
					players.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							editedName.setText(players.getSelectedItem().toString());
						}
					});
					nameEditor.add(players, "span 3,wrap");
					nameEditor.add(editedName, "span 2");
					nameEditor.add(submitEditName);
					nameEditor.setVisible(true);
					submitEditName.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							String oldName = players.getSelectedItem().toString();
							String newName = editedName.getText();
							t.renamePlayer(oldName, newName);
							pairingsBox.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
							resultsBox.setText(t.generateInDepthRankings(t.players) + "\n");
							t.save();
							nameEditor.dispose();
						}
					});
				}
			}
		});
		toolbar.add(editNameButton);

		JButton reopenGameButton = new JButton("Reopen Game");
		reopenGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (t.currentBattles.size() > 0) {
					JFrame nameEditor = new JFrame("Reopen Game");
					nameEditor.setSize(400, 100);
					nameEditor.setLayout(new MigLayout("", "[grow,fill]"));
					ArrayList<String> playerNames = new ArrayList<String>();
					for (Player p : t.players) {
						playerNames.add(p.getName());
					}
					Collections.sort(playerNames);
					String[] ps = playerNames.toArray(new String[playerNames.size()]);
					JComboBox p1s = new JComboBox(ps);
					JComboBox p2s = new JComboBox(ps);
					JButton submitEditName = new JButton("Submit");
					nameEditor.add(p1s, "span 2");
					nameEditor.add(p2s, "span 2, wrap");
					nameEditor.add(submitEditName);
					nameEditor.setVisible(true);
					submitEditName.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Player p1 = Utils.findPlayerByName(p1s.getSelectedItem().toString(), t.players);
							Player p2 = Utils.findPlayerByName(p2s.getSelectedItem().toString(), t.players);
							if (p1.getName().equals(p2.getName())) {
								postString("A player can't possibly have played themself.\n");
							} else {
								Boolean reopened = t.reopenBattle(p1, p2);
								if (reopened) {
									pairingsBox.setText(t.getCurrentBattles(t.currentBattles, t.roundString) + "\n");
									resultsBox.setText(t.generateInDepthRankings(t.players) + "\n");
									postString("Game between " + p1.getName() + " and  " + p2.getName() + " reopened.");
								} else {
									postString("Could not reopen game between " + p1.getName() + " and " + p2.getName()
											+ ". Did it actually occur?");
								}
							}
							t.save();
							nameEditor.dispose();
						}
					});
				}
			}
		});
		toolbar.add(reopenGameButton);

		startButton = new JButton("START"); 
		startButton.addActionListener(new ActionListener() { 
	      public void actionPerformed(ActionEvent e) { 
	        if (t.currentBattles.size() == 0) { 
	          JFrame seedPanel = new JFrame("Initial Seed"); 
	          seedPanel.setSize(450, 150); 
	          seedPanel.setLayout(new MigLayout("", "[grow,fill]")); 
	          ArrayList<String> playerNames = new ArrayList<String>(); 
	          for (Player p : t.players) { 
	            playerNames.add(p.getName()); 
	          } 
	          Collections.sort(playerNames); 
	          String[] ps = playerNames.toArray(new String[playerNames.size()]); 
	          JComboBox seed1 = new JComboBox(ps); 
	          JComboBox seed2 = new JComboBox(ps); 
	          JButton submitPair = new JButton("Seed Pairing"); 
	          JButton start = new JButton("Start Tournament"); 
	          seedPanel.add(seed1); 
	          seedPanel.add(seed2, "wrap"); 
	          seedPanel.add(submitPair); 
	          seedPanel.add(start); 
	          seedPanel.setVisible(true); 
	          submitPair.addActionListener(new ActionListener() { 
	            @Override 
	            public void actionPerformed(ActionEvent e) { 
	              String n1 = seed1.getSelectedItem().toString(); 
	              String n2 = seed2.getSelectedItem().toString(); 
	              if (!n1.equals(n2)) { 
	                Player p1 = Utils.findPlayerByName(n1, t.players); 
	                Player p2 = Utils.findPlayerByName(n2, t.players); 
	                //t.players.remove(p1); 
	                //t.players.remove(p2); 
	                //t.initialSeed(p1, p2); 
	                t.currentBattles.add(new Battle(p1, p2));
	                seed1.removeItem(p1.getName()); 
	                seed1.removeItem(p2.getName()); 
	                seed2.removeItem(p1.getName()); 
	                seed2.removeItem(p2.getName()); 
	                t.postListOfConfirmedSignups(); 
	              } 
	            } 
	          }); 
	          start.addActionListener(new ActionListener() { 
	            @Override 
	            public void actionPerformed(ActionEvent arg0) { 
	             t.allParticipantsIn = true;
	             t.setUserSelection("no"); 
	             seedPanel.dispose(); 
	             toolbar.remove(startButton); 
	             toolbar.repaint(); 
	            } 
	          }); 
	        } 
	      } 
	    }); 
	    toolbar.add(startButton); 
		
		pairingsBox = new JTextArea(20, 60);
		pairingsBox.setEditable(false);
		pairingsBox.setLineWrap(true);
		pairingsBox.setFont(new Font("monospaced", Font.PLAIN, 16));

		resultsBox = new JTextArea(20, 30);
		resultsBox.setEditable(false);
		resultsBox.setLineWrap(false);
		resultsBox.setFont(new Font("monospaced", Font.PLAIN, 14));

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

	public static String getTextFromArea() {
		return pairingsBox.getText();
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
}