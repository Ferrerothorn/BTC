package swissTournamentRunner;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class PairingsPanel extends JFrame {

	public ArrayList<Battle> currentBattles;

	public PairingsPanel(Tournament t) {
		currentBattles = t.currentBattles;

		JFrame pairingsPanel = new JFrame("Report Results");
		pairingsPanel.setSize(700, 500);
		pairingsPanel.setLayout(new MigLayout("fill,wrap 5"));
		for (Battle b : currentBattles) {
			JLabel tableLabel = new JLabel("Table " + b.getTableNumber() + ")");
			JButton p1Button = new JButton(b.getP1().getName());
			JLabel vs = new JLabel("vs.");
			JButton p2Button = new JButton(b.getP2().getName());
			JLabel prediction = new JLabel("[" + b.getElo(b.getP1()) + "% - " + b.getElo(b.getP2()) + "%]");
			p1Button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Utils.reportWinnerByName(p1Button.getText(), p2Button.getText(), currentBattles);
					t.setUserSelection("P1");
					t.updateParticipantStats();
					t.sortRankings();
					GUI.pairingsBox.setText(t.getCurrentBattles(t.currentBattles, Tournament.roundString) + "\n");
					GUI.resultsBox.setText(Tournament.generateInDepthRankings(Tournament.players) + "\n");
					pairingsPanel.remove(tableLabel);
					pairingsPanel.remove(p1Button);
					pairingsPanel.remove(vs);
					pairingsPanel.remove(p2Button);
					pairingsPanel.remove(prediction);
					pairingsPanel.revalidate();
					pairingsPanel.repaint();
					t.save();
				}
			});
			p2Button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Utils.reportWinnerByName(p2Button.getText(), p1Button.getText(), t.currentBattles);
					t.setUserSelection("P2");
					t.updateParticipantStats();
					t.sortRankings();
					GUI.pairingsBox.setText(t.getCurrentBattles(t.currentBattles, Tournament.roundString) + "\n");
					GUI.resultsBox.setText(Tournament.generateInDepthRankings(Tournament.players) + "\n");
					pairingsPanel.remove(tableLabel);
					pairingsPanel.remove(p1Button);
					pairingsPanel.remove(vs);
					pairingsPanel.remove(p2Button);
					pairingsPanel.remove(prediction);
					pairingsPanel.revalidate();
					pairingsPanel.repaint();
					t.save();
				}
			});
			pairingsPanel.add(tableLabel, "growx, gapright 100");
			pairingsPanel.add(p1Button, "growx, gapright 100");
			pairingsPanel.add(vs);
			pairingsPanel.add(p2Button, "growx, gapleft 100");
			pairingsPanel.add(prediction, "growx, gapleft 100");
			this.setVisible(true);
		}

	}
}
