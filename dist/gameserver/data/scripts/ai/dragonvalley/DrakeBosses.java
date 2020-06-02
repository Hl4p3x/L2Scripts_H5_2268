package ai.dragonvalley;

import java.util.Map;

import l2s.gameserver.Config;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.AggroList.HateInfo;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PlayerGroup;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author pchayka
 */
public class DrakeBosses extends Fighter
{
	public DrakeBosses(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance corpse = null;
		switch(getActor().getNpcId())
		{
			case 25725:
				corpse = NpcUtils.spawnSingle(32884, getActor().getLoc(), 300000);
				break;
			case 25726:
				corpse = NpcUtils.spawnSingle(32885, getActor().getLoc(), 300000);
				break;
			case 25727:
				corpse = NpcUtils.spawnSingle(32886, getActor().getLoc(), 300000);
				break;
		}

		if (killer != null && corpse != null)
		{
			final Player player = killer.getPlayer();
			if (player != null)
			{
				PlayerGroup pg = player.getPlayerGroup();
				if (pg != null)
				{
					QuestState qs;
					Map<Playable, HateInfo> aggro = getActor().getAggroList().getPlayableMap();
					for (Player pl : pg)
					{
						if (pl != null && !pl.isDead() && aggro.containsKey(pl) && (getActor().isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || getActor().isInRangeZ(killer, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
						{
							qs = pl.getQuestState(456);
							if (qs != null && qs.getCond() == 1)
								qs.set("RaidKilled", corpse.getObjectId());
						}
					}
				}
			}
		}

		super.onEvtDead(killer);
		getActor().endDecayTask();
	}
}