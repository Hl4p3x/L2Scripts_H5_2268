package handler.items;

import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.ItemTemplate;

/**
 * @author VISTALL
 * @date 7:34/17.03.2011
 */
public class ItemSkills extends ScriptItemHandler
{
	private int[] _itemIds;

	public ItemSkills()
	{
		TIntHashSet set = new TIntHashSet();
		for(ItemTemplate template : ItemHolder.getInstance().getAllTemplates())
		{
			if(template == null)
				continue;

			for(Skill skill : template.getAttachedSkills())
				if(skill.isHandler())
					set.add(template.getItemId());
		}
		_itemIds = set.toArray();
	}

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		Player player;
		if(playable.isPlayer())
			player = (Player) playable;
		else if(playable.isPet())
			player = playable.getPlayer();
		else
			return false;

		Skill[] skills = item.getTemplate().getAttachedSkills();

		for(int i = 0; i < skills.length; i++)
		{
			Skill skill = skills[i];
			Creature aimingTarget = skill.getAimingTarget(player, player.getTarget());
			if(skill.checkCondition(player, aimingTarget, ctrl, false, true))
				player.getAI().Cast(skill, aimingTarget, ctrl, false);
			else if(i == 0)  //FIXME [VISTALL] всегда первый скил идет вместо конда?
				return false;
		}
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
