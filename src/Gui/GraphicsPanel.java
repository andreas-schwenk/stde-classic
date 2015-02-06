/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   Gui
 * Class:       GraphicsPanel
 * Created:     2011-10-28
 */
package Gui;

import Graph.Graph;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JPanel;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * The graph will be rendered (displayed) in this graphics-panel.
 * This class also provides functionality to render simple shapes (e. g. arrow-
 * heads)
 * 
 * @author Andreas Schwenk
 */
public class GraphicsPanel extends JPanel
{
    // *** ATTRIBUTES ***

    // graph-association
    private Graph graph;

    // render-target: rendering to an image is faster than directly to the panel
    private static BufferedImage img;
    
    // Strokes ("brushes")
    // (i) small fatness
    public static BasicStroke solidStroke = new BasicStroke(1.0f, 
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    // (ii) fat
    public static BasicStroke fatStroke = new BasicStroke(2.0f, 
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    // (iii) dashed ("dotted")
    private final static float dash[] = {2.0f};
    public static BasicStroke dashedStroke = new BasicStroke(1.0f,
                                          BasicStroke.CAP_BUTT,
                                          BasicStroke.JOIN_MITER,
                                          10.0f, dash, 0.0f);
    // font
    private Font font12 = new Font("Arial", Font.PLAIN, 12);

    // dimension of this panel
    private Point panelSize;
    
// TODO: ADD TO CLASS-DIAGRAM:
    private boolean isRenderSelectionRect = false;
// TODO: ADD TO CLASS-DIAGRAM:
    private Point selectionRectP1=null, selectionRectP2=null;

// TODO: ADD TO CLASS-DIAGRAM:
    private boolean isRenderGrid=true;
        
    // *** METHODS ***
    /**
     * standard-constructor
     * 
     * @author Andreas Schwenk
     */
    public GraphicsPanel()
    {
        setPreferredSize(new Dimension(800, 500));
    }

    /**
     * construct graph-association
     * 
     * @param g instance of a graph
     * 
     * @author Andreas Schwenk
     */
    public void setGraph(Graph g)
    {
        graph = g;
    }
    
    /**
     * renders a grid; the mouse "snaps" on the grid to achieve a better visual
     * effect (better alignment of states)
     * 
     * @author Andreas Schwenk
     */
    public void renderGrid(Graphics2D g2d)
    {
        if(isRenderGrid == false)
            return;
        
        int x, y;
        int width = getPreferredSize().width;
        int height = getPreferredSize().height;

        // reset transform
        g2d.setTransform(new AffineTransform());
        
        // render lines
        g2d.setColor(new Color(240, 240, 240));
        for(y=0; y<height; y+=16)
            g2d.drawLine(0, y, width, y);
        for(x=0; x<width; x+=16)
            g2d.drawLine(x, 0, x, height);

        // render document-outline
        g2d.setColor(new Color(175, 175, 175));
        g2d.drawRect(1, 1, width-1, height-1);
        
        // reset color
        g2d.setColor(Color.black);
    }

    /**
     * gets the standard size in pixels
     * 
     * @return size of the panel
     * 
     * @author Andreas Schwenk
     */
    /*@Override
    public Dimension getPreferredSize()
    {
        return new Dimension(1500,1500);
    }*/
    
    /**
     * Core rendering method which draws the graph.
     * This method is either called by the operation-system, or (additionally)
     * manually to obtain an immediate refresh
     * 
     * @param g graphics-object, provided by the system
     * 
     * @author Andreas Schwenk
     */
    @Override
    public void paint(Graphics g)
    {        
        super.paintComponents(g);
        
        // get current window-dimension
        Dimension dim = this.getSize();
        
        // (re)create rendering-buffer, when user changes the window-size
        if(img==null || img.getWidth()!=dim.width || img.getHeight()!=dim.height)
        {
            img = (BufferedImage)createImage(dim.width, dim.height);
        }
        
        // get (faster) graphics device from buffered image
        Graphics2D g2d = img.createGraphics();
        
        // clear background
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, dim.width, dim.height);
        
        // use antialiasing (reduces "stairs"-effect)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // set color and stroke
        g2d.setPaint(Color.black);
        g2d.setStroke(solidStroke);

        // render grid
        renderGrid(g2d);
        
        // get current mouse-position
        Point mousePos = getMousePosition();
        
        // render graph-type and output-vector-legend as string
        String titleAndLegend = "Titel: '" + graph.getName() + "',  Typ: '";
        if(graph.getGraphType() == Graph.GRAPH_TYPE.MOORE)
            titleAndLegend += "MOORE', "+graph.getLegend();
        else
            titleAndLegend += "MEALY', "+graph.getLegend();
        
        //  (a) render white background
        g2d.setColor(Color.white);
        //g2d.fillRect(2, 2, GraphicsPanel.getFontDimensions(titleAndLegend).x, 20);
        //  (b) render text
        g2d.setColor(Color.black);
        if(graph.getGraphType() == Graph.GRAPH_TYPE.MOORE)
            g2d.drawString(titleAndLegend, 4, 20);
        else
            g2d.drawString(titleAndLegend, 4, 20);
        
        // render graph
        if(graph != null)
        {
            g2d.setFont(font12);
            graph.render(g2d, font12, mousePos);
        }
        
        // reset transformations
        AffineTransform at = new AffineTransform();
        g2d.setTransform(at);
        
        // render rectangle-selection
        if(isRenderSelectionRect && selectionRectP1 != null && selectionRectP2 != null)
        {
            g2d.setStroke(dashedStroke);
            Point p1=selectionRectP1, p2=selectionRectP2;
            g2d.setColor(Color.black);
            g2d.drawLine(p1.x, p1.y, p2.x, p1.y); // top
            g2d.drawLine(p1.x, p2.y, p2.x, p2.y); // bottom
            g2d.drawLine(p1.x, p1.y, p1.x, p2.y); // left
            g2d.drawLine(p2.x, p1.y, p2.x, p2.y); // right
            g2d.setStroke(solidStroke);
        }
        
        //g2d.dispose();
        if(img != null)
            g.drawImage(img, 0, 0, this);
    }
    
    /**
     * saves current graph to file
     * 
     * @param file destination file
     * @return success
     * @throws IOException 
     * 
     * @author Andreas Schwenk
     */
/*TODO: ADD TO CLASS DIAGRAM*/
    public boolean exportImage(File file) throws IOException
    {
        if(img == null)
            return false;
        
        ImageIO.write(img, "png", file);
        
        return true;
    }
    
    /**
     * measures the size of given string (in pixels)
     * 
     * @param string the string to be measured
     * @return metrics
     * 
     * @author Andreas Schwenk
     */
    public static Point getFontDimensions(String string)
    {
        FontMetrics metrics = img.getGraphics().getFontMetrics();        
        return new Point(metrics.stringWidth(string), metrics.getHeight());
    }
    
    /**
     * renders a circle within an arrow-cross
     * 
     * @param g2d graphics-object
     * @param position render-position
     * @param color color
     * 
     * @author Andreas Schwenk
     */
    public static void renderMovePicker(Graphics2D g2d, Point position, Color color)
    {
        // render white background-circle (if drawing before other geometries)
        g2d.setColor(Color.white);
        g2d.fill(new Ellipse2D.Double((double)(position.x-9), (double)(position.y-9), 18.0, 18.0));
        g2d.setColor(color);
        // render visible circle in given color
        g2d.drawOval(position.x-9, position.y-9, 18, 18);

        // construct geometry
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        // arrow heads:
        // (i) top
        xPoints[0] = position.x+0;
        yPoints[0] = position.y-8;
        xPoints[1] = position.x-3;
        yPoints[1] = position.y-4;
        xPoints[2] = position.x+3;
        yPoints[2] = position.y-4;
        g2d.fillPolygon(new Polygon(xPoints, yPoints, 3));
        // (ii) bottom
        xPoints[0] = position.x+0;
        yPoints[0] = position.y+8;
        xPoints[1] = position.x-3;
        yPoints[1] = position.y+4;
        xPoints[2] = position.x+3;
        yPoints[2] = position.y+4;
        g2d.fillPolygon(new Polygon(xPoints, yPoints, 3));
        // (iii) right
        xPoints[0] = position.x+8;
        yPoints[0] = position.y+0;
        xPoints[1] = position.x+4;
        yPoints[1] = position.y+3;
        xPoints[2] = position.x+4;
        yPoints[2] = position.y-3;
        g2d.fillPolygon(new Polygon(xPoints, yPoints, 3));
        // (iv) left
        xPoints[0] = position.x-8;
        yPoints[0] = position.y+0;
        xPoints[1] = position.x-4;
        yPoints[1] = position.y+3;
        xPoints[2] = position.x-4;
        yPoints[2] = position.y-3;
        g2d.fillPolygon(new Polygon(xPoints, yPoints, 3));    
        
        // horizontal line
        g2d.drawLine(position.x-8, position.y, position.x+8, position.y);
        // vertical line
        g2d.drawLine(position.x, position.y-8, position.x, position.y+8);
    }
    
    /**
     * renders a head of an arrow
     * 
     * @param g2d graphics-object
     * 
     * @author Andreas Schwenk
     */
    public static void renderArrowHead(Graphics2D g2d)
    {
        // construct vertices
        int xpoints[] = new int[3];
        int ypoints[] = new int[3];

        // build geometry
        xpoints[0] = -14;
        ypoints[0] = +6;
        xpoints[1] = 0;
        ypoints[1] = 0;
        xpoints[2] = -14;
        ypoints[2] = -6;

        // render
        g2d.fillPolygon(new Polygon(xpoints, ypoints, 3));
    }

    /**
     * sets the panel-size
     * 
     * @param panelSize new panel-size
     * 
     * @author Andreas Schwenk
     */
    public void setPanelSize(Point panelSize) {
        this.panelSize = panelSize;
    }

    /**
     * sets weather the selection rectangle should be rendered
     * 
     * @param val rendered?
     * 
     * @author Andreas Schwenk
     */
// TODO: ADD TO CLASS-DIAGRAM
    public void setIsRenderSelectionRect(boolean val)
    {
        isRenderSelectionRect = val;
    }
    
    /**
     * sets the boundary of the selection-rectangle
     * 
     * @param p1 point p1
     * @param p2 point p2
     * 
     * @author Andreas Schwenk
     */
// TODO: ADD TO CLASS-DIAGRAM
    public void setSelectionRect(Point p1, Point p2)
    {
        selectionRectP1 = p1;
        selectionRectP2 = p2;
    }

    /**
     * grid-rendering on?
     * 
     * @return grid-rendering on?
     * 
     * @author Andreas Schwenk
     */
    public boolean isIsRenderGrid() {
        return isRenderGrid;
    }

    /**
     * set: grid-rendering on?
     * 
     * @param isRenderGrid grid-rendering on?
     * 
     * @author Andreas Schwenk
     */
    public void setIsRenderGrid(boolean isRenderGrid) {
        this.isRenderGrid = isRenderGrid;
    }
}
