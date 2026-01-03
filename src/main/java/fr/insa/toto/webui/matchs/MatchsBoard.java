/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.toto.webui.matchs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.model.Ronde;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.tournois.TournoisEditor;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.utils.Utils;
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
    private Ronde ronde;
    private Tournois tournois;

    private MatchsEditor matchsEditor;

    private final VerticalLayout infoSection = new VerticalLayout();
    
    private final Span nomText = new Span();
    private final Span rondeText = new Span();
    private final Span tournoisText = new Span();

    private final Button buttonEdit = new Button();
    private final Button buttonTournois = new Button("Afficher");

    private void updateContents(Connection con) throws SQLException {
        matchs.populate(con);

        try {
            ronde = Ronde.findById(con, matchs.getIdRonde()).get();
            ronde.populate(con);
            rondeText.setText("Nom de la ronde : " + ronde.getName());

            this.matchsEditor.setIdTournois(ronde.getIdTournois());

            tournois = Tournois.findById(con, ronde.getIdTournois()).get();
            tournoisText.setText("Nom du tournoi : " + tournois.getName());
        } catch (NoSuchElementException ex) {
            NotificationError.internError(ex);
        }

        nomText.setText("Nom de la ronde : " + ronde.getName());
        rondeText.setText("Nombre de matchs : " + ronde.getNbMatchs(con));
    }
    
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
                    NotificationError.internError("L'un des éléments du match " + matchs.getId() + " n'a pas été trouvé", ex);
                }

                if (!infoSection.getChildren().anyMatch(c -> c == nomText)) {
                    buttonEdit.addClickListener(e -> {
                        this.matchsEditor.open(matchs);

                        this.matchsEditor.setOnSavedCallback(t -> {
                            try {
                                this.updateContents(con);
                            } catch (SQLException ex) {
                                NotificationError.sql(ex);
                            }
                        });
                    });
                    buttonEdit.setText(Session.isAdmin() ? "Éditer" : "Afficher");
                    infoSection.add(buttonEdit, nomText, rondeText);

                    buttonTournois.addClickListener(e -> {
                        var editor = new TournoisEditor();
                        editor.open(tournois);

                        editor.setOnSavedCallback(t -> {
                            try {
                                this.updateContents(con);
                            } catch (SQLException ex) {
                                NotificationError.sql(ex);
                            }
                        });
                    });
                    infoSection.add(new HorizontalLayout(tournoisText, buttonTournois));
                }

                this.matchsEditor = new MatchsEditor();
                this.updateContents(con);

                
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.internError("Le match " + id + " n'a pas été trouvé dans la base de données", ex);
            }
        }
    }
    
     public MatchsBoard() {
        this.setSpacing(true);
        this.setPadding(true);
        
        infoSection.add(new H2("Match"));
        
        this.add(infoSection);
    }
}
