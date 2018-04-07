package leaf04;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;
import leaf04.PhysicalAgent;
import leaf04.utils.OutputManager;
import leaf04.utils.ParserUtils;

public class Bacteria extends PhysicalAgent{
	
    public double initMass;
	public double divMass;
	public double deathMass;
	public long lineageId;
	public long history;
	public String deathClass;

	@SuppressWarnings("unchecked")
	public void initAgentProperties(Context<PhysicalAgent> context) {

		
		//Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
		Zoo zoo = ((Leaf04Context)context).zoo;
		
		Map<String, Map<String, String>> speciesDefaultProperties = zoo.speciesDefaultProperties;
		divMass = zoo.getRandomNormal(
				Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassMean")), 
				Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassSD")));
		divMass = Math.max(
					Math.min(
							divMass, 
							Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassMax"))), 
					Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassMin")));
		
		initMass = zoo.getRandomNormal(
				Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassMean")), 
				Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassSD")));
		initMass = Math.max(
					Math.min(
							initMass, 
							Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassMax"))), 
					Double.parseDouble(speciesDefaultProperties.get(speciesName).get("divMassMin")));
		initMass = initMass * 0.5;
		
		deathMass = zoo.getRandomNormal(
				Double.parseDouble(speciesDefaultProperties.get(speciesName).get("deathMassMean")), 
				Double.parseDouble(speciesDefaultProperties.get(speciesName).get("deathMassSD")));
		deathMass = Math.max(
					Math.min(
							deathMass, 
							Double.parseDouble(speciesDefaultProperties.get(speciesName).get("deathMassMax"))), 
					Double.parseDouble(speciesDefaultProperties.get(speciesName).get("deathMassMin")));
		
		
		deathClass = speciesDefaultProperties.get(speciesName).get(
				"deathClass");
		
		super.initAgentProperties(context);

	}

//	@Override
//	public void setInitialMass(double mass) {
//		this.mass = initMass;
//		this.radius = Math.sqrt(this.mass / Math.PI);
//	}
	
	public void setLineageId(long lineageId2) {
		this.lineageId = lineageId2;
	}
	
	public void initHistory() {
		this.history = 1;
	}
	
	public void updateHistory(long oldHistory, Boolean first) {
		this.history = oldHistory * 2;
		if (first) {
			this.history = this.history + 1; 
		}
	}
	
	@SuppressWarnings("unchecked")
	public Bacteria(String speciesName) {
		super(speciesName);
		speciesClassName = "Bacteria";
	}

	
	@SuppressWarnings("unchecked")
	@ScheduledMethod ( start = 1, interval = 1, priority = 2)
	public void reproduce() {
		if (this.mass > divMass) {
			doReproduce();
		}
	}
	
	@SuppressWarnings("unchecked")
	public Bacteria doReproduce() {
		Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
		
		// output to log
		String eventStr = this.getGeo() + ", " + "split";
		((OutputManager) ((Leaf04Context)context).getOutputManager()).addEventStringToLog(eventStr);
		
		Zoo zoo = ((Leaf04Context)context).zoo;

		Bacteria clone = (Bacteria) zoo.createAgent(speciesClassName, speciesName);
		clone.setMass(mass*0.5);
		context.add(clone);
		
		clone.setLineageId(this.lineageId);
		clone.updateHistory(this.history, false);
		this.updateHistory(this.history, true);

		setMass(this.mass * 0.5);
//		this.mass = this.mass * 0.5;
//		this.radius = Math.sqrt(this.mass / Math.PI);
		
		this.oldMass = oldMass * 0.5;
		clone.oldMass = oldMass * 0.5;

		
		
		NdPoint myPoint = space.getLocation(this);
		space.moveTo(clone, myPoint.getX(), myPoint.getY());
		double angle = (float) RandomHelper.nextDoubleFromTo(0.0, 2.0*Math.PI);
		this.moveByAngleAndDist(angle, this.radius * 0.3);
		clone.moveByAngleAndDist(angle + Math.PI, clone.radius * 0.3);
		
		

		
		return clone;
	}
	
	@SuppressWarnings("unchecked")
	@ScheduledMethod ( start = 1, interval = 1, priority = 2)
	public void tryDie() {
		if (this.mass < deathMass) {
			die();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void die() {

		Context<PhysicalAgent> context = ContextUtils.getContext((Object)this);
		String eventStr = this.getGeo() + ", " + "dead";
		((OutputManager) ((Leaf04Context)context).getOutputManager()).addEventStringToLog(eventStr);
		
		if (deathClass == null) {
			context.remove(this);
		}
		else {
			
			Zoo zoo = ((Leaf04Context)context).zoo;
			
			Bacteria clone = (Bacteria) zoo.createAgent("DeadBacteria", deathClass);
			clone.setMass(mass);
			context.add(clone);
			clone.setLineageId(this.lineageId);

			NdPoint myPoint = space.getLocation(this);
			space.moveTo(clone, myPoint.getX(), myPoint.getY());
			context.remove(this);
			

		}

		

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
	
	@Override
	public String getGeo() {
		NdPoint myPoint = space.getLocation(this);
		
		return 
			String.valueOf(
			speciesName + ", " + 
			myPoint.getX()) + ", " + 
			String.valueOf(myPoint.getY()) + ", " + 
			String.valueOf(this.radius) +  ", " + 
			String.valueOf(this.deltaMass) +  ", " + 
			String.valueOf(this.attached ? 1 : 0) +  ", " +
			String.valueOf(this.color.getRed()) +  ", " +
			String.valueOf(this.color.getGreen()) +  ", " +
			String.valueOf(this.color.getBlue()) +  ", " +
			String.valueOf(this.lineageId) +  ", " + 
			String.valueOf(this.history) + ", " + 
			String.valueOf(this.getNeighborsInRadius(10)) + ", " +
			"0";

	}

}
	

