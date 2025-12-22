/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.toto.webui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.SQLException;


/**
 *
 * @author pmarchal01
 */
public class MatchsLayout extends AppLayout implements BeforeEnterObserver {
    private int matchId;
    private final SideNavItem todo;
    private final SideNavItem ronde;
    private final SideNavItem board;
    
    private Select<Matchs> select;
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.matchId = Integer.parseInt(event.getRouteParameters().get("matchsId").get());


        final String pfx = "matchs/" + matchId + "/";
        this.board.setPath(pfx);
        this.ronde.setPath(pfx + "ronde");
     
        try (var con = ConnectionPool.getConnection()) {
            var list = Matchs.tousLesMatchs(con);
            select.setItems(list);

            var tournois = Matchs.findById(con, matchId);
            if (tournois.isPresent()) {
                select.setValue(tournois.get());
            } else {

                NotificationError.error("Le match " + matchId + " n'existe pas !");
            }
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }
    
    public MatchsLayout() {
        select = new Select<>();
        select.setItemLabelGenerator(Matchs::getName);
        select.setPlaceholder("Choisir un match...");
        select.addValueChangeListener(t -> {
            if (t.getValue() != null) {
                this.matchId = t.getValue().getId();
                this.getUI().ifPresent(ui -> ui.navigate("matchs/" + this.matchId));
            }
        });
        select.setLabel("Matchs");

        SideNav sideNav = new SideNav();

        this.todo = new SideNavItem("TODO: classes et nav");
        sideNav.addItem(this.todo);
        this.board = new SideNavItem("Tableau de bord");
        sideNav.addItem(this.board);
        this.ronde = new SideNavItem("Matchs");
        sideNav.addItem(this.ronde);

        sideNav.getItems().forEach(each -> each.getStyle().set("margin-bottom", "0.5em"));

        sideNav.setWidthFull();
        sideNav.getStyle().set("display", "flex");
        sideNav.getStyle().set("flexDirection", "column");
        sideNav.getStyle().set("gap", "1rem"); // spacing between items

        super.addToDrawer(new VerticalLayout(
                new H2("Matchs"),
                select,
                sideNav
        ));
    }
}
