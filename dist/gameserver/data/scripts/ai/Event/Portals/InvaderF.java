package ai.Event.Portals;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.ItemFunctions;

//By Evil_dnk

public class InvaderF extends Fighter
{
	private int CHEST = 40004;

	public InvaderF(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(killer != null && killer.isPlayer())
		{
			if(Rnd.chance(70))
			{
				ItemInstance keyitem = ItemFunctions.createItem(10017);
				keyitem.dropToTheGround(getActor(), getActor().getLoc());
			}
		}
		super.onEvtDead(killer);
	}
}