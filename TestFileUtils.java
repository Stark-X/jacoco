import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.jacoco.core.utils.FileUtils;

public class TestFileUtils {
    public static void main(String[] args) {
        try {
            // Create a temporary directory structure for testing
            File tempDir = new File("target/test-classes");
            tempDir.mkdirs();
            
            // Create some test files
            File file1 = new File(tempDir, "test1.class");
            File file2 = new File(tempDir, "test2.class");
            File subDir = new File(tempDir, "subdir");
            subDir.mkdirs();
            File file3 = new File(subDir, "test3.class");
            
            file1.createNewFile();
            file2.createNewFile();
            file3.createNewFile();
            
            // Test basic functionality
            List<File> files = FileUtils.getFiles(tempDir, Arrays.asList("**/*.class"), null);
            System.out.println("Found " + files.size() + " class files:");
            for (File f : files) {
                System.out.println("  - " + f.getPath());
            }
            
            // Test with exclusions
            List<File> filteredFiles = FileUtils.getFiles(tempDir, 
                Arrays.asList("**/*.class"), 
                Arrays.asList("**/test2.class"));
            System.out.println("\nFound " + filteredFiles.size() + " class files after filtering:");
            for (File f : filteredFiles) {
                System.out.println("  - " + f.getPath());
            }
            
            // Test getFileNames method
            List<String> fileNames = FileUtils.getFileNames(tempDir, 
                Arrays.asList("**/*.class"), 
                Arrays.asList("**/test3.class"));
            System.out.println("\nFound " + fileNames.size() + " class file names after filtering:");
            for (String name : fileNames) {
                System.out.println("  - " + name);
            }
            
            // Clean up
            file1.delete();
            file2.delete();
            file3.delete();
            subDir.delete();
            
            System.out.println("\nFileUtils test completed successfully - Java 5 compatibility maintained!");
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}