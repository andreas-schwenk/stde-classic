/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Gui
 * Class:       GuiPreferences
 * Created:     2012-01-14
 */
package Gui;

import Graph.Graph.GRAPH_TYPE;
import Gui.Boundary.GuiPreferencesBoundary;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

    /**
    * A Window to set properties of the program
    *
    * @author Jan Montag
    */
public class GuiPreferences extends JFrame
{
    private GuiMain guiMain;
    private Container c;
            
    protected JTextField projectName, projectWidth, projectHeight;
    protected JTextField projectPath, exportPath;

    protected JRadioButton rbMoore, rbMealy;
    
    protected JCheckBox cbUseVhdlProcess;
    
    protected JButton bOK, bCancel;
    protected JButton bBrowseProjectDir, bBrowseExportDir;
    
    private GuiPreferencesBoundary boundary;
    
    private int currentLine = 0;

    private class GuiPreferencesListener implements ActionListener
    {
        GuiPreferences guiPreferences;
        
        public GuiPreferencesListener(GuiPreferences guiPreferences)
        {
            this.guiPreferences = guiPreferences;
        }
        
        @Override
        public void actionPerformed(ActionEvent ae)
        {
            Object src = ae.getSource();

            if(src == bOK)
            {
                int width=600, height=400;
                
                try 
                {
                    width = Integer.parseInt(projectWidth.getText().toString());
                } 
                catch(Exception e) 
                { 
                    JOptionPane.showMessageDialog(guiPreferences, 
                            "Bitte einen numerischen Wert für 'Breite' eingeben!"); 
                    return; 
                }
                
                try 
                {
                    height = Integer.parseInt(projectHeight.getText().toString());
                } 
                catch(Exception e) 
                { 
                    JOptionPane.showMessageDialog(guiPreferences, 
                            "Bitte einen numerischen Wert für 'Höhe' eingeben!"); 
                    return; 
                }
                
                boundary.setProjectName(projectName.getText());
                boundary.setProjectWidth(width);
                boundary.setProjectHeight(height);
                boundary.setProjectPath(projectPath.getText());
                boundary.setExportPath(exportPath.getText());
                boundary.setVhdlUseProcess(cbUseVhdlProcess.isSelected());
                
                guiMain.getWorkflow().savePreferences(boundary);
                guiPreferences.setVisible(false);
            }
            else if(src == bCancel)
            {
                guiPreferences.setVisible(false);
            }
            else if(src == bBrowseProjectDir)
            {
                JFileChooser openDialog = new JFileChooser();
                openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if(boundary.getProjectPath() != null)
                    openDialog.setCurrentDirectory(new File(boundary.getProjectPath()));
                if(openDialog.showOpenDialog(guiPreferences) == JFileChooser.APPROVE_OPTION)
                {
                    projectPath.setText(openDialog.getSelectedFile().toString());
                }
            }
            else if(src == bBrowseExportDir)
            {
                JFileChooser openDialog = new JFileChooser();
                openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if(boundary.getExportPath() != null)
                    openDialog.setCurrentDirectory(new File(boundary.getExportPath()));
                if(openDialog.showOpenDialog(guiPreferences) == JFileChooser.APPROVE_OPTION)
                {
                    exportPath.setText(openDialog.getSelectedFile().toString());
                }
            }
        }
    }

    /**
    * adds a Headline
    *
    * @param the text of the headline
    *
    * @author Jan Montag
    */
    private void addHeadline(String text)
    {
        GridBagConstraints constr = new GridBagConstraints();
        constr.gridx = 0;
        constr.gridy = currentLine;
        constr.gridwidth = 3;
        constr.gridheight = 1;
        constr.weightx = 1.0;
        constr.fill = GridBagConstraints.HORIZONTAL;
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        c.add(label, constr);
        currentLine ++;
    }

    /**
    * adds a Line consiting of a text, a guiComponent and a button
    *
    * @param labelText text of the lable
    * @param guiComponent guiComponent
    * @param button a JButton
    *
    * @author Jan Montag
    */
    private void addLine(String labelText, Component guiComponent, JButton button)
    {
        GridBagConstraints constr = new GridBagConstraints();
        constr.gridx = 0;
        constr.gridy = currentLine;
        constr.gridwidth = 1;
        constr.gridheight = 1;
        constr.fill = GridBagConstraints.HORIZONTAL;
        JLabel label = new JLabel(labelText);
        c.add(label, constr);
        
        constr = new GridBagConstraints();
        constr.gridx = 1;
        constr.gridy = currentLine;
        if(button == null)
            constr.gridwidth = 2;
        else
        {
            constr.gridwidth = 1;
            constr.weightx = 1;
        }
        constr.gridheight = 1;
        constr.fill = GridBagConstraints.HORIZONTAL;
        label = new JLabel(labelText);
        c.add(guiComponent, constr);
        
        if(button != null)
        {
            constr = new GridBagConstraints();
            constr.gridx = 2;
            constr.gridy = currentLine;
            constr.gridwidth = 1;
            constr.gridheight = 1;
            constr.weightx = 0;
            constr.fill = GridBagConstraints.HORIZONTAL;
            c.add(button, constr);
        }
        
        currentLine ++;
    }

    /**
    * adds two buttons
    *
    * @param b1 first button
    * @param b2 second button
    *
    * @author Jan Montag
    */
    private void addButtons(JButton b1, JButton b2)
    {
        Box box = Box.createHorizontalBox();
        box.add(b1);
        box.add(b2);
        
        GridBagConstraints constr = new GridBagConstraints();
        constr.gridx = 0;
        constr.gridy = currentLine;
        constr.gridwidth = 3;
        constr.gridheight = 1;
        constr.fill = GridBagConstraints.HORIZONTAL;
        c.add(box, constr);
        
        currentLine ++;
    }    

    /**
    * constructor og guiPreferences
    *
    * @param guiMain the guiMain
    * @param boundary the boundary with stored data
    *
    * @author Jan Montag
    */
    public GuiPreferences(GuiMain guiMain, GuiPreferencesBoundary boundary)
    {
        super("Einstellungen");
        
        this.guiMain = guiMain;
        
        this.boundary = boundary;
        
        GuiPreferencesListener listener = new GuiPreferencesListener(this);

        c = getContentPane();
        
        GridBagLayout gbl = new GridBagLayout();
        
        setLayout(gbl);
        setSize(600, 300);
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        setLocation(screenSize.width/2-getSize().width/2, screenSize.height/2-getSize().height/2);
        
        addHeadline(" Projekteinstellungen");
        
        projectName = new JTextField();
        addLine("  Projektname", projectName, null);
        
        ButtonGroup graphType;
        rbMoore = new JRadioButton("Moore");
        rbMoore.setSelected(true);
        rbMoore.setEnabled(false);
        rbMealy = new JRadioButton("Mealy");
        rbMealy.setEnabled(false);
        graphType = new ButtonGroup();
        graphType.add(rbMoore);
        graphType.add(rbMealy);
        Box box = Box.createHorizontalBox();
        box.add(rbMoore);
        box.add(rbMealy);
        
        addLine("  Automaten-Typ", box, null);
        
        projectWidth = new JTextField();
        addLine("  Zeichenblatt-Breite:", projectWidth, null);

        projectHeight = new JTextField();
        addLine("  Zeichenblatt-Höhe:", projectHeight, null);
        
        
        addHeadline(" "); // empty line

        addHeadline(" Allgemeine Einstellungen");

        bBrowseProjectDir = new JButton("...");
        bBrowseProjectDir.addActionListener(listener);
        projectPath = new JTextField();
        addLine("  Projektepfad:", projectPath, bBrowseProjectDir);
            
        bBrowseExportDir = new JButton("...");
        bBrowseExportDir.addActionListener(listener);
        exportPath = new JTextField();
        addLine("  Exportpfad:", exportPath, bBrowseExportDir);

        addHeadline(" "); // empty line

        cbUseVhdlProcess = new JCheckBox("Logik im VHDL-Prozess? (falls 'nein': Variablen nicht erlaubt)", false);
        cbUseVhdlProcess.addActionListener(listener);
        addLine("  VHDL:", cbUseVhdlProcess, null);

        bOK = new JButton("OK");
        bOK.addActionListener(listener);
        
        bCancel = new JButton("Abbrechen");
        bCancel.addActionListener(listener);
        
        addButtons(bOK, bCancel);
    }

    /**
    * fills the boundary
    *
    * @author Jan Montag
    */
    public void fill(GuiPreferencesBoundary boundary)
    {
        this.boundary = boundary;
        
        projectName.setText(boundary.getProjectName());
        projectWidth.setText("" + boundary.getProjectWidth());
        projectHeight.setText("" + boundary.getProjectHeight());
        projectPath.setText(boundary.getProjectPath());
        exportPath.setText(boundary.getExportPath());
        cbUseVhdlProcess.setSelected(boundary.getVhdlUseProcess());
        
        if(boundary.getGraphType() == GRAPH_TYPE.MOORE)
            rbMoore.setSelected(true);
        else
            rbMealy.setSelected(true);
    }
}
