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
package fr.insa.toto.webui.ronde;

import fr.insa.toto.webui.tournois.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Ronde;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.equipe.EquipeStats;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.utils.PodiumComponent;
import fr.insa.toto.webui.utils.Utils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * @author elio
 */
@Route(value = "ronde/", layout = RondeLayout.class)
public class RondeBoard extends VerticalLayout implements BeforeEnterObserver {
    private Ronde ronde;
    private Tournois tournois;

    private final VerticalLayout infoSection = new VerticalLayout();
    private final VerticalLayout podiumSection = new VerticalLayout();
    
    private final Span nomText = new Span();
    private final Span rondesText = new Span();
    private final Span tournoisText = new Span();

    private final Button buttonEdit = new Button("Éditer");
    private final Button buttonTournois = new Button("Afficher");

    public RondeBoard() {
        this.setSpacing(true);
        this.setPadding(true);
        
        infoSection.add(new H2("Ronde"));
        
        this.add(infoSection, podiumSection);
    }

    private void updateContents(Connection con) throws SQLException {
        ronde.populate(con);

        try {
            tournois = Tournois.findById(con, ronde.getIdTournois()).get();
            tournoisText.setText("Nom du tournoi : " + tournois.getName());
        } catch (NoSuchElementException ex) {
            NotificationError.internError(ex);
        }

        nomText.setText("Nom de la ronde : " + ronde.getName());
        rondesText.setText("Nombre de matchs : " + ronde.getNbMatchs(con));

        podiumSection.removeAll();
        List<Equipe> top3 = EquipeStats.getTop3(con, Optional.empty());

        if (top3.isEmpty()) {
            podiumSection.add(new Span("Aucun match avec score n'a été trouvé pour ce tournoi."));
        } else {
            podiumSection.add(new PodiumComponent(top3));
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        if (id == null) {
            Session.addErrorMessage("RondeBoard: pas d'ID de ronde en mémoire");
            event.forwardTo(InternError.class);
        } else {
            try (Connection con = ConnectionPool.getConnection()) {
                this.ronde = Ronde.findById(con, id).get();

                if (!infoSection.getChildren().anyMatch(c -> c == nomText)) {
                    infoSection.add(nomText, rondesText);

                    buttonEdit.addClickListener(e -> {
                        var editor = new RondeEditor();
                        editor.open(ronde);

                        editor.setOnSavedCallback(t -> {
                            try {
                                this.updateContents(con);
                            } catch (SQLException ex) {
                                NotificationError.sql(ex);
                            }
                        });
                    });
                    Utils.visibleAdmin(buttonEdit);
                    infoSection.add(buttonEdit);

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
                    infoSection.add(new HorizontalLayout(tournoisText, buttonTournois));

                    podiumSection.setAlignItems(FlexComponent.Alignment.CENTER);

                    H2 titre = new H2("Classement des équipes dans la ronde " + ronde.getName());
                    podiumSection.setAlignSelf(FlexComponent.Alignment.CENTER, titre);
                    podiumSection.add(titre);
                }

                this.updateContents(con);

            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.internError("La ronde " + id + " n'a pas été trouvée dans la base de données", ex);
            }
        }
    }
}