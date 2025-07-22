import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.Properties;

// 添加HTTP客户端相关导入
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RobotDataImporter {
    
    // 用户信息类
    public static class UserInfo {
        private String nickname;
        private String realName;
        private String avatarPath;
        private List<String> albumPaths;
        // 新增字段
        private String phone;
        private int sex;
        private int birthday;
        private int emotionStatus;
        private int workStatus;
        private int verifyStatus;
        private String hometown;
        private String location;
        private String company;
        private String carrier;
        
        public UserInfo(String nickname, String realName) {
            this.nickname = nickname;
            this.realName = realName;
            this.albumPaths = new ArrayList<>();
            this.avatarPath = ""; // 初始化为空字符串而不是null
            // 初始化其他字段
            generateUserInfo();
        }
        
        // Getters and Setters
        public String getNickname() { return nickname; }
        public String getRealName() { return realName; }
        public String getAvatarPath() { return avatarPath; }
        public List<String> getAlbumPaths() { return albumPaths; }
        public String getPhone() { return phone; }
        public int getSex() { return sex; }
        public int getBirthday() { return birthday; }
        public int getEmotionStatus() { return emotionStatus; }
        public int getWorkStatus() { return workStatus; }
        public int getVerifyStatus() { return verifyStatus; }
        public String getHometown() { return hometown; }
        public String getLocation() { return location; }
        public String getCompany() { return company; }
        public String getCarrier() { return carrier; }
        
        public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
        public void addAlbumPath(String albumPath) { this.albumPaths.add(albumPath); }
        
        // 生成用户信息
        private void generateUserInfo() {
            Random random = new Random();
            
            // 生成手机号 (13位数字)
            StringBuilder phoneBuilder = new StringBuilder("1");
            String[] prefixes = {"3", "5", "7", "8", "9"};
            phoneBuilder.append(prefixes[random.nextInt(prefixes.length)]);
            for (int i = 0; i < 9; i++) {
                phoneBuilder.append(random.nextInt(10));
            }
            this.phone = phoneBuilder.toString();
            
            // 根据昵称判断性别 (简单规则)
            String nickLower = nickname.toLowerCase();
            if (nickLower.contains("美") || nickLower.contains("丽") || nickLower.contains("花") || 
                nickLower.contains("香") || nickLower.contains("红") || nickLower.contains("霞") ||
                nickLower.contains("芳") || nickLower.contains("敏") || nickLower.contains("静")) {
                this.sex = 2; // 女性
            } else {
                this.sex = 1; // 男性
            }
            
            // 生成生日 (中老年用户，1950-1980年)
            int year = 1950 + random.nextInt(31); // 1950-1980
            int month = 1 + random.nextInt(12);
            int day = 1 + random.nextInt(28);
            this.birthday = year * 10000 + month * 100 + day;
            
            // 情感状态 (1-单身, 2-已婚, 3-离异)
            int[] emotionWeights = {10, 70, 20}; // 已婚比例较高
            this.emotionStatus = weightedRandomChoice(new int[]{1, 2, 3}, emotionWeights);
            
            // 工作状态 (1-在职, 2-退休, 3-自由职业)
            int[] workWeights = {40, 40, 20}; // 在职和退休比例较高
            this.workStatus = weightedRandomChoice(new int[]{1, 2, 3}, workWeights);

            this.verifyStatus = 1;
            
            // 家乡和所在地 (中老年用户常见城市)
            String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "南京", "武汉", "成都", "西安", "重庆", 
                             "天津", "青岛", "大连", "厦门", "苏州", "无锡", "宁波", "长沙", "郑州", "济南"};
            this.hometown = cities[random.nextInt(cities.length)];
            this.location = cities[random.nextInt(cities.length)];
            
            // 公司和职业 (中老年用户常见职业)
            String[] companies = {"国有企业", "事业单位", "民营企业", "外资企业", "政府部门", "教育机构", "医疗机构"};
            String[] carriers = {"工程师", "教师", "医生", "公务员", "销售经理", "财务主管", "人力资源", 
                               "技术总监", "项目经理", "行政主管", "退休人员", "自由职业者"};
            
            this.company = companies[random.nextInt(companies.length)];
            this.carrier = carriers[random.nextInt(carriers.length)];
        }
        
        // 加权随机选择
        private int weightedRandomChoice(int[] choices, int[] weights) {
            Random random = new Random();
            int totalWeight = 0;
            for (int weight : weights) {
                totalWeight += weight;
            }
            
            int randomValue = random.nextInt(totalWeight);
            int currentWeight = 0;
            
            for (int i = 0; i < choices.length; i++) {
                currentWeight += weights[i];
                if (randomValue < currentWeight) {
                    return choices[i];
                }
            }
            return choices[0];
        }
        
        @Override
        public String toString() {
            return String.format("用户: %s (%s), 头像: %s, 相册数量: %d, 性别: %s, 年龄: %d岁", 
                nickname, realName, avatarPath, albumPaths.size(), 
                sex == 1 ? "男" : "女", 2024 - (birthday / 10000));
        }
    }
    
    // 文件夹信息类
    public static class FolderInfo {
        private String folderPath;
        private String excelFile;
        private String avatarFolder;
        private String albumFolder;
        private List<UserInfo> users;
        private List<String> avatarFiles;
        private List<String> albumFiles;
        
        public FolderInfo(String folderPath) {
            this.folderPath = folderPath;
            this.users = new ArrayList<>();
            this.avatarFiles = new ArrayList<>();
            this.albumFiles = new ArrayList<>();
        }
        
        // Getters and Setters
        public String getFolderPath() { return folderPath; }
        public String getExcelFile() { return excelFile; }
        public String getAvatarFolder() { return avatarFolder; }
        public String getAlbumFolder() { return albumFolder; }
        public List<UserInfo> getUsers() { return users; }
        public List<String> getAvatarFiles() { return avatarFiles; }
        public List<String> getAlbumFiles() { return albumFiles; }
        
        public void setExcelFile(String excelFile) { this.excelFile = excelFile; }
        public void setAvatarFolder(String avatarFolder) { this.avatarFolder = avatarFolder; }
        public void setAlbumFolder(String albumFolder) { this.albumFolder = albumFolder; }
        public void addUser(UserInfo user) { this.users.add(user); }
        public void addAvatarFile(String file) { this.avatarFiles.add(file); }
        public void addAlbumFile(String file) { this.albumFiles.add(file); }
    }
    
    // 配置属性
    private static Properties config = new Properties();
    
    // OSS上传相关
    private static HttpClient httpClient;
    private static ExecutorService executorService;
    private static final ConcurrentHashMap<String, String> fileUrlCache = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        // 加载配置文件
        loadConfig();
        
        String basePath = config.getProperty("data.base.path", "/Users/zty/Downloads/机器人");
        
        try {
            // 扫描所有文件夹
            List<FolderInfo> folders = scanFolders(basePath);
            
            // 处理每个文件夹
            for (FolderInfo folder : folders) {
                System.out.println("处理文件夹: " + folder.getFolderPath());
                
                // 读取Excel文件
                readExcelFile(folder);
                
                // 扫描图片文件
                scanImageFiles(folder);
                
                // 分配头像和相册图片
                assignImages(folder);
                
                // 上传分配给用户的图片到OSS
                uploadAssignedImagesToOss(folder);
                
                // 输出结果
                printResults(folder);
                
                System.out.println("----------------------------------------");
            }
            
                    // 生成导入脚本
        generateImportScript(folders);
        
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        // 关闭线程池
        executorService.shutdown();
    }
}
    
    // 加载配置文件
    private static void loadConfig() {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            config.load(fis);
            System.out.println("配置文件加载成功");
        } catch (IOException e) {
            System.out.println("警告: 未找到配置文件，使用默认设置");
            // 设置默认值
            config.setProperty("data.base.path", "/Users/zty/Downloads/robot");
            config.setProperty("album.count.min", "3");
            config.setProperty("album.count.max", "5");
            config.setProperty("image.extensions", "jpg,jpeg,png,gif");
            config.setProperty("output.sql.file", "robot_import_script.sql");
            config.setProperty("console.verbose", "true");
            config.setProperty("db.table.users", "robot_users");
            config.setProperty("db.table.albums", "robot_albums");
            config.setProperty("oss.upload.url", "https://app.beatconnects.com/app/upload/pic");
            config.setProperty("oss.upload.threads", "10");
            config.setProperty("oss.upload.timeout", "30000");
            config.setProperty("oss.avatar.key", "avatar");
            config.setProperty("oss.photo.key", "photo");
        }
        
        // 初始化OSS相关组件
        int threads = Integer.parseInt(config.getProperty("oss.upload.threads", "10"));
        int timeout = Integer.parseInt(config.getProperty("oss.upload.timeout", "30000"));
        
        httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofMillis(timeout))
            .build();
        executorService = Executors.newFixedThreadPool(threads);
        
        System.out.println("OSS上传组件初始化完成，线程数: " + threads + ", 超时时间: " + timeout + "ms");
    }
    
    // 扫描所有文件夹
    private static List<FolderInfo> scanFolders(String basePath) throws IOException {
        List<FolderInfo> folders = new ArrayList<>();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(basePath))) {
            for (Path path : stream) {
                if (Files.isDirectory(path) && path.getFileName().toString().matches("\\d{3}-\\d{8}")) {
                    FolderInfo folder = new FolderInfo(path.toString());
                    
                    // 查找Excel文件
                    try (DirectoryStream<Path> fileStream = Files.newDirectoryStream(path)) {
                        for (Path file : fileStream) {
                            String fileName = file.getFileName().toString();
                            // 排除临时文件和隐藏文件，只选择有效的Excel文件
                            if (Files.isRegularFile(file) && 
                                fileName.endsWith(".xlsx") && 
                                !fileName.startsWith(".~") && 
                                !fileName.startsWith("~$") &&
                                !fileName.startsWith(".")) {
                                folder.setExcelFile(file.toString());
                                break;
                            }
                        }
                    }
                    
                    // 查找TX和XC文件夹
                    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
                        for (Path dir : dirStream) {
                            if (Files.isDirectory(dir)) {
                                String dirName = dir.getFileName().toString();
                                if (dirName.contains("TX")) {
                                    folder.setAvatarFolder(dir.toString());
                                } else if (dirName.contains("XC")) {
                                    folder.setAlbumFolder(dir.toString());
                                }
                            }
                        }
                    }
                    
                    folders.add(folder);
                }
            }
        }
        
        return folders;
    }
    
    // 读取Excel文件
    private static void readExcelFile(FolderInfo folder) throws IOException {
        if (folder.getExcelFile() == null) {
            System.out.println("警告: 未找到Excel文件");
            return;
        }
        
        System.out.println("正在读取Excel文件: " + folder.getExcelFile());
        
        try (FileInputStream fis = new FileInputStream(folder.getExcelFile());
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 跳过标题行
                
                Cell nicknameCell = row.getCell(2); // 昵称在第3列（索引2）
                Cell nameCell = row.getCell(3);     // 姓名在第4列（索引3）
                
                if (nicknameCell != null && nameCell != null) {
                    String nickname = getCellValueAsString(nicknameCell);
                    String realName = getCellValueAsString(nameCell);
                    
                    if (!nickname.trim().isEmpty() && !realName.trim().isEmpty()) {
                        UserInfo user = new UserInfo(nickname.trim(), realName.trim());
                        folder.addUser(user);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("读取Excel文件失败: " + folder.getExcelFile());
            System.err.println("错误信息: " + e.getMessage());
            if (e instanceof org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException) {
                System.err.println("该文件不是有效的Excel文件，可能是临时文件或损坏的文件");
                System.err.println("请检查文件是否完整，或者删除临时文件后重试");
            }
            throw new IOException("无法读取Excel文件: " + folder.getExcelFile(), e);
        }
        
        System.out.println("读取到 " + folder.getUsers().size() + " 个用户");
    }
    
    // 获取单元格值
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }
    
    // 扫描图片文件
    private static void scanImageFiles(FolderInfo folder) throws IOException {
        // 扫描头像文件
        if (folder.getAvatarFolder() != null) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folder.getAvatarFolder()))) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file) && isImageFile(file.toString())) {
                        folder.addAvatarFile(file.getFileName().toString());
                    }
                }
            }
        }
        
        // 扫描相册文件
        if (folder.getAlbumFolder() != null) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folder.getAlbumFolder()))) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file) && isImageFile(file.toString())) {
                        folder.addAlbumFile(file.getFileName().toString());
                    }
                }
            }
        }
        
        System.out.println("头像文件: " + folder.getAvatarFiles().size() + " 个");
        System.out.println("相册文件: " + folder.getAlbumFiles().size() + " 个");
    }
    
    // 判断是否为图片文件
    private static boolean isImageFile(String fileName) {
        String extensions = config.getProperty("image.extensions", "jpg,jpeg,png,gif");
        String[] extArray = extensions.split(",");
        String lowerName = fileName.toLowerCase();
        
        for (String ext : extArray) {
            if (lowerName.endsWith("." + ext.trim())) {
                return true;
            }
        }
        return false;
    }
    
    // 分配头像和相册图片
    private static void assignImages(FolderInfo folder) {
        List<String> avatarFiles = new ArrayList<>(folder.getAvatarFiles());
        List<String> albumFiles = new ArrayList<>(folder.getAlbumFiles());
        
        // 随机打乱文件列表
        Collections.shuffle(avatarFiles);
        Collections.shuffle(albumFiles);
        
        int avatarIndex = 0;
        int albumIndex = 0;
        
        for (UserInfo user : folder.getUsers()) {
            // 分配头像
            if (avatarIndex < avatarFiles.size()) {
                String avatarFileName = avatarFiles.get(avatarIndex++);
                user.setAvatarPath(avatarFileName); // 先设置为文件名
            }
            
            // 分配相册图片
            int minCount = Integer.parseInt(config.getProperty("album.count.min", "3"));
            int maxCount = Integer.parseInt(config.getProperty("album.count.max", "5"));
            int albumCount = minCount + new Random().nextInt(maxCount - minCount + 1);
            for (int i = 0; i < albumCount && albumIndex < albumFiles.size(); i++) {
                String albumFileName = albumFiles.get(albumIndex++);
                user.addAlbumPath(albumFileName); // 先添加文件名
            }
        }
    }
    
    // 输出结果
    private static void printResults(FolderInfo folder) {
        boolean verbose = Boolean.parseBoolean(config.getProperty("console.verbose", "true"));
        if (!verbose) return;
        
        System.out.println("\n分配结果:");
        int validUsers = 0;
        int invalidUsers = 0;
        
        for (UserInfo user : folder.getUsers()) {
            if (user.getAvatarPath() == null || user.getAvatarPath().trim().isEmpty()) {
                System.out.println("⚠️  用户无头像: " + user.getNickname() + " (" + user.getRealName() + ")");
                invalidUsers++;
            } else {
                System.out.println("✅ 用户: " + user.getNickname() + " (" + user.getRealName() + ")");
                System.out.println("  头像文件: " + user.getAvatarPath());
                System.out.println("  性别: " + (user.getSex() == 1 ? "男" : "女") + 
                                 ", 年龄: " + (2024 - (user.getBirthday() / 10000)) + "岁");
                System.out.println("  手机: " + user.getPhone() + 
                                 ", 情感状态: " + getEmotionStatusText(user.getEmotionStatus()) +
                                 ", 工作状态: " + getWorkStatusText(user.getWorkStatus()));
                System.out.println("  家乡: " + user.getHometown() + 
                                 ", 所在地: " + user.getLocation());
                System.out.println("  公司: " + user.getCompany() + 
                                 ", 职业: " + user.getCarrier());
                System.out.println("  相册图片文件: " + String.join(", ", user.getAlbumPaths()));
                validUsers++;
            }
        }
        
        System.out.println("\n统计信息:");
        System.out.println("有效用户（有头像）: " + validUsers + " 个");
        System.out.println("无效用户（无头像）: " + invalidUsers + " 个");
        System.out.println("总用户数: " + folder.getUsers().size() + " 个");
    }
    
    // 生成导入脚本
    private static void generateImportScript(List<FolderInfo> folders) throws IOException {
        String outputFile = config.getProperty("output.sql.file", "robot_import_script.sql");
        String usersTable = config.getProperty("db.table.users", "joy_profile");
        String albumsTable = config.getProperty("db.table.albums", "joy_photo");
        
        StringBuilder script = new StringBuilder();
        script.append("-- 机器人数据导入脚本\n");
        script.append("-- 生成时间: ").append(new Date()).append("\n\n");

        // 生成用户ID序列
        long userId = 1; // 起始用户ID
        long photoId = 1; // 起始照片ID
        long albumId = 0; // 起始相册ID
        long robotId = 7527642214542049306L; // 固定的机器人ID
        
        for (FolderInfo folder : folders) {
            script.append("-- 文件夹: ").append(folder.getFolderPath()).append("\n");
            
            for (UserInfo user : folder.getUsers()) {
                // 过滤掉头像为空的用户
                if (user.getAvatarPath() == null || user.getAvatarPath().trim().isEmpty()) {
                    System.out.println("跳过头像为空的用户: " + user.getNickname());
                    continue;
                }
                
                // 验证头像URL是否为有效的OSS链接
                if (user.getAvatarPath() == null || user.getAvatarPath().trim().isEmpty() || !user.getAvatarPath().startsWith("http")) {
                    System.out.println("警告: 用户头像不是有效的OSS链接: " + user.getNickname() + " - " + user.getAvatarPath());
                    continue;
                }
                
                // 生成唯一的open_id
                String openId = String.valueOf(System.currentTimeMillis() / 1000);
                
                // 插入用户信息到joy_profile表
                script.append("INSERT INTO ").append(usersTable).append(" (");
                script.append("user_id, open_id, nick_name, avatar, status, robot_id, ");
                script.append("phone, sex, birthday, emotion_status, work_status, verify_status, ");
                script.append("hometown, location, company, carrier");
                script.append(") VALUES (");
                script.append(userId).append(", ");
                script.append("'").append(openId).append("', ");
                script.append("'").append(escapeSql(user.getNickname())).append("', ");
                script.append("'").append(escapeSql(user.getAvatarPath())).append("', ");
                script.append("1, "); // status = 1 表示正常状态
                script.append(robotId).append(", "); // 使用固定的机器人ID
                script.append("'").append(user.getPhone()).append("', ");
                script.append(user.getSex()).append(", ");
                script.append(user.getBirthday()).append(", ");
                script.append(user.getEmotionStatus()).append(", ");
                script.append(user.getWorkStatus()).append(", ");
                script.append(user.getVerifyStatus()).append(", ");
                script.append("'").append(escapeSql(user.getHometown())).append("', ");
                script.append("'").append(escapeSql(user.getLocation())).append("', ");
                script.append("'").append(escapeSql(user.getCompany())).append("', ");
                script.append("'").append(escapeSql(user.getCarrier())).append("'");
                script.append(");\n");
                
                // 为每个用户创建一个相册，并插入相册图片
                for (String albumPath : user.getAlbumPaths()) {
                    // 验证相册图片URL是否为有效的OSS链接
                    if (albumPath == null || albumPath.trim().isEmpty() || !albumPath.startsWith("http")) {
                        System.out.println("警告: 相册图片不是有效的OSS链接: " + albumPath);
                        continue;
                    }
                    
                    script.append("INSERT INTO ").append(albumsTable).append(" (");
                    script.append("id, user_id, album_id, title, url, status");
                    script.append(") VALUES (");
                    script.append(photoId).append(", ");
                    script.append(userId).append(", ");
                    script.append(albumId).append(", ");
                    script.append("'相册图片', ");
                    script.append("'").append(escapeSql(albumPath)).append("', ");
                    script.append("1"); // status = 1 表示正常状态
                    script.append(");\n");
                    photoId++;
                }
                
                userId++;
            }
            script.append("\n");
        }
        
        // 写入文件
        Files.write(Paths.get(outputFile), script.toString().getBytes());
        System.out.println("导入脚本已生成: " + outputFile);
    }
    
    // 获取情感状态文本
    private static String getEmotionStatusText(int status) {
        switch (status) {
            case 1: return "单身";
            case 2: return "恋爱中";
            case 3: return "已婚";
            case 4: return "离异";
            default: return "未知";
        }
    }
    
    // 获取工作状态文本
    private static String getWorkStatusText(int status) {
        switch (status) {
            case 1: return "在职";
            case 2: return "退休";
            case 3: return "自由职业";
            default: return "未知";
        }
    }
    
    // SQL转义
    private static String escapeSql(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''");
    }
    
    // 上传分配给用户的图片到OSS
    private static void uploadAssignedImagesToOss(FolderInfo folder) {
        System.out.println("开始上传分配给用户的图片到OSS...");
        
        Set<String> avatarFilesToUpload = new HashSet<>();
        Set<String> albumFilesToUpload = new HashSet<>();
        
        // 收集需要上传的头像文件
        for (UserInfo user : folder.getUsers()) {
            if (user.getAvatarPath() != null && !user.getAvatarPath().trim().isEmpty()) {
                avatarFilesToUpload.add(user.getAvatarPath());
            }
        }
        
        // 收集需要上传的相册文件
        for (UserInfo user : folder.getUsers()) {
            for (String albumPath : user.getAlbumPaths()) {
                if (albumPath != null && !albumPath.trim().isEmpty()) {
                    albumFilesToUpload.add(albumPath);
                }
            }
        }
        
        System.out.println("需要上传的头像文件: " + avatarFilesToUpload.size() + " 个");
        System.out.println("需要上传的相册文件: " + albumFilesToUpload.size() + " 个");
        
        // 上传头像文件
        if (folder.getAvatarFolder() != null && !avatarFilesToUpload.isEmpty()) {
            String avatarKey = config.getProperty("oss.avatar.key", "avatar");
            uploadSpecificImages(folder.getAvatarFolder(), new ArrayList<>(avatarFilesToUpload), avatarKey);
        }
        
        // 上传相册文件
        if (folder.getAlbumFolder() != null && !albumFilesToUpload.isEmpty()) {
            String photoKey = config.getProperty("oss.photo.key", "photo");
            uploadSpecificImages(folder.getAlbumFolder(), new ArrayList<>(albumFilesToUpload), photoKey);
        }
        
        // 更新用户信息中的图片路径为OSS URL
        updateUserImagePaths(folder);
        
        System.out.println("图片上传完成");
    }
    
    // 上传指定的图片文件
    private static void uploadSpecificImages(String folderPath, List<String> files, String ossKey) {
        System.out.println("上传 " + folderPath + " 中的 " + files.size() + " 个指定文件到OSS (ossKey: " + ossKey + ")");
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (String fileName : files) {
            // 检查是否已经上传过
            if (fileUrlCache.containsKey(fileName)) {
                System.out.println("⏭️  跳过已上传文件: " + fileName);
                continue;
            }
            
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String filePath = Paths.get(folderPath, fileName).toString();
                    String url = uploadFileToOss(filePath, ossKey);
                    if (url != null) {
                        fileUrlCache.put(fileName, url);
                        System.out.println("✅ 上传成功: " + fileName + " -> " + url);
                    } else {
                        System.err.println("❌ 上传失败: " + fileName);
                    }
                } catch (Exception e) {
                    System.err.println("❌ 上传异常: " + fileName + " - " + e.getMessage());
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // 等待所有上传完成
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }
    
    // 更新用户信息中的图片路径为OSS URL
    private static void updateUserImagePaths(FolderInfo folder) {
        for (UserInfo user : folder.getUsers()) {
            // 更新头像路径
            if (user.getAvatarPath() != null && !user.getAvatarPath().trim().isEmpty()) {
                String avatarUrl = fileUrlCache.get(user.getAvatarPath());
                if (avatarUrl != null) {
                    user.setAvatarPath(avatarUrl);
                } else {
                    System.err.println("警告: 未找到头像URL: " + user.getAvatarPath());
                    user.setAvatarPath(""); // 设置为空字符串
                }
            }
            
            // 更新相册路径
            List<String> newAlbumPaths = new ArrayList<>();
            for (String albumPath : user.getAlbumPaths()) {
                if (albumPath != null && !albumPath.trim().isEmpty()) {
                    String albumUrl = fileUrlCache.get(albumPath);
                    if (albumUrl != null) {
                        newAlbumPaths.add(albumUrl);
                    } else {
                        System.err.println("警告: 未找到相册图片URL: " + albumPath);
                    }
                }
            }
            // 清空原有列表并添加新的URL
            user.getAlbumPaths().clear();
            user.getAlbumPaths().addAll(newAlbumPaths);
        }
    }
    
    // 上传单个文件到OSS
    private static String uploadFileToOss(String filePath, String ossKey) {
        try {
            // 读取文件内容
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            String fileName = Paths.get(filePath).getFileName().toString();
            
            // 构建multipart请求
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            byte[] multipartBody = buildMultipartBodyBytes(fileName, fileContent, ossKey, boundary);
            
            // 创建HTTP请求
            String uploadUrl = config.getProperty("oss.upload.url", "https://app.beatconnects.com/app/upload/pic");
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpblR5cGUiOiJsb2dpbiIsImxvZ2luSWQiOiJhcHA6MTk0NDIzOTE1OTA5MTI3MzcyOSIsInJuU3RyIjoibERBTGN4Mzg3TkRqNkpxTTR0S0pSQlVYbWVxYzlxQ0wiLCJ1c2VySWQiOjE5NDQyMzkxNTkwOTEyNzM3Mjl9.kAAIEba1T7d3qv8e30JSxbfOW2KPMuuZ0kLy5knipxI")
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();
            
            // 发送请求
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 解析响应
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                // 简单解析JSON响应，提取URL
                if (responseBody.contains("\"data\":")) {
                    int startIndex = responseBody.indexOf("\"data\":\"") + 8;
                    int endIndex = responseBody.indexOf("\"", startIndex);
                    if (startIndex > 7 && endIndex > startIndex) {
                        return responseBody.substring(startIndex, endIndex);
                    }
                }
            }
            
            System.err.println("上传失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
            return null;
            
        } catch (Exception e) {
            System.err.println("上传文件异常: " + filePath + " - " + e.getMessage());
            return null;
        }
    }
    
    // 构建multipart请求体（字节数组版本）
    private static byte[] buildMultipartBodyBytes(String fileName, byte[] fileContent, String ossKey, String boundary) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // 添加文件部分
            String fileHeader = "--" + boundary + "\r\n" +
                              "Content-Disposition: form-data; name=\"picFile\"; filename=\"" + fileName + "\"\r\n" +
                              "Content-Type: image/jpeg\r\n\r\n";
            baos.write(fileHeader.getBytes(StandardCharsets.UTF_8));
            baos.write(fileContent);
            baos.write("\r\n".getBytes(StandardCharsets.UTF_8));
            
            // 添加ossKey参数
            String ossKeyPart = "--" + boundary + "\r\n" +
                              "Content-Disposition: form-data; name=\"ossKey\"\r\n\r\n" +
                              ossKey + "\r\n";
            baos.write(ossKeyPart.getBytes(StandardCharsets.UTF_8));
            
            // 结束边界
            String endBoundary = "--" + boundary + "--\r\n";
            baos.write(endBoundary.getBytes(StandardCharsets.UTF_8));
            
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("构建multipart请求体失败", e);
        }
    }
} 