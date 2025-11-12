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
package fr.insa.toto.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Tournois;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
@Route("tournois")
public class TournoisListe extends VerticalLayout {
    Grid<Tournois> grid;

    private static void notifyTodo() {
        Notification.show("Still to be done...");
    }

    private void updateGridList() {
        try (Connection con = ConnectionPool.getConnection()) {
            grid.setItems(Tournois.tousLesTournois(con));
        } catch (SQLException ex) {
            Notification.show("Problème : " + ex.getLocalizedMessage());
        }
    }

    public TournoisListe() {
        this.add(new H2("Liste des tournois"));

        this.grid = new Grid<>();
        grid.addColumn(Tournois::getNom).setHeader("Nom");
        grid.addColumn(Tournois::getNombreRondes).setHeader("Nombre de rondes");
        grid.addColumn(new ComponentRenderer<>(t -> {
            Button bt = new Button("Afficher");
            bt.addClickListener(event -> {
                bt.getUI().ifPresent(ui -> ui.navigate(TournoisSpecific.class, t.getId()));
            });
            return bt;
        }));
        grid.addColumn(new ComponentRenderer<>(t -> {
            Button bt = new Button("Supprimer");
            bt.addClickListener(event -> {
                var d = new ConfirmDialog();

                d.setHeader("Suppression");
                d.setText("Supprimer le tournois " + t.getNom() + " ?");

                d.setCancelable(true);
                d.setRejectable(false);

                d.setConfirmText("Supprimer");
                d.addConfirmListener(e -> {
                    try (Connection con = ConnectionPool.getConnection()) {
                        t.deleteFromDB(con);
                        this.updateGridList();
                    } catch (SQLException ex) {
                        Notification.show("Erreur : " + ex.getMessage());
                    }
                });

                d.open();
            });
            return bt;
        }));

        this.updateGridList();

        this.add(new PageList(grid, t -> notifyTodo(), t -> notifyTodo()));
    }
}
