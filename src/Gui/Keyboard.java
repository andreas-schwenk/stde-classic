/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   Gui
 * Class:       Keyboard
 * Created:     2011-10-28
 */
package Gui;

import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.event.KeyEvent;

/**
* recognizes global keyboard events
*
* @author Jan Montag
    */
public class Keyboard implements KeyEventDispatcher
{
    // *** ATTRIBUTES ***
    private GuiMain guiMain;

    // *** METHODS ***

    /**
    * the standard-constructor
    *
    * @author Jan Montag
    */
    public Keyboard(GuiMain guiMain)
    {
        this.guiMain = guiMain;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e)
    {
        boolean discard = guiMain.graphicsPanel.getMousePosition() != null;
        
        if(e.getID() == KeyEvent.KEY_TYPED)
        {            
            switch(e.getKeyCode())
            {
            }
        }

        if(e.getID() == KeyEvent.KEY_PRESSED)
        {
            Point viewPos = guiMain.graphicsPanelScrollPane.getViewport().getViewPosition();
            Point moveVec = new Point();
            
            switch(e.getKeyCode())
            {
                case KeyEvent.VK_SHIFT:
                    guiMain.workflow.shiftKeyHold(true);
                    break;
                case KeyEvent.VK_LEFT:
                    moveVec.x -= 16;
                    viewPos.x -= 20;
                    break;
                case KeyEvent.VK_RIGHT:
                    moveVec.x += 16;
                    viewPos.x += 20;
                    break;
                case KeyEvent.VK_UP:
                    moveVec.y -= 16;
                    viewPos.y -= 20;
                    break;
                case KeyEvent.VK_DOWN:
                    moveVec.y += 16;
                    viewPos.y += 20;
                    break;
            }
            
            if(discard)
            {
                int maxWidth = guiMain.graphicsPanelScrollPane.getViewport().getViewSize().width
                        - guiMain.graphicsPanelScrollPane.getViewport().getSize().width;
                int maxHeight = guiMain.graphicsPanelScrollPane.getViewport().getViewSize().height
                        - guiMain.graphicsPanelScrollPane.getViewport().getSize().height;
                
                if(guiMain.getWorkflow().getGraph().getSelectedComponents().size() == 0 && e.getKeyCode() != KeyEvent.VK_ESCAPE)
                {
                    if(viewPos.x < 0)
                        viewPos.x = 0;
                    if(viewPos.y < 0)
                        viewPos.y = 0;
                    if(viewPos.x > maxWidth)
                        viewPos.x = maxWidth;
                    if(viewPos.y > maxHeight)
                        viewPos.y = maxHeight;                
                    guiMain.graphicsPanelScrollPane.getViewport().setViewPosition(viewPos);
                }
                else
                {
                    guiMain.workflow.keyEvent(e);
                }
            }
        }

        if(e.getID() == KeyEvent.KEY_RELEASED)
        {
            switch(e.getKeyCode())
            {
                case KeyEvent.VK_SHIFT:
                    guiMain.workflow.shiftKeyHold(false);
                    break;
                case KeyEvent.VK_ALT:
                    break;
                case KeyEvent.VK_CONTROL:
                    break;
            }
        }
        return discard;
    }
    
}
