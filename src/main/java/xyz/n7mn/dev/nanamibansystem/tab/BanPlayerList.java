package xyz.n7mn.dev.nanamibansystem.tab;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import xyz.n7mn.dev.api.Ban;
import xyz.n7mn.dev.api.data.BanData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BanPlayerList implements TabExecutor {

    private final Ban banSystem;
    private int cacheCount = 0;
    private List<String> cacheList = new ArrayList<>();

    public BanPlayerList(Ban banSystem){
        this.banSystem = banSystem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1){
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<BanData> banList = new ArrayList<>();
        List<String> list = new ArrayList<>();

        try {
            banList = banSystem.getList(true);
        } catch (Exception e){
            e.printStackTrace();
        }

        if (cacheCount != banList.size()){
            cacheList.clear();
            cacheCount = banList.size();
            for (BanData data : banList){

                String UserName = "";
                UUID userId = data.getBanUser();
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.mojang.com/user/profiles/" + userId.toString().replaceAll("-","")+"/names")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    ApiData[] json = new Gson().fromJson(response.body().string(), ApiData[].class);
                    UserName = json[0].getName();
                    response.close();
                } catch (Exception e){
                    e.printStackTrace();
                }

                if (UserName.length() > 0){
                    list.add(UserName);
                }
            }
            cacheList.addAll(list);
        }

        if (args[args.length - 1].length() == 0){
            return cacheList;
        }

        for (String str : cacheList){
            if (!str.toLowerCase().startsWith(args[args.length - 1])){
                continue;
            }

            list.add(str);
        }

        return list;
    }
}
