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
package fr.insa.toto.webui.user;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.User;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.DialogDelete;
import fr.insa.toto.webui.utils.Layout;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public class UserBoardContents extends VerticalLayout {
    public UserBoardContents(User user) {
        var editor = new UserEditor();

        editor.setOnDeletedCallback(u -> {
            new DialogDelete("l'utilisateur " + u.getName(), () -> {
                try (Connection con = ConnectionPool.getConnection()) {
                    u.deleteFromDB(con);
                    UI.getCurrent().getPage().setLocation("logout");
                } catch (SQLException ex) {
                    NotificationError.sql(ex);
                }
            }).open();
        });

        var title = new H2("Tableau de bord : utilisateur " + user.getUsername());

        var edit = new Button("Éditer");
        edit.addClickListener(e -> editor.open(user));

        super.add(title, edit);
    }

    public UserBoardContents() {
        this(new User());
    }
}
