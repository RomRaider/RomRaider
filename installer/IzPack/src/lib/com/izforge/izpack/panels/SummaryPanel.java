/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2005 Klaus Bartz
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

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.SummaryProcessor;

/**
 * Summary panel to use before InstallPanel. This panel calls the {@link SummaryProcessor} which
 * calls all declared panels for a summary and shows the given captiond and messaged in a
 * <code>JEditorPane</code>.
 * 
 * @author Klaus Bartz
 * 
 */
public class SummaryPanel extends IzPanel
{

    /**
     * 
     */
    private static final long serialVersionUID = 3832626166401282361L;

    /** The text area. */
    private JEditorPane textArea;

    /**
     * The constructor.
     * 
     * @param parent The parent.
     * @param idata The installation data.
     */
    public SummaryPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, new IzPanelLayout());
        add(createMultiLineLabelLang("SummaryPanel.info"));
        try
        {
            textArea = new JEditorPane();
            textArea.setContentType("text/html");
            textArea.setEditable(false);
            JScrollPane scroller = new JScrollPane(textArea);
            add(scroller, NEXT_LINE);
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
        getLayoutHelper().completeLayout();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.installer.IzPanel#panelActivate()
     */
    public void panelActivate()
    {
        super.panelActivate();
        textArea.setText(SummaryProcessor.getSummary(idata));
        textArea.setCaretPosition(0);
    }

}
