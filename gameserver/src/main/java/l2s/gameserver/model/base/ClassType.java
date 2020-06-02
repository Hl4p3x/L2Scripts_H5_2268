package l2s.gameserver.model.base;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;

public enum ClassType
{
	FIGHTER,
	MYSTIC,
	PRIEST;

	public static final ClassType[] VALUES = values();
	public static final ClassType[] MAIN_TYPES = getMainTypes();

	public static ClassType[] getMainTypes()
	{
		return new ClassType[]{ FIGHTER, MYSTIC };
	}

	public ClassType getMainType()
	{
		if(this == PRIEST)
			return MYSTIC;
		return this;
	}

	public boolean isMagician()
	{
		return this != FIGHTER;
	}
	
	public boolean isHealer()
	{
		return this == PRIEST;
	}

	public final String getName(Player player)
	{
		return new CustomMessage("l2s.gameserver.model.base.ClassType.name." + getMainType().ordinal(), player).toString();
	}
}