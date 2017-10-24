package leaf04;

import repast.simphony.space.continuous.WrapAroundBorders;
import repast.simphony.valueLayer.IGridValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;

public class ValueLayerDiffuserLW extends ValueLayerDiffuser {

	  public ValueLayerDiffuserLW(IGridValueLayer valueLayer, double evaporationConst,
		      double diffusionConst, boolean toroidal) {
		    super( valueLayer, evaporationConst, diffusionConst, toroidal);
	  }
	  
	@Override
	protected void computeVals() {
		// this is being based on
		// http://www.mathcs.sjsu.edu/faculty/rucker/capow/santafe.html
		int size = valueLayer.getDimensions().size();
		

		if (size == 2) {
			 int width = (int) valueLayer.getDimensions().getWidth();
		      int height = (int) valueLayer.getDimensions().getHeight();

		      double[][] oldVals = new double[width+2][height+2];
		      double[][] newVals = new double[width][height];
		      for (int y = -1; y < height + 1; y++) {
			        for (int x = -1; x < width + 1; x++) {
			        	oldVals[x+1][y+1] =  getValue(x, y);
			        }
		      }
		      
		      int a = 0;
		      
		      for (int y = 0; y < height; y++) {
			        for (int x = 0; x < width; x++) {
				          double uE = oldVals[x+2][y+1];
				          double uN = oldVals[x+1][y+2];
				          double uW = oldVals[x][y+1];
				          double uS = oldVals[x+1][y];

				          double sum = (uE + uN + uW + uS);
				          double oldVal = oldVals[x+1][y+1];
//				          double delta = weightedAvg - oldVal;

				          //double newVal = (1.0 - 4.0 * diffusionConst) * oldVal + diffusionConst * sum;
				          double newVal = oldVal + diffusionConst * (sum - 4.0 * oldVal);
	
				          
				          
				          // double newVal = (oldVal + delta * diffusionConst) * evaporationConst;

				          
				          // bring the value into [min, max]
				          newVals[x][y] = constrainByMinMax(newVal);
				          
			        }
		      }
		      /*
		      for (int y = 0; y < height; y++) {
		        for (int x = 0; x < width; x++) {
		          // these are the neighbors that are directly north/south/east/west to
		          // the given cell 4 times those that are diagonal to the cell
		          double uE = getValue(x + 1, y);
		          double uN = getValue(x, y + 1);
		          double uW = getValue(x - 1, y);
		          double uS = getValue(x, y - 1);

		          // these are the neighbors that are diagonal to the given cell
		          // they are only weighted 1/4 of the ones that are
		          // north/south/east/west
		          // of the cell
//		          double uNE = getValue(x + 1, y + 1);
//		          double uNW = getValue(x - 1, y + 1);
//		          double uSW = getValue(x - 1, y - 1);
//		          double uSE = getValue(x + 1, y - 1);

		          // compute the weighted avg, those directly north/south/east/west
		          // are given 4 times the weight of those on a diagonal
		          double weightedAvg = (uE + uN + uW + uS) / 4.0;
//		          double weightedAvg = ((uE + uN + uW + uS) * 4 + (uNE + uNW + uSW + uSE)) / 20.0;

		          // apply the diffusion and evaporation constants
		          double oldVal = getValue(x, y);
		          double delta = weightedAvg - oldVal;

		          double newVal = (oldVal + delta * diffusionConst) * evaporationConst;

		          // bring the value into [min, max]
		          newVals[x][y] = constrainByMinMax(newVal);

		          // System.out.println("x: " + x + " y: " + y + "val: " + oldVal +
		          // " delta: "
		          // + delta + " d: " + newVals[x][y]);
				}
				
			}
			*/
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

