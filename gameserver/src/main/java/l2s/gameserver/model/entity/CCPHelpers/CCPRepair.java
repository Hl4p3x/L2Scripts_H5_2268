package l2s.gameserver.model.entity.CCPHelpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.data.xml.holder.PlayerTemplateHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ItemInstance.ItemLocation;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Location;

public class CCPRepair
{
	private static final Logger _log = LoggerFactory.getLogger(CCPRepair.class);
  
	public static boolean repairChar(Player activeChar, String target)
	{
		if(!target.isEmpty())
		{
			if(activeChar.getName().equalsIgnoreCase(target))
			{
				activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.YouCantRepairYourself", activeChar));
				return false;
			}

			int objId = 0;

			for(Map.Entry<Integer, String> e : activeChar.getAccountChars().entrySet())
			{
				if(e.getValue().equalsIgnoreCase(target))
				{
					objId = e.getKey();
					break;
				}
			}

			if(objId == 0)
			{
				activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.YouCanRepairOnlyOnSameAccount", activeChar));
				return false;
			}
			else if(World.getPlayer(objId) != null)
			{
				activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.CharIsOnline", activeChar));
				return false;
			}
			
			String name = "jailed";
			boolean found = false;
			
			Connection con2 = null;
			PreparedStatement statement2 = null;
			ResultSet rs2 = null;
			try
			{
				con2 = DatabaseFactory.getInstance().getConnection();
				statement2 = con2.prepareStatement("SELECT name FROM character_variables WHERE obj_Id=?");
				statement2.setInt(1, objId);
				rs2 = statement2.executeQuery();
				while(rs2.next())
				{
					if(name.equals(rs2.getString("name")))
					{
						activeChar.sendMessage("You cannot repair a jailed character");
						found = true;
						break;
						
					}
				}	
			}
			catch(Exception e)
			{
				_log.error("", e);
				return false;
			}
			finally
			{
				DbUtils.closeQuietly(con2, statement2, rs2);
			}
			
			if(found)
				return false;
			
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rs = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT c.karma, c.sex, cs.class_id FROM characters AS c LEFT JOIN character_subclasses AS cs ON c.obj_Id=cs.char_obj_id WHERE c.obj_Id=? AND cs.isBase=1");
				statement.setInt(1, objId);
				statement.execute();
				rs = statement.getResultSet();

				if(rs.next())
				{
					int karma = rs.getInt("karma");
					Sex sex = Sex.VALUES[rs.getInt("sex")];
					ClassId classId = ClassId.valueOf(rs.getInt("class_id"));

					DbUtils.close(statement, rs);

					if(karma > 0)
					{
						statement = con.prepareStatement("UPDATE characters SET x=17144, y=170156, z=-3502 WHERE obj_Id=?"); // Teleport to Floran
						statement.setInt(1, objId);
						statement.execute();
						DbUtils.close(statement);
					}
					else
					{
						Location loc = PlayerTemplateHolder.getInstance().getPlayerTemplate(classId, sex).getStartLocation();
						statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE obj_Id=?"); // Teleport to Start Loc
						statement.setInt(1, loc.getX());
						statement.setInt(2, loc.getY());
						statement.setInt(3, loc.getZ());
						statement.setInt(4, objId);
						statement.execute();
						DbUtils.close(statement);

						Collection<ItemInstance> items = ItemsDAO.getInstance().getItemsByOwnerIdAndLoc(objId, ItemLocation.PAPERDOLL);
						for(ItemInstance item : items)
						{
							item.setEquipped(false);
							item.setLocData(0);
							item.setLocation(ItemLocation.WAREHOUSE);
							item.setJdbcState(JdbcEntityState.UPDATED);
							item.update();
						}
					}
				}

				CharacterVariablesDAO.getInstance().delete(objId, "reflection");

				activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.RepairDone", activeChar));
				return true;
			}
			catch(Exception e)
			{
				_log.error("", e);
				return false;
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rs);
			}
		}
		else
			activeChar.sendMessage(new CustomMessage("common.Admin.Repair.Help", activeChar));

		return false;
	}

	public static String getCharsOnAccount(String myCharName, String accountName)
	{
		List<String> chars = new ArrayList<String>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name=?");
			statement.setString(1, accountName);
          
			rset = statement.executeQuery();
            while (rset.next())
            {
              chars.add(rset.getString("char_name"));
            }
		}
		catch(Exception e)
		{
			_log.error("Error while getting Chars on Account", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
    
		StringBuilder resultBuilder = new StringBuilder();
		for(String charName : chars)
		{
			if(!charName.equalsIgnoreCase(myCharName))
				resultBuilder.append(charName).append(';');
		}
		if (resultBuilder.length() == 0)
			return "";
		return resultBuilder.substring(0, resultBuilder.length() - 1);
	}	
}
