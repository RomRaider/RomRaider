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

package enginuity.util;

import enginuity.util.exception.NameableNotFoundException;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class NamedSet<E> implements Set<E>, Serializable {
    
    Vector<Nameable> objects = new Vector<Nameable>();
    
    public void add(Nameable n) {        
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equalsIgnoreCase(n.getName())) {
                objects.remove(i);
                objects.add(i, n);
                return;
            }
        }
        objects.add(n);
    }
    
    public Nameable get(int i) {
        return objects.get(i);
    }
    
    public Nameable get(String name) throws NameableNotFoundException {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equalsIgnoreCase(name)) {
                return objects.get(i);
            }
        }    
        // Name not found, throw exception
        throw new NameableNotFoundException(name);
    }
    
    public Nameable get(Nameable n) throws NameableNotFoundException {
        return get(n.getName());
    }
    
    public int size() {
        return objects.size();
    }
    
    public void remove(Nameable n) throws NameableNotFoundException { 
        remove(n.getName());
    }
    
    public void remove(String name) throws NameableNotFoundException {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equalsIgnoreCase(name)) {
                objects.remove(i);
                return;
            }
        }            
        // Name not found, throw exception
        throw new NameableNotFoundException(name);
    }    

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public boolean contains(Object o) {
        return objects.contains(o);
    }

    public Iterator<E> iterator() {
        return (Iterator<E>)objects.iterator();
    }

    public Object[] toArray() {
        return objects.toArray();
    }

    public boolean add(E o) {
        add((Nameable) o);
        return true;
    }

    public boolean remove(Object o) {
        return objects.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return objects.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        Iterator it = c.iterator();
        while (it.hasNext()) {
            add((E)it.next());
        }
        return true;
    }

    public boolean retainAll(Collection<?> c) {
        return objects.retainAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return objects.removeAll(c);
    }

    public void clear() {
        objects.clear();
    }

    public <T> T[] toArray(T[] a) {
        return null;
    }
    
    public String toString() {
        StringBuffer output = new StringBuffer();
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            output.append(it.next().toString() + "\n");
        }
        return output+"";
    }
    
    public void move(int src, int dest) {
        Nameable obj = (Nameable)objects.get(src);
        objects.remove(obj);
        objects.insertElementAt(obj, dest);
    }    
    
    public void moveBefore(Nameable moving, Nameable anchor) {
        move(objects.indexOf(moving), objects.indexOf(anchor) - 1);        
    }
    
    public int indexOf(Nameable obj) {
        return objects.indexOf(obj);
    }
}