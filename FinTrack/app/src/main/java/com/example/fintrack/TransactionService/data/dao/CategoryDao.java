package com.example.fintrack.TransactionService.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fintrack.TransactionService.data.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    void insert(CategoryEntity category);

    @Update
    void update(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    @Query(
            "SELECT * FROM categories " +
                    "WHERE (user_id IS NULL OR user_id = :userId) " +
                    "AND parent_category_id IS NULL"
    )
    List<CategoryEntity> getParentCategories(String userId);

    @Query(
            "SELECT * FROM categories " +
                    "WHERE (user_id IS NULL OR user_id = :userId) " +
                    "AND parent_category_id = :parentId"
    )
    List<CategoryEntity> getChildren(String userId, String parentId);

    @Query(
            "SELECT * FROM categories " +
                    "WHERE user_id IS NULL OR user_id = :userId"
    )
    List<CategoryEntity> getAll(String userId);
    @Query(
            "SELECT * FROM categories " +
                    "WHERE parent_category_id IS NULL"
    )
    List<CategoryEntity> getAllParentCategories();

    @Query(
            "SELECT * FROM categories " +
                    "WHERE (user_id IS NULL OR user_id = :userId) " +
                    "AND parent_category_id IS NULL " +
                    "AND tx_type_id = :type"
    )
    List<CategoryEntity> getParentByType(String userId, String type);

}
