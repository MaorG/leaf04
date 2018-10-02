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
import repast.simphony.valueLayer.GridValueLayer;

public class ReactionMonod extends Reaction {

	protected double muMax;
	protected double Ks;

	public ReactionMonod(Context<PhysicalAgent> context, Node reactionNode) {
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

		double biomassRelativeYield = yieldMap.get("biomass");

//		// sanity
//		OutputManager om = ((OutputManager) ((Leaf04Context) context).getOutputManager());
//
//		// biomass
//		String eol = System.getProperty("line.separator");
//		double finalTotalBiomass = 0;
//		for (int y = 0; y < height; y++) {
//			for (int x = 0; x < width; x++) {
//				Iterable<PhysicalAgent> agentList = (Iterable<PhysicalAgent>) grid.getObjectsAt(x, y);
//				for (PhysicalAgent b : agentList) {
//					double agentMass = b.mass;
//					finalTotalBiomass += agentMass;
//				}
//			}
//		}
//		String strBiomass = eol + "total biomass: " + finalTotalBiomass;
//		
//
//		// concentration
//		String strAllConc = "";
//		String strConc = null;
//		for (Entry<String, Double> entry : yieldMap.entrySet()) {
//			String key = entry.getKey();
//			if (!key.equalsIgnoreCase("biomass")) {
//				GridValueLayer someValueLayer = (GridValueLayer) context.getValueLayer(key);
//
//				double amount = 0;
//				for (int y = 0; y < height; y++) {
//					for (int x = 0; x < width; x++) {
//						amount += someValueLayer.get(x, y) * cellVolume;
//					}
//				}
//				strConc = eol + "total " + key + ": " + amount;
//				strAllConc = strAllConc.concat(strConc);
//			}
//		}
//
//		HashMap<String, Double> totalDomainDeltaMap = new HashMap<String, Double>();
//		for (Entry<String, Double> entry : yieldMap.entrySet()) {
//			String key = entry.getKey();
//			totalDomainDeltaMap.put(key, 0.0);
//		}

		// actual reaction with some sanity

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double finalDeltaBiomassPerCell = 0;
				double totalDeltaBioMass = 0;
				// apply reaction within grid cell
				Iterable<PhysicalAgent> agentList = (Iterable<PhysicalAgent>) grid.getObjectsAt(x, y);
				for (PhysicalAgent b : agentList) {
					if (b.hasReaction(name)) {
						double agentMass = b.mass;
						double conc = valueLayer.get(x, y);
						double mu = (muMax * conc) / (Ks + conc);
						double newAgentMass = agentMass * Math.exp(mu * dt);
						b.tempDeltaAgentMass = (newAgentMass - agentMass) * biomassRelativeYield;
						totalDeltaBioMass += b.tempDeltaAgentMass;

						// b.setMass(newAgentMass);
					}
				}

				for (Entry<String, Double> entry : yieldMap.entrySet()) {
					String key = entry.getKey();
					double soluteRelativeYield = entry.getValue();

					if (!key.equalsIgnoreCase("biomass")) {
						double deltaAmount = totalDeltaBioMass * (soluteRelativeYield / biomassRelativeYield);
						// TODO take volume into account!!!

						GridValueLayer someValueLayer = (GridValueLayer) context.getValueLayer(key);
						double initialAmount = someValueLayer.get(x, y) * cellVolume;
						double finalAmount = Math.max(0.0, initialAmount + deltaAmount);
						double ratio = 1.0;
						if (finalAmount == 0.0) {
							ratio = -initialAmount / deltaAmount;
						}

						// Todo: in order to make sure that reactions with more than one
						// non-biomass-solutes will not overdraft,
						// compare the ratios for all solutes, and choose the minimal

//						totalDomainDeltaMap.put(key, (finalAmount - initialAmount) + totalDomainDeltaMap.get(key));

						someValueLayer.set(finalAmount / cellVolume, x, y);

						agentList = (Iterable<PhysicalAgent>) grid.getObjectsAt(x, y);
						for (PhysicalAgent b : agentList) {
							if (b.hasReaction(name)) {
								b.setMass(b.mass + b.tempDeltaAgentMass * ratio);
								finalDeltaBiomassPerCell += b.tempDeltaAgentMass * ratio;
							}
						}
					}

				}
//				totalDomainDeltaMap.put("biomass", finalDeltaBiomassPerCell + totalDomainDeltaMap.get("biomass"));
			}
		}

		// sanity again

//		String deltas = "";
//
//		for (Entry<String, Double> entry : totalDomainDeltaMap.entrySet()) {
//			String key = entry.getKey();
//			double val = entry.getValue();
//			deltas = deltas.concat(deltas + eol + key + ": " + String.valueOf(val));
//			System.out.println(key);
//			System.out.println(String.valueOf(val));
//		}
//
//		String strDelta = eol + this.name + deltas;
//
//		
//		
//		om.addSanityEventStringToLog(strBiomass + strAllConc + strDelta);

	}

}
