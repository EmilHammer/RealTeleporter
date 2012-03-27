package fr.crafter.tickleman.realteleporter;

import java.util.Date;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.crafter.tickleman.realplugin.RealLocation;

//#################################################################### RealTeleporterPlayerListener
public class RealTeleporterPlayerListener implements Listener
{

	private final RealTeleporterPlugin plugin;
	private HashMap<Player, Long> nextCheck = new HashMap<Player, Long>();

	//------------------------------------------------------------------ RealTeleporterPlayerListener
	public RealTeleporterPlayerListener(RealTeleporterPlugin instance)
	{
		plugin = instance;
	}

	//---------------------------------------------------------------------------------- onPlayerMove
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.hasPermission(player, "realteleporter.teleport")) {
			long time = new Date().getTime();
			Long next = nextCheck.get(event.getPlayer());
			if (next == null) {
				nextCheck.put(player, next = (long)0);
			}
			if (time > next) {
				nextCheck.put(player, time + 100);
				String playerName = player.getName();
				RealTeleporter teleporter = plugin.teleporters.teleporterAt(player);
				if ((teleporter != null) && (teleporter.target != null)) {
					Location playerLocation = player.getLocation();
					String key = RealLocation.getId(playerLocation);
					if (!key.equals(plugin.playerLocation.get(playerName))) {
						plugin.playerLocation.put(playerName, key);
						RealTeleporter target = teleporter.target;
						for (World world : plugin.getServer().getWorlds()) {
							if (world.getName().equals(target.worldName)) {
								float yaw;
								switch (target.direction) {
									case 'E': yaw = 180; break;
									case 'S': yaw = 270; break;
									case 'W': yaw = 0; break;
									default:  yaw = 90; break;
								}
								Location location = new Location(
									world, target.x + .5, target.y, target.z + .5, yaw, 0
								);
								plugin.getLog().info(
									"<" + playerName + "> from "
									+ teleporter.name
									+ " to " + target.name + " ("
									+ target.worldName + "," + target.x + "," + target.y + "," + target.z + "," + yaw
									+ ")"
								);
								player.teleport(location);
								event.setTo(location);
								plugin.playerLocation.put(playerName, target.getLocationKey());
								if (plugin.hasPermission(player, "realteleporter.teleport.showgatename")) {
									player.sendMessage(
										plugin.tr("Teleport from +1 to +2")
										.replace("+1", teleporter.name)
										.replace("+2", target.name)
									);
								}
							}
						}
					}
				} else if (plugin.playerLocation.get(playerName) != null) {
					plugin.playerLocation.remove(playerName);
				}
			}
		}
	}

	//---------------------------------------------------------------------------------- onPlayerQuit
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		nextCheck.remove(event.getPlayer());
	}

}
