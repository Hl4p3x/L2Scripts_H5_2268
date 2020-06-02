package l2s.gameserver.network.l2.c2s;

import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.AcquireSkillInfoPacket;

/**
 * Reworked: VISTALL
 */
public class RequestAquireSkillInfo extends L2GameClientPacket
{
	private int _id;
	private int _level;
	private AcquireType _type;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_type = ArrayUtils.valid(AcquireType.VALUES, readD());
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || player.getTransformation() != 0 || SkillHolder.getInstance().getSkill(_id, _level) == null || _type == null)
			return;

		NpcInstance trainer = player.getLastNpc();
		if(_type != AcquireType.REBORN && _type != AcquireType.MULTICLASS)
		{
			if((trainer == null || !player.checkInteractionDistance(trainer)) && !player.isGM()) {
				if (_type == AcquireType.NORMAL) {
					if(!Config.CAN_LEARN_SKILLS_FROM_INTERFACE) {
						player.sendPacket(new CustomMessage("cannot_learn_skill_from_interface", player));
						return;
					}
				}
				else
					return;
			}
		}

		ClassId selectedMultiClassId = player.getSelectedMultiClassId();
		if(_type == AcquireType.MULTICLASS)
		{
			if(selectedMultiClassId == null)
				return;
		}
		else
			selectedMultiClassId = null;

		SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, selectedMultiClassId, _id, _level, _type);
		if(skillLearn == null)
			return;

		sendPacket(new AcquireSkillInfoPacket(_type, skillLearn));
	}
}