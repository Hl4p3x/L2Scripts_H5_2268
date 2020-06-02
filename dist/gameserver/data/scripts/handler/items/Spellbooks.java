package handler.items;

import java.util.List;

import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

/**
 * @author VISTALL
 */
public class Spellbooks extends ScriptItemHandler
{
	private int[] _itemIds = null;

	public Spellbooks()
	{
		TIntHashSet list = new TIntHashSet();
		List<SkillLearn> l = SkillAcquireHolder.getInstance().getAllNormalSkillTreeWithForgottenScrolls();
		for(SkillLearn learn : l)
			list.add(learn.getItemId());

		_itemIds = list.toArray();
	}

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
			return false;

		Player player = (Player) playable;

		if(item.getCount() < 1)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return false;
		}

		boolean multiclass = false;

		List<SkillLearn> list = SkillAcquireHolder.getInstance().getSkillLearnListByItemId(player, item.getItemId(), false);
		if(list.isEmpty())
		{
			multiclass = true;
			list = SkillAcquireHolder.getInstance().getSkillLearnListByItemId(player, item.getItemId(), Config.MULTICLASS_SYSTEM_ENABLED);
		}

		if(list.isEmpty())
		{
			player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}
		
		// проверяем ли есть нужные скилы
		boolean alreadyHas = true;
		boolean good = true;
		for(SkillLearn learn : list)
		{
			if(player.getSkillLevel(learn.getId()) != learn.getLevel())
			{
				alreadyHas = false;
				break;
			}
		}
		for(SkillLearn learn2 : list)
		{
			if(item.getItemId() == 13728 && learn2.getItemId() != 13728)
			{
				good = false;
				break;
			}
		}
		if(!good)
		{
			player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}		
		if(alreadyHas)
		{
			player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}

		// проверка по уровне
		boolean wrongLvl = false;
		for(SkillLearn learn : list)
		{
			if(player.getLevel() < learn.getMinLevel())
				wrongLvl = true;
		}

		if(wrongLvl)
		{
			player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}

		if(multiclass)
		{
			if(Config.MULTICLASS_SYSTEM_FORGOTTEN_SCROLLS_COUNT_MODIFIER > 0 && !player.consumeItem(item.getItemId(), Config.MULTICLASS_SYSTEM_FORGOTTEN_SCROLLS_COUNT_MODIFIER))
			{
				player.sendMessage(new CustomMessage("handler.items.Spellbooks.multiclass.incorrect_item_count", player).addNumber(Config.MULTICLASS_SYSTEM_FORGOTTEN_SCROLLS_COUNT_MODIFIER));
				return false;
			}
		}
		else if(!player.consumeItem(item.getItemId(), 1L))
			return false;

		for(SkillLearn skillLearn :  list)
		{
			Skill skill = SkillHolder.getInstance().getSkill(skillLearn.getId(), skillLearn.getLevel());
			if(skill == null)
				continue;

			player.sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(),  skill.getLevel()));
			player.addSkill(skill, true);
		}

		player.updateStats();
		player.sendSkillList();
		// Анимация изучения книги над головой чара (на самом деле, для каждой книги своя анимация, но они одинаковые)
		player.broadcastPacket(new MagicSkillUse(player, player, 2790, 1, 1, 0));
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}