package com.example.demo.controller;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

  private final Path directory;

  public UploadController(@Value("${app.upload-dir}") String dir) {
    directory = Paths.get(dir).toAbsolutePath().normalize();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> upload(@RequestParam("file") MultipartFile file)
    throws IOException {
    if (
      file.isEmpty() || file.getSize() > 5 * 1024 * 1024
    ) throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "Select an image up to 5 MB."
    );
    if (
      !Set.of("image/png", "image/jpeg").contains(
        Objects.toString(file.getContentType(), "")
      )
    ) throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "Only PNG and JPEG images are supported."
    );
    java.awt.image.BufferedImage image;
    try {
      image = ImageIO.read(file.getInputStream());
    } catch (IOException ex) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "The image file could not be decoded."
      );
    }
    if (
      image == null || image.getWidth() > 5000 || image.getHeight() > 5000
    ) throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "Invalid image or dimensions larger than 5000 pixels."
    );
    Files.createDirectories(directory);
    String filename = UUID.randomUUID() + ".png";
    ImageIO.write(image, "png", directory.resolve(filename).toFile());
    return Map.of("url", "/uploads/" + filename);
  }
}
