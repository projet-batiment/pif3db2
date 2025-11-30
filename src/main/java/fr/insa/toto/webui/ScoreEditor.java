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
package fr.insa.toto.webui;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Score;
import java.sql.SQLException;


/**
 *
 * @author pmarchal01
 */
public class ScoreEditor extends Editor {
    private Select<Score> select;
    private Score scores;
    
    private final static Score nouveau = new Score(-2, 0, -2, -2);
    
    private TextField score;
    private TextField idEquipe;
    private TextField idMatch;
    
    private void setScores(Score scores) {
        if (scores == this.nouveau) {
            this.scores = new Score();
            super.setEnabled(false);
        } else {
            this.scores = scores;
            super.setEnabled(true);
        }

        if (scores == null) {
            this.score.setValue("");
            this.score.setEnabled(false);
            this.idEquipe.setValue("");
            this.idEquipe.setEnabled(false);
            this.idMatch.setValue("");
            this.idMatch.setEnabled(false);
        } else {
            this.score.setValue("" + this.scores.getScore());
            this.score.setEnabled(true);
            this.idEquipe.setValue("" + this.scores.getIdEquipe());
            this.idEquipe.setEnabled(true);
            this.idMatch.setValue("" + this.scores.getIdMatch());
            this.idMatch.setEnabled(true);
        }
    }
    
    public void open(Score scores){
        try (var con = ConnectionPool.getConnection()) {
            var list = Score.tousLesScores(con);
            list.add(this.nouveau);

            select.setItems(list);
            select.setValue(scores == null ? this.nouveau : scores);

            super.open();
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }
    
    public ClasseMiroir compile() {
        if (this.score.getValue().isBlank()) {
            Notification.show("Il manque le score");
            return null;
        }
        if (this.idEquipe.getValue().isBlank()) {
            Notification.show("Il manque l'id de l'équipe");
            return null;
        }
        if (this.idMatch.getValue().isBlank()) {
            Notification.show("Il manque l'id du match");
            return null;
        }

        this.scores.setScore(Integer.parseInt(score.getValue()));
        this.scores.setIdEquipe(Integer.parseInt(idEquipe.getValue()));
        this.scores.setIdMatch(Integer.parseInt(idMatch.getValue()));

        return (ClasseMiroir)this.scores;
    }
    
    public ScoreEditor() {
        super.setHeaderTitle("Aperçu du score");

        super.addSavedCallback(() -> {
            this.select.setValue(this.scores);
            Notification.show("Le score " + this.scores.getScore()+ " a bien été sauvegardé");
        });
        super.setOpenBoard(() -> {
            if (this.scores != null) {
                this.getUI().ifPresent(ui -> ui.navigate("scores/" + scores.getId()));
                this.close();
            }
        });

        select = new Select<>();
        select.setItemLabelGenerator(scoress -> Integer.toString(scoress.getScore()));
        select.setPlaceholder("Choisir un score...");
        select.addValueChangeListener(t -> this.setScores(t.getValue()));
        select.setLabel("Scores");

        score = new TextField();
        score.setLabel("Score de l'équipe");

        idEquipe = new TextField();
        idEquipe.setLabel("Id de l'équipe");
        idEquipe.setAllowedCharPattern("[0-9]");
        idEquipe.setMaxLength(2);
        
        idMatch = new TextField();
        idMatch.setLabel("Id du match");
        idMatch.setAllowedCharPattern("[0-9]");
        idMatch.setMaxLength(2);

        super.add(new VerticalLayout(select, score, idEquipe, idMatch));
    }
}
