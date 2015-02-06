/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Graph
 * Class:       Signal
 * Created:     2011-10-28
 */

package Graph;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Derivation of superclass "SigVar".
 * 
 * Signals describe the interface between the model and the world outside.
 * A signal has a direction. Either it comes from outside <input> or control
 * the output behavior <output>. Also a combination <inout> (bidirectional) exists.
 * 
 * @author Andreas Schwenk
 */
public class Signal extends SigVar
{
    // *** ENUMARATIONS ***
    public enum SIGNAL_DIRECTION {IN,OUT,INOUT}; // direction

    // *** ATTRIBUTES ***
    private SIGNAL_DIRECTION direction;

    // *** METHODS ***
    /**
     * Constructor. Performs assignments
     * 
     * @param graph owner of the signal
     * @param name name of the signal
     * @param description description of the signal
     * @param type type of the signal
     * @param direction  direction of the signal 
     * 
     * @author Andreas Schwenk
     */
    public Signal(Graph graph, String name, String description, SIGVAR_TYPE type, SIGNAL_DIRECTION direction)
    {
        super();
        
        this.name = name;
        this.description = description;
        this.type = type;
        this.direction = direction;
        
        this.parent = graph;
    }
        
    /**
     * Constructor. Creates a Signal from a given file (more precisely:
     * a given data-input-stream).
     *
     * @param in data-stream of the input-file
     * @param graph owner
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public Signal(DataInputStream in, Graph graph) throws IOException
    {
        loadFromFile(in, graph);        
    }
    
    /**
     * gets the direction (in|out|inout) of the signal
     * 
     * @return direction of the signal
     * 
     * @author Andreas Schwenk
     */
    public SIGNAL_DIRECTION getDirection() {
        return direction;
    }
    
    /**
     * sets the direction (in|out|inout) of the signal
     * 
     * @param direction direction of the signal
     */
    public void setDirection(SIGNAL_DIRECTION direction) {
        this.direction = direction;
    }
    
    /**
     * for explanation: see superclass "SigVar"
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
        super.loadSigVarFromFile(in, graph);
        
        direction = SIGNAL_DIRECTION.valueOf(in.readUTF());
    }
    
    /**
     * for explanation: see superclass "SigVar"
     * 
     * @param out
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void saveToFile(DataOutputStream out) throws IOException
    {
        super.saveToFile(out);
        
        out.writeUTF(direction.name());
    }
}
