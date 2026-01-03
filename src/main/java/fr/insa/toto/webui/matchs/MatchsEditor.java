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
package fr.insa.toto.webui.matchs;

import com.vaadin.flow.component.littemplate.IllegalAttributeException;
import fr.insa.toto.webui.utils.Editor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.model.Ronde;
import fr.insa.toto.model.utils.ModifiedState;
import fr.insa.toto.webui.utils.NotificationError;
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
    private Select<Ronde> ronde;
    private Select<Equipe> equipeA;
    private Select<Equipe> equipeB;
    private TextField scoreA;
    private TextField scoreB;

    private Equipe currentEquipe = null;
    public void setCurrentEquipe(Equipe currentEquipe) {
        this.currentEquipe = currentEquipe;
    }

    private int idTournois = Ronde.ID_UNSAVED;
    public void setIdTournois(int idTournois) {
        this.idTournois = idTournois;
    }

    @Override
    protected Matchs newObject() {
        return new Matchs();
    }

    protected void setObject() {
        if (this.object == null || this.object == Matchs.PORCELAINE) {
            super.setEnabled(false);

            this.equipeA.setValue(currentEquipe);
            this.equipeB.setValue(null);
            this.ronde.setValue(null);
            this.scoreA.setValue("");
            this.scoreB.setValue("");
        } else {
            super.setEnabled(true);

            this.equipeA.setValue(this.object.getScoreEquipeA().equipe == null ? currentEquipe : this.object.getScoreEquipeA().equipe);
            this.equipeB.setValue(this.object.getScoreEquipeB().equipe == null ? currentEquipe : this.object.getScoreEquipeB().equipe);
            this.ronde.setValue(this.object.getRonde());
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

        var equipes = Equipe.findByIdTournois(con, this.idTournois);
        equipeA.setItems(equipes);
        equipeB.setItems(equipes);

        var rondes = Ronde.findByIdTournois(con, this.idTournois);
        for (Ronde each: rondes) {
            each.populate(con);
        }
        ronde.setItems(rondes);

        return list;
    }

    public Matchs compile() {
        if (this.object == null) return null;

        if (this.equipeA.getValue() == null) {
            NotificationError.userError("Il manque l'équipe A");
            return null;
        }
        if (this.equipeB.getValue() == null) {
            NotificationError.userError("Il manque l'équipe B");
            return null;
        }

        if (this.ronde.getValue() == null) {
            NotificationError.userError("Il manque la ronde");
            return null;
        }

        if (this.scoreA.getValue().isBlank()) {
            NotificationError.userError("Il manque le score de l'équipe A");
            return null;
        }
        if (this.scoreA.getValue().isBlank()) {
            NotificationError.userError("Il manque le score de l'équipe B");
            return null;
        }

        this.object.getScoreEquipeA().equipe = this.equipeA.getValue();
        this.object.getScoreEquipeB().equipe = this.equipeB.getValue();
        this.object.getScoreEquipeA().score.setScore(Integer.parseInt(this.scoreA.getValue()));
        this.object.getScoreEquipeB().score.setScore(Integer.parseInt(this.scoreB.getValue()));

        // inutile car déjà effectué par (Select)ronde.addValueChangeListener
        // this.object.setIdRonde(this.ronde.getValue().getId());

        try (Connection con = ConnectionPool.getConnection()) {
            this.object.checkSavable(con);

        } catch (NoSuchElementException ex) {
            NotificationError.internError(ex);
            return null;

        } catch (IllegalAttributeException ex) {
            NotificationError.userError(ex.getLocalizedMessage());
            return null;

        } catch (SQLException ex) {
            NotificationError.sql(ex);
            return null;
        }

        return this.object;
    }

    @Override
    protected void onSaved() {
        NotificationError.info("Le match " + object.getName()+ " a bien été sauvegardé");
    }

    @Override
    protected String generatedUrl() {
        return "match";
    }

    public MatchsEditor() {
        nouveau = Matchs.PORCELAINE;

        super.setHeaderTitle("Aperçu du match");

        super.setSelectItemLabelGenerator(t -> {
            if (t == Matchs.PORCELAINE) {
                return "Nouveau match...";
            } else {
                return t.getName();
            }
        });
        super.setSelectLabel("Matchs");

        ronde = new Select<>();
        ronde.setItemLabelGenerator(Ronde::getName);
        ronde.setPlaceholder("Choisir une ronde...");
        ronde.addValueChangeListener(t -> {
            if (this.object != null && ronde.getValue() != null)
                this.object.setIdRonde(t.getValue().getId());
        });
        ronde.setLabel("Ronde");

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
