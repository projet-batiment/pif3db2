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
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.NotificationError;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public class TournoisLayout extends AppLayout implements BeforeEnterObserver {
    private int tournoisId;
    private final SideNavItem todo;
    private final SideNavItem matchs;
    private final SideNavItem equipes;
    private final SideNavItem joueurs;
    private final SideNavItem board;

    private Select<Tournois> select;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.tournoisId = Integer.parseInt(event.getRouteParameters().get("tournoisId").get());


        this.board.setPath(TournoisBoard.class, new RouteParameters("tournoisId", "" + tournoisId));
        this.matchs.setPath(TournoisMatchs.class, new RouteParameters("tournoisId", "" + tournoisId));
        this.equipes.setPath(TournoisEquipe.class, new RouteParameters("tournoisId", "" + tournoisId));
        this.joueurs.setPath(TournoisJoueur.class, new RouteParameters("tournoisId", "" + tournoisId));

        try (var con = ConnectionPool.getConnection()) {
            var list = Tournois.tousLesTournois(con);
            select.setItems(list);

            var tournois = Tournois.findById(con, tournoisId);
            if (tournois.isPresent()) {
                select.setValue(tournois.get());
            } else {

                NotificationError.error("Le tournois " + tournoisId + " n'existe pas !");
            }
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    public TournoisLayout() {
        select = new Select<>();
        select.setItemLabelGenerator(Tournois::getNom);
        select.setPlaceholder("Choisir un tournois...");
        select.addValueChangeListener(t -> {
            if (t.getValue() != null) {
                this.tournoisId = t.getValue().getId();
                this.getUI().ifPresent(ui -> ui.navigate("tournois/" + this.tournoisId));
            }
        });
        select.setLabel("Tournois");

        SideNav sideNav = new SideNav();

        this.todo = new SideNavItem("TODO: classes et nav");
        sideNav.addItem(this.todo);
        this.board = new SideNavItem("Tableau de bord");
        sideNav.addItem(this.board);
        this.matchs = new SideNavItem("Matchs");
        sideNav.addItem(this.matchs);
        this.equipes = new SideNavItem("Équipes");
        sideNav.addItem(this.equipes);
        this.joueurs = new SideNavItem("Joueurs");
        sideNav.addItem(this.joueurs);

        sideNav.getItems().forEach(each -> each.getStyle().set("margin-bottom", "0.5em"));

        sideNav.setWidthFull();
        sideNav.getStyle().set("display", "flex");
        sideNav.getStyle().set("flexDirection", "column");
        sideNav.getStyle().set("gap", "1rem"); // spacing between items

        super.addToDrawer(new VerticalLayout(
                new H2("Tournois"),
                select,
                sideNav
        ));
    }
}
