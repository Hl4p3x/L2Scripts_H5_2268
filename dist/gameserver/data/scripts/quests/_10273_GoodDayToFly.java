package quests;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10273_GoodDayToFly extends QuestScript
{
	private final static int Lekon = 32557;
	private final static int VultureRider1 = 22614;
	private final static int VultureRider2 = 22615;

	private final static int Mark = 13856;

	public _10273_GoodDayToFly()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Lekon);

		addQuestItem(Mark);

		addKillId(VultureRider1, VultureRider2);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();

		if(event.equalsIgnoreCase("32557-06.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("32557-09.htm"))
		{
			if(player.getTransformation() != 0)
			{
				player.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
				return null;
			}
			st.set("transform", "1");
			SkillHolder.getInstance().getSkill(5982, 1).getEffects(player, player, false, false);
		}
		else if(event.equalsIgnoreCase("32557-10.htm"))
		{
			if(player.getTransformation() != 0)
			{
				player.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
				return null;
			}
			SkillHolder.getInstance().getSkill(5983, 1).getEffects(player, player, false, false);
		}
		else if(event.equalsIgnoreCase("32557-13.htm"))
		{
			if(player.getTransformation() != 0)
			{
				player.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
				return null;
			}
			if(st.getInt("transform") == 1)
				SkillHolder.getInstance().getSkill(5982, 1).getEffects(player, player, false, false);
			else if(st.getInt("transform") == 2)
				SkillHolder.getInstance().getSkill(5983, 1).getEffects(player, player, false, false);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Lekon)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 75)
					htmltext = "32557-00.htm";
				else
					htmltext = "32557-01.htm";
			}
			else
			{
				int transform = st.getInt("transform");
				if(st.getQuestItemsCount(Mark) >= 5)
				{
					htmltext = "32557-14.htm";
					if(transform == 1)
						st.giveItems(13553, 1, false, false);
					else if(transform == 2)
						st.giveItems(13554, 1, false, false);
					st.takeAllItems(Mark);
					st.giveItems(13857, 1, false, false);
					st.addExpAndSp(25160, 2525);
					st.finishQuest();
				}
				else if(transform < 1)
					htmltext = "32557-07.htm";
				else
					htmltext = "32557-11.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Lekon)
			htmltext = "32557-0a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		long count = st.getQuestItemsCount(Mark);
		if(cond == 1 && count < 5)
		{
			st.giveItems(Mark, 1, true, true);
			if(st.getQuestItemsCount(Mark) > 4)
				st.setCond(2);
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}