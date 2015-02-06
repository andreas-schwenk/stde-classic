/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Generation
 * Class:       Generation
 * Created:     2011-10-28
 *
 * Rev. 1.2 GH: changed integer to signed/unsigned in VHDL code generation
 * Rev. 1.3 GH: changed to C function generation
 */
package Generation;

import Generation.Lexer.TOKEN;
import Generation.OutputVectorParser.GeneratedOutputVector;
import Generation.VariableAssignmentParser.GeneratedVarAssignment;
import Graph.Component;
import Graph.Graph;
import Graph.Graph.GRAPH_TYPE;
import Graph.SigVar.SIGVAR_TYPE;
import Graph.Signal;
import Graph.Signal.SIGNAL_DIRECTION;
import Graph.State;
import Graph.Transition;
import Graph.Variable;
import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides functionality for verifying the current graph. Further it is
 * possible to export the graph as SCXML-file.
 *
 * @author Andreas Schwenk
 */
public class Generation implements I_GENERATION {
    // *** ENUMERATIONS ***

    // *** SUBSCASSES ***
    private class VHDL_OutputHelper {

        String vec;
        LinkedList<String> cond = new LinkedList<String>();
    }

    // *** ATTRIBUTES ***
    private Log errorLog = new Log(); // error-logging

    private boolean verification_passed = true;

    // *** METHODS ***

    /**
     * Verifies the current graph and generates partial code for the languates
     * SCXML / C / VHDL
     *
     * @param graph graph that shall be verified
     * @return success of verification
     *
     * @author Andreas Schwenk
     */
    @Override
    public String verifyGraphAndPartialGenerate(Graph graph) {
        errorLog.setLogString("");

        Lexer lexer;

        boolean success = true;

        // null-pointer?
        if (graph == null) {
            // report error(s)
            errorLog.append("");
            errorLog.append(">> VERIFIKATION NICHT ERFOLGREICH! <<\n");
            verification_passed = false;
            return errorLog.getLogString();
        }

        // (0) graph-name OK?
        lexer = new Lexer();
        lexer.setString((graph.getName() + '\0').toCharArray());
        if (lexer.getToken() == Lexer.TOKEN.IDENTIFIER) {
            lexer.getNextToken();
            if (lexer.getToken() != Lexer.TOKEN.END) {
                success = false;
                errorLog.append("error: Projektname fehlerhaft: '" + graph.getName()
                        + "' (keine Sonderzeichen erlaubt; mit Buchstaben beginnend)");
            }
        } else {
            success = false;
            errorLog.append("error: Projektname fehlerhaft: '" + graph.getName()
                    + "' (keine Sonderzeichen erlaubt; mit Buchstaben beginnend)");
        }

        // (I) start-node existing?
        if (graph.getStartNode() == null) {
            errorLog.append("error: kein Startzustand vorhanden");
            success = false;
        }

        // (II) Variables
        Iterator<Variable> varIt = graph.getVariables().iterator();
        Variable var;
        while (varIt.hasNext()) {
            var = varIt.next();
            // (a) verify the name (regular grammer)
            lexer = new Lexer();
            lexer.setString((var.getName() + '\0').toCharArray());
            if (lexer.getToken() == Lexer.TOKEN.IDENTIFIER) {
                lexer.getNextToken();
                if (lexer.getToken() != Lexer.TOKEN.END) {
                    success = false;
                    errorLog.append("error: Variablenname fehlerhaft: '" + var.getName() + "'");
                }
            } else {
                success = false;
                errorLog.append("error: Variablenname fehlerhaft: '" + var.getName() + "'");
            }
            // (b) duplicate test
            // (b1) variable-by-variable
            Iterator<Variable> varItCompare = graph.getVariables().iterator();
            Variable varCompare;
            while (varItCompare.hasNext()) {
                varCompare = varItCompare.next();
                if (var != varCompare && var.getName().toLowerCase().equals(varCompare.getName().toLowerCase())) {
                    success = false;
                    errorLog.append("error: Doppelter Variablenname: '" + var.getName() + "'");
                    break;
                }
            }
            // (b2) variable-by-state
            Iterator<State> stateItCompare = graph.getStates().iterator();
            State stateCompare;
            while (stateItCompare.hasNext()) {
                stateCompare = stateItCompare.next();
                if (var.getName().toLowerCase().equals(stateCompare.getName().toLowerCase())) {
                    success = false;
                    errorLog.append("error: Name nicht disjunkt: '" + var.getName() + "' (Zustand & Variable)");
                    break;
                }
            }
            // (b3) variable-by-signal
            Iterator<Signal> sigItCompare = graph.getSignals().iterator();
            Signal sigCompare;
            while (sigItCompare.hasNext()) {
                sigCompare = sigItCompare.next();
                if (var.getName().toLowerCase().equals(sigCompare.getName().toLowerCase())) {
                    success = false;
                    errorLog.append("error: Name nicht disjunkt: '" + var.getName() + "' (Zustand & Signal)");
                    break;
                }
            }
            // (b4) identifier "reset" or "clk" used? (forbidden)
            if (var.getName().toLowerCase().equals("reset") || var.getName().toLowerCase().equals("clk")) {
                success = false;
                errorLog.append("error: Die Bezeichner 'reset' und 'clk' dürfen nicht verwendet werden");
            }
            // (c) identifiers "shl" or "shr" used? (forbidden)
            if (var.getName().toLowerCase().equals("shl") || var.getName().toLowerCase().equals("shr")) {
                success = false;
                errorLog.append("error: Die Bezeichner 'shl' und 'shr' dürfen als Variablennamen nicht verwendet werden");
            }
        }

        // (III) Signals
        Iterator<Signal> sigIt = graph.getSignals().iterator();
        Signal sig;
        while (sigIt.hasNext()) {
            sig = sigIt.next();
            // (a) verify the name (regular grammer)
            lexer = new Lexer();
            lexer.setString((sig.getName() + '\0').toCharArray());
            if (lexer.getToken() == Lexer.TOKEN.IDENTIFIER) {
                lexer.getNextToken();
                if (lexer.getToken() != Lexer.TOKEN.END) {
                    success = false;
                    errorLog.append("error: Signalname fehlerhaft: '" + sig.getName() + "'");
                }
            } else {
                success = false;
                errorLog.append("error: Signalname fehlerhaft: '" + sig.getName() + "'");
            }
            // (b) duplicate test
            // (b1) signal-by-signal
            Iterator<Signal> sigItCompare = graph.getSignals().iterator();
            Signal sigCompare;
            while (sigItCompare.hasNext()) {
                sigCompare = sigItCompare.next();
                if (sig != sigCompare && sig.getName().toLowerCase().equals(sigCompare.getName().toLowerCase())) {
                    success = false;
                    errorLog.append("error: Doppelter Signalname: '" + sig.getName() + "'");
                    break;
                }
            }
            // (b2) signal-by-state
            Iterator<State> stateItCompare = graph.getStates().iterator();
            State stateCompare;
            while (stateItCompare.hasNext()) {
                stateCompare = stateItCompare.next();
                if (sig.getName().toLowerCase().equals(stateCompare.getName().toLowerCase())) {
                    success = false;
                    errorLog.append("error: Name nicht disjunkt: '" + sig.getName() + "' (Signal & Zustand)");
                    break;
                }
            }
            // (c) identifier "reset" or "clk" used? (forbidden)
            if (sig.getName().toLowerCase().equals("reset") || sig.getName().toLowerCase().equals("clk")) {
                success = false;
                errorLog.append("error: Die Bezeichner 'reset' und 'clk' dürfen nicht verwendet werden");
            }
        }

        // (IV) parse transition-conditions, variable-assignments and output-vectors
        ConditionParser cp = new ConditionParser(
                graph.getSignals(Signal.SIGNAL_DIRECTION.IN), graph.getVariables());
        VariableAssignmentParser vap = new VariableAssignmentParser(
                graph.getSignals(Signal.SIGNAL_DIRECTION.IN), graph.getVariables());
        OutputVectorParser ovp = new OutputVectorParser(
                graph.getSignals(SIGNAL_DIRECTION.OUT), graph.getVariables(),
                graph.getSignals(Signal.SIGNAL_DIRECTION.IN));

        Component component;

        // start node (Mealy only)
        if (graph.getGraphType() == Graph.GRAPH_TYPE.MEALY && graph.getStartNode() != null) {
            Transition startNode = graph.getStartNode();
            if (ovp.parseOutputVector(startNode.getMealyOutputString()) == false) // parsing
            {
                success = false;
                errorLog.append("error: Syntaxfehler im Ausgabevektor des Startknotens: '"
                        + startNode.getMealyOutputString() + "'");
                errorLog.append(ovp.getErrorLog());
                startNode.setOutputVectorError(true);
            } else {
                startNode.setOutputVectorError(false);
                startNode.setGeneratedOutputVector(ovp.getGeneratedOutputVector());
            }
        }

        // for all components
        for (Iterator it = graph.getComponents().iterator(); it.hasNext();) {
            component = (Component) it.next();

            // in case of a transition => verification by the condition-parser
            if (component instanceof Transition) {
                Transition t = (Transition) component;
                if (cp.parseCondition(t.getCondition()) == false) // parsing
                {
                    success = false;
                    errorLog.append("error: Syntaxfehler in der Übergangsbed.: '" + t.getCondition() + "'");
                    errorLog.append(cp.getErrorLog());
                    t.setConditionError(true);
                } else {
                    t.setGeneratedCondition(cp.getGeneratedCondition());
                    t.setConditionError(false);
                }
                if (graph.getGraphType() == Graph.GRAPH_TYPE.MEALY) {
                    if (ovp.parseOutputVector(t.getMealyOutputString()) == false) // parsing
                    {
                        success = false;
                        errorLog.append("error: Syntaxfehler im Ausgabevektor des Übergangs '"
                                + t.getCondition() + "': '" + t.getMealyOutputString() + "'");
                        errorLog.append(ovp.getErrorLog());
                        t.setOutputVectorError(true);
                    } else {
                        t.setOutputVectorError(false);
                        t.setGeneratedOutputVector(ovp.getGeneratedOutputVector());
                    }
                }
            } // in case of a state
            else if (component instanceof State) {
                State s = (State) component;
                // verify the name
                lexer = new Lexer();
                lexer.setString((s.getName() + '\0').toCharArray());
                if (lexer.getToken() == Lexer.TOKEN.IDENTIFIER) {
                    lexer.getNextToken();
                    if (lexer.getToken() == Lexer.TOKEN.END) {
                        s.setNameError(false);
                    } else {
                        success = false;
                        errorLog.append("error: Zustandsname fehlerhaft: '" + s.getName() + "'");
                        s.setNameError(true);
                    }
                } else {
                    success = false;
                    errorLog.append("error: Zustandsname fehlerhaft: '" + s.getName() + "'");
                    s.setNameError(true);
                }
                // verify the output-vector in case of moore
                if (graph.getGraphType() == Graph.GRAPH_TYPE.MOORE) {
                    if (ovp.parseOutputVector(s.getMooreOutputString()) == false) // parsing
                    {
                        success = false;
                        errorLog.append("error: Syntaxfehler im Ausgabevektor des Zustands '"
                                + s.getName() + "': '" + s.getMooreOutputString() + "'");
                        errorLog.append(ovp.getErrorLog());
                        s.setOutputVectorError(true);
                    } else {
                        s.setOutputVectorError(false);
                        s.setGeneratedOutputVector(ovp.getGeneratedOutputVector());
                    }
                }

                // => verificaton by the variable-assignment-parser
                // any variable assignemts?
                if (s.isVariableAssignmentsEnabled()) {
                    s.setVariableAssignmentError(false);
                    // parse variable assigment
                    if (vap.parseVariableAssignment(s.getVariableAssignments())) {
                        s.setGeneratedVariableAssignments(vap.getGeneratedVariableAssignments());
                    } else {
                        success = false;
                        errorLog.append("error: Syntaxfehler in der Variablenzuweisung: '" + s.getVariableAssignments() + "'");
                        errorLog.append(vap.getErrorLog());
                        s.setVariableAssignmentError(true);
                    }
                }
                // duplicate test (two states with same name?)
                Iterator<Component> compItCompare = graph.getComponents().iterator();
                Component compCompare;
                while (compItCompare.hasNext()) {
                    compCompare = compItCompare.next();
                    if (component != compCompare && component.getName().toLowerCase().equals(compCompare.getName().toLowerCase())) {
                        success = false;
                        errorLog.append("error: Doppelter Zustandsname: '" + component.getName() + "'");
                        s.setNameError(true);
                        break;
                    }
                }

            }
        }

        // verify graph
        errorLog.append("");

        if (success) {
            errorLog.append(">> VERIFIKATION ERFOLGREICH! <<\n");
        } else {
            errorLog.append(">> VERIFIKATION NICHT ERFOLGREICH! <<\n");
        }

        verification_passed = success;

        return errorLog.getLogString();
    }

    /**
     * shows all signals and variables (DEBUG)
     *
     * @param graph
     *
     * @author Andreas Schwenk
     */
    private void showSigVar(Graph graph) {
        // (i) show signals
        Iterator<Signal> itSig = graph.getSignals().iterator();
        Signal s;
        System.out.println("** used signals:");
        while (itSig.hasNext()) {
            s = itSig.next();
            System.out.println(s.getName() + ", " + s.getType() + ", " + s.getDirection()
                    + ", " + s.getDescription());
        }
        // (ii) show variables
        Iterator<Variable> itVar = graph.getVariables().iterator();
        Variable v;
        System.out.println("** used variables:");
        while (itVar.hasNext()) {
            v = itVar.next();
            System.out.println(v.getName() + ", " + v.getType()
                    + ", " + v.getDescription());
        }
    }

    /**
     * exports given graph as a SCXML-file. Formal specification can be found
     * here: http://www.w3.org/2005/07/scxml The standard was changed in sone
     * points in the year before (see "Software-Praktikum 2 - 2010 CUAS")
     *
     * @param file output-file
     * @param graph graph to be exported
     * @throws IOException java-input-output-exception
     *
     * @author Andreas Schwenk
     */
    @Override
    public String exportAsSCXML(File file, Graph graph) throws IOException {
// DEBUG: show all signals / variables
//        showSigVar(graph);
// END DEBUG

        // verify graph
        verifyGraphAndPartialGenerate(graph);
        if (!verification_passed) {
            return errorLog.getLogString();
        }

        // create a new file
        PrintWriter pw = new PrintWriter(new FileWriter(file));

        // (I.) writer header
        pw.println("<?xml version=\"1.0\"?>");
        pw.println("<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\" profile=\"diagram\" name=\""
                + graph.getName() + "\" initial=\"" + graph.getStartNode().getToState().getName() 
                + "\" graph_type=\""+graph.getGraphType().name().toLowerCase()+"\">");

        // (II.) write signals
        pw.println(); // empty line
        pw.println("  <signals>");
        Signal signal;
        String direction = "", type = "";
        // for all signals
        Iterator<Signal> itSig;
        itSig = graph.getSignals().iterator();
        while (itSig.hasNext()) {
            signal = itSig.next();
            // detect signal-direction
            switch (signal.getDirection()) {
                case IN:
                    direction = "in";
                    break;
                case OUT:
                    direction = "out";
                    break;
                case INOUT:
                    direction = "inout";
                    break;
            }
            // detect signal-type
            switch (signal.getType()) {
                case BIT:
                    type = "bit";
                    break;
                case BIT_N:
                    type = "vector";
                    break;
                case SIGNED:
                    type = "integer";
                    break;
                case UNSIGNED:
                    type = "vector";
                    break;
            }

            // type := nibble | byte  if applicable (bitLenght in {4,8})
            if (type.equals("vector")) {
                switch (signal.getBitLength()) {
                    case 4:
                        type = "nibble";
                        break;
                    case 8:
                        type = "byte";
                        break;
                }
            }

            // write to file
            // (a) no need of attribute 'size'
            if (type.equals("bit") || type.equals("nibble") || type.equals("byte")) {
                pw.println("    <signal name = \"" + signal.getName() + "\" dir=\""
                        + direction + "\" type = \"" + type + "\"/>");
            } // (b) attribute 'size' needed
            else {
                long size = signal.getBitLength();
                // output
                pw.println("    <signal name = \"" + signal.getName() + "\" dir=\""
                        + direction + "\" type = \"" + type + "\" size = \"" + size + "\" />");
            }
        }
        pw.println("  </signals>");

        // (III.) write variables
        pw.println(); // empty line
        pw.println("  <variables>");
        Variable variable;
        type = "";
        // for all variables
        Iterator<Variable> itVar;
        itVar = graph.getVariables().iterator();
        while (itVar.hasNext()) {
            variable = itVar.next();
            // detect signal-type
            switch (variable.getType()) {
                case BIT:
                    type = "bit";
                    break;
                case BIT_N:
                    type = "vector";
                    break;
                case SIGNED:
                    type = "integer";
                    break;
                case UNSIGNED:
                    type = "vector";
                    break;
            }
            // type := nibble | byte  if applicable (bitLenght in {4,8})
            if (type.equals("vector")) {
                switch (variable.getBitLength()) {
                    case 4:
                        type = "nibble";
                        break;
                    case 8:
                        type = "byte";
                        break;
                }
            }

            // write to file
            // (a) no need of attribute 'size'
            if (type.equals("bit") || type.equals("nibble") || type.equals("byte")) {
                pw.println("    <var name = \"" + variable.getName() + "\" type = \"" + type + "\"/>");
            } // (b) attribute 'size' needed
            else {
                long size = variable.getBitLength();
                // output
                pw.println("    <var name = \"" + variable.getName() + "\" type = \"" + type
                        + "\" size = \"" + size + "\" />");
            }
        }
        pw.println("  </variables>");

        // (IV.) states and transitions
        pw.println(); // empty line
        pw.println("  <states>");
        Component component;
        State state;
        String conditionStr; // condition as a string (for transitions)
        // for all states
        Iterator<State> itStates = graph.getStates().iterator();
        while (itStates.hasNext()) {
            state = itStates.next();

            // (IV.i) start tag
            pw.println("    <state id = \"" + state.getName() + "\">");

            // (IV.ii) position
            Point position = state.getPosition();
            pw.println("      <position x=\"" + position.x + "\" y=\"" + position.y + "\" />");

            // (IV.iii) size
            Point size = state.getSize();
            pw.println("      <size width=\"" + size.x + "\" height=\"" + size.y + "\" />");

            // (IV.iv) transitions
            Transition transition;
            pw.println("      <transitions>");
            // for all transitions of current state
            for (Iterator itTrans = state.getTransitions().iterator(); itTrans.hasNext();) {
                transition = (Transition) itTrans.next();
                // write to file
                conditionStr = transition.getGeneratedCondition().neutral;

                conditionStr = conditionStr.replaceAll("#", ""); // in case of SCXML all numbers are decimal
                // => just remove '#' from intermediate-
                //    condition-format (e. g. "#123#" -> "123")

                State toState = transition.getToState();
                pw.println("        <transition cond = \"" + conditionStr
                        + "\" target = \"" + toState.getName() + "\">");

                // output in case of Mealy
                if (graph.getGraphType() == GRAPH_TYPE.MEALY) {
                    LinkedList<GeneratedOutputVector> llGov = transition.getGeneratedOutputVector();

                    Iterator<Signal> outSigIt = graph.getSignals(SIGNAL_DIRECTION.OUT).iterator();
                    Signal outSignal;
                    int i = 0;
                    while (outSigIt.hasNext()) {
                        outSignal = outSigIt.next();
                        GeneratedOutputVector gov = llGov.get(i);
                        if (gov.variable != null) {
                            pw.println("          <assign signal = \"" + outSignal.getName() + "\" expr = \""
                                    + gov.variable.getName() + "(" + gov.upperBound + "," + gov.lowerBound + ")" + "\"/>");
                        } else {
                            pw.println("          <assign signal = \"" + outSignal.getName() + "\" expr = \"" + gov.value + "\"/>");
                        }

                        i++;
                    }
                }

                pw.println("        </transition>");
            }
            pw.println("      </transitions>");
            // Variable assignments?
            if (state.isVariableAssignmentsEnabled()) {
                Iterator<GeneratedVarAssignment> itGVA = state.getGeneratedVariableAssignments().iterator();
                GeneratedVarAssignment gva;
                while (itGVA.hasNext()) {
                    gva = itGVA.next();
                    pw.println("      <onentry>");
                    pw.println("        <assign variable = \"" + gva.lhs + "\" expr = \"" + gva.rhsNeutral + "\"/>");
                    pw.println("      </onentry>");
                }
            }

            // Moore? => write output-vector
            if (graph.getGraphType() == GRAPH_TYPE.MOORE) {
                LinkedList<GeneratedOutputVector> llGov = state.getGeneratedOutputVector();

                pw.println("      <during>");

                Iterator<Signal> outSigIt = graph.getSignals(SIGNAL_DIRECTION.OUT).iterator();
                Signal outSignal;
                int i = 0;
                while (outSigIt.hasNext()) {
                    outSignal = outSigIt.next();
                    GeneratedOutputVector gov = llGov.get(i);
                    if (gov.variable != null) {
                        pw.println("        <assign signal = \"" + outSignal.getName() + "\" expr = \""
                                + gov.variable.getName() + "(" + gov.upperBound + "," + gov.lowerBound + ")" + "\"/>");
                    } else {
                        pw.println("        <assign signal = \"" + outSignal.getName() + "\" expr = \"" + gov.value + "\"/>");
                    }

                    i++;
                }

                pw.println("      </during>");
            }

            // (IV.vi) end tag
            pw.println("    </state>\n");
        }
        pw.println("  </states>");

        // (V.) write startNode
        pw.println(""); //empty line
        Transition startNode = graph.getStartNode();
        State sNTarget = startNode.getToState();
        pw.println("  <startNode target=\"" + sNTarget.getName() + "\" condition=\"" + startNode.getCondition() + "\">");
        pw.println("  </startNode>");

        // (VI.) write trailer
        pw.println("</scxml>");

        // close file
        pw.close();

        errorLog.append(">> EXPORT ALS SCXML ERFOLGREICH! <<\n");

        return errorLog.getLogString();
    }

    /**
     * generate C-Code
     *
     * @param file_c c output-file
     * @param file_h h output-file
     * @param file_e c exec output file
     * @param graph graph to be exported
     * @throws IOException java-input-output-exception
     *
     * Method of Operation From version 1.3, 3 files are generated: -
     * fsm_<modelname>.h contains the declarations including InVector,
     * OutVector, step function - fsm_<modelname>_FSM.c contains the definitions
     * of the step function - fsm_exec_<modelname>.c contains an example for
     * calling the FSM The header file contains - typedefs of struct for input
     * and output vector - typedef enum for the states - declaraions of -
     * reset-function (omputes start state) - step function (computes transition
     * as well as output (Moore-type FSA) - transition function (computes next
     * state) - ourput function (computes outputs from state and inputs
     * (Mealy-type FSA) - to realize Mealy semantics we need the separation
     * between the functions. The source code file contains these functions.
     *
     * @author Andreas Schwenk, Georg Hartung (from V1.2)
     *
     * This function was totally reworked in V1.3
     *
     */
    @Override
    public String generateCode_C(File file_h, File file_c, File file_e, Graph graph) throws IOException {
        // verify graph
        verifyGraphAndPartialGenerate(graph);
        if (!verification_passed) {
            return errorLog.getLogString();
        }

        // create a new file
        /* 
         */
        PrintWriter pwc = new PrintWriter(new FileWriter(file_c));
        PrintWriter pwh = new PrintWriter(new FileWriter(file_h));
        PrintWriter pwe = new PrintWriter(new FileWriter(file_e));

        // (I.) writer header changed 
        pwh.println("/* ");
        pwh.println("*   " + graph.getName());
        pwh.println("*");
        pwh.println("*   Header for FSM implementation in C");
        pwh.println("*     h file");
        pwh.println("*/");
        pwh.println();

        pwc.println("/* ");
        pwc.println("*   " + graph.getName());
        pwc.println("*");
        pwc.println("*   Definition of FSM function in C");
        pwc.println("*     c file");
        pwc.println("*/");
        pwc.println();
        pwc.println("#include \"" + file_h.getName() + "\"");
        pwc.println();

        // Definition of standard types bool, uint8_t etc.
        pwh.println("#ifndef __cplusplus");
        pwh.println("#ifndef false");
        pwh.println("#define false 0");
        pwh.println("#define true 1");
        pwh.println("#endif");
        pwh.println("typedef int bool;");
        pwh.println("#endif");
        pwh.println();
        pwh.println("#ifndef byte");
        pwh.println("typedef unsigned char byte;");
        pwh.println("#endif");
        pwh.println();
        pwh.println("#ifndef __gtiTypes");
        pwh.println("#define __gtiTypes");
        pwh.println("typedef unsigned long long uint64_t;");
        pwh.println("typedef unsigned long int uint32_t;");
        pwh.println("typedef unsigned short int uint16_t;");
        pwh.println("typedef unsigned char uint8_t;");
        pwh.println("typedef long long int64_t;");
        pwh.println("typedef long int int32_t;");
        pwh.println("typedef short int int16_t;");
        pwh.println("typedef char int8_t;");
        pwh.println("#endif");

        // (II.) write signal type  
        // Extension/Change in Version 1.3
        pwh.println();
        pwh.println("/* SIGNAL TYPE */");
        Signal signal;
        String direction = "", type = "";
        int bitLen;
        // rev. 1.3
        String typename = "";
        boolean hasInOuts = false;
        // for all signals
        Iterator<Signal> itSig;
        for (int i = 0; i < 3; i++) // input + output signals
        {
            if (i == 0) {
                pwh.println("/*   (a) input signal type */");
                typename = "InVector_" + graph.getName() + " ";
                itSig = graph.getSignals(SIGNAL_DIRECTION.IN).iterator();
                pwh.println("typedef struct {");
            } else if (i == 1) {
                pwh.println("/*   (b) output signal type */");
                itSig = graph.getSignals(SIGNAL_DIRECTION.OUT).iterator();
                typename = "OutVector_" + graph.getName();
                pwh.println("typedef struct {");
            } else {
                pwc.println("/*   (c) input/output signals */");
                itSig = graph.getSignals(SIGNAL_DIRECTION.INOUT).iterator();
                if ((hasInOuts = itSig.hasNext())) {
                    typename = "InOutVector_" + graph.getName();
                    pwh.println("typedef struct {");
                }
            }

            while (itSig.hasNext()) {
                signal = itSig.next();
                bitLen = signal.getBitLength();
                // detect signal-type
                switch (signal.getType()) {
                    case BIT:
                        type = "bool";
                        break;
                    case BIT_N:
                    case UNSIGNED:
                        if (bitLen == 1) {
                            type = "bool";
                        } else if (bitLen <= 8) {
                            type = "uint8_t";
                        } else if (bitLen <= 16) {
                            type = "uint16_t";
                        } else if (bitLen <= 32) {
                            type = "uint32_t";
                        } else if (bitLen <= 64) {
                            type = "uint64_t";
                        }
                        break;
                    case SIGNED:
                        if (bitLen == 1) {
                            type = "bool";
                        } else if (bitLen <= 8) {
                            type = "int8_t";
                        } else if (bitLen <= 16) {
                            type = "int16_t";
                        } else if (bitLen <= 32) {
                            type = "int32_t";
                        } else if (bitLen <= 64) {
                            type = "int64_t";
                        }
                        break;
                }
                pwh.println("  " + type + " " + signal.getName() + "; // " + signal.getDescription());
            }   // end iteration over signals
            // generate end of typedef struct and generate pointer type 
            if ((i < 2) || hasInOuts) {
                pwh.println("  } " + typename + ";");
                pwh.println("typedef " + typename + " *Ptr" + typename + ";");
            }
        }
        pwh.println();
        // Transition/Output function
        pwh.println("bool fsm_" + graph.getName() + "(");
        pwc.println("bool fsm_" + graph.getName() + "(");
        pwh.println("  bool reset,              // reset of fsm");
        pwc.println("  bool reset,              // reset of fsm");
        pwh.println("  PtrInVector_" + graph.getName() + " inV,         // input signals");
        pwc.println("  PtrInVector_" + graph.getName() + " inV,         // input signals");
        pwh.println("  PtrOutVector_" + graph.getName() + " outV        // output signals");
        pwc.println("  PtrOutVector_" + graph.getName() + " outV        // output signals");
        if (hasInOuts) {
            pwh.println(",  PtrInOutVector inoutV   // in/out signals");
            pwc.println(",  PtrInOutVector inoutV   // in/out signals");
        }
        pwh.println(");");
        pwc.println(")");
        pwc.println("{");
        pwh.println("void exec_fsm_" + graph.getName() + "();");
        pwh.println();
        pwh.close(); // header finished

        // generate state Variable which is intern to fsm
        pwc.println("    /* state variable (static, inside step function   */");
        pwc.println("    static enum ");
        pwc.println("    {");
        State state;
        // for all states
        Iterator<State> itStates = graph.getStates().iterator();
        State s;
        while (itStates.hasNext()) {
            s = itStates.next();
            pwc.println("      " + s.getName() + (itStates.hasNext() ? "," : "")
                    + " // " + s.getDescription());
        }
        pwc.println("    } state;");

        pwc.println("    static initialized = false;   // enforce resetting!");

        // (II.) write variables
        // here no initialisation is made!
        LinkedList<Variable> variables = graph.getVariables();
        if (variables.size() > 0) {
            pwc.println();
            pwc.println("    /* VARIABLES */");
            Variable variable;
            type = "";
            Iterator<Variable> itVar;
            itVar = variables.iterator();
            while (itVar.hasNext()) {
                variable = itVar.next();
                bitLen = variable.getBitLength();
                // detect signal-type
                switch (variable.getType()) {
                    case BIT:
                        type = "bool";
                        break;
                    case BIT_N:
                    case UNSIGNED:
                        if (bitLen == 1) {
                            type = "bool";
                        } else if (bitLen <= 8) {
                            type = "uint8_t";
                        } else if (bitLen <= 16) {
                            type = "uint16_t";
                        } else if (bitLen <= 32) {
                            type = "uint32_t";
                        } else if (bitLen <= 64) {
                            type = "uint64_t";
                        }
                        break;
                    case SIGNED:
                        if (bitLen == 1) {
                            type = "bool";
                        } else if (bitLen <= 8) {
                            type = "int8_t";
                        } else if (bitLen <= 16) {
                            type = "int16_t";
                        } else if (bitLen <= 32) {
                            type = "int32_t";
                        } else if (bitLen <= 64) {
                            type = "int64_t";
                        }
                        break;
                }
                pwc.println("    static " + type + " " + variable.getName() + "; // " + variable.getDescription());
            }
        }

        pwc.println();
        pwc.println("    /* SET INITIAL STATE if requested */");
        pwc.println("    if (reset) {");
        pwc.println("        state = " + graph.getStartNode().getToState().getName() + ";");
        pwc.println("        initialized = true; ");
        pwc.println("        /* output of start-node */");
        /*
         With Mealy type FSM this is the output denotated on the start arrow.
         With Moore type FSM this is the output denotated on the start arrow's toState 
         */
        if (graph.getGraphType() == GRAPH_TYPE.MEALY) {  // in this case output is denoted at the start transition
            writeCOutput(graph.getStartNode().getGeneratedOutputVector(), graph.getSignals(SIGNAL_DIRECTION.OUT), pwc, 8);
        } else {
            State startState = graph.getStartNode().getToState();  // from start transition to start node
            writeCOutput(startState.getGeneratedOutputVector(), graph.getSignals(SIGNAL_DIRECTION.OUT), pwc, 8);
        }
        pwc.println("    }");
        pwc.println("    else if (initialized) {");
        // state transition (Moore, Mealy) + output function (Mealy)
        if (graph.getGraphType() == GRAPH_TYPE.MEALY) {
            pwc.println("        /* state transition function + output function */");
        } else {
            pwc.println("        /* state transition function */");
        }
        pwc.println("        switch(state)");
        pwc.println("        {");
        // for all states
        itStates = graph.getStates().iterator();
        while (itStates.hasNext()) {
            state = itStates.next();
            pwc.println("            case " + state.getName() + ":");
            // variable assignments
            if (state.isVariableAssignmentsEnabled()) {
                Iterator<GeneratedVarAssignment> itGVA = state.getGeneratedVariableAssignments().iterator();
                GeneratedVarAssignment gva;
                while (itGVA.hasNext()) {
                    gva = itGVA.next();
                    pwc.println("                " + gva.lhs + " = " + gva.rhsC + "; // variable assignment");
                }
            }
            // for all outgoing transitions of current state
            Transition transition;
            Iterator<Transition> itTrans = state.getTransitions().iterator();
            String IF = "";
            String conditionStr; // condition string
            while (itTrans.hasNext()) {
                transition = itTrans.next();
                if (IF.equals("")) {
                    IF = "if";
                } else {
                    IF = "else if";
                }

                conditionStr = transition.getGeneratedCondition().C;

                // write condition
                pwc.println("                " + IF + "(" + conditionStr + ")");
                if (graph.getGraphType() == Graph.GRAPH_TYPE.MEALY) {
                    pwc.println("                {");
                    // mealy-output if applicable
                    writeCOutput(transition.getGeneratedOutputVector(), graph.getSignals(SIGNAL_DIRECTION.OUT), pwc, 20);
                }

                pwc.println("                    state = " + transition.getToState().getName() + ";");
                if (graph.getGraphType() == Graph.GRAPH_TYPE.MEALY) {
                    pwc.println("                }");
                }
            }
// TODO: "INOUT" MISSING
            pwc.println("                break;");
        }
        pwc.println("            default: ;  // never reached");

        pwc.println("        }      // end of switch ");
        pwc.println("    }         // end if (initialized) ");
        pwc.println("    else ;    // nothing to do here");
        // generate MOORE outputs in 2nd loop
        if (graph.getGraphType() == GRAPH_TYPE.MOORE) {
            pwc.println();
            pwc.println("    /*   Output function    */");
            pwc.println("    switch (state) { ");
            // for all states
            itStates = graph.getStates().iterator();
            while (itStates.hasNext()) {
                state = itStates.next();
                pwc.println("        case " + state.getName() + ":");
                writeCOutput(state.getGeneratedOutputVector(), graph.getSignals(SIGNAL_DIRECTION.OUT), pwc, 12);
                pwc.println("            break;");
            }
            pwc.println("        default: ;  // never reached");
            pwc.println("    }      // end of output switch ");
            pwc.println();
        }
        pwc.println("    return(initialized);");
        pwc.println("}  // end of fsm function ");
        pwc.println();
        pwc.close();  // close C File

// Start pwe generation from here
        pwe.println("/* ");
        pwe.println("*   " + graph.getName());
        pwe.println("*");
        pwe.println("*   example code for executing the FSM in C ");
        pwe.println("*     exec c file");
        pwe.println("*/");
        pwe.println();
        pwe.println();
        pwe.println("/* For Simulation here a flag is used ");
        pwe.println("   Outcomment next line with define of SIMULATION_" + graph.getName() + " if real operation */");
        pwe.println("#define SIMULATION_" + graph.getName());
        pwe.println();
        pwe.println("#ifdef SIMULATION_" + graph.getName());
        pwe.println("#include <ctype.h>");
        pwe.println("#include <stdio.h>");
        pwe.println("#endif");
        pwe.println();
        pwe.println("#include \"" + file_h.getName() + "\"");
        pwe.println();
        pwe.println("/* ");
        pwe.println("  input function ");
        pwe.println("   @param bool *reset     out   reset wanted");
        pwe.println("   @param InVector_" + graph.getName() + " *inV   out   input values");
        pwe.println("   @returns true, if not shutdown of fsm ");
        pwe.println("*/");
        pwe.println("bool readInput_" + graph.getName() + "(bool *reset, InVector_" + graph.getName() + " *inV)");
        pwe.println("{");
        pwe.println("   bool end;");
        pwe.println("   uint64_t inputBuf;");
        pwe.println();
        pwe.println("   /* get information whether the FSM should be  ");
        pwe.println("      shut down (Variable end)                                 ");
        pwe.println("      resetted  (Parameter *reset)                           */");
        pwe.println("   // ### INSERT MANUALLY IF NOT SIMULATION FROM CONSOLE ###");
        pwe.println("#ifdef SIMULATION_" + graph.getName());
        pwe.println("   char str[2];");
        pwe.println("   printf(\"\\nFSM Stop (S,s), Reset (R,r) or Normal Step (N) (default N): \");");
        pwe.println("   scanf(\"%s\", str);");
        pwe.println("   end = (toupper(str[0])==\'S\');");
        pwe.println("   if (end) return false;");
        pwe.println("   *reset = (toupper(str[0])==\'R\');");
        pwe.println("   // ### END PART FOR SIMULATION READING FSM Stop or Reset ###");
        pwe.println("#else");
        pwe.println("   if (end) return false;");
        pwe.println("#endif");
        pwe.println("   if (*reset) return true;");
        pwe.println("   /* read input-signals from device */");
        pwe.println("   // ### INSERT MANUALLY OTHER CODE IF NOT SIMULATION ###");
        Signal sig;
        LinkedList<Signal> inSigs = graph.getSignals(SIGNAL_DIRECTION.IN);
        inSigs.addAll(graph.getSignals(SIGNAL_DIRECTION.INOUT));
        Iterator<Signal> inSigIt = inSigs.iterator();
        while (inSigIt.hasNext()) {
            sig = inSigIt.next();
            pwe.println("   // inV->" + sig.getName() + " = XXX;");
        }
        pwe.println("#ifdef SIMULATION_" + graph.getName());
        Iterator<Signal> inSigIt2 = inSigs.iterator();
        while (inSigIt2.hasNext()) {
            sig = inSigIt2.next();
            pwe.println("   printf(\"Enter hexadecimal value for " + sig.getName() + ": \");");
            pwe.println("   scanf(\"%lx\", &inputBuf);");
            pwe.println("   inV->" + sig.getName() + " = (/*### ADD CAST WITH APPROPRIATE TYPE ###*/)inputBuf;");
        }
        pwe.println("#endif");
        pwe.println("   return true;");
        pwe.println("}; /* end input function */");
        pwe.println();
        pwe.println("/* ");
        pwe.println("  output function  ");
        pwe.println("   @param OutVector *outV_" + graph.getName() + "   in   output values");
        pwe.println("*/");
        pwe.println("void writeOutput_" + graph.getName() + "(OutVector_" + graph.getName() + " *outV)");
        pwe.println("{");
        pwe.println("    int64_t outputBuf;");
        pwe.println("    /* write output-signals to device */");
        pwe.println("    // ### INSERT MANUALLY ###");

        // output signals (including inout-signals)
        LinkedList<Signal> outSigs = graph.getSignals(SIGNAL_DIRECTION.OUT);
        outSigs.addAll(graph.getSignals(SIGNAL_DIRECTION.INOUT));
        Iterator<Signal> outSigIt = outSigs.iterator();
        while (outSigIt.hasNext()) {
            sig = outSigIt.next();
            pwe.println("    // XXX = outV." + sig.getName() + ";");
        }
        pwe.println();
        pwe.println("#ifdef SIMULATION_" + graph.getName());
        pwe.println("    printf(\"\\nActual output signal values of FSM\");");
        Iterator<Signal> outSigIt2 = outSigs.iterator();
        while (outSigIt2.hasNext()) {
            sig = outSigIt2.next();
            pwe.println("    outputBuf = (int64_t) (outV->" + sig.getName() + ");");
            pwe.println("    printf(\"\\n  " + sig.getName() + " = %d \", outputBuf);");
        }
        pwe.println("#endif");
        pwe.println("}    /* end of output function*/");
        pwe.println();
        pwe.println("/* ");
        pwe.println("  exec function  ");
        pwe.println("*/");

        pwe.println("void exec_fsm_" + graph.getName() + "()");
        pwe.println("{");
        pwe.println("    /* Generation of input/ output vector */");
        pwe.println("    InVector_" + graph.getName() + " inV = {0};");
        pwe.println("    OutVector_" + graph.getName() + " outV = {0};");
        pwe.println("    bool exec = false;");
        pwe.println("    bool reset = true;");
        pwe.println("    /* Initialisation  */");
        pwe.println("    exec = fsm_" + graph.getName() + "( true, &inV, &outV);");
        pwe.println("    writeOutput_" + graph.getName() + "(&outV);");
        pwe.println("    /* Example of execution LOOP */");
        pwe.println("    while(exec)");
        pwe.println("    {");
        pwe.println("        exec = readInput_" + graph.getName() + "( &reset, &inV);");
        pwe.println("        if (!exec) break;");
        pwe.println("        exec = fsm_" + graph.getName() + "( reset, &inV, &outV);");
        pwe.println("        writeOutput_" + graph.getName() + "(&outV);");
        pwe.println("        // ### INSERT OTHER STUFF HERE");
        pwe.println("        // ### NO WHILE LOOP OR UNCONTROLLED GETS, SCANF ETC.!!!");
        pwe.println("    } // end while loop of fsm ");
        pwe.println("#ifdef SIMULATION_" + graph.getName());
        pwe.println("    printf(\"\\nEND OF SIMULATION OF FSM!!!\");");
        pwe.println("#endif");
        pwe.println("}  /* end of exec function */");

        // close file
        pwe.close(); // close exec file

        errorLog.append(">> GENERIERUNG C-CODE: ERFOLGREICH! <<\n");

        return errorLog.getLogString();
    }

    private void writeCOutput(LinkedList<GeneratedOutputVector> llGov,
            LinkedList<Signal> outsig, PrintWriter pw, int spaces) {
// TODO: "INOUT" MISSING
        Iterator<Signal> outSigIt = outsig.iterator();
        Signal outSignal;
        int i = 0;
        while (outSigIt.hasNext()) {
            outSignal = outSigIt.next();

            String value = "";
            GeneratedOutputVector gov = llGov.get(i);
            if ((gov.variable != null)) {
                if (gov.isVectorComponent) {
                    value = String.format("(" + gov.variable.getName() + " >> " + gov.upperBound + ") & 0x1");
                } else if (gov.isVectorSlice) {
                    int mask = ((1 << (gov.upperBound - gov.lowerBound + 1)) - 1);
                    value = String.format("(" + gov.variable.getName() + " >> " + gov.lowerBound + ") & 0x%X", mask);
                } else {
                    value = "" + gov.variable.getName();
                }
            } else if (gov.signal != null) {
                if (gov.isVectorComponent) {
                    value = String.format("((inV->" + gov.signal.getName() + ") >> " + gov.upperBound + ") & 0x1");
                } else if (gov.isVectorSlice) {
                    int mask = ((1 << (gov.upperBound - gov.lowerBound + 1)) - 1);
                    value = String.format("((inV->" + gov.signal.getName() + ") >> " + gov.lowerBound + ") & 0x%X", mask);
                } else {
                    value = "(inV->" + gov.signal.getName() + ")";
                }
            } else { // constant
                value = "" + gov.value;
            }
            String spacesStr = String.format("%" + spaces + "s", " ");  // print spaces
            pw.println(spacesStr + "(outV->" + outSignal.getName() + ") = " + value + ";");
            i++;
        }
    }

    /**
     * generate VHDL-Code
     *
     * @param file output-file
     * @param graph graph to be exported
     * @throws IOException java-input-output-exception
     *
     * @author Andreas Schwenk
     */
    @Override
    public String generateCode_VHDL(File file, Graph graph, boolean useProcess) throws IOException {
        // verify graph
        verifyGraphAndPartialGenerate(graph);
        if (!verification_passed) {
            return errorLog.getLogString();
        }

        // create a new file
        PrintWriter pw = new PrintWriter(new FileWriter(file));

        // (I.) writer header
        pw.println("-- " + graph.getName());
        pw.println();
        pw.println("library ieee;");
        pw.println("use ieee.std_logic_1164.all;");
        pw.println("use ieee.numeric_std.all;");
        pw.println();

        // (II.) write ENTITY
        pw.println("entity " + graph.getName().toUpperCase() + " is");
        pw.println("    port");
        pw.println("    (");
        Signal signal;
        String direction = "", type = "";
        int bitLen;
        // for all signals
        Iterator<Signal> itSig;
        //  (a) calculate max signal-name length
        int maxSignalNameLength = 5; // "RESET"
        itSig = graph.getSignals().iterator();
        while (itSig.hasNext()) {
            signal = itSig.next();
            if (signal.getName().length() > maxSignalNameLength) {
                maxSignalNameLength = signal.getName().length();
            }
        }
        //  (b) write CLK and RESET-signals
        pw.println(String.format("        %-" + maxSignalNameLength + "s : %-5s %-36s -- %s",
                "CLK", "in", "std_logic;", "clock"));
        pw.println(String.format("        %-" + maxSignalNameLength + "s : %-5s %-36s -- %s",
                "RESET", "in", "std_logic;", "reset"));
        //  (c) get number of signals
        int numSigs = graph.getSignals().size();
        //  (d) write signals
        int currentSigNumber = 0;
        for (int i = 0; i < 3; i++) // input + output signals
        {
            if (i == 0) {
                itSig = graph.getSignals(SIGNAL_DIRECTION.IN).iterator();
            } else if (i == 1) {
                itSig = graph.getSignals(SIGNAL_DIRECTION.OUT).iterator();
            } else {
                itSig = graph.getSignals(SIGNAL_DIRECTION.INOUT).iterator();
            }
            while (itSig.hasNext()) {
                signal = itSig.next();
                bitLen = signal.getBitLength();
                // detect signal-type
                switch (signal.getType()) {
                    case BIT:
                        type = "std_logic";
                        break;
                    case BIT_N:
                        type = "std_logic_vector(" + (long) (bitLen - 1) + " downto 0)";
                        break;
                    case UNSIGNED:
                        if (bitLen == 1) {
                            type = "std_logic";
                        } else {
                            type = "unsigned(" + (long) (bitLen - 1) + " downto 0)";
                        }
                        break;
                    case SIGNED:
                        if (bitLen == 1) {
                            type = "std_logic";
                        } else {
                            type = "signed(" + (long) (bitLen - 1) + " downto 0)";
                        }
                        break;
                }
                pw.println(String.format("        %-" + maxSignalNameLength + "s : %-5s %-36s -- %s",
                        signal.getName().toUpperCase(), signal.getDirection().toString().toLowerCase(), type + ((currentSigNumber < numSigs - 1) ? ";" : " "), signal.getDescription()));

                currentSigNumber++;
            }
        }
        pw.println("    );");
        pw.println("end " + graph.getName().toUpperCase() + ";");
        pw.println();

        // (III.) write architecture
        pw.println("architecture BEHAVE of " + graph.getName().toUpperCase() + " is");

        // use VHDL-process for state transitions, ...?
//##### USE PROCESS #####
        if (useProcess) {
            // (III.b) begin architecture
            pw.println("begin");
            // (III.c) process incl sensitivity-list
            String sensitivityList = "";
            // in-signals
            LinkedList<Signal> sensitivitySignals = graph.getSignals(SIGNAL_DIRECTION.IN);
            // inout-signals
            sensitivitySignals.addAll(graph.getSignals(SIGNAL_DIRECTION.INOUT));
            Iterator<Signal> itSensitivitySignals = sensitivitySignals.iterator();
            Signal sig;
            if (sensitivitySignals.size() > 0) {
                sensitivityList += ", ";
            }
            while (itSensitivitySignals.hasNext()) {
                sig = itSensitivitySignals.next();
                sensitivityList += sig.getName().toUpperCase() + (itSensitivitySignals.hasNext() ? ", " : "");
            }
            pw.println("    process(RESET, CLK" + sensitivityList + ") is");

            // (III.a) write state-type
            pw.println("        -- DEFINE A STATE-TYPE");
            pw.println("        type TSTATE is(");
            State state;
            // for all states
            State s;
            // calculate lenght max state-name
            int maxStateNameLength = 0;
            Iterator<State> itStates = graph.getStates().iterator();
            while (itStates.hasNext()) {
                s = itStates.next();
                if (s.getName().length() > maxStateNameLength) {
                    maxStateNameLength = s.getName().length();
                }
            }
            // write states
            itStates = graph.getStates().iterator();
            while (itStates.hasNext()) {
                s = itStates.next();
                pw.println(String.format("            %-" + (maxStateNameLength + 1) + "s -- %s",
                        s.getName().toUpperCase() + (itStates.hasNext() ? "," : " "), s.getDescription()));
            }
            pw.println("        );");
            pw.println("        variable STATE : TSTATE;");

            // (III.b) write variables
            LinkedList<Variable> variables = graph.getVariables();
            Iterator<Variable> itVar;
            Variable variable;
            if (variables.size() > 0) {
                // calculate lenght max state-name
                int maxVariableNameLength = 0;
                itVar = graph.getVariables().iterator();
                while (itVar.hasNext()) {
                    variable = itVar.next();
                    if (variable.getName().length() > maxVariableNameLength) {
                        maxVariableNameLength = variable.getName().length();
                    }
                }
                // write variables
                pw.println("        -- VARIABLES");
                type = "";
                itVar = variables.iterator();
                while (itVar.hasNext()) {
                    variable = itVar.next();
                    bitLen = variable.getBitLength();
                    // detect signal-type
                    switch (variable.getType()) {
                        case BIT:
                            type = "std_logic";
                            break;
                        case BIT_N:
                            type = "std_logic_vector(" + (long) (bitLen - 1) + " downto 0)";
                            break;
                        case UNSIGNED:
                            if (bitLen == 1) {
                                type = "std_logic";
                            } else {
                                type = "unsigned(" + (long) (bitLen - 1) + " downto 0)";
                            }
                            break;
                        case SIGNED:
                            if (bitLen == 1) {
                                type = "std_logic";
                            } else {
                                type = "signed(" + (long) (bitLen - 1) + " downto 0)";
                            }
                            break;
                    }
                    pw.println(String.format("        variable %-" + maxVariableNameLength + "s : %-36s -- %s",
                            variable.getName().toUpperCase(), type + ";", variable.getDescription()));
                }
            }

            pw.println("    begin"); // begin process

            pw.println("        if RESET='1' then");
            pw.println("            STATE := " + graph.getStartNode().getToState().getName().toUpperCase() + ";");
            pw.println("        elsif CLK'event and CLK='1' then");

            // STATE TRANSITION FUNCTION
            pw.println("            -- STATE-TRANSITION-FUNCTION");
            pw.println("            case STATE is");
            // for all states
            itStates = graph.getStates().iterator();
            while (itStates.hasNext()) {
                state = itStates.next();
                pw.println("                when " + state.getName().toUpperCase() + " =>");
                // variable assignments
                if (state.isVariableAssignmentsEnabled()) {
                    Iterator<GeneratedVarAssignment> itGVA = state.getGeneratedVariableAssignments().iterator();
                    GeneratedVarAssignment gva;
                    while (itGVA.hasNext()) {
                        gva = itGVA.next();
                        // hier muss noch erweitert werden!!! if (gva.rhs)
                        pw.println("                    " + gva.lhs + " := " + gva.rhsVHDL + "; -- variable assignment");
                    }
                }
                // for all outgoing transitions of current state
                Iterator<Transition> itTrans = state.getTransitions().iterator();
                String IF = "";
                String conditionStr; // condition string
                Transition transition;
                while (itTrans.hasNext()) {
                    transition = itTrans.next();
                    if (IF.equals("")) {
                        IF = "if";
                    } else {
                        IF = "elsif";
                    }

                    conditionStr = transition.getGeneratedCondition().VHDL;

                    if (conditionStr.equals("1")) {
                        conditionStr = "true";
                    }

                    // write condition
                    pw.println("                    " + IF + "(" + conditionStr + ") then");

                    pw.println("                        STATE := " + transition.getToState().getName().toUpperCase() + ";");
                }
                if (state.getTransitions().size() > 0) {
                    pw.println("                    end if;");
                }

            }
            pw.println("                when others =>");
            pw.println("                    STATE := " + graph.getStartNode().getToState().getName().toUpperCase() + ";");
            pw.println("            end case;");
            // end if
            pw.println("        end if;");

            // OUTPUT FUNCTION
            pw.println("        -- OUTPUT-FUNCTION");
            pw.println("        case STATE is");
            // for all states
            itStates = graph.getStates().iterator();
            while (itStates.hasNext()) {
                state = itStates.next();
                pw.println("                when " + state.getName().toUpperCase() + " =>");

                // ** output
                LinkedList<Signal> outsig = graph.getSignals(SIGNAL_DIRECTION.OUT);
                Signal outSignal;
                // (a) get maximum output-signal length
                int maxlen = 0;
                Iterator<Signal> outSigIt = outsig.iterator();
                while (outSigIt.hasNext()) {
                    outSignal = outSigIt.next();
                    if (outSignal.getName().length() > maxlen) {
                        maxlen = outSignal.getName().length();
                    }
                }
                // (b) output
                // (b1) MOORE
                if (graph.getGraphType() == GRAPH_TYPE.MOORE) {
                    LinkedList<GeneratedOutputVector> llGov = state.getGeneratedOutputVector();
                    writeVHDLOutput(llGov, outsig, pw, maxlen, true);
                } // (b2) MEALY
                else {
                    // start-state
                    if (state == graph.getStartNode().getToState()) {
                        pw.println("                    if RESET='1' then");
                        LinkedList<GeneratedOutputVector> llGov = graph.getStartNode().getGeneratedOutputVector();
                        writeVHDLOutput(llGov, outsig, pw, maxlen, false);
                    }
                    // for all outgoing transitions of current state
                    Iterator<Transition> itTrans = state.getTransitions().iterator();
                    String IF = "";
                    String conditionStr; // condition string
                    Transition transition;
                    while (itTrans.hasNext()) {
                        transition = itTrans.next();
                        if (IF.equals("") && !(state == graph.getStartNode().getToState())) {
                            IF = "if";
                        } else {
                            IF = "elsif";
                        }

                        conditionStr = transition.getGeneratedCondition().VHDL;

                        if (conditionStr.equals("1")) {
                            conditionStr = "true";
                        }

                        // write condition
                        pw.println("                    " + IF + "(" + conditionStr + ") then");

                        // write output
                        LinkedList<GeneratedOutputVector> llGov = transition.getGeneratedOutputVector();
                        writeVHDLOutput(llGov, outsig, pw, maxlen, false);

                        if (state.getTransitions().size() > 0 || (state == graph.getStartNode().getToState())) {
                            pw.println("                    end if;");
                        }
                    }
                }

            }
            pw.println("            end case;");

            pw.println("    end process;");
        } //##### DO NOT USE PROCESS #####
        else {
            pw.println();

            // (III.a) write state-type
            pw.println("    -- define state-type");
            pw.println("    type TSTATE is");
            pw.println("    (");
            State state;
            // for all states
            State s;
            // calculate lenght max state-name
            int maxStateNameLength = 0;
            Iterator<State> itStates = graph.getStates().iterator();
            while (itStates.hasNext()) {
                s = itStates.next();
                if (s.getName().length() > maxStateNameLength) {
                    maxStateNameLength = s.getName().length();
                }
            }
            // write states
            itStates = graph.getStates().iterator();
            while (itStates.hasNext()) {
                s = itStates.next();
                pw.println(String.format("        %-" + (maxStateNameLength + 1) + "s -- %s",
                        s.getName().toUpperCase() + (itStates.hasNext() ? "," : " "), s.getDescription()));
            }
            pw.println("    );");
            pw.println("    signal CURRENT_STATE, NEXT_STATE : TSTATE;");

            pw.println("    constant RESET_STATE : TSTATE := " + graph.getStartNode().getToState().getName().toUpperCase() + ";");

            // (III.b) begin architecture
            pw.println();
            pw.println("begin");
            pw.println();

            // STATE TRANSITION FUNCTION
            pw.println("    NEXT_STATE <=");
            itStates = graph.getStates().iterator();
            while (itStates.hasNext()) {
                state = itStates.next();

                // for all outgoing transitions of current state
                Iterator<Transition> itTrans = state.getTransitions().iterator();
                Transition transition;
                while (itTrans.hasNext()) {
                    transition = itTrans.next();

                    if (transition.getToState().getName().toUpperCase().equals(
                            state.getName().toUpperCase())) {
                        continue; // skip, if no state change
                    }
                    pw.println(String.format(
                            "        %-"
                            + (maxStateNameLength)
                            + "s when CURRENT_STATE = %-"
                            + (maxStateNameLength)
                            + "s and (%s) else",
                            transition.getToState().getName().toUpperCase(),
                            state.getName().toUpperCase(),
                            transition.getGeneratedCondition().VHDL));
                }
            }
            pw.println("        CURRENT_STATE;");

            // RESET-PROCESS
            pw.println();
            pw.println("    process(CLK, RESET, NEXT_STATE) is");
            pw.println("    begin"); // begin process
            pw.println("        if RESET='1' then");
            pw.println("            CURRENT_STATE <= RESET_STATE;");
            pw.println("        elsif CLK'event and CLK='1' then");
            pw.println("            CURRENT_STATE <= NEXT_STATE;");
            pw.println("        end if;");
            pw.println("    end process;");
            pw.println();

            // OUTPUT FUNCTION
            String value = "";
            pw.println("    -- OUTPUT-FUNCTION");

            // (a) ##### MOORE #####
            if (graph.getGraphType() == GRAPH_TYPE.MOORE) {
                itSig = graph.getSignals(SIGNAL_DIRECTION.OUT).iterator();
                int i = 0;
                while (itSig.hasNext()) {
                    Signal outSignal = itSig.next();
                    pw.println("    " + outSignal.getName() + " <=");

                    // ** (a) collect -> a set of states is mapped to each output-value of the current signal **
                    LinkedList<VHDL_OutputHelper> vohList = new LinkedList<VHDL_OutputHelper>();

                    itStates = graph.getStates().iterator();
                    while (itStates.hasNext()) {
                        state = itStates.next();

                        GeneratedOutputVector gov;
                        gov = state.getGeneratedOutputVector().get(i);

                        switch (outSignal.getType()) {
                            case BIT:
                                value = "'" + gov.value + "'";
                                break;
                            case BIT_N:
                                // convert to binary
                                value = "\"" + String.format("%" + outSignal.getBitLength() + "s",
                                        Integer.toBinaryString(gov.value)).replace(" ", "0") + "\"";
                                break;
                            case SIGNED:
                            case UNSIGNED:
                                value = "" + gov.value;
                                break;
                        }

                        String cond = String.format("CURRENT_STATE = %s", state.getName().toUpperCase());

                        VHDL_OutputHelper voh;
                        boolean found = false;
                        for (int j = 0; j < vohList.size(); j++) {
                            voh = vohList.get(j);
                            if (voh.vec.equals(value)) {
                                found = true;
                                voh.cond.add(cond);
                            }
                        }
                        if (!found) {
                            voh = new VHDL_OutputHelper();
                            voh.vec = value;
                            voh.cond.add(cond);
                            vohList.add(voh);
                        }
                    }
                    // ** (b) consolidation and output **
                    VHDL_OutputHelper voh;
                    for (int j = 0; j < vohList.size(); j++) {
                        voh = vohList.get(j);
                        String line = String.format("        %s when ", voh.vec);
                        if (j == vohList.size() - 1) {
                            line = String.format("        %s;", voh.vec);
                        } else {
                            for (int k = 0; k < voh.cond.size(); k++) {
                                if (k == 0) {
                                    line += "(" + voh.cond.get(k) + ")";
                                } else {
                                    line += " or (" + voh.cond.get(k) + ")";
                                }
                                if (k == voh.cond.size() - 1) {
                                    line += " else";
                                }
                            }
                        }
                        pw.println(line);
                    }

                    // ** next **
                    i++;
                }

            } // (b) ##### MEALY #####
            else {
                itSig = graph.getSignals(SIGNAL_DIRECTION.OUT).iterator();
                int i = 0;
                while (itSig.hasNext()) {
                    Signal outSignal = itSig.next();
                    pw.println("    " + outSignal.getName() + " <=");

                    // ** (a) collect -> a set of states is mapped to each output-value of the current signal **
                    LinkedList<VHDL_OutputHelper> vohList = new LinkedList<VHDL_OutputHelper>();

                    Transition transition;
                    Iterator<Transition> itTransitions;
                    itTransitions = graph.getTransitions().iterator();
                    while (itTransitions.hasNext()) {
                        transition = itTransitions.next();

                        GeneratedOutputVector gov;
                        gov = transition.getGeneratedOutputVector().get(i);

                        switch (outSignal.getType()) {
                            case BIT:
                                value = "'" + gov.value + "'";
                                break;
                            case BIT_N:
                                // convert to binary
                                value = "\"" + String.format("%" + outSignal.getBitLength() + "s",
                                        Integer.toBinaryString(gov.value)).replace(" ", "0") + "\"";
                                break;
                            case SIGNED:
                            case UNSIGNED:
                                value = "" + gov.value;
                                break;
                        }

                        String cond = String.format("CURRENT_STATE = %s and (%s)", transition.getFromState().getName().toUpperCase(),
                                transition.getGeneratedCondition().VHDL);

                        VHDL_OutputHelper voh;
                        boolean found = false;
                        for (int j = 0; j < vohList.size(); j++) {
                            voh = vohList.get(j);
                            if (voh.vec.equals(value)) {
                                found = true;
                                voh.cond.add(cond);
                            }
                        }
                        if (!found) {
                            voh = new VHDL_OutputHelper();
                            voh.vec = value;
                            voh.cond.add(cond);
                            vohList.add(voh);
                        }
                    }

                    // ** (b) consolidation and output **
                    VHDL_OutputHelper voh;
                    for (int j = 0; j < vohList.size(); j++) {
                        voh = vohList.get(j);
                        String line = String.format("        %s when ", voh.vec);
                        if (j == vohList.size() - 1) {
                            line = String.format("        %s;", voh.vec);
                        } else {
                            for (int k = 0; k < voh.cond.size(); k++) {
                                if (k == 0) {
                                    line += "(" + voh.cond.get(k) + ")";
                                } else {
                                    line += " or (" + voh.cond.get(k) + ")";
                                }
                                if (k == voh.cond.size() - 1) {
                                    line += " else";
                                }
                            }
                        }
                        pw.println(line);
                    }

                    // ** next **
                    i++;
                }

            }

        }

        pw.println();
        pw.println("end BEHAVE;"); // end main

        // close file
        pw.close();

        errorLog.append(">> GENERIERUNG VHDL-CODE: ERFOLGREICH! <<\n");

        return errorLog.getLogString();
    }

    /**
     * writes the output-vector (VHDL)
     *
     * @param llGov generated output-vector
     * @param outsig output-signal-list
     * @param pw generated text
     * @param maxlen max name-length of an output-signal
     * @param moore Moore? (just needed for white-spaces)
     *
     * @author Andreas Schwenk
     */
    private void writeVHDLOutput(LinkedList<GeneratedOutputVector> llGov,
            LinkedList<Signal> outsig, PrintWriter pw, int maxlen,
            boolean moore) {
        Iterator<Signal> outSigIt = outsig.iterator();
        String value = ""; // can be a constant or variable-name
// TODO: "INOUT" MISSING
        int i = 0;
        Signal outSignal;
        while (outSigIt.hasNext()) {
            outSignal = outSigIt.next();

            GeneratedOutputVector gov = llGov.get(i);

            if (gov.variable != null) {
                Variable var = llGov.get(i).variable;
                // variable adressing (get appropriate bits)
                String varAdressing = "";
                if (gov.isVectorComponent) {
                    varAdressing = "(" + gov.upperBound + ")";
                } else if (gov.isVectorSlice) {
                    varAdressing = "(" + gov.upperBound + " downto " + gov.lowerBound + ")";
                }
                value = var.getName().toUpperCase() + varAdressing;
            } else if (gov.signal != null) {
                Signal sgn = llGov.get(i).signal;
                // variable adressing (get appropriate bits)
                String sgnAdressing = "";
                if (gov.isVectorComponent) {
                    sgnAdressing = "(" + gov.upperBound + ")";
                } else if (gov.isVectorSlice) {
                    sgnAdressing = "(" + gov.upperBound + " downto " + gov.lowerBound + ")";
                }
                value = sgn.getName().toUpperCase() + sgnAdressing;
            } else {
                switch (outSignal.getType()) {
                    case BIT:
                        value = "'" + gov.value + "'";
                        break;
                    case BIT_N:
                        // convert to binary
                        value = "\"" + String.format("%" + outSignal.getBitLength() + "s",
                                Integer.toBinaryString(gov.value)).replace(" ", "0") + "\"";
                        break;
                    case SIGNED:
                        value = "to_signed(" + gov.value + "," + outSignal.getBitLength() + ")";
                        break;
                    case UNSIGNED:
                        value = "to_unsigned(" + gov.value + "," + outSignal.getBitLength() + ")";
                        break;
                }
            }

            if (moore) {
                pw.println(String.format("                    %-" + maxlen
                        + "s <= %s;", outSignal.getName().toUpperCase(), value));
            } else {
                pw.println(String.format("                        %-" + maxlen
                        + "s <= %s;", outSignal.getName().toUpperCase(), value));
            }

            i++;
        }

    }

}
