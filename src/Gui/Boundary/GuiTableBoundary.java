/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Gui.Boundary
 * Class:       GuiTableBoundary
 * Created:     ?
 */

package Gui.Boundary;

import Graph.Signal;
import Graph.Variable;
import Graph.State;
import java.util.LinkedList;

/**
 *
 * @author user
 */
public class GuiTableBoundary {

    private LinkedList<Signal> signals = new LinkedList<Signal>();
    private LinkedList<Variable> variables = new LinkedList<Variable>();
    private LinkedList<State> states = new LinkedList<State>();

    public LinkedList<Signal> getSignals() {
        return signals;
    }

    public void setSignals(LinkedList<Signal> signals) {
        this.signals = signals;
    }

    public LinkedList<Variable> getVariables() {
        return variables;
    }

    public void setVariables(LinkedList<Variable> variables) {
        this.variables = variables;
    }

    public LinkedList<State> getStates() {
        return states;
    }

    public void setStates(LinkedList<State> states) {
        this.states = states;
    }

    public void addSignal(Signal s) {
        signals.add(s);
    }

    public Signal removeSignal(int i) {
        Signal sig = signals.get(i);
        signals.remove(i);
        return sig;
    }

    public Variable removeVariable(int i) {
        Variable var = variables.get(i);
        variables.remove(i);
        return var;
    }

    public void addVariable(Variable v) {
        variables.add(v);
    }

    public void addState(State s) {
        states.add(s);
    }

    public State removeState(int i) {
        State s = states.get(i);
        states.remove(i);
        return s;
    }
}
