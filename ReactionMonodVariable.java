package leaf04;

import java.util.Map.Entry;

import org.w3c.dom.Node;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class ReactionMonodVariable extends ReactionMonod {

	public ReactionMonodVariable(Context<PhysicalAgent> context,
			Node reactionNode) {
		super(context, reactionNode);

	}
	
	@SuppressWarnings("unchecked")
	public void react(double dt) {
		//Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
		GridValueLayer valueLayer = (GridValueLayer) context.getValueLayer(soluteName);
		Grid <PhysicalAgent> grid = (Grid<PhysicalAgent>) context.getProjection("grid");

		int width = (int) valueLayer.getDimensions().getWidth();
		int height = (int) valueLayer.getDimensions().getHeight();


		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double totalDeltaBioMass = 0;				
				// apply reaction within grid cell
				Iterable<PhysicalAgent> agentList = (Iterable<PhysicalAgent>) grid.getObjectsAt(x,y);
				for (PhysicalAgent b : agentList) {
					if (b.hasReaction(name)) {
						double agentMass = b.mass;
						double conc = valueLayer.get(x,y);
						double varMu = 1.0;
						if (b.speciesClassName.equals("DensityBacteria")) {
							varMu = muMax * ((DensityBacteria)b).getMuFactor();
						}
						else {
							varMu = muMax;
						}
						double mu = (varMu * conc) / (Ks + conc);
						double newAgentMass = agentMass * Math.exp(mu * dt);
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
