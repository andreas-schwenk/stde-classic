/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Gui
 * Class:       GuiTableVariables
 * Created:     2011-10-28
 */

package Gui;

import Graph.SigVar.SIGVAR_TYPE;
import Graph.Variable;
import Gui.Boundary.GuiTableBoundary;
import Workflow.Workflow;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * GuiTable for Variables
 *
 * @author Jan Montag
 */
public class GuiTableVariables extends JPanel
{
    // *** ATTRIBUTES ***

    private JTable table;

    private GuiMain guiMain;

    private Workflow wflow;

    private GuiTableBoundary tableBoundary;

    private JButton button;

    private Color lastRowColor;

    private ButtonRenderer bRenderer;

    private int selectedRowNumber, selectedColumnNumber;

    private JComboBox variableType;

    private Object[] headline = {"Name", "Typ", "Länge", "Beschreibung", ""};

    private DefaultTableModel model;

    private TableModelListener tableModelLister;

    // *** METHODS ***

    /**
     * standard-constructor
     *
     * @param guiMain
     *
     * @author Jan Montag
     */
    public GuiTableVariables(GuiMain guiMain)
    {
        super();

        lastRowColor = new Color(227, 227, 227);

        button = new JButton();

        this.guiMain = guiMain;

        wflow = guiMain.getWorkflow();

        tableBoundary = new GuiTableBoundary();

        setLayout(new GridLayout(1,1));

        model = newTableModel();
        Object[] emptyRow = {"","","",""};
        model.addRow(emptyRow);
        model.setValueAt("<Neu>", 0, 0);

        tableModelLister = new TableModelListener() 
        {
            @Override
            public void tableChanged(TableModelEvent tme)
            {
                int editedRow = tme.getFirstRow();
                int editedColumn = tme.getColumn();

                if(editedRow >= table.getRowCount()-1
                        || table.getSelectedColumn() == (table.getColumnCount()-1)
                        || editedRow == -1 || editedColumn == -1)
                  return;               

                String cellValueString = null;
                int cellValueInt = 1;
                Variable tVar;
                
                Object cellValue = table.getValueAt(editedRow, editedColumn);
                if(cellValue instanceof String)
                {
                    cellValueString = (String) table.getValueAt(editedRow, editedColumn);
                }

                if(cellValue instanceof Integer)
                {
                    cellValueInt = Integer.parseInt(table.getValueAt(editedRow, editedColumn).toString());
                }

                if(editedRow < (tableBoundary.getVariables().size()))
                {
                    tVar = tableBoundary.getVariables().get(editedRow);

                    switch(editedColumn)
                    {
                        case 0: 
                            tVar.setName(cellValueString);
                            break;
                        case 1: 
                            if(cellValue == SIGVAR_TYPE.BIT)
                            {
                                tVar.setType(SIGVAR_TYPE.BIT);
                                // default bit-length: 1
                                model.setValueAt(1, editedRow, editedColumn+1);
                            }
                            else if(cellValue == SIGVAR_TYPE.BIT_N)
                            {
                                tVar.setType(SIGVAR_TYPE.BIT_N);
                                // default bit-length: 8
                                model.setValueAt(8, editedRow, editedColumn+1);
                            }
                            else if(cellValue == SIGVAR_TYPE.SIGNED)
                            {
                                tVar.setType(SIGVAR_TYPE.SIGNED);
                                // default bit-length: 16
                                model.setValueAt(16, editedRow, editedColumn+1);
                            }
                            else if(cellValue == SIGVAR_TYPE.UNSIGNED)
                            {
                                tVar.setType(SIGVAR_TYPE.UNSIGNED);
                                // default bit-length: 16
                                model.setValueAt(16, editedRow, editedColumn+1);
                            }
                            else
                                return;
                            break;
                        case 2: 
                            tVar.setbitLength(cellValueInt);
                            // if the type is bit, then reset to bitlength '1'
                            if(tVar.getType() != null && tVar.getType()==SIGVAR_TYPE.BIT && cellValueInt != 1)
                                model.setValueAt(1, editedRow, editedColumn);
                            break;
                        case 3: 
                            tVar.setDescription(cellValueString);
                            break;
                        default:
                            return;
                    }

                }
            }
        };
        
        table = new JTable(model)
        {
            // Last Row Background-Color: gray
            @Override
            public Component prepareRenderer(TableCellRenderer renderer,
                                     int rowIndex, int colIndex)
            {
                if(((colIndex == 2) || (colIndex == 1)) && rowIndex < table.getRowCount()-1)
                {
                    JLabel jl = new JLabel ("" + getValueAt(rowIndex, colIndex));
                    jl.setHorizontalAlignment(SwingConstants.CENTER);
                    return jl;
                }
                
                Component c = super.prepareRenderer(renderer, rowIndex, colIndex);
                if (rowIndex == table.getRowCount()-1 && !isCellSelected(rowIndex, colIndex)) {
                    c.setBackground(lastRowColor);
                }
                else if (rowIndex != table.getRowCount()-1 && !isCellSelected(rowIndex, colIndex))
                {
                    // If not shaded, match the table's background
                    c.setBackground(getBackground());
                }
                return c;
            }

            @Override
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.white);
                int singleRowHeight = table.getRowHeight();
                int width = table.getWidth();
                int widthLastColumn = width - 16;

                g2d.drawLine(widthLastColumn, 0, widthLastColumn , singleRowHeight*table.getRowCount()-singleRowHeight-2);
                g2d.setColor(new Color(0xe3, 0xe3, 0xe3));
                g2d.fillRect(widthLastColumn, singleRowHeight*table.getRowCount()-singleRowHeight-1, 16, rowHeight+2);

                g2d.setColor(new Color(0xe3, 0xe3, 0xe3));
                for(int i=1; i<table.getRowCount()-1; i++)
                {
                    g2d.drawLine(widthLastColumn, (i*(singleRowHeight))-1, width-2 , (i*(singleRowHeight))-1);
                }

            }

        };

        setTableProperties();

        button.addActionListener
        (
            new ActionListener()
            {
                 @Override
                public void actionPerformed(ActionEvent event)
                {
                   int deleteThisRow = table.getSelectedRow();
                   model.removeRow(deleteThisRow);
                   Variable v = tableBoundary.removeVariable(deleteThisRow);
                   wflow.deleteVariable(v);
                }
            }
        );

        table.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseClicked ( MouseEvent me )
            {
                int rowCount = table.getRowCount();
                if(table.getSelectedRow()+1 == rowCount)
                {
                    model.insertRow(rowCount,new Object[]  {"","","","",""});
                    table.setValueAt("<Neu>", rowCount, 0);
                    variableType.setSelectedItem(Variable.SIGVAR_TYPE.BIT);


                    table.setRowSelectionInterval(rowCount-1, rowCount-1);
                    table.editCellAt(rowCount-1, 0);
                    String s = (String) table.getValueAt(rowCount-1, 0);
                    TableCellEditor cedit  = table.getCellEditor(rowCount-1, 0);
                    Component tf = (Component) cedit.getTableCellEditorComponent(table, s, true, rowCount-1, 0);
                    if(tf instanceof JTextField)
                    {
                        JTextField tf2 = (JTextField)tf;
                        tf2.selectAll();
                        tf2.requestFocusInWindow();
                    }
                    Variable variableNew = new Variable(wflow.getGraph(), "test", "x", SIGVAR_TYPE.BIT);
                    tableBoundary.addVariable(variableNew);
                    wflow.insertVariable(variableNew);

                }
            }
        });

        table.addMouseMotionListener( new MouseAdapter()
        {
            @Override
            public void mouseMoved( MouseEvent me )
            {
		Point p = me.getPoint();
		selectedRowNumber = table.rowAtPoint(p);
                selectedColumnNumber = table.columnAtPoint(p);
		ListSelectionModel model = table.getSelectionModel();
		model.setSelectionInterval( selectedRowNumber, selectedRowNumber );
            }
        });



        JScrollPane scrollPane = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        add(scrollPane);

    }

    /**
     * Defines a DefaultTableModel
     *
     * @return the DefaultTableModel
     */
    private DefaultTableModel newTableModel()
    {
        DefaultTableModel dtm = new DefaultTableModel(null, headline)
        {

            // BitLength nur Integer
            @Override
            public Class getColumnClass(int columnIndex)
            {
                switch (columnIndex)
                {
                    case 2:
                        return Integer.class;
                    default:
                        return Object.class;
                }
            }


            @Override
            public boolean isCellEditable(int row,int cols)
            {
              if (row >= (table.getRowCount()-1) )
              {
                    return false;
              }
              return super.isCellEditable(row, cols);
            }
        };
        
        dtm.addTableModelListener(tableModelLister);
        
        return dtm;
    }

    /**
     * sets Properties like width of the Table
     *
     */
    private void setTableProperties()
    {
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(0).setMinWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setMinWidth(55);
        table.getColumnModel().getColumn(1).setMaxWidth(55);
        table.getColumnModel().getColumn(2).setPreferredWidth(45);
        table.getColumnModel().getColumn(2).setMinWidth(45);
        table.getColumnModel().getColumn(2).setMaxWidth(45);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(1);
        table.getColumnModel().getColumn(4).setMaxWidth(1);
        table.getColumnModel().getColumn(4).setMinWidth(15);
        table.setGridColor(new Color(0xe3, 0xe3, 0xe3));
        table.setCellSelectionEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFocusable(false);

        //table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        bRenderer = new ButtonRenderer();
        table.getColumnModel().getColumn(4).setCellRenderer(bRenderer);
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        variableType = new JComboBox(Variable.SIGVAR_TYPE.values());
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(variableType));
    }

    /************** Button adden ************/

    class ButtonRenderer extends DeleteButton implements TableCellRenderer
    {
        public ButtonRenderer()
        {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column)
            {
                setText((value == null) ? "" : value.toString());

                if(row == selectedRowNumber && column == selectedColumnNumber)
                {
                        setIcon(new ImageIcon(getClass().getResource("/Resources/tableDeleteRolloverIcon.png")));
                }
                else if(row == selectedRowNumber)
                {
                    setIcon(new ImageIcon(getClass().getResource("/Resources/tableDeleteIcon.png")));

                }
                else
                {
                    setIcon(new ImageIcon(getClass().getResource("/Resources/tableDeleteEmptyIcon.png")));
                }

                return this;
            }

    }

    class ButtonEditor extends DefaultCellEditor
    {
        private String label;

        public ButtonEditor(JCheckBox checkBox)
        {
            super(checkBox);
            setClickCountToStart(1);
        }


        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column)
        {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setVisible(false); // wenn true -> schoener Button, aber leider outOfBounds =(
            return button;
        }

        @Override
        public Object getCellEditorValue()
        {
            String temp = label;
            return temp;
        }
    }

    public class myTableModel extends DefaultTableModel
    {
        String dat;
        JButton button=new JButton("");

        myTableModel(String tname)
        {
            super(null,headline);
            //dat=tname;
        }

        @Override
        public boolean isCellEditable(int row,int cols)
        {
            if((dat.equals("owntable")))
            {
                if(cols==0)
                {
                    return false;
                }
            }
            return true;
        }
   }

    public class DeleteButton extends JButton
    {
       public DeleteButton()
       {
            setRolloverEnabled(true);
            setRolloverIcon(new ImageIcon(getClass().getResource("/Resources/tableDeleteRolloverIcon.png")));
            setPressedIcon(new ImageIcon(getClass().getResource("/Resources/tableDeletePressedIcon.png")));
            setDisabledIcon(new ImageIcon(getClass().getResource("/Resources/tableDeleteIcon.png")));
            setSelectedIcon(new ImageIcon(getClass().getResource("/Resources/tableDeleteRolloverIcon.png")));
            setDisabledSelectedIcon(new ImageIcon(getClass().getResource("/Resources/tableDeleteIcon.png")));
            setIcon(new ImageIcon(getClass().getResource("/Resources/tableDeleteEmptyIcon.png")));
            setBorder(new EmptyBorder(0,0,0,0));
            setFocusPainted(false);
       }
   }

    public class DirComboBoxEditor extends DefaultCellEditor
    {
        public DirComboBoxEditor(String[] items)
        {
            super(new JComboBox(items));
        }
   }

    public class TypeComboBoxEditor extends DefaultCellEditor
    {
        public TypeComboBoxEditor(String[] items)
        {
            super(new JComboBox(items));
        }
   }

    /**
    * fills the table from graph
    */
    public void fillTableFromGraph()
    {
       model = newTableModel();
       table.setModel(model);

       setTableProperties();

       LinkedList<Variable> variableList = wflow.getGraph().getVariables();

       Iterator<Variable> it = variableList.iterator();
       while(it.hasNext())
       {
           tableBoundary.addVariable(it.next());
       }



       Iterator<Variable> itSig = tableBoundary.getVariables().iterator();
       Variable v;
       while(itSig.hasNext())
       {
           v = itSig.next();
           Object row[] = {v.getName(), v.getType(), v.getBitLength(), v.getDescription()};
           model.addRow(row);
       }


       Object[] emptyRow = {"","","","",""};
       model.addRow(emptyRow);
       model.setValueAt("<Neu>", model.getRowCount()-1, 0);

   }

    /**
    * clears the data in the boundary of the table
    *
    */
    public void clearBoundary()
    {
      int size = tableBoundary.getVariables().size();

      for(int i=0; i<size ;i++){
        tableBoundary.removeVariable(0);
      }
   }

}
