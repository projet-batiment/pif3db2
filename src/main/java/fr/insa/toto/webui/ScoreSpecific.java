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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Score;
import java.sql.SQLException;

/**
 *
 * @author pmarchal01
 */

@Route(value = "scores/specific")
public class ScoreSpecific extends VerticalLayout implements HasUrlParameter<Integer> {
    private Select<Score> select;
    private Score scores;

    private TextField score;
    private TextField idEquipe;
    private TextField idMatch;

    private final static Score nouveau = new Score(-2, 5, -2, -2);

    @Override
    public void setParameter(BeforeEvent be, @OptionalParameter Integer id) {
        if (id != null) {
            setScores(id);
        }
    }

    private void setScores(int id) {
        try {
            var con = ConnectionPool.getConnection();
            Score.findById(con, id)
                    .ifPresentOrElse(t -> setScores(t), () -> Notification.show("Erreur: le score id=" + id + "n'existe pas"));

        } catch (SQLException ex) {
            this.add(new Text("Erreur SQL: '" + ex.getMessage() + "'"));
        }
    }

    private void setScores(Score scores) {
        if (scores == this.nouveau) {
            this.scores = new Score();
        } else {
            this.scores = scores;
        }

        this.select.setValue(scores);

        updateFields();
    }

    private void updateFields() {
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

    private void updateSelectList() throws SQLException {
        var con = ConnectionPool.getConnection();

        var list = Score.tousLesScores(con);
        list.add(this.nouveau);

        select.setItems(list);
    }

    private void save() {
        try {
            var con = ConnectionPool.getConnection();

            this.scores.setScore(Integer.parseInt(score.getValue()));
            this.scores.setIdMatch(Integer.parseInt(idMatch.getValue()));
            this.scores.setIdEquipe(Integer.parseInt(idEquipe.getValue()));

            int id = this.scores.updateOrNew(con);

            if (this.select.getValue() == this.nouveau) {
                this.updateSelectList();
                this.select.setValue(new Score(id));
            }

            Notification.show("Score " + scores.getScore() + " sauvegardé");
        } catch (SQLException ex) {
            Notification.show("Erreur: '" + ex.getMessage() + "'");
        }
    }

    public ScoreSpecific() {
        this.scores = null;

        this.add(new H2("Tournois"));

//        try {
//            select = new Select<>();
//            select.setItemLabelGenerator(Score::getScore);
//            select.setPlaceholder("Choisir un score...");
//            select.setValue(scores);
//            select.addValueChangeListener(t -> this.setScores(t.getValue()));
//            select.setLabel("Tournois");
//
//            updateSelectList();
//
//            nom = new TextField();
//            nom.setLabel("Nom du tournois");
//            nombreRondes = new TextField();
//            nombreRondes.setLabel("Nombre de rondes");
//            nombreRondes.setAllowedCharPattern("[0-9]");
//            nombreRondes.setMaxLength(2);
//
//            var apply = new Button("Appliquer");
//            apply.addClickListener(t -> save());
//
//            var create = new Button("Nouveau");
//            create.addClickListener(t -> this.setTournois(this.nouveau));
//
//            updateFields();
//
//            this.add(new HorizontalLayout(select, create), nom, nombreRondes, apply);
//
//        } catch (SQLException ex) {
//            Notification.show("Erreur: '" + ex.getMessage() + "'");
//        }
    }
}
