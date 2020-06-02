package ai;

import l2s.commons.math.random.RndSelector;
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;

public class ChristmasTree extends DefaultAI
{
	private static final RndSelector<Integer> SOUNDS;

	static
	{
		SOUNDS = new RndSelector<Integer>(5);
		SOUNDS.add(2140, 20);
		SOUNDS.add(2142, 20);
		SOUNDS.add(2145, 20);
		SOUNDS.add(2147, 20);
		SOUNDS.add(2149, 20);
	}

	private boolean _buffsEnabled = false;
	private int _timer = 0;

	public ChristmasTree(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		if(_buffsEnabled)
		{
			_timer++;
			if(_timer >= 180)
			{
				_timer = 0;

				final NpcInstance actor = getActor();
				if(actor == null)
				{
					return false;
				}

				addTaskBuff(actor, SkillHolder.getInstance().getSkill(2139, 1));

				if(Rnd.chance(33))
				{
					actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, SOUNDS.select(), 1, 500, 0));
				}
			}
		}

		return super.thinkActive();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		_buffsEnabled = !getActor().isInZonePeace();
		_timer = 0;
	}
}