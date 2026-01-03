package fr.insa.toto.webui.parentChild;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.webui.equipe.EquipeEditor;
import fr.insa.toto.webui.equipe.EquipeStats;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.utils.PodiumComponent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * @author elio
 */
public abstract class ParentEquipe extends ParentChild<Equipe> {

    public ParentEquipe() {
        super(new EquipeEditor());
        
        Integer tournoiId = Session.getId(0); 

        super.addColumn(Equipe::getNom).setHeader("Nom").setSortable(true);
        super.addColumn(Equipe::getNbJoueurs).setHeader("Joueurs").setSortable(true);

        super.addColumn(equipe -> {
            return EquipeStats.findById(equipe.getId())
                    .map(s -> s.getPoints() + " pts")
                    .orElse("0 pts");
        }).setHeader("Points").setSortable(true);

        super.addColumn(equipe -> {
            int rang = 0;
            if (tournoiId != null) {
                rang = EquipeStats.getRangEquipe(equipe.getId(), tournoiId);
            }
            if (rang == 0) return "-";
            return (rang == 1) ? "1er" : rang + "ème";
        }).setHeader("Rang").setSortable(true);

        H2 titreClassement = new H2("Classement actuel");
        this.setAlignSelf(FlexComponent.Alignment.CENTER, titreClassement);
        this.add(titreClassement);
        
        try (Connection con = ConnectionPool.getConnection()) {
            List<Equipe> top3 = EquipeStats.getTop3(con, tournoiId);
            this.add(new PodiumComponent(top3));
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }
}