package com.golfpvcc.martytracker.di

import com.golfpvcc.martytracker.ui.history.HistoryViewModel
import com.golfpvcc.martytracker.ui.tracking.TrackingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::TrackingViewModel)
    viewModelOf(::HistoryViewModel)
}
