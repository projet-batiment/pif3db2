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
import com.vaadin.flow.server.VaadinSession;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.User;
import fr.insa.toto.webui.utils.NotificationError;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private HashMap<String, Integer> map;

    public Session() {
        this.user = null;
        this.checkedFirstSession = false;
        this.ids = new ArrayList<>();
        this.errorMessage = new ArrayList<>();
        this.map = new HashMap<>();
    }

    private static Session getSession() {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        Session session = vaadinSession.getAttribute(Session.class);

        if (session == null) {
            session = new Session();
            vaadinSession.setAttribute(Session.class, session);
            session.init();
        }

        return session;
    }

    public void init() {
        if (this.checkedFirstSession) return;
        NotificationError.log("session/init: now");

        this.checkedFirstSession = true;

        try (Connection con = ConnectionPool.getConnection()) {
            if (User.tousLesAdmins(con).isEmpty()) {
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
    }

    public static void pushId(Integer id) {
        var session = getSession();
        session.ids.add(id);
    }

    public static void popId() {
        var session = getSession();
        session.ids.removeLast();
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
    }

    public static void addErrorMessage(String errorMessage) {
        var session = getSession();
        session.errorMessage.add(errorMessage);
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
                NotificationError.info("Connexion à " + session.user.getUsername() + " réussie");
                UI.getCurrent().navigate("/");
                return true;
            }
        }

        return false;
    }

    public static void disconnect() {
        Session session = getSession();
        if (isConnected()) {
            NotificationError.info("Déconnexion de " + session.user.getUsername());
            session.user = null;
        }
    }
}
