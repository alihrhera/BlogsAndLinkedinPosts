package hrhera.ali.backgroundsync.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


@RunWith(RobolectricTestRunner::class)
class FileSeparatorUtilTest {

    private lateinit var context: Context
    private lateinit var inputFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        inputFile = File(context.cacheDir, "input.txt")
        val data = ByteArray(3 * 1024 * 1024) { 1 }
        FileOutputStream(inputFile).use { it.write(data) }
    }

    @After
    fun tearDown() {
        context.cacheDir.deleteRecursively()
    }

    @Test
    fun `GIVEN new file WHEN split THEN create parts and info json`() {
        // GIVEN
        val itemId = "item1"
        val chunkSizeMb = 1

        // WHEN
        val result = FileSeparatorUtil.splitFileToChach(
            context = context,
            inputFile = inputFile,
            itemId = itemId,
            chunkSize = chunkSizeMb
        )

        // THEN
        val itemDir = File(context.cacheDir, itemId)
        val jsonFile = File(itemDir, "info.json")

        assertTrue(itemDir.exists())
        assertTrue(jsonFile.exists())
        assertEquals(3, result.parts.size)
    }

    @Test
    fun `GIVEN valid cached data WHEN split called again THEN return old info`() {
        // GIVEN
        val itemId = "item2"
        val chunkSizeMb = 1

        FileSeparatorUtil.splitFileToChach(
            context,
            inputFile,
            itemId,
            chunkSizeMb
        )

        // WHEN
        val result = FileSeparatorUtil.splitFileToChach(
            context,
            inputFile,
            itemId,
            chunkSizeMb
        )

        // THEN
        assertEquals(3, result.parts.size)
        assertEquals(inputFile.absolutePath, result.orignalFilePath)
    }


    @Test
    fun `GIVEN existing cache WHEN chunk size changes THEN recreate cache`() {
        // GIVEN
        val itemId = "item3"

        FileSeparatorUtil.splitFileToChach(
            context,
            inputFile,
            itemId,
            1
        )

        // WHEN
        val result = FileSeparatorUtil.splitFileToChach(
            context,
            inputFile,
            itemId,
            2
        )

        // THEN
        assertEquals(2, result.parts.size)
    }

    @Test
    fun `GIVEN missing part file WHEN split THEN recreate cache`() {
        // GIVEN
        val itemId = "item4"
        val chunkSizeMb = 1

        val oldResult = FileSeparatorUtil.splitFileToChach(
            context,
            inputFile,
            itemId,
            chunkSizeMb
        )

        File(oldResult.parts[0]!!).delete()

        // WHEN
        val newResult = FileSeparatorUtil.splitFileToChach(
            context,
            inputFile,
            itemId,
            chunkSizeMb
        )

        // THEN
        assertEquals(3, newResult.parts.size)
    }

    @Test
    fun `GIVEN file smaller than chunk WHEN split THEN create single part`() {
        // GIVEN
        val smallFile = File(context.cacheDir, "small.txt")
        val data = ByteArray(1 * 1024 * 1024) { 1 }
        FileOutputStream(smallFile).use { it.write(data) }

        // WHEN
        val result = FileSeparatorUtil.splitFileToChach(
            context,
            smallFile,
            "smallItem",
            5
        )

        // THEN
        assertEquals(1, result.parts.size)
    }

    @Test
    fun `GIVEN file equals chunk size WHEN split THEN create single full part`() {
        // GIVEN
        val exactFile = File(context.cacheDir, "exact.txt")
        val data = ByteArray(2 * 1024 * 1024) { 1 }
        FileOutputStream(exactFile).use { it.write(data) }

        // WHEN
        val result = FileSeparatorUtil.splitFileToChach(
            context,
            exactFile,
            "exactItem",
            2
        )

        // THEN
        assertEquals(1, result.parts.size)
        assertEquals(
            2L * 1024 * 1024,
            File(result.parts[0]!!).length()
        )
    }

    @Test(expected = Exception::class)
    fun `GIVEN empty file WHEN split THEN no parts created`() {
        // GIVEN
        val emptyFile = File(context.cacheDir, "empty.txt")
        emptyFile.createNewFile()

       FileSeparatorUtil.splitFileToChach(
            context,
            emptyFile,
            "emptyItem",
            1
        )
    }

    @Test
    fun `GIVEN corrupted json WHEN split THEN recreate cache`() {
        // GIVEN
        val itemId = "corruptItem"

        val itemDir = File(context.cacheDir, itemId)
        itemDir.mkdirs()

        File(itemDir, "info.json").writeText("INVALID_JSON")

        // WHEN
        val result = FileSeparatorUtil.splitFileToChach(
            context,
            inputFile,
            itemId,
            1
        )

        // THEN
        assertEquals(3, result.parts.size)
    }

    @Test
    fun `GIVEN cache for different file WHEN split THEN recreate cache`() {
        // GIVEN
        val otherFile = File(context.cacheDir, "other.txt")
        val data = ByteArray(3 * 1024 * 1024) { 1 }
        FileOutputStream(otherFile).use { it.write(data) }

        FileSeparatorUtil.splitFileToChach(
            context,
            otherFile,
            "sameItem",
            1
        )

        // WHEN
        val result = FileSeparatorUtil.splitFileToChach(
            context,
            inputFile,
            "sameItem",
            1
        )

        // THEN
        assertEquals(3, result.parts.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN chunk size zero WHEN split THEN throw exception`() {
        FileSeparatorUtil.splitFileToChach(
            context,
            inputFile,
            "zeroChunk",
            0
        )
    }
}
