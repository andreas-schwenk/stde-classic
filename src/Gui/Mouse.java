/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   Gui
 * Class:       Mouse
 * Created:     2011-10-28
 */
package Gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 *  the mouslistener which recognizes mouse movements and clicks and reacts on them
 *
 * @author Jan Montag
 */
public class Mouse implements MouseListener, MouseMotionListener
{
    // *** ATTRIBUTES ***
    private GuiMain guiMain;

    private Point dragStart=null;
    
    // *** METHODS ***

    /**
    * the standard-constructor
    *
    * @author Jan Montag
    */
    public Mouse(GuiMain guiMain)
    {
        this.guiMain = guiMain;
    }

    @Override
    public void mouseClicked(MouseEvent me)
    {
        if(me.getSource() == guiMain.clearLogPanel)
            guiMain.log.setText("");
        
        guiMain.workflow.leftMouseButtonClicked();
    }

    @Override
    public void mousePressed(MouseEvent me) {
        dragStart = me.getPoint(); 
        guiMain.workflow.leftMouseButtonPressed();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        guiMain.workflow.leftMouseButtonReleased();
    }

    @Override
    public void mouseEntered(MouseEvent me) {
       
    }

    @Override
    public void mouseExited(MouseEvent me) {
      
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        guiMain.workflow.mouseDragged(dragStart);
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        guiMain.workflow.mouseMoved();
    }
    
}
