package handler.bbs.custom.communitybuffer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.utils.Language;

/**
 * @author Bonux
**/
public class CommunityBufferParser extends AbstractParser<CommunityBufferHolder> {
	private static CommunityBufferParser _instance = new CommunityBufferParser();

	public static CommunityBufferParser getInstance() {
		return _instance;
	}

	private CommunityBufferParser() {
		super(CommunityBufferHolder.getInstance());
	}

	@Override
	public File getXMLPath() {
		return new File(Config.DATAPACK_ROOT, "data/bbs_buffs.xml");
	}

	@Override
	public String getDTDFileName() {
		return "bbs_buffs.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception {
		for (Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();) {
			Element element = iterator.next();
			if("available_skills".equalsIgnoreCase(element.getName())) {
				for (Iterator<Element> secondIterator = element.elementIterator(); secondIterator.hasNext(); ) {
					Element secondElement = secondIterator.next();
					if ("skills_group".equalsIgnoreCase(secondElement.getName())) {
						parseSkillsGroup(secondElement, false).forEach(skill -> getHolder().addAvailableSkill(skill));
					} else if ("skill".equalsIgnoreCase(secondElement.getName())) {
						getHolder().addAvailableSkill(parseSkill(secondElement, 1D, -1, false));
					} else if ("premium_skills".equalsIgnoreCase(secondElement.getName())) {
						for (Iterator<Element> thirdIterator = secondElement.elementIterator(); thirdIterator.hasNext();) {
							Element thirdElement = thirdIterator.next();
							if ("skills_group".equalsIgnoreCase(thirdElement.getName())) {
								parseSkillsGroup(thirdElement, true).forEach(skill -> getHolder().addAvailablePremiumSkill(skill));
							} else if ("skill".equalsIgnoreCase(thirdElement.getName())) {
								getHolder().addAvailablePremiumSkill(parseSkill(thirdElement, 1D, -1, true));
							}
						}
					}
				}
			} else if("base_buff_sets".equalsIgnoreCase(element.getName())) {
				for (Iterator<Element> secondIterator = element.elementIterator("buff_set"); secondIterator.hasNext(); ) {
					Element secondElement = secondIterator.next();
					int id = parseInt(secondElement, "id");
					BuffSet buffSet = new BuffSet(-1, id, null);
					for (Iterator<Element> thirdIterator = secondElement.elementIterator(); thirdIterator.hasNext(); ) {
						Element thirdElement = thirdIterator.next();
						if ("name".equalsIgnoreCase(thirdElement.getName())) {
							for (Iterator<Element> fourthIterator = thirdElement.elementIterator(); fourthIterator.hasNext(); ) {
								Element fourthElement = fourthIterator.next();
								Language lang = Language.getLanguage(fourthElement.getName(), null);
								if (lang != null)
									buffSet.addName(lang, fourthElement.getTextTrim());
							}
						} else if ("skills".equalsIgnoreCase(thirdElement.getName())) {
							for (Iterator<Element> fourthIterator = thirdElement.elementIterator(); fourthIterator.hasNext(); ) {
								buffSet.getSkills().add(parseInt(fourthIterator.next(), "id"));
							}
						}
					}
					getHolder().addBuffSet(buffSet);
				}
			}
		}
	}

	private List<BuffSkill> parseSkillsGroup(Element element, boolean premium) throws Exception {
		List<BuffSkill> skills = new ArrayList<>();
		double timeModifier = parseDouble(element, "time_modifier", 1D);
		int timeAssign = parseInt(element, "time_assign", -1);
		for (Iterator<Element> secondIterator = element.elementIterator("skill"); secondIterator.hasNext(); ) {
			skills.add(parseSkill(secondIterator.next(), timeModifier, timeAssign, premium));
		}
		return skills;
	}

	private BuffSkill parseSkill(Element element, double baseTimeModifier, int baseTimeAssign, boolean premium) throws Exception {
		int id = parseInt(element, "id");
		int level = parseInt(element, "level", -1);
		double timeModifier = parseDouble(element, "time_modifier", baseTimeModifier);
		int timeAssign = parseInt(element, "time_assign", baseTimeAssign);
		return BuffSkill.makeBuffSkill(id, level, timeModifier, timeAssign, premium);
	}
}
