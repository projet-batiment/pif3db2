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
package fr.insa.toto.webui.utils;

import com.vaadin.flow.component.notification.Notification;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public class NotificationError extends Notification {
    public static final int NORMAL = 0;
    public static final int LOG = 1;
    public static int loglevel = 0;

    public static final void info(String message) {
        String s = message;
        Notification.show(s);
    }

    public static final void userError(String message) {
        String s = "Erreur : '" + message + "'";
        Notification.show(s);
    }

    public static final void internError(String message) {
        String s = "Erreur interne : '" + message + "'";
        Notification.show(s);
        System.out.println(s);
    }

    public static final void log(String message) {
        String s = "Log : '" + message + "'";
        if (loglevel > LOG)
            Notification.show(s);
        System.out.println(s);
    }

    public static final void todo(String message) {
        String s = "TODO : '" + message + "'";
        if (loglevel > LOG)
            Notification.show(s);
        System.out.println(s);
    }

    public static void sql(SQLException ex) {
        var s = "Erreur SQL: " + ex.getLocalizedMessage();
        Notification.show(s);
        System.out.println(s);
        ex.printStackTrace();
    }
}
