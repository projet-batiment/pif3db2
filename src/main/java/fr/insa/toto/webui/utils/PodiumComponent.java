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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import fr.insa.toto.model.Equipe;
import java.util.List;

public class PodiumComponent extends HorizontalLayout {

    public PodiumComponent(List<Equipe> top3) {
        this.setWidthFull();
        this.setAlignItems(Alignment.END); 
        this.setJustifyContentMode(JustifyContentMode.CENTER);
        this.getStyle().set("padding-bottom", "20px");

        if (top3 != null && !top3.isEmpty()) {
            Equipe p1 = top3.get(0);
            Equipe p2 = (top3.size() > 1) ? top3.get(1) : null;
            Equipe p3 = (top3.size() > 2) ? top3.get(2) : null;

            if (p2 != null) this.add(creerMarche(p2, "120px", "#C0C0C0", "2ème"));
            this.add(creerMarche(p1, "180px", "#FFD700", "1er"));
            if (p3 != null) this.add(creerMarche(p3, "80px", "#CD7F32", "3ème"));
        }
    }

    private VerticalLayout creerMarche(Equipe equipe, String hauteur, String couleur, String label) {
        VerticalLayout colonne = new VerticalLayout();
        colonne.setAlignItems(Alignment.CENTER);
        colonne.setPadding(false);
        colonne.setSpacing(false);
        colonne.setWidth("140px");

        Span nom = new Span(equipe.getNom());
        nom.getStyle().set("font-weight", "bold").set("margin-bottom", "10px");

        Div bloc = new Div();
        bloc.setWidthFull();
        bloc.setHeight(hauteur);
        bloc.getStyle()
            .set("background-color", couleur)
            .set("border-radius", "8px 8px 0 0")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("box-shadow", "0 -4px 10px rgba(0,0,0,0.1)");

        Span texteRang = new Span(label);
        texteRang.getStyle().set("color", "white").set("font-weight", "bold");

        bloc.add(texteRang);
        colonne.add(nom, bloc);
        
        return colonne;
    }
}
