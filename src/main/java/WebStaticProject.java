import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;

import java.io.File;

@RequiredArgsConstructor
public class WebStaticProject {

    private final String gitRepoUrl;
    private final String folderPath;

    public String getAbsolutePath() {
        return new File(folderPath).getAbsolutePath();
    }

    @SneakyThrows
    public void cloneMasterTo() {
        File webDir = new File(folderPath);
        if (!webDir.exists()) {
            if (!webDir.mkdirs()) {
                throw new RuntimeException("Can't create dirs for web static files");
            }
        }
        try (Git git = getGit(webDir)) {
            git.pull().call();
        }
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
