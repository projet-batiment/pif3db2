/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.toto.webui.matchs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.Layout;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.SQLException;
import java.util.NoSuchElementException;


/**
 *
 * @author pmarchal01
 */
public class MatchsLayout extends Layout implements BeforeEnterObserver {
    private int matchId;
    private final SideNavItem board;
    
    private Select<Matchs> select;
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        if (id == null) {
            Session.addErrorMessage("MatchsLayout: pas d'ID de match en mémoire");
            event.forwardTo(InternError.class);
        } else {
            this.matchId = id;

            this.board.setPath(MatchsBoard.class);
         
            try (var con = ConnectionPool.getConnection()) {
                var list = Matchs.tousLesMatchs(con);
                list.forEach(each -> {
                    try {
                        each.populate(con);
                    } catch (SQLException ex) {
                        NotificationError.sql(ex);
                    } catch (NoSuchElementException ex) {
                        NotificationError.internError("L'un des éléments du match " + each.getId() + " n'a pas été trouvé", ex);
                    }
                });
                select.setItems(list);
                select.setValue(Matchs.findById(con, matchId).get());

            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.internError("Le match " + id + " n'a pas été trouvé dans la base de données", ex);
            }
        }
    }
    
    public MatchsLayout() {
        select = new Select<>();
        select.setItemLabelGenerator(Matchs::getName);
        select.setPlaceholder("Choisir un match...");
        select.addValueChangeListener(t -> {
            if (t.getValue() != null) {
                this.matchId = t.getValue().getId();
                Session.setIds(this.matchId);
                UI.getCurrent().refreshCurrentRoute(true);
            }
        });
        select.setLabel("Matchs");

        SideNav sideNav = new SideNav();

        this.board = new SideNavItem("Tableau de bord");
        sideNav.addItem(this.board);

        sideNav.setWidthFull();
        sideNav.getStyle().set("display", "flex");
        sideNav.getStyle().set("flexDirection", "column");

        super.addToDrawer(new VerticalLayout(
                new H2("Matchs"),
                select,
                sideNav
        ));
    }
}
