package leaf04;

import repast.simphony.context.Context;
import repast.simphony.util.ContextUtils;

public class DeadBacteria extends Bacteria {

	public DeadBacteria(String speciesName) {
		super(speciesName);
		
	}

	@SuppressWarnings("unchecked")
	public void die() {
		Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
		context.remove(this);
		
		
		
	}
}
