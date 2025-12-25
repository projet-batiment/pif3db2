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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.User;
import fr.insa.toto.webui.utils.NotificationError;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author elio
 */
public class Session implements Serializable {
    private User user;
    private boolean checkedFirstSession;
    private List<Integer> ids;
    private List<String> errorMessage;

    private class LocalSession {
        static User user = null;
        static boolean checkedAdmin = false;
        static List<Integer> ids = new ArrayList<>();
        static List<String> errorMessage = new ArrayList<>();
    }

    private static Session getSession() {
        Session session = new Session();

        session.user = LocalSession.user;
        session.checkedFirstSession = LocalSession.checkedAdmin;
        session.ids = LocalSession.ids;
        session.errorMessage = LocalSession.errorMessage;

        return session;
    }

    private void saveSession() {
        LocalSession.user = this.user;
        LocalSession.checkedAdmin = this.checkedFirstSession;
        LocalSession.ids = this.ids;
        LocalSession.errorMessage = this.errorMessage;
    }

    public static void init() {
        Session session = getSession();

        if (session.checkedFirstSession) return;

        session.checkedFirstSession = true;
        session.saveSession();

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

    public static List<Integer> getIds() {
        return getSession().ids;
    }

    public static Integer getId(int which) {
        try {
            return getSession().ids.get(which);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public static void setIds(Integer ...ids) {
        var session = getSession();
        session.ids = List.of(ids);
        session.saveSession();
    }

    public static void pushId(Integer id) {
        var session = getSession();
        session.ids.add(id);
        session.saveSession();
    }

    public static void popId() {
        var session = getSession();
        session.ids.removeLast();
        session.saveSession();
    }

    public static User getUser() {
        Session session = getSession();
        return session.user;
    }

    public static List<String> getErrorMessages() {
        return getSession().errorMessage;
    }

    public static void clearErrorMessages() {
        var session = getSession();
        session.errorMessage.clear();
        session.saveSession();
    }

    public static void addErrorMessage(String errorMessage) {
        var session = getSession();
        session.errorMessage.add(errorMessage);
        session.saveSession();
    }

    public static boolean isConnected() {
        Session session = getSession();
        return session.user != null;
    }

    public static boolean isAdmin() {
        Session session = getSession();
        if (isConnected())
            return session.user.isAdmin();
        else
            return false;
    }

    public static boolean isNormal() {
        Session session = getSession();
        if (isConnected())
            return ! session.user.isAdmin();
        else
            return false;
    }

    public static boolean ensureAdmin(BeforeEnterEvent event) {
        if (! Session.isAdmin()) {
            event.forwardTo("access-denied");
            return false;
        }
        return true;
    }

    public static boolean ensureConnected(BeforeEnterEvent event) {
        if (! Session.isConnected()) {
            event.forwardTo("access-denied");
            return false;
        }
        return true;
    }

    public static boolean tryConnect(String username, String password) throws SQLException {
        try (Connection con = ConnectionPool.getConnection()) {
            return tryConnect(con, username, password);
        }
    }

    public static boolean tryConnect(Connection con, String username, String password) throws SQLException {
        Session session = getSession();
        if (session.user != null)
            return true;

        var ans = User.findByUsername(con, username);
        if (ans.isPresent()) {
            var unwrapped = ans.get();
            if (unwrapped.getPassword().equals(password)) {
                session.user = unwrapped;
                session.saveSession();
                NotificationError.show("Connexion à " + session.user.getUsername() + " réussie");
                UI.getCurrent().navigate("/");
                return true;
            }
        }

        return false;
    }

    public static void disconnect(UI ui) {
        Session session = getSession();
        if (isConnected()) {
            NotificationError.show("Déconnexion de " + session.user.getUsername());
            session.user = null;
            session.saveSession();
        }
    }
}
