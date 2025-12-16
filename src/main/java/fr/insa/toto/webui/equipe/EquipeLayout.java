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

import fr.insa.toto.webui.equipe.*;
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
import fr.insa.toto.model.Equipe;
import fr.insa.toto.webui.NotificationError;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public class EquipeLayout extends AppLayout implements BeforeEnterObserver {
    private int equipeId;
    private final SideNavItem todo;
    private final SideNavItem matchs;
    private final SideNavItem tournois;
    private final SideNavItem joueurs;
    private final SideNavItem board;

    private Select<Equipe> select;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.equipeId = Integer.parseInt(event.getRouteParameters().get("equipeId").get());

        this.board.setPath(EquipeBoard.class, new RouteParameters("equipeId", "" + equipeId));
        this.matchs.setPath(EquipeMatchs.class, new RouteParameters("equipeId", "" + equipeId));
//        this.equipes.setPath(EquipeEquipe.class, new RouteParameters("equipeId", "" + equipeId));
//        this.joueurs.setPath(EquipeJoueur.class, new RouteParameters("equipeId", "" + equipeId));
        this.joueurs.setPath("equipe/" + equipeId + "/joueur");

        event.getUI().getPage().fetchCurrentURL(u -> NotificationError.show("layout " + u));

        try (var con = ConnectionPool.getConnection()) {
            var list = Equipe.toutesLesEquipes(con);
            select.setItems(list);

            var equipe = Equipe.findById(con, equipeId);
            if (equipe.isPresent()) {
                select.setValue(equipe.get());
            } else {

                NotificationError.error("L'équipe " + equipeId + " n'existe pas !");
            }
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    public EquipeLayout() {
        select = new Select<>();
        select.setItemLabelGenerator(Equipe::getNom);
        select.setPlaceholder("Choisir une équipe...");
        select.addValueChangeListener(t -> {
            if (t.getValue() != null) {
                this.equipeId = t.getValue().getId();
                this.getUI().ifPresent(ui -> ui.navigate("equipe/" + this.equipeId));
            }
        });
        select.setLabel("Équipe");

        SideNav sideNav = new SideNav();

        this.todo = new SideNavItem("TODO: classes et nav");
        sideNav.addItem(this.todo);
        this.board = new SideNavItem("Tableau de bord");
        sideNav.addItem(this.board);
        this.matchs = new SideNavItem("Matchs");
        sideNav.addItem(this.matchs);
        this.tournois = new SideNavItem("Tournois");
        sideNav.addItem(this.tournois);
        this.joueurs = new SideNavItem("Joueurs");
        sideNav.addItem(this.joueurs);

        sideNav.getItems().forEach(each -> each.getStyle().set("margin-bottom", "0.5em"));

        sideNav.setWidthFull();
        sideNav.getStyle().set("display", "flex");
        sideNav.getStyle().set("flexDirection", "column");
        sideNav.getStyle().set("gap", "1rem"); // spacing between items

        super.addToDrawer(new VerticalLayout(
                new H2("Équipe"),
                select,
                sideNav
        ));
    }
}
