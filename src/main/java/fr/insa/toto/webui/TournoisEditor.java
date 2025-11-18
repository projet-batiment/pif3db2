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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.model.Tournois;
import java.sql.Connection;
import java.sql.SQLException;
import org.aspectj.weaver.ast.Not;

/**
 *
 * @author elio
 */
public class TournoisEditor extends Editor {
    private Select<Tournois> select;
    private Tournois tournois;

    // parceque (id=0).equals throws EntiteNonSauvegardee
    private final static Tournois nouveau = new Tournois(-2, "Nouveau...", 0);

    private TextField nom;
    private TextField nombreRondes;

    // Toujours appeler setTournois depuis select.setValue !!
    private void setTournois(Tournois tournois) {
        if (tournois == this.nouveau) {
            this.tournois = new Tournois();
            super.setEnabled(false);
        } else {
            this.tournois = tournois;
            super.setEnabled(true);
        }

        if (tournois == null) {
            this.nom.setValue("");
            this.nom.setEnabled(false);
            this.nombreRondes.setValue("");
        } else {
            this.nom.setValue(this.tournois.getNom());
            this.nom.setEnabled(true);
            this.nombreRondes.setValue("" + this.tournois.getNombreRondes());
            this.nombreRondes.setEnabled(true);
        }
    }

    public void open(Tournois tournois) {
        try {
            var con = ConnectionPool.getConnection();

            var list = Tournois.tousLesTournois(con);
            list.add(this.nouveau);

            select.setItems(list);
            select.setValue(tournois == null ? this.nouveau : tournois);

            super.open();
        } catch (SQLException ex) {
            NotificationError.error(ex.getMessage());
        }
    }

    public ClasseMiroir compile() {
        if (this.nom.getValue().isBlank()) {
            Notification.show("Il manque le nom du tournois");
            return null;
        }
        if (this.nombreRondes.getValue().isBlank()) {
            Notification.show("Il manque le nombre de rondes");
            return null;
        }

        this.tournois.setNom(nom.getValue());
        this.tournois.setNombreRondes(Integer.parseInt(nombreRondes.getValue()));

        return (ClasseMiroir)this.tournois;
    }

    public TournoisEditor() {
        super.setHeaderTitle("Apperçu du tournois");

        super.addSavedCallback(() -> {
            this.select.setValue(this.tournois);
            Notification.show("Le tournois " + this.tournois.getNom() + " a bien été sauvegardé");
        });
        super.setOpenBoard(() -> {
            if (this.tournois != null) {
                this.getUI().ifPresent(ui -> ui.navigate("tournois/" + tournois.getId() + "/match"));
                this.close();
            }
        });

        select = new Select<>();
        select.setItemLabelGenerator(Tournois::getNom);
        select.setPlaceholder("Choisir un tournois...");
        select.addValueChangeListener(t -> this.setTournois(t.getValue()));
        select.setLabel("Tournois");

        nom = new TextField();
        nom.setLabel("Nom du tournois");

        nombreRondes = new TextField();
        nombreRondes.setLabel("Nombre de rondes");
        nombreRondes.setAllowedCharPattern("[0-9]");
        nombreRondes.setMaxLength(2);

        super.add(new VerticalLayout(select, nom, nombreRondes));
    }
}
