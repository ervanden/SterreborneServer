package sterreborneserver;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class SchedulerPanel extends JPanel implements ActionListener, ListSelectionListener {



    MatrixTableModel tm;

    JTable table;
    static JTextPane msgPane;  // because of static method to write to this pane
    JRadioButton onceButton;
    JRadioButton alwaysButton;
    JButton sendButton;

    boolean onceMode = false;

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int[] selectedRows = table.getSelectedRows();
            int[] selectedColumns = table.getSelectedColumns();
            for (int r = 0; r < selectedRows.length; r++) {
                for (int c = 0; c < selectedColumns.length; c++) {
                    int row = selectedRows[r];
                    int col = selectedColumns[c];
                    tm.setCyclic(row, col, !tm.getCyclic(row, col));
                    tm.setOnce(row, col, onceMode);
                }
            }
            table.clearSelection();
        }
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        executeAction(action);
    }

    void executeAction(String action) {
        if (action.equals("Set once")) {
            onceMode = true;
        }
        if (action.equals("Set always")) {
            onceMode = false;
        }
        if (action.equals("Send")) {
            tm.sendScheduleToServer();
        }
    }

    public SchedulerPanel(MatrixTableModel tm) {
        super();

        BoxLayout box = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        this.setLayout(box);

        this.tm = tm;

        table = new JTable(tm);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setCellSelectionEnabled(true);
        table.getSelectionModel().addListSelectionListener(this);
        table.setDefaultRenderer(String.class, new MyRenderer());

        JScrollPane tableScrollPane = new JScrollPane(table);

        msgPane = new JTextPane();
        JScrollPane msgScrollPane = new JScrollPane(msgPane);

        int width = 300;
        int height = 100;
        Dimension minimumDimension = new Dimension(width, 50);
        Dimension preferredDimension = new Dimension(width, height);
        Dimension maximumDimension = new Dimension(10000, 10000);
        msgPane.setMinimumSize(minimumDimension);
        msgPane.setPreferredSize(preferredDimension);
        msgPane.setMaximumSize(maximumDimension);
        msgScrollPane.setMinimumSize(minimumDimension);
        msgScrollPane.setPreferredSize(preferredDimension);
        msgScrollPane.setMaximumSize(maximumDimension);

        onceButton = new JRadioButton("Set once");
        onceButton.setActionCommand("Set once");
        onceButton.addActionListener(this);
        alwaysButton = new JRadioButton("Set always");
        alwaysButton.setActionCommand("Set always");
        alwaysButton.addActionListener(this);
        alwaysButton.setSelected(true);
        sendButton = new JButton("Send");
        sendButton.setActionCommand("Send");
        sendButton.addActionListener(this);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(onceButton);
        group.add(alwaysButton);

        JPanel ioPane = new JPanel();
        ioPane.setLayout(new BoxLayout(ioPane, BoxLayout.LINE_AXIS));
        ioPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        ioPane.add(alwaysButton);
        ioPane.add(Box.createRigidArea(new Dimension(10, 0)));
        ioPane.add(onceButton);
        ioPane.add(Box.createRigidArea(new Dimension(30, 0)));
        ioPane.add(sendButton);

        add(tableScrollPane);
        add(ioPane);
        add(msgScrollPane);
    }

    static public void clientMessage(int verbosity, String msg) {
        if (verbosity <= SterreborneServer.client_verbosity) {
            Document doc = msgPane.getDocument();
            try {
                doc.insertString(doc.getLength(), msg + "\n", null);
            } catch (BadLocationException blex) {
            }
            msgPane.setCaretPosition(doc.getLength());
        }
    }

    static public void serverMessage(
           int serverIdentifier, 
            int verbosity, String msg) {
        if (verbosity <= SterreborneServer.server_verbosity) {
            TimeValue tnow= new TimeValue();
            System.out.println(tnow.dateName() + "  ["+serverIdentifier + "] "+msg);
        }
    }

    class MyRenderer extends DefaultTableCellRenderer {

        Color backgroundColor = getBackground();

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (tm.getCyclic(row, column)) {
                if (tm.getOnce(row, column)) {
                    c.setBackground(Color.red);
                } else {
                    c.setBackground(Color.pink);
                }
            } else if (tm.getOnce(row, column)) {
                c.setBackground(Color.blue);
            } else {
                c.setBackground(Color.cyan);
            }
            return c;
        }
    }



 
}

