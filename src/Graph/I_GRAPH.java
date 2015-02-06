/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Graph
 * Interface:   I_GRAPH
 * Created:     2011-10-28
 */

package Graph;

import Graph.Graph.GRAPH_TYPE;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 *
 * see class: Generation
 *
 * @author Andreas Schwenk
 */
public interface I_GRAPH
{
    // mouse and keyboard
    public void                 keyEvent(KeyEvent e);
    public void                 mouseDragged(Point mousePosition);

    // rendering
    public void                 render(Graphics2D g2d, Font font, Point mousePosition);
    
    // load and save
    public void                 loadGraph(DataInputStream in, int fileVersion) throws IOException;   
    public void                 saveGraph(DataOutputStream out) throws IOException;
    
    // selection
    public void                 select(Point mousePosition, boolean multiSelect);
    public void                 rectangleSelect(Point pos1, Point pos2);
    public void                 selectAll();
    public boolean              selectPicker(Point mousePosition);
    public Transition.StateConnection pickState(Point mousePosition);
    
    // deselection
    public void                 deselectAll();
    public void                 deselectAllPickers();
    
    // manipulate transition control-points
    public void                 incrementTransitionCtrlPointNumber();
    public void                 decrementTransitionCtrlPointNumber();

    // movement
    public void                 moveAllSelectedComponents(Point moveVec);
    
    // insertion
    public void                 insertComponent(Component c);
    public void                 insertSignal(Signal s);
    public void                 insertVariable(Variable v);

    // deletion
    public void                 deleteSignal(Signal s);
    public void                 deleteVariable(Variable v);
    public void                 deleteComponents(LinkedList<Component> list);
    public void                 deleteAllComponents();
    public void                 deleteAllVariables();
    public void                 deleteAllSignals();
    
    // set
    public void                 setGraphType(GRAPH_TYPE graphType);
    public void                 setComponents(LinkedList<Component> components);
    public void                 setSignals(LinkedList<Signal> s);
    public void                 setVariables(LinkedList<Variable> v);
    public void                 setTemporaryComponent(Component tmpComponent);
    public void                 setIsRenderTemporaryComponent(boolean value);
/*TODO:CHANGE NAME IN CLASS-DIAGRAM*/public void setStartNode(Transition startNode);
    public void                 setName(String name);
    public void                 setRenderDockingPoints(boolean renderDockingPoints);
    
    // get
    public LinkedList<Signal>   getSignals();
    public LinkedList<Signal>   getSignals(Signal.SIGNAL_DIRECTION direction);
    public LinkedList<Variable> getVariables();
    public Component            getTemporaryComponent();
    public boolean              getRenderTemporaryComponent();
/*TODO:CHANGE NAME IN CLASS-DIAGRAM*/public Transition getStartNode();
    public String               getName();
    public boolean              getRenderDockingPoints();
    public GRAPH_TYPE           getGraphType();
    public Component            getComponentByID(int id);
    public String               getLegend();
    public LinkedList<Component> getComponents();
    public LinkedList<Component> getSelectedComponents();
    public LinkedList<State>      getStates();
    public LinkedList<Transition> getTransitions();
}
