package ai.boxes;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author: rage
 * @date: 19.12.11 21:03
 */
public class TreasureBox45 extends Fighter
{
	public TreasureBox45(NpcInstance actor)
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
		if( i0 < 7662 )
		{
			getActor().dropItem(player, 736, 5);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4789 )
		{
			getActor().dropItem(player, 1061, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2554 )
		{
			getActor().dropItem(player, 737, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7662 )
		{
			getActor().dropItem(player, 1539, 5);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3831 )
		{
			getActor().dropItem(player, 8625, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3303 )
		{
			getActor().dropItem(player, 8631, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6385 )
		{
			getActor().dropItem(player, 8637, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6385 )
		{
			getActor().dropItem(player, 8636, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4404 )
		{
			getActor().dropItem(player, 8630, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5321 )
		{
			getActor().dropItem(player, 8624, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5894 )
		{
			getActor().dropItem(player, 10260, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5894 )
		{
			getActor().dropItem(player, 10261, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5894 )
		{
			getActor().dropItem(player, 10262, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5894 )
		{
			getActor().dropItem(player, 10263, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5894 )
		{
			getActor().dropItem(player, 10264, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5894 )
		{
			getActor().dropItem(player, 10265, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5894 )
		{
			getActor().dropItem(player, 10266, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5894 )
		{
			getActor().dropItem(player, 10267, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5894 )
		{
			getActor().dropItem(player, 10268, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8186 )
		{
			getActor().dropItem(player, 5593, 9);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7367 )
		{
			getActor().dropItem(player, 5594, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 737 )
		{
			getActor().dropItem(player, 5595, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5894 )
		{
			getActor().dropItem(player, 10269, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6226 )
		{
			getActor().dropItem(player, 10137, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6226 )
		{
			getActor().dropItem(player, 10138, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8301 )
		{
			getActor().dropItem(player, 1538, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3113 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2491 )
		{
			getActor().dropItem(player, 5577, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2491 )
		{
			getActor().dropItem(player, 5578, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2491 )
		{
			getActor().dropItem(player, 5579, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1437 )
		{
			getActor().dropItem(player, 70, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1080 )
		{
			getActor().dropItem(player, 21747, 1);
		}
	}
}