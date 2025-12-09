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
import com.vaadin.flow.component.textfield.TextField;
import fr.insa.toto.model.Joueur;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @author elio
 */
public class JoueurEditor extends Editor<Joueur> {
    private TextField surnom;
    private TextField taillecm;
    private TextField categorie;

    @Override
    protected Joueur newObject() {
        return new Joueur();
    }

    protected void setObject() {
        if (this.object instanceof Joueur joueur) {
            this.surnom.setValue(joueur.getSurnom());
            this.surnom.setEnabled(true);
            this.taillecm.setValue("" + joueur.getTaillecm());
            this.taillecm.setEnabled(true);
            this.categorie.setValue(joueur.getCategorie());
            this.categorie.setEnabled(true);
        } else {
            this.surnom.setValue("");
            this.surnom.setEnabled(false);
            this.taillecm.setValue("");
            this.taillecm.setEnabled(false);
            this.categorie.setValue("");
            this.categorie.setEnabled(false);
        }
    }

    @Override
    protected List<Joueur> openObject(Connection con) throws SQLException {
        return Joueur.tousLesJoueurs(con);
    }

    public Joueur compile() {
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

        object.setSurnom(surnom.getValue());
        object.setTaillecm(Integer.parseInt(taillecm.getValue()));
        object.setCategorie(categorie.getValue());

        return this.object;
    }

    @Override
    protected void onSaved() {
        Notification.show("Le joueur " + object.getSurnom()+ " a bien été sauvegardé");
    }

    @Override
    protected String generatedUrl() {
        return "joueur/" + this.object.getId();
    }

    public JoueurEditor() {
        nouveau = new Joueur(Joueur.ID_PORCELAINE, "Nouveau...", "", 0);

        super.setHeaderTitle("Apperçu du joueur");

        super.setSelectItemLabelGenerator(Joueur::getSurnom);
        super.setSelectLabel("Joueur");

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

        super.addChildren(surnom, taillecm, categorie);
    }
}
