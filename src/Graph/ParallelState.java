/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   Graph
 * Class:       ParallelState
 * Created:     2011-10-28
 */
package Graph;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A parallel-state is one kind of a container (a container is again a component).
 * It provides the possibility of concurrency.
 * 
 * A container stores other components itself.
 * 
 * Note: Currently, it is not foreseeable if containers will be implemented
 *       (due to lack of time)
 * 
 * @author Andreas Schwenk
 */
public class ParallelState extends Container
{
    // *** ATTRIBUTES ***

    // *** METHODS ***
    /**
     * Constructor. Creates a ParallelState by a given graph
     * 
     * @param graph owner of the new created ParallelState
     * 
     * @author Andreas Schwenk
     */
    public ParallelState(Graph graph)
    {
        super();
        this.parent = graph;
    }
    
    /**
     * Constructor. Creates a ParallelState from a given file (more precisely:
     *  a given data-input-stream).
     * 
     * @param in data-stream of the input-file
     * @param graph parent
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public ParallelState(DataInputStream in, Graph graph) throws IOException
    {
        loadFromFile(in, graph);
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
                        Point mousePosition) {
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
    public void select(Point mousePosition, boolean multiSelect) {
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
    public void keyEvent(KeyEvent e) {
    }

    /**
     * for explanation: see superclass "Component"
     * 
     * @param mousePosition 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public boolean selectPicker(Point mousePosition) {
        return false;
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @param topLeft
     * @param bottomRight 
     */
    @Override
    public void rectSelect(Point topLeft, Point bottomRight) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * for explanation: see superclass "Component"
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void deselectPickers() {
    }

    /**
     * for explanation: see superclass "Component"
     * 
     * @param mousePosition 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void mouseDragged(Point mousePosition) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        super.loadComponentFromFile(in, graph);
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
        out.writeInt(Graph.COMP_PARALLEL_STATE);
        super.saveToFile(out);
    }

}
