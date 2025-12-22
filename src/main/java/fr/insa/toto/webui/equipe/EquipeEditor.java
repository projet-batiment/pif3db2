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
package fr.insa.toto.webui.equipe;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.webui.utils.Editor;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @author elio
 */
public class EquipeEditor extends Editor<Equipe> {
    private TextField nom;

    @Override
    protected Equipe newObject() {
        return new Equipe();
    }

    protected void setObject() {
        if (this.object instanceof Equipe equipe) {
            this.nom.setValue(equipe.getNom());
            this.nom.setEnabled(true);
        } else {
            this.nom.setValue("");
            this.nom.setEnabled(false);
        }
    }

    @Override
    protected List<Equipe> openObject(Connection con) throws SQLException {
        return Equipe.toutesLesEquipes(con);
    }

    public Equipe compile() {
        if (this.nom.getValue().isBlank()) {
            Notification.show("Il manque le nom de l'équipe");
            return null;
        }

        object.setNom(nom.getValue());

        return this.object;
    }

    @Override
    protected void onSaved() {
        Notification.show("L'équipe " + object.getNom()+ " a bien été sauvegardée");
    }

    @Override
    protected String generatedUrl() {
        return "equipe/" + this.object.getId();
    }

    public EquipeEditor() {
        nouveau = new Equipe(Equipe.ID_PORCELAINE, "Nouveau...");

        super.setHeaderTitle("Apperçu de l'équipe");

        super.setSelectItemLabelGenerator(Equipe::getNom);
        super.setSelectLabel("Équipe");

        nom = new TextField();
        nom.setLabel("Nom de l'équipe");

        super.addChildren(nom);
    }
}
