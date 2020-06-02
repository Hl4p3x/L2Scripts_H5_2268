package l2s.gameserver.model.instances;

import l2s.gameserver.templates.npc.NpcTemplate;

/**
 Team: In4Play
 User: DraKovvka
 Date: 03.12.2014
 Time: 14:32
 */
public class TerrainObjectInstance extends NpcInstance
{
	public TerrainObjectInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setShowName(false);
		setLockedTarget(true);
	}
}
