/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   Gui
 * Class:       CustomTextField
 * Created:     2011-11-07
 */

package Gui;

import Graph.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implements a manually text-field. Necessary to allow editing in the graph.
 * In this case a Java "JTextField" does not perform the required behavior.
 * 
 * @author Andreas Schwenk
 */
public class CustomTextField
{   
    // *** ENUMERATIONS **

    // The symbol-list restricts the keyboard to a certain set of characters.
    // This shall reduce errors in the generation-process
    public enum SYMBOL_LIST { NAME, CONDITION, OUTPUT_VECTOR, VARIABLE_ASSIGNMENT };

    // *** ATTRIBUTES ***
    
    // current (shown) text
    private String text="";

    // position of the cursor
    private int cursorPos=0;
    
    // active: texted can be entered
    private boolean active=false;
    
    // owner of the text-field: e. g. a state or a transiton etc
    private Component relatedComponent=null;
    
    // symbol-list
    private SYMBOL_LIST symbolList;
    
    // vertical displacement relative to the "relatedComponents" vertical position
    //   y-displacement := relatedComponents.size.y * vertical factor
    private double verticalFactor;
    
    // position of the textfield
    private Point position = new Point();
    
    private boolean underlined = false;
    
    // *** METHODS ***
    
    /**
     * constructor: creates a new CustomTextField
     * 
     * @param relatedComponent owner of this text-field, e. g. a state
     * @param symbolList restriction to a certain amount of characters
     * @param verticalFactor vertical displacement relative to the "relatedComponents" 
     *  vertical position y-displacement := relatedComponents.size.y * vertical factor
     * 
     * @author Andreas Schwenk
     */
    public CustomTextField(Component relatedComponent, SYMBOL_LIST symbolList, 
            double verticalFactor)
    {
        this.relatedComponent = relatedComponent;
        cursorPos = text.length();
        
        this.symbolList = symbolList;
        
        this.verticalFactor = verticalFactor;
    }
    
    /**
     *  constructor: Creates a Transition from a given file (more precisely:
     *  a given data-input-stream).
     * 
     * @param in data-stream of the input-file
     * @param relatedComponent owner of this text-field, e. g. a state
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public CustomTextField(DataInputStream in, Component relatedComponent) throws IOException
    {
        loadFromFile(in);
        this.relatedComponent = relatedComponent;
    }    
    
    /**
     * renders (displays) the text-field
     * 
     * @param g2d graphics-object from GUI.GraphicsPanel
     * @param font font-object
     * 
     * @author Andreas Schwenk
     */
    public void render(Graphics2D g2d, Font font)
    {
        // do not render, if this textfiled does not hava an owner
        if(relatedComponent == null)
            return;
        
        // ** render font
        // font-metrics for font-size-measurement
        FontMetrics metrics = g2d.getFontMetrics(font);
        
        // get font-metrics (divided by two for alignment-purposes)
        Point p = new Point();
        p.x = -metrics.stringWidth(text)/2;
        p.y =  metrics.getHeight()/2;
        // add vertical factor as describen in section "attributes"
        p.y += relatedComponent.getSize().y * verticalFactor;
        
        // calculate the final position
        position.x = p.x + (int)g2d.getTransform().getTranslateX();
        position.y = p.y + (int)g2d.getTransform().getTranslateY();
        
        // underlined?
        if(underlined)
        {
            int lineLength = metrics.stringWidth(text);
            if(lineLength < 5)
                lineLength = 5;
            g2d.setColor(Color.red);
            g2d.fillRect(p.x, p.y+4, lineLength, 2);
            g2d.setColor(Color.black);
        }
        
        // set color to red when active
        if(active)
            g2d.setColor(Color.red);
        
        // draw the text
        g2d.drawString(text, p.x, p.y);
        
        // reset the color
        g2d.setColor(Color.black);
        
        // render cursor
        if(active)
        {
            // calculate cursor-position by substring
            int cursorPosX = metrics.stringWidth(text.substring(0, cursorPos));
            g2d.setColor(Color.red);
            // render cursor
            g2d.drawLine(p.x+cursorPosX, p.y+2, p.x+cursorPosX, p.y-14);
            // reset color
            g2d.setColor(Color.black);
        }
    }
    
    /**
     * A mouse-click will set the text-cursor to the mouse-cursor.
     * Activates the text-field when clicked on the text.
     * Deactivates the text-field when NOT clicked on the text.
     * 
     * @param mousePosition current mouse-position
     * 
     * @author Andreas Schwenk
     */
    public void mouseClick(Point mousePosition)
    {
        // deactiate here (will be activated below, when click matches)
        active = false;
        
        // y-axis of the cursor over text?
        if(mousePosition.y > position.y+4)
            return;
        if(mousePosition.y < position.y - GraphicsPanel.getFontDimensions(text).y)
            return;
        
        // x-axis of the cursor over text?
        if(mousePosition.x < position.x-5)
        {
            cursorPos = 0;
            return;
        }
        if(mousePosition.x > (position.x+GraphicsPanel.getFontDimensions(text).x+5))
        {
            cursorPos = text.length();
            return;
        }
        
        // calculate text-cusorpos by mouse-positon
        //  because the used font is not fixed-sized, getting the width of each
        //  character is not trivial (has to be done sequentially)
        int i;
        for(i=0; i<text.length(); i++)
        {
            if(mousePosition.x <= GraphicsPanel.getFontDimensions(text.substring(0, i)).x + position.x)
                break;
        }
        // set cursor pos
        cursorPos = i;
        
        // set active (code will only be reached when mouse-cursor is over the text
        active = true;
    }
    
    /**
     * implement a mouse-over detection
     * 
     * @param mousePosition current mouse-position
     * @return is mouse over?
     * 
     * @author Andreas Schwenk
     */
    public boolean isMouseOver(Point mousePosition)
    {
        Point fontDim = GraphicsPanel.getFontDimensions(text);
        return( mousePosition.x >= position.x-4 &&
            mousePosition.x <= position.x+4+fontDim.x &&
            mousePosition.y >= position.y-2-fontDim.y &&
            mousePosition.y <= position.y+2);
    }
    
    /**
     * Called in case a key-event occurred. Since the text-field is programmed
     * manually, text-editing has to be done by hand
     * 
     * @param e key-event
     * 
     * @author Andreas Schwenk
     */
    public void keyEvent(KeyEvent e)
    {
        // return, if not active
        if(!active)
            return;
        
        // get key
        int keyCodeChar = e.getKeyChar();
        int keyCode = e.getKeyCode();
       
        switch(keyCode)
        {
            case KeyEvent.VK_LEFT:
                // move cursor to the left
                if(cursorPos > 0)
                    cursorPos --;
                return;
            case KeyEvent.VK_RIGHT:
                // move cursor to the left
                if(cursorPos < text.length())
                    cursorPos ++;
                return;
            case KeyEvent.VK_BACK_SPACE:
                // delete character left to the cursor
                if(cursorPos > 0)
                {
                    text = text.substring(0, cursorPos-1) + text.substring(cursorPos, text.length());
                    cursorPos --;
                }                
                return;
            case KeyEvent.VK_DELETE:
                // delete character right to the cursor
                if(cursorPos < text.length())
                {
                    text = text.substring(0, cursorPos) + text.substring(cursorPos+1, text.length());
                }                
                return;
            case KeyEvent.VK_HOME:
                cursorPos = 0;
                return;
            case KeyEvent.VK_END:
                cursorPos = text.length();
                return;
            case KeyEvent.VK_TAB:
            case KeyEvent.VK_ENTER:
                // deactivate (callin function has to active the "next" text-field)
                active = false;
                return;
        }

        // restrict keys by a symbol-list: this reduces errors in generation-process
        // (i) name
        if(symbolList == SYMBOL_LIST.NAME &&
           (keyCodeChar >= 'A' && keyCodeChar <= 'Z' ||
            keyCodeChar >= 'a' && keyCodeChar <= 'z' ||
            keyCodeChar >= '0' && keyCodeChar <= '9' ||
            keyCodeChar == '_'))
        {
            text = text.substring(0, cursorPos) + (char)keyCodeChar + text.substring(cursorPos, text.length());
            cursorPos ++;
        }
        // (ii) transition-condition
        else if(symbolList == SYMBOL_LIST.CONDITION &&
           (keyCodeChar >= 'A' && keyCodeChar <= 'Z' ||
            keyCodeChar >= 'a' && keyCodeChar <= 'z' ||
            keyCodeChar >= '0' && keyCodeChar <= '9' ||
            keyCodeChar == '_' || keyCodeChar == '|' ||
            keyCodeChar == '(' || keyCodeChar == ')' ||
            keyCodeChar == ' ' || keyCodeChar == '!' ||
            keyCodeChar == '&' || keyCodeChar == '=' ||
            keyCodeChar == '<' || keyCodeChar == '>' ||
            keyCodeChar == '#'))
        {
            text = text.substring(0, cursorPos) + (char)keyCodeChar + text.substring(cursorPos, text.length());
            cursorPos ++;
        }
        // (iii) variable-assignment(s)
        else if(symbolList == SYMBOL_LIST.VARIABLE_ASSIGNMENT &&
           (keyCodeChar >= 'A' && keyCodeChar <= 'Z' ||
            keyCodeChar >= 'a' && keyCodeChar <= 'z' ||
            keyCodeChar >= '0' && keyCodeChar <= '9' ||
            keyCodeChar == '+' || keyCodeChar == '-' ||
            keyCodeChar == ' ' || keyCodeChar == '=' ||
            keyCodeChar == '(' || keyCodeChar == ')' ||
            keyCodeChar == '#' || keyCodeChar == ';' ||
            keyCodeChar == '_'))
        {
            text = text.substring(0, cursorPos) + (char)keyCodeChar + text.substring(cursorPos, text.length());
            cursorPos ++;
        }
        // (iv) output-vector
        else if(symbolList == SYMBOL_LIST.OUTPUT_VECTOR &&
           (keyCodeChar >= '0' && keyCodeChar <= '9' ||
            keyCodeChar >= 'A' && keyCodeChar <= 'Z' ||
            keyCodeChar >= 'a' && keyCodeChar <= 'z' ||
            keyCodeChar == ',' || keyCodeChar == ' ' ||
            keyCodeChar == '(' || keyCodeChar == ')' ||
            keyCodeChar == '#' || keyCodeChar == ':' || 
            keyCodeChar == '_' || keyCodeChar == '-'))
        {
            text = text.substring(0, cursorPos) + (char)keyCodeChar + text.substring(cursorPos, text.length());
            cursorPos ++;
        }
    }
       
    /**
     * gets whether the text-field is active
     * 
     * @return whether the text-field is active
     * 
     * @author Andreas Schwenk
     */
    public boolean getActive() {
        return active;
    }

    /**
     * sets active / not active
     * 
     * @param active new active-status
     * 
     * @author Andreas Schwenk
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * gets the cursor-position
     * 
     * @return current cursor position
     * 
     * @author Andreas Schwenk
     */
    public int getCursorPos() {
        return cursorPos;
    }

    /**
     * sets a new cursor-position
     * 
     * @param cursorPos a new cursor-position
     * 
     * @author Andreas Schwenk
     */
    public void setCursorPos(int cursorPos) {
        this.cursorPos = cursorPos;
    }

    /**
     * get the text
     * 
     * @return current text
     * 
     * @author Andreas Schwenk
     */
    public String getText() {
        return text;
    }

    /**
     * replaces the text by a given one
     * 
     * @param text new text
     * 
     * @author Andreas Schwenk
     */
    public void setText(String text) {
        this.text = text;
        // set cursor right to the end of the string
        cursorPos = text.length();
    }

    /**
     * sets the vertical factor: vertical displacement relative to the 
     *  "relatedComponents" vertical position:
     *  y-displacement := relatedComponents.size.y * vertical factor
     * 
     * @param verticalFactor new vertical factor
     * 
     * @author Andreas Schwenk
     */
    public void setVerticalFactor(double verticalFactor) {
        this.verticalFactor = verticalFactor;
    }
    
    /**
     * load the text-field from a given file (data-output-stream)
     * 
     * @param in data-stream of the input-file
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public final void loadFromFile(DataInputStream in) throws IOException
    {
        text = in.readUTF();
        active = in.readBoolean();
        cursorPos = in.readInt();
        symbolList = SYMBOL_LIST.valueOf(in.readUTF());
        verticalFactor = in.readDouble();
        position.x = in.readInt();
        position.y = in.readInt();
    }
    
    /**
     * saves the text-field to a given file (data-output-stream)
     * 
     * @param out data-stream of the output-file
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
    public void saveToFile(DataOutputStream out) throws IOException
    {
        out.writeUTF(text);
        out.writeBoolean(active);
        out.writeInt(cursorPos);
        out.writeUTF(symbolList.name());
        out.writeDouble(verticalFactor);
        out.writeInt(position.x);
        out.writeInt(position.y);
    }

    /**
     * sets weather the text is underlined
     * 
     * @param underlined is underlined
     */
    public void setUnderlined(boolean underlined) {
        this.underlined = underlined;
    }
    
    
    
}
