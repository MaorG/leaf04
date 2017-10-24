package leaf04;

import repast.simphony.valueLayer.IGridValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;

public abstract class CustumValueLayerDiffuser extends ValueLayerDiffuser {
	
	protected double dx;
	protected double dt;

	public CustumValueLayerDiffuser(IGridValueLayer valueLayer, double evaporationConst,
			double diffusionConst, boolean toroidal) {
		super(valueLayer, evaporationConst, diffusionConst, toroidal);
	}
	
	public CustumValueLayerDiffuser() {
		super();
	}
	
	public void setScale(double scale) {
		this.dx = scale;
	}
	
	public void setDt(double dt) {
		this.dt = dt;
	}
	
	abstract public void determineMaxStableDt();


}
