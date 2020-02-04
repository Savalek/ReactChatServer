import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

@RequiredArgsConstructor
public class WebStaticProject {

    private final String gitRepoUrl;
    private final String folderPath;

    @SneakyThrows
    public void buildWebProject() {
        File webDir = new File(folderPath);
        if (webDir.exists()) {
            deleteDirectoryStream(webDir.toPath());
        }
        if (!webDir.mkdirs()) {
            throw new RuntimeException("Can't create dirs for web static files");
        }
        try (Git git = getGit(webDir)) {
            git.pull().call();
        }
        runYarnBuild();
    }

    @SneakyThrows
    private void deleteDirectoryStream(Path path) {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public String getStaticFilesFolderPath() {
        return new File(folderPath, "build").getAbsolutePath();
    }

    private void runYarnBuild() {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process yarnBuildProcess = runtime.exec("yarn install", new String[0], new File(folderPath));
            redirectOutput(yarnBuildProcess);
            yarnBuildProcess.waitFor();
            yarnBuildProcess = runtime.exec("yarn build", new String[0], new File(folderPath));
            redirectOutput(yarnBuildProcess);
            yarnBuildProcess.waitFor();
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new RuntimeException("Error then run yarn build: " + e.getMessage(), e);
        }
    }

    private void redirectOutput(Process process) {
        new Thread(() -> {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = err.readLine()) != null) {
                    System.err.println(line);
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @SneakyThrows
    private Git getGit(File repoDir) {
        try {
            return Git.open(repoDir);
        } catch (RepositoryNotFoundException e) {
            return Git.cloneRepository()
                    .setDirectory(repoDir)
                    .setURI(gitRepoUrl)
                    .setBranch("master")
                    .call();
        }
    }
}
