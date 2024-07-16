package bubbling_rainwater.dataplane.state_machine_replication;

import org.apache.paimon.fs.PositionOutputStream;

import java.io.IOException;

public class LoggingPOStream extends PositionOutputStream {
    private final PositionOutputStream delegate;

    public LoggingPOStream(PositionOutputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public long getPos() throws IOException {
        return this.delegate.getPos();
    }

    @Override
    public void write(int b) throws IOException {
        this.delegate.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.delegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        this.delegate.flush();
    }

    @Override
    public void close() throws IOException {
        this.delegate.close();
    }
}
