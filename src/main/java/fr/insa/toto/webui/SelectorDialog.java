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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import fr.insa.toto.model.Named;
import java.util.function.Consumer;

/**
 *
 * @author elio
 */
public abstract class SelectorDialog<T extends Named> extends Dialog {
    private final Button close = new Button("Fermer");
    private final Button apply = new Button("Valider");
    private Consumer<T> applyCallback;
    private Select<T> select;

    public SelectorDialog(String prefixUn, String typeObjets) {
        this.close.addThemeVariants(ButtonVariant.LUMO_ERROR);
        this.close.addClickListener(e -> super.close());
        this.close.getStyle().set("margin-right", "auto");

        this.apply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.apply.addClickListener(e -> {
            if (this.applyCallback != null)
                this.applyCallback.accept(select.getValue());
        });

        select = new Select<>();
        select.setItemLabelGenerator(Named::getNom);
        select.setPlaceholder("Choisir " + prefixUn + " " + typeObjets + "...");

        super.setHeaderTitle("Choisir " + prefixUn + " " + typeObjets + "...");

        var buttons = new HorizontalLayout(close, apply);
        buttons.setWidth("100%");

        super.getFooter().add(buttons);
    }
}
