package leaf04;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

import repast.simphony.visualizationOGL2D.StyleOGL2D;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;

public class BacteriaStyle implements StyleOGL2D<PhysicalAgent> {

	private Font font1, font2;
	private ShapeFactory2D shapeFactory;

	public BacteriaStyle() {
		font1 = new JLabel().getFont();
		font2 = new Font("SansSerif", Font.BOLD, 20);
	}

	public void init(ShapeFactory2D shapeFactory) {
		this.shapeFactory = shapeFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getBorderColor(java.lang
	 * .Object)
	 */
	public Color getBorderColor(PhysicalAgent object) {
//		return Color.black;
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getBorderSize(java.lang
	 * .Object)
	 */
	public int getBorderSize(PhysicalAgent object) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getColor(java.lang.Object)
	 */
	public Color getColor(PhysicalAgent object) {
		return object.getColor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getRotation(java.lang.Object
	 * )
	 */
	public float getRotation(PhysicalAgent object) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getScale(java.lang.Object)
	 */
	public float getScale(PhysicalAgent object) {
		return 2.0f * (float)((PhysicalAgent)object).radius;
//		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getVSpatial(java.lang.Object
	 * , saf.v3d.scene.VSpatial)
	 */
	public VSpatial getVSpatial(PhysicalAgent object, VSpatial spatial) {
		if (spatial == null) {
			VSpatial vs = shapeFactory.createCircle((float)((PhysicalAgent)object).radius, 16); 
			return vs;
		}
		return spatial;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getLabel(java.lang.Object)
	 */
	public String getLabel(PhysicalAgent object) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getLabelColor(java.lang
	 * .Object)
	 */
	public Color getLabelColor(PhysicalAgent object) {
		return Color.WHITE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getLabelFont(java.lang.
	 * Object)
	 */
	public Font getLabelFont(PhysicalAgent object) {
		if (Math.random() < .5) {
			return font1;
		} else {
			return font2;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getLabelPosition(java.lang
	 * .Object)
	 */
	public Position getLabelPosition(PhysicalAgent object) {
		return Position.SOUTH;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getLabelXOffset(java.lang
	 * .Object)
	 */
	public float getLabelXOffset(PhysicalAgent object) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getLabelYOffset(java.lang
	 * .Object)
	 */
	public float getLabelYOffset(PhysicalAgent object) {
		// TODO Auto-generated method stub
		return 0;
	}
}


