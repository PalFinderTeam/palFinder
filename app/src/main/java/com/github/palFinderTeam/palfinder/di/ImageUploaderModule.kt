package com.github.palFinderTeam.palfinder.di

import com.github.palFinderTeam.palfinder.utils.image.FirebaseImageUploader
import com.github.palFinderTeam.palfinder.utils.image.ImageUploader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/**
 * Inject imageUploader when needed, it makes certain class more testable.
 */
abstract class ImageUploaderModule {
    @Singleton
    @Binds
    abstract fun bindImageUploader(
        imageUploader: FirebaseImageUploader
    ): ImageUploader
}