package leaf04;

import java.awt.Color;
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

public class ComplicatedBacteria extends AntibioticDensityBacteria {

	protected String attachedOn;
	protected String attachedOff;
	
	public ComplicatedBacteria(String speciesName) {
		super(speciesName);
		speciesClassName = "ComplicatedBacteria";
	}

//	public double getStepTime() {
//		@SuppressWarnings("unchecked")
//		Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
//		return ((Leaf04Context) context).stepTime;
//	}
	
	public void initAgentProperties(Context<PhysicalAgent> context) {
		
		Zoo zoo = ((Leaf04Context) context).zoo;
		
		Map<String, Map<String, String>> speciesDefaultProperties = zoo.speciesDefaultProperties;

		attachedOn = speciesDefaultProperties.get(speciesName).get(
				"attachedOn");
		attachedOff = speciesDefaultProperties.get(speciesName).get(
				"attachedOff");
		
		super.initAgentProperties(context);
	}
	
	@SuppressWarnings("unchecked")
	@ScheduledMethod ( start = 1, interval = 1, priority = 100)
	public void checkReactions() {
		

		
		if (attached == true && attachedOn != null) {
			if(reactionNamesMap.containsKey(attachedOn)) {
				reactionNamesMap.put(attachedOn, true);
			}
			if(reactionNamesMap.containsKey(attachedOff)) {
				reactionNamesMap.put(attachedOff, false);
			}
		}
		if (attached == false && attachedOff != null) {
			if(reactionNamesMap.containsKey(attachedOff)) {
				reactionNamesMap.put(attachedOff, true);
			}
			if(reactionNamesMap.containsKey(attachedOn)) {
				reactionNamesMap.put(attachedOn, false);
			}
		}
		



	}
	


	@SuppressWarnings("unchecked")
	@ScheduledMethod ( start = 1, interval = 1, priority = 2)
	public void antibiotic() {
		if (densityRadius == null) {
			return;
		}
		int count = 0;
		if (attached) {
			count = getNeighborsInRadius(densityRadius);
		}
		//double density = count / ( Math.PI * (densityRadius * densityRadius));
		Variable X = Variable.make("X");
		Variable W = Variable.make("W");
		//X.setValue(density);
		X.setValue(count);	
		
		Context<Object> context = ContextUtils.getContext((Object)this);
		GridValueLayer waterLevelLayer = (GridValueLayer) context.getValueLayer("waterLevel");
		NdPoint myPoint = space.getLocation(this);
		myPoint = space.getLocation(this);
		double wl = waterLevelLayer.get((int) (myPoint.getX()*gridScaleXInv), (int) (myPoint.getY()*gridScaleYInv));
		W.setValue(wl); 

		double chancePerHour = densityExpression.value();
	
		double rnd = RandomHelper.nextDoubleFromTo(0.0, 1.0);
	
		double chancePerStep = 1 - Math.pow(1-chancePerHour, stepTime);
		
		
		if (rnd < chancePerStep) {
			 die();
		}
		
		
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
	
	Color getColor() {
		Color color =  this.color;
		
	
		if (!this.attached) {
			color = new Color(
					172,
					255,
					157);
		}
		return color;
		//return this.color;
	}

}
