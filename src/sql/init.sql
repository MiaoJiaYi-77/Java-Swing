-- 创建数据库
CREATE DATABASE IF NOT EXISTS student_practice DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE student_practice;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    user_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 学生信息表
CREATE TABLE IF NOT EXISTS student_info (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    department VARCHAR(100),
    major VARCHAR(100),
    class_name VARCHAR(100),
    teacher_id INT,
    skills TEXT,
    gpa DECIMAL(3,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- 简历表
CREATE TABLE IF NOT EXISTS resumes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    title VARCHAR(200),
    content TEXT,
    file_path VARCHAR(255),
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- 企业信息表
CREATE TABLE IF NOT EXISTS company_info (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    company_name VARCHAR(200),
    address TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 实习岗位表
CREATE TABLE IF NOT EXISTS internship_positions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    company_id INT NOT NULL,
    title VARCHAR(200),
    description TEXT,
    requirements TEXT,
    quota INT,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company_info(id)
);

-- 实习申请表
CREATE TABLE IF NOT EXISTS internship_applications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    position_id INT NOT NULL,
    resume_id INT NOT NULL,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (position_id) REFERENCES internship_positions(id),
    FOREIGN KEY (resume_id) REFERENCES resumes(id)
);

-- 实习评分表
CREATE TABLE IF NOT EXISTS internship_scores (
    id INT PRIMARY KEY AUTO_INCREMENT,
    application_id INT NOT NULL,
    scorer_id INT NOT NULL,
    score DECIMAL(3,1),
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES internship_applications(id),
    FOREIGN KEY (scorer_id) REFERENCES users(id)
);

-- 实习日志表
CREATE TABLE IF NOT EXISTS internship_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    content TEXT,
    date DATE,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- 讨论区表
CREATE TABLE IF NOT EXISTS discussion_posts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(200),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 讨论回复表
CREATE TABLE IF NOT EXISTS discussion_replies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES discussion_posts(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 插入管理员用户
INSERT INTO users (id, username, password, name, email, phone, user_type) VALUES
    (1, 'admin', '123456', '系统管理员', 'admin@school.edu', '13800000000', '管理员');

-- 插入教师用户
INSERT INTO users (id, username, password, name, email, phone, user_type) VALUES
                                                                              (2, 'teacher1', '123456', '张教授', 'teacher1@school.edu', '13800000001', '教师'),
                                                                              (3, 'teacher2', '123456', '李教授', 'teacher2@school.edu', '13800000002', '教师'),
                                                                              (4, 'teacher3', '123456', '王教授', 'teacher3@school.edu', '13800000003', '教师');

-- 插入学生用户
INSERT INTO users (id, username, password, name, email, phone, user_type) VALUES
                                                                              (5, 'student1', '123456', '张三', 'student1@school.edu', '13810000001', '学生'),
                                                                              (6, 'student2', '123456', '李四', 'student2@school.edu', '13810000002', '学生'),
                                                                              (7, 'student3', '123456', '王五', 'student3@school.edu', '13810000003', '学生'),
                                                                              (8, 'student4', '123456', '赵六', 'student4@school.edu', '13810000004', '学生'),
                                                                              (9, 'student5', '123456', '钱七', 'student5@school.edu', '13810000005', '学生');

-- 插入企业用户
INSERT INTO users (id, username, password, name, email, phone, user_type) VALUES
                                                                              (10, 'company1', '123456', '腾讯科技HR', 'hr@tencent.com', '13820000001', '企业'),
                                                                              (11, 'company2', '123456', '阿里巴巴HR', 'hr@alibaba.com', '13820000002', '企业'),
                                                                              (12, 'company3', '123456', '百度HR', 'hr@baidu.com', '13820000003', '企业');

-- 插入学生信息
INSERT INTO student_info (user_id, department, major, class_name, teacher_id, skills, gpa) VALUES
                                                                                               (5, '计算机学院', '计算机科学与技术', '计科1801', 2, 'Java, Python, MySQL', 3.8),
                                                                                               (6, '计算机学院', '软件工程', '软件1801', 3, 'C++, JavaScript, React', 3.6),
                                                                                               (7, '信息学院', '电子信息工程', '电信1801', 2, '嵌入式开发, C语言', 3.5),
                                                                                               (8, '信息学院', '通信工程', '通信1801', 4, '5G, 网络协议', 3.7),
                                                                                               (9, '管理学院', '工商管理', '工商1801', 4, '市场营销, 项目管理', 3.9);

-- 插入企业信息
INSERT INTO company_info (id, user_id, company_name, address, description) VALUES
                                                                               (1, 10, '腾讯科技', '广东省深圳市南山区腾讯大厦', '中国领先的互联网综合服务提供商'),
                                                                               (2, 11, '阿里巴巴集团', '浙江省杭州市余杭区文一西路969号', '全球领先的电子商务平台'),
                                                                               (3, 12, '百度公司', '北京市海淀区上地十街10号百度大厦', '全球最大的中文搜索引擎');

-- 插入简历数据
INSERT INTO resumes (id, student_id, title, content, file_path, status) VALUES
                                                                            (1, 5, '张三的简历', '熟练掌握Java和Python开发，有多个项目经验', '/resumes/student1_resume1.pdf', '待投递'),
                                                                            (2, 5, '张三的英文简历', 'English version of my resume', '/resumes/student1_resume2.pdf', '待投递'),
                                                                            (3, 6, '李四的简历', '前端开发工程师，熟悉React框架', '/resumes/student2_resume1.pdf', '待投递'),
                                                                            (4, 7, '王五的简历', '嵌入式开发工程师，熟悉C语言和单片机', '/resumes/student3_resume1.pdf', '待投递'),
                                                                            (5, 8, '赵六的简历', '通信工程专业，熟悉5G技术', '/resumes/student4_resume1.pdf', '待投递'),
                                                                            (6, 9, '钱七的简历', '工商管理专业，擅长项目管理和市场营销', '/resumes/student5_resume1.pdf', '待投递');

-- 插入实习岗位
INSERT INTO internship_positions (id, company_id, title, description, requirements, quota, status) VALUES
                                                                                                       (1, 1, 'Java后端开发实习生', '参与腾讯云相关产品的后端开发', '熟悉Java, Spring框架, MySQL', 5, '招聘中'),
                                                                                                       (2, 1, '前端开发实习生', '参与微信小程序的前端开发', '熟悉JavaScript, HTML, CSS, React', 3, '招聘中'),
                                                                                                       (3, 2, '产品经理实习生', '参与阿里巴巴电商平台的产品设计', '良好的沟通能力, 逻辑思维强', 2, '招聘中'),
                                                                                                       (4, 2, '数据分析实习生', '参与用户行为数据分析', '熟悉Python, SQL, 数据分析基础', 3, '招聘中'),
                                                                                                       (5, 3, 'AI算法实习生', '参与百度搜索算法优化', '熟悉机器学习算法, Python', 2, '招聘中'),
                                                                                                       (6, 3, '测试开发实习生', '参与百度产品的自动化测试', '熟悉Java/Python, 了解测试框架', 4, '招聘中');

-- 插入实习申请
INSERT INTO internship_applications (id, student_id, position_id, resume_id, status) VALUES
                                                                                         (1, 5, 1, 1, '待审核'),
                                                                                         (2, 5, 2, 2, '待审核'),
                                                                                         (3, 6, 2, 3, '待审核'),
                                                                                         (4, 7, 5, 4, '待审核'),
                                                                                         (5, 8, 4, 5, '待审核'),
                                                                                         (6, 9, 3, 6, '待审核');

-- 插入实习评分
INSERT INTO internship_scores (id, application_id, scorer_id, score, comments) VALUES
                                                                                   (1, 3, 10, 4.5, '表现优秀，学习能力强'),
                                                                                   (2, 6, 11, 4.0, '沟通能力好，需要加强技术能力');

-- 插入实习日志
INSERT INTO internship_logs (id, student_id, content, date, status) VALUES
                                                                        (1, 6, '第一天入职，熟悉团队和环境', '2023-07-01', '已提交'),
                                                                        (2, 6, '开始参与项目开发，学习公司框架', '2023-07-02', '已提交'),
                                                                        (3, 9, '参加产品需求讨论会', '2023-07-01', '已提交');

-- 插入讨论区帖子
INSERT INTO discussion_posts (id, user_id, title, content) VALUES
                                                               (1, 5, '腾讯实习面试经验分享', '上周参加了腾讯的面试，分享一些经验...'),
                                                               (2, 6, '阿里巴巴实习感受', '在阿里实习一个月了，谈谈我的感受...'),
                                                               (3, 2, '实习注意事项', '各位同学实习期间请注意以下事项...');

-- 插入讨论回复
INSERT INTO discussion_replies (id, post_id, user_id, content) VALUES
                                                                   (1, 1, 6, '感谢分享，很有帮助！'),
                                                                   (2, 1, 7, '请问技术面主要问了哪些问题？'),
                                                                   (3, 2, 5, '阿里的工作环境怎么样？'),
                                                                   (4, 3, 8, '谢谢老师的提醒！');

-- 给张三（user_id=5，teacher_id=2）的实习申请打分
INSERT INTO internship_scores (application_id, scorer_id, score, comments) VALUES
                                                                               (1, 2, 4.2, '张三表现良好'),
                                                                               (2, 2, 4.0, '张三有进步空间');

-- 给王五（user_id=7，teacher_id=2）的实习申请打分
INSERT INTO internship_scores (application_id, scorer_id, score, comments) VALUES
    (4, 2, 3.8, '王五需要加强沟通');


