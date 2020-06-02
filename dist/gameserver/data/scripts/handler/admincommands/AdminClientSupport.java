package handler.admincommands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author VISTALL
 * @date 23:46/17.05.2011
 */
public class AdminClientSupport extends ScriptAdminCommand
{
	private static final Logger _log = LoggerFactory.getLogger(AdminClientSupport.class);

	public enum Commands
	{
		admin_setskill,
		admin_summon,
		admin_systemmsg
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player player)
	{
		Commands c = (Commands)comm;
		GameObject target = player.getTarget();
		switch(c)
		{
			case admin_systemmsg:
				if(wordList.length != 2)
					return false;
				if(!player.getPlayerAccess().CanEditChar)
					return false;
				final String word = wordList[1];	
				player.sendPacket(new L2GameServerPacket()
				{
					@Override
					protected void writeImpl()
					{
						writeC(0x62);
						writeD(Integer.parseInt(word));
						writeD(0);
					}
				});
				break;
			case admin_setskill:
				if(wordList.length != 3)
					return false;

				if(!player.getPlayerAccess().CanEditChar)
					return false;
				if(target == null || !target.isPlayer())
					return false;
				try
				{
					Skill skill = SkillHolder.getInstance().getSkill(Integer.parseInt(wordList[1]), Integer.parseInt(wordList[2]));
					target.getPlayer().addSkill(skill, true);
					target.getPlayer().sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(),  skill.getLevel()));
				}
				catch(NumberFormatException e)
				{
					_log.info("AdminClientSupport:useAdminCommand(Enum,String[],String,L2Player): " + e, e);
					return false;
				}
				break;
			case admin_summon:
				if(wordList.length != 3)
					return false;

				if(!player.getPlayerAccess().CanEditChar)
					return false;
				try
				{
					int id = Integer.parseInt(wordList[1]);
					long count = Long.parseLong(wordList[2]);

					if(id >= 1000000)
					{
						if(!player.getPlayerAccess().CanEditNPC)
							return false;

						if(target == null)
							target = player;

						NpcTemplate template = NpcHolder.getInstance().getTemplate(id - 1000000);

						for(int i = 0; i < count; i++)
						{
							NpcInstance npc = template.getNewInstance();
							npc.setSpawnedLoc(target.getLoc());
							npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);

							npc.spawnMe(npc.getSpawnedLoc());
						}
					}
					else
					{
						if(!player.getPlayerAccess().UseGMShop)
							return false;
						if(target == null)
							target = player;

						if(!target.isPlayer())
							return false;

						ItemTemplate template = ItemHolder.getInstance().getTemplate(id);
						if(template == null)
							return false;

						if(template.isStackable())
						{
							ItemInstance item = ItemFunctions.createItem(id);
							item.setCount(count);

							target.getPlayer().getInventory().addItem(item);
							target.getPlayer().sendPacket(SystemMessagePacket.obtainItems(id, count, 0));
						}
						else
						{
							for(int i = 0; i < count; i++)
							{
								ItemInstance item = ItemFunctions.createItem(id);

								target.getPlayer().getInventory().addItem(item);
								target.getPlayer().sendPacket(SystemMessagePacket.obtainItems(item));
							}
						}
					}
				}
				catch(NumberFormatException e)
				{
					_log.info("AdminClientSupport:useAdminCommand(Enum,String[],String,L2Player): " + e, e);
					return false;
				}

				break;
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
