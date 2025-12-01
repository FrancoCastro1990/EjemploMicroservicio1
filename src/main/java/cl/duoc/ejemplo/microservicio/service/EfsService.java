package cl.duoc.ejemplo.microservicio.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EfsService {

    @Value("${efs.path}")
    private String efsPath;

    public File saveToEfs(String filename, MultipartFile multipartFile) throws IOException {
        File dest = new File(efsPath, filename);
        File parentDir = dest.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        multipartFile.transferTo(dest);
        return dest;
    }

    public File saveToEfs(String filename, byte[] content) throws IOException {
        File dest = new File(efsPath, filename);
        File parentDir = dest.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(content);
        }
        return dest;
    }

    public byte[] readFromEfs(String filename) throws IOException {
        Path path = Paths.get(efsPath, filename);
        return Files.readAllBytes(path);
    }

    public boolean deleteFromEfs(String filename) {
        File file = new File(efsPath, filename);
        return file.delete();
    }

    public boolean existsInEfs(String filename) {
        File file = new File(efsPath, filename);
        return file.exists();
    }

    public String getEfsPath() {
        return efsPath;
    }
}
