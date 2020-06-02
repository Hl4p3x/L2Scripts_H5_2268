package npc.model.residences.dominion;

import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.residence.Dominion;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;

public class TerritoryManagerInstance extends NpcInstance
{
	public TerritoryManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		Dominion dominion = getDominion();
		DominionSiegeEvent siegeEvent = dominion.getSiegeEvent();
		int npcId = getNpcId();
		int badgeId = 13676 + dominion.getId();

		if(command.equalsIgnoreCase("buyspecial"))
		{
			if(ItemFunctions.getItemCount(player, badgeId) < 1)
				showChatWindow(player, 1);
			else
				MultiSellHolder.getInstance().SeparateAndSend(npcId, player, 0);
		}
		else if(command.equalsIgnoreCase("buyNobless"))
		{
			if(player.isNoble())
			{
				showChatWindow(player, 9);
				return;
			}
			if(player.consumeItem(badgeId, 100L))
			{
				Quest q = QuestHolder.getInstance().getQuest(234);
				QuestState qs = player.getQuestState(q);
				if(qs == null)
					qs = q.newQuestState(player);
				qs.finishQuest();

				if(player.getRace() == Race.KAMAEL)
				{
					q = QuestHolder.getInstance().getQuest(236);
					if(q != null)
					{
						qs = player.getQuestState(q);
						if(qs == null)
							qs = q.newQuestState(player);
						qs.finishQuest();
					}
				}
				else
				{
					q = QuestHolder.getInstance().getQuest(235);
					if(q != null)
					{
						qs = player.getQuestState(q);
						if(qs == null)
							qs = q.newQuestState(player);
						qs.finishQuest();
					}
				}

				Olympiad.addNoble(player);
				player.setNoble(true);
				player.updatePledgeClass();
				player.updateNobleSkills();
				player.sendSkillList();
				player.broadcastUserInfo(true);
				showChatWindow(player, 10);
			}
			else
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		}
		else if(command.equalsIgnoreCase("calculate"))
		{
			if(!player.isQuestContinuationPossible(true))
				return;
			int[] rewards = siegeEvent.calculateReward(player);
			if(rewards == null || rewards[0] == 0)
			{
				showChatWindow(player, 4);
				return;
			}

			NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this, getHtmlPath(npcId, 5, player), 5);
			html.replace("%territory%", HtmlUtils.htmlResidenceName(dominion.getId()));
			html.replace("%badges%", String.valueOf(rewards[0]));
			html.replace("%adena%", String.valueOf(rewards[1]));
			html.replace("%fame%", String.valueOf(rewards[2]));
			player.sendPacket(html);
		}
		else if(command.equalsIgnoreCase("recivelater"))
			showChatWindow(player, getHtmlPath(npcId, 6, player));
		else if(command.equalsIgnoreCase("recive"))
		{
			int[] rewards = siegeEvent.calculateReward(player);
			if(rewards == null || rewards[0] == 0)
			{
				showChatWindow(player, 4);
				return;
			}

			ItemFunctions.addItem(player, badgeId, rewards[0], true, "Receive territory siege reward by TerritoryManagerInstance");
			ItemFunctions.addItem(player, ItemTemplate.ITEM_ID_ADENA, rewards[1], true, "Receive territory siege reward by TerritoryManagerInstance");
			if(rewards[2] > 0)
				player.setFame(player.getFame() + rewards[2], "CalcBadges:" + dominion.getId());

			siegeEvent.clearReward(player.getObjectId());
			showChatWindow(player, 7);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		if(player.getLevel() < 40 || player.getClassLevel() <= 1)
			val = 8;
		return val == 0 ? "residence2/dominion/TerritoryManager.htm" : "residence2/dominion/TerritoryManager-" + val + ".htm";
	}
}