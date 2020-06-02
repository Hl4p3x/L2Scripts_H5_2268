package ai.SkyshadowMeadow;

import org.apache.log4j.Logger;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author PaInKiLlEr
 *         - AI для Fire Feed (18933).
 *         - Удаляется через 10-60 секунд.
 *         - AI проверен и работает.
 */
public class FireFeed extends DefaultAI
{
	protected static Logger _log = Logger.getLogger(FireFeed.class.getName());
	private long _wait_timeout = System.currentTimeMillis() + Rnd.get(10, 30) * 1000;

	public FireFeed(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		if(_wait_timeout < System.currentTimeMillis())
			actor.decayMe();

		return true;
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		if(skill.getId() != 9075)
			return;

		NpcInstance actor = getActor();
		if(actor == null)
			return;

		actor.doCast(SkillHolder.getInstance().getSkill(6688, 1), caster, true);
	}
}