package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.utils.SkillUtils;

/**
 *
 * sample
 *
 * 0000: 85 02 00 10 04 00 00 01 00 4b 02 00 00 2c 04 00    .........K...,..
 * 0010: 00 01 00 58 02 00 00                               ...X...
 *
 *
 * format   h (dhd)
 *
 * @version $Revision: 1.3.2.1.2.6 $ $Date: 2005/04/05 19:41:08 $
 */
public class AbnormalStatusUpdatePacket extends L2GameServerPacket
{
	public static final int INFINITIVE_EFFECT = -1;
	private List<Effect> _effects;

	class Effect
	{
		int skillId;
		int dat;
		int duration;

		public Effect(int skillId, int dat, int duration)
		{
			this.skillId = skillId;
			this.dat = dat;
			this.duration = duration;
		}
	}

	public AbnormalStatusUpdatePacket()
	{
		_effects = new ArrayList<Effect>();
	}

	public void addEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_effects.size());

		for(Effect temp : _effects)
		{
			writeD(temp.skillId);
			writeH(SkillUtils.getSkillLevelFromMask(temp.dat));
			writeH(SkillUtils.getSubSkillLevelFromMask(temp.dat)); // Odyssey: TODO: Enchant Level?
			writeD(0x00);	// Abnormal Type
			writeOptionalD(temp.duration);
		}
	}
	
	@Override
	protected final void writeImplHF()
	{
		writeH(_effects.size());

		for(Effect temp : _effects)
		{
			writeD(temp.skillId);
			writeH(temp.dat);
			writeD(temp.duration);
		}
	}
}