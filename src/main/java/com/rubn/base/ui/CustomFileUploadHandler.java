package com.rubn.base.ui;

import com.rubn.base.ui.utility.ConfirmDialogBuilder;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.communication.TransferUtil;
import com.vaadin.flow.server.streams.TransferContext;
import com.vaadin.flow.server.streams.TransferProgressAwareHandler;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author rubn
 */
@Log4j2
@RequiredArgsConstructor
public class CustomFileUploadHandler extends TransferProgressAwareHandler<UploadEvent, CustomFileUploadHandler> implements UploadHandler {

    private final String fixedDir;
    private final Upload upload;

    @Override
    public void handleUploadRequest(UploadEvent event) throws IOException {
        Path targetDir = Path.of(fixedDir);
        Path safeFilePath = this.validateAndPreparePath(targetDir, event.getFileName());
        this.transferFile(event, safeFilePath);
        log.info("Transfer file completed");
        event.getUI().access(upload::clearFileList);
    }

    private Path validateAndPreparePath(Path targetDir, String fileName) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new SecurityException("Invalid characters in file name: " + fileName);
        }

        if (!Files.exists(targetDir)) {
            try {
                Files.createDirectories(targetDir);
            } catch (IOException ex) {
                throw new IOException("Failed to create directory ", ex);
            }
        }

        final Path normalizedTarget = targetDir.toAbsolutePath().normalize();
        final Path resolvedPath = normalizedTarget.resolve(fileName).normalize();

        if (!resolvedPath.startsWith(normalizedTarget)) {
            throw new SecurityException("Path traversal attempt detected");
        }

        return resolvedPath;
    }

    private void transferFile(UploadEvent event, Path targetPath) throws IOException {
        try (final InputStream inputStream = event.getInputStream();
             final BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(targetPath))) {

            TransferUtil.transfer(inputStream, outputStream, this.getTransferContext(event), super.getListeners());

        } catch (IOException error) {
            this.notifyError(event, error);
            throw new IOException("File transfer failed: ", error);
        }
    }

    @Override
    protected TransferContext getTransferContext(UploadEvent event) {
        return new TransferContext(
                event.getRequest(),
                event.getResponse(),
                event.getSession(),
                event.getFileName(),
                event.getOwningElement(),
                event.getFileSize());
    }
}