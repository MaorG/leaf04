package leaf04;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leaf04.utils.ParserUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.jet.random.Normal;
import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Zoo {
	
	// TODO - should be a singleton, just like context.
	
	private Context<PhysicalAgent> context;
	private Map<String, Map<String, Boolean>> speciesReactionNamesMap = new HashMap<String, Map<String, Boolean>>();
	public Map<String, Map<String, String>> speciesDefaultProperties = new HashMap<String, Map<String, String>>();
	public Map<String, Color> speciesColors = new HashMap<String, Color>();
	private ContinuousSpace<PhysicalAgent> space;
	private Grid<PhysicalAgent> grid;
	
	// random utilities... this is not the perfect place :(
	public Normal normalDist;

	public Zoo(Leaf04Context context, ContinuousSpace<PhysicalAgent> space, Grid<PhysicalAgent> grid) {
		this.context = context;
		this.space = space;
		this.grid = grid;
		
		normalDist = RandomHelper.createNormal(0.0, 1.0);
		
	}

	public void init(Node agentsNode) {
		List <Node> agentNodes = ParserUtils.getNodesByTagName(agentsNode, "agent");
		for (Node agentNode : agentNodes) {
			initAgentSpecies(agentNode);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void initAgentSpecies(Node agentNode) {

		//String agentClassName = ParserUtils.getStringByName(agentNode, "class");
		String agentSpeciesName = ParserUtils.getStringByName(agentNode, "species");
		speciesReactionNamesMap.put(agentSpeciesName, new HashMap<String, Boolean>());
		speciesDefaultProperties.put(agentSpeciesName, new HashMap<String, String>());



		Node reactionNamesNode = ParserUtils.getNodeByTagName(agentNode, "reactionNames");

		if (reactionNamesNode != null) {
			NodeList nodeList = reactionNamesNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				switch (nodeList.item(i).getNodeType()) {
				case Node.ELEMENT_NODE:
					Element element = (Element) nodeList.item(i);
					String reactionName = element.getAttribute("name");
					Boolean active = Integer.parseInt(nodeList.item(i).getTextContent()) != 0;
					speciesReactionNamesMap.get(agentSpeciesName).put(reactionName, active);
				}
			}
		} 

		Node defaultPropertiesNode = ParserUtils.getNodeByTagName(agentNode, "properties");

		if (defaultPropertiesNode != null) {
			NodeList nodeList = defaultPropertiesNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				switch (nodeList.item(i).getNodeType()) {
				case Node.ELEMENT_NODE:
					Element element = (Element) nodeList.item(i);
					String propertyName = element.getAttribute("name");
					String propertyValue = nodeList.item(i).getTextContent();
					speciesDefaultProperties.get(agentSpeciesName).put(propertyName, propertyValue);
				}
			}
		}
		
		Node defaultColor = ParserUtils.getNodesByName(agentNode, "color").get(0);
		if (defaultColor != null) {
			String colorValue = defaultColor.getTextContent();
			List<String> channels = Arrays.asList(colorValue.split("\\s*,\\s*"));
			Color color = new Color(
					(float)Double.parseDouble(channels.get(0)),
					(float)Double.parseDouble(channels.get(1)),
					(float)Double.parseDouble(channels.get(2))
			);
			speciesColors.put(agentSpeciesName, color);
		}
		
		
		
		colonizeAgents(agentNode);
		
	}
	
	private void colonizeAgents(Node agentNode) {
		Node colonizeNode = ParserUtils.getNodeByTagName(agentNode, "colonize");
		String agentClassName = ParserUtils.getStringByName(agentNode, "class");
		String agentSpeciesName = ParserUtils.getStringByName(agentNode, "species");
		double gridScale = space.getDimensions().getWidth() / grid.getDimensions().getWidth();

		
		double maxx = 0;
		if (colonizeNode != null) {
			int amount = ParserUtils.getIntByName(colonizeNode, "amount");
			for (int i = 0; i < amount; i++) {
				
				
				PhysicalAgent agent = createAgent(agentClassName, agentSpeciesName);
				((PhysicalAgent)agent).setInitialMass(1.0);
				
				if(agent instanceof Bacteria) {
					((Bacteria) agent).setLineageId(i);
					((Bacteria) agent).initHistory();
				}

				
				NdPoint pt = space.getLocation(agent);
				if (pt.getX() > maxx)
					maxx = pt.getX(); 
				grid.moveTo((PhysicalAgent)agent, (int)(pt.getX()/gridScale), (int)(pt.getY()/gridScale));
			}
		}
		
		
	}

	public PhysicalAgent createAgent(String speciesClassName, String speciesName) {
		PhysicalAgent agent = null;

		if (speciesClassName.equalsIgnoreCase("PhysicalAgent")) {
			agent = new PhysicalAgent(speciesName);
		}
		if (speciesClassName.equalsIgnoreCase("Bacteria")) {
			agent = new Bacteria(speciesName);
		}
		if (speciesClassName.equalsIgnoreCase("EPSBacteria")) {
			agent = new EPSBacteria(speciesName);
		}
		if (speciesClassName.equalsIgnoreCase("EmittingBacteria")) {
			agent = new EmittingBacteria(speciesName);
		}
		if (speciesClassName.equalsIgnoreCase("AttachingBacteria")) {
			agent = new AttachingBacteria(speciesName);
		}
		if (speciesClassName.equalsIgnoreCase("DensityBacteria")) {
			agent = new DensityBacteria(speciesName);
		}
		if (speciesClassName.equalsIgnoreCase("AntibioticDensityBacteria")) {
			agent = new AntibioticDensityBacteria(speciesName);
		}
		if (speciesClassName.equalsIgnoreCase("ComplicatedBacteria")) {
			agent = new ComplicatedBacteria(speciesName);
		}
		if (speciesClassName.equalsIgnoreCase("DeadBacteria")) {
			agent = new DeadBacteria(speciesName);
		}		
		
		context.add(agent);
		agent.grid = this.grid;
		agent.space = this.space;

		agent.setReactionNamesMap(speciesReactionNamesMap.get(speciesName));

		agent.stepTime = ((Leaf04Context) context).stepTime;
		agent.initAgentProperties(context);
		agent.init();
		return agent;
		
	}
	
	public double getRandomNormal(double mean, double std) {
		
		return normalDist.nextDouble() * std + mean;
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static class AgentInitializer {
		
		public Map<String, Map<String, Boolean>> speciesReactionNamesMap = new HashMap<String, Map<String, Boolean>>();
		public Map<String, Map<String, String>> speciesDefaultProperties = new HashMap<String, Map<String, String>>();
		public Map<String, Color> speciesColors = new HashMap<String, Color>();
		Zoo zoo;
		
		public AgentInitializer(Zoo zoo, Node agentNode) {

			this.zoo = zoo;
			
			String agentSpeciesName = ParserUtils.getStringByName(agentNode, "species");
			speciesReactionNamesMap.put(agentSpeciesName, new HashMap<String, Boolean>());
			speciesDefaultProperties.put(agentSpeciesName, new HashMap<String, String>());
			
			Node reactionNamesNode = ParserUtils.getNodeByTagName(agentNode, "reactionNames");

			if (reactionNamesNode != null) {
				NodeList nodeList = reactionNamesNode.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					switch (nodeList.item(i).getNodeType()) {
					case Node.ELEMENT_NODE:
						Element element = (Element) nodeList.item(i);
						String reactionName = element.getAttribute("name");
						Boolean active = Integer.parseInt(nodeList.item(i).getTextContent()) != 0;
						speciesReactionNamesMap.get(agentSpeciesName).put(reactionName, active);
					}
				}
			} 

			Node defaultPropertiesNode = ParserUtils.getNodeByTagName(agentNode, "properties");

			if (defaultPropertiesNode != null) {
				NodeList nodeList = defaultPropertiesNode.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					switch (nodeList.item(i).getNodeType()) {
					case Node.ELEMENT_NODE:
						Element element = (Element) nodeList.item(i);
						String propertyName = element.getAttribute("name");
						String propertyValue = nodeList.item(i).getTextContent();
						speciesDefaultProperties.get(agentSpeciesName).put(propertyName, propertyValue);
					}
				}
			}
			
			Node defaultColor = ParserUtils.getNodesByName(agentNode, "color").get(0);
			if (defaultColor != null) {
				String colorValue = defaultColor.getTextContent();
				List<String> channels = Arrays.asList(colorValue.split("\\s*,\\s*"));
				Color color = new Color(
						(float)Double.parseDouble(channels.get(0)),
						(float)Double.parseDouble(channels.get(1)),
						(float)Double.parseDouble(channels.get(2))
				);
				speciesColors.put(agentSpeciesName, color);
			}
			
		}
		
		public PhysicalAgent createAgent(String speciesClassName, String speciesName) {
			
			PhysicalAgent agent = new PhysicalAgent(speciesName);
			
			initAgent(agent);
			
			return agent;
		}
		
		public void initAgent(PhysicalAgent agent) {
			zoo.context.add(agent);
			agent.grid = this.zoo.grid;
			agent.space = this.zoo.space;
			//agent.setReactionNamesMap(speciesReactionNamesMap.get(agent.speciesName));
			agent.init();
			//agent.initAgentProperties(zoo.context);
			
			if (this.speciesColors.containsKey(agent.speciesName)) {
				agent.setColor(zoo.speciesColors.get(agent.speciesName));
			}
			else {
				agent.setColor(new Color(1.0f,1.0f,1.0f));
			}
		}
		
	}
	
	private static class BacteriaInitializer extends AgentInitializer {

		public BacteriaInitializer(Zoo zoo, Node agentNode) {
			super(zoo, agentNode);

		}
		
		@Override
		public PhysicalAgent createAgent(String speciesClassName, String speciesName) {
			PhysicalAgent agent = new Bacteria(speciesName);
			initAgent(agent);
			return agent;
		}
		
		@Override
		public void initAgent(PhysicalAgent agent) {
			super.initAgent(agent);
			
			String speciesName = agent.speciesName;
			Bacteria bacteria = (Bacteria)agent;
			
			Map<String, Map<String, String>> speciesDefaultProperties = zoo.speciesDefaultProperties;
			bacteria.divMass = zoo.getRandomNormal(
					Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassMean")), 
					Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassSD")));
			bacteria.divMass = Math.max(
						Math.min(
								bacteria.divMass, 
								Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassMax"))), 
						Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassMin")));
			
			bacteria.deathMass = zoo.getRandomNormal(
					Double.parseDouble(speciesDefaultProperties.get(speciesName).get("deathMassMean")), 
					Double.parseDouble(speciesDefaultProperties.get(speciesName).get("deathMassSD")));
			bacteria.deathMass = Math.max(
						Math.min(
								bacteria.deathMass, 
								Double.parseDouble(speciesDefaultProperties.get(speciesName).get("deathMassMax"))), 
						Double.parseDouble(speciesDefaultProperties.get(speciesName).get("deathMassMin")));
			
		}
		
	}

	private static class AttachingBacteriaInitializer extends BacteriaInitializer {

		public Map<String, Map<String, String>> speciesNghAttachmentProperties = new HashMap<String, Map<String, String>>();
		public Map<String, Map<String, String>> speciesSoluteAttachmentProperties = new HashMap<String, Map<String, String>>();
		public Map<String, Map<String, String>> speciesRandomAttachmentProperties = new HashMap<String, Map<String, String>>();

		
		public AttachingBacteriaInitializer(Zoo zoo, Node agentNode) {
			
			super(zoo, agentNode);	
			
			String agentSpeciesName = ParserUtils.getStringByName(agentNode, "species");
			Node defaultPropertiesNode = ParserUtils.getNodeByTagName(agentNode, "properties");

			if (defaultPropertiesNode != null) {
				NodeList nodeList = defaultPropertiesNode.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					switch (nodeList.item(i).getNodeType()) {
					case Node.ELEMENT_NODE:
						Element element = (Element) nodeList.item(i);
						String propertyName = element.getAttribute("name");
						String propertyValue = nodeList.item(i).getTextContent();
						speciesNghAttachmentProperties.get(agentSpeciesName).put(propertyName, propertyValue);
					}
				}
			}
			

		}
	
		@Override
		public PhysicalAgent createAgent(String speciesClassName, String speciesName) {
			PhysicalAgent agent = new AttachingBacteria(speciesName);
			initAgent(agent);
			return agent;
		}
		
		@Override
		public void initAgent(PhysicalAgent agent) {
			super.initAgent(agent);
		}

		
	}



}
