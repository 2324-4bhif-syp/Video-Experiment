package htl.leonding.media;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

public class MediaStreaming implements StreamingOutput {

    private static final int BUFFER_SIZE = 1000;

    private long from;
    private long to;
    private File file;

    public MediaStreaming(File file, long from, long to) {
        this.file = file;
        this.from = from;
        this.to = to;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        try (FileChannel inChannel = new FileInputStream(file).getChannel();
             WritableByteChannel outChannel = Channels.newChannel(output)) {
            while (from < to && AppLifeCycle.isRunning()) {
                if (to - from + 1 > BUFFER_SIZE) {
                    inChannel.transferTo(from, BUFFER_SIZE, outChannel);
                    from += BUFFER_SIZE;
                } else {
                    inChannel.transferTo(from, to - from + 1, outChannel);
                    from = to;
                }
            }
        } catch (IOException e) {
        }
    }
}
