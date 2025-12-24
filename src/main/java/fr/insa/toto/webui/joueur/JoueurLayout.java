package fr.insa.toto.webui.joueur;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.webui.utils.Layout;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.SQLException;

public class JoueurLayout extends Layout implements BeforeEnterObserver {

    private int joueurId;
    private final SideNavItem board;
    private final Select<Joueur> select;

    public JoueurLayout() {
        this.select = new Select<>();
        select.setItemLabelGenerator(Joueur::getSurnom);
        select.setLabel("Joueur");
        select.addValueChangeListener(e -> {
            if (e.getValue() != null && e.isFromClient()) {
                this.getUI().ifPresent(ui -> ui.navigate("joueur/" + e.getValue().getId()));
            }
        });

        SideNav sideNav = new SideNav();
        this.board = new SideNavItem("Tableau de bord");
        sideNav.addItem(this.board);

        super.addToDrawer(new VerticalLayout(new H2("Joueur"), select, sideNav));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("joueurId").ifPresent(idStr -> {
            this.joueurId = Integer.parseInt(idStr);
            this.board.setPath("joueur/" + joueurId);

            try (var con = ConnectionPool.getConnection()) {
                select.setItems(Joueur.tousLesJoueurs(con));
                Joueur.findById(con, joueurId).ifPresent(select::setValue);
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        });
    }
}
