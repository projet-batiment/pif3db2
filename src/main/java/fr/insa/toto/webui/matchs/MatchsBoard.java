/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.toto.webui.matchs;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;


/**
 *
 * @author pmarchal01
 */
@Route(value = "match", layout = MatchsLayout.class)
public class MatchsBoard extends VerticalLayout implements BeforeEnterObserver {
    private Matchs matchs;
    private H2 title;
    private VerticalLayout NomMatch;
    private VerticalLayout Ronde;
    private Text nom;
    private Text ronde;
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        if (id == null) {
            Session.addErrorMessage("MatchsBoard: pas d'ID de match en mémoire");
            event.forwardTo(InternError.class);
        } else {
            try (Connection con = ConnectionPool.getConnection()) {
                this.matchs = Matchs.findById(con, id).get();
                try {
                    matchs.populate(con);
                } catch (NoSuchElementException ex) {
                    NotificationError.error("L'un des éléments du match n'a pas été trouvé : " + ex.getLocalizedMessage());
                }

                this.nom.setText(matchs.getName());
                this.ronde.setText(String.valueOf(matchs.getRonde().getNumero()));
                this.NomMatch.add(nom);
                this.Ronde.add(ronde);
                
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.error("Le match " + id + " n'a pas été trouvé dans la base de données : " + ex.getMessage());
            }
        }
    }
    
     public MatchsBoard() {
        this.title = new H2("Tableau de bord : matchs");
        nom = new Text("temp");
        ronde = new Text("temp");
        this.NomMatch = new VerticalLayout(new H2("Nom du match"));
        this.Ronde = new VerticalLayout(new H2("Numéro de la ronde"));
        this.add(title);
        this.add(NomMatch);
        this.add(Ronde);
    }
}
