package com.example.sample

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Persists the product list using SharedPreferences and Gson.
 * Each mutating operation immediately saves the full list to disk.
 */
class ProductRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("product_storage", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val listType = object : TypeToken<MutableList<Product>>() {}.type

    /** Returns all saved products, or an empty list if none exist. */
    fun getAllProducts(): MutableList<Product> {
        val json = prefs.getString(KEY_PRODUCTS, null) ?: return mutableListOf()
        return gson.fromJson(json, listType)
    }

    /** Adds a product and persists the updated list. */
    fun addProduct(product: Product) {
        val products = getAllProducts()
        products.add(0, product)
        save(products)
    }

    /** Updates an existing product by ID and persists the updated list. */
    fun updateProduct(product: Product) {
        val products = getAllProducts()
        val index = products.indexOfFirst { it.id == product.id }
        if (index != -1) {
            products[index] = product
            save(products)
        }
    }

    /** Deletes a product by ID and persists the updated list. */
    fun deleteProduct(productId: String) {
        val products = getAllProducts()
        products.removeAll { it.id == productId }
        save(products)
    }

    /** Clears all saved products. */
    fun clearProducts() {
        prefs.edit().remove(KEY_PRODUCTS).apply()
    }

    private fun save(products: List<Product>) {
        prefs.edit().putString(KEY_PRODUCTS, gson.toJson(products)).apply()
    }

    companion object {
        private const val KEY_PRODUCTS = "products"
    }
}
