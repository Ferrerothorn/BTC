package archive;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

public class GUI implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final static String newline = "\n";
	public static Tournament tourney;
	public static JTextField textField;
	public static JTextArea textOutputBox;
	public static JTextArea resultsBox;
	public static JPanel buttonWindow;
	public static JFrame frame = new JFrame("BTC");

	public GUI(Tournament t) {
		tourney = t;
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setLayout(new MigLayout("wrap 2"));

		textOutputBox = new JTextArea(20, 60);
		textOutputBox.setEditable(false);
		textOutputBox.setLineWrap(true);
		textOutputBox.setFont(new Font("monospaced", Font.PLAIN, 16));

		resultsBox = new JTextArea(20, 30);
		resultsBox.setEditable(false);
		resultsBox.setLineWrap(false);
		resultsBox.setFont(new Font("monospaced", Font.PLAIN, 14));

		buttonWindow = new CombatantPanel(tourney.currentBattles, tourney);
		paintButtons();
		buttonWindow.setVisible(true);

		JLabel inputLabel = new JLabel(" Enter options here: ");
		textField = new JTextField(500);
		textField.addActionListener(this);

		JPanel inputPanel = new JPanel(new MigLayout());
		inputPanel.add(inputLabel, "shrink, span 2");
		inputPanel.add(textField, "span 4");

		frame.add(new JScrollPane(textOutputBox));
		frame.add(new JScrollPane(buttonWindow), "span 2, grow, wrap");
		frame.add(new JScrollPane(resultsBox), "span 3, grow, wrap");
		frame.add(inputPanel, "span 3");
	}

	static void paintButtons() {
		buttonWindow.removeAll();
		for (Battle b : tourney.currentBattles) {
			JPanel b1Panel = new JPanel();
			JButton b1 = new JButton(b.getP1().getName());
			JPanel b2Panel = new JPanel();
			JButton b2 = new JButton(b.getP2().getName());
			b1Panel.add(b1);
			buttonWindow.add(b1Panel);
			b2Panel.add(b2);
			buttonWindow.add(b2Panel);
		}
		buttonWindow.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String text = textField.getText();
		tourney.setUserSelection(text);
		if (text.length() > 0) {
			textOutputBox.append(" " + text + newline);
			textField.setText(null);
			textOutputBox.setCaretPosition(textOutputBox.getDocument().getLength());
		}
	}

	public static String getTextFromArea() {
		return textOutputBox.getText();
	}

	public static void createAndShowGUI(Boolean show) {
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(show);
	}

	public static void postString(String s) {
		textOutputBox.append(s + newline);
		textOutputBox.setCaretPosition(textOutputBox.getDocument().getLength());
	}

	public static void postResultsString(String s) {
		resultsBox.setText("");
		resultsBox.append(s + newline);
		resultsBox.setCaretPosition(0);
	}

	public static void wipePane() {
		textOutputBox.setText("");
		textOutputBox.setCaretPosition(textOutputBox.getDocument().getLength());
	}

	public static String generateInDepthRankings(ArrayList<Player> ps) {
		String participantString = "";
		int longestPlayerNameLength = 0;

		for (Player p : ps) {
			if (p.getName().length() > longestPlayerNameLength) {
				longestPlayerNameLength = p.getName().length();
			}
		}

		if (tourney.topCutThreshold != 0) {
			participantString += "===Rankings - Top Cut===" + "\n";
			for (int i = 1; i <= tourney.topCutThreshold; i++) {
				if (!ps.get(i - 1).getName().equals("BYE")) {

					String pScore = Integer.toString(ps.get(i - 1).getScore());
					String pTB = Integer.toString(ps.get(i - 1).getTB());
					String pOWR = Integer.toString(ps.get(i - 1).getOppWr()) + "%";
					String pOOWR = Integer.toString(ps.get(i - 1).getOppOppWr()) + "%";

					participantString += Utils.rpad(
							"" + i + ") " + ps.get(i - 1).getName() + "                         ",
							longestPlayerNameLength + 7)
							+ Utils.rpad("Score: " + pScore + "                         ", 15) + "   "
							+ Utils.rpad("TB: " + pTB + "                         ", 8) + "   "
							+ Utils.rpad(("Opp WR: " + pOWR + "  "), 14) + "  "
							+ Utils.rpad("Opp Opp WR: " + pOOWR + "  ", 18) + "  "
							+ Utils.rpad("STB: " + ps.get(i - 1).oppositionTBSum, 9) + '\n';
				}
			}
			participantString += "==Rankings - Qualifiers==" + "\n";
		} else {
			participantString += "-=-=-=-Rankings-=-=-=-" + '\n';
		}

		for (int j = tourney.topCutThreshold + 1; j <= ps.size(); j++) {
			if (!ps.get(j - 1).getName().equals("BYE")) {

				String pScore = Integer.toString(ps.get(j - 1).getScore());
				String pTB = Integer.toString(ps.get(j - 1).getTB());
				String pOWR = Integer.toString(ps.get(j - 1).getOppWr()) + "%";
				String pOOWR = Integer.toString(ps.get(j - 1).getOppOppWr()) + "%";

				participantString += Utils.rpad("" + j + ") " + ps.get(j - 1).getName() + "                         ",
						longestPlayerNameLength + 7) + "   "
						+ Utils.rpad("Score: " + pScore + "                         ", 15) + "   "
						+ Utils.rpad("TB: " + pTB + "                         ", 8) + "   "
						+ Utils.rpad("Opp WR: " + pOWR + "                         ", 12) + "    "
						+ Utils.rpad("Opp Opp WR: " + pOOWR + "                         ", 16) + "  "
						+ Utils.rpad("STB: " + ps.get(j - 1).oppositionTBSum, 9) + '\n';
			}
		}
		return participantString;
	}

	public static void printCurrentBattles(ArrayList<Battle> battles, String roundString) {
		int longestPlayerNameLength = 0;
		for (Battle b : battles) {
			if (b.getP1().getName().length() > longestPlayerNameLength) {
				longestPlayerNameLength = b.getP1().getName().length();
			}
			if (b.getP2().getName().length() > longestPlayerNameLength) {
				longestPlayerNameLength = b.getP2().getName().length();
			}
		}

		postString(roundString);
		for (Battle b : battles) {
			String playerOneString = b.getP1().getName() + " (" + b.getP1().getPositionInRankings()
					+ ")                          ";
			String playerTwoString = b.getP2().getName() + " (" + b.getP2().getPositionInRankings()
					+ ")                          ";

			postString(Utils.rpad("Table " + b.getTableNumber() + ") ", 11)
					+ Utils.rpad(playerOneString, longestPlayerNameLength + 8) + "vs.    "
					+ Utils.rpad(playerTwoString, longestPlayerNameLength + 8));
		}
		TntFileManager.saveTournament(tourney);
	}

	public static void printRankings(String generateInDepthRankings) {
		postResultsString(generateInDepthRankings);
	}
	public static void refresh() {
		for (Component c : frame.getContentPane().getComponents()) {
			c.validate();
			c.repaint();
		}
	}
}