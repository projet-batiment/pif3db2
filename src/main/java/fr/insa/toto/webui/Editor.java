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
public abstract class Editor extends Dialog {
    private final Button close = new Button("Fermer");
    private final Button apply = new Button("Enregistrer");
    private final Button board = new Button("Tableau de bord");
    private final ArrayList<Runnable> callbacks = new ArrayList<>();
    private Runnable openBoard;

    public Editor() {
        this.close.addThemeVariants(ButtonVariant.LUMO_ERROR);
        this.close.addClickListener(e -> super.close());
        this.close.getStyle().set("margin-right", "auto");
        this.apply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.apply.addClickListener(e -> this.save());

        board.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_PRIMARY);
        board.setWidth("100%");
        board.addClickListener(t -> openBoard.run());

        var view = new VerticalLayout(board, new HorizontalLayout(close, apply));
        view.setAlignItems(FlexComponent.Alignment.CENTER);

        super.getFooter().add(view);
    }

    public abstract ClasseMiroir compile();

    private void save() {
        if (this.compile() instanceof ClasseMiroir obj) {
            try (Connection con = ConnectionPool.getConnection()) {
                obj.updateOrNew(con);
                callbacks.forEach(each -> each.run());
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }
        }
    }

    public void addSavedCallback(Runnable c) {
        callbacks.add(c);
    }

    public void setOpenBoard(Runnable openBoard) {
        this.openBoard = openBoard;
    }

    public void setEnabled(boolean value) {
        this.board.setEnabled(value);
    }
}
