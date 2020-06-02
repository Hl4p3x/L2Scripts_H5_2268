package ai;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.scripts.Functions;


/**
 * Dilios AI
 * 
 * @author pchayka
 * 
 */
public class GeneralDilios extends DefaultAI
{
	private static final int GUARD_ID = 32619;
	private long _wait_timeout = 0;

	private static final NpcString[] diliosText =
	{
			/* "Messenger, inform the patrons of the Keucereus Alliance Base! The Seed of Infinity is currently secured under the flag of the Keucereus Alliance!", */
			NpcString.MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_WERE_GATHERING_BRAVE_ADVENTURERS_TO_ATTACK_TIATS_MOUNTED_TROOP_THATS_ROOTED_IN_THE_SEED_OF_DESTRUCTION,
			NpcString.MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_EKIMUS_IS_ABOUT_TO_BE_REVIVED_BY_THE_RESURRECTED_UNDEAD_IN_SEED_OF_INFINITY,
			NpcString.STABBING_THREE_TIMES
	};

	public GeneralDilios(NpcInstance actor)
	{
		super(actor);
		AI_TASK_ATTACK_DELAY = 10000;
	}

	@Override
	public boolean thinkActive()
	{
		NpcInstance actor = getActor();

		if(System.currentTimeMillis() > _wait_timeout)
		{
			_wait_timeout = System.currentTimeMillis() + 60000;
			int j = Rnd.get(1, 3);
			switch(j)
			{
				case 1:
					Functions.npcSay(actor, diliosText[0]);
					break;
				case 2:
					Functions.npcSay(actor, diliosText[1]);
					break;
				case 3:
					Functions.npcSay(actor, diliosText[2]);
					List<NpcInstance> around = actor.getAroundNpc(1500, 100);
					if(around != null && !around.isEmpty())
						for(NpcInstance guard : around)
							if(!guard.isMonster() && guard.getNpcId() == GUARD_ID)
								guard.broadcastPacket(new SocialActionPacket(guard.getObjectId(), 4));
			}
		}
		return false;
	}
}