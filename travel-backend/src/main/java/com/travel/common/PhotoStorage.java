package com.travel.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 景点照片目录解析。
 *
 * <p>照片放在仓库根目录的 {@code scenic_photos/}，不随 jar 打包。原来的实现只认
 * {@code ../scenic_photos}，只有“工作目录是 travel-backend”时才能找到；从仓库根目录
 * 或 IDE 里以其他工作目录启动时，照片接口会全部 404，前端就只剩“图片加载失败”占位图。</p>
 *
 * <p>这里按常见启动方式依次尝试多个候选目录，也可以用环境变量 {@code SCENIC_PHOTO_DIR}
 * 显式指定。</p>
 */
public final class PhotoStorage {

    private static final Logger log = LoggerFactory.getLogger(PhotoStorage.class);

    private static volatile Path resolved;

    private PhotoStorage() {
    }

    /** 返回照片目录；找不到时返回候选路径中最后一个，并已打印告警。 */
    public static Path directory() {
        Path dir = resolved;
        if (dir != null) {
            return dir;
        }
        synchronized (PhotoStorage.class) {
            if (resolved == null) {
                resolved = resolveDirectory();
            }
            return resolved;
        }
    }

    private static Path resolveDirectory() {
        List<Path> candidates = new ArrayList<>();

        String configured = System.getenv("SCENIC_PHOTO_DIR");
        if (configured != null && !configured.isBlank()) {
            candidates.add(Paths.get(configured.trim()));
        }

        Path cwd = Paths.get("").toAbsolutePath().normalize();
        // 仓库根目录启动：./scenic_photos
        candidates.add(cwd.resolve("scenic_photos"));
        // travel-backend 启动（含 Docker /app）：../scenic_photos
        candidates.add(cwd.resolve("../scenic_photos"));
        // target/ 等更深一层启动：../../scenic_photos
        candidates.add(cwd.resolve("../../scenic_photos"));

        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.isDirectory(normalized)) {
                log.info("景点照片目录: {}", normalized);
                return normalized;
            }
        }

        Path fallback = cwd.resolve("../scenic_photos").normalize();
        log.warn("未找到景点照片目录 scenic_photos，已尝试 {}；图片接口将返回 404。"
                + "可通过环境变量 SCENIC_PHOTO_DIR 指定照片目录。", candidates);
        return fallback;
    }
}
