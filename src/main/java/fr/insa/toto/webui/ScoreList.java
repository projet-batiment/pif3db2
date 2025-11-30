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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Score;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author pmarchal01
 */

@Route("scores")
public class ScoreList extends VerticalLayout {
    private Grid<Score> grid;

    private static void notifyTodo() {
        Notification.show("Still to be done...");
    }

    private void updateGridList() {
        try (Connection con = ConnectionPool.getConnection()) {
            grid.setItems(Score.tousLesScores(con));
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    public ScoreList() {
        this.add(new H2("Liste des scores"));
        
        var bNew = new Button("Nouveau");
        
        var scoresEditor = new ScoreEditor();
        scoresEditor.addSavedCallback(() -> updateGridList());
        bNew.addClickListener(t -> scoresEditor.open(null));

        this.grid = new Grid<>();
        grid.addColumn(Score::getScore).setHeader("Score");
        grid.addColumn(Score::getIdEquipe).setHeader("Id de l'équipe");
        grid.addColumn(Score::getIdMatch).setHeader("Id du match");
        grid.addColumn(new ComponentRenderer<>(t -> {
            Button bEdit = new Button("Afficher");
            bEdit.addClickListener(event -> {
                scoresEditor.open(t);
            });

            Button bDelete = new Button("Supprimer");
            bDelete.addClickListener(event -> {
                new DialogDelete("le score " + t.getScore(), () -> {
                    try (Connection con = ConnectionPool.getConnection()) {
                        t.deleteFromDB(con);
                        this.updateGridList();
                        Notification.show("Le score " + t.getScore()+ " a bien été supprimé");
                    } catch (SQLException ex) {
                        NotificationError.sql(ex);
                    }
                }).open();
            });

            return new HorizontalLayout(bEdit, bDelete);
        })).setHeader(bNew);

        this.updateGridList();

        this.add(grid);
        
    }
}
