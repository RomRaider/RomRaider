/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

/**
 *
 */
package enginuity.newmaps.swing;

import java.util.Random;
import javax.swing.DefaultListSelectionModel;

/**
 * @author Stefan Taranu
 * @date 19.01.2007
 */
public class TableSelectionModel extends DefaultListSelectionModel
{



	 public static final int MULTIPLE_SELECTION = 3;

	 private int selectionMode = MULTIPLE_INTERVAL_SELECTION;

		/* (non-Javadoc)
		 * @see javax.swing.ListSelectionModel#setSelectionMode(int)
		 */
		public void setSelectionMode( int selectionMode )
		{
	    	switch (selectionMode) {
	    	case SINGLE_SELECTION:
	    	case SINGLE_INTERVAL_SELECTION:
	    	case MULTIPLE_INTERVAL_SELECTION:
	    	case MULTIPLE_SELECTION:
	    	    this.selectionMode = selectionMode;
	    	    break;
	    	default:
	    	    throw new IllegalArgumentException("invalid selectionMode");
	    	}
        }

}
