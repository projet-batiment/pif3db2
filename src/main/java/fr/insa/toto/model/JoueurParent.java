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
package fr.insa.toto.model;

import fr.insa.beuvron.utils.database.ClasseMiroir;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author elio
 */
public interface JoueurParent {
    public abstract String parentName();
    public abstract int addJoueur(Joueur joueur, Connection con) throws SQLException, ClasseMiroir.EntiteDejaSauvegardee;
    public abstract void deleteJoueur(Joueur joueur, Connection con) throws SQLException, ClasseMiroir.EntiteNonSauvegardee;
    public abstract List<Joueur> getJoueurs(Connection con) throws SQLException;
}
