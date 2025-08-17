package me.wither.walldashh;


import com.projectkorra.projectkorra.BendingPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WallDashListener implements Listener {
    public WallDashListener() {
    }

    @EventHandler
    public void OnClick(PlayerInteractEvent e) {

        if(e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = e.getPlayer();

        new WallDash(player);
    }

}
