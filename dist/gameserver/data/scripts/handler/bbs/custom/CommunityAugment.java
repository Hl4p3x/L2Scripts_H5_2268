package handler.bbs.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.map.hash.TIntStringHashMap;
import l2s.commons.util.Rnd;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.OptionDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.stats.triggers.TriggerInfo;
import l2s.gameserver.templates.OptionDataTemplate;
import l2s.gameserver.templates.item.WeaponFightType;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;
import l2s.gameserver.utils.VariationUtils;

/**
 * @author Bonux
**/
public class CommunityAugment extends CustomCommunityHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommunityAugment.class);

	private static final int MAX_ROW_COUNT = 5;
	private static final int MAX_COLUMN_COUNT = 3;
	private static final int MAX_SKILLS_ON_PAGE = MAX_ROW_COUNT * MAX_COLUMN_COUNT;

	private static final TIntObjectMap<OptionDataTemplate> EMPTY_VARIATIONS_MAP = new TIntObjectHashMap<>(0);

	private final TIntObjectMap<OptionDataTemplate> warriorSkillVariations = new TIntObjectHashMap<>();
	private final TIntObjectMap<OptionDataTemplate> mageSkillVariations = new TIntObjectHashMap<>();

	@Override
	public String[] getBypassCommands() {
		return new String[]{
			"_cbbsaugment"
		};
	}

	@Override
	protected void doBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";

		if("cbbsaugment".equals(cmd)) {
			if(BBSConfig.AUGMENT_SERVICE_COST_ITEM_ID == 0) {
				player.sendMessage(player.isLangRus() ? "Данный сервис отключен." : "This service disallowed.");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/augment.htm", player);
			html = tpls.get(0);

			StringBuilder content = new StringBuilder();

			ItemInstance equippedWeapon = player.getActiveWeaponInstance();
			if(equippedWeapon == null)
				content.append(tpls.get(120));
			else {
				String cmd2 = st.hasMoreTokens() ? st.nextToken() : "1";
				if ("buy".equalsIgnoreCase(cmd2)) {
					int variationId = 1;
					try {
						variationId = Integer.parseInt(st.nextToken());
					} catch (Exception e) {
						return;
					}

					TIntObjectMap<OptionDataTemplate> variations = getSkillVariations(equippedWeapon);
					if (variations.isEmpty())
						content.append(tpls.get(121));
					else {
						OptionDataTemplate skillsVariation = variations.get(variationId);
						if(skillsVariation == null)
							content.append(tpls.get(122));
						else {
							OptionDataTemplate statsVariation = getRandomStatVariation(equippedWeapon);
							if(statsVariation == null) {
								content.append(tpls.get(122));
								LOGGER.warn(getClass().getSimpleName() + ": Cannot found stats variation for weapon TYPE[" + equippedWeapon.getTemplate().getWeaponFightType() + "!");
							}
							else {
								if(BBSConfig.AUGMENT_SERVICE_COST_ITEM_COUNT > 0 && ItemFunctions.deleteItem(player, BBSConfig.AUGMENT_SERVICE_COST_ITEM_ID, BBSConfig.AUGMENT_SERVICE_COST_ITEM_COUNT, true)) {
									VariationUtils.setVariation(player, equippedWeapon, -1, statsVariation.getId(), skillsVariation.getId());
									player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
									content.append(tpls.get(124));
								} else {
									String noHaveItemBlock = tpls.get(123);
									noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(BBSConfig.AUGMENT_SERVICE_COST_ITEM_COUNT));
									noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(BBSConfig.AUGMENT_SERVICE_COST_ITEM_ID));
									content.append(noHaveItemBlock);
								}
							}
						}
					}
				} else {
					Map<Skill, OptionDataTemplate> augmentSkills = getVariationSkills(equippedWeapon);
					if (augmentSkills.isEmpty())
						content.append(tpls.get(121));
					else {
						int page = 1;
						try {
							page = Integer.parseInt(cmd2);
						} catch (NumberFormatException e) {
							//
						}

						int maxPage = (int) Math.ceil((double) augmentSkills.size() / MAX_SKILLS_ON_PAGE);
						page = Math.max(1, Math.min(page, maxPage));

						String subContentBlock;
						if (BBSConfig.AUGMENT_SERVICE_COST_ITEM_COUNT > 0) {
							subContentBlock = tpls.get(110);
							subContentBlock = subContentBlock.replace("<?fee_item_count?>", Util.formatAdena(BBSConfig.AUGMENT_SERVICE_COST_ITEM_COUNT));
							subContentBlock = subContentBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(BBSConfig.AUGMENT_SERVICE_COST_ITEM_ID));
						} else
							subContentBlock = tpls.get(111);

						String navigationBlock = tpls.get(200);
						navigationBlock = navigationBlock.replace("<?current_page?>", String.valueOf(page));
						navigationBlock = navigationBlock.replace("<?prev_button?>", page > 1 ? tpls.get(210).replace("<?prev_page?>", String.valueOf(page - 1)) : "");
						navigationBlock = navigationBlock.replace("<?next_button?>", page < maxPage ? tpls.get(211).replace("<?next_page?>", String.valueOf(page + 1)) : "");

						StringBuilder rows = new StringBuilder();
						rows.append(tpls.get(300));

						int rowsCount = 0;
						String columns = tpls.get(320);
						Skill[] skills = augmentSkills.keySet().toArray(new Skill[augmentSkills.size()]);
						for (int i = ((page - 1) * MAX_SKILLS_ON_PAGE); i < Math.min(skills.length, (page * MAX_SKILLS_ON_PAGE)); i++) {
							Skill skill = skills[i];
							String column = tpls.get(330);
							column = column.replace("<?skill_level?>", String.valueOf(skill.getDisplayLevel()));
							String skillName = skill.getName(player);
							column = column.replace("<?skill_name?>", skillName.substring(skillName.indexOf(": ") + 2));
							column = column.replace("<?skill_icon?>", skill.getIcon());
							column = column.replace("<?variation_id?>", String.valueOf(augmentSkills.get(skill).getId()));
							if (columns.contains("<?column1?>"))
								columns = columns.replace("<?column1?>", column);
							else if (columns.contains("<?column2?>"))
								columns = columns.replace("<?column2?>", column);
							else if (columns.contains("<?column3?>")) {
								columns = columns.replace("<?column3?>", column);

								String rowBlock = (rowsCount % 2) == 0 ? tpls.get(310) : tpls.get(311);
								rowBlock = rowBlock.replace("<?columns?>", columns);
								rows.append(rowBlock);
								columns = tpls.get(320);
								rowsCount++;
							}
						}

						if (!columns.contains("<?column1?>") || !columns.contains("<?column2?>") || !columns.contains("<?column3?>")) {
							columns = columns.replace("<?column1?>", "");
							columns = columns.replace("<?column2?>", "");
							columns = columns.replace("<?column3?>", "");

							String rowBlock = (rowsCount % 2) == 0 ? tpls.get(310) : tpls.get(311);
							rowBlock = rowBlock.replace("<?columns?>", columns);
							rows.append(rowBlock);
						}

						rows.append(tpls.get(300));

						String contentBlock = tpls.get(100);
						contentBlock = contentBlock.replace("<?sub_content?>", subContentBlock);
						contentBlock = contentBlock.replace("<?pages_navigation?>", navigationBlock);
						contentBlock = contentBlock.replace("<?augments_table?>", rows.toString());

						content.append(contentBlock);
					}
				}
			}

			html = html.replace("<?content?>", content.toString());
		}
		ShowBoardPacket.separateAndSend(html, player);
	}

	@Override
	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {
		//
	}

	@Override
	public void onLoad() {
		super.onLoad();
		loadVariations(warriorSkillVariations, WeaponFightType.WARRIOR);
		loadVariations(mageSkillVariations, WeaponFightType.MAGE);
	}

	private void loadVariations(TIntObjectMap<OptionDataTemplate> variationsMap, WeaponFightType weaponFightType) {
		final int[] skillVariations = weaponFightType == WeaponFightType.MAGE ? BBSConfig.AUGMENT_SERVICE_SKILLS_VARIATIONS_MAGE : BBSConfig.AUGMENT_SERVICE_SKILLS_VARIATIONS_WARRIOR;
		if(skillVariations.length == 0) {
			LOGGER.warn(getClass().getSimpleName() + ": Skills variations is empty for weapon TYPE[" + weaponFightType + "!");
			return;
		}

		final int[] statsVariations = weaponFightType == WeaponFightType.MAGE ? BBSConfig.AUGMENT_SERVICE_STATS_VARIATIONS_MAGE : BBSConfig.AUGMENT_SERVICE_STATS_VARIATIONS_WARRIOR;
		if(statsVariations.length == 0) {
			LOGGER.warn(getClass().getSimpleName() + ": Stats variations is empty for weapon TYPE[" + weaponFightType + "!");
			return;
		}

		for(int optionDataId : skillVariations) {
			OptionDataTemplate skillsOptionData = OptionDataHolder.getInstance().getTemplate(optionDataId);
			if(skillsOptionData == null) {
				LOGGER.warn(getClass().getSimpleName() + ": Cannot found option data ID[" + optionDataId + "]!");
				continue;
			}

			if(skillsOptionData.getSkills().isEmpty() && skillsOptionData.getTriggerList().isEmpty()) {
				LOGGER.warn(getClass().getSimpleName() + ": Skills variation option data ID[" + optionDataId + "] dont have skills and triggers!");
				continue;
			}
			variationsMap.put(skillsOptionData.getId(), skillsOptionData);
		}
	}

	private TIntObjectMap<OptionDataTemplate> getSkillVariations(ItemInstance weapon) {
		if(weapon == null)
			return EMPTY_VARIATIONS_MAP;

		WeaponFightType weaponFightType = weapon.getTemplate().getWeaponFightType();
		return weaponFightType == WeaponFightType.MAGE ? mageSkillVariations : warriorSkillVariations;
	}

	private OptionDataTemplate getRandomStatVariation(ItemInstance weapon) {
		if(weapon == null)
			return null;

		WeaponFightType weaponFightType = weapon.getTemplate().getWeaponFightType();
		int[] statVariationsId = weaponFightType == WeaponFightType.MAGE ? BBSConfig.AUGMENT_SERVICE_STATS_VARIATIONS_MAGE : BBSConfig.AUGMENT_SERVICE_STATS_VARIATIONS_WARRIOR;
		return OptionDataHolder.getInstance().getTemplate(Rnd.get(statVariationsId));
	}

	private Map<Skill, OptionDataTemplate> getVariationSkills(ItemInstance weapon) {
		Map<Skill, OptionDataTemplate> skills = new HashMap<>();
		if(weapon == null)
			return skills;

		TIntObjectMap<OptionDataTemplate> variations = getSkillVariations(weapon);
		for(OptionDataTemplate optionData : variations.valueCollection()) {
			for(Skill skill : optionData.getSkills())
				skills.put(skill, optionData);

			for(TriggerInfo triggerInfo : optionData.getTriggerList()) {
				Skill skill = triggerInfo.getSkill();
				if(skill != null)
					skills.put(skill, optionData);
			}
		}
		return skills;
	}
}