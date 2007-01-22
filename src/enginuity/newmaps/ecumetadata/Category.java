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

package enginuity.newmaps.ecumetadata;

import enginuity.util.Nameable;
import enginuity.util.NamedSet;

import java.io.Serializable;
import java.util.Iterator;

public class Category extends NamedSet implements Nameable, Serializable {
    
    private String name;
    private String description;
    private NamedSet<TableMetadata> tables = new NamedSet<TableMetadata>();
    
    private Category() { }
    
    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append(" --- CATEGORY: " + name + " ---\n");
            
        Iterator it = iterator();        
        while (it.hasNext()) {
            output.append("  " + it.next().toString() + "\n");
        }   
        
        it = tables.iterator();
        while (it.hasNext()) {
            output.append(it.next().toString() + "\n");
        }                            

        return output + " --- END CATEGORY: " + name + " ---\n";
    }

    public void addTable(TableMetadata table) {
        tables.add(table);
    }
    
    public void removeTable(TableMetadata table) {
        try {
            tables.remove(table);
        } catch (Exception ex) {
            
        } finally {
            Iterator it = iterator();
            while (it.hasNext()) {
                ((Category)it.next()).removeTable(table);
            }
        }
    }
    
}