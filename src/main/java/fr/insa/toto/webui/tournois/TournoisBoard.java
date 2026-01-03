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

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.equipe.EquipeStats;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.utils.PodiumComponent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * @author elio
 */
@Route(value = "tournois/", layout = TournoisLayout.class)
public class TournoisBoard extends VerticalLayout implements BeforeEnterObserver {

    private Tournois tournois;
    private final VerticalLayout NomTournoi = new VerticalLayout();
    private final VerticalLayout NombreRondes = new VerticalLayout();
    private final VerticalLayout podiumSection = new VerticalLayout();
    
    private final Span nomText = new Span();
    private final Span rondesText = new Span();

    public TournoisBoard() {
        this.setSpacing(true);
        this.setPadding(true);
        
        NomTournoi.add(new H2("Nom du Tournoi"));
        NombreRondes.add(new H2("Nombre de Terrains"));
        
        this.add(NomTournoi, NombreRondes, podiumSection);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        if (id == null) {
            Session.addErrorMessage("TournoisBoard: pas d'ID de tournois en mémoire");
            event.forwardTo(InternError.class);
        } else {
            try (Connection con = ConnectionPool.getConnection()) {
                this.tournois = Tournois.findById(con, id).get();
                nomText.setText(tournois.getName());
                rondesText.setText(String.valueOf(tournois.getNombreTerrains()));

                if (!NomTournoi.getChildren().anyMatch(c -> c == nomText)) {
                    NomTournoi.add(nomText);
                    NombreRondes.add(rondesText);
                }

                podiumSection.removeAll();
                podiumSection.setAlignItems(FlexComponent.Alignment.CENTER);

                H2 titre = new H2("Classement actuel");
                podiumSection.setAlignSelf(FlexComponent.Alignment.CENTER, titre);
                podiumSection.add(titre);

                List<Equipe> top3 = EquipeStats.getTop3(con, id);

                if (top3.isEmpty()) {
                    podiumSection.add(new Span("Aucun match avec score n'a été trouvé dans la base."));
                } else {
                    podiumSection.add(new PodiumComponent(top3));
                }

            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.internError("Le tournois " + id + " n'a pas été trouvé dans la base de données : " + ex.getMessage());
            }
        }
    }
}