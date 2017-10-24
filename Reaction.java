package leaf04;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import leaf04.utils.ParserUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;


public abstract class Reaction {

	protected String name;
	protected String soluteName; 
	protected Map<String, Double> yieldMap = new HashMap<String, Double>();
	protected Context<PhysicalAgent> context;
	protected double cellArea;


	public Reaction(Context<PhysicalAgent> context, Node reactionNode) {
		this.context = context; // TODO - add to context somehow
	}

	@SuppressWarnings("unchecked")
	public void init (Node reactionNode) {
		Grid <PhysicalAgent> grid = (Grid<PhysicalAgent>) context.getProjection("grid");
		ContinuousSpace<PhysicalAgent> space = (ContinuousSpace<PhysicalAgent>) context.getProjection("space");;
		Element e = (Element)reactionNode;
		name = e.getAttribute("name");
		double gridScale = space.getDimensions().getWidth() / grid.getDimensions().getWidth();
		cellArea = gridScale * gridScale;
		
		Node yieldNode = ParserUtils.getNodesByTagName(reactionNode, "yield").get(0);

		//TODO: maybe move to ParserUtils, something like "node2map"

		NodeList nodeList = yieldNode.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			switch (nodeList.item(i).getNodeType()) {
			case Node.ELEMENT_NODE:
				Element element = (Element) nodeList.item(i);
				String yieldName = element.getAttribute("name");
				double yieldRatio = Double.parseDouble(nodeList.item(i).getTextContent());
				yieldMap.put(yieldName, yieldRatio);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	abstract public void react(double dt);

}
