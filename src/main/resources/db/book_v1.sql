-- Thêm tác giả
INSERT INTO authors (id, name, created_at, updated_at) VALUES
                                                           (1, 'J.K. Rowling', NOW(), NOW()),
                                                           (2, 'J.R.R. Tolkien', NOW(), NOW()),
                                                           (3, 'George Orwell', NOW(), NOW()),
                                                           (4, 'Nguyễn Nhật Ánh', NOW(), NOW());

-- Thêm dịch giả
INSERT INTO translators (id, name, created_at, updated_at) VALUES
    (1, 'Lý Lan', NOW(), NOW());

-- Thêm thể loại
INSERT INTO genres (id, name, created_at, updated_at) VALUES
                                                          (1, 'Fantasy', NOW(), NOW()),
                                                          (2, 'Dystopian', NOW(), NOW()),
                                                          (3, 'Novel', NOW(), NOW()),
                                                          (4, 'History', NOW(), NOW()),
                                                          (5, 'Natural Science', NOW(), NOW()),
                                                          (6, 'Social Science', NOW(), NOW()),
                                                          (7, 'Philosophy', NOW(), NOW()),
                                                          (8, 'Sociology', NOW(), NOW());

-- Thêm nhà xuất bản
INSERT INTO publishers (id, name, created_at, updated_at) VALUES
                                                              (1, 'Bloomsbury Publishing', NOW(), NOW()),
                                                              (2, 'Allen & Unwin', NOW(), NOW()),
                                                              (3, 'Secker & Warburg', NOW(), NOW()),
                                                              (4, 'Nhà Xuất Bản Trẻ', NOW(), NOW()),
                                                              (5, 'Nhà Xuất Bản Kim Đồng', NOW(), NOW()),
                                                              (6, 'Nhà Xuất Bản Giáo Dục Việt Nam', NOW(), NOW()),
                                                              (7, 'Nhà Xuất Bản Tri Thức', NOW(), NOW()),
                                                              (8, 'Nhà Xuất Bản Tổng Hợp TP.HCM', NOW(), NOW()),
                                                              (9, 'Nhà Xuất Bản Chính Trị Quốc Gia Sự Thật', NOW(), NOW());

-- 2. Thêm dữ liệu cho bảng books

INSERT INTO books (id, code, title, edition, publisher_id, publication_date, language, page_count, description, has_physical_edition, has_electric_edition, is_active, created_at, updated_at) VALUES
                                                                                                                                                                                                   (
                                                                                                                                                                                                       1,
                                                                                                                                                                                                       'HP1_UK',
                                                                                                                                                                                                       'Harry Potter and the Philosopher''s Stone',
                                                                                                                                                                                                       1,
                                                                                                                                                                                                       1, -- Bloomsbury Publishing
                                                                                                                                                                                                       '1997-06-26',
                                                                                                                                                                                                       'ENGLISH',
                                                                                                                                                                                                       223,
                                                                                                                                                                                                       'The first novel in the Harry Potter series and Rowling''s debut novel, it follows Harry Potter, a young wizard who discovers his magical heritage on his eleventh birthday, when he receives a letter of acceptance to Hogwarts School of Witchcraft and Wizardry.',
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       NOW(),
                                                                                                                                                                                                       NOW()
                                                                                                                                                                                                   ),
                                                                                                                                                                                                   (
                                                                                                                                                                                                       2,
                                                                                                                                                                                                       'TH01',
                                                                                                                                                                                                       'The Hobbit',
                                                                                                                                                                                                       1,
                                                                                                                                                                                                       2, -- Allen & Unwin
                                                                                                                                                                                                       '1937-09-21',
                                                                                                                                                                                                       'ENGLISH',
                                                                                                                                                                                                       310,
                                                                                                                                                                                                       'The Hobbit, or There and Back Again is a children''s fantasy novel by English author J. R. R. Tolkien.',
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       NOW(),
                                                                                                                                                                                                       NOW()
                                                                                                                                                                                                   ),
                                                                                                                                                                                                   (
                                                                                                                                                                                                       3,
                                                                                                                                                                                                       'NNA_MS',
                                                                                                                                                                                                       'Mắt Biếc',
                                                                                                                                                                                                       1,
                                                                                                                                                                                                       4, -- Nhà Xuất Bản Trẻ
                                                                                                                                                                                                       '1990-01-01',
                                                                                                                                                                                                       'VIETNAMESE',
                                                                                                                                                                                                       288,
                                                                                                                                                                                                       'Câu chuyện tình yêu đơn phương của Ngạn dành cho Hà Lan, một cô bạn gái có đôi mắt đẹp hút hồn nhưng lại hướng về Dũng, một chàng trai thành thị sành điệu.',
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       false,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       NOW(),
                                                                                                                                                                                                       NOW()
                                                                                                                                                                                                   ),
                                                                                                                                                                                                   (
                                                                                                                                                                                                       4,
                                                                                                                                                                                                       'DMPLK_VN',
                                                                                                                                                                                                       'Dế Mèn Phiêu Lưu Ký',
                                                                                                                                                                                                       1,
                                                                                                                                                                                                       5, -- Nhà Xuất Bản Kim Đồng
                                                                                                                                                                                                       '1941-01-01',
                                                                                                                                                                                                       'VIETNAMESE',
                                                                                                                                                                                                       180,
                                                                                                                                                                                                       'Tác phẩm nổi tiếng của nhà văn Tô Hoài, kể về hành trình phiêu lưu của chú dế mèn.',
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       NOW(),
                                                                                                                                                                                                       NOW()
                                                                                                                                                                                                   ),
                                                                                                                                                                                                   (
                                                                                                                                                                                                       5,
                                                                                                                                                                                                       'VNSL_VN',
                                                                                                                                                                                                       'Việt Nam sử lược',
                                                                                                                                                                                                       1,
                                                                                                                                                                                                       7, -- Nhà Xuất Bản Tri Thức
                                                                                                                                                                                                       '1919-01-01',
                                                                                                                                                                                                       'VIETNAMESE',
                                                                                                                                                                                                       800,
                                                                                                                                                                                                       'Tác phẩm lịch sử kinh điển tổng hợp về lịch sử Việt Nam của Trần Trọng Kim.',
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       NOW(),
                                                                                                                                                                                                       NOW()
                                                                                                                                                                                                   ),
                                                                                                                                                                                                   (
                                                                                                                                                                                                       6,
                                                                                                                                                                                                       'KHTN_VN',
                                                                                                                                                                                                       'Kiến thức bách khoa Khoa học Tự nhiên',
                                                                                                                                                                                                       1,
                                                                                                                                                                                                       6, -- Nhà Xuất Bản Giáo Dục Việt Nam
                                                                                                                                                                                                       '2005-01-01',
                                                                                                                                                                                                       'VIETNAMESE',
                                                                                                                                                                                                       400,
                                                                                                                                                                                                       'Tổng hợp kiến thức cơ bản về các lĩnh vực khoa học tự nhiên.',
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       NOW(),
                                                                                                                                                                                                       NOW()
                                                                                                                                                                                                   ),
                                                                                                                                                                                                   (
                                                                                                                                                                                                       7,
                                                                                                                                                                                                       'KHXH_VN',
                                                                                                                                                                                                       'Nhập môn Khoa học Xã hội',
                                                                                                                                                                                                       1,
                                                                                                                                                                                                       8, -- Nhà Xuất Bản Tổng Hợp TP.HCM
                                                                                                                                                                                                       '2010-01-01',
                                                                                                                                                                                                       'VIETNAMESE',
                                                                                                                                                                                                       320,
                                                                                                                                                                                                       'Giới thiệu nền tảng, phương pháp và phạm vi của các ngành khoa học xã hội.',
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       NOW(),
                                                                                                                                                                                                       NOW()
                                                                                                                                                                                                   ),
                                                                                                                                                                                                   (
                                                                                                                                                                                                       8,
                                                                                                                                                                                                       'THNM_VN',
                                                                                                                                                                                                       'Triết học nhập môn',
                                                                                                                                                                                                       1,
                                                                                                                                                                                                       7, -- Nhà Xuất Bản Tri Thức
                                                                                                                                                                                                       '2000-01-01',
                                                                                                                                                                                                       'VIETNAMESE',
                                                                                                                                                                                                       350,
                                                                                                                                                                                                       'Tác phẩm nhập môn, trình bày các trường phái và khái niệm nền tảng của triết học.',
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       NOW(),
                                                                                                                                                                                                       NOW()
                                                                                                                                                                                                   ),
                                                                                                                                                                                                   (
                                                                                                                                                                                                       9,
                                                                                                                                                                                                       'XHH_VN',
                                                                                                                                                                                                       'Xã hội học đại cương',
                                                                                                                                                                                                       1,
                                                                                                                                                                                                       9, -- Nhà Xuất Bản Chính Trị Quốc Gia Sự Thật
                                                                                                                                                                                                       '2012-01-01',
                                                                                                                                                                                                       'VIETNAMESE',
                                                                                                                                                                                                       300,
                                                                                                                                                                                                       'Giới thiệu những khái niệm cơ bản và phương pháp nghiên cứu của ngành xã hội học.',
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       true,
                                                                                                                                                                                                       NOW(),
                                                                                                                                                                                                       NOW()
                                                                                                                                                                                                   );

-- 3. Thêm dữ liệu cho các bảng join

-- Liên kết sách với tác giả
-- Thêm tác giả Việt Nam
INSERT INTO authors (id, name, created_at, updated_at) VALUES
                                                           (5, 'Tô Hoài', NOW(), NOW()),
                                                           (6, 'Trần Trọng Kim', NOW(), NOW()),
                                                           (7, 'Trần Hữu Quang', NOW(), NOW()),
                                                           (8, 'Nguyễn Hiến Lê', NOW(), NOW()),
                                                           (9, 'Nhiều Tác Giả', NOW(), NOW());

INSERT INTO books_authors (book_id, author_id) VALUES
                                                   (1, 1), -- Harry Potter -> J.K. Rowling
                                                   (2, 2), -- The Hobbit -> J.R.R. Tolkien
                                                   (3, 4), -- Mắt Biếc -> Nguyễn Nhật Ánh
                                                   (4, 5), -- Dế Mèn Phiêu Lưu Ký -> Tô Hoài
                                                   (5, 6), -- Việt Nam sử lược -> Trần Trọng Kim
                                                   (6, 9), -- Kiến thức bách khoa Khoa học Tự nhiên -> Nhiều Tác Giả
                                                   (7, 7), -- Nhập môn Khoa học Xã hội -> Trần Hữu Quang
                                                   (8, 8), -- Triết học nhập môn -> Nguyễn Hiến Lê
                                                   (9, 7); -- Xã hội học đại cương -> Trần Hữu Quang

-- Liên kết sách với dịch giả (ví dụ: bản dịch tiếng Việt của Harry Potter)
INSERT INTO books_translators (book_id, translator_id) VALUES
    (1, 1); -- Harry Potter -> Lý Lan

-- Liên kết sách với thể loại
-- Liên kết sách với thể loại (genres)
INSERT INTO books_genres (book_id, genre_id) VALUES
                                                 (1, 1), -- Harry Potter -> Fantasy
                                                 (2, 1), -- The Hobbit -> Fantasy
                                                 (3, 3), -- Mắt Biếc -> Novel
                                                 (4, 3), -- Dế Mèn Phiêu Lưu Ký -> Novel
                                                 (5, 4), -- Việt Nam sử lược -> History
                                                 (6, 5), -- Kiến thức bách khoa Khoa học Tự nhiên -> Natural Science
                                                 (7, 6), -- Nhập môn Khoa học Xã hội -> Social Science
                                                 (8, 7), -- Triết học nhập môn -> Philosophy
                                                 (9, 8); -- Xã hội học đại cương -> Sociology