package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.scripts.Functions;

/**
 * @author PaInKiLlEr
 */
public class SkillUse extends Functions implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] {"useskill"};

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		int skills = Integer.parseInt(args);

		Skill skill = SkillHolder.getInstance().getSkill(skills, activeChar.getSkillLevel(skills));

		String sk = "/useskill " + skill.getName();
		SayPacket2 cs = new SayPacket2(activeChar.getObjectId(), ChatType.ALL, activeChar.getName(), sk);

		activeChar.setMacroSkill(skill);
		activeChar.sendPacket(cs);

		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}