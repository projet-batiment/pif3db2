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
package fr.insa.toto.webui.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 *
 * @author elio
 */
public abstract class Editor<T extends ClasseMiroir> extends EditorDialog {
    private final Select<T> select;
    protected T object;

    private Consumer<T> onSavedCallback;

    public void setOnSavedCallback(Consumer<T> onSavedCallback) {
        this.onSavedCallback = onSavedCallback;
    }

    public void setOnDeletedCallback(Consumer<T> onDeletedCallback) {
        super.setDeleteCallback(() -> onDeletedCallback.accept(object));
    }

    // parceque (id=0).equals throws EntiteNonSauvegardee
    protected T nouveau;
    protected abstract T newObject();

    private VerticalLayout view;

    protected abstract void setObject();
    protected abstract List<T> openObject(Connection con) throws SQLException;

    protected abstract T compile();
    protected void onSaved(){};
    protected abstract String generatedUrl();

    // toujours appeler DEPUIS select.setValue !!! jamais depuis ailleurs
    private final void set(T object) {
        if (object == this.nouveau) {
            this.object = newObject();

            super.setBoardEnabled(false);
            super.setDeleteEnabled(false);
        } else {
            this.object = object;

            super.setBoardEnabled(true);
            super.setDeleteEnabled(true);
        }

        this.setObject();
    }

    private void updateSelect(T object) {
        try (var con = ConnectionPool.getConnection()) {
            var list = this.openObject(con);
            list.add(this.nouveau);

            select.setItems(list);
            select.setValue(object == null ? this.nouveau : object);
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
    }

    public final void open(T object) {
        this.updateSelect(object);

        super.open();
    }

    public Editor() {
        select = new Select<>();
        select.addValueChangeListener(t -> this.set(t.getValue()));
        select.setPlaceholder("Choisir...");
        this.view = new VerticalLayout(select);
        this.add(view);

        super.setSaveCallback(() -> {
            if (this.compile() instanceof T obj) {
                try (Connection con = ConnectionPool.getConnection()) {
                    int id = obj.updateOrNew(con);
                    this.updateSelect(object);

                    if (this.onSavedCallback != null)
                        this.onSavedCallback.accept(this.object);

                    this.onSaved();
                } catch (SQLException ex) {
                    NotificationError.sql(ex);
                } catch (NoSuchElementException ex) {
                    NotificationError.error(ex.getLocalizedMessage());
                }
            }
        });

        super.setBoardCallback(() -> {
            if (this.object != null) {
                this.getUI().ifPresent(ui -> ui.navigate(this.generatedUrl()));
                this.close();
            }
        });
    }

    protected void addChildren(Component... components) {
        this.view.add(components);
    }

    protected void setSelectLabel(String label) {
        this.select.setLabel(label);
    }

    protected void setSelectItemLabelGenerator(ItemLabelGenerator<T> label) {
        this.select.setItemLabelGenerator(label);
    }
}
