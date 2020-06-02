package quests;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;

public class _450_GraveRobberMemberRescue extends QuestScript
{

	private static int KANEMIKA = 32650;
	private static int WARRIOR_NPC = 32651;

	private static int WARRIOR_MON = 22741;

	private static int EVIDENCE_OF_MIGRATION = 14876;

	public _450_GraveRobberMemberRescue()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(KANEMIKA);
		addTalkId(WARRIOR_NPC);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("32650-05.htm"))
		{
			st.setCond(1);
		}
		return event;
	}

	@Override
	public String onTalk(final NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();

		if(npcId == KANEMIKA)
		{
			if(cond == 0)
			{
				if(player.getLevel() < 80)
					htmltext = "32650-00.htm";
				else if(!canEnter(player))
					htmltext = "32650-09.htm";
				else
					htmltext = "32650-01.htm";
			}
			else if(cond == 1)
			{
				if(st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) >= 1)
					htmltext = "32650-07.htm";
				else
					htmltext = "32650-06.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) >= 10)
			{
				htmltext = "32650-08.htm";
				st.giveItems(ADENA_ID, 65000, true, true);
				st.takeItems(EVIDENCE_OF_MIGRATION, -1);
				st.finishQuest();
				st.getPlayer().setVar(getName(), String.valueOf(System.currentTimeMillis()), -1);
			}
		}
		else if(cond == 1 && npcId == WARRIOR_NPC)
			if(Rnd.chance(50))
			{
				htmltext = "32651-01.htm";
				st.giveItems(EVIDENCE_OF_MIGRATION, 1, false, false);
				st.playSound(SOUND_ITEMGET);
				npc.moveToLocation(new Location(npc.getX() + 200, npc.getY() + 200, npc.getZ()), 0, false);

				ThreadPoolManager.getInstance().schedule(new RunnableImpl()
						{

							@Override
							public void runImpl() throws Exception
							{
								npc.deleteMe();
							}

						}, 2500L);

				if(st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) >= 10)
				{
					st.setCond(2);
				}
			}
			else
			{
				htmltext = "";
				player.sendPacket(new ExShowScreenMessage(NpcString.NONE, 4000, ScreenMessageAlign.MIDDLE_CENTER, false, "The grave robber warrior has been filled with dark energy and is attacking you!"));
				NpcInstance warrior = st.addSpawn(WARRIOR_MON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 120000);
				warrior.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, Rnd.get(1, 100));

				if(Rnd.chance(50))
					Functions.npcSay(warrior, "...Grunt... oh...");
				else
					Functions.npcSay(warrior, "Grunt... What's... wrong with me...");

				npc.decayMe();

				return null;
			}

		return htmltext;
	}

	private boolean canEnter(Player player)
	{
		if(player.isGM())
			return true;
		String var = player.getVar(getName());
		if(var == null)
			return true;
		return Long.parseLong(var) - System.currentTimeMillis() > 24 * 60 * 60 * 1000;
	}
}