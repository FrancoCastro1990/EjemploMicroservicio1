package cl.duoc.ejemplo.microservicio.controller;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cl.duoc.ejemplo.microservicio.dto.S3ObjectDto;
import cl.duoc.ejemplo.microservicio.service.AwsS3Service;
import cl.duoc.ejemplo.microservicio.service.EfsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
@Slf4j
public class AwsS3Controller {

	@Autowired
	private AwsS3Service awsS3Service;

	@Autowired
	private EfsService efsService;

	// Listar objetos en un bucket
	@GetMapping("/{bucket}/objects")
	public ResponseEntity<List<S3ObjectDto>> listObjects(@PathVariable String bucket) {

		List<S3ObjectDto> dtoList = awsS3Service.listObjects(bucket);
		return ResponseEntity.ok(dtoList);
	}

	// Descargar archivo como byte[]
	@GetMapping("/{bucket}/object")
	public ResponseEntity<byte[]> downloadObject(@PathVariable String bucket, @RequestParam String key) {

		byte[] fileBytes = awsS3Service.downloadAsBytes(bucket, key);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + key)
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(fileBytes);
	}

	// Subir archivo (guarda en EFS temporalmente, luego sube a S3)
	@PostMapping("/{bucket}/object")
	public ResponseEntity<Void> uploadObject(@PathVariable String bucket, @RequestParam String key,
			@RequestParam("file") MultipartFile file) {

		try {
			// Guardar en EFS temporalmente
			String efsFilename = "uploads/" + key.replace("/", "_");
			File efsFile = efsService.saveToEfs(efsFilename, file);
			log.info("Archivo guardado en EFS: {}", efsFile.getAbsolutePath());

			// Subir a S3
			awsS3Service.upload(bucket, key, file);
			log.info("Archivo subido a S3: {}/{}", bucket, key);

			// Eliminar archivo temporal de EFS
			efsService.deleteFromEfs(efsFilename);
			log.info("Archivo temporal eliminado de EFS");

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error("Error al subir archivo", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	// Mover objeto dentro del mismo bucket
	@PostMapping("/{bucket}/move")
	public ResponseEntity<Void> moveObject(@PathVariable String bucket, @RequestParam String sourceKey,
			@RequestParam String destKey) {

		awsS3Service.moveObject(bucket, sourceKey, destKey);
		return ResponseEntity.ok().build();
	}

	// Borrar objeto
	@DeleteMapping("/{bucket}/object")
	public ResponseEntity<Void> deleteObject(@PathVariable String bucket, @RequestParam String key) {

		awsS3Service.deleteObject(bucket, key);
		return ResponseEntity.noContent().build();
	}
}
