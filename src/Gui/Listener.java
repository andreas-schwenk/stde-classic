/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   Gui
 * Class:       Listener
 * Created:     2011-10-28
 */
package Gui;

import Graph.Graph;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.*;

/**
* the Main-Listener
*
* @author Jan Montag
*/
public class Listener implements ActionListener, WindowListener
{
    // *** SUBCLASSES ***
    private FileFilter filenameFilterSTDE = new javax.swing.filechooser.FileFilter()
    {
        @Override
        public boolean accept(File f) {
            return f.getName().endsWith(".stde");
        }

        @Override
        public String getDescription() {
            return "STDE-Projekt";
        }
    };
    
    private FileFilter filenameFilterC = new javax.swing.filechooser.FileFilter()
    {
        @Override
        public boolean accept(File f) {
            return f.getName().endsWith(".c");
        }

        @Override
        public String getDescription() {
            return "C-Datei";
        }
    };
        
    private FileFilter filenameFilterVHDL = new javax.swing.filechooser.FileFilter()
    {
        @Override
        public boolean accept(File f) {
            return f.getName().endsWith(".vhd");
        }

        @Override
        public String getDescription() {
            return "VHDL-Datei";
        }
    };
            
    private FileFilter filenameFilterSCXML = new javax.swing.filechooser.FileFilter()
    {
        @Override
        public boolean accept(File f) {
            return f.getName().endsWith(".txt");
        }

        @Override
        public String getDescription() {
            return "SCXML-Datei";
        }
    };
    
    private FileFilter filenameFilterPNG = new javax.swing.filechooser.FileFilter()
    {
        @Override
        public boolean accept(File f) {
            return f.getName().endsWith(".png");
        }

        @Override
        public String getDescription() {
            return "PNG-Bild";
        }
    };
    
    // *** ATTRIBUTES ***
    private GuiMain guiMain;
    private JFileChooser saveDialog;
    
    private File projectFile = null;
    
    // *** METHODS ***

    /**
    * the standard-constructor
    */
    public Listener(GuiMain guiMain)
    {
        this.guiMain = guiMain;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) 
    {
        Object src = ae.getSource();
        
        if(src == guiMain.bNewFile)
        {
            newFile();
        }
        else if(src == guiMain.bOpenFile)
        {
            openFile();
        }
        else if(src == guiMain.bSaveFile)
        {
            saveFile();
        }
        else if(src == guiMain.bExportImage)
        {
            exportAsImage();
        }
        else if(src == guiMain.bInsertState)
        {
            guiMain.workflow.insertState();
        }
        else if(src == guiMain.bInsertTransition)
        {
            guiMain.workflow.insertTransition();
        }
        else if(src == guiMain.bInsertStartNode)
        {
            guiMain.workflow.insertStartNode();
        }
        else if(src == guiMain.btInsert)
        {
            guiMain.btInsert.setSelected(true);
            guiMain.btSelect.setSelected(false);
            guiMain.bInsertState.setEnabled(true);
            guiMain.bInsertTransition.setEnabled(true);
            guiMain.bInsertStartNode.setEnabled(true);
            guiMain.workflow.setInsertMode();
        }
        else if(src == guiMain.btSelect)
        {
            guiMain.btSelect.setSelected(true);
            guiMain.btInsert.setSelected(false);
            guiMain.bInsertState.setEnabled(false);
            guiMain.bInsertTransition.setEnabled(false);
            guiMain.bInsertStartNode.setEnabled(false);
            guiMain.workflow.setSelectionMode();
        }
        else if(src == guiMain.btGrid)
        {
            guiMain.graphicsPanel.setIsRenderGrid(guiMain.btGrid.isSelected());
        }
        else if(src == guiMain.bDelete)
        {
            guiMain.workflow.deleteSelectedComponents();
            guiMain.guiTableStates1.clearBoundary();
            guiMain.guiTableStates1.fillTableFromGraph();
            guiMain.guiTableStates2.clearBoundary();
            guiMain.guiTableStates2.fillTableFromGraph();
        }
        else if(src == guiMain.bSelectAll)
        {
            guiMain.workflow.getGraph().selectAll();
        }
        else if(src == guiMain.bPreferences)
        {
            Graph g = guiMain.getWorkflow().getGraph();
            
            guiMain.guiPreferences.fill(guiMain.workflow.loadPreferences());
            
            guiMain.guiPreferences.setVisible(true);
        }
        else if(src == guiMain.bVerify)
        {
            guiMain.getWorkflow().verifyGraph();
            guiMain.log.setText(guiMain.log.getText() + "\n----------------------------\n" 
                     + guiMain.getWorkflow().getLogString());
        }
        else if(src == guiMain.bSCXML)
        {
            if(exportAsSCXML())
            {
                guiMain.log.setText(guiMain.log.getText() + "\n----------------------------\n" 
                         + guiMain.getWorkflow().getLogString());
            }
        }
        else if(src == guiMain.bGenerateC)
        {
            if(exportAsC())
            {
                guiMain.log.setText(guiMain.log.getText() + "\n----------------------------\n" 
                         + guiMain.getWorkflow().getLogString());
            }
        }
        else if(src == guiMain.bGenerateVHDL)
        {
            if(exportAsVHDL())
            {
                guiMain.log.setText(guiMain.log.getText() + "\n----------------------------\n" 
                         + guiMain.getWorkflow().getLogString());
            }
        }
        else if(src == guiMain.bIncTransCtrlPoints)
        {
            guiMain.getWorkflow().incCtrlPoints();
        }
        else if(src == guiMain.bDecTransCtrlPoints)
        {
            guiMain.getWorkflow().decCtrlPoints();
        }

        guiMain.graphicsPanel.repaint();
    }

    /**
    * opens a fileDialog and clears old data in the boundaries
    *
    * @author Jan Montag
    */
    public void newFile()
    {
        projectFile = null;
        int i = guiMain.confirmationDialog("Möchten Sie die Änderungen vor dem Anlegen eines neuen STDE-Projekts speichern?");
        if(i == 0)
        {
            this.saveFileAs();
            NewFileDialog tmpDialog = new NewFileDialog(guiMain, guiMain);
        }
        else if(i == 1)
        {
            NewFileDialog tmpDialog = new NewFileDialog(guiMain, guiMain);
        }
    }

    /**
    * open a file. opens a open-file-dialog
    *
    * @author Jan Montag
    */
    public void openFile()
    {
        try 
        {
            int i = guiMain.confirmationDialog("Möchten Sie die Änderungen vor dem Anlegen eines neuen STDE-Projekts speichern?");
            boolean open=false;
            if(i == 0)
            {
                this.saveFileAs();
                open = true;
            }
            else if(i == 1)
            {
                open = true;
            }

            if(open)
            {
                JFileChooser openDialog = new JFileChooser();
                
                // set file-filtering
                openDialog.addChoosableFileFilter(filenameFilterSTDE);
                
                // set standard-path if defined
                String path = guiMain.getWorkflow().getGuiPreferencesBoundary().getProjectPath();
                if(!path.equals(""))
                    openDialog.setCurrentDirectory(new File(path));
                
                // open dialog
                if(openDialog.showOpenDialog(guiMain) == JFileChooser.APPROVE_OPTION)
                {
                    projectFile = openDialog.getSelectedFile();
                    guiMain.workflow.loadFile(projectFile);
                    guiMain.changeTools();

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
                    // states (left and right)
                    refreshStatesTable();

                    guiMain.changeTitleByProjectName(guiMain.workflow.getGraph().getName());
                }
            }

        } 
        catch (IOException e) 
        {
            System.out.println("guiMain.workflow.loadFile(..) failed; "
                    + e.getMessage());
        }
    }

    /**
    * saves the data in a file
    *
    * @author Jan Montag
    */
    public void saveFile()
    {
        if(projectFile == null)
            saveFileAs();
        else
        {
            try
            {
                guiMain.workflow.saveFile(projectFile);
            }
            catch (IOException e)
            {
                System.out.println("guiMain.workflow.saveFile(..) failed; "
                        + e.getMessage());
            }            
        }        
    }

    /**
    * opens a dialog to save the data at a specific directory that can be chosen
    *
    * @author Jan Montag
    */
    public void saveFileAs()
    {
        try
        {
            saveDialog = new JFileChooser()
            {
                @Override
                public void approveSelection()
                {
                    File f = getSelectedFile();
                    if(f.exists() && getDialogType() == SAVE_DIALOG)
                    {
                        int result = JOptionPane.showConfirmDialog(this,
                                "Diese Datei ist bereits vorhanden. Möchten Sie sie ersetzen?",
                                "STDE",JOptionPane.YES_NO_CANCEL_OPTION);
                        switch(result)
                        {
                            case JOptionPane.YES_OPTION:
                                super.approveSelection();
                                return;
                            case JOptionPane.NO_OPTION:
                                return;
                            case JOptionPane.CANCEL_OPTION:
                                super.cancelSelection();
                                return;
                        }
                    }
                    super.approveSelection();
                }

            };

            String filename = saveDialog.getSelectedFile() + File.separator + guiMain.getWorkflow().getGraph().getName();
            saveDialog.setSelectedFile(new File(filename));

            // set standard-path if defined
            String path = guiMain.getWorkflow().getGuiPreferencesBoundary().getProjectPath();
            if(path != null && !path.equals(""))
                saveDialog.setCurrentDirectory(new File(path));
            
            // set file-filtering
            saveDialog.addChoosableFileFilter(filenameFilterSTDE);
            
            if(saveDialog.showSaveDialog(guiMain) == JFileChooser.APPROVE_OPTION)
            {
                projectFile = saveDialog.getSelectedFile();
                // file ending
                if(!projectFile.getName().endsWith(".stde"))
                {
                    projectFile = new File(projectFile.getPath() + ".stde");
                }
                guiMain.workflow.saveFile(projectFile);
            }
        }
        catch (IOException e)
        {
            System.out.println("guiMain.workflow.saveFile(..) failed; "
                    + e.getMessage());
        }
    }

    /**
    * exports the project as SCXML
    *
    * @author Jan Montag
    */
    public boolean exportAsSCXML()
    {
        saveDialog = new JFileChooser()
        {
            @Override
            public void approveSelection()
            {
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG)
                {
                    int result = JOptionPane.showConfirmDialog(this,
                            "Diese Datei ist bereits vorhanden. Möchten Sie sie ersetzen?","STDE",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result)
                    {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            super.cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }

        };

        String filename = saveDialog.getSelectedFile() + File.separator + guiMain.getWorkflow().getGraph().getName();
        saveDialog.setSelectedFile(new File(filename));

        // set standard-path if defined
        String path = guiMain.getWorkflow().getGuiPreferencesBoundary().getExportPath();
        if(path != null && !path.equals(""))
            saveDialog.setCurrentDirectory(new File(path));

        // set file-filtering
        saveDialog.addChoosableFileFilter(filenameFilterSCXML);

        if(saveDialog.showSaveDialog(guiMain) == JFileChooser.APPROVE_OPTION)
        {
            File dest = saveDialog.getSelectedFile();
            // file ending
            if(!dest.getName().endsWith(".xml"))
            {
                dest = new File(dest.getPath() + ".xml");
            }
            guiMain.workflow.exportAsSCXML(dest);
            return true;
        }
        return false;
    }

    /**
    * exports the project as C-Code
    *
    * @author Jan Montag
    */
    public boolean exportAsC()
    {
        saveDialog = new JFileChooser()
        {
            @Override
            public void approveSelection()
            {
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG)
                {
                    int result = JOptionPane.showConfirmDialog(this,
                            "Diese Datei ist bereits vorhanden. Möchten Sie sie ersetzen?","STDE",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result)
                    {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            super.cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }

        };

        String filename = saveDialog.getSelectedFile() + File.separator + guiMain.getWorkflow().getGraph().getName();
        saveDialog.setSelectedFile(new File(filename));

        // set standard-path if defined
        String path = guiMain.getWorkflow().getGuiPreferencesBoundary().getExportPath();
        if(path != null && !path.equals(""))
            saveDialog.setCurrentDirectory(new File(path));

        // set file-filtering
        saveDialog.addChoosableFileFilter(filenameFilterC);

        if(saveDialog.showSaveDialog(guiMain) == JFileChooser.APPROVE_OPTION)
        {
            /* we have to strip off the file ending in order to generate 
               the three files
            */
            String filenameS = saveDialog.getSelectedFile().getPath();
            if (filenameS.endsWith(".c")||filenameS.endsWith(".h")) {
                filenameS = filenameS.substring(0, filenameS.length()-2);
            }
            File dest_c = new File(filenameS + ".c");
            File dest_h = new File(filenameS + ".h");
            File dest_e = new File(filenameS + "_exec.c");
            guiMain.workflow.generateCode_C(dest_h, dest_c, dest_e);
            return true;
        }
        return false;
    }

    /**
    * exports the Project as VHDL-Code
    *
    * @author Jan Montag
    */
    public boolean exportAsVHDL()
    {
        saveDialog = new JFileChooser()
        {
            @Override
            public void approveSelection()
            {
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG)
                {
                    int result = JOptionPane.showConfirmDialog(this,
                            "Diese Datei ist bereits vorhanden. Möchten Sie sie ersetzen?","STDE",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result)
                    {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            super.cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }

        };

        String filename = saveDialog.getSelectedFile() + File.separator + guiMain.getWorkflow().getGraph().getName();
        saveDialog.setSelectedFile(new File(filename));

        // set standard-path if defined
        String path = guiMain.getWorkflow().getGuiPreferencesBoundary().getExportPath();
        if(path != null && !path.equals(""))
            saveDialog.setCurrentDirectory(new File(path));

        // set file-filtering
        saveDialog.addChoosableFileFilter(filenameFilterVHDL);

        if(saveDialog.showSaveDialog(guiMain) == JFileChooser.APPROVE_OPTION)
        {
            File dest = saveDialog.getSelectedFile();
            // file ending
            if(!dest.getName().endsWith(".vhd"))
            {
                dest = new File(dest.getPath() + ".vhd");
            }
            guiMain.workflow.generateCode_VHDL(dest);
            return true;
        }
        return false;
    }

    /**
    * Exports the GrpahicsPanel as Image
    *
    * @author Jan Montag
    */
    public void exportAsImage()
    {
        try
        {
            saveDialog = new JFileChooser()
            {
                @Override
                public void approveSelection()
                {
                    File f = getSelectedFile();
                    if(f.exists() && getDialogType() == SAVE_DIALOG)
                    {
                        int result = JOptionPane.showConfirmDialog(this,"Diese Datei ist bereits vorhanden. Möchten Sie sie ersetzen?","STDE",JOptionPane.YES_NO_CANCEL_OPTION);
                        switch(result)
                        {
                            case JOptionPane.YES_OPTION:
                                super.approveSelection();
                                return;
                            case JOptionPane.NO_OPTION:
                                return;
                            case JOptionPane.CANCEL_OPTION:
                                super.cancelSelection();
                                return;
                        }
                    }
                    super.approveSelection();
                }

            };
            
            String filename = saveDialog.getSelectedFile() + File.separator + guiMain.getWorkflow().getGraph().getName();
            saveDialog.setSelectedFile(new File(filename));

            // set standard-path if defined
            String path = guiMain.getWorkflow().getGuiPreferencesBoundary().getProjectPath();
            if(path != null && !path.equals(""))
                saveDialog.setCurrentDirectory(new File(path));
            
            // set file-filtering
            saveDialog.addChoosableFileFilter(filenameFilterPNG);
            
            if(saveDialog.showSaveDialog(guiMain) == JFileChooser.APPROVE_OPTION)
            {
                File file = saveDialog.getSelectedFile();
                // file ending
                if(!file.getName().endsWith(".png"))
                {
                    file = new File(projectFile.getPath() + ".png");
                }
                guiMain.graphicsPanel.exportImage(file);
                guiMain.log.setText(guiMain.log.getText() + "\nImage gespeichert\n");
                guiMain.graphicsPanel.repaint();
            }
        }
        catch (IOException e)
        {
            System.out.println("guiMain.workflow.saveFile(..) failed; "
                    + e.getMessage());
        }
    }

    /**
    * refreshs the state table
    *
    */
    public void refreshStatesTable()
    {
        // left table
        guiMain.guiTableStates1.clearBoundary();
        guiMain.guiTableStates1.fillTableFromGraph();
        // right table
        guiMain.guiTableStates2.clearBoundary();
        guiMain.guiTableStates2.fillTableFromGraph();        
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        int i = guiMain.confirmationDialog("Möchten Sie die Änderungen vor dem Beenden des STDE speichern?");
        if(i == 0)
        {
            saveFileAs();
        }
        else if(i == 1)
        {
            System.exit(0);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {
       
    }
    
}
