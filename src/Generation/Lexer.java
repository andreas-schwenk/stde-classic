/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Generation
 * Class:       Lexer
 * Created:     2011-11-18
 */
package Generation;

/**
 * Acts as tokenizer. An input-string (ASCII) is split into tokens
 * that can be used by a parser.
 * 
 * @author Andreas Schwenk
 */
public class Lexer
{
    // *** ENUMERATIONS ***
    
    // known tokens
    public enum TOKEN 
    { 
        ASSIGNMENT, // "="
        EQUALS, // "=="
        UNEQUAL, // "!="
        GREATER, // ">"
        GREATER_EQUAL, // ">="
        LESS, // "<"
        LESS_EQUAL, // "<="
        AND, // "&&"
        OR, // "||"
        NOT, // "!"
        PLUS, // "+"
        MINUS, // "-"
        INCREMENT, // "++"
        DECREMENT, // "--"
        LPARENTH, // "("
        RPARENTH, // ")"
        COMMA, // ","
        SEMICOLON, // ";"
        COLON, // ":"
        IDENTIFIER, // A..Z | a..z { a..z | A..Z | 0..9 }
        SHIFT_LEFT, // shl
        SHIFT_RIGHT, // shr
        NUMBER, // 0|1{0|1} | 0x0..F{0..F} | #0..9{0..9}
        INVALID_NUMBER, // start with 2..9
        HEX_NUMBER, // "0x" 0..F { 0..F }
        UNKNOWN, // ???
        END // '\0'
    }

    // *** ATTRIBUTES ***
    
    // current (ASCII)-character, last (ASCII)-character
    private char ch, lastCh;
    
    // input-string
    private char[] string;
    // current position of input-string
    private int stringPos;

    // number (sequence of figures)
    private int number;
    // identifier (sequence of alpha-numeric characters)
    private String identifier;
    
    // ASCII-character not element of known tokens
    private char unknownCharacter;
    
    // current token
    private TOKEN token;
       
    // *** METHODS ***
    
    /**
     * constructor; currently empty
     * 
     * @author Andreas Schwenk
     */
    public Lexer()
    {
        /* empty */
    }
    
    /**
     * reads the next (ASCII-)character from the input-string
     * and stores it into "ch" (current character)
     * 
     * @author Andreas Schwenk
     */
    private void readChar()
    {
        // in bounds?
        if(stringPos < string.length)
        {
            ch = string[stringPos];
            stringPos ++;
        }
    }
    
    /**
     * Get the next token from the input-string
     * the result will be stored in the attribute "token".
     * (It is not returned for easier parser-implementation)
     * 
     * @author Andreas Schwenk
     */
    public void getNextToken()
    {
        if(token == TOKEN.UNKNOWN)
            return;
        
        // skip all whitespaces (space, tab, newline)
        if(ch != ' ' && ch != '\t')
            lastCh = ch;
        while(ch==' ' || ch=='\t' || ch=='\n')
            readChar();
        
        // identifier?
        //  starts with a..z | A..Z
        if(ch>='a'&&ch<='z' || ch>='A'&&ch<='Z' || ch=='_')
        {
            token = TOKEN.IDENTIFIER;
            identifier = "";
            // read following charaters: { a..z | A..Z | 0..9 }
            do
            {
                identifier = identifier+ch;
                readChar();
            } while(ch>='a'&&ch<='z' || ch>='A'&&ch<='Z' || ch>='0'&&ch<='9' || ch=='_');
            // shl?
            if(identifier.toLowerCase().equals("shl"))
                token = TOKEN.SHIFT_LEFT;
            // shr?
            if(identifier.toLowerCase().equals("shr"))
                token = TOKEN.SHIFT_RIGHT;
        }        
        // number (e. g.: 00001010, 0xA, #10, #-10)
        else if(ch=='0' || ch=='1' || ch=='#')
        {
            token = TOKEN.NUMBER;
            // binary or hexadecimal
            if(ch=='0' || ch=='1')
            {
                char tmp = ch;
                readChar();
                // hexadecimal
                if(tmp == '0' && ch == 'x')
                {
                    number = 0;
                    readChar();
                    while(ch>='0' && ch<='9' || ch>='A' && ch<='Z' || ch>='a' && ch<='z')
                    {
                        number *= 16;
                        if(ch>='0' && ch<='9')
                            number += ch-'0';
                        if(ch>='A' && ch<='Z')
                            number += ch-'A'+10;
                        if(ch>='a' && ch<='z')
                            number += ch-'a'+10;
                        readChar();
                    }
                }
                // binary
                else
                {
                    number = tmp-'0';
                    while(ch=='0' || ch=='1')
                    {
                        number *= 2;
                        number += ch - '0';
                        readChar();
                    }
                }
            }
            else // decimal
            {
                boolean negative=false;
                readChar();
                if(ch=='-')
                {
                    negative=true;
                    readChar();
                }
                number = 0;
                // read the following characters: { 0..9 }
                do 
                {
                    number *= 10;
                    number += ch-'0';
                    readChar();            
                } while(ch>='0' && ch<='9');
                if(negative)
                    number = -number;
            }
        }
        // invalic number (a number may only start with 0|1|# like described above)
        else if(ch>='2' && ch<='9')
        {
            token = TOKEN.INVALID_NUMBER;
        }
        // other character
        else switch(ch)
        {
            // "&&" - AND
            case '&':
                readChar();
                if(ch=='&')
                {
                    token = TOKEN.AND;
                    readChar();
                }
                else
                {
                    token = TOKEN.UNKNOWN;
                    unknownCharacter = lastCh;
                }
                break;
            // "||" - OR
            case '|':
                readChar();
                if(ch=='|')
                {
                    token = TOKEN.OR;
                    readChar();
                }
                else
                {
                    token = TOKEN.UNKNOWN;
                    unknownCharacter = lastCh;
                }
                break;
            // "=" ASSIGNMENT | "==" - EQUALS
            case '=':
                token = TOKEN.ASSIGNMENT;
                readChar();
                if(ch=='=')
                {
                    token = TOKEN.EQUALS;
                    readChar();
                }
                break;
            // "+" - PLUS | "++" - INCREMENT
            case '+':
                token = TOKEN.PLUS;
                readChar();
                if(ch=='+')
                {
                    token = TOKEN.INCREMENT;
                    readChar();
                }
                break;                
            // "-" - MINUS | "--" - DECREMENT
            case '-':
                token = TOKEN.MINUS;
                readChar();
                if(ch=='-')
                {
                    token = TOKEN.DECREMENT;
                    readChar();
                }
                break;                                
            // "!" - NOT
            case '!':
                readChar();
                if(ch=='=')
                {
                    token = TOKEN.UNEQUAL;
                    readChar();
                }
                else
                {
                    token = TOKEN.NOT;
                }
                break;
            // "(" - left paraenthesis
            case '(':
                readChar();
                token = TOKEN.LPARENTH;
                break;
            // ")" - right parenthesis
            case ')':
                readChar();
                token = TOKEN.RPARENTH;
                break;
            // '\0' - end of string
            case '\0':
                readChar();
                token = TOKEN.END;
                break;
            // ',' - comma
            case ',':
                readChar();
                token = TOKEN.COMMA;
                break;
            // ':' - colon
            case ':':
                readChar();
                token = TOKEN.COLON;
                break;
            // ';' - semicolon
            case ';':
                readChar();
                token = TOKEN.SEMICOLON;
                break;
            // '<': LESS | LESS_EQUAL
            case '<':
                token = TOKEN.LESS;
                readChar();
                if(ch=='=')
                {
                    token = TOKEN.LESS_EQUAL;
                    readChar();
                }
                break;
            // '>': GREATER | GREATER_EQUAL
            case '>':
                token = TOKEN.GREATER;
                readChar();
                if(ch=='=')
                {
                    token = TOKEN.GREATER_EQUAL;
                    readChar();
                }
                break;
            // unknown character
            default:
                token = TOKEN.UNKNOWN;
                unknownCharacter = lastCh;
                break;
        }
    }

    /**
     * 
     * returns the current token
     * 
     * @return current token
     * 
     * @author Andreas Schwenk
     */
    public TOKEN getToken() {
        return token;
    }
    
    /**
     * sets the string that will be lexed (tokenized)
     * 
     * @param str 
     * 
     * @author Andreas Schwenk
     */
    public final void setString(char[] str)
    {
        string = str;
        ch = string[0];
        stringPos = 1;
                
        getNextToken();
    }

    /**
     * returns the last read character that could not be identified (is unknown)
     * 
     * @return unknown character
     *
     * @author Andreas Schwenk
     */
    public char getUnknownCharacter() {
        return unknownCharacter;
    }

    /**
     * returns the last read identifier
     * 
     * @return identifier
     * 
     * @author Andreas Schwenk
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * returns the last read number
     * 
     * @return number
     * 
     * @author Andreas Schwenk
     */
    public int getNumber() {
        return number;
    }

}
