package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Effect;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.stats.Env;

public final class EffectParalyze extends Effect
{
	private final boolean _postEffectOnly; // псевдопаралич для Anchor-подобных скиллов, не парализует, но может быть снят Purify-ем.
	
	public EffectParalyze(Env env, EffectTemplate template)
	{
		super(env, template);
		_postEffectOnly = template.getParam().getBool("postEffectOnly", false);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isParalyzeImmune())
			return false;
		if(_effector.getServitor() != null && _effected == _effector.getServitor())
		{
			_effector.getPlayer().sendPacket(new SystemMessage(SystemMessage.THAT_IS_THE_INCORRECT_TARGET));
			return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!_postEffectOnly)
		{		
			_effected.startParalyzed();
			_effected.abortAttack(true, true);
			_effected.abortCast(true, true);
		}	
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(!_postEffectOnly)
			_effected.stopParalyzed();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}