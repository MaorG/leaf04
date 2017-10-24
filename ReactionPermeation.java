package leaf04;

import leaf04.utils.ParserUtils;

import org.w3c.dom.Node;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import expr.Variable;
import repast.simphony.context.Context;
import repast.simphony.valueLayer.GridValueLayer;

public class ReactionPermeation extends Reaction {

	double concentrationInLeaf;
	protected String affectingSoluteName;
	double sourceConcentration;
	protected String diffusionExpressionStr;
	protected Expr diffusionExpression;
	
	
	public ReactionPermeation(Context<PhysicalAgent> context, Node reactionNode) {
		super(context, reactionNode);
		this.init(reactionNode);
	}

	@Override
	public void init (Node reactionNode)
	{
		super.init(reactionNode);
		
		soluteName = ParserUtils.getStringByName(reactionNode, "soluteName");
		affectingSoluteName = ParserUtils.getStringByName(reactionNode, "affectingSoluteName");
		
		
		
		sourceConcentration = ParserUtils.getDoubleByName(reactionNode, "sourceConcentration");
		diffusionExpressionStr = ParserUtils.getStringByName(reactionNode, "diffusionExpression");
		try {
			diffusionExpression = Parser.parse(diffusionExpressionStr);
		} catch (SyntaxException e) {
			System.err.println(e.explain());
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public void react(double dt) {
		
		GridValueLayer soluteValueLayer = (GridValueLayer) context.getValueLayer(soluteName);
		GridValueLayer affectingSoluteValueLayer = (GridValueLayer) context.getValueLayer(affectingSoluteName);

		
		int width = (int) soluteValueLayer.getDimensions().getWidth();
		int height = (int) soluteValueLayer.getDimensions().getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
			    double oldConc = soluteValueLayer.get(x, y);
			    
				Variable X = Variable.make("X");
				X.setValue(affectingSoluteValueLayer.get(x, y));
			    double localDiffVal = diffusionExpression.value();
 
			    double amountDiffused = dt * localDiffVal * (sourceConcentration - oldConc);
			    
			    soluteValueLayer.set(amountDiffused + oldConc, x, y);

			}
		}
	}

}
