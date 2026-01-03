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
    private boolean hasClassement = false;

    protected Optional<Integer> classementLabel(Equipe equipe) {
        return Optional.empty();
    }

    protected Optional<Integer> pointsLabel(Equipe equipe) {
        return Optional.empty();
    }

    public void addClassement(List<Equipe> top3) {
        if (hasClassement)
            return;

        hasClassement = true;

        H2 titreClassement = new H2("Classement actuel (points de résultats)");
        this.setAlignSelf(FlexComponent.Alignment.CENTER, titreClassement);
        this.add(titreClassement);
        
        this.add(new PodiumComponent(top3));
    }

    public ParentEquipe() {
        super(new EquipeEditor());

        super.addColumn(Equipe::getNom).setHeader("Nom").setSortable(true);
        super.addColumn(Equipe::getNbJoueurs).setHeader("Joueurs").setSortable(true);

        super.addColumn(equipe -> {
            return this.pointsLabel(equipe).map(v -> v + " pts").orElse("-");
        }).setHeader("Points de résultats").setSortable(true);

        super.addColumn(equipe -> {
            return this.classementLabel(equipe).map(v -> v + (v == 1 ? "er" : "ème")).orElse("-");
        }).setHeader("Classement").setSortable(true);
    }
}