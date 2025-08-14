package me.wither.walldashh;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.*;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import me.wither.wallgliding.WallGliding;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class WallDash extends ChiAbility implements AddonAbility, Listener {

    private Vector direction;
    //private Vector downvelocity;
    private double power;
    private Listener listener;
    private long time;
    private long duration;
    private Permission perm;
    private Location location;
    private float DAMAGE;

    public void setFields() {
        power = ConfigManager.getConfig().getDouble("ExtraAbilities.Sc1_original.Passives.WallDash.JumpPower");
        //downvelocity = config.getVector("ExtraAbilities.Sc1_original.Passives.WallDash.DownForce");
        DAMAGE = ConfigManager.getConfig().getInt("ExtraAbilities.Sc1_original.Passives.WallDash.Damage");
        duration = ConfigManager.getConfig().getLong("ExtraAbilities.Sc1_original.Passives.WallDash.Duration");

    }

    public static boolean isAgainstWall(Player player) {
        if(player==null) return false;
        Location location = player.getLocation();
        if (location.getBlock().getRelative(BlockFace.NORTH).getType().isSolid()) {
            return true;
        } else if (location.getBlock().getRelative(BlockFace.SOUTH).getType().isSolid()) {
            return true;
        } else {
            return location.getBlock().getRelative(BlockFace.WEST).getType().isSolid() || location.getBlock().getRelative(BlockFace.EAST).getType().isSolid();
        }
    }

    public static boolean ReadytoDo(Player player, BendingPlayer bPlayer) {
        return player.isSneaking() && isAgainstWall(player) && !player.isOnGround() && !player.getGameMode().equals(GameMode.SPECTATOR) && !bPlayer.isChiBlocked() && !bPlayer.isBloodbent() && !bPlayer.isControlledByMetalClips() && !bPlayer.isParalyzed() && bPlayer.isToggledPassives() && !player.isFlying();
    }

    public WallDash(Player player) {
        super(player);

        setFields();

        float pitch = player.getLocation().getPitch();

        if (pitch <= -85 || pitch > 60) {
            return; // too vertical — cancel ability , prevents the ability from turning into wallrun or dropdash
        }


        direction = player.getLocation().getDirection().normalize().clone().multiply(1);


        if(ReadytoDo(player, bPlayer)){

            ParticleEffect.CLOUD.display(player.getLocation(), 5, Math.random(), 0, Math.random(), 0.0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 0.3f);

            player.setVelocity(direction);
        }


        start();

    }


    private void affectTargets() {
        List<Entity> targets = GeneralMethods.getEntitiesAroundPoint(location, 1.05);
        for (Entity target : targets) {

            if(target.getUniqueId() == player.getUniqueId()){
                continue;
            }

            if(target.getType() != EntityType.IRON_GOLEM){
                target.setVelocity(direction.normalize().clone().multiply(2));
            }

            DamageHandler.damageEntity(target , DAMAGE , this);
            target.setFireTicks(0);

            ParticleEffect.CRIT.display(target.getLocation().add(Math.random(),Math.random(),Math.random()), 5, Math.random(), 0, Math.random(), 0.0);
            player.playSound(this.player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 0.5f);

            player.setVelocity(direction.normalize().clone().multiply(-1.5));
            remove();
            return;
        }
    }

    @Override
    public void progress() {
        time = System.currentTimeMillis();
        location = player.getLocation().clone();




        if (!bPlayer.isToggled()) {
            remove();
            return;
        }

        //
        //        downvelocity = GeneralMethods.getDirection(player.getLocation(), player.getLocation().add(0,-0.01,0));
        //
        //        //v=at + v0
        //        downvelocity.add(downvelocity.normalize().multiply(0.08 * time));
        //
        //        if(player.getLocation().getBlock().getRelative(BlockFace.NORTH).getType().isSolid()){
        //            player.setVelocity(downvelocity);
        //        }

        if( time > getStartTime() + 250 && time < getStartTime() + duration){
            affectTargets();
        }

       // if(!WallGliding.ReadytoDo(player,bPlayer)){
        //            remove();
        //            return;
        //        }

        if(time > getStartTime() + duration){
            remove();
        }

    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return 0;
    }

    @Override
    public String getName() {
        return "WallDash";
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void load() {

        listener = new WallDashListener();

        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);

        this.perm = new org.bukkit.permissions.Permission("bending.ability.walldash");
        this.perm.setDefault(PermissionDefault.OP);
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(this.perm);

        //config.addDefault("ExtraAbilities.Sc1_original.Passives.WallDash.DownForce", 1);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Sc1_original.Passives.WallDash.Damage", 4);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Sc1_original.Passives.WallDash.Duration" ,2000L);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Sc1_original.Passives.WallDash.JumpPower", 3.0);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(listener);
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(perm);
    }

    @Override
    public String getAuthor() {
        return "Sc1_original";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "WallDash is an ability helping chi blockers to go up by wall to wall jump, to use, hold sneak(shift) when you are connected to a wall to do wallgliding, then click on anywhere u wish to jump (Note: you cant do it if you look so much down)." +
                "Flying Foot kick: if an enemy is close on your way when u are doing the ability, you will kick him and both will shoot to back";
    }
}
