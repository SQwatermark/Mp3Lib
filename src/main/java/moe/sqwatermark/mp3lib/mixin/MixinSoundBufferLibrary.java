package moe.sqwatermark.mp3lib.mixin;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import moe.sqwatermark.mp3lib.Mp3AudioStream;
import net.minecraft.Util;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Mixin(SoundBufferLibrary.class)
public class MixinSoundBufferLibrary {

    /**
     * 目前把所有的音频流都换成了run/music/test.mp3，别的功能还没写
     * @author SQwatermark
     * @reason 待会改成inject
     */
    @Overwrite
    public CompletableFuture<AudioStream> getStream(ResourceLocation pResourceLocation, boolean pIsWrapper) {
        return CompletableFuture.supplyAsync(() -> {
            File file = Paths.get("music").resolve("test.mp3").toFile();
            if (file.isFile()) {
                try {
                    InputStream inputStream = new FileInputStream(file);
                    AudioInputStream originalInputStream = new MpegAudioFileReader().getAudioInputStream(inputStream);
                    AudioFormat originalFormat = originalInputStream.getFormat();
                    // 将mp3转换成pcm TODO 预先检测格式
                    AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), 16,
                            originalFormat.getChannels(), originalFormat.getChannels() * 2, originalFormat.getSampleRate(), false);
                    AudioInputStream targetInputStream = AudioSystem.getAudioInputStream(targetFormat, originalInputStream);
                    return new Mp3AudioStream(targetInputStream);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }
            throw new RuntimeException("TODO");
//            try {
//                Resource resource = this.resourceManager.getResource(pResourceLocation);
//                InputStream inputstream = resource.getInputStream();
//                return pIsWrapper ? new LoopingAudioStream(OggAudioStream::new, inputstream) : new OggAudioStream(inputstream);
//            } catch (IOException ioexception) {
//                throw new CompletionException(ioexception);
//            }
        }, Util.backgroundExecutor());
    }

}
