package leaf04;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.query.space.grid.GridWithin;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;











/// TODO perhaps should be wrapped into parser class
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

import java.io.*;

import leaf04.utils.ParserUtils;


public class PhysicalAgent implements Agent {
	
	public String speciesName;
	public String speciesClassName;
	
	protected ContinuousSpace <PhysicalAgent> space ;
	protected Grid <PhysicalAgent> grid ;
	
	public Boolean attached;
	public double mass;	
	public double deltaMass;
	public double oldMass;
	public double radius;
	protected double fX;
	protected double fY;
	
	// here?
	public double stepTime;
	
	//TODO: perhaps use some properties class
	protected static double gridScaleXInv; 
	protected static double gridScaleYInv; 
	
	protected Map<String, Boolean> reactionNamesMap = new HashMap<String, Boolean>();
	protected Map<String, String> speciesProperties;
	protected Color color;
	public double tempDeltaAgentMass;
	
	public void setReactionNamesMap(Map<String, Boolean> map) {
		
		for (Entry<String, Boolean> entry : map.entrySet()) {
		    String key = entry.getKey();
		    Boolean value = entry.getValue();
		    this.reactionNamesMap.put(key, value);

		}
	}
	
	public Boolean hasReaction(String reactionName) {
		if(!reactionNamesMap.containsKey(reactionName))
			return false;
		Boolean temp = reactionNamesMap.get(reactionName); 
		return temp;
	}

	@SuppressWarnings("unchecked")
	public PhysicalAgent() {}
	
	public PhysicalAgent(String speciesName) {
		this.speciesName = speciesName; 
		speciesClassName = "PhysicalAgent";

		
		// TODO move all this to static init methods in the zoo
//		Context<Object> context = ContextUtils.getContext((Object)this);
//		space = (ContinuousSpace<PhysicalAgent>) context.getProjection("space");;
//		grid = (Grid<PhysicalAgent>) context.getProjection("grid");
	}
	
	public void init() {
		attached = true;
		gridScaleXInv = grid.getDimensions().getWidth() / space.getDimensions().getWidth();
		gridScaleYInv = grid.getDimensions().getHeight() / space.getDimensions().getHeight();
		oldMass = 0;
	}
	
	public void setMass(double mass) {
		this.mass = mass;
		this.radius = Math.sqrt(this.mass / Math.PI) * 1e6;
	}

	public void setInitialMass(double mass) {
		this.mass = mass;
		this.radius = Math.sqrt(this.mass / Math.PI) * 1e6;
	}

	
	@SuppressWarnings("unchecked")
	//@ScheduledMethod ( start = 1, interval = 1, priority = 3)
	public void calculateForces2() {
		Context<Object> context = ContextUtils.getContext((Object)this);

		int initialNeighborDistance = 5;
		GridWithin<Object> query = new GridWithin<Object>(context, this, 2.0 + initialNeighborDistance*gridScaleYInv);
		
		// instead of computing distance twice, use a map with object as key?
		this.fX = 0;
		this.fY = 0;
		
		if (!attached) {
			return;
		}
		
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

			double d = Math.pow(qpX*qpX + qpY*qpY, 0.5);

			if (d < this.radius + neigh.radius) {
				double hh = (this.radius + neigh.radius) - d;
				
				double F = (1 - Math.pow (hh, 1.5)) / this.radius;

			    double Fxa = (F * qpX / d);
			    double Fya = (F * qpY / d);

			    this.fX = this.fX + Fxa;
			    this.fY = this.fY + Fya;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	//@ScheduledMethod ( start = 1, interval = 1, priority = 3)
	public void calculateForces() {
		Context<Object> context = ContextUtils.getContext((Object)this);

		int initialNeighborDistance = 5;
		GridWithin<Object> query = new GridWithin<Object>(context, this, 1.0 + initialNeighborDistance);
		
		// instead of computing distance twice, use a map with object as key?
		this.fX = 0;
		this.fY = 0;
		
		for (Object o : query.query()){
			PhysicalAgent neigh = (PhysicalAgent)o;
			
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

			double d = Math.pow(qpX*qpX + qpY*qpY, 0.5);

			if (d < this.radius + neigh.radius) {
				double hh = (this.radius + neigh.radius) - d;
				
				double F = Math.pow(d, 0.5) * Math.pow (hh, 1.5);

			    double Fxa = (F * qpX / d);
			    double Fya = (F * qpY / d);

			    this.fX = this.fX + Fxa;
			    this.fY = this.fY + Fya;
			}
		}
	}

	public void calculateForcesOld() {
		Context<Object> context = ContextUtils.getContext((Object)this);

		int initialNeighborDistance = 5;
		GridWithin<Object> query = new GridWithin<Object>(context, this, 1.0 + initialNeighborDistance*gridScaleYInv);

		ArrayList<Object> foundOverlapping = new ArrayList<Object>();
		
		// instead of computing distance twice, use a map with object as key?
		
		for (Object o : query.query()){
			PhysicalAgent neigh = (PhysicalAgent)o;
			
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

			double d = Math.pow(qpX*qpX + qpY*qpY, 0.5);

			if (d < this.radius + neigh.radius) {
				foundOverlapping.add((Object)neigh);
			}
		}
		this.fX = 0;
		this.fY = 0;
		for (Object o : foundOverlapping){
			PhysicalAgent b = (PhysicalAgent)o;
			NdPoint q = space.getLocation(this);
			NdPoint p = space.getLocation(b);
			
			Dimensions dims = this.space.getDimensions();
			double w = dims.getWidth(); 
			double h = dims.getHeight();
			double qpX = q.getX() - p.getX();
			if (Math.abs(qpX) > 0.5f * w) {
				if (qpX > 0) qpX = qpX - w;
				else qpX = qpX + w;
			}
			double qpY = q.getY() - p.getY();
			
			if (Math.abs(qpY) > 0.5f * h) {
				if (qpY > 0) qpY = qpY - h;
				else qpY = qpY + h;
			}			

			double d = Math.pow(qpX*qpX + qpY*qpY, 0.5);
			double hh = (this.radius + b.radius) - d;
			
			double F = Math.pow(d, 0.5) * Math.pow (hh, 1.5);

		    double Fxa = (F * qpX / d);
		    double Fya = (F * qpY / d);

		    this.fX = this.fX + Fxa;
		    this.fY = this.fY + Fya;
		
		}
	}
	
	//@ScheduledMethod ( start = 1, interval = 1, priority = 2)
	protected void followForces(double coeff) {
		NdPoint myPoint = space.getLocation(this);
		double nextX = myPoint.getX() + (this.fX / this.mass) * coeff;
		double nextY = myPoint.getY() + (this.fY / this.mass) * coeff;
		space.moveTo(this, nextX, nextY);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) (myPoint.getX()*gridScaleXInv), (int) (myPoint.getY()*gridScaleYInv));
	}
	
	protected void followForces2(double coeff) {
		NdPoint myPoint = space.getLocation(this);
		double nextX = myPoint.getX() + (this.fX ) * coeff;
		double nextY = myPoint.getY() + (this.fY ) * coeff;
		space.moveTo(this, nextX, nextY);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) (myPoint.getX()*gridScaleXInv), (int) (myPoint.getY()*gridScaleYInv));
	}

	protected void moveByAngleAndDist(double angle, double dist) {
		NdPoint myPoint = space.getLocation(this);
		space.moveByVector(this, dist, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) (myPoint.getX()*gridScaleXInv), (int) (myPoint.getY()*gridScaleYInv));

	}
	
	
	public static String getGeoHeader() {
		return "species, X, Y, radius, deltaMass, attached, colorR, colorG, colorB, LineageId, history, neighbors, lastAttachDetach";  

		//return "species, X, Y, radius, deltaMass, attached, colorR, colorG, colorB, LineageId, history";  
	}
	
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
			"0" + ", " + 
			"0" + ", " +
			"0" + ", " +
			"0" + ", ";
			
	}

	public void initAgentProperties(Context<PhysicalAgent> context) {
		Zoo zoo = ((Leaf04Context) context).zoo;
		
		if (zoo.speciesColors.containsKey(speciesName)) {
			setColor(zoo.speciesColors.get(speciesName));
		}
		
	}

	void setColor(Color color) {
		this.color = color;
	}
	
	Color getColor() {
		return this.color;
		
	}


}
