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
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author elio
 */
public class MatchsEditor extends Editor {
    private Select<Matchs> select;
    private Matchs matchs;

    // parceque (id=0).equals throws EntiteNonSauvegardee
    private final static Matchs nouveau = new Matchs(-2, 0);

    private TextField ronde;
    private Select<Equipe> equipeA;
    private Select<Equipe> equipeB;
    private TextField scoreA;
    private TextField scoreB;

    // Toujours appeler setMatchs depuis select.setValue !!
    private void setMatchs(Matchs matchs) {
        if (matchs == this.nouveau) {
            this.matchs = new Matchs();
        } else {
            this.matchs = matchs;
        }

        if (matchs == null || matchs == this.nouveau) {
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
            this.scoreA.setValue("" + this.matchs.getScoreA());
            this.scoreB.setValue("" + this.matchs.getScoreA());
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

            list.add(this.nouveau);

            select.setItems(list);
            select.setValue(matchs == null ? this.nouveau : matchs);

            var equipes = Equipe.toutesLesEquipes(con);
            equipeA.setItems(equipes);
            equipeA.setValue(Equipe.findById(con, this.matchs.getIdEquipeA()).orElse(null));
            equipeB.setItems(equipes);
            equipeB.setValue(Equipe.findById(con, this.matchs.getIdEquipeB()).orElse(null));

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

        this.matchs.setIdEquipeA(this.equipeA.getValue().getId());
        this.matchs.setIdEquipeB(this.equipeB.getValue().getId());

        this.matchs.setRonde(Integer.parseInt(ronde.getValue()));
        this.matchs.setScoreA(Integer.valueOf(scoreA.getValue()));
        this.matchs.setScoreB(Integer.valueOf(scoreB.getValue()));

        return (ClasseMiroir)this.matchs;
    }

    public MatchsEditor() {
        super.setHeaderTitle("Apperçu du match");

        super.addSavedCallback(() -> {
            this.select.setValue(this.matchs);
            Notification.show("Le match " + this.matchs.getNom() + " a bien été sauvegardé");
        });
        super.setOpenBoard(() -> {
            if (this.matchs != null) {
                this.getUI().ifPresent(ui -> ui.navigate("match/" + matchs.getId()));
                this.close();
            }
        });

        select = new Select<>();
        select.setItemLabelGenerator(t -> {
            if (t == this.nouveau) {
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
        equipeA.addValueChangeListener(t -> this.matchs.setIdEquipeA(t.getValue().getId()));
        equipeA.setLabel("Équipe A");

        equipeB = new Select<>();
        equipeB.setItemLabelGenerator(Equipe::getNom);
        equipeB.setPlaceholder("Choisir une équipe...");
        equipeB.addValueChangeListener(t -> this.matchs.setIdEquipeB(t.getValue().getId()));
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
