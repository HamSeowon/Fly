package a;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import me.onebone.economyapi.EconomyAPI;

public class Main extends PluginBase implements Listener{
	public Config config;
	private static Main instance;
	public Config np;
	private double min;
	@Override
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getLogger().info("PayForFly Enabled!-");
		this.getDataFolder().mkdir();
		this.np=new Config(getDataFolder()+"/np.yml",Config.YAML);
		this.config=new Config(getDataFolder()+"/config.yml",Config.YAML);
		if(config.exists("flycost")){
			this.saveResource("config.yml");
		}else if(!(config.exists("flycost"))){
			config.set("flycost",1000);
		}
		config.save();
		instance=this;
	}
	@Override
	public void onDisable(){
		config.save();
		np.save();
	}
	public static Main getInstance(){
		return instance;
	}
	@SuppressWarnings("deprecation")
	@Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		Player player = (Player)sender; 
		if(label.equalsIgnoreCase("fly")){
			 if(sender instanceof Player){
				 sender.sendMessage("플레이어만 사용가능한 명령어입니다.");
				 return false;
			 }else{
				 if(!np.exists(player.getName())){
					 np.set(player.getName(),1);
				 }
				 try {
					min=Double.parseDouble(args[0]);
				} catch (NumberFormatException e) {
					sender.sendMessage("숫자만을 입력해주세요.");
					return false;
					}
				double flytime = Double.parseDouble(np.get(player.getName()).toString());
				double flycost = Double.parseDouble(config.get("flycost").toString());
				 if(args.length==0){
					 if(EconomyAPI.getInstance().myMoney(player)>=flycost){
						 if(!np.exists(player.getName().toLowerCase())){
							 np.set(player.getName().toLowerCase(),60);
						 this.getServer().getScheduler().scheduleDelayedRepeatingTask(this,new Runnable() {
							double sec=60;
							@Override
							public void run() {
								player.setAllowFlight(true);
								if(sec>0){
									np.set(player.getName(),sec);
									player.sendTip(""+sec);
									sec--;
									np.save();
								}else if(sec==0){
									player.setAllowFlight(false);
									player.sendMessage("플라이 시간이 만료되었습니다.");
									Main.getInstance().getServer().getScheduler().cancelAllTasks();
									np.remove(player.getName());
								}
								
							}
						},0,20);
						 }else if(np.exists(player.getName().toLowerCase())){
							 this.getServer().getScheduler().scheduleDelayedRepeatingTask(this, new Runnable() {
								 double sec=flytime+60;
								 @Override
									public void run() {
										player.setAllowFlight(true);
										if(sec>0){
											sec--;
											np.set(player.getName(),sec);
											player.sendTip(""+sec);
											np.save();
										}else if(sec==0){
											player.setAllowFlight(false);
											player.sendMessage("플라이 시간이 만료되었습니다.");
											Main.getInstance().getServer().getScheduler().cancelAllTasks();
											np.remove(player.getName());
										}
										
									}
							},0,20);
						 }
					 }else if(EconomyAPI.getInstance().myMoney(player)<flycost){
						 player.sendMessage("금액이 $"+flycost+" 만큼 부족합니다.");
						 return false;
					 }
				 }else if(args.length==1){
					 if(EconomyAPI.getInstance().myMoney(player)>=flycost*min){
						 this.getServer().getScheduler().scheduleDelayedRepeatingTask(this,new Runnable(){
							 double sec=min*60;
							 @Override
							public void run() {
								player.setAllowFlight(true);
								if(sec>0){
									np.set(player.getName(),sec);
									player.sendMessage(""+sec);
									this.sec--;
									np.save();
								}else if(sec==0){
									player.setAllowFlight(false);
									player.sendMessage("플라이 시간이 만료되었습니다.");
									Main.getInstance().getServer().getScheduler().cancelAllTasks();
								}
							}
							 
						 },0,20);
					 }else if(np.exists(player.getName().toLowerCase())){
						 this.getServer().getScheduler().scheduleDelayedRepeatingTask(this, new Runnable() {
							 double sec=flytime*min*60;
							 @Override
								public void run() {
									player.setAllowFlight(true);
									if(sec>0){
										sec--;
										np.set(player.getName(),sec);
										player.sendTip(""+sec);
										np.save();
									}else if(sec==0){
										player.setAllowFlight(false);
										player.sendMessage("플라이 시간이 만료되었습니다.");
										Main.getInstance().getServer().getScheduler().cancelAllTasks();
										np.remove(player.getName());
									}
									
								}
						},0,20);
					 }
					 else if(EconomyAPI.getInstance().myMoney(player)<flycost*min){
						 player.sendMessage("금액이 $"+flycost*min+" 만큼 부족합니다.");
						 return false;
					 }
					 
				 }
			 }
		 }
		return false;
	}
}
