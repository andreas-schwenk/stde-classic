/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Generation
 * Class:       ConditionParser
 * Created:     2011-12-09
 * 
 * Version 3: added output of variables with signed/unsigned type GH 2015/02
 *
 */
package Generation;

import Generation.Lexer.TOKEN;
import Graph.SigVar.SIGVAR_TYPE;
import Graph.Signal;
import Graph.Variable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Verifies an output-vector Syntax-description (EBNF) can be found in the
 * specification-document.
 *
 * EBNF:
 *
 * <OUTPUT_VEC> ::= <OUTPUT> { "," <OUTPUT> }
 * <OUTPUT> ::= <NUMBER>
 * | <VARIABLE> ["("<NUMBER>,<NUMBER>")"]
 *
 * reference: "Niklaus Wirth - Compiler Construction"
 *
 * @author Andreas Schwenk
 */
public class OutputVectorParser {

    // *** SUB-CLASSES ***
    /**
     * stores a constant or a variable Revision 1.2: extended with information
     * whether slice, component or none
     *
     * @author Andreas Schwenk
     *
     */
    public class GeneratedOutputVector {

        public int value = 0;
        public boolean isNumber = false;
        public boolean isVectorComponent = false;
        public boolean isVectorSlice = false;
        public Variable variable = null;
        public Signal signal = null;
        public int upperBound = 0, lowerBound = 0;
    }

    // *** ENUMERATIONS ***
    // *** ATTRIBUTES ***    
    // Tokenizer
    private Lexer lex;
    // error-reporting
    private String errorStr;

    // all "output"-signals of the graph
    private LinkedList<Signal> outputSignals;
    private LinkedList<Signal> inputSignals;
    private LinkedList<Variable> variables;

    // logging (errors and success-messages; shown in the GUI [bottom-left corner])
    private Log log;

    private int signalIndex = 0;

    // generated output vector
    private LinkedList<GeneratedOutputVector> generatedOutputVector = null;

    // *** METHODS ***
    /**
     * check, if the given identifier is a variable; if true then return it
     *
     * @param identifier
     * @return variable
     *
     * @author Andreas Schwenk
     */
    private Variable getVariable(String identifier) {
        Variable v;
        // search identifier in variable-list
        Iterator<Variable> it = variables.iterator();
        while (it.hasNext()) {
            v = it.next();
            if (v.getName().equals(identifier)) {
                return v;
            }
        }
        // not a variable
        return null;
    }

    /**
     * check, if the given identifier is a input signal; if true then return it
     *
     * @param identifier
     * @return variable
     *
     * @author Andreas Schwenk
     */
    private Signal getSignal(String identifier) {
        Signal v;
        // search identifier in variable-list
        Iterator<Signal> it = inputSignals.iterator();
        while (it.hasNext()) {
            v = it.next();
            if (v.getName().equals(identifier)) {
                return v;
            }
        }
        // not a signal
        return null;
    }

    /**
     * constructor; output signals (of the graph) are needed for type-check
     *
     * @param outputSignals output-signals of the graph
     * @param variables variables of the graph
     *
     * @author Andreas Schwenk
     */
    public OutputVectorParser(LinkedList<Signal> outputSignals,
            LinkedList<Variable> variables, LinkedList<Signal> inputSignals) {
        this.outputSignals = outputSignals;
        this.inputSignals = inputSignals;
        this.variables = variables;
    }

    /**
     * <OUTPUT_VEC> ::= <OUTPUT> { "," <OUTPUT> }
     *
     * @author Andreas Schwenk
     */
    private void parseOUTPUT_VEC() {
        // <OUTPUT>
        parseOUTPUT();
        // { “||” <AND> }
        while (lex.getToken() == TOKEN.COMMA) {
            lex.getNextToken();
            signalIndex++;
            parseOUTPUT();
        }
    }

    /**
     * <OUTPUT> ::= <NUMBER> | (<VARIABLE>|<SIGNAL>) ["("<NUMBER>,<NUMBER>")"]
     *
     * Erweitert V. 1.2: Input-Signale zugelassen
     *
     * @author Andreas Schwenk
     *
     */
    private void parseOUTPUT() {
        GeneratedOutputVector gov = new GeneratedOutputVector();

        if (signalIndex >= outputSignals.size()) {
            errorStr = "Der angegebene Ausgabevektor hat mehr Elemente als erlaubt (" + outputSignals.size() + ")";
            return;
        }
        // ab hier zunaechst den Output einfach scannen. Ob es zum Input passt, erst
        // spaeter kontrollieren
        Signal s = outputSignals.get(signalIndex);
        SIGVAR_TYPE type = s.getType();
        Variable var = null;
        Signal iSgn = null;
        if (lex.getToken() == TOKEN.NUMBER) {
            lex.getNextToken();
            gov.value = lex.getNumber();
            gov.isNumber = true;
        } // (<VARIABLE>|<SIGNAL> ["("<NUMBER>,<NUMBER>")"]
        else if (lex.getToken() == TOKEN.IDENTIFIER) {
            String identifier = lex.getIdentifier();
            lex.getNextToken();
            if ((var = getVariable(identifier)) != null) {
                gov.variable = var;
            } else if ((iSgn = getSignal(identifier)) != null) {
                gov.signal = iSgn;
            } else {
                errorStr = "Identifier not a Variable or Input Signal";
            }
            if ((var != null) || (iSgn != null)) {
                if (lex.getToken() == TOKEN.LPARENTH) {
                    lex.getNextToken();
                    if (lex.getToken() == TOKEN.NUMBER) {
                        gov.upperBound = lex.getNumber();
                        lex.getNextToken();
                        if (lex.getToken() == TOKEN.COLON) {
                            lex.getNextToken();
                            if (lex.getToken() == TOKEN.NUMBER) {
                                gov.lowerBound = lex.getNumber();
                                lex.getNextToken();
                                if (lex.getToken() == TOKEN.RPARENTH) {
                                    lex.getNextToken();
                                    if (gov.lowerBound < gov.upperBound) {
                                        gov.isVectorSlice = true;
                                    } else {
                                        errorStr = "Unter Bereichsgrenze > obere Bereichsgrenze.";
                                    }
                                } else {
                                    errorStr = "Schließende Klammer fehlt.";
                                }
                            } else {
                                errorStr = "Untere Bereichsgrenze muss eine Konstante sein.";
                            }
                        } else if (lex.getToken() == TOKEN.RPARENTH) {
                            lex.getNextToken();
                            gov.isVectorComponent = true;
                        } else {
                            errorStr = "Doppelpunkt oder schließende Klammer fehlt";
                        }
                    } else {
                        errorStr = "Index oder obere Bereichsgrenze muss eine Konstante sein.";
                    }
                } else {
                    // hier muss nichts passieren. Ausdruck ist Variable
                }
            } 
            // number invalid?
            else if (lex.getToken() == TOKEN.INVALID_NUMBER) {
                errorStr = "ungültiges Format für Konstante";
            } else {
                errorStr = "erwartet: Konstante oder Variable";
            }
        }
        // generation output
        switch (type) {
            case BIT:
                if (gov.isNumber) {
                    if (gov.value != 0 && gov.value != 1) {
                        errorStr = "Ausgabesignal '" + s.getName() + "' darf nur 0 oder 1 erhalten";
                    } else {
                        generatedOutputVector.add(gov);
                    }
                } else if (gov.isVectorComponent) {
                    if (iSgn != null) {
                        if ((iSgn.getType() == SIGVAR_TYPE.BIT_N)
                                && (0 <= gov.upperBound)
                                && (gov.upperBound < iSgn.getBitLength())) {
                            generatedOutputVector.add(gov);
                        } else {
                            errorStr = "Ausgabesignal '" + s.getName() + "' muss gueltige Komponente erhalten";
                        }
                    } else if (var != null) {
                        if ((var.getType() == SIGVAR_TYPE.BIT_N)
                                && (0 <= gov.upperBound)
                                && (gov.upperBound < var.getBitLength())) {
                            generatedOutputVector.add(gov);
                        } else {
                            errorStr = "Ausgabesignal '" + s.getName() + "' muss gueltige Komponente erhalten";
                        }
                    }
                } else if (!gov.isVectorSlice) {
                    if (iSgn != null) {
                        if ((iSgn.getType() == SIGVAR_TYPE.BIT)) {
                            generatedOutputVector.add(gov);
                        } else {
                            errorStr = "Ausgabesignal '" + s.getName() + "' Falscher Wert";
                        }
                    } else if (var != null) {
                        if ((var.getType() == SIGVAR_TYPE.BIT)) {
                            generatedOutputVector.add(gov);
                        } else {
                            errorStr = "Ausgabesignal '" + s.getName() + "' Falscher Wert";
                        }
                    }
                } else {
                    errorStr = "Ausgabesignal '" + s.getName() + "' Kein Bereich erlaubt";
                }
                break;

            case BIT_N:
                if (gov.isNumber) {
                    generatedOutputVector.add(gov);
                } else if (gov.signal != null) {
                    if ((gov.signal.getType() == SIGVAR_TYPE.BIT_N)
                            && ((!gov.isVectorSlice) && (s.getBitLength() == gov.signal.getBitLength()))) {
                        generatedOutputVector.add(gov);
                    } else if ((gov.signal.getType() == SIGVAR_TYPE.BIT_N)
                            && ((gov.isVectorSlice) && (s.getBitLength() == (gov.upperBound - gov.lowerBound + 1)))) {
                        generatedOutputVector.add(gov);
                    } else {
                        errorStr = "Ausgabesignal '" + s.getName() + "' Wert passt nicht";
                    }
                } else if (gov.variable != null) {
                    if ((gov.variable.getType() == SIGVAR_TYPE.BIT_N)
                            && ((!gov.isVectorSlice) && (s.getBitLength() == gov.variable.getBitLength()))) {
                        generatedOutputVector.add(gov);
                    } else if ((gov.variable.getType() == SIGVAR_TYPE.BIT_N)
                            && ((gov.isVectorSlice) && (s.getBitLength() == (gov.upperBound - gov.lowerBound + 1)))) {
                        generatedOutputVector.add(gov);
                    } else {
                        errorStr = "Ausgabesignal '" + s.getName() + "' Wert passt nicht";
                    }
                }
                break;

            case SIGNED:
                if (gov.isNumber) {
                    generatedOutputVector.add(gov);
                } else if (gov.signal != null) {
                    if (gov.signal.getType() == SIGVAR_TYPE.SIGNED) {
                        generatedOutputVector.add(gov);
                    } else {
                        errorStr = "Ausgabesignal '" + s.getName() + "' kein signed-Wert";
                    }
                } else if (gov.variable != null) {
                    if (gov.variable.getType() == SIGVAR_TYPE.SIGNED) {
                        generatedOutputVector.add(gov);
                    } else {
                        errorStr = "Ausgabesignal '" + s.getName() + "' kein signed-Wert";
                    }
                } else {
                    errorStr = "Ausgabesignal '" + s.getName() + "' kein Zahlen- oder signed-Wert";
                }
                break;

            case UNSIGNED:
                if (gov.isNumber) {
                    if (gov.value >= 0) {
                        generatedOutputVector.add(gov);
                    } else {
                        errorStr = "Ausgabesignal '" + s.getName() + "' kann keinen negativen Wert erhalten";
                    }
                } else if (gov.signal != null) {
                    if (gov.signal.getType() == SIGVAR_TYPE.UNSIGNED) {
                        generatedOutputVector.add(gov);
                    } else {
                        errorStr = "Ausgabesignal '" + s.getName() + "' kein unsigned-Wert";
                    }
                } else if (gov.variable != null) {
                    if (gov.variable.getType() == SIGVAR_TYPE.UNSIGNED) {
                        generatedOutputVector.add(gov);
                    } else {
                        errorStr = "Ausgabesignal '" + s.getName() + "' kein unsigned-Wert";
                    }
                } else {
                    errorStr = "Ausgabesignal '" + s.getName() + "' kein Zahlen- oder unsigned-Wert";
                }
                break;
        }
    }

    /**
     * parses an output-vector (string)
     *
     * @param str output-vector
     * @return success
     *
     * @author Andreas Schwenk
     */
    public boolean parseOutputVector(String str) {
        this.log = new Log();

        if (outputSignals.size() == 0) {
            if (str.length() == 0) {
                return true;
            } else {
                log.append(" -> keine Ausgabesignale vorhanden");
                return false;
            }
        }

        // init generated output-vector
        generatedOutputVector = new LinkedList<GeneratedOutputVector>();

        // create a new instance for tokenizing
        lex = new Lexer();

        signalIndex = 0;
        // reset errors
        errorStr = null;

        // set string to tokenizer (lexer)
        lex.setString(
                (str + '\0').toCharArray());

        // parse expression (condition)
        parseOUTPUT_VEC();

        if (str.length()
                == 0 || signalIndex != outputSignals.size() - 1) {
            errorStr = "Ausgabevektor mit falscher Länge, bzw. Parsen fehlerhaft";
        }

        // only acceptable if...
        //  (i)  no errors
        //  (ii) all tokens processed
        if (errorStr
                == null && lex.getToken()
                == TOKEN.END) {
            //log.append("parsing OK");
            return true;
        }

        if (errorStr
                != null) {
            log.append(" -> " + errorStr);
        }

        if (lex.getToken()
                != TOKEN.END) {
            if (lex.getToken() == TOKEN.UNKNOWN) {
                log.append(" -> " + "Unbekanntes Symbol: '" + lex.getUnknownCharacter() + "'");
            } else {
                log.append(" -> " + "Unerwartetes Token: '" + lex.getToken() + "'");
            }
        }

        return false;
    }

    /**
     * return the error-string
     *
     * @return error log
     *
     * @author Andreas Schwenk
     */
    public String getErrorLog() {
        return log.getLogString();
    }

    /**
     * get generated output-vector
     *
     * @return generated output-vector
     */
    public LinkedList<GeneratedOutputVector> getGeneratedOutputVector() {
        return generatedOutputVector;
    }

}
