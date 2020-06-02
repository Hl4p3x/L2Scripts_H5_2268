package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Effect;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.Stats;

public class EffectManaHealPercent extends Effect
{
	private final boolean _ignoreMpEff;

	public EffectManaHealPercent(Env env, EffectTemplate template)
	{
		super(env, template);
		_ignoreMpEff = template.getParam().getBool("ignoreMpEff", true);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isHealBlocked())
			return false;
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();

		if(_effected.isHealBlocked())
			return;

		double mp = calc() * _effected.getMaxMp() / 100.;
		double newMp = mp * (!_ignoreMpEff ? _effected.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100., _effector, getSkill()) : 100.) / 100.;
		double addToMp = Math.max(0, Math.min(newMp, _effected.calcStat(Stats.MP_LIMIT, null, null) * _effected.getMaxMp() / 100. - _effected.getCurrentMp()));

		_effected.sendPacket(new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToMp)));

		if(addToMp > 0)
			_effected.setCurrentMp(addToMp + _effected.getCurrentMp());
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}