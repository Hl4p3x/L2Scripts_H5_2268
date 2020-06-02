package l2s.gameserver.network.l2.components;

/**
 * @author VISTALL
 * @date  12:48/29.12.2010
 */
public enum ChatType
{
	ALL,  //0 WHITE
	SHOUT, //1    ! ORANGE
	TELL,  //2    " PURPLE
	PARTY,  //3   # GREEN
	CLAN,   //4    @ BLUE
	GM,      //5 WHITE
	PETITION_PLAYER, //6   NOT IN USE
	PETITION_GM,    //7   * NOT IN USE
	TRADE,          //8  + - PINK
	ALLIANCE,       //9   $ BRIGHT GREED
	ANNOUNCEMENT,    //10 BRIGHT BLUE
	SYSTEM_MESSAGE,  //11 Dont use with plain string... gives critical error :D NOT IN USE
	L2FRIEND, //NOT IN USE
	MSNCHAT, //NOT IN USE
	PARTY_ROOM,    //14 NOT IN USE
	COMMANDCHANNEL_ALL, //15 `` BRIGHT RED
	COMMANDCHANNEL_COMMANDER,  //16  ` BRIGHT YELLOW
	HERO_VOICE,                //17 % NOT IN USE
	CRITICAL_ANNOUNCE,   //18 NOT IN USE
	SCREEN_ANNOUNCE, //NOT IN USE
	BATTLEFIELD, //20   ^ YELLOW
	MPCC_ROOM, //NOT IN USE
	NPC_ALL,  //NOT IN USE
	NPC_SHOUT, // 23 
	BLUE_UNK,	//24
	WORLD; // 25 - 

	public static final ChatType[] VALUES = values();

	public static ChatType getTypeFromName(String name)
	{
		for (ChatType type : values())
		{
			if (type.name().equalsIgnoreCase(name))
				return type;
		}

		return ALL;
	}
}