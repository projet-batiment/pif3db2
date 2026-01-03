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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.tournois.TournoisEditor;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author qleveque01
 */
@Route(value = "equipe/", layout = EquipeLayout.class)
public class EquipeBoard extends VerticalLayout implements BeforeEnterObserver {
    private Equipe equipe;
    private Tournois tournois;

    private H2 title;
    private Grid<EquipeStats> grid;
    private Span tournoisText = new Span();

    private final Button buttonEdit = new Button("Éditer");
    private final Button buttonTournois = new Button("Afficher");

    private void updateContents(Connection con) throws SQLException {
        title.setText("Tableau de bord : équipe " + equipe.getNom());
        tournoisText.setText("Nom du tournoi : " + tournois.getName());

        tournois.populate(con);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        if (id == null) {
            Session.addErrorMessage("EquipeBoard: pas d'ID d'équipe en mémoire");
            event.forwardTo(InternError.class);
        } else {
            try (Connection con = ConnectionPool.getConnection()) {
                this.equipe = Equipe.findById(con, id).get();
                grid.setItems(EquipeStats.findStatsForGrid(this.equipe.getId()));

                try {
                    this.tournois = Tournois.findById(con, equipe.getIdTournoi()).get();
                } catch (NoSuchElementException ex) {
                    NotificationError.internError("Le tournoi " + equipe.getIdTournoi() + " n'a pas été trouvé dans la base de données", ex);
                }

                updateContents(con);

            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.internError("L'équipe " + id + " n'a pas été trouvée dans la base de données", ex);
            }
        }
    }

    public EquipeBoard() {
        this.title = new H2("Tableau de bord : Equipe");
        var equipeEditor = new EquipeEditor();

        this.add(title, this.tournoisText);

        try (Connection con = ConnectionPool.getConnection()) {
            buttonEdit.addClickListener(e -> {
                var editor = new EquipeEditor();
                editor.open(equipe);

                editor.setOnSavedCallback(t -> {
                    try {
                        this.updateContents(con);
                    } catch (SQLException ex) {
                        NotificationError.sql(ex);
                    }
                });
            });
            buttonEdit.setText(Session.isAdmin() ? "Éditer" : "Afficher");

            buttonTournois.addClickListener(e -> {
                var editor = new TournoisEditor();
                editor.open(tournois);

                editor.setOnSavedCallback(t -> {
                    try {
                        this.updateContents(con);
                    } catch (SQLException ex) {
                        NotificationError.sql(ex);
                    }
                });
            });
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }

        this.add(buttonEdit, new HorizontalLayout(tournoisText, buttonTournois));

        this.add(new H3("Statistiques détaillées"));
        this.grid = new Grid<>();
        grid.addColumn(EquipeStats::getNombreDeMatchs).setHeader("Nombre de matchs");
        grid.addColumn(EquipeStats::getVictoires).setHeader("Victoires");
        grid.addColumn(EquipeStats::getDefaites).setHeader("Défaites");
        grid.addColumn(EquipeStats::getNuls).setHeader("Nuls");
        grid.addColumn(EquipeStats::getButsInscrits).setHeader("Buts inscrits");
        grid.addColumn(EquipeStats::getButsEncaisses).setHeader("Buts encaissés");
        grid.addColumn(EquipeStats::getDifferenceDeButs).setHeader("Différence");
        
        grid.setWidthFull();
        add(grid);
    }
}

