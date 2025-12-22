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

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

/**
 *
 * @author elio
 */
public class DialogDelete extends ConfirmDialog {
    public DialogDelete(String what, Runnable callback) {
        super.setHeader("Suppression");
        super.setText("Supprimer " + what + " ?");

        super.setRejectable(false);

        super.setCancelable(true);
        super.setCancelText("Annuler");

        super.setConfirmText("Supprimer");
        super.setConfirmButtonTheme("error primary");
        super.addConfirmListener(e -> callback.run());
    }
}
