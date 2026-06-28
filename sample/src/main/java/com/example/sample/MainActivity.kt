package com.example.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.roomauditkit.AuditAction
import com.example.roomauditkit.AuditLogBuilder
import com.example.roomauditkit.AuditLogger
import com.example.roomauditkit.RoomAuditKit
import com.google.gson.Gson
import java.util.UUID
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val products = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter
    private val gson = Gson()
    private val entityName = "Product"
    private lateinit var auditLogger: AuditLogger
    private lateinit var productRepository: ProductRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize audit logger via Builder API
        auditLogger = RoomAuditKit.with(this)
            .setDatabaseName("room_audit_db")
            .build()

        // Initialize product persistence
        productRepository = ProductRepository(this)

        val btnAddProduct = findViewById<Button>(R.id.btnAddProduct)
        val btnViewLogs = findViewById<Button>(R.id.btnViewLogs)
        val listViewProducts = findViewById<ListView>(R.id.listViewProducts)

        adapter = ProductAdapter()
        listViewProducts.adapter = adapter

        // Load persisted products on startup
        products.addAll(productRepository.getAllProducts())
        adapter.notifyDataSetChanged()

        btnAddProduct.setOnClickListener {
            val id = UUID.randomUUID().toString().substring(0, 8)
            val name = "Product ${Random.nextInt(100, 999)}"
            val price = Math.round(Random.nextDouble(10.0, 100.0) * 100) / 100.0
            val newProduct = Product(id, name, price)

            // Persist first, then update UI list
            productRepository.addProduct(newProduct)
            products.add(0, newProduct)
            adapter.notifyDataSetChanged()

            // Log INSERT using AuditLogBuilder
            auditLogger.log(
                AuditLogBuilder()
                    .action(AuditAction.INSERT)
                    .entityName(entityName)
                    .itemId(id)
                    .newValueJson(gson.toJson(newProduct))
                    .build()
            )
        }

        btnViewLogs.setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
        }
    }

    private fun editProduct(product: Product, position: Int) {
        val oldJson = gson.toJson(product)

        // Mutate
        product.name = product.name + " (Edited)"
        product.price = Math.round((product.price + 5.0) * 100) / 100.0

        // Persist updated product
        productRepository.updateProduct(product)
        adapter.notifyDataSetChanged()

        val newJson = gson.toJson(product)

        // Log UPDATE
        auditLogger.logUpdate(
            entityName = entityName,
            itemId = product.id,
            oldValueJson = oldJson,
            newValueJson = newJson
        )
    }

    private fun deleteProduct(product: Product, position: Int) {
        val oldJson = gson.toJson(product)

        // Persist deletion first, then update UI list
        productRepository.deleteProduct(product.id)
        products.removeAt(position)
        adapter.notifyDataSetChanged()

        // Log DELETE
        auditLogger.logDelete(
            entityName = entityName,
            itemId = product.id,
            oldValueJson = oldJson
        )
    }

    inner class ProductAdapter : BaseAdapter() {
        override fun getCount(): Int = products.size
        override fun getItem(position: Int): Any = products[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_product, parent, false)
            val product = products[position]

            view.findViewById<TextView>(R.id.tvProductName).text = product.name
            view.findViewById<TextView>(R.id.tvProductPrice).text =
                String.format("$%.2f", product.price)

            view.findViewById<Button>(R.id.btnEdit).setOnClickListener {
                editProduct(product, position)
            }
            view.findViewById<Button>(R.id.btnDelete).setOnClickListener {
                deleteProduct(product, position)
            }

            return view
        }
    }
}
