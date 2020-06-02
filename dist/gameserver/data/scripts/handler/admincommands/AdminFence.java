package handler.admincommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.commons.map.hash.TIntStringHashMap;
import l2s.gameserver.dao.FencesDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.FenceState;
import l2s.gameserver.model.instances.FenceInstance;
import l2s.gameserver.utils.FenceUtils;
import l2s.gameserver.utils.HtmlUtils;

/**
 * @author Sahar, Nik64
 * @reworked by Bonux
 */
public class AdminFence extends ScriptAdminCommand
{
	private static enum Commands
	{
		admin_addfence,
		admin_setfencestate,
		admin_removefence,
		admin_listfence,
		admin_gofence
	};

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player player)
	{
		Commands cmd = (Commands) comm;
		switch(cmd)
		{
			case admin_addfence:
			{
				try
				{
					final int width = Integer.parseInt(wordList[1]);
					final int length = Integer.parseInt(wordList[2]);
					final int height = Integer.parseInt(wordList[3]);
					if(width < 100 || length < 100)
					{
						player.sendMessage("Width and length values must be equal to or greater than 100.");
						sendHtml(player, 0);
						return false;
					}
					if(height < 1 || height > 3)
					{
						player.sendMessage("The range for height can only be 1-3.");
						sendHtml(player, 0);
						return false;
					}
					FenceInstance fence = FenceUtils.spawnFence("Spawned by GM: \"" + player.getName() + "\"", player.getX(), player.getY(), player.getZ(), width, length, height, FenceState.CLOSED, player.getReflection());
					if(fence != null)
					{
						// Сохраняем заборы только в реальном мире.
						FencesDAO.getInstance().insert(fence);
						player.sendMessage("Fence added succesfully.");
					}
					else
						player.sendMessage("Fence not added.");
				}
				catch(Exception e)
				{
					player.sendMessage("Format must be: //addfence <width> <length> <height>");
				}
				sendHtml(player, 0);
				break;
			}
			case admin_setfencestate:
			{
				try
				{
					final int objId = Integer.parseInt(wordList[1]);
					final int fenceTypeOrdinal = Integer.parseInt(wordList[2]);
					if(fenceTypeOrdinal < 0 || fenceTypeOrdinal >= FenceState.VALUES.length)
					{
						player.sendMessage("Specified FenceType is out of range. Only 0-" + (FenceState.VALUES.length - 1) + " are permitted.");
					}
					else
					{
						final GameObject obj = GameObjectsStorage.findObject(objId);
						if(obj instanceof FenceInstance)
						{
							final FenceInstance fence = (FenceInstance) obj;
							final FenceState state = FenceState.VALUES[fenceTypeOrdinal];
							fence.setState(state);
							player.sendMessage("Fence " + fence.getName() + "[" + fence.getObjectId() + "]'s state has been changed to " + state.toString());
						}
						else
						{
							player.sendMessage("Target is not a fence.");
						}
					}
				}
				catch(Exception e)
				{
					player.sendMessage("Format mustr be: //setfencestate <fenceObjectId> <fenceState>");
				}
				sendHtml(player, 0);
				break;
			}
			case admin_removefence:
			{
				try
				{
					final int objId = Integer.parseInt(wordList[1]);
					final GameObject obj = GameObjectsStorage.findObject(objId);
					if(obj instanceof FenceInstance)
					{
						final FenceInstance fence = (FenceInstance) obj;
						fence.deleteMe();
						FencesDAO.getInstance().delete(fence);
						player.sendMessage("Fence removed succesfully.");
					}
					else
					{
						player.sendMessage("Target is not a fence.");
					}
				}
				catch(Exception e)
				{
					player.sendMessage("Invalid object ID or target was not found.");
				}
				sendHtml(player, 0);
				break;
			}
			case admin_listfence:
			{
				int page = 0;
				if(wordList.length > 1)
				{
					page = Integer.parseInt(wordList[1]);
				}
				sendHtml(player, page);
				break;
			}
			case admin_gofence:
			{
				try
				{
					final int objId = Integer.parseInt(wordList[1]);
					final GameObject obj = GameObjectsStorage.findObject(objId);
					if(obj != null)
					{
						player.teleToLocation(obj.getLoc(), obj.getReflection());
					}
				}
				catch(Exception e)
				{
					player.sendMessage("Invalid object ID or target was not found.");
				}
				break;
			}
		}
		
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private static void sendHtml(Player player, int page)
	{
		final TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("admin/fences.htm", player);

		String html = tpls.get(0);

		List<FenceInstance> fences = new ArrayList<FenceInstance>(GameObjectsStorage.getFences());

		Collections.sort(fences, (f1, f2) ->
		{
			return Integer.compare(f1.getObjectId(), f2.getObjectId());
		});

		final int maxPage = fences.size() / 10;
		page = Math.min(page, maxPage);

		StringBuilder fsb = new StringBuilder();

		if(fences.isEmpty())
			fsb.append("There are no fences.");
		else
		{
			for(int i = (page * 10); i < Math.min((page + 1) * 10, fences.size()); i++)
			{
				FenceInstance fence = fences.get(i);
				if(fence == null)
					continue;

				String block = tpls.get(1);
				block = block.replace("<?fence_name?>", fence.getName());
				block = block.replace("<?fence_object_id?>", String.valueOf(fence.getObjectId()));
				fsb.append(block);
			}
		}

		String prevButton = page > 1 ? HtmlUtils.htmlButton("<", "bypass -h admin_listfence" + (page - 1), 40, 15) : "";
		String nextButton = page < maxPage ? HtmlUtils.htmlButton(">", "bypass -h admin_listfence" + (page + 1), 40, 15) : "";

		html = html.replace("<?fences?>", fsb.toString());
		html = html.replace("<?prev_button?>", prevButton);
		html = html.replace("<?page?>", String.valueOf(page));
		html = html.replace("<?next_button?>", nextButton);

		HtmlUtils.sendHtm(player, html);
	}
}