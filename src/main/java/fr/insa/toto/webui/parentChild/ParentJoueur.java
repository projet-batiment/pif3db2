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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.model.Matchs;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.model.utils.ParentFace;
import fr.insa.toto.webui.utils.DialogDelete;
import fr.insa.toto.webui.utils.DialogDeleteChild;
import fr.insa.toto.webui.utils.HandyButtons;
import fr.insa.toto.webui.joueur.JoueurEditor;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author elio
 */
public abstract class ParentJoueur extends ParentChild<Joueur> {
    public ParentJoueur() {
        var joueurEditor = new JoueurEditor();
        joueurEditor.setOnSavedCallback(j -> {
            try (Connection con = ConnectionPool.getConnection()) {
                super.getParentFace().add(j, con);
                super.updateGridList();
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        });
        joueurEditor.setOnDeletedCallback(j -> {
            new DialogDeleteChild<Joueur>(super.getParentFace(), j, () -> {
                super.updateGridList();
                NotificationError.show("DELETED DONE");
            }).open();
        });
        super.setEditor(joueurEditor);

        super.addColumn(Joueur::getSurnom).setHeader("Surnom");
        super.addColumn(Joueur::getCategorie).setHeader("Catégorie");
        super.addColumn(Joueur::getTaillecm).setHeader("Taille (cm)");
    }
}
