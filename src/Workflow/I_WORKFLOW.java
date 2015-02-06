/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Workflow
 * Interface:   I_WORKFLOW
 * Created:     2011-10-28
 */

package Workflow;

import Graph.Graph;
import Graph.Signal;
import Graph.Variable;
import Gui.Boundary.GuiPreferencesBoundary;
import Gui.Boundary.NewFileBoundary;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * see class: Workflow
 *
 * @author Jan Montag
 */
public interface I_WORKFLOW
{
    // file-IO
    public void newFile(NewFileBoundary boundary);
    public void loadFile(File file) throws IOException;
    public void saveFile(File file) throws FileNotFoundException, IOException;
        
    // insertion
    public void insertState();
    public void insertTransition();
    public void insertStartNode();
    public void insertSignal(Signal signal);
    public void insertVariable(Variable variable);
     
    // deletion
    public void deleteSelectedComponents();
    public void deleteSignal(Signal signal);
    public void deleteVariable(Variable variable);
    
    // transition manipulation
    public void incCtrlPoints();
    public void decCtrlPoints();

    // mouse and keyboard
    public void mouseMoved();
    public void mouseDragged(Point dragStart);
    public void keyEvent(KeyEvent e);
    public void leftMouseButtonClicked();
    public void leftMouseButtonPressed();
    public void leftMouseButtonReleased();
    public void shiftKeyHold(boolean value);

    // get
    public Graph getGraph();
    public String getLogString();
    
    // set
    public void setSelectionMode();
    public void setInsertMode();
    
    // generation
    public void exportAsSCXML(File file);
    
    public void verifyGraph();
    public void generateCode_C(File file_h, File file_c, File file_e );
    public void generateCode_VHDL(File file);
    
    public GuiPreferencesBoundary loadPreferences();
    public void savePreferences(GuiPreferencesBoundary boundary);
}
