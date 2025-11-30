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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.DialogDelete;
import fr.insa.toto.webui.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
@Route("tournois")
public class TournoisListe extends VerticalLayout {
    private Grid<Tournois> grid;

    private void updateGridList() {
        try (Connection con = ConnectionPool.getConnection()) {
            grid.setItems(Tournois.tousLesTournois(con));
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    private void deleteDialog(Tournois t) {
        new DialogDelete("le tournois " + t.getNom(), () -> {
            try (Connection con = ConnectionPool.getConnection()) {
                t.deleteFromDB(con);
                this.updateGridList();
                Notification.show("Le tournois " + t.getNom() + " a bien été supprimé");
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        }).open();
    }

    public TournoisListe() {
        this.add(new H2("Liste des tournois"));

        var bNew = new Button("Nouveau");

        var tournoisEditor = new TournoisEditor();
        tournoisEditor.setOnSavedCallback(t -> {
            updateGridList();
        });
        tournoisEditor.setOnDeletedCallback(t -> deleteDialog(t));
        bNew.addClickListener(t -> tournoisEditor.open(null));

        this.grid = new Grid<>();
        grid.addColumn(Tournois::getNom).setHeader("Nom");
        grid.addColumn(Tournois::getNombreRondes).setHeader("Nombre de rondes");
        grid.addColumn(new ComponentRenderer<>(t -> {
            Button bEdit = new Button("Afficher");
            bEdit.addClickListener(event -> {
                tournoisEditor.open(t);
            });

            Button bDelete = new Button("Supprimer");
            bDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            bDelete.addClickListener(event -> {
                deleteDialog(t);
            });

            return new HorizontalLayout(bEdit, bDelete);
        })).setHeader(bNew);

        this.updateGridList();

        this.add(grid);
    }
}
