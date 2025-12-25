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
package fr.insa.toto.webui.tournois;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.parentChild.ParentJoueur;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.session.Session;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

/**
 *
 * @author elio
 */
@Route(value = "tournois/joueur", layout = TournoisLayout.class)
public class TournoisJoueur extends ParentJoueur implements BeforeEnterObserver {
    private Tournois tournois;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        if (id == null) {
            Session.addErrorMessage("TournoisJoueur: pas d'ID de tournois en mémoire");
            event.forwardTo(InternError.class);
        } else {
            try (Connection con = ConnectionPool.getConnection()) {
                this.tournois = Tournois.findById(con, id).get();

            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.error("Le tournois " + id + " n'a pas été trouvé dans la base de données : " + ex.getMessage());

            } finally {
                super.initialize(this.tournois.joueurs);
            }
        }
    }
}
