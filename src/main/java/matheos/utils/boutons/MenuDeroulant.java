/** «Copyright 2011 François Billioud»
 *
 * This file is part of MathEOS.
 *
 * MathEOS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MathEOS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MathEOS. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Additional Terms according section 7 of GNU/GPL V3 :
 *
 * These additional terms refer to the source code of matheos.
 *
 * According to GNU GPL v3, section 7 b) :
 * You should mention any contributor of the work as long as his/her contribution
 * is meaningful in a covered work. If you convey a source code using a part of the
 * source code of MathEOS, you should keep the original author in the resulting
 * source code. If you propagate a covered work with the same objectives as the
 * Program (help student to attend maths classes with an adapted software), you
 * should mention «Ludovic Faubourg», «Frédéric Marinoni» as author of the idea of
 * this software. In any case, if you propagate a covered work you have to mention
 * François Billioud and Guillaume Varoquaux as author of the realisation of MathEOS
 * software. The paternity of the authors have to appear in a legible, unobscured
 * manner, showing clearly their link to the covered work in any document,
 * web pages,... which describe the project or participate to the distribution of
 * the covered work.
 */

package matheos.utils.boutons;

import matheos.utils.managers.ImageManager;

import javax.swing.JComboBox;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class MenuDeroulant extends JComboBox<Object> {

    public MenuDeroulant() {
        setFocusable(false);
    }

    public MenuDeroulant(Object[] objets) {
        this();
        addItem(objets);
    }

    public MenuDeroulant(Object[] objets, String aspect) {
    	this(objets);
        InfoBouton info = new InfoBouton(aspect);
        setToolTipText(info.texteDescription);
        setActionCommand(info.balise);
    }
	
    public void addItem(String aspect) {
        super.addItem(ImageManager.getIcone(aspect));
    }

    public final void addItem(Object[] objets) {
        for (Object o : objets) addItem(o);
    }

}