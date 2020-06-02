package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.RadarControlPacket;

public class _1102_Nottingale extends QuestScript
{
	private final static int Nottingale = 32627;

	public _1102_Nottingale()
	{
		super(PARTY_NONE, REPEATABLE);
		addFirstTalkId(Nottingale);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		Player player = st.getPlayer();
		if(!player.isQuestCompleted(10273))
		{
			player.sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControlPacket(0, 2, -184545, 243120, 1581));
			htmltext = "32627.htm";
		}
		else if(event.equalsIgnoreCase("32627-3.htm"))
		{
			player.sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControlPacket(0, 2, -192361, 254528, 3598));
		}
		else if(event.equalsIgnoreCase("32627-4.htm"))
		{
			player.sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControlPacket(0, 2, -174600, 219711, 4424));
		}
		else if(event.equalsIgnoreCase("32627-5.htm"))
		{
			player.sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControlPacket(0, 2, -181989, 208968, 4424));
		}
		else if(event.equalsIgnoreCase("32627-6.htm"))
		{
			player.sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControlPacket(0, 2, -252898, 235845, 5343));
		}
		else if(event.equalsIgnoreCase("32627-8.htm"))
		{
			player.sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControlPacket(0, 2, -212819, 209813, 4288));
		}
		else if(event.equalsIgnoreCase("32627-9.htm"))
		{
			player.sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControlPacket(0, 2, -246899, 251918, 4352));
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		QuestState qs = player.getQuestState(this);
		if(qs == null)
			newQuestState(player);
		return "";
	}
}