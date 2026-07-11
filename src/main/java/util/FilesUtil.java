package util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

public class FilesUtil {

    public static String read(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (Exception e) {
            throw new RuntimeException("Cannot read file: " + path, e);
        }
    }

    public static void write(String path, String content) {
        try {
            Files.createDirectories(Path.of(path).getParent());
            Files.writeString(Path.of(path), content);
        } catch (Exception e) {
            throw new RuntimeException("Cannot write file: " + path, e);
        }
    }

    public static String readFilesBySuffix(String directory, String suffix) {
        try (var files = Files.list(Path.of(directory))) {
            String content = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .sorted()
                    .map(path -> "Файл: " + path.getFileName() + "\n" + read(path.toString()))
                    .collect(Collectors.joining("\n\n"));
            return content.isBlank() ? "Результаты Allure не найдены." : content;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось прочитать файлы из: " + directory, e);
        }
    }

    public static void clearDirectory(String directory) {
        Path path = Path.of(directory);
        if (!Files.exists(path)) {
            return;
        }
        try (var paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(item -> !item.equals(path))
                    .forEach(item -> {
                        try {
                            Files.delete(item);
                        } catch (Exception e) {
                            throw new RuntimeException("Не удалось удалить: " + item, e);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("Не удалось очистить каталог: " + directory, e);
        }
    }
}
