package l2s.gameserver.network.l2.s2c;

import java.util.Collection;
import java.util.Collections;

import l2s.gameserver.model.Creature;

public class MagicSkillLaunchedPacket extends L2GameServerPacket
{
	private final int _casterId;
	private final int _skillId;
	private final int _skillLevel;
	private final Collection<Creature> _targets;

	public MagicSkillLaunchedPacket(int casterId, int skillId, int skillLevel, Creature target)
	{
		_casterId = casterId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = Collections.singletonList(target);
	}

	public MagicSkillLaunchedPacket(int casterId, int skillId, int skillLevel, Collection<Creature> targets)
	{
		_casterId = casterId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = targets;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(0);
		writeD(_casterId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_targets.size());
		if(!_targets.isEmpty() && _targets != null)
		{
			for(Creature target : _targets)
				if(target != null)
					writeD(target.getObjectId());
		}
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_casterId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_targets.size());
		if(!_targets.isEmpty() && _targets != null)
		{
			for(Creature target : _targets)
				if(target != null)
					writeD(target.getObjectId());
		}
	}
}