/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   Graph
 * Class:       Component
 * Created:     2011-10-28
 */
package Graph;

import Gui.CustomTextField;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Abstract super class of all components, e. g. State, Transition, SuperStae,
 *  ParallelState.
 * Stores parameters like name, description, position, position, size, selection
 * 
 * @author Andreas Schwenk
 */
public abstract class Component
{
    // *** ATTRIBUTES ***
    
    // name and description
    protected CustomTextField name;
    protected String description;
    
    // visual properties
    private Point position; // private to force calling setPosition()-method (see below)
    private Point size; // private to force calling setSize()-method (see below)
    
    // selection
    protected boolean isSelected=false;
    
    // graph
    protected Graph parent=null;
    
    // ID
    protected int id=0; // component's ID
    protected static int idCounter=0; // stores next component id
    
    // *** METHODS ***
    
    /**
     * constructor; 
     * sets ID, initializes attributes
     * 
     * @author Andreas Schwenk
     */
    public Component()
    {
        id = idCounter++;
        name = new CustomTextField(this, CustomTextField.SYMBOL_LIST.NAME, 0.0);
        description = "";
        position = new Point();
        // size
        size = new Point();
        size.x = 50;
        size.y = 20;
    }
    
    /**
     * Abstract method, that is called in case a key-event occurred.
     * Needed for all CustomTextFields, e. g. for component's name etc.
     * 
     * for further information see class "GUI.CustomTextField"
     * 
     * @param e key-event
     * 
     * @author Andreas Schwenk
     */
    public abstract void keyEvent(KeyEvent e);
    
    /**
     * Abstract method. Derived methods render component on the screen
     * 
     * @param g2d graphics-object from GUI.GraphicsPanel
     * @param font font-object
     * @param mousePosition current mouse-position (needed for highlighting etc)
     * 
     * @author Andreas Schwenk
     */
    public abstract void render(Graphics2D g2d, Font font, Point mousePosition);
    
    /**
     * Abstract method. Derived methods load component's data from file.
     * Common data is loaded from method: "loadComponentFromFile" in this class.
     * 
     * @param in input-stream, belongs to file that is read
     * @param graph e. g. needed to obtain the graph-type (Moore, Mealy)
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public abstract void loadFromFile(DataInputStream in, Graph graph) throws IOException;
    
    /**
     * Abstract method. Derived methods fix associations. This is important
     * in the process of loading:
     * 
     * Example:
     *   Transitions (derivative of class Component) have associations to
     *   States ("From-state" and "to-state").
     *   When a transition is saved into a file, only the IDs of the states
     *   are stored.
     *   To regain the Java-references, a search has to be performed for
     *   each of the associated IDs.
     *   Thus, associations can only be fixed, after loading ALL components.
     * 
     * @return success
     * 
     * @author Andreas Schwenk
     */
    public abstract boolean fixAssociations();
    
    /**
     * Abstract method. Derived methods implement their selection by mouse.
     * A selection is called from an instance of the class "Workflow".
     * 
     * @param mousePosition current mouse-position
     * @param multiSelect enables support for selecting multiple components
     * 
     * @author Andreas Schwenk
     */
    public abstract void select(Point mousePosition, boolean multiSelect);
    
    
    /**
     * Abstract method. Derived methods implement their rectangle-selection by mouse.
     * A selection is called from an instance of the class "Workflow".
     * 
     * @param topLeft top-left
     * @param bottomRight bottom-right
     * 
     * @author Andreas Schwenk
     */
    public abstract void rectSelect(Point topLeft, Point bottomRight);
    
    /**
     * Abstract method. Derived methods implement a selections of their
     * so called "pickers".
     * A picker is a (displayed) object, which allows to manipulate an object.
     * E. g. a "move-picker" allows to drag a component (changes translation).
     * 
     * @param mousePosition current mouse-position
     * 
     * @author Andreas Schwenk
     */
    public abstract boolean selectPicker(Point mousePosition);
    
    /**
     * Abstract method. Derived methods implement a deselection of all
     * their pickers. See method "selecPicker(..)" for further information about
     * "pickers"
     * 
     * @author Andreas Schwenk
     */
    public abstract void deselectPickers();
    
    /**
     * Abstract method. Derived methods implement a dragging of designated
     * objects like "pickers". See method "select" for further information about
     * "pickers"
     * 
     * @param mousePosition current mouse position
     * 
     * @author Andreas Schwenk
     */
    public abstract void mouseDragged(Point mousePosition);
    
    /**
     * Abstract method. Derived methods implement a mouse-over detection
     * 
     * @param mousePosition current mouse-position
     * @return mouse over: yes / no
     * 
     * @author Andreas Schwenk
     */
    public abstract boolean isMouseOver(Point mousePosition);
    
    /**
     * Loads a component from given file (data-input-stream).
     * Only common properties of this super-class are loaded. There is
     * necessity in loading all further parameters in derivation of abstract
     * method: "loadFromFile(..)".
     * 
     * @param in data-input-stream of input-file
     * @param graph e. g. needed to obtain the graph-type (Moore, Mealy)
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public void loadComponentFromFile(DataInputStream in, Graph graph) throws IOException
    {
        id = in.readInt();
        description = in.readUTF();
        position.x = in.readInt();
        position.y = in.readInt();
        size.x = in.readInt();
        size.y = in.readInt();
        name = new CustomTextField(in, this);
        parent = graph;
    }
    
    /**
     * saves properties which are common to all components, e. g. name, size
     * 
     * @param out data-stream of output-file
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public void saveToFile(DataOutputStream out) throws IOException
    {
        out.writeInt(id);
        out.writeUTF(description);
        out.writeInt(position.x);
        out.writeInt(position.y);
        out.writeInt(size.x);
        out.writeInt(size.y);
        name.saveToFile(out);
    }
        
    /**
     * gets the description of the component
     * 
     * @return description
     * 
     * @author Andreas Schwenk
     */
    public String getDescription() {
        return description;
    }

    /**
     * sets the description of the component
     * 
     * @param description new description
     * 
     * @author Andreas Schwenk
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * gets the name of the component
     * 
     * @return name
     * 
     * @author Andreas Schwenk
     */
    public String getName() {
        return name.getText();
    }

    /**
     * sets the name of the component
     * 
     * @param name new name
     * 
     * @author Andreas Schwenk
     */
    public void setName(String name)
    {
        this.name.setText(name);
    }

    /**
     * gets the position of the component
     * 
     * @return position
     * 
     * @author Andreas Schwenk
     */
    public Point getPosition() {
        return position;
    }

    /**
     * sets the position of the component
     * 
     * @param position new position
     * 
     * @author Andreas Schwenk
     */
    public void setPosition(Point position) {
        position.x -= position.x % 16;
        position.y -= position.y % 16;
        this.position = position;
    }

    /**
     * gets the size of the component
     * 
     * @return size
     * 
     * @author Andreas Schwenk
     */
    public Point getSize() {
        return size;
    }

    /**
     * sets a new size for the component
     * 
     * @param size new size
     * 
     * @author Andreas Schwenk
     */
    public void setSize(Point size) {
        size.x -= size.x % 16;
        size.y -= size.y % 16;
        this.size = size;
    }
    
    public void setSizeX(int sizex)
    {
        sizex -= sizex % 16;
        this.size.x = sizex;
    }
    
    public void setSizeY(int sizey)
    {
        sizey -= sizey % 16;
        this.size.y = sizey;
    }

    /**
     * gets whether the component is selected or not
     * 
     * @return is the component selected?
     * 
     * @author Andreas Schwenk
     */
    public boolean getIsSelected() {
        return isSelected;
    }

    /**
     * selects the component. This method may be overridden by subclasses to
     * provide a more precise handling
     * 
     * @author Andreas Schwenk
     */
    public void select() {
        this.isSelected = true;
    }

    /**
     * deselects the component. This method may be overridden by subclasses to
     * provide a more precise handling
     * 
     * @author Andreas Schwenk
     */
    public void deselect() {
        this.isSelected = false;
    }

    /**
     * gets the ID of the component
     * 
     * @return ID
     * 
     * @author Andreas Schwenk
     */
    public int getId() {
        return id;
    }

    /**
     * sets the owner (parent) of the component => a graph
     * 
     * @param parent graph
     * 
     * @author Andreas Schwenk
     */
    public void setParent(Graph parent) {
        this.parent = parent;
    }
    
    /**
     * moves the component by given vector
     * 
     * @param moveVec move vector
     * 
     * @author Andreas Schwenk
     */
    public void move(Point moveVec)
    {
        setPosition(new Point(getPosition().x + moveVec.x, getPosition().y + moveVec.y));
    }
    
    /**
     * set weather the name contains errors or not
     * @param val errors?
     */
    public void setNameError(boolean val)
    {
        name.setUnderlined(val);
    }
}
