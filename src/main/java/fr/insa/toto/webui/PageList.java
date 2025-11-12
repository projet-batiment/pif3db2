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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 *
 * @author elio
 */
public class PageList<T> extends VerticalLayout {
    private Grid<T> grid;
    private final Button add;
    private final Button edit;

    public PageList(Grid<T> grid, ComponentEventListener<ClickEvent<Button>> editListener, ComponentEventListener<ClickEvent<Button>> addListener) {
        this.grid = grid;

        this.add = new Button("Nouveau...");
        this.setAddListener(addListener);

        this.edit = new Button("Éditer...");
        this.setEditListener(editListener);

        this.add(new HorizontalLayout(add, edit), grid);
    }

    public Grid<T> getGrid() {
        return grid;
    }

    public void setGrid(Grid<T> grid) {
        this.grid = grid;
    }

    public final void setAddListener(ComponentEventListener<ClickEvent<Button>> addListener) {
        this.add.addClickListener(addListener);
    }

    public final void setEditListener(ComponentEventListener<ClickEvent<Button>> editListener) {
        this.edit.addClickListener(editListener);
    }
}
