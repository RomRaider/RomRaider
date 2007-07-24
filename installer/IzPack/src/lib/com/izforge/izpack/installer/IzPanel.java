/*
 * $Id: IzPanel.java 1816 2007-04-23 19:57:27Z jponge $
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
package com.izforge.izpack.installer;

import com.izforge.izpack.Panel;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.LayoutConstants;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.MultiLineLabel;
import com.izforge.izpack.util.VariableSubstitutor;
import net.n3.nanoxml.XMLElement;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;

/**
 * Defines the base class for the IzPack panels. Any panel should be a subclass of it and should
 * belong to the <code>com.izforge.izpack.panels</code> package.
 * Since IzPack version 3.9 the layout handling will be delegated to the class
 * LayoutHelper which can be accessed by <code>getLayoutHelper</code>.
 * There are some layout helper methods in this class which will be exist some time longer,
 * but they are deprecated. At a redesign or new panel use the layout helper.
 * There is a special layout manager for IzPanels. This layout manager will be supported
 * by the layout helper. There are some points which should be observed at layouting.
 * One point e.g. is the anchor. All IzPanels have to be able to use different anchors, as
 * minimum CENTER and NORTHWEST. 
 * To use a consistent appearance use this special layout manger and not others.
 * 
 * @author Julien Ponge
 * @author Klaus Bartz
 */
public class IzPanel extends JPanel implements AbstractUIHandler, LayoutConstants
{

    private static final long serialVersionUID = 3256442495255786038L;

    /** The helper object which handles layout */
    protected LayoutHelper layoutHelper;
    
    /** The component which should get the focus at activation */
    protected Component initialFocus = null;

    /**
     * The installer internal data (actually a melting-pot class with all-public fields.
     */
    protected InstallData idata;

    /** The parent IzPack installer frame. */
    protected InstallerFrame parent;
    
    /** i.e. "com.izforge.izpack.panels.HelloPanel" */
    protected String myFullClassname;

    /** myClassname=i.e "FinishPanel" */
    protected String myClassname;

    /** i.e. "FinishPanel." useFull for getString() */
    protected String myPrefix;

    /** internal headline string */
    protected String headline;
    
    /** internal headline Label */
    protected JLabel headLineLabel;
    
    /** Is this panel general hidden or not */
    protected boolean hidden;
    
    /** HEADLINE = "headline" */
    public final static String HEADLINE = "headline";
    
    /** X_ORIGIN = 0 */
    public final static int X_ORIGIN = 0;

    /** Y_ORIGIN = 0 */
    public final static int Y_ORIGIN = 0;
    /** D = "." ( dot ) */
    public final static String D = ".";

    /** d = D */
    public final static String d = D;
    
    /** COLS_1 = 1 */
    public final static int COLS_1 = 1;

    /** ROWS_1 = 1 */
    public final static int ROWS_1 = 1;

    /**
     * Information about the panel
     */
    public Panel metadata;

    /**
     * The constructor.
     * 
     * @param parent The parent IzPack installer frame.
     * @param idata The installer internal data.
     */
    public IzPanel(InstallerFrame parent, InstallData idata)
    {
      this( parent, idata, (LayoutManager2) null);  
    }
    
    /**
     * Creates a new IzPanel object with the given layout manager. Valid layout manager are the
     * IzPanelLayout and the GridBagLayout. New panels should be use the IzPanelLaout.
     * If lm is null, no layout manager will be created or initialized. 
     * @param parent The parent IzPack installer frame.
     * @param idata The installer internal data.
     * @param lm layout manager to be used with this IzPanel
     */
    public IzPanel(InstallerFrame parent, InstallData idata, LayoutManager2 lm)
    {
      super();
      init( parent, idata );
      if( lm != null )
          getLayoutHelper().startLayout(lm);
    }
    
    /**
     * Creates a new IzPanel object.
     *
     * @param parent the Parent Frame
     * @param idata Installers Runtime Data Set
     * @param iconName The Headline IconName
     */
    public IzPanel( InstallerFrame parent, InstallData idata, String iconName )
    {
      this( parent, idata, iconName, -1 );
    }    
    
    /**
     * The constructor with Icon.
     *
     * @param parent The parent IzPack installer frame.
     * @param idata The installer internal data.
     * @param iconName A iconname to show as left oriented headline-leading Icon.
     * @param instance An instance counter
     */
    public IzPanel( InstallerFrame parent, InstallData idata, String iconName, int instance )
    {
      this(parent, idata);
      buildHeadline( iconName, instance );
    }
    
    /** 
     * Build the IzPanel internal Headline. If an external headline#
     * is used, this method returns immediately with false.
     * Allows also to display a leading Icon for the PanelHeadline.
     * This Icon can also be different if the panel has more than one Instances. 
     * The UserInputPanel is one of these Candidates.
     * 
     * by marc.eppelmann&#064;gmx.de
     *
     * @param imageIconName an Iconname
     * @param instanceNumber an panel instance
     *
     * @return true if successful build
     */
    protected boolean buildHeadline( String imageIconName, int instanceNumber )
    {
      boolean result = false;
      if( parent.isHeading(this))
          return(false);

      // TODO: proteced instancenumber
      // TODO: is to be validated
      // TODO: 
      // TODO: first Test if a Resource for your protected Instance exists.
      String headline;
      String headlineSearchBaseKey = myClassname + d + "headline"; // Results for example in "ShortcutPanel.headline" : 

      if( instanceNumber > -1 )  // Search for Results for example in "ShortcutPanel.headline.1, 2, 3 etc." :
      {
        String instanceSearchKey = headlineSearchBaseKey + d +
                                   Integer.toString( instanceNumber );

        String instanceHeadline = getString( instanceSearchKey );

        if( Debug.isLOG() ) 
        { 
          System.out.println( "found headline: " + instanceHeadline  +  d + " for instance # " +  instanceNumber ); 
        }
        if( ! instanceSearchKey.equals( instanceHeadline ) )
        {
          headline = instanceHeadline;
        }
        else
        {
          headline = getString( headlineSearchBaseKey );
        }
      }
      else
      {
        headline = getString( headlineSearchBaseKey );
      }

      if( headline != null )
      {
        if( ( imageIconName != null ) && ! "".equals( imageIconName ) )
        {
          headLineLabel = new JLabel( headline, getImageIcon( imageIconName ),
                                      SwingConstants.LEADING );
        }
        else
        {
          headLineLabel = new JLabel( headline );
        }

        Font  font  = headLineLabel.getFont(  );
        float size  = font.getSize(  );
        int   style = 0;
        font = font.deriveFont( style, ( size * 1.5f ) );
        headLineLabel.setFont( font );

        GridBagConstraints gbc = new GridBagConstraints(  );

        gbc.gridx      = 0;
        gbc.gridy      = 0;
        gbc.gridwidth  = 1;
        gbc.gridheight = 1;

        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        headLineLabel.setName( HEADLINE );
        ((GridBagLayout) getLayout()).addLayoutComponent( headLineLabel, gbc );

        add( headLineLabel );
      }

      return result;
    }
    
    /** 
     * Gets a language Resource String from the parent, which  holds these global resource.
     *
     * @param key The Search key
     *
     * @return The Languageresource or the key if not found.
     */
    public String getString( String key )
    {
      return parent.langpack.getString( key );
    }
    
    /** 
     * Gets a named image icon
     *
     * @param iconName a valid image icon
     *
     * @return the icon
     */
    public ImageIcon getImageIcon( String iconName )
    {
      return parent.icons.getImageIcon( iconName );
    }


    
    /**
     * Inits and sets the internal layout helper object.
     */
    protected void initLayoutHelper()
    {
        layoutHelper = new LayoutHelper(this);
    }
    

    /** 
     * Gets and fills the classname fields
     */
    protected void getClassName(  )
    {
      myFullClassname = getClass(  ).getName(  );
      myClassname     = myFullClassname.substring( myFullClassname.lastIndexOf( "." ) + 1 );
      myPrefix        = myClassname + ".";
    }
    
    /** 
     * Internal init method
     *
     * @param parent the parent frame
     * @param idata installers runtime dataset
     */
    protected void init( InstallerFrame parent, InstallData idata )
    { 
      getClassName(  );
      
      this.idata           = idata;
      this.parent          = parent;
      // To get the Panel object via idata is a hack because InstallData will
      // be hold global data, not panel specific data. But the Panel object will
      // be needed in the constructor of some derived classes. And to expand the
      // constructor is also not a good way because all derived classes have to
      // change then the signature. Also the custem IzPanels elswhere. Therefore
      // this hack...
      // Problems with this hack will be exist if more than one threads calls the
      // constructors of derived clases. This is not the case.
      this.metadata = idata.currentPanel;
      idata.currentPanel = null;
      initLayoutHelper(  );

    }

    /**
     * Indicates wether the panel has been validated or not. The installer won't let the user go
     * further through the installation process until the panel is validated. Default behaviour is
     * to return <code>true</code>.
     * 
     * @return A boolean stating wether the panel has been validated or not.
     */
    public boolean isValidated()
    {
        return true;
    }

    /**
     * This method is called when the panel becomes active. Default is to do nothing : feel free to
     * implement what you need in your subclasses. A panel becomes active when the user reaches it
     * during the installation process.
     */
    public void panelActivate()
    {
    }

    /**
     * This method is called when the panel gets desactivated, when the user switches to the next
     * panel. By default it doesn't do anything.
     */
    public void panelDeactivate()
    {
    }

    /**
     * Asks the panel to set its own XML data that can be brought back for an automated installation
     * process. Use it as a blackbox if your panel needs to do something even in automated mode.
     * 
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    public void makeXMLData(XMLElement panelRoot)
    {
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

        int user_choice = JOptionPane.showConfirmDialog(this, (Object) question, title, jo_choices,
                JOptionPane.QUESTION_MESSAGE);

        if (user_choice == JOptionPane.CANCEL_OPTION) return AbstractUIHandler.ANSWER_CANCEL;

        if (user_choice == JOptionPane.YES_OPTION) return AbstractUIHandler.ANSWER_YES;

		if (user_choice == JOptionPane.CLOSED_OPTION) return AbstractUIHandler.ANSWER_NO;

        if (user_choice == JOptionPane.NO_OPTION) return AbstractUIHandler.ANSWER_NO;

        return default_choice;
    }

	public boolean emitNotificationFeedback(String message)
    {
       return (JOptionPane.showConfirmDialog(this, message, idata.langpack.getString("installer.Message"), JOptionPane.WARNING_MESSAGE,
             JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
    }

    /**
     * Notify the user about something.
     * 
     * @param message The notification.
     */
    public void emitNotification(String message)
    {
        JOptionPane.showMessageDialog(this, message);
    }

    /**
     * Warn the user about something.
     * 
     * @param message The warning message.
     */
    public boolean emitWarning(String title, String message)
    {
        return (JOptionPane.showConfirmDialog(this, message, title, JOptionPane.WARNING_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);

    }

    /**
     * Notify the user of some error.
     * 
     * @param message The error message.
     */
    public void emitError(String title, String message)
    {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Returns the component which should be get the focus at activation of this panel.
     * 
     * @return the component which should be get the focus at activation of this panel
     */
    public Component getInitialFocus()
    {
        return initialFocus;
    }

    /**
     * Sets the component which should be get the focus at activation of this panel.
     * 
     * @param component which should be get the focus at activation of this panel
     */
    public void setInitialFocus(Component component)
    {
        initialFocus = component;
    }

    /**
     * Calls the langpack of parent InstallerFrame for the String <tt>RuntimeClassName.subkey</tt>.
     * Do not add a point infront of subkey, it is always added in this method.
     * If <tt>RuntimeClassName.subkey</tt> is not found, the super class name will be used
     * until it is <tt>IzPanel</tt>. If no key will be found, null returns.
     * 
     * @param subkey the subkey for the string which should be returned
     * @return the founded string
     */
    public String getI18nStringForClass(String subkey)
    {
        String retval = null;
        Class clazz = this.getClass();
        while (retval == null && !clazz.getName().endsWith(".IzPanel"))
        {
            retval = getI18nStringForClass(clazz.getName(), subkey, null);
            clazz = clazz.getSuperclass();
        }
        return (retval);
    }

    /**
     * Calls the langpack of parent InstallerFrame for the String <tt>RuntimeClassName.subkey</tt>.
     * Do not add a point infront of subkey, it is always added in this method.
     * If no key will be found the key or - if alternate class is null - null returns.
     * 
     * @param subkey the subkey for the string which should be returned
     * @param alternateClass the short name of the class which should be used if no string is
     * present with the runtime class name
     * @return the founded string
     */
    public String getI18nStringForClass(String subkey, String alternateClass)
    {
        return( getI18nStringForClass(getClass().getName(), subkey, alternateClass));

    }

    private String getI18nStringForClass(String curClassName, String subkey, String alternateClass)
    {

        int nameStart = curClassName.lastIndexOf('.') + 1;
        curClassName = curClassName.substring(nameStart, curClassName.length());
        StringBuffer buf = new StringBuffer();
        buf.append(curClassName).append(".").append(subkey);
        String fullkey = buf.toString();
        String panelid = null;
        if( getMetadata() != null )
        {
            panelid = getMetadata().getPanelid();
        }
        String retval = null;
        if (panelid != null)
        {
            buf.append(".");
            buf.append(panelid);
            retval = parent.langpack.getString(buf.toString());
        }
        if (retval == null || retval.startsWith(fullkey))
            retval = parent.langpack.getString(fullkey);
        if (retval == null || retval.startsWith(fullkey))
        {
            if (alternateClass == null) return (null);
            buf.delete(0, buf.length());
            buf.append(alternateClass).append(".").append(subkey);
            retval = parent.langpack.getString(buf.toString());
        }
        if (retval != null && retval.indexOf('$') > -1)
        {
            VariableSubstitutor substitutor = new VariableSubstitutor(idata.getVariables());
            retval = substitutor.substitute(retval, null);
        }
        return (retval);
    }
    
        
    /**
     * Returns the parent of this IzPanel (which is a InstallerFrame).
     * 
     * @return the parent of this IzPanel
     */
    public InstallerFrame getInstallerFrame()
    {
        return (parent);
    }

    // ------------- Helper for common used components ----- START ---

    /**
     * Creates a label via LabelFactory using iconId, pos and method getI18nStringForClass for
     * resolving the text to be used. If the icon id is null, the label will be created also.
     * 
     * @param subkey the subkey which should be used for resolving the text
     * @param alternateClass the short name of the class which should be used if no string is
     * present with the runtime class name
     * @param iconId id string for the icon
     * @param pos horizontal alignment
     * @return the newly created label
     */
    public JLabel createLabel(String subkey, String alternateClass, String iconId, int pos)
    {
        ImageIcon ii = (iconId != null) ? parent.icons.getImageIcon(iconId) : null;
        String msg = getI18nStringForClass(subkey, alternateClass);
        JLabel label = LabelFactory.create(msg, ii, pos);
        if (label != null) label.setFont(getControlTextFont());
        return (label);

    }

    /**
     * Creates a label via LabelFactory using iconId, pos and method getI18nStringForClass for
     * resolving the text to be used. If the icon id is null, the label will be created also. If
     * isFullLine true a LabelFactory.FullLineLabel will be created instead of a JLabel. The
     * difference between both classes are a different layout handling.
     * 
     * @param subkey the subkey which should be used for resolving the text
     * @param alternateClass the short name of the class which should be used if no string is
     * present with the runtime class name
     * @param iconId id string for the icon
     * @param pos horizontal alignment
     * @param isFullLine determines whether a FullLineLabel or a JLabel should be created
     * @return the newly created label
     */
    public JLabel createLabel(String subkey, String alternateClass, String iconId, int pos,
            boolean isFullLine)
    {
        ImageIcon ii = (iconId != null) ? parent.icons.getImageIcon(iconId) : null;
        String msg = getI18nStringForClass(subkey, alternateClass);
        JLabel label = LabelFactory.create(msg, ii, pos, isFullLine);
        if (label != null) label.setFont(getControlTextFont());
        return (label);

    }

    /**
     * Creates a label via LabelFactory with the given ids and the given horizontal alignment. If
     * the icon id is null, the label will be created also. The strings are the ids for the text in
     * langpack and the icon in icons of the installer frame.
     * 
     * @param textId id string for the text
     * @param iconId id string for the icon
     * @param pos horizontal alignment
     * @return the newly created label
     */
    public JLabel createLabel(String textId, String iconId, int pos)
    {
        return (createLabel(textId, iconId, pos, false));
    }

    /**
     * Creates a label via LabelFactory with the given ids and the given horizontal alignment. If
     * the icon id is null, the label will be created also. The strings are the ids for the text in
     * langpack and the icon in icons of the installer frame. If isFullLine true a
     * LabelFactory.FullLineLabel will be created instead of a JLabel. The difference between both
     * classes are a different layout handling.
     * 
     * @param textId id string for the text
     * @param iconId id string for the icon
     * @param pos horizontal alignment
     * @param isFullLine determines whether a FullLineLabel or a JLabel should be created
     * @return the newly created label
     */
    public JLabel createLabel(String textId, String iconId, int pos, boolean isFullLine)
    {
        ImageIcon ii = (iconId != null) ? parent.icons.getImageIcon(iconId) : null;
        JLabel label = LabelFactory.create(parent.langpack.getString(textId), ii, pos, isFullLine);
        if (label != null) label.setFont(getControlTextFont());
        return (label);

    }

    /**
     * Creates a multi line label with the language dependent text given by the text id. The strings
     * is the id for the text in langpack of the installer frame. The horizontal alignment will be
     * LEFT.
     * 
     * @param textId id string for the text
     * @return the newly created multi line label
     */
    public MultiLineLabel createMultiLineLabelLang(String textId)
    {
        return (createMultiLineLabel(parent.langpack.getString(textId)));
    }

    /**
     * Creates a multi line label with the given text. The horizontal alignment will be LEFT.
     * 
     * @param text text to be used in the label
     * @return the newly created multi line label
     */
    public MultiLineLabel createMultiLineLabel(String text)
    {
        return (createMultiLineLabel(text, null, SwingConstants.LEFT));
    }

    /**
     * Creates a label via LabelFactory with the given text, the given icon id and the given
     * horizontal alignment. If the icon id is null, the label will be created also. The strings are
     * the ids for the text in langpack and the icon in icons of the installer frame.
     * 
     * @param text text to be used in the label
     * @param iconId id string for the icon
     * @param pos horizontal alignment
     * @return the created multi line label
     */
    public MultiLineLabel createMultiLineLabel(String text, String iconId, int pos)
    {
        MultiLineLabel mll = null;
        mll = new MultiLineLabel(text, 0, 0);
        if (mll != null) mll.setFont(getControlTextFont());
        return (mll);
    }

    /**
     * The Font of Labels in many cases
     */
    public Font getControlTextFont()
    {
        return (getLAF() != null ? MetalLookAndFeel.getControlTextFont() : getFont());
    }

    protected static MetalLookAndFeel getLAF()
    {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf instanceof MetalLookAndFeel) return ((MetalLookAndFeel) laf);
        return (null);
    }

    // ------------- Helper for common used components ----- END ---
    // ------------------- Layout stuff -------------------- START ---
    /**
     * Returns the default GridBagConstraints of this panel.
     * 
     * @return the default GridBagConstraints of this panel
     * @deprecated use <code>getLayoutHelper().getDefaulConstraints</code> instead
     */
    public GridBagConstraints getDefaultGridBagConstraints()
    {
        return(GridBagConstraints) ( layoutHelper.getDefaultConstraints());
    }

    /**
     * Sets the default GridBagConstraints of this panel to the given object.
     * 
     * @param constraints which should be set as default for this object
     * @deprecated use <code>getLayoutHelper().setDefaultConstraints</code> instead
     */
    public void setDefaultGridBagConstraints(GridBagConstraints constraints)
    {
        layoutHelper.setDefaultConstraints(constraints);
    }

    /**
     * Resets the grid counters which are used at getNextXGridBagConstraints and
     * getNextYGridBagConstraints.
     * @deprecated use <code>getLayoutHelper().resetGridCounter</code> instead
     */
    public void resetGridCounter()
    {
        layoutHelper.resetGridCounter();
    }

    /**
     * Returns a newly created GridBagConstraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters.
     * 
     * @param gridx value to be used for the new constraint
     * @param gridy value to be used for the new constraint
     * @return newly created GridBagConstraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters
     * @deprecated use <code>getLayoutHelper().getNewConstraints</code> instead
     */
    public GridBagConstraints getNewGridBagConstraints(int gridx, int gridy)
    {
        return(GridBagConstraints) ( layoutHelper.getNewConstraints(gridx, gridy));
    }

    /**
     * Returns a newly created GridBagConstraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters.
     * 
     * @param gridx value to be used for the new constraint
     * @param gridy value to be used for the new constraint
     * @param gridwidth value to be used for the new constraint
     * @param gridheight value to be used for the new constraint
     * @return newly created GridBagConstraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters
     * @deprecated use <code>getLayoutHelper().getNewConstraints</code> instead
     */
    public GridBagConstraints getNewGridBagConstraints(int gridx, int gridy, int gridwidth,
            int gridheight)
    {
        return(GridBagConstraints) (layoutHelper.getNewConstraints(gridx, gridy, gridwidth, gridheight));
    }

    /**
     * Returns a newly created GridBagConstraints for the next column of the current layout row.
     * 
     * @return a newly created GridBagConstraints for the next column of the current layout row
     * 
     * @deprecated use <code>getLayoutHelper().getNextXConstraints</code> instead
     */
    public GridBagConstraints getNextXGridBagConstraints()
    {
        return(GridBagConstraints) (layoutHelper.getNextXConstraints());
    }

    /**
     * Returns a newly created GridBagConstraints with column 0 for the next row.
     * 
     * @return a newly created GridBagConstraints with column 0 for the next row
     * 
     * @deprecated use <code>getLayoutHelper().getNextYConstraints</code> instead
     */
    public GridBagConstraints getNextYGridBagConstraints()
    {
        return(GridBagConstraints) (layoutHelper.getNextYConstraints());
    }

    /**
     * Returns a newly created GridBagConstraints with column 0 for the next row using the given
     * parameters.
     * 
     * @param gridwidth width for this constraint
     * @param gridheight height for this constraint
     * @return a newly created GridBagConstraints with column 0 for the next row using the given
     * parameters
     * @deprecated use <code>getLayoutHelper().getNextYConstraints</code> instead
     */
    public GridBagConstraints getNextYGridBagConstraints(int gridwidth, int gridheight)
    {
        return(GridBagConstraints) (layoutHelper.getNextYConstraints(gridwidth, gridheight));
    }

    /**
     * Start layout determining. If it is needed, a dummy component will be created as first row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "SOUTH" or "SOUTHWEST". The earlier used value "BOTTOM" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     * @deprecated use <code>getLayoutHelper().startLayout</code> instead
     */
    public void startGridBagLayout()
    {
        layoutHelper.startLayout(new GridBagLayout());
    }

    /**
     * Complete layout determining. If it is needed, a dummy component will be created as last row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "NORTH" or "NORTHWEST". The earlier used value "TOP" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     * @deprecated use <code>getLayoutHelper().completeLayout</code> instead
     */
    public void completeGridBagLayout()
    {
        layoutHelper.completeLayout();
    }

    // ------------------- Layout stuff -------------------- END ---

    // ------------------- Summary stuff -------------------- START ---
    /**
     * This method will be called from the SummaryPanel to get the summary of this class which
     * should be placed in the SummaryPanel. The returned text should not contain a caption of this
     * item. The caption will be requested from the method getCaption. If <code>null</code>
     * returns, no summary for this panel will be generated. Default behaviour is to return
     * <code>null</code>.
     * 
     * @return the summary for this class
     */
    public String getSummaryBody()
    {
        return null;
    }

    /**
     * This method will be called from the SummaryPanel to get the caption for this class which
     * should be placed in the SummaryPanel. If <code>null</code> returns, no summary for this
     * panel will be generated. Default behaviour is to return the string given by langpack for the
     * key <code>&lt;current class name>.summaryCaption&gt;</code> if exist, else the string
     * &quot;summaryCaption.&lt;ClassName&gt;&quot;.
     * 
     * @return the caption for this class
     */
    public String getSummaryCaption()
    {
        String caption;
        if (parent.isHeading(this)
                && idata.guiPrefs.modifier.containsKey("useHeadingForSummary")
                && ((String) idata.guiPrefs.modifier.get("useHeadingForSummary"))
                        .equalsIgnoreCase("yes"))
        {
            caption = getI18nStringForClass("headline", this.getClass().getName());
        }
        else
            caption = getI18nStringForClass("summaryCaption", this.getClass().getName());

        return (caption);
    }

    // ------------------- Summary stuff -------------------- END ---

    // ------------------- Inner classes ------------------- START ---
    public static class Filler extends JComponent
    {

        private static final long serialVersionUID = 3258416144414095153L;

    }
    // ------------------- Inner classes ------------------- END ---

    
    /**
     * Returns whether this panel will be hidden general or not.
     * A hidden panel will be not counted  in the step counter and
     * for panel icons.
     * @return whether this panel will be hidden general or not
     */
    public boolean isHidden()
    {
        return hidden;
    }

    
    /**
     * Set whether this panel should be hidden or not.
     * A hidden panel will be not counted  in the step counter and
     * for panel icons.
     * @param hidden flag to be set
     */
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    
    /**
     * Returns the used layout helper. Can be used in a derived class
     * to create custom layout.
     * @return the used layout helper
     */
    public LayoutHelper getLayoutHelper()
    {
        return layoutHelper;
    }

    /**
     * @return the metadata
     */
    public Panel getMetadata() {
      return this.metadata;
    }

    /**
     * @param p the metadata to set
     */
    public void setMetadata(Panel p) {
      this.metadata = p;
    }
}
