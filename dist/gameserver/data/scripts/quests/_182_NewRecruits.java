package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.utils.Location;

/**
 * @author: pchayka
 * @date: 09.06.2010
 */
public class _182_NewRecruits extends QuestScript
{
	// NPC's
	private static int Kekropus = 32138;
	private static int Mother_Nornil = 32239;
	// ITEMS
	private static int Ring_of_Devotion = 10124;
	private static int Red_Crescent_Earring = 10122;
	// teleport to garden w/o instance initialize
	private static final Location TELEPORT_POSITION = new Location(-119544, 87176, -12619);

	public _182_NewRecruits()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Kekropus);
		addTalkId(Kekropus);
		addTalkId(Mother_Nornil);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;

		if(event.equals("take") && cond == 0)
		{
			st.setCond(1);
			htmltext = "kekropus_q182_2.htm";
		}
		else if(event.equals("mother_nornil_q182_2.htm") && cond == 1)
		{
			st.giveItems(Ring_of_Devotion, 2, false, false);
			st.finishQuest();
		}
		else if(event.equals("mother_nornil_q182_3.htm") && cond == 1)
		{
			st.giveItems(Red_Crescent_Earring, 2, false, false);
			st.finishQuest();
		}
		else if(event.equals("EnterNornilsGarden") && cond == 1)
			st.getPlayer().teleToLocation(TELEPORT_POSITION);

		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Kekropus)
		{
			if(cond == 0 && st.getPlayer().getRace() != Race.KAMAEL && st.getPlayer().getLevel() >= 17)
				htmltext = "kekropus_q182_1.htm";
			else
				htmltext = "kekropus_q182_1a.htm";
		}
		else if(npcId == Mother_Nornil)
			if(cond == 1)
				htmltext = "mother_nornil_q182_1.htm";
		return htmltext;
	}
}