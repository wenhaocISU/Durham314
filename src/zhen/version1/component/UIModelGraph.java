package zhen.version1.component;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

import zhen.version1.Support.Utility;

import com.android.hierarchyviewerlib.models.ViewNode;


/**
 * Responsibility: Given a set of UIStates and Events, maintain a graph structure
 * and a list UIStates for latter access.
 * 
 * 
 * @author zhenxu
 *
 */
public class UIModelGraph {
	public static boolean DEBUG = true;
	private final static String TAG = "UIModelGraph";
	private UIState lastState;
	private ListenableDirectedMultigraph graph;
	
	private List<UIState> knownVertices = new ArrayList<UIState>();
	
	public UIState getOrBuildState(String appName, String actName, ViewNode root, WindowInformation topWindowInfo){
		
		if(DEBUG) Utility.log(TAG, "getOrBuildState input,"+appName);
		if(DEBUG) Utility.log(TAG, "getOrBuildState input,"+actName);
		if(DEBUG) Utility.log(TAG, "------------------------------");
		
		int i = actName.indexOf(":");
		if(i > 0) actName= actName.substring(0,i);
		
		for(UIState state: knownVertices){
			if(DEBUG) Utility.log(TAG, "getOrBuildState state,"+state.appName);
			if(DEBUG) Utility.log(TAG, "getOrBuildState state,"+state.actName);
			
			if(!state.appName.equals(appName)) continue;
			if(!state.actName.equals(actName)) continue;
			
			boolean sameView = state.equals(root);
			if(DEBUG) Utility.log(TAG, "getOrBuildState sameView,"+sameView);
			if(sameView){ state.visitCount += 1; return state; }
		}
		return new UIState(appName,actName,root,topWindowInfo);
	}
	
	public void init() {
		graph = new ListenableDirectedMultigraph(Event.class);
		graph.addVertex(UIState.Launcher);
		knownVertices.add(UIState.Launcher);
		lastState = UIState.Launcher;
	}

	public void addLaunch(Event event, UIState newState){
		if(newState.visitCount == 0){ 
			knownVertices.add(newState); 
			this.graph.addVertex(newState);
		}
		event.setVertices(UIState.Launcher, newState);
		this.graph.addEdge(UIState.Launcher, newState, event);
		this.lastState = newState;
	}
	
	public void addTransition(Event event, UIState newState){
		if(newState.visitCount == 0){ 
			knownVertices.add(newState); 
			this.graph.addVertex(newState);
		}
		event.setVertices(lastState, newState);
		this.graph.addEdge(lastState, newState, event);
		this.lastState = newState;
	}
	
	
	
	public ListenableDirectedMultigraph getGraph() {
		return graph;
	}

	public void enableGUI(){
		JFrame frame = new JFrame();
		this.enableGUI(frame.getContentPane());
        frame.setTitle("LayoutRelationViewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
	}
	
	public void enableGUI(Container jContainer) {
		LayoutRelationViewer applet = new LayoutRelationViewer(graph);
		applet.init();
		jContainer.add(applet);
	}
	
	public UIState getPreviousState(){
		return lastState;
	}

	public static class ListenableDirectedMultigraph extends
			DefaultListenableGraph<UIState, Event> implements DirectedGraph<UIState, Event> {
		private static final long serialVersionUID = 1L;

		ListenableDirectedMultigraph(Class<Event> edgeClass) {
			super(new DirectedMultigraph<UIState, Event>(edgeClass));
		}
		@Override
		public boolean addEdge(UIState sourceVertex, UIState targetVertex, Event e){
			System.out.println(sourceVertex+" -> "+targetVertex);
			
			if(sourceVertex.equals(targetVertex)){
				sourceVertex.addIneffectiveEvent(e);
			}else{
				super.addEdge(sourceVertex, targetVertex, e);
			}
			return true;
		}
	}
	
	public static class LayoutRelationViewer extends JApplet {
		private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
		private static final Dimension DEFAULT_SIZE = new Dimension(800,600);
		private JGraphModelAdapter<UIState, Event> jgAdapter;
		private ListenableDirectedMultigraph g;

		public LayoutRelationViewer(ListenableDirectedMultigraph g) {
			this.g = g;
		}

		public void init() {
			jgAdapter = new JGraphModelAdapter<UIState, Event>(g);
			JGraph jgraph = new JGraph(jgAdapter);
			adjustDisplaySettings(jgraph);
			getContentPane().add(jgraph);
			resize(DEFAULT_SIZE);
		}

		private void adjustDisplaySettings(JGraph jg) {
			jg.setPreferredSize(DEFAULT_SIZE);
			Color c = DEFAULT_BG_COLOR;
			String colorStr = null;
			try {
				colorStr = getParameter("bgcolor");
			} catch (Exception e) {
			}
			if (colorStr != null) {
				c = Color.decode(colorStr);
			}
			jg.setBackground(c);
		}
		
	    private void positionVertexAt(Object vertex, int x, int y){
	        DefaultGraphCell cell = jgAdapter.getVertexCell(vertex);
	        AttributeMap attr = cell.getAttributes();
	        Rectangle2D bounds = GraphConstants.getBounds(attr);

	        Rectangle2D newBounds =
	            new Rectangle2D.Double(
	                x,
	                y,
	                bounds.getWidth(),
	                bounds.getHeight());

	        GraphConstants.setBounds(attr, newBounds);

	        // TODO: Clean up generics once JGraph goes generic
	        AttributeMap cellAttr = new AttributeMap();
	        cellAttr.put(cell, attr);
	        jgAdapter.edit(cellAttr, null, null, null);
	    }
	}
}