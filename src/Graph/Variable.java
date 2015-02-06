/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Graph
 * Class:       Variable
 * Created:     2011-10-28
 */

package Graph;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Derivation of superclass "SigVar".
 * 
 * Variables allow states to be "countable". That means a state can be
 * traversed more than once - for example for a serial transmitter / receiver.
 * This repetitive behavior is controlled by variables.
 * 
 * @author Andreas Schwenk
 */
public class Variable extends SigVar
{
    // *** ATTRIBUTES ***
    
    /**
     * Constructor. Creates a state by a given graph
     * 
     * @param graph owner of the variable
     * @param name name of the variable
     * @param description description of the variable
     * @param type type of the variable
     * 
     * @author Andreas Schwenk
     */
    public Variable(Graph graph, String name, String description, SIGVAR_TYPE type)
    {
        super();
        this.name = name;
        this.description = description;
        this.type = type;
        
        this.parent = graph;
    }
    
    /**
     * Constructor. Creates a Variable from a given file (more precisely:
     * a given data-input-stream).
     * 
     * @param in data-stream of the input-file
     * @param graph owner
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public Variable(DataInputStream in, Graph graph) throws IOException
    {
        loadFromFile(in, graph);
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
    }
}


