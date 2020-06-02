package l2s.gameserver.handler.voicecommands.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.dao.RefferalDao;
import l2s.gameserver.dao.RefferalDao.CharInfo;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;

public class RefferalSystem extends Functions implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] {"ref"};
	
	public static CopyOnWriteArrayList<CharInfo> all_info = new CopyOnWriteArrayList<CharInfo>();
	
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS || !Config.ALLOW_REFFERAL_SYSTEM)
			return false;
		if(command.equals("ref"))
		{
			if(args != null)
			{
				String[] param = args.split(" ");
				if(param.length == 2)
				{
					if(param[0].equalsIgnoreCase("add"))
					{
						if(CharacterDAO.getInstance().getObjectIdByName(param[1]) == 0)
							activeChar.sendMessage(new CustomMessage("common.Admin.RefSystem.InvalidName", activeChar));
						else if(CharacterDAO.getInstance().getObjectIdByName(param[1]) == activeChar.getObjectId())
							activeChar.sendMessage(new CustomMessage("common.Admin.RefSystem.InvalidSelf", activeChar));
						else if(RefferalDao.getInstance().countReffs(param[1]) >= Config.MAX_REFFERALS_PER_CHAR)
							activeChar.sendMessage(new CustomMessage("common.Admin.RefSystem.LimitReached", activeChar));	
							
						else	
						{
							int reffered_id = CharacterDAO.getInstance().getObjectIdByName(param[1]);
							CharInfo char_info = new CharInfo(reffered_id, param[1], activeChar.getObjectId(), activeChar.getName());
							all_info.add(char_info);
							activeChar.sendMessage(new CustomMessage("common.Admin.RefSystem.RefSuccess", activeChar));	
						}		
					}
					else if(param[0].equalsIgnoreCase("remove"))
					{
						int obj = Integer.parseInt(param[1]);
						RefferalDao.getInstance().removeBlock(obj);
						activeChar.sendMessage(new CustomMessage("common.Admin.RefSystem.DelSuccess", activeChar));
					}	
				}
			}	
		}
		String newApp = "";
		
		if(activeChar.getOnlineTime() / 360 < Config.MIN_ONLINE_TIME) //mins
			newApp += "Система доступна после "+Config.MIN_ONLINE_TIME+" часов игры<br>";
		
		else if(activeChar.getLevel() < Config.MIN_REFF_LEVEL)
			newApp += "Система доступна с "+Config.MIN_REFF_LEVEL+" уровня<br>";
			
		else if(!RefferalDao.getInstance().isCharReffered(activeChar.getObjectId()))
		{
			newApp += "<edit var=\"new_reff\" width=150> <br>";
			newApp += "<button value=\"Добавить\" action=\"bypass -h user_ref add $new_reff \" width=120 height=25><br>";
		}
		else
		{
			newApp += "Вы уже задали реферала <br>";
			newApp += "<button value=\"Удалить\" action=\"bypass -h user_ref remove "+activeChar.getObjectId()+" \" width=75 height=15><br>";
		}	
				
		newApp += "Список рефералов:<br>";
			
		List<String> name_list = RefferalDao.getInstance().getCharNamesByRef(activeChar.getObjectId());
		int i = 1;
		newApp += "<table width=200><tr><td>#</td><td></td><td>Имя</td><td></td><td>Удалить</td></tr>";
		for(String name : name_list)
		{
			int id = RefferalDao.getReffererIdByName(name);
			newApp += "<tr><td>"+i+"</td><td></td><td>"+name+"</td><td></td>";
			newApp += "<td><button value=\"Удалить\" action=\"bypass -h user_ref remove "+id+" \" width=75 height=15></td></tr>";
			i++;
		}	
		newApp += "</table>";	

		String dialog = HtmCache.getInstance().getHtml("command/refferal.htm", activeChar);
		dialog = dialog.replace("%Reff%", newApp);
		
		show(dialog, activeChar);
		return true;	
	}
	
	
	public static void applyBonus(Player rewarded, int id, int count)
	{
		int r_id = RefferalDao.getInstance().getRefferedIdByRefferer(rewarded.getObjectId());
		if(r_id == 0)
			return;
		Player player = World.getPlayer(r_id);	
		if(player == null)
			return;
	
		double _count = count * Config.REF_PERCENT_GIVE;
		_count = Math.round(_count);
		
		ItemTemplate item = ItemHolder.getInstance().getTemplate(id);
		player.sendMessage(new CustomMessage("common.Admin.RefSystem.CharNotice", player).addNumber((long)_count).addString(item.getName()).addString(rewarded.getName()));
		
		ItemFunctions.addItem(player, id, (long)_count, "Give bonus by refferal system");
	}
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}