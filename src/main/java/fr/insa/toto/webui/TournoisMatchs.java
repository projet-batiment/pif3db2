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
import fr.insa.toto.model.Matchs;
import fr.insa.toto.model.Tournois;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author elio
 */
@Route(value = "tournois/:tournoisId([0-9]*)/match", layout = TournoisLayout.class)
public class TournoisMatchs extends VerticalLayout implements BeforeEnterObserver {
    private Tournois tournois;
    private Grid<Matchs> grid;

    private H2 title;
    private Button bNew;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        int id = Integer.parseInt(event.getRouteParameters().get("tournoisId").get());

        try (Connection con = ConnectionPool.getConnection()) {
            this.tournois = Tournois.findById(con, id).get();
            title.setText("Matchs du tournois " + tournois.getNom());

            this.updateGridList();
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        } catch (NoSuchElementException ex) {
            NotificationError.error("Le tournois " + id + " n'a pas été trouvé dans la base de données : " + ex.getMessage());
        }
    }


    private void updateGridList() {
        try (Connection con = ConnectionPool.getConnection()) {
            var list = Matchs.tousLesMatchs(con);
            for (var each: list) {
                try {
                    each.populate(con);
                } catch (NoSuchElementException ex) {
                    NotificationError.error("L'un des éléments du match " + each.getId() + "n'a pas bien été sauvegardé");
                }
            }
            grid.setItems(list);
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    public TournoisMatchs() {
        bNew = new Button("Nouveau...");

        var matchsEditor = new MatchsEditor();
        matchsEditor.addSavedCallback(() -> updateGridList());
        bNew.addClickListener(t -> matchsEditor.open(null));

        this.grid = new Grid<>();
        grid.addColumn(Matchs::getNomA).setHeader("Equipe A");
        grid.addColumn(Matchs::getScoreA);
        grid.addColumn(Matchs::getScoreB);
        grid.addColumn(Matchs::getNomB).setHeader("Equipe B");
        grid.addColumn(new ComponentRenderer<>(t -> {
            Button bEdit = new Button("Afficher");
            bEdit.addClickListener(e -> {
                matchsEditor.open(t);
            });

            Button bDelete = new Button("Supprimer");
            bDelete.addClickListener(e -> {
                new DialogDelete("le match", () -> {
                    try (Connection con = ConnectionPool.getConnection()) {
                        t.deleteFromDB(con);
                        this.updateGridList();
                        Notification.show("Le match a bien été supprimé");
                    } catch (SQLException ex) {
                        NotificationError.sql(ex);
                    }
                }).open();
            });

            return new HorizontalLayout(bEdit, bDelete);
        })).setHeader(bNew);

        title = new H2();
        this.add(title, grid);
    }
}
