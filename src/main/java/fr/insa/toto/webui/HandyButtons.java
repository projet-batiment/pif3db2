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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.toto.model.Named;
import fr.insa.toto.model.utils.ParentFace;

/**
 *
 * @author elio
 */
public class HandyButtons<ChildType extends ClasseMiroir & Named> extends HorizontalLayout {
    public HandyButtons(ParentFace<ChildType> parent, ChildType eachObject, Editor<ChildType> editor, Runnable onRemoved) {
        Button bEdit = new Button("Afficher");
        bEdit.addClickListener(e ->
            editor.open(eachObject)
        );

        Button bDelete = new Button("Supprimer");
        var dialogDelete = new DialogDeleteChild<ChildType>(parent, eachObject, onRemoved);
        bDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        bDelete.addClickListener(e -> 
            dialogDelete.open()
        );

        super.add(bEdit, bDelete);
    }
}
