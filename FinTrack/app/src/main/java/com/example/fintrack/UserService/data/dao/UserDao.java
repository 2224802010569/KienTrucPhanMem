package com.example.fintrack.UserService.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.fintrack.UserService.data.entity.UserEntity;

@Dao
public interface UserDao {

    @Insert
    void insertUser(UserEntity user);

    // tìm user theo email (dùng khi register check trùng email)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);
    // tìm user theo fullname/username trùng
    @Query("SELECT * FROM users WHERE full_name = :username LIMIT 1")
    UserEntity getUserByUsername(String username);

    // tìm user theo username (full_name) và passwword
    @Query("SELECT * FROM users WHERE full_name = :username AND password = :password LIMIT 1")
    UserEntity login(String username, String password);

    // tìm theo user_id
    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    UserEntity getUserById(String userId);

    // phần cập nhat thông tin người dùng với 2 phần thuộc tính tự thêm đó là mk và avatar
    @Query("UPDATE users SET full_name = :username, password = :password, avatar = :avatar WHERE user_id = :userId")
    void updateUser(String userId, String username, String password, String avatar);

    // Cập nhật mật khẩu bằng email (Dùng cho Quên mật khẩu)
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    void updatePasswordByEmail(String email, String newPassword);
}