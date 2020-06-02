package l2s.gameserver.network.l2.s2c;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import l2s.gameserver.model.entity.events.impl.KrateisCubeEvent;
import l2s.gameserver.model.entity.events.objects.KrateisCubePlayerObject;

/**
 * @author VISTALL
 */
public class ExPVPMatchCCRecord extends L2GameServerPacket
{
	private final Map<String, Integer> _scores;

	public ExPVPMatchCCRecord(Map<String, Integer> scores)
	{
		_scores = scores;
	}

	public ExPVPMatchCCRecord(KrateisCubeEvent cube)
	{
		_scores = new HashMap<String, Integer>();
		for(KrateisCubePlayerObject p : cube.getSortedPlayers())
			_scores.put(p.getName(), p.getPoints());
	}

	@Override
	public void writeImpl()
	{
		writeD(0x00); // Open/Dont Open
		writeD(_scores.size());
		for(Entry<String, Integer> p : _scores.entrySet())
		{
			writeS(p.getKey());
			writeD(p.getValue());
		}
	}
}