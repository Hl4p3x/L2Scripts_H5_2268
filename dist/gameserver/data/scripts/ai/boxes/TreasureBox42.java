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
public class TreasureBox42 extends Fighter
{
	public TreasureBox42(NpcInstance actor)
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
		if( i0 < 6668 )
		{
			getActor().dropItem(player, 736, 5);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4168 )
		{
			getActor().dropItem(player, 1061, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2223 )
		{
			getActor().dropItem(player, 737, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6668 )
		{
			getActor().dropItem(player, 1539, 5);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3334 )
		{
			getActor().dropItem(player, 8625, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2874 )
		{
			getActor().dropItem(player, 8631, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5557 )
		{
			getActor().dropItem(player, 8637, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5557 )
		{
			getActor().dropItem(player, 8636, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3832 )
		{
			getActor().dropItem(player, 8630, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4631 )
		{
			getActor().dropItem(player, 8624, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5129 )
		{
			getActor().dropItem(player, 10260, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5129 )
		{
			getActor().dropItem(player, 10261, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5129 )
		{
			getActor().dropItem(player, 10262, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5129 )
		{
			getActor().dropItem(player, 10263, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5129 )
		{
			getActor().dropItem(player, 10264, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5129 )
		{
			getActor().dropItem(player, 10265, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5129 )
		{
			getActor().dropItem(player, 10266, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5129 )
		{
			getActor().dropItem(player, 10267, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5129 )
		{
			getActor().dropItem(player, 10268, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7124 )
		{
			getActor().dropItem(player, 5593, 9);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6411 )
		{
			getActor().dropItem(player, 5594, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 642 )
		{
			getActor().dropItem(player, 5595, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5129 )
		{
			getActor().dropItem(player, 10269, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5418 )
		{
			getActor().dropItem(player, 10137, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5418 )
		{
			getActor().dropItem(player, 10138, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7223 )
		{
			getActor().dropItem(player, 1538, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2709 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2167 )
		{
			getActor().dropItem(player, 5577, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2167 )
		{
			getActor().dropItem(player, 5578, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2167 )
		{
			getActor().dropItem(player, 5579, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1250 )
		{
			getActor().dropItem(player, 70, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 940 )
		{
			getActor().dropItem(player, 21747, 1);
		}
	}
}