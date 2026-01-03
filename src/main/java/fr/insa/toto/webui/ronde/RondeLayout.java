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

import fr.insa.toto.webui.ronde.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouteParameters;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Ronde;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.Layout;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author elio
 */
public class RondeLayout extends Layout implements BeforeEnterObserver {
    private int rondeId;
    private final SideNavItem matchs;
    private final SideNavItem equipes;
    private final SideNavItem joueurs;
    private final SideNavItem board;

    private Select<Ronde> select;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        if (id == null) {
            Session.addErrorMessage("RondeLayout: pas d'ID de ronde en mémoire");
            event.forwardTo(InternError.class);
        } else {
            this.rondeId = id;

            this.board.setPath(RondeBoard.class);
//            this.matchs.setPath(RondeMatchs.class);
//            this.equipes.setPath(RondeEquipe.class);
//            this.joueurs.setPath(RondeJoueur.class);

            try (var con = ConnectionPool.getConnection()) {
                var ronde = Ronde.findById(con, rondeId).get();

                var list = Ronde.findByIdTournois(con, ronde.getIdTournois());
                for (var each: list)
                    each.populate(con);
                select.setItems(list);
                select.setLabel("Rondes du tournoi " + ronde.getNomTournois());

                select.setValue(ronde);
            } catch (SQLException ex) {
                NotificationError.sql(ex);

            } catch (NoSuchElementException ex) {
                NotificationError.internError("La ronde " + id + " n'a pas été trouvé dans la base de données", ex);
            }
        }
    }

    public RondeLayout() {
        select = new Select<>();
        select.setItemLabelGenerator(Ronde::getName);
        select.setPlaceholder("Choisir un tournoi...");
        select.addValueChangeListener(t -> {
            if (t.getValue() != null) {
                this.rondeId = t.getValue().getId();
                Session.setIds(this.rondeId);
                UI.getCurrent().refreshCurrentRoute(true);
            }
        });

        SideNav sideNav = new SideNav("Cette ronde");

        this.board = new SideNavItem("Tableau de bord");
        sideNav.addItem(this.board);
        this.joueurs = new SideNavItem("Joueurs");
        sideNav.addItem(this.joueurs);
        this.equipes = new SideNavItem("Équipes");
        sideNav.addItem(this.equipes);
        this.matchs = new SideNavItem("Matchs");
        sideNav.addItem(this.matchs);

        sideNav.setWidthFull();
        sideNav.getStyle().set("display", "flex");
        sideNav.getStyle().set("flexDirection", "column");

        super.addToDrawer(new VerticalLayout(
                new H2("Ronde"),
                select,
                sideNav
        ));
    }
}
