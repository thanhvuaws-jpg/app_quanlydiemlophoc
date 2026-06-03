package vn.edu.vaa.classmanagerdemo.storage;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.models.Todo;

public class JsonTodoStorage {
    public static final String FILE_NAME = "todos.json";

    public static void save(Context context, List<Todo> todos) throws IOException, JSONException {
        JSONArray arr = new JSONArray();
        for (Todo t : todos) {
            JSONObject obj = new JSONObject();
            obj.put("id", t.getId());
            obj.put("title", t.getTitle());
            obj.put("deadline", t.getDeadline());
            obj.put("completed", t.isCompleted());
            arr.put(obj);
        }
        TextFileManager.writeText(context, FILE_NAME, arr.toString(2));
    }

    public static List<Todo> load(Context context) throws IOException, JSONException {
        String json;
        try {
            json = TextFileManager.readText(context, FILE_NAME);
        } catch (IOException e) {
            List<Todo> seedList = new ArrayList<>();
            seedList.add(new Todo(1, "Học lý thuyết SharedPreferences", "28/05/2026", true));
            seedList.add(new Todo(2, "Thực hành SQLite CRUD Sinh viên", "01/06/2026", false));
            seedList.add(new Todo(3, "Tìm hiểu cấu trúc file XML & CSV", "05/06/2026", false));
            save(context, seedList);
            return seedList;
        }
        List<Todo> list = new ArrayList<>();
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            list.add(new Todo(
                    obj.getInt("id"),
                    obj.getString("title"),
                    obj.optString("deadline", ""),
                    obj.optBoolean("completed", false)
            ));
        }
        return list;
    }

    public static String readRawJson(Context context) {
        try {
            return TextFileManager.readText(context, FILE_NAME);
        } catch (IOException e) {
            return "(File todos.json chưa tồn tại)";
        }
    }
}
