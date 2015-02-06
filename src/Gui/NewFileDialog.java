/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Gui
 * Class:       NewFileDialog
 * Created:     2011-11-02
 */
package Gui;

import Graph.Graph.GRAPH_TYPE;
import Gui.Boundary.NewFileBoundary;
import Workflow.Workflow;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.*;

import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;

/**
* opens a Dialog to create a new file
*
* @author Jan Montag
*/
public class NewFileDialog extends JDialog {

    private NewFileBoundary boundary = new NewFileBoundary();
    private Workflow wflow;
    private GuiMain guiMain;

    protected ButtonGroup buttonGroupAutomatenmodell;
    protected JButton buttonOk, buttonCancel;
    protected JLabel labelName, labelHeight, labelWidth;
    protected JPanel panelOptions, panelGraphType;//, panelTop;
    protected JRadioButton radioButtonMoore, radioButtonMealy;
    protected JScrollPane jScrollPane1, jScrollPane2, jScrollPane3;
    protected JTextField textName, textWidth, textHeigth;

    /** Creates new form CustomDialog */
    public NewFileDialog(JFrame frame, GuiMain guiMain) {
       super(frame);
       super.setTitle("Neu");
       super.setPreferredSize(new Dimension(300,240));
       super.setModal(true);
       super.setResizable(false);
       pack();
       initComponents();
       pack();
       setLocationRelativeTo(frame);
       this.guiMain = guiMain;
       wflow = guiMain.getWorkflow();

       //default name
       boundary.setName("Unbenannt");

       setVisible(true);
    }

    /**
    * initializes the components
    * @author Jan Montag
    */
    private void initComponents()
    {
        Container container = getContentPane();

        labelName = new JLabel("Name: ");
        labelName.setFont(new java.awt.Font("Arial", 0, 12));
        labelName.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 0, 0, 0));

        textName = new JTextField();
        textName.setText("Unbenannt");
        textName.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(127, 127, 127)), new EmptyBorder(new Insets(0, 4, 0, 0))));
        Dimension dim = new Dimension(100,20);
        textName.setMaximumSize(dim);
        textName.setMinimumSize(dim);
        textName.setPreferredSize(dim);
        textName.setSize(dim);


        labelHeight = new JLabel("Höhe:");
        labelHeight.setFont(new java.awt.Font("Arial", 0, 12));
        labelHeight.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 0, 0, 0));

        labelWidth = new JLabel("Breite:");
        labelWidth.setFont(new java.awt.Font("Arial", 0, 12));
        labelWidth.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 0, 0, 0));

        textHeigth = new JTextField();
        textHeigth.setText("600");
        textHeigth.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(127, 127, 127)), new EmptyBorder(new Insets(0, 4, 0, 0))));
        textHeigth.setColumns(6);
        textHeigth.setMaximumSize(dim);
        textHeigth.setMinimumSize(dim);
        textHeigth.setPreferredSize(dim);
        textHeigth.setSize(dim);

        textWidth = new JTextField();
        textWidth.setText("800");
        textWidth.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(127, 127, 127)), new EmptyBorder(new Insets(0, 4, 0, 0))));
        textWidth.setColumns(6);
        textWidth.setMaximumSize(dim);
        textWidth.setMinimumSize(dim);
        textWidth.setPreferredSize(dim);
        textWidth.setSize(dim);

        panelOptions = new JPanel();
        panelOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "  Optionen: ", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Arial", 0, 12), new java.awt.Color(0, 0, 0))); // NOI18N
        panelOptions.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        panelOptions.setFont(new java.awt.Font("Arial", 0, 12));
        panelOptions.setName("Optionen");
        panelOptions.setPreferredSize(new java.awt.Dimension(98, 45));

        panelGraphType = new JPanel();
        panelGraphType.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "  Automatenmodell: ", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Arial", 0, 12)));

        buttonGroupAutomatenmodell = new ButtonGroup();
        radioButtonMoore = new JRadioButton();
        radioButtonMoore.setFont(new java.awt.Font("Arial", 0, 11));
        radioButtonMoore.setSelected(true);
        radioButtonMoore.setText("Moore");

        radioButtonMealy = new JRadioButton();
        radioButtonMealy.setFont(new java.awt.Font("Arial", 0, 11));
        radioButtonMealy.setText("Mealy");

        buttonGroupAutomatenmodell.add(radioButtonMoore);
        buttonGroupAutomatenmodell.add(radioButtonMealy);

        buttonOk = new JButton("      OK      ");
        buttonOk.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okIsPresssed();
            }
        });

        buttonCancel = new JButton("Abbrechen");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVisible(false);
            }
        });


        BoxLayout layout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
        setLayout(layout);

        container.add(Box.createVerticalStrut(10));

        Box box = Box.createHorizontalBox();
        box.add(labelName);
        box.add(textName);
        container.add(box);

        container.add(Box.createVerticalStrut(10));

        panelOptions.setMaximumSize(new Dimension(260,130));
        panelOptions.setMinimumSize(new Dimension(260,130));
        panelOptions.setPreferredSize(new Dimension(260,130));
        panelOptions.setSize(new Dimension(260,130));
        panelOptions.add(labelHeight);
        panelOptions.add(textHeigth);
        panelOptions.add(labelWidth);
        panelOptions.add(textWidth);
        panelOptions.add(panelGraphType);
        panelGraphType.add(radioButtonMoore);
        panelGraphType.add(radioButtonMealy);
        container.add(panelOptions);

        container.add(Box.createVerticalStrut(10));

        Box b = Box.createHorizontalBox();
        b.add(buttonOk);
        b.add(buttonCancel);
        container.add(b);

        container.add(Box.createVerticalStrut(10));
    }

    /**
    * returns the newfileboundary
    *
    * @return the newfileboundary
    *
    * @author Jan Montag
    */
    public NewFileBoundary getBoundary() {
        return boundary;
    }

    /**
    * sets newfileboundary
    *
    * @param boundary the newfileboundary
    *
    * @author Jan Montag
    */
    public void setBoundary(NewFileBoundary boundary) {
        this.boundary = boundary;
    }

    /**
    * executes different commands like clearing the boundaries when ok is pressed
    *  and a new file is created
    *
    * @author Jan Montag
    */
    public void okIsPresssed()
    {
        boundary.setName(textName.getText().toString());
        //guiMain.setTitle(boundary.getName()+" - "+"State Transition Diagram Editor   2011/12 Jan Montag, Andreas Schwenk");
        guiMain.getWorkflow().getGraph().setName(boundary.getName());

        if(radioButtonMoore.isSelected()){
            boundary.setType(GRAPH_TYPE.MOORE);
        }
        else if(radioButtonMealy.isSelected()){
            boundary.setType(GRAPH_TYPE.MEALY);
        }
        
        int width=600, height=400;
        
        try 
        {
            width = Integer.parseInt(textWidth.getText().toString());
        } 
        catch(Exception e) 
        { 
            JOptionPane.showMessageDialog(this, "Bitte einen numerischen Wert für 'Breite' eingeben!"); 
            return; 
        }

        try 
        {
            height = Integer.parseInt(textHeigth.getText().toString());
        } 
        catch(Exception e) 
        { 
            JOptionPane.showMessageDialog(this, "Bitte einen numerischen Wert für 'Höhe' eingeben!"); 
            return; 
        }

        boundary.setWidth(width);
        boundary.setHeight(height);

        wflow.newFile(getBoundary());

        // left table
        guiMain.guiTableSignals1.clearBoundary();
        guiMain.guiTableSignals1.fillTableFromGraph();
        guiMain.guiTableVariables1.clearBoundary();
        guiMain.guiTableVariables1.fillTableFromGraph();
        guiMain.guiTableStates1.clearBoundary();
        guiMain.guiTableStates1.fillTableFromGraph();

        // right table
        guiMain.guiTableSignals2.clearBoundary();
        guiMain.guiTableSignals2.fillTableFromGraph();
        guiMain.guiTableVariables2.clearBoundary();
        guiMain.guiTableVariables2.fillTableFromGraph();
        guiMain.guiTableStates2.clearBoundary();
        guiMain.guiTableStates2.fillTableFromGraph();

        guiMain.changeTools();
        setVisible(false);
    }

}
