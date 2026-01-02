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
package fr.insa.toto.webui.utils;

import com.vaadin.flow.component.Component;
import fr.insa.toto.webui.tournois.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouteParameters;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public abstract class Layout extends AppLayout implements AfterNavigationObserver {
    private SideNavItem tournois;
    private SideNavItem joueurs;
    private SideNavItem equipes;
    private SideNavItem matchs;
    private SideNavItem connect;
    private SideNavItem disconnect;
    private SideNavItem users;

    private SideNav primaryNav;
    private SideNav userNav;

    @Override
    public void afterNavigation(AfterNavigationEvent ane) {
        Utils.visibleConnected(this.disconnect);
        Utils.visibleAdmin(users);

        String userHeader;
        String userLabel;
        String userLink;
        if (Session.isConnected()) {
            userHeader = "Bienvenue " + Session.getUser().getUsername() + " !";
            userLabel = "Mon compte";
            userLink = "/user/current";
        } else {
            userHeader = "Compte";
            userLabel = "Se connecter";
            userLink = "login";
        }
        this.userNav.setLabel(userHeader);
        this.connect.setLabel(userLabel);
        this.connect.setPath(userLink);
    }

    public static class Default extends Layout {
        public Default() {
            super.addToDrawer();
        }
    }

    @Override
    public final void addToDrawer(Component... components) {
        super.addToDrawer(components);

        this.primaryNav = new SideNav("Général");

        this.tournois = new SideNavItem("Tournois", "/tournois/list");
        this.primaryNav.addItem(this.tournois);
        this.joueurs = new SideNavItem("Joueurs", "/joueur/list");
        this.primaryNav.addItem(this.joueurs);
//        this.equipes = new SideNavItem("Équipes", "/equipe/list");
//        this.primaryNav.addItem(this.equipes);
//        this.matchs = new SideNavItem("Matchs", "/match/list");
//        this.primaryNav.addItem(this.matchs);
        this.users = new SideNavItem("Utilisateurs", "/user/list");
        this.primaryNav.addItem(users);

        this.userNav = new SideNav();
        this.connect = new SideNavItem("");
        this.disconnect = new SideNavItem("Déconnexion", "logout");
        this.userNav.addItem(this.connect, this.disconnect);

        this.primaryNav.setWidthFull();
        this.primaryNav.getStyle().set("display", "flex");
        this.primaryNav.getStyle().set("flexDirection", "column");
        this.userNav.setWidthFull();
        this.userNav.getStyle().set("display", "flex");
        this.userNav.getStyle().set("flexDirection", "column");

        super.addToDrawer(new VerticalLayout(
            primaryNav,
            userNav
        ));
    }
}
