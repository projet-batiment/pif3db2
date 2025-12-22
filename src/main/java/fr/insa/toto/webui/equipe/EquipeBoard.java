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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.utils.DialogDelete;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.tournois.TournoisEditor;
import fr.insa.toto.webui.tournois.TournoisLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author qleveque01
 */
@Route(value = "equipe/:equipeId([0-9]*)", layout = EquipeLayout.class)
public class EquipeBoard extends VerticalLayout implements BeforeEnterObserver {
    private Equipe equipe;
    private H2 title;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        int id = Integer.parseInt(event.getRouteParameters().get("equipeId").get());

        try (Connection con = ConnectionPool.getConnection()) {
            this.equipe = Equipe.findById(con, id).get();
            title.setText("Équipes " + equipe.getNom());
            title.setText("Tableau de bord : équipe " + equipe.getNom());

        } catch (SQLException ex) {
            NotificationError.sql(ex);
        } catch (NoSuchElementException ex) {
            NotificationError.error("L'équipe " + id + " n'a pas été trouvé dans la base de données : " + ex.getMessage());
        }
    }
    private void deleteDialog(Equipe e) {
        new DialogDelete("l'équipe " + e.getNom(), () -> {
            try (Connection con = ConnectionPool.getConnection()) {
                e.deleteFromDB(con);
                Notification.show("L'équipe " + e.getNom() + " a bien été supprimée");
                this.getUI().ifPresent(ui->ui.navigate("/"));
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        }).open();
    }

    public EquipeBoard() {
        this.title = new H2("Tableau de bord : équipe");
        var equipeEditor = new EquipeEditor();

        this.add(title);
            Button bDelete = new Button("Supprimer");
            bDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            bDelete.addClickListener(event -> {
                deleteDialog(this.equipe);
            });        
            Button bEdit = new Button("Modifier");
            bEdit.addClickListener(event -> {
                equipeEditor.open(this.equipe);
            }); 
        this.add(new HorizontalLayout(bEdit, bDelete));
    }
}

