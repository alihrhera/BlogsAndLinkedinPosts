package hrhera.ali.backgroundsync.ui.worker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hrhera.ali.backgroundsync.util.FILE_PATH_KEY
import hrhera.ali.backgroundsync.util.FileSeparatorUtil.createDummyFile
import hrhera.ali.backgroundsync.util.ITEM_ID_KEY
import hrhera.ali.backgroundsync.util.PROGRESS_KEY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class UploadWorkerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    fun dummyFileWithSize(): File {
        return createDummyFile(context)
    }

    fun startUpload(itemId: String, filePath: String = "") {
        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(
                workDataOf(
                    ITEM_ID_KEY to itemId,
                    FILE_PATH_KEY to filePath
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                30,
                TimeUnit.SECONDS
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(
            itemId,
            ExistingWorkPolicy.REPLACE,
            request
        )
        workManager.getWorkInfoByIdLiveData(request.id)
            .observeForever { workInfo ->
                workInfo?.let {
                    if (it.state.isFinished) {
                        _progress.value = 100f
                    } else {
                        val prog = it.progress.getInt(PROGRESS_KEY, 0)
                        _progress.value = prog.toFloat()
                    }
                }
            }
    }


}

