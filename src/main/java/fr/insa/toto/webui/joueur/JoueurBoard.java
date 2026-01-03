package fr.insa.toto.webui.joueur;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.DialogDelete;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 *
 * @author qleveque01
 */
@Route(value = "joueur", layout = JoueurLayout.class)
public class JoueurBoard extends VerticalLayout implements BeforeEnterObserver {

    private Joueur joueur;
    private H2 title;
    private VerticalLayout containerTournois; 
    private Grid<JoueurStats> gridtotal;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        if (id == null) {
            Session.addErrorMessage("JoueurBoard: pas d'ID de joueur en mémoire");
            event.forwardTo(InternError.class);
        } else {
            try (Connection con = ConnectionPool.getConnection()) {
                var optJoueur = Joueur.findById(con, id);
                if (optJoueur.isPresent()) {
                    this.joueur = optJoueur.get();
                    title.setText("Tableau de bord : " + joueur.getSurnom());

                    gridtotal.setItems(JoueurStats.findStatsForGrid(this.joueur.getId()));

                    containerTournois.removeAll();
                    List<JoueurStats> details = JoueurStats.findStatsDetaillees(this.joueur.getId());
                    
                    Map<String, List<JoueurStats>> maps = details.stream()
                            .collect(Collectors.groupingBy(JoueurStats::getNomTournoi));

                    maps.forEach((nomTournoi, stats) -> {
                        containerTournois.add(new H3("Tournoi : " + nomTournoi));
                        Grid<JoueurStats> g = createGridTemplate(); 
                        g.setItems(stats);
                        containerTournois.add(g);
                    });

                } else {
                    throw new NoSuchElementException("ID non trouvé");
                }

            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.internError("Le joueur " + id + " n'a pas été trouvé dans la base de données", ex);
            }
        }
    }

    private void deleteDialog(Joueur j) {
        new DialogDelete("le joueur " + j.getSurnom(), () -> {
            try (Connection con = ConnectionPool.getConnection()) {
                j.deleteFromDB(con);
                NotificationError.info("Le joueur " + j.getSurnom() + " a bien été supprimé");
                this.getUI().ifPresent(ui -> ui.navigate("/"));
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        }).open();
    }

    public JoueurBoard() {
        this.title = new H2("Tableau de bord : Joueur");
        var joueurEditor = new JoueurEditor(); 

        this.add(title);

        Button bDelete = new Button("Supprimer");
        bDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        bDelete.addClickListener(event -> {
            if (this.joueur != null) {
                deleteDialog(this.joueur);
            }
        });

        Button bEdit = new Button("Modifier");
        bEdit.addClickListener(event -> {
            if (this.joueur != null) {
                joueurEditor.open(this.joueur);
            }
        });

        this.add(new HorizontalLayout(bEdit, bDelete));

        this.containerTournois = new VerticalLayout();
        this.containerTournois.setPadding(false);
        this.add(containerTournois);

        this.add(new H3("Statistiques tous tournois confondus"));

        this.gridtotal = createGridTemplate();
        
        gridtotal.setPartNameGenerator(item -> {
            if (" TOTAL CUMULÉ".equals(item.getNomEquipe())) return "total-row";
            return null;
        });

        add(gridtotal);
    }
    private Grid<JoueurStats> createGridTemplate() {
        Grid<JoueurStats> g = new Grid<>();
        g.addColumn(JoueurStats::getNomEquipe).setHeader("Équipe / Cumul");
        g.addColumn(JoueurStats::getNombreDeMatchs).setHeader("Nombre de matchs");
        g.addColumn(JoueurStats::getVictoires).setHeader("Victoires");
        g.addColumn(JoueurStats::getDefaites).setHeader("Défaites");
        g.addColumn(JoueurStats::getNuls).setHeader("Nuls");
        g.addColumn(JoueurStats::getButsInscrits).setHeader("Score +");
        g.addColumn(JoueurStats::getButsEncaisses).setHeader("Score -");
        g.addColumn(JoueurStats::getDifferenceDeButs).setHeader("Différence");
        g.setWidthFull();
        g.setAllRowsVisible(true);
        return g;
    }
}