package leaf04;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import leaf04.utils.OutputManager;
import leaf04.utils.ParserUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
import repast.simphony.valueLayer.GridValueLayer;

public class ReactionMonodUbique extends Reaction {

	protected double muMax;
	protected double Ks;

	public ReactionMonodUbique(Context<PhysicalAgent> context, Node reactionNode) {
		super(context, reactionNode);
		this.init(reactionNode);
	}

	@Override
	public void init(Node reactionNode) {
		super.init(reactionNode);
		muMax = ParserUtils.getDoubleByName(reactionNode, "muMax");
		Ks = ParserUtils.getDoubleByName(reactionNode, "Ks");
		soluteName = ParserUtils.getStringByName(reactionNode, "soluteName");
	}

	@SuppressWarnings("unchecked")
	public void react(double dt) {
		// Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
		GridValueLayer valueLayer = (GridValueLayer) context.getValueLayer(soluteName);
		Grid<PhysicalAgent> grid = (Grid<PhysicalAgent>) context.getProjection("grid");

		int width = (int) valueLayer.getDimensions().getWidth();
		int height = (int) valueLayer.getDimensions().getHeight();

		
		// get total conc and amount for all solutes

		
		
		double biomassRelativeYield = yieldMap.get("biomass");

		
		HashMap<String, Double> totalConcMap = new HashMap<String, Double>();
		for (Entry<String, Double> entry : yieldMap.entrySet()) {
			String key = entry.getKey();
			GridValueLayer someValueLayer = (GridValueLayer) context.getValueLayer(key);
			double totalConc = 0;
			if (!key.equalsIgnoreCase("biomass")) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {		
						totalConc += someValueLayer.get(x, y);
					}
				}
				totalConcMap.put(key, totalConc / (height*width));
			}
		}
		

		
		
		
		// sanity
		OutputManager om = ((OutputManager) ((Leaf04Context) context).getOutputManager());

		// biomass
		String eol = System.getProperty("line.separator");
		double finalTotalBiomass = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Iterable<PhysicalAgent> agentList = (Iterable<PhysicalAgent>) grid.getObjectsAt(x, y);
				for (PhysicalAgent b : agentList) {
					double agentMass = b.mass;
					finalTotalBiomass += agentMass;
				}
			}
		}
		String strBiomass = eol + "total biomass: " + finalTotalBiomass;
		

		// concentration
		String strAllConc = "";
		String strConc = null;
		for (Entry<String, Double> entry : yieldMap.entrySet()) {
			String key = entry.getKey();
			if (!key.equalsIgnoreCase("biomass")) {
				GridValueLayer someValueLayer = (GridValueLayer) context.getValueLayer(key);

				double amount = 0;
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						amount += someValueLayer.get(x, y) * cellVolume;
					}
				}
				strConc = eol + "total " + key + ": " + amount;
				strAllConc = strAllConc.concat(strConc);
			}
		}

		HashMap<String, Double> totalDomainDeltaMap = new HashMap<String, Double>();
		for (Entry<String, Double> entry : yieldMap.entrySet()) {
			String key = entry.getKey();
			totalDomainDeltaMap.put(key, 0.0);
		}

		
		
		// actual reaction with some sanity
		double totalDeltaBioMass = 0;
		
		IndexedIterable<PhysicalAgent> allAgents = context.getObjects(Object.class);
		for (PhysicalAgent b : allAgents) {
			if (b.hasReaction(name)) {
				double agentMass = b.mass;
				double conc = totalConcMap.get(soluteName);
				double mu = (muMax * conc) / (Ks + conc);
				double newAgentMass = agentMass * Math.exp(mu * dt);
				b.tempDeltaAgentMass = (newAgentMass - agentMass) * biomassRelativeYield;
				totalDeltaBioMass += b.tempDeltaAgentMass;

				// b.setMass(newAgentMass);
			}
		}

		double finalDeltaBiomass = 0;
		for (Entry<String, Double> entry : yieldMap.entrySet()) {
			String key = entry.getKey();
			double soluteRelativeYield = entry.getValue();

			if (!key.equalsIgnoreCase("biomass")) {
				double deltaAmount = totalDeltaBioMass * (soluteRelativeYield / biomassRelativeYield);
				// TODO take volume into account!!!

				double initialAmount = totalConcMap.get(key) * cellVolume * width * height;
				double finalAmount = Math.max(0.0, initialAmount + deltaAmount);
				double ratio = 1.0;
				if (deltaAmount == 0.0) {
					ratio = 1.0;
				}
				else if (finalAmount == 0.0) {
					ratio = -initialAmount / deltaAmount;
				}

				// Todo: in order to make sure that reactions with more than one
				// non-biomass-solutes will not overdraft,
				// compare the ratios for all solutes, and choose the minimal

				// sanity
				totalDomainDeltaMap.put(key, (finalAmount - initialAmount) + totalDomainDeltaMap.get(key));


				// updating value layer - move to a function
				GridValueLayer someValueLayer = (GridValueLayer) context.getValueLayer(key);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {		
						someValueLayer.set(finalAmount / (cellVolume * width * height), x, y);
					}
				}
				
				allAgents = context.getObjects(Object.class);
				for (PhysicalAgent b : allAgents) {
					if (b.hasReaction(name)) {
						b.addMass(b.tempDeltaAgentMass * ratio);
						finalDeltaBiomass += b.tempDeltaAgentMass * ratio;
					}
				}
			}

		}
		totalDomainDeltaMap.put("biomass", finalDeltaBiomass + totalDomainDeltaMap.get("biomass"));

		// sanity again

		String deltas = "";

		for (Entry<String, Double> entry : totalDomainDeltaMap.entrySet()) {
			String key = entry.getKey();
			double val = entry.getValue();
//			deltas = deltas.concat(deltas + eol + key + ": " + String.valueOf(val));
//			System.out.println(key);
//			System.out.println(String.valueOf(val));
		}

		String strDelta = eol + this.name + deltas;

		
		
		om.addSanityEventStringToLog(strBiomass + strAllConc + strDelta);

	}

}
