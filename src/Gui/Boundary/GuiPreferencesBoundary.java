/*
 * STDE - State Transition Diagram Editor
 *
 * 2011, 2012 Jan Montag, Andreas Schwenk
 *
 * Component:   Gui.Boundary
 * Class:       GuiPreferencesBoundary
 * Created:     2012-01-14
 */
package Gui.Boundary;

import Graph.Graph.GRAPH_TYPE;

public class GuiPreferencesBoundary
{
    private String  projectName="MyGraph";
    private int     projectWidth=600;
    private int     projectHeight=400;
    private String  projectPath="";
    private String  exportPath="";
    private GRAPH_TYPE graphType=GRAPH_TYPE.MOORE;
    private Boolean vhdlUseProcess=false;

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String expertPath) {
        this.exportPath = expertPath;
    }

    public int getProjectHeight() {
        return projectHeight;
    }

    public void setProjectHeight(int projectHeight) {
        this.projectHeight = projectHeight;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public int getProjectWidth() {
        return projectWidth;
    }

    public void setProjectWidth(int projectWidth) {
        this.projectWidth = projectWidth;
    }

    public GRAPH_TYPE getGraphType() {
        return graphType;
    }

    public void setGraphType(GRAPH_TYPE graphType) {
        this.graphType = graphType;
    }

    public Boolean getVhdlUseProcess() {
        return vhdlUseProcess;
    }

    public void setVhdlUseProcess(Boolean vhdlUseProcess) {
        this.vhdlUseProcess = vhdlUseProcess;
    }
    
}
