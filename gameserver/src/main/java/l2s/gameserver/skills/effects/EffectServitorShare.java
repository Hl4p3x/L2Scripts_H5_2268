package l2s.gameserver.skills.effects;

import l2s.gameserver.listener.actor.player.OnPlayerSummonServitorListener;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncTemplate;

/**
 * @author G1ta0
 * @author VISTALL
 */
public class EffectServitorShare extends Effect
{
	public static class FuncShare extends Func
	{
		public FuncShare(Stats stat, int order, Object owner, double value)
		{
			super(stat, order, owner, value);
		}

		@Override
		public void calc(Env env)
		{
			double val = 0;
			// временный хардкод, до переписи статов
			switch(stat)
			{
				case MAX_HP:
					val = env.character.getPlayer().getMaxHp();
					break;
				case MAX_MP:
					val = env.character.getPlayer().getMaxMp();
					break;
				case POWER_ATTACK:
					val = env.character.getPlayer().getPAtk(null);
					break;
				case MAGIC_ATTACK:
					val = env.character.getPlayer().getMAtk(null, null);
					break;
				case POWER_DEFENCE:
					val = env.character.getPlayer().getPDef(null);
					break;
				case MAGIC_DEFENCE:
					val = env.character.getPlayer().getMDef(null, null);
					break;
				case POWER_ATTACK_SPEED:
					val = env.character.getPlayer().getPAtkSpd();
					break;
				case MAGIC_ATTACK_SPEED:
					val = env.character.getPlayer().getMAtkSpd();
					break;
				case CRITICAL_BASE:
					val = env.character.getPlayer().getCriticalHit(null, null);
					break;
			}

			env.value += val * value;
		}
	}

	private class OnPlayerSummonServitorListenerImpl implements OnPlayerSummonServitorListener
	{
		@Override
		public void onSummonServitor(Player player, Servitor servitor)
		{
			FuncTemplate[] funcTemplates = getTemplate().getAttachedFuncs();
			Func[] funcs = new Func[funcTemplates.length];
			for(int i = 0; i < funcs.length; i++)
				funcs[i] = new FuncShare(funcTemplates[i]._stat, funcTemplates[i]._order, EffectServitorShare.this, funcTemplates[i]._value);

			servitor.addStatFuncs(funcs);
			servitor.updateStats();
		}
	}

	private OnPlayerSummonServitorListener _listener = new OnPlayerSummonServitorListenerImpl();

	public EffectServitorShare(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		getEffected().addListener(_listener);

		Servitor servitor = getEffected().getServitor();
		if(servitor != null)
			_listener.onSummonServitor(null, servitor);
	}

	@Override
	public void onExit()
	{
		super.onExit();

		getEffected().removeListener(_listener);

		Servitor servitor = getEffected().getServitor();
		if(servitor != null)
		{
			servitor.removeStatsOwner(this);
			servitor.updateStats();
		}
	}

	@Override
	public Func[] getStatFuncs()
	{
		return Func.EMPTY_FUNC_ARRAY;
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}