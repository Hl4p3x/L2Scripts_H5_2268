package l2s.gameserver.model.items.listeners;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemEquipResult;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.item.ItemTemplate;

public final class ItemSkillsListener implements OnEquipListener
{
	private static final ItemSkillsListener _instance = new ItemSkillsListener();

	public static ItemSkillsListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		Player player = (Player)actor;

		Skill[] itemSkills = null;
		Skill enchant4Skill = null;

		ItemTemplate it = item.getTemplate();

		itemSkills = it.getAttachedSkills();

		enchant4Skill = it.getEnchant4Skill();

		player.removeTriggers(it);

		if(itemSkills != null && itemSkills.length > 0)
		{
			for(Skill itemSkill : itemSkills)
			{
				if(itemSkill.getId() >= 26046 && itemSkill.getId() <= 26048)
				{
					int level = player.getSkillLevel(itemSkill.getId());
					int newlevel = level - 1;
					if(newlevel > 0)
						player.addSkill(SkillHolder.getInstance().getSkill(itemSkill.getId(), newlevel), false);
					else
						player.removeSkillById(itemSkill.getId());
				}
				else
				{
					player.removeSkill(itemSkill, false);
				}
			}
		}

		if(enchant4Skill != null)
			player.removeSkill(enchant4Skill, false);

		if(itemSkills != null && itemSkills.length > 0 || enchant4Skill != null)
		{
			result.sendSkillList = true;
			result.sendChanges = true;
			result.refreshEquip = true;
		}
	}

	@Override
	public void onEquip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		Player player = (Player)actor;

		Skill[] itemSkills = null;
		Skill enchant4Skill = null;

		ItemTemplate it = item.getTemplate();

		itemSkills = it.getAttachedSkills();

		if(item.getEnchantLevel() >= 4)
			enchant4Skill = it.getEnchant4Skill();

		// Для оружия при несоотвествии грейда скилы не выдаем
		if(it.getType2() == ItemTemplate.TYPE2_WEAPON && player.getWeaponsExpertisePenalty() > 0)
			return;

		player.addTriggers(it);

		if(itemSkills.length > 0)
		{
			for(Skill itemSkill : itemSkills)
			{
				if(itemSkill.getId() >= 26046 && itemSkill.getId() <= 26048)
				{
					int level = player.getSkillLevel(itemSkill.getId());
					int newlevel = level;
					if(level > 0)
					{
						if(SkillHolder.getInstance().getSkill(itemSkill.getId(), level + 1) != null)
							newlevel = level + 1;
					}
					else
						newlevel = 1;

					if(newlevel != level)
					{
						player.addSkill(SkillHolder.getInstance().getSkill(itemSkill.getId(), newlevel), false);
						result.sendSkillList = true;
					}
				}
				else if(player.getSkillLevel(itemSkill.getId()) < itemSkill.getLevel())
				{
					player.addSkill(itemSkill, false);
					result.sendSkillList = true;

					if(itemSkill.isActive())
					{
						long reuseDelay = Formulas.calcSkillReuseDelay(player, itemSkill);
						reuseDelay = Math.min(reuseDelay, 30000);

						if(reuseDelay > 0 && !player.isSkillDisabled(itemSkill))
						{
							player.disableSkill(itemSkill, reuseDelay);
							result.sendSkillCollTime = true;
						}
					}
				}
			}
		}

		if(enchant4Skill != null)
		{
			player.addSkill(enchant4Skill, false);
			result.sendSkillList = true;
		}

		result.sendChanges = true;
	}
}