package data.scripts.weapons;

import java.awt.Color;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
//import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import org.magiclib.util.MagicTargeting;
import org.magiclib.util.MagicTargeting.targetSeeking;

public class zp_arti_plugin extends BaseEveryFrameCombatPlugin{

    private static final int RANGE_VAR = -300; // charges drift to stop in 300 units
    private static final int RANGE_FIRE = 700;

    private MissileAPI missile;
    private float stop_range;
    //private float proj_speed;
    private boolean slow_point = false;
    private Vector2f fire_point;
    private IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);
    private CombatEntityAPI target = null;
    private float eccm = 1.2f;

    public zp_arti_plugin(DamagingProjectileAPI projectile, Vector2f point){
        this.missile = (MissileAPI)projectile;
        this.stop_range = projectile.getWeapon().getRange() + RANGE_VAR;
        this.stop_range += (Math.random() * 100) - 50; // Add minor variance to stop point
        this.fire_point = point;
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            eccm = 0.6f;
        }
    }

    public void init(CombatEngineAPI engine) {
		
	}

    @Override
    public void advance(float amount, List<InputEventAPI> events){

        if (Global.getCombatEngine() == null) {
			return;
		}
		if (Global.getCombatEngine().isPaused()) {
			amount = 0f;
		}
		if (missile == null || missile.didDamage() || missile.isFading() || !Global.getCombatEngine().isEntityInPlay(missile)) {
			Global.getCombatEngine().removePlugin(this);
			return;
		}

        interval.advance(amount);

        /*if(bip < missile.getFlightTime()){
            MagicRender.objectspace(Global.getSettings().getSprite("fx","zp_charge_glow"), missile, new Vector2f(0,0), new Vector2f(), new Vector2f(12,17), new Vector2f(),
                0, 0, true, new Color(255,0,0,230), true, 0.2f, 0.2f, 0.2f, true);
            bip++;
        }*/

        if(interval.intervalElapsed()){
            if(slow_point && missile.getVelocity().length() > 1){
                missile.getVelocity().scale(0.8f);
            }else if(MathUtils.getDistance(missile, fire_point) > stop_range){
                slow_point = true;
            }

            if(missile.isArmed()){
                if(target == null || MathUtils.getDistance(missile, target) > RANGE_FIRE+100){
                    target = MagicTargeting.pickTarget(missile, targetSeeking.IGNORE_SOURCE, RANGE_FIRE, 360, 0, 1, 2, 3, 4, true);
                }
                if(target != null){
                    if(MathUtils.getDistance(missile, target) < RANGE_FIRE + 500){
                        for(MissileAPI potential_flare : AIUtils.getNearbyEnemyMissiles(missile, RANGE_FIRE)){
                            if(potential_flare.isFlare()){
                                if(Math.random()<eccm/2) target = potential_flare;
                                break;
                            }
                        }
                        float angle = VectorUtils.getAngle(missile.getLocation(), MathUtils.getRandomPointInCircle(target.getLocation(), target.getCollisionRadius()*eccm));
                        CombatEngineAPI engine = Global.getCombatEngine();
                        Global.getSoundPlayer().playSound("hellbore_fire", 1, 1, missile.getLocation(), new Vector2f(0,0));
                        engine.spawnProjectile(missile.getSource(), missile.getWeapon(), "zp_artillery_charge", missile.getLocation(), angle, new Vector2f(0,0));
                        engine.spawnExplosion(missile.getLocation(), missile.getVelocity(), new Color(245,180,40,255), 12, 0.6f);
                        engine.removeEntity(missile);
                        Global.getCombatEngine().removePlugin(this);
                    }
                }
            }
        }

    }

}
