package retroloader.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Downloads Minecraft client jars for a given version.
 *
 * Strategy:
 * <ol>
 *   <li>Check Mojang's official version manifest ({@code launchermeta.mojang.com}).
 *       If the version exists, download the client jar from the official URL.</li>
 *   <li>If the version is not in Mojang's manifest, attempt to download from
 *       an archive/custom source for versions that still exist but are no longer
 *       in the launcher.</li>
 * </ol>
 *
 * Downloaded jars are cached in the given output directory.
 */
public final class VersionDownloader {

    private static final Logger LOGGER = Logger.getLogger("RetroLoader");

    private static final String MOJANG_VERSION_MANIFEST =
            "https://launchermeta.mojang.com/v1/packages/be7088bf6a4971a961c67e9d3c24d2c6f5c3b9e3/version_manifest.json";

    // Archive source template — {0} is replaced with the version id.
    // This can be updated to point to a different archive.
    private static final String ARCHIVE_URL_TEMPLATE =
            "https://archive.org/download/Minecraft-JE-{0}/client.jar";

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final Path cacheDir;

    public VersionDownloader(Path cacheDir) {
        this.cacheDir = cacheDir;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT)
                .build();
    }

    /**
     * Downloads the Minecraft client jar for the given version.
     *
     * @param versionId the Minecraft version identifier (e.g., "c0.0.13a_03")
     * @return path to the downloaded (or cached) client jar
     * @throws IOException          if the download fails or the file cannot be written
     * @throws InterruptedException if the HTTP request is interrupted
     */
    public Path download(String versionId) throws IOException, InterruptedException {
        Path targetFile = cacheDir.resolve(versionId + "-client.jar");

        if (Files.exists(targetFile)) {
            LOGGER.info("Version " + versionId + " already cached at " + targetFile);
            return targetFile;
        }

        Files.createDirectories(cacheDir);

        // Try Mojang manifest first
        Optional<String> mojangUrl = resolveMojangDownloadUrl(versionId);
        if (mojangUrl.isPresent()) {
            LOGGER.info("Downloading " + versionId + " from Mojang...");
            downloadFile(mojangUrl.get(), targetFile);
            LOGGER.info("Downloaded " + versionId + " from Mojang to " + targetFile);
            return targetFile;
        }

        // Fall back to archive source
        String archiveUrl = ARCHIVE_URL_TEMPLATE.replace("{0}", versionId);
        LOGGER.info("Version " + versionId + " not in Mojang manifest. Trying archive: " + archiveUrl);
        try {
            downloadFile(archiveUrl, targetFile);
            LOGGER.info("Downloaded " + versionId + " from archive to " + targetFile);
            return targetFile;
        } catch (IOException e) {
            Files.deleteIfExists(targetFile);
            throw new IOException("Failed to download Minecraft " + versionId
                    + " from both Mojang manifest and archive source", e);
        }
    }

    /**
     * Queries Mojang's version manifest and returns the client download URL
     * for the given version, if present.
     *
     * @param versionId the Minecraft version identifier
     * @return the download URL, or empty if not in the manifest
     * @throws IOException          if the manifest cannot be fetched
     * @throws InterruptedException if the HTTP request is interrupted
     */
    private Optional<String> resolveMojangDownloadUrl(String versionId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MOJANG_VERSION_MANIFEST))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            LOGGER.warning("Mojang manifest returned HTTP " + response.statusCode());
            return Optional.empty();
        }

        String body = response.body();
        // Simple JSON string search — avoids pulling in a JSON parser dependency.
        // Look for the version id in the manifest, then find the "client" download URL.
        String versionUrl = extractVersionMetadataUrl(body, versionId);
        if (versionUrl == null) {
            return Optional.empty();
        }

        // Fetch the per-version metadata to get the client download URL
        HttpRequest versionRequest = HttpRequest.newBuilder()
                .uri(URI.create(versionUrl))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> versionResponse = httpClient.send(versionRequest, HttpResponse.BodyHandlers.ofString());
        if (versionResponse.statusCode() != 200) {
            return Optional.empty();
        }

        String clientUrl = extractClientDownloadUrl(versionResponse.body());
        return Optional.ofNullable(clientUrl);
    }

    /**
     * Extracts the per-version metadata URL from the version manifest JSON.
     * Uses simple string search to avoid a JSON parser dependency.
     *
     * @param manifestJson the version manifest JSON body
     * @param versionId    the version to find
     * @return the metadata URL, or null if not found
     */
    private String extractVersionMetadataUrl(String manifestJson, String versionId) {
        String searchKey = "\"" + versionId + "\"";
        int idIndex = manifestJson.indexOf(searchKey);
        if (idIndex == -1) {
            return null;
        }

        int urlIndex = manifestJson.indexOf("\"url\"", idIndex);
        if (urlIndex == -1) {
            return null;
        }

        int urlStart = manifestJson.indexOf('"', urlIndex + 5) + 1;
        int urlEnd = manifestJson.indexOf('"', urlStart);
        if (urlStart == 0 || urlEnd == -1) {
            return null;
        }

        return manifestJson.substring(urlStart, urlEnd);
    }

    /**
     * Extracts the client download URL from a per-version metadata JSON.
     *
     * @param versionJson the per-version metadata JSON body
     * @return the client jar download URL, or null if not found
     */
    private String extractClientDownloadUrl(String versionJson) {
        int clientIndex = versionJson.indexOf("\"client\"");
        if (clientIndex == -1) {
            return null;
        }

        int urlIndex = versionJson.indexOf("\"url\"", clientIndex);
        if (urlIndex == -1) {
            return null;
        }

        int urlStart = versionJson.indexOf('"', urlIndex + 5) + 1;
        int urlEnd = versionJson.indexOf('"', urlStart);
        if (urlStart == 0 || urlEnd == -1) {
            return null;
        }

        return versionJson.substring(urlStart, urlEnd);
    }

    /**
     * Downloads a file from the given URL to the target path.
     *
     * @param url      the download URL
     * @param target   the destination file path
     * @throws IOException if the download fails
     */
    private void downloadFile(String url, Path target) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(5))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " downloading from " + url);
        }

        try (InputStream body = response.body()) {
            Files.copy(body, target);
        }
    }
}
