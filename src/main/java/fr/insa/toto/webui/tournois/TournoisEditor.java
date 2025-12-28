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
package fr.insa.toto.webui.tournois;

import com.vaadin.flow.component.textfield.TextField;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.utils.Editor;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author elio
 */
public class TournoisEditor extends Editor<Tournois> {
    private Consumer<Tournois> onSavedCallback;

    public void setOnSavedCallback(Consumer<Tournois> onSavedCallback) {
        this.onSavedCallback = onSavedCallback;
    }

    private TextField nom;
    private TextField nombreRondes;

    @Override
    protected Tournois newObject() {
        return new Tournois();
    }

    protected void setObject() {
        if (this.object instanceof Tournois tournois) {
            this.nom.setValue(tournois.getName());
            this.nom.setEnabled(true);
            this.nombreRondes.setValue("" + tournois.getNombreRondes());
            this.nombreRondes.setEnabled(true);
        } else {
            this.nom.setValue("");
            this.nom.setEnabled(false);
            this.nombreRondes.setValue("");
            this.nombreRondes.setEnabled(false);
        }
    }

    @Override
    protected List<Tournois> openObject(Connection con) throws SQLException {
        return Tournois.tousLesTournois(con);
    }

    public Tournois compile() {
        if (this.nom.getValue().isBlank()) {
            NotificationError.userError("Il manque le nom du tournois");
            return null;
        }
        if (this.nombreRondes.getValue().isBlank()) {
            NotificationError.userError("Il manque le nombre de rondes");
            return null;
        }

        object.setName(nom.getValue());
        object.setNombreRondes(Integer.parseInt(nombreRondes.getValue()));

        return this.object;
    }

    @Override
    protected void onSaved() {
        NotificationError.info("Le tournoi " + object.getName()+ " a bien été sauvegardé");
    }

    @Override
    protected String generatedUrl() {
        return "tournois";
    }

    public TournoisEditor() {
        nouveau = new Tournois(Tournois.ID_PORCELAINE, "Nouveau...", 0);

        super.setHeaderTitle("Aperçu du tournois");

        super.setSelectItemLabelGenerator(Tournois::getName);
        super.setSelectLabel("Tournois");

        nom = new TextField();
        nom.setLabel("Nom du tournois");

        nombreRondes = new TextField();
        nombreRondes.setLabel("Nombre de rondes");
        nombreRondes.setAllowedCharPattern("[0-9]");
        nombreRondes.setMaxLength(2);

        super.addChildren(nom, nombreRondes);
    }
}
