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
public class TreasureBox81 extends Fighter
{
	public TreasureBox81(NpcInstance actor)
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
		if( i0 < 7420 )
		{
			getActor().dropItem(player, 8627, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6625 )
		{
			getActor().dropItem(player, 8633, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 10000 )
		{
			getActor().dropItem(player, 8639, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 891 )
		{
			getActor().dropItem(player, 9546, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 891 )
		{
			getActor().dropItem(player, 9547, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 891 )
		{
			getActor().dropItem(player, 9548, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 891 )
		{
			getActor().dropItem(player, 9549, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 891 )
		{
			getActor().dropItem(player, 9550, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 891 )
		{
			getActor().dropItem(player, 9551, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 45 )
		{
			getActor().dropItem(player, 959, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 446 )
		{
			getActor().dropItem(player, 960, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2226 )
		{
			getActor().dropItem(player, 14701, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3339 )
		{
			getActor().dropItem(player, 10260, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3339 )
		{
			getActor().dropItem(player, 10261, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3339 )
		{
			getActor().dropItem(player, 10262, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3339 )
		{
			getActor().dropItem(player, 10263, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3339 )
		{
			getActor().dropItem(player, 10264, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3339 )
		{
			getActor().dropItem(player, 10265, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3339 )
		{
			getActor().dropItem(player, 10266, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3339 )
		{
			getActor().dropItem(player, 10267, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3339 )
		{
			getActor().dropItem(player, 10268, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 627 )
		{
			getActor().dropItem(player, 5595, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 251 )
		{
			getActor().dropItem(player, 9898, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 126 )
		{
			getActor().dropItem(player, 17185, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3339 )
		{
			getActor().dropItem(player, 10269, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4348 )
		{
			getActor().dropItem(player, 9574, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3623 )
		{
			getActor().dropItem(player, 10484, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3021 )
		{
			getActor().dropItem(player, 14167, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2756 )
		{
			getActor().dropItem(player, 21185, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1654 )
		{
			getActor().dropItem(player, 21186, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2363 )
		{
			getActor().dropItem(player, 21187, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2363 )
		{
			getActor().dropItem(player, 21188, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2363 )
		{
			getActor().dropItem(player, 21189, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2363 )
		{
			getActor().dropItem(player, 21190, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2363 )
		{
			getActor().dropItem(player, 21191, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 207 )
		{
			getActor().dropItem(player, 9552, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 207 )
		{
			getActor().dropItem(player, 9553, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 207 )
		{
			getActor().dropItem(player, 9554, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 207 )
		{
			getActor().dropItem(player, 9555, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 207 )
		{
			getActor().dropItem(player, 9556, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 207 )
		{
			getActor().dropItem(player, 9557, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3308 )
		{
			getActor().dropItem(player, 6622, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 207 )
		{
			getActor().dropItem(player, 9627, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5512 )
		{
			getActor().dropItem(player, 1538, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4134 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 21 )
		{
			getActor().dropItem(player, 10480, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 21 )
		{
			getActor().dropItem(player, 10482, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 21 )
		{
			getActor().dropItem(player, 10481, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 16 )
		{
			getActor().dropItem(player, 10215, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 27 )
		{
			getActor().dropItem(player, 21749, 1);
		}
	}
}