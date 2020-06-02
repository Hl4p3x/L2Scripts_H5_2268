package ai.boxes;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author: rage
 * @date: 19.12.11 21:08
 */
public class TreasureBox84 extends Fighter
{
	public TreasureBox84(NpcInstance actor)
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
		if( i0 < 8005 )
		{
			getActor().dropItem(player, 8627, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7147 )
		{
			getActor().dropItem(player, 8633, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 10000 )
		{
			getActor().dropItem(player, 8639, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 961 )
		{
			getActor().dropItem(player, 9546, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 961 )
		{
			getActor().dropItem(player, 9547, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 961 )
		{
			getActor().dropItem(player, 9548, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 961 )
		{
			getActor().dropItem(player, 9549, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 961 )
		{
			getActor().dropItem(player, 9550, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 961 )
		{
			getActor().dropItem(player, 9551, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 49 )
		{
			getActor().dropItem(player, 959, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 481 )
		{
			getActor().dropItem(player, 960, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2402 )
		{
			getActor().dropItem(player, 14701, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3602 )
		{
			getActor().dropItem(player, 10260, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3602 )
		{
			getActor().dropItem(player, 10261, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3602 )
		{
			getActor().dropItem(player, 10262, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3602 )
		{
			getActor().dropItem(player, 10263, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3602 )
		{
			getActor().dropItem(player, 10264, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3602 )
		{
			getActor().dropItem(player, 10265, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3602 )
		{
			getActor().dropItem(player, 10266, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3602 )
		{
			getActor().dropItem(player, 10267, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3602 )
		{
			getActor().dropItem(player, 10268, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 676 )
		{
			getActor().dropItem(player, 5595, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 271 )
		{
			getActor().dropItem(player, 9898, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 136 )
		{
			getActor().dropItem(player, 17185, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3602 )
		{
			getActor().dropItem(player, 10269, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4690 )
		{
			getActor().dropItem(player, 9574, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3909 )
		{
			getActor().dropItem(player, 10484, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3259 )
		{
			getActor().dropItem(player, 14167, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2973 )
		{
			getActor().dropItem(player, 21185, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1784 )
		{
			getActor().dropItem(player, 21186, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2549 )
		{
			getActor().dropItem(player, 21187, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2549 )
		{
			getActor().dropItem(player, 21188, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2549 )
		{
			getActor().dropItem(player, 21189, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2549 )
		{
			getActor().dropItem(player, 21190, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2549 )
		{
			getActor().dropItem(player, 21191, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 223 )
		{
			getActor().dropItem(player, 9552, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 223 )
		{
			getActor().dropItem(player, 9553, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 223 )
		{
			getActor().dropItem(player, 9554, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 223 )
		{
			getActor().dropItem(player, 9555, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 223 )
		{
			getActor().dropItem(player, 9556, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 223 )
		{
			getActor().dropItem(player, 9557, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3568 )
		{
			getActor().dropItem(player, 6622, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 223 )
		{
			getActor().dropItem(player, 9627, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5946 )
		{
			getActor().dropItem(player, 1538, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4460 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 12 )
		{
			getActor().dropItem(player, 13071, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 12 )
		{
			getActor().dropItem(player, 13073, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 12 )
		{
			getActor().dropItem(player, 13072, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 13 )
		{
			getActor().dropItem(player, 13457, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 29 )
		{
			getActor().dropItem(player, 21749, 1);
		}
	}
}