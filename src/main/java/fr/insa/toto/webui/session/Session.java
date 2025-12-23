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
package fr.insa.toto.webui.session;

import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.User;
import fr.insa.toto.webui.utils.NotificationError;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public class Session implements Serializable {
    private static User user;
    private static boolean checkedAdmin = false;

    public static void init() {
        if (checkedAdmin) return;

        checkedAdmin = true;

        try (Connection con = ConnectionPool.getConnection()) {
            if (User.tousLesAdmins(con).size() == 0) {
                new User("admin", "admin", true).saveInDB(con);
                NotificationError.log("session/initAdmins: created default admin user");
            } else {
                NotificationError.log("session/initAdmins: no need to create default admin");
            }
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    public static User getUser() {
        return user;
    }

    public static boolean isConnected() {
        return user != null;
    }

    public static boolean isAdmin() {
        if (isConnected())
            return user.isAdmin();
        else
            return false;
    }

    public static boolean isNormal() {
        if (isConnected())
            return ! user.isAdmin();
        else
            return false;
    }

    public static boolean tryConnect(String username, String password) throws SQLException {
        try (Connection con = ConnectionPool.getConnection()) {
            return tryConnect(con, username, password);
        }
    }

    public static boolean tryConnect(Connection con, String username, String password) throws SQLException {
        if (user != null)
            return true;

        var ans = User.findByUsername(con, username);
        if (ans.isPresent()) {
            var unwrapped = ans.get();
            if (unwrapped.getPassword().equals(password)) {
                Session.user = unwrapped;
                NotificationError.show("Connexion à " + user.getUsername() + " réussie");
                return true;
            }
        }

        return false;
    }

    public static void disconnect() {
        NotificationError.show("Déconnexion de " + user.getUsername());
        user = null;
    }
}
