package vn.edu.vaa.classmanagerdemo.utils;

public class ExplanationBuilder {
    public static String build(String event, String input, String validation,
                               String processing, String storage, String result) {
        return "Sự kiện:\n" + event + "\n\n" +
                "Dữ liệu lấy từ View:\n" + input + "\n\n" +
                "Kiểm tra dữ liệu:\n" + validation + "\n\n" +
                "Xử lý:\n" + processing + "\n\n" +
                "Lưu trữ:\n" + storage + "\n\n" +
                "Kết quả:\n" + result;
    }

    public static String simple(String title, String detail) {
        return title + "\n\n" + detail;
    }
}
