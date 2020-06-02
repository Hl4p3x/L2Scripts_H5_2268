package npc.model;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.RaidBossInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import manager.FourSepulchersManager;
import manager.FourSepulchersSpawn;

public class SepulcherRaidInstance extends RaidBossInstance
{
	public int mysteriousBoxId = 0;

	public SepulcherRaidInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		Player player = killer.getPlayer();
		if(player != null)
			giveCup(player);

		super.onDeath(killer);
	}

	@Override
	protected void onDelete()
	{
		FourSepulchersSpawn.spawnEmperorsGraveNpc(mysteriousBoxId);
		super.onDelete();
	}

	private void giveCup(Player player)
	{
		int questId = FourSepulchersManager.QUEST_ID;
		int cupId = 0;
		int oldBrooch = 7262;

		switch(getNpcId())
		{
			case 25339:
				cupId = 7256;
				break;
			case 25342:
				cupId = 7257;
				break;
			case 25346:
				cupId = 7258;
				break;
			case 25349:
				cupId = 7259;
				break;
		}

		Party party = player.getParty();
		if(party != null) {
			for (Player mem : party.getPartyMembers()) {
				QuestState qs = mem.getQuestState(questId);
				if (qs != null && (qs.isStarted() || qs.isCompleted()) && mem.getInventory().getItemByItemId(oldBrooch) == null && player.isInRange(mem, 700))
					ItemFunctions.addItem(mem, cupId, 1, "Give sup for party by SepulcherRaidInstance");
			}
		}
		else
		{
			QuestState qs = player.getQuestState(questId);
			if(qs != null && (qs.isStarted() || qs.isCompleted()) && player.getInventory().getItemByItemId(oldBrooch) == null)
				ItemFunctions.addItem(player, cupId, 1, "Give sup for player by SepulcherRaidInstance");
		}
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}