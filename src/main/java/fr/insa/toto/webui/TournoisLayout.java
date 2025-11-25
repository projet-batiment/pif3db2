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

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouteParameters;

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
    private final SideNavItem main;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.tournoisId = Integer.parseInt(event.getRouteParameters().get("tournoisId").get());

        this.matchs.setPath(TournoisMatchs.class, new RouteParameters("tournoisId", "" + tournoisId));
        this.equipes.setPath(TournoisEquipe.class, new RouteParameters("tournoisId", "" + tournoisId));
        this.joueurs.setPath(TournoisJoueur.class, new RouteParameters("tournoisId", "" + tournoisId));
    }

    public TournoisLayout() {
        SideNav sideNav = new SideNav();

        this.todo = new SideNavItem("TODO: classes et nav");
        sideNav.addItem(this.todo);
        this.main = new SideNavItem("Tableau de bord");
        sideNav.addItem(this.main);
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
                sideNav
        ));
    }
}
