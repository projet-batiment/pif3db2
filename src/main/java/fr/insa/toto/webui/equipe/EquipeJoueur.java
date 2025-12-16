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
package fr.insa.toto.webui.equipe;

import fr.insa.toto.webui.equipe.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.webui.DialogDelete;
import fr.insa.toto.webui.JoueurEditor;
import fr.insa.toto.webui.NotificationError;
import fr.insa.toto.webui.parentChild.ParentJoueur;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author elio
 */
@Route(value = "equipe/:equipeId([0-9]*)/joueur", layout = EquipeLayout.class)
public class EquipeJoueur extends ParentJoueur implements BeforeEnterObserver {
    private Equipe equipe = null;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        int id = Integer.parseInt(event.getRouteParameters().get("equipeId").get());

        event.getUI().getPage().fetchCurrentURL(u -> NotificationError.show("joueur " + u));

        try (Connection con = ConnectionPool.getConnection()) {
            this.equipe = Equipe.findById(con, id).get();

        } catch (SQLException ex) {
            NotificationError.sql(ex);
        } catch (NoSuchElementException ex) {
            NotificationError.error("L'équipe " + id + " n'a pas été trouvée dans la base de données : " + ex.getMessage());

        } finally {
            super.initialize(this.equipe.joueurs);
        }
    }
}
