package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import l2s.gameserver.model.Playable;
import l2s.gameserver.utils.EffectsComparator;
import l2s.gameserver.utils.SkillUtils;

public class PartySpelledPacket extends L2GameServerPacket
{
	private final int _type;
	private final int _objId;
	private final List<Effect> _effects;

	public PartySpelledPacket(Playable activeChar, boolean full)
	{
		_objId = activeChar.getObjectId();
		_type = activeChar.isPet() ? 1 : activeChar.isSummon() ? 2 : 0;
		// 0 - L2Player // 1 - петы // 2 - саммоны
		_effects = new ArrayList<Effect>();
		if(full)
		{
			l2s.gameserver.model.Effect[] effects = activeChar.getEffectList().getAllFirstEffects();
			Arrays.sort(effects, EffectsComparator.getInstance());
			for(l2s.gameserver.model.Effect effect : effects)
				if(effect != null && effect.isInUse())
					effect.addPartySpelledIcon(this);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_type);
		writeD(_objId);
		writeD(_effects.size());
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(SkillUtils.getSkillLevelFromMask(temp._level)); // @Rivelia. Skill level by mask.
			writeH(SkillUtils.getSubSkillLevelFromMask(temp._level)); // @Rivelia. Sub skill level by mask.
			writeD(0x00);	// Abnormal Type
			writeOptionalD(temp._duration);
		}
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_type);
		writeD(_objId);
		writeD(_effects.size());
		for(Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
			writeD(temp._duration);
		}
	}

	public void addPartySpelledEffect(int skillId, int level, int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}

	static class Effect
	{
		final int _skillId;
		final int _level;
		final int _duration;

		public Effect(int skillId, int level, int duration)
		{
			_skillId = skillId;
			_level = level;
			_duration = duration;
		}
	}
}