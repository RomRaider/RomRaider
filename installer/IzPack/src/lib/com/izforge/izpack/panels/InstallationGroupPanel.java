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
package com.izforge.izpack.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.OsConstraint;

import java.util.ArrayList;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;


/**
 * A panel which displays the available installGroups found on the packs to
 * allow the user to select a subset of the packs based on the pack
 * installGroups attribute. This panel will be skipped if there are no
 * pack elements with an installGroups attribute.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class InstallationGroupPanel extends IzPanel
    implements ListSelectionListener
{
    private static final long serialVersionUID = 1L;

    /** HashMap<String, Pack> of the InstallData.availablePacks */
    private HashMap packsByName;
    private TableModel groupTableModel;
    private JTextPane descriptionField;
    private JScrollPane groupScrollPane;
    private JTable groupsTable;
    private GroupData[] rows;
    private int selectedGroup = -1;

    public InstallationGroupPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        buildLayout();
    }

    /**
     * If there are no packs with an installGroups attribute, this panel is
     * skipped. Otherwise, the unique installGroups are displayed in a table.
     */
    public void panelActivate()
    {
        // Set/restore availablePacks from allPacks; consider OS constraints
        idata.availablePacks = new ArrayList();
        for (Iterator i = idata.allPacks.iterator(); i.hasNext(); ) {
          Pack p = (Pack)i.next();
          if (OsConstraint.oneMatchesCurrentSystem(p.osConstraints)) idata.availablePacks.add(p);
        }

        Debug.trace("InstallationGroupPanel.panelActivate, selectedGroup="+selectedGroup);
        // If there are no groups, skip this panel
        HashMap installGroups = getInstallGroups(idata);
        if (installGroups.size() == 0)
        {
            super.askQuestion("Skip InstallGroup selection",
                "Skip InstallGroup selection", AbstractUIHandler.CHOICES_YES_NO);
            parent.skipPanel();
            return;
        }

        // Build the table model from the unique groups
        groupTableModel = getModel(installGroups);
        groupsTable.setModel(groupTableModel);
        TableColumnModel tcm = groupsTable.getColumnModel();

        // renders the radio buttons and adjusts their state
        TableCellRenderer radioButtonRenderer = new TableCellRenderer() {
          public Component getTableCellRendererComponent(JTable table, Object value,
                  boolean isSelected, boolean hasFocus,
                  int row, int column) {
            if (value==null) return null;
            
            int selectedRow = table.getSelectedRow();
            
            if (selectedRow != -1) {
              JRadioButton selectedButton = (JRadioButton)table.getValueAt(selectedRow, 0);
              if (!selectedButton.isSelected()) {
                selectedButton.doClick();
              }
            }

            JRadioButton button = (JRadioButton) value;
            button.setForeground(isSelected ?
              table.getSelectionForeground() : table.getForeground());
            button.setBackground(isSelected ?
              table.getSelectionBackground() : table.getBackground());
            
            // long millis = System.currentTimeMillis() % 100000;
            // System.out.printf("%1$5d: row: %2$d; isSelected: %3$5b; buttonSelected: %4$5b; selectedRow: %5$d%n", millis, row, isSelected, button.isSelected(), selectedRow);
            
            return button;
          }
        };
        tcm.getColumn(0).setCellRenderer(radioButtonRenderer);
        
        //groupsTable.setColumnSelectionAllowed(false);
        //groupsTable.setRowSelectionAllowed(true);
        groupsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupsTable.getSelectionModel().addListSelectionListener (this);
        groupsTable.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        groupsTable.setIntercellSpacing(new Dimension(0, 0));
        groupsTable.setShowGrid(false);
        if( selectedGroup >= 0 )
        {
            groupsTable.getSelectionModel().setSelectionInterval(selectedGroup, selectedGroup);
            descriptionField.setText(rows[selectedGroup].description);
        }
        else
        {
            descriptionField.setText(rows[0].description);
        }
    }

    /**
     * Remove all packs from the InstallData availablePacks and selectedPacks
     * that do not list the selected installation group. Packs without any
     * installGroups are always included.
     */
    public void panelDeactivate()
    {

        Debug.trace("InstallationGroupPanel.panelDeactivate, selectedGroup="+selectedGroup);
        if( selectedGroup >= 0 )
        {
            removeUnusedPacks();
            GroupData group = this.rows[selectedGroup];
            idata.setVariable("INSTALL_GROUP", group.name);
            Debug.trace("Added variable INSTALL_GROUP="+group.name);
        }
    }

    /**
     * There needs to be a valid selectedGroup to go to the next panel
     * @return true if selectedGroup >= 0, false otherwise
     */
    public boolean isValidated()
    {
        Debug.trace("InstallationGroupPanel.isValidated, selectedGroup="+selectedGroup);
        return selectedGroup >= 0;
    }

    /**
     * Update the current selected install group index.
     * @param e
     */
    public void valueChanged(ListSelectionEvent e)
    {
        Debug.trace("valueChanged: " + e);
        if (e.getValueIsAdjusting() == false)
        {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if( lsm.isSelectionEmpty()  )
            {
                descriptionField.setText("");
            }
            else
            {
                selectedGroup = lsm.getMinSelectionIndex();
                if( selectedGroup >= 0 )
                {
                    GroupData data = rows[selectedGroup];
                    descriptionField.setText(data.description);
                    ((JRadioButton)groupTableModel.getValueAt(selectedGroup, 0)).setSelected(true);
                }
                Debug.trace("selectedGroup set to: "+selectedGroup);
            }
        }
    }

    /* Add the installation group to pack mappings
	 * @see com.izforge.izpack.installer.IzPanel#makeXMLData(net.n3.nanoxml.XMLElement)
	 */
	public void makeXMLData(XMLElement panelRoot)
	{
		InstallationGroupPanelAutomationHelper helper = new InstallationGroupPanelAutomationHelper();
		idata.setAttribute("GroupData", rows);
		idata.setAttribute("packsByName", packsByName);
		helper.makeXMLData(idata, panelRoot);
	}

	/**
     * Create the panel ui.
     */
    protected void buildLayout()
    {
        GridBagConstraints gridBagConstraints;

        descriptionField = new JTextPane();
        groupScrollPane = new JScrollPane();
        groupsTable = new JTable();

        setLayout(new GridBagLayout());

        descriptionField.setMargin(new Insets(2, 2, 2, 2));
        descriptionField.setAlignmentX(LEFT_ALIGNMENT);
        descriptionField.setCaretPosition(0);
        descriptionField.setEditable(false);
        descriptionField.setOpaque(false);
        descriptionField.setText("<b>Install group description text</b>");
        descriptionField.setContentType("text/html");
        descriptionField.setBorder(new TitledBorder(idata.langpack.getString("PacksPanel.description")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        add(descriptionField, gridBagConstraints);

        groupScrollPane.setBorder(new EmptyBorder(1, 1, 1, 1));
        groupScrollPane.setViewportView(groupsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(groupScrollPane, gridBagConstraints);
    }

    protected void removeUnusedPacks()
    {
        GroupData data = rows[selectedGroup];
        Debug.trace("InstallationGroupPanel.removeUnusedPacks, GroupData="+data.name);

        // Now remove the packs not in groupPackNames
        Iterator iter = idata.availablePacks.iterator();
        while( iter.hasNext() )
        {
            Pack p = (Pack) iter.next();

            //reverse dependencies must be reset in case the user is going
            //back and forth between the group selection panel and the packs selection panel
            p.revDependencies = null;

            if( data.packNames.contains(p.name) == false )
            {
                iter.remove();
                Debug.trace("Removed AvailablePack: "+p.name);
            }
        }

        idata.selectedPacks.clear();
        if (!"no".equals(idata.getVariable("InstallationGroupPanel.selectPacks"))) {
            idata.selectedPacks.addAll(idata.availablePacks);
        } else {
            for (Iterator i = idata.availablePacks.iterator(); i.hasNext(); ) {
              Pack p = (Pack)i.next();
              if (p.preselected) idata.selectedPacks.add(p);
            }
        }
    }
    protected void addDependents(Pack p, HashMap packsByName, GroupData data)
    {
        data.packNames.add(p.name);
        data.size += p.nbytes;
        Debug.trace("addDependents, added pack: "+p.name);
        if( p.dependencies == null || p.dependencies.size() == 0 )
            return;

        Iterator iter = p.dependencies.iterator();
        Debug.trace(p.name+" dependencies: "+p.dependencies);
        while( iter.hasNext() )
        {
            String dependent = (String) iter.next();
            if( data.packNames.contains(dependent) == false )
            {
                Debug.trace("Need dependent: "+dependent);
                Pack dependentPack = (Pack) packsByName.get(dependent);
                addDependents(dependentPack, packsByName, data);
            }
        }
    }

    /**
     * Build the set of unique installGroups data. The GroupData description
     * is taken from the InstallationGroupPanel.description.[name] property
     * where [name] is the installGroup name. The GroupData size is built
     * from the Pack.nbytes sum.
     * 
     * @param idata - the panel install data
     * @return HashMap<String, GroupData> of unique install group names
     */
    protected HashMap getInstallGroups(InstallData idata)
    {
        /* First create a packsByName<String, Pack> of all packs and identify
        the unique install group names.
        */
        packsByName = new HashMap();
        HashMap installGroups = new HashMap();
        for (int n = 0; n < idata.availablePacks.size(); n++)
        {
            Pack p = (Pack) idata.availablePacks.get(n);
            packsByName.put(p.name, p);
            Set groups = p.installGroups;
            Iterator iter = groups.iterator();
            Debug.trace("Pack: "+p.name+", installGroups: "+groups);
            while (iter.hasNext())
            {
                String group = (String) iter.next();
                GroupData data = (GroupData) installGroups.get(group);
                if (data == null)
                {
                    String description = getGroupDescription(group);
                    data = new GroupData(group, description);
                    installGroups.put(group, data);
                }
            }
        }
        Debug.trace("Found installGroups: " + installGroups.keySet());

        /* Build up a set of the packs to include in the installation by finding
        all packs in the selected group, and then include their dependencies.
        */
        Iterator gditer = installGroups.values().iterator();
        while( gditer.hasNext() )
        {
            GroupData data = (GroupData) gditer.next();
            Debug.trace("Adding dependents for: "+data.name);
            Iterator iter = idata.availablePacks.iterator();
            while( iter.hasNext() )
            {
                Pack p = (Pack) iter.next();
                Set groups = p.installGroups;
                if( groups.size() == 0 || groups.contains(data.name) == true )
                {
                    // The pack may have already been added while traversing dependencies
                    if( data.packNames.contains(p.name) == false )
                        addDependents(p, packsByName, data);
                }
            }
            Debug.trace("Completed dependents for: "+data);
            if( Debug.tracing() )
                Debug.trace(data);
        }

        return installGroups;
    }

    /**
     * Look for a key = InstallationGroupPanel.description.[group] entry:
     * first using idata.langpack.getString(key+".html")
     * next using idata.langpack.getString(key)
     * next using idata.getVariable(key)
     * lastly, defaulting to group + " installation"
     * @param group - the installation group name
     * @return the group description
     */
    protected String getGroupDescription(String group)
    {
        String description = null;
        String key = "InstallationGroupPanel.description." + group;
        if( idata.langpack != null )
        {
            String htmlKey = key+".html";
            String html = idata.langpack.getString(htmlKey);
            // This will equal the key if there is no entry
            if( htmlKey.equalsIgnoreCase(html) )
                description = idata.langpack.getString(key);
            else
                description = html;
        }
        if (description == null || key.equalsIgnoreCase(description))
            description = idata.getVariable(key);
        if (description == null)
            description = group + " installation";
        try
        {
            description = URLDecoder.decode(description, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            emitWarning("Failed to convert description", e.getMessage());
        }

        return description;
    }

    protected TableModel getModel(HashMap groupData)
    {
        String c1 = parent.langpack.getString("InstallationGroupPanel.colNameSelected");
        String c2 = parent.langpack.getString("InstallationGroupPanel.colNameInstallType");
        String c3 = parent.langpack.getString("InstallationGroupPanel.colNameSize");
        String[] columns = {c1, c2, c3};
         DefaultTableModel model = new DefaultTableModel (columns, 0)
         {
            public boolean isCellEditable (int row, int column)
            {
               return false;
            }
        };
        rows = new GroupData[groupData.size()];
        // The name of the group to select if there is no current selection
        String defaultGroup = idata.getVariable("InstallationGroupPanel.defaultGroup");
         Debug.trace("InstallationGroupPanel.defaultGroup="+defaultGroup+", selectedGroup="+selectedGroup);
         List values = new ArrayList(groupData.values());
         Collections.sort(values, new Comparator()
         {
           public int compare(Object o1, Object o2)
           {
               GroupData g1 = (GroupData) o1;
               GroupData g2 = (GroupData) o2;

               if (g1.name == null || g2.name==null)
               {
                   return 0;
               }

               return g1.name.compareTo(g2.name);
           }
        });

        Iterator iter = values.iterator();
        ButtonGroup buttonGroup = new ButtonGroup();
        boolean madeSelection = false;
        int count = 0;
        while (iter.hasNext())
        {
            GroupData gd = (GroupData) iter.next();
            rows[count] = gd;
            Debug.trace("Creating button#"+count+", group="+gd.name);
            JRadioButton btn = new JRadioButton(gd.name);
            if( selectedGroup == count )
            {
                btn.setSelected(true);
                Debug.trace("Selected button#"+count);
            }
            else if ( selectedGroup < 0 && madeSelection == false )
            {
                if( defaultGroup != null )
                {
                   if( defaultGroup.equals(gd.name) )
                     madeSelection = true;
                }
                else if( count == 0 )
                    madeSelection = true;
                if( madeSelection )
                {
                    btn.setSelected(true);
                    Debug.trace("Selected button#"+count);
                    selectedGroup = count;
                }
            }
            else
            {
                btn.setSelected(false);
            }
            buttonGroup.add(btn);
            String sizeText = gd.getSizeString();
            Object[] data = { btn, gd.description, sizeText};
            model.addRow(data);
            count ++;
        }
        return model;
    }

    protected static class GroupData
    {
        static final long ONEK = 1024;
        static final long ONEM = 1024 * 1024;
        static final long ONEG = 1024 * 1024 * 1024;

        String name;
        String description;
        long size;
        HashSet packNames = new HashSet();

        GroupData(String name, String description)
        {
            this.name = name;
            this.description = description;
        }

        String getSizeString()
        {
            String s;
            if (size < ONEK)
            {
                s = size + " bytes";
            }
            else if (size < ONEM)
            {
                s = size / ONEK + " KBytes";
            }
            else if (size < ONEG)
            {
                s = size / ONEM + " MBytes";
            }
            else
            {
                s = size / ONEG + " GBytes";
            }
            return s;
        }
        public String toString()
        {
            StringBuffer tmp = new StringBuffer("GroupData(");
            tmp.append(name);
            tmp.append("){description=");
            tmp.append(description);
            tmp.append(", size=");
            tmp.append(size);
            tmp.append(", sizeString=");
            tmp.append(getSizeString());
            tmp.append(", packNames=");
            tmp.append(packNames);
            tmp.append("}");
            return tmp.toString();
        }
    }

}
