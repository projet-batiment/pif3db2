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
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.webui.EditorOld;
import fr.insa.toto.webui.NotificationError;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public class EquipeEditor extends EditorOld {
    private Select<Equipe> select;
    private Equipe equipe;

    // parceque (id=0).equals throws EntiteNonSauvegardee
    private final static Equipe nouveau = new Equipe(ClasseMiroir.ID_PORCELENE, "Nouveau...");

    private TextField nom;

    // Toujours appeler setEquipe depuis select.setValue !!
    private void setEquipe(Equipe equipe) {
        if (equipe == this.nouveau) {
            this.equipe = new Equipe();
            super.setEnabled(false);
        } else {
            this.equipe = equipe;
            super.setEnabled(true);
        }

        if (equipe == null) {
            this.nom.setValue("");
            this.nom.setEnabled(false);
        } else {
            this.nom.setValue(this.equipe.getNom());
            this.nom.setEnabled(true);
        }
    }

    public void open(Equipe equipe) {
        try (var con = ConnectionPool.getConnection()) {
            var list = Equipe.toutesLesEquipes(con);
            list.add(this.nouveau);

            select.setItems(list);
            select.setValue(equipe == null ? this.nouveau : equipe);

            super.open();
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    public ClasseMiroir compile() {
        if (this.nom.getValue().isBlank()) {
            Notification.show("Il manque le nom de l'équipe");
            return null;
        }

        this.equipe.setNom(nom.getValue());

        return (ClasseMiroir)this.equipe;
    }

    public EquipeEditor() {
        super.setHeaderTitle("Apperçu de l'équipe");

        super.addSavedCallback(() -> {
            this.select.setValue(this.equipe);
            Notification.show("L'équipe " + this.equipe.getNom() + " a bien été sauvegardé");
        });
        super.setOpenBoardCallback(() -> {
            if (this.equipe != null) {
                this.getUI().ifPresent(ui -> ui.navigate("equipe/" + equipe.getId()));
                this.close();
            }
        });

        select = new Select<>();
        select.setItemLabelGenerator(Equipe::getNom);
        select.setPlaceholder("Choisir une équipe...");
        select.addValueChangeListener(t -> this.setEquipe(t.getValue()));
        select.setLabel("Équipe");

        nom = new TextField();
        nom.setLabel("Nom de l'équipe");

        super.add(new VerticalLayout(select, nom));
    }
}
