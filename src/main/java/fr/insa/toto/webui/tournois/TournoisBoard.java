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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author elio
 */
@Route(value = "tournois/:tournoisId([0-9]*)", layout = TournoisLayout.class)
public class TournoisBoard extends VerticalLayout implements BeforeEnterObserver {
    private Tournois tournois;
    private H2 title;
    private VerticalLayout NomTournoi;
    private VerticalLayout NombreRondes;
    private Text nom;
    private Text nombreRondes;
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        int id = Integer.parseInt(event.getRouteParameters().get("tournoisId").get());
        try (Connection con = ConnectionPool.getConnection()) {
            this.tournois = Tournois.findById(con, id).get();

            this.nom.setText(tournois.getNom());
            this.nombreRondes.setText(String.valueOf(tournois.getNombreRondes()));
            this.NomTournoi.add(nom);
            this.NombreRondes.add(nombreRondes);
            
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        } catch (NoSuchElementException ex) {
            NotificationError.error("Le tournois " + id + " n'a pas été trouvé dans la base de données : " + ex.getMessage());
        }
    }
    
    public TournoisBoard() {
        this.title = new H2("Tableau de bord : tournois");
        nom = new Text("temp");
        nombreRondes = new Text("temp");
        this.NomTournoi = new VerticalLayout(new H2("Nom du tournoi"));
        this.NombreRondes = new VerticalLayout(new H2("Nombre de rondes"));
        this.add(title);
        this.add(NomTournoi);
        this.add(NombreRondes);
    }
}
