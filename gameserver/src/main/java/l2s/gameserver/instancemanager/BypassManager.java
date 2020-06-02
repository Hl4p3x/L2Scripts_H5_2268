package l2s.gameserver.instancemanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.Strings;


public class BypassManager
{
	private static final Pattern base_pattern = Pattern.compile("\"(bypass +-h +)(.+?)\"");
	private static final Pattern bbs_pattern = Pattern.compile("\"(bypass)(.+?)\"");
	
	public static final DecodedBypass DEFAULT_BBS = new DecodedBypass("_bbshome", true);
	
	public static final Map<String, DecodedBypass> STATIC_BBS_SIMPLE = new HashMap<String, DecodedBypass>(7)
	{
		{
			put("_bbshome", new DecodedBypass("_bbshome", true));
			put("_bbsgetfav", new DecodedBypass("_bbsgetfav", true));
			put("_bbsloc", new DecodedBypass("_bbsloc", true));
			put("_bbslink", new DecodedBypass("_bbslink", true));
			put("_bbsclan", new DecodedBypass("_bbsclan", true));
			put("_bbsmemo", new DecodedBypass("_bbsmemo", true));
			put("_maillist_0_1_0_", new DecodedBypass("_maillist_0_1_0_", true));
			put("_friendlist_0_", new DecodedBypass("_friendlist_0_", true));
		
		}
	};

	public static enum BypassType
	{
		ENCODED,
		ENCODED_BBS,
		SIMPLE,
		SIMPLE_BBS,
		SIMPLE_DIRECT
	}

	public static BypassType getBypassType(String bypass)
	{
		switch(bypass.charAt(0))
		{
			case '0':
				return BypassType.ENCODED;
			case '1':
				return BypassType.ENCODED_BBS;
			default:
				if(Strings.matches(bypass, "^(_mrsl|_diary|_match|manor_menu_select|_match|_olympiad|menu_select?|multiclass?).*", Pattern.DOTALL))
					return BypassType.SIMPLE;
				if(STATIC_BBS_SIMPLE.containsKey(bypass))
					return BypassType.SIMPLE_BBS;
				return BypassType.SIMPLE_DIRECT;
		}
	}

	public static String encode(String html, List<String> bypassStorage, boolean bbs)
	{
		Matcher m = (bbs ? bbs_pattern : base_pattern).matcher(html);
		StringBuffer sb = new StringBuffer();

		while(m.find())
		{
			String bypass = m.group(2);
			String code = bypass;
			String params = "";
			int i = bypass.indexOf(" $");
			boolean use_params = i >= 0;
			if(use_params)
			{
				code = bypass.substring(0, i);
				params = bypass.substring(i).replace("$", "\\$");
			}

			if(bbs)
				m.appendReplacement(sb, "\"bypass 1" + Integer.toHexString(bypassStorage.size()) + params + "\"");
			else
				m.appendReplacement(sb, "\"bypass -h 0" + Integer.toHexString(bypassStorage.size()) + params + "\"");

			bypassStorage.add(code);
		}

		m.appendTail(sb);
		return sb.toString();
	}

	public static DecodedBypass decode(String bypass, List<String> bypassStorage, boolean bbs, Player player)
	{
		synchronized (bypassStorage)
		{
			String[] bypass_parsed = bypass.split(" ");
			int idx = Integer.parseInt(bypass_parsed[0].substring(1), 16);
			String bp;

			try
			{
				bp = bypassStorage.get(idx);
			}
			catch(Exception e)
			{
				bp = null;
			}

			if(bp == null)
			{
				Log.add("Can't decode bypass (bypass not exists): " + (bbs ? "[bbs] " : "") + bypass + " / Player: " + player.getName() + " / Npc: " + ((player.getLastNpc() == null) ? "null" : player.getLastNpc().getName()), "debug_bypass");
				return null;
			}

			DecodedBypass result = new DecodedBypass(bp, bbs);
			for(int i = 1; i < bypass_parsed.length; i++)
				result.bypass += " " + bypass_parsed[i];
			result.trim();

			return result;
		}
	}

	public static class DecodedBypass
	{
		public boolean bbs;
		public String bypass;

		public DecodedBypass(String _bypass, boolean _bbs)
		{
			bypass = _bypass;
			bbs = _bbs;
		}


		public DecodedBypass trim()
		{
			bypass = bypass.trim();
			return this;
		}
	}
}