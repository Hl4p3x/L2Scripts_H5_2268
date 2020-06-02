package ai.boxes;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author: rage
 * @date: 19.12.11 21:07
 */
public class TreasureBox78 extends Fighter
{
	public TreasureBox78(NpcInstance actor)
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
		if( i0 < 6836 )
		{
			getActor().dropItem(player, 8627, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6103 )
		{
			getActor().dropItem(player, 8633, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 10000 )
		{
			getActor().dropItem(player, 8639, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 821 )
		{
			getActor().dropItem(player, 9546, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 821 )
		{
			getActor().dropItem(player, 9547, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 821 )
		{
			getActor().dropItem(player, 9548, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 821 )
		{
			getActor().dropItem(player, 9549, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 821 )
		{
			getActor().dropItem(player, 9550, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 821 )
		{
			getActor().dropItem(player, 9551, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 42 )
		{
			getActor().dropItem(player, 959, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 411 )
		{
			getActor().dropItem(player, 960, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2051 )
		{
			getActor().dropItem(player, 14701, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3076 )
		{
			getActor().dropItem(player, 10260, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3076 )
		{
			getActor().dropItem(player, 10261, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3076 )
		{
			getActor().dropItem(player, 10262, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3076 )
		{
			getActor().dropItem(player, 10263, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3076 )
		{
			getActor().dropItem(player, 10264, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3076 )
		{
			getActor().dropItem(player, 10265, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3076 )
		{
			getActor().dropItem(player, 10266, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3076 )
		{
			getActor().dropItem(player, 10267, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3076 )
		{
			getActor().dropItem(player, 10268, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 577 )
		{
			getActor().dropItem(player, 5595, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 231 )
		{
			getActor().dropItem(player, 9898, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 116 )
		{
			getActor().dropItem(player, 17185, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3076 )
		{
			getActor().dropItem(player, 10269, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4006 )
		{
			getActor().dropItem(player, 9574, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3338 )
		{
			getActor().dropItem(player, 10484, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2783 )
		{
			getActor().dropItem(player, 14167, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2539 )
		{
			getActor().dropItem(player, 21185, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1524 )
		{
			getActor().dropItem(player, 21186, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2177 )
		{
			getActor().dropItem(player, 21187, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2177 )
		{
			getActor().dropItem(player, 21188, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2177 )
		{
			getActor().dropItem(player, 21189, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2177 )
		{
			getActor().dropItem(player, 21190, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2177 )
		{
			getActor().dropItem(player, 21191, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 191 )
		{
			getActor().dropItem(player, 9552, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 191 )
		{
			getActor().dropItem(player, 9553, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 191 )
		{
			getActor().dropItem(player, 9554, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 191 )
		{
			getActor().dropItem(player, 9555, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 191 )
		{
			getActor().dropItem(player, 9556, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 191 )
		{
			getActor().dropItem(player, 9557, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3047 )
		{
			getActor().dropItem(player, 6622, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 191 )
		{
			getActor().dropItem(player, 9627, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5078 )
		{
			getActor().dropItem(player, 1538, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3809 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 39 )
		{
			getActor().dropItem(player, 9570, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 39 )
		{
			getActor().dropItem(player, 9572, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 39 )
		{
			getActor().dropItem(player, 9571, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 21 )
		{
			getActor().dropItem(player, 9442, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 25 )
		{
			getActor().dropItem(player, 21749, 1);
		}
	}
}