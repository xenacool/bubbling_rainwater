package bubbling_rainwater.dataplane.state_machine_replication;

import org.apache.paimon.catalog.CatalogContext;
import org.apache.paimon.fs.*;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReplicatingDelegateFileIO implements FileIO, AutoCloseable {

    private final FileIO delegate;
    private final ReplicatingFileIO replicatingFileIO;
    private final ConcurrentHashMap<Path, Instant> openFileHandles = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public ReplicatingDelegateFileIO(FileIO delegate) {
        this(delegate, 0, Executors.newVirtualThreadPerTaskExecutor());
    }

    public ReplicatingDelegateFileIO(FileIO fileIODelegate, int offset, ExecutorService executorService) {
        this.delegate = fileIODelegate;
        this.replicatingFileIO = new ReplicatingFileIO(offset);
        this.executorService = executorService;
    }

    @Override
    public boolean isObjectStore() {
        return this.delegate.isObjectStore();
    }

    @Override
    public void configure(CatalogContext context) {
        this.delegate.configure(context);
    }

    @Override
    public SeekableInputStream newInputStream(Path path) throws IOException {
        return this.delegate.newInputStream(path);
    }

    @Override
    public PositionOutputStream newOutputStream(Path path, boolean overwrite) throws IOException {

        return new LoggingPOStream(this.delegate.newOutputStream(path, overwrite));
    }

    @Override
    public FileStatus getFileStatus(Path path) throws IOException {
        return this.delegate.getFileStatus(path);
    }

    @Override
    public FileStatus[] listStatus(Path path) throws IOException {
        return this.delegate.listStatus(path);
    }

    @Override
    public boolean exists(Path path) throws IOException {
        return this.delegate.exists(path);
    }

    @Override
    public boolean delete(Path path, boolean recursive) throws IOException {
        return this.delegate.delete(path, recursive);
    }

    @Override
    public boolean mkdirs(Path path) throws IOException {
        return this.delegate.mkdirs(path);
    }

    @Override
    public boolean rename(Path src, Path dst) throws IOException {
        return false;
    }

    public void commit () throws IOException {}

    @Override
    public void close() throws Exception {
        
    }
}
