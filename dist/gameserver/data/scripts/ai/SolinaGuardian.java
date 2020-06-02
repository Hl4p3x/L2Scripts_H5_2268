package ai;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author pchayka
 */
public class SolinaGuardian extends Fighter
{

	public SolinaGuardian(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		getActor().altOnMagicUseTimer(getActor(), SkillHolder.getInstance().getSkill(6371, 1));
	}
}