package com.example.fintrack.TransactionService.domain.usecase;

import com.example.fintrack.TransactionService.data.dao.CategoryDao;
import com.example.fintrack.TransactionService.data.entity.CategoryEntity;

import java.util.List;

public class DeleteCategoryUseCase {

    private final CategoryDao categoryDao;

    public DeleteCategoryUseCase(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public void execute(String userId, CategoryEntity category) {

        List<CategoryEntity> children =
                categoryDao.getChildren(userId, category.category_id);

        // 2. Không cho xoá nếu còn danh mục con
        if (children != null && !children.isEmpty()) {
            throw new IllegalStateException("Không thể xoá danh mục cha");
        }

        // 3. Xoá danh mục
        categoryDao.delete(category);
    }
}
