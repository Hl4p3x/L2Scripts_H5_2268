package handler.admincommands;

import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.scripts.ScriptFile;

/**
 * @author VISTALL
 * @date 22:57/08.04.2011
 */
public abstract class ScriptAdminCommand implements IAdminCommandHandler, ScriptFile
{
	@Override
	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}
}
