package l2s.gameserver.handler.voicecommands.impl.BotReport;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;

public class ReportCommand implements IVoicedCommandHandler
{
	private static final String[] COMMANDS = { "report" };
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (target.isEmpty() || !target.startsWith("answer"))
		{
			if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage("Target player that might be Bot and write .report");
				return false;
			}
			CaptchaHandler.tryReportPlayer(activeChar, activeChar.getTarget().getPlayer());
		}
		else if (target.startsWith("answer "))
		{
			String answer = target.substring("answer ".length());
			CaptchaHandler.onAnswerCaptcha(activeChar, answer);
		}
		return true;
	}
  
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}
