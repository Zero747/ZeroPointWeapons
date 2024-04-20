package data.scripts.weapons;

import java.util.List;
 
import org.lwjgl.util.vector.Vector2f;
 
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
 
public class zp_resonanceCascadeEffect implements EveryFrameWeaponEffectPlugin {
 
    public static float TARGET_RIFT_RANGE = 20f; // Variance in rift spawning on target
    public static float RIFT_RANGE = 50f; // how far random rifts can scatter
    
    protected IntervalUtil interval = new IntervalUtil(0.8f, 1.2f);
    
    public zp_resonanceCascadeEffect() {
        interval.setElapsed((float) Math.random() * interval.getIntervalDuration());
    }
    
    //public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        List<BeamAPI> beams = weapon.getBeams();
        if (beams.isEmpty()) return;
        BeamAPI beam = beams.get(0);
        if (beam.getBrightness() < 1f) return;
    
        interval.advance(amount * 2f);
        if (interval.intervalElapsed()) {
            if (beam.getLengthPrevFrame() < 10) return;
            
            Vector2f loc;
            CombatEntityAPI target = beam.getDamageTarget();
            if (target == null) {
                loc = pickNoTargetDest(beam, beam.getWeapon(), engine);
            } else {
                loc = beam.getRayEndPrevFrame();
            }

            spawnMine(beam.getSource(), Misc.getPointWithinRadius(loc, RIFT_RANGE));
        }
    }
    
    public void spawnMine(ShipAPI source, Vector2f mineLoc) {
        CombatEngineAPI engine = Global.getCombatEngine();
        
        
        //Vector2f currLoc = mineLoc;
        MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null, 
                                                              "riftbeam_minelayer", 
                                                              mineLoc, 
                                                              (float) Math.random() * 360f, null);
        if (source != null) {
            Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
                                            source, WeaponType.MISSILE, false, mine.getDamage());
        }
        
        
        float fadeInTime = 0.05f;
        mine.getVelocity().scale(0);
        mine.fadeOutThenIn(fadeInTime);
        
        float liveTime = 0f;
        //liveTime = 0.01f;
        mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
        mine.addDamagedAlready(source);
        mine.setNoMineFFConcerns(true);
    }
 
    public Vector2f pickNoTargetDest(BeamAPI beam, WeaponAPI weapon, CombatEngineAPI engine) {
        Vector2f from = beam.getFrom();
        Vector2f to = beam.getRayEndPrevFrame();
        float length = beam.getLengthPrevFrame();
        
        float f = 0.25f + (float) Math.random() * 0.75f;
        Vector2f loc = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(from, to));
        loc.scale(length * f);
        Vector2f.add(from, loc, loc);
        
        return Misc.getPointWithinRadius(loc, RIFT_RANGE);
    }
    
 
}