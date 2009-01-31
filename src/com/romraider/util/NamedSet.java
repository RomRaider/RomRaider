/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

package com.romraider.util;

import com.romraider.util.exception.NameableNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NamedSet<T extends Nameable> implements Set<T>, Serializable {
    private static final long serialVersionUID = 3700068171618250762L;
    private final List<T> objects = new ArrayList<T>();

    public boolean add(T n) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equalsIgnoreCase(n.getName())) {
                objects.remove(i);
                objects.add(i, n);
                return false;
            }
        }
        return objects.add(n);
    }

    public Nameable get(int i) {
        return objects.get(i);
    }

    public Nameable get(String name) throws NameableNotFoundException {
        for (Nameable object : objects) {
            if (object.getName().equalsIgnoreCase(name)) return object;
        }
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
        throw new NameableNotFoundException(name);
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public boolean contains(Object o) {
        return objects.contains(o);
    }

    public Iterator<T> iterator() {
        return objects.iterator();
    }

    public Object[] toArray() {
        return objects.toArray();
    }

    public boolean remove(Object o) {
        return objects.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return objects.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c) {
        return objects.addAll(c);
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
        for (Nameable object : objects) output.append(object).append("\n");
        return output.toString();
    }

    public void move(int src, int dest) {
        T t = objects.get(src);
        objects.remove(t);
        objects.add(dest, t);
    }

    public void moveBefore(T moving, T anchor) {
        move(objects.indexOf(moving), objects.indexOf(anchor) - 1);
    }

    public int indexOf(T obj) {
        return objects.indexOf(obj);
    }
}