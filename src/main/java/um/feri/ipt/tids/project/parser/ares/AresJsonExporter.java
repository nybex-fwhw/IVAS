package um.feri.ipt.tids.project.parser.ares;

import tools.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.*;

/** Replace the destination only after serialization and writing have completed. */
public final class AresJsonExporter {
    private final JsonMapper mapper = JsonMapper.builder().build();

    public Path write(AresResult result, Path destination) throws IOException {
        Path absolute = destination.toAbsolutePath();
        Files.createDirectories(absolute.getParent());
        byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(result);
        Path temporary = Files.createTempFile(absolute.getParent(), ".ares-", ".tmp");
        try {
            Files.write(temporary, json);
            try {
                Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return absolute;
    }
}
