/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Gui
 * Class:       GuiAbout
 * Created:     2012-01-16
 */
package Gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

    /**
    * opens the about-window
    *
    * @author Jan Montag
    */
public class GuiAbout extends JFrame
{
    private GuiMain guiMain;
    private Container c;
    
    protected JButton button;
    
    private class Listener implements ActionListener
    {
        private GuiAbout guiAbout;
    
        public Listener(GuiAbout guiAbout)
        {
            this.guiAbout = guiAbout;
        }
                
        @Override
        public void actionPerformed(ActionEvent ae)
        {
            Object src = ae.getSource();
            if(src == guiAbout.button)
            {
                guiAbout.setVisible(false);
            }
        }
    }

    /**
    * constructor of GuiAbout
    *
    * @param guiMain the guiMain
    * @param img an image
    *
    * @author Jan Montag
    */
    public GuiAbout(GuiMain guiMain, ImageIcon img)
    {
        super("STDE");
        
        this.guiMain = guiMain;
        this.setResizable(false);
        
        c = getContentPane();
        
        BoxLayout bl = new BoxLayout(c, BoxLayout.Y_AXIS);
        setLayout(bl);
        
        JLabel label = new JLabel("State Transition Diagram Editor");
        label.setFont(new Font("Arial", Font.BOLD, 12));
        
        button = new JButton(img);
        button.setRolloverEnabled(true);
        button.setBorder(new EmptyBorder(0,0,0,0));
        button.setFocusPainted(false);
        button.addActionListener(new Listener(this));
        button.setToolTipText("schliessen");

        c.add(button);
        
        pack();
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        setLocation(screenSize.width/2-getSize().width/2, screenSize.height/2-getSize().height/2);
    }
    
}
