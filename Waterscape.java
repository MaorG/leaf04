package leaf04;

import leaf04.utils.ParserUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import expr.Variable;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.valueLayer.GridValueLayer;

public class Waterscape {
	Expr waterLevelExpression;
	double minWaterLevel;

	@SuppressWarnings("unchecked")
	public Waterscape(Leaf04Context context, Node waterscapeNode) {
		Node patternNode = ParserUtils.getNodeByTagName(waterscapeNode, "pattern");
		
		Element e = (Element)patternNode;
		String patternName = e.getAttribute("name");


		Grid <PhysicalAgent> grid = (Grid<PhysicalAgent>) context.getProjection("grid");
		int width = grid.getDimensions().getWidth();
		int height = grid.getDimensions().getHeight();

		GridValueLayer terrainLayer = new GridValueLayer("terrain", 0.0, true, new WrapAroundBorders(), new int[]{(int)width, (int)height}, new int[]{0,0});
		context.addValueLayer(terrainLayer);
		
		if (patternName.equalsIgnoreCase("rects")) {
			
			double sqWidth = ParserUtils.getDoubleByName(patternNode, "width");
			double sqHeight = ParserUtils.getDoubleByName(patternNode, "height");
			double maxLevel = ParserUtils.getDoubleByName(patternNode, "maxLevel");
			double minLevel = ParserUtils.getDoubleByName(patternNode, "minLevel");


			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					terrainLayer.set(maxLevel, i,j);
					if ((i % sqWidth) < 3 || (j % sqHeight) < 3) {
						terrainLayer.set(minLevel, i,j);
					} 
					else { 
						terrainLayer.set(maxLevel, i,j);
					}
				}
			}
		}
		
		String waterLevelExpressionStr = ParserUtils.getStringByName(waterscapeNode, "waterLevel");
		minWaterLevel = ParserUtils.getDoubleByName(waterscapeNode, "minWaterLevel");
		
		try {
			waterLevelExpression = Parser.parse(waterLevelExpressionStr);
		} catch (SyntaxException ex) {
			System.err.println(ex.explain());
			ex.printStackTrace();
			return;
		}
		
		GridValueLayer waterLevelLayer = new GridValueLayer("waterLevel", 0.0, true, new WrapAroundBorders(), new int[]{(int)width, (int)height}, new int[]{0,0});
		context.addValueLayer(waterLevelLayer);
		
		// not necessary, should be called only be scheduler
		updateWaterLevel(context, 0);

	}
	
	@SuppressWarnings("unchecked")
	public void updateWaterLevel(Leaf04Context context, double t) {

		GridValueLayer terrainLayer = (GridValueLayer) context.getValueLayer("terrain");
		GridValueLayer waterLevelLayer = (GridValueLayer) context.getValueLayer("waterLevel");
		Grid <PhysicalAgent> grid = (Grid<PhysicalAgent>) context.getProjection("grid");
		int width = grid.getDimensions().getWidth();
		int height = grid.getDimensions().getHeight();
		
		
		Variable T = Variable.make("T");
		T.setValue(t);
		double waterLevel = waterLevelExpression.value();
		if (false && waterLevel != 0.0 ) 
			System.out.printf("T: %f, wl: %f\n", t, waterLevel);
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double waterDepth = Math.max(waterLevel - terrainLayer.get(i,j),minWaterLevel); 
				waterLevelLayer.set(waterDepth, i,j);
			}
		}
	}
}
