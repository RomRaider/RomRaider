/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.uninstaller;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.util.AbstractUIHandler;

/**
 * The uninstaller frame class.
 * 
 * @author Julien Ponge
 */
public class UninstallerFrame extends JFrame
{

    /**
     * 
     */
    private static final long serialVersionUID = 3257281444152684850L;

    /** The icons database. */
    private IconsDatabase icons;

    /** The language pack. */
    protected static LocaleDatabase langpack;

    /** The target destroy checkbox. */
    protected JCheckBox targetDestroyCheckbox;

    /** The progress bar. */
    protected JProgressBar progressBar;

    /** The destroy button. */
    protected JButton destroyButton;

    /** The quit button. */
    protected JButton quitButton;

    /** The buttons hover color. */
    private Color buttonsHColor = new Color(230, 230, 230);

    /** The installation path. */
    protected String installPath;

    /**
     * The constructor.
     * 
     * @exception Exception Description of the Exception
     */
    public UninstallerFrame() throws Exception
    {
        super("IzPack - Uninstaller");

        // Initializations
        langpack = new LocaleDatabase(UninstallerFrame.class.getResourceAsStream("/langpack.xml"));
        getInstallPath();
        icons = new IconsDatabase();
        loadIcons();
        UIManager.put("OptionPane.yesButtonText", langpack.getString("installer.yes"));
        UIManager.put("OptionPane.noButtonText", langpack.getString("installer.no"));
        UIManager.put("OptionPane.cancelButtonText", langpack.getString("installer.cancel"));

        // Sets the frame icon
        setIconImage(icons.getImageIcon("JFrameIcon").getImage());

        // We build the GUI & show it
        buildGUI();
        addWindowListener(new WindowHandler());
        pack();
        centerFrame(this);
        setResizable(false);
        setVisible(true);
    }

    /** Builds the GUI. */
    private void buildGUI()
    {
        // We initialize our layout
        JPanel contentPane = (JPanel) getContentPane();
        GridBagLayout layout = new GridBagLayout();
        contentPane.setLayout(layout);
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(5, 5, 5, 5);

        // We prepare our action handler
        ActionsHandler handler = new ActionsHandler();

        // Prepares the glass pane to block gui interaction when needed
        JPanel glassPane = (JPanel) getGlassPane();
        glassPane.addMouseListener(new MouseAdapter() {});
        glassPane.addMouseMotionListener(new MouseMotionAdapter() {});
        glassPane.addKeyListener(new KeyAdapter() {});

        // We set-up the buttons factory
        ButtonFactory.useButtonIcons();
        ButtonFactory.useHighlightButtons();

        // We put our components

        JLabel warningLabel = new JLabel(langpack.getString("uninstaller.warning"), icons
                .getImageIcon("warning"), JLabel.TRAILING);
        buildConstraints(gbConstraints, 0, 0, 2, 1, 1.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.WEST;
        gbConstraints.fill = GridBagConstraints.NONE;
        layout.addLayoutComponent(warningLabel, gbConstraints);
        contentPane.add(warningLabel);

        targetDestroyCheckbox = new JCheckBox(langpack.getString("uninstaller.destroytarget")
                + installPath, false);
        buildConstraints(gbConstraints, 0, 1, 2, 1, 1.0, 0.0);
        layout.addLayoutComponent(targetDestroyCheckbox, gbConstraints);
        contentPane.add(targetDestroyCheckbox);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString(langpack.getString("InstallPanel.begin"));
        buildConstraints(gbConstraints, 0, 2, 2, 1, 1.0, 0.0);
        layout.addLayoutComponent(progressBar, gbConstraints);
        contentPane.add(progressBar);

        destroyButton = ButtonFactory.createButton(langpack.getString("uninstaller.uninstall"),
                icons.getImageIcon("delete"), buttonsHColor);
        destroyButton.addActionListener(handler);
        buildConstraints(gbConstraints, 0, 3, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.WEST;
        layout.addLayoutComponent(destroyButton, gbConstraints);
        contentPane.add(destroyButton);

        quitButton = ButtonFactory.createButton(langpack.getString("installer.quit"), icons
                .getImageIcon("stop"), buttonsHColor);
        quitButton.addActionListener(handler);
        buildConstraints(gbConstraints, 1, 3, 1, 1, 0.5, 0.0);
        gbConstraints.anchor = GridBagConstraints.EAST;
        layout.addLayoutComponent(quitButton, gbConstraints);
        contentPane.add(quitButton);

    }

    /**
     * Centers a window on screen.
     * 
     * @param frame The window to center.
     */
    private void centerFrame(Window frame)
    {
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Dimension frameSize = frame.getSize();
        frame.setLocation(center.x - frameSize.width / 2,
                center.y - frameSize.height / 2 - 10);
    }

    /**
     * Sets the parameters of a GridBagConstraints object.
     * 
     * @param gbc The constraints object.
     * @param gx The x coordinates.
     * @param gy The y coordinates.
     * @param gw The width.
     * @param wx The x wheight.
     * @param wy The y wheight.
     * @param gh Description of the Parameter
     */
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh,
            double wx, double wy)
    {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }

    /**
     * Gets the installation path from the log file.
     * 
     * @exception Exception Description of the Exception
     */
    private void getInstallPath() throws Exception
    {
        InputStream in = UninstallerFrame.class.getResourceAsStream("/install.log");
        InputStreamReader inReader = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(inReader);
        installPath = reader.readLine();
        reader.close();
    }

    /**
     * Loads the icons.
     * 
     * @exception Exception Description of the Exception
     */
    private void loadIcons() throws Exception
    {
        // Initialisations
        icons = new IconsDatabase();
        URL url;
        ImageIcon img;

        // We load it
        url = UninstallerFrame.class.getResource("/img/trash.png");
        img = new ImageIcon(url);
        icons.put("delete", img);

        url = UninstallerFrame.class.getResource("/img/stop.png");
        img = new ImageIcon(url);
        icons.put("stop", img);

        url = UninstallerFrame.class.getResource("/img/flag.png");
        img = new ImageIcon(url);
        icons.put("warning", img);

        url = UninstallerFrame.class.getResource("/img/JFrameIcon.png");
        img = new ImageIcon(url);
        icons.put("JFrameIcon", img);
    }

    /** Blocks GUI interaction. */
    public void blockGUI()
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);
        getGlassPane().setEnabled(true);
    }

    /** Releases GUI interaction. */
    public void releaseGUI()
    {
        getGlassPane().setEnabled(false);
        getGlassPane().setVisible(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * The window events handler.
     * 
     * @author Julien Ponge
     */
    private final class WindowHandler extends WindowAdapter
    {

        /**
         * We can't avoid the exit here, so don't call exit elsewhere.
         * 
         * @param e The event.
         */
        public void windowClosing(WindowEvent e)
        {
            System.exit(0);
        }
    }

    /**
     * The destroyer handler.
     * 
     * This class also implements the InstallListener because the FileExecutor needs it. TODO: get
     * rid of the InstallListener - implement generic Listener
     * 
     * @author Julien Ponge
     * @author Tino Schwarze
     */
    private final class DestroyerHandler implements
            com.izforge.izpack.util.AbstractUIProgressHandler
    {

        /**
         * The destroyer starts.
         * 
         * @param name The name of the overall action. Not used here.
         * @param max The maximum value of the progress.
         */
        public void startAction(final String name, final int max)
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    progressBar.setMinimum(0);
                    progressBar.setMaximum(max);
                    blockGUI();                    
                }
            });
        }

        /** The destroyer stops. */
        public void stopAction()
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    progressBar.setString(langpack.getString("InstallPanel.finished"));
                    targetDestroyCheckbox.setEnabled(false);
                    destroyButton.setEnabled(false);
                    releaseGUI();
                }
            });
        }

        /**
         * The destroyer progresses.
         * 
         * @param pos The actual position.
         * @param message The message.
         */
        public void progress(final int pos, final String message)
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    progressBar.setValue(pos);
                    progressBar.setString(message);
                }
            });
        }

        public void nextStep(String step_name, int step_no, int no_of_substeps)
        {
        }

        /**
         * Output a notification.
         * 
         * Does nothing here.
         * 
         * @param text
         */
        public void emitNotification(String text)
        {
        }

        /**
         * Output a warning.
         * 
         * @param text
         */
        public boolean emitWarning(String title, String text)
        {
            return (JOptionPane.showConfirmDialog(null, text, title, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION);
        }

        /**
         * The destroyer encountered an error.
         * 
         * @param error The error message.
         */
        public void emitError(String title, String error)
        {
            progressBar.setString(error);
            JOptionPane.showMessageDialog(null, error, title, JOptionPane.OK_CANCEL_OPTION);
        }

        /**
         * Ask the user a question.
         * 
         * @param title Message title.
         * @param question The question.
         * @param choices The set of choices to present.
         * 
         * @return The user's choice.
         * 
         * @see AbstractUIHandler#askQuestion(String, String, int)
         */
        public int askQuestion(String title, String question, int choices)
        {
            return askQuestion(title, question, choices, -1);
        }

        /**
         * Ask the user a question.
         * 
         * @param title Message title.
         * @param question The question.
         * @param choices The set of choices to present.
         * @param default_choice The default choice. (-1 = no default choice)
         * 
         * @return The user's choice.
         * @see AbstractUIHandler#askQuestion(String, String, int, int)
         */
        public int askQuestion(String title, String question, int choices, int default_choice)
        {
            int jo_choices = 0;

            if (choices == AbstractUIHandler.CHOICES_YES_NO)
                jo_choices = JOptionPane.YES_NO_OPTION;
            else if (choices == AbstractUIHandler.CHOICES_YES_NO_CANCEL)
                jo_choices = JOptionPane.YES_NO_CANCEL_OPTION;

            int user_choice = JOptionPane.showConfirmDialog(null, (Object) question, title,
                    jo_choices, JOptionPane.QUESTION_MESSAGE);

            if (user_choice == JOptionPane.CANCEL_OPTION) return AbstractUIHandler.ANSWER_CANCEL;

            if (user_choice == JOptionPane.YES_OPTION) return AbstractUIHandler.ANSWER_YES;

            if (user_choice == JOptionPane.NO_OPTION) return AbstractUIHandler.ANSWER_NO;

            return default_choice;
        }

    }

    /**
     * The actions events handler.
     * 
     * @author Julien Ponge
     */
    class ActionsHandler implements ActionListener
    {

        /**
         * Action handling method.
         * 
         * @param e The event.
         */
        public void actionPerformed(ActionEvent e)
        {
            Object src = e.getSource();
            if (src == quitButton)
                System.exit(0);
            else if (src == destroyButton)
            {
                destroyButton.setEnabled(false);
                Destroyer destroyer = new Destroyer(installPath,
                        targetDestroyCheckbox.isSelected(), new DestroyerHandler());
                destroyer.start();
            }
        }
    }

    /**
     * Returns the langpack.
     * 
     * @return Returns the langpack.
     */
    public static LocaleDatabase getLangpack()
    {
        return langpack;
    }

}
