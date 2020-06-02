package l2s.gameserver.scripts;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Scripts.ScriptClassAndMethod;
import l2s.gameserver.utils.Strings;

public final class Events
{
	public static boolean onAction(Player player, GameObject obj, boolean shift)
	{
		if(shift)
		{
			if(player.getVarBoolean("noShift"))
				return false;
			ScriptClassAndMethod handler = Scripts.onActionShift.get(obj.getL2ClassShortName());
			if(handler == null && obj.isNpc())
				handler = Scripts.onActionShift.get("NpcInstance");
			if(handler == null && obj.isPet())
				handler = Scripts.onActionShift.get("PetInstance");
			if(handler == null)
				return false;
			return Strings.parseBoolean(Scripts.getInstance().callScripts(player, handler.className, handler.methodName, new Object[] { player, obj }));
		}
		else
		{
			ScriptClassAndMethod handler = Scripts.onAction.get(obj.getL2ClassShortName());
			if(handler == null && obj.isDoor())
				handler = Scripts.onAction.get("DoorInstance");
			if(handler == null)
				return false;
			return Strings.parseBoolean(Scripts.getInstance().callScripts(player, handler.className, handler.methodName, new Object[] { player, obj }));
		}
	}
}