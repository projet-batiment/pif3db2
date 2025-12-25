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
package fr.insa.toto.webui.joueur;

import fr.insa.toto.webui.tournois.*;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.model.Tournois;
import fr.insa.toto.webui.parentChild.ParentJoueur;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.parentChild.ParentMatchs;
import fr.insa.toto.webui.parentChild.ParentTournois;
import fr.insa.toto.webui.utils.Layout;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author elio
 */
@Route(value = "joueur/list", layout = Layout.Default.class)
public class JoueurList extends ParentJoueur implements BeforeEnterObserver {
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.initialize(Joueur.joueurs);
    }

    public JoueurList() {
        super(true);
    }
}
