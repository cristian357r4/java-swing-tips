package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

// http://www2.gol.com/users/tame/swing/examples/JTableExamples2.html
public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"Integer", "Answer"};
        Object[][] data = {
            {1, Answer.A}, {2, Answer.B}, {3, Answer.C},
            {4, Answer.C}, {5, Answer.A}
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
//         if (System.getProperty("java.version").startsWith("1.6.0")) {
//             // 1.6.0_xx bug? column header click -> edit cancel?
//             table.getTableHeader().addMouseListener(new MouseAdapter() {
//                 @Override public void mousePressed(MouseEvent e) {
//                     if (table.isEditing()) {
//                         table.getCellEditor().stopCellEditing();
//                     }
//                 }
//             });
//         }

//         table.addMouseListener(new MouseAdapter() {
//             @Override public void mouseReleased(MouseEvent e) {
//                 JTable t = (JTable) e.getComponent();
//                 Point pt = e.getPoint();
//                 int row = t.rowAtPoint(pt);
//                 int col = t.columnAtPoint(pt);
//                 if (t.convertRowIndexToModel(row) >= 0 && t.convertColumnIndexToModel(col) == 1) {
//                     TableCellEditor ce = t.getCellEditor(row, col);
//                     // https://tips4java.wordpress.com/2009/07/12/table-button-column/
//                     ce.stopCellEditing();
//                     Component c = ce.getTableCellEditorComponent(t, null, true, row, col);
//                     Point p = SwingUtilities.convertPoint(t, pt, c);
//                     Component b = SwingUtilities.getDeepestComponentAt(c, p.x, p.y);
//                     if (b instanceof JRadioButton) {
//                         ((JRadioButton) b).doClick();
//                     }
//                 }
//             }
//         });
        // RadioButtonEditorRenderer rbe = new RadioButtonEditorRenderer();
        table.getColumnModel().getColumn(1).setCellRenderer(new RadioButtonsRenderer());
        table.getColumnModel().getColumn(1).setCellEditor(new RadioButtonsEditor());
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class RadioButtonsPanel extends JPanel {
    private final String[] answer = {Answer.A.toString(), Answer.B.toString(), Answer.C.toString()};
    protected final List<JRadioButton> buttons = new ArrayList<>(answer.length);
    protected ButtonGroup bg = new ButtonGroup();
    protected RadioButtonsPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        initButtons();
    }
    private void initButtons() {
        buttons.clear();
        removeAll();
        bg = new ButtonGroup();
        for (String title: answer) {
            JRadioButton b = makeButton(title);
            buttons.add(b);
            add(b);
            bg.add(b);
        }
    }
    private static JRadioButton makeButton(String title) {
        JRadioButton b = new JRadioButton(title);
        b.setActionCommand(title);
        b.setFocusable(false);
        b.setRolloverEnabled(false);
        return b;
    }
    protected void updateSelectedButton(Object v) {
        if (v instanceof Answer) {
            initButtons();
            switch ((Answer) v) {
                case A:
                    buttons.get(0).setSelected(true);
                    break;
                case B:
                    buttons.get(1).setSelected(true);
                    break;
                case C:
                    buttons.get(2).setSelected(true);
                    break;
                default:
                    break;
            }
        }
    }
}

class RadioButtonsRenderer extends RadioButtonsPanel implements TableCellRenderer {
    @Override public void updateUI() {
        super.updateUI();
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        updateSelectedButton(value);
        return this;
    }
}

class RadioButtonsEditor extends RadioButtonsPanel implements TableCellEditor {
    protected transient ChangeEvent changeEvent;

    protected RadioButtonsEditor() {
        super();
        ActionListener al = e -> fireEditingStopped();
        for (AbstractButton b: buttons) {
            b.addActionListener(al);
        }
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        updateSelectedButton(value);
        return this;
    }
    @Override public Object getCellEditorValue() {
        return Answer.valueOf(bg.getSelection().getActionCommand());
    }

    // Copied from AbstractCellEditor
    // protected EventListenerList listenerList = new EventListenerList();
    // protected transient ChangeEvent changeEvent;
    @Override public boolean isCellEditable(EventObject e) {
        return true;
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    @Override public void cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
    }
    protected final void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
            }
        }
    }
}

enum Answer { A, B, C }
