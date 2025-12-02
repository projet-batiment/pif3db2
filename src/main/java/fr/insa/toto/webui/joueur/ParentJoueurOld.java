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
import fr.insa.toto.webui.DialogDelete;
import fr.insa.toto.webui.JoueurEditor;
import fr.insa.toto.webui.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author elio
 */
public abstract class ParentJoueurOld extends VerticalLayout {
    private String parentTypeName;
    private String parentPrefixLe;
    private String parentPrefixDu;

    private String duParentObject() {
        return this.parentPrefixDu + this.parentTypeName + " " + this.parent.parentObjectName();
    }

    private ParentFace<Joueur> parent;
    private Grid<Joueur> grid;

    private H2 title;
    private Button bNew;

    public void initialize(ParentFace<Joueur> parent) {
        if (parent == null) {
            title.setText("(Erreur)");
        } else {
            this.parent = parent;
            title.setText("Joueurs " + this.duParentObject());

            this.updateGridList();
        }
    }

    private void updateGridList() {
        try (Connection con = ConnectionPool.getConnection()) {
            var list = parent.get(con);
            for (var each: list) {
                try {
                    //each.populate(con);
                    Notification.show("TODO: populate joueurs ?");
                } catch (NoSuchElementException ex) {
                    NotificationError.error("L'un des éléments du joueur " + each.getId() + "n'a pas bien été sauvegardé");
                }
            }
            grid.setItems(list);
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    private void deleteDialog(Joueur joueur) {
        new DialogDelete("le joueur " + joueur.getSurnom() + " " + this.duParentObject(), () -> {
            try (Connection con = ConnectionPool.getConnection()) {
                this.parent.remove(joueur, con);
                this.updateGridList();
                Notification.show("Le joueur a bien été supprimé " + this.duParentObject());
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        }).open();
    }

    public ParentJoueurOld(String parentTypeName, String parentPrefixLe, String parentPrefixDu) {
        this.parentTypeName = parentTypeName;
        this.parentPrefixLe = parentPrefixLe;
        this.parentPrefixDu = parentPrefixDu;

        bNew = new Button("Ajouter...");

        var joueurEditor = new JoueurEditor();
        joueurEditor.setOnSavedCallback(j -> {
            try (Connection con = ConnectionPool.getConnection()) {
                this.parent.add(j, con);
                updateGridList();
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        });
        joueurEditor.setOnDeletedCallback(j -> deleteDialog(j));
        bNew.addClickListener(t -> joueurEditor.open(null));

          this.grid = new Grid<>();
          grid.addColumn(Joueur::getSurnom).setHeader("Surnom");
          grid.addColumn(t -> "TODO").setHeader("Autres ... ...");
          grid.addColumn(new ComponentRenderer<>(joueur -> {
              Button bEdit = new Button("Afficher");
              bEdit.addClickListener(e -> {
                  joueurEditor.open(joueur);
              });
  
              Button bDelete = new Button("Supprimer");
              bDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
              bDelete.addClickListener(e -> 
                      deleteDialog(joueur)
              );
  
              return new HorizontalLayout(bEdit, bDelete);
          })).setHeader(bNew);

        title = new H2();
        this.add(title, grid);
    }
}
