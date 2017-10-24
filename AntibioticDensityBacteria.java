package leaf04;

import java.util.Map;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import expr.Variable;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;

public class AntibioticDensityBacteria extends AttachingBacteria {

	protected Double densityRadius;
	protected String densityExpressionStr;
	protected Expr densityExpression;
	
	public AntibioticDensityBacteria(String speciesName) {
		super(speciesName);
		speciesClassName = "AntibioticDensityBacteria";
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
			try {
				densityExpression = Parser.parse(densityExpressionStr);
			} catch (SyntaxException e) {
				System.err.println(e.explain());
				e.printStackTrace();
				return;
			}
		}
		else {
			densityExpressionStr = null;
			densityExpression = null;
		}
		

		
		super.initAgentProperties(context);
	}

	@SuppressWarnings("unchecked")
	@ScheduledMethod ( start = 1, interval = 1, priority = 2)
	public void antibiotic() {
		
//		if (densityRadius == null) {
//			return;
//		}
//		int count = 0;
//		if (attached) {
//			count = getNeighborsInRadius(densityRadius);
//		}
//		//double density = count / ( Math.PI * (densityRadius * densityRadius));
//		Variable X = Variable.make("X");
//		Variable W = Variable.make("W");
//
//		//X.setValue(density);
//		X.setValue(count);	
//		
//		Context<Object> context = ContextUtils.getContext((Object)this);
//		GridValueLayer waterLevelLayer = (GridValueLayer) context.getValueLayer("waterLevel");
//		NdPoint myPoint = space.getLocation(this);
//		myPoint = space.getLocation(this);
//		double wl = waterLevelLayer.get((int) (myPoint.getX()*gridScaleXInv), (int) (myPoint.getY()*gridScaleYInv));
//		W.setValue(wl); 
//
//
//		double chance = densityExpression.value();
//		double rnd = RandomHelper.nextDoubleFromTo(0.0, 1.0);
//		
//		if (rnd < chance) {
//			 die();
//		}
		
		
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
