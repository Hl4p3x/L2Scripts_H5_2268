package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.TreasureChestInstance;
import l2s.gameserver.templates.StatsSet;


public class Unlock extends Skill
{
	private int _unlockPower;

	public Unlock(StatsSet set)
	{
		super(set);
		_unlockPower = set.getInteger("unlockPower", 0) + 100;
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || target instanceof TreasureChestInstance && target.isDead())
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(target instanceof TreasureChestInstance && activeChar.isPlayer())
			return super.checkCondition(activeChar, target, forceUse, dontMove, first);

		if(!target.isDoor() || _unlockPower == 0)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		DoorInstance door = (DoorInstance) target;

		if(door.isOpen())
		{
			activeChar.sendPacket(Msg.IT_IS_NOT_LOCKED);
			return false;
		}

		if(!door.isUnlockable())
		{
			activeChar.sendPacket(Msg.YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR);
			return false;
		}

		if(door.getKey() > 0) // ключ не подходит к двери
		{
			activeChar.sendPacket(Msg.YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR);
			return false;
		}

		if(_unlockPower - door.getLevel() * 100 < 0) // Дверь слишком высокого уровня
		{
			activeChar.sendPacket(Msg.YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR);
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature targ : targets)
			if(targ != null)
			{
				if(targ.isDoor())
				{
					DoorInstance target = (DoorInstance) targ;
					_unlockPower = Math.min(100, _unlockPower - target.getLevel() * 100);
					if(!target.isOpen() && (target.getKey() > 0 || Rnd.chance(_unlockPower)))
					{
						target.openMe((Player)activeChar, true);
					}
					else
						activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR);
				}
				else if(targ instanceof TreasureChestInstance)
				{
					TreasureChestInstance target = (TreasureChestInstance) targ;
					if(!target.isDead())
						target.tryOpen((Player) activeChar, this);
				}
			}
	}
}