package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
//import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;


public class zp_arti_fire implements OnFireEffectPlugin{

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float speedMult = 0.5f + 0.25f * (float) Math.random();
		projectile.getVelocity().scale(speedMult);
		
		float angVel = (float) (Math.signum((float) Math.random() - 0.5f) * 
						(0.5f + Math.random()) * 220f);
		projectile.setAngularVelocity(angVel);

		engine.addPlugin(new zp_arti_plugin(projectile, projectile.getSpawnLocation()));
		
		/*if (projectile instanceof MissileAPI) {
			MissileAPI missile = (MissileAPI) projectile;
			float flightTimeMult = 0.25f + 0.75f * (float) Math.random();
			missile.setMaxFlightTime(missile.getMaxFlightTime() * flightTimeMult);
		}
		
		if (weapon != null) {
			float delay = 0.25f + 0.75f * (float) Math.random();
			weapon.setRefireDelay(delay);
		}*/
		
	}
}
