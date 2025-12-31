package com.financialmanager.app.util

import android.content.Context
import com.financialmanager.app.data.entities.InventoryItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object InventoryImporter {
    fun importFromAssets(context: Context): List<InventoryItem> {
        val items = mutableListOf<InventoryItem>()
        try {
            val jsonString = context.assets.open("inventory_items.json")
                .bufferedReader()
                .use { it.readText() }
            
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val item = InventoryItem(
                    id = 0,
                    name = jsonObject.getString("name"),
                    category = if (jsonObject.isNull("category")) null else jsonObject.getString("category"),
                    quantity = jsonObject.getInt("quantity"),
                    purchasePrice = jsonObject.getDouble("purchasePrice"),
                    sellingPrice = jsonObject.getDouble("sellingPrice"),
                    wholesalePrice = jsonObject.getDouble("wholesalePrice"),
                    notes = if (jsonObject.isNull("notes")) null else jsonObject.getString("notes"),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                items.add(item)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }
}


