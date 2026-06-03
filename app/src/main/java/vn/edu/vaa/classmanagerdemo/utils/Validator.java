package vn.edu.vaa.classmanagerdemo.utils;

import android.util.Patterns;
import android.widget.EditText;

public class Validator {
    public static boolean require(EditText editText, String message) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            editText.setError(message);
            editText.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean optionalEmail(EditText editText, String message) {
        String value = editText.getText().toString().trim();
        if (!value.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            editText.setError(message);
            editText.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean optionalPhone(EditText editText, String message) {
        String value = editText.getText().toString().trim();
        if (!value.isEmpty() && !value.matches("[0-9]{10,11}")) {
            editText.setError(message);
            editText.requestFocus();
            return false;
        }
        return true;
    }
}
