/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   Graph
 * Class:       State
 * Created:     2011-10-28
 */
package Graph;

import Generation.OutputVectorParser.GeneratedOutputVector;
import Generation.VariableAssignmentParser.GeneratedVarAssignment;
import Gui.CustomTextField;
import Gui.GraphicsPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Derivation of superclass "Component".
 * 
 * A state is a key-component to this project. Some properties:
 *  - name
 *  - output-vector (in case of Moore)
 *  - outgoing transitions
 * 
 * @author Andreas Schwenk
 */
public class State extends Component
{    
    // *** ATTRIBUTES ***
    
    // variable assignments (for explanaion): see class "Variable"
    private CustomTextField variableAssignments;
    
    // output-vector for moore-automata (Mealy: see Transition)
    private CustomTextField mooreOutput;

    // outgoing transitions
    private LinkedList<Transition> transitions = new LinkedList<Transition>();
    private LinkedList<Integer> transitionIDs = new LinkedList<Integer>(); // for loading purposes
    
    // current selection of pickers (for explanation of pickers: 
    //  see appropriate methods in superclasss "Component")
    private boolean movePickerSelected=false;
    private boolean sizePickerSelected=false;
    
    // variable-assignments can be switched on and off
    private boolean variableAssignmentsEnabled=false;
    
    // generated variable Assigments
    private LinkedList<GeneratedVarAssignment> generatedVariableAssignments=null;
    private LinkedList<GeneratedOutputVector> generatedOutputVector=null;
    
    // *** METHODS ***
    
    /**
     * Constructor. Creates a state by a given graph
     * 
     * @param graph owner of the new created State
     * 
     * @author Andreas Schwenk
     */
    public State(Graph graph)
    {
        // constructor call of "Component"
        super();
        
        // set owner
        this.parent = graph;
        
        // set template names and description
        name.setText("NEW_STATE");
        description = "";
        
        // initialize output-vector in case that the graph (parent) is of type MOORE
        if(graph.getGraphType() == Graph.GRAPH_TYPE.MOORE)
        {
            mooreOutput = new CustomTextField(this, CustomTextField.SYMBOL_LIST.OUTPUT_VECTOR,
                    1.0/4.0);
            
            String outputVector="";
            for(int i=0; i<graph.getSignals(Signal.SIGNAL_DIRECTION.OUT).size(); i++)
            {
                outputVector += "0, ";
            }
            if(graph.getSignals(Signal.SIGNAL_DIRECTION.OUT).size() > 0)
                outputVector = outputVector.substring(0, outputVector.length()-2); // remove last comma
                    
            mooreOutput.setText(outputVector);
        }
        
        // initialize variable-assignemts-textfield
        if(graph.getGraphType() == Graph.GRAPH_TYPE.MOORE)
        {
            variableAssignments = new CustomTextField(this, CustomTextField.SYMBOL_LIST.VARIABLE_ASSIGNMENT, -1.0/8.0);
            variableAssignments.setText("var op");
        }
        else
        {
            variableAssignments = new CustomTextField(this, CustomTextField.SYMBOL_LIST.VARIABLE_ASSIGNMENT, +1.0/8.0);
            variableAssignments.setText("var op");
        }
        
        // set standard-width
        setSizeX(150);
        
        // the hight depends on the graph-type (Moore vz Mealy)
        if(graph.getGraphType() == Graph.GRAPH_TYPE.MOORE)
        {
            name.setVerticalFactor(-1.0/4.0); // y-position for name-textfield
            setSizeY(75);
        }
        else /* mealy */
        {
            name.setVerticalFactor(0.0); // y-position for name-textfield
            setSizeY(50);
        }
        
        // set textfield "name" active => the user may change the text immediately
        name.setActive(true);
    }

    /**
     * Constructor. Creates a State from a given file (more precisely:
     *  a given data-input-stream).
     * 
     * @param in data-stream of the input-file
     * @param graph parent
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public State(DataInputStream in, Graph graph) throws IOException
    {
        loadFromFile(in, graph);
    }
    
    /**
     * gets the output-string in case of Moore
     * 
     * @return output-string
     * 
     * @author Andreas Schwenk
     */
    public String getMooreOutputString() {
        if(mooreOutput != null)
            return mooreOutput.getText();
        else
            return null;
    }

    /**
     * for explanation: see superclass "Component"
     * 
     * @param g2d
     * @param font
     * @param mousePosition
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void render(Graphics2D g2d, Font font,
                        Point mousePosition)
    {
        // set transformation:
        //   translate to the state's position
        AffineTransform at = new AffineTransform();
        at.translate(getPosition().x, getPosition().y);
        g2d.setTransform(at);

        // ** SELECTION-BOX **
        if(getIsSelected())
        {
            // render a red dashed ("dotted") box. This visualizes a selection
            g2d.setStroke(GraphicsPanel.dashedStroke);
            g2d.setColor(Color.red);
            g2d.drawRect(-getSize().x/2, -getSize().y/2, getSize().x, getSize().y);
            g2d.setColor(Color.black);
            g2d.setStroke(GraphicsPanel.solidStroke);
        }
        
        // ** MOVE-PICKER **
        if(getIsSelected())
        {
            // if the state is selected, a move-picker is shown. It allows the
            //  state to be moved by mouse-dragging
            Point p = new Point(-getSize().x/2, -getSize().y/2);
            // color depends on selection of the move-picker
            if(movePickerSelected)
                GraphicsPanel.renderMovePicker(g2d, p, Color.red);
            else
                GraphicsPanel.renderMovePicker(g2d, p, Color.black);
        }
        
        // ** SIZE-PICKER **
        if(isSelected)
        {
            // if the state is selected, a size-picker is shown. It allows the
            //  state to be resized by mouse-dragging
            g2d.setColor(Color.white);
            g2d.fill(new Arc2D.Double(getSize().x/2-7, getSize().y/2-7, 18, 18, 0+55, 270-55*2, Arc2D.OPEN));
            // color depends on selection of the size-picker
            if(sizePickerSelected)
                g2d.setColor(Color.red);
            else
                g2d.setColor(Color.black);
            // geometry
            g2d.drawArc(getSize().x/2-7, getSize().y/2-7, 18, 18, 0+55, 270-55*2);
            g2d.drawLine(getSize().x/2+8, getSize().y/2-6, getSize().x/2+8, getSize().y/2+8);
            g2d.drawLine(getSize().x/2-6, getSize().y/2+8, getSize().x/2+8, getSize().y/2+8);
        }
        
        // ** GEOMETRY **
        if(isSelected)
            g2d.setColor(Color.red);
        // background of the state
        g2d.setColor(Color.white);
        g2d.fillOval(-getSize().x / 2, -getSize().y / 2, getSize().x, getSize().y);
        // outline of the state
        g2d.setColor(Color.black);
        g2d.drawOval(-getSize().x / 2, -getSize().y / 2, getSize().x, getSize().y);
        // horizontal line in case of Moore
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE)
            g2d.drawLine(-getSize().x / 2, 0, getSize().x / 2, 0);
        if(isSelected)
            g2d.setColor(Color.black);
        
        // ** FONT **
        // name-textfield
        name.render(g2d, font);
        
        // variable-assignments if enabled
        if(variableAssignmentsEnabled)
        {
            if(isSelected)
                g2d.setColor(Color.blue);
            variableAssignments.render(g2d, font);
        }
        
        // mooreOutput-vector, in case of Moore
        if(isSelected)
            g2d.setColor(new Color(0,150,0));
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE)
            mooreOutput.render(g2d, font);
        
        // "buuton" to enable/disable variable-assignments
        if(isSelected)
        {
            g2d.setColor(Color.blue);
            g2d.setColor(Color.white);
            g2d.fillRect(-getSize().x/2-2, -10, 14, 18);
            g2d.setColor(Color.blue);
            // "V"
            if(variableAssignmentsEnabled == false)
                g2d.setStroke(GraphicsPanel.fatStroke);
            g2d.drawLine(-getSize().x/2, -5, -getSize().x/2+5, 5);
            g2d.drawLine(-getSize().x/2+10, -5, -getSize().x/2+5, 5);
            g2d.setStroke(GraphicsPanel.solidStroke);
            g2d.drawRect(-getSize().x/2-2, -10, 14, 18);
            g2d.setColor(Color.black);
        }
        
        // ** Docking-Points **
        // docking points are displayed, when connecting a transition to a state
        if(parent.getRenderDockingPoints() 
                && mousePosition!=null && isMouseOver(mousePosition))
        {
            int x, y;
            // delta := 360 / 12 (twelve is the number of docking-points)
            double angleDelta = 2.0*Math.PI/12.0;
            Ellipse2D.Double ellipse;
            // render the dockingpoints on the outline of the state.
            //  The outline is realized as an ellipse; therefore a streched circle
            //  is calculated
            for(int i=0; i<12; i++)
            {
                x=(int)(Math.cos(angleDelta * (double)i) * (double)getSize().x/2.0);
                y=(int)(Math.sin(angleDelta * (double)i) * (double)getSize().y/2.0);
                ellipse = new Ellipse2D.Double(x-3, y-3, 6, 6);
                g2d.setColor(Color.white);
                g2d.fill(ellipse);
                g2d.setColor(Color.red);
                g2d.draw(ellipse);
            }
            g2d.setColor(Color.black);
        }
        
    }

    /**
     * for explanation: see superclass "Component"
     * 
     * @param mousePosition
     * @return mouse over?
     * 
     * @author Andreas Schwenk
     */
    @Override
    public boolean isMouseOver(Point mousePosition)
    {
        // note: constant "5" invisiblly grows the state a litte, so that
        //       a selection is easier
        return(mousePosition.x >= (getPosition().x-getSize().x/2-5) && 
           mousePosition.x <= (getPosition().x+getSize().x/2+5) &&
           mousePosition.y >= (getPosition().y-getSize().y/2-5) &&
           mousePosition.y <= (getPosition().y+getSize().y/2+5));
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @param mousePosition
     * @param multiSelect 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void select(Point mousePosition, boolean multiSelect)
    {
        // selection can only be done, if the mouse "flies" over the state
        if(isMouseOver(mousePosition))
        {
            // do selection
            select();
            // send a signal to the text-fields, so that the cursor can be set
            //  to an appropriate position
            // (i)   name
            name.mouseClick(mousePosition);
            // (ii)  output-vector
            if(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE)
                mooreOutput.mouseClick(mousePosition);
            // (iii) variable-assignemts
            if(variableAssignmentsEnabled)
                variableAssignments.mouseClick(mousePosition);
        }
        // if the mouse is NOT over the state, it shold be deselected
        else
        {
            // exceoption: in case of multiselection we select additively and
            //  DO NOT deselect anything selected so far
            if(multiSelect == false)
            {
                deselect();
            }
        }
        // variable-assignments
        //   variable-assignments are possible, when enabled via button
        variableAssignmentsEnabled =
           mousePosition.x >= (getPosition().x-getSize().x/2-2) && 
           mousePosition.x <= (getPosition().x-getSize().x/2+12) &&
           mousePosition.y >= (getPosition().y-10) &&
           mousePosition.y <= (getPosition().y+10) ? !variableAssignmentsEnabled : variableAssignmentsEnabled;
        // fix text-positions
        if(variableAssignmentsEnabled)
        {
            if(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE)
                name.setVerticalFactor(-1.0/3.0);
            else
                name.setVerticalFactor(-1.0/4.0);
        }
        else
        {
            if(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE)
                name.setVerticalFactor(-1.0/4.0);
            else
                name.setVerticalFactor(0.0);
        }
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @param topLeft
     * @param bottomRight 
     */
    @Override
    public void rectSelect(Point topLeft, Point bottomRight)
    {
        if(topLeft.x < getPosition().x && topLeft.y < getPosition().y &&
           bottomRight.x > getPosition().x && bottomRight.y > getPosition().y)
        {
            isSelected = true;
        }
    }

    
    /**
     * for explanation: see superclass "Component"
     * 
     * @param mousePosition 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public boolean selectPicker(Point mousePosition)
    {
        // move-picker
        //  position: top-left of the state
        movePickerSelected = 
           mousePosition.x >= (getPosition().x-getSize().x/2-9) && 
           mousePosition.x <= (getPosition().x-getSize().x/2+9) &&
           mousePosition.y >= (getPosition().y-getSize().y/2-9) &&
           mousePosition.y <= (getPosition().y-getSize().y/2+9);
        // size-picker
        //  position: bottom-right of the state
        sizePickerSelected = 
           mousePosition.x >= (getPosition().x+getSize().x/2-9) && 
           mousePosition.x <= (getPosition().x+getSize().x/2+9) &&
           mousePosition.y >= (getPosition().y+getSize().y/2-9) &&
           mousePosition.y <= (getPosition().y+getSize().y/2+9);
        return(movePickerSelected || sizePickerSelected);
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deselectPickers()
    {
        movePickerSelected = false;
        sizePickerSelected = false;
    }

    /**
     * for explanation: see superclass "Component"
     * 
     * @param mousePosition 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void mouseDragged(Point mousePosition) 
    {
        // (I) move-picker dragged
        if(movePickerSelected)
        {
            Point newPos, moveVector;
            newPos = new Point(mousePosition.x+getSize().x/2, mousePosition.y+getSize().y/2);
            moveVector = new Point();
            moveVector.x = newPos.x - getPosition().x;
            moveVector.y = newPos.y - getPosition().y;
            parent.moveAllSelectedComponents(moveVector);
        }
        // (II) size-picker dragged
        if(sizePickerSelected)
        {
            // recalculate size
            setSize(new Point(2*(mousePosition.x-getPosition().x), 2*(mousePosition.y-getPosition().y)));
            // force to a minimum / maximum size, if exceeded
            if(getSize().x < 100)
                setSizeX(100);
            if(getSize().x > 500)
                setSizeX(500);
            if(getSize().y < 50)
                setSizeY(50);
            if(getSize().y > 200)
                setSizeY(200);
        }
        parent.recalculateTransitions();
    }

    /**
     * adds an OUTGOING transition
     * 
     * @param t outgoing transition to be added
     * 
     * @author Andreas Schwenk
     */
    public void addTransition(Transition t)
    {
        transitions.add(t);
    }

    /**
     * removes an outgoing transition
     * 
     * @param t outgoing transition to be removed
     * 
     * @author Andreas Schwenk
     */
    public void removeTransition(Transition t)
    {
        transitions.remove(t);
    }

    /**
     * gets a list of all outgoing transitions
     * 
     * @return list of all outgoing transitions
     * 
     * @author Andreas Schwenk
     */
    public LinkedList<Transition> getTransitions() {
        return transitions;
    }

    /**
     * for explanation: see superclass "Component"
     * 
     * @param e 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void keyEvent(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
       
        if( !(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE && mooreOutput.getActive()
                || variableAssignmentsEnabled && variableAssignments.getActive()
                || name.getActive()))
        {
            Point moveVec = new Point(0, 0);
            switch(keyCode)
            {
                case KeyEvent.VK_LEFT:
                    moveVec.x = -16;
                    break;
                case KeyEvent.VK_RIGHT:
                    moveVec.x = 16;
                    break;
                case KeyEvent.VK_UP:
                    moveVec.y = -16;
                    break;
                case KeyEvent.VK_DOWN:
                    moveVec.y = 16;
                    break;
            }
            parent.moveAllSelectedComponents(moveVec);            
            return;
        }
                
        // (I) Moore
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE)
        {
            // textfields active?
            boolean nameActive = name.getActive();
            boolean mooreOutputActive = mooreOutput.getActive();
            boolean variableAssignmentsActive = variableAssignments.getActive();
            
            // send keyevents to textfields (will only be processed internally, when active)
            name.keyEvent(e);
            mooreOutput.keyEvent(e);
            if(variableAssignmentsActive)
                variableAssignments.keyEvent(e);
            
            // if name-textfield was active and is now not active, then the user
            //  pressed tab/enter to get to the next textfield
            if(nameActive && !name.getActive())
            {
                // deactivate name-textfield
                name.setActive(false);
                // enable variable-assignment-textfield, if variable-assigments are enabled
                if(variableAssignmentsEnabled)
                {
                    // enable variable-assignment textfield
                    variableAssignments.setActive(true);
                    variableAssignments.setCursorPos(variableAssignments.getText().length());
                }
                // else: enable moore-output-vector textfield
                else
                {
                    // enable moore-output textfield
                    mooreOutput.setActive(true);
                    mooreOutput.setCursorPos(mooreOutput.getText().length());
                }
            }
            
            // if variable-assignemtn-textfield was active and is now not active, then the user
            //  pressed tab/enter to get to the next textfield
            if(variableAssignmentsEnabled && variableAssignmentsActive && !variableAssignments.getActive())
            {
                // deactivate variable-assignment textfield
                variableAssignments.setActive(false);
                // enable moore-output textfield
                mooreOutput.setActive(true);
                mooreOutput.setCursorPos(mooreOutput.getText().length());
            }
            
            // if moore-output-textfield was active and is now not active, then the user
            //  pressed tab/enter to get to the next textfield
            if(mooreOutputActive && !mooreOutput.getActive())
            {
                // deactivate moore-output textfield
                mooreOutput.setActive(false);
                // activate name-textfield
                name.setActive(true);
                name.setCursorPos(name.getText().length());
            }
        }
        // (II) Mealy
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MEALY)
        {
            // textfields active?
            boolean nameActive = name.getActive();
            boolean variableAssignmentsActive = variableAssignments.getActive();
            
            // send keyevents to textfields (will only be processed internally, when active)
            name.keyEvent(e);
            if(variableAssignmentsActive)
                variableAssignments.keyEvent(e);
            
            // if name-textfield was active and is now not active, then the user
            //  pressed tab/enter to get to the next textfield
            if(nameActive && !name.getActive())
            {
                // enable variable-assignment-textfield, if variable-assigments are enabled
                if(variableAssignmentsEnabled)
                {
                    // deactivate name-textfield
                    name.setActive(false);
                    // enable variable-assignment textfield
                    variableAssignments.setActive(true);
                    variableAssignments.setCursorPos(variableAssignments.getText().length());
                }
                else
                    name.setActive(true);
            }
            
            // if variable-assignment-textfield was active and is now not active, then the user
            //  pressed tab/enter to get to the next textfield
            if(variableAssignmentsEnabled && variableAssignmentsActive && !variableAssignments.getActive())
            {
                // deactivate variable-assignment textfield
                variableAssignments.setActive(false);
                // enable name textfield
                name.setActive(true);
                name.setCursorPos(name.getText().length());
            }
        }
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void select() 
    {
        super.select();
        // when state is selected, the name-textfield should be activated
        this.name.setActive(true);
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deselect() 
    {
        super.deselect();
        // deselect name-textfield
        this.name.setActive(false);
        // deselect moore-output-textfield
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE)
            this.mooreOutput.setActive(false);
        // deselect variable-assignment-textfield
        this.variableAssignments.setActive(false);
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @return success
     * 
     * @author Andreas Schwenk
     */
    @Override
    public boolean fixAssociations()
    {
        // for all transition-IDs (needed after loading, see superclass)
        //  => references to transitions are reconstructed from transition-ids
        Integer transitionID;
        Component comp;        
        for(Iterator it=transitionIDs.iterator(); it.hasNext(); )
        {
            transitionID = (Integer)it.next();
            // get the appropriate transition by ID
            comp = parent.getComponentByID(transitionID);
            if(comp instanceof Transition) // check, if the component is really a transition
            {
                transitions.add((Transition)comp);
            }
            else // ID could not be found
            {
                System.out.println("ERROR: State.fixAssociations(): unknown transition, id="+transitionID);
            }
        }
        return true;
    }

    /**
     * for explanation: see superclass "Component"
     * 
     * @param in
     * @param graph
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public final void loadFromFile(DataInputStream in, Graph graph) throws IOException
    {
        // load properties that are common to all components
        super.loadComponentFromFile(in, graph);
        
        // load selection of pickers
        movePickerSelected = in.readBoolean();
        sizePickerSelected = in.readBoolean();

        // load: variable assignments active?
        variableAssignmentsEnabled = in.readBoolean();

        // load textfield for variable assignemts
        variableAssignments = new CustomTextField(in, this);
        
        // load textfield for moore-output-vector (in case of Moore)
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE)
        {
            mooreOutput = new CustomTextField(in, this);
        }
        
        // load number of outgoing transitions
        int numTransitions = in.readInt();
        
        // for all outgoing transitions
        int transitionId;
        for(int i=0; i<numTransitions; i++)
        {
            // load and store transition-ID
            //   note: "fixAssociations()" has to be called later to
            //         reconstruct the Java-references
            transitionId = in.readInt();
            transitionIDs.add(new Integer(transitionId));
        }
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @param out
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void saveToFile(DataOutputStream out) throws IOException
    {
        // write the type of this component ("state")
        out.writeInt(Graph.COMP_STATE);
        
        // save properties that are common to all components
        super.saveToFile(out);
        
        // save selection of pickers
        out.writeBoolean(movePickerSelected);
        out.writeBoolean(sizePickerSelected);
        
        // save: variable assignments active?
        out.writeBoolean(variableAssignmentsEnabled);
        
        // save textfield for variable assignemts
        variableAssignments.saveToFile(out);
        
        // save textfield for moore-output-vector (in case of Moore)
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE)
            mooreOutput.saveToFile(out);
        
        // save transitions
        Transition t;
        // (i) save number of outgoing transitions
        out.writeInt(transitions.size());
        for(Iterator it=transitions.iterator(); it.hasNext(); )
        {
            t = (Transition)it.next();
            // save transition-ID
            //   note: "fixAssociations()" has to be called in loading-process to
            //         reconstruct the Java-references
            out.writeInt(t.id);
        }
    }

    /**
     * get variable-assignments as a string
     * 
     * @return variable-assignments as a string
     * 
     * @author Andreas Schwenk
     */
    public String getVariableAssignments() {
        return variableAssignments.getText();
    }

    /**
     * get whether variable-assignments are enabled
     * 
     * @return variable-assignments enabled?
     * 
     * @author Andreas Schwenk
     */
    public boolean isVariableAssignmentsEnabled() {
        return variableAssignmentsEnabled;
    }
    
    /**
     * set weather the output-vector contains errors or not
     * 
     * @param val errors?
     * 
     * @author Andreas Schwenk
     */
    public void setOutputVectorError(boolean val)
    {
        mooreOutput.setUnderlined(val);
    }
    
    /**
     * set weather the variable-assignment contains errors or not
     * 
     * @param val errors?
     * 
     * @author Andreas Schwenk
     */
    public void setVariableAssignmentError(boolean val)
    {
        variableAssignments.setUnderlined(val);
    }

    /**
     * get generated variable assignments
     * 
     * @return list of generated variable assignments
     * 
     * @author Andreas Schwenk
     */
    public LinkedList<GeneratedVarAssignment> getGeneratedVariableAssignments() {
        return generatedVariableAssignments;
    }

    /**
     * set generated variable assignments
     * 
     * @param generatedVariableAssignments generated variable assignments
     * 
     * @author Andreas Schwenk
     */
    public void setGeneratedVariableAssignments(LinkedList<GeneratedVarAssignment> generatedVariableAssignments) {
        this.generatedVariableAssignments = generatedVariableAssignments;
    }
    
    /**
     * get generated output vector
     * 
     * @return list of generated output vector
     * 
     * @author Andreas Schwenk
     */
    public LinkedList<GeneratedOutputVector> getGeneratedOutputVector() {
        return generatedOutputVector;
    }

    /**
     * set generated output vector
     * 
     * @param generatedOutputVector generated output vector
     * 
     * @author Andreas Schwenk
     */
    public void setGeneratedOutputVector(LinkedList<GeneratedOutputVector> generatedOutputVector) {
        this.generatedOutputVector = generatedOutputVector;
    }
}
