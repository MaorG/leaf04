package leaf04;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;

public class CODStyle implements ValueLayerStyleOGL {
	  
	  private ValueLayer layer;
	  private Map<Integer, Color> colorMap = new HashMap<Integer, Color>();
	  
	  public CODStyle() {
	    for (int i = 0; i < 256; i++) {
	      colorMap.put(i, new Color(i / 256.0f, 0f, 0f, 0.5f));
	    }
	  }

	  /* (non-Javadoc)
	   * @see repast.simphony.visualizationOGL2D.ValueLayerStyleOGL#getCellSize()
	   */
	  public float getCellSize() {
	    return 10;
	  }

	  /* (non-Javadoc)
	   * @see repast.simphony.visualizationOGL2D.ValueLayerStyleOGL#getColor(double[])
	   */
	  public Color getColor(double... coordinates) {
	    double val = layer.get(coordinates);
	    Color color = colorMap.get((int)(val * 20 * 256));
	    if (color == null) {
	      //System.out.printf("Trying to get color for %d%n", (int)(val * 256));
	      color = Color.RED;
	    }
	    return color;
	  }

	  /* (non-Javadoc)
	   * @see repast.simphony.visualizationOGL2D.ValueLayerStyleOGL#init(repast.simphony.valueLayer.ValueLayer)
	   */
	  public void init(ValueLayer layer) {
	    this.layer = layer;
	  }
	}

