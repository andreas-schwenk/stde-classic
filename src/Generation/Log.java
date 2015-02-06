/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Generation
 * Class:       Log
 * Created:     2011-11-01
 */

package Generation;

/**
 * Logs the overall generation-process; the user gets an overview about error.
 * @author Andreas Schwenk
 */
public class Log
{
    // *** ATTRIBUTES ***
    private String logString;
    
    // *** METHODS ***
    /**
     * @author Andreas Schwenk
     */
    public Log()
    {
        logString = "";
    }
    
    /**
     * writes a new line into the log-string
     * @param msg message to be logged
     * @author Andreas Schwenk
     */
    public void append(String msg)
    {
        if(logString.length() == 0)
            logString = msg;
        else
            logString = logString + "\n" + msg;
    }

    /**
     * returns the current log-string
     * @author Andreas Schwenk
     */
    public String getLogString() {
        return logString;
    }

    /**
     * replaces the current log-string by the given one
     * @param logString new log string
     * @author Andreas Schwenk
     */
    public void setLogString(String logString) {
        this.logString = logString;
    }
        
}
