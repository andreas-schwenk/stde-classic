/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   Graph
 * Class:       Container
 * Created:     2011-10-28
 */
package Graph;

import java.util.LinkedList;

/**
 * Abstract superclass for all components that act as container, e. g. SuperState,
 *  ParallelState.
 * Most important, a set of states and transition have to be stored
 * 
 * @author Andreas Schwenk
 */
public abstract class Container extends Component
{
    // *** ATTRIBUTES ***
    protected LinkedList<Component> containedComponents;
    
    // *** METHODS ***
    /**
     * Constructor. Instantiations etc.
     * 
     * @author Andreas Schwenk
     */
    public Container()
    {
        containedComponents = new LinkedList<Component>();
    }
    
    /**
     * Adds a component to the container
     * 
     * @param c component to be added
     * 
     * @author Andreas Schwenk
     */
    public void addComponent(Component c)
    {
        containedComponents.add(c);
    }
    
    /**
     * Removes a component from the container
     * 
     * @param c component to be removed
     * 
     * @author Andreas Schwenk
     */
    public void removeComponent(Component c)
    {
        containedComponents.remove(c);
    }
}
