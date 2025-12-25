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

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.User;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public class ResetPasswordDialog {
    public static void open(User user) {
        try (Connection con = ConnectionPool.getConnection()) {
            user.resetPassword();
            user.update(con);

            var info = new ConfirmDialog();

            info.setHeader("Information");
            info.setText("Le nouveau mot de passe de l'utilisateur " + user.getUsername() + " est : " + user.getPassword() + ". Retenez-le bien !");

            info.setRejectable(false);
            info.setCancelable(false);

            info.setConfirmText("OK");

            info.open();
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }
}
