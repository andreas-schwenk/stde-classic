/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Workflow
 * Class:       Workflow
 * Created:     2011-10-28
 */

package Workflow;

import Generation.Generation;
import Graph.Graph;
import Graph.Signal;
import Graph.State;
import Graph.Transition;
import Graph.Variable;
import Gui.Boundary.GuiPreferencesBoundary;
import Gui.Boundary.NewFileBoundary;
import Gui.GraphicsPanel;
import Gui.GuiMain;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The workflow is the interface between the GUI and generation/graph
 *
 * @author Jan Montag
 */
public class Workflow implements I_WORKFLOW
{

    // *** ENUMERATIONS ***
    public enum GUI_STATE_TYPE
    {
        /** insert-mode **/
        INSERT_MODE,
          // states
        FIND_STATE_POS,
          // transitions
        FIND_TRANSITION_START_STATE, FIND_TRANSITION_TARGET_STATE,
          // start node
        FIND_STARTNODE_START_POS, FIND_STARTNODE_TARGET_STATE,
        /** select-mode **/
        SELECT_MODE
    }    
    
    // *** ATTRIBUTES ***
    private GuiMain guiMain;
    private GraphicsPanel graphicsPanel;
    // temporary compontents
    private State graphState;
    private Transition graphTransition;
    private Transition graphStartNode;
    // gui state (see state-chart)
    private GUI_STATE_TYPE guiState = GUI_STATE_TYPE.INSERT_MODE;
    // graph
    private Graph graph;
    // generation
    private Generation generation;
    // logstring
    private String logString;

    private boolean shiftKey=false;
    
    private boolean isPickerSelected=false;
    
    private GuiPreferencesBoundary guiPreferencesBoundary = new GuiPreferencesBoundary();
    
    // *** METHODS ***

    /**
     * standard-constructor
     *
     * @param guiMain mainGui
     * @param gp the graphicspanel
     *
     * @author Jan Montag
     */
    public Workflow(GuiMain guiMain, GraphicsPanel gp)
    {
        this.guiMain = guiMain;
        this.graphicsPanel = gp;
        this.graph = new Graph();
        this.graphicsPanel.setGraph(graph);
        this.generation = new Generation();
        
        loadPreferences();
    }

    /**
     * sets the size og the graphicspanel
     *
     * @param width the width of gp
     * @param height the heigth of gp
     *
     * @author Jan Montag
     */
    public void setGraphicsPanelSize(int width, int height)
    {
        if(width < 400)
            width = 400;
        if(height < 400)
            height = 400;
        if(width > 2000)
            width = 2000;
        if(height > 2000)
            height = 2000;
        
        graphicsPanel.setMinimumSize(new Dimension(width, height));
        graphicsPanel.setMaximumSize(new Dimension(width, height));
        graphicsPanel.setPreferredSize(new Dimension(width, height));
        graphicsPanel.setSize(width, height);
        
        guiMain.refreshGraphicsPanelScrollPane();

        graphicsPanel.repaint();
    }

    /**
     * clears the boundaries when creating a new file
     *
     * @param boundary the newfileboundary
     *
     * @author Jan Montag
     */
    @Override
    public void newFile(NewFileBoundary boundary)
    {
        graph.setGraphType(boundary.getType());
        graph.deleteAllComponents();
        graph.deleteAllSignals();
        graph.deleteAllVariables();
        graph.setWidth(boundary.getWidth());
        graph.setHeight(boundary.getHeight());

        setGraphicsPanelSize(boundary.getWidth(), boundary.getHeight());
        
        guiMain.changeTitleByProjectName(boundary.getName());
        
        guiMain.clearLogText();
    }

    /**
     * deletes all selected Components
     *
     * @author Jan Montag
     */
    @Override
    public void deleteSelectedComponents()
    {
        if(guiState == GUI_STATE_TYPE.SELECT_MODE)
        {
            graph.deleteComponents(graph.getSelectedComponents());
            graphicsPanel.repaint();
        }
    }

    /**
     * inserts a State
     *
     * @author Jan Montag
     */
    @Override
    public void insertState()
    {
        graph.deselectAll();
        guiState = GUI_STATE_TYPE.FIND_STATE_POS;
        graphState = new State(graph);
        graph.setTemporaryComponent(graphState);
        graph.setIsRenderTemporaryComponent(true);
    }

    /**
     * inserts a Transition
     *
     * @author Jan Montag
     */
    @Override
    public void insertTransition()
    {
        graph.deselectAll();
        guiState = GUI_STATE_TYPE.FIND_TRANSITION_START_STATE;
        graphTransition = new Transition(graph, false);
        graphTransition.setCondition("1"); /* 1 := epsilon move */
    }

    /**
     * inserts a Startnode
     *
     * @author Jan Montag
     */
    @Override 
    public void insertStartNode()
    {
        graph.deselectAll();
        guiState = GUI_STATE_TYPE.FIND_STARTNODE_START_POS;
        graphStartNode = new Transition(graph, true);
        graphStartNode.setCondition("1");
        graph.setTemporaryComponent(graphStartNode);
        graph.setIsRenderTemporaryComponent(true);
    }

    /**
     * recognizes if the mouse is moved and reacts with different operations
     *
     * @author Jan Montag
     */
    @Override
    public void mouseMoved()
    {
        if(graphicsPanel == null)
            return;
        // is mouse in graphicsPanel?
        if(graphicsPanel.getMousePosition()==null)
        {
            graph.setIsRenderTemporaryComponent(false);
            graphicsPanel.repaint();
            return;
        }
        if(graph.getTemporaryComponent() != null)
        {
            // Zustand innerhalb der Zeichenflaeche?
            if(graph.getTemporaryComponent() instanceof State)
            {
                if(graphicsPanel.getMousePosition() == null)
                    return;
                if((graphicsPanel.getMousePosition().x <= graph.getTemporaryComponent().getSize().x/2) ||
                (graphicsPanel.getMousePosition().y <= graph.getTemporaryComponent().getSize().y/2) ||
                (graphicsPanel.getMousePosition().x >= graphicsPanel.getWidth()-graph.getTemporaryComponent().getSize().x/2 ) ||
                (graphicsPanel.getMousePosition().y >= graphicsPanel.getHeight()-graph.getTemporaryComponent().getSize().y/2))
                {
                    graph.setIsRenderTemporaryComponent(false);
                    graphicsPanel.repaint();
                    return;
                }
            }
        }

        // get mouse position?
        Point v = graphicsPanel.getMousePosition();
        
        // ** STATES **
        if(guiState == GUI_STATE_TYPE.FIND_STATE_POS)
        {
            graph.setRenderDockingPoints(false);
            graph.setIsRenderTemporaryComponent(true);
            graphState.setPosition(v);
            graphicsPanel.repaint();
        }
        
        // ** TRANSITIONS **
        else if(guiState == GUI_STATE_TYPE.FIND_TRANSITION_START_STATE)
        {
            graph.setRenderDockingPoints(true);
            graphicsPanel.repaint();
        }
        else if(guiState == GUI_STATE_TYPE.FIND_TRANSITION_TARGET_STATE)
        {
            graph.setRenderDockingPoints(true);
            graphTransition.recalculateGeometry(true, v);
            graphicsPanel.repaint();
        }
        
        // ** START STATE **
        else if(guiState == GUI_STATE_TYPE.FIND_STARTNODE_START_POS)
        {
            graphicsPanel.repaint();
        }
        else if(guiState == GUI_STATE_TYPE.FIND_STARTNODE_TARGET_STATE)
        {
            graph.setRenderDockingPoints(true);
            graphStartNode.recalculateGeometry(true, v);
            graphicsPanel.repaint();
        }
    }

    /**
     * recognizes if the mouse is dragged and reacts with different operations
     *
     * @param dragStart startpoint of the drag
     *
     * @author Jan Montag
     */
    @Override
    public void mouseDragged(Point dragStart)
    {
        if(guiState == GUI_STATE_TYPE.FIND_STATE_POS)
            return;
        
        // is mouse in graphicsPanel?
        Point mousePosition = graphicsPanel.getMousePosition();
        if(mousePosition!=null)
        {
            if(isPickerSelected)
            {
                graph.mouseDragged(mousePosition);
            }
            else
            {
                graphicsPanel.setIsRenderSelectionRect(true);
                graphicsPanel.setSelectionRect(dragStart, mousePosition);
                graph.rectangleSelect(mousePosition, dragStart);
            }
            graphicsPanel.repaint();    
        }
    }

    /**
     * recognizes if the shift key is hold for multiple selecion
     *
     * @param value "shift hold?"
     *
     * @author Jan Montag
     */
    @Override
    public void shiftKeyHold(boolean value)
    {
        shiftKey = value;
    }

    /**
     * recognizes if a key is pressed and reacts with different operations
     *
     * @author Jan Montag
     */
    @Override
    public void keyEvent(KeyEvent e)
    {
        switch(e.getKeyCode())
        {
            case KeyEvent.VK_ESCAPE:
                graph.deselectAll();
                graph.setIsRenderTemporaryComponent(false);
                setSelectionMode();
                break;
        }        
        graph.keyEvent(e);
        guiMain.getListener().refreshStatesTable();
        graphicsPanel.repaint();
    }

    /**
     * recognizes if the left mouse is clicked and reacts with different operations
     * like inserting a state or transition
     *
     * @author Jan Montag
     */
    @Override
    public void leftMouseButtonClicked()
    {
        // ** INSERT MODE **
        if(guiState == GUI_STATE_TYPE.FIND_STATE_POS)
        {
            graph.insertComponent(graphState);
            graphState.select();
            guiState = GUI_STATE_TYPE.SELECT_MODE;
            guiMain.getListener().refreshStatesTable();
        }
        // TRANSITION
        else if(guiState == GUI_STATE_TYPE.FIND_TRANSITION_START_STATE)
        {
            Transition.StateConnection connectedState = graph.pickState(graphicsPanel.getMousePosition());
            if(connectedState != null)
            {
                graphTransition.setFromState(connectedState);
                guiState = GUI_STATE_TYPE.FIND_TRANSITION_TARGET_STATE;
                graph.setTemporaryComponent(graphTransition);
                graph.setIsRenderTemporaryComponent(true);
            }
        }
        else if(guiState == GUI_STATE_TYPE.FIND_TRANSITION_TARGET_STATE)
        {
            Transition.StateConnection connectedState = graph.pickState(graphicsPanel.getMousePosition());
            if(connectedState != null)
            {
                graphTransition.setToState(connectedState);
                graph.setIsRenderTemporaryComponent(false);
                graph.insertComponent(graphTransition);
                graphTransition.select();
                graph.setRenderDockingPoints(false);
                guiState = GUI_STATE_TYPE.SELECT_MODE;
            }
            else
            {
                Point mouse = graphicsPanel.getMousePosition();
                if(mouse != null)
                {
                    graphTransition.addCtrlPoint(new Point(mouse));
                }
            }
        }
        // START NODE
        else if(guiState == GUI_STATE_TYPE.FIND_STARTNODE_START_POS)
        {
            Point mouse = graphicsPanel.getMousePosition();
            if(mouse != null)
            {
                graphStartNode.setStartNodeStartPos(mouse);
                guiState = GUI_STATE_TYPE.FIND_STARTNODE_TARGET_STATE;
                graph.setIsRenderTemporaryComponent(true);
            }            
        }
        else if(guiState == GUI_STATE_TYPE.FIND_STARTNODE_TARGET_STATE)
        {
            Transition.StateConnection connectedState = graph.pickState(graphicsPanel.getMousePosition());
            if(connectedState != null)
            {
                graphStartNode.setToState(connectedState);
                graph.setIsRenderTemporaryComponent(false);
                graph.setStartNode(graphStartNode);
                graphStartNode.select();
                graph.setRenderDockingPoints(false);
                guiState = GUI_STATE_TYPE.SELECT_MODE;
            }
            else
            {
                Point mouse = graphicsPanel.getMousePosition();
                if(mouse != null)
                {
                    graphStartNode.addCtrlPoint(new Point(mouse));
                }
            }
        }
        // ** SELECT MODE **
        else if(guiState == GUI_STATE_TYPE.SELECT_MODE)
        {
            Point mouse = graphicsPanel.getMousePosition();
            if(mouse != null)
            {
                graph.select(mouse, shiftKey);
            }
        }
        graphicsPanel.repaint();
    }

    /**
     * recognizes if the left mouse button is pressed to move the picker
     *
     * @author Jan Montag
     */
    @Override
    public void leftMouseButtonPressed()
    {
        Point mousePosition = graphicsPanel.getMousePosition();
        if(mousePosition != null)
        {
            isPickerSelected = graph.selectPicker(mousePosition);
        }
        graphicsPanel.repaint();
    }

    /**
     * recognizes if the left mouse button is released to stop moving with the picker
     *
     * @author Jan Montag
     */
    @Override
    public void leftMouseButtonReleased()
    {
        graph.deselectAllPickers();
        graph.setRenderDockingPoints(false);
        graphicsPanel.repaint();
        
        isPickerSelected = false;
        
        graphicsPanel.setIsRenderSelectionRect(false);
    }

    /**
     * sets program in selectionmode
     *
     * @author Jan Montag
     */
    @Override
    public void setSelectionMode()
    {
        graph.setRenderDockingPoints(false);
        graphicsPanel.repaint();
        this.guiState = GUI_STATE_TYPE.SELECT_MODE;
    }

    /**
     * sets program in insertmode
     *
     * @author Jan Montag
     */
    @Override
    public void setInsertMode()
    {
        graph.deselectAll();
        graphicsPanel.repaint();
        this.guiState = GUI_STATE_TYPE.INSERT_MODE;
    }

    /**
     * insert a signal
     *
     * @param signal a signal
     *
     * @author Jan Montag
     */
    @Override
    public void insertSignal(Signal signal)
    {
        graph.insertSignal(signal);
    }

    /**
     * deletes a Signal
     *
     * @param signal a signal
     *
     *
     */
    @Override
    public void deleteSignal(Signal signal)
    {
        graph.deleteSignal(signal);
    }

    /**
     * insert a variable
     *
     * @param variable
     *
     * @author Jan Montag
     */
    @Override
    public void insertVariable(Variable variable)
    {
        graph.insertVariable(variable);
    }

    /**
     * deletes a variable
     *
     * @param variable
     *
     * @author Jan Montag
     */
    @Override
    public void deleteVariable(Variable variable)
    {
        graph.deleteVariable(variable);
    }

    /**
     * loads a file
     *
     * @param file a file
     *
     * @author Jan Montag
     */
    @Override
    public void loadFile(File file) throws IOException
    {
        int fileVersion;
        
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        
        fileVersion = dis.readInt();
        
        graph.loadGraph(dis, fileVersion);
        
        setGraphicsPanelSize(graph.getWidth(), graph.getHeight());
        graphicsPanel.repaint();
        
        guiMain.clearLogText();
        
        this.guiState = GUI_STATE_TYPE.SELECT_MODE;
    }

    /**
     * saves a file
     *
     * @param file a file
     *
     * @author Jan Montag
     */
    @Override
    public void saveFile(File file) throws FileNotFoundException, IOException
    {
        int fileVersion=1;

        DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
        
        dos.writeInt(fileVersion);
        
        graph.saveGraph(dos);
    }

    /**
     * verifies the Graph
     *
     * @author Jan Montag
     */
    @Override
    public void verifyGraph()
    {
        logString = generation.verifyGraphAndPartialGenerate(graph);
        graphicsPanel.repaint();
    }
        
    /**
     * exports the Project as SCXML
     *
     * @param file a SCXML file
     *
     * @author Jan Montag
     */
    @Override
    public void exportAsSCXML(File file)
    {
        try
        {
            logString = generation.exportAsSCXML(file, graph);
            graphicsPanel.repaint();
        }
        catch(IOException exception)
        {
            logString = "Export als SCXML fehlgeschlagen!";
        }
    }

    /**
     * exports the Project as C-Code
     *
     * @param file a C file
     *
     * @author Jan Montag
     */
    @Override
    public void generateCode_C(File file_h, File file_c, File file_e)
    {
        try
        {
            logString = generation.generateCode_C(file_h, file_c, file_e, graph);
            graphicsPanel.repaint();
        }
        catch(IOException exception)
        {
            logString = "Genrierung von C-Code fehlgeschlagen!";
        }
    }

    /**
     * exports the Project as VHDL
     *
     * @param file a VHDL file
     *
     * @author Jan Montag
     */
    @Override
    public void generateCode_VHDL(File file)
    {
        try
        {
            logString = generation.generateCode_VHDL(file, graph, guiPreferencesBoundary.getVhdlUseProcess());
            graphicsPanel.repaint();
        }
        catch(IOException exception)
        {
            logString = "Genrierung von VHDL-Code fehlgeschlagen!";
        }
    }

    /**
     * return the Graph
     *
     * @return the graph
     *
     * @author Jan Montag
     */
    @Override
    public Graph getGraph() {
        return graph;
    }

    /**
     * returns the logstring
     *
     * @return the logstring
     *
     * @author Jan Montag
     */
    @Override
    public String getLogString() {
        return logString;
    }

     /**
     * loads the properties of the Project
     *
     * @return boundary
     *
     * @author Jan Montag
     */
    @Override
    public final GuiPreferencesBoundary loadPreferences()
    {
        // load preferences from file
        String projectPath="";
        String exportPath="";
        Boolean vhdlUseProcess=false;
        try 
        {
            BufferedReader br = new BufferedReader(new FileReader("preferences.txt"));
            projectPath = br.readLine();
            exportPath = br.readLine();
            if(br.readLine().equals("1"))
                vhdlUseProcess = true;
        }
        catch (Exception ex) 
        { 
            // no preferences file existing
            projectPath=""; 
            exportPath="";
        }
        // store in boundary
        guiPreferencesBoundary.setProjectPath(projectPath);
        guiPreferencesBoundary.setExportPath(exportPath);
        guiPreferencesBoundary.setVhdlUseProcess(vhdlUseProcess);
        
        guiPreferencesBoundary.setProjectName(graph.getName());
        guiPreferencesBoundary.setProjectWidth(graph.getWidth());
        guiPreferencesBoundary.setProjectHeight(graph.getHeight());
        guiPreferencesBoundary.setGraphType(graph.getGraphType());
        
        
        return guiPreferencesBoundary;
    }

    /**
     * sets the preferences of the boundary
     *
     * @param boundary boundary with propertie-data
     *
     * @author Jan Montag
     */
    @Override
    public void savePreferences(GuiPreferencesBoundary boundary)
    {
        guiPreferencesBoundary = boundary;
        
        graph.setName(boundary.getProjectName());
        graph.setWidth(boundary.getProjectWidth());
        graph.setHeight(boundary.getProjectHeight());

        int width = boundary.getProjectWidth();
        int height = boundary.getProjectHeight();
        
        setGraphicsPanelSize(width, height);
        
        // save preferences to file
        String projectPath = boundary.getProjectPath();
        String exportPath = boundary.getExportPath();
        try {
            BufferedWriter bw = new BufferedWriter(new PrintWriter("preferences.txt"));
            bw.write(projectPath);
            bw.newLine();
            bw.write(exportPath);
            bw.newLine();
            bw.write(boundary.getVhdlUseProcess()==true?"1":"0");
            bw.newLine();
            bw.close();
        } catch (Exception ex) { System.out.println("could not write to 'preferences.txt'"); }        
        
        guiMain.changeTitleByProjectName(boundary.getProjectName());
    }

    /**
     * returns the propertie-data of the project drom the boundary
     *
     *
     * @return properties (boundary)
     *
     * @author Jan Montag
     */
    public GuiPreferencesBoundary getGuiPreferencesBoundary() {
        return guiPreferencesBoundary;
    }
    
    /**
     * increments number of control points of the current selected transition
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void incCtrlPoints()
    {
        graph.incrementTransitionCtrlPointNumber();
        graphicsPanel.repaint();
    }

    /**
     * decrements number of control points of the current selected transition
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void decCtrlPoints()
    {
        graph.decrementTransitionCtrlPointNumber();
        graphicsPanel.repaint();
    }

}
