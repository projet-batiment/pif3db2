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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Tournois;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author elio
 */
@Route(value = "tournois/specific")
public class TournoisSpecific extends VerticalLayout implements HasUrlParameter<Integer> {

    private Select<Tournois> select;
    private Tournois tournois;
    private List<Tournois> list;

    private TextField nom;
    private TextField nombreRondes;

    @Override
    public void setParameter(BeforeEvent be, @OptionalParameter Integer id) {
        if (id != null) {
            setTournois(id);
        }
    }

    private void setTournois(int id) {
        try {
            var con = ConnectionPool.getConnection();
            Tournois.findById(con, id)
                    .ifPresentOrElse(t -> setTournois(t), () -> Notification.show("Erreur: le tournois id=" + id + "n'existe pas"));

        } catch (SQLException ex) {
            this.add(new Text("Erreur SQL: '" + ex.getMessage() + "'"));
        }
    }

    private void setTournois(Tournois tournois) {
        this.tournois = tournois;
        this.select.setValue(this.tournois);

        updateFields();
    }

    private void updateFields() {
        if (tournois == null) {
            this.nom.setValue("");
            this.nom.setEnabled(false);
            this.nombreRondes.setValue("");
            this.nombreRondes.setEnabled(false);
        } else {
            this.nom.setValue(this.tournois.getNom());
            this.nom.setEnabled(true);
            this.nombreRondes.setValue("" + this.tournois.getNombreRondes());
            this.nombreRondes.setEnabled(true);
        }
    }

    public enum EditionState {
        VIEW,
        MODIFIED,
        NEW,
    }

    public TournoisSpecific() {
        this.tournois = null;
        this.list = null;

        this.add(new H2("Tournois"));

        try {
            var con = ConnectionPool.getConnection();
            this.list = Tournois.tousLesTournois(con);

            select = new Select<>();
            select.setItemLabelGenerator(Tournois::getNom);
            select.setItems(this.list);
            select.setPlaceholder("Choisir un tournois...");
            select.setValue(tournois);
            select.addValueChangeListener(t -> this.setTournois(t.getValue()));

            nom = new TextField();
            nom.setLabel("Nom du tournois");
            nombreRondes = new TextField();
            nombreRondes.setLabel("Nombre de rondes");
            nombreRondes.setAllowedCharPattern("[0-9]");
            nombreRondes.setMaxLength(2);

            updateFields();

            this.add(select, nom, nombreRondes);

        } catch (SQLException ex) {
            this.add(new Text("Erreur: '" + ex.getMessage() + "'"));
        }
    }
}
