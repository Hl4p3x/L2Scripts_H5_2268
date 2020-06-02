package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bbs.CommunityBoardManager;
import l2s.gameserver.handler.bbs.ICommunityBoardHandler;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.ChangeAllowedHwid;
import l2s.gameserver.network.authcomm.gs2as.ChangeAllowedIp;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;

public class Security implements IVoicedCommandHandler {
	
	private String[] _commandList = {"lock", "unlock", "lockIp", "lockHwid", "unlockIp", "unlockHwid" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target) {
		
		if(command.equalsIgnoreCase("lock")) {	
			NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(activeChar.getObjectId());
			html.setFile("mods/lock/lock.htm");
			html.replace("%ip_block%", IpBlockStatus());
			html.replace("%hwid_block%", HwidBlockStatus());
			html.replace("%hwid_val%", HwidBlockBy());
			html.replace("%curIP%", activeChar.getIP());
			activeChar.sendPacket(html);
			return true;
		}
		
		else if(command.equalsIgnoreCase("lockIp")) {
			
			if(!Config.ALLOW_IP_LOCK)
				return true;
			
			AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), activeChar.getIP()));
			
			NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(activeChar.getObjectId());
			html.setFile("mods/lock/lock_ip.htm");
			html.replace("%curIP%", activeChar.getIP());
			activeChar.sendPacket(html);
			ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(Config.BBS_DEFAULT_PAGE);
			if(handler != null)
				handler.onBypassCommand(activeChar, Config.BBS_DEFAULT_PAGE);			
			return true;
		}
		
		else if(command.equalsIgnoreCase("lockHwid")) {
			
			if(!Config.ALLOW_HWID_LOCK)
				return true;
			
			AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), activeChar.getHWID()));
			NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(activeChar.getObjectId());
			html.setFile("mods/lock/lock_hwid.htm");
			activeChar.sendPacket(html);
			ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(Config.BBS_DEFAULT_PAGE);
			if(handler != null)
				handler.onBypassCommand(activeChar, Config.BBS_DEFAULT_PAGE);			
			return true;
		}
		
		else if(command.equalsIgnoreCase("unlockIp")) {
			
			AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), ""));
			
			NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(activeChar.getObjectId());
			html.setFile("mods/lock/unlock_ip.htm");
			html.replace("%curIP", activeChar.getIP());
			activeChar.sendPacket(html);
			ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(Config.BBS_DEFAULT_PAGE);
			if(handler != null)
				handler.onBypassCommand(activeChar, Config.BBS_DEFAULT_PAGE);			
			return true;
		}
		
		else if(command.equalsIgnoreCase("unlockHwid")) {
			
			AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), ""));
			
			NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(activeChar.getObjectId());
			html.setFile("mods/lock/unlock_hwid.htm");
			activeChar.sendPacket(html);
			ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(Config.BBS_DEFAULT_PAGE);
			if(handler != null)
				handler.onBypassCommand(activeChar, Config.BBS_DEFAULT_PAGE);			
			return true;
		}
		
		
		return true;
	}
	
	private String IpBlockStatus() {
		if(Config.ALLOW_IP_LOCK)
			return "Разрешено";
		else
			return "Запрещено";
	}
	
	private String HwidBlockStatus() {
		if(Config.ALLOW_HWID_LOCK)
			return "Разрешено";
		return "Запрещено";
	}
	
	private String HwidBlockBy() {
		String result = "(CPU/HDD)";
		
		switch(Config.HWID_LOCK_MASK) {
		case 2: 
			result = "(HDD)";
			break;
		case 4:
			result = "(BIOS)";
			break;
		case 6:
			result = "(BIOS/HDD)";
			break;
		case 8:
			result = "(CPU)";
			break;
		case 10:
			result = "(CPU/HDD)";
			break;
		case 12:
			result = "(CPU/BIOS)";
			break;
		case 14:
			result = "(CPU/HDD/BIOS)";
			break;
		case 1:
		case 3:
		case 5:
		case 7:
		case 9:
		case 11:
		case 13:
		default:
			result = "(unknown)";
				
		}
		return result;
	}

	@Override
	public String[] getVoicedCommandList() {
		return _commandList;
	}

}
