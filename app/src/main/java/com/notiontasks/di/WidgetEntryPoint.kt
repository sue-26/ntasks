package com.notiontasks.di

import com.notiontasks.data.api.NotionTasksDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun database(): NotionTasksDatabase
}
