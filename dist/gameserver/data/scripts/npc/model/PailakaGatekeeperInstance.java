package npc.model;

import org.apache.commons.lang3.ArrayUtils;

import instances.RimPailaka;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.ResidenceType;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author pchayka
 */

public final class PailakaGatekeeperInstance extends NpcInstance
{
	private static final int rimIzId = 80;
	private static final int[] CastleWarden = {36403, 36404, 36405, 36406, 36407, 36408, 36409, 36410, 36411};

	public PailakaGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("rimentrance"))
		{
			// FIXME: повторный заход из r.289 но без спауна мобов...
			if(player.canEnterInstance(rimIzId))
			{
				if(checkGroup(player))
					ReflectionUtils.enterReflection(player, new RimPailaka(ArrayUtils.contains(CastleWarden, getNpcId()) ? 0 : 1), rimIzId);
				else
					//FIXME [G1ta0] кастом сообщение
					player.sendMessage("Failed to enter Rim Pailaka due to improper conditions");
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	private boolean checkGroup(Player p)
	{
		if(!p.isInParty())
			return false;
		for(Player member : p.getParty().getPartyMembers())
		{
			if(member.getClan() == null)
				return false;
			if(member.getClan().getResidenceId(ResidenceType.Castle) == 0 && member.getClan().getResidenceId(ResidenceType.Fortress) == 0)
				return false;
		}

		if(p.getQuestState(727) != null && !p.getQuestState(727).isStarted()
		   || p.getQuestState(726) != null && !p.getQuestState(726).isStarted())
		{
			p.sendMessage(new CustomMessage("scripts.npc.model.PailakaGatekeeperInstance.NoQuest", p));
			return false;
		}
		return true;
	}
}