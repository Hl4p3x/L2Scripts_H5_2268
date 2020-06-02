package l2s.gameserver.templates.fakeplayer.actions;

import org.dom4j.Element;

import l2s.gameserver.ai.FakeAI;

/**
 * @author Bonux
**/
public class TeleportToClosestTownAction extends AbstractAction
{
	public TeleportToClosestTownAction(double chance)
	{
		super(chance);
	}

	@Override
	public boolean performAction(FakeAI ai)
	{
		ai.getActor().teleToClosestTown();
		return true;
	}

	public static TeleportToClosestTownAction parse(Element element)
	{
		double chance = element.attributeValue("chance") == null ? 100. : Double.parseDouble(element.attributeValue("chance"));
		return new TeleportToClosestTownAction(chance);
	}
}