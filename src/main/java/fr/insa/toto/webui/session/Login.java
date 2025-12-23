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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.Layout;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
@Route(value = "login", layout = Layout.Default.class)
public class Login extends VerticalLayout {
    public Login() {
        TextField username = new TextField("Utilisateur");
        PasswordField password = new PasswordField("Mot de passe");

        Button login = new Button("Connexion");
        login.addClickListener(e -> {
            try {
                if (Session.tryConnect(username.getValue(), password.getValue())) {
                    super.getUI().ifPresent(ui -> ui.navigate("user"));
                } else {
                    NotificationError.show("Nom d'utilisateur ou mot de passe erroné...");
                }
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        });

        super.add(
            new H2("Connexion"),
            new HorizontalLayout(username, password),
            login
        );
    }
}
