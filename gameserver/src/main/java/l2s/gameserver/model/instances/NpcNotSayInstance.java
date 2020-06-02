package l2s.gameserver.model.instances;

import l2s.gameserver.templates.npc.NpcTemplate;

public class NpcNotSayInstance extends NpcInstance
{
	public NpcNotSayInstance(final int objectID, final NpcTemplate template)
	{
		super(objectID, template);
		setHasChatWindow(false);
	}
}
