package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.stats.Env;

public final class EffectInvulnerable extends Effect
{
	private final boolean _blockBuff;
	private final boolean _blockDebuff;
	
	public EffectInvulnerable(Env env, EffectTemplate template)
	{
		super(env, template);
		_blockBuff = template.getParam().getBool("blockBuff", false);
		_blockDebuff = template.getParam().getBool("blockDebuff", true);		
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isInvul())
			return false;
		Skill skill = _effected.getCastingSkill();
		if(skill != null && (skill.getSkillType() == SkillType.TAKECASTLE || skill.getSkillType() == SkillType.TAKEFORTRESS || skill.getSkillType() == SkillType.TAKEFLAG))
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startHealBlocked();
		_effected.setIsInvul(true);
		if (_blockBuff)
			_effected.startBuffImmunity();
		if (_blockDebuff)
			_effected.startDebuffImmunity();		
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopHealBlocked();
		_effected.setIsInvul(false);		
		if (_blockBuff)
			_effected.stopBuffImmunity();
		if (_blockDebuff)
			_effected.stopDebuffImmunity();		
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}