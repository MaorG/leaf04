package leaf04;

import java.util.Map;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import expr.Variable;
import repast.simphony.context.Context;
import repast.simphony.query.space.grid.GridWithin;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;

public class DensityBacteria extends AttachingBacteria {

	protected Double densityRadius;
	protected String densityExpressionStr;
	protected Expr densityExpression;
	
	public DensityBacteria(String speciesName) {
		super(speciesName);
		speciesClassName = "DensityBacteria";
		attached = false;
	}
	
	public void initAgentProperties(Context<PhysicalAgent> context) {
		String temp;  

		Zoo zoo = ((Leaf04Context) context).zoo;

		Map<String, Map<String, String>> speciesDefaultProperties = zoo.speciesDefaultProperties;
		
		temp = speciesDefaultProperties.get(speciesName).get("densityRadius");
		if (temp != null) {
			densityRadius = Double.parseDouble(temp);
		}
		else {
			densityRadius = null;
		}
		
		temp = speciesDefaultProperties.get(speciesName).get("densityExpression");
		if (temp != null) {
			densityExpressionStr = temp;
		}
		else {
			densityExpressionStr = null;
		}
		
		try {
			densityExpression = Parser.parse(densityExpressionStr);
		} catch (SyntaxException e) {
			System.err.println(e.explain());
			e.printStackTrace();
			return;
		}
		
		super.initAgentProperties(context);
	}

	public double getMuFactor() {
		if (densityRadius == null) {
			return 1.0;
		}
		int count = getNeighborsInRadius(densityRadius);
		
		Variable X = Variable.make("X");
		X.setValue(count);
				
		double factor = densityExpression.value();
		
		return factor;
	}
	

}
