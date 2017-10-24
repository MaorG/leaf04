package leaf04;

import repast.simphony.space.continuous.WrapAroundBorders;
import repast.simphony.valueLayer.IGridValueLayer;
import expr.*;

public class ValueLayerDiffuserHeterogeneous extends CustumValueLayerDiffuser {
	protected IGridValueLayer diffusionLayer;
	protected String diffusionExpressionStr;
	protected Expr diffusionExpression;


	protected transient WrapAroundBorders borders;

	public ValueLayerDiffuserHeterogeneous(IGridValueLayer valueLayer,
			double evaporationConst, IGridValueLayer diffusionLayer,
			boolean toroidal) {
		this.evaporationConst = evaporationConst;
		this.diffusionConst = 0;
		this.toroidal = toroidal;

		setValueLayer(valueLayer);
		setDiffusionLayer(diffusionLayer);
	}

	public ValueLayerDiffuserHeterogeneous(IGridValueLayer valueLayer,
			double evaporationConst, double diffusionConst, boolean toroidal) {
		super(valueLayer, evaporationConst, diffusionConst, toroidal);
	}

	public void setDiffusionExpression(String expression) {
		this.diffusionExpressionStr = expression;
		try {
			diffusionExpression = Parser.parse(diffusionExpressionStr);
		} catch (SyntaxException e) {
			System.err.println(e.explain());
			e.printStackTrace();
			return;
		}
	}

	public void setDiffusionLayer(IGridValueLayer diffusionLayer) {
		// exceptions?

		this.diffusionLayer = diffusionLayer;

	}

	@Override
	public void determineMaxStableDt() {

		// deltaT <= 0.5 * deltaX^2 / alpha
		
		int width = (int) diffusionLayer.getDimensions().getWidth();
		int height = (int) diffusionLayer.getDimensions().getHeight();

		
		double maxDiffValue = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (getDiffValue(x, y) > maxDiffValue) {
					maxDiffValue = getDiffValue(x, y);
				}
			}
		}
		
		dt = 0.5 * dx * dx / maxDiffValue;
		
	}

	@Override
	protected void computeVals() {
		// this is being based on
		// http://www.mathcs.sjsu.edu/faculty/rucker/capow/santafe.html
		int size = valueLayer.getDimensions().size();

		if (size == 2) {
			int width = (int) valueLayer.getDimensions().getWidth();
			int height = (int) valueLayer.getDimensions().getHeight();

			double[][] oldVals = new double[width + 2][height + 2];
			double[][] newVals = new double[width][height];
			double[][] diffVals = new double[width + 2][height + 2];

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

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					double oldVal = getValue(x, y);

					double duE = oldVals[x + 2][y + 1] - oldVal;
					double duN = oldVals[x + 1][y + 2] - oldVal;
					double duW = oldVals[x][y + 1] - oldVal;
					double duS = oldVals[x + 1][y] - oldVal;

					double localDiff = diffVals[x + 1][y + 1];
					double diffE = 0.5 * (diffVals[x + 2][y + 1] + localDiff);
					double diffN = 0.5 * (diffVals[x + 1][y + 2] + localDiff);
					double diffW = 0.5 * (diffVals[x][y + 1] + localDiff);
					double diffS = 0.5 * (diffVals[x + 1][y] + localDiff);

					// compute the weighted avg, those directly
					// north/south/east/west
					// are given 4 times the weight of those on a diagonal
					double weightedAvgDeltaDiff = (duE * diffE + duW * diffW
							+ duN * diffN + duS * diffS) * 0.25;

					// apply the diffusion and evaporation constants
					double newVal = oldVal +  (dt / (dx*dx)) * weightedAvgDeltaDiff;
					
					newVal = newVal * evaporationConst;

					// bring the value into [min, max]
					newVals[x][y] = constrainByMinMax(newVal);

				}
			}
			computedVals = newVals;
		}
	}


	protected double getDiffValue(double... coords) {
		if (toroidal) {
			if (borders == null) {
				borders = new WrapAroundBorders();
				borders.init(valueLayer.getDimensions());
			}
			// use the wrap around borders class to set this up
			borders.transform(coords, coords);
		} else if (inBounds(coords) == 0.0) {
			return 0.0;
		}
		Variable X = Variable.make("X");
		X.setValue(diffusionLayer.get(coords));
		return diffusionExpression.value();
	}

}
