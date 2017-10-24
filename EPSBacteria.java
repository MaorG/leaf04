package leaf04;

import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;

public class EPSBacteria extends Bacteria {

	private double epsMass;
	private double epsEmitMass;
	private double bioMass;
	private double epsProductionRatio;
	private double oldMass;
	
	public EPSBacteria(String speciesName) {
		super(speciesName);		
		speciesClassName = "EPSBacteria";
		
		oldMass = 0;
	}

	
	public void initAgentProperties(Context<PhysicalAgent> context) {

		
		//Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
		Zoo zoo = ((Leaf04Context)context).zoo;
		
		Map<String, Map<String, String>> speciesDefaultProperties = zoo.speciesDefaultProperties;
		epsEmitMass = zoo.getRandomNormal(
				Double.parseDouble(speciesDefaultProperties.get(speciesName).get("epsEmitMassMean")), 
				Double.parseDouble(speciesDefaultProperties.get(speciesName).get("epsEmitMassSD")));
		epsEmitMass = Math.max(
					Math.min(
							epsEmitMass, 
							Double.parseDouble(speciesDefaultProperties.get(speciesName).get("epsEmitMassMax"))), 
					Double.parseDouble(speciesDefaultProperties.get(speciesName).get("epsEmitMassMin")));
		
		epsProductionRatio = Double.parseDouble(speciesDefaultProperties.get(speciesName).get("epsProductionRatio"));
		
		super.initAgentProperties(context);

	}
	
	
	// TODO - reproduce determined by biomass. not mass! 
	
	
	@SuppressWarnings("unchecked")
	@ScheduledMethod ( start = 1, interval = 1, priority = 4)
	public void handleEPS() {
		
		double deltaMass = mass - oldMass;
		
		// if deltaMass is positive && not the first step 
		if (deltaMass > 0 && deltaMass < mass) {
			epsMass += deltaMass * epsProductionRatio;
			bioMass = mass - epsMass;
		}
		
		
		
		if (this.epsMass > epsEmitMass) {
			Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
			
			Zoo zoo = ((Leaf04Context)context).zoo;

			PhysicalAgent eps = (PhysicalAgent) zoo.createAgent("PhysicalAgent", "");
			eps.setMass(epsMass);
			context.add(eps);

			this.epsMass = 0;
			this.setMass(bioMass);
			
			NdPoint myPoint = space.getLocation(this);
			space.moveTo(eps, myPoint.getX(), myPoint.getY());
			double angle = (float) RandomHelper.nextDoubleFromTo(0.0, 2.0*Math.PI);
			this.moveByAngleAndDist(angle, eps.radius * 0.03);
			eps.moveByAngleAndDist(angle + Math.PI, this.radius);
		}
		
		oldMass = mass;
	}

}
