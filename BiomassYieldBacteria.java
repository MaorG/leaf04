package leaf04;

import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;

public class BiomassYieldBacteria extends ComplicatedBacteria{

	protected Double attachedYield;
	protected Double detachedYield;
	
	public BiomassYieldBacteria(String speciesName) {
		super(speciesName);
		speciesClassName = "BiomassYieldBacteria";
	}

//	public double getStepTime() {
//		@SuppressWarnings("unchecked")
//		Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
//		return ((Leaf04Context) context).stepTime;
//	}
	
	public void initAgentProperties(Context<PhysicalAgent> context) {
		
		Zoo zoo = ((Leaf04Context) context).zoo;
		
		Map<String, Map<String, String>> speciesDefaultProperties = zoo.speciesDefaultProperties;

		attachedYield = Double.parseDouble(speciesDefaultProperties.get(speciesName).get("attachedYield"));
		detachedYield = Double.parseDouble(speciesDefaultProperties.get(speciesName).get("detachedYield"));
		
		super.initAgentProperties(context);
	}
	
	@Override
	public void addMass(double d) {
		
		
		if (this.attached) {
			setMass(this.mass + attachedYield*d);
			System.out.println("adding " + String.valueOf(d) + " * " + String.valueOf(attachedYield));
		}
		else {
			setMass(this.mass + detachedYield*d);
			System.out.println("adding " + String.valueOf(d) + " * " + String.valueOf(detachedYield));
		}				
	}


}
