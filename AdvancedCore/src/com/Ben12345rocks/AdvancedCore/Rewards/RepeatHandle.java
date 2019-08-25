package com.Ben12345rocks.AdvancedCore.Rewards;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.configuration.ConfigurationSection;

import com.Ben12345rocks.AdvancedCore.AdvancedCorePlugin;
import com.Ben12345rocks.AdvancedCore.UserManager.User;

import lombok.Getter;
import lombok.Setter;

public class RepeatHandle {
	public RepeatHandle(Reward reward) {
		this.reward = reward;
		timer = new Timer();
		ConfigurationSection data = reward.getConfig().getConfigData().getConfigurationSection("Repeat");
		if (data != null) {
			enabled = data.getBoolean("Enabled", false);
			timeBetween = data.getLong("TimeBetween", 0);
			amount = data.getInt("Amount", -1);
			repeatOnStartup = data.getBoolean("RepeatOnStartup", false);
			autoStop = data.getBoolean("AutoStop", true);
		}
	}

	@Getter
	private Reward reward;
	@Getter
	@Setter
	private boolean enabled = false;
	@Getter
	@Setter
	private long timeBetween = 0;
	@Getter
	@Setter
	private int amount = -1;
	@Getter
	@Setter
	private boolean repeatOnStartup = false;
	@Getter
	@Setter
	private boolean autoStop = false;

	private Timer timer;

	public void giveRepeat(User user) {
		if (repeatOnStartup) {
			return;
		}
		AdvancedCorePlugin.getInstance().debug("Giving repeat reward in " + timeBetween);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (amount > 0) {
					int cAmount = user.getRepeatAmount(reward);
					if (cAmount >= amount) {
						user.setRepeatAmount(reward, 0);
						cancel();
						return;
					}
					// add amount
					user.setRepeatAmount(reward, cAmount + 1);
					if (autoStop) {
						if (!reward.canGiveReward(user, new RewardOptions())) {
							user.setRepeatAmount(reward, 0);
							cancel();
							return;
						} else {
							giveReward(user, true);
							return;
						}
					} else {
						giveReward(user, false);
						return;
					}

				} else {
					if (autoStop) {
						if (!reward.canGiveReward(user, new RewardOptions())) {
							cancel();
							return;
						} else {
							giveReward(user, true);
							return;
						}
					} else {
						giveReward(user, false);
						return;
					}
				}
			}
		}, timeBetween);
	}

	public void giveReward(User user, boolean bypassRequirement) {
		AdvancedCorePlugin.getInstance()
				.debug("Giving repeat reward " + reward.getName() + " for " + user.getPlayerName());
		if (bypassRequirement) {
			reward.giveReward(user, new RewardOptions().setIgnoreRequirements(false).setIgnoreChance(false));
		} else {
			reward.giveReward(user, new RewardOptions());
		}
		giveRepeat(user);
	}

}