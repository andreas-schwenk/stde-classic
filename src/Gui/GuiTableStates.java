/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Gui
 * Class:       GuiTableStates
 * Created:     ?
 */

package Gui;

import Graph.Component;
import Graph.State;
import Graph.Transition;
import Gui.Boundary.GuiTableBoundary;
import Workflow.Workflow;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

/**
 * GuiTable for States
 *
 * @author Jan Montag
 */
public class GuiTableStates extends JPanel {

    private JTable table;

    private Workflow wflow;

    private DefaultTableModel dtm;

    private Object[] headline = {"Name", "Beschreibung"};
    String[][] rowData = null;

    private GuiTableBoundary tableBoundary;

    private TableModelListener tableModelLister;
    
    private int selectedRowNumber, selectedColumnNumber;

    /**
     * standard-constructor
     *
     * @param guiMain
     *
     * @author Jan Montag
     */
    public GuiTableStates(GuiMain guiMain)
    {
        wflow = guiMain.getWorkflow();
        tableBoundary = new GuiTableBoundary();

        setLayout(new GridLayout(1,1));

        dtm = new DefaultTableModel(rowData, headline);
        
        tableModelLister = new TableModelListener() 
        {
            @Override
            public void tableChanged(TableModelEvent tme)
            {
                int editedRow = tme.getFirstRow();
                int editedColumn = tme.getColumn();

                if(editedRow == -1 || editedColumn == -1)
                  return;               
                
                //System.out.println("table changed: "+table.getSelectedRow()+","+table.getSelectedColumn());
                
                String cellValueString = null;
                State tState;
                
                Object cellValue = table.getValueAt(editedRow, editedColumn);

                if(cellValue instanceof String)
                {
                    cellValueString = (String)table.getValueAt(editedRow, editedColumn);
                }

                if(editedRow < (tableBoundary.getStates().size()))
                {
                    tState = tableBoundary.getStates().get(editedRow);
                    
                    switch(editedColumn)
                    {
                        case 1: tState.setDescription(cellValueString);
                            break;
                    }
                }
            }
        };
        
        dtm.addTableModelListener(tableModelLister);
        
        table = new JTable(dtm)
        {
            @Override
            public boolean isCellEditable(int row, int col)
            {
              if (col == 0 ) // state name
              {
                    return false;
              }
              return super.isCellEditable(row, col);
            }

        };
        
        table.addMouseMotionListener(new MouseAdapter()
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

        setTableProperties();

        JScrollPane scrollPane = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        add(scrollPane);
    }

    /**
     * sets Properties like width of the Table
     *
     */
    private void setTableProperties()
    {
        table.setGridColor(new Color(0xe3, 0xe3, 0xe3));
        table.setCellSelectionEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFocusable(false);
    }

    /**
    * clears the data in the boundary of the table
    *
    */
    public void clearBoundary()
    {
        int size = tableBoundary.getStates().size();

        for(int i=0; i<size ;i++){
            tableBoundary.removeState(0);
        }
    } 

    /**
    * fills the table from graph
    */
    public void fillTableFromGraph()
    {
       dtm = new DefaultTableModel(rowData, headline);
       
       dtm.addTableModelListener(tableModelLister);
       
       table.setModel(dtm);

       Graph.Component component;

       for(Iterator it=wflow.getGraph().getComponents().iterator(); it.hasNext(); )
       {
            component = (Component)it.next();

            if(component instanceof Transition){}
            else if(component instanceof State)
            {
                State s=(State)component;
                tableBoundary.addState(s);
            }
       }

       Iterator<Graph.State> itState = tableBoundary.getStates().iterator();
       Component s;
       while(itState.hasNext())
       {
           s = itState.next();
           Object row[] = {s.getName(), s.getDescription()};
           dtm.addRow(row);
       }


   }
}
