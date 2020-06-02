package l2s.gameserver.templates.spawn;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author VISTALL
 * @date 4:33/19.05.2011
 */
public class SpawnNpcInfo
{
	private final NpcTemplate _template;
	private final int _max;
	private final MultiValueSet<String> _parameters;

	public SpawnNpcInfo(int npcId, int max, MultiValueSet<String> set)
	{
		_template = NpcHolder.getInstance().getTemplate(npcId);
		_max = max;
		_parameters = set;
	}

	public NpcTemplate getTemplate()
	{
		return _template;
	}

	public int getMax()
	{
		return _max;
	}

	public MultiValueSet<String> getParameters()
	{
		return _parameters;
	}
}
