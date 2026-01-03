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

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Ronde;
import fr.insa.toto.webui.equipe.EquipeEditor;
import fr.insa.toto.webui.equipe.EquipeStats;
import fr.insa.toto.webui.parentChild.ParentEquipe;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.session.InternError;
import fr.insa.toto.webui.session.Session;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 *
 * @author elio
 */
@Route(value = "ronde/equipe", layout = RondeLayout.class)
public class RondeEquipe extends ParentEquipe implements BeforeEnterObserver {
    private int equipeId;
    private Ronde ronde;

    @Override
    protected Optional<Integer> pointsLabel(Equipe equipe) {
        return EquipeStats.getPointsEquipeRonde(equipe.getId(), this.equipeId);
    }

    @Override
    protected Optional<Integer> classementLabel(Equipe equipe) {
        return EquipeStats.getRangEquipeRonde(equipe.getId(), this.equipeId);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Integer id = Session.getId(0);
        this.equipeId = id;

        if (id == null) {
            Session.addErrorMessage("RondeEquipe: pas d'ID de ronde en mémoire");
            event.forwardTo(InternError.class);
        } else {
            try (Connection con = ConnectionPool.getConnection()) {
                this.ronde = Ronde.findById(con, id).get();
                ronde.populate(con);
                ((EquipeEditor)super.editor).setIdTournois(ronde.getIdTournois());

                super.addClassement(EquipeStats.getTop3Ronde(con, id));

                super.initialize(this.ronde.equipes);

            } catch (SQLException ex) {
                NotificationError.sql(ex);
            } catch (NoSuchElementException ex) {
                NotificationError.internError("Le ronde " + id + " n'a pas été trouvé dans la base de données", ex);
            }
        }
    }
}
