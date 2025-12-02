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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Intéret d'une classe par rapport à une interface Parent :
 *  une classe d'objets peut être parente de plusieurs autres,
 *  et cela ne passe pas par l'implémentation directe
 *  mais par une classe intermédiaire
 * (voir les implémentations de la classe Parent<T>)
 * 
 * @author elio
 */
public abstract class ParentFace<T> {
    public abstract String parentObjectName();
    public abstract String parentTypeName();

    public final ChildFace child;

    protected abstract String le();
    protected abstract String du();

    public final String leParent() {
        return this.le() + this.parentTypeName();
    }
    public final String leParentName() {
        return this.leParent() + " " + this.parentObjectName();
    }

    public final String duParent() {
        return this.du() + this.parentTypeName();
    }
    public final String duParentName() {
        return this.duParent() + " " + this.parentObjectName();
    }

    public final String leChildDuParent() {
        return this.child.leChild() + " " + this.du() + this.parentTypeName();
    }
    public final String leChildDuParentName() {
        return this.leChildDuParent() + " " + this.parentObjectName();
    }

    public final String lesChildrenDuParent() {
        return this.child.lesChild() + " " + this.du() + this.parentTypeName();
    }
    public final String lesChildrenDuParentName() {
        return this.lesChildrenDuParent() + " " + this.parentObjectName();
    }

    public abstract int add(T child, Connection con) throws SQLException, ClasseMiroir.EntiteDejaSauvegardee;
    public abstract void remove(T child, Connection con) throws SQLException, ClasseMiroir.EntiteNonSauvegardee;
    public abstract List<T> get(Connection con) throws SQLException;

    public ParentFace(ChildFace child) {
        this.child = child;
    }
}