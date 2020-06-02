package npc.model;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.SpecialMonsterInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;

public class GuzenInstance extends SpecialMonsterInstance
{
	private static final long serialVersionUID = 1L;
	private static final int GuzenDoor = 20260004;
	
	public GuzenInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	protected void onSpawn()
	{
		DoorInstance door = ReflectionUtils.getDoor(GuzenDoor);
		
		if(door.isOpen())
			door.closeMe();
		super.onSpawn();
	}
	
	protected void onDeath(Creature killer)
	{
		DoorInstance door = ReflectionUtils.getDoor(GuzenDoor);
		
		if(!door.isOpen())
			door.openMe();
		super.onDeath(killer);
	}
}