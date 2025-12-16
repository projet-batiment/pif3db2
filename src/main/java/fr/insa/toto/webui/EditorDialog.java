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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author elio
 */
public abstract class EditorDialog extends Dialog {
    private final Button close = new Button("Fermer");
    private final Button apply = new Button("Enregistrer");
    private final Button delete = new Button("Supprimer");
    private final Button board = new Button("Voir les détails");
    private Runnable doSaveCallback;
    private Runnable doBoardCallback;
    private Runnable doDeleteCallback;

    private void exec(Runnable r) {
        if (r != null) r.run();
    }

    public EditorDialog() {
        this.close.addThemeVariants(ButtonVariant.LUMO_ERROR);
        this.close.addClickListener(e -> super.close());
        this.close.getStyle().set("margin-right", "auto");
        this.delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        this.delete.addClickListener(e -> {
            this.exec(doDeleteCallback);
            super.close();
        });
        this.apply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.apply.addClickListener(e -> this.exec(doSaveCallback));

        var bottomButtons = new HorizontalLayout(close, delete, apply);
        bottomButtons.setWidth("100%");

        this.board.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_PRIMARY);
        this.board.setWidth("100%");
        this.board.addClickListener(e -> this.exec(doBoardCallback));

        var view = new VerticalLayout(board, bottomButtons);
        view.setAlignItems(FlexComponent.Alignment.CENTER);

        super.getFooter().add(view);
    }

    public void setSaveCallback(Runnable c) {
        this.doSaveCallback = c;
    }

    public void setBoardCallback(Runnable c) {
        this.doBoardCallback = c;
    }

    public void setDeleteCallback(Runnable c) {
        this.doDeleteCallback = c;
    }

    public void setBoardEnabled(boolean value) {
        this.board.setEnabled(value);
    }

    public void setDeleteEnabled(boolean value) {
        this.delete.setEnabled(value);
    }

    public void setSaveEnabled(boolean value) {
        this.apply.setEnabled(value);
    }
}
