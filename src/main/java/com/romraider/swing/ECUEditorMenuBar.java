/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

import static com.romraider.Version.ABOUT_ICON;
import static com.romraider.Version.BUILDNUMBER;
import static com.romraider.Version.ECU_DEFS_URL;
import static com.romraider.Version.PRODUCT_NAME;
import static com.romraider.Version.SUPPORT_URL;
import static com.romraider.Version.VERSION;
import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.dataflowSimulation.DataflowSimulation;
import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.net.BrowserControl;
import com.romraider.ramtune.test.RamTuneTestApp;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public class ECUEditorMenuBar extends JMenuBar implements ActionListener {

	private static final long serialVersionUID = -4777040428837855236L;
	private static final ResourceBundle rb = new ResourceUtil().getBundle(ECUEditorMenuBar.class.getName());
	private final JMenu fileMenu = new JMenu(rb.getString("FILE"));
	private final JMenuItem openImage = new JMenuItem(rb.getString("OPENIMG"));
	private final JMenuItem quickSaveImage = new JMenuItem(rb.getString("SAVE"));
	private final JMenuItem saveImage = new JMenuItem(rb.getString("SAVEAS"));
	private final JMenuItem saveAsRepository = new JMenuItem(rb.getString("SAVEREPO"));
	private final JMenuItem refreshImage = new JMenuItem(rb.getString("REFRESH"));
	private final JMenuItem closeImage = new JMenuItem(rb.getString("CLOSE"));
	private final JMenuItem closeAll = new JMenuItem(rb.getString("CLOSEALL"));
	private final JMenuItem exit = new JMenuItem(rb.getString("EXIT"));
	private final JMenuItem exportDef = new JMenuItem(rb.getString("EXPORTDEF"));

	private final JMenu definitionMenu = new JMenu(rb.getString("ECUDEF"));
	private final JMenuItem defManager = new JMenuItem(rb.getString("ECUDEFMAN"));
	private final JMenuItem editDefinition = new JMenuItem(rb.getString("EDITDEF"));
	private final JMenuItem updateDefinition = new JMenuItem(rb.getString("GETDEF"));

	private final JMenu editMenu = new JMenu(rb.getString("EDIT"));
	private final JMenuItem settings = new JMenuItem(MessageFormat.format(rb.getString("SETTINGS"), PRODUCT_NAME));
	private final JMenuItem compareImages = new JMenuItem(rb.getString("COMPARE"));
	private final JMenu convertRom = new JMenu(rb.getString("CONVERT"));
	private final JMenuItem convertIncrease = new JMenuItem(rb.getString("ONE60"));
	private final JMenuItem convertDecrease = new JMenuItem(rb.getString("ONE92"));
	private final ButtonGroup convertGroup = new ButtonGroup();

	private final JMenu viewMenu = new JMenu(rb.getString("VIEW"));
	private final JMenuItem romProperties = new JMenuItem(rb.getString("PROPERTIES"));
	private final ButtonGroup levelGroup = new ButtonGroup();
	private final JMenu levelMenu = new JMenu(rb.getString("USERLVL"));
	private final JRadioButtonMenuItem level1 = new JRadioButtonMenuItem(rb.getString("BEGIN"));
	private final JRadioButtonMenuItem level2 = new JRadioButtonMenuItem(rb.getString("INTER"));
	private final JRadioButtonMenuItem level3 = new JRadioButtonMenuItem(rb.getString("ADVND"));
	private final JRadioButtonMenuItem level4 = new JRadioButtonMenuItem(rb.getString("HIGH"));
	private final JRadioButtonMenuItem level5 = new JRadioButtonMenuItem(rb.getString("DEBUG"));

	private final JMenu loggerMenu = new JMenu(rb.getString("LOGGER"));
	private final JMenuItem openLogger = new JMenuItem(rb.getString("LLOGGER"));

	private final JMenu toolMenu = new JMenu(rb.getString("TOOLS"));
	private final JMenuItem launchRamTuneTestApp = new JMenuItem(rb.getString("TESTAPP"));
	private final JMenuItem launchDataflowViews = new JMenu(rb.getString("DATAFLOW"));

	private final JMenu helpMenu = new JMenu(rb.getString("HELP"));
	private final JMenuItem about = new JMenuItem(MessageFormat.format(rb.getString("ABOUT"), PRODUCT_NAME));

	public ECUEditorMenuBar() {
		// file menu items
		add(fileMenu);
		fileMenu.setMnemonic('F');

		fileMenu.add(openImage);
		openImage.addActionListener(this);
		openImage.setMnemonic('O');

		fileMenu.add(saveImage);
		saveImage.addActionListener(this);
		saveImage.setMnemonic('S');

		fileMenu.add(saveAsRepository);
		saveAsRepository.setMnemonic('D');
		saveAsRepository.addActionListener(this);

		fileMenu.add(quickSaveImage);
		quickSaveImage.addActionListener(this);
		quickSaveImage.setMnemonic('q');

		fileMenu.add(refreshImage);
		refreshImage.addActionListener(this);
		refreshImage.setMnemonic('R');

		fileMenu.add(new JSeparator());

		fileMenu.add(exportDef);
		exportDef.addActionListener(this);
		// exportDef.setMnemonic('C');
		fileMenu.add(new JSeparator());

		fileMenu.add(closeImage);
		closeImage.addActionListener(this);
		closeImage.setMnemonic('C');

		// fileMenu.add(closeAll);
		// closeAll.addActionListener(this);
		// closeAll.setMnemonic('A');

		fileMenu.add(new JSeparator());

		fileMenu.add(exit);
		exit.addActionListener(this);
		exit.setMnemonic('X');

		// edit menu items
		add(editMenu);
		editMenu.setMnemonic('E');

		editMenu.add(settings);
		settings.addActionListener(this);
		settings.setMnemonic('S');

		editMenu.add(compareImages);
		compareImages.addActionListener(this);
		compareImages.setMnemonic('C');

		editMenu.add(convertRom);
		convertRom.setMnemonic('O');

		convertRom.add(convertIncrease);
		convertIncrease.addActionListener(this);
		convertIncrease.setMnemonic('I');

		convertRom.add(convertDecrease);
		convertDecrease.addActionListener(this);
		convertDecrease.setMnemonic('D');

		convertGroup.add(convertIncrease);
		convertGroup.add(convertDecrease);

		// ecu def menu items
		add(definitionMenu);
		definitionMenu.setMnemonic('D');

		definitionMenu.add(defManager);
		defManager.addActionListener(this);
		defManager.setMnemonic('D');

		definitionMenu.add(updateDefinition);
		updateDefinition.addActionListener(this);
		updateDefinition.setMnemonic('U');

		// definitionMenu.add(editDefinition);
		// editDefinition.setMnemonic('E');
		// editDefinition.addActionListener(this);

		// view menu items
		add(viewMenu);
		viewMenu.setMnemonic('V');

		viewMenu.add(romProperties);
		romProperties.addActionListener(this);
		romProperties.setMnemonic('P');

		viewMenu.add(levelMenu);
		levelMenu.setMnemonic('U');

		levelMenu.add(level1);
		level1.addActionListener(this);
		level1.setMnemonic('1');

		levelMenu.add(level2);
		level2.addActionListener(this);
		level2.setMnemonic('2');

		levelMenu.add(level3);
		level3.addActionListener(this);
		level3.setMnemonic('3');

		levelMenu.add(level4);
		level4.addActionListener(this);
		level4.setMnemonic('4');

		levelMenu.add(level5);
		level5.addActionListener(this);
		level5.setMnemonic('5');

		levelGroup.add(level1);
		levelGroup.add(level2);
		levelGroup.add(level3);
		levelGroup.add(level4);
		levelGroup.add(level5);

		// select correct userlevel button
		if (getSettings().getUserLevel() == 1) {
			level1.setSelected(true);
		} else if (getSettings().getUserLevel() == 2) {
			level2.setSelected(true);
		} else if (getSettings().getUserLevel() == 3) {
			level3.setSelected(true);
		} else if (getSettings().getUserLevel() == 4) {
			level4.setSelected(true);
		} else if (getSettings().getUserLevel() == 5) {
			level5.setSelected(true);
		}

		// logger menu items
		add(loggerMenu);
		loggerMenu.setMnemonic('L');

		loggerMenu.add(openLogger);
		openLogger.addActionListener(this);
		openLogger.setMnemonic('O');

		// ramtune menu items
		add(toolMenu);
		toolMenu.setMnemonic('R');

		toolMenu.add(launchRamTuneTestApp);
		launchRamTuneTestApp.addActionListener(this);
		launchRamTuneTestApp.setMnemonic('L');

		toolMenu.add(launchDataflowViews);

		// help menu items
		add(helpMenu);
		helpMenu.setMnemonic('H');

		helpMenu.add(about);
		about.addActionListener(this);
		about.setMnemonic('A');

		updateMenu();
	}

	public void updateMenu() {
		Rom lastSelectedRom = ECUEditorManager.getECUEditor().getLastSelectedRom();
		String file = lastSelectedRom == null ? "" : lastSelectedRom.getFileName();

		if ("".equals(file)) {
			quickSaveImage.setEnabled(false);
			saveImage.setEnabled(false);
			saveAsRepository.setEnabled(false);
			exportDef.setEnabled(false);
			closeImage.setEnabled(false);
			// closeAll.setEnabled(false);
			romProperties.setEnabled(false);
			quickSaveImage.setText(MessageFormat.format(rb.getString("SAVE"), file));
			saveImage.setText(rb.getString("SAVEAS"));
			saveAsRepository.setText(rb.getString("SAVEREPO"));
			exportDef.setText(rb.getString("EXPORTDEFF"));
			compareImages.setEnabled(false);
			convertRom.setEnabled(false);
		} else {
			saveImage.setEnabled(true);
			quickSaveImage.setEnabled(true);
			saveAsRepository.setEnabled(true);
			closeImage.setEnabled(true);
			exportDef.setEnabled(true);
			// closeAll.setEnabled(true);
			romProperties.setEnabled(true);
			quickSaveImage.setText(MessageFormat.format(rb.getString("QUICKSAVE"), file));
			saveImage.setText(MessageFormat.format(rb.getString("SAVEFAS"), file));
			saveAsRepository.setText(MessageFormat.format(rb.getString("SAVEFREPO"), file));
			exportDef.setText(MessageFormat.format(rb.getString("EXPORTDEF"), file));
			compareImages.setEnabled(true);
			convertRom.setEnabled(true);
		}
		refreshImage.setText(MessageFormat.format(rb.getString("REFRESHF"), file));
		closeImage.setText(MessageFormat.format(rb.getString("CLOSEF"), file));
		romProperties.setText(MessageFormat.format(rb.getString("PROPERTIESF"), file));

		updateDataflowSimulations(lastSelectedRom);
		int lastSelectedRomSize = 0;
		if (null != lastSelectedRom) {
			lastSelectedRomSize = lastSelectedRom.getRealFileSize();
		}

		if (Settings.SIXTEENBIT_SMALL_SIZE == lastSelectedRomSize) {
			convertIncrease.setEnabled(true);
			convertDecrease.setEnabled(false);
		} else if (Settings.SIXTEENBIT_LARGE_SIZE == lastSelectedRomSize) {
			convertIncrease.setEnabled(false);
			convertDecrease.setEnabled(true);
		} else {
			convertIncrease.setEnabled(false);
			convertDecrease.setEnabled(false);
		}

		editDefinition.setEnabled(false);
		revalidate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ECUEditor parent = ECUEditorManager.getECUEditor();
		if (e.getSource() == openImage) {
			try {
				openImageDialog();
			} catch (Exception ex) {
				showMessageDialog(parent, new DebugPanel(ex, getSettings().getSupportURL()), rb.getString("EXCEPTN"),
						ERROR_MESSAGE);
			}
		} else if (e.getSource() == saveImage) {
			try {
				this.saveImage(false);
			} catch (Exception ex) {
				showMessageDialog(parent, new DebugPanel(ex, getSettings().getSupportURL()), rb.getString("EXCEPTN"),
						ERROR_MESSAGE);
			}
		} else if (e.getSource() == quickSaveImage) {
			try {
				this.saveImage(true);
			} catch (Exception ex) {
				showMessageDialog(parent, new DebugPanel(ex, getSettings().getSupportURL()), rb.getString("EXCEPTN"),
						ERROR_MESSAGE);
			}
		} else if (e.getSource() == saveAsRepository) {
			try {
				this.saveAsRepository();
			} catch (Exception ex) {
				showMessageDialog(parent, new DebugPanel(ex, getSettings().getSupportURL()), rb.getString("EXCEPTN"),
						ERROR_MESSAGE);
			}
		} else if (e.getSource() == closeImage) {
			parent.closeImage();
		} else if (e.getSource() == closeAll) {
			parent.closeAllImages();
		} else if (e.getSource() == exportDef) {
			parent.handleExportDefinition();
		} else if (e.getSource() == exit) {
			parent.handleExit();
			System.exit(0);

		} else if (e.getSource() == romProperties) {
			showMessageDialog(parent, new RomPropertyPanel(parent.getLastSelectedRom()),
					MessageFormat.format(rb.getString("PROPERTIESF"), parent.getLastSelectedRom().getRomIDString()),
					INFORMATION_MESSAGE);

		} else if (e.getSource() == refreshImage) {
			try {
				refreshImage();
			} catch (Exception ex) {
				showMessageDialog(parent, new DebugPanel(ex, getSettings().getSupportURL()), rb.getString("EXCEPTN"),
						ERROR_MESSAGE);
			}

		} else if (e.getSource() == settings) {
			SettingsForm form = new SettingsForm();
			form.setLocationRelativeTo(parent);
			form.setVisible(true);

		} else if (e.getSource() == compareImages) {
			CompareImagesForm form = new CompareImagesForm(parent.getImages(), parent.getIconImage());
			form.setLocationRelativeTo(parent);
			form.setVisible(true);

		} else if (e.getSource() == convertIncrease) {
			try {
				increaseRomSize();
				refreshImage();
			} catch (Exception ex) {
				showMessageDialog(parent, new DebugPanel(ex, getSettings().getSupportURL()), rb.getString("EXCEPTN"),
						ERROR_MESSAGE);
			}

		} else if (e.getSource() == convertDecrease) {
			try {
				decreaseRomSize();
				refreshImage();
			} catch (Exception ex) {
				showMessageDialog(parent, new DebugPanel(ex, getSettings().getSupportURL()), rb.getString("EXCEPTN"),
						ERROR_MESSAGE);
			}

		} else if (e.getSource() == defManager) {
			DefinitionManager form = new DefinitionManager();
			form.setLocationRelativeTo(parent);
			form.setVisible(true);

		} else if (e.getSource() == level1) {
			parent.setUserLevel(1);

		} else if (e.getSource() == level2) {
			parent.setUserLevel(2);

		} else if (e.getSource() == level3) {
			parent.setUserLevel(3);

		} else if (e.getSource() == level4) {
			parent.setUserLevel(4);

		} else if (e.getSource() == level5) {
			parent.setUserLevel(5);

		} else if (e.getSource() == openLogger) {
			parent.launchLogger();
		} else if (e.getSource() == updateDefinition) {
			BrowserControl.displayURL(ECU_DEFS_URL);

		} else if (e.getSource() == launchRamTuneTestApp) {
			RamTuneTestApp.startTestApp(DISPOSE_ON_CLOSE);
		} else if (e.getSource() == about) {
			showMessageDialog(parent,
					MessageFormat.format(rb.getString("ABOUTMSG"), PRODUCT_NAME, VERSION, BUILDNUMBER, SUPPORT_URL,
							System.getProperty("java.vendor"), System.getProperty("java.runtime.version"),
							System.getProperty("os.arch")),
					MessageFormat.format(rb.getString("ABOUT"), PRODUCT_NAME), INFORMATION_MESSAGE, ABOUT_ICON);
		}
	}

	private void updateDataflowSimulations(Rom rom) {
		launchDataflowViews.removeAll();
		if (rom == null) {
			launchDataflowViews.setEnabled(false);
		} else {
			List<DataflowSimulation> sims = rom.getSimulations();
			if (sims.isEmpty()) {
				launchDataflowViews.setEnabled(false);
			} else {
				launchDataflowViews.setEnabled(true);
				for (final DataflowSimulation sim : sims) {
					JMenuItem item = new JMenuItem(sim.getName());
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							DataflowFrame.openWindow(sim);
						}
					});
					launchDataflowViews.add(item);
				}
			}
		}
	}

	public void refreshImage() throws Exception {
		ECUEditor parent = ECUEditorManager.getECUEditor();
		if (parent.getLastSelectedRom() != null) {
			File file = parent.getLastSelectedRom().getFullFileName();
			parent.closeImage();
			parent.openImage(file);
		}
	}

	public void openImageDialog() throws Exception {
		ECUEditor parent = ECUEditorManager.getECUEditor();
		JFileChooser fc = new JFileChooser(SettingsManager.getSettings().getLastImageDir());
		fc.setFileFilter(new ECUImageFilter());
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle(rb.getString("OPENIMG"));

		if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			parent.openImages(fc.getSelectedFiles());
			SettingsManager.getSettings().setLastImageDir(fc.getCurrentDirectory());
		}
	}

	public void saveImage(boolean quickSave) throws Exception {
		Rom lastSelectedRom = ECUEditorManager.getECUEditor().getLastSelectedRom();
		if (lastSelectedRom != null) {

			File selectedFile = lastSelectedRom.getFullFileName();
			if (!quickSave)
				selectedFile = getImageOutputFile();

			if (null != selectedFile) {
				byte[] output = lastSelectedRom.saveFile();
				this.writeImage(output, selectedFile);
			}
		}
	}

	private File getImageOutputFile() throws Exception {
		ECUEditor parent = ECUEditorManager.getECUEditor();

		JFileChooser fc = new JFileChooser(SettingsManager.getSettings().getLastImageDir());
		fc.setFileFilter(new ECUImageFilter());
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();

			// Append suffix if user didn't set anything
			if (!selectedFile.getName().contains(".")) {
				Rom lastSelectedRom = ECUEditorManager.getECUEditor().getLastSelectedRom();
				String lastFile = lastSelectedRom.getFileName().toLowerCase();
				String format = ".bin";
				if (lastFile.endsWith(".hex"))
					format = ".hex";
				selectedFile = new File(selectedFile + format);
			}

			if (selectedFile.exists()) {
				int option = showConfirmDialog(parent,
						MessageFormat.format(rb.getString("OVERWRITE"), selectedFile.getName()));

				// option: 0 = Cancel, 1 = No
				if (option == CANCEL_OPTION || option == 1) {
					return null;
				}
			}

			return selectedFile;
		}
		return null;
	}

	private void writeImage(byte[] output, File selectedFile) throws Exception {
		ECUEditor parent = ECUEditorManager.getECUEditor();
		FileOutputStream fos = new FileOutputStream(selectedFile);
		try {
			fos.write(output);
		} finally {
			fos.close();
		}
		parent.getLastSelectedRom().setFullFileName(selectedFile.getAbsoluteFile());
		parent.setLastSelectedRom(parent.getLastSelectedRom());
		SettingsManager.getSettings().setLastImageDir(selectedFile.getParentFile());
	}

	private File getRepositoryOutputDir() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(getSettings().getLastRepositoryDir());
		fc.setDialogTitle(rb.getString("SELECTDIR"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		// disable the "All files" option
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(ECUEditorManager.getECUEditor()) == JFileChooser.APPROVE_OPTION) {
			File selectedDir = fc.getSelectedFile();
			if (selectedDir.exists()) {
				int option = showConfirmDialog(ECUEditorManager.getECUEditor(),
						MessageFormat.format(rb.getString("OVERWRITE"), selectedDir.getName()));

				// option: 0 = Cancel, 1 = No
				if (option == CANCEL_OPTION || option == 1) {
					return null;
				}
			}
			return selectedDir;
		}
		return null;
	}

	private void saveAsRepository() throws Exception {
		File selectedDir = getRepositoryOutputDir();
		String separator = System.getProperty("file.separator");

		if (null != selectedDir) {
			for (TableTreeNode treeNode : ECUEditorManager.getECUEditor().getLastSelectedRom().getTableNodes()
					.values()) {
				Table table = treeNode.getTable();
				String category = table.getCategory();
				String tableName = table.getName();
				String tableDirString = selectedDir.getAbsolutePath() + separator + category;
				File tableDir = new File(tableDirString.replace('/', '-'));
				tableDir.mkdirs();
				String tableFileString = tableDir.getAbsolutePath() + separator + tableName + ".txt";
				File tableFile = new File(tableFileString.replace('/', '-'));

				if (tableFile.exists()) {
					tableFile.delete();
				}

				tableFile.createNewFile();
				StringBuffer tableData = table.getTableAsString();
				BufferedWriter out = new BufferedWriter(new FileWriter(tableFile));
				try {
					out.write(tableData.toString());
				} finally {
					try {
						out.close();
					} catch (Exception ex) {
						// Do Nothing.
					}
				}
			}
			getSettings().setLastRepositoryDir(selectedDir);
		}
	}

	private void increaseRomSize() throws Exception {
		Rom lastSelectedRom = ECUEditorManager.getECUEditor().getLastSelectedRom();
		if (lastSelectedRom != null) {
			File selectedFile = getImageOutputFile();
			if (null != selectedFile) {
				if (lastSelectedRom.getRealFileSize() != Settings.SIXTEENBIT_SMALL_SIZE) {
					showMessageDialog(ECUEditorManager.getECUEditor(), rb.getString("CONVERTERR"));
				} else {
					byte[] output = lastSelectedRom.saveFile();
					byte[] incOutput = new byte[Settings.SIXTEENBIT_LARGE_SIZE];
					System.arraycopy(output, 0, incOutput, 0, Settings.SIXTEENBIT_START_ADDRESS);
					System.arraycopy(output, Settings.SIXTEENBIT_START_ADDRESS, incOutput,
							Settings.SIXTEENBIT_END_ADDRESS, Settings.SIXTEENBIT_SEGMENT_SIZE);
					for (int i = Settings.SIXTEENBIT_START_ADDRESS; i < Settings.SIXTEENBIT_END_ADDRESS; i++) {
						// Fill space.
						incOutput[i] = Settings.SIXTEENBIT_SEGMENT_VALUE;
					}
					this.writeImage(incOutput, selectedFile);
				}
			}
		}
	}

	private void decreaseRomSize() throws Exception {
		Rom lastSelectedRom = ECUEditorManager.getECUEditor().getLastSelectedRom();
		if (lastSelectedRom != null) {
			File selectedFile = getImageOutputFile();
			if (null != selectedFile) {
				if (lastSelectedRom.getRealFileSize() != Settings.SIXTEENBIT_LARGE_SIZE) {
					showMessageDialog(ECUEditorManager.getECUEditor(), rb.getString("CONVERTERR"));
				} else {
					byte[] output = lastSelectedRom.saveFile();
					byte[] decOutput = new byte[Settings.SIXTEENBIT_SMALL_SIZE];
					System.arraycopy(output, 0, decOutput, 0, Settings.SIXTEENBIT_START_ADDRESS);
					System.arraycopy(output, Settings.SIXTEENBIT_END_ADDRESS, decOutput,
							Settings.SIXTEENBIT_START_ADDRESS, Settings.SIXTEENBIT_SEGMENT_SIZE);
					this.writeImage(decOutput, selectedFile);
				}
			}
		}
	}

	private Settings getSettings() {
		return SettingsManager.getSettings();
	}
}