package l2s.gameserver.network.l2.s2c;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.CharSelectInfoPackage;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.utils.AutoBan;

public class CharacterSelectionInfoPacket extends L2GameServerPacket
{
	// d (SdSddddddddddffdQdddddddddddddddddddddddddddddddddddddddffdddchhd)
	private static final Logger _log = LoggerFactory.getLogger(CharacterSelectionInfoPacket.class);

	private String _loginName;

	private int _sessionId;

	private CharSelectInfoPackage[] _characterPackages;

	public CharacterSelectionInfoPacket(String loginName, int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(loginName);
	}

	public CharSelectInfoPackage[] getCharInfo()
	{
		return _characterPackages;
	}

	@Override
	protected final void writeImpl()
	{
		int size = _characterPackages != null ? _characterPackages.length : 0;

		boolean hi5 = isHF();

		writeD(size);
		writeD(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT); // Максимальное количество персонажей на сервере
		writeC(size >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT ? 0x01 : 0x00); // 0x00 - Разрешить, 0x01 - запретить. Разрешает или запрещает создание игроков
		if(!hi5)
		{
			writeC(0x00);
			writeD(0x02); // 0x01 - Выводит окно, что нужно купить игру, что создавать более 2х чаров. 0х02 - обычное лобби.
			writeC(0x00); // 0x01 - Предлогает купить ПА.
			writeC(0x00); // 140 protocol
		}

		long lastAccess = -1L;
		int lastUsed = -1;
		for(int i = 0; i < size; i++)
			if(lastAccess < _characterPackages[i].getLastAccess())
			{
				lastAccess = _characterPackages[i].getLastAccess();
				lastUsed = i;
			}

		for(int i = 0; i < size; i++)
		{
			CharSelectInfoPackage charInfoPackage = _characterPackages[i];

			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getCharId()); // ?
			writeS(_loginName);
			writeD(_sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0x00); // ??

			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());
			writeD(charInfoPackage.getBaseClassId());

			writeD(Config.REQUEST_ID);

			writeD(charInfoPackage.getX());
			writeD(charInfoPackage.getY());
			writeD(charInfoPackage.getZ());

			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());

			if(hi5)
				writeD(charInfoPackage.getSp());
			else
				writeQ(charInfoPackage.getSp());

			writeQ(charInfoPackage.getExp());
			int lvl = charInfoPackage.getLevel();
			writeF(Experience.getExpPercent(lvl, charInfoPackage.getExp()));
			writeD(lvl);

			writeD(charInfoPackage.getKarma());
			writeD(charInfoPackage.getPk());
			writeD(charInfoPackage.getPvP());

			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);

			if(!hi5)
			{
				writeD(0x00); // unk Ertheia
				writeD(0x00); // unk Ertheia
			}

			if(hi5)
			{
				for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER_HF)
					writeD(charInfoPackage.getPaperdollItemId(PAPERDOLL_ID));
			}
			else
			{
				for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
					writeD(charInfoPackage.getPaperdollItemId(PAPERDOLL_ID));
			}

			if(!hi5)
			{
				writeD(0x00); //Внешний вид оружия (ИД Итема).
				writeD(0x00); //Внешний вид щита (ИД Итема).
				writeD(0x00); //Внешний вид перчаток (ИД Итема).
				writeD(0x00); //Внешний вид верха (ИД Итема).
				writeD(0x00); //Внешний вид низа (ИД Итема).
				writeD(0x00); //Внешний вид ботинок (ИД Итема).
				writeD(0x00); //???
				writeD(0x00); //Внешний вид шляпы (ИД итема).
				writeD(0x00); //Внешний вид маски (ИД итема).

				writeH(0x00); // PaperdollEnchantEffect PAPERDOLL_CHEST
				writeH(0x00); // PaperdollEnchantEffect PAPERDOLL_LEGS
				writeH(0x00); // PaperdollEnchantEffect PAPERDOLL_HEAD
				writeH(0x00); // PaperdollEnchantEffect PAPERDOLL_GLOVES
				writeH(0x00); // PaperdollEnchantEffect PAPERDOLL_FEET
			}

			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR) > 0 ? charInfoPackage.getSex() : charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());

			writeF(charInfoPackage.getMaxHp()); // hp max
			writeF(charInfoPackage.getMaxMp()); // mp max

			writeD(charInfoPackage.getAccessLevel() > -100 ? charInfoPackage.getDeleteTimer() : -1);
			writeD(charInfoPackage.getClassId());
			writeD(i == lastUsed ? 1 : 0);

			writeC(Math.min(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_RHAND), 127));

			if(hi5)
			{
				writeH(charInfoPackage.getPaperdollVariation1Id(Inventory.PAPERDOLL_RHAND));
				writeH(charInfoPackage.getPaperdollVariation2Id(Inventory.PAPERDOLL_RHAND));
			}
			else
			{
				writeD(charInfoPackage.getPaperdollVariation1Id(Inventory.PAPERDOLL_RHAND));
				writeD(charInfoPackage.getPaperdollVariation2Id(Inventory.PAPERDOLL_RHAND));
			}

			int weaponId = charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND);
			if(weaponId == 8190) // Transform id (на оффе отображаются только КВ трансформации или вообще не отображаются ;)
				writeD(301);
			else if(weaponId == 8689)
				writeD(302);
			else
				writeD(0x00);

			//TODO: Pet info?
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeF(0x00);
			writeF(0x00);

			if(!hi5)
			{
				writeD(charInfoPackage.getVitalityPoints() * 7);
				writeD(charInfoPackage.getVitalityPoints() > 0 ? (int) (100 * Config.ALT_VITALITY_RATE) : 100);
				writeD(0x00); // Use Vitality Potions Left

				writeD(charInfoPackage.isAvailable());
				writeC(0x00); // Chaos Festival Winner
				writeC(charInfoPackage.isHero()); // hero glow
				writeC(0x01); // show hair accessory if enabled
			}
			else
				writeD(charInfoPackage.getVitalityPoints());
		}
	}

	public static CharSelectInfoPackage[] loadCharacterSelectInfo(String loginName)
	{
		CharSelectInfoPackage charInfopackage;
		List<CharSelectInfoPackage> characterList = new ArrayList<CharSelectInfoPackage>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters AS c LEFT JOIN character_subclasses AS cs ON (c.obj_Id=cs.char_obj_id AND cs.active=1) WHERE account_name=? LIMIT 7");
			statement.setString(1, loginName);
			rset = statement.executeQuery();
			while(rset.next()) // fills the package
			{
				charInfopackage = restoreChar(rset);
				if(charInfopackage != null)
					characterList.add(charInfopackage);
			}
		}
		catch(Exception e)
		{
			_log.error("could not restore charinfo:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
	}

	private static int restoreBaseClassId(int objId)
	{
		int classId = 0;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id FROM character_subclasses WHERE char_obj_id=? AND isBase=1");
			statement.setInt(1, objId);
			rset = statement.executeQuery();
			while(rset.next())
				classId = rset.getInt("class_id");
		}
		catch(Exception e)
		{
			_log.error("could not restore base class id:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return classId;
	}

	private static CharSelectInfoPackage restoreChar(ResultSet chardata)
	{
		CharSelectInfoPackage charInfopackage = null;
		try
		{
			int objectId = chardata.getInt("obj_Id");
			int classid = chardata.getInt("class_id");
			int baseClassId = classid;
			boolean useBaseClass = chardata.getInt("isBase") > 0;
			if(!useBaseClass)
				baseClassId = restoreBaseClassId(objectId);
			Race race = ClassId.VALUES[baseClassId].getRace();
			String name = chardata.getString("char_name");
			charInfopackage = new CharSelectInfoPackage(objectId, name);
			charInfopackage.setLevel(chardata.getInt("level"));
			charInfopackage.setMaxHp(chardata.getInt("maxHp"));
			charInfopackage.setCurrentHp(chardata.getDouble("curHp"));
			charInfopackage.setMaxMp(chardata.getInt("maxMp"));
			charInfopackage.setCurrentMp(chardata.getDouble("curMp"));

			charInfopackage.setX(chardata.getInt("x"));
			charInfopackage.setY(chardata.getInt("y"));
			charInfopackage.setZ(chardata.getInt("z"));
			charInfopackage.setPk(chardata.getInt("pkkills"));
			charInfopackage.setPvP(chardata.getInt("pvpkills"));

			charInfopackage.setFace(chardata.getInt("face"));
			charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
			charInfopackage.setHairColor(chardata.getInt("haircolor"));
			charInfopackage.setSex(chardata.getInt("sex"));

			charInfopackage.setExp(chardata.getLong("exp"));
			charInfopackage.setSp(chardata.getInt("sp"));
			charInfopackage.setClanId(chardata.getInt("clanid"));

			charInfopackage.setKarma(chardata.getInt("karma"));
			charInfopackage.setRace(race.ordinal());
			charInfopackage.setClassId(classid);
			charInfopackage.setBaseClassId(baseClassId);
			long deletetime = chardata.getLong("deletetime");
			int deletedays = 0;
			if(Config.DELETE_DAYS > 0)
				if(deletetime > 0)
				{
					deletetime = (int) (System.currentTimeMillis() / 1000 - deletetime);
					deletedays = (int) (deletetime / 3600 / 24);
					if(deletedays >= Config.DELETE_DAYS)
					{
						CharacterDAO.getInstance().deleteCharByObjId(objectId);
						return null;
					}
					deletetime = Config.DELETE_DAYS * 3600 * 24 - deletetime;
				}
				else
					deletetime = 0;
			charInfopackage.setDeleteTimer((int) deletetime);
			charInfopackage.setLastAccess(chardata.getLong("lastAccess") * 1000L);
			charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
			charInfopackage.setVitalityPoints(Math.max(0, Math.min(20000, chardata.getInt("vitality") + (int) ((System.currentTimeMillis() - charInfopackage.getLastAccess()) / 1000L / 15.))));

			if(charInfopackage.getAccessLevel() < 0 && !AutoBan.isBanned(objectId))
				charInfopackage.setAccessLevel(0);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}

		return charInfopackage;
	}
}