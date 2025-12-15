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


}
