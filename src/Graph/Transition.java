/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   Graph
 * Class:       Transition
 * Created:     2011-10-28
 */
package Graph;

import AdditionalMath.AdditionalMath;
import Generation.ConditionParser.GeneratedCondition;
import Generation.OutputVectorParser.GeneratedOutputVector;
import Gui.CustomTextField;
import Gui.GraphicsPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Derivation of superclass "Component".
 * 
 * A transition connects two states. Is is traversed (switching from the first
 * state to the second state) if the condition true.
 * 
 * @author Andreas Schwenk
 */
public class Transition extends Component
{
    // *** SUB-CLASSES ***
    /**
     * this class describes how a transition is connected to a state.
     * Two attributes are essential:
     * 
     *  - state
     *  - docking point index
     * 
     * A state owns twelve docking-point-positions. A transition can be "wired"
     * to one of these positions (ore two in case that the "from" and the "to"-state
     * is the same)
     * 
     * @author Andreas Schwenk
     */
    public static class StateConnection
    {
        // reference to the state
        State state=null;
        // state ID (for loading purposes: only ID can be saved in a file)
        int stateID;
        // docking-point-index as described in the class-description
        int dockingPointIndex=0;
                
        /**
         * constructor: create a State-Connection by a state-reference
         * 
         * @param state reference to the state
         * @param dockingPointIndex A state owns twelve docking-point-positions. 
         *  A transition can be "wired" to one of these positions (or two in 
         *  case that the "from" and the "to"-state is the same)
         * 
         * @author Andreas Schwenk
         */
        public StateConnection(State state, int dockingPointIndex)
        {
            this.state = state;
            this.dockingPointIndex = dockingPointIndex;
        }
        
        /**
         * constructor: create a StateConnection by a state-id (for loading-purposes)
         * 
         * @param stateID ID of the state
         * @param dockingPointIndex A state owns twelve docking-point-positions. 
         *  A transition can be "wired" to one of these positions (or two in 
         *  case that the "from" and the "to"-state is the same)
         * 
         * @author Andreas Schwenk
         */
        public StateConnection(int stateID, int dockingPointIndex)
        {
            this.stateID = stateID;
            this.dockingPointIndex = dockingPointIndex;
        }        
    }
    
    // *** ATTRIBUTES ***
    
    // condition
    private CustomTextField condition;
    private Point relativeConditionPosition;
    
    // control-points for the Bezier-curve
    private LinkedList<Point> ctrlPoints = new LinkedList<Point>();
    private Point selectedCtrlPoint = null;
    
    // output-vector in case of Mealy
    private CustomTextField mealyOutput;
    
    // state-connections
    private StateConnection fromState=null, toState=null;
    
    // geometry that is rendered
    private Polygon curve;
    private Polygon guideline; 
    private double arrowHeadAngle=0.0;
    
    // current selection of pickers (for explanation of pickers: 
    //  see appropriate methods in superclasss "Component")
    private boolean moveConditionPickerSelected = false;
    private boolean fromStatePickerSelected = false;
    private boolean toStatePickerSelected = false;
    //private boolean ctrlPointPickerSelected = false;
    //private int ctrlPointPickerSelectedIndex;
    
    // start node?
    private boolean isStartNode = false;
    private Point startNodeStartPos;

    private LinkedList<GeneratedOutputVector> generatedOutputVector=null;

    private GeneratedCondition generatedCondition=null;

    private Point conditionPosition=new Point();

    // *** METHODS ***   
    
    /**
     * Constructor. Creates a transition by a given graph
     * 
     * @param graph owner of the new created Transition
     * 
     * @author Andreas Schwenk
     */
    public Transition(Graph graph, boolean isStartNode)
    {
        // constructor call of "Component"
        super();
        
        // start node?
        this.isStartNode = isStartNode;
        
        // set owner
        this.parent = graph;
        
        // initialization
        init();
        
        // initialize condition-textfield
        condition = new CustomTextField(this, CustomTextField.SYMBOL_LIST.CONDITION, 0.0);
        condition.setActive(true);
        
        // initialize output-textfield in case of Mealy
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MEALY)
        {
            mealyOutput = new CustomTextField(this, CustomTextField.SYMBOL_LIST.OUTPUT_VECTOR, 1.25);
            String outputVector="";
            for(int i=0; i<graph.getSignals(Signal.SIGNAL_DIRECTION.OUT).size(); i++)
            {
                outputVector += "0, ";
            }
            if(graph.getSignals(Signal.SIGNAL_DIRECTION.OUT).size() > 0)
                outputVector = outputVector.substring(0, outputVector.length()-2); // remove last comma
                    
            mealyOutput.setText(outputVector);        
        }
    }

    /**
     * Constructor. Creates a Transition from a given file (more precisely:
     *  a given data-input-stream).
     * 
     * @param in data-stream of the input-file
     * @param graph parent
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public Transition(DataInputStream in, Graph graph, boolean isStartNode) throws IOException
    {
        this.isStartNode = isStartNode;
        loadFromFile(in, graph);
    }
    
    /**
     * initialization
     * 
     * @author Andreas Schwenk
     */
    private void init()
    {        
        relativeConditionPosition = new Point(0,0);
        
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MEALY)
            relativeConditionPosition.y += 20;
    }
    
    /**
     * gets the condition as a string
     * 
     * @return condition as a string
     * 
     * @author Andreas Schwenk
     */
    public String getCondition() {
        return condition.getText();
    }
    
    /**
     * sets the condition
     * 
     * @param condition the new condition
     * 
     * @author Andreas Schwenk
     */
    public void setCondition(String condition) {
        this.condition.setText(condition);
    }

    /**
     * gets the "from"-state (state where the transition starts)
     * 
     * @return "from"-state (state where the transition starts)
     * 
     * @author Andreas Schwenk
     */
    public State getFromState() {
        return fromState.state;
    }

    /**
     * gets the "to"-state (state where the transition ends)
     * 
     * @return "to"-state (state where the transition ends)
     * 
     * @author Andreas Schwenk
     */
    public State getToState() {
        return toState.state;
    }
    
    /**
     * recalculates the geometry. The transition is rendered as a Bezier-curve
     *  (approximated by small lines)
     * 
     * @param inConstructionMode construction-mode: transition ends at mouse-position
     * @param mousePosition current mouse-position (can be null, if not in construction-mode)
     * 
     * @author Andreas Schwenk
     */
    public void recalculateGeometry(boolean inConstructionMode, Point mousePosition)
    {
        if(isStartNode && startNodeStartPos==null)
            return;

        double stateDistance = calcStateDistance();
        
        int pt=0;
        
        // *** curve ***
        // create vertices which guide the Bezier-curve
        Point[] bezierPoints = new Point[6 + ctrlPoints.size()*3];

        if(isStartNode)
        {
            bezierPoints[pt] = new Point(startNodeStartPos);
            pt ++;
            bezierPoints[pt] = new Point(startNodeStartPos);
            pt ++;
        }
        else
        {
            // calulate the start-vertex
            //   start at a docking-point from a state
            bezierPoints[pt] = new Point();
            bezierPoints[pt].x = fromState.state.getPosition().x + (int)((double)fromState.state.getSize().x/2.0
                    * Math.cos(2.0*Math.PI/12.0*(double)fromState.dockingPointIndex));
            bezierPoints[pt].y = fromState.state.getPosition().y + (int)((double)fromState.state.getSize().y/2.0
                    * Math.sin(2.0*Math.PI/12.0*(double)fromState.dockingPointIndex));

            pt ++;

            // the next vertex can be found, when moving in the direction from the sate-center to the first vertex plus 50
            bezierPoints[pt] = new Point();
            bezierPoints[pt].x = fromState.state.getPosition().x + (int)(((double)fromState.state.getSize().x/2.0+stateDistance)
                    * Math.cos(2.0*Math.PI/12.0*(double)fromState.dockingPointIndex));
            bezierPoints[pt].y = fromState.state.getPosition().y + (int)(((double)fromState.state.getSize().y/2.0+stateDistance)
                    * Math.sin(2.0*Math.PI/12.0*(double)fromState.dockingPointIndex));

            pt ++;
        }
        
        // to give last last vertex a higher weight, it is cloned
        bezierPoints[pt] = (Point)bezierPoints[pt-1].clone();

        pt ++;
        
        
        // insert control-points
        Iterator<Point> it = ctrlPoints.iterator();
        while(it.hasNext())
        {
            bezierPoints[pt] = (Point)it.next().clone();
            pt ++;
            // to give last last vertex a higher weight, it is cloned
            bezierPoints[pt] = (Point)bezierPoints[pt-1].clone();
            pt ++;
            bezierPoints[pt] = (Point)bezierPoints[pt-1].clone();
            pt ++;
        }        
                
        
        // when in construction mode:
        //  - calculate the "from"-state wiring as usual
        //  - the end of the transiton is on the position of the mouse-cursor
        if(inConstructionMode)
        {
            bezierPoints[pt] = new Point();
            bezierPoints[pt].x = mousePosition.x;
            bezierPoints[pt].y = mousePosition.y;
            
            pt ++;
            
            // double twice, to get 6 vertices (as if we were not in construction-mode)
            bezierPoints[pt] = (Point)bezierPoints[pt-1].clone();            
            
            pt ++;
            
            bezierPoints[pt] = (Point)bezierPoints[pt-1].clone();            
        }
        else // not in construction-mode
        {
            // see: second vertex; but take the position of the "to"-state
            bezierPoints[pt] = new Point();
            bezierPoints[pt].x = toState.state.getPosition().x + (int)(((double)toState.state.getSize().x/2.0+stateDistance)
                    * Math.cos(2.0*Math.PI/12.0*(double)toState.dockingPointIndex));
            bezierPoints[pt].y = toState.state.getPosition().y + (int)(((double)toState.state.getSize().y/2.0+stateDistance)
                    * Math.sin(2.0*Math.PI/12.0*(double)toState.dockingPointIndex));

            pt ++;
            
            // give height weight
            bezierPoints[pt] = (Point)bezierPoints[pt-1].clone();            

            pt ++;
            
            // see: first vertex; but take the position of the "to"-state
            bezierPoints[pt] = new Point();
            bezierPoints[pt].x = toState.state.getPosition().x + (int)((double)toState.state.getSize().x/2.0
                    * Math.cos(2.0*Math.PI/12.0*(double)toState.dockingPointIndex));
            bezierPoints[pt].y = toState.state.getPosition().y + (int)((double)toState.state.getSize().y/2.0
                    * Math.sin(2.0*Math.PI/12.0*(double)toState.dockingPointIndex));
        }

        // create a Bezier-curve of the control-points
        curve = AdditionalMath.createBezierCurve(bezierPoints, 50);  
        
        // guideline
        guideline = new Polygon();
        guideline.npoints = bezierPoints.length;
        guideline.xpoints = new int[bezierPoints.length];
        guideline.ypoints = new int[bezierPoints.length];
        for(int i=0; i<bezierPoints.length; i++)
        {
            guideline.xpoints[i] = bezierPoints[i].x;
            guideline.ypoints[i] = bezierPoints[i].y;
        }

        // *** arrow-head ***
        // calculate the angel of the arrow head (can be derived from the angele
        //  of the last two constrol-points for the Bezier-curve
        arrowHeadAngle = AdditionalMath.getAngle(bezierPoints[bezierPoints.length-1], bezierPoints[bezierPoints.length-2]);
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
        // reset transformation (if any)
        AffineTransform at = new AffineTransform();
        g2d.setTransform(at);
        
        g2d.setColor(Color.black);
        
        if(isStartNode && startNodeStartPos == null && mousePosition != null)
        {
            Point p = new Point(mousePosition.x, mousePosition.y);
            mousePosition.x -= mousePosition.x % 16;
            mousePosition.y -= mousePosition.y % 16;
            g2d.fillOval(mousePosition.x-7, mousePosition.y-7, 14, 14);
        }
        
        // "from"-state set?
        // if not, there is nothing to render
        if(fromState != null || isStartNode)
        {
            
            
            
            
            
            
            // "to"-state set?
            // If not, construction is in progres. The curve will be constructed
            //  from "from"-state to current mouse-positon
            if(toState == null)
            {
                // recalculate the curve if not done so far
                if(curve == null)
                    recalculateGeometry(true, mousePosition);
                // ** CURVE **
                if(isStartNode && startNodeStartPos != null)
                    g2d.fillOval(startNodeStartPos.x-7, startNodeStartPos.y-7, 14, 14);

                // render the Bezier-curve
                if(curve != null)
                    g2d.drawPolyline(curve.xpoints, curve.ypoints, curve.npoints);
            }
            // of "to"-state is set, the entire transition can be rendered
            else
            {
                // recalculate the curve if not done so far
                if(curve == null)
                {
                    recalculateGeometry(false, null);
                }

                // color is influenced by selection
                if(isSelected)
                    g2d.setColor(Color.red);

                // ** CURVE **
                
                // start point in case of start-node
                if(isStartNode)
                {
                    g2d.fillOval(startNodeStartPos.x-7, startNodeStartPos.y-7, 14, 14);
                }
                
                // render Bezier-curve
                g2d.drawPolyline(curve.xpoints, curve.ypoints, curve.npoints);
                
                // ** ARROW-HEAD **
                // set translation and rotation for rendering the arrow-head
                at.translate(curve.xpoints[curve.npoints-1], curve.ypoints[curve.npoints-1]);
                at.rotate(arrowHeadAngle);
                g2d.setTransform(at);
                // render teh arrow-head
                GraphicsPanel.renderArrowHead(g2d);

                // reset color to black
                if(isSelected)
                    g2d.setColor(Color.black);

                // calculate the position of the condition
                //  => lies on the "middle"-curve-control-point
                //     plus an offset (the user can change this offset to move the
                //     relative condition-position)
                conditionPosition = new Point(curve.xpoints[curve.npoints/2] 
                        + relativeConditionPosition.x,
                        curve.ypoints[curve.npoints/2] + relativeConditionPosition.y);

                // ** MOVE-PICKER **
                // set transformation (translation)
                at = new AffineTransform();
                at.translate(conditionPosition.x, conditionPosition.y);
                g2d.setTransform(at);
                // render move-picker only, if the transiton is selected
                if(isSelected)
                {
                    Point p = new Point(0, -20);
                    if(moveConditionPickerSelected)
                        GraphicsPanel.renderMovePicker(g2d, p, Color.red);
                    else
                        GraphicsPanel.renderMovePicker(g2d, p, Color.green);                
                }

                // ** condition string **
                condition.render(g2d, font);

                // ** mealy output-vector **
                if(parent.getGraphType() == Graph.GRAPH_TYPE.MEALY)
                {
                    // transformation
                    at = new AffineTransform();
                    at.translate(conditionPosition.x, conditionPosition.y);
                    g2d.setTransform(at);
                    // horizontal line over the output-vector
                    if(isSelected)
                        g2d.setColor(Color.red);
                    g2d.drawLine(-30, 15, 30, 15);
                    if(isSelected)
                        g2d.setColor(Color.black);
                    // render the output-vector
                    mealyOutput.render(g2d, font);
                }

                // ** FROM-STATE-PICKER **
                if(isSelected)
                {
                    // transformation
                    at = new AffineTransform();
                    at.translate(curve.xpoints[0], curve.ypoints[0]);
                    g2d.setTransform(at);
                    Point p = new Point(0, 0);
                    if(fromStatePickerSelected)
                        GraphicsPanel.renderMovePicker(g2d, p, Color.red);
                    else
                        GraphicsPanel.renderMovePicker(g2d, p, Color.blue);                
                }

                // ** TO-STATE-PICKER **
                at = new AffineTransform();
                at.translate(curve.xpoints[curve.npoints-1], curve.ypoints[curve.npoints-1]);
                g2d.setTransform(at);
                if(isSelected)
                {
                    Point p = new Point(0, 0);
                    if(toStatePickerSelected)
                        GraphicsPanel.renderMovePicker(g2d, p, Color.red);
                    else
                        GraphicsPanel.renderMovePicker(g2d, p, Color.blue);             
                }
            }
                
            // ** CONTROL-POINT-PICKERs **

            // render guidelines
            g2d.setTransform(new AffineTransform());
            if(isSelected || toState==null)
            {
                g2d.setStroke(GraphicsPanel.dashedStroke);
                g2d.drawPolyline(guideline.xpoints, guideline.ypoints, guideline.npoints);
                g2d.setStroke(GraphicsPanel.solidStroke);
            }

            Iterator<Point> it = ctrlPoints.iterator();
            int i=0;
            Point tmp;
            while(it.hasNext())
            {
                tmp = it.next();
                at = new AffineTransform();
                at.translate(tmp.x, tmp.y);
                g2d.setTransform(at);
                if(isSelected || toState==null)
                {
                    Point p = new Point(0, 0);
                    if(selectedCtrlPoint != null && selectedCtrlPoint == tmp)
                    //if(ctrlPointPickerSelected && i == ctrlPointPickerSelectedIndex)
                        GraphicsPanel.renderMovePicker(g2d, p, Color.red);
                    else
                        GraphicsPanel.renderMovePicker(g2d, p, Color.black);             
                }
                i ++;
            }
                


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
        conditionPosition = new Point(curve.xpoints[curve.npoints/2] + relativeConditionPosition.x,
                curve.ypoints[curve.npoints/2] + relativeConditionPosition.y);
        // move-condition-picker
        moveConditionPickerSelected = 
           mousePosition.x >= (conditionPosition.x-9) && 
           mousePosition.x <= (conditionPosition.x+9) && 
           mousePosition.y >= (conditionPosition.y-9-20) && 
           mousePosition.y <= (conditionPosition.y+9-20);
        // from-state-picker
        fromStatePickerSelected = 
           mousePosition.x >= (curve.xpoints[0]-9) && 
           mousePosition.x <= (curve.xpoints[0]+9) && 
           mousePosition.y >= (curve.ypoints[0]-9) && 
           mousePosition.y <= (curve.ypoints[0]+9);
        // to-state-picker
        toStatePickerSelected = 
           mousePosition.x >= (curve.xpoints[curve.npoints-1]-9) && 
           mousePosition.x <= (curve.xpoints[curve.npoints-1]+9) && 
           mousePosition.y >= (curve.ypoints[curve.npoints-1]-9) && 
           mousePosition.y <= (curve.ypoints[curve.npoints-1]+9);
        // render docking-points (of the states)?
        parent.setRenderDockingPoints(fromStatePickerSelected || toStatePickerSelected);
        // control-point-pickers
        //ctrlPointPickerSelected = false;
        selectedCtrlPoint = null;
        Iterator<Point> it = ctrlPoints.iterator();
        int i=0;
        Point tmp;
        while(it.hasNext())
        {
            tmp = it.next();
            if( mousePosition.x >= (tmp.x-9) && 
                mousePosition.x <= (tmp.x+9) && 
                mousePosition.y >= (tmp.y-9) && 
                mousePosition.y <= (tmp.y+9) )
            {
                //ctrlPointPickerSelected = true;
                //ctrlPointPickerSelectedIndex = i;
                selectedCtrlPoint = tmp;
            }
            i ++;
        }
        return(moveConditionPickerSelected || fromStatePickerSelected 
                || toStatePickerSelected || selectedCtrlPoint!=null);
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deselectPickers()
    {
        parent.setRenderDockingPoints(false);
        moveConditionPickerSelected = false;
        fromStatePickerSelected = false;
        toStatePickerSelected = false;
        //ctrlPointPickerSelected = false;
        selectedCtrlPoint = null;
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
        // selection of the curve is done by constructing axis-aligned-rectangles
        // ("bounding-boxes") on each line-segment
        int x1, y1, x2, y2;
        isSelected = false;
        // for all line-segemnts (points)
        for(int i=0; i<curve.npoints-1; i++)
        {
            x1 = curve.xpoints[i];
            x2 = curve.xpoints[i+1];
            y1 = curve.ypoints[i];
            y2 = curve.ypoints[i+1];
            if(mousePosition.x >= Math.min(x1, x2)-2 && 
               mousePosition.x <= Math.max(x1, x2)+2 && 
               mousePosition.y >= Math.min(y1, y2)-2 && 
               mousePosition.y <= Math.max(y1, y2)+2)
            {
                select();
                return;
            }
        }
        
        // selection of the horizonal line (Mealy only)
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MEALY)
        {
            if(mousePosition.x >= conditionPosition.x-30 && 
               mousePosition.x <= conditionPosition.x+30 && 
               mousePosition.y >= conditionPosition.y+15-2 && 
               mousePosition.y <= conditionPosition.y+15+2)
            {
                select();
                return;
            }
        }
        
        // condition-selection?
        if(condition.isMouseOver(mousePosition))
        {
            select();
            // send a signal to the conditon-text-fields, so that the cursor can
            // be set to an appropriate position
            condition.mouseClick(mousePosition);
            return;
        }
        
        // mealy-output-selection?
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MEALY)
        {
            if(mealyOutput.isMouseOver(mousePosition))
            {
                select();
                // send a signal to the Mealy-output-text-fields, so that the 
                // cursor can be set to an appropriate position
                mealyOutput.mouseClick(mousePosition);
                condition.setActive(false);
                return;
            }
        }
        
        // exception: in case of multiselection we select additively and
        //  DO NOT deselect anything selected so far
        if(multiSelect == false)
            deselect();
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
        // selection of the curve is done by constructing axis-aligned-rectangles
        // ("bounding-boxes") on each line-segment
        int x1, y1, x2, y2;
        isSelected = false;
        // for all line-segemnts (points)
        for(int i=0; i<curve.npoints-1; i++)
        {
            x1 = curve.xpoints[i];
            x2 = curve.xpoints[i+1];
            y1 = curve.ypoints[i];
            y2 = curve.ypoints[i+1];
            if(topLeft.x <= Math.min(x1, x2)-2 && 
               bottomRight.x >= Math.max(x1, x2)+2 && 
               topLeft.y <= Math.min(y1, y2)-2 && 
               bottomRight.y >= Math.max(y1, y2)+2)
            {
                select();
                return;
            }
        }
    }

    /**
     * Sets the "from"-state. This is the state from where the transition starts
     * 
     * @param stateConnection "from"-state
     * 
     * @author Andreas Schwenk
     */
    public void setFromState(StateConnection stateConnection)
    {
        this.fromState = stateConnection;
        this.fromState.state.addTransition(this);
    }

    /**
     * Sets the "to"-state. This is the state where the transition ends
     * 
     * @param stateConnection "to"-state
     * 
     * @author Andreas Schwenk
     */
    public void setToState(StateConnection stateConnection)
    {
        this.toState = stateConnection;
        curve = null; // curve must be recalculated

        double startX, startY;
        
        // intelligent placement of the condition-position
        if(isStartNode)
        {
            startX = startNodeStartPos.x;
            startY = startNodeStartPos.y;
        }
        else
        {
            startX = fromState.state.getPosition().x + (int)(((double)fromState.state.getSize().x/2.0)
                    * Math.cos(2.0*Math.PI/12.0*(double)fromState.dockingPointIndex));
            startY = fromState.state.getPosition().y + (int)(((double)fromState.state.getSize().y/2.0)
                    * Math.sin(2.0*Math.PI/12.0*(double)fromState.dockingPointIndex));
        }
        double endX = toState.state.getPosition().x + (int)(((double)toState.state.getSize().x/2.0)
                * Math.cos(2.0*Math.PI/12.0*(double)toState.dockingPointIndex));
        double endY = toState.state.getPosition().y + (int)(((double)toState.state.getSize().y/2.0)
                * Math.sin(2.0*Math.PI/12.0*(double)toState.dockingPointIndex));
        // if delta of start and end is low => move, so that the condition is not
        //   rendered on top of the line
        // (a) x
        if(Math.abs(startX-endX) < 30)
            relativeConditionPosition.x += 20;
        // (b) y
        if(Math.abs(startY-endY) < 30)
            relativeConditionPosition.y -= 20;
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
    public boolean isMouseOver(Point mousePosition) {
        return false;
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
        // (I) MOORE
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MOORE)
        {
            // send keyevents to textfields (will only be processed internally, when active)
            condition.keyEvent(e);
            condition.setActive(true);
        }
        
        // (II) MEALY
        else if(parent.getGraphType() == Graph.GRAPH_TYPE.MEALY)
        {
            // textfields active?
            boolean conditionActive = condition.getActive();
            boolean mealyOutputActive = mealyOutput.getActive();
            
            // send keyevents to textfields (will only be processed internally, when active)
            condition.keyEvent(e);
            mealyOutput.keyEvent(e);
            
            // if condition-textfield was active and is now not active, then the user
            //  pressed tab/enter to get to the next textfield
            if(conditionActive && !condition.getActive())
            {
                // deactivate condition textfield
                condition.setActive(false);
                // enable mealy-output textfield
                mealyOutput.setActive(true);
                mealyOutput.setCursorPos(mealyOutput.getText().length());
            }
            
            // if mealy-output-textfield was active and is now not active, then the user
            //  pressed tab/enter to get to the next textfield
            if(mealyOutputActive && !mealyOutput.getActive())
            {
                // deactivate mealyOutput textfield
                mealyOutput.setActive(false);
                // enable the condition-textfield
                condition.setActive(true);
                condition.setCursorPos(condition.getText().length());
            }
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
    public void mouseDragged(Point mousePosition)
    {
        // (I) move-picker dragged
        if(moveConditionPickerSelected)
        {
            relativeConditionPosition = new Point(mousePosition.x-curve.xpoints[curve.npoints/2], 
                    mousePosition.y-curve.ypoints[curve.npoints/2]+20);
        }
        // (II) from-state-picker dragged
        else if(fromStatePickerSelected)
        {
            if(isStartNode)
            {
                startNodeStartPos = new Point(mousePosition);
                startNodeStartPos.x -= startNodeStartPos.x % 16;
                startNodeStartPos.y -= startNodeStartPos.y % 16;
            }
            else
            {
                // get the new state (with appropriate docking-point)
                StateConnection sc = parent.pickState(mousePosition);
                // only set, if at the mouse-position is any state
                if(sc != null)
                    fromState = sc;
            }
        }
        // (III) to-state-picker dragged
        else if(toStatePickerSelected)
        {
            // get the new state (with appropriate docking-point)
            StateConnection sc = parent.pickState(mousePosition);
            // only set, if at the mouse-position is any state
            if(sc != null)
                toState = sc;
        }
        // (IV) control-point-picker dragged
        //else if(ctrlPointPickerSelected)
        else if(selectedCtrlPoint != null)
        {
            //ctrlPoints.get(ctrlPointPickerSelectedIndex).x = mousePosition.x;
            //ctrlPoints.get(ctrlPointPickerSelectedIndex).y = mousePosition.y;
            selectedCtrlPoint.x = mousePosition.x;
            selectedCtrlPoint.y = mousePosition.y;
        }
        
        recalculateGeometry(false, mousePosition);
    }
   
    /**
     * for explanation: see superclass "Component"
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void select() {
        super.select();
        // when transiton is selected, the condition-textfield should be activated
        this.condition.setActive(true);
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deselect() {
        super.deselect();
        // deactivate all text-fields
        condition.setActive(false);
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MEALY)
            mealyOutput.setActive(false);
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

        // initialization
        init();
        
        // read weather the transition acts as a start-node
        isStartNode = in.readBoolean();
        // read start-position
        if(isStartNode)
        {
            startNodeStartPos = new Point();
            startNodeStartPos.x = in.readInt();
            startNodeStartPos.y = in.readInt();
        }

        // load textfield for condition
        condition = new CustomTextField(in, this);
        
        // load relative condition-position
        relativeConditionPosition.x = in.readInt();
        relativeConditionPosition.y = in.readInt();

        // load control-points for the Bzeier-curve
        // (i)  load number of control-points
        int numControlPoints = in.readInt();
        // (ii) load the control-points itself
        for(int i=0; i<numControlPoints; i++)
        {
            Point point = new Point();
            point.x = in.readInt();
            point.y = in.readInt();
            ctrlPoints.add(point);
        }
        
        // load mealy-output
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MEALY)
            mealyOutput = new CustomTextField(in, this);

        // load StateConnections
        //  note: only the IDs of the states are loaded
        //        => fixAssociations() has to be called later to rebuild the
        //           Java-references
        int index, readId;
        //  (i)  fromState
        if(!isStartNode)
        {
            index = in.readInt();
            readId = in.readInt();
            fromState = new StateConnection(readId, index);
        }
        //  (ii) toState
        index = in.readInt();
        readId = in.readInt();
        toState = new StateConnection(readId, index);
        
        // load pickers
        moveConditionPickerSelected = in.readBoolean();
        fromStatePickerSelected = in.readBoolean();
        toStatePickerSelected = in.readBoolean();
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
        Component comp;

        // "from"-state
        //   search state by given ID and reconstruct Java-reference
        if(isStartNode == false)
        {
            comp=parent.getComponentByID(fromState.stateID);
            if(comp instanceof State) // realy a state?
            {
                fromState.state = (State)comp;
            }
            else
            {
                System.out.println("ERROR: Transition.fixAssociations(): unknown transition, id="+fromState.stateID);
                return false;
            }
        }
        
        // "to"-state
        //   search state by given ID and reconstruct Java-reference
        comp=parent.getComponentByID(toState.stateID);
        if(comp instanceof State) // realyy a state?
        {
            toState.state = (State)comp;
        }
        else
        {
            System.out.println("ERROR: Transition.fixAssociations(): unknown transition, id="+toState.stateID);
            return false;
        }
        return true;
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
        // write the type of this component ("transition")
        out.writeInt(Graph.COMP_TRANSITION);

        // save properties that are common to all components
        super.saveToFile(out);
        
        // save weather the transition acts as a start-node
        out.writeBoolean(isStartNode);
        // save start-position
        if(isStartNode)
        {
            out.writeInt(startNodeStartPos.x);
            out.writeInt(startNodeStartPos.y);
        }
        
        // load textfield for condition
        condition.saveToFile(out);

        // save relative condition-position
        out.writeInt(relativeConditionPosition.x);
        out.writeInt(relativeConditionPosition.y);

        // load control-points for the Bzeier-curve
        // (i)  load number of control-points
        out.writeInt(ctrlPoints.size());
        // (i)  load control-points itself
        Point point;
        for(Iterator it=ctrlPoints.iterator(); it.hasNext(); )
        {
            point = (Point)it.next();
            out.writeInt(point.x);
            out.writeInt(point.y);
        }

        // save mealy-output
        if(parent.getGraphType() == Graph.GRAPH_TYPE.MEALY)
            mealyOutput.saveToFile(out);

        // ave StateConnections
        //  note: only the IDs of the states are saved
        //        => in loading-process: fixAssociations() has to be called
        //           later to rebuild the Java-references
        //  (i)  fromState
        if(!isStartNode)
        {
            out.writeInt(fromState.dockingPointIndex);
            out.writeInt(fromState.state.id);
        }
        //  (ii) toState
        out.writeInt(toState.dockingPointIndex);
        out.writeInt(toState.state.id);
        
        // save pickers
        out.writeBoolean(moveConditionPickerSelected);
        out.writeBoolean(fromStatePickerSelected);
        out.writeBoolean(toStatePickerSelected);        
    }
    
    /**
     * 
     * @param p 
     */
    public void addCtrlPoint(Point p)
    {
        ctrlPoints.add(p);
    }

    /**
     * moves the component by given vector
     * 
     * @param moveVec move vector
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void move(Point moveVec)
    {
        Point pt;
        Iterator<Point> it = ctrlPoints.iterator();
        while(it.hasNext())
        {
            pt = it.next();
            pt.x += moveVec.x;
            pt.y += moveVec.y;
            pt.x -= pt.x % 16;
            pt.y -= pt.y % 16;
        }
        if(isStartNode)
        {
            startNodeStartPos.x += moveVec.x;
            startNodeStartPos.y += moveVec.y;
            startNodeStartPos.x -= startNodeStartPos.x % 16;
            startNodeStartPos.y -= startNodeStartPos.y % 16;
        }
    }
    
 
    /**
     * gets the output-string in case of Mealy
     * 
     * @return output-string
     * 
     * @author Andreas Schwenk
     */
    public String getMealyOutputString() {
        if(mealyOutput != null)
            return mealyOutput.getText();
        else
            return null;
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
        mealyOutput.setUnderlined(val);
    }
    
    /**
     * set weather the condition contains errors or not
     * 
     * @param val errors?
     * 
     * @author Andreas Schwenk
     */
    public void setConditionError(boolean val)
    {
        condition.setUnderlined(val);
    }
    
    /**
     * sets the start position in case of a start node
     * 
     * @param p start position
     * 
     * @author Andreas Schwenk
     */
    public void setStartNodeStartPos(Point p)
    {
        startNodeStartPos = new Point(p);
    }
    
    /**
     * is start node?
     * 
     * @return is start node?
     * 
     * @author Andreas Schwenk
     */
    public boolean getIsStartNode()
    {
        return isStartNode;
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

    /**
     * get generated condition
     * 
     * @return generated condition
     */
    public GeneratedCondition getGeneratedCondition() {
        return generatedCondition;
    }

    /**
     * set generated condition
     * 
     * @param generatedCondition 
     */
    public void setGeneratedCondition(GeneratedCondition generatedCondition) {
        this.generatedCondition = generatedCondition;
    }
    
    private double calcStateDistance()
    {
        double stateDistance = 50.0;
        if(fromState != null && toState != null)
        {
            double deltaX = Math.abs(fromState.state.getPosition().x - toState.state.getPosition().x);
            double deltaY = Math.abs(fromState.state.getPosition().y - toState.state.getPosition().y);
            stateDistance = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
            stateDistance /= 5.0;
            if(stateDistance > 50.0)
                stateDistance = 50.0;
            
            if(fromState.state == toState.state)
                stateDistance = 50.0;
        }
        return stateDistance;
    }

    public void incCtrlPointNumber()
    {
        double stateDistance = calcStateDistance();

        selectedCtrlPoint = null;
        
        Point newPoint = new Point();
        
        LinkedList<Point> tmpList = new LinkedList<Point>();
        tmpList.add(new Point(
            fromState.state.getPosition().x + (int)(((double)fromState.state.getSize().x/2.0+stateDistance)
                    * Math.cos(2.0*Math.PI/12.0*(double)fromState.dockingPointIndex)),
            fromState.state.getPosition().y + (int)(((double)fromState.state.getSize().y/2.0+stateDistance)
                    * Math.sin(2.0*Math.PI/12.0*(double)fromState.dockingPointIndex))
        ));
        tmpList.addAll(ctrlPoints);
        tmpList.add(new Point(
            toState.state.getPosition().x + (int)(((double)toState.state.getSize().x/2.0+stateDistance)
                    * Math.cos(2.0*Math.PI/12.0*(double)toState.dockingPointIndex)),
            toState.state.getPosition().y + (int)(((double)toState.state.getSize().y/2.0+stateDistance)
                    * Math.sin(2.0*Math.PI/12.0*(double)toState.dockingPointIndex))
        ));
                
        LinkedList<Point> oldlist = ctrlPoints;
        
        double highestDistance = 0.0;
        int insertIndex=0;
        
        Point pt=null, lastPt;
        for(int i=0; i<tmpList.size(); i++)
        {
            lastPt = pt;
            pt = tmpList.get(i);
            if(lastPt != null)
            {
                double dist = Math.sqrt( 
                        (lastPt.x-pt.x)*(lastPt.x-pt.x) +  
                        (lastPt.y-pt.y)*(lastPt.y-pt.y) );
                if(dist > highestDistance)
                {
                    highestDistance = dist;
                    insertIndex = i;
                    newPoint.x = lastPt.x + (pt.x - lastPt.x)/2;
                    newPoint.y = lastPt.y + (pt.y - lastPt.y)/2;
                }
            }
        }
                
        ctrlPoints = new LinkedList<Point>();
        
        for(int i=0; i<=oldlist.size(); i++)
        {
            if(i == insertIndex-1)
                ctrlPoints.add(newPoint);
            
            if(i != oldlist.size())
                ctrlPoints.add(oldlist.get(i));
        }        
        
    }

    public void decCtrlPointNumber()
    {
        if(ctrlPoints.size() == 0)
            return;
        
        double stateDistance = calcStateDistance();

        selectedCtrlPoint = null;
        
        LinkedList<Point> tmpList = new LinkedList<Point>();
        tmpList.add(new Point(
            fromState.state.getPosition().x + (int)(((double)fromState.state.getSize().x/2.0+stateDistance)
                    * Math.cos(2.0*Math.PI/12.0*(double)fromState.dockingPointIndex)),
            fromState.state.getPosition().y + (int)(((double)fromState.state.getSize().y/2.0+stateDistance)
                    * Math.sin(2.0*Math.PI/12.0*(double)fromState.dockingPointIndex))
        ));
        tmpList.addAll(ctrlPoints);
        tmpList.add(new Point(
            toState.state.getPosition().x + (int)(((double)toState.state.getSize().x/2.0+stateDistance)
                    * Math.cos(2.0*Math.PI/12.0*(double)toState.dockingPointIndex)),
            toState.state.getPosition().y + (int)(((double)toState.state.getSize().y/2.0+stateDistance)
                    * Math.sin(2.0*Math.PI/12.0*(double)toState.dockingPointIndex))
        ));
                
        LinkedList<Point> oldlist = ctrlPoints;
        
        double lowestDistance = Double.POSITIVE_INFINITY;
        int deleteIndex=0;
        
        Point pt=null, lastPt;
        for(int i=0; i<tmpList.size(); i++)
        {
            lastPt = pt;
            pt = tmpList.get(i);
            if(lastPt != null)
            {
                double dist = Math.sqrt( 
                        (lastPt.x-pt.x)*(lastPt.x-pt.x) +  
                        (lastPt.y-pt.y)*(lastPt.y-pt.y) );
                if(dist < lowestDistance)
                {
                    lowestDistance = dist;
                    deleteIndex = i;
                }
            }
        }
                
        ctrlPoints = new LinkedList<Point>();

        deleteIndex --;
        if(deleteIndex == oldlist.size())
            deleteIndex --;
        
        for(int i=0; i<oldlist.size(); i++)
        {
            if(i != deleteIndex)
                ctrlPoints.add(oldlist.get(i));
        }                
        
    }

}
