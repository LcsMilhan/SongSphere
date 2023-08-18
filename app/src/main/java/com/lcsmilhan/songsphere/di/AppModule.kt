package com.lcsmilhan.songsphere.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.lcsmilhan.songsphere.common.Constants.SONG_COLLECTION
import com.lcsmilhan.songsphere.data.repository.SongRepositoryImpl
import com.lcsmilhan.songsphere.domain.repository.SongRepository
import com.lcsmilhan.songsphere.service.notification.SongNotificationManager
import com.lcsmilhan.songsphere.service.player.SongServiceHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): MediaSession =
        MediaSession.Builder(context, player).build()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideSongCollection(firestore: FirebaseFirestore): CollectionReference {
        return firestore.collection(SONG_COLLECTION)
    }

    @Provides
    @Singleton
    fun provideSongRepository(
        songCollection: CollectionReference
    ): SongRepository = SongRepositoryImpl(songCollection)

    @Provides
    @Singleton
    @UnstableApi
    fun providePlayer(
        @ApplicationContext context: Context,
    ): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build()
        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setTrackSelector(DefaultTrackSelector(context))
            .build()
    }

    @Provides
    @Singleton
    fun provideSongNotificationManager(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): SongNotificationManager =
        SongNotificationManager(context, player)


    @Provides
    @Singleton
    fun provideSongServiceHandler(
        player: ExoPlayer
    ): SongServiceHandler = SongServiceHandler(player)

}