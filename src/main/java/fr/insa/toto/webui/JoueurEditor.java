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
import fr.insa.toto.model.Joueur;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.model.Tournois;
import java.sql.Connection;
import java.sql.SQLException;
import org.aspectj.weaver.ast.Not;

/**
 *
 * @author elio
 */
public class JoueurEditor extends Editor {
    private Select<Joueur> select;
    private Joueur joueur;

    // parceque (id=0).equals throws EntiteNonSauvegardee
    private final static Joueur nouveau = new Joueur(-2, "Nouveau...", "", 0);

    private TextField surnom;
    private TextField taillecm;
    private TextField categorie;

    // Toujours appeler setJoueur depuis select.setValue !!
    private void setJoueur(Joueur joueur) {
        if (joueur == this.nouveau) {
            this.joueur = new Joueur();
            super.setEnabled(false);
        } else {
            this.joueur = joueur;
            super.setEnabled(true);
        }

        if (joueur == null) {
            this.surnom.setValue("");
            this.surnom.setEnabled(false);
            this.taillecm.setValue("");
            this.taillecm.setEnabled(false);
            this.categorie.setValue("");
            this.categorie.setEnabled(false);
        } else {
            this.surnom.setValue(this.joueur.getSurnom());
            this.surnom.setEnabled(true);
            this.taillecm.setValue("" + this.joueur.getTaillecm());
            this.taillecm.setEnabled(true);
            this.categorie.setValue(this.joueur.getCategorie());
            this.categorie.setEnabled(true);
        }
    }

    public void open(Joueur joueur) {
        try (var con = ConnectionPool.getConnection()) {
            var list = Joueur.tousLesJoueurs(con);
            list.add(this.nouveau);

            select.setItems(list);
            select.setValue(joueur == null ? this.nouveau : joueur);

            super.open();
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    public ClasseMiroir compile() {
        if (this.surnom.getValue().isBlank()) {
            Notification.show("Il manque le surnom du joueur");
            return null;
        }
        if (this.taillecm.getValue().isBlank()) {
            Notification.show("Il manque la taille du joueur");
            return null;
        }
        if (this.categorie.getValue().isBlank()) {
            Notification.show("Il manque la catégorie du joueur");
            return null;
        }

        this.joueur.setSurnom(surnom.getValue());
        this.joueur.setTaillecm(Integer.parseInt(taillecm.getValue()));
        this.joueur.setCategorie(categorie.getValue());

        return (ClasseMiroir)this.joueur;
    }

    public JoueurEditor() {
        super.setHeaderTitle("Apperçu du joueur");

        super.addSavedCallback(() -> {
            this.select.setValue(this.joueur);
            Notification.show("Le joueur " + this.joueur.getSurnom()+ " a bien été sauvegardé");
        });
        super.setOpenBoard(() -> {
            if (this.joueur != null) {
                this.getUI().ifPresent(ui -> ui.navigate("tournois/" + joueur.getId() + "/joueur"));
                this.close();
            }
        });

        select = new Select<>();
        select.setItemLabelGenerator(Joueur::getSurnom);
        select.setPlaceholder("Choisir un joueur...");
        select.addValueChangeListener(t -> this.setJoueur(t.getValue()));
        select.setLabel("Joueur");

        surnom = new TextField();
        surnom.setLabel("Surnom du joueur");

        taillecm = new TextField();
        taillecm.setLabel("Taille (cm)");
        taillecm.setAllowedCharPattern("[0-9]");
        taillecm.setMaxLength(3);

        categorie = new TextField();
        categorie.setLabel("Catégorie");
//        categorie.setAllowedCharPattern("[0-9]");
        categorie.setMaxLength(1);

        super.add(new VerticalLayout(select, surnom, taillecm, categorie));
    }
}
