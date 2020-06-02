package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;

public class RequestMagicSkillUse extends L2GameClientPacket
{
	private Integer _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/**
	 * packet type id 0x39
	 * format:		cddc
	 */
	@Override
	protected void readImpl()
	{
		_magicId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			activeChar.setMacroSkill(null);
			return;
		}	

		activeChar.setActive();

		if(activeChar.isOutOfControl())
		{
			activeChar.setMacroSkill(null);
			activeChar.sendActionFailed();
			return;
		}
		
		if(activeChar.getMacroSkill() != null)
			_magicId = activeChar.getMacroSkill().getId();		
			
		Skill skill = SkillHolder.getInstance().getSkill(_magicId, activeChar.getSkillLevel(_magicId));
		
		if(activeChar.isPendingOlyEnd() || activeChar.getPendingLfcEnd())
		{
			if(skill != null && skill.isOffensive())
			{
				activeChar.setMacroSkill(null);
				activeChar.sendActionFailed();
				return;		
			}
		}	
		if(skill != null)
		{
			if(!(skill.isActive() || skill.isToggle()))
			{
				activeChar.setMacroSkill(null);
				return;
			}	

			FlagItemAttachment attachment = activeChar.getActiveWeaponFlagAttachment();
			if(attachment != null && !attachment.canCast(activeChar, skill))
			{
				activeChar.setMacroSkill(null);
				activeChar.sendActionFailed();
				return;
			}

			// В режиме трансформации доступны только скилы трансформы
			if(activeChar.getTransformation() != 0 && !activeChar.getAllSkills().contains(skill))
			{
				activeChar.setMacroSkill(null);
				return;
			}	

			if(skill.isToggle())
			{
				if(activeChar.getEffectList().getEffectsBySkill(skill) != null)
				{
					activeChar.setMacroSkill(null);
					activeChar.getEffectList().stopEffect(skill.getId());
					activeChar.sendActionFailed();
					return;
				}
			}

			activeChar.setGroundSkillLoc(null);
			
			if(activeChar.getMacroSkill() != null)
			{
				if(skill.getReuseDelay() < 9000)
				{
					activeChar.setReuseDelay(Math.max(0, skill.getReuseDelay() - 3000));
					activeChar.setMacroSkill(null);
				}
			}
			activeChar.getAI().Cast(skill, skill.getAimingTarget(activeChar, activeChar.getTarget()), _ctrlPressed, _shiftPressed);
		}
		else
		{
			activeChar.setMacroSkill(null);
			activeChar.sendActionFailed();
		}	
	}
}