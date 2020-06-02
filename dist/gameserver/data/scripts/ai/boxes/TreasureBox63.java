package ai.boxes;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author: rage
 * @date: 19.12.11 21:05
 */
public class TreasureBox63 extends Fighter
{
	public TreasureBox63(NpcInstance actor)
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
		if( i0 < 5714 )
		{
			getActor().dropItem(player, 8627, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5102 )
		{
			getActor().dropItem(player, 8633, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5714 )
		{
			getActor().dropItem(player, 8639, 5);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5714 )
		{
			getActor().dropItem(player, 8638, 6);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5953 )
		{
			getActor().dropItem(player, 8632, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4572 )
		{
			getActor().dropItem(player, 8626, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 96 )
		{
			getActor().dropItem(player, 729, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 715 )
		{
			getActor().dropItem(player, 730, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4286 )
		{
			getActor().dropItem(player, 1540, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1929 )
		{
			getActor().dropItem(player, 10260, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1929 )
		{
			getActor().dropItem(player, 10261, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1929 )
		{
			getActor().dropItem(player, 10262, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1929 )
		{
			getActor().dropItem(player, 10263, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1929 )
		{
			getActor().dropItem(player, 10264, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1929 )
		{
			getActor().dropItem(player, 10265, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1929 )
		{
			getActor().dropItem(player, 10266, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1929 )
		{
			getActor().dropItem(player, 10267, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1929 )
		{
			getActor().dropItem(player, 10268, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 724 )
		{
			getActor().dropItem(player, 5595, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 724 )
		{
			getActor().dropItem(player, 9898, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1929 )
		{
			getActor().dropItem(player, 10269, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4822 )
		{
			getActor().dropItem(player, 8739, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4018 )
		{
			getActor().dropItem(player, 8740, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3349 )
		{
			getActor().dropItem(player, 8741, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3014 )
		{
			getActor().dropItem(player, 8742, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 9117 )
		{
			getActor().dropItem(player, 21180, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7294 )
		{
			getActor().dropItem(player, 21181, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7294 )
		{
			getActor().dropItem(player, 21182, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6078 )
		{
			getActor().dropItem(player, 1538, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4559 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 845 )
		{
			getActor().dropItem(player, 9654, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 845 )
		{
			getActor().dropItem(player, 9655, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 183 )
		{
			getActor().dropItem(player, 5580, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 183 )
		{
			getActor().dropItem(player, 5581, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 183 )
		{
			getActor().dropItem(player, 5582, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 130 )
		{
			getActor().dropItem(player, 80, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 128 )
		{
			getActor().dropItem(player, 21748, 1);
		}
	}
}