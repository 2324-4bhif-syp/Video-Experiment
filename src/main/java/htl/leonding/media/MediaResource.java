package htl.leonding.media;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/media")
public class MediaResource {

    private final ManagedExecutor managedExecutor;

    public MediaResource(ManagedExecutor executor) {
        this.managedExecutor = executor;
    }

    @GET
    @Produces("video/mp4") // changing to octet stream will make videos unplayable on IOS devices
    //@Produces(MediaType.APPLICATION_OCTET_STREAM)
    public void getMediaByName(@Suspended AsyncResponse asyncResponse, @HeaderParam("Range") String rangeHeader) {

        String filename = "screenshot/luka.mp4";

        managedExecutor.runAsync(() -> {

            if (!Files.exists(Paths.get(filename))) {
                asyncResponse.resume(Response.status(Response.Status.NOT_FOUND).build());
                return;
            }

            final File mediaFile = new File(filename);
            final long length = mediaFile.length();

            long from = 0;
            long to = length - 1;

            if (rangeHeader != null) {
                // partial content, range specfied, skipped to specific byte
                // e.g., bytes = 123-124
                String fromTo = rangeHeader.split("=")[1];
                Pattern pattern = Pattern.compile("^(\\d*)\\-(\\d*)$");
                Matcher matcher = pattern.matcher(fromTo);
                if (matcher.find()) {
                    if (!matcher.group(1).isEmpty()) {
                        from = Long.parseLong(matcher.group(1));
                    }
                    if (!matcher.group(2).isEmpty()) {
                        to = Long.parseLong(matcher.group(2));
                    }
                }
            }

            StreamingOutput streamOut = new MediaStreaming(mediaFile, from, to);

            Response.ResponseBuilder resp = Response.ok(streamOut);

            if (rangeHeader != null)
                // partial content 206
                resp = resp.status(Response.Status.PARTIAL_CONTENT);

            resp = resp
                    .header("Content-Range", String.format("bytes %d-%d/%d", from, to, length))
                    .header("Accept-Ranges", "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, to - from + 1);

            asyncResponse.resume(resp.build());
        });
    }
}
