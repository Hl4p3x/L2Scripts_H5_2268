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
public class TreasureBox72 extends Fighter
{
	public TreasureBox72(NpcInstance actor)
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
		if( i0 < 7649 )
		{
			getActor().dropItem(player, 8627, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6829 )
		{
			getActor().dropItem(player, 8633, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7649 )
		{
			getActor().dropItem(player, 8639, 5);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7649 )
		{
			getActor().dropItem(player, 8638, 6);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7968 )
		{
			getActor().dropItem(player, 8632, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6119 )
		{
			getActor().dropItem(player, 8626, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 128 )
		{
			getActor().dropItem(player, 729, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 957 )
		{
			getActor().dropItem(player, 730, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5737 )
		{
			getActor().dropItem(player, 1540, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2582 )
		{
			getActor().dropItem(player, 10260, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2582 )
		{
			getActor().dropItem(player, 10261, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2582 )
		{
			getActor().dropItem(player, 10262, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2582 )
		{
			getActor().dropItem(player, 10263, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2582 )
		{
			getActor().dropItem(player, 10264, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2582 )
		{
			getActor().dropItem(player, 10265, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2582 )
		{
			getActor().dropItem(player, 10266, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2582 )
		{
			getActor().dropItem(player, 10267, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2582 )
		{
			getActor().dropItem(player, 10268, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 968 )
		{
			getActor().dropItem(player, 5595, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 968 )
		{
			getActor().dropItem(player, 9898, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2582 )
		{
			getActor().dropItem(player, 10269, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6454 )
		{
			getActor().dropItem(player, 8739, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5378 )
		{
			getActor().dropItem(player, 8740, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4482 )
		{
			getActor().dropItem(player, 8741, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4034 )
		{
			getActor().dropItem(player, 8742, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8136 )
		{
			getActor().dropItem(player, 21183, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8136 )
		{
			getActor().dropItem(player, 21184, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4068 )
		{
			getActor().dropItem(player, 21185, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8136 )
		{
			getActor().dropItem(player, 1538, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6102 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1130 )
		{
			getActor().dropItem(player, 9654, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1130 )
		{
			getActor().dropItem(player, 9655, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 123 )
		{
			getActor().dropItem(player, 5908, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 123 )
		{
			getActor().dropItem(player, 5911, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 123 )
		{
			getActor().dropItem(player, 5914, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 58 )
		{
			getActor().dropItem(player, 6364, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 171 )
		{
			getActor().dropItem(player, 21748, 1);
		}
	}
}