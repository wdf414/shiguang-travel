package com.travel.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.common.PhotoStorage;

/**
 * 本地景点照片服务。
 *
 * <p>图片直接复用仓库根目录的 {@code scenic_photos/}，不产生任何副本。
 * 目录定位见 {@link PhotoStorage}：会依次尝试从仓库根目录、{@code travel-backend/}
 * 或容器内 {@code /app} 启动时的常见相对路径，避免换个工作目录就整页图片 404。</p>
 *
 * <p>{@code /pick?seed=xxx&keyword=三亚} 会优先在文件名包含关键字的照片中，
 * 按种子稳定地挑选一张；同一 seed 永远返回同一张图，避免刷新后跳图。</p>
 */
@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private static final Logger log = LoggerFactory.getLogger(PhotoController.class);
    private static final Duration CACHE_TTL = Duration.ofDays(7);

    private volatile List<Path> photoFiles;

    @GetMapping("/pick")
    public ResponseEntity<Resource> pick(@RequestParam(defaultValue = "default") String seed,
                                         @RequestParam(required = false) String keyword) {
        List<Path> files = photoFiles();
        if (files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Path> candidates = files;
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            List<Path> matched = files.stream()
                    .filter(path -> path.getFileName().toString().contains(kw))
                    .toList();
            if (!matched.isEmpty()) {
                candidates = matched;
            }
        }

        int index = Math.floorMod(seed.hashCode(), candidates.size());
        return serve(candidates.get(index));
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> byFilename(@PathVariable String filename) {
        Path dir = PhotoStorage.directory();
        Path file = dir.resolve(filename).normalize();
        if (!file.startsWith(dir) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        return serve(file);
    }

    private List<Path> photoFiles() {
        List<Path> cached = photoFiles;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (photoFiles == null) {
                Path dir = PhotoStorage.directory();
                try (Stream<Path> stream = Files.list(dir)) {
                    photoFiles = stream
                            .filter(Files::isRegularFile)
                            .filter(this::isImage)
                            // 固定排序，保证同一 seed 始终命中同一张图
                            .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                            .toList();
                } catch (IOException e) {
                    log.warn("读取景点照片目录失败: {}", dir, e);
                    photoFiles = List.of();
                }
                if (photoFiles.isEmpty()) {
                    log.warn("景点照片目录中没有可用图片: {}，/api/photos/pick 将返回 404", dir);
                }
            }
            return photoFiles;
        }
    }

    private boolean isImage(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".png") || name.endsWith(".webp");
    }

    private ResponseEntity<Resource> serve(Path path) {
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "image/jpeg";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .cacheControl(CacheControl.maxAge(CACHE_TTL).cachePublic())
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
