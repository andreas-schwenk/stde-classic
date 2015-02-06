/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Generation
 * Class:       VariableAssignmentParser
 * Created:     2011-11-17
*
*  Revision 3   2015-02-03 : Assignment erweitert G. Hartung
 */

package Generation;

import Generation.Lexer.TOKEN;
import Graph.SigVar;
import Graph.Signal;
import Graph.Variable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Verifies the variable-assignment of a state (if any)
 * Syntax-description (EBNF) can be found in the specification-document.
 * 
 * reference:  "Niklaus Wirth - Compiler Construction"
 *
 * @author Andreas Schwenk
 */
public class VariableAssignmentParser
{
    // *** SUB-CLASSES ***
    /**
     * stores left/right-hand side of a given expression
     * 
     * @author Andreas Schwenk
     */
    public class GeneratedVarAssignment
    {
        public String lhs; // left-hand side
        public String rhsNeutral, rhsC, rhsVHDL; // right-hand side
    }
    
    // *** ENUMERATIONS ***

    // *** ATTRIBUTES ***    
    
    // Tokenizer
    private Lexer lex;
    // error-reporting
    private String errorStr;
    
    // all variables of the graph
    private LinkedList<Variable> variables;
    
    // all input-signals of the graph
    private LinkedList<Signal> inputSignals;
    
    // logging (errors and success-messages; shown in the GUI [bottom-left corner])
    private Log log;
    
    // generated variable Assigments
    private LinkedList<GeneratedVarAssignment> generatedVariableAssignments=null;

    
    // *** METHODS ***
    
    /**
     * constructor; variables (of the graph) are needed
     * to verify that all used identifiers are known
     * 
     * @param inputSignals input-signals of the graph
     * @param variables  variables of the graph
     * 
     * @author Andreas Schwenk
     */
    public VariableAssignmentParser(LinkedList<Signal> inputSignals,
            LinkedList<Variable> variables)
    {
        this.variables = variables;
        this.inputSignals = inputSignals;
    }
   
    /**
     * check, if the given identifier is a variable; if true then return it
     * 
     * @param identifier
     * @return variable
     * 
     * @author Andreas Schwenk
     */
    private Variable getVariable(String identifier)
    {
        Variable v;
        // search identifier in variable-list
        Iterator<Variable> it=variables.iterator();
        while(it.hasNext())
        {
            v = it.next();
            if(v.getName().equals(identifier))
            {
                return v;
            }
        }
        // not a variable
        return null;
    }

    /**
     * check, if the given identifier is an input-signal; if true then return it
     * 
     * @param identifier
     * @return input-signal
     * 
     * @author Andreas Schwenk
     */
    private Signal getSignal(String identifier)
    {
        Signal s;
        // search identifier in variable-list
        Iterator<Signal> it=inputSignals.iterator();
        while(it.hasNext())
        {
            s = it.next();
            if(s.getName().equals(identifier))
            {
                return s;
            }
        }
        // not a variable
        return null;
    }
    
    /**
     * <ASSIGNLIST> ::= <ASSIGNMENT> { ";" <ASSIGNMENT> }
     */
    private void parseASSIGNMENTLIST()
    {
        GeneratedVarAssignment genVarAssign = new GeneratedVarAssignment();
        generatedVariableAssignments.add(genVarAssign);
        // <ASSIGNEMNT>
        parseASSIGNMENT(genVarAssign);
        // { “;” <ASSIGNMENT> }
        while(lex.getToken() == TOKEN.SEMICOLON)
        {
            lex.getNextToken();
            genVarAssign = new GeneratedVarAssignment();
            generatedVariableAssignments.add(genVarAssign);
            parseASSIGNMENT(genVarAssign);
        }
    }
   
    /**
     * <ASSIGNMENT> ::=  "shl" "(" <VARIABLE> ")"
     *                 | "shr" "(" <VARIABLE> ")"
     *                 | <VARIABLE> "=" <INPUT-SIGNAL>   [note: types must be equal]
     *                 | <VARIABLE> "=" <VARIABLE> ("+"|"-") <NUMBER>
     *                 | <VARIABLE> "=" <VARIABLE> ("+"|"-") <VARIABLE>    // Erweiterung Rev. 1.2
     *                 | <VARIABLE> "=" <NUMBER>
     *                 | <VARIABLE> "++" 
     *                 | <VARIABLE> "--"
     */
    private void parseASSIGNMENT(GeneratedVarAssignment genVarAssign)
    {
        if(lex.getToken() == TOKEN.IDENTIFIER || 
                lex.getToken() == TOKEN.SHIFT_LEFT || lex.getToken() == TOKEN.SHIFT_RIGHT)
        {
            String identifier = lex.getIdentifier();
            
            // "shl" "(" <IDENTIFIER> ")"
            if(lex.getToken() == TOKEN.SHIFT_LEFT)
            {
                lex.getNextToken();
                if(lex.getToken() == TOKEN.LPARENTH)
                {
                    lex.getNextToken();
                    if(lex.getToken()==TOKEN.IDENTIFIER)
                    {
                        identifier = lex.getIdentifier();
                        Variable v;
                        if((v=getVariable(identifier)) != null)
                        {
                            lex.getNextToken();
                            // left-hand side
                            genVarAssign.lhs = identifier;
                            // right-hand side
                              // neutral, C:
                            genVarAssign.rhsNeutral = "shl(" + v.getName() + ")";
                            genVarAssign.rhsC = v.getName() + " << 1";
                              // VHDL:
                            if(v.getType() == SigVar.SIGVAR_TYPE.UNSIGNED)
                                genVarAssign.rhsVHDL = "to_integer(to_unsigned("
                                        +v.getName().toUpperCase()+","+v.getBitLength()+") sll 1)";
                            else if(v.getType() == SigVar.SIGVAR_TYPE.SIGNED)
                                genVarAssign.rhsVHDL = "to_integer(to_signed("
                                        +v.getName().toUpperCase()+","+v.getBitLength()+") sll 1)";
                            else if(v.getType() == SigVar.SIGVAR_TYPE.BIT_N)
                                genVarAssign.rhsVHDL = v.getName().toUpperCase()
                                        + "("+(v.getBitLength()-2)+" downto 0) & '0'";
                            else // BIT
                                errorStr = "Shift beim Typ 'BIT' nicht möglich";
                            
                            if(lex.getToken() == TOKEN.RPARENTH)
                            {
                                lex.getNextToken();
                                return;
                            }
                            else
                            {
                                errorStr = "')' fehlt";
                            }
                        }
                        else
                        {
                            errorStr = "nach '(' wird eine Variable erwartet";
                        }
                    }
                    else
                    {
                        errorStr = "nach '(' wird eine Variable erwartet";
                    }
                }
                else
                {
                    errorStr = "nach 'SHL' wird '(' erwartet";
                }
            }
            // "shr" "(" <IDENTIFIER> ")"
            else if(lex.getToken() == TOKEN.SHIFT_RIGHT)
            {
                lex.getNextToken();
                if(lex.getToken() == TOKEN.LPARENTH)
                {
                    lex.getNextToken();
                    if(lex.getToken()==TOKEN.IDENTIFIER)
                    {
                        identifier = lex.getIdentifier();
                        Variable v;
                        if((v=getVariable(identifier)) != null)
                        {
                            lex.getNextToken();
                            // left-hand side
                            genVarAssign.lhs = identifier;
                            // right-hand side
                              // neutral, C:
                            genVarAssign.rhsNeutral = "shr(" + v.getName() + ")";
                            genVarAssign.rhsC = v.getName() + " >> 1";
                              // VHDL:
                            if(v.getType() == SigVar.SIGVAR_TYPE.UNSIGNED)
                                genVarAssign.rhsVHDL = "to_integer(to_unsigned("
                                        +v.getName().toUpperCase()+","+v.getBitLength()+") srl 1)";
                            else if(v.getType() == SigVar.SIGVAR_TYPE.SIGNED)
                                genVarAssign.rhsVHDL = "to_integer(to_signed("
                                        +v.getName().toUpperCase()+","+v.getBitLength()+") sra 1)";
                            else if(v.getType() == SigVar.SIGVAR_TYPE.BIT_N)
                                genVarAssign.rhsVHDL = "'0' & " + v.getName().toUpperCase() 
                                        + "("+(v.getBitLength()-1)+" downto 1)";
                            else // BIT
                                errorStr = "Shift beim Typ 'BIT' nicht möglich";
                            
                            if(lex.getToken() == TOKEN.RPARENTH)
                            {
                                lex.getNextToken();
                                return;
                            }
                            else
                            {
                                errorStr = "')' fehlt";
                            }
                        }
                        else
                        {
                            errorStr = "nach '(' wird eine Variable erwartet";
                        }
                    }
                    else
                    {
                        errorStr = "nach '(' wird eine Variable erwartet";
                    }
                }
                else
                {
                    errorStr = "nach 'SHR' wird '(' erwartet";
                }
            }
            //   <VARIABLE> "=" <INPUT-SIGNAL>   [note: types must be equal]
            // | <VARIABLE> "=" <VARIABLE> ("+"|"-") <NUMBER>
            // | <VARIABLE> "=" <VARIABLE> ("+"|"-") <VARIABLE>
            // | <VARIABLE> "=" <NUMBER>
            // | <VARIABLE> "++" | <VARIABLE> "--"
            else
            {
                // IDENTIFIER == variable?
                Variable varLhs, varRhs, varRhs2;         // Erweiterung Rev. 3
                if((varLhs=getVariable(identifier)) != null)
                {
                    lex.getNextToken();
                    genVarAssign.lhs = identifier;
                    // '='
                    if(lex.getToken() == TOKEN.ASSIGNMENT)
                    {
                        lex.getNextToken();
                        // <IDENTIFIER> == input-signal?
                        Signal sigRhs;
                        if(lex.getToken() == TOKEN.IDENTIFIER && (sigRhs=getSignal(lex.getIdentifier()))!=null)
                        {
                            identifier = lex.getIdentifier();
                            lex.getNextToken();
                            if(varLhs.getType() == sigRhs.getType() && varLhs.getBitLength() == sigRhs.getBitLength())
                            {
                                genVarAssign.rhsNeutral = identifier;
                                genVarAssign.rhsC = "inV->"+identifier;
                                genVarAssign.rhsVHDL = identifier.toUpperCase();
                            }
                            else
                            {
                                errorStr = "Typ oder Bitlänge von Variable und Eingangssignal ungleich!";
                            }
                        }
                        // <IDENTIFIER> == variable?
                        else if(lex.getToken() == TOKEN.IDENTIFIER 
                                && (varRhs=getVariable(lex.getIdentifier()))!=null)
                        {
                            identifier = lex.getIdentifier();
                            lex.getNextToken();
                            genVarAssign.rhsNeutral = identifier;
                            genVarAssign.rhsC = identifier;
                            genVarAssign.rhsVHDL = identifier.toUpperCase();
                            // '+' | '-'
                            if(lex.getToken() == TOKEN.PLUS || lex.getToken() == TOKEN.MINUS)
                            {
                                if(lex.getToken() == TOKEN.PLUS)
                                {
                                    genVarAssign.rhsNeutral += " + ";
                                    genVarAssign.rhsC += " + ";
                                    genVarAssign.rhsVHDL += " + ";
                                }
                                else
                                {
                                    genVarAssign.rhsNeutral += " - ";
                                    genVarAssign.rhsC += " - ";
                                    genVarAssign.rhsVHDL += " - ";
                                }
                                lex.getNextToken();
                                // <NUMBER>
                                if(lex.getToken() == TOKEN.NUMBER)
                                {
                                    genVarAssign.rhsNeutral += lex.getNumber();
                                    genVarAssign.rhsC += lex.getNumber();
                                    switch(varRhs.getType())
                                    {
                                        case BIT_N:
                                            genVarAssign.rhsVHDL += "\"" + 
                                                    String.format("%"+varLhs.getBitLength()+"s", 
                                                    Integer.toBinaryString(lex.getNumber())).replace(" ", "0") 
                                                    + "\"";
                                            break;
                                        case SIGNED:
                                        case UNSIGNED:
                                            genVarAssign.rhsVHDL += lex.getNumber();
                                            break;
                                        default:
                                            errorStr = "'+'|'-' beim Typ BIT nicht erlaubt";
                                    }
                                    
                                    lex.getNextToken();
                                    return;
                                }
                                // Erweitertung Rev.3: Variable auf rechter Seite erlaubt
                                else if (lex.getToken() == TOKEN.IDENTIFIER) {
                                    if ((varRhs2=getVariable(lex.getIdentifier()))!= null) {
                                        if (varRhs.getType() == varRhs2.getType()) {
                                            genVarAssign.rhsVHDL += lex.getIdentifier();
                                            genVarAssign.rhsNeutral += lex.getIdentifier();
                                            genVarAssign.rhsC += lex.getIdentifier();
                                            lex.getNextToken();
                                        }
                                        else {
                                            errorStr = "Variablenausdruck nur mit Variablen gleichen Typs moeglich";
                                        }
                                    }
                                    else if ((sigRhs = getSignal(lex.getIdentifier())) != null) {
                                        if (sigRhs.getType() == varRhs.getType()) {
                                            genVarAssign.rhsVHDL += lex.getIdentifier();
                                            genVarAssign.rhsNeutral += lex.getIdentifier();
                                            genVarAssign.rhsC += lex.getIdentifier();
                                            lex.getNextToken();
                                        } else {
                                            errorStr = "Variablenausdruck nur mit Variable/Signal gleichen Typs moeglich";
                                        }
                                    } else {
                                        errorStr = "2. Variable in Variablenausdruck nicht definiert";
                                    }
                                        return;
                                }
                                else
                                {
                                        errorStr = "nach'+'|'-' Konstante erwartet";
                                }
                            }
                            else
                            {
                                errorStr = "nach Variable '+' oder '-' erwartet";
                            }
                        }
                        // <NUMBER?>
                        else if(lex.getToken() == TOKEN.NUMBER)
                        {
                            genVarAssign.rhsNeutral = "" + lex.getNumber();
                            genVarAssign.rhsC = "" + lex.getNumber();
                            switch(varLhs.getType())
                            {
                                case BIT_N:
                                    genVarAssign.rhsVHDL = "\"" + String.format("%"+varLhs.getBitLength()+"s", 
                                                    Integer.toBinaryString(lex.getNumber())).replace(" ", "0")  + "\"";
                                    break;
                                case SIGNED:
                                    genVarAssign.rhsVHDL = "to_signed(" + lex.getNumber()+", "+ varLhs.getBitLength() + ")";
                                    break;
                                case UNSIGNED:
                                    genVarAssign.rhsVHDL = "to_unsigned(" + lex.getNumber()+", "+ varLhs.getBitLength() + ")";
                                    break;
                                default: // "BIT"
                                    errorStr = "'+'|'-' beim Typ BIT nicht erlaubt";
                            }
                            lex.getNextToken();
                            return;
                        }
                        else
                        {
                            errorStr = "Rechter Teil der Gleichung (nach '=') ist ungültig";
                        }
                    }
                    // '++'
                    else if(lex.getToken() == TOKEN.INCREMENT)
                    {
                        genVarAssign.rhsNeutral = genVarAssign.lhs + " + 1";
                        genVarAssign.rhsC = genVarAssign.lhs + " + 1";
                        switch(varLhs.getType())
                        {
                            case BIT_N:
                                genVarAssign.rhsVHDL = genVarAssign.lhs + " + \"1\"";
                                break;
                            case SIGNED:
                            case UNSIGNED:
                                genVarAssign.rhsVHDL = genVarAssign.lhs + " + 1";
                                break;
                            default: // "BIT"
                                errorStr = "Inkrementierten ('++') beim Typ BIT nicht erlaubt";
                        }
                        lex.getNextToken();
                    }
                    // '--'
                    else if(lex.getToken() == TOKEN.DECREMENT)
                    {
                        genVarAssign.rhsNeutral = genVarAssign.lhs + " - 1";
                        genVarAssign.rhsC = genVarAssign.lhs + " - 1";
                        switch(varLhs.getType())
                        {
                            case BIT_N:
                                genVarAssign.rhsVHDL = genVarAssign.lhs + " - \"1\"";
                                break;
                            case SIGNED:
                            case UNSIGNED:
                                genVarAssign.rhsVHDL = genVarAssign.lhs + " - 1";
                                break;
                            default: // "BIT"
                                errorStr = "Dekrementierten ('--') beim Typ BIT nicht erlaubt";
                        }
                        lex.getNextToken();
                    }
                    else
                    {
                        errorStr = "Zuweisung '=' oder Inkrement / Dekrement '++' / '--' erwartet";
                    }
                }
                else
                {
                    errorStr = "unbekannter Bezeichener: '"+identifier+"'";
                }
            }
        }
        // unknown token
        else
        {
            errorStr = "Syntaxfehler!";
        }
    }
    
    /**
     * parses a variable-assignment (string)
     * 
     * @param str variable assignment
     * @return success
     * 
     * @author Andreas Schwenk
     */
    public boolean parseVariableAssignment(String str)
    {
        this.log = new Log();
        
        // init generated variable-assignments
        generatedVariableAssignments = new LinkedList<GeneratedVarAssignment>();

        // create a new instance for tokenizing
        lex = new Lexer();
        
        // reset errors
        errorStr = null;
        
        // set string to tokenizer (lexer)
        lex.setString((str+'\0').toCharArray());
        
        // parse expression (variable-assignment)
        parseASSIGNMENTLIST();
        
        // only acceptable if...
        //  (i)  no errors
        //  (ii) all tokens processed
        if(errorStr == null && lex.getToken()==TOKEN.END)
        {
            //log.append("parsing OK");
            return true;
        }

        // append new error
        if(errorStr != null)
            log.append(" -> " + errorStr);
        
        // further logging
        if(lex.getToken()!=TOKEN.END)
        {
            if(lex.getToken() == TOKEN.UNKNOWN)
                log.append(" -> " + "Unbekanntes Symbol: '" + lex.getUnknownCharacter() + "'");
            else
                log.append(" -> " + "Unerwartetes Token: '" + lex.getToken() + "'");
        }
        
        return false;
    }
    
    /**
     * returns the error-string
     * 
     * @return error log
     * 
     * @author Andreas Schwenk
     */
/* TODO: change in class diagram (renamed) */
    public String getErrorLog() 
    {
        return log.getLogString();
    }

    /**
     * gets generated variable assignments
     * 
     * @return generated variable assignments
     * 
     * @author Andreas Schwenk
     */
    public LinkedList<GeneratedVarAssignment> getGeneratedVariableAssignments() 
    {
        return generatedVariableAssignments;
    }
    
    
}
