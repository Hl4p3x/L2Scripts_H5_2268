package l2s.gameserver.utils;

import java.util.Comparator;

import l2s.gameserver.model.Effect;


/**
 * Сортирует эффекты по группам для корректного отображения в клиенте: включаемые, танцы/песни, положительные/отрицательные
 * 
 * @author G1ta0
 */
public class EffectsComparator implements Comparator<Effect>
{
	private static final EffectsComparator instance = new EffectsComparator();

	public static final EffectsComparator getInstance()
	{
		return instance;
	}

	@Override
	public int compare(Effect e1, Effect e2)
	{
		boolean toggle1 = e1.getSkill().isToggle();
		boolean toggle2 = e2.getSkill().isToggle();

		if(toggle1 && toggle2)
			return compareStartTime(e1, e2);

		if(toggle1 || toggle2)
			if (toggle1)
				return 1;
			else
				return -1;

		boolean music1 = e1.getSkill().isMusic();
		boolean music2 = e2.getSkill().isMusic();

		if(music1 && music2)
			return compareStartTime(e1, e2);

		if(music1 || music2)
			if (music1)
				return 1;
			else
				return -1;

		boolean offensive1 = e1.isOffensive();
		boolean offensive2 = e2.isOffensive();

		if(offensive1 && offensive2)
			return compareStartTime(e1, e2);

		if(offensive1 || offensive2)
			if (!offensive1)
				return 1;
			else
				return -1;

		boolean trigger1 = e1.getSkill().isTrigger();
		boolean trigger2 = e2.getSkill().isTrigger();

		if(trigger1 && trigger2)
			return compareStartTime(e1, e2);

		if(trigger1 || trigger2)
			if (trigger1)
				return 1;
			else
				return -1;

		return compareStartTime(e1, e2);
	}

	private int compareStartTime(Effect o1, Effect o2)
	{
		if(o1.getStartTime() > o2.getStartTime())
			return 1;

		if(o1.getStartTime() < o2.getStartTime())
			return -1;

		return 0;
	}
}