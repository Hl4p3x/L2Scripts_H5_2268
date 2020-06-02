package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.StatsSet;

/**
 * @author VISTALL
 * @date 15:52/01.05.2011
 */
public class EnergyReplenish extends Skill
{
	private int _addEnergy;

	public EnergyReplenish(StatsSet set)
	{
		super(set);
		_addEnergy = set.getInteger("addEnergy");
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;

		if(!activeChar.isPlayer())
			return false;

		Player player = (Player)activeChar;
		ItemInstance item = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		if(item == null || (item.getTemplate().getAgathionEnergy() - item.getAgathionEnergy()) < _addEnergy)
		{
			player.sendPacket(SystemMsg.YOUR_ENERGY_CANNOT_BE_REPLENISHED_BECAUSE_CONDITIONS_ARE_NOT_MET);
			return false;
		}

		return true;
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature cha : targets)
		{
			cha.setAgathionEnergy(cha.getAgathionEnergy() + _addEnergy);
			cha.sendPacket(new SystemMessagePacket(SystemMsg.ENERGY_S1_REPLENISHED).addInteger(_addEnergy));
		}
	}
}
