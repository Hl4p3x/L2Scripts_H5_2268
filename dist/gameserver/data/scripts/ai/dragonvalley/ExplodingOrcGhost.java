package ai.dragonvalley;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author Aristo AI моба 22818. После спавна взрывается через 3 секунды с уроном.
 */
public class ExplodingOrcGhost extends Fighter {

    private Skill SELF_DESTRUCTION = SkillHolder.getInstance().getSkill(6850, 1);

    public ExplodingOrcGhost(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtSpawn() {
        ThreadPoolManager.getInstance().schedule(new StartSelfDestructionTimer(getActor()), 3000L);
        super.onEvtSpawn();
    }

    private class StartSelfDestructionTimer extends RunnableImpl {

        private NpcInstance _npc;

        public StartSelfDestructionTimer(NpcInstance npc) {
            _npc = npc;
        }

        @Override
        public void runImpl() {
            _npc.abortAttack(true, false);
            _npc.abortCast(true, false);
            _npc.doCast(SELF_DESTRUCTION, _actor, true);
        }
    }

}
