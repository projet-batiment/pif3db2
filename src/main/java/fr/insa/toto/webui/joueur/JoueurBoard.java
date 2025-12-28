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
package fr.insa.toto.webui.joueur;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.DialogDelete;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author qleveque01
 */
@Route(value = "joueur", layout = JoueurLayout.class)
public class JoueurBoard extends VerticalLayout implements BeforeEnterObserver {

    private Joueur joueur;
    private H2 title;
    private Grid<JoueurStats> grid;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        if (id == null) {
            Session.addErrorMessage("JoueurBoard: pas d'ID de joueur en mémoire");
            event.forwardTo(InternError.class);
        } else {
            try (Connection con = ConnectionPool.getConnection()) {
                var optJoueur = Joueur.findById(con, id);
                if (optJoueur.isPresent()) {
                    this.joueur = optJoueur.get();
                    grid.setItems(JoueurStats.findStatsForGrid(this.joueur.getId()));
                    title.setText("Tableau de bord : " + joueur.getSurnom());
                } else {
                    throw new NoSuchElementException("ID non trouvé");
                }

            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.internError("Le joueur " + id + " n'a pas été trouvé dans la base de données : " + ex.getMessage());
            }
        }
    }

    private void deleteDialog(Joueur j) {
        new DialogDelete("le joueur " + j.getSurnom(), () -> {
            try (Connection con = ConnectionPool.getConnection()) {
                j.deleteFromDB(con);
                NotificationError.info("Le joueur " + j.getSurnom() + " a bien été supprimé");
                this.getUI().ifPresent(ui -> ui.navigate("/"));
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        }).open();
    }

    public JoueurBoard() {
        this.title = new H2("Tableau de bord : Joueur");
        // Supposant que vous avez un éditeur pour Joueur similaire à EquipeEditor
        var joueurEditor = new JoueurEditor(); 

        this.add(title);

        Button bDelete = new Button("Supprimer");
        bDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        bDelete.addClickListener(event -> {
            if (this.joueur != null) {
                deleteDialog(this.joueur);
            }
        });

        Button bEdit = new Button("Modifier");
        bEdit.addClickListener(event -> {
            if (this.joueur != null) {
                joueurEditor.open(this.joueur);
            }
        });

        this.add(new HorizontalLayout(bEdit, bDelete));
        this.add(new H3("Statistiques détaillées"));

        this.grid = new Grid<>();
        grid.addColumn(JoueurStats::getNomEquipe).setHeader("Équipe / Cumul");
        grid.addColumn(JoueurStats::getNombreDeMatchs).setHeader("Nombre de matchs");
        grid.addColumn(JoueurStats::getVictoires).setHeader("Victoires");
        grid.addColumn(JoueurStats::getDefaites).setHeader("Défaites");
        grid.addColumn(JoueurStats::getNuls).setHeader("Nuls");
        grid.addColumn(JoueurStats::getButsInscrits).setHeader("Score +");
        grid.addColumn(JoueurStats::getButsEncaisses).setHeader("Score -");
        grid.addColumn(JoueurStats::getDifferenceDeButs).setHeader("Différence");

        grid.setPartNameGenerator(item -> {
            if (" TOTAL CUMULÉ".equals(item.getNomEquipe())) return "total-row";
            return null;
        });

        grid.setWidthFull();
        add(grid);
    }
}