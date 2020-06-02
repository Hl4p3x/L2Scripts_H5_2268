package l2s.gameserver.model.items.listeners;

import l2s.gameserver.data.xml.holder.OptionDataHolder;
import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemEquipResult;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.OptionDataTemplate;

/**
 * @author VISTALL
 * @date 19:34/19.05.2011
 */
public final class ItemEnchantOptionsListener implements OnEquipListener
{
	private static final ItemEnchantOptionsListener _instance = new ItemEnchantOptionsListener();

	public static ItemEnchantOptionsListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onEquip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;

		Player player = actor.getPlayer();

		for(int i : item.getEnchantOptions())
		{
			OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
			if(template == null)
				continue;

			if(player.addOptionData(template) == template)
				continue;

			for(Skill skill : template.getSkills())
			{
				result.sendSkillList = true;
				result.sendChanges = true;
				if(player.isSkillDisabled(skill))
					result.sendSkillCollTime = true;
			}
		}
	}

	@Override
	public void onUnequip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;

		Player player = actor.getPlayer();

		for(int i : item.getEnchantOptions())
		{
			OptionDataTemplate template = player.removeOptionData(i);
			if(template == null)
				continue;

			if(!template.getSkills().isEmpty())
			{
				result.sendSkillList = true;
				result.sendChanges = true;
				result.refreshEquip = true;
			}
		}
	}
}
