package leaf04;

import repast.simphony.space.continuous.WrapAroundBorders;
import repast.simphony.valueLayer.IGridValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;

public class ValueLayerDiffuserInstant extends CustumValueLayerDiffuser {

	
	public ValueLayerDiffuserInstant(IGridValueLayer valueLayer, double evaporationConst,
			double diffusionConst, boolean toroidal) {
		super( valueLayer, evaporationConst, diffusionConst, toroidal);
	}
	
	@Override
	public void determineMaxStableDt() {


	}

	@Override
	protected void computeVals() {

		int size = valueLayer.getDimensions().size();

		if (size == 2) {
			int width = (int) valueLayer.getDimensions().getWidth();
			int height = (int) valueLayer.getDimensions().getHeight();

			double[][] newVals = new double[width][height];

			double sum = 0;
			double num = width*height;

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					sum += getValue(x,y);
				}
			}
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					newVals[x][y] = sum / num;
				}
			}
			computedVals = newVals;
		}
	}


	@Override
	public void diffuse() {

		computeVals();
		int size = valueLayer.getDimensions().size();
		if (size == 1) {
			double[] newVals = (double[]) computedVals;
			for (int x = 0; x < newVals.length; x++) {
				valueLayer.set(newVals[x], x);
			}
		} else if (size == 2) {
			double[][] newVals = (double[][]) computedVals;
			for (int x = 0; x < newVals.length; x++) {
				for (int y = 0; y < newVals[0].length; y++) {
					valueLayer.set(newVals[x][y], x, y);
				}
			}
		} else {
			double[][][] newVals = (double[][][]) computedVals;
			for (int x = 0; x < newVals[0].length; x++) {
				for (int y = 0; y < newVals[0][0].length; y++) {
					for (int z = 0; z < newVals.length; z++) {
						valueLayer.set(newVals[x][y][z], x, y, z);
					}
				}
			}
		}
	}


}
