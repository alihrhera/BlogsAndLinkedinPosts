package hrhera.ali.backgroundsync.util

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


object FileSeparatorUtil {


    fun splitFileToChach(
        context: Context,
        inputFile: File,
        itemId: String,
        chunkSize: Int
    ): ItemFileInfo {

        val itemCacheDir = File(context.cacheDir, itemId)
        val jsonFile = File(itemCacheDir, "info.json")
        val result: ItemFileInfo? =
            itemFileInfo(jsonFile = jsonFile, itemCacheDir = itemCacheDir)

        if (result != null) {
            return result
        }

        val buffer = ByteArray(chunkSizeInByte(chunkSize).toInt())
        val partsMap = mutableMapOf<Int, String>()
        FileInputStream(inputFile).use { inputStream ->
            var bytesRead: Int
            var index = 0
            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                val chunkFile = File(
                    itemCacheDir,
                    "${inputFile.name}.part${index + 1}"
                )
                FileOutputStream(chunkFile).use { outputStream ->
                    outputStream.write(buffer, 0, bytesRead)
                }
                partsMap[index] = chunkFile.absolutePath
                index++
            }
        }

        val newItemInfo = ItemFileInfo(
            itemName = itemId,
            folderName = itemCacheDir.name,
            parts = partsMap,
            orignalFilePath = inputFile.absolutePath,
            chankSizeInMb = chunkSize
        )
        jsonFile.writeText(Gson().toJson(newItemInfo))

        return newItemInfo
    }

    private fun itemFileInfo(
        jsonFile: File,
        itemCacheDir: File
    ): ItemFileInfo? {
        val result: ItemFileInfo? =
            if (!itemCacheDir.exists()) {
                itemCacheDir.mkdirs()
                null
            } else {
                getOldInfo(
                    jsonFile = jsonFile,
                )
            }
        if (result == null || !itemCacheDir.exists()) {
            itemCacheDir.mkdirs()
        }
        return result
    }

    private fun chunkSizeInByte(chunkSizeInMb: Int): Long {
        return (chunkSizeInMb * 1024 * 1024).toLong()
    }

    private fun getOldInfo(jsonFile: File):
            ItemFileInfo? {

        if (jsonFile.exists()) {
            val gson = Gson()
            val itemInfo = gson.fromJson(jsonFile.readText(), ItemFileInfo::class.java)
            return itemInfo
        } else {
            return null
        }
    }
}

