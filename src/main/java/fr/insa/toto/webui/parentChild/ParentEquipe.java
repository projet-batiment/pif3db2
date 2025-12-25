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
package fr.insa.toto.webui.parentChild;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.webui.equipe.EquipeEditor;
import fr.insa.toto.webui.equipe.EquipeStats;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.utils.PodiumComponent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * @author elio
 */
public abstract class ParentEquipe extends ParentChild<Equipe> {

    public ParentEquipe() {
        super(new EquipeEditor());

        super.addColumn(Equipe::getNom).setHeader("Nom").setSortable(true);
        super.addColumn(Equipe::getNbJoueurs).setHeader("Joueurs").setSortable(true);

        super.addColumn(equipe -> {
            return EquipeStats.findById(equipe.getId())
                    .map(s -> s.getPoints() + " pts")
                    .orElse("0 pts");
        }).setHeader("Points").setSortable(true);

        super.addColumn(equipe -> {
            int rang = EquipeStats.getRangEquipe(equipe.getId());
            return (rang == 1) ? "1er" : rang + "ème";
        }).setHeader("Rang").setSortable(true);

        H2 titreClassement = new H2("Classement actuel");
        this.setAlignSelf(FlexComponent.Alignment.CENTER, titreClassement);
        this.add(titreClassement);
        
        try (Connection con = ConnectionPool.getConnection()) {
            List<Equipe> top3 = EquipeStats.getTop3(con, Optional.empty());
            this.add(new PodiumComponent(top3));
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }
}