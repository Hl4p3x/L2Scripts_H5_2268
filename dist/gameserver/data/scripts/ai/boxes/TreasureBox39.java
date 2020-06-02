package ai.boxes;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author: rage
 * @date: 19.12.11 21:02
 */
public class TreasureBox39 extends Fighter
{
	public TreasureBox39(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);

		if(killer == null)
			return;

		Player player = killer.getPlayer();
		if(player == null)
			return;

		int i0 = Rnd.get(10000);
		if( i0 < 6879 )
		{
			getActor().dropItem(player, 736, 7);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6019 )
		{
			getActor().dropItem(player, 1061, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 9630 )
		{
			getActor().dropItem(player, 737, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 10260, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 10261, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 10262, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 10263, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 10264, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 10265, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 10266, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 10267, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 10268, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6019 )
		{
			getActor().dropItem(player, 5593, 6);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 5594, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2889 )
		{
			getActor().dropItem(player, 10269, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8346 )
		{
			getActor().dropItem(player, 10134, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8346 )
		{
			getActor().dropItem(player, 10135, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8346 )
		{
			getActor().dropItem(player, 10136, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8346 )
		{
			getActor().dropItem(player, 1538, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3130 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2527 )
		{
			getActor().dropItem(player, 69, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 815 )
		{
			getActor().dropItem(player, 21747, 1);
		}
	}
}