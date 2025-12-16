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

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.model.utils.ModifiedState;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @author elio
 */
public class MatchsEditor extends Editor<Matchs> {
    private TextField ronde;
    private Select<Equipe> equipeA;
    private Select<Equipe> equipeB;
    private TextField scoreA;
    private TextField scoreB;

    @Override
    protected Matchs newObject() {
        return new Matchs();
    }

    protected void setObject() {
        if (this.object == null || this.object == Matchs.PORCELAINE) {
            super.setEnabled(false);

            this.equipeA.setValue(null);
            this.equipeB.setValue(null);
            this.ronde.setValue("");
            this.scoreA.setValue("");
            this.scoreB.setValue("");
        } else {
            super.setEnabled(true);

            this.equipeA.setValue(this.object.getScoreEquipeA().equipe.getState() == ModifiedState.CREATED ? null : this.object.getScoreEquipeA().equipe);
            this.equipeB.setValue(this.object.getScoreEquipeB().equipe.getState() == ModifiedState.CREATED ? null : this.object.getScoreEquipeB().equipe);
            this.ronde.setValue("" + this.object.getRonde());
            this.scoreA.setValue("" + this.object.getScoreEquipeA().score.getScore());
            this.scoreB.setValue("" + this.object.getScoreEquipeB().score.getScore());
        }

        if (this.object == null) {
            this.ronde.setEnabled(false);
            this.equipeA.setEnabled(false);
            this.equipeB.setEnabled(false);
            this.scoreA.setEnabled(false);
            this.scoreB.setEnabled(false);
        } else {
            this.ronde.setEnabled(true);
            this.equipeA.setEnabled(true);
            this.equipeB.setEnabled(true);
            this.scoreA.setEnabled(true);
            this.scoreB.setEnabled(true);
        }
    }

    @Override
    protected List<Matchs> openObject(Connection con) throws SQLException {
        var list = Matchs.tousLesMatchs(con);
        list.forEach(e -> {
            try {
                e.populate(con);
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.error("L'un des éléments du matchs " + e.getId() + " n'a pas été trouvé dans la base de données : " + ex.getMessage());
            }
        });

        Notification.show("TODO: ajouter un updateListMatchs ici pour après save");

        var equipes = Equipe.toutesLesEquipes(con);
        equipeA.setItems(equipes);
        equipeB.setItems(equipes);

        return list;
    }

    public Matchs compile() {
        if (this.object == null) return null;

        if (this.equipeA.getValue() == null) {
            Notification.show("Il manque l'équipe A");
            return null;
        }
        if (this.equipeB.getValue() == null) {
            Notification.show("Il manque l'équipe B");
            return null;
        }

        if (this.scoreA.getValue().isBlank()) {
            Notification.show("Il manque le score de l'équipe A");
            return null;
        }
        if (this.scoreA.getValue().isBlank()) {
            Notification.show("Il manque le score de l'équipe B");
            return null;
        }

        this.object.getScoreEquipeA().equipe = this.equipeA.getValue();
        this.object.getScoreEquipeB().equipe = this.equipeB.getValue();
        this.object.getScoreEquipeA().score.setScore(Integer.parseInt(this.scoreA.getValue()));
        this.object.getScoreEquipeB().score.setScore(Integer.parseInt(this.scoreB.getValue()));

        this.object.setRonde(Integer.parseInt(ronde.getValue()));

        return this.object;
    }

    @Override
    protected void onSaved() {
        Notification.show("Le match " + object.getName()+ " a bien été sauvegardé");
    }

    @Override
    protected String generatedUrl() {
        return "match/" + this.object.getId();
    }

    public MatchsEditor() {
        nouveau = Matchs.PORCELAINE;

        super.setHeaderTitle("Apperçu du match");

        super.setSelectItemLabelGenerator(t -> {
            if (t == Matchs.PORCELAINE) {
                return "Nouveau match...";
            } else {
                return t.getName();
            }
        });
        super.setSelectLabel("Matchs");

        ronde = new TextField();
        ronde.setLabel("Ronde");
        ronde.setAllowedCharPattern("[0-9]");
        ronde.setMaxLength(2);

        equipeA = new Select<>();
        equipeA.setItemLabelGenerator(Equipe::getNom);
        equipeA.setPlaceholder("Choisir une équipe...");
        equipeA.addValueChangeListener(t -> {
            if (this.object != null)
                this.object.getScoreEquipeA().equipe = t.getValue();
        });
        equipeA.setLabel("Équipe A");

        equipeB = new Select<>();
        equipeB.setItemLabelGenerator(Equipe::getNom);
        equipeB.setPlaceholder("Choisir une équipe...");
        equipeB.addValueChangeListener(t -> {
            if (this.object != null)
                this.object.getScoreEquipeB().equipe = t.getValue();
        });
        equipeB.setLabel("Équipe B");

        scoreA = new TextField();
        scoreA.setLabel("Score de A");
        scoreA.setAllowedCharPattern("[0-9]");

        scoreB = new TextField();
        scoreB.setLabel("Score de B");
        scoreB.setAllowedCharPattern("[0-9]");

        super.addChildren(
                ronde,
                new HorizontalLayout(equipeA, scoreA),
                new HorizontalLayout(equipeB, scoreB)
        );
    }
}
