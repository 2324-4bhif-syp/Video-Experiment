package htl.leonding.opencv;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

@Path("java")
public class JavaCV {


    // bei produces video/mp4 geht er automatisch 2 mal rein

    @GET
    //@Produces("video/mp4")
    @Produces(MediaType.TEXT_PLAIN)
    @Blocking
    public String greeting() {

        Log.info("start");
        Log.info("end");

        return "HIII";
    }

    @GET
    @Produces("video/mp4")
    @Path("vid")
    @Blocking
    public File javacv() throws Exception {

        File directroy = new File("screenshot/luka2");

        File[] screenshots = directroy.listFiles(f -> f.getName().endsWith("png"));

        if(screenshots == null){
            return null;
        }

        Arrays.sort(screenshots);

        BufferedImage test = ImageIO.read(screenshots[0]);

        try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                String.format("%s/luka2.mp4", directroy.getPath()),
                test.getWidth(),
                test.getHeight()
        )){

            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(1);
            recorder.setVideoBitrate(100000);

            recorder.start();

            test.getGraphics().dispose();
            test.flush();

            System.gc();

            for (File imageFile : screenshots) {

                try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(imageFile.getPath())){
                    grabber.start();
                    try(Frame frame = grabber.grab()) {
                        // Add frame to video
                        recorder.record(frame);

                        frame.close();

                        System.gc();
                    }
                    catch (Exception e){
                        Log.error(e.getMessage());
                    }

                    grabber.flush();
                    grabber.release();
                    grabber.stop();
                    grabber.release();
                    grabber.close();

                    System.gc();
                }
                catch (Exception e){
                    Log.error(e.getMessage());
                }
            }

            recorder.flush();
            recorder.stop();
            recorder.release();
            recorder.close();

            System.gc();
        }

        System.gc();

        return new File(String.format("%s/luka2.mp4", directroy.getPath()));
    }
}
