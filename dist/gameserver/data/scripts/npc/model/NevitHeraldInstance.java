package npc.model;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author pchayka
 */

public final class NevitHeraldInstance extends NpcInstance
{
	public NevitHeraldInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("request_blessing"))
		{
			if(player.getEffectList().getEffectsBySkillId(23312) != null)
			{
				showChatWindow(player, 1);
				return;
			}
			List<Creature> target = new ArrayList<Creature>();
			target.add(player);
			broadcastPacket(new MagicSkillUse(this, player, 23312, 1, 0, 0));
			callSkill(SkillHolder.getInstance().getSkill(23312, 1), target, true);
		}
		else
			super.onBypassFeedback(player, command);
	}
}