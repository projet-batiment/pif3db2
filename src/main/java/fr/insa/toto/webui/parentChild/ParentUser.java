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
package fr.insa.toto.webui.parentChild;

import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.model.User;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.user.UserEditor;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public abstract class ParentUser extends ParentChild<User> {
    public ParentUser() {
        super(new UserEditor());

        super.addColumn(m -> m.getName()).setHeader("Nom d'utilisateur");
        super.addColumn(m -> {
            try (Connection con = ConnectionPool.getConnection()) {
                var joueur = Joueur.findByIdUser(con, m.getId());
                if (joueur.isPresent()) {
                    return joueur.get().getName();
                } else {
                    return "(Aucun)";
                }
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }

            return "";
        }).setHeader("Joueur associé");
        super.addColumn(m -> m.isAdmin() ? "Administrateur" : "");
    }
}
