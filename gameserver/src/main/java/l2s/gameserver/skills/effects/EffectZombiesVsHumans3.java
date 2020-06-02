package l2s.gameserver.skills.effects;

import java.util.concurrent.ScheduledFuture;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.Env;

/**
 * @author PaInKiLlEr
 */
public final class EffectZombiesVsHumans3 extends Effect
{
	private static ScheduledFuture<?> _zaraghenieTask;

	public EffectZombiesVsHumans3(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isParalyzeImmune())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		Player target = (Player) getEffected();
		if(target.getTransformation() == 303)
			return;

		super.onStart();

		_effected.startParalyzed();
		_effected.abortAttack(true, true);
		_effected.abortCast(true, true);
		_zaraghenieTask = ThreadPoolManager.getInstance().schedule(new ZaraghenieTask(), 180000);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopParalyzed();
		if(_zaraghenieTask != null)
		{
			_zaraghenieTask.cancel(false);
			_zaraghenieTask = null;
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	public class ZaraghenieTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			Player target = (Player) getEffected();
			if(target.getTransformation() != 21435 && target.getTransformation() != 22512 && target.getTransformation() != 22513)
			{
				removeAllSkills(target);
				Skill revengeSkill = SkillHolder.getInstance().getSkill(6611, 1);
				revengeSkill.getEffects(target, target, false, false);
			}
			else if(target.getTransformation() == 21435)
			{
				removeAllSkills(target);
				Skill revengeSkill = SkillHolder.getInstance().getSkill(6612, 1);
				revengeSkill.getEffects(target, target, false, false);
			}
			else if(target.getTransformation() == 22512)
			{
				removeAllSkills(target);
				Skill revengeSkill = SkillHolder.getInstance().getSkill(6613, 1);
				revengeSkill.getEffects(target, target, false, false);
			}
		}
	}

	public static void removeAllSkills(Player player)
	{
		player.getEffectList().stopEffect(6600);
		player.getEffectList().stopEffect(6601);
		player.getEffectList().stopEffect(6602);
		player.getEffectList().stopEffect(6603);
		player.getEffectList().stopEffect(6604);
		player.getEffectList().stopEffect(6605);
		player.getEffectList().stopEffect(6606);
		player.getEffectList().stopEffect(6607);
		player.getEffectList().stopEffect(6608);
		player.getEffectList().stopEffect(6609);
		player.getEffectList().stopEffect(6610);
		player.getEffectList().stopEffect(6611);
		player.getEffectList().stopEffect(6612);
		player.getEffectList().stopEffect(6613);
	}
}