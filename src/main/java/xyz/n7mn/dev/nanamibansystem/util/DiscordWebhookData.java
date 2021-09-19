package xyz.n7mn.dev.nanamibansystem.util;

public class DiscordWebhookData {

    private String content;
    private String username;
    private String avatar_url;
    private boolean tts;

    public DiscordWebhookData(String content, String username, String avatar_url, boolean tts){
        this.content = content;
        this.username = username;
        this.avatar_url = avatar_url;
        this.tts = tts;
    }

    public DiscordWebhookData(String content){
        this.content = content;
        this.username = "";
        this.avatar_url = "";
        this.tts = false;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public boolean isTts() {
        return tts;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }
}
