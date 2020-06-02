package l2s.gameserver.templates.fakeplayer.actions;

import org.dom4j.Element;

import l2s.gameserver.ai.FakeAI;

/**
 * @author Bonux
**/
public class FarmAction extends MoveAction
{
	public FarmAction(double chance)
	{
		super(chance);
	}

	@Override
	public boolean performAction(FakeAI ai)
	{
		if(ai.performFarm())
			return true;
		return false;
	}

	public static FarmAction parse(Element element)
	{
		double chance = element.attributeValue("chance") == null ? 100. : Double.parseDouble(element.attributeValue("chance"));
		return new FarmAction(chance);
	}
}