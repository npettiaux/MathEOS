/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of MathEOS
 *
 * MathEOS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Additional Terms according section 7 of GNU/GPL V3 :
 *
 * These additional terms refer to the source code of bomehc.
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
 *
 **/

package matheos.proportionality;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import matheos.table.Table;
import matheos.table.TableEdits;
import matheos.table.TableLayout;
import static matheos.proportionality.OngletProportionality.*;
import static matheos.proportionality.Fleche.*;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;
import matheos.utils.librairies.DimensionTools;
import matheos.utils.managers.ColorManager;

/**
 * Layout qui vient enrichir une Table par des objets autours.
 * @author François Billioud
 */
public class OngletProportionalityLayout implements LayoutManager {

    public static final int HAUT = Table.HAUT;
    public static final int GAUCHE = Table.GAUCHE;
    public static final int BAS = Table.BAS;
    public static final int DROITE = Table.DROITE;
    public static final int ALL = HAUT + GAUCHE + BAS + DROITE;

    private final ProportionalityTable table;
    
    private final Map<ORIENTATION, TableSideLayout> sidePanels = new HashMap<>();

    public OngletProportionalityLayout(ProportionalityTable table, JPanel parent) {
        this.table = table;

        sidePanels.put(ORIENTATION.HAUT, new TableSideLayout(ORIENTATION.HAUT, table, parent));
        sidePanels.put(ORIENTATION.BAS, new TableSideLayout(ORIENTATION.BAS, table, parent));
        sidePanels.put(ORIENTATION.GAUCHE, new TableSideLayout(ORIENTATION.GAUCHE, table, parent));
        sidePanels.put(ORIENTATION.DROITE, new TableSideLayout(ORIENTATION.DROITE, table, parent));

        parent.add(table);
    }

    void addPropertyChangeListener(PropertyChangeListener l) {
        for(TableSideLayout side : sidePanels.values()) {
            side.addPropertyChangeListener(l);
        }
    }

    void removePropertyChangeListener(PropertyChangeListener l) {
        for(TableSideLayout side : sidePanels.values()) {
            side.removePropertyChangeListener(l);
        }
    }

    void setMode(int mode) {
        for(TableSideLayout side : sidePanels.values()) {
            side.setMode(mode);
        }
    }

    void setOrientation(int orientation) {
        sidePanels.get(ORIENTATION.HAUT).setActif(orientation%2==1);
        sidePanels.get(ORIENTATION.GAUCHE).setActif(orientation/2%2==1);
        sidePanels.get(ORIENTATION.BAS).setActif(orientation/4%2==1);
        sidePanels.get(ORIENTATION.DROITE).setActif(orientation/8%2==1);
    }
    
    void setEnabled(boolean b) {
        for(TableSideLayout panel : sidePanels.values()) {panel.setEnabled(b);}
    }
    
    @Override
    public void addLayoutComponent(String name, Component comp) {}
    @Override
    public void removeLayoutComponent(Component comp) {}
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension minCell = table.getMinimumCellSize(), prefTable = table.getPreferredSize();
        int min = Math.min(minCell.width, minCell.height);
        return new DimensionTools.DimensionT(min,min).plus(Fleche.MARGIN_LATERAL, Fleche.MARGIN_VERTICAL).fois(2).plus(prefTable);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Dimension minCell = table.getMinimumCellSize(), minTable = table.getMinimumSize();
        int min = Math.min(minCell.width, minCell.height);
        return new DimensionTools.DimensionT(min,min).plus(Fleche.MARGIN_LATERAL, Fleche.MARGIN_VERTICAL).fois(2).plus(minTable);
    }

    @Override
    public void layoutContainer(Container parent) {
        //La table est localisée à +offsetGauche, +offsetHaut, où les offsets sont l'espace laissé pour dessiner le boutons d'interaction utilisateur
        Dimension minCell = table.getMinimumCellSize();
        int min = Math.min(minCell.width, minCell.height);
        table.setLocation(min+Fleche.MARGIN_LATERAL,min+Fleche.MARGIN_VERTICAL);

        //La taille de la table est la preferredSize, si possible. Mais on fixe la taille max afin d'adapter la font-size pour que tout tienne tjs à l'écran
        Dimension size = parent.getSize();
        if(size.width==0 || size.height==0) {size = parent.getPreferredSize();}
        Dimension max = new Dimension(size.width-(min+Fleche.MARGIN_LATERAL)*2, size.height-(min+Fleche.MARGIN_VERTICAL)*2);
        table.setMaximumSize(max);

        //On calcul à présent la taille réelle de la table
        Dimension mini = table.getMinimumSize();
        DimensionTools.DimensionT pref = new DimensionTools.DimensionT(table.getPreferredSize()).min(max).max(mini);
        table.setSize(pref);
        table.getLayout().layoutContainer(table);//On doit revalider la taille des cellules immédiatement pour pouvoir dessiner les SidePanels

        //Enfin, on positionne les objets sur les côtés de la table
        try {//HACK pour le moment, le système étant instable, il est plus sûr de mettre de coté cette méthode
            if(table.isEmpty()) {return;}
            Dimension miniSize = table.getMinimumCellSize();
            int miniDimension = Math.min(miniSize.width, miniSize.height);
            for(TableSideLayout side : sidePanels.values()) {
                if(side.isActif()) {side.positionComponent(miniDimension);}
            }
        } catch(Exception ex) {
            Logger.getLogger(OngletProportionality.class.getName()).log(Level.SEVERE, "no such element", ex);
        }
    }
    
    /**
     * Panel qui permet d'afficher les boutons autour du tableau (flèches, boutons, etc).
     * @author François Billioud
     */
    private static class TableSideLayout {

        public static final String ACTIVATE_PROPERTY = "active";
        public static final String ENABLED_PROPERTY = "enabled";

    //    private GeneralUndoManager undo;

        private final ORIENTATION orientation;
        private final ProportionalityTable table;
        private final JPanel support;                                           //support sur lequel seront  dessinés la table et les composants
        private final LinkedList<JComponent> components = new LinkedList<>();   //liste des composants dont cet objet a la charge

        private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

        private int mode = NORMAL;

        private TableSideLayout(ORIENTATION orientation, ProportionalityTable table, JPanel support) {
            this.orientation = orientation;
            this.table = table;
            this.support = support;

            getTable().addTableModelListener(new ProportionalityTable.ModelListener() {//TODO remplacer par un adapter
                @Override
                public void arrowInserted(int direction, Fleche fleche) {applyMode(mode);TableSideLayout.this.support.repaint();}
                @Override
                public void arrowDeleted(int direction, Fleche fleche) {remove(fleche);TableSideLayout.this.support.repaint();}
                @Override
                public void rowInserted(TableLayout.Cell[] row, int index) {applyMode(mode);}
                @Override
                public void columnInserted(TableLayout.Cell[] column, int index) {applyMode(mode);}
                @Override
                public void rowDeleted(TableLayout.Cell[] row, int index) {applyMode(mode);}
                @Override
                public void columnDeleted(TableLayout.Cell[] column, int index) {applyMode(mode);}
                @Override
                public void contentEdited(TableLayout.Cell c, Object newContent) {}
                @Override
                public void cleared(TableLayout.Cell[][] table) {applyMode(mode);}
                @Override
                public void cellReplaced(TableLayout.Cell oldCell, TableLayout.Cell newCell) {}
                @Override
                public void colorChanged(Color oldColor, Color newColor) {}
            });
        }

        public void setMode(int mode) {
            if(this.mode==mode) {return;}
            int old = this.mode;
            this.mode = mode;

            applyMode(mode);

            changeSupport.firePropertyChange(MODE_PROPERTY, old, mode);
            startIndex = -1;
        }

        private boolean actif = true;
        public void setActif(boolean b) {
            if(actif==b) {return;}
            actif = b;
            if(!b) {removeAll();} else {applyMode(mode);}
            changeSupport.firePropertyChange(ACTIVATE_PROPERTY, !b, b);
        }
        public boolean isActif() {return actif;}

        private boolean enabled = true;
        private int modeMemory;//retient le dernier mode avant une désactivation
        public void setEnabled(boolean b) {
            if(enabled==b) {return;}
            enabled = b;
            if(enabled) {
                for(JComponent c : components) {
                    final Fleche f = (Fleche) c;
                    f.setEnabled(true);
                }
                setMode(modeMemory);
            } else {
                modeMemory = mode;
                setMode(NORMAL);
                for(JComponent c : components) {
                    final Fleche f = (Fleche) c;
                    f.setEnabled(false);
                }
            }
            
            changeSupport.firePropertyChange(ENABLED_PROPERTY, !b, b);
        }
        public boolean isEnabled() {return enabled;}

        /** permet de redessiner les éléments du mode. Utile lorsque les éléments nécessitent une mise à jour **/
        private void applyMode(int mode) {
            switch(mode) {
                case NORMAL : setModeNormal(); break;
                case INSERTION : setModeInsertion(); break;
                case SUPPRESSION : setModeSuppression(); break;
                case COLORER : setModeColorer(); break;
                case CREATION_FLECHE : setModeCreationFleches(); break;
                case SUPPRESSION_FLECHE : setModeSuppressionFleches(); break;
            }
        }

        public ORIENTATION getOrientation() {return orientation;}

        public void addPropertyChangeListener(PropertyChangeListener l) {
            changeSupport.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            changeSupport.removePropertyChangeListener(l);
        }

        private boolean isVertical() {return orientation.isVertical();}

        private void removeAll() {
            while(!components.isEmpty()) {
                support.remove(components.poll());
            }
            support.revalidate();
            support.repaint();
        }

        private void add(JComponent c) {
            support.add(c);
            components.add(c);
        }

        private void remove(JComponent c) {
            support.remove(c);
            components.remove(c);
        }

        private void replace(JComponent old, JComponent newComp) {
            support.remove(old);
            int index = components.indexOf(old);
            components.set(index, newComp);
            support.add(newComp);
            support.revalidate();
            support.repaint();
        }

        /**
         * Renvoie la ligne des cellules du tableau au-dessus de laquelle les boutons sont positionnés.
         * @return Tableau des cellules concernées.
         */
        private TableLayout.Cell[] getReferredLine() {
            TableLayout.Cell[] line = null;
            switch(orientation) {
                case HAUT : line = getTable().get(Table.ROW, 0); break;
                case GAUCHE : line = getTable().get(Table.COLUMN, 0); break;
                case BAS : line = getTable().get(Table.ROW, getTable().getRowCount()-1); break;
                case DROITE : line = getTable().get(Table.COLUMN, getTable().getColumnCount()-1); break;
            }
            return line;
        }

        /**
         * Demande au layout de positionner ses composants.
         * @param buttonSize la taille qui sera utilisée comme référence pour dimensionner tous les éléments.
         * Il s'agit de la dimension minimum d'une cellule du tableau, ceci afin d'avoir la place de dessiner les boutons entre deux cellules
         */
        public void positionComponent(int buttonSize) {
            switch(mode) {
                case NORMAL : positionNormal(buttonSize); break;
                case INSERTION : positionInsertion(buttonSize); break;
                case SUPPRESSION : positionSuppression(buttonSize); break;
                case COLORER : positionColorer(buttonSize); break;
                case CREATION_FLECHE : positionCreationFleche(buttonSize); break;
                case SUPPRESSION_FLECHE : positionSuppressionFleche(buttonSize); break;
            }
        }

        public void positionNormal(int buttonSize) {
            for(JComponent c : components) {
                final Fleche f = (Fleche) c;
                if(mode!=SUPPRESSION_FLECHE) {f.setModeEdition();}
                f.setHeight(buttonSize);
                f.positionComponent();
                f.repaint();
            }
        }

        private static final int GAP = 0;
        private Point getOrientationTranslationPoint(JComponent c, JComponent ref, Point p0) {
            Point p1 = isVertical() ? new Point(-c.getWidth()/2,0) : new Point(0, -c.getHeight()/2);

            switch(orientation) {
                case HAUT : p1.translate(0, -p0.y-GAP-c.getHeight()); break;
                case BAS : p1.translate(0, -p0.y+GAP+ref.getHeight()); break;
                case GAUCHE : p1.translate(-GAP-c.getWidth()-p0.x,0); break;
                case DROITE : p1.translate(ref.getWidth()+GAP-p0.x,0); break;
            }
            return p1;
        }
        private Point getModeTranslationPoint(JComponent ref) {
            Point p = new Point(0,0);
            switch(mode) {
                case INSERTION : break;
                case SUPPRESSION : p = new Point(ref.getWidth()/2,ref.getHeight()/2); break;
                case COLORER : p = new Point(ref.getWidth()/2,ref.getHeight()/2); break;
                case CREATION_FLECHE : p = new Point(ref.getWidth()/2,ref.getHeight()/2); break;
                case SUPPRESSION_FLECHE : p = new Point(ref.getWidth()/2,ref.getHeight()/2); break;
            }
            return p;
        }

        private void positionComponentOverCells(int buttonSize) {
            TableLayout.Cell[] line = getReferredLine();
            if(components.size()<line.length) {applyMode(mode);}//Si on est dans ce cas, alors les boutons ne sont pas tous présents
            Iterator<JComponent> componentIterator = components.iterator();
            for(int i=0; i<line.length; i++) {
                if(!componentIterator.hasNext()) {
                    Logger.getLogger(TableSideLayout.class.getName()).log(Level.SEVERE, "no such element", "");
                    return;
                }
                JComponent c = componentIterator.next();
                c.setSize(buttonSize, buttonSize);

                JComponent ref = line[i];
                Point p0 = getModeTranslationPoint(ref);
                Point p1 = getOrientationTranslationPoint(c, ref, p0);

                Point p = SwingUtilities.convertPoint(ref, p0, support);
                p.translate(p1.x, p1.y);
                c.setLocation(p.x,p.y);
            }
            if(componentIterator.hasNext()) {
                JComponent c = componentIterator.next();
                c.setSize(buttonSize, buttonSize);

                JComponent ref = line[line.length-1];
                Point p0 = getModeTranslationPoint(ref);
                Point p1 = getOrientationTranslationPoint(c, ref, p0);

                Point p = SwingUtilities.convertPoint(ref, p0, support);
                p.translate(p0.x+p1.x+(isVertical() ? ref.getWidth() : 0), p0.y+p1.y+(isVertical() ? 0 : ref.getHeight()));
                c.setLocation(p.x,p.y);
            }
        }

        public void positionInsertion(int buttonSize) {
            positionComponentOverCells(buttonSize);
        }
        public void positionSuppression(int buttonSize) {
            positionComponentOverCells(buttonSize);
        }
        public void positionColorer(int buttonSize) {
            positionComponentOverCells(buttonSize);
        }
        public void positionCreationFleche(int buttonSize) {
            positionComponentOverCells(buttonSize);
        }
        public void positionSuppressionFleche(int buttonSize) {
            positionNormal(buttonSize);
        }

        private ProportionalityTable getTable() {return table;}

        private void createButtons(Action action, int nbElements) {
            removeAll();
            for(int i=0; i<nbElements; i++) {
                //On utilise les clientProperty pour ne créer qu'une action pour plusieurs boutons (très important lors des resize)
                Bouton bouton = new Bouton(action);
                bouton.getButtonComponent().putClientProperty("index", i);
                add(bouton);
            }
            support.revalidate();
            support.repaint();
        }
        private void setModeNormal() {
            removeAll();
            Set<Fleche> fleches = getTable().getArrows(orientation.getOrientationId());
            for(Fleche f : fleches) {
                add(f);
                f.setUndoManager(table.getUndoManager());
                f.setModeEdition();
            }

            support.revalidate();
            support.repaint();
        }
        private void setModeInsertion() {
            if(orientation==ORIENTATION.DROITE || orientation==ORIENTATION.BAS) {removeAll();return;}
            Action action = new ActionInsertion();
            int n = getTable().getRowCount(), m = getTable().getColumnCount();
            int nbElements = (isVertical() ? m : n)+1;
            createButtons(action, nbElements);
        }
        private void setModeSuppression() {
            if(orientation==ORIENTATION.DROITE || orientation==ORIENTATION.BAS) {removeAll();return;}
            if(orientation==ORIENTATION.HAUT && getTable().getColumnCount()<=1) {return;}
            if(orientation==ORIENTATION.GAUCHE && getTable().getRowCount()<=1) {return;}
            Action action = new ActionSuppression();
            int n = getTable().getRowCount(), m = getTable().getColumnCount();
            int nbElements = (isVertical() ? m : n);
            createButtons(action, nbElements);
        }
        private void setModeColorer() {
            if(orientation==ORIENTATION.DROITE || orientation==ORIENTATION.BAS) {removeAll();return;}
            final Action actionPaint = new ActionCouleur(true);
            final Action actionUnpaint = new ActionCouleur(false);
            int n = getTable().getRowCount(), m = getTable().getColumnCount();
            int nbElements = (isVertical() ? m : n);

            removeAll();
            for(int i=0; i<nbElements; i++) {
                boolean isAlreadyColored = getTable().getColor(isVertical()==Table.COLUMN,i)!=null;
                final Bouton bouton = new BoutonDouble(actionPaint, actionUnpaint, !isAlreadyColored);
                bouton.getButtonComponent().putClientProperty("index", i);
                add(bouton);
            }
            support.revalidate();
            support.repaint();
        }

        private class BoutonDouble extends Bouton implements ActionListener {
            private final Action action1, action2;
            private boolean currentAction1;
            private BoutonDouble(Action action1, Action action2, boolean action1Initiale) {
                super(action1Initiale ? action1 : action2);
                this.action1 = action1;
                this.action2 = action2;
                this.currentAction1 = action1Initiale;

                getButtonComponent().addActionListener(this);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                BoutonDouble.this.setAction(currentAction1 ? BoutonDouble.this.action2 : BoutonDouble.this.action1);
                currentAction1 = !currentAction1;
            }
        }

        private void setModeCreationFleches() {
            Action action = new ActionFlecheCreation();
            int n = getTable().getRowCount(), m = getTable().getColumnCount();
            int nbElements = (isVertical() ? m : n);
            createButtons(action, nbElements);
        }
        private void setModeSuppressionFleches() {
            setModeNormal();
            for(JComponent c : components) {
                final Fleche f = (Fleche) c;
                f.setModeSuppression();
            }
        }

        private class ActionInsertion extends ActionComplete {
            private ActionInsertion() {
                super(orientation.getBalise("table arrow"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                AbstractButton b = (AbstractButton) source;
                int i = (int) b.getClientProperty("index");
                boolean line = isVertical()==Table.COLUMN;

                Bouton bouton = new Bouton(b.getAction());
                bouton.getButtonComponent().putClientProperty("index", getTable().getCount(line));
                add(bouton);

                getTable().insert(line, i);
                table.getUndoManager().addEdit(new TableEdits.LineChangeEdit.InsertionEdit(i, getTable(), line));
            }
        }

        private class ActionSuppression extends ActionComplete {
            private ActionSuppression() {
                super("table suppression");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                JComponent c = (JComponent) source;
                int i = (int) c.getClientProperty("index");
                boolean line = isVertical()==Table.COLUMN;
                List<Fleche> arrowsToRemove = getFlechesInvolving(i, getTable().getArrows(orientation.getOrientationId()));//les flèches qui seront impactées par cette suppression
                ArrayList<TableLayout.Cell> L = getTable().delete(line, i);
                table.getUndoManager().addEdit(getTable().new SuppressionEdit(i, line, L.toArray(new TableLayout.Cell[L.size()]), arrowsToRemove));
                remove(components.getLast());
                if(orientation==ORIENTATION.HAUT && getTable().getColumnCount()==1) {remove(components.getLast());}
                else if(orientation==ORIENTATION.GAUCHE && getTable().getRowCount()==1) {remove(components.getLast());}
                support.revalidate();
                support.repaint();
            }
        }

        private class ActionCouleur extends ActionComplete {
            private final boolean colorer;
            private static final String COLORER = "table paint";
            private static final String EFFACER = "table unpaint";
            private ActionCouleur(boolean colorer) {
                super(orientation.getBalise(colorer ? COLORER : EFFACER));
                this.colorer = colorer;
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                JComponent c = (JComponent) source;
                int i = (int) c.getClientProperty("index");
                boolean line = isVertical()==Table.COLUMN;
                Color oldColor = getTable().getColor(line, i), newColor;
                getTable().setColor(line, i, newColor = colorer ? ColorManager.get("color cell color1") : null);
                table.getUndoManager().addEdit(new TableEdits.LineColorEdit(i, line, oldColor, newColor, getTable()));
            }
        }

        private int startIndex = -1;
        private class ActionFlecheCreation extends ActionComplete {
            private ActionFlecheCreation() {
                super(orientation.getBalise("table arrow"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                JComponent c = (JComponent) source;
                int i = (int) c.getClientProperty("index");
                if(startIndex==-1) {
                    startIndex = i;
                    TableSideLayout.this.changeSupport.firePropertyChange(ORIENTATION_PROPERTY, null, orientation.getOrientationId());//XXX Il serait mieux d'utiliser un listener dédié
                    components.get(i).setVisible(false);
                    Set<Fleche> fleches = getTable().getArrows(orientation.getOrientationId());
                    List<Fleche> L = getFlechesInvolving(i, fleches);
                    for(Fleche f : L) {components.get(f.getOtherSide(i)).setVisible(false);}
                }
                else {
                    Fleche f = new Fleche(orientation, startIndex, i, getTable());
                    getTable().insertArrow(orientation.getOrientationId(), f);
                    table.getUndoManager().addEdit(getTable().new ArrowInsertedEdit(f));
                    setMode(NORMAL);
                }
            }
        }
        private List<Fleche> getFlechesInvolving(int i, Collection<Fleche> col) {
            LinkedList<Fleche> L = new LinkedList<>();
            for(Fleche f : col) {if(f.getStartIndex()==i || f.getEndIndex()==i) {L.add(f);}}
            return L;
        }
    }
}
