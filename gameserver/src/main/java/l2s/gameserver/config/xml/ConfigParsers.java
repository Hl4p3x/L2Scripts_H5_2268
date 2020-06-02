package l2s.gameserver.config.xml;

import l2s.gameserver.config.xml.parser.HostsConfigParser;
import l2s.gameserver.config.xml.parser.VoteRewardConfigParser;

/**
 * @author Bonux
**/
public abstract class ConfigParsers
{
	public static void parseAllOnLoad()
	{
		HostsConfigParser.getInstance().load();
	}

	public static void parseAllOnInit()
	{
		VoteRewardConfigParser.getInstance().load();
	}
}
