/*
Copyright 2000- Francois de Bertrand de Beuvron

This file is part of CoursBeuvron.

CoursBeuvron is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

CoursBeuvron is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with CoursBeuvron.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.insa.toto.model.utils;

import fr.insa.beuvron.utils.database.ClasseMiroir;

/**
 *
 * @author elio
 */
public abstract class ChildFace {
    public abstract String typeName();
    public String typeNamePlural() {
        return this.typeName() + "s";
    }

    protected abstract String leChildPrefix();
    protected abstract String duChildPrefix();

    public final String leChild() {
        return this.leChildPrefix() + this.typeName();
    }
    public final String leChild(String s) {
        return this.leChild() + " " + s;
    }

    public final String lesChild() {
        return "les " + this.typeNamePlural();
    }

    public final String duChild() {
        return this.duChildPrefix() + this.typeName();
    }
    public final String duChild(String s) {
        return this.duChild() + " " + s;
    }
}
