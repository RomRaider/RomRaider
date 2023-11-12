/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.romraider.dataflowSimulation.DataflowSimulation;
import com.romraider.dataflowSimulation.GenericAction;
import com.romraider.dataflowSimulation.GenericAction.GenericActionType;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.maps.Table;
import com.romraider.maps.Table2DView;
import com.romraider.maps.Table3DView;
import com.romraider.maps.TableView;

public final class DataflowFrame extends AbstractFrame {
	private static final long serialVersionUID = 7140513114169019846L;

	private final DataflowSimulation sim;

	HashMap<String, JTextField> inputsFields = new HashMap<String, JTextField>();

	LinkedList<JLabel> inputs = new LinkedList<JLabel>();
	LinkedList<JLabel> outputs = new LinkedList<JLabel>();;
	LinkedList<Component> centerDisplay = new LinkedList<Component>();
	JCheckBox enableLogButton;
	private final Font boldFont = new Font("Dialog", Font.BOLD, 14);

	public DataflowFrame(DataflowSimulation sim) {
		super("Dataflow Simulation: " + sim.getName());
		this.sim = sim;
		sim.setFrame(this);
		initUserInterface();
	}

	private void initUserInterface() {
		// setup main panel
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel contentPanel = buildContentPanel();
		mainPanel.add(new JScrollPane(contentPanel), BorderLayout.CENTER);
		mainPanel.add(buildInputPanel(), BorderLayout.NORTH);

		// Causes scrolling issues...
		// if (!sim.getDescription().isEmpty())
		// mainPanel.add(buildDescriptionPanel(), BorderLayout.NORTH);
		updateContentPanel();

		// add to container
		getContentPane().add(mainPanel);
	}

	private JPanel buildDescriptionPanel() {
		JPanel descPanel = new JPanel(new GridLayout(sim.getNumberOfActions(), 3));
		descPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Description"));
		JLabel desc = new JLabel(sim.getDescription());
		descPanel.add(desc);

		return descPanel;
	}

	private JPanel buildContentPanel() {
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Simulation"));

		for (int i = 0; i < sim.getNumberOfActions(); i++) {
			GenericAction a = sim.getAction(i);
			a.init(sim.getRom());

			JPanel line = new JPanel(new GridLayout(1, 3));

			JLabel input = new JLabel("");
			input.setHorizontalAlignment(JLabel.CENTER);
			input.setFont(boldFont);
			inputs.add(input);
			line.add(input);

			if (a.getType() == GenericActionType.CALCULATION) {
				JLabel center = new JLabel("");
				center.setHorizontalAlignment(JLabel.CENTER);
				center.setFont(boldFont);
				centerDisplay.add(center);
				line.add(center);
			} else if (a.getType() == GenericActionType.TABLE) {
				TableView v = ECUEditor.getTableViewForTable(a.getTable());
				if (v != null) {
					centerDisplay.add(v);
					line.add(v);
					v.populateTableVisual();
				} else {
					JLabel error = new JLabel("Failed to find table!");
					error.setFont(boldFont);
					error.setHorizontalAlignment(JLabel.CENTER);
					centerDisplay.add(error);
					line.add(error);
				}
			}

			JLabel output = new JLabel();
			output.setHorizontalAlignment(JLabel.CENTER);
			output.setFont(boldFont);
			outputs.add(output);
			line.add(output);
			contentPanel.add(line);
			contentPanel.add(Box.createVerticalStrut(20));
		}

		return contentPanel;
	}

	public void updateContentPanel() {
		// Update inputs if variables were changed via logger
		if (enableLogButton.isSelected()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					for (Map.Entry<String, JTextField> entry : inputsFields.entrySet()) {
						String newText = GenericAction.DEFAULT_FORMATTER.format(sim.getVariableValue(entry.getKey()));
						if (!newText.equals(entry.getValue().getText())) {
							entry.getValue().setText(newText);
						}
					}
				}
			});
		}

		for (int i = 0; i < sim.getNumberOfActions(); i++) {
			GenericAction a = sim.getAction(i);
			sim.simulate(i);

			inputs.get(i).setText(a.getInputText());
			outputs.get(i).setText(a.getOutputText());

			String centerText = a.getCenterTextReference();
			Table table = a.getTable();

			if (centerText != null) {
				// Add linebreak if needed
				((JLabel) centerDisplay.get(i)).setText("<html>" + centerText.replaceAll("(.{80})", "$1<br>"));
			} else if (table != null) {
				TableView v = ((TableView) centerDisplay.get(i));

				v.setOverlayLog(true);
				v.clearLiveDataTrace();
				if (v instanceof Table3DView) {
					Table3DView view3D = (Table3DView) v;
					view3D.getXAxis().highlightLiveData(a.getInputs().get(0).toString());
					view3D.getYAxis().highlightLiveData(a.getInputs().get(1).toString());
				} else if (v instanceof Table2DView) {
					Table2DView view2D = (Table2DView) v;
					view2D.getAxis().highlightLiveData(a.getInputs().get(0).toString());
				}
				v.drawTable();
			}

		}
	}

	private Component buildInputPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		JPanel inputPanel = new JPanel(gridBagLayout);
		inputPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Inputs"));
		JPanel fieldPanel = new JPanel(new FlowLayout());

		enableLogButton = new JCheckBox("Update from Logger");
		enableLogButton.setEnabled(!sim.getInputsWithLogParam().isEmpty());
		fieldPanel.add(enableLogButton);
		enableLogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sim.setUpdateFromLogger(enableLogButton.isSelected());

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						HashSet<String> inputsWithLog = sim.getInputsWithLogParam();
						for (Map.Entry<String, JTextField> entry : inputsFields.entrySet()) {
							if (inputsWithLog.contains(entry.getKey())) {
								entry.getValue().setEnabled(!enableLogButton.isSelected());
							}
						}
					}
				});
			}
		});

		LinkedList<String> inputs = sim.getInputs();
		for (final String i : inputs) {
			final JTextField text = new JTextField("", 5);
			fieldPanel.add(new JLabel(i));
			text.setText(sim.getVariableValue(i).toString());
			inputsFields.put(i, text);

			text.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					update();
				}

				public void removeUpdate(DocumentEvent e) {
					update();
				}

				public void insertUpdate(DocumentEvent e) {
					update();
				}

				public void update() {
					try {
						if (!text.getText().equals(sim.getVariableValue(i).toString())) {
							sim.setVariableValue(i, Double.parseDouble(text.getText()));
							updateContentPanel();
						}
					} catch (NumberFormatException ex) {
						// Do nothing
					}

				}
			});

			fieldPanel.add(text);
			inputPanel.add(fieldPanel);
		}

		return inputPanel;
	}

	public static void openWindow(final DataflowSimulation sim) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				DataflowFrame dataflow = new DataflowFrame(sim);
				dataflow.setIconImage(new ImageIcon(getClass().getResource("/graphics/romraider-ico.gif")).getImage());
				dataflow.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				dataflow.addWindowListener(dataflow);
				dataflow.setLocation(100, 50);
				dataflow.pack();
				dataflow.setVisible(true);
			}
		});
	}
}
