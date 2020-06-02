package ai.boxes;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author: rage
 * @date: 19.12.11 21:06
 */
public class TreasureBox66 extends Fighter
{
	public TreasureBox66(NpcInstance actor)
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
		if( i0 < 6323 )
		{
			getActor().dropItem(player, 8627, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5646 )
		{
			getActor().dropItem(player, 8633, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6323 )
		{
			getActor().dropItem(player, 8639, 5);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6323 )
		{
			getActor().dropItem(player, 8638, 6);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6587 )
		{
			getActor().dropItem(player, 8632, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5059 )
		{
			getActor().dropItem(player, 8626, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 106 )
		{
			getActor().dropItem(player, 729, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 791 )
		{
			getActor().dropItem(player, 730, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4742 )
		{
			getActor().dropItem(player, 1540, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2134 )
		{
			getActor().dropItem(player, 10260, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2134 )
		{
			getActor().dropItem(player, 10261, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2134 )
		{
			getActor().dropItem(player, 10262, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2134 )
		{
			getActor().dropItem(player, 10263, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2134 )
		{
			getActor().dropItem(player, 10264, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2134 )
		{
			getActor().dropItem(player, 10265, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2134 )
		{
			getActor().dropItem(player, 10266, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2134 )
		{
			getActor().dropItem(player, 10267, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2134 )
		{
			getActor().dropItem(player, 10268, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 801 )
		{
			getActor().dropItem(player, 5595, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 801 )
		{
			getActor().dropItem(player, 9898, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2134 )
		{
			getActor().dropItem(player, 10269, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5335 )
		{
			getActor().dropItem(player, 8739, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4446 )
		{
			getActor().dropItem(player, 8740, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3705 )
		{
			getActor().dropItem(player, 8741, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3335 )
		{
			getActor().dropItem(player, 8742, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 10088 )
		{
			getActor().dropItem(player, 21180, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8070 )
		{
			getActor().dropItem(player, 21181, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8070 )
		{
			getActor().dropItem(player, 21182, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6725 )
		{
			getActor().dropItem(player, 1538, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5044 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 935 )
		{
			getActor().dropItem(player, 9654, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 935 )
		{
			getActor().dropItem(player, 9655, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 202 )
		{
			getActor().dropItem(player, 5580, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 202 )
		{
			getActor().dropItem(player, 5581, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 202 )
		{
			getActor().dropItem(player, 5582, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 144 )
		{
			getActor().dropItem(player, 80, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 141 )
		{
			getActor().dropItem(player, 21748, 1);
		}
	}
}