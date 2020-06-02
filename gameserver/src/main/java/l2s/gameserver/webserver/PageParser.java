package l2s.gameserver.webserver;

import l2s.gameserver.model.GameObjectsStorage;

abstract class PageParser
{
	public static String parse(String s)
	{
		if(s.contains("%online%"))
			s = s.replaceAll("%online%", String.valueOf(GameObjectsStorage.getAllPlayersCount()));
		return s;
	}
}