package l2s.gameserver.skills.effects;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.stats.Env;


/**
 * @author VISTALL
 * @date 12:01/29.01.2011
 */
public class EffectRandomHate extends Effect
{
	public EffectRandomHate(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		return getEffected().isMonster();
	}

	@Override
	public void onStart()
	{
		MonsterInstance monster = (MonsterInstance)getEffected();
		Creature mostHated = monster.getAggroList().getMostHated(monster.getAI().getMaxHateRange());
		if(mostHated == null)
			return;

		AggroList.AggroInfo mostAggroInfo = monster.getAggroList().get(mostHated);
		List<Creature> hateList = monster.getAggroList().getHateList(monster.getAI().getMaxHateRange());
		hateList.remove(mostHated);

		if(!hateList.isEmpty())
		{
			AggroList.AggroInfo newAggroInfo = monster.getAggroList().get(hateList.get(Rnd.get(hateList.size())));
			final int oldHate = newAggroInfo.hate;

			newAggroInfo.hate = mostAggroInfo.hate;
			mostAggroInfo.hate = oldHate;
		}
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}
