package l2s.gameserver.instancemanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.utils.PositionUtils;
import l2s.gameserver.utils.TradeHelper;
import l2s.gameserver.utils.Util;


public class OfflineBufferManager
{
	protected static final Logger _log = Logger.getLogger(OfflineBufferManager.class.getName());
	private static final int MAX_INTERACT_DISTANCE = 100;
	private final Map<Integer, BufferData> _buffStores = new ConcurrentHashMap<Integer, BufferData>();
	
	public Map<Integer, BufferData> getBuffStores()
	{
		return _buffStores;
	}
	
	public void processBypass(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();
		
		switch(st.nextToken())
		{
			case "setstore": 
				try
				{
					int price = Integer.parseInt(st.nextToken());
					String title = st.nextToken();
					while(st.hasMoreTokens())
					{
						title = title + " " + st.nextToken();
					}
					title = title.trim();
					if(!_buffStores.containsKey(Integer.valueOf(player.getObjectId())))
					{
						if(player.getPrivateStoreType() != 0)
							player.sendMessage(player.isLangRus() ? "У вас уже есть магазин" : "You already have a store");
						else if(!Config.BUFF_STORE_ALLOWED_CLASS_LIST.contains(Integer.valueOf(player.getClassId().getId())))
							player.sendMessage(player.isLangRus() ? "Вашей профессии нельзя открывать магазин бафов" : "Your profession is not allowed to set an Buff Store");
						else if(TradeHelper.checksIfCanOpenStore(player, 20))
						{
							if(title.isEmpty() || title.length() >= 29)
							{
								player.sendMessage(player.isLangRus() ? "Максимальное кол-во символов до 29, используйте титул по короче!" : "You must put a title for this store and it must have less than 29 characters");
								throw new Exception();
							}
							if ((price < 1) || (price > 10000000))
							{
								player.sendMessage(player.isLangRus() ? "Цена для каждого баффа должна быть между 1-10кк Аден" : "The price for each buff must be between 1 and 10kk");
								throw new Exception();
							}
							if(!player.isGM() && !player.isInZone(ZoneType.buff_store_only))
							{
								player.sendMessage(player.isLangRus() ? "Вы не можете открыть магазин тут, поищите специальные места где можно открывать магазин!" : "You can't put a buff store here. Look for special designated zones or clan halls");
								throw new Exception();
							}
							else if (player.isAlikeDead() || player.isInOlympiadMode() || player.isMounted() || player.isCastingNow() || player.getOlympiadObserveGame() != null || player.getOlympiadGame() != null || Olympiad.isRegisteredInComp(player))
							{
								player.sendMessage(player.isLangRus() ? "Вы не соблюдаете правила открытие магазина, попробуйте позже!" : "You don't meet the required conditions to put a buff store right now");
								throw new Exception();
							}
							else
							{
								BufferData buffer = new BufferData(player, title, price, null);
								for(Skill skill : player.getAllSkills())
								{
									if((skill.isActive()) && (skill.getSkillType() == Skill.SkillType.BUFF) && (!skill.isHeroic()) && (skill.getTargetType() != Skill.SkillTargetType.TARGET_SELF) && (skill.getTargetType() != Skill.SkillTargetType.TARGET_PET) && ((!player.getClassId().equalsOrChildOf(ClassId.DOOMCRYER)) || (skill.getTargetType() != Skill.SkillTargetType.TARGET_CLAN)) && ((!player.getClassId().equalsOrChildOf(ClassId.DOMINATOR)) || ((skill.getTargetType() != Skill.SkillTargetType.TARGET_PARTY) && (skill.getTargetType() != Skill.SkillTargetType.TARGET_ONE))) && (!Config.BUFF_STORE_ALLOWED_SKILL_LIST.contains(Integer.valueOf(skill.getId()))))
									{
										buffer.getBuffs().put(Integer.valueOf(skill.getId()), skill);
									}
								}
								if (buffer.getBuffs().isEmpty())
								{
									player.sendMessage(player.isLangRus() ? "У вас нет не одного баффа который вы бы могли продать." : "You don't have any available buff to put on sale in the store");
									throw new Exception();
								}	
								else
								{
									_buffStores.put(Integer.valueOf(player.getObjectId()), buffer);
									player.sitDown(null); 
									player.setTitleColor(Config.BUFF_STORE_TITLE_COLOR, false);
									player.setTitle(title);
									player.setNameColor(Config.BUFF_STORE_NAME_COLOR, false);
									player.broadcastUserInfo(true);
									player.setPrivateStoreType(Player.STORE_PRIVATE_BUFF);
									player.sendMessage(player.isLangRus() ? "Ваш магазин был успешно установлен!" : "Your Buff Store was set succesfully");
								}
							}
						}
					}
				} 
				catch (NumberFormatException e)
				{ 
					player.sendMessage("The price for each buff must be between 1 and 10kk");
					NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(0);
					html.setFile("command/buffstore/buff_store_create.htm");
					player.sendPacket(html);
				}
				catch (Exception e)
				{
					NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(0);
					html.setFile("command/buffstore/buff_store_create.htm");
					player.sendPacket(html);
				}
				break;
			case "stopstore": 
				if(player.getPrivateStoreType() != Player.STORE_PRIVATE_BUFF)
					player.sendMessage(player.isLangRus() ? "У вас нет магазина на данный момент!" : "You dont have any store set right now");
				else
				{
					_buffStores.remove(Integer.valueOf(player.getObjectId()));
					player.setPrivateStoreType(0);
					player.standUp();
        
					player.setTitleColor(Player.DEFAULT_TITLE_COLOR, false);
					player.setTitle("");
					player.setNameColor(Config.NORMAL_NAME_COLOUR, false);
					player.broadcastUserInfo(true);
        
					player.sendMessage("Your Buff Store was removed succesfuly");
				}
				break;
			case "bufflist": 
				try
				{
					int playerId = Integer.parseInt(st.nextToken());
					boolean isPlayer = st.hasMoreTokens() ? st.nextToken().equalsIgnoreCase("player") : true;
					int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0;
					BufferData buffer = _buffStores.get(Integer.valueOf(playerId));
					if (buffer != null)
					{
						if (PositionUtils.calculateDistance(player, buffer.getOwner(), true) <= 100.0D)
						{
							if ((!isPlayer) && (player.getServitor() == null))
							{
								player.sendMessage(player.isLangRus() ? "У вас нет активных самонов на данный момент!" : "You don't have any active summon right now");
								showStoreWindow(player, buffer, !isPlayer, page);
							}
							else
							{
								showStoreWindow(player, buffer, isPlayer, page);
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case "purchasebuff": 
				try
				{
					int playerId = Integer.parseInt(st.nextToken());
					boolean isPlayer = st.hasMoreTokens() ? st.nextToken().equalsIgnoreCase("player") : true;
					int buffId = Integer.parseInt(st.nextToken());
					int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0;
					BufferData buffer = _buffStores.get(Integer.valueOf(playerId));
					if (buffer != null)
					{
						if (buffer.getBuffs().containsKey(Integer.valueOf(buffId)))
						{
							if (PositionUtils.calculateDistance(player, buffer.getOwner(), true) <= 100.0D)
							{
								if ((!isPlayer) && (player.getServitor() == null))
								{
									player.sendMessage(player.isLangRus() ? "У вас нет активных самонов на данный момент!" : "You don't have any active summon right now");
									showStoreWindow(player, buffer, !isPlayer, page);
								}
								else if ((player.getPvpFlag() > 0) || (player.isInCombat()) || (player.getKarma() > 0) || (player.isAlikeDead()) || (player.isJailed()) || (player.isInOlympiadMode()) || (player.isCursedWeaponEquipped()) || (player.isInStoreMode()) || (player.isInTrade()) || (player.getEnchantScroll() != null) || (player.isFishing()))
								{
									player.sendMessage("You don't meet the required conditions to use the buffer right now");
								}
								else
								{
									double buffMpCost = Config.BUFF_STORE_MP_ENABLED ? buffer.getBuffs().get(buffId).getMpConsume() * Config.BUFF_STORE_MP_CONSUME_MULTIPLIER : 0.0D;
									if ((buffMpCost > 0.0D) && (buffer.getOwner().getCurrentMp() < buffMpCost))
									{
										player.sendMessage(player.isLangRus() ? "У владельца недостаточно МП =(" : "This store doesn't have enough mp to give sell you this buff");
										showStoreWindow(player, buffer, isPlayer, page);
									}
									else
									{
										int buffPrice = player.getClanId() == buffer.getOwner().getClanId() && player.getClanId() != 0 ? 0 : buffer.getBuffPrice();
										if ((buffPrice > 0) && (player.getAdena() < buffPrice))
										{
											player.sendMessage(player.isLangRus() ? "У вас не достаточно Адены!" : "You don't have enough adena to purchase a buff");
										}
										else if ((buffPrice > 0) && (!player.reduceAdena(buffPrice, true)))
										{
											player.sendMessage(player.isLangRus() ? "У вас не достаточно Адены!" : "You don't have enough adena to purchase a buff");
										}
										else
										{
											if (buffPrice > 0)
											{
												buffer.getOwner().addAdena(buffPrice, true);
											}
											if (buffMpCost > 0.0D)
											{
												buffer.getOwner().reduceCurrentMp(buffMpCost, null);
											}
											if (isPlayer)
											{
												buffer.getBuffs().get(buffId).getEffects(player, player, false, false);
											}
											else
											{
												buffer.getBuffs().get(buffId).getEffects(player.getServitor(), player.getServitor(), false, false);
											}
											player.sendMessage("You have bought " + buffer.getBuffs().get(buffId).getName() + " from " + player.getName());
											showStoreWindow(player, buffer, isPlayer, page);
										}
									}
								}
							}
						}
					}
				}
				catch (Exception e) {}
				break;
		}
    
	}
  

	private void showStoreWindow(Player player, BufferData buffer, boolean isForPlayer, int page)
	{
		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(0);
		html.setFile("command/buffstore/buff_store_buffer.htm");
    
		int MAX_ENTRANCES_PER_ROW = 7;
		double entrancesSize = buffer.getBuffs().size();
		int maxPage = (int)Math.ceil(entrancesSize / 7.0D) - 1;
		int currentPage = Math.min(maxPage, page);
    
		StringBuilder buffList = new StringBuilder();
		Iterator<Skill> it = buffer.getBuffs().values().iterator();
		int i = 0;
		boolean changeColor = false;
		while (it.hasNext())
		{
			if (i < currentPage * 7)
			{
				it.next();
				i++;
			}
			else
			{
				if (i >= currentPage * 7 + 7)
				{
					break;
				}
				Skill buff = it.next();
				int baseMaxLvl = SkillHolder.getInstance().getBaseLevel(buff.getId());
        
				buffList.append("<tr>");
				buffList.append("<td fixwidth=300>");
				buffList.append("<table height=36 cellspacing=-1 bgcolor=" + (changeColor ? "171612" : "23221e") + ">");
				buffList.append("<tr>");
				buffList.append("<td width=42 valign=top><button value=\" \" action=\"bypass -h BuffStore purchasebuff " + buffer.getOwner().getObjectId() + " " + (isForPlayer ? "player" : "summon") + " " + buff.getId() + " " + currentPage + "\" width=32 height=32 back=" + buff.getIcon() + " fore=" + buff.getIcon() + "></td>");
				if (buff.getLevel() > baseMaxLvl)
				{
					int enchantType = (buff.getLevel() - baseMaxLvl) / buff.getEnchantLevelCount();
					int enchantLvl = (buff.getLevel() - baseMaxLvl) % buff.getEnchantLevelCount();
					enchantLvl = enchantLvl == 0 ? buff.getEnchantLevelCount() : enchantLvl; 
					buffList.append("<td fixwidth=240>" + buff.getName() + " <font color=a3a3a3>Lv</font> <font color=ae9978>" + baseMaxLvl + "</font>");
					buffList.append(" <font color=ffd969>+" + enchantLvl + " " + (enchantType >= 2 ? "Cost" : enchantType >= 3 ? "Power" : "Time") + "</font></td>");
				}
				else
				{
					buffList.append("<td fixwidth=240>" + buff.getName() + " <font color=a3a3a3>Lv</font> <font color=ae9978>" + buff.getLevel() + "</font></td>");
				}
				buffList.append("</tr>");
				buffList.append("</table>");
				buffList.append("</td>");
				buffList.append("</tr>");
				buffList.append("<tr>");
				buffList.append("<td height=10></td>");
				buffList.append("</tr>");
				i++;
				changeColor = !changeColor;
			}
		}
		String previousPageButton;
		if (currentPage > 0)
		{
			previousPageButton = "<button value=\" \" width=16 height=16 action=\"bypass -h BuffStore bufflist " + buffer.getOwner().getObjectId() + " " + (isForPlayer ? "player" : "summon") + " " + (currentPage - 1) + "\" fore=L2UI_CH3.shortcut_prev_down back=L2UI_CH3.shortcut_prev>";
		} 
		else
		{
			previousPageButton = "<button value=\" \" width=16 height=16 action=\"\" fore=L2UI_CH3.shortcut_prev_down back=L2UI_CH3.shortcut_prev>";
		}	
		String nextPageButton;
		if (currentPage < maxPage)
		{
			nextPageButton = "<button value=\" \" width=16 height=16 action=\"bypass -h BuffStore bufflist " + buffer.getOwner().getObjectId() + " " + (isForPlayer ? "player" : "summon") + " " + (currentPage + 1) + "\" fore=L2UI_CH3.shortcut_next_down back=L2UI_CH3.shortcut_next>";
		} 
		else
		{
			nextPageButton = "<button value=\" \" width=16 height=16 action=\"\" fore=L2UI_CH3.shortcut_next_down back=L2UI_CH3.shortcut_next>";
		}
		html.replace("%bufferId%", String.valueOf(buffer.getOwner().getObjectId()));
		html.replace("%bufferClass%", String.valueOf(buffer.getOwner().getClassId().getName(buffer.getOwner())));
		html.replace("%bufferLvl%", String.valueOf(buffer.getOwner().getLevel() >= 84 ? 84 : (buffer.getOwner().getLevel() >= 76) && (buffer.getOwner().getLevel() < 80) ? 76 : Math.round(buffer.getOwner().getLevel() / 10) * 10));
		html.replace("%bufferName%", buffer.getOwner().getName());
		html.replace("%bufferMp%", String.valueOf((int)buffer.getOwner().getCurrentMp()));
		html.replace("%buffPrice%", String.valueOf(Util.convertToLineagePriceFormat(buffer.getBuffPrice())));
		html.replace("%target%", isForPlayer ? "Player" : "Summon");
		html.replace("%page%", String.valueOf(currentPage));
		html.replace("%buffs%", buffList.toString());
		html.replace("%previousPageButton%", String.valueOf(previousPageButton));
		html.replace("%nextPageButton%", nextPageButton);
		html.replace("%pageCount%", currentPage + 1 + "/" + (maxPage + 1));
		player.sendPacket(html);
	}
  
	public static OfflineBufferManager getInstance()
	{
		return SingletonHolder._instance;
	}
  
	private static class SingletonHolder
	{
		protected static final OfflineBufferManager _instance = new OfflineBufferManager();
	}
  

	public static class BufferData
	{
		private final Player _owner;
		private final String _saleTitle;
		private final int _buffPrice;
		private final Map<Integer, Skill> _buffs = new HashMap<Integer, Skill>();
    
		public BufferData(Player player, String title, int price, List<Skill> buffs)
		{
			_owner = player;
			_saleTitle = title;
			_buffPrice = price;
			if (buffs != null)
			{
				for (Skill buff : buffs)
				{
					_buffs.put(Integer.valueOf(buff.getId()), buff);
				}
			}
		}
    
		public Player getOwner()
		{
			return _owner;
		}
    
		public String getSaleTitle()
		{
			return _saleTitle;
		}
    
		public int getBuffPrice()
		{
			return _buffPrice;
		}
    
		public Map<Integer, Skill> getBuffs()
		{
			return _buffs;
		}
	}
}