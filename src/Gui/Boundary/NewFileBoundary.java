/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Gui.Boundary
 * Class:       NewFileBoundary
 * Created:     ?
 */

package Gui.Boundary;

import Graph.Graph.GRAPH_TYPE;

/**
 *
 * @author user
 */
public class NewFileBoundary {

    private String name;
    private GRAPH_TYPE type;
    private int width, height;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GRAPH_TYPE getType() {
        return type;
    }

    public void setType(GRAPH_TYPE type) {
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }


}
