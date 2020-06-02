package handler.items;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;

public class BeastShot extends ScriptItemHandler
{
	private final static int[] _itemIds = { 6645, 6646, 6647, 20332, 20333, 20334 };

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		boolean isAutoSoulShot = false;
		if(player.getAutoSoulShot().contains(item.getItemId()))
			isAutoSoulShot = true;

		Servitor servitor = player.getServitor();
		if(servitor == null)
		{
			if(!isAutoSoulShot)
				player.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return false;
		}

		if(servitor.isDead())
		{
			if(!isAutoSoulShot)
				player.sendPacket(Msg.WHEN_PET_OR_SERVITOR_IS_DEAD_SOULSHOTS_OR_SPIRITSHOTS_FOR_PET_OR_SERVITOR_ARE_NOT_AVAILABLE);
			return false;
		}

		int consumption = 0;
		int skillid = 0;

		switch(item.getItemId())
		{
			case 6645:
			case 20332:
				if(servitor.getChargedSoulShot())
					return false;
				consumption = servitor.getSoulshotConsumeCount();
				if(!Config.INFINITY_SHOT)				
					if(!player.getInventory().destroyItem(item, consumption))
					{
						player.sendPacket(Msg.YOU_DONT_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR);
						return false;
					}
				servitor.chargeSoulShot();
				skillid = 2033;
				break;
			case 6646:
			case 20333:
				if(servitor.getChargedSpiritShot() > 0)
					return false;
				consumption = servitor.getSpiritshotConsumeCount();
				if(!Config.INFINITY_SHOT)	
					if(!player.getInventory().destroyItem(item, consumption))
					{
						player.sendPacket(Msg.YOU_DONT_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR);
						return false;
					}
				servitor.chargeSpiritShot(ItemInstance.CHARGED_SPIRITSHOT);
				skillid = 2008;
				break;
			case 6647:
			case 20334:
				if(servitor.getChargedSpiritShot() > 1)
					return false;
				consumption = servitor.getSpiritshotConsumeCount();
				if(!Config.INFINITY_SHOT)	
					if(!player.getInventory().destroyItem(item, consumption))
					{
						player.sendPacket(Msg.YOU_DONT_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR);
						return false;
					}
				servitor.chargeSpiritShot(ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
				skillid = 2009;
				break;
		}

		servitor.broadcastPacket(new MagicSkillUse(servitor, servitor, skillid, 1, 0, 0));
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}

	@Override
	public boolean isAutoUse()
	{
		return true;
	}
}