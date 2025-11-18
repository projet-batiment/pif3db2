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
import com.vaadin.flow.component.select.Select;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public abstract class Editor extends Dialog {
    private final Button close = new Button("Annuler");
    private final Button apply = new Button("Enregistrer");
    private final Runnable callback;

    public Editor(Runnable callback) {
        this.close.addThemeVariants(ButtonVariant.LUMO_ERROR);
        this.close.addClickListener(e -> super.close());
        this.close.getStyle().set("margin-right", "auto");
        this.apply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.apply.addClickListener(e -> this.save());

        this.callback = callback;

        super.getFooter().add(close, apply);
    }

    public abstract ClasseMiroir compile();

    private void save() {
        try {
            var con = ConnectionPool.getConnection();
            this.compile().updateOrNew(con);

            super.close();
            this.callback.run();
        } catch (SQLException ex) {
            NotificationError.show(ex.getMessage());
        }
    }
}
