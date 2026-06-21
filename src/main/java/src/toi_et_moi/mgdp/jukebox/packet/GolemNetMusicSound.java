package src.toi_et_moi.mgdp.jukebox.packet;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Config;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GolemNetMusicSound extends AbstractTickableSoundInstance {

    private static final Pattern MUSIC_ID_PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");
    private static final String MUSIC_163_URL = "https://music.163.com";

    private static Object netMusicSoundEvent;
    private static Class<?> netMusicAudioStreamClass;
    private static java.lang.reflect.Constructor<?> audioStreamCtor;

    // Lyric system
    private static Object lyricRecord;  // LyricRecord instance
    private static java.lang.reflect.Method updateLyricMethod;
    private static java.lang.reflect.Method getLyricsMethod;
    private static java.lang.reflect.Method getFirstKeyMethod;
    private static Object lastLyricRecord;
    private static String lastSongName;

    public static String currentLyricLine = "";
    public static String currentTransLyric = "";
    public static int activeEntityId = -1;

    private final Entity entity;
    private final URL songUrl;
    private final int tickTimes;
    private final String songName;
    private int tick;
    private boolean lyricsLoaded = false;

    public GolemNetMusicSound(Entity entity, String urlString, int timeSecond, String songName) {
        super(getSoundEvent(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.tickTimes = timeSecond * 20;
        this.volume = (float) Config.jukeboxVolume;
        this.tick = 0;
        this.songName = songName;
        activeEntityId = entity.getId();

        URL resolvedUrl;
        try {
            resolvedUrl = URI.create(urlString).toURL();
        } catch (Exception e) {
            resolvedUrl = null;
        }
        this.songUrl = resolvedUrl;

        if (this.songUrl != null) {
            try {
                initAudioStream();
            } catch (Exception ignored) {
            }
        }
        loadLyricsAsync(urlString);
    }

    private static net.minecraft.sounds.SoundEvent getSoundEvent() {
        if (netMusicSoundEvent == null) {
            netMusicSoundEvent = ForgeRegistries.SOUND_EVENTS.getValue(
                    new ResourceLocation("netmusic", "net_music"));
        }
        return (net.minecraft.sounds.SoundEvent) netMusicSoundEvent;
    }

    private static void initAudioStream() throws Exception {
        if (netMusicAudioStreamClass == null) {
            netMusicAudioStreamClass = Class.forName("com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream");
            audioStreamCtor = netMusicAudioStreamClass.getConstructor(URL.class);
        }
    }

    private void loadLyricsAsync(String urlString) {
        if (!urlString.startsWith(MUSIC_163_URL)) return;
        Matcher matcher = MUSIC_ID_PATTERN.matcher(urlString);
        if (!matcher.find()) return;
        long musicId = Long.parseLong(matcher.group(1));

        CompletableFuture.runAsync(() -> {
            try {
                var netMusicClass = Class.forName("com.github.tartaricacid.netmusic.NetMusic");
                var webApiField = netMusicClass.getField("NET_EASE_WEB_API");
                var webApi = webApiField.get(null);
                var lyricMethod = webApi.getClass().getMethod("lyric", long.class);
                String lyricText = (String) lyricMethod.invoke(webApi, musicId);

                var parserClass = Class.forName("com.github.tartaricacid.netmusic.api.lyric.LyricParser");
                var parseMethod = parserClass.getMethod("parseLyric", String.class, String.class);
                var result = parseMethod.invoke(null, lyricText, songName);

                synchronized (GolemNetMusicSound.class) {
                    lyricRecord = result;
                    lastLyricRecord = result;
                    lastSongName = songName;
                    updateLyricMethod = result.getClass().getMethod("updateCurrentLine", int.class);
                    getLyricsMethod = result.getClass().getMethod("getLyrics");
                    var transMethod = result.getClass().getMethod("getTransLyrics");
                }
                lyricsLoaded = true;
            } catch (Exception ignored) {
            }
        }, Util.backgroundExecutor());
    }

    @Override
    public void tick() {
        if (entity == null || !entity.isAlive()) {
            this.stop();
            return;
        }
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();

        tick++;
        if (tick > tickTimes + 50) {
            if (activeEntityId == entity.getId()) {
                currentLyricLine = "";
                currentTransLyric = "";
                activeEntityId = -1;
            }
            this.stop();
        } else {
            // Advance lyrics
            if (lyricsLoaded && lyricRecord != null && activeEntityId == entity.getId()) {
                try {
                    updateLyricMethod.invoke(lyricRecord, tick);
                    var lyricsMap = getLyricsMethod.invoke(lyricRecord);
                    if (lyricsMap != null) {
                        var sizeMethod = lyricsMap.getClass().getMethod("size");
                        var getMethod = lyricsMap.getClass().getMethod("get", int.class);
                        if ((int) sizeMethod.invoke(lyricsMap) > 0) {
                            getFirstKeyMethod = lyricsMap.getClass().getMethod("firstIntKey");
                            int firstKey = (int) getFirstKeyMethod.invoke(lyricsMap);
                            currentLyricLine = (String) getMethod.invoke(lyricsMap, firstKey);
                        } else {
                            currentLyricLine = "";
                        }
                    }
                    // Read translated lyrics
                    try {
                        var transMap = lyricRecord.getClass().getMethod("getTransLyrics").invoke(lyricRecord);
                        if (transMap != null) {
                            var tSize = transMap.getClass().getMethod("size");
                            if ((int) tSize.invoke(transMap) > 0) {
                                int tKey = (int) transMap.getClass().getMethod("firstIntKey").invoke(transMap);
                                currentTransLyric = (String) transMap.getClass().getMethod("get", int.class).invoke(transMap, tKey);
                            } else {
                                currentTransLyric = "";
                            }
                        }
                    } catch (Exception ignored) {}
                } catch (Exception ignored) {
                }
            }
            if (Minecraft.getInstance().level != null
                    && Minecraft.getInstance().level.getGameTime() % 8 == 0) {
                var level = Minecraft.getInstance().level;
                for (int i = 0; i < 2; i++) {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.NOTE,
                            x - 0.5 + level.random.nextDouble(),
                            y + 1.5 + level.random.nextDouble(),
                            z - 0.5 + level.random.nextDouble(),
                            level.random.nextGaussian(), level.random.nextGaussian(), level.random.nextInt(3));
                }
            }
        }
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        if (songUrl == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (audioStreamCtor != null) {
                    return (AudioStream) audioStreamCtor.newInstance(songUrl);
                }
            } catch (Exception e) {
                Minecraft.getInstance().submit(() -> {
                    this.tick = tickTimes;
                });
            }
            try {
                InputStream inputstream = Minecraft.getInstance().getResourceManager().open(
                        new ResourceLocation("netmusic", "sounds/error.ogg"));
                return (AudioStream) new com.mojang.blaze3d.audio.OggAudioStream(inputstream);
            } catch (IOException ioexception) {
                throw new java.util.concurrent.CompletionException(ioexception);
            }
        }, Util.backgroundExecutor());
    }

    /** Called when a NetMusic sound is stopped to clear lyric state */
    public static void onSoundStop(int entityId) {
        if (activeEntityId == entityId) {
            currentLyricLine = "";
            currentTransLyric = "";
            activeEntityId = -1;
            lyricRecord = null;
        }
    }
}
