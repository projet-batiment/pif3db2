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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.model.utils.ModifiedState;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author elio
 */
public class MatchsEditor extends EditorOld {
    private Select<Matchs> select;
    private Matchs matchs;

    private TextField ronde;
    private Select<Equipe> equipeA;
    private Select<Equipe> equipeB;
    private TextField scoreA;
    private TextField scoreB;

    // Toujours appeler setMatchs depuis select.setValue !!
    private void setMatchs(Matchs matchs) {
        if (matchs == Matchs.PORCELAINE) {
            this.matchs = new Matchs();
        } else {
            this.matchs = matchs;
        }

        if (matchs == null || matchs == Matchs.PORCELAINE) {
            super.setEnabled(false);

            this.equipeA.setValue(null);
            this.equipeB.setValue(null);
            this.ronde.setValue("");
            this.scoreA.setValue("");
            this.scoreB.setValue("");
        } else {
            super.setEnabled(true);

            this.equipeA.setValue(null);
            this.equipeB.setValue(null);
            this.ronde.setValue("" + this.matchs.getRonde());
            this.scoreA.setValue("" + this.matchs.getScoreEquipeA().score.getScore());
            this.scoreB.setValue("" + this.matchs.getScoreEquipeB().score.getScore());
        }

        if (matchs == null) {
            this.ronde.setEnabled(false);
            this.scoreA.setEnabled(false);
            this.scoreB.setEnabled(false);
        } else {
            this.ronde.setEnabled(true);
            this.scoreA.setEnabled(true);
            this.scoreB.setEnabled(true);
        }
    }

    public void open(Matchs matchs) {
        try (var con = ConnectionPool.getConnection()) {
            var list = Matchs.tousLesMatchs(con);
            list.forEach(e -> {
                try {
                    e.populate(con);
                } catch (SQLException ex) {
                    NotificationError.sql(ex);
                } catch (NoSuchElementException ex) {
                    NotificationError.error("L'un des éléments du tournois " + e.getId() + " n'a pas été trouvé dans la base de données : " + ex.getMessage());
                }
            });

            Notification.show("TODO: ajouter un updateListMatchs ici pour après save");

            list.add(Matchs.PORCELAINE);

            select.setItems(list);
            select.setValue(matchs == null ? Matchs.PORCELAINE : matchs);

            var equipes = Equipe.toutesLesEquipes(con);
            equipeA.setItems(equipes);
            equipeB.setItems(equipes);

            if (matchs == null) {
                select.setValue(Matchs.PORCELAINE);
            } else {
                try {
                    matchs.populate(con);
                } catch (SQLException ex) {
                    NotificationError.sql(ex);
                } catch (NoSuchElementException ex) {
                    NotificationError.error("L'un des éléments du tournois à éditer (" + matchs.getId() + ") n'a pas été trouvé dans la base de données : " + ex.getMessage());
                }
                select.setValue(this.matchs);
                equipeA.setValue(this.matchs.getScoreEquipeA().equipe.getState() == ModifiedState.CREATED ? null : this.matchs.getScoreEquipeA().equipe);
                equipeB.setValue(this.matchs.getScoreEquipeB().equipe.getState() == ModifiedState.CREATED ? null : this.matchs.getScoreEquipeB().equipe);
            }

            super.open();
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    public ClasseMiroir compile() {
        if (this.matchs == null) return null;

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

        this.matchs.getScoreEquipeA().equipe = this.equipeA.getValue();
        this.matchs.getScoreEquipeB().equipe = this.equipeB.getValue();
        this.matchs.getScoreEquipeA().score.setScore(Integer.parseInt(this.scoreA.getValue()));
        this.matchs.getScoreEquipeB().score.setScore(Integer.parseInt(this.scoreB.getValue()));

        this.matchs.setRonde(Integer.parseInt(ronde.getValue()));

        return (ClasseMiroir)this.matchs;
    }

    public MatchsEditor() {
        super.setHeaderTitle("Apperçu du match");

        super.addSavedCallback(() -> {
            this.select.setValue(this.matchs);
            Notification.show("Le match " + this.matchs.getNom() + " a bien été sauvegardé");
        });
        super.setOpenBoardCallback(() -> {
            if (this.matchs != null) {
                this.getUI().ifPresent(ui -> ui.navigate("match/" + matchs.getId()));
                this.close();
            }
        });

        select = new Select<>();
        select.setItemLabelGenerator(t -> {
            if (t == Matchs.PORCELAINE) {
                return "Nouveau match...";
            } else {
                return t.getNom();
            }
        });
        select.setPlaceholder("Choisir un match...");
        select.addValueChangeListener(t -> this.setMatchs(t.getValue()));
        select.setLabel("Match");

        ronde = new TextField();
        ronde.setLabel("Ronde");
        ronde.setAllowedCharPattern("[0-9]");
        ronde.setMaxLength(2);

        equipeA = new Select<>();
        equipeA.setItemLabelGenerator(Equipe::getNom);
        equipeA.setPlaceholder("Choisir une équipe...");
        equipeA.addValueChangeListener(t -> {
            if (this.matchs != null)
                this.matchs.getScoreEquipeA().equipe = t.getValue();
        });
        equipeA.setLabel("Équipe A");

        equipeB = new Select<>();
        equipeB.setItemLabelGenerator(Equipe::getNom);
        equipeB.setPlaceholder("Choisir une équipe...");
        equipeB.addValueChangeListener(t -> {
            if (this.matchs != null)
                this.matchs.getScoreEquipeB().equipe = t.getValue();
        });
        equipeB.setLabel("Équipe B");

        scoreA = new TextField();
        scoreA.setLabel("Score de A");
        scoreA.setAllowedCharPattern("[0-9]");

        scoreB = new TextField();
        scoreB.setLabel("Score de B");
        scoreB.setAllowedCharPattern("[0-9]");

        super.add(new VerticalLayout(
                new HorizontalLayout(select, ronde),
                new HorizontalLayout(equipeA, scoreA),
                new HorizontalLayout(equipeB, scoreB)
        ));
    }
}
