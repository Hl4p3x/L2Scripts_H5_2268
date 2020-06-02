package services;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.Util;

public class VoteManager extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private static class Vote
	{
		public boolean active;
		public String name;
		public int id;
		public int maxPerAccount;
		public TreeMap<Integer, String> variants = new TreeMap<Integer, String>();
		public Map<String, Integer[]> results = new HashMap<String, Integer[]>();
	}

	private static Map<Integer, Vote> VoteList = new HashMap<Integer, Vote>();

	private boolean vote(String command, Player activeChar, String args)
	{
		if(args != null && !args.isEmpty()) // применение голоса
		{
			String[] param = args.split(" ");
			if(param.length >= 2 && Util.isNumber(param[0]) && Util.isNumber(param[1]))
			{
				String playerId = activeChar.getAccountName();
				Vote v = VoteList.get(Integer.parseInt(param[0]));
				if(v == null || !v.active)
					return false;
				int var = Integer.parseInt(param[1]);
				Integer[] alreadyResults = v.results.get(playerId);
				if(alreadyResults == null)
				{
					v.results.put(playerId, new Integer[] { var });
					mysql.set("INSERT IGNORE INTO vote (`id`, `HWID`, `vote`) VALUES (?,?,?)", param[0], playerId, param[1]);
				}
				else if(alreadyResults.length < v.maxPerAccount)
				{
					for(int id : alreadyResults)
						if(id == var)
						{
							show("Error: you have already voted for this entry.", activeChar);
							return false;
						}
					v.results.put(playerId, ArrayUtils.add(alreadyResults, var));
					mysql.set("INSERT IGNORE INTO vote (`id`, `HWID`, `vote`) VALUES (?,?,?)", param[0], playerId, param[1]);
				}
				else
				{
					show("Error: you have reached votes limit.", activeChar);
					return false;
				}
			}
		}

		int count = 0;
		StringBuilder html = new StringBuilder("VoteManager:\n<br>");
		String playerId = activeChar.getAccountName();
		for(Entry<Integer, Vote> e : VoteList.entrySet())
			if(e.getValue().active)
			{
				count++;
				html.append(e.getValue().name).append(":<br>");
				Integer[] already = e.getValue().results.get(playerId);
				if(already != null && already.length >= e.getValue().maxPerAccount)
					html.append("You have already voted.<br>");
				else
				{
					List<Entry<Integer, String>> variants = new ArrayList<Entry<Integer, String>>(e.getValue().variants.size());
					for(Entry<Integer, String> variant : e.getValue().variants.entrySet())
						variants.add(variant);

					Collections.shuffle(variants);

					loop: for(Entry<Integer, String> variant : variants)
					{
						if(already != null)
							for(Integer et : already)
								if(et.equals(variant.getKey()))
									continue loop;
						html.append("[user_vote " + e.getValue().id + " " + variant.getKey() + "|" + variant.getValue() + "]<br1>");
					}
					html.append("<br>");
				}
			}
		if(count == 0)
			html.append("No active votes now.");
		show(html.toString(), activeChar);

		return true;
	}

	public static void load()
	{
		VoteList.clear();

		// грузим голосования
		try
		{
			File file = new File(Config.DATAPACK_ROOT, "data/vote.xml");
			DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
			factory2.setValidating(false);
			factory2.setIgnoringComments(true);
			Document doc2 = factory2.newDocumentBuilder().parse(file);

			for(Node n2 = doc2.getFirstChild(); n2 != null; n2 = n2.getNextSibling())
				if("list".equalsIgnoreCase(n2.getNodeName()))
					for(Node d2 = n2.getFirstChild(); d2 != null; d2 = d2.getNextSibling())
						if("vote".equalsIgnoreCase(d2.getNodeName()))
						{
							Vote v = new Vote();
							v.id = Integer.parseInt(d2.getAttributes().getNamedItem("id").getNodeValue());
							v.maxPerAccount = Integer.parseInt(d2.getAttributes().getNamedItem("maxPerAccount").getNodeValue());
							v.name = d2.getAttributes().getNamedItem("name").getNodeValue();
							v.active = Boolean.parseBoolean(d2.getAttributes().getNamedItem("active").getNodeValue());

							for(Node i = d2.getFirstChild(); i != null; i = i.getNextSibling())
								if("variant".equalsIgnoreCase(i.getNodeName()))
									v.variants.put(Integer.parseInt(i.getAttributes().getNamedItem("id").getNodeValue()), i.getAttributes().getNamedItem("desc").getNodeValue());

							VoteList.put(v.id, v);
						}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// грузим голоса
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("SELECT * FROM vote");
			rs = st.executeQuery();
			while(rs.next())
			{
				Vote v = VoteList.get(rs.getInt("id"));
				if(v != null)
				{
					String HWID = rs.getString("HWID");
					Integer[] rez = v.results.get(HWID);
					v.results.put(HWID, ArrayUtils.add(rez, rs.getInt("vote")));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, st, rs);
		}
	}

	private String[] _commandList = new String[] { "vote" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(command.equalsIgnoreCase("vote"))
			return vote(command, activeChar, args);
		return false;
	}

	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
		load();
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}