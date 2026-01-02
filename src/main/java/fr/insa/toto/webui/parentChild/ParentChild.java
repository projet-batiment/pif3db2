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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.ValueProvider;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.utils.Named;
import fr.insa.toto.model.utils.ParentFace;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.DialogDelete;
import fr.insa.toto.webui.utils.DialogDeleteChild;
import fr.insa.toto.webui.utils.Editor;
import fr.insa.toto.webui.utils.HandyButtons;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.utils.Utils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 *
 * @author elio
 */
public abstract class ParentChild<ChildType extends ClasseMiroir & Named> extends VerticalLayout {
    private ParentFace<ChildType> parent;
    private Grid<ChildType> grid;
    protected final Editor<ChildType> editor;

    private boolean initialized = false;

    private H2 title;

    public void initialize(ParentFace<ChildType> parent) {
        if (parent == null) {
            title.setText("(Erreur)");
        } else {
            this.parent = parent;
            title.setText(Utils.capitalizeFirst(this.parent.lesChildrenDuParentName()));

            if (! this.initialized) {
                this.initialized = true;

                var bNew = new Button("Ajouter...");
                bNew.addClickListener(t -> {
                    editor.open(null);
                });
                Utils.visibleAdmin(bNew);
                this.addColumn(new ComponentRenderer<>(each ->
                    new HandyButtons(
                        this.parent,
                        each,
                        editor,
                        () -> this.updateGridList()
                    )
                )).setHeader(bNew);
            }

            this.updateGridList();
        }
    }

    protected final void updateGridList() {
        try (Connection con = ConnectionPool.getConnection()) {
            var list = parent.get(con);

            for (var each: list) {
                try {
                    each.populate(con);
                } catch (NoSuchElementException ex) {
                    NotificationError.internError("L'un des éléments " + this.parent.child.duChild(each.getName()) + " n'a pas bien été sauvegardé : " + ex.getLocalizedMessage());
                }
            }
            grid.setItems(list);
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    protected final void deleteDialog(ChildType object) {
        new DialogDelete(this.parent.leChildDuParentName(), () -> {
            try (Connection con = ConnectionPool.getConnection()) {
                this.parent.remove(object, con);
                this.updateGridList();
                NotificationError.info(this.parent.child.leChild() + " a bien été supprimé(e) " + this.parent.duParent());
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        }).open();
    }

    public Grid.Column<ChildType> addColumn(Renderer<ChildType> r) {
        return this.grid.addColumn(r);
    }

    public Grid.Column<ChildType> addColumn(ValueProvider<ChildType,?> valueProvider) {
        return this.grid.addColumn(valueProvider);
    }

    public ParentFace<ChildType> getParentFace() {
        return parent;
    }

    public Editor<ChildType> getEditor() {
        return editor;
    }

    protected ParentChild(Editor<ChildType> editor) {
        this.grid = new Grid<>();

        this.editor = editor;

        title = new H2();
        this.add(title, grid);

        this.editor.setOnSavedCallback(j -> {
            try (Connection con = ConnectionPool.getConnection()) {
                getParentFace().add(j, con);
                updateGridList();
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        });

        this.editor.setOnDeletedCallback(j -> {
            new DialogDeleteChild<>(getParentFace(), j, () -> {
                updateGridList();
            }).open();
        });
    }
}
