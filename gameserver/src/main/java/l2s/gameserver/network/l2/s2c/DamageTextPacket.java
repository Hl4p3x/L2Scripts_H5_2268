package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

/**
esli xochew po off, to sobstvenno peredaew znachenija crit miss blocked magic
magic + blocked = failed
font color was pokazhy
###Damage On Screen System###
#Enable Damage on Screen system. If true, make sure everybody have the damagetext.utx file in their client!
EnableDamageOnScreen = True

#Font ID for the damage system, 1-8 ; small to large 
DamageOnScreenFontId = 3

#Font color, RBGA format. In decimal, to determain HEX color find here -> https://www.lineage2.es/misc/l2-user_name_color.php and convert from hex to decimal here -> http://www.binaryhexconverter.com/hex-to-decimal-converter
#Font color give damage (Default white)
OnScreenDamageGiven = 16777215

#Font clolor take damage (Default red)
OnScreenDamageReceived = 16711680
#Custom Strings are also available if needed.
String customString - zamenit cifry na liuboe customnui String
String ucstomTexture, int texture_x, int texture_y, int texture_height, int texture_width

icon_name, texture_x i y -> nado tebe najti sobstvenno eto 2D polozhenija na ekrane, tyt nyzhnu testu primerov y menja net. height/width eto tu znaew 16x16 vrode no mozhew yvelichit'
**/
public class DamageTextPacket extends L2GameServerPacket
{
	int _font_id;
	int _font_color;
	int _victim;
	int _damage;
	int _crit;
	int _miss;
	int _blocked;
	int _magic;
	String _customString;
	
	// custom texture data
	String _customTexture;
	int _texture_x;
	int _texture_y;
	int _texture_height;
	int _texture_width;
	
	public DamageTextPacket(int victim_id, long damage, boolean crit, boolean miss, boolean blocked, boolean magic, int font_id, int font_color, String customString, String ucstomTexture, int texture_x, int texture_y, int texture_height, int texture_width)
	{
		_font_id = font_id;
		_font_color = font_color;
		_victim = victim_id;
		_damage = (int)damage;
		_crit = crit ? 1 : 0;
		_miss = miss ? 1 : 0;
		_blocked = blocked ? 1 : 0;
		_magic = magic ? 1 : 0;
		_customString = customString;
		_customTexture = ucstomTexture;
		_texture_x = texture_x;
		_texture_y = texture_y;
		_texture_height = texture_height;
		_texture_width = texture_width;
	}

	@Override
	protected boolean writeOpcodes()
	{
		return true;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFF);
		writeC(8);
		
		writeD(_font_id);
		writeD(_font_color);

		writeD(_victim);		
		writeD(_damage);
		
		writeC(_crit);
		writeC(_miss);
		writeC(_blocked);
		writeC(_magic);
		
		writeS(_customString);
		
		writeS(_customTexture);
		writeD(_texture_x);
		writeD(_texture_y);
		writeD(_texture_height);
		writeD(_texture_width);
	}

	@Override
	public L2GameServerPacket packet(Player player) {
		if (!player.isDmgOnScreenEnable())
			return null;
		return super.packet(player);
	}
}
