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
package fr.insa.toto.webui.ronde;

import com.vaadin.flow.component.checkbox.Checkbox;
import fr.insa.toto.webui.joueur.*;
import fr.insa.toto.webui.utils.Editor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.model.Ronde;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @author elio
 */
public class RondeEditor extends Editor<Ronde> {
    private TextField numero;
    private Checkbox enCours;

    @Override
    protected Ronde newObject() {
        return new Ronde();
    }

    protected void setObject() {
        if (this.object instanceof Ronde ronde) {
            this.numero.setValue("" + ronde.getNumero());
            this.numero.setEnabled(true);
            this.enCours.setValue(ronde.isEnCours());
            this.enCours.setEnabled(true);
        } else {
            this.numero.setValue("");
            this.numero.setEnabled(false);
            this.enCours.setValue(false);
            this.enCours.setEnabled(false);
        }
    }

    @Override
    protected List<Ronde> openObject(Connection con) throws SQLException {
        return Ronde.toutesLesRondes(con);
    }

    public Ronde compile() {
        if (this.numero.getValue().isBlank()) {
            Notification.show("Il manque le surnom du ronde");
            return null;
        }

        object.setNumero(Integer.parseInt(numero.getValue()));
        object.setEnCours(enCours.getValue());

        return this.object;
    }

    @Override
    protected void onSaved() {
        Notification.show("La ronde " + object.getNumero()+ " a bien été sauvegardée");
    }

    @Override
    protected String generatedUrl() {
        return "ronde/";
    }

    public RondeEditor() {
        nouveau = new Ronde(Ronde.ID_PORCELAINE, 0, 0, false);

        super.setHeaderTitle("Apperçu du ronde");

        super.setSelectItemLabelGenerator(each -> each.getId() == Ronde.ID_PORCELAINE ? "Nouvelle..." : ""+each.getNumero());
        super.setSelectLabel("Ronde");

        numero = new TextField();
        numero.setLabel("Ronde n°");
        numero.setAllowedCharPattern("[0-9]");
        numero.setMaxLength(3);

        enCours = new Checkbox();
        enCours.setLabel("En cours");

        super.addChildren(numero, enCours);
    }
}
