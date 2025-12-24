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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import fr.insa.toto.model.User;
import fr.insa.toto.webui.session.Session;

/**
 *
 * @author elio
 */
public class Utils {
    public static final String capitalizeFirst(String s) {
        switch (s.length()) {
            case 0 -> {
                return "";
            }
            case 1 -> {
                return s.toUpperCase();
            }
            default -> {
                return s.substring(0, 1).toUpperCase() + s.substring(1);
            }
        }
    }

    public static final void enableAdmin(HasEnabled component) {
        if (Session.isAdmin())
            component.setEnabled(true);
        else
            component.setEnabled(false);
    }

    public static final void visibleAdmin(Component component) {
        if (Session.isAdmin())
            component.setVisible(true);
        else
            component.setVisible(false);
    }

    public static final void enableConnected(HasEnabled component) {
        if (Session.isConnected())
            component.setEnabled(true);
        else
            component.setEnabled(false);
    }

    public static final void visibleConnected(Component component) {
        if (Session.isConnected())
            component.setVisible(true);
        else
            component.setVisible(false);
    }

    public static final void visibleLegitimate(Component component, User user) {
        if (Session.isAdmin() || (user != null && Session.getUser().equals(user)))
            component.setVisible(true);
        else
            component.setVisible(false);
    }
}
