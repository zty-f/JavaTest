import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片裁剪测试工具
 * 专门用于裁剪包含"文心一言"的图片
 */
public class ImageCropTester {
    
    // 裁剪配置
    private static final String SOURCE_FOLDER = "004-20250218/004-XC-20250218";
    private static final String OUTPUT_FOLDER = "cropped_images";
    private static final String KEYWORD = "文心一言"; // 要查找的关键词
    private static final double CROP_BOTTOM_CM = 1.5; // 裁剪底部1.5cm
    private static final int DPI = 96; // 假设96 DPI，可以根据实际情况调整
    
    public static void main(String[] args) {
        System.out.println("=== 图片裁剪测试工具 ===");
        System.out.println("源文件夹: " + SOURCE_FOLDER);
        System.out.println("输出文件夹: " + OUTPUT_FOLDER);
        System.out.println("查找关键词: " + KEYWORD);
        System.out.println("裁剪底部: " + CROP_BOTTOM_CM + "cm");
        System.out.println("DPI设置: " + DPI);
        System.out.println();
        
        try {
            // 创建输出文件夹
            createOutputFolder();
            
            // 查找包含关键词的图片
            List<File> targetFiles = findTargetImages();
            
            if (targetFiles.isEmpty()) {
                System.out.println("❌ 未找到包含关键词 '" + KEYWORD + "' 的图片文件");
                return;
            }
            
            System.out.println("✅ 找到 " + targetFiles.size() + " 个目标图片文件:");
            for (File file : targetFiles) {
                System.out.println("  - " + file.getName());
            }
            System.out.println();
            
            // 裁剪图片
            cropImages(targetFiles);
            
            System.out.println("🎉 图片裁剪完成！");
            
        } catch (Exception e) {
            System.err.println("❌ 处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建输出文件夹
     */
    private static void createOutputFolder() throws IOException {
        Path outputPath = Paths.get(OUTPUT_FOLDER);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            System.out.println("✅ 创建输出文件夹: " + OUTPUT_FOLDER);
        } else {
            System.out.println("ℹ️  输出文件夹已存在: " + OUTPUT_FOLDER);
        }
    }
    
    /**
     * 查找包含关键词的图片文件
     */
    private static List<File> findTargetImages() throws IOException {
        List<File> targetFiles = new ArrayList<>();
        Path sourcePath = Paths.get(SOURCE_FOLDER);
        
        if (!Files.exists(sourcePath)) {
            throw new IOException("源文件夹不存在: " + SOURCE_FOLDER);
        }
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourcePath)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    String fileName = path.getFileName().toString();
                    if (fileName.contains(KEYWORD) && isImageFile(fileName)) {
                        targetFiles.add(path.toFile());
                    }
                }
            }
        }
        
        return targetFiles;
    }
    
    /**
     * 判断是否为图片文件
     */
    private static boolean isImageFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
               lowerName.endsWith(".png") || lowerName.endsWith(".gif") ||
               lowerName.endsWith(".bmp");
    }
    
    /**
     * 裁剪图片列表
     */
    private static void cropImages(List<File> imageFiles) {
        int successCount = 0;
        int failCount = 0;
        
        for (File imageFile : imageFiles) {
            try {
                System.out.println("🔄 正在处理: " + imageFile.getName());
                
                // 读取原图
                BufferedImage originalImage = ImageIO.read(imageFile);
                if (originalImage == null) {
                    System.err.println("❌ 无法读取图片: " + imageFile.getName());
                    failCount++;
                    continue;
                }
                
                // 裁剪图片
                BufferedImage croppedImage = cropImage(originalImage);
                
                // 生成输出文件名
                String outputFileName = generateOutputFileName(imageFile.getName());
                File outputFile = new File(OUTPUT_FOLDER, outputFileName);
                
                // 保存裁剪后的图片
                String format = getImageFormat(imageFile.getName());
                ImageIO.write(croppedImage, format, outputFile);
                
                System.out.println("✅ 裁剪成功: " + outputFileName + 
                                 " (" + originalImage.getWidth() + "x" + originalImage.getHeight() + 
                                 " -> " + croppedImage.getWidth() + "x" + croppedImage.getHeight() + 
                                 ", 裁剪底部: " + CROP_BOTTOM_CM + "cm)");
                successCount++;
                
            } catch (Exception e) {
                System.err.println("❌ 处理失败: " + imageFile.getName() + " - " + e.getMessage());
                failCount++;
            }
        }
        
        System.out.println();
        System.out.println("📊 处理结果统计:");
        System.out.println("  成功: " + successCount + " 个");
        System.out.println("  失败: " + failCount + " 个");
        System.out.println("  总计: " + imageFiles.size() + " 个");
    }
    
    /**
     * 裁剪单张图片 - 裁剪底部1cm
     */
    private static BufferedImage cropImage(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 将厘米转换为像素 (1cm = 0.3937英寸, 1英寸 = DPI像素)
        int cropBottomPixels = (int) (CROP_BOTTOM_CM * 0.3937 * DPI);
        
        // 确保裁剪的像素数不超过图片高度
        cropBottomPixels = Math.min(cropBottomPixels, originalHeight);
        
        // 计算新的高度（原高度减去底部裁剪的像素）
        int newHeight = originalHeight - cropBottomPixels;
        
        // 如果裁剪后高度为0或负数，返回原图
        if (newHeight <= 0) {
            System.out.println("⚠️  警告: 图片高度不足以裁剪 " + CROP_BOTTOM_CM + "cm，保持原图");
            return originalImage;
        }
        
        // 执行裁剪：从左上角开始，保持原宽度，裁剪掉底部
        BufferedImage croppedImage = originalImage.getSubimage(0, 0, originalWidth, newHeight);
        
        return croppedImage;
    }
    
    /**
     * 生成输出文件名
     */
    private static String generateOutputFileName(String originalFileName) {
        String nameWithoutExt = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        return nameWithoutExt + "_cropped" + extension;
    }
    
    /**
     * 获取图片格式
     */
    private static String getImageFormat(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "jpg";
        } else if (lowerName.endsWith(".png")) {
            return "png";
        } else if (lowerName.endsWith(".gif")) {
            return "gif";
        } else if (lowerName.endsWith(".bmp")) {
            return "bmp";
        } else {
            return "jpg"; // 默认格式
        }
    }
    
    /**
     * 测试方法：可以调整裁剪参数
     */
    public static void testWithCustomSettings(int width, int height, String sourcePath, String outputPath) {
        System.out.println("=== 自定义参数测试 ===");
        System.out.println("裁剪尺寸: " + width + "x" + height);
        System.out.println("源路径: " + sourcePath);
        System.out.println("输出路径: " + outputPath);
        
        // 这里可以实现自定义参数的裁剪逻辑
        // 为了简化，这里只是示例
        System.out.println("自定义参数测试功能待实现...");
    }
}
