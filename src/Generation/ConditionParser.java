/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Generation
 * Class:       ConditionParser
 * Created:     2011-10-28
 */

package Generation;

import Generation.Lexer.TOKEN;
import Graph.SigVar;
import Graph.SigVar.SIGVAR_TYPE;
import Graph.Signal;
import Graph.Variable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Verifies the condition of a transition.
 * Syntax-description (EBNF) can be found in the specification-document.
 * 
 * reference:  "Niklaus Wirth - Compiler Construction"
 * 
 * @author Andreas Schwenk
 */
public class ConditionParser
{
    // *** SUB-CLASSES ***
    /**
     * stores a generated condition in the following languages
     *  - "neutral": syntax like in the editor (also used for SCXML)
     *  - C
     *  - VHDL
     * 
     * @author Andreas Schwenk
     */
    public class GeneratedCondition
    {
        public String neutral="", C="", VHDL="";
    }

    // *** ENUMERATIONS ***

    // *** ATTRIBUTES ***    
    
    // Tokenizer
    private Lexer lex;
    // error-reporting
    private String errorStr;
    
    // all "input"-signals of the graph
    private LinkedList<Signal> inputSignals;
    // all variables of the graph
    private LinkedList<Variable> variables;
    
    // logging (errors and success-messages; shown in the GUI [bottom-left corner])
    private Log log;
    
    // generated condition
    private GeneratedCondition generatedCondition=null;
    
    
    // *** METHODS ***
    /**
     * constructor; input signals and variables (of the graph) are needed
     * to verify that all used identifiers are known
     * 
     * @param inputSignals input-signals of the graph
     * @param variables  variables of the graph
     * 
     * @author Andreas Schwenk
     */
    public ConditionParser(LinkedList<Signal> inputSignals, 
            LinkedList<Variable> variables)
    {
        this.inputSignals = inputSignals;
        this.variables = variables;
    }
    
    /**
     * <EXP> ::= <AND> { “||” <AND> }
     * 
     * @author Andreas Schwenk
     */
    private void parseEXP()
    {
        // <AND>
        parseAND();
        // { “||” <AND> }
        while(lex.getToken() == TOKEN.OR)
        {
            generatedCondition.neutral += "or ";
            generatedCondition.C += " || ";
            generatedCondition.VHDL += " or ";
            lex.getNextToken();
            parseAND();
        }
    }
    
    /**
     * <AND> ::= <COMPARE> { “&&” <COMPARE> }
     * 
     * @author Andreas Schwenk
     */
    private void parseAND()
    {
        // <COMPARE>
        parseCOMPARE();
        // { "&&" <COMPARE> }
        while(lex.getToken() == TOKEN.AND)
        {
            generatedCondition.neutral += "and ";
            generatedCondition.C += " && ";
            generatedCondition.VHDL += " and ";
            lex.getNextToken();
            parseCOMPARE();
        }
    }
    
    /**
     * <COMPARE> ::= <UNARY> { “==” <UNARY> } | <UNARY> { “<>” <UNARY> }
     *             | <UNARY> { “<=” <UNARY> } | <UNARY> { “>=” <UNARY> }
     *             | <UNARY> { “<” <UNARY> } | <UNARY> { “>” <UNARY> }
     * 
     * @author Andreas Schwenk
     */
    private void parseCOMPARE()
    {
        // <UNARY>
        SigVar lhs_Type = parseUNARY(null); // type of lhs (:= left hand side)
        // { “==” <UNARY> } | { “<>” <UNARY> }
        while(lex.getToken() == TOKEN.EQUALS || lex.getToken() == TOKEN.UNEQUAL
           || lex.getToken() == TOKEN.LESS || lex.getToken() == TOKEN.LESS_EQUAL
           || lex.getToken() == TOKEN.GREATER || lex.getToken() == TOKEN.GREATER_EQUAL)
        {
            if(lex.getToken() == TOKEN.EQUALS)
            {
                generatedCondition.neutral += "equal ";
                generatedCondition.C += "==";
                generatedCondition.VHDL += "=";
            }
            else if(lex.getToken() == TOKEN.UNEQUAL)
            {
                generatedCondition.neutral += "not_equal ";
                generatedCondition.C += "!=";
                generatedCondition.VHDL += "/=";
            }
            else if(lex.getToken() == TOKEN.LESS)
            {
                if(lhs_Type.getType() == SIGVAR_TYPE.BIT)
                    errorStr = "'<' nicht auf Typ 'BIT' anwendbar";
                generatedCondition.neutral += "less_than ";
                generatedCondition.C += "<";
                generatedCondition.VHDL += "<";
            }
            else if(lex.getToken() == TOKEN.LESS_EQUAL)
            {
                if(lhs_Type.getType() == SIGVAR_TYPE.BIT)
                    errorStr = "'<=' nicht auf Typ 'BIT' anwendbar";
                generatedCondition.neutral += "less_equal ";
                generatedCondition.C += "<=";
                generatedCondition.VHDL += "<=";
            }
            else if(lex.getToken() == TOKEN.GREATER)
            {
                if(lhs_Type.getType() == SIGVAR_TYPE.BIT)
                    errorStr = "'>' nicht auf Typ 'BIT' anwendbar";
                generatedCondition.neutral += "greater_than ";
                generatedCondition.C += ">";
                generatedCondition.VHDL += ">";
            }
            else if(lex.getToken() == TOKEN.GREATER_EQUAL)
            {
                if(lhs_Type.getType() == SIGVAR_TYPE.BIT)
                    errorStr = "'>=' nicht auf Typ 'BIT' anwendbar";
                generatedCondition.neutral += "greater_equal ";
                generatedCondition.C += ">=";
                generatedCondition.VHDL += ">=";
            }
            lex.getNextToken();
            parseUNARY(lhs_Type);
        }
    }
    
    /**
     * <UNARY> ::= [“!”] “(“ <EXP> “)” | [“!”] <SIGNAL> | [“!”] <VARIABLE> | <NUMBER>
     * 
     * Rev. 1.2 Fehlerbeseitigung in Codeerzeugung VHDL 
     * 
     * 
     * @author Andreas Schwenk, Georg Hartung (R1.2)
     * 
     */
    private SigVar parseUNARY(SigVar typeIn)
    {
        SigVar typeOut=null;
        
        boolean not=false;
        // [“!”]
        if(lex.getToken() == TOKEN.NOT)
        {
            not = true;
            lex.getNextToken();
        }
        // “(“ <EXP> “)”
        if(lex.getToken() == TOKEN.LPARENTH)
        {
            if(not)
            {
                generatedCondition.neutral += "not ";
                generatedCondition.C += "!";
                generatedCondition.VHDL += "not ";
            }
            generatedCondition.neutral += "( ";
            generatedCondition.C += " (";
            generatedCondition.VHDL += " (";
            lex.getNextToken();
            // <EXP>
            parseEXP();
            // ")"
            if(lex.getToken() == TOKEN.RPARENTH)
            {
                generatedCondition.neutral += ") ";
                generatedCondition.C += ") ";
                generatedCondition.VHDL += ") ";
                lex.getNextToken();
            }
            // error-handling
            else
            {
                if(lex.getToken() == TOKEN.UNKNOWN)
                    errorStr = "unbekanntes Symbol '"+lex.getUnknownCharacter()+"'";
                else
                    errorStr = ") fehlt";
            }
        }
        // <SIGNAL> | <VARIABLE>
        else if(lex.getToken() == TOKEN.IDENTIFIER)
        {
            String identifier = lex.getIdentifier();
            
            lex.getNextToken();
            
            boolean isSignal=false;
            boolean isVariable=false;
            
            // IDENTIFIER == input-signal?
            Signal s=null;
            // search identifier in input-signal-list
            Iterator it=inputSignals.iterator();
            while(it.hasNext())
            {
                s = (Signal)it.next();
                if(s.getName().equals(identifier))
                {
                    isSignal = true;
                    typeOut = s;
                    break;
                }
            }
            // IDENTIFIER == variable?
            Variable v=null;
            // search identifier in variable-list
            it=variables.iterator();
            while(it.hasNext())
            {
                v = (Variable)it.next();
                if(v.getName().equals(identifier))
                {
                    isVariable = true;
                    typeOut = v;
                    break;
                }
            }
            
            // error-handling
            if(isSignal || isVariable)
            {
                if(not)
                {
                    // special handling for VHDL:
                    //  "!Signal" or "!Variable" is translated to => "(Signal='0')" or "(Variable='0')"
                    //  note: this is only done in case of bit; otherwise: behavior not yet defined
                    if(isSignal && s.getType()==SIGVAR_TYPE.BIT)
                        generatedCondition.VHDL += "(" + s.getName().toUpperCase()+"='0')";
                    else if(isVariable && v.getType()==SIGVAR_TYPE.BIT)
                        generatedCondition.VHDL += "(" + v.getName().toUpperCase()+"='0')";
                    else
                        generatedCondition.VHDL += "not " + identifier.toUpperCase();
                    // neutral, C:
                    generatedCondition.neutral += "not " + identifier +" ";
                    if (isSignal) {
                        generatedCondition.C += "!(inV->" + identifier +")";
                    }
                    else {
                        generatedCondition.C += "!" + identifier;
                    }
                }
                else
                {
                    // special handling for VHDL:
                    //  "Signal" or "Variable" is translated to => "Signal='1'" or "Variable='1'"
                    //  note: this is only done in case of bit; otherwise: behavior not yet defined
                    if(isSignal && s.getType()==SIGVAR_TYPE.BIT
                           /* && lex.getToken() != TOKEN.EQUALS && lex.getToken() != TOKEN.UNEQUAL*/)
                        generatedCondition.VHDL += "(" + s.getName().toUpperCase()+"='1')";
                    else if(isVariable && v.getType()==SIGVAR_TYPE.BIT
                            /*&& lex.getToken() != TOKEN.EQUALS && lex.getToken() != TOKEN.UNEQUAL*/)
                        generatedCondition.VHDL += "(" + v.getName().toUpperCase()+"='1')";
                    else
                        generatedCondition.VHDL += identifier.toUpperCase();
                    // neutral, C
                    generatedCondition.neutral += identifier+" ";
                    if (isSignal) {
                        generatedCondition.C += "(inV->" + identifier +")";
                    }
                    else {
                        generatedCondition.C += identifier;
                    }
                }
            }   
            else
            {
                errorStr = "Symbol/Variable '"+identifier+"' unbekannt." ;
            }
        }
        // <NUMBER>
        else if(lex.getToken() == TOKEN.NUMBER)
        {
            generatedCondition.neutral += lex.getNumber()+" ";
            generatedCondition.C += lex.getNumber();
            // VHDL: number has to be converted according to type
            if(typeIn != null)
            {
                switch(typeIn.getType())
                {
                    case BIT:
                        generatedCondition.VHDL += "'"+lex.getNumber()+"'";
                        break;
                    case BIT_N:
                        generatedCondition.VHDL += "\"" + String.format("%"+typeIn.getBitLength()+"s", 
                                Integer.toBinaryString(lex.getNumber())).replace(" ", "0")  + "\"";
                        break;
                    case SIGNED:
                        generatedCondition.VHDL += lex.getNumber();
                        break;
                    case UNSIGNED:
                        generatedCondition.VHDL += lex.getNumber();
                        break;
                }
            }
            else
            {
                generatedCondition.VHDL += lex.getNumber();
            }
            
            lex.getNextToken();
        }
        else if(lex.getToken() == TOKEN.INVALID_NUMBER)
        {
            errorStr = "ungültiges Format für eine Zahl";
        }
        // unknown token
        else
        {
            errorStr = "Syntaxfehler!";
        }
        
        return typeOut;
    }
    
    /**
     * parses a transition-condition (string)
     * 
     * @param str condition-string
     * @return success
     * 
     * @author Andreas Schwenk
     */
    public boolean parseCondition(String str)
    {
        this.log = new Log();

        // init generated condition
        generatedCondition = new GeneratedCondition();

        // create a new instance for tokenizing
        lex = new Lexer();

        // reset errors
        errorStr = null;
        // set string to tokenizer (lexer)
        lex.setString((str+'\0').toCharArray());
        
        // parse expression (condition)
        parseEXP();
        
        // only acceptable if...
        //  (i)  no errors
        //  (ii) all tokens processed
        if(errorStr == null && lex.getToken()==TOKEN.END)
        {
            //log.append("parsing OK");
            return true;
        }

        if(errorStr != null)
            log.append(" -> " + errorStr);
        
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
     * return the error-string
     * 
     * @return error log
     * 
     * @author Andreas Schwenk
     */
    public String getErrorLog() 
    {
        return log.getLogString();
    }

    /**
     * get generated condition
     * 
     * @return generated condition
     */
    public GeneratedCondition getGeneratedCondition() {
        return generatedCondition;
    }
}
