package io.github.degipe.youtubewhitelist.feature.parent.ui.exportimport

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.export.ExportImportService
import io.github.degipe.youtubewhitelist.core.export.ImportResult
import io.github.degipe.youtubewhitelist.core.export.ImportStrategy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExportImportViewModelTest {

    private lateinit var exportImportService: ExportImportService
    private val testDispatcher = StandardTestDispatcher()
    private val parentAccountId = "account-1"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        exportImportService = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ExportImportViewModel {
        return ExportImportViewModel(exportImportService, parentAccountId)
    }

    @Test
    fun `initial state is idle`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        with(viewModel.uiState.value) {
            assertThat(isExporting).isFalse()
            assertThat(isImporting).isFalse()
            assertThat(exportedJson).isNull()
            assertThat(importResult).isNull()
            assertThat(error).isNull()
        }
    }

    @Test
    fun `export success sets exportedJson`() = runTest(testDispatcher) {
        coEvery { exportImportService.exportToJson(parentAccountId) } returns
            AppResult.Success("{\"version\":1}")

        val viewModel = createViewModel()
        viewModel.export()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.exportedJson).isEqualTo("{\"version\":1}")
        assertThat(viewModel.uiState.value.isExporting).isFalse()
    }

    @Test
    fun `export error sets error message`() = runTest(testDispatcher) {
        coEvery { exportImportService.exportToJson(parentAccountId) } returns
            AppResult.Error("Export failed")

        val viewModel = createViewModel()
        viewModel.export()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isEqualTo("Export failed")
        assertThat(viewModel.uiState.value.exportedJson).isNull()
    }

    @Test
    fun `import merge success sets importResult`() = runTest(testDispatcher) {
        val importResult = ImportResult(profilesImported = 2, itemsImported = 5, itemsSkipped = 1)
        coEvery {
            exportImportService.importFromJson(parentAccountId, any(), ImportStrategy.MERGE)
        } returns AppResult.Success(importResult)

        val viewModel = createViewModel()
        viewModel.importData("{}", ImportStrategy.MERGE)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.importResult).isEqualTo(importResult)
        assertThat(viewModel.uiState.value.isImporting).isFalse()
    }

    @Test
    fun `import overwrite calls service with overwrite strategy`() = runTest(testDispatcher) {
        val importResult = ImportResult(profilesImported = 1, itemsImported = 3, itemsSkipped = 0)
        coEvery {
            exportImportService.importFromJson(parentAccountId, "{}", ImportStrategy.OVERWRITE)
        } returns AppResult.Success(importResult)

        val viewModel = createViewModel()
        viewModel.importData("{}", ImportStrategy.OVERWRITE)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { exportImportService.importFromJson(parentAccountId, "{}", ImportStrategy.OVERWRITE) }
        assertThat(viewModel.uiState.value.importResult).isEqualTo(importResult)
    }

    @Test
    fun `import error sets error message`() = runTest(testDispatcher) {
        coEvery {
            exportImportService.importFromJson(parentAccountId, any(), any())
        } returns AppResult.Error("Invalid JSON")

        val viewModel = createViewModel()
        viewModel.importData("bad", ImportStrategy.MERGE)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isEqualTo("Invalid JSON")
        assertThat(viewModel.uiState.value.importResult).isNull()
    }

    @Test
    fun `dismissError clears error`() = runTest(testDispatcher) {
        coEvery { exportImportService.exportToJson(parentAccountId) } returns
            AppResult.Error("fail")

        val viewModel = createViewModel()
        viewModel.export()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.dismissError()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `dismissResult clears importResult`() = runTest(testDispatcher) {
        val importResult = ImportResult(profilesImported = 1, itemsImported = 2, itemsSkipped = 0)
        coEvery {
            exportImportService.importFromJson(parentAccountId, any(), any())
        } returns AppResult.Success(importResult)

        val viewModel = createViewModel()
        viewModel.importData("{}", ImportStrategy.MERGE)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.dismissResult()
        assertThat(viewModel.uiState.value.importResult).isNull()
    }

    @Test
    fun `clearExportedJson clears exported data`() = runTest(testDispatcher) {
        coEvery { exportImportService.exportToJson(parentAccountId) } returns
            AppResult.Success("{}")

        val viewModel = createViewModel()
        viewModel.export()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearExportedJson()
        assertThat(viewModel.uiState.value.exportedJson).isNull()
    }

    @Test
    fun `export clears previous error`() = runTest(testDispatcher) {
        coEvery { exportImportService.exportToJson(parentAccountId) } returns
            AppResult.Success("{}")

        val viewModel = createViewModel()
        // Set an error state first
        viewModel.importData("bad", ImportStrategy.MERGE)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.export()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isNull()
    }
}
