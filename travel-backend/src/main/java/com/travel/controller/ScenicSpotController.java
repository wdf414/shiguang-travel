package com.travel.controller;

import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.common.PageResult;
import com.travel.common.PhotoStorage;
import com.travel.common.Result;
import com.travel.service.ScenicSpotService;

@RestController
@RequestMapping("/api/scenic-spot")
public class ScenicSpotController {
    @Autowired
    private ScenicSpotService scenicSpotService;

    @GetMapping("/search")
    public Result<PageResult<Map<String, Object>>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(scenicSpotService.search(city, keyword, page, size));
    }

    @GetMapping("/photo")
    public ResponseEntity<?> getPhoto(@RequestParam("name") String name) {
        try {
            Path dir = PhotoStorage.directory();
            if (!Files.isDirectory(dir)) {
                return fallback();
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path path : stream) {
                    if (Files.isDirectory(path)) {
                        continue;
                    }
                    String filename = path.getFileName().toString();
                    if (!filename.startsWith(name)) {
                        continue;
                    }
                    Resource resource = new UrlResource(path.toUri());
                    if (!resource.exists()) {
                        continue;
                    }
                    String contentType = Files.probeContentType(path);
                    if (contentType == null) {
                        contentType = "image/jpeg";
                    }
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .body(resource);
                }
            }
        } catch (Exception e) {
        }
        return fallback();
    }

    private ResponseEntity<?> fallback() {
        // 兜底使用本地景点照片，避免依赖境外图床
        return ResponseEntity.status(302)
                .location(URI.create("/api/photos/pick?seed=scenic-default"))
                .build();
    }
}
