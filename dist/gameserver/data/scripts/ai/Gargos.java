package ai;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;

/**
 * AI Gargos<br>
 * Юзает огненый скил, пишет в чат фразу "Вперед!"<br>
 * @author n0nam3
 */
public class Gargos extends Fighter
{
	private long _lastFire;

	public Gargos(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		return super.thinkActive() || thinkFire();
	}

	protected boolean thinkFire()
	{
		if(System.currentTimeMillis() - _lastFire > 60000L)
		{
			NpcInstance actor = getActor();
			Functions.npcSayCustomMessage(actor, "scripts.ai.Gargos.fire");
			actor.doCast(SkillHolder.getInstance().getSkill(5705, 1), actor, false);
			_lastFire = System.currentTimeMillis();
			return true;
		}

		return false;
	}
}