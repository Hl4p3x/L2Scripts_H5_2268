package l2s.gameserver.model.items;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.SkillCoolTimePacket;

/**
 * @author Bonux
 * !!!ВНИМАНИЕ!!! КОСТЫЛИ! Если не собираетесь ПОЛНОСТЬЮ переписывать движок итемов, то лучше это не трогать!
**/
public class ItemEquipResult
{
	private final Playable _actor;

	public boolean refreshEquip;
	public boolean sendSkillList;
	public boolean sendSkillCollTime;
	public boolean sendChanges;

	public ItemEquipResult(Playable actor)
	{
		_actor = actor;
	}

	public void resultAction()
	{
		if(refreshEquip)
			_actor.getInventory().refreshEquip(this);

		if(_actor.isPlayer())
		{
			Player player = _actor.getPlayer();
			if(sendSkillList)
				player.sendSkillList();
			if(sendSkillCollTime)
				player.sendPacket(new SkillCoolTimePacket(player));
		}

		if(sendChanges)
			_actor.updateStats();
	}
}