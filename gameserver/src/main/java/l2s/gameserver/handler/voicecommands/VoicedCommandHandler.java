package l2s.gameserver.handler.voicecommands;

import java.util.HashMap;
import java.util.Map;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.impl.AntiGrief;
import l2s.gameserver.handler.voicecommands.impl.Atod;
import l2s.gameserver.handler.voicecommands.impl.Away;
import l2s.gameserver.handler.voicecommands.impl.CWHPrivileges2;
import l2s.gameserver.handler.voicecommands.impl.Cfg;
import l2s.gameserver.handler.voicecommands.impl.Cfg2;
import l2s.gameserver.handler.voicecommands.impl.CombineTalismans;
import l2s.gameserver.handler.voicecommands.impl.CombineTalismans2;
import l2s.gameserver.handler.voicecommands.impl.Debug;
import l2s.gameserver.handler.voicecommands.impl.Delevel;
import l2s.gameserver.handler.voicecommands.impl.Hellbound;
import l2s.gameserver.handler.voicecommands.impl.Help;
import l2s.gameserver.handler.voicecommands.impl.LockPc;
import l2s.gameserver.handler.voicecommands.impl.Offline;
import l2s.gameserver.handler.voicecommands.impl.Offline2;
import l2s.gameserver.handler.voicecommands.impl.OfflineBuffs;
import l2s.gameserver.handler.voicecommands.impl.Online;
import l2s.gameserver.handler.voicecommands.impl.Online2;
import l2s.gameserver.handler.voicecommands.impl.Password;
import l2s.gameserver.handler.voicecommands.impl.Password2;
import l2s.gameserver.handler.voicecommands.impl.Ping2;
import l2s.gameserver.handler.voicecommands.impl.PollCommand;
import l2s.gameserver.handler.voicecommands.impl.RefferalSystem;
import l2s.gameserver.handler.voicecommands.impl.Relocate;
import l2s.gameserver.handler.voicecommands.impl.Repair;
import l2s.gameserver.handler.voicecommands.impl.Repair2;
import l2s.gameserver.handler.voicecommands.impl.Security;
import l2s.gameserver.handler.voicecommands.impl.ServerInfo;
import l2s.gameserver.handler.voicecommands.impl.Siege;
import l2s.gameserver.handler.voicecommands.impl.SkillUse;
import l2s.gameserver.handler.voicecommands.impl.Wedding;
import l2s.gameserver.handler.voicecommands.impl.WhoAmI;
import l2s.gameserver.handler.voicecommands.impl.BotReport.ReportCommand;

public class VoicedCommandHandler extends AbstractHolder
{
	private static final VoicedCommandHandler _instance = new VoicedCommandHandler();

	public static VoicedCommandHandler getInstance()
	{
		return _instance;
	}

	private Map<String, IVoicedCommandHandler> _datatable = new HashMap<String, IVoicedCommandHandler>();

	private VoicedCommandHandler()
	{
		registerVoicedCommandHandler(new Help());
		if(Config.ENABLE_HELLBOUND)	
			registerVoicedCommandHandler(new Hellbound());
		if(Config.ENABLE_INFO)	
			registerVoicedCommandHandler(new ServerInfo());
		if(Config.ENABLE_WEDDING)	
			registerVoicedCommandHandler(new Wedding());
		if(Config.ENABLE_WHOAMI)	
			registerVoicedCommandHandler(new WhoAmI());
		if(Config.ENABLE_DEBUG)	
			registerVoicedCommandHandler(new Debug());
		if(Config.ENABLE_RELOCATE)	
			registerVoicedCommandHandler(new Relocate());
		if(Config.ENABLE_REFFERAL)	
			registerVoicedCommandHandler(new RefferalSystem());
		if(Config.ALLOW_MACROS_REUSE_BUG)
			registerVoicedCommandHandler(new SkillUse());
		if(Config.ENABLE_SECURITY)	
			registerVoicedCommandHandler(new Security());
		if(Config.ALLOW_AWAY_STATUS)
			registerVoicedCommandHandler(new Away());
		if(Config.BUFF_STORE_ENABLED)
			registerVoicedCommandHandler(new OfflineBuffs());
		if(Config.SIEGE_COMMAND_ENABLED)
			registerVoicedCommandHandler(new Siege());
		registerVoicedCommandHandler(new ReportCommand());		
		if(Config.ENABLE_DELEVEL)
			registerVoicedCommandHandler(new Delevel());			
		
		if(Config.ENABLE_NEW_CFG)
		{
			registerVoicedCommandHandler(new AntiGrief());
			registerVoicedCommandHandler(new Atod());
			registerVoicedCommandHandler(new Cfg2());
			registerVoicedCommandHandler(new CombineTalismans2());
			registerVoicedCommandHandler(new CWHPrivileges2());
			registerVoicedCommandHandler(new LockPc());
			registerVoicedCommandHandler(new Offline2());
			registerVoicedCommandHandler(new Online2());
			registerVoicedCommandHandler(new Password2());
			registerVoicedCommandHandler(new Ping2());
			registerVoicedCommandHandler(new PollCommand());
			registerVoicedCommandHandler(new Repair2());
		}
		else
		{
			if(Config.ENABLE_COMBINE_TALISMAN)
				registerVoicedCommandHandler(new CombineTalismans());		
			if(Config.ENABLE_CFG)	
				registerVoicedCommandHandler(new Cfg());		
			if(Config.ENABLE_OFFLINE)	
				registerVoicedCommandHandler(new Offline());
			if(Config.ENABLE_REPAIR)
				registerVoicedCommandHandler(new Repair());		
			if(Config.ENABLE_ONLINE)	
				registerVoicedCommandHandler(new Online());	
			if(Config.ENABLE_PASSWORD)	
				registerVoicedCommandHandler(new Password());				
		}
	}

	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for(String element : ids)
			_datatable.put(element, handler);
	}

	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if(voicedCommand.indexOf(" ") != -1)
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));

		return _datatable.get(command);
	}

	@Override
	public int size()
	{
		return _datatable.size();
	}

	@Override
	public void clear()
	{
		_datatable.clear();
	}
}
