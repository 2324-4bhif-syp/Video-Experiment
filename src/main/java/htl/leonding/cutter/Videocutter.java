package htl.leonding.cutter;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

@Path("cut")
public class Videocutter {

    @GET
    @Path("wrapper")
    @Produces("video/mp4")
    public FileInputStream showVideo() {

        File directroy = new File("screenshot/luka");

        File[] screenshots = directroy.listFiles(f -> f.getName().endsWith("png"));

        if(screenshots == null){
            return null;
        }

        Arrays.sort(screenshots);

        /*
        int counter = 0; //207

        for(File file: screenshots){
            file.renameTo(new File(String.format("screenshot/luka/luka-%03d.png", counter)));
            counter++;
        }
        */

        FileInputStream stream;

        try {

            FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
            FFprobe ffprobe = new FFprobe("/usr/bin/ffprobe");

            FFmpegBuilder builder = new FFmpegBuilder()
                    .addInput("screenshot/luka/luka-%03d.png")

                    .overrideOutputFiles(true)

                    .addOutput("output.mp4")
                        .setFormat("mp4")
                        .setVideoCodec("libx264")
                        .setVideoFrameRate(1, 1)
                        .setVideoBitRate(100_000)
                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

            // Run a two-pass encode
            executor.createTwoPassJob(builder).run();

            stream = new FileInputStream("output.mp4");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return stream;
    }

    @GET
    @Path("/ffmpeg")
    //@Produces("video/mp4")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Blocking
    public Uni<Response> ffmpeg() {

        try {

            Runtime r = Runtime.getRuntime();

            Process p = r.exec(
                    "ffmpeg -y -framerate 1 -i screenshot/luka/luka-%03d.png -c:v libx264 -r 1 outputF.mp4"
            );

            p.waitFor();
            p.destroy();

            Response.ResponseBuilder response = Response.ok(new File("outputF.mp4"));
            //response.header("Content-Disposition", "attachment;filename=" + nf.getName());
            response.header("Content-Type", "video/mp4");
            Uni<Response> re = Uni.createFrom().item(response.build());


            r.freeMemory();
            r.gc();

            return re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
