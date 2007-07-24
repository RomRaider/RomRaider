/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Marcus Wolschon
 * Copyright 2002 Jan Blok
 * Copyright 2004 Klaus Bartz
 * Copyright 2007 Dennis Reil
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

package com.izforge.izpack.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * The base class for Packs panels. It brings the common member and methods of the different packs
 * panels together. This class handles the common logic of pack selection. The derived class should
 * be create the layout and other specific actions. There are some helper methods to simplify layout
 * creation in the derived class.
 * 
 * @author Julien Ponge
 * @author Klaus Bartz
 * @author Dennis Reil
 */
public abstract class PacksPanelBase extends IzPanel implements PacksPanelInterface,
        ListSelectionListener
{

    // Common used Swing fields
    /**
     * The free space label.
     */
    protected JLabel freeSpaceLabel;

    /**
     * The space label.
     */
    protected JLabel spaceLabel;

    /**
     * The tip label.
     */
    protected JTextArea descriptionArea;

    /**
     * The dependencies label.
     */
    protected JTextArea dependencyArea;

    /**
     * The packs table.
     */
    protected JTable packsTable;

    /**
     * The packs model.
     */
    protected PacksModel packsModel;
    
    /**
     * The tablescroll.
     */
    protected JScrollPane tableScroller;

    // Non-GUI fields
    /**
     * Map that connects names with pack objects
     */
    private Map names;

    /**
     * The bytes of the current pack.
     */
    protected int bytes = 0;

    /**
     * The free bytes of the current selected disk.
     */
    protected long freeBytes = 0;

    /**
     * Are there dependencies in the packs
     */
    protected boolean dependenciesExist = false;

    /**
     * The packs locale database.
     */
    private LocaleDatabase langpack = null;

    /**
     * The name of the XML file that specifies the panel langpack
     */
    private static final String LANG_FILE_NAME = "packsLang.xml";

    /**
     * The constructor.
     * 
     * @param parent The parent window.
     * @param idata The installation data.
     */
    public PacksPanelBase(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        // Load langpack.
        try
        {
            this.langpack = parent.langpack;
            InputStream inputStream = ResourceManager.getInstance().getInputStream(LANG_FILE_NAME);
            this.langpack.add(inputStream);
        }
        catch (Throwable exception)
        {
            Debug.trace(exception);
        }
        // init the map
        computePacks(idata.availablePacks);

        createNormalLayout();
    }

    /**
     * The Implementation of this method should create the layout for the current class.
     */
    abstract protected void createNormalLayout();

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.panels.PacksPanelInterface#getLangpack()
     */
    public LocaleDatabase getLangpack()
    {
        return (langpack);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.panels.PacksPanelInterface#getBytes()
     */
    public int getBytes()
    {
        return (bytes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.panels.PacksPanelInterface#setBytes(int)
     */
    public void setBytes(int bytes)
    {
        this.bytes = bytes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.panels.PacksPanelInterface#showSpaceRequired()
     */
    public void showSpaceRequired()
    {
        if (spaceLabel != null) spaceLabel.setText(Pack.toByteUnitsString(bytes));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.panels.PacksPanelInterface#showFreeSpace()
     */
    public void showFreeSpace()
    {
        if (IoHelper.supported("getFreeSpace") && freeSpaceLabel != null)
        {
            String msg = null;
            freeBytes = IoHelper.getFreeSpace(IoHelper.existingParent(
                    new File(idata.getInstallPath())).getAbsolutePath());
            if (freeBytes < 0)
                msg = parent.langpack.getString("PacksPanel.notAscertainable");
            else
                msg = Pack.toByteUnitsString(freeBytes);
            freeSpaceLabel.setText(msg);
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return true if the needed space is less than the free space, else false
     */
    public boolean isValidated()
    {
        if (IoHelper.supported("getFreeSpace") && freeBytes >= 0 && freeBytes <= bytes)
        {
            JOptionPane.showMessageDialog(this, parent.langpack
                    .getString("PacksPanel.notEnoughSpace"), parent.langpack
                    .getString("installer.error"), JOptionPane.ERROR_MESSAGE);
            return (false);
        }
        return (true);
    }

    /**
     * Asks to make the XML panel data.
     * 
     * @param panelRoot The XML tree to write the data in.
     */
    public void makeXMLData(XMLElement panelRoot)
    {
        new ImgPacksPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e)
    {
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

        int i = packsTable.getSelectedRow();
        if (i < 0) return;
        
        // toggle the value stored in the packsModel
        Integer checked = (Integer)packsModel.getValueAt(i, 0);
        if (checked.intValue() == 0) {
          packsModel.setValueAt(new Integer(1), i, 0);
        } else if (checked.intValue() == 1) {
          packsModel.setValueAt(new Integer(0), i, 0);
        }
        
        // Operations for the description
        if (descriptionArea != null)
        {
            Pack pack = (Pack) idata.availablePacks.get(i);
            String desc = "";
            String key = pack.id + ".description";
            if (langpack != null && pack.id != null && !"".equals(pack.id))
            {
                desc = langpack.getString(key);
            }
            if ("".equals(desc) || key.equals(desc))
            {
                desc = pack.description;
            }
            desc = vs.substitute(desc, null);
            descriptionArea.setText(desc);
        }
        // Operation for the dependency listing
        if (dependencyArea != null)
        {
            Pack pack = (Pack) idata.availablePacks.get(i);
            List dep = pack.dependencies;
            String list = "";
            if (dep != null)
            {
                list += (langpack == null) ? "Dependencies: " : langpack
                        .getString("PacksPanel.dependencies");
            }
            for (int j = 0; dep != null && j < dep.size(); j++)
            {
                String name = (String) dep.get(j);
                list += getI18NPackName((Pack) names.get(name));
                if (j != dep.size() - 1) list += ", ";
            }

            // add the list of the packs to be excluded
            String excludeslist = (langpack == null) ? "Excludes: " : langpack
                    .getString("PacksPanel.excludes");
            int numexcludes = 0;
            if (pack.excludeGroup != null)
            {
                for (int q = 0; q < idata.availablePacks.size(); q++)
                {
                    Pack otherpack = (Pack) idata.availablePacks.get(q);
                    String exgroup = otherpack.excludeGroup;
                    if (exgroup != null)
                    {
                        if (q != i && pack.excludeGroup.equals(exgroup))
                        {

                            excludeslist += getI18NPackName(otherpack) + ", ";
                            numexcludes++;
                        }
                    }
                }
            }
            // concatenate
            if (dep != null) excludeslist = "    " + excludeslist;
            if (numexcludes > 0) list += excludeslist;
            if (list.endsWith(", ")) list = list.substring(0, list.length() - 2);

            // and display the result
            dependencyArea.setText(list);
        }
    }

    /**
     * This method tries to resolve the localized name of the given pack. If this is not possible,
     * the name given in the installation description file in ELEMENT <pack> will be used.
     * 
     * @param pack for which the name should be resolved
     * @return localized name of the pack
     */
    private String getI18NPackName(Pack pack)
    {
        // Internationalization code
        String packName = pack.name;
        String key = pack.id;
        if (langpack != null && pack.id != null && !"".equals(pack.id))
        {
            packName = langpack.getString(key);
        }
        if ("".equals(packName) || key == null || key.equals(packName) )
        {
            packName = pack.name;
        }
        return (packName);
    }

    /**
     * Layout helper method:<br>
     * Creates an label with a message given by msgId and an icon given by the iconId. If layout and
     * constraints are not null, the label will be added to layout with the given constraints. The
     * label will be added to this object.
     * 
     * @param msgId identifier for the IzPack langpack
     * @param iconId identifier for the IzPack icons
     * @param layout layout to be used
     * @param constraints constraints to be used
     * @return the created label
     */
    protected JLabel createLabel(String msgId, String iconId, GridBagLayout layout,
            GridBagConstraints constraints)
    {
        JLabel label = LabelFactory.create(parent.langpack.getString(msgId), parent.icons
                .getImageIcon(iconId), TRAILING);
        if (layout != null && constraints != null) layout.addLayoutComponent(label, constraints);
        add(label);
        return (label);
    }

    /**
     * Creates a panel containing a anonymous label on the left with the message for the given msgId
     * and a label on the right side with initial no text. The right label will be returned. If
     * layout and constraints are not null, the label will be added to layout with the given
     * constraints. The panel will be added to this object.
     * 
     * @param msgId identifier for the IzPack langpack
     * @param layout layout to be used
     * @param constraints constraints to be used
     * @return the created (right) label
     */
    protected JLabel createPanelWithLabel(String msgId, GridBagLayout layout,
            GridBagConstraints constraints)
    {
        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        if (label == null) label = new JLabel("");
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(LabelFactory.create(parent.langpack.getString(msgId)));
        panel.add(Box.createHorizontalGlue());
        panel.add(label);
        if (layout != null && constraints != null) layout.addLayoutComponent(panel, constraints);
        add(panel);
        return (label);
    }

    /**
     * Creates a text area with standard settings and the title given by the msgId. If scroller is
     * not null, the create text area will be added to the scroller and the scroller to this object,
     * else the text area will be added directly to this object. If layout and constraints are not
     * null, the text area or scroller will be added to layout with the given constraints. The text
     * area will be returned.
     * 
     * @param msgId identifier for the IzPack langpack
     * @param scroller the scroller to be used
     * @param layout layout to be used
     * @param constraints constraints to be used
     * @return the created text area
     */
    protected JTextArea createTextArea(String msgId, JScrollPane scroller, GridBagLayout layout,
            GridBagConstraints constraints)
    {
        JTextArea area = new JTextArea();
        // area.setMargin(new Insets(2, 2, 2, 2));
        area.setAlignmentX(LEFT_ALIGNMENT);
        area.setCaretPosition(0);
        area.setEditable(false);
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createTitledBorder(parent.langpack.getString(msgId)));
        area.setFont(getControlTextFont());

        if (layout != null && constraints != null)
        {
            if (scroller != null)
            {
                layout.addLayoutComponent(scroller, constraints);
            }
            else
                layout.addLayoutComponent(area, constraints);
        }
        if (scroller != null)
        {
            scroller.setViewportView(area);
            add(scroller);
        }
        else
            add(area);
        return (area);

    }

    /**
     * Creates the table for the packs. All parameters are required. The table will be returned.
     * 
     * @param width of the table
     * @param scroller the scroller to be used
     * @param layout layout to be used
     * @param constraints constraints to be used
     * @return the created table
     */
    protected JTable createPacksTable(int width, JScrollPane scroller, GridBagLayout layout,
            GridBagConstraints constraints)
    {

        JTable table = new JTable();
        table.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.white);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setShowGrid(false);
        scroller.setViewportView(table);
        scroller.setAlignmentX(LEFT_ALIGNMENT);
        scroller.getViewport().setBackground(Color.white);
        scroller.setPreferredSize(new Dimension(width, (idata.guiPrefs.height / 3 + 30)));

        if (layout != null && constraints != null)
            layout.addLayoutComponent(scroller, constraints);
        add(scroller);
        return (table);
    }

    /**
     * Computes pack related data like the names or the dependencies state.
     * 
     * @param packs
     */
    private void computePacks(List packs)
    {
        names = new HashMap();
        dependenciesExist = false;
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            names.put(pack.name, pack);
            if (pack.dependencies != null || pack.excludeGroup != null) dependenciesExist = true;
        }
    }

    /**
     * Called when the panel becomes active. If a derived class implements this method also, it is
     * recomanded to call this method with the super operator first.
     */
    public void panelActivate()
    {
        try
        {
            // TODO the PacksModel could be patched such that isCellEditable
            // allows returns false. In that case the PacksModel must not be
            // adapted here.
            packsModel = new PacksModel(this, idata, this.parent.getRules()) {
              public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
            };
            packsTable.setModel(packsModel);
            CheckBoxRenderer packSelectedRenderer = new CheckBoxRenderer();
            packsTable.getColumnModel().getColumn(0).setCellRenderer(packSelectedRenderer);
            packsTable.getColumnModel().getColumn(0).setMaxWidth(40);
            
            //packsTable.getColumnModel().getColumn(1).setCellRenderer(renderer1);
            packsTable.getColumnModel().getColumn(1).setCellRenderer(new PacksPanelTableCellRenderer());
            PacksPanelTableCellRenderer renderer2 = new PacksPanelTableCellRenderer();
            renderer2.setHorizontalAlignment(RIGHT);
            packsTable.getColumnModel().getColumn(2).setCellRenderer(renderer2);
            packsTable.getColumnModel().getColumn(2).setMaxWidth(100);

            // remove header,so we don't need more strings
            tableScroller.remove(packsTable.getTableHeader());
            tableScroller.setColumnHeaderView(null);
            tableScroller.setColumnHeader(null);

            // set the JCheckBoxes to the currently selected panels. The
            // selection might have changed in another panel
            java.util.Iterator iter = idata.availablePacks.iterator();
            bytes = 0;
            while (iter.hasNext())
            {
                Pack p = (Pack) iter.next();
                if (p.required)
                {
                    bytes += p.nbytes;
                    continue;
                }
                if (idata.selectedPacks.contains(p)) bytes += p.nbytes;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        showSpaceRequired();
        showFreeSpace();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
     */
    public String getSummaryBody()
    {
        StringBuffer retval = new StringBuffer(256);
        Iterator iter = idata.selectedPacks.iterator();
        boolean first = true;
        while (iter.hasNext())
        {
            if (!first)
            {
                retval.append("<br>");
            }
            first = false;
            Pack pack = (Pack) iter.next();
            if (langpack != null && pack.id != null && !"".equals(pack.id))
            {
                retval.append(langpack.getString(pack.id));
            }
            else
                retval.append(pack.name);
        }
        return (retval.toString());
    }

    static class CheckBoxRenderer implements TableCellRenderer
    {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column)
        {
            JCheckBox display = new JCheckBox();
            if(com.izforge.izpack.util.OsVersion.IS_UNIX)
            {
                display.setIcon(new LFIndependentIcon());
                display.setDisabledIcon(new LFIndependentIcon());
                display.setSelectedIcon(new LFIndependentIcon());
                display.setDisabledSelectedIcon(new LFIndependentIcon());
            }
            display.setHorizontalAlignment(CENTER);
            
            if (isSelected)
            {
                display.setForeground(table.getSelectionForeground());
                display.setBackground(table.getSelectionBackground());
            }
            else
            {
                display.setForeground(table.getForeground());
                display.setBackground(table.getBackground());
            }
            int state = ((Integer) value).intValue();
            if (state == -2)
            {
                // condition not fulfilled
                display.setForeground(Color.GRAY);                
            }
            display.setSelected((value != null && Math.abs(state) == 1));
            display.setEnabled(state >= 0);
            return display;
        }
    }
    
    public static class LFIndependentIcon implements Icon
    {
       ButtonModel buttonModel = null;
       protected int getControlSize() { return 13; }
       public void paintIcon(Component c, Graphics g, int x, int y)
       {
          ButtonModel model = ((JCheckBox)c).getModel();
          buttonModel = model;
          int controlSize = getControlSize();
          if (model.isPressed() && model.isArmed())
          {
             g.setColor( MetalLookAndFeel.getControlShadow() );
             if(model.isEnabled()) g.setColor(Color.green); else g.setColor(Color.gray);
             g.fillRect( x, y, controlSize-1, controlSize-1);
             drawPressedBorder(g, x, y, controlSize, controlSize, model);
          }
          else
          {
             drawBorder(g, x, y, controlSize, controlSize, model);
          }
          g.setColor( Color.green );
          if (model.isSelected())
          {
             drawCheck(c,g,x,y);
          }
       }
       private void drawBorder(Graphics g, int x, int y, int w, int h, ButtonModel model)
       {
          g.translate(x, y);

          // outer frame rectangle
          g.setColor(MetalLookAndFeel.getControlDarkShadow());
          if(!model.isEnabled()) g.setColor(new Color(0.4f, 0.4f, 0.4f));
          g.drawRect(0, 0, w-2, h-2);

          // middle frame
          g.setColor(MetalLookAndFeel.getControlHighlight());
          if(!model.isEnabled()) g.setColor(new Color(0.6f, 0.6f, 0.6f));
          g.drawRect(1, 1, w-2, h-2);

          // background
          if(model.isEnabled()) g.setColor(Color.white); else g.setColor(new Color(0.8f, 0.8f, 0.8f));
          g.fillRect(2, 2, w-3, h-3);

          //some extra lines for FX
          g.setColor(MetalLookAndFeel.getControl());
          g.drawLine(0, h-1, 1, h-2);
          g.drawLine(w-1, 0, w-2, 1);
          g.translate(-x, -y);
       }
       private void drawPressedBorder(Graphics g, int x, int y, int w, int h, ButtonModel model)
       {
          g.translate(x, y);
          drawBorder(g, 0, 0, w, h, model);
          g.setColor(MetalLookAndFeel.getControlShadow());
          g.drawLine(1, 1, 1, h-2);
          g.drawLine(1, 1, w-2, 1);
          g.drawLine(2, 2, 2, h-3);
          g.drawLine(2, 2, w-3, 2);
          g.translate(-x, -y);
       }
       protected void drawCheck(Component c, Graphics g, int x, int y)
       {
          int controlSize = getControlSize();
          if(buttonModel!=null)
             if(buttonModel.isEnabled())
                g.setColor(new Color(0.0f,0.6f,0.0f));
             else g.setColor(new Color(0.1f,0.1f,0.1f));

          g.drawLine( x+(controlSize-4), y+2, x+(controlSize-4)-4, y+2+4 );
          g.drawLine( x+(controlSize-4), y+3, x+(controlSize-4)-4, y+3+4 );
          g.drawLine( x+(controlSize-4), y+4, x+(controlSize-4)-4, y+4+4 );

          g.drawLine( x+(controlSize-4)-4, y+2+4, x+(controlSize-4)-4-2, y+2+4-2 );
          g.drawLine( x+(controlSize-4)-4, y+3+4, x+(controlSize-4)-4-2, y+3+4-2 );
          g.drawLine( x+(controlSize-4)-4, y+4+4, x+(controlSize-4)-4-2, y+4+4-2 );
       }
       public int getIconWidth() {return getControlSize();}
       public int getIconHeight() {return getControlSize();}
    }
    
    static class PacksPanelTableCellRenderer extends DefaultTableCellRenderer
    {

        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,row, column);
            
            int state = ((Integer) table.getModel().getValueAt(row, 0)).intValue();
            if (state == -2)
            {
                // condition not fulfilled
                renderer.setForeground(Color.GRAY);
                if (isSelected){
                    renderer.setBackground(table.getSelectionBackground());
                }
                else {
                    renderer.setBackground(table.getBackground());
                }
            }
            else {
                if (isSelected){
                    renderer.setForeground(table.getSelectionForeground());
                    renderer.setBackground(table.getSelectionBackground());
                }
                else {
                    renderer.setForeground(table.getForeground());
                    renderer.setBackground(table.getBackground());
                }
            }
            
            return renderer;
        }

    }

}
