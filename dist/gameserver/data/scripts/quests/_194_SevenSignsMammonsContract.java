package quests;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.ExStartScenePlayer;

public class _194_SevenSignsMammonsContract extends QuestScript
{
	// NPCs
	private static int Colin = 32571;
	private static int SirGustavAthebaldt = 30760;
	private static int Frog = 32572;
	private static int Tess = 32573;
	private static int Kuta = 32574;
	private static int ClaudiaAthebaldt = 31001;

	// ITEMS
	private static int AthebaldtsIntroduction = 13818;
	private static int FrogKingsBead = 13820;
	private static int GrandmaTessCandyPouch = 13821;
	private static int NativesGlove = 13819;

	public _194_SevenSignsMammonsContract()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(SirGustavAthebaldt);
		addTalkId(Colin, SirGustavAthebaldt, Frog, Tess, Kuta, ClaudiaAthebaldt);
		addQuestItem(AthebaldtsIntroduction, FrogKingsBead, GrandmaTessCandyPouch, NativesGlove);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("sirgustavathebaldt_q194_2.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("sirgustavathebaldt_q194_2c.htm"))
		{
			st.setCond(2);
			player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_CONTRACT_OF_MAMMON);
			return null;
		}
		else if(event.equalsIgnoreCase("sirgustavathebaldt_q194_3.htm"))
		{
			st.setCond(3);
			st.giveItems(AthebaldtsIntroduction, 1, false, false);
		}
		else if(event.equalsIgnoreCase("colin_q194_3.htm"))
		{
			st.takeItems(AthebaldtsIntroduction, -1);
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("colin_q194_3a.htm"))
		{
			if(player.getTransformation() != 0 || player.isMounted())
			{
				player.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
				return null;
			}
			negateSpeedBuffs(player);
			SkillHolder.getInstance().getSkill(6201, 1).getEffects(npc, player, false, false);
		}
		else if(event.equalsIgnoreCase("frog_q194_2.htm"))
		{
			st.setCond(5);
			st.giveItems(FrogKingsBead, 1, false, false);
		}
		else if(event.equalsIgnoreCase("colin_q194_5.htm"))
		{
			st.setCond(6);
			st.takeItems(FrogKingsBead, -1);
		}
		else if(event.equalsIgnoreCase("colin_q194_6.htm"))
		{
			if(player.getTransformation() != 0 || player.isMounted())
			{
				player.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
				return null;
			}
			st.setCond(7);
			negateSpeedBuffs(player);
			SkillHolder.getInstance().getSkill(6202, 1).getEffects(player, player, false, false);
		}
		else if(event.equalsIgnoreCase("tess_q194_2.htm"))
		{
			st.setCond(8);
			st.giveItems(GrandmaTessCandyPouch, 1, false, false);
		}
		else if(event.equalsIgnoreCase("colin_q194_8.htm"))
		{
			st.setCond(9);
			st.takeItems(GrandmaTessCandyPouch, -1);
		}
		else if(event.equalsIgnoreCase("colin_q194_9.htm"))
		{
			if(player.getTransformation() != 0 || player.isMounted())
			{
				player.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
				return null;
			}
			st.setCond(10);
			negateSpeedBuffs(player);
			SkillHolder.getInstance().getSkill(6203, 1).getEffects(player, player, false, false);
		}
		else if(event.equalsIgnoreCase("kuta_q194_2.htm"))
		{
			st.setCond(11);
			st.giveItems(NativesGlove, 1, false, false);
		}
		else if(event.equalsIgnoreCase("colin_q194_10a.htm"))
		{
			st.setCond(12);
			st.takeItems(NativesGlove, -1);
		}
		else if(event.equalsIgnoreCase("claudiaathebaldt_q194_2.htm"))
		{
			if(player.getBaseClassId() == player.getActiveClassId())
			{
				st.addExpAndSp(25000000, 2500000);
				st.finishQuest();
			}
			else
				return "subclass_forbidden.htm";
		}
		else if(event.equalsIgnoreCase("colin_q194_11a.htm"))
		{
			if(player.getTransformation() != 0 || player.isMounted())
			{
				player.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
				return null;
			}
			negateSpeedBuffs(player);
			SkillHolder.getInstance().getSkill(6201, 1).getEffects(player, player, false, false);
		}
		else if(event.equalsIgnoreCase("colin_q194_12a.htm"))
		{
			if(player.getTransformation() != 0 || player.isMounted())
			{
				player.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
				return null;
			}
			negateSpeedBuffs(player);
			SkillHolder.getInstance().getSkill(6202, 1).getEffects(player, player, false, false);
		}
		else if(event.equalsIgnoreCase("colin_q194_13a.htm"))
		{
			if(player.getTransformation() != 0 || player.isMounted())
			{
				player.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
				return null;
			}
			negateSpeedBuffs(player);
			SkillHolder.getInstance().getSkill(6203, 1).getEffects(player, player, false, false);
		}
		else if(event.equalsIgnoreCase("colin_q194_0c.htm"))
			negateTransformations(player);
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();
		String htmltext = NO_QUEST_DIALOG;
		if(player.getBaseClassId() != player.getActiveClassId())
			return "subclass_forbidden.htm";
		if(npcId == SirGustavAthebaldt)
		{
			if(cond == 0)
			{
				if(player.getLevel() >= 79 && player.isQuestCompleted(193))
					htmltext = "sirgustavathebaldt_q194_1.htm";
				else
					htmltext = "sirgustavathebaldt_q194_0.htm";
			}
			else if(cond == 1)
				htmltext = "sirgustavathebaldt_q194_2b.htm";
			else if(cond == 2)
				htmltext = "sirgustavathebaldt_q194_2c.htm";
			else if(cond == 3)
			{
				if(st.getQuestItemsCount(AthebaldtsIntroduction) < 1)
					st.giveItems(AthebaldtsIntroduction, 1, false, false);
				htmltext = "sirgustavathebaldt_q194_4.htm";
			}
		}
		else if(npcId == Colin)
		{
			if(cond == 3)
			{
				if(st.getQuestItemsCount(AthebaldtsIntroduction) > 0)
					htmltext = "colin_q194_1.htm";
				else
					htmltext = "colin_q194_0b.htm";
			}
			else if(cond == 5)
				htmltext = "colin_q194_4.htm";
			else if(cond == 6)
				htmltext = "colin_q194_5.htm";
			else if(cond == 8)
				htmltext = "colin_q194_7.htm";
			else if(cond == 9)
				htmltext = "colin_q194_8.htm";
			else if(cond == 11)
				htmltext = "colin_q194_10.htm";
			else if(cond == 12)
				htmltext = "colin_q194_14.htm";

				//if player has lost transformation - sarcasticly giving him it again
			else if(cond == 4 && player.getTransformation() == 0)
				htmltext = "colin_q194_11.htm";
			else if(cond == 7 && player.getTransformation() == 0)
				htmltext = "colin_q194_12.htm";
			else if(cond == 10 && player.getTransformation() == 0)
				htmltext = "colin_q194_13.htm";
			else if((cond == 4 || cond == 7 || cond == 10) && player.getTransformation() != 0)
				htmltext = "colin_q194_0a.htm";
		}
		else if(npcId == Frog)
		{
			if(cond == 4 && player.getTransformation() == 111)
				htmltext = "frog_q194_1.htm";
			else if(cond == 5 && player.getTransformation() == 111)
				htmltext = "frog_q194_4.htm";
			else
				htmltext = "frog_q194_3.htm";
		}
		else if(npcId == Tess)
		{
			if(cond == 7 && player.getTransformation() == 112)
				htmltext = "tess_q194_1.htm";
			else if(cond == 8 && player.getTransformation() == 112)
				htmltext = "tess_q194_3.htm";
			else
				htmltext = "tess_q194_0.htm";
		}
		else if(npcId == Kuta)
		{
			if(cond == 10 && player.getTransformation() == 101)
				htmltext = "kuta_q194_1.htm";
			else if(cond == 11 && player.getTransformation() == 101)
				htmltext = "kuta_q194_3.htm";
			else
				htmltext = "kuta_q194_0.htm";
		}
		else if(npcId == ClaudiaAthebaldt)
			if(cond == 12)
				htmltext = "claudiaathebaldt_q194_1.htm";
			else
				htmltext = "claudiaathebaldt_q194_0.htm";
		return htmltext;
	}

	private void negateSpeedBuffs(Player p)
	{
		for(Effect e : p.getEffectList().getAllEffects())
			if(e.getStackType().equalsIgnoreCase("SpeedUp") && !e.isOffensive())
				e.exit();
	}

	private void negateTransformations(Player p)
	{
		p.setTransformation(0);
	}
}