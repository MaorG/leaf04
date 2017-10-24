package leaf04;

import java.util.Map.Entry;

import leaf04.utils.ParserUtils;

import org.w3c.dom.Node;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class ReactionConstVariable extends Reaction {


	public double rate;

	public ReactionConstVariable(Context<PhysicalAgent> context, Node reactionNode) {
		super(context, reactionNode);
		this.init(reactionNode);

	}

	@Override
	public void init(Node reactionNode) {
		super.init(reactionNode);
		rate = ParserUtils.getDoubleByName(reactionNode, "rate");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void react(double dt) {
		Grid <PhysicalAgent> grid = (Grid<PhysicalAgent>) context.getProjection("grid");

		int width = grid.getDimensions().getWidth();
		int height = grid.getDimensions().getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double totalDeltaBioMass = 0;				
				// apply reaction within grid cell
				Iterable<PhysicalAgent> agentList = (Iterable<PhysicalAgent>) grid.getObjectsAt(x,y);
				for (PhysicalAgent b : agentList) {
					if (b.hasReaction(name)) {
						double biomassRelativeYield = yieldMap.get("biomass");
						double agentMass = b.mass;
						
						double varMu = 1.0;
						if (b.speciesClassName.equals("DensityBacteria")) {
							varMu = ((DensityBacteria)b).getMuFactor();
						}

						double newAgentMass = agentMass + varMu * rate * dt * biomassRelativeYield;
						totalDeltaBioMass += (newAgentMass - agentMass);

						b.setMass(newAgentMass);
					}
				}
				for (Entry<String, Double> entry : yieldMap.entrySet()) {
					String key = entry.getKey();
					double soluteRelativeYield = entry.getValue();

					double biomassRelativeYield = yieldMap.get("biomass");

					if (!key.equalsIgnoreCase("biomass")) {
						double deltaAmount = totalDeltaBioMass * (soluteRelativeYield / biomassRelativeYield);
						// TODO take volume into account!!!

						GridValueLayer someValueLayer = (GridValueLayer) context.getValueLayer(key);
						double initialAmount = someValueLayer.get(x,y) * cellArea;
						double finalAmount = Math.max(0.0, initialAmount + deltaAmount);
						someValueLayer.set(finalAmount / cellArea, x,y);

					}
				}
			}
		}


	}

}
