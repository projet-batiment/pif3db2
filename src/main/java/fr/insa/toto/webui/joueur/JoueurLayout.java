package fr.insa.toto.webui.joueur;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.webui.equipe.EquipeBoard;
import fr.insa.toto.webui.equipe.EquipeJoueur;
import fr.insa.toto.webui.equipe.EquipeMatchs;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.session.Session;
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
                this.joueurId = e.getValue().getId();
                Session.setIds(this.joueurId);
                UI.getCurrent().refreshCurrentRoute(true);
            }
        });

        SideNav sideNav = new SideNav();
        this.board = new SideNavItem("Tableau de bord");
        sideNav.addItem(this.board);

        sideNav.setWidthFull();
        sideNav.getStyle().set("display", "flex");
        sideNav.getStyle().set("flexDirection", "column");

        super.addToDrawer(new VerticalLayout(new H2("Joueur"), select, sideNav));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        if (id == null) {
            Session.addErrorMessage("JoueurLayout: pas d'ID de joueur en mémoire");
            event.forwardTo(InternError.class);
        } else {
            this.joueurId = id;
            this.board.setPath(JoueurBoard.class);

            try (var con = ConnectionPool.getConnection()) {
                select.setItems(Joueur.tousLesJoueurs(con));
                Joueur.findById(con, joueurId).ifPresent(select::setValue);
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        }
    }
}
