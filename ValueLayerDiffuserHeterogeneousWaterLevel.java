package leaf04;

import expr.Variable;
import repast.simphony.space.continuous.WrapAroundBorders;
import repast.simphony.valueLayer.IGridValueLayer;

public class ValueLayerDiffuserHeterogeneousWaterLevel extends
		ValueLayerDiffuserHeterogeneous {

	protected IGridValueLayer waterLevelLayer;

	
	
	public ValueLayerDiffuserHeterogeneousWaterLevel(
			IGridValueLayer valueLayer, double evaporationConst,
			IGridValueLayer diffusionLayer, boolean toroidal) {
		super(valueLayer, evaporationConst, diffusionLayer, toroidal);
	}
	
	public ValueLayerDiffuserHeterogeneousWaterLevel(
			IGridValueLayer valueLayer, double evaporationConst, double d,
			boolean toroidal) {
		super(valueLayer, evaporationConst, d, toroidal);
	}

	@Override
	protected void computeVals() {

		int size = valueLayer.getDimensions().size();
		
		if (size == 2) {
			int width = (int) valueLayer.getDimensions().getWidth();
			int height = (int) valueLayer.getDimensions().getHeight();

			double[][] oldVals = new double[width + 2][height + 2];
			double[][] newVals = new double[width][height];
			double[][] diffVals = new double[width + 2][height + 2];
			double[][] waterLevelVals = new double[width + 2][height + 2];

			for (int y = -1; y < height + 1; y++) {
				for (int x = -1; x < width + 1; x++) {
					oldVals[x + 1][y + 1] = getValue(x, y);
				}
			}

			for (int y = -1; y < height + 1; y++) {
				for (int x = -1; x < width + 1; x++) {
					diffVals[x + 1][y + 1] = getDiffValue(x, y);
				}
			}
			
			for (int y = -1; y < height + 1; y++) {
				for (int x = -1; x < width + 1; x++) {
					waterLevelVals[x + 1][y + 1] = getWaterLevelValue(x, y);
				}
			}

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {

					double oldVal = getValue(x, y);
					double uE = oldVals[x + 2][y + 1];
					double uN = oldVals[x + 1][y + 2];
					double uW = oldVals[x][y + 1];
					double uS = oldVals[x + 1][y];

					double localDiff = diffVals[x + 1][y + 1];
					double diffE = 0.5 * (diffVals[x + 2][y + 1] + localDiff);
					double diffN = 0.5 * (diffVals[x + 1][y + 2] + localDiff);
					double diffW = 0.5 * (diffVals[x][y + 1] + localDiff);
					double diffS = 0.5 * (diffVals[x + 1][y] + localDiff);

					double localZ = waterLevelVals[x + 1][y + 1];
					double zE = 0.5 * (waterLevelVals[x + 2][y + 1] + localZ);
					double zN = 0.5 * (waterLevelVals[x + 1][y + 2] + localZ);
					double zW = 0.5 * (waterLevelVals[x][y + 1] + localZ);
					double zS = 0.5 * (waterLevelVals[x + 1][y] + localZ);

				
					
					// D*(d^2(c) / dx^2)
					double firstTerm =  
							(dt / (dx*dx*localZ)) * 
							(-oldVal * (diffE * zE + diffN * zN + diffW * zW + zS * diffS) + 
									uE * diffE * zE + 
									uN * diffN * zN + 
									uW * diffW * zW + 
									uS * diffS * zS
									);
					
					// (d(D)/dx) * dc/dx)
					double secondTerm = 0.25*
							(dt / (dx*dx*localZ)) *
							(
								(diffE * zE - diffW * zW) * (uE - uW) + 
								(diffN * zN - diffS * zS) * (uN - uS)
							);
					

					
					newVals[x][y] = oldVal + firstTerm;// + secondTerm;

					if (!(newVals[x][y]  >= 0.0)){
						newVals[x][y] = 0;
					}

					
					//					newVals[x][y] = oldVal + (dttemp / (dx*dx*localZ)) * 
//							(-oldVal * (diffE * zE + diffN * zN + diffW * zW + zS * diffS) + 
//									uE * diffE * zE + 
//									uN * diffN * zN + 
//									uW * diffW * zW + 
//									uS * diffS * zS
//									)
//									;

				}
			}
			computedVals = newVals;
		}
	}
	
	public void setWaterLevelLayer(IGridValueLayer waterLevelLayer) {
		this.waterLevelLayer = waterLevelLayer;

	}
	
	protected double getWaterLevelValue(double... coords) {
		return waterLevelLayer.get(coords);
	}
	
	public void determineMaxStableDt() {

		// deltaT <= 0.5 * deltaX^2 / alpha
		
		int width = (int) diffusionLayer.getDimensions().getWidth();
		int height = (int) diffusionLayer.getDimensions().getHeight();

		
		double maxDiffValue = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (getDiffValue(x, y) * getWaterLevelValue(x,y) > maxDiffValue) {
					maxDiffValue = getDiffValue(x, y) * getWaterLevelValue(x,y);
				}
//				if (getDiffValue(x, y) > maxDiffValue) {
//					maxDiffValue = getDiffValue(x, y);
//				}
			}
		}
		
		dt = 0.1 * 0.5 * dx * dx / maxDiffValue;
				
	}

}
