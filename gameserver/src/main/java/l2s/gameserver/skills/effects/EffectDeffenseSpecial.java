package l2s.gameserver.skills.effects;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.stats.Env;

public class EffectDeffenseSpecial extends Effect
{
	private int _min;
	private int _max;

	public EffectDeffenseSpecial(Env env, EffectTemplate template)
	{
		super(env, template);
		_min = getTemplate().getParam().getInteger("minDefPercent", 1);
		_max = getTemplate().getParam().getInteger("maxDefPercent", 100);
	}
	

	@Override
	public boolean checkCondition()
	{
		if(getEffected().getPlayer() == null) //for summons also
			return false;
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Player player = _effected.getPlayer();
		
		int chosen = Rnd.get(_min, _max);
		double _curr_def = ((double) chosen / 100);
		player.setEnchantedDeffense(_curr_def);
	}	
	
	@Override
	public void onExit()
	{
		Player player = _effected.getPlayer();
		
		super.onExit();
		player.setEnchantedDeffense(1);
	}	

	@Override
	public boolean onActionTime()
	{
		return false;
	}	
}