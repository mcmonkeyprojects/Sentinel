package org.mcmonkey.sentinel.metrics;

import org.bukkit.Bukkit;
import org.mcmonkey.sentinel.SentinelPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class StatsRecord extends Thread {

    public static void trigger() {
        StatsRecord recorder = new StatsRecord();
        recorder.gather();
        recorder.start();
    }

    public static String hash_md5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes, 0, bytes.length);
            return new BigInteger(1, md.digest()).toString(16).substring(0, 16);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void gather() {
        // We don't need the real value of the MOTD / port, but they're useful for differentiating, so use them to generate a hash
        String differentiator = hash_md5((Bukkit.getServer().getMotd() + Bukkit.getServer().getPort()).getBytes(StandardCharsets.UTF_8));
        String mcVersion = Bukkit.getVersion();
        int firstDash = mcVersion.indexOf('-');
        int secondDash = firstDash == -1 ? -1 : mcVersion.indexOf('-', firstDash + 1);
        int mcPart = mcVersion.indexOf("(MC: ");
        int endPart = mcPart == -1 ? -1 : mcVersion.indexOf(")", mcPart);
        String platform = secondDash == -1 ? "" : mcVersion.substring(firstDash + 1, secondDash);
        mcVersion = (endPart == -1) ? "" : mcVersion.substring(mcPart + "(MC: ".length(), endPart);
        content = "postid=pluginstats&plugin=Denizen"
                + "&differentiator=" + differentiator
                + "&pl_plugin_version=" + URLEncoder.encode(SentinelPlugin.instance.getDescription().getVersion())
                + "&pl_platform=" + URLEncoder.encode(platform)
                + "&pl_minecraft_version=" + URLEncoder.encode(mcVersion)
                + "&pl_player_count=" + Bukkit.getOnlinePlayers().size();
    }

    public String content;

    @Override
    public void run() {
        BufferedReader in = null;
        try {
            // Open a connection to the stats server
            URL url = new URL("https://stats.mcmonkey.org/Stats/Submit"
                    + URLEncoder.encode(SentinelPlugin.instance.getDescription().getVersion()));
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoInput(true);
            uc.setDoOutput(true);
            uc.setConnectTimeout(10000);
            uc.connect();
            // Safely connected at this point
            // Create the final message pack and upload it
            uc.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
            // Wait for a response from the server
            in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            // Record the response
            String Result = in.readLine();
            // TODO: Use return?
            // Close the connection
            in.close();
        }
        catch (Exception e) {
            if (SentinelPlugin.debugMe) {
                System.out.println("Sentinel stat issue: " + e.getMessage());
            }
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (Exception e) {
                if (SentinelPlugin.debugMe) {
                    System.out.println("Sentinel stat issue (backup): " + e.getMessage());
                }
            }
        }
    }
}
