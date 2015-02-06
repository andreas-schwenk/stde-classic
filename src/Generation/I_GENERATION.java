/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Generation
 * Interface:   I_GENERATION
 * Created:     2011-10-28
 */

package Generation;

import Graph.Graph;
import java.io.File;
import java.io.IOException;

/**
 *
 * see class: Generation
 * 
 * @author Andreas Schwenk
 */
public interface I_GENERATION
{
    // verification
    public String verifyGraphAndPartialGenerate(Graph graph);
        
    // generation
    public String exportAsSCXML(File file, Graph graph) throws IOException;
    public String generateCode_C(File file_h, File file_c, File file_e,  Graph graph) throws IOException;
    public String generateCode_VHDL(File file, Graph graph, boolean useProcess) throws IOException;
}
