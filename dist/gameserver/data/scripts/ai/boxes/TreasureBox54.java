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
public class TreasureBox54 extends Fighter
{
	public TreasureBox54(NpcInstance actor)
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
		if( i0 < 7727 )
		{
			getActor().dropItem(player, 736, 8);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7727 )
		{
			getActor().dropItem(player, 1061, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4121 )
		{
			getActor().dropItem(player, 737, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6182 )
		{
			getActor().dropItem(player, 8625, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5329 )
		{
			getActor().dropItem(player, 8631, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7727 )
		{
			getActor().dropItem(player, 8637, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8242 )
		{
			getActor().dropItem(player, 8638, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4293 )
		{
			getActor().dropItem(player, 8632, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4945 )
		{
			getActor().dropItem(player, 8626, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4451 )
		{
			getActor().dropItem(player, 10260, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4451 )
		{
			getActor().dropItem(player, 10261, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4451 )
		{
			getActor().dropItem(player, 10262, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4451 )
		{
			getActor().dropItem(player, 10263, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4451 )
		{
			getActor().dropItem(player, 10264, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4451 )
		{
			getActor().dropItem(player, 10265, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4451 )
		{
			getActor().dropItem(player, 10266, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4451 )
		{
			getActor().dropItem(player, 10267, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4451 )
		{
			getActor().dropItem(player, 10268, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5563 )
		{
			getActor().dropItem(player, 5594, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 557 )
		{
			getActor().dropItem(player, 5595, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4451 )
		{
			getActor().dropItem(player, 10269, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6439 )
		{
			getActor().dropItem(player, 8736, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5563 )
		{
			getActor().dropItem(player, 8737, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4636 )
		{
			getActor().dropItem(player, 8738, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5786 )
		{
			getActor().dropItem(player, 21182, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4822 )
		{
			getActor().dropItem(player, 21183, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4822 )
		{
			getActor().dropItem(player, 1538, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 3616 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 670 )
		{
			getActor().dropItem(player, 9648, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 804 )
		{
			getActor().dropItem(player, 9649, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 145 )
		{
			getActor().dropItem(player, 5580, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 145 )
		{
			getActor().dropItem(player, 5581, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 145 )
		{
			getActor().dropItem(player, 5582, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 217 )
		{
			getActor().dropItem(player, 142, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 92 )
		{
			getActor().dropItem(player, 21748, 1);
		}
	}
}