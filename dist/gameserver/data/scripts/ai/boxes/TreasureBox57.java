package ai.boxes;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author: rage
 * @date: 19.12.11 21:04
 */
public class TreasureBox57 extends Fighter
{
	public TreasureBox57(NpcInstance actor)
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
		if( i0 < 8657 )
		{
			getActor().dropItem(player, 736, 8);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8657 )
		{
			getActor().dropItem(player, 1061, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4617 )
		{
			getActor().dropItem(player, 737, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6926 )
		{
			getActor().dropItem(player, 8625, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5971 )
		{
			getActor().dropItem(player, 8631, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8657 )
		{
			getActor().dropItem(player, 8637, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 9234 )
		{
			getActor().dropItem(player, 8638, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4810 )
		{
			getActor().dropItem(player, 8632, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5541 )
		{
			getActor().dropItem(player, 8626, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4987 )
		{
			getActor().dropItem(player, 10260, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4987 )
		{
			getActor().dropItem(player, 10261, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4987 )
		{
			getActor().dropItem(player, 10262, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4987 )
		{
			getActor().dropItem(player, 10263, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4987 )
		{
			getActor().dropItem(player, 10264, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4987 )
		{
			getActor().dropItem(player, 10265, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4987 )
		{
			getActor().dropItem(player, 10266, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4987 )
		{
			getActor().dropItem(player, 10267, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4987 )
		{
			getActor().dropItem(player, 10268, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6233 )
		{
			getActor().dropItem(player, 5594, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 624 )
		{
			getActor().dropItem(player, 5595, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4987 )
		{
			getActor().dropItem(player, 10269, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7214 )
		{
			getActor().dropItem(player, 8736, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6233 )
		{
			getActor().dropItem(player, 8737, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5195 )
		{
			getActor().dropItem(player, 8738, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5402 )
		{
			getActor().dropItem(player, 21183, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5402 )
		{
			getActor().dropItem(player, 21184, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5402 )
		{
			getActor().dropItem(player, 1538, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4052 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 751 )
		{
			getActor().dropItem(player, 9648, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 901 )
		{
			getActor().dropItem(player, 9649, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 163 )
		{
			getActor().dropItem(player, 5580, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 163 )
		{
			getActor().dropItem(player, 5581, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 163 )
		{
			getActor().dropItem(player, 5582, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 161 )
		{
			getActor().dropItem(player, 79, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 103 )
		{
			getActor().dropItem(player, 21748, 1);
		}
	}
}