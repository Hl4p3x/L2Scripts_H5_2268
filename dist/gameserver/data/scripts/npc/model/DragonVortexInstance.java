package npc.model;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author pchayka
 */

public final class DragonVortexInstance extends NpcInstance
{
	private final int[] bosses = { 25718, 25719, 25720, 25721, 25722, 25723, 25724 };
	private NpcInstance boss;
	
	private List<NpcInstance> bosses_list = new ArrayList<NpcInstance>();
	private List<NpcInstance> temp_list = new ArrayList<NpcInstance>();
	
	public DragonVortexInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("request_boss"))
		{

			if(ItemFunctions.getItemCount(player, 17248) > 0)
			{
				if(validateBosses())
				{
					ItemFunctions.deleteItem(player, 17248, 1, true);
					boss = NpcUtils.spawnSingle(bosses[Rnd.get(bosses.length)], Location.coordsRandomize(getLoc(), 300, 600), getReflection());
					bosses_list.add(boss);
					showChatWindow(player, "default/32871-1.htm");
				}
				else
					showChatWindow(player, "default/32871-3.htm");
				
			}
			else
				showChatWindow(player, "default/32871-2.htm");
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	private boolean validateBosses()
	{
		if(bosses_list == null || bosses_list.isEmpty())
			return true;
			
		temp_list.addAll(bosses_list);	
		
		for(NpcInstance npc : temp_list)
		{
			if(npc == null || npc.isDead())
				bosses_list.remove(npc);
		}
		
		temp_list.clear();
		
		if(bosses_list.size() >= 15)
			return false;
			
		return true;	
	}
}