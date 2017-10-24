package leaf04;

import java.util.Map;

import leaf04.utils.OutputManager;
import cern.jet.random.Uniform;
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

public class AttachingBacteria extends Bacteria {

	public String sensingSolute;
	public Double attachAboveConc;
	public Double detachBelowConc;
	public Double nghRadius;
	public Double attachAboveAmount;
	public Double attachAboveAmountChance;
	public Double detachBelowAmount;
	public Double attachRandom;
	public Double detachRandom;

	public Double attachRandomPerTimeStep;
	public Double detachRandomPerTimeStep;
	
	public Double attachInit;
	public Double attachBirth;
	public Double detachBirth;
	public String attachAboveAmountExpressionStr;
	public Expr attachAboveAmountExpression;
	public String speedExpressionStr;
	public Expr speedExpression;
	
	public AttachingBacteria(String speciesName) {
		super(speciesName);
		speciesClassName = "AttachingBacteria";
	}

	@Override
	public void init() {
		super.init();

		
		attached = false;
		if (attachInit != null) {
			double rnd = RandomHelper.nextDoubleFromTo(0.0, 1.0);
			if (attachInit > rnd) {
				attached = true;
			} else {
				attached = false;
			}
		}
		//checkAttachDetach();
	}

	public void initAgentProperties(Context<PhysicalAgent> context) {
		String temp;  

		// Context<PhysicalAgent> context =
		// ContextUtils.getContext((Object)this);
		Zoo zoo = ((Leaf04Context) context).zoo;

		Map<String, Map<String, String>> speciesDefaultProperties = zoo.speciesDefaultProperties;

		sensingSolute = speciesDefaultProperties.get(speciesName).get(
				"sensingSolute");
		temp = speciesDefaultProperties.get(speciesName).get("attachAboveConc");
		if (temp != null) {
			attachAboveConc = Double.parseDouble(temp);
		}
		else {
			attachAboveConc = null;
		}

		temp = speciesDefaultProperties.get(speciesName).get("detachBelowConc");
		if (temp != null) {
			detachBelowConc = Double.parseDouble(temp);
		}
		else {
			detachBelowConc = null;
		}

		temp = speciesDefaultProperties.get(speciesName).get("nghRadius");
		if (temp != null) {
			nghRadius = Double.parseDouble(temp);
		}
		else {
			nghRadius = null;
		}

		temp = speciesDefaultProperties.get(speciesName).get("attachAboveAmount");
		if (temp != null) {
			attachAboveAmount = Double.parseDouble(temp);
		}
		else {
			attachAboveAmount = null;
		}
		

		temp = speciesDefaultProperties.get(speciesName).get("attachAboveAmountChance");

		if (temp != null) {
			attachAboveAmountChance = Double.parseDouble(temp);
		}
		else {
			attachAboveAmountChance = 1.0;
		}
		
		
		
		temp = speciesDefaultProperties.get(speciesName).get("attachRandom");
		if (temp != null) {
			attachRandom = Double.parseDouble(temp);
		}
		else {
			attachRandom = null;
		}

		temp = speciesDefaultProperties.get(speciesName).get("detachRandom");
		if (temp != null) {
			detachRandom = Double.parseDouble(temp);
		}
		else {
			detachRandom = null;
		}
		
		temp = speciesDefaultProperties.get(speciesName).get("attachBirth");
		if (temp != null) {
			attachBirth = Double.parseDouble(temp);
		}
		else {
			attachBirth = null;
		}

		temp = speciesDefaultProperties.get(speciesName).get("detachBirth");
		if (temp != null) {
			detachBirth = Double.parseDouble(temp);
		}
		else {
			detachBirth = null;
		}
		
		temp = speciesDefaultProperties.get(speciesName).get("attachInit");
		if (temp != null) {
			attachInit = Double.parseDouble(temp);
		}
		else {
			attachInit = null;
		}
		
		temp = speciesDefaultProperties.get(speciesName).get("attachAboveAmountExpression");
		if (temp != null) {
			attachAboveAmountExpressionStr = temp;
			try {
				attachAboveAmountExpression = Parser.parse(attachAboveAmountExpressionStr);
			} catch (SyntaxException e) {
				System.err.println(e.explain());
				e.printStackTrace();
				return;
			}
		}
		else {
			attachAboveAmountExpressionStr = null;
			attachAboveAmountExpression = null;
		}
		
		temp = speciesDefaultProperties.get(speciesName).get("speedExpression");
		if (temp != null) {
			speedExpressionStr = temp;
			try {
				speedExpression = Parser.parse(speedExpressionStr);
			} catch (SyntaxException e) {
				System.err.println(e.explain());
				e.printStackTrace();
				return;
			}
		}
		else {
			speedExpressionStr = null;
			speedExpression = null;
		}
		
		
		attachRandomPerTimeStep = 1 - Math.pow(1-attachRandom, stepTime);
		detachRandomPerTimeStep = 1 - Math.pow(1-detachRandom, stepTime);
		
		super.initAgentProperties(context);
	}

	@SuppressWarnings("unchecked")
	@ScheduledMethod(start = 1, interval = 1, priority = 2)
	public void step() {

		checkAttachDetach();
		

	}
	
	public void checkAttachDetach() {
		
		Boolean oldAttachState = attached;


		
		checkAttachDetachBySolute();
		if (attached != oldAttachState) {
			reportAttachDetach("Solute");
			return;
		}
		
		checkAttachDetachByNgh();
		if (attached != oldAttachState) {
			reportAttachDetach("Ngh");
			return;
		}
		
		checkAttachDetachByRandom();
		if (attached != oldAttachState) {
			reportAttachDetach("Random");
			return;
		}
	}
	
	@SuppressWarnings("unchecked")
	void reportAttachDetach(String strSuffix) {
		Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
		String eventStr = null;
		if (attached) {
			eventStr = this.getGeo() + ", " + "attach" + strSuffix;
		}
		else {
			eventStr = this.getGeo() + ", " + "detach" + strSuffix;
		}
		((OutputManager) ((Leaf04Context)context).getOutputManager()).addEventStringToLog(eventStr);
	}
	
	public int getNeighborsInRadius(double radius) {
		
		@SuppressWarnings("unchecked")
		Context<Object> context = ContextUtils.getContext((Object)this);
		GridWithin<Object> query = new GridWithin<Object>(context, this, radius*gridScaleYInv);
		
		
		int count = 0;
		for (Object o : query.query()){
			PhysicalAgent neigh = (PhysicalAgent)o;
			
			if (! neigh.attached) {
				continue;
			}
			
			NdPoint q = space.getLocation(this);
			NdPoint p = space.getLocation(neigh);

			Dimensions dims = this.space.getDimensions();
			double w = dims.getWidth(); 
			double h = dims.getHeight();
			
			double qpX = q.getX() - p.getX();
			if (2.0f * Math.abs(qpX) > w) {
				if (qpX > 0) qpX = qpX - w;
				else qpX = qpX + w;
			}
			double qpY = q.getY() - p.getY();
			if (2.0f * Math.abs(qpY) > h) {
				if (qpY > 0) qpY = qpY - h;
				else qpY = qpY + h;
			}			

			double dsqr = qpX*qpX + qpY*qpY;
			
			
			if (dsqr < radius * radius) {
				count++;
			}
		}
		
		return count;
	}

	
	public void checkAttachDetachByNgh() {
	
		if (nghRadius != null) {
			if (attachAboveAmountExpression != null) {
				checkAttachDetachByNghExpr();
			} 
			else {
				checkAttachDetachByNghThresh();
			}
		}
	}
		
	public void checkAttachDetachByNghExpr() {
		
		if (attached == false) {
			int count = getNeighborsInRadius(nghRadius);
			
			double rnd = RandomHelper.nextDoubleFromTo(0.0, 1.0);
			
			Variable X = Variable.make("X");
	
			X.setValue(count);
			
			Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
			Variable T = Variable.make("T");
			double ticks = ((Leaf04Context) context).getTicks();
			T.setValue(ticks);
			
			double chance = attachAboveAmountExpression.value();
			
			double chancePerTimeStep = 1 - Math.pow(1-chance, stepTime);
	
			if (rnd < chancePerTimeStep) {
				attached = true;
			}
		}		
		
		
	}

	public void checkAttachDetachByNghThresh() {
		@SuppressWarnings("unchecked")
		
		Context<Object> context = ContextUtils.getContext((Object)this);
		
		double rnd = RandomHelper.nextDoubleFromTo(0.0, 1.0);

		
		
		if (attached == false && attachAboveAmountChance != null && attachAboveAmountChance > rnd) {
			// todo - check if query can be smaller
			
			int count = getNeighborsInRadius(nghRadius);
			
		
			
			if (attachAboveAmount != null && attachAboveAmount < count) {
				attached = true;
			}
		}



//		if (detachBelowAmount != null && detachBelowAmount >  count) {
//			attached = false;
//		}

		
	}
	
	public void checkAttachDetachByRandom() {
		
		double rnd = RandomHelper.nextDoubleFromTo(0.0, 1.0);
		
		if (attached == true) {
			if (detachRandom != null && detachRandomPerTimeStep > rnd) {
				attached = false;
			}
		}	
		else {
			if (attachRandom != null && attachRandomPerTimeStep > rnd) {
				attached = true;
			}
		}
		
	
	}
	
	public void checkAttachDetachBySolute() {


		if (sensingSolute != null) {
			@SuppressWarnings("unchecked")
			Context<Object> context = ContextUtils.getContext((Object)this);
			GridValueLayer someValueLayer = (GridValueLayer) context.getValueLayer(sensingSolute);



			NdPoint myPoint = space.getLocation(this);
			myPoint = space.getLocation(this);
			double concVal = someValueLayer.get((int) (myPoint.getX()*gridScaleXInv), (int) (myPoint.getY()*gridScaleYInv));

			if (attachAboveConc != null && concVal > attachAboveConc) {
				attached = true;
			}
			if (detachBelowConc != null && concVal < detachBelowConc) {
				attached = false;
			}

		}

	}
	
	public void checkAttachDetachBirth() {

		double rnd = RandomHelper.nextDoubleFromTo(0.0, 1.0);
		if (attached == false) {
			if (attachBirth != null && attachBirth > rnd) {
				attached = true;
			}
		}
		else {
			if (detachBirth != null && detachBirth > rnd) {
				attached = false;
			}
		}
	}



	@SuppressWarnings("unchecked")
	@ScheduledMethod ( start = 1, interval = 1, priority = 2)
	public void move() {
		if (!attached) {
			double w = space.getDimensions().getWidth();
			double h = space.getDimensions().getHeight();

			Uniform ur = RandomHelper.createUniform();

			double angle = RandomHelper.nextDoubleFromTo(0.0, 2.0*Math.PI);
			
			double r;
			if (speedExpressionStr == null) {
				r = RandomHelper.nextDoubleFromTo(0.0, 10 * (w + h));
			}
			else {
				Context<Object> context = ContextUtils.getContext((Object)this);
				Variable W = Variable.make("W");
				GridValueLayer waterLevelLayer = (GridValueLayer) context.getValueLayer("waterLevel");
				NdPoint myPoint = space.getLocation(this);
				myPoint = space.getLocation(this);
				double wl = waterLevelLayer.get((int) (myPoint.getX()*gridScaleXInv), (int) (myPoint.getY()*gridScaleYInv));
				W.setValue(wl); 
		
				double speed = speedExpression.value();
				
				r = RandomHelper.nextDoubleFromTo(0, speed);

			}
			moveByAngleAndDist(angle,r);

		}



	}
	
	@Override
	@SuppressWarnings("unchecked")
	@ScheduledMethod ( start = 1, interval = 1, priority = 2)
	public void reproduce() {
		if (this.mass > divMass) {
			Bacteria clone = super.doReproduce();
			
			clone.attached = this.attached;
			
			this.checkAttachDetachBirth();
			((AttachingBacteria) clone).checkAttachDetachBirth();
		}
	}
}
