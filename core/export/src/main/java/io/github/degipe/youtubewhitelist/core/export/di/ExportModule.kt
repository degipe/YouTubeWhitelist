package io.github.degipe.youtubewhitelist.core.export.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.degipe.youtubewhitelist.core.export.ExportImportService
import io.github.degipe.youtubewhitelist.core.export.ExportImportServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExportModule {

    @Binds
    @Singleton
    abstract fun bindExportImportService(
        impl: ExportImportServiceImpl
    ): ExportImportService
}
