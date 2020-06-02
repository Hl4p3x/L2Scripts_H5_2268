package handler.voicecommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import l2s.commons.map.hash.TIntStringHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.JoinPledgePacket;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdatePacket;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListAddPacket;
import l2s.gameserver.network.l2.s2c.PledgeSkillListPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.Util;

/**
 * @author Bonux
**/
public class Autoacademy extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private static class ClanComparator implements Comparator<Clan>
	{
		private static final Comparator<Clan> instance = new ClanComparator();

		public static final Comparator<Clan> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(Clan o1, Clan o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			if(o1.getAutoacademyRewardCount() == o2.getAutoacademyRewardCount())
				return Integer.compare(o2.getLevel(), o1.getLevel());
			return Long.compare(o2.getAutoacademyRewardCount(), o1.getAutoacademyRewardCount());
		}
	}

	private static final String[] COMMANDS = new String[] { "autoacademy" };

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(!Config.AUTOACADEMY_ENABLED)
			return false;

		StringTokenizer st = new StringTokenizer(args);
		TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("command/autoacademy.htm", player);
		String html = tpls.get(0);
		String message = null;
		StringBuilder content = new StringBuilder();

		Clan clan = player.getClan();
		if(clan == null || Clan.isAcademy(player.getPledgeType()))
		{
			int page = 1;
			if(st.hasMoreTokens())
			{
				String cmd = st.nextToken();
				if(cmd.equalsIgnoreCase("enter"))
				{
					if(clan != null && Clan.isAcademy(player.getPledgeType()))
					{
						message = tpls.get(8);
					}
					else if(player.getLevel() > 40 || player.getClassLevel() >= 2)
					{
						message = tpls.get(6);
					}
					else if(st.hasMoreTokens())
					{
						Clan c = ClanTable.getInstance().getClan(Integer.parseInt(st.nextToken()));
						if(c == null || !checkClan(c))
						{
							message = tpls.get(11);
						}
						else
						{
							if(!player.canJoinClan())
							{
								player.sendPacket(SystemMsg.AFTER_LEAVING_OR_HAVING_BEEN_DISMISSED_FROM_A_CLAN_YOU_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN);
								return true;
							}

							player.sendPacket(new JoinPledgePacket(c.getClanId()));

							SubUnit subUnit = c.getSubUnit(Clan.SUBUNIT_ACADEMY);
							if(subUnit == null)
								return true;

							UnitMember member = new UnitMember(c, player.getName(), player.getTitle(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), Clan.SUBUNIT_ACADEMY, player.getPowerGrade(), player.getApprentice(), player.getSex(), Clan.SUBUNIT_NONE);
							subUnit.addUnitMember(member);

							player.setPledgeType(Clan.SUBUNIT_ACADEMY);
							player.setClan(c);

							member.setPlayerInstance(player, false);

							player.setLvlJoinedAcademy(player.getLevel());
							player.setVar("autoacademy_reward", c.getAutoacademyRewardCount());
							c.getWarehouse().destroyItemByItemId(ItemTemplate.ITEM_ID_ADENA, c.getAutoacademyRewardCount());

							member.setPowerGrade(c.getAffiliationRank(player.getPledgeType()));

							c.broadcastToOtherOnlineMembers(new PledgeShowMemberListAddPacket(member), player);
							c.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.S1_HAS_JOINED_THE_CLAN).addString(player.getName()), new PledgeShowInfoUpdatePacket(c));

							// this activates the clan tab on the new member
							player.sendPacket(SystemMsg.ENTERED_THE_CLAN);
							player.sendPacket(player.getClan().listAll());
							player.setLeaveClanTime(0);
							player.updatePledgeClass();

							// добавляем скилы игроку, ток тихо
							c.addSkillsQuietly(player);
							// отображем
							player.sendPacket(new PledgeSkillListPacket(c));
							player.sendSkillList();

							EventHolder.getInstance().findEvent(player);
							if(c.getWarDominion() > 0) // баг оффа, после вступа в клан нужен релог для квестов
							{
								DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);

								siegeEvent.updatePlayer(player, true);
							}
							else
								player.broadcastCharInfo();

							player.store(false);
							return true;
						}
					}
					else
					{
						message = tpls.get(11);
					}
				}
				else
					page = Integer.parseInt(cmd);
			}

			List<Clan> clans = new ArrayList<Clan>();
			for(Clan c : ClanTable.getInstance().getClans())
			{
				if(checkClan(c))
					clans.add(c);
			}

			if(clans.isEmpty())
			{
				if(message == null)
					message = tpls.get(10);
			}
			else
			{
				Collections.sort(clans, ClanComparator.getInstance());

				int maxPage = ((clans.size() - 1) / 9) + 1;
				page = Math.max(1, Math.min(page, maxPage));

				StringBuilder clanList = new StringBuilder();
				for(int i = ((page - 1) * 9); i < Math.min(clans.size(), page * 9); i++)
				{
					Clan c = clans.get(i);
					String clanBlock = tpls.get(201);
					clanBlock = clanBlock.replace("<?bg_color?>", (i == 0 || (i % 2) == 0) ? tpls.get(202) : tpls.get(203));
					clanBlock = clanBlock.replace("<?clan_name?>", c.getName());
					clanBlock = clanBlock.replace("<?clan_level?>", String.valueOf(c.getLevel()));
					clanBlock = clanBlock.replace("<?reward?>", Util.formatAdena(c.getAutoacademyRewardCount()));
					clanBlock = clanBlock.replace("<?clan_id?>", String.valueOf(c.getClanId()));
					clanList.append(clanBlock);
				}

				String mainBlock = tpls.get(200);
				mainBlock = mainBlock.replace("<?clan_list?>", clanList.toString());
				mainBlock = mainBlock.replace("<?prev_page_button?>", page == 1 ? "" : tpls.get(204).replace("<?prev_page?>", String.valueOf(page - 1)));
				mainBlock = mainBlock.replace("<?current_page?>", String.valueOf(page));
				mainBlock = mainBlock.replace("<?next_page_button?>", page == maxPage ? "" : tpls.get(205).replace("<?next_page?>", String.valueOf(page + 1)));
				content.append(mainBlock);
			}
		}
		else
		{
			if(st.hasMoreTokens())
			{
				String cmd = st.nextToken();
				if(cmd.equalsIgnoreCase("add"))
				{
					if(!player.isClanLeader())
					{
						message = tpls.get(9);
					}
					else if(clan.getLevel() < 5)
					{
						message = tpls.get(7);
					}
					else
					{
						if(st.hasMoreTokens())
						{
							long value = Long.parseLong(st.nextToken());
							if(value >= 0)
							{
								clan.setAutoacademyRewardCount(value);
								clan.updateClanInDB();
								message = tpls.get(3);
							}
							else
								message = tpls.get(2);
						}
						else
							message = tpls.get(2);
					}
				}
				else if(cmd.equalsIgnoreCase("change"))
				{
					if(!player.isClanLeader())
					{
						message = tpls.get(9);
					}
					else if(clan.getLevel() < 5)
					{
						message = tpls.get(7);
						clan.setAutoacademyRewardCount(-1);
						clan.updateClanInDB();
					}
					else
					{
						if(st.hasMoreTokens())
						{
							long value = Long.parseLong(st.nextToken());
							if(value >= 0)
							{
								clan.setAutoacademyRewardCount(value);
								clan.updateClanInDB();
								message = tpls.get(4);
							}
							else
								message = tpls.get(2);
						}
						else
							message = tpls.get(2);
					}
				}
				else if(cmd.equalsIgnoreCase("remove"))
				{
					message = tpls.get(5);
					clan.setAutoacademyRewardCount(-1);
					clan.updateClanInDB();
				}
			}

			String mainBlock = tpls.get(100);
			if(clan.getAutoacademyRewardCount() >= 0)
			{
				mainBlock = mainBlock.replace("<?description?>", tpls.get(102));
				mainBlock = mainBlock.replace("<?current_reward?>", tpls.get(103).replace("<?current_reward_value?>", Util.formatAdena(clan.getAutoacademyRewardCount())));
				mainBlock = mainBlock.replace("<?button_change?>", tpls.get(105));
				mainBlock = mainBlock.replace("<?button_remove?>", tpls.get(106));
			}
			else
			{
				mainBlock = mainBlock.replace("<?description?>", tpls.get(101));
				mainBlock = mainBlock.replace("<?current_reward?>", "");
				mainBlock = mainBlock.replace("<?button_change?>", tpls.get(104));
				mainBlock = mainBlock.replace("<?button_remove?>", "");
			}
			content.append(mainBlock);
		}

		if(message != null)
			html = html.replace("<?message?>", tpls.get(1).replace("<?message_text?>", message));
		else
			html = html.replace("<?message?>", "");

		html = html.replace("<?content?>", content.toString());

		show(html, player);
		return true;
	}

	private static boolean checkClan(Clan clan)
	{
		if(clan.isPlacedForDisband())
			return false;
		if(!clan.canInvite())
			return false;
		if(clan.getSubUnit(Clan.SUBUNIT_ACADEMY) == null)
			return false;
		if(clan.getUnitMembersSize(Clan.SUBUNIT_ACADEMY) >= clan.getSubPledgeLimit(Clan.SUBUNIT_ACADEMY))
			return false;
		if(clan.getAutoacademyRewardCount() == -1)
			return false;
		if(clan.getWarehouse().getCountOf(ItemTemplate.ITEM_ID_ADENA) < clan.getAutoacademyRewardCount())
			return false;
		return true;
	}

	@Override
	public void onLoad()
	{
		if(Config.AUTOACADEMY_ENABLED)
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}