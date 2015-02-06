/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Graph
 * Class:       Graph
 * Created:     2011-10-28
 */

package Graph;

import AdditionalMath.AdditionalMath;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * The graph is the core of this project: It stores all Components, signals and
 * variables and acts therefore as a manager for the following entity-classes:
 * 
 *  - State
 *  - Transition
 *  - Container:
 *      - SuperState
 *      - ParallelState
 *  - Signal
 *  - Variable
 * 
 * Beside its storage-functionality the graphs offers methods for rendering
 * (process of visibility), searching, selecting etc.
 * 
 * @author Andreas Schwenk
 */
public class Graph implements I_GRAPH
{
    // for loading purposes
    public static final int COMP_STATE = 1;
    public static final int COMP_TRANSITION = 2;
    public static final int COMP_SUPER_STATE = 3;
    public static final int COMP_PARALLEL_STATE = 4;

    // *** ENUMARATIONS ***
    public enum GRAPH_TYPE { MOORE, MEALY };
    
    // *** ATTRIBUTES ***
    private GRAPH_TYPE graphType=GRAPH_TYPE.MOORE;
    private String name = "Graph01";
    
    // component that is currently inserted
    //  termporariy, but needed for rendering-purposes
    private Component temporaryComponent; 
    private boolean renderTemporaryComponent;
    
    private Transition startNode=null;
    private LinkedList<Component> components;
    private LinkedList<Signal> signals;
    private LinkedList<Variable> variables;
    
    private boolean renderDockingPoints=false;
    
    private int width=600;
    private int height=400;

    // *** METHODS ***
    public Graph()
    {
        // instantiate lists
        components = new LinkedList<Component>();
        signals = new LinkedList<Signal>();
        variables = new LinkedList<Variable>();
    }
    
    /**
     * Method, is called in case a key-event occurred.
     * Needed for all CustomTextFields, e. g. for component's name etc.
     * 
     * for further information see class "GUI.CustomTextField"
     * 
     * @param e key-event
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void keyEvent(KeyEvent e)
    {
        if(startNode != null && startNode.getIsSelected() && graphType == graphType.MEALY)
            startNode.keyEvent(e);
        Component comp;
        // for all components
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            // forward key-events to components in case that the component is selected
            if(comp.getIsSelected())
                comp.keyEvent(e);
        }
    }
    
    /**
     * Iterates all components and calls their rendering-methods.
     * Graph will be displayed in GUI.GraphicsPanel
     * 
     * @param g2d graphics-object from GUI.GraphicsPanel
     * @param font font-object
     * @param mousePosition current mouse-position (needed for highlighting etc)
     * 
     * @author Andreas Schwenk
     */    
    @Override
    public void render(Graphics2D g2d, Font font,
            Point mousePosition)
    {
        // render termporary component
        if(temporaryComponent != null && renderTemporaryComponent)
        {
            if(temporaryComponent.getPosition().x != 0 && temporaryComponent.getPosition().y != 0
                    || temporaryComponent instanceof Transition)
                temporaryComponent.render(g2d, font, mousePosition);
        }
        
        // render component-list
        Component comp;
        //  for all components
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            // render component
            comp.render(g2d, font, mousePosition);
        }
        
        // render start-node
        if(startNode != null)
            startNode.render(g2d, font, mousePosition);
    }
    
    /**
     * Iterates all components and calls their selection-methods which
     * implement their selection by mouse.
     * A selection is called from an instance of the class "Workflow".
     * 
     * @param mousePosition current mouse-position
     * @param multiSelect enables support for selecting multiple components     * 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void select(Point mousePosition, boolean multiSelect)
    {
        // in case that the mouse-position is not valid no selection can be performed
        if(mousePosition == null)
            return;
        // if a muliple-selection is NOT desired, all previous selections have
        //  to be cleared
        if(multiSelect == false)
            deselectAll();
        // start node
        if(startNode != null)
            startNode.select(mousePosition, multiSelect);
        // for all compontes
        Component comp;
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            // call compnents select-method
            comp.select(mousePosition, multiSelect);
        }        
    }
    
    /**
     * Iterates all components and calls their rectangle-selection-methods which
     * implement their rectangle-selection by mouse.
     * A selection is called from an instance of the class "Workflow".
     * 
     * @param pos1 first position
     * @param pos2 second position
     */    
    @Override
    public void rectangleSelect(Point pos1, Point pos2)
    {
        if(pos1==null || pos2==null)
            return;
        
        Point topLeft = new Point(pos2.x>pos1.x ? pos1.x:pos2.x, 
                pos2.y>pos1.y ? pos1.y:pos2.y);
        Point bottomRight = new Point(pos2.x<pos1.x ? pos1.x:pos2.x, 
                pos2.y<pos1.y ? pos1.y:pos2.y);
        
        deselectAll();
        
        // start node
        if(startNode != null)
            startNode.rectSelect(topLeft, bottomRight);
        // for all compontes
        Component comp;
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            // call compnents select-method
            comp.rectSelect(topLeft, bottomRight);
        }        
        
    }
    
    /**
     * selects ALL components of the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void selectAll()
    {
        Component comp;
        // start node
        if(startNode != null)
            startNode.select();
        // for all components
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            // select, no matter on mouseposition, etc
            comp.select();
        }
    }
    
    /**
     * deselects ALL components of the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deselectAll()
    {
        Component comp;
        // start-node
        if(startNode != null)
            startNode.deselect();
        // for all components
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            // deselect component
            comp.deselect();
        }
    }    
    
    /**
     * Gets a state by current mouse position. The return-type does not
     * only contain the state itself, but also a "docking-point-position/index".
     * As described in the GUI-design-document transitions will be attached
     * to these points.
     * 
     * @param mousePosition
     * @return state connection
     * 
     * @author Andreas Schwenk
     */
    @Override
    public Transition.StateConnection pickState(Point mousePosition)
    {
        // in case mouse-positoin is null, nothing can performed
        if(mousePosition == null)
            return null;

        // for all componetns
        Component comp;
        State state;
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            // we are only interested in states...
            comp = (Component)it.next();
            if(comp instanceof State)
            {
                state = (State)comp;
                // if gotten a state, check weather the mouse "flies" over
                if(state.isMouseOver(mousePosition))
                {
                    Point p = new Point();
                    
                    // p := imitate the mouse mouse on a "circled" path
                    p.x = mousePosition.x;
                    p.y = (int)((double)state.getPosition().y + (double)(mousePosition.y-
                            state.getPosition().y)*(double)state.getSize().x/(double)state.getSize().y);
                    
                    // angle beween mouse (here: point p) and the sate
                    double angle = AdditionalMath.getAngle(p, state.getPosition());
                    angle /= 2.0 * Math.PI;
                    angle *= 12.0;
                    angle += 0.5; // => rounding: double->int
                    
                    // calculate the index of the docking-point
                    int dockingPointPos = (int)angle;
                    
                    // create a new StateConnection and return it
                    return new Transition.StateConnection(state, dockingPointPos);
                }
            }
        }
        
        // faild: mouse is not over etc..
        return null;
    }
    
    /**
     * Implements a selections of so called "pickers" of all components.
     * A picker is a (displayed) object, which allows to manipulate an object.
     * E. g. a "move-picker" allows to drag a component (changes translation).
     * 
     * @param mousePosition current mouse position
     * 
     * @author Andreas Schwenk
     */
    @Override
    public boolean selectPicker(Point mousePosition)
    {
        boolean selectedPicker = false;
        
        // start node
        if(startNode != null && startNode.getIsSelected())
        {
            if(startNode.selectPicker(mousePosition))
                selectedPicker = true;
        }
        // for all components
        Component comp;
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            // if component is selected (requirement) => call components method
            if(comp.getIsSelected())
            {
                if(comp.selectPicker(mousePosition))
                    selectedPicker = true;
            }
        }
        return selectedPicker;
    }
    
    /*+
     * Implements a deselection of the pickers of all components. 
     * See method "selectPicker(..)" for further information about
     * "pickers"
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deselectAllPickers()
    {
        Component comp;
        // start node
        if(startNode != null)
            startNode.deselectPickers();
        // for all componnts
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            comp.deselectPickers();
        }
    }
    
    /*
     * Implements a dragging of designated objects like "pickers".
     * See method "select" for further information about "pickers"
     * 
     * @param mousePosition current mouse position
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void mouseDragged(Point mousePosition)
    {
        // start node: drag "picker"
        if(startNode != null)
            startNode.mouseDragged(mousePosition);
        // for all components: drag "picker"
        Component comp;
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            if(comp.getIsSelected())
                comp.mouseDragged(mousePosition);
        }
    }
    
    /**
     * Adds a component to the graph
     * 
     * @param c the component that will be inserted
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void insertComponent(Component c)
    {
        components.add(c);
    }

    /**
     * Returns all components of the graph
     * 
     * @return list of all components of the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public LinkedList<Component> getComponents() {
        return components;
    }

    /**
     * Sets a set of components to the graph.
     * All previous components will be lost.
     * 
     * @param components list of components to be set to the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void setComponents(LinkedList<Component> components) {
        this.components = components;
    }

    /**
     * Adds a signal to the graph
     * 
     * @param s the signal that will be inserted
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void insertSignal(Signal s)
    {
        signals.add(s);
    }
    
    /**
     * Deletes a signal from the graph
     * 
     * @param s the signal that will be deleted
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deleteSignal(Signal s)
    {
        signals.remove(s);
    }

    /**
     * Returns all signals of the graph
     * 
     * @return list of all signals of the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public LinkedList<Signal> getSignals() {
        return signals;
    }

    /**
     * Returns all signals of the graph filtered by a given direction.
     * 
     * @param direction filter
     * @return list of signals filtered by given direction
     * 
     * @author Andreas Schwenk
     */
    @Override
    public LinkedList<Signal> getSignals(Signal.SIGNAL_DIRECTION direction)
    {
        // resulting list of all signals with given direction-requirements
        LinkedList<Signal> list = new LinkedList<Signal>();
        
        // for all signals
        Signal s;
        Iterator it=signals.iterator();
        while(it.hasNext())
        {
            s = (Signal)it.next();
            // only collect signal, if the direction is the desired one
            if(s.getDirection() == direction)
            {
                list.add(s);
            }
        }
        // return the list
        return list;
    }

    /**
     * Sets a list of signals to the graph.
     * All previous signals will be lost.
     * 
     * @param s list of signals to be set to the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void setSignals(LinkedList<Signal> s) {
        this.signals = s;
    }

    /**
     * Adds a variable to the graph
     * 
     * @param v the variable that will be inserted
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void insertVariable(Variable v)
    {
        variables.add(v);
    }
    
    /**
     * Deletes a variable from the graph
     * 
     * @param v the variable that will be deleted
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deleteVariable(Variable v)
    {
        variables.remove(v);
    }

    /**
     * Returns all variables of the graph
     * 
     * @return list of all variables of the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public LinkedList<Variable> getVariables() {
        return variables;
    }

    /**
     * Sets a list of variables to the graph.
     * All previous variables will be lost.
     * 
     * @param v list of variables to be set to the graph     * 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void setVariables(LinkedList<Variable> v) {
        this.variables = v;
    }
    
    /**
     * Gets the temporary component. The temporary component is the one that
     * will be inserted while the user is customizing it. Needed for rendering
     * purposes
     * 
     * @return temporary component
     * 
     * @author Andreas Schwenk
     */
    @Override
    public Component getTemporaryComponent() {
        return temporaryComponent;
    }

    /**
     * Set the temporary component. The temporary component is the one that
     * will be inserted while the user is customizing it. Needed for rendering
     * purposes
     * 
     * @param tmpComponent  temporary component
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void setTemporaryComponent(Component tmpComponent) {
        this.temporaryComponent = tmpComponent;
    }

    /**
     * Gets whether the temporary component should be rendered. 
     * The temporary component is the one that will be inserted while the user
     * is customizing it. Needed for rendering purposes
     * 
     * @return is temporary component rendered?
     * 
     * @author Andreas Schwenk
     */
    @Override
    public boolean getRenderTemporaryComponent() {
        return renderTemporaryComponent;
    }

    /**
     * Set whether the temporary component should be rendered. 
     * The temporary component is the one that will be inserted while the user
     * is customizing it. Needed for rendering purposes
     * 
     * @param value shall temporary component rendered?
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void setIsRenderTemporaryComponent(boolean value) {
        this.renderTemporaryComponent = value;
    }

    /**
     * gets the start node of the graph
     * 
     * @return start node of the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public Transition getStartNode() {
        return startNode;
    }

    /**
     * sets the initial state of the graph
     * 
     * @param startNode the start node of the graph to be set
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void setStartNode(Transition startNode) {
        this.startNode = startNode;
    }

    /**
     * gets the graph's name
     * 
     * @return name of the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * sets the graph's name
     * 
     * @param name name of the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void setName(String name) {
        this.name = name.replace(" ", ""); // remove spaces
    }

    /**
     * Get, whether docking-points are currently rendered 
     * 
     * @return are docking-points are currently rendered?
     * 
     * @author Andreas Schwenk
     */
    @Override
    public boolean getRenderDockingPoints() {
        return renderDockingPoints;
    }

    /**
     * Set, whether docking-points are currently rendered 
     * 
     * @param renderDockingPoints render-docking-points yes/no
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void setRenderDockingPoints(boolean renderDockingPoints)
    {
        this.renderDockingPoints = renderDockingPoints;
    }

    /**
     * all of the graph's components will be deleted
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deleteAllComponents() 
    {
        this.components = new LinkedList<Component>();
        if(startNode != null)
            startNode = null;
    }

    /**
     * all of the graph's variables will be deleted
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deleteAllVariables() 
    {
        this.variables = new LinkedList<Variable>();
    }

    /**
     * all of the graph's signals will be deleted
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deleteAllSignals() 
    {
        this.signals = new LinkedList<Signal>();
    }

    /**
     * gets a list of all currently selected components
     * 
     * @return list of all currently selected components
     * 
     * @author Andreas Schwenk
     */
    @Override
    public LinkedList<Component> getSelectedComponents()
    {
        // resulting list
        LinkedList<Component> selectionList=new LinkedList<Component>();
        // for all components
        Component c;
        Iterator it=components.iterator();
        while(it.hasNext())
        {
            c = (Component)it.next();
            // is compoent selected?
            if(c.getIsSelected())
            {
                selectionList.add(c);
            }
        }
        // start node
        if(startNode != null)
        {
            if(startNode.getIsSelected())
                selectionList.add(startNode);
        }
        
        return selectionList;
    }
    
    /**
     * deletes all components of the graph given by a deletion-list
     * 
     * @param list list of the components that shall be deleted
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deleteComponents(LinkedList<Component> list)
    {
        // for all components in given list
        Iterator it=list.iterator();
        Component c;
        while(it.hasNext())
        {
            c = (Component)it.next();
            // special case Transition:
            // => remove transition-associations from states
            if(c instanceof Transition)
            {
                Transition t=(Transition)c;
                if(!t.getIsStartNode())
                    t.getFromState().removeTransition(t);
            }
            // remove component
            components.remove(c);
            // start node?
            if(startNode != null && startNode == c)
                startNode = null;
        }
    }

    /**
     * gets the type of the graph (GRAPH_TYPE.MOORE | GRAPH_TYPE.MEALY)
     * 
     * @return type of the graph
     * 
     * @author Andreas Schwenk
     */
    @Override
    public GRAPH_TYPE getGraphType() {
        return graphType;
    }

    /**
     * gets the type of the graph (GRAPH_TYPE.MOORE | GRAPH_TYPE.MEALY)
     * 
     * @param graphType type of the graph to be set
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void setGraphType(GRAPH_TYPE graphType) {
        this.graphType = graphType;
    }
    
    /**
     * gets a component of the graph thats ID is equal to the given one
     * 
     * @param id ID of the component that shall be returned
     * @return component
     * 
     * @author Andreas Schwenk
     */
    @Override
    public Component getComponentByID(int id)
    {
        // for all componnts
        Component comp;
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            // return componnt, if the ID is matching
            if(comp.id == id)
                return comp;
        }
        return null;
    }
    
    /**
     * gets all states of the graph
     * 
     * @return state-list
     * 
     * @author Andreas Schwenk
     */
    @Override
    public LinkedList<State> getStates()
    {
        LinkedList<State> stateList = new LinkedList<State>();
       
        Iterator<Component> itComp=components.iterator();
        Component comp;
        while(itComp.hasNext())
        {
            comp = itComp.next();
            if(comp instanceof State)
                stateList.add((State)comp);
        }
        
        return stateList;
    }
    
    /**
     * gets all transitions of the graph
     * 
     * @return transition-list
     * 
     * @author Andreas Schwenk
     */
    @Override
    public LinkedList<Transition> getTransitions()
    {
        LinkedList<Transition> transitionList = new LinkedList<Transition>();
       
        Iterator<Component> itComp=components.iterator();
        Component comp;
        while(itComp.hasNext())
        {
            comp = itComp.next();
            if(comp instanceof Transition)
                transitionList.add((Transition)comp);
        }
        
        return transitionList;
    }    
     
    /**
     * Saves the entire graph with all of its components to file (identified
     * by an output-stream-parameter)
     * 
     * @param out data-stream of the output-file
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void saveGraph(DataOutputStream out) throws IOException
    {
        // save graph-attributes
        out.writeUTF(graphType.name());   
        out.writeInt(Component.idCounter);
        
        out.writeUTF(name);
        out.writeInt(width);
        out.writeInt(height);
        
        // save signals
        out.writeInt(signals.size());
        Signal sig;
        for(Iterator it=signals.iterator(); it.hasNext(); )
        {
            sig = (Signal)it.next();
            sig.saveToFile(out);
        }
        
        // save variables
        out.writeInt(variables.size());
        Variable var;
        for(Iterator it=variables.iterator(); it.hasNext(); )
        {
            var = (Variable)it.next();
            var.saveToFile(out);
        }
        
        // save components sequential
        out.writeInt(components.size());
        Component comp;
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            comp.saveToFile(out);
        }
        
        // start node existing?
        if(startNode != null)
        {
            out.writeBoolean(true);
            startNode.saveToFile(out);
        }
        else
            out.writeBoolean(false);
    }
    
    /**
     * Loads the entire graph with all of its components from a given file
     * (identified by an input-stream-parameter)
     * 
     * @param in data-stream of the input-file
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void loadGraph(DataInputStream in, int fileVersion) throws IOException
    {
        // load graph-attributes
        graphType = GRAPH_TYPE.valueOf(in.readUTF());
        Component.idCounter = in.readInt();
        
        name = in.readUTF();
        width = in.readInt();
        height = in.readInt();

        // delete everything
        deleteAllComponents();
        deleteAllSignals();
        deleteAllVariables();

        // load signals
        int numSignals = in.readInt();
        for(int i=0; i<numSignals; i++)
        {
            Signal s = new Signal(in, this);
            signals.add(s);
        }
        
        // load variables
        int numVariables = in.readInt();
        for(int i=0; i<numVariables; i++)
        {
            Variable v = new Variable(in, this);
            variables.add(v);
        }
        
        // load components
        int component_type;
        int numComponents = in.readInt();
        // for the number of saved components
        Component comp=null;
        for(int i=0; i<numComponents; i++)
        {
            // (i)   read components type
            // (ii)  create appropriate instances
            // (iII) insert just created component into the component-list
            component_type = in.readInt();
            switch(component_type)
            {
                case COMP_STATE:
                    comp = new State(in, this);
                    components.add(comp);
                    break;
                case COMP_TRANSITION:
                    comp = new Transition(in, this, false);
                    components.add(comp);
                    break;
                case COMP_SUPER_STATE:
                    comp = new SuperState(in, this);
                    components.add(comp);
                    break;
                case COMP_PARALLEL_STATE:
                    comp = new ParallelState(in, this);
                    components.add(comp);
                    break;
                default:
                    System.out.println("ERROR: Graph.loadGraph(..): unknown "
                            + "component_type, type="+component_type);
                    break;
            }
        }
        
        // start node existing?
        if(in.readBoolean())
        {
            in.readInt(); // type (here: transition in any case)
            startNode = new Transition(in, this, true);
        }
        
        // Fix all associations. This is neccessary, because only the component-IDs
        //  are stored. The Java-references have to be set up AFTER loading
        if(startNode != null)
            startNode.fixAssociations();
        for(Iterator it=components.iterator(); it.hasNext(); )
        {
            comp = (Component)it.next();
            comp.fixAssociations();
        }
    }
    
    /**
     * Gets the "legend" of the output-vector.
     * An output-vector is a comma-separated String, that hold the output
     * for a state (Moore) respectively transition (Mealy).
     * The legend concatenates the names of all output-signals (in an appropriate
     * order).
     * 
     * @return legend of the output-vector (concatenation of the names of all output-signals)
     * 
     * @author Andreas Schwenk
     */
    @Override
    public String getLegend()
    {
        String legend="Ausgabevektor:  [";
        
        // for all signals with direction "OUT"
        LinkedList<Signal> outputSignals = getSignals(Signal.SIGNAL_DIRECTION.OUT);
        Signal s;
        Iterator it=outputSignals.iterator();    
        while(it.hasNext())
        {
            // append string
            s = (Signal)it.next();
            legend += s.getName() + ", ";
        }
        
        // remove last comma
        if(getSignals(Signal.SIGNAL_DIRECTION.OUT).size() > 0)
        {
            legend = legend.substring(0, legend.length()-2);
        }
        
        return legend + "]";
    }
 
    /**
     * Moves all selected components by given movement-vector
     * @param moveVec movement-vector
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void moveAllSelectedComponents(Point moveVec)
    {
        Component c;
        Iterator<Component> it=components.iterator();
        while(it.hasNext())
        {
            c = it.next();
            if(c.getIsSelected())
                c.move(moveVec);
        }
        if(startNode != null && startNode.getIsSelected())
        {
            startNode.move(moveVec);
        }
        recalculateTransitions();
    }
    
    /**
     * recalculates all transitions
     * 
     * @author Andreas Schwenk
     */
    public void recalculateTransitions()
    {
        // recalculate transition-curves (geometry changes, if relative state-
        //  distances move)
        Component comp;
        // (a) start node
        if(startNode != null)
            startNode.recalculateGeometry(false, null);
        // (b) transitions
        Iterator<Component> it;
        it = components.iterator();
        while(it.hasNext())
        {
            comp = (Component)it.next();
            if(comp instanceof Transition)
                ((Transition)comp).recalculateGeometry(false, null);
        }
    }

    /**
     * gets the height of the graphics-area
     * 
     * @return height height
     * 
     * @author Andreas Schwenk
     */
    public int getHeight() {
        return height;
    }

    /**
     * sets the height of the graphics-area
     * 
     * @param height height
     * 
     * @author Andreas Schwenk
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * gets the width of the graphics-area
     * 
     * @return width width
     * 
     * @author Andreas Schwenk
     */
    public int getWidth() {
        return width;
    }

    /**
     * sets the width of the graphics-area
     * 
     * @param width width
     * 
     * @author Andreas Schwenk
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * @author Andreas Schwenk
     */
    @Override
    public void incrementTransitionCtrlPointNumber()
    {
        Component comp;
        Iterator<Component> it;
        it = components.iterator();
        while(it.hasNext())
        {
            comp = (Component)it.next();
            if(comp instanceof Transition && comp.isSelected)
            {
                ((Transition)comp).incCtrlPointNumber();
            }
        }
        recalculateTransitions();
    }

    /**
     * @author Andreas Schwenk
     */
    @Override
    public void decrementTransitionCtrlPointNumber()
    {
        Component comp;
        Iterator<Component> it;
        it = components.iterator();
        while(it.hasNext())
        {
            comp = (Component)it.next();
            if(comp instanceof Transition && comp.isSelected)
            {
                ((Transition)comp).decCtrlPointNumber();
            }
        }
        recalculateTransitions();
    }

    
    
}
