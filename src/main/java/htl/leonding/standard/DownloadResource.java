package htl.leonding.standard;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.file.AsyncFile;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.nio.file.Files;

@Path("/download")
public class DownloadResource {

    @Inject
    Vertx vertx;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive";
    }

    @GET
    @Path("/a")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Uni<Response> showVideo() {
        final OpenOptions openOptions = (new OpenOptions()).setCreate(false).setWrite(false);

        Uni<AsyncFile> uni1 = vertx.fileSystem()
                .open("screenshot/finalVideo.mp4", openOptions);

        return uni1.onItem()
                .transform(asyncFile -> Response.ok(asyncFile)
                        .type(MediaType.valueOf("video/mp4"))
                        //.header("Content-Disposition", "inline;filename=\"finalVideo.mp4\"")
                        .build());
    }

    @GET
    @Path("{dataURL}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Uni<Response> downloadImages(@PathParam("dataURL") String dataURL) {
        File nf = new File("screenshot/" + dataURL);

        if (!Files.exists(nf.toPath())) {
            Log.error("File does not exist");
            return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
        }

        Response.ResponseBuilder response = Response.ok(nf);
        //response.header("Content-Disposition", "attachment;filename=" + nf.getName());
        response.header("Content-Type", "video/mp4");
        Uni<Response> re = Uni.createFrom().item(response.build());

        return re;
    }
}
