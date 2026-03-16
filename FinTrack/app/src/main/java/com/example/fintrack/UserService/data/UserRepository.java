package com.example.fintrack.UserService.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fintrack.UserService.data.dao.UserDao;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.UserService.data.entity.UserEntity;

public class UserRepository {

    private final UserDao userDao;
    private final SharedPreferences prefs;

    public UserRepository(Context context) {

        FintrackDatabase db = FintrackDatabase.getInstance(context);
        userDao = db.userDao();

        prefs = context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE);
    }

    // ================= REGISTER =================
    public boolean register(String email, String fullName, String password) {

        UserEntity exist = userDao.getUserByEmail(email); //ktra email trùng
        UserEntity existUsername = userDao.getUserByUsername(fullName);//ktra fullname/username trùng
        if (exist != null || existUsername != null) {
            return false;
        }

        UserEntity user = new UserEntity();

        user.user_id = String.valueOf(System.currentTimeMillis());
        user.email = email;
        user.full_name = fullName;
        user.password = password;
        user.status = "Standard Member";
        user.created_at = String.valueOf(System.currentTimeMillis());

        userDao.insertUser(user);

        saveSession(user.user_id);

        return true;
    }
    // ================= GET USER BY USERNAME =================
    public UserEntity getUserByUsername(String username) {
        return userDao.getUserByUsername(username);
    }
    // ================= LOGIN =================
    public boolean login(String username, String password) {

        UserEntity user = userDao.login(username, password);

        if (user == null) {
            return false;
        }

        saveSession(user.user_id);

        return true;
    }

    // ================= UPDATE USER =================
    public void updateUser(String userId, String username, String password, String avatar) {
        userDao.updateUser(userId, username, password, avatar);
    }



    // ================= GET USER =================
    public UserEntity getCurrentUser() {

        String userId = prefs.getString("user_id", null);

        if (userId == null) return null;

        return userDao.getUserById(userId);
    }
    // ================= GET USER BY EMAIL =================
    public UserEntity getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    // ================= SESSION =================
    private void saveSession(String userId) {

        prefs.edit()
                .putString("user_id", userId)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getString("user_id", null) != null;
    }

    // ================= LOGOUT =================
    public void logout() {

        prefs.edit()
                .clear()
                .apply();
    }

    // ================= FORGOT PASSWORD =================
    public void updatePasswordByEmail(String email, String newPassword) {
        userDao.updatePasswordByEmail(email, newPassword);
    }
}